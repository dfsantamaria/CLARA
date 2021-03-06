import sys
sys.path.insert(0, "./lib")

from py4j.java_gateway import JavaGateway
import subprocess
import os
import time
import socket
import rdflib
from rdflib import *
from pathlib import Path
from threading import *
from phidias.Types  import *
from phidias.Main import *
from phidias.Lib import *
from datetime import datetime

profonto = ''
oasis = 'http://www.dmi.unict.it/oasis.owl#'
oasisabox = 'http://www.dmi.unict.it/oasis-abox.owl#'
assistant=''
host = None
port = None
owlobj=URIRef("http://www.w3.org/2002/07/owl#ObjectProperty")
owldat=URIRef("http://www.w3.org/2002/07/owl#DatatypeProperty")
#################################################PHIDIAS PART ##############################

class welcome(Procedure): pass
class decide(Procedure): pass



def getGraph(value):
    g = rdflib.Graph()
    g.parse(data=value)
    return g

def setExecutionStatus(graph):
    for execution, status in graph.subject_objects(predicate=URIRef(oasis + "hasStatus")):
      profonto.setExecutionStatus(execution, status)

def transmitExecutionStatus(execution, status, addr, sock,  server_socket):
    g=Graph()
    iri=retrieveURI(execution).replace(".owl","-response.owl")
    generateRequest(g, iri)
    iriassist="http://www.dmi.unict.it/profonto-home.owl#"+assistant

    g.add((URIRef(iriassist), RDF.type, URIRef(oasis + "Device")))  # has request
    g.add((URIRef(iriassist), URIRef(oasis + "requests"), URIRef(iri + "#request")))

    task = URIRef(iri + "#task")
    g.add((URIRef(oasis + "hasTaskOperator"), RDF.type, owlobj))
    g.add((task, URIRef(oasis + "hasTaskOperator"), URIRef(oasis + "add")))  # task operator

    g.add((URIRef(oasis + "hasTaskObject"), RDF.type, owlobj))
    g.add((task, URIRef(oasis + "hasTaskObject"), URIRef(iriassist)))  # task object
    g.add((URIRef(iriassist), URIRef(oasis + "hasType"), URIRef(oasisabox + "belief_description_object_type")))  # task object

    parameter = URIRef(iri + "#parameter")  # the parameter
    g.add((parameter, RDF.type, URIRef(oasis + "TaskInputParameter")))
    g.add((parameter, RDF.type, URIRef(oasis + "OntologyDescriptionObject")))

    g.add((URIRef(oasis+ "hasInformationObjectType"), RDF.type, owlobj))
    g.add((parameter, URIRef(oasis + "hasInformationObjectType"),URIRef(oasisabox + "ontology_description_object_type")))

    g.add((URIRef(oasis + "descriptionProvidedByIRI"), RDF.type, owldat))
    g.add((parameter, URIRef(oasis + "descriptionProvidedByIRI"), Literal(iri, datatype=XSD.string)))

    g.add((URIRef(oasis + "refersTo"), RDF.type, owlobj))
    g.add((parameter, URIRef(oasis + "refersTo"), URIRef(execution)))

    g.add((URIRef(oasis + "hasTaskInputParameter"), RDF.type, owlobj))
    g.add((task, URIRef(oasis + "hasTaskInputParameter"), parameter))  # task parameter

    g.add((URIRef(oasis + "hasStatus"), RDF.type, owlobj))
    g.add((URIRef(execution),URIRef(oasis + "hasStatus"), URIRef(status)))
    transmit(g.serialize(format='pretty-xml'), sock, addr,  server_socket)
    return

def transmit(data, sock, addr, server_socket):
    print("Sending response to: ", addr, "port ", sock)
    server_socket.send(data)
    #server_socket.close()
    return


def generateRequest(reqGraph, iri):

    request = URIRef(iri+"#request")             #the request
    reqGraph.add(( request, RDF.type, URIRef(oasis+"PlanDescription")))  # request type

    goal = URIRef(iri + "#goal")  # the goal
    reqGraph.add((goal, RDF.type, URIRef(oasis + "GoalDescription")))  # goal type

    task = URIRef(iri + "#task")  # the task
    reqGraph.add((task, RDF.type, URIRef(oasis + "TaskDescription")))  # task type

    reqGraph.add((URIRef(oasis + "consistsOfGoalDescription"), RDF.type, owlobj))
    reqGraph.add((request, URIRef(oasis + "consistsOfGoalDescription"), goal))  # has goal

    reqGraph.add((URIRef(oasis + "consistsOfTaskDescription"), RDF.type, owlobj))
    reqGraph.add((goal, URIRef(oasis + "consistsOfTaskDescription"), task))  # has goal
    return

