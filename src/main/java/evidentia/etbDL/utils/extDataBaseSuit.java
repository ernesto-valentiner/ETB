package evidentia.etbDL.utils;

import java.util.Collection;
import evidentia.etbDL.engine.IndexedSet;
import evidentia.etbDL.utils.Expr;

/*
 defines an extensional database, which uses an IndexedSet for an in-memory EDB.
 */
public class extDataBaseSuit {

    private IndexedSet<Expr, String> edb = new IndexedSet<Expr, String>();
    
	public Collection<Expr> allFacts() {
		return edb;
	}

	public void add(Expr fact) {
		edb.add(fact);
	}

	public boolean removeAll(Collection<Expr> facts) {
		return edb.removeAll(facts);
	}

	public Collection<Expr> getFacts(String predicate) {
		return edb.getIndexed(predicate);
	}

}
