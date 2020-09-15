package evidentia.etbDL.output;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import evidentia.etbDL.utils.Expr;
import evidentia.etbDL.utils.Rule;
import evidentia.etbDL.Datalog;

/**
 * Utilities for printing {@link Datalog}'s output.
 */
public class OutputUtils {

    /**
     * Formats a collection of etbDatalog entities, like {@link Expr}s and {@link Rule}s
     * @param collection the collection to convert to a string
     * @return A String representation of the collection.
     */
    public static String listToString(List<?> collection) {
        StringBuilder sb = new StringBuilder("[");
        for(Object o : collection)
            sb.append(o.toString()).append(". ");
        sb.append("]");
        return sb.toString();
    }

    /**
     * Formats a Map of variable bindings to a String for output
     * @param bindings the bindings to convert to a String
     * @return A string representing the variable bindings
     */
    public static String bindingsToString(Map<String, String> bindings) {
        StringBuilder sb = new StringBuilder("{");
        int s = bindings.size(), i = 0;
        for(String k : bindings.keySet()) {
            String v = bindings.get(k);
            sb.append(k).append(": ");
            if(v.startsWith("\"")) {
                // Needs more org.apache.commons.lang3.StringEscapeUtils#escapeJava(String)
                sb.append('"').append(v.substring(1).replaceAll("\"", "\\\\\"")).append("\"");
            } else {
                sb.append(v);
            }
            if(++i < s)
                sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Helper method to convert a collection of answers to a String.
     * <ul>
     * <li> If {@code answers} is null, the line passed to {@code etbDatalog.query(line)} was a statement that didn't
     *      produce any results, like a fact or a rule, rather than a query.
     * <li> If {@code answers} is empty, then it was a query that doesn't have any answers, so the output is "No."
     * <li> If {@code answers} is a list of empty maps, then it was the type of query that only wanted a yes/no
     *      answer, like {@code siblings(alice,bob)} and the answer is "Yes."
     * <li> Otherwise {@code answers} is a list of all bindings that satisfy the query.
     * </ul>
     * @param answers The collection of answers
     * @return A string representing the answers.
     */
    public static String answersToString(Collection<Map<String, String>> answers) {

        StringBuilder sb = new StringBuilder();
        if(answers != null) { // null answer would imply a statement that did not produce any results (like Fact or Rule)
            //non-null answer implies a Query
            if(!answers.isEmpty()){//Query with certain answers
                if(answers.iterator().next().isEmpty()) {// YES/NO query with positive result (not binding)
                    sb.append("Yes.");
                } else { //Query with binding
                    Iterator<Map<String, String>> iter = answers.iterator();
                    while (iter.hasNext()) {
                        sb.append(bindingsToString(iter.next()));
                        if (iter.hasNext()) {
                            sb.append("\n");
                        }
                    }
                }
            } else { //a Query with no answers
                sb.append("No.");
            }
        }
        return sb.toString();
    }
}
