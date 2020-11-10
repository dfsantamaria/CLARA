Copyright 2019 Department of Mathematics and Computer Science, University of Catania, Italy.

Domenico Cantone - domenico.cantone@unict.it, 
Carmelo Fabio Longo - fabio.longo@unict.it, 
Marianna Nicolosi Asmundo - nicolosi@dmi.unict.it, 
Daniele Francesco Santamaria - santamaria@dmi.unict.it, 
Corrado Santoro - santoro@dmi.unict.it


CLARA

CLARA is a domotic system based on the ontology OASIS - an Ontology for Agent Systems and Integration of Services 
(http://www.dmi.unict.it/~santamaria/projects/oasis/oasis.php).

Documentation:

- See https://www.overleaf.com/read/mjxhtwpxhmxk on Overleaf (only read permission).


Publications:
-Ontological Smart Contracts in OASIS: Ontology forAgents, Systems, and Integration of Services Domenico Cantone, Carmelo Fabio Longo, Marianna Nicolosi-Asmundo, Daniele Francesco Santamaria, Corrado Santoro
To app. in: Proceedings of IDC 2021, The 14th International Symposium on Intelligent Distributed Computing, 16-18 September, 2021, Scilla, Reggio Calabria, Italy.

-Towards an Ontology-Based Framework for a Behavior-Oriented Integration of the IoT Domenico Cantone, Carmelo Fabio Longo, 
 Marianna Nicolosi-Asmundo, Daniele Francesco Santamaria, Corrado Santoro. Proceedings of the 20th Workshop From Objects to Agents, 
 26-28 June, 2019, Parma, Italy, CEUR Workshop Proceedings, ISSN 1613-0073, Vol. 2404, pp. 119--126.

Presentations:
- WOA 2019: shorturl.at/hjwCD


Versioning:
-Version 1.0 CLARA is the new version of the domotic assistant Prof-Onto: https://github.com/dfsantamaria/ProfOnto .


Disclaimer:

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by 
the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a 
copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses/.

Software and its documentation made available on this repository:

-could include technical or other mistakes, inaccuracies or typographical errors. The authors may make changes to the software or 
documentation made available on this repository. -may be out of date, and the authors make no commitment to update such materials. 
The authors assume no responsibility for errors or ommissions in the software or documentation available from its web site.

In no event shall the authors be liable to you or any third parties for any special, punitive, incidental, indirect or consequential 
damages of any kind, or any damages whatsoever, including, without limitation, those resulting from loss of use, data or profits, whether
or not the BGS has been advised of the possibility of such damages, and on any theory of liability, arising out of or in connection with 
the use of this software.

The use of the software downloaded through this repository is done at your own discretion and risk and with agreement that you will be 
solely responsible for any damage to your computer system or loss of data that results from such activities. No advice or information, 
whether oral or written, obtained by you from the BGS or from the BGS web site shall create any warranty for the software.


Instructions for users:

- Notice that Python 3.7 is required.

- Refer to the paper:
  "Towards an Ontology-Based Framework for a Behavior-Oriented Integration of the IoT, by Domenico Cantone, Carmelo 
  Fabio Longo, Marianna Nicolosi-Asmundo, Daniele Francesco Santamaria, and Corrado Santoro, in Proceedings of the 20th Workshop
  From Objects to Agents, 26-28 June, 2019, Parma, Italy, CEUR Workshop Proceedings"
  for an explanation of the ontology. Link: http://ceur-ws.org/Vol-2404/paper18.pdf .

1) Download. 
   1.1)Use git clone or download the source code from GitHub.

2) Setup. 
   2.1) Make sure that Python, Maven, and Java are in your system path.
   2.2) Locate and run the "setup.py" file in the folder "main". Wait for the installation and make sure maven terminates without errors.
   2.3) You can now delete the folder "src". All required files are in the folder "target"

