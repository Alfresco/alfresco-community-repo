/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.repo.events;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;

public class EventGenerationBehavioursTest extends BaseSpringTest
{
    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private PolicyComponent policyComponent;

    @Autowired
    private RetryingTransactionHelper retryingTransactionHelper;

    private EventsService eventsService;

    @Before
    public void setUp()
    {
        eventsService = Mockito.mock(EventsService.class);
    }

    @Test
    public void shoulSendEventsWhenEnabled()
    {
        bindEventGenerationBehaviours(true);

        NodeRef nodeRef = createTestNode();

        Mockito.verify(eventsService, Mockito.times(1)).nodeCreated(nodeRef);
    }

    @Test
    public void shouldNotSendEventsWhenDisabled()
    {
        bindEventGenerationBehaviours(false);

        createTestNode();

        Mockito.verifyNoInteractions(eventsService);
    }

    private NodeRef createTestNode()
    {
        final var rootNode = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

        return retryingTransactionHelper.doInTransaction(() -> nodeService.createNode(rootNode, ContentModel.ASSOC_CHILDREN, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, UUID.randomUUID().toString()), ContentModel.TYPE_FOLDER)).getChildRef();
    }

    private void bindEventGenerationBehaviours(boolean enabled)
    {
        final var behaviours = new EventGenerationBehaviours();
        behaviours.setDictionaryService(dictionaryService);
        behaviours.setNodeService(nodeService);
        behaviours.setPolicyComponent(policyComponent);
        behaviours.setEnabled(enabled);
        behaviours.setIncludeEventTypes("NODEADDED");
        behaviours.setEventsService(eventsService);
        behaviours.init();
    }
}
