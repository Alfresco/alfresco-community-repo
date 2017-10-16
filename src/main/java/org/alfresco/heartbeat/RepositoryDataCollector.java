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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.repo.dictionary.CustomModelsInfo;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class collects repository data for HeartBeat through multiple collector.
 * <br>
 * <b>Collector ID: </b> acs.repository.info
 * <br>
 * <b>Data points: </b> repoName, edition, versionMajor, versionMinor, schema
 *
 * <br>
 * <b>Collector ID: </b> acs.repository.usage.system
 * <br>
 * <b>Data points: </b> memFree, memMax, memTotal
 *
 * <br>
 * <b>Collector ID: </b> acs.repository.usage.model
 * <br>
 * <b>Data points: </b> numOfActiveModels, numOfActiveTypes, numOfActiveAspects
 *
 * @author eknizat
 */
public class RepositoryDataCollector extends HBBaseDataCollector
{

    /** The logger. */
    private static final Log logger = LogFactory.getLog(RepositoryDataCollector.class);

    /**
     * The parameters that we expect to remain static throughout the lifetime of the repository. There is no need to
     * continuously update these.
     */
    private Map<String, Object> staticParameters;

    /** The transaction service. */
    private TransactionService transactionService;

    /** DAO for current repository descriptor. */
    private DescriptorDAO currentRepoDescriptorDAO;

    /** DAO for current descriptor. */
    private DescriptorDAO serverDescriptorDAO;

    /** Provides information about custom models */
    private CustomModelService customModelService;

    public void setCurrentRepoDescriptorDAO(DescriptorDAO currentRepoDescriptorDAO) 
    {
        this.currentRepoDescriptorDAO = currentRepoDescriptorDAO;
    }

    public void setServerDescriptorDAO(DescriptorDAO serverDescriptorDAO)
    {
        this.serverDescriptorDAO = serverDescriptorDAO;
    }

    public void setTransactionService(TransactionService transactionService) 
    {
        this.transactionService = transactionService;
    }

    public void setCustomModelService(CustomModelService customModelService) 
    {
        this.customModelService = customModelService;
    }

    @Override
    public List<HBData> collectData()
    {
        List<HBData> collectedData = new LinkedList<>();

        RetryingTransactionHelper.RetryingTransactionCallback<Void> initCallback = new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                lazyInit();
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(initCallback, true);

        // collect repository info data
        logger.debug("Preparing repository info data...");
        Map<String, Object> infoValues = new HashMap<>();
        infoValues.put("repoName", this.staticParameters.get("repoName"));
        infoValues.put("edition", this.staticParameters.get("edition"));
        infoValues.put("versionMajor", this.staticParameters.get("versionMajor"));
        infoValues.put("versionMinor", this.staticParameters.get("versionMinor"));
        infoValues.put("schema", this.staticParameters.get("schema"));
        HBData infoData = new HBData(
                this.currentRepoDescriptorDAO.getDescriptor().getId(),
                "acs.repository.info",
                "1.0",
                new Date(),
                infoValues);
        collectedData.add(infoData);

        // collect repository usage (system) data
        logger.debug("Preparing repository usage (system) data...");
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> systemUsageValues = new HashMap<>();
        systemUsageValues.put("memFree", runtime.freeMemory());
        systemUsageValues.put("memMax", runtime.maxMemory());
        systemUsageValues.put("memTotal", runtime.totalMemory());
        HBData systemUsageData = new HBData(
                this.currentRepoDescriptorDAO.getDescriptor().getId(),
                "acs.repository.usage.system",
                "1.0",
                new Date(),
                systemUsageValues);
        collectedData.add(systemUsageData);

        // collect repository usage (model) data
        logger.debug("Preparing repository usage (model) data...");
        final CustomModelsInfo customModelsInfo = transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<CustomModelsInfo>()
                {
                    public CustomModelsInfo execute()
                    {
                        return customModelService.getCustomModelsInfo();
                    }
                }, true);

        Map<String, Object> modelUsageValues = new HashMap<>();
        modelUsageValues.put("numOfActiveModels", new Integer(customModelsInfo.getNumberOfActiveModels()));
        modelUsageValues.put("numOfActiveTypes", new Integer(customModelsInfo.getNumberOfActiveTypes()));
        modelUsageValues.put("numOfActiveAspects", new Integer(customModelsInfo.getNumberOfActiveAspects()));
        HBData modelUsageData = new HBData(
                this.currentRepoDescriptorDAO.getDescriptor().getId(),
                "acs.repository.usage.model",
                "1.0",
                new Date(),
                modelUsageValues);
        collectedData.add(modelUsageData);

        return collectedData;
    }

    /**
     * Initializes static parameters on first invocation. Avoid doing it on construction due to bootstrap dependencies
     * (e.g. patch service must have run)
     *
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private synchronized void lazyInit() throws GeneralSecurityException, IOException
    {
        if (this.staticParameters == null)
        {
            this.staticParameters = new TreeMap<String, Object>();

            // Load up the static parameters
            final Descriptor serverDescriptor = this.serverDescriptorDAO.getDescriptor();
            this.staticParameters.put("repoName", serverDescriptor.getName());
            this.staticParameters.put("edition", serverDescriptor.getEdition());
            this.staticParameters.put("versionMajor", serverDescriptor.getVersionMajor());
            this.staticParameters.put("versionMinor", serverDescriptor.getVersionMinor());
            this.staticParameters.put("schema", new Integer(serverDescriptor.getSchema()));

        }
    }
}
