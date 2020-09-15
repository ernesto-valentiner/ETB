package evidentia.etbCS;

import java.io.*;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeoutException;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.hyperledger.fabric.gateway.*;
import org.apache.commons.io.FileUtils;

import evidentia.Entity;
import evidentia.etbDL.utils.*;
import evidentia.etbDL.services.*;
import evidentia.etbCS.utils.*;


/**
 * This class handles orchestration of a query to local and remote nodes.
 * <p> When the query can be handled by a local node, the query is sent to the appropriate wrapper.
 * Otherwise, a Client Mode is instantiated to send the query to a remote node that provides the service for the query.
 * Once the query is processed remotely, its result is returned.
 * </p>
 */
public class Orchestrator {
    //TODO: more than one results per service invocation
    Expr query;
    Expr result;
    String evidence;
    boolean local = false;

    public Orchestrator(Expr query) {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
        this.query = query;
        this.evidence = "{service:" + this.query.getPredicate() + ", version:1.0, result:okay}";
    }
        
    public void process(Entity node) {
        if (node.noDENMode()) {
            this.processNoDEN(node);
        }
        else {
            //TODO: sending claimID
            this.processDEN(node);
        }
    }

    private void invoke(Expr query, String signature, String repoDirPath) {
        try {
            Class<?> wrapperClass = Class.forName("evidentia.wrappers." + query.getPredicate() + "WRP");
            genericWRP genWRP = (genericWRP) wrapperClass.newInstance();
            genWRP.invoke(query.getMode(), query.getTerms(), repoDirPath);
            this.result = new Expr(query.getPredicate(), genWRP.getOutParams(), signature, query.getMode());

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            System.out.println("\u001B[31mmissing service wrapper\u001B[30m please check '" + query.getPredicate()+ "' service)");
            e.printStackTrace();
        }
    }

    public Expr getResult(){
        return result;
    }

    public String getEvidence() {
        return evidence;
    }

    public boolean isLocal() {
        return local;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--> query: " + query);
        if (result.equals(null)){
            sb.append("--> result: null (\u001B[31m [warning]\u001B[30m service could not be invoked)");
        }
        else {
            sb.append("--> result: " + result);
        }
        if (evidence.equals(null)) {
            sb.append("--> evidence: null (\u001B[31m [warning]\u001B[30m please check the wrapper)");
        }
        else {
            sb.append("--> evidence: " + evidence);
        }
        return sb.toString();
    }
    
    //********** DEN-specific methods
    /**
     * Handles service execution via the DEN
     * @param entity - the entity from which execution request is sent
     */
    public void processDEN(Entity entity) {
        servicePackage servicePack = entity.getServicePack();
        String serviceID = query.getPredicate();
        System.out.println("getting service providers for service: " + serviceID);
        String serviceParams = String.join(",", query.getTerms());
        String serviceProviders = this.getServiceProviders(entity, serviceID, serviceParams);
        if (serviceProviders.equals("")) {
            System.out.println("no matching service provider found for service: " + serviceID);
            return;
        }
        
        String[] providers = serviceProviders.split(",");
        boolean islocalService = false;
        for(int i = 0; i < providers.length; i+=3) {
            if (providers[i].equals(entity.getIP()) && providers[i+1].equals(entity.getPort()+"")) {
                islocalService = true;
                break;
            }
        }

        if (islocalService) {
            boolean isService = entity.getServicePack().getServices().keySet().contains(serviceID);
            if(isService) {
                this.updateExecutionTarget(entity, serviceID, serviceParams, entity.getName());
                System.out.println("\t -> query processing as a local service");
                System.out.println(servicePack.get(serviceID).toString());
                this.invoke(query, servicePack.get(serviceID).getSignature(), entity.getRepoDirPath());
                local = true;
                this.updateExecutionResponse(entity, serviceID, serviceParams);
            }
            //TODO: double check else case - when local but not a service, e.g., fact or rule
        }
        else {
            for(int i = 0; i < providers.length; i+=3) {
                System.out.println("\t -> processing as a remote service");
                System.out.println("\t\t -> server spec " + providers[i] + " " + providers[i+1] + " " + providers[i+2]);
                clientMode cm = new clientMode(providers[i], Integer.parseInt(providers[i+1]), entity.getRepoDirPath());
                if (cm.isConnected()) {
                    //update target <providers[i+2]> using the remote servers' username but in the source service_execution obj
                    this.updateExecutionTarget(entity, serviceID, serviceParams, providers[i+2]);
                    this.result = cm.remoteServiceExecution(query);
                    this.evidence = cm.getEvidence();
                    //update response and evidence in the source service_execution obj
                    this.updateExecutionResponse(entity, serviceID, serviceParams);
                    //TODO: special tactic/heuristic for more servers per service?
                    break;
                }
            }
        }
    }

