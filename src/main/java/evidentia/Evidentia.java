package evidentia;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.net.*;
import java.io.*;
import java.util.*;

/**
* Defines  an EVIDENTIA class.
* <p>
* The entity will be instantiated at a given location by {@link initialise} by taking the port and repoDirPath.
* The method makes use of the machine's IP address as its hostIP.
* </p>
* @see validCMD
*/
public class Evidentia {
    
    boolean modeNoDEN = false;
	String prefsFilePath = System.getProperty("user.dir") + "/prefs.json";
    String paramsFilePath = System.getProperty("user.dir") + "/params.json";

    public Evidentia() {
        File prefsFile = new File(this.prefsFilePath);
        if (prefsFile.exists()) {
            try {
                JSONParser parser = new JSONParser();
                JSONObject prefsJSON = (JSONObject) parser.parse(new FileReader(prefsFilePath));
                this.modeNoDEN = (boolean) prefsJSON.get("modeNoDEN");

            } catch (IOException | ParseException e) {
                System.out.println("\u001B[31m[error]\u001B[30m problem reading evidentia params file");
            }
        }
    }

    public boolean isModeNoDEN() {
		return modeNoDEN;
	}
    
    private boolean validCMD(String args[]) {
        
        if (args.length == 0) {
            return true;
        }
        
        Map<Integer, List<String>> cmdOptions = new HashMap<>();
        String[] options0Len1 = {"-help", "-h", "-show-modes", "-uninit", "-clean", "-clean-claims", "-show-info", "-claims-status", "-export"};
        List<String> optionsLen1 = Arrays.asList(options0Len1);
        cmdOptions.put(1, optionsLen1);
        ArrayList<String> allOptions = new ArrayList(Arrays.asList(options0Len1));
        String[] options0Len2 = {"-init", "-set-port", "-set-repo", "-add-claim", "-rm-claim", "-add-service", "-rm-service", "-add-server", "-rm-server", "-add-workflow", "-rm-workflow", "-update-claim", "-import", "-set-mode"};
        List<String> optionsLen2 = Arrays.asList(options0Len2);
        cmdOptions.put(2, optionsLen2);
        allOptions.addAll(optionsLen2);
        
        //TODO: check during settting evidentia mode
        String[] allEvidModes = {"-DEN", "-noDEN"};
        List<String> evidModes = Arrays.asList(allEvidModes);
        
        String[] options0Len3 = {"-update-service"};
        List<String> optionsLen3 = Arrays.asList(options0Len3);
        cmdOptions.put(3, optionsLen3);
        allOptions.addAll(optionsLen3);
        
        if (cmdOptions.get(args.length).contains(args[0])) {
            return true;
        }
        else if (allOptions.contains(args[0])){
            System.out.println("\u001B[31m[error]\u001B[30m incorrect number of arguments for '" + args[0] + "' (use -h to see description for options)");
        }
        else {
            System.out.println("\u001B[31m[error]\u001B[30m unknown option '" + args[0] + "'");
        }
        return false;
    }

    public void run(String args[]) {
        
        if (!validCMD(args)) {
            return;
        }
        
        File paramsFile = new File(this.paramsFilePath);
        boolean existsEntity = paramsFile.exists();

        if (args.length == 1 && (args[0].equals("-help") || args[0].equals("-h"))){
            help();
        }
        else if (args.length == 1 && args[0].equals("-show-modes")){
            showModes();
        }
        
        else if (args.length == 2 && args[0].equals("-set-mode")) {
            //TODO: checking valid modes
            if (args[1].equals("-noDEN")){
                modeNoDEN = true;
                save();
            }
            else if (args[1].equals("-DEN")){
                modeNoDEN = false;
                save();
            }
            else {
                System.out.println("\u001B[31m[error]\u001B[30m invalid option(s) '" + String.join(" ", args) + "'");
            }
        }
        
        else if (args.length == 2 && args[0].equals("-init")) {
            if (existsEntity) {
                System.out.println("\u001B[31m[error]\u001B[30m an entity already initialised at this location (use -h to see more options)");
            }
            else {
                initialise(args[1]);
            }
        }

        else if (existsEntity) {
            if (args.length == 1 && args[0].equals("-uninit")) {
                uninitialise();
            }
            else {
                Entity entity = new Entity(modeNoDEN);
                entity.run(args);
            }
        }
        
        else {
            System.out.println("\u001B[33m[warning]\u001B[30m no entity initialised at this location (use -init to initialise)");
            System.out.println("\u001B[31m[error]\u001B[30m invalid option(s) '" + String.join(" ", args) + "'");
        }
    }
    
