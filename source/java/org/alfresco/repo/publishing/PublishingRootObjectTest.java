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

package org.alfresco.repo.publishing;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

/**
 * @author Nick Smith
 * 
 */
public class PublishingRootObjectTest extends AbstractPublishingIntegrationTest
{
    @Resource(name="publishingRootObject")
    private PublishingRootObject rootObject;

    @Test
    public void testGetEnvironment() throws Exception
    {
        Environment theEnvironment = rootObject.getEnvironment();
        assertNotNull(theEnvironment);
        assertTrue(nodeService.exists(theEnvironment.getNodeRef()));
        assertEquals(PublishingModel.TYPE_ENVIRONMENT, nodeService.getType(theEnvironment.getNodeRef()));
    }

    @Test
    public void testGetPublishingQueue() throws Exception
    {
        PublishingQueueImpl theQueue = rootObject.getPublishingQueue();
        assertNotNull(theQueue);
        NodeRef queueNode = theQueue.getNodeRef();
        assertTrue(nodeService.exists(queueNode));
        assertEquals(PublishingModel.TYPE_PUBLISHING_QUEUE, nodeService.getType(queueNode));
        ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(queueNode);
        assertEquals(PublishingModel.ASSOC_PUBLISHING_QUEUE, parentAssoc.getTypeQName());
        assertEquals(rootObject.getEnvironment().getNodeRef(), parentAssoc.getParentRef());
    }

    @Test
    public void testGetChannelContainer() throws Exception
    {
        NodeRef channels = rootObject.getChannelContainer();
        assertNotNull(channels);
        assertTrue(nodeService.exists(channels));
        assertEquals(ContentModel.TYPE_FOLDER, nodeService.getType(channels));
        ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(channels);
        assertEquals(PublishingRootObject.CHANNELS_QNAME, parentAssoc.getQName());
        assertEquals(rootObject.getEnvironment().getNodeRef(), parentAssoc.getParentRef());
    }

    @Override
    public void onSetUp() throws Exception
    {
        super.onSetUp();
        this.rootObject = (PublishingRootObject) getApplicationContext().getBean(PublishingRootObject.NAME);
    }

}
