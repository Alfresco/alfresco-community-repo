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

package org.alfresco.repo.virtual.bundle;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.lock.mem.Lifetime;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;

public class VirtualLockableAspectInterceptorExtensionTest extends VirtualizationIntegrationTest
{
    private LockService lockService;

    private NodeRef originalContentNodeRef;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        lockService = ctx.getBean("lockService",
                LockService.class);
        NodeRef node = nodeService.getChildByName(virtualFolder1NodeRef,
                ContentModel.ASSOC_CONTAINS,
                "Node1");
        originalContentNodeRef = createContent(node,
                "originalContentName",
                "0",
                MimetypeMap.MIMETYPE_TEXT_PLAIN,
                "UTF-8").getChildRef();
    }

    @Test
    public void testHasLockableAspect() throws Exception
    {
        assertFalse("Node should not be reported as lockable",
                nodeService.hasAspect(originalContentNodeRef,
                        ContentModel.ASPECT_LOCKABLE));

        lockService.lock(originalContentNodeRef,
                LockType.WRITE_LOCK,
                10,
                Lifetime.EPHEMERAL,
                null);

        assertTrue("Node should be reported as lockable",
                nodeService.hasAspect(originalContentNodeRef,
                        ContentModel.ASPECT_LOCKABLE));
    }

    @Test
    public void testGetAspectsHasLockableAspect() throws Exception
    {
        assertFalse("Node should not contain lockable aspect",
                nodeService.getAspects(originalContentNodeRef).contains(ContentModel.ASPECT_LOCKABLE));

        lockService.lock(originalContentNodeRef,
                LockType.WRITE_LOCK,
                10,
                Lifetime.EPHEMERAL,
                null);

        assertTrue("Node should contain lockable aspect",
                nodeService.getAspects(originalContentNodeRef).contains(ContentModel.ASPECT_LOCKABLE));
    }
}
