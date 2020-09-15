package evidentia.etbCS.utils;

import java.io.*;
import java.util.*;

import evidentia.etbCS.queryProcessor;
import evidentia.etbDL.utils.Expr;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.apache.commons.io.FileUtils;

import evidentia.etbDL.Datalog;
import evidentia.etbDL.DatalogEngine;
import evidentia.etbDL.DatalogParser;
import evidentia.Entity;
import evidentia.etbCS.Orchestrator;


import evidentia.etbDL.utils.*;

public class claimsPack {
    
    Map<Integer, claimSpec> claims = new HashMap<Integer, claimSpec>();
    
    public claimsPack() {
        claims = new HashMap<>();
    }
    
    public claimsPack(JSONArray claimsJSON) {
        Iterator<JSONObject> claimsIter = claimsJSON.iterator();
        while (claimsIter.hasNext()) {
            JSONObject claimObj = (JSONObject) claimsIter.next();
            claimSpec claim = new claimSpec(claimObj);
            this.claims.put(claim.getID(), claim);
        }
    }
    
    public JSONArray toJSONObject() {
        JSONArray claimsJSON = new JSONArray();
        for (Integer claimID : claims.keySet()) {
            claimsJSON.add(claims.get(claimID).toJSONObject());
        }
        return claimsJSON;
    }
    
    public void checkStatus(servicePackage servicePack, Map<String, workFlowSpec> workflows, String repoDirPath) {
        for (Integer claimID : claims.keySet()) {
            System.out.println("=> checking claim status [query name: " + claims.get(claimID).getQuery().getPredicate() + "]");
            claimStatus status = claims.get(claimID).getStatus(servicePack, repoDirPath, workflows);
            if (status.isUptodate())
                System.out.println("-> \u001B[32mclaim is up-to-date\u001B[30m");
            else {
                if (status.inputsUpdated()) {
                    System.out.println("-> file inputs updated at positions: " + status.getUpdatedInputs());
                    System.out.println("-> \u001B[31mclaim inputs are outdated\u001B[30m (please maintain claim)");
                }
                if (status.servicesUpdated()) {
                    System.out.println("-> updated services: " + status.getUpdatedServices());
                    System.out.println("-> \u001B[31mclaim services are outdated\u001B[30m (please maintain claim)");
                }
                if (!status.getWorkFlowStatus().equals("notUpdated")) {
                    System.out.println("-> \u001B[31mclaim workflow is outdated\u001B[30m (please maintain claim)");
                }
            }
        }
    }
    
    /** Checks well-formedness of input query given as string and, if so, solved for the corresponding claim
     * @param queryStr input query as string
     * @param entity the entity initiating the claim addition
     * @see add(Expr, Entity)
     */
    public void add(String queryStr, Entity entity) {
        Expr queryExpr = readQuery(queryStr, entity.getRepoDirPath());
        if (queryExpr == null) {
            System.out.println("\u001B[31m[error]\u001B[30m invalid query");
            return;
        }
        this.add(queryExpr, entity);
    }
    
    /** takes string @param inputQuery and returns the corresponding query as #Expr*/
    private Expr readQuery(String inputQuery, String repoDirPath) {
        try {
            Reader reader = new StringReader(inputQuery);
            StreamTokenizer scan = new StreamTokenizer(reader);
            scan.ordinaryChar('.');
            scan.commentChar('%');
            scan.quoteChar('"');
            scan.quoteChar('\'');
            Expr query = DatalogParser.parseExpr(scan, repoDirPath);
            System.out.println("=> valid input query: " + query.toString());
            reader.close();
            return query;
        }
        catch (IOException | DatalogException e){
            e.printStackTrace();
            System.out.println("=> [\u001B[31merror\u001B[30m] invalid input query");
            return null;
        }
        
    }
    
    public void remove(Integer claimID) {
        //checking if workflow already exists with the same name
        if (claims.keySet().contains(claimID)) {
            claims.remove(claimID); //TODO: chain of reactions for claim removal
            System.out.println("=> claim removed successfully");
        }
        else {
            System.out.println("=> a claim with ID '" + claimID + "' does not exist \u001B[31m(removal not successful)\u001B[30m");
        }
    }
    
