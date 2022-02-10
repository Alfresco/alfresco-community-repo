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

package org.alfresco.module.org_alfresco_module_rm.capability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.util.ParameterCheck;

/**
 * @author Roy Wetherall
 * @since 2.0
 */
public class CapabilityServiceImpl implements CapabilityService
{
    /** Capabilities */
    private Map<String, Capability> capabilities = new HashMap<>(57);

    /** Groups */
    private Map<String, Group> groups = new HashMap<>(13);

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapability(java.lang.String)
     */
    @Override
    public Capability getCapability(String name)
    {
        ParameterCheck.mandatoryString("name", name);

        return capabilities.get(name);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#registerCapability(org.alfresco.module.org_alfresco_module_rm.capability.Capability)
     */
    @Override
    public void registerCapability(Capability capability)
    {
        ParameterCheck.mandatory("capability", capability);

        capabilities.put(capability.getName(), capability);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapabilities()
     */
    @Override
    public Set<Capability> getCapabilities()
    {
        return getCapabilities(true);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapabilities(boolean)
     */
    @Override
    public Set<Capability> getCapabilities(boolean includePrivate)
    {
        Set<Capability> result = null;
        if (includePrivate)
        {
            result = new HashSet<>(capabilities.values());
        }
        else
        {
            result = new HashSet<>(capabilities.size());
            for (Capability capability : capabilities.values())
            {
                if (!capability.isPrivate())
                {
                    result.add(capability);
                }
            }
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapabilitiesAccessState(org.alfresco.service.cmr.repository.NodeRef)
     */
    public Map<Capability, AccessStatus> getCapabilitiesAccessState(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        return getCapabilitiesAccessState(nodeRef, false);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapabilitiesAccessState(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override
    public Map<Capability, AccessStatus> getCapabilitiesAccessState(NodeRef nodeRef, boolean includePrivate)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        Set<Capability> listOfCapabilites = getCapabilities(includePrivate);
        HashMap<Capability, AccessStatus> answer = new HashMap<>();
        for (Capability capability : listOfCapabilites)
        {
            AccessStatus status = capability.hasPermission(nodeRef);
            if (answer.put(capability, status) != null)
            {
                throw new IllegalStateException();
            }
        }
        return answer;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapabilitiesAccessState(org.alfresco.service.cmr.repository.NodeRef, java.util.List)
     */
    public Map<Capability, AccessStatus> getCapabilitiesAccessState(NodeRef nodeRef, List<String> capabilityNames)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("capabilityNames", capabilityNames);

        HashMap<Capability, AccessStatus> answer = new HashMap<>();
        for (String capabilityName : capabilityNames)
        {
            Capability capability = capabilities.get(capabilityName);
            if (capability != null)
            {
                AccessStatus status = capability.hasPermission(nodeRef);
                if (answer.put(capability, status) != null)
                {
                    throw new IllegalStateException();
                }
            }
        }
        return answer;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapabilityAccessState(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public AccessStatus getCapabilityAccessState(NodeRef nodeRef, String capabilityName)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("capabilityName", capabilityName);

        AccessStatus result = AccessStatus.UNDETERMINED;
        Capability capability = getCapability(capabilityName);
        if (capability != null)
        {
            List<String> list = Collections.singletonList(capabilityName);
            Map<Capability, AccessStatus> map = getCapabilitiesAccessState(nodeRef, list);
            result = map.get(capability);
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getGroups()
     */
    @Override
    public List<Group> getGroups()
    {
        List<Group> groups = new ArrayList<>();
        for (Map.Entry<String, Group> entry : this.groups.entrySet())
        {
            groups.add(entry.getValue());
        }

        Collections.sort(groups, new Comparator<Group>()
        {
            @Override
            public int compare(Group g1, Group g2)
            {
                return g1.getIndex() - g2.getIndex();
            }
        });

        return groups;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapabilitiesByGroupId(java.lang.String)
     */
    @Override
    public List<Capability> getCapabilitiesByGroupId(String groupId)
    {
        ParameterCheck.mandatoryString("groupId", groupId);

        String id = this.groups.get(groupId).getId();

        List<Capability> capabilities = new ArrayList<>();
        for (Capability capability : getCapabilities())
        {
            Group group = capability.getGroup();
            if (group != null && group.getId().equalsIgnoreCase(id))
            {
                capabilities.add(capability);
            }
        }

        Collections.sort(capabilities, new Comparator<Capability>()
        {
            @Override
            public int compare(Capability c1, Capability c2)
            {
                return c1.getIndex() - c2.getIndex();
            }
        });

        return capabilities;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getCapabilitiesByGroup(org.alfresco.module.org_alfresco_module_rm.capability.Group)
     */
    @Override
    public List<Capability> getCapabilitiesByGroup(Group group)
    {
        ParameterCheck.mandatory("group", group);

        return getCapabilitiesByGroupId(group.getId());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#getGroup(java.lang.String)
     */
    @Override
    public Group getGroup(String groupId)
    {
        ParameterCheck.mandatoryString("groupId", groupId);

        return this.groups.get(groupId);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#addGroup(org.alfresco.module.org_alfresco_module_rm.capability.Group)
     */
    @Override
    public void addGroup(Group group)
    {
        ParameterCheck.mandatory("group", group);

        groups.put(group.getId(), group);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#removeGroup(org.alfresco.module.org_alfresco_module_rm.capability.Group)
     */
    @Override
    public void removeGroup(Group group)
    {
        ParameterCheck.mandatory("group", group);

        groups.remove(group.getId());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService#hasCapability(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public boolean hasCapability(NodeRef nodeRef, String capabilityName)
    {
        Capability capability = getCapability(capabilityName);
        if (capability != null)
        {
            AccessStatus accessStatus = getCapabilityAccessState(nodeRef, capabilityName);

            if (accessStatus.equals(AccessStatus.ALLOWED))
            {
                return true;
            }
        }

        return false;
    }
}
