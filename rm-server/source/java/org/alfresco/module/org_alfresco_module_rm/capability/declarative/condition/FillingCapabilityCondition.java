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
package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;

/**
 * Filling capability condition.
 * 
 * @author Roy Wetherall
 */
public class FillingCapabilityCondition extends AbstractCapabilityCondition
{
    /** Dictionary service */
    private DictionaryService dictionaryService;
    
    /**
     * @param dictionaryService     dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }    
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluate(NodeRef nodeRef)
    {
        boolean result = false;
        
        NodeRef filePlan = rmService.getFilePlan(nodeRef);
        
        if (permissionService.hasPermission(filePlan, RMPermissionModel.ROLE_ADMINISTRATOR) == AccessStatus.ALLOWED)
        {
            result = true;
        }
        else
        {
            QName nodeType = nodeService.getType(nodeRef);
            if (rmService.isRecord(nodeRef) == true ||
                dictionaryService.isSubClass(nodeType, ContentModel.TYPE_CONTENT) == true)
            {
                // Multifiling - if you have filing rights to any of the folders in which the record resides
                // then you have filing rights.
                for (ChildAssociationRef car : nodeService.getParentAssocs(nodeRef))
                {
                    if (car != null)
                    {
                        if (permissionService.hasPermission(car.getParentRef(), RMPermissionModel.FILE_RECORDS) == AccessStatus.ALLOWED)
                        {
                            result = true;
                            break;
                        }
                    }
                }                
            }
            else if (rmService.isRecordFolder(nodeRef) == true)
            {
                if (permissionService.hasPermission(nodeRef, RMPermissionModel.FILE_RECORDS) != AccessStatus.DENIED)
                {
                    result = true;
                }
            }
            else if (rmService.isRecordCategory(nodeRef) == true)
            {
                if (permissionService.hasPermission(nodeRef, RMPermissionModel.FILE_RECORDS) != AccessStatus.DENIED)
                {
                    result = true;
                }
                else if (permissionService.hasPermission(filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FOLDERS) != AccessStatus.DENIED)
                {
                    result = true;
                }
            }
            // else other file plan component
            else
            {
                if (permissionService.hasPermission(nodeRef, RMPermissionModel.FILE_RECORDS) != AccessStatus.DENIED)
                {
                    result = true;
                }
                else if (permissionService.hasPermission(filePlan, RMPermissionModel.CREATE_MODIFY_DESTROY_FILEPLAN_METADATA) != AccessStatus.DENIED)
                {
                    result = true;
                }
            }

        }
        
        return result;
    }
}
