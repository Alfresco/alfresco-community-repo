/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.activities;

import org.alfresco.repo.Client;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.TransactionListener;

/**
 * A Transaction Listener to post activities once the transaction has been committed,
 * or at the end of a read-only transaction.
 *
 * @author Gethin James
 */
public class ActivitiesTransactionListener implements TransactionListener
{
    private final String activityType;
    private final ActivityInfo activityInfo;
    private final ActivityPoster poster;
    private final RetryingTransactionHelper retryingTransactionHelper;
    private final String appTool;
    private final Client client;
    private final String tenantDomain;

    public ActivitiesTransactionListener(String activityType, ActivityInfo activityInfo, String tenantDomain, String appTool, Client client, ActivityPoster poster, RetryingTransactionHelper retryingTransactionHelper)
    {
        //Data
        this.activityType = activityType;
        this.activityInfo = activityInfo;
        this.appTool = appTool;
        this.client = client;
        this.tenantDomain = tenantDomain;

        //Services
        this.poster = poster;
        this.retryingTransactionHelper = retryingTransactionHelper;

    }

    @Override
    public void afterCommit()
    {
        //Activity posting needs a new transaction
        retryingTransactionHelper.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                if (activityType != null && activityInfo != null)
                {
                    poster.postFileFolderActivity(activityType, null, tenantDomain,
                            activityInfo.getSiteId(), activityInfo.getParentNodeRef(), activityInfo.getNodeRef(),
                            activityInfo.getFileName(), appTool, client, activityInfo.getFileInfo());
                }
                return null;
            }
        }, false, true);
    }

    @Override
    public void flush()
    {
        //do nothing
    }

    @Override
    public void beforeCommit(boolean readOnly)
    {
        //do nothing
    }

    @Override
    public void beforeCompletion()
    {
        //do nothing
    }

    @Override
    public void afterRollback()
    {
        //do nothing
    }

}
