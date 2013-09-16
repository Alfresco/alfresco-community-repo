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

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executeInternal() throws Throwable
    {
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("RM Module RMv2FilePlanNodeRef Patch ...");
        }
        
        Pair<Long, QName> aspectPair = qnameDAO.getQName(ASPECT_FILE_PLAN_COMPONENT);
        if (aspectPair != null)
        {
            List<Long> records = patchDAO.getNodesByAspectQNameId(aspectPair.getFirst(), 0L, patchDAO.getMaxAdmNodeID());
    
            if (logger.isDebugEnabled() == true)
            {
                logger.debug("   ... updating " + records.size() + " items" );
            }
            
            behaviourFilter.disableBehaviour();
            try
            {
                for (Long record : records)
                {
                    Pair<Long, NodeRef> recordPair = nodeDAO.getNodePair(record);
                    NodeRef recordNodeRef = recordPair.getSecond();
                    
                    if (nodeService.getProperty(recordNodeRef, PROP_ROOT_NODEREF) == null)
                    {
                       nodeService.setProperty(recordNodeRef, PROP_ROOT_NODEREF, recordsManagementService.getFilePlan(recordNodeRef));
                    }
                }
            }
            finally
            {
                behaviourFilter.enableBehaviour();
            }
        }
        
        if (logger.isDebugEnabled() == true)
        {
            logger.debug("   ... complete RM Module RMv2FilePlanNodeRef Patch");
        }
        
    }   
}
