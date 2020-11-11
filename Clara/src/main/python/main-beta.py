import sys
from py4j.java_gateway import JavaGateway
import subprocess
import os
import time
import socket
import rdflib
from rdflib import *
from pathlib import Path
from threading import *
from datetime import datetime
from lib.utils import *
import re

sys.path.insert(0, "./lib")


class Clara(Thread):
    def __init__(self, oasis, oasisabox, assistant, address, port):
        Thread.__init__(self)
        self.serversocket = None
        self.clara = None
        self.claraGateWay = None
        self.home = None
        self.oasis = oasis
        self.oasisabox = oasisabox
        self.assistant = ''
        self.host = address
        self.port = port
        self.alive = True
        self.restart = True
        self.iriassistant = assistant
        self.start()

    def run(self):
        self.init_gateway()
        print(open("amens/logo.txt", "r").read())
        # Adding HomeAssistant
        self.home = Utils.readOntoFile("ontologies/test/homeassistant.owl")
        if self.host is not None and self.port is not None:
            self.home = self.modifyConnection(self.home, self.host, self.port)
        self.assistant = self.clara.addDevice(self.home)  # read the device data
        if self.assistant is None:
            print("The assistant cannot be started")
            return
        sarray = self.clara.getConnectionInfo(self.assistant)
        self.host = sarray[0]
        self.port = sarray[1]
        print("Home assistant added:", self.assistant, "at ", self.host, self.port)
        self.init_server()

    def modifyConnection(self, home, host, port):
        g = Utils.getGraph(home)
        for s, o in g.subject_objects(predicate=URIRef(self.oasis + "hasPortNumber")):
            g.remove((s, URIRef(self.oasis + "hasPortNumber"), o))
            g.add((s, URIRef(self.oasis + "hasPortNumber"), Literal(port, datatype=XSD.integer)))

        for s, o in g.subject_objects(predicate=URIRef(self.oasis + "hasIPAddress")):
            g.remove((s, URIRef(self.oasis + "hasIPAddress"), o))
            g.add((s, URIRef(self.oasis + "hasIPAddress"), Literal(host, datatype=XSD.string)))
        return Utils.libbug(g, self.assistant)

    def setConnection(self, host, port):
        if Utils.checkAddress(host, port) == 0:
            print("Invalid port typed.")
            return 0
        if self.clara.modifyConnection(self.iriassistant, host, port) == 1:
            self.host = host
            self.port = port
            self.alive = False
            # self.serversocket.shutdown(socket.SHUT_WR)
            self.serversocket.close()
            time.sleep(2)
            return 1
        else:
            return 0

    def stop(self):
        self.alive = False
        self.restart = False
        print("Server is closing. Wait.")
        try:
            self.serversocket.close()
            self.claraGateWay.close()
            self.claraGateWay.shutdown()
        except Exception as e:
            print("Disconnected.")
            return
        return

    def init_server(self):
        print("CLARA Assistant has been started")
        while self.restart:
            print("CLARA Assistant is listening:", self.host, "port ", self.port)
            self.serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.serversocket.bind((self.host, int(self.port)))
            self.serversocket.listen(5)
            self.alive = True
            while self.alive:
                try:
                    clients, address = self.serversocket.accept()
                    request = Utils.recvall(clients).decode()
                    self.decide(request, address[1], address[0], clients)
                except Exception as e:
                    print("Disconnected.")
                    break
            # client(self, clients, address, serversocket)
        return

    def addExecutionStatusToDataBelief(self, executer, execution, status):
        g = Graph()
        sta = URIRef(execution + "-status")
        g.add((execution, URIRef(self.oasis + "hasStatus"), sta))
        g.add((URIRef(self.oasis + "hasStatus"), RDF.type, Utils.owlobj))
        g.add((sta, URIRef(self.oasis + "hasStatusType"), URIRef(status)))
        g.add((URIRef(self.oasis + "hasStatusType"), RDF.type, Utils.owlobj))
        g.add((URIRef(executer), URIRef(self.oasis + "performs"), URIRef(execution)))
        g.add((URIRef(self.oasis + "performs"), RDF.type, Utils.owlobj))
        self.clara.addDataBelief(g.serialize(format='xml').decode())
        return

    def transmitExecutionStatus(self, execution, executer, status, addr, sock, server_socket):
        g = Graph()
        timestamp = Utils.getTimeStamp()
        iri = Utils.retrieveURI(execution).replace(".owl", "-response" + timestamp + ".owl")
        iriassist = self.iriassistant + '#' + self.assistant
        Utils.generateExecutionStatus(g, URIRef(iriassist), execution, executer, status, iri, self.oasis,
                                      self.oasisabox, iriassist, timestamp)
        res = Utils.serverTransmit(g.serialize(format='pretty-xml'), sock, addr, server_socket)
        server_socket.close()
        self.addExecutionStatusToDataBelief(iriassist, execution, status)
        return res

    def computesDependencies(self, graph, executions):
        for first, second in graph.subject_objects(predicate=URIRef(self.oasis + "dependsOn")):
            index = 0
            while index < len(executions):
                key, value = executions[index]
                if second == key:
                    executions[index] = (key, value + 1)
                index += 1

    def getOntologyFile(self, graph, execution):
        file = None
        for d in graph.objects(execution, URIRef(self.oasis + "hasTaskActualInputParameter")):  # retrieving source
            for t in graph.objects(d, URIRef(self.oasis + "refersAsNewTo")):
                for s in graph.objects(t, URIRef(self.oasis + "descriptionProvidedByURL")):
                    if s is not None:
                        file = Utils.readOntoFile(s)

                for s in graph.objects(t, URIRef(self.oasis + "descriptionProvidedByIRI")):
                    if s is not None:
                        file = s

                g = Graph()
                if file is not None:
                    for h in graph.objects(execution, URIRef(self.oasis + "descriptionProvidedByEntityIRI")):
                        retrieveTripleFromGraph(h, file, g)
                if len(g) > 0:
                    return g
        return file

    def device_engage(self, graph, iri, execution, performer, timestamp):
        # for s,p,o in graph.triples( (None,None,None) ):
        # print(s,p,o)
        # performer = next(graph.objects(entrust, URIRef(self.oasis + "entrusts")))
        toreturn = Graph()
        iriassist = self.iriassistant + '#' + self.assistant
        taskObj = next(graph.objects(subject=None, predicate=URIRef(self.oasis + "performsEntrustment")))
        Utils.generateRequest(toreturn, iri, self.oasis, iriassist, URIRef(performer),
                              "#planDe-" + timestamp, "#goalDe-" + timestamp, "#taskDe-" + timestamp,
                              "#taskOb-" + timestamp, URIRef(taskObj), URIRef(self.oasis + "refersExactlyTo"),
                              URIRef(self.oasisabox + "perform"), None, None, timestamp)

        devip = next(graph.objects(subject=None, predicate=URIRef(self.oasis + "hasIPAddress")))
        devport = next(graph.objects(subject=None, predicate=URIRef(self.oasis + "hasPortNumber")))
        value = 100
        # entrust = next(graph.subjects(URIRef(self.oasis + "entrustedWith"), execution))
        taskOb = next(graph.objects(execution, URIRef(self.oasis + "hasTaskObject")))
        taskOp = next(graph.objects(execution, URIRef(self.oasis + "hasTaskOperator")))
        taskObject = next(graph.objects(taskOb, URIRef(self.oasis + "refersExactlyTo")))
        taskOperator = next(graph.objects(taskOp, URIRef(self.oasis + "refersExactlyTo")))
        for s, t in graph.subject_objects(URIRef(self.oasis + "hasTaskActualInputParameter")):
            o = next(graph.objects(t, URIRef(self.oasis + "refersAsNewTo")))  # check also for refersExactlyTo
            for v in graph.objects(o, URIRef(self.oasis + "hasDataValue")):
                value = v
                break

        print("To engage:", performer, taskObject, value, taskOperator, devip, devport)
        toret = (toreturn + graph).serialize(format='xml')
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect((devip, int(devport)))
        client_socket.sendall(toret)
        message = Utils.recvall(client_socket)
        client_socket.close()
        return message, toreturn

    # Actions that the assistant performs
    def profhome_decide(self, graph, execution, executer, addr, sock, server_socket):
        req = next(graph.objects(execution, URIRef(self.oasis + "hasTaskObject")))
        requester = next(graph.objects(req, URIRef(self.oasis + "refersExactlyTo")))
        operator = next(graph.objects(execution, URIRef(self.oasis + "hasTaskOperator")))
        for actions in graph.objects(operator, URIRef(self.oasis + "refersExactlyTo")):
            res = 0
            if actions == URIRef(self.oasisabox + "install"):
                file = self.getOntologyFile(graph, execution)
                value = self.clara.addDevice(file)  # read the device data
                if value is None or value == "":
                    print("A device cannot be added")
                    res = self.transmitExecutionStatus(execution, executer,
                                                       URIRef(self.oasisabox + "failed_status_type"), addr, sock,
                                                       server_socket)
                else:
                    print("Device", value, "added.")
                    res = self.transmitExecutionStatus(execution, executer,
                                                       URIRef(self.oasisabox + "succeeded_status_type"), addr, sock,
                                                       server_socket)

            elif actions == URIRef(self.oasisabox + "update"):
                file = self.getOntologyFile(graph, execution)
                value = self.clara.modifyDevice(file)  # read the device data
                if value is None:
                    print("A device cannot be updated")
                    res = self.transmitExecutionStatus(execution, executer,
                                                       URIRef(self.oasisabox + "failed_status_type"), addr, sock,
                                                       server_socket)
                else:
                    print("Device", value, "updated.")
                    res = self.transmitExecutionStatus(execution, executer,
                                                       URIRef(self.oasisabox + "succeeded_status_type"), addr, sock,
                                                       server_socket)

            elif actions == URIRef(self.oasisabox + "uninstall"):  # uninstallation task
                # requester = next(graph.objects(execution, URIRef(oasis + "hasTaskObject")))
                value = self.clara.removeDevice(Utils.retrieveEntityName(requester))  # read the device data
                if int(value) < 1:
                    print("Device", Utils.retrieveEntityName(requester) + " cannot be removed")
                    res = self.transmitExecutionStatus(execution, executer,
                                                       URIRef(self.oasisabox + "failed_status_type"), addr, sock,
                                                       server_socket)
                else:
                    print("Device", Utils.retrieveEntityName(requester), "correctly removed")
                    res = self.transmitExecutionStatus(execution, executer,
                                                       URIRef(self.oasisabox + "succeeded_status_type"), addr, sock,
                                                       server_socket)

            elif actions == URIRef(self.oasisabox + "add") or actions == URIRef(
                    self.oasisabox + "remove"):  # add user task

                for thetype in graph.objects(requester, URIRef(self.oasis + "hasType")):
                    if thetype == URIRef(self.oasisabox + "user_type"):  # adding or removing user
                        if actions == URIRef(self.oasisabox + "add"):
                            file = self.getOntologyFile(graph, execution)
                            value = self.clara.addUser(file)
                            if value is None:
                                print("A user cannot be added")
                                res = self.transmitExecutionStatus(execution, executer,
                                                                   URIRef(self.oasisabox + "failed_status_type"), addr,
                                                                   sock,
                                                                   server_socket)
                            else:
                                res = self.transmitExecutionStatus(execution, executer,
                                                                   URIRef(self.oasisabox + "succeeded_status_type"),
                                                                   addr, sock, server_socket)
                                print("User", value, "added.")
                        elif actions == URIRef(self.oasisabox + "remove"):
                            value = self.clara.removeUser(Utils.retrieveEntityName(requester))
                            if int(value) < 1:
                                print("User " + Utils.retrieveEntityName(requester) + " cannot be removed")
                                res = self.transmitExecutionStatus(execution, executer,
                                                                   URIRef(self.oasisabox + "failed_status_type"), addr,
                                                                   sock,
                                                                   server_socket)
                            else:
                                res = self.transmitExecutionStatus(execution, executer,
                                                                   URIRef(self.oasisabox + "succeeded_status_type"),
                                                                   addr, sock, server_socket)
                                print("User", Utils.retrieveEntityName(requester), "correctly removed")
                    elif thetype == URIRef(self.oasisabox + "user_configuration_type"):  # adding or removing user
                        if actions == URIRef(self.oasisabox + "add"):
                            file = self.getOntologyFile(graph, execution)
                            value = self.clara.addConfiguration(file)
                            if value is None:
                                print("A configuration cannot be added")
                                res = self.transmitExecutionStatus(execution, executer,
                                                                   URIRef(self.oasisabox + "failed_status_type"), addr,
                                                                   sock,
                                                                   server_socket)
                            else:
                                res = self.transmitExecutionStatus(execution, executer,
                                                                   URIRef(self.oasisabox + "succeeded_status_type"),
                                                                   addr, sock, server_socket)
                                print("Configuration added:", value, ".")
                        elif actions == URIRef(self.oasisabox + "remove"):
                            value = self.clara.removeConfiguration(Utils.retrieveEntityName(requester))
                            if int(value) < 1:
                                print("A configuration cannot be removed")
                                res = self.transmitExecutionStatus(execution, executer,
                                                                   URIRef(self.oasisabox + "failed_status_type"), addr,
                                                                   sock,
                                                                   server_socket)
                            else:
                                res = self.transmitExecutionStatus(execution, executer,
                                                                   URIRef(self.oasisabox + "succeeded_status_type"),
                                                                   addr, sock, server_socket)
                                print("Configuration", Utils.retrieveEntityName(requester), "correctly removed.")
                    elif thetype == URIRef(self.oasisabox + "belief_description_object_type"):
                        file = self.getOntologyFile(graph, execution)
                        if actions == URIRef(self.oasisabox + "add"):
                            value = self.clara.addDataBelief(file)
                            if int(value) < 1:
                                print("Data belief cannot be added")
                                res = self.transmitExecutionStatus(execution, executer,
                                                                   URIRef(self.oasisabox + "failed_status_type"), addr,
                                                                   sock,
                                                                   server_socket)
                            else:
                                res = self.transmitExecutionStatus(execution, executer,
                                                                   URIRef(self.oasisabox + "succeeded_status_type"),
                                                                   addr, sock, server_socket)
                                print("Belief  correctly added")
                        elif actions == URIRef(self.oasisabox + "remove"):
                            value = self.clara.removeDataBelief(file)
                            if int(value) < 1:
                                print("Belief cannot be removed")
                                res = self.transmitExecutionStatus(execution, executer,
                                                                   URIRef(self.oasisabox + "failed_status_type"), addr,
                                                                   sock,
                                                                   server_socket)
                            else:
                                res = self.transmitExecutionStatus(execution, executer,
                                                                   URIRef(self.oasisabox + "succeeded_status_type"),
                                                                   addr, sock, server_socket)
                                print("Belief correctly removed")
                    else:
                        res = self.transmitExecutionStatus(execution, executer,
                                                           URIRef(self.oasisabox + "failed_status_type"), addr, sock,
                                                           server_socket)

            elif actions == URIRef(self.oasisabox + "parse"):
                for thetype in graph.objects(requester, URIRef(self.oasis + "hasType")):
                    if thetype == URIRef(self.oasisabox + "generalUtterance"):
                        print("General utterances parser is being developed... stay tuned!")
                        res = self.transmitExecutionStatus(execution, executer,
                                                           URIRef(self.oasisabox + "failed_status_type"), addr, sock,
                                                           server_socket)
                    else:
                        res = self.transmitExecutionStatus(execution, executer,
                                                           URIRef(self.oasisabox + "failed_status_type"), addr, sock,
                                                           server_socket)

            elif actions == URIRef(self.oasisabox + "retrieve"):
                for thetype in graph.objects(requester, URIRef(self.oasis + "hasType")):
                    if thetype == URIRef(self.oasisabox + "belief_description_object_type"):
                        file = self.getOntologyFile(graph, execution)
                        value = self.clara.retrieveDataBelief(file)
                        if value is None:
                            print("Belief cannot be retrieved")
                            res = self.transmitExecutionStatus(execution, executer,
                                                               URIRef(self.oasisabox + "failed_status_type"), addr,
                                                               sock,
                                                               server_socket)
                        else:
                            res = self.transmitExecutionStatus(execution, executer,
                                                               URIRef(self.oasisabox + "succeeded_status_type"), addr,
                                                               sock, server_socket)
                            print("Belief retrieved:\n" + value)
                    else:
                        res = self.transmitExecutionStatus(execution, executer,
                                                           URIRef(self.oasisabox + "failed_status_type"), addr, sock,
                                                           server_socket)
            elif actions == URIRef(self.oasisabox + "check"):
                for arg in graph.objects(execution, URIRef(self.oasis + "hasTaskOperatorArgument")):
                    argument = next(graph.objects(arg, URIRef(self.oasis + "refersExactlyTo")))
                    if argument == URIRef(self.oasisabox + "installation"):
                        print("Checking for the presence of ", requester)
                        isPresent = self.clara.checkDevice(requester)
                        if isPresent == 1:
                            res = self.transmitExecutionStatus(execution, executer,
                                                               URIRef(self.oasisabox + "succeeded_status_type"), addr,
                                                               sock,
                                                               server_socket)
                            print("Device ", requester, " is installed")
                        else:
                            res = self.transmitExecutionStatus(execution, executer,
                                                               URIRef(self.oasisabox + "failed_status_type"), addr,
                                                               sock,
                                                               server_socket)
                            print("Device ", requester, " is not installed")
                    else:
                        res = self.transmitExecutionStatus(execution, executer,
                                                           URIRef(self.oasisabox + "failed_status_type"), addr, sock,
                                                           server_socket)
            else:
                print("Action", actions, "not supported yet")
                res = self.transmitExecutionStatus(execution, executer, URIRef(self.oasisabox + "failed_status_type"),
                                                   addr, sock, server_socket)

            if res < 1:
                print("Execution status cannot be transmitted")

    def decide(self, rdf_source, sock, addr, server_socket):
        value = self.clara.parseRequest(rdf_source)[0]
        if value is None:
            print("Received data from " + str(addr) + " " + str(sock))
            return
        g = Utils.getGraph(value)
        executions = []
        for execution in g.subjects(RDF.type, URIRef(self.oasis + "TaskExecution")):
            executions.append((execution, 0))

        if len(executions) > 1:
            self.computesDependencies(g, executions)
            executions = sorted(executions, key=lambda x: x[1])
        for execution, val in executions:
            for taskEntrust in g.subjects(URIRef(self.oasis + "entrustedWith"), execution):
                for executer in g.objects(taskEntrust, URIRef(self.oasis + "entrusts")):
                    if Utils.retrieveEntityName(executer) == self.assistant:
                        self.profhome_decide(g, execution, executer, addr, sock, server_socket)

                    else:
                        timestamp = Utils.getTimeStamp()
                        iri = str(self.iriassistant).rsplit('.', 1)[0] + "-request" + timestamp + ".owl"
                        message, reqG = self.device_engage(g, iri, execution, executer, timestamp)
                        Utils.serverTransmit(message, sock, addr, server_socket)
                        belief = self.clara.parseRequest(message.decode())[0]  # direct request
                        entrust_status = URIRef(self.oasisabox + "failed_status_type")
                        if belief is None:
                            print("A belief from " + execution + " cannot be added")
                        else:
                            entrust_status = URIRef(self.oasisabox + "succeeded_status_type")
                        s = Graph()
                        # original request status and perform
                        Utils.addPerformInfo(s, self.oasis, executer, execution)
                        self.clara.addDataBelief(s.serialize(format='xml').decode())
                        self.addExecutionStatusToDataBelief(executer, execution, entrust_status)
                        # engagement request status and perform
                        ex = Utils.addExecutionGraph(reqG, self.oasis, iri, executer, timestamp)
                        self.clara.addDataRequest(reqG.serialize(format='xml').decode())
                        s = Graph()
                        Utils.addPerformInfo(s, self.oasis, executer, ex)
                        self.addExecutionStatusToDataBelief(executer, ex, entrust_status)
                        # belief update solo request status and perform
                        s = Utils.getGraph(message)
                        ex = next(s.subjects(RDF.type, URIRef(self.oasis + "TaskExecution")))
                        Utils.addPerformInfo(s, self.oasis, URIRef(self.iriassistant + '#' + self.assistant), ex)
                        self.addExecutionStatusToDataBelief(URIRef(self.iriassistant + '#' + self.assistant), ex,
                                                            entrust_status)
                        print("Engagement status: ", entrust_status)

    def extractStatusInfo(self, belief):
        g = Graph()
        for s, t in belief.subject_objects(predicate=URIRef(self.oasis + "hasStatus")):
            g.add((s, URIRef(self.oasis + "hasStatus"), t))
            for f, c in belief.predicate_objects(subject=t):
                g.add((t, f, c))
        return g

    def init_gateway(self):
        # p = Path(__file__).parents[1]
        # os.chdir(p)
        folder = 'ontologies/devices'
        for the_file in os.listdir(folder):
            file_path = os.path.join(folder, the_file)
            try:
                if os.path.isfile(file_path):
                    os.unlink(file_path)
                # elif os.path.isdir(file_path): shutil.rmtree(file_path)
            except Exception as e:
                print(e)
        jar = "java -jar CLARA-1.0-SNAPSHOT.jar"
        process = subprocess.Popen(jar, universal_newlines=True, stdout=subprocess.PIPE)
        # stdout, stderr = process.communicate()
        print(self.getProcessOut(process))
        self.claraGateWay = JavaGateway()  # connect to the JVM
        self.clara = self.claraGateWay.entry_point

    @staticmethod
    def getProcessOut(process):
        message = ''
        while True:
            out = process.stdout.read(1)
            if out != '\n':
                message += out
            else:
                break
        return message


