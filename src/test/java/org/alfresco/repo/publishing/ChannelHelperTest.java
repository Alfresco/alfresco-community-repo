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

package org.alfresco.repo.publishing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test/alfresco/test-web-publishing-context.xml"})
public class ChannelHelperTest
{
    @Autowired
    private ChannelHelper helper;
    
    @Test
    public void testMapNodeRef() throws Exception
    {
//        String guid = GUID.generate();
//        NodeRef testNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, guid);
//        NodeRef liveEnvironmentNode = environmentHelper.getEnvironment(siteId, PublishingService.LIVE_ENVIRONMENT_NAME);
//        NodeRef mappedNodeRef = environmentHelper.mapEditorialToEnvironment(liveEnvironmentNode, testNodeRef);
//        assertNotSame(mappedNodeRef, testNodeRef);
//        NodeRef unmappedNodeRef = environmentHelper.mapEnvironmentToEditorial(liveEnvironmentNode, mappedNodeRef);
//        assertEquals(testNodeRef, unmappedNodeRef);
    }
}
