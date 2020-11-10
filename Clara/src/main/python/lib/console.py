from threading import *
from lib.consolecommand import *


class Console(Thread):
    inputText = "Enter a command: ---> "

    def __init__(self, agent, commandSet):
        Thread.__init__(self)
        self.agent = agent
        self.commandSet = commandSet
        self.exec_status = True
        # self.start()
        return

    def stop(self):
        self.exec_status = False
        print("Console closing. Goodbye.")
        return False

    def run(self):
        while self.exec_status:
            print(Console.inputText, end='')
            entered = input("").strip()
            check = 0
            for command in self.commandSet:
                check = command.checkCommand(self, entered, self.agent)
                if check == 1:
                    break
            if check == 0:
                print("Unrecognized command")
                print(
                    "Use start | start [address] [port] | stop | exit | status | install | uninstall | set hub ["
                    "address] [port] | set device address port | check install")
            time.sleep(1)
        return
