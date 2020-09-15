package evidentia.etbDL.statements;

import java.util.Collection;
import evidentia.etbDL.Datalog;
import evidentia.etbDL.utils.DatalogException;
import evidentia.etbDL.output.OutputUtils;
import evidentia.etbDL.DatalogParser;

/**
 * constructs a statement that can be added to the Datalog database.
 * <p>
 * There are 3 types of statements: facts, rules, and queries.
 * </p><p>
 * Instances of etbDLStatement are created by {@link DatalogStatementFactory}.
 * </p><p>
 * Strings can be parsed to Statements through {@link DatalogParser#parseStmt(StreamTokenizer, String)}
 * </p>
 * @see DatalogStatementFactory
 * @see DatalogParser
 */
public interface DatalogStatement {
	
	/**
	 * Executes a statement against a etbDatalog database.
	 * @param datalog The database against which to execute the statement.
	 * <p>
	 * A statement like "a(B,C)?" with bindings {@code <B = "foo", C = "bar">}
	 * is equivalent to the statement "a(foo,bar)?"
	 * </p> 
     * <ul>
	 * <li> If null, the statement was an insert or delete that didn't produce query results.
	 * <li> If empty the query's answer is "No."
	 * <li> If a list of empty maps, then answer is "Yes."
	 * <li> Otherwise it is a list of all bindings that satisfy the query.
	 * </ul>
	 * etbDatalog provides a {@link OutputUtils#answersToString(Collection)} method that can convert answers to 
	 * Strings
	 * @throws DatalogException if an error occurs in handling the statement
	 * @see OutputUtils#answersToString(Collection)
	 */
    public void addTo(Datalog datalog) throws DatalogException;    

}
