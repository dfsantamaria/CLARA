import rdflib
from rdflib import *
from datetime import datetime
import time
import re
import socket


class Utils:
    owlobj = URIRef("http://www.w3.org/2002/07/owl#ObjectProperty")
    owldat = URIRef("http://www.w3.org/2002/07/owl#DatatypeProperty")

    def getOWLobj(self):
        return self.owlobj

    @property
    def getOWLdat(self):
        return owldat

    @staticmethod
    def recvall(sock):
        BUFF_SIZE = 1024  # 1 KiB
        data = b''
        timeout = time.time() + 60
        while time.time() < timeout:
            part = sock.recv(BUFF_SIZE)
            data += part
            if len(part) < BUFF_SIZE:
                # either 0 or end of data
                break
        return data

    @staticmethod
    def getTimeStamp():
        return (str(datetime.timestamp(datetime.now()))).replace(".", "-")

    @staticmethod
    def retrieveURI(string):
        out = string.split("#", 1)[0]
        return out

    @staticmethod
    def retrieveEntityName(string):
        out = string.split("#", 1)[1]
        return out

    @staticmethod
    def readOntoFile(file):
        f = open(file, "r")
        return f.read()

    @staticmethod
    def getGraph(value):
        g = rdflib.Graph()
        g.parse(data=value)
        return g

    @staticmethod
    def replacenth(string, sub, wanted, n):
        where = [m.start() for m in re.finditer(sub, string)][n - 1]
        before = string[:where]
        after = string[where:]
        after = after.replace(sub, wanted, 1)
        newString = before + after
        return newString

    @staticmethod
    def libbug(graph, iri):
        tosend = graph.serialize(format='pretty-xml').decode()  # transmits template
        replace = "  xml:base=\"" + iri + "\"> \n"
        tosend = Utils.replacenth(tosend, ">", replace, 2)
        return tosend

    ###

    @staticmethod
    def checkAddress(address, port):
        if int(port) < 0 or int(port) > 65535 or address is None:
            return 0

    @staticmethod
    def checkStatus(g, oasisIri, status):
        for b in g.objects(None, URIRef(oasisIri + "hasStatusType")):
            if str(b) == status:
                return 1
        return 0

    @staticmethod
    def addPerformInfo(s, oasisIRI, executer, execution):
        s.add((URIRef(executer), URIRef(oasisIRI + "performs"), URIRef(execution)))
        s.add((URIRef(oasisIRI + "performs"), RDF.type, Utils.owlobj))
        return

    @staticmethod
    def addExecutionGraph(g, oasisIRI, iri, executer, timestamp):
        plane = URIRef(iri + "#planExecution-" + timestamp)
        goale = URIRef(iri + "#goalExecution-" + timestamp)
        execution = URIRef(iri + "#taskExecution-" + timestamp)
        g.add((plane, RDF.type, URIRef(oasisIRI + "PlanExecution")))
        g.add((goale, RDF.type, URIRef(oasisIRI + "GoalExecution")))
        g.add((execution, RDF.type, URIRef(oasisIRI + "TaskExecution")))
        g.add((URIRef(oasisIRI + "consistsOfGoalExecution"), RDF.type, Utils.owlobj))
        g.add((URIRef(oasisIRI + "consistsOfTaskExecution"), RDF.type, Utils.owlobj))
        g.add((plane, URIRef(oasisIRI + "consistsOfGoalExecution"), goale))
        g.add((goale, URIRef(oasisIRI + "consistsOfTaskExecution"), execution))
        goald = next(g.subjects(RDF.type, URIRef(oasisIRI + "GoalDescription")))
        taskd = next(g.subjects(RDF.type, URIRef(oasisIRI + "TaskDescription")))
        g.add((goald, URIRef(oasisIRI + "hasGoalExecution"), goale))
        g.add((taskd, URIRef(oasisIRI + "hasTaskExecution"), execution))
        g.add((URIRef(executer), URIRef(oasisIRI + "performs"), execution))
        return execution

    @staticmethod
    def generateExecutionStatus(g, requester, execution, destination, status, iri, oasisIRI, oasisaboxIRI,
                                iriassist, timestamp):

        objectTask = URIRef(iri + "#belief-data-" + timestamp)  # the obj
        parameter = URIRef(iri + "#parameter-" + timestamp)  # the parameter
        operator = URIRef(oasisaboxIRI + "add")  # task operator

        Utils.generateRequest(g, iri, oasisIRI, URIRef(requester), destination, "#planDe-" + timestamp,
                              "#goalDe-" + timestamp, "#taskDe-" + timestamp, "#taskObDe-" + timestamp, objectTask,
                              oasisIRI + "refersAsNewTo", operator, None, parameter, timestamp)

        g.add((URIRef(objectTask), RDF.type, URIRef(oasisIRI + "BeliefDescriptionObject")))
        g.add((URIRef(parameter), RDF.type, URIRef(oasisIRI + "TaskActualInputParameter")))
        g.add((URIRef(operator), RDF.type, URIRef(oasisIRI + "Action")))
        g.add((URIRef(execution), RDF.type, URIRef(oasisIRI + "TaskExecution")))
        g.add((URIRef(iriassist), RDF.type, URIRef(oasisIRI + "Device")))  # has request

        g.add((URIRef(oasisIRI + "hasInformationObjectType"), RDF.type, Utils.owlobj))

        g.add((objectTask, URIRef(oasisIRI + "hasInformationObjectType"),
               URIRef(oasisaboxIRI + "belief_description_object_type")))
        g.add((URIRef(oasisIRI + "descriptionProvidedByURL"), RDF.type, Utils.owldat))
        g.add((objectTask, URIRef(oasisIRI + "descriptionProvidedByURL"), Literal(iri, datatype=XSD.string)))

        g.add((parameter, RDF.type, URIRef(oasisIRI + "OntologyDescriptionObject")))
        g.add((parameter, URIRef(oasisIRI + "hasInformationObjectType"),
               URIRef(oasisaboxIRI + "ontology_description_object_type")))

        g.add((URIRef(oasisIRI + "descriptionProvidedByEntityIRI"), RDF.type, Utils.owldat))
        g.add((parameter, URIRef(oasisIRI + "descriptionProvidedByEntityIRI"), Literal(execution, datatype=XSD.string)))

        g.add((URIRef(oasisIRI + "hasStatus"), RDF.type, Utils.owlobj))
        g.add((URIRef(oasisIRI + "hasStatusType"), RDF.type, Utils.owlobj))
        thestatusob = URIRef(iri + "#exec-status-obj")
        g.add((URIRef(execution), URIRef(oasisIRI + "hasStatus"), URIRef(thestatusob)))
        g.add((URIRef(thestatusob), URIRef(oasisIRI + "hasStatusType"), URIRef(status)))
        return

    @staticmethod
    def addImportAxioms(g, iri, axioms):
        for s in axioms:
            g.add((URIRef(iri), OWL.imports, URIRef(s)))

    @staticmethod
    def generateRequest(reqGraph, iri, iriOasis, requester, destination, plan, goal, taskde, taskobj, object_,
                        objectReferProp, operator, argument, parameter, timestamp):

        reqGraph.add((URIRef(iri), RDF.type, OWL.Ontology))
        request = URIRef(iri + plan)  # the request
        if destination is not None:
            reqGraph.add((URIRef(iriOasis + "requestedTo"), RDF.type, Utils.owlobj))
            reqGraph.add((request, URIRef(iriOasis + "requestedTo"), URIRef(destination)))

        reqGraph.add((URIRef(iriOasis + "requests"), RDF.type, Utils.owlobj))
        reqGraph.add((URIRef(requester), URIRef(iriOasis + "requests"), request))  # has request
        reqGraph.add((request, RDF.type, URIRef(iriOasis + "PlanDescription")))  # request type

        goal = URIRef(iri + goal)  # the goal
        reqGraph.add((goal, RDF.type, URIRef(iriOasis + "GoalDescription")))  # goal type

        task = URIRef(iri + taskde)
        reqGraph.add((task, RDF.type, URIRef(iriOasis + "TaskDescription")))  # task type

        reqGraph.add((URIRef(iriOasis + "consistsOfGoalDescription"), RDF.type, Utils.owlobj))
        reqGraph.add((request, URIRef(iriOasis + "consistsOfGoalDescription"), goal))  # has goal

        reqGraph.add((URIRef(iriOasis + "consistsOfTaskDescription"), RDF.type, Utils.owlobj))
        reqGraph.add((goal, URIRef(iriOasis + "consistsOfTaskDescription"), task))  # has goal

        taskObject = URIRef(iri + taskobj)  # the taskobject

        reqGraph.add((taskObject, RDF.type, URIRef(iriOasis + "TaskObject")))
        reqGraph.add((URIRef(iriOasis + "hasTaskObject"), RDF.type, Utils.owlobj))

        reqGraph.add((task, URIRef(iriOasis + "hasTaskObject"), taskObject))  # task object
        reqGraph.add((URIRef(objectReferProp), RDF.type, Utils.owlobj))
        reqGraph.add((taskObject, URIRef(objectReferProp), object_))  # task object

        taskOperator = URIRef(iri + "#taskOperator-" + timestamp)  # the taskobject
        reqGraph.add((taskOperator, RDF.type, URIRef(iriOasis + "TaskOperator")))
        reqGraph.add((URIRef(iriOasis + "hasTaskOperator"), RDF.type, Utils.owlobj))
        reqGraph.add((task, URIRef(iriOasis + "hasTaskOperator"),
                      taskOperator))  # task operator
        reqGraph.add((URIRef(iriOasis + "refersExactlyTo"), RDF.type, Utils.owlobj))
        reqGraph.add((taskOperator, URIRef(iriOasis + "refersExactlyTo"), operator))  # task object

        if parameter is not None:
            taskParameter = URIRef(iri + "#taskInputParameter-" + timestamp)  # the taskobject
            reqGraph.add((taskParameter, RDF.type, URIRef(iriOasis + "TaskActualInputParameter")))
            reqGraph.add((URIRef(iriOasis + "hasTaskActualInputParameter"), RDF.type, Utils.owlobj))
            reqGraph.add((task, URIRef(iriOasis + "hasTaskActualInputParameter"), taskParameter))  # task parameter
            reqGraph.add((taskParameter, URIRef(iriOasis + "refersAsNewTo"), parameter))

        if argument is not None:
            opArgument = URIRef(iri + "#taskOperatorArgument-" + timestamp)  # the taskobject
            reqGraph.add((opArgument, RDF.type, URIRef(iriOasis + "TaskOperatorArgument")))
            reqGraph.add((URIRef(iriOasis + "hasTaskOperatorArgument"), RDF.type, Utils.owlobj))
            reqGraph.add((task, URIRef(iriOasis + "hasTaskOperatorArgument"), opArgument))  # argument
            reqGraph.add((opArgument, URIRef(iriOasis + "refersExactlyTo"), argument))
        return

    @staticmethod
    def transmit(data, response, address, port):
        # print(data)
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        received = ''
        try:
            client_socket.connect((address, int(port)))
            client_socket.send(data)
            if response:
                received = Utils.recvall(client_socket).decode()
        except socket.error:
            client_socket.close()
            return None
        else:
            client_socket.close()
            return received

    @staticmethod
    def serverTransmit(data, sock, addr, server_socket):
        print("Sending response to: ", addr, "port ", sock)
        try:
            server_socket.send(data)
        except socket.error:
            return 0
        # server_socket.close()
        else:
            return 1