    public void maintain(Integer claimID, servicePackage servicePack, Entity entity) {
        if (claims.containsKey(claimID)) {
            claims.get(claimID).maintain(entity);
        }
        else {
            System.out.println("ERROR. unknown claimID '" + claimID + "'");
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n==> number of claims: " + claims.size());
        int count = 1;
        for (Integer claimID : claims.keySet()) {
            sb.append("\n[claim " + count++ + "]");
            sb.append(claims.get(claimID));
        }
        return sb.toString();
    }
        
    /** Adds claim corresponding to the input query to the entity
    * @param queryExpr - input query to the claim
    * @param entity - the entity initiating the claim addition request
    */
    public claimSpec add(Expr queryExpr, Entity entity) {
        if (claims.containsKey(queryExpr.hashCode())) {
            System.out.println("=> existing claim : " + claims.get(queryExpr.hashCode()).getClaimExpr().toString());
            System.out.println("=> [\u001B[33mwarning\u001B[30m] claim addition not successful");
            return claims.get(queryExpr.hashCode());
        }
        else {
            claimSpec claim = this.runQuery(queryExpr, entity);
            if (claim != null) {
                System.out.println("=> query processed successfully");
                this.claims.put(queryExpr.hashCode(), claim);
                System.out.println("=> claim added successfully");
                return claim;
            }
            else {
                System.out.println("=> \u001B[31mquery processing not successful\u001B[30m");
                System.out.println("=> [\u001B[33mwarning\u001B[30m] claim addition not successful");
                return null;
            }

        }
        
    }

    private claimSpec runQuery(Expr queryExpr, Entity node) {
        String repoDirPath = node.getRepoDirPath();
        //workflow invocation - grab and execute a matching workflow
        Map<String, workFlowSpec> workflows = node.getWorkflowsPack().getWorkflows();
        List<String> applWorkflows = node.getWorkflowsPack().getWorkflows(queryExpr.queryHashCode());
        System.out.println("=> number of matching applicable workflows: " + applWorkflows.size());
        for (String workFlowID : applWorkflows) {
            //could be a datalog oor a cyberlogic workflow
            Datalog dlPack = workflows.get(workFlowID).getDatalog(repoDirPath);
            DatalogEngine dlEngine = new DatalogEngine(queryExpr);
            Collection<Map<String, String>> answers;
            if ((answers = dlEngine.run(node, dlPack)) == null) {
                System.out.println("=> \u001B[31mquery processing not successful\u001B[30m (workflow: " + workFlowID + ")");
            }
            else {
                Expr claimExpr = queryExpr.substitute(answers.iterator().next());
                claimSpec claim = new claimSpec(queryExpr, claimExpr, workFlowID, workflows.get(workFlowID).getScriptID(repoDirPath), repoDirPath);
                claim.setDerivationRules(dlEngine.getDerivationRules(), dlEngine.getDerivationFacts());
                claim.setDerivationFacts(dlEngine.getDerivationFacts());
                claim.setDerivationServices(dlEngine.getDerivationServices());
                this.composeEvidence(queryExpr, dlEngine.getDerivationRules(), dlEngine.getSubClaims(), repoDirPath);
                //TODO: better huristics than stopping with first succesfull query processing
                return claim;
            }
        }
        System.out.println("=> no matching applicable workflow");
        
        //TODO: trying if there is a matching service
        Orchestrator inv = new Orchestrator(queryExpr);
        inv.process(node);
        if (inv.getResult() == null) {
            System.out.println("\t -> no matching service");
            return null;
        }
        else {
            claimSpec claim = new claimSpec(queryExpr, inv.getResult(), inv.getEvidence(), repoDirPath);
            Map<String, serviceSpec> derivServices = new HashMap<>();
            if (inv.isLocal()) {
                String serviceName = queryExpr.getPredicate();
                derivServices.put(serviceName, node.getServicePack().get(serviceName));
            }
            claim.setDerivationServices(derivServices);
            return claim;
        }
    }
    
    private void composeEvidence(Expr query, List<Rule> arguments, List<claimSpec> subclaims, String repoDirPath) {
        String evidencePath = repoDirPath+"claims/claim" + query.hashCode() + "/evidence.json";
        JSONObject evidSpecJSON = new JSONObject();
        JSONArray argsJSON = new JSONArray();
        for (Rule arg : arguments) {
            argsJSON.add(arg.toJSONObject());
        }
        evidSpecJSON.put("arguments", argsJSON);
        
        JSONArray subclaimsJSON = new JSONArray();
        for (claimSpec claim : subclaims) {
            JSONObject evidJSON = new JSONObject();
            evidJSON.put("claim", claim.getClaimExpr().toJSONObject());
            if (claim.isLocal()) {
                evidJSON.put("type", "local");
            }
            else {
                evidJSON.put("type", "remote");
            }
            evidJSON.put("storage", claim.getEvidence());
            subclaimsJSON.add(evidJSON);
        }
        evidSpecJSON.put("evidence", subclaimsJSON);
        
        try {
            FileUtils.writeStringToFile(new File(evidencePath), evidSpecJSON.toJSONString(), (String) null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

