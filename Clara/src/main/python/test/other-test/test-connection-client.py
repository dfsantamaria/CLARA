import socket
from threading import *

def recvall(sock):
    BUFF_SIZE = 1024 # 4 KiB
    data = b''
    while True:
        part = sock.recv(BUFF_SIZE)
        data += part
        if len(part) < BUFF_SIZE:
            # either 0 or end of data
            break
    return data

client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect(('localhost', 8000))
f=open("rasb-lightagent.owl")
client_socket.send(f.read().encode())
request=recvall(client_socket)
print(request)
client_socket.close()