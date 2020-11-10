import os
import re
import socket
import time
from datetime import datetime
from pathlib import Path
from threading import *

import rdflib
from lib.utils import *
from rdflib import *


# current date and time


class AgentServerManager(Thread):
    def __init__(self):
        super().__init__()
        self.sock = None
        self.addr = None
        self.agent = None
        return

    def launchServer(self, sockets, address, agent):
        Thread.__init__(self)
        self.sock = sockets
        self.addr = address
        self.agent = agent
        self.start()

    def performOperation(self, request, execution):
        raise NotImplementedError()
        # print("\n Action ", taskOperator, "on ", taskObject)
        # execution = next(g.subjects(RDF.type, URIRef(self.agent.iriSet[0] + "#TaskExecution")))
        # taskObject = next(g.objects(execution, URIRef(self.agent.iriSet[0] + "#hasTaskObject")))
        # taskOperator = next(g.objects(execution, URIRef(self.agent.iriSet[0] + "#hasTaskOperator")))
        # return

    def response(self, request):
        g = rdflib.Graph()
        g.parse(data=request)
        execution = None
        for s in g.subjects(URIRef(self.agent.iriSet[0] + "#entrusts"),
                            URIRef(self.agent.iriSet[2] + "#" + self.agent.agentInfo[0])):
            for t in g.objects(s, URIRef(self.agent.iriSet[0] + "#entrustedWith")):
                if (t, RDF.type, URIRef(self.agent.iriSet[0] + "#TaskExecution")) in g:
                    execution = t
        if execution is None:
            return 0
        self.performOperation(g, execution)
        #
        status = self.agent.iriSet[0] + "#" + "succeded_status_type"
        timestamp = Utils.getTimeStamp()
        iri = str(execution).rsplit('.', 1)[0] + "-updatestatus" + timestamp + ".owl"
        agent = URIRef(self.agent.iriSet[2] + "#" + self.agent.agentInfo[0])
        s = Graph()
        Utils.addImportAxioms(s, iri, [self.agent.iriSet[0], self.agent.iriSet[1]])
        Utils.generateExecutionStatus(s, agent, execution, None, status, iri, self.agent.iriSet[0] + "#",
                                      self.agent.iriSet[1] + "#", agent, timestamp)

        tosend = s.serialize(format='pretty-xml').decode()
        self.sock.sendall(tosend.encode())
        return 1

    def run(self):
        request = Utils.recvall(self.sock)
        self.response(request)
        return


