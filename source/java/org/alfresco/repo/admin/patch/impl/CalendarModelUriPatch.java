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
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.search.Indexer;
import org.alfresco.repo.search.IndexerAndSearcher;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryParser;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Patch usr:user and cm:person objects so that the user name properties are in the index in untokenized form. If not
 * authentication may fail in mixed language use.
 * 
 * @author andyh
 */
public class CalendarModelUriPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.calendarModelNamespacePatch.result";

    private static final String URI_BEFORE = "com.infoaxon.alfresco.calendar";

    private static final String URI_AFTER = "http://www.alfresco.org/model/calendar";

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

    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(patchDAO, "patchDAO");
        checkPropertyNotNull(qnameDAO, "qnameDAO");
        checkPropertyNotNull(nodeDAO, "nodeDAO");
        checkPropertyNotNull(retryingTransactionHelper, "retryingTransactionHelper");
    }

    @Override
    protected String applyInternal() throws Exception
    {
        Long maxNodeId = patchDAO.getMaxAdmNodeID();
        long count = 0L;

        // Make sure the old name spaces exists before we update it ...
        Pair<Long, String> before = qnameDAO.getOrCreateNamespace(URI_BEFORE);
        for (Long i = 0L; i < maxNodeId; i+=BATCH_SIZE)
        {
            Work work = new Work(before.getFirst(), i);
            count += retryingTransactionHelper.doInTransaction(work, false, true);
        }

        // modify namespace for all calendar entries
        qnameDAO.updateNamespace(URI_BEFORE, URI_AFTER);

        return I18NUtil.getMessage(MSG_SUCCESS, count);
    }

    private class Work implements RetryingTransactionHelper.RetryingTransactionCallback<Integer>
    {
        long nsId;
        
        long lower;

        Work(long nsId, long lower)
        {
            this.nsId = nsId;
            this.lower = lower;
        }

        /*
         * (non-Javadoc)
         * @see org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback#execute()
         */
        @Override
        public Integer execute() throws Throwable
        {
            List<Long> nodeIds = patchDAO.getNodesByTypeUriId(nsId, lower, lower + BATCH_SIZE);
            nodeDAO.touchNodes(nodeDAO.getCurrentTransactionId(true), nodeIds);
            return nodeIds.size();
        }
    }
}
