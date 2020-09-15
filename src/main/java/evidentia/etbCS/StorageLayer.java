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

//import evidentia.Entity;
import evidentia.etbDL.utils.*;
//import evidentia.etbDL.services.*;
//import evidentia.etbCS.utils.*;


/**
 * This class interacts with the IPFS for concretising the input query as well as abstracting the resulting claims and evidence.
 * <p> TODO
 * </p>
 */
public class StorageLayer {
    Expr abstQuery;
    Expr concQuery;
    Expr abstClaim;
    Expr concClaim;
    String abstEvidence;
    String concEvidence;

    public StorageLayer(Expr abstQuery) {
        this.abstQuery = abstQuery;
    }
    
    public void run(String signature, String claimDirPath) {
        concretise();
        EvidenceLayer evLayer = new EvidenceLayer(concQuery, signature, claimDirPath);
        evLayer.run();
        this.concClaim = evLayer.getClaim();
        this.concEvidence = evLayer.getEvidence();
        performAbstraction();
    }
    
    private void concretise() {
        //TODO: use IPFS
        this.concQuery = abstQuery;
    }
    
    private void performAbstraction() {
        //TODO: use IPFS
        this.abstClaim = this.concClaim;
        this.abstEvidence = this.concEvidence;
    }
    
    public Expr getClaim() {
        return abstClaim;
    }

    public String getEvidence() {
        return abstEvidence;
    }

}
