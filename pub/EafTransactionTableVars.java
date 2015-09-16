package eaf.core.entities.pub;

import eaf.core.common.DataTableVars;
import eaf.core.common.EafException;
import eaf.core.common.HqlUtilities;

public class EafTransactionTableVars extends DataTableVars {

    @Override
    public void buildVars() throws EafException {

        setSchema("ea");
        setTable("EafTransaction");
        setCount(99999);
        setTitle("Transactions");
        setTableName("transactions");

        addColumn("s.tlogId", "primaryKey", true, false);
        addColumn("s.tlogEntityNm", "Entity");
        addColumn("s.tlogParentEntityNm", "Area");
        addColumn("s.tlogActionTaken", "Action Taken");
        addColumn("s.tlogOwner", "Actor");
        addColumn("s.tlogDatetime", "Transaction Time");

        setViewUrl(globalVars.getBaseUrl() + "EAJAVA/util/transactions/view");
        setExpandable(false);

        setOrder("s.tlogId");

        String entityName = null;
        try {
            entityName = (String) globalVars.getRequest().get(HqlUtilities.md5Digest("entityNm"));
        } catch (EafException e) {
            // TODO CJ - EafException
            new EafException("Missing object 'entityNm'.", e);
        }
        if (entityName != null && !entityName.contentEquals("-1")) {
            setFilter("(s.tlogEntityNm ilike '" + entityName + "' or s.tlogParentEntityNm ilike '" + entityName + "')");
        }

    }

}
