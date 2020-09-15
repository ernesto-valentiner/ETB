package evidentia.etbCS;

import java.util.*;

import evidentia.Entity;
import java.net.*;
import java.io.*;
import evidentia.etbDL.utils.*;
import evidentia.etbCS.utils.claimSpec;

public class serverMode {
        
    public void run(Entity node) {
        
        String repoDirPath = node.getRepoDirPath();
        int port = node.getPort();
        
        while (true) {
            System.out.println(node.toString());
            System.out.println("waiting for a client ...");
            try (
                 ServerSocket serverSocket = new ServerSocket(port);
                 Socket clientSocket = serverSocket.accept();
                 InputStream inStr = clientSocket.getInputStream();
                 DataInputStream fromClientData = new DataInputStream(inStr);
                 OutputStream outStr = clientSocket.getOutputStream();
                 DataOutputStream toClientData = new DataOutputStream(outStr);
                 ){
                System.out.println("connected to client : " + clientSocket.getInetAddress().getHostAddress());
                String request;
                if ((request = fromClientData.readUTF()) == null) {
                    System.out.println("null request found (liveness check?)");
                }
                else if (request.equals("regstReqst")){
                    System.out.println("this is a service registration request");
                    System.out.println("registration being done ... ");
                    //TODO: announce all my services
                    //grabbing services and workflow queries provides by the entity
                    List<String> combinedServicesQueries = node.getWorkflowsPack().getQueryIDs();
                    combinedServicesQueries.addAll(Arrays.asList(node.getServicePack().
                                getServices().keySet().stream().toArray(String[]::new)));
                    //TODO: better way of sending nodeName
                    combinedServicesQueries.add(0, node.getName());
                    toClientData.writeUTF(String.join(" ", combinedServicesQueries));
                    System.out.println("registration completed");
                }
                else if (request.equals("execReqst")){
                    System.out.println("=> request received for service execution");
                    //reading service details from client
                    String serviceName = fromClientData.readUTF();
                    System.out.println("-> serviceName : " + serviceName);
                    String serviceInvMode = fromClientData.readUTF();
                    System.out.println("-> serviceInvMode : " + serviceInvMode);
                    //reading service args from client
                    List<String> serviceArgs = getArgsFromClient(repoDirPath, fromClientData, toClientData);
                    System.out.println("-> serviceArgs : " + serviceArgs.toString());
                    String serviceSign = fromClientData.readUTF();
                    System.out.println("-> serviceSign : " + serviceSign);
                    
                    //re-creating query from passed arguments
                    Expr query = new Expr(serviceName, serviceArgs);
                    query.setMode(serviceInvMode);
                    query.setSignature(serviceSign);
                    
                    //Expr execResultExpr = node.getClaims().add(query, node);
                    claimSpec claim = node.getClaims().add(query, node);
                    if (claim == null) {
                        System.out.println("=> \u001B[31mquery execution not successful\u001B[30m (service: " + serviceName + ")");
                    }
                    else {
                        Expr execResultExpr = claim.getClaimExpr();
                        System.out.println("-> query execution done");
                        System.out.println("-> execution result: " + execResultExpr);
                        node.save();
                        List<String> resultArgs = execResultExpr.getTerms();
                        Iterator<String> resultIter = resultArgs.iterator();
                        while (resultIter.hasNext()) {
                            toClientData.writeUTF(resultIter.next());
                        }
                        toClientData.writeUTF("done");
                        toClientData.writeUTF(claim.getEvidence());
                    }
                    
                }
                else if (request.equals("statusCheck")){
                    System.out.println("request received for availability check");
                }
                else {
                    System.out.println("unknown request type");
                }
                clientSocket.close();
                serverSocket.close();
            } catch (IOException e) {
                System.out.println("error while trying to listen on port " + port + " or connection error");
                System.out.println(e.getMessage());
            }
        }
    }

