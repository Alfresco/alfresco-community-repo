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

package org.alfresco.module.org_alfresco_module_rm.script.capability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.Group;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Capabilities GET web service implementation.
 */
public class CapabilitiesGet extends DeclarativeWebScript
{
	/** File plan service */
    private FilePlanService filePlanService;

    /** Capability service */
    private CapabilityService capabilityService;

    /**
     * @param capabilityService	capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    /**
     * @param filePlanService	file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
		this.filePlanService = filePlanService;
	}

    /**
     * @see org.alfresco.repo.web.scripts.content.StreamContent#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String storeType = templateVars.get("store_type");
        String storeId = templateVars.get("store_id");
        String nodeId = templateVars.get("id");

        NodeRef nodeRef = null;
        if (StringUtils.isNotBlank(storeType) && StringUtils.isNotBlank(storeId) && StringUtils.isNotBlank(nodeId))
        {
            nodeRef = new NodeRef(new StoreRef(storeType, storeId), nodeId);
        }
        else
        {
            // we are talking about the file plan node
            // TODO we are making the assumption there is only one file plan here!
            nodeRef = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            if (nodeRef == null)
            {
                throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "The default file plan node could not be found.");
            }
        }

        boolean grouped = false;
        String groupedString = req.getParameter("grouped");
        if (StringUtils.isNotBlank(groupedString))
        {
            grouped = Boolean.parseBoolean(groupedString);
        }

        Map<String, Object> model = new TreeMap<>();
        if (grouped)
        {
            // Construct the map which is needed to build the model
            Map<String, GroupedCapabilities> groupedCapabilitiesMap = new TreeMap<>();

            List<Group> groups = capabilityService.getGroups();
            for (Group group : groups)
            {
                String capabilityGroupTitle = group.getTitle();
                if (StringUtils.isNotBlank(capabilityGroupTitle))
                {
                    String capabilityGroupId = group.getId();

                    List<Capability> capabilities = capabilityService.getCapabilitiesByGroupId(capabilityGroupId);
                    for (Capability capability : capabilities)
                    {
                       String capabilityName = capability.getName();
                       String capabilityTitle = capability.getTitle();

                       if (groupedCapabilitiesMap.containsKey(capabilityGroupId))
                       {
                           groupedCapabilitiesMap.get(capabilityGroupId).addCapability(capabilityName, capabilityTitle);
                       }
                       else
                       {
                           GroupedCapabilities groupedCapabilities = new GroupedCapabilities(capabilityGroupId, capabilityGroupTitle, capabilityName, capabilityTitle);
                           groupedCapabilities.addCapability(capabilityName, capabilityTitle);
                           groupedCapabilitiesMap.put(capabilityGroupId, groupedCapabilities);
                       }
                    }
                }
            }
            model.put("groupedCapabilities", groupedCapabilitiesMap);
        }
        else
        {
            boolean includePrivate = false;
            String includePrivateString = req.getParameter("includeAll");
            if (StringUtils.isNotBlank(includePrivateString))
            {
                includePrivate = Boolean.parseBoolean(includePrivateString);
            }

            Map<Capability, AccessStatus> map = capabilityService.getCapabilitiesAccessState(nodeRef, includePrivate);
            List<String> list = new ArrayList<>(map.size());
            for (Map.Entry<Capability, AccessStatus> entry : map.entrySet())
            {
                AccessStatus accessStatus = entry.getValue();
                if (!AccessStatus.DENIED.equals(accessStatus))
                {
                    Capability capability = entry.getKey();
                    list.add(capability.getName());
                }
            }
            model.put("capabilities", list);
        }

        return model;
    }

    /**
     * Class to represent grouped capabilities for use in a Freemarker template
     *
     */
    public class GroupedCapabilities
    {
        private String capabilityGroupId;
        private String capabilityGroupTitle;
        private String capabilityName;
        private String capabilityTitle;
        private Map<String, String> capabilities;

        public GroupedCapabilities(String capabilityGroupId, String capabilityGroupTitle, String capabilityName, String capabilityTitle)
        {
            this.capabilityGroupId = capabilityGroupId;
            this.capabilityGroupTitle = capabilityGroupTitle;
            this.capabilityName = capabilityName;
            this.capabilityTitle = capabilityTitle;
            this.capabilities = new TreeMap<>();
        }

        public String getGroupId()
        {
            return this.capabilityGroupId;
        }

        public String getGroupTitle()
        {
            return this.capabilityGroupTitle;
        }

        public String getCapabilityName()
        {
            return this.capabilityName;
        }

        public String getCapabilityTitle()
        {
            return this.capabilityTitle;
        }

        public Map<String, String> getCapabilities()
        {
            return this.capabilities;
        }

        public void addCapability(String capabilityName, String capabilityTitle)
        {
            this.capabilities.put(capabilityName, capabilityTitle);
        }
    }
}
