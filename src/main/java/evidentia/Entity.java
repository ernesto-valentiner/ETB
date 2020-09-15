package evidentia;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.*;
import java.io.*;

import evidentia.etbCS.serverMode;
import evidentia.etbCS.utils.*;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Contract;

/**
 * Defines an entity with unique name on a given IP and port
 * <p>
 * query string can be parsed to query expression and processed through {@link claimsPack#add(String, Entity)}
 * </p>
 * @see claimsPack
 */
public class Entity {
    
    String name;
    String IP = "127.0.0.1";
    int port = 0;
    EntityType type;
    final String walletPath = "wallet";
    
    String repoDirPath = System.getProperty("user.dir"); //gitRepo

    serversPackage serversPack = new serversPackage();
    servicePackage servicePack = new servicePackage();
    workFlowsPackage workflowsPack = new workFlowsPackage();
    claimsPack claims = new claimsPack();

    String paramsFilePath = System.getProperty("user.dir") + "/params.json";

    boolean modeNoDEN = false;
    
    /**
     * constructs an entity
     * <p>
     * @param modeNoDEN specifies the mode evidentia will be running with
     * </p>
     */
    public Entity(boolean modeNoDEN) {
        try {
            this.IP = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        }
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
        try {
            JSONParser parser = new JSONParser();
            JSONObject paramsJSON = (JSONObject) parser.parse(new FileReader(paramsFilePath));

            this.name = (String) paramsJSON.get("name");
            this.port = Integer.valueOf(paramsJSON.get("port").toString());
            this.repoDirPath = (String) paramsJSON.get("repoDirPath");

            this.servicePack = new servicePackage((JSONArray) paramsJSON.get("servicePack"));
            this.serversPack = new serversPackage((JSONArray) paramsJSON.get("servers"));
            this.workflowsPack = new workFlowsPackage(repoDirPath, (JSONArray) paramsJSON.get("workflows"));
            this. claims = new claimsPack((JSONArray) paramsJSON.get("claims"));

        } catch (FileNotFoundException e) {
            System.out.println("\u001B[31m[error]\u001B[30m no evidentia entity at this location (use -init to initialise an entity)");
        } catch (IOException | ParseException e) {
            //e.printStackTrace();
            System.out.println("\u001B[31m[error]\u001B[30m aprams file can not be read (use -uninit to re-initialise an entity)");
        }
        
        //only if DEN mode is on, DEN settings will be done
        this.modeNoDEN = modeNoDEN;
        if (!modeNoDEN) {
            setDENSettings();
        }
        
    }
    
    public Entity(String name, int port, String repoDirPath) {
        this.name = name;
        this.port = port;
        this.repoDirPath = repoDirPath;
    }
    
