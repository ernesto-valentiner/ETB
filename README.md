
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

Optional
- eclipse

### Installation and preparation

1. Clone the ETB project  

	```console
	$ git clone https://git.fortiss.org/evidentia/etb.git
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

If we run ETB with the '-show-info' (or any other option) now, we get a notification that no ETB node is currently initialized at the given location.

```console
$ mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-show-info"
[error] no ETB node at this location (use -init to initialise an ETB node)
```

In this section, we see how an ETB node can be initialized on a local machine and used for integrating and continuous verification tasks to generate verification claims supported by evidences.

### Node initialisation

The first step in using ETB is initializing an ETB node, which is done by running ETB with the option **-init <PathToInitFile\>**.
The initialization file specifies a port number and a directory to be used by the ETB node.
This specification is written in json.   
For example, the file *test/configFiles/init.txt*, which is part of this distribution, contains the following json object.

```json
{
	"port": "4010",
	"repoDirPath": "TempRepo"
}
```

This specification will be used to initialize an ETB node at port **4010**, and the new node uses the directory **TempRepo** as its workspace.
If no director with that name exists, ETB will create it during the initialization process.

We run the command below to initialize the ETB node.

```console
$ mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-init test/configFiles/initFile.txt"
```

A successful initialization of the node is signaled by the message below.

```console
ETB node initialized (use -h to see more options to update the node)
```

Once a node is initialized, if we run ETB with the option '-show-info', we get information about the new node, as shown below.

```console
$ mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-show-info"
hostIP : 192.168.0.32
port : 4010
git repo path : ETB/TempRepo
==> total number of claims: 0
==> total number of workflows: 0
==> total number of local services: 0
==> total number of servers: 0
```

You can see that the port number and the git repo provided above during the initialization step in the node info. The hostIP parameter is the host machine's IP address, which is automatically read by ETB during the node initialization.

Note that the number of claims, workflows, local services and servers are all set to zero. This is not surprising as the node is just created and we have not yet any of these components.

### Adding service

Any service can be made available to the node by adding the service using the option **-add-service <PathToServiceSpecFile\>**, where the second argument is path to a specification file of the service.

This specification file contains a json object with the following 3 entires.

- *ID* - a unique identifier for the service.

- *signature* - a list of argument types for a given service.

	We currently support 4 argument types in ETB; *string*, *file*, *string_list* and *file_list*.
	Syntactically, a service signature is a list of such types separated by (at least one) space.
	For example, the signature '*file file_list string*' specifies that a given service has *file* as type for its first argument, *list of files* as type for its second argument, and *string* as type for its third argument.

- *modes* - a set of possible service invocation modes.

	A service mode for a given service defines which of the service arguments are given as inputs during its invocation, and which of its arguments are outputs expected to be produced at the end of its execution.
	Inspired by prolog predicate input/output notation, we use **+** for input arguments and **-** for output arguments.
	For example, the mode **++-** for a service with three arguments specifies that the first two arguments are inputs and the third argument is the output of the service.
	In ETB, a service can be invoked in different modes at different times, and hence, a set of such possible modes is one specification of the service.
	An example set of modes for a given service is is **{++-, +++}**.


For example, the file *test/configFiles/service.txt*, which is part of this distribution, contains a service specification in the form of the json object given below.

```json
{
	"ID": "genPDF",
	"signature": ["file", "file", "file"],
	"modes":["++-"]
}
```

We run the command below to add a service, e.g., the *genPDF* service specified above.

```console
$ mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-add-service test/configFiles/service.txt"
```

A successful addition of the service to the node is signaled by the message below.

```console
=> service added successfully
```

By adding a service to the node, we make a given service available for the workflows to use the service.
The definition of the service, i.e., what is does with the inputs and how it produces its output must be specified by the service itself.
ETB enables this by automatically generating wrappers for each added service.
You can find the generated wrappers in the location *src/main/java/evidentia/wrappers*.
For each added service, ETB generates two wrapper template files, called ETB and user wrapper files.
For example, ETB generates the ETB wrapper file *genPDFETBWRP.java*, and the user wrapper file *genPDFWRP.java* for the *genPDF* service added above.

An ETB wrapper file, e.g., *genPDFETBWRP.java*, is used to automatically define all inputs and outputs of the service together their types, service invocation modes, evidence created during the service invocation, etc.
The ETB wrapper file is only for internal use by ETB.

A user wrapper file, e.g., *genPDFWRP.java*, extends the corresponding ETB wrapper file, and specifies the relation between service inputs and outputs.
This is done by overriding the *run* method.
You can see below the automatically generated *genPDFWRP.java* that the user needs to modify for the service to do something useful.

```java
public class genPDFWRP extends genPDFETBWRP {

