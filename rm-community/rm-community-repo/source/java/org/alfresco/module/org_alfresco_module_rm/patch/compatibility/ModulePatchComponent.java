package org.alfresco.module.org_alfresco_module_rm.patch.compatibility;

import org.alfresco.module.org_alfresco_module_rm.patch.ModulePatchExecuterImpl;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Module patch component base class.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@Deprecated
public abstract class ModulePatchComponent extends AbstractModuleComponent
{
    /** logger */
    protected static final Log LOGGER = LogFactory.getLog(ModulePatchComponent.class);

    /** Retrying transaction helper */
    protected RetryingTransactionHelper retryingTransactionHelper;

    /** Behaviour filter */
    protected BehaviourFilter behaviourFilter;

    /** module patch executer */
    protected ModulePatchExecuterImpl modulePatchExecuter;

    /**
     * @param retryingTransactionHelper retrying transaction helper
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    /**
     * @param behaviourFilter   behaviour filter
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * @param modulePatchExecuter   module patch executer
     */
    public void setModulePatchExecuter(ModulePatchExecuterImpl modulePatchExecuter)
    {
        this.modulePatchExecuter = modulePatchExecuter;
    }

    /**
     * Init method
     */
    @Override
    public void init()
    {
        super.init();
        modulePatchExecuter.getDependsOn().add(this);
    }

    /**
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executeInternal()
    {
        try
        {
            if (LOGGER.isInfoEnabled())
            {
                LOGGER.info("Module patch component '" + getName() + "' is executing ...");
            }
            
            // execute path within an isolated transaction
            retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute()
                {
                    behaviourFilter.disableBehaviour();
                    try
                    {
                        executePatch();
                    }
                    finally
                    {
                        behaviourFilter.enableBehaviour();
                    }
                    return null;
                }

            }, false, true);

            if (LOGGER.isInfoEnabled())
            {
                LOGGER.info(" ... completed module patch '" + getName() + "'");
            }
        }
        catch (Exception exception)
        {
            // record the exception otherwise it gets swallowed
            if (LOGGER.isInfoEnabled())
            {
                LOGGER.info("  ... error encountered.  " + exception.getMessage(), exception);
            }
            throw exception;
        }
    }

    /**
     * Execute patch work.
     */
    protected abstract void executePatch();
}
