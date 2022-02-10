/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.patch.v20;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.patch.compatibility.ModulePatchComponent;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.springframework.beans.factory.BeanNameAware;

/**
 * RM v2.0 Model Updates Patch
 *
 * @author Roy Wetherall
 * @since 2.0
 */
@SuppressWarnings("deprecation")
public class RMv2ModelPatch extends ModulePatchComponent
                            implements BeanNameAware, RecordsManagementModel, DOD5015Model
{
    private static final long BATCH_SIZE = 100000L;

    private PatchDAO patchDAO;
    private NodeDAO nodeDAO;
    private QNameDAO qnameDAO;

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
    protected void executePatch()
    {
        updateQName(QName.createQName(DOD_URI, "filePlan"), TYPE_FILE_PLAN, "TYPE");
        updateQName(QName.createQName(DOD_URI, "recordCategory"), TYPE_RECORD_CATEGORY, "TYPE");
        updateQName(QName.createQName(DOD_URI, "ghosted"), ASPECT_GHOSTED, "ASPECT");
    }

    private void updateQName(QName qnameBefore, QName qnameAfter, String reindexClass)
    {
        Work work = new Work(qnameBefore, qnameAfter, reindexClass);
        retryingTransactionHelper.doInTransaction(work, false, true);
    }

    private class Work implements RetryingTransactionHelper.RetryingTransactionCallback<Integer>
    {
        private QName qnameBefore;
        private QName qnameAfter;
        private String reindexClass;

        /**
         * Constructor
         *
         * @param qnameBefore   qname before
         * @param qnameAfter    qname after
         * @param reindexClass  reindex class
         */
        Work(QName qnameBefore, QName qnameAfter, String reindexClass)
        {
            this.qnameBefore = qnameBefore;
            this.qnameAfter  = qnameAfter;
            this.reindexClass = reindexClass;
        }

        /**
         * @see org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback#execute()
         */
        @Override
        public Integer execute() throws Throwable
        {
            Long maxNodeId = patchDAO.getMaxAdmNodeID();

            Pair<Long, QName> before = qnameDAO.getQName(qnameBefore);

            if (before != null)
            {
            	for (Long i = 0L; i < maxNodeId; i+=BATCH_SIZE)
            	{
            		if ("TYPE".equals(reindexClass))
            		{
            			List<Long> nodeIds = patchDAO.getNodesByTypeQNameId(before.getFirst(), i, i + BATCH_SIZE);
            			nodeDAO.touchNodes(nodeDAO.getCurrentTransactionId(true), nodeIds);
            		}
            		else if ("ASPECT".equals(reindexClass))
            		{
            			List<Long> nodeIds = patchDAO.getNodesByAspectQNameId(before.getFirst(), i, i + BATCH_SIZE);
            			nodeDAO.touchNodes(nodeDAO.getCurrentTransactionId(true), nodeIds);
            		}
            	}

            	qnameDAO.updateQName(qnameBefore, qnameAfter);

            	if (LOGGER.isDebugEnabled())
            	{
            		LOGGER.debug(" ... updated qname " + qnameBefore.toString());
            	}
        	}
            else
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug(" ... no need to update qname " + qnameBefore.toString());
                }
            }

        	//nothing to do
        	return 0;
        }
    }
}
