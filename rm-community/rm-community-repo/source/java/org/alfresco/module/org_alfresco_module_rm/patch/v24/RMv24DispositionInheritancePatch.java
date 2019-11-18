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
import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel.ASPECT_FROZEN;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.module.org_alfresco_module_rm.query.RecordsManagementQueryDAO;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * RM v2.4 patch that ensures that file plan root containers do not inherited rules, because this is no longer enforced
 * in the service code anymore.
 * <p>
 * See https://issues.alfresco.com/jira/browse/RM-3154
 *
 * @author Roy Wetherall
 * @since 2.4
 */
public class RMv24DispositionInheritancePatch extends AbstractModulePatch
{
    private static final Log logger = LogFactory.getLog(RMv24DispositionInheritancePatch.class);

    private static final long BATCH_SIZE = 5L;

    private DispositionService dispositionService;

    private RecordService recordService;

    private NodeService nodeService;

    private RecordsManagementQueryDAO recordsManagementQueryDAO;

    private BehaviourFilter behaviourFilter;

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

    /**
     * @see AbstractModulePatch#applyInternal()
     * <p>
     * Checks for records added to a disposition schedule pre MNT-19967 and applys disposition step and properties
     */
    @Override
    public void applyInternal()
    {
        int totalFolders = recordsManagementQueryDAO.getRecordFoldersWithSchedulesCount();
        logger.info("Folders to update: "+ totalFolders);

        for (Long i = 0L; i < totalFolders; i += BATCH_SIZE)
        {
            final Long finali = i;
            int updatedRecords = transactionService.getRetryingTransactionHelper()
                .doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Integer>()
                {
                    public Integer execute() throws Throwable
                    {
                        int recordCount = 0;
                        List<NodeRef> folders = recordsManagementQueryDAO.getRecordFoldersWithSchedules(finali, finali + BATCH_SIZE);
                        for (NodeRef folder : folders)
                        {
                            if (LOGGER.isDebugEnabled())
                            {
                                logger.info("Checking folder: " + folder);
                            }
                            behaviourFilter.disableBehaviour(folder);
                            DispositionSchedule schedule = dispositionService.getDispositionSchedule(folder);
                            if (schedule.isRecordLevelDisposition())
                            {
                                List<NodeRef> records = recordService.getRecords(folder);
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
                            behaviourFilter.enableBehaviour(folder);
                        }
                        return recordCount;
                    }
                }, false, true);

            logger.info("Records updated: "+ updatedRecords);
            logger.info("Updating folders: "+ finali + " to: " +(finali + BATCH_SIZE) + " of "+totalFolders);
        }
    }
}

