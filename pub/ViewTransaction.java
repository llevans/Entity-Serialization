package eaf.core.entities.pub;

public class ViewTransaction extends EafTransaction {

    public ViewTransaction() {

        super();

        setTlogAction(EafTransaction.actions.VIEW);

    }
}
