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

package org.alfresco.module.org_alfresco_module_rm.patch.v21;

import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanPermissionServiceImpl;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.springframework.beans.factory.BeanNameAware;

/**
 * RM v2.1 patch to change the record inheritance of permissions.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@SuppressWarnings("deprecation")
public class RMv21RecordInheritancePatch extends RMv21PatchComponent
                                         implements BeanNameAware, RecordsManagementModel, DOD5015Model
{
    /** file plan permission service */
    private FilePlanPermissionServiceImpl filePlanPermissionServiceImpl;

    /** node service */
    private NodeService nodeService;

    /** patch DAO */
    private PatchDAO patchDAO;

    /** qname DAO */
    private QNameDAO qnameDAO;

    /** node DAO */
    private NodeDAO nodeDAO;

    /**
     * @param patchDAO  patch DAO
     */
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    /**
     * @param qnameDAO  qname DAO
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    /**
     * @param nodeDAO   node DAO
     */
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    /**
     * @param filePlanPermissionServiceImpl file plan permission service implementation
     */
    public void setFilePlanPermissionServiceImpl(FilePlanPermissionServiceImpl filePlanPermissionServiceImpl)
    {
        this.filePlanPermissionServiceImpl = filePlanPermissionServiceImpl;
    }

    /**
     * @param nodeService node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @see org.alfresco.repo.module.AbstractModuleComponent#executeInternal()
     */
    @Override
    protected void executePatch()
    {
        Pair<Long, QName> aspectPair = qnameDAO.getQName(ASPECT_RECORD);
        if (aspectPair != null)
        {
            List<Long> records = patchDAO.getNodesByAspectQNameId(aspectPair.getFirst(), 0L, patchDAO.getMaxAdmNodeID());

            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("  ... updating " + records.size() + " records");
            }

            for (Long record : records)
            {
                Pair<Long, NodeRef> recordPair = nodeDAO.getNodePair(record);
                NodeRef recordNodeRef = recordPair.getSecond();

                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("  ... updating record " + recordNodeRef.toString());

                    // get the primary parent
                    ChildAssociationRef assoc = nodeService.getPrimaryParent(recordNodeRef);
                    NodeRef parent = assoc.getParentRef();
                    if (parent != null)
                    {
                        filePlanPermissionServiceImpl.setupPermissions(parent, recordNodeRef);
                    }
                }
            }
        }
    }
}
