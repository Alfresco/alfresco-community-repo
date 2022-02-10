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

package org.alfresco.module.org_alfresco_module_rm.patch.v22;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * DOD model separation module patch implementation
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class RMv22DODModelSeparationModulePatch extends AbstractModulePatch
                                           implements RecordsManagementModel
{
    /** query batch size */
    private static final long BATCH_SIZE = 100000L;

    /** indicates whether we convert to a standard file plan or not */
    private boolean convertToStandardFilePlan = false;

    /** Patch DAO */
    private PatchDAO patchDAO;

    /** Node DAO */
    private NodeDAO nodeDAO;

    /** qnames to update (switch to dod namespace) */
    private QName[] qnames =
    {
        DOD5015Model.PROP_ORIGINATOR,
        DOD5015Model.PROP_ORIGINATING_ORGANIZATION,
        DOD5015Model.PROP_PUBLICATION_DATE,
        DOD5015Model.PROP_MEDIA_TYPE,
        DOD5015Model.PROP_FORMAT,
        DOD5015Model.PROP_DATE_RECEIVED,
        DOD5015Model.PROP_ADDRESS,
        DOD5015Model.PROP_OTHER_ADDRESS
    };

    /**
     * @param convertToStandardFilePlan	convert to standard file if true, false otherwise
     */
    public void setConvertToStandardFilePlan(boolean convertToStandardFilePlan)
    {
		this.convertToStandardFilePlan = convertToStandardFilePlan;
	}

    /**
     * @param patchDAO  patch DAO
     */
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    /**
     * @param nodeDAO   node DAO
     */
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch#applyInternal()
     */
    @Override
    public void applyInternal()
    {
    	if (!convertToStandardFilePlan)
    	{
	        Long maxNodeId = nodeDAO.getMaxNodeId();
	        long recordCount = patchDAO.getCountNodesWithAspects(Collections.singleton(ASPECT_RECORD));
	        if (LOGGER.isDebugEnabled())
	        {
	            LOGGER.debug("   ... updating " + recordCount + " records in batches of " + BATCH_SIZE);
	        }

	        // apply the DOD record aspect to all exiting records
	        int completed = 0;
	        for (Long i = 0L; i < maxNodeId; i+=BATCH_SIZE)
	        {
	        	final Long finali = i;
	        	Integer batchCount = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Integer>()
	            {
	        		int batchCount = 0;

	        		public Integer execute() throws Throwable
					{
						nodeDAO.getNodesWithAspects(Collections.singleton(ASPECT_RECORD), finali, finali + BATCH_SIZE, new NodeDAO.NodeRefQueryCallback()
				        {
				            public boolean handle(Pair<Long, NodeRef> nodePair)
				            {
				            	 // get the records properties
				            	 Map<QName, Serializable> properties = nodeDAO.getNodeProperties(nodePair.getFirst());
				            	 boolean changed = false;

				            	 for (QName qname : qnames)
				            	 {
				            	     // if the record has any of the moved properties
				            	     QName origional = QName.createQName(RecordsManagementModel.RM_URI, qname.getLocalName());
				            	     if (properties.containsKey(origional))
				            	     {
				            	         // move the property value
				            	         Serializable value = properties.get(origional);
				            	         properties.put(qname, value);
				            	         properties.remove(origional);
				            	         changed = true;
				            	     }
				            	 }

				            	 // set properties and add aspect
				            	 if (changed)
				            	 {
				            		 nodeDAO.setNodeProperties(nodePair.getFirst(), properties);
				            	 }
				            	 nodeDAO.addNodeAspects(nodePair.getFirst(), Collections.singleton(DOD5015Model.ASPECT_DOD_5015_RECORD));
				            	 batchCount ++;

				            	 return true;
				            }
				        });

						return batchCount;
					}
	            } , false, true);

	        	if (batchCount != 0)
	        	{
		        	completed = completed + batchCount;
		            if (LOGGER.isDebugEnabled())
		            {
		                LOGGER.debug("   ... completed " + completed + " of " + recordCount);
		            }
	        	}
	        }
    	}
    }
}