    //sets up a node in a given location -- with a config file
    public void initialise(String initFilePath) {
        String name, repoDirPath;
        int port;
        File paramsFile = new File(paramsFilePath);
        if (paramsFile.exists()){
            System.out.println("\u001B[31m[error]\u001B[30m an evidentia entity already initialised at this location (use -h to see more options)");
            return;
        }

        try {
            JSONParser parser = new JSONParser();
            JSONObject initSpecJSON = (JSONObject) parser.parse(new FileReader(initFilePath));
            name = (String) initSpecJSON.get("name");
            String port0 = (String) initSpecJSON.get("port");
            try {
                port = Integer.valueOf(port0.trim());
            }
            catch (NumberFormatException e) {
                System.out.println("\u001B[31m[error]\u001B[30m no port or non-numeric port given \u001B[31m(operation not successful)\u001B[30m");
                return;
            }

            repoDirPath = (String) initSpecJSON.get("repoDirPath");
            if (repoDirPath == null) {
                System.out.println("=> no valid repository is given \u001B[31m(operation not successful)\u001B[30m");
                return;
            }
            
            File repoDir = new File(repoDirPath.trim());
            repoDirPath = repoDir.getCanonicalPath();
            if (!repoDir.exists()){
                repoDir.mkdirs();
                System.out.println("\u001B[32m[done]\u001B[30m new directory created for local repo");
            }
            else if (repoDir.isDirectory()){
                System.out.println("\u001B[33m[warning]\u001B[30m existing directory used as local repo");
            }
            else {
                System.out.println("\u001B[31m[error]\u001B[30m please provide a valid path");
                return;
            }

            try {
                FileUtils.forceMkdir(new File("src/main/java/evidentia/wrappers"));
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("\u001B[33m[warning]\u001B[30m could not create dirctory for service wrappers");
            } catch (NullPointerException e) {
                System.out.println("\u001B[31m[error]\u001B[30m *** critical error *** null service wrappers");
            }
            
            System.out.println("[\u001B[32mdone\u001B[30m] evidentia entity initialised (use -h to see more options to update the node)");
            Entity initEntity = new Entity(name, port, repoDirPath);
            initEntity.save();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void uninitialise() {
        /** removing service wrappers */
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
        
        /** removing params file */
        if (FileUtils.deleteQuietly(new File("params.json"))) {
            System.out.println("[\u001B[32mdone\u001B[30m] node uninitialised successfully");
        }
        else {
            System.out.println("[\u001B[33mwarning\u001B[30m] no entity to uninitialize at this location");
        }
        
    }
    
    private void help() {
        System.out.println("\nOverview: Evidential version 1.0 (Linux 64-bit version)\n");
        System.out.println("Usage:     evidentia [options] [inputs]\n");
        System.out.println("Options: \n");
        System.out.println("-help/-h          shows this help menue");
        System.out.println("-init <configFile>            initialises an entity at a given location");
        System.out.println("-show-info        displays details of the node, like its port, claims, workflows, local services and available remote servers/services");
        System.out.println("-show-modes      displays status of all evidentia modes");
        System.out.println("-clean            removes available local services and remote servers from the server");
        System.out.println("-uninit           deletes initialisation componenets of the node");
        System.out.println("-set-port <port>        sets integer <port> as the port number of the entity");
        System.out.println("-set-repo <dir>                 sets <dir> as the git repo used as working directory");
        System.out.println("-set-mode <mode>                sets evidentia to the given mode, e.g., -noDEN, -DEN, etc.");
        System.out.println("-add-service <configFile>       adds local service(s) to the server");
        System.out.println("-rm-service <serviceID>                    removes local service(s) from the node");
        //System.out.println("-add-server                 adds remote server(s) whose services will avilable to the etb node");
        //System.out.println("-rm-server                  removes remote servers");
        System.out.println("-add-claim <query>          adds claim(s) to the etb node");
        System.out.println("-rm-claim <claimID>                  removes claim(s) from the etb node");
        System.out.println("-update-claim <claimID>     updates an outdated claim");
        System.out.println("-upgrade-claim              upgrades an outdated claim");
        System.out.println("-reconst-claim      reconstructs an outdated claim");
        System.out.println("-export             exports services and workflows of the entity into a directory");
        System.out.println("-import <dir>       imports services and workflows to the entity from the directory <dir>\n");
    }

    private void showModes() {
        System.out.println("\n*** Evidential version 1.0 (Linux 64-bit version)***\n");
        System.out.println("Framework settings\n");
            
        String modeNoDENSTR = "[\u001B[31mdisabled\u001B[30m]";
        if (modeNoDEN)
            modeNoDENSTR = "[\u001B[32menabled\u001B[30m]";
        
        System.out.println("-noDEN  : " + modeNoDENSTR);
        System.out.println("\n");

    }
    
    public void save() {
        JSONObject NewObj = new JSONObject();
        NewObj.put("modeNoDEN", this.modeNoDEN);
        try {
            FileWriter fw = new FileWriter(prefsFilePath);
            fw.write(NewObj.toJSONString());
            fw.flush();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String args[]) {
        
        Evidentia evidentia = new Evidentia();
        evidentia.run(args);
        
    }
    
}

