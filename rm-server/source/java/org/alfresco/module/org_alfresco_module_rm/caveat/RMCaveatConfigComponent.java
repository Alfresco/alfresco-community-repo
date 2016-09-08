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

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface RMCaveatConfigComponent
{
    public void init();
    
    /**
     * Get allowed values for given caveat list (for current user)
     * 
     * @param constraintName
     * @return
     */
    public List<String> getRMAllowedValues(String constraintName);
    
    /**
     * Get custom caveat models
     * 
     * @return
     */
    public List<QName> getRMCaveatModels();
    
    /**
     * Check whether access to 'record component' node is vetoed for current user due to caveat(s)
     * 
     * @param nodeRef
     * @return false, if caveat(s) veto access otherwise return true
     */
    public boolean hasAccess(NodeRef nodeRef);
    
    /**
     * Get RM constraint list
     * 
     * @param listName the name of the RMConstraintList
     */
    public RMConstraintInfo getRMConstraint(String listName);
    
    /**
     * Add RM constraint
     */
    public void addRMConstraint(String listName);
    
    /**
     * Add RM constraint value for given authority
     */
    public void addRMConstraintListValue(String listName, String authorityName, String value);
    
    /**
     * Update RM constraint values for given authority
     */
    public void updateRMConstraintListAuthority(String listName, String authorityName, List<String>values);
    
    /**
     * Update RM constraint authorities for given value
     */
    public void updateRMConstraintListValue(String listName, String valueName, List<String>authorities);
    
    /**
     * Remove RM constraint value (all authorities)
     */
    public void removeRMConstraintListValue(String listName, String valueName);
    
    /**
     * Remove RM constraint authority (all values)
     */
    public void removeRMConstraintListAuthority(String listName, String authorityName);
    
    /**
     * Delete RM Constraint
     * 
     * @param listName the name of the RMConstraintList
     */
    public void deleteRMConstraint(String listName);
    
    /**
     * Get the details of a caveat list
     * @param listName
     * @return
     */
    public Map<String, List<String>> getListDetails(String listName);
    
    public NodeRef updateOrCreateCaveatConfig(File jsonFile);
     
    public NodeRef updateOrCreateCaveatConfig(String jsonString);
    
    public NodeRef updateOrCreateCaveatConfig(InputStream is);
}
