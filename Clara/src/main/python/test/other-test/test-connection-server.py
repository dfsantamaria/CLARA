import socket
from threading import *

host='localhost'
port=8000

def init_server():
    serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    serversocket.bind((host, int(port)))
    serversocket.listen(5)
    while 1:
        clients, address = serversocket.accept()
        client(clients, address, serversocket)
    return


def recvall(sock):
    BUFF_SIZE = 1024 # 2 KiB
    data = b''
    while True:
        part = sock.recv(BUFF_SIZE)
        data += part
        if len(part) < BUFF_SIZE:
            # either 0 or end of data
            break
    return data

class client(Thread):
    def __init__(self, socket, address, serversocket):
        Thread.__init__(self)
        self.sock = socket
        self.addr = address
        self.serversocket=serversocket
        self.start()

    def run(self):
        request=recvall(self.sock)
        #print (request, self.addr[1], self.addr[0])
        print(request)
        message = "test"
        #self.serversocket.connect(( self.addr[0], int(self.addr[1]) ))
        self.sock.send(message.encode())

init_server()
