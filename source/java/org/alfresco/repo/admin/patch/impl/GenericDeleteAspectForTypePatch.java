/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * A patch to delete aspect for nodes of specific type.
 * 
 * @author Viachaslau Tsikhanovich
 * @since 4.2
 */
public class GenericDeleteAspectForTypePatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.GenericDeleteAspectForTypePatch.result";

    /* Injected properties */
    private String qnameStringType;
    private String qnameStringAspect;
    private QNameDAO qnameDAO;
    private PatchDAO patchDAO;
    private NodeDAO nodeDAO;
    private RetryingTransactionHelper retryingTransactionHelper;

    private static long BATCH_SIZE = 100000L;
    
    

    public void setQnameStringType(String qnameStringType)
    {
        this.qnameStringType = qnameStringType;
    }
    
    public void setQnameStringAspect(String qnameStringAspect)
    {
        this.qnameStringAspect = qnameStringAspect;
    }
    
    /**
     * @param qnameDAO the qnameDAO to set
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    /**
     * @param patchDAO the patchDAO to set
     */
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    /**
     * @param nodeDAO the nodeDAO to set
     */
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    /**
     * @param retryingTransactionHelper the retryingTransactionHelper to set
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(patchDAO, "patchDAO");
        checkPropertyNotNull(qnameDAO, "qnameDAO");
        checkPropertyNotNull(nodeDAO, "nodeDAO");
        checkPropertyNotNull(retryingTransactionHelper, "retryingTransactionHelper");
        checkPropertyNotNull(qnameStringType, "qnameStringType");
        checkPropertyNotNull(qnameStringAspect, "qnameStringAspect");
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        // We don't need to catch the potential InvalidQNameException here as it will be caught
        // in AbstractPatch and correctly handled there
        QName qnameType = QName.createQName(this.qnameStringType);
        QName qnameAspect = QName.createQName(this.qnameStringAspect);

        Long maxNodeId = patchDAO.getMaxAdmNodeID();
        
        Pair<Long, QName> type = qnameDAO.getQName(qnameType);
        Pair<Long, QName> aspect = qnameDAO.getQName(qnameAspect);
        
        if (type != null && aspect != null)
        {
            for (Long i = 0L; i < maxNodeId; i+=BATCH_SIZE)
            {
                Work work = new Work(type, aspect, i);
                retryingTransactionHelper.doInTransaction(work, false, true);
            }
        }
    
        return I18NUtil.getMessage(MSG_SUCCESS, qnameAspect, qnameType);
    }
    
    private class Work implements RetryingTransactionHelper.RetryingTransactionCallback<Integer>
    {
        Pair<Long, QName> type;
        Pair<Long, QName> aspect;
        long lower;

        Work(Pair<Long, QName> type, Pair<Long, QName> aspect, long lower)
        {
            this.type = type;
            this.aspect = aspect;
            this.lower = lower;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback#execute()
         */
        @Override
        public Integer execute() throws Throwable
        {
                List<Long> nodeIds = patchDAO.getNodesByTypeQNameAndAspectQNameId(type.getFirst(), aspect.getFirst(), lower, lower + BATCH_SIZE);
                for (Long nodeId : nodeIds)
                {
                    NodeRef nodeRef = nodeService.getNodeRef(nodeId);
                    // removes aspect with associated properties and touches the node to trigger reindex
                    nodeService.removeAspect(nodeRef, aspect.getSecond());

                }
                return nodeIds.size();
        }
    }

}
