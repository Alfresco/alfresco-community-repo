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
import org.alfresco.repo.deployment.DeploymentMethodProvider;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

/**
 * A collector of data related to the meta-data for the Alfresco stack.
 * <ul>
 *  <li>Collector ID: <b>acs.repository.info</b></li>
 *  <li>Data:
 *      <ul>
 *          <li><b>repoName:</b> Int number of active models. {@link Descriptor#getName()}</li>
 *          <li><b>version: Object which contains version information:</b>
 *              <ul>
 *                  <li>full: String - The full version number. {@link Descriptor#getVersion()}</li>
 *                  <li>servicePack: String - The major and minor version, e.g. <u>1.2</u>.3</li>
 *                  <li>major: String - The major version number, e.g. <u>1</u>.2.3. {@link Descriptor#getVersionMajor()}</li>
 *                  <li>minor: String - The minor version number, e.g. 1.<u>2</u>.3. {@link Descriptor#getVersionMinor()}</li>
 *                  <li>patch: String - The version revision number, e.g. 1.2.<u>3</u>. {@link Descriptor#getVersionRevision()}</li>
 *                  <li>hotfix: String - The version label. {@link Descriptor#getVersionLabel()}</li>
 *              </ul>
 *          </li>
 *          <li><b>schema:</b> Int - The schema number. {@link Descriptor#getSchema()}</li>
 *          <li><b>edition:</b> String - The edition. {@link Descriptor#getEdition()}</li>
 *          <li><b>deploymentMethod:</b> String - The deployment method used to deploy this Alfresco instance. {@link DeploymentMethodProvider#getDeploymentMethod()}</li>
 *      </ul>
 *  </li>
 * </ul>
 */
public class InfoDataCollector extends HBBaseDataCollector implements InitializingBean
{
    /** The logger. */
    private static final Log logger = LogFactory.getLog(InfoDataCollector.class);

    /** DAO for current repository descriptor. */
    private DescriptorDAO currentRepoDescriptorDAO;

    /** DAO for current descriptor. */
    private DescriptorDAO serverDescriptorDAO;

    private DeploymentMethodProvider deploymentMethodProvider;

    public InfoDataCollector(String collectorId, String collectorVersion, String cronExpression)
    {
        super(collectorId, collectorVersion, cronExpression);
    }

    public void setCurrentRepoDescriptorDAO(DescriptorDAO currentRepoDescriptorDAO)
    {
        this.currentRepoDescriptorDAO = currentRepoDescriptorDAO;
    }

    public void setServerDescriptorDAO(DescriptorDAO serverDescriptorDAO)
    {
        this.serverDescriptorDAO = serverDescriptorDAO;
    }

    public void setDeploymentMethodProvider(DeploymentMethodProvider deploymentMethodProvider)
    {
        this.deploymentMethodProvider = deploymentMethodProvider;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "serverDescriptorDAO", serverDescriptorDAO);
        PropertyCheck.mandatory(this, "currentRepoDescriptorDAO", currentRepoDescriptorDAO);
        PropertyCheck.mandatory(this, "deploymentMethodProvider", deploymentMethodProvider);
    }

    @Override
    public List<HBData> collectData()
    {
        logger.debug("Preparing repository info data...");

        final Descriptor serverDescriptor = this.serverDescriptorDAO.getDescriptor();

        Map<String, Object> infoValues = new HashMap<>();
        infoValues.put("repoName", serverDescriptor.getName());

        Map<String, Object> version = new HashMap<>();
        version.put("full", serverDescriptor.getVersion());
        version.put("servicePack", serverDescriptor.getVersionMajor() + "." + serverDescriptor.getVersionMinor());
        version.put("major", serverDescriptor.getVersionMajor());
        version.put("minor", serverDescriptor.getVersionMinor());
        version.put("patch", serverDescriptor.getVersionRevision());
        String hotfix = serverDescriptor.getVersionLabel();
        if (hotfix != null && hotfix.length() > 0)
        {
            version.put("hotfix", hotfix.startsWith(".") ? hotfix.substring(1) : hotfix);
        }
        infoValues.put("version", version);
        infoValues.put("schema", new Integer(serverDescriptor.getSchema()));
        infoValues.put("edition", serverDescriptor.getEdition());
        infoValues.put("deploymentMethod", deploymentMethodProvider.getDeploymentMethod().toString());

        HBData infoData = new HBData(
                this.currentRepoDescriptorDAO.getDescriptor().getId(),
                this.getCollectorId(),
                this.getCollectorVersion(),
                new Date(),
                infoValues);

        return Arrays.asList(infoData);
    }

}
