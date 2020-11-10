import sys
sys.path.insert(0, "../lib")

from spacyparser import *


dep = SpaCyEnParser('turn off the light')
intentions = dep.getIntentions()
print("Intentions: ", intentions)
modificators = dep.getModificators()
print("Modificators: ", modificators)
temp_modificators = dep.getTemp_modificators()
print("Temp modificators: ",temp_modificators)
conditionals = dep.getConditionals()
print("Conditionals: ",conditionals)