def computesDependencies(graph, executions):
      for first, second in graph.subject_objects(predicate=URIRef(oasis + "dependsOn")):
          index=0
          while index < len(executions):
              key,value = executions[index]
              if second == key :
                  executions[index]= (key,value+1)
              index += 1

def getOntologyFile(graph, execution):
    file=None
    for t in graph.objects(execution, URIRef(oasis + "hasTaskInputParameter")): # retrieving source
       for s in graph.objects(t, URIRef(oasis + "descriptionProvidedByURL")):
           if (s is not None):
             file = readOntoFile(s)
             return file
       for s in graph.objects(t, URIRef(oasis + "descriptionProvidedByIRI")):
         if (s is not None):
             file=s
             return s


def getTimeStamp():
    return  (str(datetime.timestamp(datetime.now()))).replace(".", "-")

def createRequest(graph,execution):
    request=Graph()
    uri='http://www.dmi.unict.it/profonto-home.owl#'
    name=retrieveEntityName(execution)
    request.add((execution, RDF.type, URIRef(oasis + "TaskExecution")))
    request.add((URIRef(uri+name+"PlanRequest"), RDF.type, URIRef(oasis+ "PlanDescription") ))
    request.add((URIRef(uri + name + "GoalRequest"), RDF.type, URIRef(oasis + "GoalDescription")))
    request.add((URIRef(uri + name + "PlanRequest"), URIRef(oasis + "consistsOfGoalDescription"), URIRef(uri + name + "GoalRequest")))
    request.add((URIRef(uri + name + "TaskRequest"), RDF.type, URIRef(oasis + "TaskDescription")))
    request.add((URIRef(uri + name + "GoalRequest"), URIRef(oasis + "consistsOfTaskDescription"), URIRef(uri + name + "TaskRequest")))
    request.add((URIRef(uri + name + "TaskRequest"), URIRef(oasis + "hasTaskObject"), execution))
    request.add((URIRef(uri + name + "TaskRequest"), URIRef(oasis + "hasTaskOperator"), URIRef(oasisabox + "performs")))
    taskObject = next(graph.objects(execution, URIRef(oasis + "hasTaskObject")))
    taskOperator = next(graph.objects(execution, URIRef(oasis + "hasTaskOperator")))
    #performer = next(graph.subjects(URIRef(oasis + "performs"), execution))
    request.add((execution, URIRef(oasis+"hasTaskObject"), taskObject))
    request.add((execution, URIRef(oasis + "hasTaskOperator"), taskOperator))
    return request

def device_engage(graph,execution):
    #for s,p,o in graph.triples( (None,None,None) ):
    #    print(s,p,o)
    taskObject = next(graph.objects(execution, URIRef(oasis + "hasTaskObject")))
    taskOperator = next(graph.objects(execution, URIRef(oasis + "hasTaskOperator")))
    performer = next(graph.subjects(URIRef(oasis + "performs"), execution))
    devip=next(graph.objects(subject=None, predicate=URIRef(oasis + "hasIPAddress")))
    devport=next(graph.objects(subject=None, predicate=URIRef(oasis + "hasPortNumber")))
    print("To engage:", performer, taskObject, taskOperator, devip, devport)
    toreturn = createRequest(graph,execution).serialize(format='xml')
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client_socket.connect((devip, int(devport)))
    client_socket.send(toreturn)
    message=recvall(client_socket).decode()
    client_socket.close()
    return message

