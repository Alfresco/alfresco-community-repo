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

import java.util.Collection;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.repo.virtual.ref.GetActualNodeRefMethod;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.junit.Test;

public class VirtualVersionServiceExtensionTest extends VirtualizationIntegrationTest
{

    private VersionService versionService;

    private NodeRef node2;

    private NodeRef node2_1;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        versionService = ctx.getBean("VersionService",
                                     VersionService.class);

        node2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                           ContentModel.ASSOC_CONTAINS,
                                           "Node2");
        node2_1 = nodeService.getChildByName(node2,
                                             ContentModel.ASSOC_CONTAINS,
                                             "Node2_1");
    }

    @Test
    public void testGetCurrentVersion() throws Exception
    {
        ChildAssociationRef contentWithVersionsAssocRef = createContent(node2_1,
                                                                        "ContentWithVersions");
        NodeRef contentWithVersionsNodeRef = contentWithVersionsAssocRef.getChildRef();

        Version currentVersion = versionService.getCurrentVersion(contentWithVersionsNodeRef);
        assertNull(currentVersion);

        Version newVersion = versionService.createVersion(contentWithVersionsNodeRef,
                                                          null);
        assertNotNull(newVersion);

        Version newCurrentVersion = versionService.getCurrentVersion(contentWithVersionsNodeRef);
        assertNotNull(newCurrentVersion);

        assertSameVersion(newVersion,
                          newCurrentVersion);

    }

    private void assertSameVersion(Version expectedVersion, Version actualVersion)
    {
        assertEquals("FrozenStateNodeRefs are not the same",
                     expectedVersion.getFrozenStateNodeRef(),
                     actualVersion.getFrozenStateNodeRef());
        assertEquals("VersionedNodeRefs are not the same",
                     expectedVersion.getVersionedNodeRef(),
                     actualVersion.getVersionedNodeRef());
        assertEquals("Versionlabels are not the same",
                     expectedVersion.getVersionLabel(),
                     actualVersion.getVersionLabel());
    }

    @Test
    public void testCreateVersion() throws Exception
    {

        ChildAssociationRef contentWithVersionsAssocRef = createContent(node2_1,
                                                                        "ContentWithVersions");
        NodeRef contentWithVersionsNodeRef = contentWithVersionsAssocRef.getChildRef();
        assertTrue(Reference.isReference(contentWithVersionsNodeRef));
        NodeRef actualContentWithVersionsNodeRef = Reference
                    .fromNodeRef(contentWithVersionsNodeRef)
                        .execute(new GetActualNodeRefMethod(environment));

        VersionHistory versionHistory = versionService.getVersionHistory(contentWithVersionsNodeRef);
        assertNull(versionHistory);
        VersionHistory actualVersionHistory = versionService.getVersionHistory(actualContentWithVersionsNodeRef);
        assertNull(actualVersionHistory);

        Version newVersion = versionService.createVersion(contentWithVersionsNodeRef,
                                                          null);

        NodeRef newVersionNodeRef = newVersion.getVersionedNodeRef();
        assertTrue(Reference.isReference(newVersionNodeRef));

        versionHistory = versionService.getVersionHistory(newVersionNodeRef);
        assertNotNull(versionHistory);

        Collection<Version> allVersions = versionHistory.getAllVersions();
        assertEquals(1,
                     allVersions.size());

        actualVersionHistory = versionService.getVersionHistory(actualContentWithVersionsNodeRef);
        assertNotNull(actualVersionHistory);

        Collection<Version> allActualVersions = versionHistory.getAllVersions();
        assertEquals(1,
                     allActualVersions.size());

        Version actualVersion = actualVersionHistory.getHeadVersion();

        NodeRef newActualVersionNodeRef = actualVersion.getVersionedNodeRef();
        assertTrue(!Reference.isReference(newActualVersionNodeRef));

        assertEquals(newActualVersionNodeRef,
                     actualContentWithVersionsNodeRef);
    }

    @Test
    public void testRevert() throws Exception
    {
        ChildAssociationRef contentWithVersionsAssocRef = createContent(node2_1,
                                                                        "ContentWithVersions");
        // Create a versionable node
        NodeRef versionableNode = contentWithVersionsAssocRef.getChildRef();

        // Create the initial version
        Version version1 = versionService.createVersion(versionableNode,
                                                        null);

        // Check the history is correct
        VersionHistory history = versionService.getVersionHistory(versionableNode);
        assertEquals(version1.getVersionLabel(),
                     history.getHeadVersion().getVersionLabel());
        assertEquals(version1.getVersionedNodeRef(),
                     history.getHeadVersion().getVersionedNodeRef());
        assertEquals(1,
                     history.getAllVersions().size());
        Version[] versions = history.getAllVersions().toArray(new Version[1]);
        assertEquals("0.1",
                     versions[0].getVersionLabel());
        assertEquals("0.1",
                     nodeService.getProperty(versionableNode,
                                             ContentModel.PROP_VERSION_LABEL));

        ContentWriter contentWriter = this.contentService.getWriter(versionableNode,
                                                                    ContentModel.PROP_CONTENT,
                                                                    true);
        assertNotNull(contentWriter);

        // Record this as a new version
        Version version2 = versionService.createVersion(versionableNode,
                                                        null);

        // Check we're now seeing both versions in the history
        history = versionService.getVersionHistory(versionableNode);
        // assertEquals(version2.getVersionLabel(),
        // history.getHeadVersion().getVersionLabel());
        assertEquals(version2.getVersionedNodeRef(),
                     history.getHeadVersion().getVersionedNodeRef());
        assertEquals(2,
                     history.getAllVersions().size());

        versions = history.getAllVersions().toArray(new Version[2]);
        assertEquals("0.2",
                     versions[0].getVersionLabel());
        assertEquals("0.1",
                     versions[1].getVersionLabel());
        assertEquals("0.2",
                     nodeService.getProperty(versionableNode,
                                             ContentModel.PROP_VERSION_LABEL));

        // Change the property and content values
        ContentWriter contentWriter2 = this.contentService.getWriter(versionableNode,
                                                                     ContentModel.PROP_CONTENT,
                                                                     true);
        assertNotNull(contentWriter2);

        // Revert to the previous version, which will loose these changes
        this.versionService.revert(versionableNode);

        // Check that the history is back how it was
        history = versionService.getVersionHistory(versionableNode);
        assertEquals(version2.getVersionedNodeRef(),
                     history.getHeadVersion().getVersionedNodeRef());
        assertEquals(2,
                     history.getAllVersions().size());

        versions = history.getAllVersions().toArray(new Version[2]);
        assertEquals("0.2",
                     versions[0].getVersionLabel());
        assertEquals("0.1",
                     versions[1].getVersionLabel());
        assertEquals("0.2",
                     nodeService.getProperty(versionableNode,
                                             ContentModel.PROP_VERSION_LABEL));

        // Revert to the first version
        this.versionService.revert(versionableNode,
                                   version1);

        // Check the history still has 2 versions
        history = versionService.getVersionHistory(versionableNode);
        assertEquals(2,
                     history.getAllVersions().size());

        assertEquals("0.1",
                     history.getHeadVersion().getVersionLabel());

    }
}
