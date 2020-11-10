from py4j.java_gateway import JavaGateway
import subprocess
import os
import time
from pathlib import Path

def readOntoFile(file):
 f=open(file,"r")
 return f.read()


p = Path(__file__).parents[2]
os.chdir(p)

folder = 'ontologies/devices'
for the_file in os.listdir(folder):
    file_path = os.path.join(folder, the_file)
    try:
        if os.path.isfile(file_path):
            os.unlink(file_path)
        #elif os.path.isdir(file_path): shutil.rmtree(file_path)
    except Exception as e:
        print(e)

jar="java -jar Prof-Onto-1.0-SNAPSHOT.jar"
process = subprocess.Popen(jar, universal_newlines=True, stdout = subprocess.PIPE)
#stdout, stderr = process.communicate()

welcome=''
while True:
  out = process.stdout.read(1)
  if out != '\n':
   welcome+=out
  else:
    break

print(welcome)

profontoGateWay = JavaGateway()                   # connect to the JVM
profonto = profontoGateWay .entry_point


home=readOntoFile("ontologies/test/homeassistant.owl")
assistant = profonto.addDevice(home)  #read the device data
print("Home assistant added:", assistant)

user=readOntoFile("ontologies/test/alan.owl")
value = profonto.addUser(user)  #read the user data
print("User added:", value)
value=""

device=readOntoFile("ontologies/test/lightagent-from-template.owl")
value = profonto.addDevice(device)  #read the device data
print("Device added:", value)
value=""

config=readOntoFile("ontologies/test/alan-config.owl")
value = profonto.addConfiguration(config)  #read the device configuration data
print("Configuration added:", value)
value=""

print("Testing parseRequest step 1...")
request=readOntoFile("ontologies/test/user-request.owl")
value=profonto.parseRequest(request)[0]
print ("Request:", value)


print("Testing parseRequest step 2 Add belief...")
request=readOntoFile("ontologies/test/add-belief-request.owl")
value=profonto.parseRequest(request)[0]
print ("Request:", value)


print("Testing parseRequest step 3 Retrieve belief...")
request=readOntoFile("ontologies/test/retrieve-belief-request.owl")
value=profonto.parseRequest(request)[0]
print ("Request:", value)

print("Testing parseRequest step 4 Remove belief...")
request=readOntoFile("ontologies/test/remove-belief-request.owl")
value=profonto.parseRequest(request)[0]
print ("Request:", value)

value=profonto.removeUser("Alan")  #remove user
print("User removed with exit code:", value)
value=profonto.removeDevice("light-device") #remove data
print("Device removed with exit code:", value)
value=profonto.removeDevice(assistant) #remove data
print("Home assistant removed:", value)

value=profonto.emptyRequest();
print("Chronology cleaned with exit code:", value)
profontoGateWay.shutdown() #Shutdown the gateway