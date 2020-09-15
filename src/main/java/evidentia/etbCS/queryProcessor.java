package evidentia.etbCS;

import java.io.*;
import java.util.*;

import evidentia.Entity;
import evidentia.etbCS.Orchestrator;
import evidentia.etbCS.utils.*;
import evidentia.etbDL.utils.Expr;
import evidentia.etbDL.Datalog;
import evidentia.etbDL.DatalogEngine;
import evidentia.etbDL.DatalogParser;
import evidentia.etbDL.CyberlogicParser;

/*
 * This class takes a query, processes the query by calling the appropriate workflow or service, and creates a claim.
 */

//TODO: move to claimsPack?
public class queryProcessor {
    
    claimSpec claim;
    
    /** Runs a query on the available workflows and services.
     * @param queryExpr input query to the claim
     * @param node entity initiating the claim addition request
     * @return true if query processed successfully; otherwise, it returns false.
     */
    public boolean run(Expr queryExpr, Entity node) {
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
                claim = new claimSpec(queryExpr, claimExpr, workFlowID, workflows.get(workFlowID).getScriptID(repoDirPath), repoDirPath);
                claim.setDerivationRules(dlEngine.getDerivationRules(), dlEngine.getDerivationFacts());
                claim.setDerivationFacts(dlEngine.getDerivationFacts());
                claim.setDerivationServices(dlEngine.getDerivationServices());
                //TODO: better huristics than stopping with first succesfull query processing
                return true;
            }
        }
        System.out.println("=> no matching applicable workflow");
        
        //TODO: trying if there is a matching service
        Orchestrator inv = new Orchestrator(queryExpr);
        inv.process(node);
        if (inv.getResult() == null) {
            System.out.println("\t -> no matching service");
            return false;
        }
        else {
            claim = new claimSpec(queryExpr, inv.getResult(), inv.getEvidence(), repoDirPath);
            Map<String, serviceSpec> derivServices = new HashMap<>();
            if (inv.isLocal()) {
                String serviceName = queryExpr.getPredicate();
                derivServices.put(serviceName, node.getServicePack().get(serviceName));
            }
            claim.setDerivationServices(derivServices);
            return true;
        }
    }
    
    /** Gets the claim resulting from the query processing 
     * @return claim - the claim corresponding to the input query
     */
    public claimSpec getClaim() {
        return claim;
    }
}

