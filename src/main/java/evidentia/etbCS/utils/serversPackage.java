package evidentia.etbCS.utils;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//import evidentia.etbDL.etbDatalog;
//import evidentia.etbDL.etbDatalogEngine;
import evidentia.etbCS.clientMode;

//import evidentia.etbDL.utils.*;
//import evidentia.etbDL.output.*;

public class serversPackage {
    
    Map<String, serverSpec> servers = new HashMap<>();
    
    public serversPackage() {
        servers = new HashMap<>();
    }
    
    public serversPackage(JSONArray serversJSON) {
        Iterator<JSONObject> serverIter = serversJSON.iterator();
        while (serverIter.hasNext()) {
            serverSpec server = new serverSpec((JSONObject) serverIter.next());
            this.servers.put(server.getName(), server);
        }
    }
    
    public JSONArray toJSONObject() {
        JSONArray serversJSON = new JSONArray();
        for (String serverID : servers.keySet()) {
            serversJSON.add(servers.get(serverID).toJSONObject());
        }
        return serversJSON;
    }
    
    public void add(String specFilePath) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject serverSpecJSON = (JSONObject) parser.parse(new FileReader(specFilePath));
            
            String IP = (String) serverSpecJSON.get("IP");
            if ((IP = IP.trim()) == null) {
                System.out.println("=> no server IP given \u001B[31m(operation not successful)\u001B[30m");
                return;
            }
            //TODO: IP address sanity/validity check?
            
            int port = 0;
            try {
                port =  Integer.valueOf((String) serverSpecJSON.get("port"));
            }
            catch (NumberFormatException e) {
                System.out.println("\u001B[31m[error]\u001B[30m non-numeric port value: " + serverSpecJSON.get("port"));
            }
            
            //running in client mode to communicate with the server and register its services
            clientMode cm = new clientMode(IP, port);
            if (cm.isConnected()) {
                List<String> remoteServices = new ArrayList<String>(Arrays.asList(cm.newServicesRegistration().split(" ")));
                String serverName = remoteServices.remove(0);
                if (servers.containsKey(serverName)) {
                    System.out.println("=> server already exists \u001B[31m(updating remote services)\u001B[30m");
                    this.servers.replace(serverName, new serverSpec(serverName, IP, port, remoteServices));
                }
                else {
                    this.servers.put(serverName, new serverSpec(serverName, IP, port, remoteServices));
                    System.out.println("=> server added successfully");
                }
            }
            else {
                System.out.println("=> server could not be added \u001B[31m(operation not successful)\u001B[30m");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
    }

    public void remove(String name) {
        if (!servers.keySet().contains(name)) {
            System.out.println("=> server with the name '" + name + "' does not exist \u001B[31m(removal not successful)\u001B[30m");
            return;
        }
        servers.remove(name);
        System.out.println("=> server removed successfully");
    }
    
    public void clean() {
        servers.clear();
    }

    public  Map<String, serverSpec> getServers() {
        return servers;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n==> number of servers: " + servers.size());
        servers.keySet().stream().forEach(serverID -> sb.append(servers.get(serverID)));
        return sb.toString();
    }

}

