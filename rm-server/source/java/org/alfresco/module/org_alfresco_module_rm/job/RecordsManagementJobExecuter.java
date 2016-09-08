/*
 * Copyright (C) 2009-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_rm.job;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;

/**
 * Records management job executer base class.
 * 
 * @author Roy Wetherall
 */
public abstract class RecordsManagementJobExecuter implements RecordsManagementModel                       
{
    /** Retrying transaction helper */
    protected RetryingTransactionHelper retryingTransactionHelper;
    
    /** Repository state helper */
    protected RepositoryState repositoryState;
    
    /**
     * @param retryingTransactionHelper retrying transaction helper
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }
    
    /**
     * @param repositoryState   repository state helper component
     */
    public void setRepositoryState(RepositoryState repositoryState) 
    {
        this.repositoryState = repositoryState;
    }
    
    /**
     * Executes the jobs work.
     */
    public void execute()
    {
        // jobs not allowed to execute unless bootstrap is complete
        if (repositoryState.isBootstrapping() == false)
        {
            retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    executeImpl();
                    
                    return null;
                }
            }, false, true);
        }
    }
    
    /**
     * Jobs work implementation.
     */
    public abstract void executeImpl();
}