3) Running.   
        
   3.1) The file "main-beta.py" in the folder "target/python" runs a beta version of the domotic assistant. Currently it can 
         - install and uninistall devices;
         - check for devices installation;
         - add and remove users;
         - add and remove configurations.
         - add, remove, and retrieve believes.
         - simulate the execution of simple requests.
         
         Run the file "main-beta.py" and  type:
         
         -> start [address] [port]   # to start the application. By default it will listen on localhost, port 8080.          
         
         You can change the listening address and port by typing:
         
         ->set address port
         
         Wait for it to load. When finished, you can type:
         
         -> stop  #stop the assistant
         -> exit  #terminate the assistant
         
   3.2.A) Then, run the file "client-test.py" in "target/python/test". It  will add a device, a user, a configuration, request a simple task 
        and then it will remove the device, the configuration,  and the user previously added.
         
        Or
        
   3.2.B) Edit the file "rasb-light-agent-beta.py" in "target/python" that simulates a raspberry PI working with OASIS by changing the variables
         "address" and "port" with the ones set in the domotic assistant. Run the file and type the following commands:
          
          -> start           # to start the application that will listen on localhost, port 8087. 
          
          # To change the address and port modify the ontology rasb-lightagent.owl in "target\ontologies\test\rasb" or type 
          
          -> start [address] [port]
                           
          Then, type
   
          -> set hub [address] [port] # to specify the address and port of the assistant that by default is set to localhost, 8000
          -> install                  # to transmit the behavior of the agent to the assistant
          -> status                   # to know the status of the device
          -> check install            # to verify that the device has been recognized by the assistant
          
          From now on, any compatible request will be displayed by the agent with a print.
          
          Run the file "rasb-user-beta.py" which add a user, a configuration, request a simple task and then it will remove the configuration and 
          the user previously added.
          
          -> uninstall #to remove the behavior of the agent from the assistant knowledge base
          
          -> stop   # to disconnect the client.
          
          -> exit # to close the console
     
Instructions for developers:

  1) Inherit the class AgentServerManager (located in lib/agent.py) and implement the virtual function 
  
     performOperation(self, g, execution), where 
     
     -"g" is the RDFLib graph of the response received from the assistant.
     -"execution" is the iri of the individual representing the execution activity received from the assistant. 
     
    Let us call such class "CustomAgentServerManager"
     
  2) Instantiate an agent by writing:
     
     agent=Agent(CustomAgentServerManager(), agent_iri_file, {template_iri_file}, agent_iri, agent_iri_template) where
     
     -"agent_iri_file" is the path of the agent ontology file.
     -"template_iri_file" is the path of the agent template file.
     -"agent_iri is the IRI" of the agent ontology.
     -"agent_iri_template" is the IRI of the agent template ontology.
     
  3) Instantiate a console command by inheriting the class ConsoleCommand (located in lib/consolecommand.py) and by implementing the virtual function 
  
     checkCommand(self, console, input, agent), where
     
     -"console" is the console the command is attached to.
     -"input" is the command that should be typed in order to invoke the command.
     -"agent" is the agent on which the command is acting.
     
  3)Instantiate the Console (located in lib/console.py) by writing
  
    Console(agent, [command1, command2, ...], where
    
    -"agent" is the agent to which the console is attached.
    -"command1", "command2", ... are the console commands instantiated at step 2. A set of defualt command can be found on file "consolecommand.py".     
    
    Write console.run() to run the console. See the file target/rasb-light-agent-beta.py for an example.
     

   
USING OASIS - Ontology Smart Contract Utility.
   
   OSCUtility allows to easily access the functions of the Ethereum smart contract that manages the ontological smart contract provided by OASIS. The smart contract is compliant with the 
   non-fungible token standard ERC721 and accessible at the address: 0x36194ab80f7649572cab9ec524950df32f638b08. 
   
   1) All the Smart Contract Utility functions are located in the class OSCUtility.java.
   2) Instantiate OSCUtility with the following parameters:
       - The address of the IPFS node.
       - The address of the ethereum node.
       - The private key of the wallet used to make transactions.
   2) Call the function load of the OSCUtility instance with "0x36194ab80f7649572cab9ec524950df32f638b08" as parameter.
   3) To publish an ontology call the function mint with the following parameters:
       - The string representing the content of the ontology.
       - The string representing the content of the query.
       - The Token ID of the previous version of the ontology as BigInteger. If there is no previous version, use the BigInteger 0.
      You will receive the hash of the transaction. Wait for it to be completed.
   4) Use the function getTokenInfo to retrieve the IPFS url of the ontology and of the query. Use as parameter the BigInteger representing the token ID.
   5) Use the function burn to destroy the token, using as parameter the token ID to be destroyed.
   6) Use the function transferToken to transfer a token to another wallet. Use the following parameters:
        -The wallet address of the current owner.
        -The wallet address of the destination wallet.
        -The ID of the token to be transferred as BigInteger.
        
<------------------------------------------------------------ Stay tuned for updates ---------------------------------------------------------->
