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
package org.alfresco.module.org_alfresco_module_rm.capability.impl;

import java.util.HashMap;
import java.util.Map;

import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCapability;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;

/**
 * File records capability.
 * 
 * @author andyh
 */
public class FileRecordsCapability extends DeclarativeCapability
{
    /** Dictionary service */
    private DictionaryService dictionaryService;
    
    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.DeclarativeCapability#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    public int evaluate(NodeRef nodeRef)
    {
        if (rmService.isFilePlanComponent(nodeRef))
        {            
            // Build the conditions map
            Map<String, Boolean> conditions = new HashMap<String, Boolean>(5);
            conditions.put("capabilityCondition.filling", Boolean.TRUE);
            conditions.put("capabilityCondition.frozen", Boolean.FALSE); 
            conditions.put("capabilityCondition.cutoff", Boolean.FALSE);  
            conditions.put("capabilityCondition.closed", Boolean.FALSE);  
            conditions.put("capabilityCondition.declared", Boolean.FALSE);
            
            if (isFileable(nodeRef) || (rmService.isRecord(nodeRef) && checkConditions(nodeRef, conditions) == true))
            {
                if (permissionService.hasPermission(nodeRef, RMPermissionModel.FILE_RECORDS) == AccessStatus.ALLOWED)
                {
                    return AccessDecisionVoter.ACCESS_GRANTED;
                }
            }
            
            conditions.put("capabilityCondition.closed", Boolean.TRUE);
            if (isFileable(nodeRef) || (rmService.isRecord(nodeRef) && checkConditions(nodeRef, conditions) == true))
            {
                if (checkPermissionsImpl(nodeRef, DECLARE_RECORDS_IN_CLOSED_FOLDERS) == true)
                {
                    return AccessDecisionVoter.ACCESS_GRANTED;
                }
            }            
            
            conditions.put("capabilityCondition.cutoff", Boolean.TRUE);  
            conditions.remove("capabilityCondition.closed");  
            conditions.remove("capabilityCondition.declared");
            if (isFileable(nodeRef) || (rmService.isRecord(nodeRef) && checkConditions(nodeRef, conditions) == true))
            {
                if (checkPermissionsImpl(nodeRef, CREATE_MODIFY_RECORDS_IN_CUTOFF_FOLDERS) == true)
                {
                    return AccessDecisionVoter.ACCESS_GRANTED;
                }
            }

            return AccessDecisionVoter.ACCESS_DENIED;

        }
        else
        {
            return AccessDecisionVoter.ACCESS_ABSTAIN;
        }
    }
    
    /**
     * Indicate whether a node if 'fileable' or not.
     * 
     * @param nodeRef   node reference
     * @return boolean  true if the node is filable, false otherwise
     */
    public boolean isFileable(NodeRef nodeRef)
    {
        QName type = nodeService.getType(nodeRef);
        return dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT);
    }
}
