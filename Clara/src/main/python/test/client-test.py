import socket, time
from threading import *
from pathlib import Path
import rdflib
from rdflib import *
import os


oasis = 'http://www.dmi.unict.it/oasis.owl#'
oasisabox = 'http://www.dmi.unict.it/oasis-abox.owl#'


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

def readOntoFile(file):
 f=open(file,"r")
 return f.read()

p = Path(__file__).parents[2]
os.chdir(p)

port=8000

class client(Thread):
 def __init__(self, socket, address):
  Thread.__init__(self)
  self.sock = socket
  self.addr = address
  self.start()

 def performs(self, request):
      g = rdflib.Graph()
      g.parse(data=request)
      # d= g.serialize(format='xml')
      # print(d)
      execution = next(g.subjects(RDF.type, URIRef(oasis + "TaskExecution")))
      taskObject = next(g.objects(execution, URIRef(oasis + "hasTaskObject")))
      taskOperator = next(g.objects(execution, URIRef(oasis + "hasTaskOperator")))
      print("Action ", taskOperator, "on ", taskObject)
      message=readOntoFile("ontologies/test/add-belief-refers.owl")
      self.sock.send(message.encode())
      return

 def run(self):
  request = recvall(self.sock).decode()
  self.performs(request)
  return


class server(Thread):
 def __init__(self):
  Thread.__init__(self)
  self.start()

 def run(self):
      host='localhost'
      port=8087
      serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
      serversocket.bind((host, int(port)))
      serversocket.listen(5)
      print("Client started:", host, "port ", port)
      while 1:
          clientsocket, address = serversocket.accept()
          client(clientsocket, address)
      return


server()

#installing device
home=readOntoFile("ontologies/test/light-installation-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.sendall(home.encode())
request = ''
request = recvall(client_socket).decode()
print(request)
client_socket.close()
#adding user


#check installing device
home=readOntoFile("ontologies/test/light-check-install-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.sendall(home.encode())
request = ''
request = recvall(client_socket).decode()
print(request)
client_socket.close()


#check installing device
home=readOntoFile("ontologies/test/light-update-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.sendall(home.encode())
request = ''
request = recvall(client_socket).decode()
print(request)
client_socket.close()


home=readOntoFile("ontologies/test/add-user-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(home.encode())
#client_socket.close()
request = recvall(client_socket).decode()
print(request)
client_socket.close()

#adding configuration
home=readOntoFile("ontologies/test/add-user-configuration-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(home.encode())
#client_socket.close()
request = recvall(client_socket).decode()
print(request)
client_socket.close()



#a request
home=readOntoFile("ontologies/test/user-request-2.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(home.encode())
request = recvall(client_socket).decode()
print(request)
client_socket.close()

#a request
home=readOntoFile("ontologies/test/interpretation-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(home.encode())
#client_socket.close()
request = recvall(client_socket).decode()
print(request)
client_socket.close()


home=readOntoFile("ontologies/test/add-belief-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(home.encode())
#client_socket.close()
request = recvall(client_socket).decode()
print(request)
client_socket.close()

home=readOntoFile("ontologies/test/add-belief-status-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(home.encode())
#client_socket.close()
request = recvall(client_socket).decode()
print(request)
client_socket.close()

home=readOntoFile("ontologies/test/retrieve-belief-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(home.encode())
#client_socket.close()
request = recvall(client_socket).decode()
print(request)
client_socket.close()

home=readOntoFile("ontologies/test/remove-belief-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(home.encode())
#client_socket.close()
request = recvall(client_socket).decode()
print(request)
client_socket.close()


#removing configuration
home=readOntoFile("ontologies/test/remove-user-configuration-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(home.encode())
request = recvall(client_socket).decode()
print(request)
client_socket.close()

#removing device

home=readOntoFile("ontologies/test/light-uninstallation-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(home.encode())
request = recvall(client_socket).decode()
print(request)
client_socket.close()

#removing user

home=readOntoFile("ontologies/test/remove-user-request.owl")
client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', port))
client_socket.send(home.encode())
request = recvall(client_socket).decode()
print(request)
client_socket.close()





