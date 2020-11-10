import subprocess
import sys
import os
import re
from distutils.dir_util import copy_tree
from pathlib import Path

def install(package):
    subprocess.call([sys.executable, "-m", "pip", "install", package])

print("Installing required python packages, please wait...")

#Uncomment the following line when wordnet will be required
#install('nltk')

install('spacy')
install('pyreadline')
install('py4j')
install('rdflib')
install('pathlib')


subprocess.call([sys.executable, "-m", "spacy", "download", "en"])

#Uncomment the following two lines when wordnet will be required
#import nltk
#nltk.download('wordnet')




print("Running maven, please wait...")

mvn="mvn clean install"
p = subprocess.Popen(mvn, shell=True, stdout = subprocess.PIPE)
stdout, stderr = p.communicate()
print(stdout.decode('utf-8'))

if  stderr is None :
   print("Almost finished...")
   print("Copying required files...")
   copy_tree("ontologies", "target/ontologies")
   copy_tree("src/main/python", "target/python")
   copy_tree("amens", "target/amens")
   if not os.path.exists('target/ontologies/devices'):
       os.makedirs('target/ontologies/devices')
   print("Setup finished")
else:
    print("Uncompilable Java source")
