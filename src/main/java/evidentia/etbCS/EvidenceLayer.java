package evidentia.etbCS;

import java.io.*;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.apache.commons.io.FileUtils;
import evidentia.etbDL.utils.*;
import evidentia.etbDL.services.*;

/**
 * This class interacts with the IPFS for concretising the input query as well as abstracting the resulting claims and evidence.
 * <p> TODO
 * </p>
 */
public class EvidenceLayer {
    
	public Expr getClaim() {
		return claim;
	}

	public String getEvidence() {
		return evidencePath;
	}

	Expr query;
    String signature;
    Expr claim;
    String evidencePath;
    String claimDirPath;
    
    public EvidenceLayer(Expr query, String signature, String claimDirPath) {
        this.query = query;
        this.signature = signature;
        this.claimDirPath = claimDirPath;
        this.evidencePath = this.claimDirPath + "/evidence.json";
        
        File claimDir = new File(claimDirPath);
        if (!claimDir.isDirectory()) {
            claimDir.mkdir();
        }
        else {
        	try{
                FileUtils.deleteDirectory(claimDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
            claimDir.mkdir();
        }
    }
    
    public void run() {
        invoke();
        createEvidence();
    }
        
    /**
     * This method invokes a wrapper to solve a query and create evidence for the solving process.
     */
    private void invoke() {
        try {
            Class<?> wrapperClass = Class.forName("evidentia.wrappers." + query.getPredicate() + "WRP");
            genericWRP genWRP = (genericWRP) wrapperClass.newInstance();
            genWRP.invoke(query.getMode(), query.getTerms(), this.claimDirPath + "/" + query.getPredicate());
            this.claim = new Expr(query.getPredicate(), genWRP.getOutParams(), signature, query.getMode());

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            System.out.println("\u001B[31mmissing service wrapper\u001B[30m please check '" + query.getPredicate()+ "' service)");
            e.printStackTrace();
        }
    }

    //TODO: critical part as evidence is created here
    private void createEvidence() {
        JSONObject evidSpecJSON = new JSONObject();
        JSONArray inputsJSON = new JSONArray();
        JSONArray outputsJSON = new JSONArray();
        
        for (int i=0; i<query.getMode().length(); i++) {
            JSONObject evidArgJSON = new JSONObject();
            evidArgJSON.put("index", i);
            evidArgJSON.put("type", signature.charAt(i));
            
            if (signature.charAt(i) == '2') {
                File origFile  = new File(utils.fromETBfile(claim.getTerms().get(i)));
                String evidFilePath =  this.claimDirPath + "/evidence/" + origFile.getName();
                File evidFile  = new File(evidFilePath);
                try{
                    FileUtils.copyFile(origFile, evidFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                evidArgJSON.put("arg", evidFilePath);
            }
            else if (signature.charAt(i) == '4') {
                List<String> eachFileLS = Arrays.asList(claim.getTerms().get(i).split(" "));
                eachFileLS = eachFileLS.subList(1, eachFileLS.size());
                List<String> evidFileLS = new ArrayList();
                
                for(String eachFile : eachFileLS) {
                    File origFile  = new File(utils.fromETBfile(eachFile));
                    String evidFilePath = this.claimDirPath + "/evidence/" + origFile.getName();
                    File evidFile  = new File(evidFilePath);
                    try{
                        FileUtils.copyFile(origFile, evidFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    evidFileLS.add(evidFilePath);
                }
                evidArgJSON.put("arg", String.join(" ", evidFileLS));
                
            }
            else {
                evidArgJSON.put("arg", query.getTerms().get(i));
            }
            
            if (query.getMode().charAt(i) == '+') {
                inputsJSON.add(evidArgJSON);
            }
            else {
                outputsJSON.add(evidArgJSON);
            }
        }
        evidSpecJSON.put("inputs", inputsJSON);
        evidSpecJSON.put("outputs", outputsJSON);

        try {
            FileWriter fw = new FileWriter(this.evidencePath);
            fw.write(evidSpecJSON.toJSONString());
            fw.flush();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
}
