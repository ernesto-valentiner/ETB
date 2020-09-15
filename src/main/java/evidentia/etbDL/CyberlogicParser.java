package evidentia.etbDL;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.io.*;

import evidentia.etbDL.utils.*;
import evidentia.etbDL.statements.*;

/**
 * This class parses workflows written in Cyberlogic.
 */
public class CyberlogicParser {
    
    public static Datalog parseScript(String scriptFile, String repoDirPath) {
        Datalog etbDL = new Datalog();
        try {
            Reader reader = new BufferedReader(new FileReader(scriptFile));
            StreamTokenizer scan = new StreamTokenizer(reader);
            scan.ordinaryChar('.'); // assumed number by default
            scan.commentChar('%'); // % comments will be ignored
            scan.quoteChar('"');
            scan.quoteChar('\'');
            
            //scanning token by token
            scan.nextToken();
            while(scan.ttype != StreamTokenizer.TT_EOF) {
                scan.pushBack();
                try {
                    //parses each line to a corresponding statement
                    DatalogStatement statement = parseStmt(scan, repoDirPath);
                    statement.addTo(etbDL);
                } catch (DatalogException e) {
                    System.out.println("[line " + scan.lineno() + "] error parsing statement");
                    e.printStackTrace();
                    return null;
                }
                scan.nextToken();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return etbDL;
    }

    // e.g., k attests [(k attests ancestor(A, B)) :- (k1 attests ancestor(A, C)), (k2 attests parent(C, B))]
    public static DatalogStatement parseStmt(StreamTokenizer scan, String repoDirPath) throws DatalogException, IOException {
        
        //parsing authority
        String authority;
        scan.nextToken();
        if(scan.ttype == StreamTokenizer.TT_WORD) {
            authority = scan.sval;
        }
        else {
            throw new DatalogException("[line " + scan.lineno() + "] rule attestation should have an authority");
        }
        
        //parsing keyword 'attests'
        scan.nextToken();
        if(!(scan.ttype == StreamTokenizer.TT_WORD && scan.sval.equalsIgnoreCase("attests"))) {
            throw new DatalogException("[line " + scan.lineno() + "] rule attestation expected");
        }
       
        //parsing '[' as opening separator of the attestated rule
        if(scan.nextToken() != '[') {
            throw new DatalogException("[line " + scan.lineno() + "] rule attestation must use '[' as an opening separator");
        }
        
        //parsing head of the rule
        Expr head = parseAttestation(scan, repoDirPath);
        
        //parsing ':-' as separator of head and body of the rule
        if(scan.nextToken() != ':') {
            throw new DatalogException("[line " + scan.lineno() + "] expected ':-'");
        }
        if(scan.nextToken() != '-') {
            throw new DatalogException("[line " + scan.lineno() + "] expected ':-'");
        }
           
        //parsing body of the rule
        List<Expr> body = new ArrayList<>();
        do {
            //parsing each attestation in the body
            Expr arg = parseAttestation(scan, repoDirPath);
            body.add(arg);
            //body attestations must be separated by comma
        } while(scan.nextToken() == ',');
        
        //parsing ']' as closing separator of the attestated rule
        if(scan.ttype != ']') {
            throw new DatalogException("[line " + scan.lineno() + "] rule attestation must use ']' as a closing separator");
        }
        
        Rule newRule = new Rule(head, body, new Authority(authority));
        System.out.println("\u001B[31m[newRule]\u001B[30m: " + newRule);
        
        //constructing a DL statement of type RULE (using statementFactory utility)
        return DatalogStatementFactory.getRuleStatement(newRule);
        
    }

    //an attestation is of the form e.g., (cons attests valid(Visa, Passport))
    public static Expr parseAttestation(StreamTokenizer scan, String repoDirPath) throws DatalogException, IOException {
        
        //prase '('
        if(scan.nextToken() != '(') {
            throw new DatalogException("[line " + scan.lineno() + "] attestations must be enclosed in '(' and ')'");
        }

        //parse authority
        String authority;
        scan.nextToken();
        if(scan.ttype == StreamTokenizer.TT_WORD) {
            authority = scan.sval;
        }
        else {
            throw new DatalogException("[line " + scan.lineno() + "] attestation should have an authority");
        }
        
        //parse keyword 'attests'
        scan.nextToken();
        if(!(scan.ttype == StreamTokenizer.TT_WORD && scan.sval.equalsIgnoreCase("attests"))) {
            throw new DatalogException("[line " + scan.lineno() + "] attestation expected");
        }
        
        //parse attested predicate into an Expr
        Expr pred = DatalogParser.parseExpr(scan, repoDirPath);
        pred.setAuthority(new Authority(authority));
        
        if(scan.nextToken() != ')') {
            throw new DatalogException("[line " + scan.lineno() + "] attestations must be enclosed in '(' and ')'");
        }

        
        return pred;
    }
    
    public static void main(String args[]) {
        String wfScriptPath = "/Users/beyene/projects/codes/git/evidentia/TRepo/workflows/twoStepCR22";
        String inputAttestation = "bank attests valid(A,B)";
        String repoDirPath = "/Users/beyene/projects/codes/git/evidentia/TRepo";
        
        Datalog dlPack = CyberlogicParser.parseScript(wfScriptPath, repoDirPath);
        
    }
    
}
