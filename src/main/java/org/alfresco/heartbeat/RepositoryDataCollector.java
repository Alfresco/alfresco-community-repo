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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.repo.dictionary.CustomModelsInfo;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.usage.RepoUsageComponent;
import org.alfresco.service.cmr.admin.RepoUsage;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.license.LicenseException;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.Base64;

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

    /** The authority service. */
    private AuthorityService authorityService;

    private RepoUsageComponent repoUsageComponent;

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

    public void setAuthorityService(AuthorityService authorityService) 
    {
        this.authorityService = authorityService;
    }

    public void setRepoUsageComponent(RepoUsageComponent repoUsageComponent) 
    {
        this.repoUsageComponent = repoUsageComponent;
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
        Map<String, Object> infoValues = new HashMap<String, Object>();
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
        Map<String, Object> systemUsageValues = new HashMap<String, Object>();
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

        Map<String, Object> modelUsageValues = new HashMap<String, Object>();
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
            final String ip = getLocalIps();
            this.staticParameters.put("ip", ip);
            final String uid;
            final Descriptor currentRepoDescriptor = this.currentRepoDescriptorDAO.getDescriptor();
            if (currentRepoDescriptor != null)
            {
                uid = currentRepoDescriptor.getId();
                this.staticParameters.put("uid", uid);
            }
            else
            {
                uid = "Unknown";
            }

            final Descriptor serverDescriptor = this.serverDescriptorDAO.getDescriptor();
            this.staticParameters.put("repoName", serverDescriptor.getName());
            this.staticParameters.put("edition", serverDescriptor.getEdition());
            this.staticParameters.put("versionMajor", serverDescriptor.getVersionMajor());
            this.staticParameters.put("versionMinor", serverDescriptor.getVersionMinor());
            this.staticParameters.put("schema", new Integer(serverDescriptor.getSchema()));
            this.staticParameters.put("numUsers", new Integer(this.authorityService.getAllAuthoritiesInZone(
                    AuthorityService.ZONE_APP_DEFAULT, AuthorityType.USER).size()));
            this.staticParameters.put("numGroups", new Integer(this.authorityService.getAllAuthoritiesInZone(
                    AuthorityService.ZONE_APP_DEFAULT, AuthorityType.GROUP).size()));

            if(repoUsageComponent != null)
            {
                RepoUsage usage = repoUsageComponent.getUsage();

                if (usage.getUsers() != null)
                {
                    this.staticParameters.put("licenseUsers", new Long((usage.getUsers())));
                }
            }

        }
    }

    /**
     * Attempts to get all the local IP addresses of this machine in order to distinguish it from other nodes in the
     * same network. The machine may use a static IP address in conjunction with a loopback adapter (e.g. to support
     * Oracle on Windows), so the IP of the default network interface may not be enough to uniquely identify this
     * machine.
     *
     * @return the local IP addresses, separated by the '/' character
     */
    private String getLocalIps()
    {
        final StringBuilder ip = new StringBuilder(1024);
        boolean first = true;
        try
        {
            final Enumeration<NetworkInterface> i = NetworkInterface.getNetworkInterfaces();
            while (i.hasMoreElements())
            {
                final NetworkInterface n = i.nextElement();
                final Enumeration<InetAddress> j = n.getInetAddresses();
                while (j.hasMoreElements())
                {
                    InetAddress a = j.nextElement();
                    if (a.isLoopbackAddress())
                    {
                        continue;
                    }
                    if (first)
                    {
                        first = false;
                    }
                    else
                    {
                        ip.append('/');
                    }
                    ip.append(a.getHostAddress());
                }
            }
        }
        catch (final Exception e)
        {
            // Ignore
        }
        return first ? "127.0.0.1" : ip.toString();
    }
}