class Console(Thread):
    def __init__(self):
        Thread.__init__(self)
        self.start()
        return

    @staticmethod
    def start_command(address, port):
        return Clara('http://www.dmi.unict.it/oasis.owl#', 'http://www.dmi.unict.it/oasis-abox.owl#',
                        'http://www.dmi.unict.it/homeassistant.owl', address, port)

    @staticmethod
    def stop_command(agent):
        agent.stop()
        return True

    def exit_command(self, agent):
        self.stop_command(agent)
        print("Console closing. Goodbye.")
        return False

    @staticmethod
    def setTestPath():
        p = Path(__file__).parents[1]
        os.chdir(p)
        return

    @staticmethod
    def set_connection(agent, host, port):
        return agent.setConnection(host, port)

    def run(self):
        self.setTestPath()
        agent = None
        exec_status = True
        while exec_status:
            print("Enter a command:  ---> ", end='')
            command = input('').strip()
            if command.startswith("start"):
                parms = command.split()
                if len(parms) == 1:
                    agent = self.start_command(None, None)  # default address, port
                elif len(parms) == 3:
                    agent = self.start_command(parms[1], parms[2])
                else:
                    print("Use: start | start address port")
            elif agent is not None:
                if command == "stop":
                    self.stop_command(agent)
                elif command == "exit":
                    exec_status = self.exit_command(agent)
                elif command.startswith("set"):
                    parms = command.split()
                    if len(parms) == 3:
                        exec_status = self.set_connection(agent, parms[1], parms[2])
                        if exec_status == 1:
                            print("Connection successifully modified")
                        else:
                            print("Connection cannot be modified")
                    else:
                        print("Use: set address port")
                else:
                    print("Unrecognized command")
                    print("Use start [address] [port] | stop | exit | set address port")
            else:
                print("Start the agent first. Use: start | start address port")
            time.sleep(2)
        return


def main():
    Console()


if __name__ == '__main__':
    main()
