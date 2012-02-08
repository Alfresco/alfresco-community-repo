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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ImapModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.node.NodeDAO.NodeRefQueryCallback;
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
    private static final String PROP_MIN_ID = "minNodeId";

    private NodeDAO nodeDAO;
    private PersonService personService;

    private final Map<String, Long> properties = new HashMap<String, Long>();

    private int batchThreads = 3;
    private int batchSize = 40;
    private long count = batchThreads * batchSize;

    @Override
    public void init()
    {
        super.init();
        properties.put(PROP_MIN_ID, 1L);
    }
    @Override
    protected String applyInternal() throws Exception
    {
        final List<ChildAssociationRef> users = nodeService.getChildAssocs(personService.getPeopleContainer(), ContentModel.ASSOC_CHILDREN, RegexQNamePattern.MATCH_ALL);

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
                nodeDAO.getNodesWithAspects(Collections.singleton(ASPECT_NON_SUBSCRIBED), properties.get(PROP_MIN_ID), count, new NodeRefQueryCallback()
                {

                    public boolean handle(Pair<Long, NodeRef> nodePair)
                    {
                        properties.put(PROP_MIN_ID, nodePair.getFirst());
                        result.add(nodePair.getSecond());
                        return true;
                    }

                });

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

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

}
