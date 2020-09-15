package evidentia.etbCS.utils;

import java.io.*;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import evidentia.Entity;
import evidentia.etbDL.Datalog;
import evidentia.etbDL.DatalogEngine;
import evidentia.etbDL.utils.*;
import evidentia.etbDL.output.*;

public class claimSpec {
    
    //basic
    Expr query;
    Expr qClaim;
    String evidence;
    
    boolean local = true; // needs better placement
    
    //meta-level
    String workFlowID = "*serviceCL*"; // TODO: snapshot of the workflow at claim creation
    String workFlowSHA1 = "*serviceCL*";
    ArrayList<String> SHA1List = new ArrayList<String>();
    
    List<Rule> derivRules = new ArrayList<Rule>();
    List<Expr> derivFacts = new ArrayList<Expr>();
    Map<String, serviceSpec> derivServices = new HashMap<>();
    
    String repoDirPath;
    
    public claimSpec(Expr query, Collection<Map<String, String>> answers, String workFlowID, String workFlowSHA1, String repoDirPath) {
        this.query = query;
        this.qClaim = query.substitute(answers.iterator().next());
        generateSHA1(repoDirPath);
        this.workFlowID = workFlowID;
        this.workFlowSHA1 = workFlowSHA1;
        this.repoDirPath = repoDirPath;
    }
    
    public claimSpec(Expr query, Expr qClaim, String workFlowID, String workFlowSHA1, String repoDirPath) {
        this.query = query;
        this.qClaim = qClaim;
        generateSHA1(repoDirPath);
        this.workFlowID = workFlowID;
        this.workFlowSHA1 = workFlowSHA1;
        this.repoDirPath = repoDirPath;
    }
    
    public claimSpec(Expr query, Expr qClaim, String evidence, String repoDirPath) {
        //using service invocations
        this.query = query;
        this.qClaim = qClaim;
        this.evidence = evidence;
        generateSHA1(repoDirPath);
        this.repoDirPath = repoDirPath;
    }
    
    public claimSpec(JSONObject claimSpecJSON) {
        this.query = new Expr((JSONObject) claimSpecJSON.get("query"));
        this.qClaim = new Expr((JSONObject) claimSpecJSON.get("qClaim"));
        this.evidence = (String) claimSpecJSON.get("evidence");
        
        JSONArray derivRulesJSON = (JSONArray) claimSpecJSON.get("derivRules");
        Iterator<JSONObject> derivRulesIter = derivRulesJSON.iterator();
        while (derivRulesIter.hasNext()) {
            this.derivRules.add(new Rule((JSONObject) derivRulesIter.next()));
        }
        
        JSONArray derivFactsJSON = (JSONArray) claimSpecJSON.get("derivFacts");
        Iterator<JSONObject> derivFactsIter = derivFactsJSON.iterator();
        while (derivFactsIter.hasNext()) {
            this.derivFacts.add(new Expr((JSONObject) derivFactsIter.next()));
        }
        
        JSONArray derivServicesJSON = (JSONArray) claimSpecJSON.get("derivServices");
        Iterator<JSONObject> derivServicesIter = derivServicesJSON.iterator();
        while (derivServicesIter.hasNext()) {
            serviceSpec service = new serviceSpec((JSONObject) derivServicesIter.next());
            this.derivServices.put(service.getID(), service);
        }
        
        JSONArray SHA1ListJSON = (JSONArray) claimSpecJSON.get("SHA1List");
        Iterator<String> SHA1ListIter = SHA1ListJSON.iterator();
        while (SHA1ListIter.hasNext()) {
            this.SHA1List.add((String) SHA1ListIter.next());
        }
        this.workFlowID = (String) claimSpecJSON.get("workFlowID");
        this.workFlowSHA1 = (String) claimSpecJSON.get("workFlowSHA1");
    }
    
    public void setDerivationRules(List<Rule> derivRules) {
        this.derivRules = derivRules;
    }
    
    public void setDerivationRules(List<Rule> derivRules, List<Expr> derivFacts) {
        for(Rule rule : derivRules) {
            rule.setModes(derivFacts);
            this.derivRules.add(rule);
        }
    }
    
    public void setDerivationFacts(List<Expr> derivFacts) {
        this.derivFacts = derivFacts;
    }
    
    public void setDerivationServices(Map<String, serviceSpec> derivServices) {
        this.derivServices = derivServices;
    }