    public static String getFile(String claimWorkingDir, String fileName, DataInputStream fromClientData) throws IOException {
        //needs to check first if claim already exists in the server
        //then the computation follows
        fileName = claimWorkingDir + "/temp_" + (new File(claimWorkingDir).list().length + 1) + "_" + fileName;
        System.out.println("fileName: " + fileName);
        //File fout = new File(fileName);
        //FileOutputStream fos = new FileOutputStream(fout);
        FileOutputStream fos = new FileOutputStream(new File(fileName));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        
        String line;
        while(!(line = fromClientData.readUTF()).equals("EOF")) {
            bw.write(line);
            bw.newLine();
        }
        bw.close();
        fos.close();
        return fileName;
    }

    private List<String> getArgsFromClient(String repoDirPath, DataInputStream fromClientData, DataOutputStream toClientData) throws IOException {
        List<String> serviceArgs = new ArrayList<>();
        String serviceArgType;
        while(!(serviceArgType = fromClientData.readUTF()).equals("done")) {
            System.out.println("serviceArgType : " + serviceArgType);
            if (serviceArgType.equals("file_list")) {
                //argument is of type file_list
                System.out.println("-> a list of files as arg");
                String eachFilePath, listRead = "listIdent";
                while(!(eachFilePath = fromClientData.readUTF()).equals("file_list_done")) {
                    System.out.println("\t\t -> file : " + eachFilePath);
                    eachFilePath = getFileArgFromClient(eachFilePath, fromClientData, toClientData, repoDirPath);
                    listRead += " file(" + eachFilePath + ")";
                }
                System.out.println("new argument after reading files : " + listRead);
                serviceArgs.add(listRead);
            }
            else if (serviceArgType.equals("file")) {
                //argument is of type file
                System.out.println("-> a file as arg");
                String fileElement = fromClientData.readUTF();
                System.out.println("\t -> file : " + fileElement);
                fileElement = getFileArgFromClient(fileElement, fromClientData, toClientData, repoDirPath);
                serviceArgs.add("file(" + fileElement + ")");
            }
            else {
                //argument is of type string or string_list
                serviceArgs.add(fromClientData.readUTF());
            }
        }
        return serviceArgs;
    }
    
    private String getFileArgFromClient(String fileElement, DataInputStream fromClientData, DataOutputStream toClientData, String repoDirPath) throws IOException {
        String SHA1 = fromClientData.readUTF();
        System.out.println("\t -> file SHA1 : " + SHA1);
        if (utils.existsInRepo(fileElement, repoDirPath) && utils.getSHA1(fileElement).equals(SHA1)) {
            System.out.println("\t\t -> file in server repo and SHA1 matches");
            toClientData.writeUTF("done");
        }
        else {
            System.out.println("\t\t -> file NOT in server repo or SHA1 does not match");
            toClientData.writeUTF("sendMeCopy");
            File eachFile = new File(fileElement);
            fileElement = copyFileFromClient(repoDirPath, eachFile.getName(), fromClientData);
        }
        return fileElement;
    }
    
    public static String copyFileFromClient(String repoDirPath, String fileName, DataInputStream fromClientData) throws IOException {
        //setting up name for new files in server (e.g., repoDir/TEMP/temp_1_cbmc.json)
        String claimWorkingDirPath = repoDirPath + "/TEMP";
        File claimWorkingDir = new File(claimWorkingDirPath);
        if (!claimWorkingDir.isDirectory()) {
            claimWorkingDir.mkdir();
        }
        fileName = claimWorkingDirPath + "/temp_" + (claimWorkingDir.list().length + 1) + "_" + fileName;
        
        System.out.println("fileName: " + fileName);
        
        FileOutputStream fos = new FileOutputStream(new File(fileName));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        String line;
        while(!(line = fromClientData.readUTF()).equals("EOF")) {
            bw.write(line);
            bw.newLine();
        }
        bw.close();
        fos.close();
        return fileName;
    }

}
