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

package org.alfresco.module.org_alfresco_module_rm.freeze;

import static org.alfresco.model.ContentModel.TYPE_FOLDER;
import static org.alfresco.repo.site.SiteModel.ASPECT_SITE_CONTAINER;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Freeze Service Implementation
 *
 * @author Roy Wetherall
 * @author Tuna Aksoy
 * @since 2.1
 */
public class FreezeServiceImpl extends    ServiceBaseImpl
                               implements FreezeService,
                                          RecordsManagementModel
{
    /** I18N */
    private static final String MSG_HOLD_NAME = "rm.hold.name";

    /** File Plan Service */
    private FilePlanService filePlanService;

    /** Hold service */
    private HoldService holdService;

    /**
     * Record Folder Service
     */
    private RecordFolderService recordFolderService;

    /**
     * Record Service
     */
    private RecordService recordService;

    /**
     * @param recordFolderService record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }

    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @return File plan service
     */
    protected FilePlanService getFilePlanService()
    {
        return this.filePlanService;
    }

    /**
     * @return Hold service
     */
    protected HoldService getHoldService()
    {
        return this.holdService;
    }

    /**
     * @param filePlanService file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param holdService hold service
     */
    public void setHoldService(HoldService holdService)
    {
        this.holdService = holdService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#isFrozen(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean isFrozen(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        return nodeService.hasAspect(nodeRef, ASPECT_FROZEN);
    }

    /**
     * Deprecated Method Implementations
     */

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#getFrozen(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public Set<NodeRef> getFrozen(NodeRef hold)
    {
        return new HashSet<>(getHoldService().getHeld(hold));
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#freeze(java.lang.String,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public NodeRef freeze(String reason, NodeRef nodeRef)
    {
        NodeRef hold = createHold(nodeRef, reason);
        getHoldService().addToHold(hold, nodeRef);
        return hold;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#freeze(org.alfresco.service.cmr.repository.NodeRef,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public void freeze(NodeRef hold, NodeRef nodeRef)
    {
        ParameterCheck.mandatory("hold", hold);
        ParameterCheck.mandatory("nodeRef", nodeRef);

        getHoldService().addToHold(hold, nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#freeze(java.lang.String,
     *      java.util.Set)
     */
    @Override
    @Deprecated
    public NodeRef freeze(String reason, Set<NodeRef> nodeRefs)
    {
        NodeRef hold = null;
        if (!nodeRefs.isEmpty())
        {
            final List<NodeRef> list = new ArrayList<>(nodeRefs);
            hold = createHold(list.get(0), reason);
            getHoldService().addToHold(hold, list);
        }
        return hold;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#freeze(org.alfresco.service.cmr.repository.NodeRef,
     *      java.util.Set)
     */
    @Override
    @Deprecated
    public void freeze(NodeRef hold, Set<NodeRef> nodeRefs)
    {
        ParameterCheck.mandatory("hold", hold);
        ParameterCheck.mandatoryCollection("nodeRefs", nodeRefs);

        for (NodeRef nodeRef : nodeRefs)
        {
            freeze(hold, nodeRef);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#unFreeze(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public void unFreeze(NodeRef nodeRef)
    {
        List<NodeRef> holds = getHoldService().heldBy(nodeRef, true);
        for (NodeRef hold : holds)
        {
            getHoldService().removeFromHold(hold, nodeRef);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#unFreeze(java.util.Set)
     */
    @Override
    @Deprecated
    public void unFreeze(Set<NodeRef> nodeRefs)
    {
        ParameterCheck.mandatoryCollection("nodeRefs", nodeRefs);

        for (NodeRef nodeRef : nodeRefs)
        {
            unFreeze(nodeRef);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#relinquish(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public void relinquish(NodeRef hold)
    {
        getHoldService().deleteHold(hold);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#getReason(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Deprecated
    public String getReason(NodeRef hold)
    {
        return getHoldService().getHoldReason(hold);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#updateReason(org.alfresco.service.cmr.repository.NodeRef,
     *      java.lang.String)
     */
    @Override
    @Deprecated
    public void updateReason(NodeRef hold, String reason)
    {
        getHoldService().setHoldReason(hold, reason);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.hold.HoldService#getHolds(NodeRef)
     */
    @Override
    public Set<NodeRef> getHolds(NodeRef filePlan)
    {
        ParameterCheck.mandatory("filePlan", filePlan);

        return new HashSet<>(getHoldService().getHolds(filePlan));
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#hasFrozenChildren(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean hasFrozenChildren(final NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        boolean result = false;

        // check that we are dealing with a record folder or a collaboration folder
        if (isRecordFolder(nodeRef) ||
                (instanceOf(nodeRef, TYPE_FOLDER) && !nodeService.hasAspect(nodeRef, ASPECT_SITE_CONTAINER)))
        {
            int heldCount = 0;

            if (nodeService.hasAspect(nodeRef, ASPECT_HELD_CHILDREN))
            {
                heldCount = (Integer)getInternalNodeService().getProperty(nodeRef, PROP_HELD_CHILDREN_COUNT);
            }
            else
            {
                final TransactionService transactionService = (TransactionService)applicationContext.getBean("transactionService");

                heldCount = AuthenticationUtil.runAsSystem(new RunAsWork<Integer>()
                {
                    @Override
                    public Integer doWork()
                    {
                        return transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Integer>()
                        {
                            public Integer execute() throws Throwable
                            {
                                int heldCount = 0;

                                // NOTE: this process remains to 'patch' older systems to improve performance next time around
                                List<ChildAssociationRef> childAssocs = getInternalNodeService().getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, null);
                                if (childAssocs != null && !childAssocs.isEmpty())
                                {
                                    for (ChildAssociationRef childAssociationRef : childAssocs)
                                    {
                                        final NodeRef childRef = childAssociationRef.getChildRef();
                                        if (childAssociationRef.isPrimary() && isFrozen(childRef))
                                        {
                                            heldCount ++;
                                        }
                                    }
                                }

                                // add aspect and set count
                                Map<QName, Serializable> props = new HashMap<>(1);
                                props.put(PROP_HELD_CHILDREN_COUNT, heldCount);
                                getInternalNodeService().addAspect(nodeRef, ASPECT_HELD_CHILDREN, props);

                                return heldCount;
                            }
                        },
                        false, true);
                    }
                });
            }

            // true if more than one child held
            result = (heldCount > 0);
        }

        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#getFreezeDate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public Date getFreezeDate(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        if (isFrozen(nodeRef))
        {
            Serializable property = nodeService.getProperty(nodeRef, PROP_FROZEN_AT);
            if (property != null) { return (Date) property; }
        }

        return null;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService#getFreezeInitiator(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public String getFreezeInitiator(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        if (isFrozen(nodeRef))
        {
            Serializable property = nodeService.getProperty(nodeRef, PROP_FROZEN_BY);
            if (property != null) { return (String) property; }
        }

        return null;
    }

    /**
     * Helper Methods
     */

    /**
     * Creates a hold using the given nodeRef and reason
     *
     * @param nodeRef the nodeRef which will be frozen
     * @param reason the reason why the record will be frozen
     * @return NodeRef of the created hold
     */
    private NodeRef createHold(NodeRef nodeRef, String reason)
    {
        // get the hold container
        final NodeRef filePlan = getFilePlanService().getFilePlan(nodeRef);
        NodeRef holdContainer = getFilePlanService().getHoldContainer(filePlan);

        // calculate the hold name
        int nextCount = getNextCount(holdContainer);
        String holdName = I18NUtil.getMessage(MSG_HOLD_NAME) + " " + StringUtils.leftPad(Integer.toString(nextCount), 10, "0");

        // create hold
        return getHoldService().createHold(filePlan, holdName, reason, null);
    }

    /**
     * Helper method to determine if a node is frozen or has frozen children
     *
     * @param nodeRef Node to be checked
     * @return <code>true</code> if the node is frozen or has frozen children, <code>false</code> otherwise
     */
    @Override
    public boolean isFrozenOrHasFrozenChildren(NodeRef nodeRef)
    {
        if (recordFolderService.isRecordFolder(nodeRef))
        {
            return isFrozen(nodeRef) || hasFrozenChildren(nodeRef);
        }
        else if (recordService.isRecord(nodeRef))
        {
            return isFrozen(nodeRef);
        }
        return Boolean.FALSE;
    }
}