    public JSONObject toJSONObject() {
        JSONObject NewObj = new JSONObject();
        NewObj.put("query", query.toJSONObject());
        NewObj.put("qClaim", qClaim.toJSONObject());
        NewObj.put("evidence", this.evidence);
        
        JSONArray derivRulesJSON = new JSONArray();
        for (Rule derivRule : derivRules) {
            derivRulesJSON.add(derivRule.toJSONObject());
        }
        NewObj.put("derivRules", derivRulesJSON);
        
        JSONArray derivFactsJSON = new JSONArray();
        for (Expr derivFact : derivFacts) {
            derivFactsJSON.add(derivFact.toJSONObject());
        }
        NewObj.put("derivFacts", derivFactsJSON);
        
        JSONArray derivServicesJSON = new JSONArray();
        for (String derivServiceKey : derivServices.keySet()) {
            if(derivServices.get(derivServiceKey) != null){
            derivServicesJSON.add(derivServices.get(derivServiceKey).toJSONObject());
            }
        }
        NewObj.put("derivServices", derivServicesJSON);
        
        
        JSONArray SHA1ListJSON = new JSONArray();
        for (String SHA1str : SHA1List) {
            SHA1ListJSON.add(SHA1str);
        }
        NewObj.put("SHA1List", SHA1ListJSON);
        NewObj.put("workFlowID", workFlowID);
        NewObj.put("workFlowSHA1", workFlowSHA1);
        
        return NewObj;
        
    }
    
    public void generateSHA1(String repoDirPath) {
        SHA1List = new ArrayList<String>();
        for (int i=0; i<query.getMode().length(); i++) {
            if (query.getMode().charAt(i) == '+') {
                if (query.getSignature().charAt(i) == '2') {
                    SHA1List.add(utils.getSHA1(utils.fromETBfile(query.getTerms().get(i))));
                }
                else if (query.getSignature().charAt(i) == '4') {
                    List<String> eachFileLS = Arrays.asList(query.getTerms().get(i).split(" "));
                    eachFileLS = eachFileLS.subList(1, eachFileLS.size());
                    List<String> subSHA1List = new ArrayList<String>();
                    subSHA1List = Arrays.asList(eachFileLS.stream().map(inFile -> utils.getSHA1(utils.fromETBfile(inFile))).toArray(String[]::new));
                    SHA1List.add(String.join(" ", subSHA1List));
                }
            }
        }
    }
    