# Actions that the assistant performs
def profhome_decide(graph, execution, addr, sock, server_socket):
    requester = next(graph.objects(execution, URIRef(oasis + "hasTaskObject")))
    for actions in graph.objects(execution, URIRef(oasis + "hasTaskOperator")):
        if actions == URIRef(oasisabox + "install"):
            file = getOntologyFile(graph, execution)
            value = profonto.addDevice(file)  # read the device data
            print("Device", value, "added.")
            transmitExecutionStatus(execution, URIRef(oasisabox+"succeded_status"), addr, sock, server_socket)
            break
        elif actions == URIRef(oasisabox + "uninstall"):  # uninstallation task
            #requester = next(graph.objects(execution, URIRef(oasis + "hasTaskObject")))
            value = profonto.removeDevice(retrieveEntityName(requester))  # read the device data
            print("Device", retrieveEntityName(requester), "removed with exit code", value,".")
            transmitExecutionStatus(execution, URIRef(oasisabox+"succeded_status"), addr, sock, server_socket)
            break
        elif actions == URIRef(oasisabox + "add") or actions == URIRef(oasisabox + "remove"):  # add user task
             for thetype in graph.objects(requester, URIRef(oasis + "hasType")):
                 if thetype== URIRef(oasisabox + "user_type"): #adding or removing user
                     if actions == URIRef(oasisabox + "add"):
                         file = getOntologyFile(graph, execution)
                         value= profonto.addUser(file)
                         profonto.setExecutionStatus(execution, URIRef(oasisabox+"succeded_status"), )
                         transmitExecutionStatus(execution,URIRef(oasisabox+"succeded_status"), addr, sock,  server_socket)
                         print("User", value, "added.")
                     elif actions == URIRef(oasisabox + "remove"):
                          value=profonto.removeUser(retrieveEntityName(requester))
                          profonto.setExecutionStatus(execution, URIRef(oasisabox+"succeded_status"))
                          transmitExecutionStatus(execution, URIRef(oasisabox+"succeded_status"), addr, sock,  server_socket)
                          print("User", retrieveEntityName(requester), "removed with exit code", value, ".")
                 elif thetype == URIRef(oasisabox + "user_configuration_type"):  # adding or removing user
                     if actions == URIRef(oasisabox + "add"):
                         file = getOntologyFile(graph, execution)
                         value= profonto.addConfiguration(file)
                         profonto.setExecutionStatus(execution, URIRef(oasisabox+"succeded_status"))
                         transmitExecutionStatus(execution, "succeded_status", addr, sock,  server_socket)
                         print("Configuration added:", value,".")
                     elif actions == URIRef(oasisabox + "remove"):
                         value = profonto.removeConfiguration(retrieveEntityName(requester))
                         profonto.setExecutionStatus(execution, URIRef(oasisabox+"succeded_status"))
                         transmitExecutionStatus(execution, URIRef(oasisabox+"succeded_status"), addr, sock,  server_socket)
                         print("Configuration", retrieveEntityName(requester), "removed.")
                 elif  thetype == URIRef(oasisabox + "belief_description_object_type"):
                      file = getOntologyFile(graph, execution)
                      if actions == URIRef(oasisabox + "add"):
                         value = profonto.addDataBelief(file)
                         profonto.setExecutionStatus(execution, URIRef(oasisabox+"succeded_status"))
                         transmitExecutionStatus(execution, URIRef(oasisabox+"succeded_status"), addr, sock,  server_socket)
                         print("Belief  added with exit code", value)
                      elif actions == URIRef(oasisabox + "remove"):
                         value = profonto.removeDataBelief(file)
                         profonto.setExecutionStatus(execution, URIRef(oasisabox+"succeded_status"))
                         transmitExecutionStatus(execution, URIRef(oasisabox+"succeded_status"), addr, sock,  server_socket)
                         print("Belief removed with exit code", value)
                 break
        elif actions == URIRef(oasisabox + "parse"):
            for thetype in graph.objects(requester, URIRef(oasis + "hasType")):
                if thetype == URIRef(oasisabox + "generalUtterance"):
                    print("General utterances parser is being developed... stay tuned!")
                    transmitExecutionStatus(execution, URIRef(oasisabox+"failed_status"), addr, sock, server_socket)
                    break
        elif actions == URIRef(oasisabox + "retrieve"):
            for thetype in graph.objects(requester, URIRef(oasis + "hasType")):
                if thetype == URIRef(oasisabox + "belief_description_object_type"):
                    file = getOntologyFile(graph, execution)
                    value = profonto.retrieveDataBelief(file)
                    profonto.setExecutionStatus(execution, URIRef(oasisabox+"succeded_status"))
                    transmitExecutionStatus(execution, URIRef(oasisabox+"succeded_status"), addr, sock,  server_socket)
                    print("Belief retrieved:\n"+ value)

        else:
            print("Action", actions, "not supported yet")
            transmitExecutionStatus(execution, URIRef(oasisabox+"failed_status"), addr, sock, server_socket)
            break

