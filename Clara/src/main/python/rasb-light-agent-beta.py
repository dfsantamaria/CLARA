import os
import re
import socket
import time
from datetime import datetime
from pathlib import Path
from threading import *

import rdflib
from lib.agent import *
from lib.console import *
from lib.consolecommand import *
from lib.utils import *
from rdflib import *


class LightAgentServerManager(AgentServerManager):
    def performOperation(self, g, execution):
        # for s,p,o in g.triples( (None,None,None) ):
        #   print(s,p,o)
        taskObject = next(g.objects(execution, URIRef(self.agent.iriSet[0] + "#hasTaskObject")))
        taskObject = next(g.objects(taskObject, URIRef(self.agent.iriSet[0] + "#refersExactlyTo")))
        taskOperator = next(g.objects(execution, URIRef(self.agent.iriSet[0] + "#hasTaskOperator")))
        taskOperator = next(g.objects(taskOperator, URIRef(self.agent.iriSet[0] + "#refersExactlyTo")))

        value = 100
        for s in g.objects(None, URIRef(self.agent.iriSet[0] + "#hasTaskActualInputParameter")):
            for c in g.objects(s, URIRef(self.agent.iriSet[0] + "#refersAsNewTo")):
                for t in g.objects(c, URIRef(self.agent.iriSet[0] + "#hasDataValue")):
                    value = t
                    break
        print("\n Action ", taskOperator, "on ", taskObject, "with value ", value)
        print(Console.inputText, end='')
        return


def setTestPath():
    p = Path(__file__).parents[1]
    os.chdir(p)
    return


def main():
    setTestPath()
    agent = Agent(LightAgentServerManager(), "ontologies/test/rasb-lightagent.owl",
                  {"ontologies/test/lightagent-from-template.owl"},
                  "http://www.dmi.unict.it/lightagent.owl", "http://www.dmi.unict.it/lightagent-template.owl")
    console = Console(agent,
                      [StartCommand(), StopCommand(), ExitCommand(), StatusCommand(), SetHubCommand(), InstallCommand(),
                       CheckInstallCommand(), UninstallCommand(), SetDeviceCommand()])
    console.start()


if __name__ == '__main__':
    main()
