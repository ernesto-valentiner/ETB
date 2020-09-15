package evidentia.etbDL.engine;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import evidentia.etbDL.utils.Authority;

import evidentia.etbDL.utils.*;

public class goalNode {
    
    int index;
    Expr literal;
    String status;
    Set<clauseNode> parents = new HashSet<>();
    ArrayList<Expr> claims = new ArrayList<>();
    Set<clauseNode> children = new HashSet<>();
    
    public goalNode(int index, Expr literal, String status, clauseNode clNode) {
        this.index = index;
        this.literal = literal;
        this.status = status;
        this.parents.add(clNode);
    }
        
    public goalNode(int index, Expr literal, String status) {
        this.index = index;
        this.literal = literal;
        this.status = status;
    }
    
    public void addNodeToParents(clauseNode clNode) {
        this.parents.add(clNode);
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getStatus() {
        return this.status;
    }
    
    public Expr getLiteral() {
        return this.literal;
    }
    
    public Set<clauseNode> getParents() {
        return parents;
    }
    
    public ArrayList<Expr> getClaims() {
        return this.claims;
    }
    
    public void addClaims(Expr claim) {
        this.claims.add(claim);
    }

    public Expr getClaim() {//TODO: Q&D more than one claims??
        if (claims.size()  == 0) {
            //System.out.println("=> no claim in the goal node \u001B[31m(warning)\u001B[30m");
            return null;
        }
        else if (claims.size() > 1) {
            System.out.println("=> more than one claims in a goal node \u001B[31m(warning)\u001B[30m");
        }
        return claims.get(0);
    }

    public void print() {
        System.out.println("=> goal literal : " + literal.toString());
        System.out.println(" -> parent clauses : ");
        Iterator<clauseNode> clIter = parents.iterator();
        while (clIter.hasNext()) {
            clIter.next().print();
        }
        System.out.println(" -> end of parent clauses ");
    }
    
}
