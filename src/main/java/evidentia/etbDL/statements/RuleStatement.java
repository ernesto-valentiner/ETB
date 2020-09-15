package evidentia.etbDL.statements;

import evidentia.etbDL.Datalog;
import evidentia.etbDL.utils.DatalogException;
import evidentia.etbDL.utils.Rule;

public class RuleStatement implements DatalogStatement {
	
	private final Rule rule;
	
	public RuleStatement(Rule rule) {
		this.rule = rule;
	}
    
    @Override
    public void addTo(Datalog datalog) throws DatalogException {
        //datalog.addRule(rule);
        datalog.add(rule);
    }    
}
