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

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.module.AbstractModuleComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;

/**
 * RM v2.0 Model Updates Patch
 * 
 * 
 * @author Roy Wetherall
 */
public class RMv2ModelPatch extends AbstractModuleComponent 
                            implements BeanNameAware, RecordsManagementModel, DOD5015Model
{
    /** Logger */
    private static Log logger = LogFactory.getLog(RMv2ModelPatch.class);  
    
    private static long BATCH_SIZE = 100000L;
    
    private PatchDAO patchDAO;
    private NodeDAO nodeDAO;
    private QNameDAO qnameDAO;
    private RetryingTransactionHelper retryingTransactionHelper;
    
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
            logger.debug("RM Module RMv2ModelPatch ...");
        }
        
        updateQName(QName.createQName(DOD_URI, "filePlan"), TYPE_FILE_PLAN, "TYPE");
        updateQName(QName.createQName(DOD_URI, "recordCategory"), TYPE_RECORD_CATEGORY, "TYPE");
        updateQName(QName.createQName(DOD_URI, "ghosted"), ASPECT_GHOSTED, "ASPECT");
    }   
    
    private void updateQName(QName qnameBefore, QName qnameAfter, String reindexClass) 
    {
        Long maxNodeId = patchDAO.getMaxAdmNodeID();
        
        Pair<Long, QName> before = qnameDAO.getQName(qnameBefore);

        if (before != null)
        {
            for (Long i = 0L; i < maxNodeId; i+=BATCH_SIZE)
            {
                Work work = new Work(before.getFirst(), i, reindexClass);
                retryingTransactionHelper.doInTransaction(work, false, true);
            }
            
            qnameDAO.updateQName(qnameBefore, qnameAfter);
            
            if (logger.isDebugEnabled() == true)
            {
                logger.debug(" ... updated qname " + qnameBefore.toString());
            }
        }
        else
        {
            if (logger.isDebugEnabled() == true)
            {
                logger.debug(" ... no need to update qname " + qnameBefore.toString());
            }
        }
    }
    
    private class Work implements RetryingTransactionHelper.RetryingTransactionCallback<Integer>
    {
        private long qnameId;        
        private long lower;
        private String reindexClass;

        Work(long qnameId, long lower, String reindexClass)
        {
            this.qnameId = qnameId;
            this.lower = lower;
            this.reindexClass = reindexClass;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback#execute()
         */
        @Override
        public Integer execute() throws Throwable
        {
            if ("TYPE".equals(reindexClass))
            {
                List<Long> nodeIds = patchDAO.getNodesByTypeQNameId(qnameId, lower, lower + BATCH_SIZE);
                nodeDAO.touchNodes(nodeDAO.getCurrentTransactionId(true), nodeIds);
                return nodeIds.size();
            }
            else if ("ASPECT".equals(reindexClass))
            {
                List<Long> nodeIds = patchDAO.getNodesByAspectQNameId(qnameId, lower, lower + BATCH_SIZE);
                nodeDAO.touchNodes(nodeDAO.getCurrentTransactionId(true), nodeIds);
                return nodeIds.size();
            }
            else
            {
                // nothing to do
                return 0;
            }
           
        }
    }
}
