/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.repo.virtual.store.VirtualStoreImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VirtualBehaviourFilterExtensionTest extends VirtualizationIntegrationTest
{

    NodeRef virtualNodeRef;
    BehaviourFilter behaviourFilter;
    VirtualStoreImpl smartStore;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        behaviourFilter = ctx.getBean("policyBehaviourFilter", BehaviourFilter.class);
        smartStore = ctx.getBean("smartStore", VirtualStoreImpl.class);

        NodeRef nodeRef = nodeService.getChildByName(
                virtualFolder1NodeRef,
                ContentModel.ASSOC_CONTAINS,
                "Node1");

        virtualNodeRef = createContent(
                nodeRef,
                "actualContentName",
                "0",
                MimetypeMap.MIMETYPE_TEXT_PLAIN,
                "UTF-8").getChildRef();
    }

    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    /**
     * Checks the aspect auditable is enabled when asking for both actual and virtual nodes
     */
    @Test
    public void auditableAspectOfActualNodesShouldBeEnableByDefault()
    {
        NodeRef actualNodeRef = smartStore.materialize(Reference.fromNodeRef(virtualNodeRef));

        assertTrue("The auditable aspect for the actual node must be enabled by default",
                behaviourFilter.isEnabled(actualNodeRef, ContentModel.ASPECT_AUDITABLE));

        assertTrue("The auditable aspect must be enabled by default even when the virtual node is used",
                behaviourFilter.isEnabled(virtualNodeRef, ContentModel.ASPECT_AUDITABLE));
    }

    /**
     * Checks the disabling for a specific aspect
     */
    @Test
    public void shouldDisbaleAuditableAspectForTheActualNode()
    {
        assertTrue("The auditable aspect must be enabled by default even when the virtual node is used, since it hasn't been disabled yet",
                behaviourFilter.isEnabled(virtualNodeRef, ContentModel.ASPECT_AUDITABLE));

        NodeRef actualNodeRef = smartStore.materialize(Reference.fromNodeRef(virtualNodeRef));

        // Disable the aspect auditable using the virtual node
        behaviourFilter.disableBehaviour(virtualNodeRef, ContentModel.ASPECT_AUDITABLE);

        assertFalse("The auditable aspect for the actual node must not be enable since it has been disabled",
                behaviourFilter.isEnabled(actualNodeRef, ContentModel.ASPECT_AUDITABLE));
    }

    /**
     * Checks the disabling for the entire node
     */
    @Test
    public void shouldDisableTheActualNode()
    {
        assertTrue("The node must be enabled by default even when the virtual node is used, since it hasn't been disabled yet",
                behaviourFilter.isEnabled(virtualNodeRef));

        NodeRef actualNodeRef = smartStore.materialize(Reference.fromNodeRef(virtualNodeRef));
        assertTrue("The actual node must be enable since it hasn't been disabled yet",
                behaviourFilter.isEnabled(actualNodeRef));

        // Disabling the aspect auditable for the virtual node
        behaviourFilter.disableBehaviour(virtualNodeRef);

        assertFalse("The actual node must not be enable since it has been disabled",
                behaviourFilter.isEnabled(actualNodeRef));
    }
}
