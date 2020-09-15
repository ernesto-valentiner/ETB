package evidentia.etbDL.statements;

import evidentia.etbDL.DatalogParser;
import evidentia.etbDL.utils.Expr;
import evidentia.etbDL.utils.Rule;

/**
 * Provides factory methods for building Statement instances of different types for the Datalog parser.
 * <p>
 * Used in {@link DatalogParser#parseStmt(StreamTokenizer, String)} to parse string tokens to Datalog statements.
 * Strings to statement object.
 * </p>
 * @see DatalogStatement
 * @see DatalogParser
 */
public class DatalogStatementFactory {
	
	/**
	 * Creates a statement to query the database.
	 * @param goal defines the query expression for the Datalog query statement 
	 * @return a statement that will query the database for the given goals.
	 */
    public static DatalogStatement getQueryStatement(Expr goal) {
        return new QueryStatement(goal);
    }
    
	/**
	 * Creates a statement that will insert a fact into the EDB.
	 * @param fact The fact to insert
	 * @return A statement that will insert the given fact into the database.
	 */
	public static DatalogStatement getFactStatement(Expr fact) {
		return new FactStatement(fact);
	}
	
	/**
	 * Creates a statement that will insert a rule into the IDB.
	 * @param rule The rule to insert
	 * @return A statement that will insert the given rule into the database.
	 */
	public static DatalogStatement getRuleStatement(Rule rule) {
		return new RuleStatement(rule);
	}
}
