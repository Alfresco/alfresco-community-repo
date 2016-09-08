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
package org.alfresco.module.org_alfresco_module_rm.patch;

import java.io.Serializable;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.springframework.beans.factory.BeanNameAware;

/**
 * RM v2.0 File Plan Node Ref Patch
 * 
 * @author Roy Wetherall
 */
public class RMv2FilePlanNodeRefPatch extends ModulePatchComponent 
                                      implements BeanNameAware, RecordsManagementModel, DOD5015Model
{    
    private NodeService nodeService;
    private PatchDAO patchDAO;
    private NodeDAO nodeDAO;
    private QNameDAO qnameDAO;
    private PermissionService permissionService;
    private FilePlanService filePlanService;
    private FilePlanRoleService filePlanRoleService;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }
    
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }
    
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }
    
    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * @param filePlanRoleService	file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService) 
    {
		this.filePlanRoleService = filePlanRoleService;
	}
    
    /**
     * @param filePlanService	file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService) 
    {
		this.filePlanService = filePlanService;
	}
    
    /**
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executePatch() throws Throwable
    {        
    	Pair<Long, QName> aspectPair = qnameDAO.getQName(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT);
        if (aspectPair != null)
        {
            List<Long> filePlanComponents = patchDAO.getNodesByAspectQNameId(aspectPair.getFirst(), 0L, patchDAO.getMaxAdmNodeID());
    
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("   ... updating " + filePlanComponents.size() + " items" );
            }
            
            
            for (Long filePlanComponent : filePlanComponents)
            {
                Pair<Long, NodeRef> recordPair = nodeDAO.getNodePair(filePlanComponent);
                NodeRef filePlanComponentNodeRef = recordPair.getSecond();
                
                NodeRef filePlan =  filePlanService.getFilePlan(filePlanComponentNodeRef);
                
                // set the file plan node reference
                if (nodeService.getProperty(filePlanComponentNodeRef, PROP_ROOT_NODEREF) == null)
                {
                   nodeService.setProperty(filePlanComponentNodeRef, PROP_ROOT_NODEREF, filePlan);
                }                    
                
                // only set the rmadmin permissions on record categories, record folders and records
                FilePlanComponentKind kind = filePlanService.getFilePlanComponentKind(filePlanComponentNodeRef);
                if (FilePlanComponentKind.RECORD_CATEGORY.equals(kind) == true ||
                    FilePlanComponentKind.RECORD_FOLDER.equals(kind) == true ||
                    FilePlanComponentKind.RECORD.equals(kind) == true )
                {
                    // ensure the that the records management role has read and file on the node
                    Role adminRole = filePlanRoleService.getRole(filePlan, "Administrator");
                    if (adminRole != null)
                    {
                        permissionService.setPermission(filePlanComponentNodeRef, adminRole.getRoleGroupName(), RMPermissionModel.FILING, true);
                    }
                    
                    // ensure that the default vital record default values have been set (RM-753)
                    Serializable vitalRecordIndicator = nodeService.getProperty(filePlanComponentNodeRef, PROP_VITAL_RECORD_INDICATOR);
                    if (vitalRecordIndicator == null)
                    {
                        nodeService.setProperty(filePlanComponentNodeRef, PROP_VITAL_RECORD_INDICATOR, false);
                    }
                    Serializable reviewPeriod = nodeService.getProperty(filePlanComponentNodeRef, PROP_REVIEW_PERIOD);
                    if (reviewPeriod == null)
                    {
                        nodeService.setProperty(filePlanComponentNodeRef, PROP_REVIEW_PERIOD, new Period("none|0"));
                    }
                }
            }            
        }
    }
}
