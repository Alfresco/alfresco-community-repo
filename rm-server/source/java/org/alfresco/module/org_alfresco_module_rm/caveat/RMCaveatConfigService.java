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
    public void init();
    
    /**
     * Get allowed values for given caveat list (for current user)
     * @param constraintName
     * @return
     */
    public List<String> getRMAllowedValues(String constraintName);
    
    /**
     * Check whether access to 'record component' node is vetoed for current user due to caveat(s)
     * 
     * @param nodeRef
     * @return false, if caveat(s) veto access otherwise return true
     */
    public boolean hasAccess(NodeRef nodeRef);
    
    /*
     *  Get a single RM constraint
     */
    public RMConstraintInfo getRMConstraint(String listName);
    
    /*
     *  Get the names of all the caveat lists
     */
    public Set<RMConstraintInfo> getAllRMConstraints();
    
    /**
     * Get the details of a caveat list
     * @param listName
     * @return
     */
    public Map<String, List<String>> getListDetails(String listName);
    
    public NodeRef updateOrCreateCaveatConfig(File jsonFile);
     
    public NodeRef updateOrCreateCaveatConfig(String jsonString);
    
    public NodeRef updateOrCreateCaveatConfig(InputStream is);
    
    /**
     * add RM constraint list
     * @param listName the name of the RMConstraintList
     * @param listTitle 
     */
    public RMConstraintInfo addRMConstraint(String listName, String listTitle, String[] allowedValues);
    
    /**
     * update RM constraint list allowed values
     * @param listName the name of the RMConstraintList - can not be changed
     * @param allowedValues
     */
    public RMConstraintInfo updateRMConstraintAllowedValues(String listName, String[] allowedValues);

    /**
     * update RM constraint Title
     * @param listName the name of the RMConstraintList - can not be changed
     * @param allowedValues
     */
    public RMConstraintInfo updateRMConstraintTitle(String listName, String newTitle);

    
    /**
     * delete RM Constraint
     * 
     * @param listName the name of the RMConstraintList
     */
    public void deleteRMConstraint(String listName);
     
    /**
     * Add a single value to an authority in a list.   The existing values of the list remain.
     * 
     * @param listName the name of the RMConstraintList
     * @param authorityName
     * @param values
     * @throws AlfrescoRuntimeException if either the list or the authority do not already exist.
     */
    public void addRMConstraintListValue(String listName, String authorityName, String value);
    
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
    public void updateRMConstraintListAuthority(String listName, String authorityName, List<String>values);
     
    /**
     * Remove an authority from a list
     * 
     * @param listName the name of the RMConstraintList
     * @param authorityName
     * @param values
     */
    public void removeRMConstraintListAuthority(String listName, String authorityName);
 
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
    public void updateRMConstraintListValue(String listName, String value, List<String>authorities);
     
    /**
     * Remove an authority from a list
     * 
     * @param listName the name of the RMConstraintList
     * @param authorityName
     * @param value
     */
    public void removeRMConstraintListValue(String listName, String valueName);

}
