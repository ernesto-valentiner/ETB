
# ETB

ETB is a framework for defining and executing distributed workflows that produce claims supported by evidence.
ETB uses Datalog as the workflow scripting language.
<!--  
It is composed of ETB2, which is a complete reengineering of the Evidential Tool Bus (ETB) using Java integrated with
the Distributed Evidence Network (DEN) implemented with Hyperledger Fabric.
The distributed network acts as a substrate for a secure distributed
execution of ETB2 services and it is based on a combination of distributed ledger technologies (DLT).
-->

## Getting Started with ETB

These instructions will get you an ETB node up and running on your local machine for testing purposes.

### Prerequisites

The following components are required to compile and run ETB.
- JDK
- Maven

### Installation and preparation

1. Clone the ETB project  

	```console
	$ git clone https://github.com/ernesto-valentiner/ETB.git
	```

2. Go to the cloned directory and build ETB

	```console
	$ cd ETB && mvn compile
	```
	Maven will import all the dependencies and build ETB. You can use standard maven procedures to run ETB from command line or from editors like eclipse. The rest of this README explains how to run ETB commands from the command line.

	<!--  
	It is composed of ETB2, which is a complete reengineering of the Evidential Tool Bus (ETB) using Java integrated with
	the Distributed Evidence Network (DEN) implemented with Hyperledger Fabric.
	The distributed network acts as a substrate for a secure distributed
	execution of ETB2 services and it is based on a combination of distributed ledger technologies (DLT).
	The text **<span style="color:green">ETB built successfully</span>** at the end of the building process signals success.

	```console
	javac -cp .:dependencies/commons-exec-1.3.jar:dependencies/commons-io-2.6.jar:dependencies/json-simple-2.1.2.jar:dependencies/org.json.jar -d . etbDL/engine/* etbDL/utils/* etbDL/etbDatalog.java etbDL/etbDatalogEngine.java etbDL/statement/* etbDL/output/* etbDL/services/* etbCS/utils/* etbCS/etbNode.java etbCS/clientMode.java
	Note: Some input files use or override a deprecated API.
	Note: Recompile with -Xlint:deprecation for details.
	Note: Some input files use unchecked or unsafe operations.
	Note: Recompile with -Xlint:unchecked for details.
	$ ETB2 built successfully$
	```

	The text <span style="color:red">ETB has failed to build</span> signals problem with the building process. One possible problem could be the access right for the **build.sh** shell script file, and make sure that it is executable by running the command below.

	```console
	$ chmod -x build.sh
	```

	3. Let us set up ETB by adding the alias command **alias etb2='java -cp .:$dependencies/commons-exec-1.3.jar:dependencies/commons-io-2.6.jar:dependencies/json-simple-2.1.2.jar:dependencies/org.json.jar etb/etbCS/etbNode'** in your **bash_profile**, **bashrc** or similar locations.
	$ mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-add-claim \"advTwoStepCR('src/null_pointer.c', 'src/spec.c', Mr, Rr, Fr)\""

	-->

3. All available ETB commands can be seen by running the -help command.

	```console
	$ mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-help"
	```

	This displays the help menu of ETB.

	```console
	Overview:  ETB 2.0 - Evidential Tool Bus (Linux 64-bit version)

	Usage:     mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="[options] <inputs>"

	Options:

	-help/-h          shows this help menue
	-init <configFile>            initialises an entity at a given location
	-show-info        displays details of the node, like its port, claims, workflows, local services and available remote servers/services
	-show-modes      displays status of all evidentia modes
	-clean            removes available local services and remote servers from the server
	-uninit           deletes initialisation componenets of the node
	-set-port <port>        sets integer <port> as the port number of the entity
	-set-repo <dir>                 sets <dir> as the git repo used as working directory
	-set-mode <mode>                sets evidentia to the given mode, e.g., -noDEN, -DEN, etc.
	-add-service <configFile>       adds local service(s) to the server
	-rm-service <serviceID>                    removes local service(s) from the node
	-add-claim <query>          adds claim(s) to the etb node
	-rm-claim <claimID>                  removes claim(s) from the etb node
	-update-claim <claimID>     updates an outdated claim
	-upgrade-claim              upgrades an outdated claim
	-reconst-claim      reconstructs an outdated claim
	-export             exports services and workflows of the entity into a directory
	-import <dir>       imports services and workflows to the entity from the directory <dir>
	```

##  Running ETB