#Decide which decision has to be taken
class Decide_Action(Action):
    def execute(self, rdf_source, sock, addr, server_socket):
       value = profonto.parseRequest(rdf_source())
       #print("Client send request:", value)
       if value == '':
           print ("Received data from " + str(addr()) + " " + str(sock()))
           return

       g = getGraph(value)

       executions=[]
       for execution in g.subjects(RDF.type, URIRef(oasis+"TaskExecution")):
           executions.append((execution, 0))

       if len(executions) > 1 :
          computesDependencies(g,executions)
          executions=sorted(executions, key = lambda x: x[1])

       for execution, val in executions:
           for executer in g.subjects( URIRef(oasis+"performs"), execution):
              if( retrieveEntityName(executer) == assistant ) :
                profhome_decide(g, execution, addr(), sock(), server_socket())
              else:
                message=device_engage(g, execution)
                #update belief
                #transmitExecutionStatus(execution, status, addr, sock,  server_socket):
                #enable receive in client
                print(message)


def_vars("rdf_source", "sock", "addr", "server_socket")
welcome() >> [ show_line("Phidias has been started. Wait for Prof-Onto to start") ]
decide(rdf_source, sock, addr, server_socket) >> [ Decide_Action(rdf_source, sock, addr, server_socket) ]


################################################ END PHIDIAS PART ##########################


def retrieveURI(string):
    out = string.split("#", 1)[0]
    return out

def retrieveEntityName(string):
    out=string.split("#", 1)[1]
    return out

def readOntoFile(file):
 f=open(file,"r")
 return f.read()

def getProcessOut(process):
  message=''
  while True:
    out = process.stdout.read(1)
    if out != '\n':
        message += out
    else:
        break
  return message


def recvall(sock):
    BUFF_SIZE = 1024 # 1 KiB
    data = b''
    while True:
        part = sock.recv(BUFF_SIZE)
        data += part
        if len(part) < BUFF_SIZE:
            # either 0 or end of data
            break
    return data

class client(Thread):
    def __init__(self, socket, address, server_socket):
        Thread.__init__(self)
        self.sock = socket
        self.addr = address
        self.server_socket=server_socket
        self.start()

    def run(self):
        request = recvall(self.sock).decode()
        PHIDIAS.achieve(decide(request, self.addr[1], self.addr[0], self.sock))

def init_gateway():
    global profonto
    p = Path(__file__).parents[1]
    os.chdir(p)
    folder = 'ontologies/devices'
    for the_file in os.listdir(folder):
        file_path = os.path.join(folder, the_file)
        try:
            if os.path.isfile(file_path):
                os.unlink(file_path)
            # elif os.path.isdir(file_path): shutil.rmtree(file_path)
        except Exception as e:
            print(e)
    jar = "java -jar Prof-Onto-1.0-SNAPSHOT.jar"
    process = subprocess.Popen(jar, universal_newlines=True, stdout=subprocess.PIPE)
    # stdout, stderr = process.communicate()
    print(getProcessOut(process))
    profontoGateWay = JavaGateway()  # connect to the JVM
    profonto = profontoGateWay.entry_point



def init_server():
      serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
      serversocket.bind((host, int(port)))
      serversocket.listen(5)
      print("Prof-Onto Assistant has been started, send requests to:", host, "port ", port)
      while 1:
          clients, address = serversocket.accept()
          client(clients, address, serversocket)
      return

PHIDIAS.run()
PHIDIAS.achieve(welcome())
init_gateway()
print(open("amens/logo.txt", "r").read())
#Adding HomeAssistant
home=readOntoFile("ontologies/test/homeassistant.owl")
assistant = profonto.addDevice(home)  #read the device data
sarray = profonto.getConnectionInfo(assistant)
host = sarray[0]
port = sarray[1]

print("Home assistant added:", assistant, "at ", host, port)
###

init_server()

