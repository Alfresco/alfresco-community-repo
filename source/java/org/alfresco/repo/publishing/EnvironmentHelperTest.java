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

import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

/**
 * @author Brian
 * 
 */
public class EnvironmentHelperTest extends AbstractPublishingIntegrationTest
{
    @Resource(name="environmentHelper")
    private EnvironmentHelper environmentHelper;

    @Override
    public void onSetUp() throws Exception
    {
        super.onSetUp();
        environmentHelper = (EnvironmentHelper) getApplicationContext().getBean("environmentHelper");
    }

    @Test
    public void testGetEnvironmentByName() throws Exception
    {
        NodeRef liveEnvironment = environmentHelper.getEnvironment(siteId);
        assertNotNull(liveEnvironment);
        assertTrue(nodeService.exists(liveEnvironment));
        assertEquals(PublishingModel.TYPE_ENVIRONMENT, nodeService.getType(liveEnvironment));
    }

    @Test
    public void testGetPublishingQueue() throws Exception
    {
        NodeRef liveEnvironment = environmentHelper.getEnvironment(siteId);
        NodeRef publishingQueue = environmentHelper.getPublishingQueue(liveEnvironment);
        assertNotNull(publishingQueue);
        assertTrue(nodeService.exists(publishingQueue));
        assertEquals(PublishingModel.TYPE_PUBLISHING_QUEUE, nodeService.getType(publishingQueue));
        assertEquals(PublishingModel.ASSOC_PUBLISHING_QUEUE, nodeService.getPrimaryParent(publishingQueue)
                .getTypeQName());
    }

}