For detailed descriptions of the functions of the ETB please refer to [here](https://git.fortiss.org/evidentia/etb).


## Thesis Prototype

The scenario that will be demonstrated through the prototype is the exchange of evidence supporting the fulfilment of components' safety requirements between suppliers.
This scenario was described in more detail in the thesis. Below we can observe the network diagram, highlighting the section which will be demonstrated through this prototype.

![alt text](/images/system_orgprot.png)


### Creating Nodes

After the network in the [evidentia prototype](https://github.com/ernesto-valentiner/evidentia-prototype) finished building, we will require 
in total three ETBNodes. To create the three nodes, clone the repository three times and assign a corresponding name to each ETBNode, such as 
etb_org2, etb_org4, and etb_org5, e.g.

### Setting up the nodes

Each node will have to follow this process to be set up correctly. Since we will have three nodes, each node will symbolize one organization. 
This means, that we will have an ETBNode for Org2, one for Org5 and one for Org4.

1. First open the /src/main/resources/networkConfig.properties file, and change the following variables to correspond to the node's organization.
    - entityName=org?
    - pemFilename=ca.org?.example.com-cert.pem
    - connectionProfileFilename=connection-org?.json
    - caClientUrl=https://localhost:*
    - MSPName=Org?MSP
 
    The ? should be changed with the corresponding organizations number. The * in the caClientUrl is the port for the client, and should be changed to:
    8054 for Org2; 11054 for Org4; and 15054 for Org5;
    The rest of the variables don't change.

2. After changing the variables, we need to add the specified files. Add the following files to the /src/main/resources/ folder for the corresponding organization of the node.
    - /evidentia-prototype/test-network/organizations/peerOrganizations/org?.example.com/ca/ca.org?.example.com-cert.pem
    
    - /evidentia-prototype/test-network/organizations/peerOrganizations/org?.example.com/connection-org?.json
    
    - /evidentia-prototype/test-network/organizations/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem
    
    - /evidentia-prototype/test-network/organizations/peerOrganizations/org1.example.com/connection-org1.json

   Each node also requires the ca and connection files from Org1, since there are executions in the chaincode which only be authorized by Org1.

3. In this third step, we create admins and users for the organizations, by executing the following command.
    ```console
    mvn exec:java -Dexec.mainClass="evidentia.Administrator"
    ```

    This command will create an admin and a user entity and store them in the wallet directory.

4. Each ETBNode also requires the admin coordinator and user coordinator. For this we execute the following command once in an ETBNode 
and then copy the created entities in de wallets of the other two nodes.

5. The next step involves initializing the node. For each node, open the /test/configFiles/initFile.txt file and set a free port for the 
node to use. Each node requires a different port.

    After setting the port run the following command to initialize the node.
    ```console
    mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-init test/configFiles/initFile.txt"
    ``` 

6. Once the node is initialized we can add workflows and services. Since each node will provide a different service and workflow, we will describe each one separately.
    - Org4: Execute the following command to create a service.
        ```console
        mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-add-service test/configFiles/org4/getFGModeIndicatorSR.txt"
        ```
         When the service is created, a file /src/main/java/evidentia/wrappers/getFGModeIndicatorSRWRP.java will be generated.
         This file will contain the code which the service will execute. Copy all the content from the /test/configFiles/org4/getFGModeIndicatorSR.java file and paste it in getFGModeIndicatorSRWRP.java.
         Finally change the variable "path" in getFGModeIndicatorSRWRP.java to the complete path to the directory /TempRepo/evidence of the Org2 ETBNode (e.g. /Users/Ernesto/BachelorThesis/ETB_org2/TempRepo/evidence).

    - Org5: Execute the following command to create a service.
        ```console
        mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-add-service test/configFiles/org5/getFGModeSelectorSR.txt"
        ```
         When the service is created, a file /src/main/java/evidentia/wrappers/getFGModeSelectorSRWRP.java will be generated.
         This file will contain the code which the service will execute. Copy all the content from the /test/configFiles/org5/getFGModeSelectorSR.java file and paste it in getFGModeSelectorSRWRP.java.
         Finally change the variable "path" in getFGModeSelectorSRWRP.java to the complete path to the directory /TempRepo/evidence of the Org2 ETBNode (e.g. /Users/Ernesto/BachelorThesis/ETB_org2/TempRepo/evidence).
         
     - Org2: Execute the following command to create a workflow.
          ```console
          mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-add-workflow test/configFiles/org2/getFGModeControllerSR.txt"
          ```
        Execute the following command to create a service.
         ```console
         mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-add-service test/configFiles/org2/generateFGModeControllerSR.txt"
         ```
         When the service is created, a file /src/main/java/evidentia/wrappers/generateFGModeControllerSRWRP.java will be generated.
         This file will contain the code which the service will execute. Copy all the content from the /test/configFiles/org2/generateFGModeControllerSR.java file and paste it in generateFGModeControllerSRWRP.java.
         Finally change the variable "path" in generateFGModeControllerSRWRP.java to the complete path to the directory /TempRepo/evidence of the Org2 ETBNode (e.g. /Users/Ernesto/BachelorThesis/ETB_org2/TempRepo/evidence).

7. The last step is to run ```mvn clean install ``` on each node to compile the changes.



### Run the workflow

The final step is to run the workflow and create the claims. The parameters are variables where the output of each service will be stored.
```console
mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-add-claim \"getFGModeControllerSR(P1, P2, P3)\""
```

### Useful Functions 
To get the information about a node run:
```console
mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-show-info"
```

To remove all the services, workflows and claims of a node run:
```console
mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-clean"
```

To uninitialize a node run:
```console
mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-uninit"
```







                  



