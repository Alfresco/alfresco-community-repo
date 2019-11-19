/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.patch.v24;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_DISPOSITION_LIFECYCLE;
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_DISPOSITION_PROCESSED;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.module.org_alfresco_module_rm.query.RecordsManagementQueryDAO;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 */
public class RMv24DispositionInheritancePatch extends AbstractModulePatch
{
    private static final Log logger = LogFactory.getLog(RMv24DispositionInheritancePatch.class);

    private static final long BATCH_SIZE = 1000L;

    private DispositionService dispositionService;

    private RecordService recordService;

    private NodeService nodeService;

    private RecordsManagementQueryDAO recordsManagementQueryDAO;

    private BehaviourFilter behaviourFilter;

    protected QNameDAO qnameDAO;

    private NodeDAO nodeDAO;

    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    public void setRecordsManagementQueryDAO(RecordsManagementQueryDAO recordsManagementQueryDAO)
    {
        this.recordsManagementQueryDAO = recordsManagementQueryDAO;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    /**
     * @see AbstractModulePatch#applyInternal()
     * <p>
     * Checks for records added to a disposition schedule pre MNT-19967 and applys disposition step and properties
     */
    @Override
    public void applyInternal()
    {
        Long maxNodeId = nodeDAO.getMaxNodeId();
        int batchCount = 0;

        qnameDAO.getOrCreateQName(ASPECT_DISPOSITION_PROCESSED);
        
        for (Long i = 0L; i < maxNodeId; i += BATCH_SIZE)
        {
            int updatedRecords = 0;
            List<NodeRef> folders = recordsManagementQueryDAO.getRecordFoldersWithSchedules(i, i + BATCH_SIZE);
            for (NodeRef folder : folders)
            {
            	updatedRecords = updatedRecords + updateRecordFolder(folder);
            }

            batchCount ++;
            logger.info("Records updated: "+ updatedRecords);
            logger.info("Completed batch "+ batchCount+" of "+ (Math.ceil(maxNodeId/BATCH_SIZE)+1));
        }
    }
    
    private int updateRecordFolder(final NodeRef recordFolder) 
    {
    	return transactionService.getRetryingTransactionHelper()
	    	.doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Integer>()
		{
	        public Integer execute() throws Throwable
	        {   
	        	int recordCount = 0;
	        	
	            behaviourFilter.disableBehaviour(recordFolder);
	            if (LOGGER.isDebugEnabled())
	            {
	                logger.info("Checking folder: " + recordFolder);
	            }
	            DispositionSchedule schedule = dispositionService.getDispositionSchedule(recordFolder);
	            if (schedule.isRecordLevelDisposition())
	            {
	                List<NodeRef> records = recordService.getRecords(recordFolder);
	                for (NodeRef record : records)
	                {
	                    if (!nodeService.hasAspect(record, ASPECT_DISPOSITION_LIFECYCLE))
	                    {
	                        if (LOGGER.isDebugEnabled())
	                        {
	                            logger.info("updating record: " + record);
	                        }
	                        behaviourFilter.disableBehaviour(record);
	                        dispositionService.updateNextDispositionAction(record, schedule);
	                        recordCount ++;
	                        behaviourFilter.enableBehaviour(record);
	                    }
	                }
	            }
	            nodeService.addAspect(recordFolder, ASPECT_DISPOSITION_PROCESSED, null);
	            behaviourFilter.enableBehaviour(recordFolder);
	            
	            return recordCount;
	        }
		}, false, true);
    }
}

