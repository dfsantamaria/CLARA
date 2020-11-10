from threading import *
from lib.agent import *


class ConsoleCommand:
    def __init__(self, name):
        self.commandName = name

    def checkCommand(self, console, inputc, agent):
        raise NotImplementedError()

    def getCommandName(self):
        return self.commandName


class StartCommand(ConsoleCommand):
    def __init__(self):
        super(StartCommand, self).__init__("start")

    def checkCommand(self, console, inputc, agent):
        if inputc.startswith(self.getCommandName()):
            parms = inputc.split()
            if len(parms) == 1:
                agent.setAgentConnection(None, None)
                agent.startAgent()  # default address, port
                return 1
            elif len(parms) == 3:
                agent.setAgentConnection(parms[1], parms[2])
                agent.startAgent()
                return 1
            else:
                print("Use: start | start address port")
                return 0


class StopCommand(ConsoleCommand):
    def __init__(self):
        super(StopCommand, self).__init__("stop")

    def checkCommand(self, console, inputc, agent):
        if inputc.startswith(self.getCommandName()):
            if not self.checkAgent(agent):
                return 1
            agent.stop()
            return 1
        else:
            return 0

    @staticmethod
    def checkAgent(agent):
        if not agent.isAlive():
            print("Agent not started. Please start the agent first")
            return 0
        return 1


class ExitCommand(ConsoleCommand):
    def __init__(self):
        super(ExitCommand, self).__init__("exit")

    def checkCommand(self, console, inputc, agent):
        if inputc.startswith(self.getCommandName()):
            if agent.isAlive():
                agent.stop()
            console.stop()
            return 1
        else:
            return 0


class StatusCommand(ConsoleCommand):
    def __init__(self):
        super(StatusCommand, self).__init__("status")

    def checkCommand(self, console, inputc, agent):
        if inputc.startswith(self.getCommandName()):
            status = agent.alive
            print("The server is ", end="")
            if not status:
                print("not ", end="")
            print("active.")
            return 1
        else:
            return 0


class InstallCommand(ConsoleCommand):
    def __init__(self):
        super(InstallCommand, self).__init__("install")

    def checkCommand(self, console, inputc, agent):
        if inputc.startswith(self.getCommandName()):
            if self.checkAgent(agent):
                if self.install_device(agent):
                    print("Device installation complete")
                else:
                    print("Device cannot be installed. Make sure the hub is correctly set")
            else:
                return 1
        else:
            return 0
        return 1

    @staticmethod
    def checkAgent(agent):
        if not agent.isAlive():
            print("Agent not started. Please start the agent first")
            return 0
        return 1

    @staticmethod
    def install_device(agent):
        return agent.install_device()


class SetHubCommand(ConsoleCommand):
    def __init__(self):
        super(SetHubCommand, self).__init__("set hub")

    def checkCommand(self, console, inputc, agent):
        if inputc.startswith(self.getCommandName()):
            if not self.checkAgent(agent):
                return 1
            parms = inputc.split()
            if len(parms) == 4:
                if self.set_hub(agent, parms[2], parms[3]):
                    print("The hub is located at address ", parms[2], "port ", parms[3])
                else:
                    print("The hub cannot be configured, check the parameters")
            else:
                print("Use: set hub address port")
            return 1
        else:
            return 0

    @staticmethod
    def set_hub(agent, host, port):
        return agent.set_hub(host, port)

    @staticmethod
    def checkAgent(agent):
        if not agent.isAlive():
            print("Agent not started. Please start the agent first")
            return 0
        return 1


class CheckInstallCommand(ConsoleCommand):
    def __init__(self):
        super(CheckInstallCommand, self).__init__("check install")

    def checkCommand(self, console, inputc, agent):
        if inputc.startswith(self.getCommandName()):
            if self.checkAgent(agent):
                if self.check_install(agent):
                    print("The device is installed")
                else:
                    print("The device is not installed")
        else:
            return 0
        return 1

    @staticmethod
    def check_install(agent):
        return agent.check_install()

    @staticmethod
    def checkAgent(agent):
        if not agent.isAlive():
            print("Agent not started. Please start the agent first")
            return 0
        return 1


class UninstallCommand(ConsoleCommand):
    def __init__(self):
        super(UninstallCommand, self).__init__("uninstall")

    def checkCommand(self, console, inputc, agent):
        if inputc.startswith(self.getCommandName()):
            if agent.isAlive():
                if self.uninstall_device(agent):
                    print("Device uninstallation complete")
                else:
                    print("Device cannot be uninstalled. Make sure the hub is correctly set")
            else:
                print("Agent not started or not installed")
        else:
            return 0
        return 1

    @staticmethod
    def checkAgent(agent):
        if not agent.isAlive():
            print("Agent not started. Please start the agent first")
            return 0
        return 1

    @staticmethod
    def uninstall_device(agent):
        return agent.uninstall_device()


class SetDeviceCommand(ConsoleCommand):
    def __init__(self):
        super(SetDeviceCommand, self).__init__("set device")

    def checkCommand(self, console, inputc, agent):
        if inputc.startswith(self.getCommandName()):
            if not agent.isAlive() or agent.check_install == 0:
                print("Agent not started or not installed")
                return 1
            parms = inputc.split()
            if len(parms) == 4:
                if self.set_connection(agent, parms[2], parms[3]):
                    print("The device has been updated")
                else:
                    print("The device cannot be updated, check the parameters")
            else:
                print("Use: set hub address port")
        else:
            return 0
        return 1

    @staticmethod
    def set_connection(agent, host, port):
        return agent.set_connection(host, port)

    @staticmethod
    def checkAgent(agent):
        if not agent.isAlive():
            print("Agent not started. Please start the agent first")
            return 0
        return 1
