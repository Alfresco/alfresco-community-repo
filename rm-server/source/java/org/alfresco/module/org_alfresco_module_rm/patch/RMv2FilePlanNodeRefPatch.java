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

import org.alfresco.module.org_alfresco_module_rm.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.security.RecordsManagementSecurityService;
import org.alfresco.module.org_alfresco_module_rm.security.Role;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;

/**
 * RM v2.0 File Plan Node Ref Patch
 * 
 * @author Roy Wetherall
 */
public class RMv2FilePlanNodeRefPatch extends AbstractModuleComponent 
                                      implements BeanNameAware, RecordsManagementModel, DOD5015Model
{
    /** Logger */
    private static Log logger = LogFactory.getLog(RMv2FilePlanNodeRefPatch.class);  
    
    private NodeService nodeService;
    private RecordsManagementService recordsManagementService;
    private BehaviourFilter behaviourFilter;
    private PatchDAO patchDAO;
    private NodeDAO nodeDAO;
    private QNameDAO qnameDAO;
    private PermissionService permissionService;
    private RecordsManagementSecurityService recordsManagementSecurityService;
    private RetryingTransactionHelper retryingTransactionHelper;
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }
    
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
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
     * @param recordsManagementSecurityService  records management security service
     */
    public void setRecordsManagementSecurityService(RecordsManagementSecurityService recordsManagementSecurityService)
    {
        this.recordsManagementSecurityService = recordsManagementSecurityService;
    }
    
    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }
    
    /**
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executeInternal() throws Throwable
    {
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("RM Module RMv2FilePlanNodeRef Patch ...");
        }
        
        retryingTransactionHelper.doInTransaction(new Work(), false, true);
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("   ... complete RM Module RMv2FilePlanNodeRef Patch");
        }
        
    }
    
    private class Work implements RetryingTransactionHelper.RetryingTransactionCallback<Integer>
    {	
    	@Override
    	public Integer execute() throws Throwable
    	{
    		Pair<Long, QName> aspectPair = qnameDAO.getQName(RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT);
            if (aspectPair != null)
            {
                List<Long> filePlanComponents = patchDAO.getNodesByAspectQNameId(aspectPair.getFirst(), 0L, patchDAO.getMaxAdmNodeID());
        
                if (logger.isDebugEnabled() == true)
                {
                    logger.debug("   ... updating " + filePlanComponents.size() + " items" );
                }
                
                behaviourFilter.disableBehaviour();
                try
                {
                    for (Long filePlanComponent : filePlanComponents)
                    {
                        Pair<Long, NodeRef> recordPair = nodeDAO.getNodePair(filePlanComponent);
                        NodeRef filePlanComponentNodeRef = recordPair.getSecond();
                        
                        NodeRef filePlan =  recordsManagementService.getFilePlan(filePlanComponentNodeRef);
                        
                        // set the file plan node reference
                        if (nodeService.getProperty(filePlanComponentNodeRef, RecordsManagementModel.PROP_ROOT_NODEREF) == null)
                        {
                           nodeService.setProperty(filePlanComponentNodeRef, RecordsManagementModel.PROP_ROOT_NODEREF, filePlan);
                        }                    
                        
                        // only set the rmadmin permissions on record categories, record folders and records
                        FilePlanComponentKind kind = recordsManagementService.getFilePlanComponentKind(filePlanComponentNodeRef);
                        if (FilePlanComponentKind.RECORD_CATEGORY.equals(kind) == true ||
                            FilePlanComponentKind.RECORD_FOLDER.equals(kind) == true ||
                            FilePlanComponentKind.RECORD.equals(kind) == true )
                        {
                            // ensure the that the records management role has read and file on the node
                            Role adminRole = recordsManagementSecurityService.getRole(filePlan, "Administrator");
                            if (adminRole != null)
                            {
                                permissionService.setPermission(filePlanComponentNodeRef, adminRole.getRoleGroupName(), RMPermissionModel.FILING, true);
                            }
                            
                            // ensure that the default vital record default values have been set (RM-753)
                            Serializable vitalRecordIndicator = nodeService.getProperty(filePlanComponentNodeRef, RecordsManagementModel.PROP_VITAL_RECORD_INDICATOR);
                            if (vitalRecordIndicator == null)
                            {
                                nodeService.setProperty(filePlanComponentNodeRef, RecordsManagementModel.PROP_VITAL_RECORD_INDICATOR, false);
                            }
                            Serializable reviewPeriod = nodeService.getProperty(filePlanComponentNodeRef, RecordsManagementModel.PROP_REVIEW_PERIOD);
                            if (reviewPeriod == null)
                            {
                                nodeService.setProperty(filePlanComponentNodeRef, RecordsManagementModel.PROP_REVIEW_PERIOD, new Period("none|0"));
                            }
                        }
                    }
                }
                finally
                {
                    behaviourFilter.enableBehaviour();
                }
            }
            // nothing to do
    		return 0;
    	}
    }
}
