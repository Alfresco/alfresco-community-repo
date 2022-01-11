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

package org.alfresco.module.org_alfresco_module_rm.recordableversion;

import static org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy.NONE;
import static org.alfresco.util.ParameterCheck.mandatory;
import static org.alfresco.util.ParameterCheck.mandatoryString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.script.slingshot.Version;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Recordable version config service
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class RecordableVersionConfigServiceImpl implements RecordableVersionConfigService, RecordableVersionModel
{
    /** Node service */
    private NodeService nodeService;

    /**
     * Gets the node service
     *
     * @return The node service
     */
    protected NodeService getNodeService()
    {
        return this.nodeService;
    }

    /**
     * Sets the node service
     *
     * @param nodeService The node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.recordableversion.RecordableVersionConfigService#getVersions(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<Version> getVersions(NodeRef nodeRef)
    {
        mandatory("nodeRef", nodeRef);

        RecordableVersionPolicy[] recordableVersionPolicies = RecordableVersionPolicy.values();
        List<Version> versions = new ArrayList<>(recordableVersionPolicies.length);

        for (RecordableVersionPolicy recordableVersionPolicy : recordableVersionPolicies)
        {
            String policy = recordableVersionPolicy.toString();
            boolean selected = isVersionPolicySelected(recordableVersionPolicy, nodeRef);
            versions.add(new Version(policy, selected));
        }

        return versions;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.recordableversion.RecordableVersionConfigService#setVersion(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public void setVersion(NodeRef nodeRef, String version)
    {
        mandatory("nodeRef", nodeRef);
        mandatoryString("recordedVersion", version);

        RecordableVersionPolicy recordableVersionPolicy = RecordableVersionPolicy.valueOf(version);
        getNodeService().setProperty(nodeRef, PROP_RECORDABLE_VERSION_POLICY, recordableVersionPolicy);
    }

    /**
     * Checks if the specified recordable version policy has been selected for the document
     *
     * @param recordableVersionPolicy The recordable version policy
     * @param nodeRef Node reference of the document
     * @return <code>true</code> if the specified recordable version policy has been selected for the document, <code>false</code> otherwise
     */
    private boolean isVersionPolicySelected(RecordableVersionPolicy recordableVersionPolicy, NodeRef nodeRef)
    {
        boolean isVersionPolicySelected = false;
        String policy = (String) getNodeService().getProperty(nodeRef, PROP_RECORDABLE_VERSION_POLICY);
        if (isNotBlank(policy))
        {
            if (RecordableVersionPolicy.valueOf(policy).equals(recordableVersionPolicy))
            {
                isVersionPolicySelected = true;
            }
        }
        else
        {
            if (recordableVersionPolicy.equals(NONE))
            {
                isVersionPolicySelected = true;
            }
        }
        return isVersionPolicySelected;
    }
}