    private void updateExecutionResponse(Entity entity, String serviceID, String serviceParams) {
        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get(entity.getWalletPath());
        Wallet wallet = null;
        try {
            wallet = Wallet.createFileSystemWallet(walletPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // load a CCP
        Path networkConfigPath = Paths.get(entity.getEntityType().getConnectionProfilePath());
        System.out.println(networkConfigPath.toAbsolutePath().toString());

        Gateway.Builder builder = Gateway.createBuilder();
        try {
            builder.identity(wallet, entity.getName()).networkConfig(networkConfigPath).discovery(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Check to see if we've already enrolled the user.
        boolean userExists = false;
        try {
            userExists = wallet.exists(entity.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!userExists) {
            System.out.println("An identity for the user \"" + entity.getName() + "\" does not exist in the wallet");
            return;
        }

        // create a gateway connection
        Gateway gateway = builder.connect();

        // get the network and contract
        Network network = gateway.getNetwork(entity.getEntityType().getNetworkChannelName());
        Contract contract = network.getContract(entity.getEntityType().getChaincodeName());

        String response = this.result.getTerms().toString();
        try {
            contract.submitTransaction("updateServiceExecutionResponse", serviceID, serviceParams, this.evidence, response);
            System.out.println("Transaction has been submitted");
        } catch (InterruptedException e) {
            System.out.println("\u001B[31m[error]\u001B[30m can not update service execution response");
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.out.println("\u001B[31m[error]\u001B[30m can not update service execution response");
            e.printStackTrace();
        } catch (ContractException e) {
            System.out.println("\u001B[31m[error]\u001B[30m can not update service execution response");
            e.printStackTrace();
        }
    }

    //TODO: add entity object in class
    private void updateExecutionTarget(Entity entity, String serviceName, String serviceParams, String targetID) {
        // Load a file system based wallet for managing identities.
        Path walletPath = Paths.get(entity.getWalletPath());
        Wallet wallet = null;
        try {
            wallet = Wallet.createFileSystemWallet(walletPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Path connectionProfilePath = Paths.get(entity.getEntityType().getConnectionProfilePath());
        System.out.println(connectionProfilePath.toAbsolutePath().toString());

        Gateway.Builder builder = Gateway.createBuilder();
        try {
            builder.identity(wallet, entity.getName()).networkConfig(connectionProfilePath).discovery(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Check to see if we've already enrolled the user.
        boolean userExists = false;
        try {
            userExists = wallet.exists(entity.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!userExists) {
            System.out.println("An identity for the user \"" + entity.getName() + "\" does not exist in the wallet");
            return;
        }

        // create a gateway connection
        Gateway gateway = builder.connect();

        // get the network and contract
        Network network = gateway.getNetwork(entity.getEntityType().getNetworkChannelName());
        Contract contract = network.getContract(entity.getEntityType().getChaincodeName());

        try {
            contract.submitTransaction("updateServiceExecutionTarget", serviceName, serviceParams, targetID);
            System.out.println("Transaction has been submitted");
        } catch (ContractException e) {
            System.out.println("\u001B[31m[error]\u001B[30m can not update service execution target");
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.out.println("\u001B[31m[error]\u001B[30m can not update service execution target");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("\u001B[31m[error]\u001B[30m can not update service execution target");
            e.printStackTrace();
        }
    }

    private String getServiceProviders(Entity entity, String serviceName, String serviceParams) {
        //load a file system based wallet for managing identities.
        Path walletPath = Paths.get(entity.getWalletPath());
        Wallet wallet = null;
        try {
            wallet = Wallet.createFileSystemWallet(walletPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //load a CCP
        Path networkConfigPath = Paths.get(entity.getEntityType().getConnectionProfilePath());
        Gateway.Builder builder = Gateway.createBuilder();
        try {
            builder.identity(wallet, entity.getName()).networkConfig(networkConfigPath).discovery(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //check if the user has already beed enrolled
        boolean userExists = false;
        try {
            userExists = wallet.exists(entity.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!userExists) {
            System.out.println("An identity for the user \"" + entity.getName() + "\" does not exist in the wallet");
            return null;
        }
        // create a gateway connection
        byte[] result = null;
        Gateway gateway = builder.connect();
        //get the network and contract
        Network network = gateway.getNetwork(entity.getEntityType().getNetworkChannelName());
        Contract contract = network.getContract(entity.getEntityType().getChaincodeName());
        try {
            result = contract.submitTransaction("requestServiceProvider", serviceName, serviceParams);
            System.out.println("Transaction has been evaluated, result is: " + new String(result));
        } catch (ContractException e) {
            System.out.println("\u001B[31m[error]\u001B[30m can not get service providers");
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.out.println("\u001B[31m[error]\u001B[30m can not get service providers");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("\u001B[31m[error]\u001B[30m can not get service providers");
            e.printStackTrace();
        }
        return new String(result);
    }
    
    //********** NoDEN-specific methods
    /**
     * Handles service execution using the default client-server service discovery
     * @param node - the entity from which execution request is sent
     */
    public void processNoDEN(Entity node) {
        if (this.query.getAuthority().getType().equals("unrestricted")) {
            //try local service
            processLocalService(node);
            //if not, then try remote services
            if (this.result == null) {
                processRemoteServers(node, new ArrayList());
            }
        }
        else if (this.query.getAuthority().getType().equals("restricted")) {
            if (this.query.getAuthority().getRestriction().contains(node.getName())) {
                //first try local service
                processLocalService(node);
                //if not, then try services of the restricted servers
                if (this.result == null) {
                    processRemoteServers(node, this.query.getAuthority().getRestriction());
                }
            }
            else {
                //try services of the restricted servers
                processRemoteServers(node, this.query.getAuthority().getRestriction());
            }
        }
        else if (this.query.getAuthority().getName().equals(node.getName())){
            //fixed local server
            processLocalService(node);
        }
        else {
            //fixed remote server
            processRemoteService(node, this.query.getAuthority().getName());
        }
    }

    public void processLocalService(Entity node) {
        servicePackage servicePack = node.getServicePack();
        serversPackage serversPack = node.getServersPack();
        String serviceID = query.getPredicate();
        
        if (servicePack.containsService(serviceID)) {
            System.out.println("\t -> query processing as a local service");
            System.out.println(servicePack.get(serviceID).toString());
            String claimDirPath = node.getRepoDirPath() + "/claims/claim_" + query.hashCode();
            
            //this.invoke(query, servicePack.get(serviceID).getSignature(), claimDirPath + "/" + serviceID);
            //setEvidence(claimDirPath, servicePack.get(serviceID).getSignature());
            
            StorageLayer storage = new StorageLayer(query);
            storage.run(servicePack.get(serviceID).getSignature(), claimDirPath);
            this.result = storage.getClaim();
            this.evidence = storage.getEvidence();
            
            
            local = true;
        }
        else {
            System.out.println("\t -> no local service found matching the query");
        }
    }

    //remote service execution for a fixed server
    public void processRemoteService(Entity node, String authorityID) {
        String serviceID = query.getPredicate();
        // grabbing the server/entity for the authority
        serverSpec serviceProvider = node.getServersPack().getServers().get(authorityID);
        System.out.println("\t -> processing as a remote service");
        System.out.println("\t\t -> server spec " + serviceProvider);
        clientMode cm = new clientMode(serviceProvider.getIP(), serviceProvider.getPort(), node.getRepoDirPath());
        if (cm.isConnected()) {
            this.result = cm.remoteServiceExecution(query);
            this.evidence = cm.getEvidence();
        }
    }
    
    //processes remote servers with out without restriction
    public void processRemoteServers(Entity node, List<String> restriction) {
        String serviceID = query.getPredicate();
        serversPackage serversPack = node.getServersPack();
        //grabs all entities that can process the query
        List<serverSpec> serviceProviders = Arrays.asList(serversPack.getServers().values().stream().filter(eachServer -> eachServer.getServices().contains(serviceID)).toArray(serverSpec[]::new));
        if (!restriction.isEmpty()) {
            //grabs the restricted entities from
            serviceProviders = Arrays.asList(serviceProviders.stream().filter(eachServer -> restriction.contains(eachServer.getName())).toArray(serverSpec[]::new));
        }
        Iterator<serverSpec> serverIter = serviceProviders.iterator();
        while (serverIter.hasNext()) {
            serverSpec serviceProvider = serverIter.next();
            System.out.println("\t -> processing as a remote service");
            System.out.println("\t\t -> server spec " + serviceProvider);
            clientMode cm = new clientMode(serviceProvider.getIP(), serviceProvider.getPort(), node.getRepoDirPath());
            if (cm.isConnected()) {
                this.result = cm.remoteServiceExecution(query);
                this.evidence = cm.getEvidence();
                //TODO: special heuristic for more servers per service?
                break;
            }
        }
    }
    
}
