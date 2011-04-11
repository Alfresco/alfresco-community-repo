package org.alfresco.repo.admin.patch;

import org.alfresco.service.transaction.TransactionService;

public class SimplePatch extends AbstractPatch
{
    public static final String MSG_SUCCESS = "SimplePatch applied successfully";

    /**
     * Default constructor for Spring config
     */
    public SimplePatch()
    {
    }
    
    /**
     * Overrides the base class version to do nothing, i.e. it does not self-register
     */
    @Override
    public void init()
    {
    }
    
    /**
     * Helper constructor for some tests.  Default properties are set automatically.
     * 
     * @param mustFail true if this instance must always fail to apply
     */
    /* protected */ SimplePatch(TransactionService transactionService, boolean requiresTransaction)
    {
        setTransactionService(transactionService);
        setId("SimplePatch");
        setDescription("This is a simple patch");
        setFixesFromSchema(0);
        setFixesToSchema(1000);
        setTargetSchema(1001);
        setRequiresTransaction(requiresTransaction);
    }
    
    /**
     * Does nothing
     * 
     * @return Returns a success or failure message dependent on the constructor used
     */
    @Override
    protected String applyInternal() throws Exception
    {
        return MSG_SUCCESS;
    }

}
