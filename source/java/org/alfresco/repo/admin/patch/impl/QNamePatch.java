/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * A patch to update the value of a QName.
 * This patch will only succeed if the target QName has not been used i.e. if there is no content
 * that actually references the QName.
 * <P/>
 * A property 'reindexClass' can be optionally injected. If it is not injected then the QName is
 * updated and no reindexing is requested by this patch.
 * If it is set to either 'TYPE' or 'ASPECT' (as appropriate) then that String will be used to
 * locate out-of-date references to the old QName and have them reindexed in a targetted way.
 * <P/>
 * Please refer to the implementation in this class for the details of how this is achieved.
 * 
 * @author Neil McErlean
 */
public class QNamePatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.QNamePatch.result";

    /* Injected properties */
    private String qnameStringBefore;
    private String qnameStringAfter;
    private String reindexClass;
    
    private QNameDAO qnameDAO;

    private PatchDAO patchDAO;

    private NodeDAO nodeDAO;

    private RetryingTransactionHelper retryingTransactionHelper;

    private static long BATCH_SIZE = 100000L;
    
    

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

    /**
     * Sets the QName to be patched.
     * @param qnameStringBefore the long-form QName to be patched from. {namespaceURI}localName
     */
    public void setQnameBefore(String qnameStringBefore)
    {
        this.qnameStringBefore = qnameStringBefore;
    }

    /**
     * Sets the new QName value to be used.
     * @param qnameStringAfter the long-form QName to be patched to. {namespaceURI}localName
     */
    public void setQnameAfter(String qnameStringAfter)
    {
        this.qnameStringAfter = qnameStringAfter;
    }
    
    /**
     * Sets a value for the class to reindex. This will be used in the Lucene query below and
     * should be either "TYPE" or "ASPECT" or not set if reindexing is not required.
     * @param reindexClass "TYPE" or "ASPECT" or not set.
     */
    public void setReindexClass(String reindexClass)
    {
        this.reindexClass = reindexClass;
    }
    
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(patchDAO, "patchDAO");
        checkPropertyNotNull(qnameDAO, "qnameDAO");
        checkPropertyNotNull(nodeDAO, "nodeDAO");
        checkPropertyNotNull(retryingTransactionHelper, "retryingTransactionHelper");
        checkPropertyNotNull(qnameStringAfter, "qnameStringAfter");
        checkPropertyNotNull(qnameStringBefore, "qnameStringBefore");
    }
    
    @Override
    protected String applyInternal() throws Exception
    {
        // We don't need to catch the potential InvalidQNameException here as it will be caught
        // in AbstractPatch and correctly handled there
        QName qnameBefore = QName.createQName(this.qnameStringBefore);
        QName qnameAfter = QName.createQName(this.qnameStringAfter);

        Long maxNodeId = patchDAO.getMaxAdmNodeID();
        
        Pair<Long, QName> before = qnameDAO.getQName(qnameBefore);
        
        if (before != null)
        {
            for (Long i = 0L; i < maxNodeId; i+=BATCH_SIZE)
            {
                Work work = new Work(before.getFirst(), i);
                retryingTransactionHelper.doInTransaction(work, false, true);
            }
            qnameDAO.updateQName(qnameBefore, qnameAfter);
        }
    
        return I18NUtil.getMessage(MSG_SUCCESS, qnameBefore, qnameAfter);
    }
    
    private class Work implements RetryingTransactionHelper.RetryingTransactionCallback<Integer>
    {
        long qnameId;
        
        long lower;

        Work(long qnameId, long lower)
        {
            this.qnameId = qnameId;
            this.lower = lower;
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
