import sys

sys.path.insert(0, "../lib")

from phidias.Types  import *
from phidias.Main import *
from phidias.Lib import *




class say_hello(Procedure): pass
class hello(Procedure): pass

class Assign(Action):
    def execute(self):
      PHIDIAS.achieve(hello())


say_hello() >> [Assign()]
hello() >> [show_line("hi")]
PHIDIAS.run()


PHIDIAS.achieve(say_hello())