    public void setDENSettings() {
        
        try (InputStream input = new FileInputStream("src/main/resources/networkConfig.properties")) {

            Properties prop = new Properties();
            prop.load(input);
            
            this.name = prop.getProperty("entityName");
            String pemFilename = prop.getProperty("pemFilename");
            String connectionProfileFilename = prop.getProperty("connectionProfileFilename");
            String coordinatorConnectionProfilePath = prop.getProperty("coordProfileFilename");
            String caClientUrl = prop.getProperty("caClientUrl");
            String networkChannelName = prop.getProperty("networkChannelName");
            String chaincodeName = prop.getProperty("chaincodeName");

            this.type = new EntityType(connectionProfileFilename, coordinatorConnectionProfilePath, pemFilename,caClientUrl, this.name, networkChannelName, chaincodeName);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public String getRepoDirPath() {
        return repoDirPath;
    }

    public int getPort() {
        return this.port;
    }

    public serversPackage getServersPack() {
        return serversPack;
    }

    public servicePackage getServicePack() {
        return servicePack;
    }

    public workFlowsPackage getWorkflowsPack() {
        return workflowsPack;
    }

    public claimsPack getClaims() {
        return claims;
    }

    public String getWalletPath() {
        return this.walletPath;
    }
    
    //cleans service wrappers
    public void clean() {
        serversPack = new serversPackage();
        servicePack = new servicePackage();
        workflowsPack = new workFlowsPackage();
        claims = new claimsPack();
        
        try {
            File wrappersSrc = new File("src/main/java/evidentia/wrappers");
            File wrappersBin = new File("target/classes/evidentia/wrappers");
            FileUtils.cleanDirectory(wrappersSrc);
            FileUtils.cleanDirectory(wrappersBin);
            System.out.println("[\u001B[32mdone\u001B[30m] node cleaned successfully");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[\u001B[33mwarning\u001B[30m] problem while cleaning service wrappers");
        } catch (IllegalArgumentException e) {
            System.out.println("[\u001B[31merror\u001B[30m] ***critical error*** entity has no service wrappers");
        }
    }
    
    //TODO: should not be done by the entity - take it to the servicePack
    private void addServiceToDEN(String ccProfile) {
        try {
            Path walletPath = Paths.get(this.walletPath);
            Wallet wallet = Wallet.createFileSystemWallet(walletPath);
            //load a CCP
            Path connectionProfilePath = Paths.get(ccProfile);
            System.out.println(connectionProfilePath.toAbsolutePath().toString());

            Gateway.Builder builder = Gateway.createBuilder();
            builder.identity(wallet, "coord").networkConfig(connectionProfilePath).discovery(true);
            boolean userExists = wallet.exists("coord");
            if (!userExists) {
                System.out.println("an identity for the user \"" + this.name + "\" does not exist in the wallet");
                return;
            }

            Gateway gateway = builder.connect();
            Network network = gateway.getNetwork(this.type.getNetworkChannelName());
            Contract contract = network.getContract(this.type.getChaincodeName());

            List<String> combinedServicesQueries = this.workflowsPack.getQueryIDs();
    
            combinedServicesQueries.addAll(Arrays.asList(servicePack.getServices().keySet().stream().toArray(String[]::new)));
            String combinedServiceQuery = "[" + String.join(",", combinedServicesQueries) + "]";
            contract.submitTransaction("addServiceProvider", this.name, this.IP, this.port+"", combinedServiceQuery);

            System.out.println("transaction has been submitted");
        }
        catch (Exception e) {
            System.out.println("\u001B[31m[error]\u001B[30m can not add service to the network");
            e.printStackTrace();
        }
    }

    private void setRepoDir(String inDirPath) {
        File repoDir = new File(inDirPath);
        if (repoDir.exists() && repoDir.isDirectory()){
            try {
                File repoDirCanonical = new File(repoDir.getCanonicalPath());
                this.repoDirPath = repoDirCanonical.getAbsolutePath();
                System.out.println("working directory successfully set");
                save();
            }
            catch (IOException e) {
                System.out.println("\u001B[31m[error]\u001B[30m canonical path for file not found");
                System.out.println(e.getMessage());
            }
        }
        else {
            System.out.println("\u001B[31m[error]\u001B[30m please provide a valid path");
        }
    }

    /*
    public void addClaim(int ID, claimSpec claim) {
        claims.add(ID, claim);
    }
    */
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("entity: " + name + " -- [IP: " + IP + ", port: " + port + "]");
        sb.append("\n==> local repo path : " + repoDirPath);
        sb.append(claims);
        sb.append(workflowsPack);
        sb.append(servicePack);
        sb.append(serversPack);
        return sb.toString();
    }

    public void save() {
        try {
            FileWriter fw = new FileWriter(paramsFilePath);
            fw.write(this.toJSONObject().toJSONString());
            fw.flush();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSONObject() {
        JSONObject entityJSON = new JSONObject();
        entityJSON.put("name", this.name);
        entityJSON.put("port", this.port);
        entityJSON.put("repoDirPath", this.repoDirPath);
        entityJSON.put("servicePack", servicePack.toJSONObject());
        entityJSON.put("servers", serversPack.toJSONObject());
        entityJSON.put("workflows", workflowsPack.toJSONObject());
        entityJSON.put("claims", claims.toJSONObject());
        return entityJSON;
    }
    
    public String getName() {
        return name;
    }

    public String getIP() {
        return IP;
    }

    public EntityType getEntityType() {
        return type;
    }

    public boolean noDENMode() {
        return modeNoDEN;
    }
    
    private void export() {
        String exportDirPath = "evidentia-export-IP"+ this.IP + "-Port" + this.port;
        File exportDir = new File(exportDirPath);
        if (!exportDir.isDirectory()) {
            exportDir.mkdir();
        }
        
        try{
            FileUtils.cleanDirectory(exportDir);
            //export services ?? wrappers
            FileUtils.copyDirectory(new File("src/main/java/evidentia/wrappers"), new File(exportDirPath+"/wrappers"));
            
            //export workflows ?? copy workflow scripts by workflowID
            Map<String, workFlowSpec> workflows = workflowsPack.getWorkflows();
            for (String workFlowID : workflows.keySet()) {
                FileUtils.copyFile(new File(workflows.get(workFlowID).getScriptPath()), new File(exportDirPath+"/workflows/" + workFlowID));
            }
            
            //export param file -- copy params
            FileUtils.copyFile(new File("params.json"), new File(exportDirPath+"/params.json"));
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        //scriptPath = scriptPath.replace(repoDirPath + "/","");
        System.out.println("\u001B[31m[success]\u001B[30m entity exported at location '" +  exportDir.getAbsolutePath() + "'");
    }
    
    private void importFrom(String importDirPath) {
        File importDir = new File(importDirPath);
        if (importDir.exists() && importDir.isDirectory()){
            try {
                JSONParser parser = new JSONParser();
                JSONObject toImportJSON = (JSONObject) parser.parse(new FileReader(importDirPath + "/params.json"));
                
                servicePack.importFrom(new servicePackage((JSONArray) toImportJSON.get("servicePack")), importDirPath);
                workflowsPack.importFrom(new workFlowsPackage(repoDirPath, (JSONArray) toImportJSON.get("workflows")), importDirPath);
                
            } catch (FileNotFoundException e) {
                System.out.println("\u001B[31m[error]\u001B[30m imported document does not have params file");
                e.printStackTrace();
            } catch (IOException | ParseException e) {
                System.out.println("\u001B[31m[error]\u001B[30m unreadable params file '" + importDirPath + "/params.json"+ "'");
                e.printStackTrace();
            }
        }
        else {
            System.out.println("\u001B[31m[error]\u001B[30m invalid import path '" + importDirPath + "'");
        }
    }
    
    public void run(String args[]) {

        if (args.length == 0) {
            serverMode SM = new serverMode();
            SM.run(this);
        }
        
        else if (args.length == 1) {
            if (args[0].equals("-show-info")){
                System.out.println(this);
            }
            else if (args[0].equals("-clean")) {
                clean();
                save();
            }
            else if (args[0].equals("-clean-claims")) {
                claims = new claimsPack();
                save();
            }
            else if (args[0].equals("-claims-status")){
                claims.checkStatus(servicePack, workflowsPack.getWorkflows(), repoDirPath);
            }
            else if (args[0].equals("-export")){
                export();
            }
            else {
                System.out.println("\u001B[31m[error]\u001B[30m ***critical error*** unknown option: " + args[0]);
            }
        }
        
        else if (args.length == 2) {
            if (args[0].equals("-set-port")){
                try {
                    this.port =  Integer.valueOf(args[1]);
                    save();
                }
                catch (NumberFormatException e) {
                    System.out.println("\u001B[31m[error]\u001B[30m non-numeric port value: " + args[1]);
                }
            }
            
            else if (args[0].equals("-set-repo")){
                setRepoDir(args[1]);
                save();
            }
            
            else if (args[0].equals("-add-claim")){
                claims.add(args[1], this);
                save();
            }
            
            else if (args[0].equals("-rm-claim")){
                claims.remove(Integer.parseInt(args[1]));
                save();
            }
            
            else if (args[0].equals("-add-service")){
                servicePack.add(args[1]);
                if (!modeNoDEN) {
                    addServiceToDEN(this.type.getCoordinatorProfilePath());
                }
                save();
            }
            
            else if (args[0].equals("-rm-service")){
                servicePack.remove(args[1]);
                save();
            }
            
            else if (args[0].equals("-add-server")){
                serversPack.add(args[1]);
                save();
            }
            
            else if (args[0].equals("-rm-server")){
                serversPack.remove(args[1]);
                save();
            }
            
            else if (args[0].equals("-add-workflow")){
                workflowsPack.add(args[1]);
                if (!modeNoDEN) {
                    addServiceToDEN(this.type.getCoordinatorProfilePath());
                }
                save();
            }
            
            else if (args[0].equals("-rm-workflow")){
                workflowsPack.remove(args[1]);
                save();
            }
            
            else if (args[0].equals("-update-claim")){
                claims.maintain(Integer.parseInt(args[1]), servicePack, this);
                save();
            }
            
            else if (args[0].equals("-import")){
                importFrom(args[1]);
                save();
            }
            
            else {
                System.out.println("\u001B[31m[error]\u001B[30m ***critical error*** unknown option: " + args[0]);
            }
        }
        
        else if (args.length == 3) {
            if (args[0].equals("-update-service")){
                servicePack.update(args[1], args[2]);
                save();
            }
            else {
                System.out.println("\u001B[31m[error]\u001B[30m ***critical error*** unknown option: " + args[0]);
            }
        }
        
        else {
            System.out.println("\u001B[33m[warning]\u001B[30m an entity already initialised at this location (use -h to see more options)");
            System.out.println("\u001B[31m[error]\u001B[30m invalid option(s) '" + String.join(" ", args) + "'");
        }
        
    }
    
}
