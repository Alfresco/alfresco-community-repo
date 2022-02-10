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

package org.alfresco.module.org_alfresco_module_rm.caveat;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;

public interface RMCaveatConfigService
{
    void init();

    /**
     * Get allowed values for given caveat list (for current user)
     * @param constraintName
     * @return
     */
    List<String> getRMAllowedValues(String constraintName);

    /**
     * Check whether access to 'record component' node is vetoed for current user due to caveat(s)
     *
     * @param nodeRef
     * @return false, if caveat(s) veto access otherwise return true
     */
    boolean hasAccess(NodeRef nodeRef);

    /*
     *  Get a single RM constraint
     */
    RMConstraintInfo getRMConstraint(String listName);

    /*
     *  Get the names of all the caveat lists
     */
    Set<RMConstraintInfo> getAllRMConstraints();

    /**
     * Get the details of a caveat list
     * @param listName
     * @return
     */
    Map<String, List<String>> getListDetails(String listName);

    NodeRef updateOrCreateCaveatConfig(File jsonFile);

    NodeRef updateOrCreateCaveatConfig(String jsonString);

    NodeRef updateOrCreateCaveatConfig(InputStream is);

    /**
     * add RM constraint list
     * @param listName the name of the RMConstraintList
     * @param listTitle
     */
    RMConstraintInfo addRMConstraint(String listName, String listTitle, String[] allowedValues);

    /**
     * update RM constraint list allowed values
     * @param listName the name of the RMConstraintList - can not be changed
     * @param allowedValues
     */
    RMConstraintInfo updateRMConstraintAllowedValues(String listName, String[] allowedValues);

    /**
     * update RM constraint Title
     * @param listName the name of the RMConstraintList - can not be changed
     * @param newTitle the new value for the title constraint
     */
    RMConstraintInfo updateRMConstraintTitle(String listName, String newTitle);


    /**
     * delete RM Constraint
     *
     * @param listName the name of the RMConstraintList
     */
    void deleteRMConstraint(String listName);

    /**
     * Add a single value to an authority in a list.   The existing values of the list remain.
     *
     * @param listName the name of the RMConstraintList
     * @param authorityName
     * @param value
     * @throws AlfrescoRuntimeException if either the list or the authority do not already exist.
     */
    void addRMConstraintListValue(String listName, String authorityName, String value);

    /**
     * Replace the values for an authority in a list.
     * The existing values are removed.
     *
     * If the authority does not already exist in the list, it will be added
     *
     * @param listName the name of the RMConstraintList
     * @param authorityName
     * @param values
     */
    void updateRMConstraintListAuthority(String listName, String authorityName, List<String>values);

    /**
     * Remove an authority from a list
     *
     * @param listName the name of the RMConstraintList
     * @param authorityName
     */
    void removeRMConstraintListAuthority(String listName, String authorityName);

    /**
     * Replace the values for an authority in a list.
     * The existing values are removed.
     *
     * If the authority does not already exist in the list, it will be added
     *
     * @param listName the name of the RMConstraintList
     * @param value
     * @param authorities
     */
    void updateRMConstraintListValue(String listName, String value, List<String>authorities);

    /**
     * Remove an authority from a list
     *
     * @param listName the name of the RMConstraintList
     * @param valueName
     */
    void removeRMConstraintListValue(String listName, String valueName);
}