	@Override
	public void run(){
		if (mode.equals("++-")) {
			//do something
		}
		else {
			System.out.println("unrecognized mode for genPDF");
		}
	}
}
```

As the *genPDF* service has three arguments, which can be inputs or outputs depending on the mode, ETB defines six variables in1, in2, and in3 for inputs and out1, out2, and out3 for outputs.
This definition is put in the ETB wrapper file *genPDFETBWRP.java*
As we specified above, the service has one mode, which is *++-*.
This means during the invocation of this mode, *in1* and *in2* will have input values, and ETB expects the result of the service execution to be written to *out3*.

You can see below the modified *run* method in the user wrapper.
The modified method now processes the input files *in1* and *in2*, and generates another file *out3*.
Remember, this service has *<file, file, file>* signature.

```java
	@Override
    public void run(){
        if (mode.equals("++-")) {
            File SourceFile = new File(in1);
            String FILEDIR = SourceFile.getParent();
            out3 = FILEDIR + "/" + in2 + "PDFreport.pdf";
            try {

                JSONParser parser = new JSONParser();
                Object JsonObj = parser.parse(new FileReader(in1));
                JSONArray Errors = (JSONArray) JsonObj;
                getReport(Errors, FILEDIR);


            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("unrecognized mode for genPDF");
        }
    }
```

If the service could also be invoked in another mode, say *+--*, the *run* method will have another block for handing this mode.
It will be the user's responsibility to write the code which takes *in1* and generates *out2* and *out3* for this case as well.
As this wrappers are doing the input-to-output transformation themselves without calling external tools, they are acting as services.
However, the real power of ETB is when external services, e.g., testing or verification tools, are used in transforming the service inputs to outputs.  
In this case, the wrapper's role will be just formatting inputs and outputs, and hence, the name wrapper is more fitting.

### Removing service

If a service is no more required by an ETB node, it can be removed by running ETB with the option **-rm-service <serviceID\>**.
For example, to remove the service with ID *genPDF*, we run the command below.

```console
$ mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-rm-service genPDF"
```

A successful removal of the service from the node is signaled by the message below.

```console
=> service removed successfully
```

### Adding workflows

ETB uses Datalog as a scripting language to define verification workflows that employ services to accomplish integrated verification.
A workflow should be added to an ETB node to automate its execution.
This is done by adding the workflow using the option **-add-workflow <workFlowSpecificationFile\>**, where the second argument is path to a specification file of the workflow.

This workflow specification file contains a json object with the following 3 entires:

- *ID* - a unique identifier of the workflow

- *script* - path to a workflow script, which is a Datalog program that defines the logic of a given verification process.
Since ETB uses this script during the execution of the corresponding workflow, make sure workflow scripts are placed in the working directory of the node.

- *queries* - a list of queries (syntactically Datalog literals) that the workflow can compute the corresponding claims for.
	Syntactically, this list comprises a set of *query specifications*, where each *query specifications* is a tuple of the following 3 subcomponents separated by semicolons:

	- **name**: unique identifying string for the workflow

	- **signature**: list of data types in a bracket and separated by commas.
	An example *query signature* is *(file, file_list, string)*.

	- **mode**: invocation mode of the query. An example mode can be '*++-*'.

	An example *query list* is *{<twoStepCR; (file,file,file,file,file,file); +++++->, <threeStepCR; (file,file,file,file,file,file); +++++->}*.

 For example, the file *test/configFiles/workflow.txt*, which is part of this distribution, contains a workflow specification in the form of the json object given below.

```json
	{
		"ID": "twoStepCR",
		"script": "workflows/twoStepCR",
		"queries": [
			{
				"ID":"twoStepCR",
				"signature": ["file", "file", "file", "file"],
				"mode": "+---"
			}
		]
	}
```	

	We run the command below to add a workflow, e.g., the *twoStepCR* workflow specified above.

```console
$ mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-add-workflow test/configFiles/workflow.txt"
```

	A successful addition of the workflow to the node is signaled by the message below.

```console
=> workflow added successfully
```	

### Removing workflow

A workflow can be removed by running ETB with the option **-rm-workflow <workflowID\>**.
For example, to remove the workflow with ID *twoStepCR*, we run the command below.

```console
$ mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-rm-workflow twoStepCR"
```

A successful removal of the workflow from the node is confirmed by the following message.

```console
=> workflow removed successfully
```

### Adding claims

A claim is added to an ETB node using the option **-add-claim <query\>**, where the second argument is the query for whom the node is going to compute and add the corresponding claim.
An example query can be *twoStepCR('src/null_pointer.c', Mr, Rr, Fr)*, where *src/null_pointer.c* is the input, and the variables Mr, Rr and Fr represent the three outputs computed during the claim computation.

We run the command below to add a claim for the query specified above.

```console
$ mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-add-claim \"twoStepCR('src/null_pointer.c', Mr, Rr, Fr)\""
```
Since the query should be given as a string, we must put it in double quotations. The Note that the quotation marks are escaped(in backslash) as maven's *-Dexec.args* itself expects list of strings.
If you are running ETB from eclipse, you can simply use *-add-claim "twoStepCR('src/null_pointer.c', Mr, Rr, Fr)"* as argument without the use of backslashes.

A successful addition of the claim to the node is signaled by the message below.

```console
=> claim added successfully
```

### Removing claims

A claim can be removed by running ETB with the option **-rm-claim <claimID\>**.
You can run ETB with the option '-show-info' to see details of existing claims, including the ID of each claim.
For example, to remove the claim with ID *89283923*, we run the command below.

```console
$ mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-rm-claim 89283923"
```

A successful removal of the claim is confirmed by the following message.

```console
=> claim removed successfully
```

### Server mode
To run ETB in server mode run:
```console
$ mvn exec:java -Dexec.mainClass="evidentia.Evidentia"
```

### Node exporting and importing

Assume we have a node with a given set of services and workflows running at a given port and location.
If we want to have a different node, i.e., at different port or location, or even on another machine, node with the same services and workflows, ETB has the ability of exporting contents of the node and importing the contents to the other port.
Of course, the other port need to be initialized first.

To import contents of a given node, ETB is run with the option **-export <exportDir\>**, where the second argument is where the exported contents will be stored.

```console
$ mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-export TEMP/toExport"
```

For example, after running the command above, ETB will export its content of the node to the directory *TEMP/toExport*.
This contents can be imported to another node by running ETB with option **-import <importDir\>**, where the second argument is a directory containing the contents to be  imported.
For example, if we have the contents to be imported (which are exported from another node) in the directoy *toImport*, we run the command below to import its contents to the node.

```console
$ mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-import toImport"
```



### Node cleaning

If we want to remove all the service and claims in a given node, we can run ETB with the '-clean' option.

```console
$ mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-clean"
```

### Node uninitialization

If we want to uninitialize an ETB node which was initialized at a given port and location, we can run ETB with the '-uninit' option.

```console
$ mvn exec:java -Dexec.mainClass="evidentia.Evidentia" -Dexec.args="-uninit"
```

Note that uninitialising a node results in permanent deletion of its working directory.

## Getting Started with the Distributed Evidence Network (DEN)
1. Start the distributed evidence network by following the instructions [here](https://git.fortiss.org/evidentia/den).
2. Copy cert.pem files from `crypto-config/peerOrganizations/../ca/` into `src/main/java/resources`  
3. Copy `connection-profile.json` file to `src/main/java/resources`
4. Create a coordinator admin (admin_coordinator) and register coordinator user(coord).

### Start Entity
First, edit the `networkConfig.properties` file (found in src/main/resources/) with the network configurations.

 Then, run `Administrator.java` in order to:

    - enroll admin for the specified entity
    - register a user for the specified entity

Copy the admin and user coordinator identities (created in step 4) in the wallet directory.
