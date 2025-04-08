/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.admin.patch.impl;

import java.util.List;
import java.util.Map;

import org.springframework.extensions.surf.util.I18NUtil;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.domain.mimetype.MimetypeDAO;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.util.Pair;

/**
 * A patch to update the value of a Mimetype.
 * <p>
 * This patch will only work fully if the content URL data has been fully normalized. It supports renaming to existing, currently-used mimetypes as well as to mimetypes that have not been used before.
 * 
 * @author Derek Hulley
 * @since 3.3 SP1
 */
public class GenericMimetypeRenamePatch extends AbstractPatch
{
    private static final String MSG_START = "patch.genericMimetypeUpdate.start";
    private static final String MSG_UPDATED = "patch.genericMimetypeUpdate.updated";
    private static final String MSG_INDEXED = "patch.genericMimetypeUpdate.indexed";
    private static final String MSG_DONE = "patch.genericMimetypeUpdate.done";
    private static final String MSG_DONE_REINDEX = "patch.genericMimetypeUpdate.doneReindex";

    /* Helper DAOs */

    private MimetypeDAO mimetypeDAO;

    private PatchDAO patchDAO;

    private NodeDAO nodeDAO;

    private RetryingTransactionHelper retryingTransactionHelper;

    private static long BATCH_SIZE = 100000L;

    /** Mimetype mappings */
    private Map<String, String> mimetypeMappings;
    private boolean reindex;

    public void setMimetypeDAO(MimetypeDAO mimetypeDAO)
    {
        this.mimetypeDAO = mimetypeDAO;
    }

    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    public void setMimetypeMappings(Map<String, String> mimetypeMappings)
    {
        this.mimetypeMappings = mimetypeMappings;
    }

    public void setReindex(boolean reindex)
    {
        this.reindex = reindex;
    }

    /**
     * @param nodeDAO
     *            the nodeDAO to set
     */
    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    /**
     * @param retryingTransactionHelper
     *            the retryingTransactionHelper to set
     */
    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(mimetypeDAO, "mimetypeDAO");
        checkPropertyNotNull(patchDAO, "patchDAO");
        checkPropertyNotNull(mimetypeMappings, "mimetypeMappings");
        checkPropertyNotNull(nodeDAO, "nodeDAO");
        checkPropertyNotNull(retryingTransactionHelper, "retryingTransactionHelper");
    }

    @Override
    protected String applyInternal() throws Exception
    {
        StringBuilder result = new StringBuilder(I18NUtil.getMessage(MSG_START));

        Long maxNodeId = patchDAO.getMaxAdmNodeID();

        for (Map.Entry<String, String> element : mimetypeMappings.entrySet())
        {
            String oldMimetype = element.getKey();
            String newMimetype = element.getValue();

            // First check if the mimetype is used at all
            Pair<Long, String> oldMimetypePair = mimetypeDAO.getMimetype(oldMimetype);
            if (oldMimetypePair == null)
            {
                // Not used
                continue;
            }

            // pull all affectsed nodes into a new transaction id indexed

            if (reindex)
            {
                long count = 0L;
                for (Long i = 0L; i < maxNodeId; i += BATCH_SIZE)
                {
                    Work work = new Work(oldMimetypePair.getFirst(), i);
                    count += retryingTransactionHelper.doInTransaction(work, false, true);
                }
                result.append(I18NUtil.getMessage(MSG_INDEXED, count, "(All stores)"));
            }

            // Check if the new mimetype exists
            Pair<Long, String> newMimetypePair = mimetypeDAO.getMimetype(newMimetype);
            int updateCount = 0;
            if (newMimetypePair == null)
            {
                // Easy, just rename the old one
                updateCount = mimetypeDAO.updateMimetype(oldMimetype, newMimetype);
            }
            else
            {
                // We need to move all the old references to the new ones
                Long oldMimetypeId = oldMimetypePair.getFirst();
                Long newMimetypeId = mimetypeDAO.getOrCreateMimetype(newMimetype).getFirst();
                updateCount = patchDAO.updateContentMimetypeIds(oldMimetypeId, newMimetypeId);
            }
            result.append(I18NUtil.getMessage(MSG_UPDATED, updateCount, oldMimetype, newMimetype));
        }
        // Done
        if (reindex)
        {
            result.append(I18NUtil.getMessage(MSG_DONE));
        }
        else
        {
            result.append(I18NUtil.getMessage(MSG_DONE_REINDEX));
        }

        return result.toString();
    }

    private class Work implements RetryingTransactionHelper.RetryingTransactionCallback<Integer>
    {
        long mimetypeId;

        long lower;

        Work(long mimetypeId, long lower)
        {
            this.mimetypeId = mimetypeId;
            this.lower = lower;
        }

        /* (non-Javadoc)
         * 
         * @see org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback#execute() */
        @Override
        public Integer execute() throws Throwable
        {
            List<Long> nodeIds = patchDAO.getNodesByContentPropertyMimetypeId(mimetypeId, lower, lower + BATCH_SIZE);
            nodeDAO.touchNodes(nodeDAO.getCurrentTransactionId(true), nodeIds);
            return nodeIds.size();
        }
    }
}
