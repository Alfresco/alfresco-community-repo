package org.alfresco.repo.admin.patch;

import org.alfresco.service.transaction.TransactionService;

public class SamplePatch extends AbstractPatch
{
    public static final String MSG_SUCCESS = "SamplePatch applied successfully";
    public static final String MSG_FAILURE = "SamplePatch failed to apply";
    
    private boolean mustFail;

    /**
     * Default constructor for Spring config
     */
    public SamplePatch()
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
    /* protected */ SamplePatch(boolean mustFail, TransactionService transactionService)
    {
        this.mustFail = mustFail;
        setTransactionService(transactionService);
        setId("SamplePatch");
        setDescription("This is a sample patch");
        setFixesFromSchema(0);
        setFixesToSchema(1000);
        setTargetSchema(1001);
    }
    
    /**
     * Does nothing
     * 
     * @return Returns a success or failure message dependent on the constructor used
     */
    @Override
    protected String applyInternal() throws Exception
    {
        if (mustFail)
        {
            throw new Exception(MSG_FAILURE);
        }
        else
        {
            return MSG_SUCCESS;
        }
    }
}
