/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
import org.alfresco.heartbeat.jobs.HeartBeatJobScheduler;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.repo.dictionary.CustomModelsInfo;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

/**
 * A collector of data related to the data models being used.
 * <ul>
 *  <li>Collector ID: <b>acs.repository.usage.model</b></li>
 *  <li>Data:
 *      <ul>
 *          <li><b>numOfActiveModels:</b> Int - Number of active models. {@link CustomModelsInfo#getNumberOfActiveModels()}</li>
 *          <li><b>numOfActiveTypes:</b> Int - Number of active types. {@link CustomModelsInfo#getNumberOfActiveTypes()}</li>
 *          <li><b>numOfActiveAspects:</b> Int - Number of active aspects. {@link CustomModelsInfo#getNumberOfActiveAspects()}</li>
 *      </ul>
 *  </li>
 * </ul>
 *
 * @author eknizat
 */
public class ModelUsageDataCollector extends HBBaseDataCollector implements InitializingBean
{
    /** The logger. */
    private static final Log logger = LogFactory.getLog(ModelUsageDataCollector.class);

    /** DAO for current repository descriptor. */
    private DescriptorDAO currentRepoDescriptorDAO;

    /** Provides information about custom models */
    private CustomModelService customModelService;

    /** The transaction service. */
    private TransactionService transactionService;

    public ModelUsageDataCollector(String collectorId, String collectorVersion, String cronExpression,
                                   HeartBeatJobScheduler hbJobScheduler)
    {
        super(collectorId, collectorVersion, cronExpression, hbJobScheduler);
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
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "customModelService", customModelService);
        PropertyCheck.mandatory(this, "currentRepoDescriptorDAO", currentRepoDescriptorDAO);
    }

    @Override
    public List<HBData> collectData()
    {
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

        return Arrays.asList(modelUsageData);
    }
}
