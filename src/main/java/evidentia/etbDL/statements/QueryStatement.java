package evidentia.etbDL.statements;

import evidentia.etbDL.Datalog;
import evidentia.etbDL.utils.DatalogException;
import evidentia.etbDL.utils.Expr;

public class QueryStatement implements DatalogStatement {
    
    private final Expr goal;
    
    public QueryStatement(Expr goal) {
        this.goal = goal;
    }
    
    @Override
    public void addTo(Datalog datalog) throws DatalogException {
        //datalog.setGoal(goal);
    }
}
