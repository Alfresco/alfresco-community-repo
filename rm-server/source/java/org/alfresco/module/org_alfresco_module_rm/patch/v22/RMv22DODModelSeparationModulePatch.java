/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.patch.v22;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
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

    /** QName DAO */
    private QNameDAO qnameDAO;

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
        DOD5015Model.PROP_DATE_RECEIVED
    };

    /**
     * @param qnameDAO  QName DAO
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
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
        Long maxNodeId = patchDAO.getMaxAdmNodeID();
        long recordCount = patchDAO.getCountNodesWithAspects(Collections.singleton(ASPECT_RECORD));
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(" ... updating " + recordCount + " records");
        }

        // apply the DOD record aspect to all exiting records
        int completed = 0;
        Pair<Long, QName> recordAspect = qnameDAO.getQName(ASPECT_RECORD);
        if (recordAspect != null)
        {
            for (Long i = 0L; i < maxNodeId; i+=BATCH_SIZE)
            {
                List<Long> nodeIds = patchDAO.getNodesByAspectQNameId(recordAspect.getFirst(), i, i + BATCH_SIZE);
                for (Long nodeId : nodeIds)
                {
                    // get the records properties
                    Map<QName, Serializable> properties = nodeDAO.getNodeProperties(nodeId);
                    
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
                        }
                    }
                    
                    // set properties and add aspect
                    nodeDAO.setNodeProperties(nodeId, properties);
                    nodeDAO.addNodeAspects(nodeId, Collections.singleton(DOD5015Model.ASPECT_DOD_5015_RECORD));
                }

                completed += completed + nodeIds.size();
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("    ... completed " + completed + " of " + recordCount);
                }
            }
        }
    }
}