class Agent(Thread):
    def __init__(self, servermanager, path, templates, iriAgent, iriTemplate):
        super().__init__()
        self.servermanager = servermanager
        self.alive = False
        self.restart = True
        self.serversocket = None
        self.address = None
        self.port = None
        # declare class members
        self.graphSet = [Graph(), Graph()]
        self.iriSet = ['http://www.dmi.unict.it/oasis.owl', 'http://www.dmi.unict.it/oasis-abox.owl', iriAgent,
                       iriTemplate]  # 2->agent 3->template
        self.agentInfo = ['', '', '']  # 0->short name, 1->host, 2->port
        self.hubInfo = ['', '']  # 0->address, 1-> port
        # graphSet = ['', '', '']  # 0->Agent,  1->Templates
        self.setAgentTemplates(templates)
        self.setAgentOntology(path)
        # self.setAgentConfiguration(configuration)
        self.setAgentIRIs(self.graphSet[0])
        # end declare
        # set agent graphs

        # end set
        # self.start()

    def isAlive(self):
        return self.alive

    def startAgent(self):
        Thread.__init__(self)
        self.start()

    def stop(self):
        self.alive = False
        self.restart = False
        self.serversocket.close()
        print("Server is closing. Wait...")
        return

    def setAgentTemplates(self, templates):
        self.graphSet[1] = Graph()
        for tem in templates:
            if len(self.graphSet[1]) == 0:
                self.graphSet[1] = Utils.getGraph(Utils.readOntoFile(tem))
            else:
                self.graphSet[1] += Utils.getGraph(Utils.readOntoFile(tem))
        return

    def setAgentOntology(self, path):
        self.graphSet[0] = Utils.getGraph(Utils.readOntoFile(path))
        return

    # def setAgentConfiguration(self, path):
    #     self.graphSet[1] = self.getGraph(self.readOntoFile(path),self.iriSet[4])
    #     return

    def setAgentConnection(self, address, port):
        self.address = address
        self.port = port
        self.setAgentConnectionInfo(self.address, self.port, self.graphSet[0])

    def setAgentConnectionInfo(self, address, port, graph):
        the_port = ''
        the_address = ''
        the_connection = None
        for agent, connection in graph.subject_objects(predicate=URIRef(self.iriSet[0] + "#hasConnection")):
            for propertyp, data in graph.predicate_objects(subject=connection):
                if propertyp == URIRef(self.iriSet[0] + "#hasIPAddress"):
                    the_address = data
                elif propertyp == URIRef(self.iriSet[0] + "#hasPortNumber"):
                    the_port = data
                the_connection = connection
        if port is not None and address is not None:
            graph.remove((the_connection, URIRef(self.iriSet[0] + "#hasPortNumber"), None))
            graph.remove((the_connection, URIRef(self.iriSet[0] + "#hasIPAddress"), None))
            graph.add((the_connection, URIRef(self.iriSet[0] + "#hasIPAddress"), Literal(address, datatype=XSD.string)))
            graph.add((the_connection, URIRef(self.iriSet[0] + "#hasPortNumber"), Literal(port, datatype=XSD.integer)))
            self.agentInfo[1] = address
            self.agentInfo[2] = port
        else:
            self.agentInfo[1] = the_address
            self.agentInfo[2] = the_port
        return 1

    def setAgentIRIs(self, graph):
        for agent in graph.subjects(predicate=RDF.type, object=URIRef(self.iriSet[0] + '#Device')):
            names = str(agent).split('#')
            self.agentInfo[0] = names[1]
            break
        return

    def check_install(self):
        if self.hubInfo[0] == '' or self.hubInfo[1] == '':
            return 0
        timestamp = Utils.getTimeStamp()
        reqGraph = rdflib.Graph()
        iri = str(self.iriSet[2]).rsplit('.', 1)[0] + "-request" + timestamp + ".owl"
        Utils.addImportAxioms(reqGraph, iri, [self.iriSet[0], self.iriSet[1]])
        reqGraph.add((URIRef(iri), RDF.type, OWL.Ontology))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[0])))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[1])))

        agent = URIRef(self.iriSet[2] + "#" + self.agentInfo[0])
        reqGraph.add(
            (agent, URIRef(self.iriSet[0] + "#hasAgentType"),
             URIRef(self.iriSet[1] + "#agent_device_type")))  # task object
        reqGraph.add((agent, RDF.type, URIRef(self.iriSet[0] + "#Device")))  # has request
        Utils.generateRequest(reqGraph, iri, self.iriSet[0] + "#", agent, None, "#planDe-" + timestamp,
                              "#goalDe-" + timestamp, "#taskDe-" + timestamp, "#taskObj-" + timestamp, agent,
                              self.iriSet[0] + "#refersExactlyTo", URIRef(self.iriSet[1] + "#check"),
                              URIRef(self.iriSet[1] + "#installation"), None, timestamp)
        tosend = Utils.libbug(reqGraph, iri)  # transmits config solving the rdflib bug of xml:base

        received = Utils.transmit(tosend.encode(), True, self.hubInfo[0], self.hubInfo[1])
        if received is None:
            return 0
        g = rdflib.Graph()
        g.parse(data=received)
        if Utils.checkStatus(g, self.iriSet[0] + "#", self.iriSet[1] + "#succeded_status_type") == 1:
            return 1
        return 0

    def install_device(self):
        if self.hubInfo[0] == '' or self.hubInfo[1] == '':
            return 0
        timestamp = Utils.getTimeStamp()

        tosend = Utils.libbug(self.graphSet[1],
                              self.iriSet[3])  # transmits template solving the rdflib bug of xml:base
        state = Utils.transmit(tosend.encode(), False, self.hubInfo[0], self.hubInfo[1])
        if state is None:
            return 0

        tosend = Utils.libbug(self.graphSet[0],
                              self.iriSet[2])  # transmits behavior solving the rdflib bug of xml:base
        state = Utils.transmit(tosend.encode(), False, self.hubInfo[0], self.hubInfo[1])
        if state is None:
            return 0
        reqGraph = rdflib.Graph()

        iri = str(self.iriSet[2]).rsplit('.', 1)[0] + "-request" + timestamp + ".owl"
        Utils.addImportAxioms(reqGraph, iri, [self.iriSet[0], self.iriSet[1]])
        reqGraph.add((URIRef(iri), RDF.type, OWL.Ontology))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[0])))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[1])))
        # if(self.iriSet[4] != ''):
        #    reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[4])))

        agent = URIRef(self.iriSet[2] + "#" + self.agentInfo[0])
        reqGraph.add((agent, RDF.type, URIRef(self.iriSet[0] + "#Device")))  # has request
        reqGraph.add(
            (agent, URIRef(self.iriSet[0] + "#hasAgentType"),
             URIRef(self.iriSet[1] + "#agent_device_type")))  # task object
        parameter = URIRef(iri + "#parameter")  # the parameter
        Utils.generateRequest(reqGraph, iri, self.iriSet[0] + "#", agent, None, "#aPlanDe-" + timestamp,
                              "#aGoalDe-" + timestamp, "#aTaskDe-" + timestamp, "#aTaskObj-" + timestamp, agent,
                              self.iriSet[0] + "#refersExactlyTo", URIRef(self.iriSet[1] + "#install"),
                              None, parameter, timestamp)

        reqGraph.add((parameter, RDF.type, URIRef(self.iriSet[0] + "#OntologyDescriptionObject")))

        reqGraph.add((URIRef(self.iriSet[0] + "#hasInformationObjectType"), RDF.type, Utils.owlobj))
        reqGraph.add((parameter, URIRef(self.iriSet[0] + "#hasInformationObjectType"),
                      URIRef(self.iriSet[1] + "#ontology_description_object_type")))

        reqGraph.add((URIRef(self.iriSet[0] + "#descriptionProvidedByIRI"), RDF.type, Utils.owldat))
        reqGraph.add((parameter, URIRef(self.iriSet[0] + "#descriptionProvidedByIRI"),
                      Literal(self.iriSet[2], datatype=XSD.string)))

        tosend = Utils.libbug(reqGraph, iri)  # transmits config solving the rdflib bug of xml:base

        received = Utils.transmit(tosend.encode(), True, self.hubInfo[0], self.hubInfo[1])
        if received is None:
            return 0
        g = rdflib.Graph()
        g.parse(data=received)
        if Utils.checkStatus(g, self.iriSet[0] + "#", self.iriSet[1] + "#succeded_status_type") == 1:
            print("Device installation confirmed by the hub")
        else:
            print("Device installation not confirmed by the hub")
            return 0
        # f=open("test.owl", "w")
        # f.write(g.serialize(format="pretty-xml").decode())
        return 1

    def uninstall_device(self):
        if self.hubInfo[0] == '' or self.hubInfo[1] == '':
            return 0
        timestamp = Utils.getTimeStamp()
        reqGraph = rdflib.Graph()
        iri = str(self.iriSet[2]).rsplit('.', 1)[0] + "-request" + timestamp + ".owl"
        Utils.addImportAxioms(reqGraph, iri, [self.iriSet[0], self.iriSet[1]])
        reqGraph.add((URIRef(iri), RDF.type, OWL.Ontology))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[0])))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[1])))
        # if(self.iriSet[4] != ''):
        #    reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[4])))

        agent = URIRef(self.iriSet[2] + "#" + self.agentInfo[0])
        reqGraph.add(
            (agent, URIRef(self.iriSet[0] + "#hasAgentType"),
             URIRef(self.iriSet[1] + "#agent_device_type")))  # task object
        reqGraph.add((agent, RDF.type, URIRef(self.iriSet[0] + "#Device")))  # has request
        Utils.generateRequest(reqGraph, iri, self.iriSet[0] + "#", agent, None, "#planDe-" + timestamp,
                              "#goalDe-" + timestamp, "#taskDe-" + timestamp, "#taskObj-" + timestamp, agent,
                              self.iriSet[0] + "#refersExactlyTo", URIRef(self.iriSet[1] + "#uninstall"),
                              None, None, timestamp)

        tosend = Utils.libbug(reqGraph, iri)  # transmits config solving the rdflib bug of xml:base

        received = Utils.transmit(tosend.encode(), True, self.hubInfo[0], self.hubInfo[1])
        if received is None:
            return 0
        g = rdflib.Graph()
        g.parse(data=received)

        if Utils.checkStatus(g, self.iriSet[0] + "#", self.iriSet[1] + "#succeded_status_type") == 1:
            print("Device uninstallation confirmed by the hub")
        else:
            print("Device uninstallation not confirmed by the hub")
            return 0
        # f=open("test.owl", "w")
        # f.write(tosend)
        return 1

    def set_hub(self, host, port):
        self.hubInfo[0] = host
        self.hubInfo[1] = port
        return 1

    def set_connection(self, host, port):
        if self.hubInfo[0] == '' or self.hubInfo[1] == '':
            return 0

        self.agentInfo[1] = host
        self.agentInfo[2] = port
        self.alive = False
        self.serversocket.close()
        time.sleep(2)

        timestamp = Utils.getTimeStamp()

        self.setAgentConnectionInfo(host, port, self.graphSet[0])

        tosend = Utils.libbug(self.graphSet[0],
                              self.iriSet[2])  # transmits behavior solving the rdflib bug of xml:base
        state = Utils.transmit(tosend.encode(), False, self.hubInfo[0], self.hubInfo[1])
        if state is None:
            return 0
        reqGraph = rdflib.Graph()
        iri = str(self.iriSet[2]).rsplit('.', 1)[0] + "-request" + timestamp + ".owl"
        Utils.addImportAxioms(reqGraph, iri, [self.iriSet[0], self.iriSet[1]])
        reqGraph.add((URIRef(iri), RDF.type, OWL.Ontology))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[0])))
        reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[1])))
        # if(self.iriSet[4] != ''):
        #    reqGraph.add((URIRef(iri), OWL.imports, URIRef(self.iriSet[4])))

        agent = URIRef(self.iriSet[2] + "#" + self.agentInfo[0])
        reqGraph.add(
            (agent, URIRef(self.iriSet[0] + "#hasType"), URIRef(self.iriSet[1] + "#device_type")))  # task object
        reqGraph.add((agent, RDF.type, URIRef(self.iriSet[0] + "#Device")))  # has request
        parameter = URIRef(iri + "#parameter")  # the parameter
        Utils.generateRequest(reqGraph, iri, self.iriSet[0] + "#", agent, None, "#planDe-" + timestamp,
                              "#goalDe-" + timestamp, "#taskDe-" + timestamp, "#taskObj-" + timestamp, agent,
                              self.iriSet[0] + "#refersExactlyTo", URIRef(self.iriSet[1] + "#update"),
                              None, parameter, timestamp)

        reqGraph.add((parameter, RDF.type, URIRef(self.iriSet[0] + "#OntologyDescriptionObject")))

        reqGraph.add((URIRef(self.iriSet[0] + "#hasInformationObjectType"), RDF.type, Utils.owlobj))
        reqGraph.add((parameter, URIRef(self.iriSet[0] + "#hasInformationObjectType"),
                      URIRef(self.iriSet[1] + "#ontology_description_object_type")))

        reqGraph.add((URIRef(self.iriSet[0] + "#descriptionProvidedByIRI"), RDF.type, Utils.owldat))
        reqGraph.add((parameter, URIRef(self.iriSet[0] + "#descriptionProvidedByIRI"),
                      Literal(self.iriSet[2], datatype=XSD.string)))

        tosend = Utils.libbug(reqGraph, iri)  # transmits config solving the rdflib bug of xml:base
        received = Utils.transmit(tosend.encode(), True, self.hubInfo[0], self.hubInfo[1])
        if received is None:
            return 0
        g = rdflib.Graph()
        g.parse(data=received)
        if Utils.checkStatus(g, self.iriSet[0] + "#", self.iriSet[1] + "#succeded_status_type") == 1:
            print("Device update confirmed by the hub")
        else:
            print("Device update not confirmed by the hub")
        # f=open("test.owl", "w")
        # f.write(g.serialize(format="pretty-xml").decode())
        return 1

    @staticmethod
    def printGraph(graph):
        print("--- printing raw triples ---")
        for s, p, o in graph:
            print((s, p, o))

    @staticmethod
    def retrieveEntityName(iri):
        start = iri.rfind('#')
        return iri[start + 1:]

    def run(self):
        # end set
        # set connection
        while self.restart:
            self.serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.serversocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            self.serversocket.bind((self.agentInfo[1], int(self.agentInfo[2])))
            self.serversocket.listen(5)
            print("Client listening on", self.agentInfo[1], "port", self.agentInfo[2])
            self.alive = True
            while self.alive:
                try:
                    clientsocket, address = self.serversocket.accept()
                    self.servermanager.launchServer(clientsocket, address, self)
                except Exception as e:
                    print("Disconnected.")
                    break
        return

    #############################################################################################
