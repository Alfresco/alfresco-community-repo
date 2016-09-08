/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.module.org_alfresco_module_rm.capability;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;

/**
 * Capability service implementation
 *
 * @author Roy Wetherall
 * @since 2.0
 */
public interface CapabilityService
{
   /**
    * Register a capability
    *
    * @param capability    capability
    */
   void registerCapability(Capability capability);

   /**
    * Get a named capability.
    *
    * @param name  capability name
    * @return {@link Capability}   capability or null if not found
    */
   Capability getCapability(String name);

   /**
    * Get a list of all the assignable capabilities.
    *
    * @return  {@link Set}<{@link Capability}>     set of all the assignable capabilities
    */
   Set<Capability> getCapabilities();

   /**
    * Get a list of all the capabilities, optionally including those that are non-assignable.
    *
    * @param includePrivate    indicates that the private, or non-assignable capabilities are included in the result
    * @return  {@link Set}<{@link Capability}>     set of capabilities
    */
   Set<Capability> getCapabilities(boolean includePrivate);

   /**
    * Get all the capabilities access state based on the current user for the assignable capabilities.
    *
    * @param nodeRef   node reference
    * @return
    */
   Map<Capability, AccessStatus> getCapabilitiesAccessState(NodeRef nodeRef);

   /**
    * Get all the capabilities access state based on the current user.
    *
    * @param nodeRef   node reference
    * @return
    */
   Map<Capability, AccessStatus> getCapabilitiesAccessState(NodeRef nodeRef, boolean includePrivate);

   /**
    *
    * @param nodeRef
    * @param capabilityNames
    * @return
    */
   Map<Capability, AccessStatus> getCapabilitiesAccessState(NodeRef nodeRef, List<String> capabilityNames);

   /**
    * Helper method to get the access state for a single capability.
    *
    * @param nodeRef
    * @param capabilityName
    * @return
    */
   AccessStatus getCapabilityAccessState(NodeRef nodeRef, String capabilityName);

   /**
    * Gets the list of all the capability groups (in index order)
    *
    * @return {@link List}<{@link Group}> List of all the capability groups (in index order) 
    */
   List<Group> getGroups();

   /**
    * Gets a list of capabilities for the given group id
    *
    * @param groupId The id of a group for which the list of capabilities should be retrieved
    * @return {@link List}<{@link Capability}> List of capabilities for the given group
    */
   List<Capability> getCapabilitiesByGroupId(String groupId);

   /**
    * Get a list of capabilities for the given group
    *
    * @param group The group for which the list of capabilities should be retrieved
    * @return {@link List}<{@link Capability}> List of capabilities for the given group
    */
   List<Capability> getCapabilitiesByGroup(Group group);

   /**
    * Gets a group from it's id
    *
    * @param groupId The id of the group which should be retrieved
    * @return Group The group with the id groupId
    */
   Group getGroup(String groupId);

   /**
    * Adds a group to the list of groups
    *
    * @param group The group which should be added
    */
   void addGroup(Group group);

   /**
    * Removes a group from the list of groups
    *
    * @param group The group which should be removed
    */
   void removeGroup(Group group);
}
