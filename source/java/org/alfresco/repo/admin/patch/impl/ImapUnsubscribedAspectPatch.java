/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeDAO.NodeRefQueryCallback;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.I18NUtil;

public class ImapUnsubscribedAspectPatch extends AbstractPatch
{
    private static final String MSG_NONSUBSCRIBED_ASPECT_REMOVED = "patch.imapUnsubscribedAspect.result.removed";
    private static final QName ASPECT_NON_SUBSCRIBED = QName.createQName("{http://www.alfresco.org/model/imap/1.0}nonSubscribed");

    private NodeDAO nodeDAO;
    private PatchDAO patchDAO;
    private PersonService personService;

    private final int batchThreads = 3;
    private final int batchSize = 40;
    private final long count = batchThreads * batchSize;
    private long minSearchNodeId = 1;

    @Override
    protected String applyInternal() throws Exception
    {
        final List<ChildAssociationRef> users = nodeService.getChildAssocs(personService.getPeopleContainer(), ContentModel.ASSOC_CHILDREN, RegexQNamePattern.MATCH_ALL);
        final long maxNodeId = patchDAO.getMaxAdmNodeID();

        BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>()
        {
            final List<NodeRef> result = new ArrayList<NodeRef>();

            public int getTotalEstimatedWorkSize()
            {
                return result.size();
            }

            public Collection<NodeRef> getNextWork()
            {
                result.clear();
                while (result.isEmpty() && minSearchNodeId < maxNodeId)
                {
                    nodeDAO.getNodesWithAspects(Collections.singleton(ASPECT_NON_SUBSCRIBED), minSearchNodeId,
                            minSearchNodeId + count, new NodeRefQueryCallback()
                            {

                                public boolean handle(Pair<Long, NodeRef> nodePair)
                                {
                                    result.add(nodePair.getSecond());
                                    return true;
                                }

                            });
                    minSearchNodeId = minSearchNodeId + count + 1;
                }

                return result;
            }
        };

        BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<NodeRef>("ImapUnsubscribedAspectPatch", transactionService.getRetryingTransactionHelper(), workProvider,
                batchThreads, batchSize, applicationEventPublisher, null, 1000);

        BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<NodeRef>()
        {

            public void afterProcess() throws Throwable
            {
            }

            public void beforeProcess() throws Throwable
            {
            }

            public String getIdentifier(NodeRef entry)
            {
                return entry.toString();
            }

            public void process(NodeRef entry) throws Throwable
            {
                nodeService.removeAspect(entry, ImapModel.ASPECT_IMAP_FOLDER_NONSUBSCRIBED);

                for (ChildAssociationRef userRef : users)
                {
                    nodeService.createAssociation(userRef.getChildRef(), entry, ImapModel.ASSOC_IMAP_UNSUBSCRIBED);
                }

            }

        };
        batchProcessor.process(worker, true);

        return I18NUtil.getMessage(MSG_NONSUBSCRIBED_ASPECT_REMOVED);

    }

    public void setNodeDAO(NodeDAO nodeDAO)
    {
        this.nodeDAO = nodeDAO;
    }

    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

}
