package evidentia.etbCS.utils;

import java.net.*;
import java.io.*;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class serverSpec {

    String name;
    String IP;
    int port;
    List<String> services = new ArrayList<>();
    
    public serverSpec(String name, String IP, int port, List<String> services) {
        this.name = name;
        this.IP = IP;
        this.port = port;
        this.services = services;
    }

    public serverSpec(JSONObject serverSpecJSON) {
        this.name = (String) serverSpecJSON.get("name");
        this.IP = (String) serverSpecJSON.get("IP");
        this.port = Integer.valueOf(String.valueOf(serverSpecJSON.get("port")));
        JSONArray lowerServersJSON = (JSONArray) serverSpecJSON.get("services");
        Iterator<String> iterator = lowerServersJSON.iterator();
        while (iterator.hasNext()) {
            this.services.add(iterator.next());
        }
    }

    public String getName() {
        return name;
    }

    public String getIP() {
        return IP;
    }
    
    public int getPort() {
        return port;
    }
    
    public List<String> getServices(){
        return services;
    }
    
    public JSONObject toJSONObject() {
        JSONObject NewObj = new JSONObject();
        NewObj.put("name", name);
        NewObj.put("IP", IP);
        NewObj.put("port", port);
        JSONArray servicesJSON = new JSONArray();
        Iterator<String> it = services.iterator();
        while( it.hasNext()) {
            servicesJSON.add(it.next());
        }
        NewObj.put("services", servicesJSON);
        return NewObj;
    }
        
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n==> name: " + name + " [IP : " + IP + " port : " + port + "]");
        sb.append("\n--> services: " + services.toString());
        /*
        if (isRunning()) {
            sb.append("\n--> status:\u001B[32m running\u001B[30m");
        }
        else {
            sb.append("\n--> status:\u001B[31m down\u001B[30m");
        }
         */
        return sb.toString();
    }

    public boolean isRunning() {
        try {
        	Socket serverSocket = new Socket();
            serverSocket.connect(new InetSocketAddress(IP, port), 2000);
            serverSocket.setSoTimeout(2000);
            if (serverSocket.isConnected()) {
                OutputStream outStr = serverSocket.getOutputStream();
                DataOutputStream toServerData = new DataOutputStream(outStr);
                toServerData.writeUTF("statusCheck");
                return true;
            }
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("-> no I/O for the connection to " + IP + " at port " + port);
        }
        return false;
    }
    
}

