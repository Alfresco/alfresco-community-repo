/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.heartbeat;

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.repo.dictionary.CustomModelsInfo;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

public class UsageModelDataCollector extends HBBaseDataCollector
{
    /** The logger. */
    private static final Log logger = LogFactory.getLog(UsageModelDataCollector.class);

    /** DAO for current repository descriptor. */
    private DescriptorDAO currentRepoDescriptorDAO;

    /** Provides information about custom models */
    private CustomModelService customModelService;

    /** The transaction service. */
    private TransactionService transactionService;

    public UsageModelDataCollector(String collectorId) {
        super(collectorId);
    }

    public void setCurrentRepoDescriptorDAO(DescriptorDAO currentRepoDescriptorDAO)
    {
        this.currentRepoDescriptorDAO = currentRepoDescriptorDAO;
    }

    public void setCustomModelService(CustomModelService customModelService)
    {
        this.customModelService = customModelService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    @Override
    public List<HBData> collectData()
    {
        if(transactionService == null)
        {
            logger.debug("Couldn't collect data because transaction service is null");
            return null;
        }
        if(customModelService == null)
        {
            logger.debug("Couldn't collect data because custom model service is null");
            return null;
        }
        if(currentRepoDescriptorDAO == null)
        {
            logger.debug("Couldn't collect data because repository descriptor is null");
            return null;
        }
        logger.debug("Preparing repository usage (model) data...");

        final CustomModelsInfo customModelsInfo = transactionService.getRetryingTransactionHelper().doInTransaction(
                () -> customModelService.getCustomModelsInfo(), true);

        Map<String, Object> modelUsageValues = new HashMap<>();
        modelUsageValues.put("numOfActiveModels", new Integer(customModelsInfo.getNumberOfActiveModels()));
        modelUsageValues.put("numOfActiveTypes", new Integer(customModelsInfo.getNumberOfActiveTypes()));
        modelUsageValues.put("numOfActiveAspects", new Integer(customModelsInfo.getNumberOfActiveAspects()));
        HBData modelUsageData = new HBData(
                this.currentRepoDescriptorDAO.getDescriptor().getId(),
                this.getCollectorId(),
                this.getCollectorVersion(),
                new Date(),
                modelUsageValues);

        List<HBData> collectedData = new LinkedList<>();
        collectedData.add(modelUsageData);
        return collectedData;
    }
}
