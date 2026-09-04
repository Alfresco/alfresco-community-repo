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
package org.alfresco.repo.domain.permissions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;

/**
 * Removes a bounded batch of ACLs that have no repository references.
 */
public class UnusedAclCleaner
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UnusedAclCleaner.class);

    private AclCrudDAO aclCrudDAO;
    private TransactionService transactionService;
    private int batchSize = 1000;
    private boolean enabled = true;

    public void setAclCrudDAO(AclCrudDAO aclCrudDAO)
    {
        this.aclCrudDAO = aclCrudDAO;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setBatchSize(int batchSize)
    {
        if (batchSize < 1)
        {
            throw new IllegalArgumentException("batchSize must be greater than zero");
        }
        this.batchSize = batchSize;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    public void init()
    {
        PropertyCheck.mandatory(this, "aclCrudDAO", aclCrudDAO);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
    }

    public int execute()
    {
        if (!enabled)
        {
            LOGGER.debug("Unused ACL cleanup is disabled.");
            return 0;
        }

        int deleted = transactionService.getRetryingTransactionHelper()
                .doInTransaction(this::cleanupBatch, false, true);
        LOGGER.info("Unused ACL cleanup removed {} ACLs.", deleted);
        return deleted;
    }

    int cleanupBatch()
    {
        List<Long> aclIds = aclCrudDAO.getUnusedAclIds(0, batchSize);
        int deleted = 0;
        for (Long aclId : aclIds)
        {
            if (aclCrudDAO.deleteUnusedAcl(aclId))
            {
                deleted++;
            }
        }
        return deleted;
    }
}