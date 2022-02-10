/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.patch;

import java.util.concurrent.TimeUnit;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;

/**
 * Abstract module patch implementation.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public abstract class AbstractModulePatch implements ModulePatch, BeanNameAware
{
    /** logger */
    protected static final Log LOGGER = LogFactory.getLog(ModulePatch.class);

    /** module patch service */
    private ModulePatchExecuter modulePatchExecuter;

    /** transaction service */
    protected TransactionService transactionService;

    /** module patch id */
    private String id;

    /** module patch description */
    private String description;

    /** module id */
    private String moduleId;

    /** module patch fixes from module schema number */
    private int fixesFromSchema;

    /** module patch fixes to module schema number */
    private int fixesToSchema;

    /** module patch target module schema number */
    private int targetSchema;
    
    /** if it should use a read only transaction */
    private boolean txnReadOnly = true;
    
    /** if it should use a new transaction */
    private boolean txnRequiresNew = false;

    /**
     * Initiialisation method
     */
    public void init()
    {
        modulePatchExecuter.register(this);
    }
    
    public void setTxnReadOnly(boolean txnReadOnly) 
    {
        this.txnReadOnly = txnReadOnly;
    }

    public void setTxnRequiresNew(boolean txnRequiresNew) 
    {
        this.txnRequiresNew = txnRequiresNew;
    }

    /**
     * @param modulePatchExecuter   module patch executer
     */
    public void setModulePatchExecuter(ModulePatchExecuter modulePatchExecuter)
    {
        this.modulePatchExecuter = modulePatchExecuter;
    }

    /**
     * @param transactionService    transaction service
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param id    module patch id
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.ModulePatch#getId()
     */
    @Override
    public String getId()
    {
        return id;
    }

    /**
     * Convenience method to set the module patch id to the bean name (if not already specified)
     */
    @Override
    public void setBeanName(String beanName)
    {
        if (id == null)
        {
            id = beanName;
        }
    }

    /**
     * @param description   module patch description
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.ModulePatch#getDescription()
     */
    @Override
    public String getDescription()
    {
        return description;
    }

    /**
     * @param moduleId  module id
     */
    public void setModuleId(String moduleId)
    {
        this.moduleId = moduleId;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.ModulePatch#getModuleId()
     */
    @Override
    public String getModuleId()
    {
        return moduleId;
    }

    /**
     * @param fixesFromSchema   fixes from schema value
     */
    public void setFixesFromSchema(int fixesFromSchema)
    {
        this.fixesFromSchema = fixesFromSchema;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.ModulePatch#getFixesFromSchema()
     */
    @Override
    public int getFixesFromSchema()
    {
        return fixesFromSchema;
    }

    /**
     * @param fixesToSchema fixes to schema value
     */
    public void setFixesToSchema(int fixesToSchema)
    {
        this.fixesToSchema = fixesToSchema;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.ModulePatch#getFixesToSchema()
     */
    @Override
    public int getFixesToSchema()
    {
        return fixesToSchema;
    }

    public void setTargetSchema(int targetSchema)
    {
        this.targetSchema = targetSchema;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.ModulePatch#getTargetSchema()
     */
    @Override
    public int getTargetSchema()
    {
        return targetSchema;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.ModulePatch#apply()
     */
    @Override
    public void apply()
    {
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info("Executing module patch \"" + description + "\"");
        }
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("   ... id=" + id +
                               ",moduleId=" + moduleId +
                               ",from=" + fixesFromSchema +
                               ",to=" + fixesToSchema +
                               ",target=" + targetSchema);
        }
               
        long startTime = System.nanoTime();
        
        // do patch in transaction
        transactionService.getRetryingTransactionHelper().doInTransaction(
                new ApplyCallback(),
                txnReadOnly,
                txnRequiresNew);

        long elapsedTime = System.nanoTime() - startTime;
        
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info("   ... module patch applied in " + TimeUnit.NANOSECONDS.toMillis(elapsedTime) + "ms");
        }
    }

    /**
     * Apply patch internal method.  Implementations can assume a transaction has
     * been started.
     */
    public abstract void applyInternal();

    /**
     * Apply callback worker class implementation
     */
    private class ApplyCallback implements RetryingTransactionCallback<Void>
    {
        /**
         * @see org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback#execute()
         */
        @Override
        public Void execute()
        {
            applyInternal();
            return null;
        }
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ModulePatch o)
    {
        int result = 0;
        if (getTargetSchema() < o.getTargetSchema())
        {
            result = -1;
        }
        else if (getTargetSchema() > o.getTargetSchema())
        {
            result = 1;
        }
        return result;
    }
}
