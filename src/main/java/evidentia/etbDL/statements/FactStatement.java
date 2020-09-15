package evidentia.etbDL.statements;

import evidentia.etbDL.Datalog;
import evidentia.etbDL.utils.DatalogException;
import evidentia.etbDL.utils.Expr;

public class FactStatement implements DatalogStatement {

	private final Expr fact;
	
	public FactStatement(Expr fact) {
		this.fact = fact;
	}

    @Override
    public void addTo(Datalog datalog) throws DatalogException {
        //datalog.addFact(fact);
        datalog.add(fact);
    }    
}
