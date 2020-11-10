import rdflib
from rdflib import *
from pathlib import Path
import os

p = Path(__file__).parents[2]
os.chdir(p)

file="ontologies/test/rasb/lightagent-from-template.owl"

f = open(file, "r")
value=f.read()
g=rdflib.Graph();
g.parse(data=value);
g.bind('xml:base', rdflib.Namespace("http://www.dmi.unict.it/lightagent-template.owl"))
g.serialize(destination='out.rdf' ,format='xml')