    public void writeDerivation(String FileName) {
        File tempDir = new File(repoDirPath + "/claimDerivations");
        if (!tempDir.isDirectory()) {
            tempDir.mkdir();
        }
        try (PrintWriter out = new PrintWriter(tempDir.getAbsolutePath() + "/" + FileName)) {
            derivRules.stream().forEach(rule -> out.println(rule.toString()));
            derivFacts.stream().forEach(fact -> out.println(fact.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //TODO: query or claim?
    public Integer getID() {
        return query.hashCode();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--> hashCode : " + query.hashCode());
        sb.append("\n--> qClaim : " + qClaim);
        sb.append("\n--> evidence : " + evidence);
        sb.append("\n--> query : " + query.toString());
        sb.append("\n--> workFlowID : " + workFlowID);
        sb.append("\n==> derivRules : ");
        derivRules.stream().forEach(rule -> sb.append("\n---> " + rule));
        sb.append("\n==> derivFacts : ");
        derivFacts.stream().forEach(fact -> sb.append("\n---> " + fact));
        sb.append("\n==> derivServices : ");
        derivServices.keySet().stream().forEach(serviceKey -> sb.append(derivServices.get(serviceKey)));
        return sb.toString();
    }
    
    public List<String> getUpdatedServices(Map<String, serviceSpec> nodeServices) {
        List<String> updatedServiceIDs = new ArrayList<String>();
        for (String derivServiceID : derivServices.keySet()) {
            String claimVersion = derivServices.get(derivServiceID).getVersion();
            String nodeVersion = nodeServices.get(derivServiceID).getVersion();
            if (!claimVersion.equals(nodeVersion)) {
                updatedServiceIDs.add(derivServiceID);
            }
        }
        return updatedServiceIDs;
    }
        
    public claimStatus getStatus(servicePackage servicePack, String repoDirPath, Map<String, workFlowSpec> workflows) {
        
        List<Integer> updatedInputPosS = new ArrayList<Integer>();
        for (int i=0; i<query.getMode().length(); i++) {
            if (query.getMode().charAt(i) == '+') {
                if (query.getSignature().charAt(i) == '2') {
                    if (!utils.getSHA1(utils.getFilePathInDirectory(utils.fromETBfile(query.getTerms().get(i)), repoDirPath)).equals(this.SHA1List.get(i))) {
                        updatedInputPosS.add(i);
                    }
                }
                else if (query.getSignature().charAt(i) == '4') {
                    //TODO: for list of files
                    System.out.println("-> [\u001B[31mTODO:InfileList@pos " + i + " : \u001B[30m]" + query.getTerms().get(i));
                }
            }
        }
        
        //checking if analysis services are changed
        List<String> updatedServiceIDs = new ArrayList<String>();
        for (String derivServiceID : derivServices.keySet()) {
            if (!derivServices.get(derivServiceID).getVersion().equals(servicePack.getServices().get(derivServiceID).getVersion())) {
                updatedServiceIDs.add(derivServiceID);
            }
        }
        
        //TODO: should changes/removal of workflows taken care here, if at all important?
        String workFlowStatus = "notUpdated";
        if(workflows.containsKey(workFlowID)) {
            if(!workflows.get(workFlowID).getScriptID(repoDirPath).equals(workFlowSHA1)) {
                //System.out.println("-> [\u001B[31mworkflow change\u001B[30m] a new version of workflow is found");
                workFlowStatus = "updated";
            }
        }
        else {
            //System.out.println("-> [\u001B[31mworkflow change\u001B[30m] workflow '" + workFlowID + "' does not exist anymore");
            workFlowStatus = "removed";
        }
        
        return new claimStatus(updatedInputPosS, updatedServiceIDs, workFlowStatus);
    }
    
    //maintain claim during change to inputs
    public List<Expr> maintainInputChange2(Entity entity, List<Integer> changedPosSet, String predName) {
        
        //for collecting those unaffected facts that are reused during the claim maintenance
        List<Expr> reUsedFacts = new ArrayList<Expr>();
        
        //starts by removing facts of this predicate
        List<Expr> existingPredFacts = Arrays.asList(derivFacts.stream().filter(derivFact ->
                                        derivFact.getPredicate().equals(predName)).toArray(Expr[]::new));
        existingPredFacts.stream().forEach(existingPredFact -> this.derivFacts.remove(existingPredFact));
        
        //add rules for this predicate
        List<Rule> affectedRules = Arrays.asList(derivRules.stream().filter(derivRule -> derivRule.getHead().getPredicate().equals(predName)).toArray(Rule[]::new));
        
        if (affectedRules.isEmpty()) {
            return new ArrayList<Expr>();
        }
        
        assert affectedRules.size() == 1 : "more than one rule with head '" + predName + "'";
        List<String> affectedArgs = new ArrayList<String>(Arrays.asList(changedPosSet.stream().map(changedPos -> affectedRules.get(0).getHead().getTerms().get(changedPos)).toArray(String[]::new)));
        
        for(Expr bodyPred : affectedRules.get(0).getBody()) {
            List<String> bodyPredIns = new ArrayList<String>();
            List<String> bodyPredOuts = new ArrayList<String>();
            for (int i=0; i < bodyPred.getMode().length(); i++) {
                if (bodyPred.getMode().charAt(i) == '+')
                    bodyPredIns.add(bodyPred.getTerms().get(i));
                else
                    bodyPredOuts.add(bodyPred.getTerms().get(i));
            }
            //keeping only changed args from the input args of the pred
            bodyPredIns.retainAll(affectedArgs);
            
            if (bodyPredIns.isEmpty()) {
                //keep fact for this predicate -- effectively blocking rule
                reUsedFacts.addAll(Arrays.asList(derivFacts.stream().filter(derivFact ->
                        derivFact.unify(bodyPred, new HashMap())).toArray(Expr[]::new)));
            }
            else {
                //recursively handle input changes
                List<Integer> predChangedPosSet = Arrays.asList(bodyPredIns.stream().map(bodyPredIn -> bodyPredIns.indexOf(bodyPredIn) ).toArray(Integer[]::new));
                reUsedFacts.addAll(maintainInputChange2(entity, predChangedPosSet, bodyPred.getPredicate()));
                //for assessing the next body predicate
                affectedArgs.addAll(bodyPredOuts);
            }
        }
        return reUsedFacts;
    }

    //
    public void maintain(Entity entity) {
        
        claimStatus status = getStatus(entity.getServicePack(), entity.getRepoDirPath(), entity.getWorkflowsPack().getWorkflows());
        
        if (status.isUptodate()) {
            System.out.println("-> \u001B[32mclaim is up-to-date\u001B[30m");
        }
        else if (status.inputsUpdated()) {
            //reused facts preparation
            List<Expr> reUsedFacts = new ArrayList<Expr>(maintainInputChange2(entity, status.getUpdatedInputs(), query.getPredicate()));
            runMaintain(entity, reUsedFacts);
            
        }
        
        else if (status.servicesUpdated()) {
            
            List<Expr> reUsedFacts = new ArrayList<Expr>(); // only facts re-used during the maintenance process
            
            List<String> updatedServiceIDs = status.getUpdatedServices();
            //List<Expr> unAffectedFacts = derivFacts; // unaffected facts but could be re-used or not re-used
            //unAffectedFacts.remove(qClaim);
            this.derivFacts.remove(qClaim);
            
            while (!updatedServiceIDs.isEmpty()) {
                //pop an updated service or affected predicate
                String updatedService = updatedServiceIDs.remove(0);
                this.derivServices.remove(updatedService);
                
                for (Rule derivRule : derivRules) {
                    //grab a rule with the predicate in its body
                    if (derivRule.inBody(updatedService)) {
                        int index = derivRule.indexOf(updatedService);
                        //grab dependency relation for the predicate
                        Map<Integer, List<Integer>> dependencyRel = derivRule.getDetailedDependency(this.derivFacts, index);
                        //removing service/predicate facts to enforce service re-run or pred rule re-run
                        List<Expr> factsUpdatedService = Arrays.asList(derivFacts.stream().filter(derivFact ->
                                    derivFact.unify(derivRule.getBody().get(index), new HashMap())).toArray(Expr[]::new));
                        this.derivFacts.removeAll(factsUpdatedService);
                        
                        for (int i=0; i<index; i++) {
                            Expr targetBodyExpr = derivRule.getBody().get(i);
                            List<Expr> factsBodyExpr = Arrays.asList(derivFacts.stream().filter(derivFact ->
                                            derivFact.unify(targetBodyExpr, new HashMap())).toArray(Expr[]::new));
                            reUsedFacts.addAll(factsBodyExpr);
                        }
                        //handling dependent predicates whose inputs include service outputs, i.e., input change for the predicates
                        for (int i=index+1; i<derivRule.getBody().size(); i++) {
                            Expr targetBodyExpr = derivRule.getBody().get(i);
                            List<Expr> factsBodyExpr = Arrays.asList(derivFacts.stream().filter(derivFact ->
                                        derivFact.unify(targetBodyExpr, new HashMap())).toArray(Expr[]::new));
                            
                            if (dependencyRel.containsKey(i)) {
                                //unAffectedFacts.removeAll(factsBodyExpr);
                                this.derivFacts.removeAll(factsBodyExpr);
                                List<Expr> reUsedFactsPred = new ArrayList<Expr>(maintainInputChange2(entity, dependencyRel.get(i), targetBodyExpr.getPredicate()));
                                System.out.println("-> reUsedFactsPred : " + reUsedFactsPred);
                                reUsedFacts.addAll(reUsedFactsPred);
                            }
                            else {
                                //unaffected facts and involved in the maintenance process, i.e., reused
                                reUsedFacts.addAll(factsBodyExpr);
                            }
                        }
                        //adding head of the rule to the impacted service
                        updatedServiceIDs.add(derivRule.getHead().getPredicate());
                    }
                }
            }
            
            runMaintain(entity, reUsedFacts);

        }
        
        else {
            System.out.println("-> \u001B[31mTODO: uknown case for claim maintainance\u001B[30m]");
        }
        
    }
    
    private void runMaintain(Entity entity, List<Expr> reUsedFacts) {
        //creating a datalog instance with ALL rules and only required facts -- facts will block rules
        Datalog dlPack = new Datalog(derivRules, reUsedFacts);
        DatalogEngine dlEngine = new DatalogEngine(query);
        Collection<Map<String, String>> refAnswers;
        if ((refAnswers = dlEngine.run(entity, dlPack))!= null) {
            this.qClaim = query.substitute(refAnswers.iterator().next());//TODO: just one derivation?
            List<Expr> refinedFacts = dlEngine.getDerivationFacts();
            refinedFacts.removeAll(reUsedFacts);
            this.derivFacts.addAll(refinedFacts);
            //adding updated service to the set of claim derivation services
            dlEngine.getDerivationServices().keySet().stream().forEach(serviceID -> this.derivServices.put(serviceID, dlEngine.getDerivationServices().get(serviceID)));
            //return true;
            System.out.println("=> claim maintained successfully");
        }
        else {
            //return false;
            System.out.println("=> \u001B[31mclaim mantainance not successful\u001B[30m ()");
        }
    }
    
    public Expr getClaimExpr() {
        return qClaim;
    }

    public boolean isLocal() {
        return local;
    }
    
    public String getEvidence() {
        return evidence;
    }
    
    public Expr getQuery() {
        return query;
    }
    
    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }
    
    
}

