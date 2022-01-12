/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.integration.version;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.version.ExtendedVersionableAspect;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;

/**
 * Test recorded version histories when interacting with cm:versionable aspect
 * and the auto-version behvaiour.
 *
 * @author Roy Wetherall
 * @since 2.3.1
 */
public class AutoVersionTest extends RecordableVersionsBaseTest
{
    /**
     * Given a versionable document
     * When I specialise the type of the document
     * Then the version history has only one initial version
     * And it does not represent the current type of the document
     */
    public void testSpecialisedNodeInitialVersionCreated()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator)
        {
            private NodeRef myDocument;

            public void given() throws Exception
            {
                // create a document
                myDocument = fileFolderService.create(dmFolder, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();

                // make versionable
                nodeService.addAspect(myDocument, ContentModel.ASPECT_VERSIONABLE, null);
            }

            public void when()
            {
                // specialise document
                nodeService.setType(myDocument, TYPE_CUSTOM_TYPE);
            }

            public void then()
            {
                VersionHistory versionHistory = versionService.getVersionHistory(myDocument);

                assertNotNull(versionHistory);
                assertEquals(1, versionHistory.getAllVersions().size());

                NodeRef frozenState = versionHistory.getHeadVersion().getFrozenStateNodeRef();
                assertEquals(ContentModel.TYPE_CONTENT, nodeService.getType(frozenState));
                assertEquals(TYPE_CUSTOM_TYPE, nodeService.getType(myDocument));
            }
        });
    }

    /**
     * Given a versionable document with initial version turned off
     * When I specialise the type of the document
     * Then the version history remains empty
     */
    public void testSpecialisedNodeInitialVersionNotCreated()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator)
        {
            private NodeRef myDocument;

            public void given() throws Exception
            {
                // create a document
                myDocument = fileFolderService.create(dmFolder, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();

                // make versionable
                Map<QName, Serializable> props = new HashMap<>(1);
                props.put(ContentModel.PROP_INITIAL_VERSION, false);
                nodeService.addAspect(myDocument, ContentModel.ASPECT_VERSIONABLE, props);
            }

            public void when()
            {
                // specialise document
                nodeService.setType(myDocument, TYPE_CUSTOM_TYPE);
            }

            public void then()
            {
                VersionHistory versionHistory = versionService.getVersionHistory(myDocument);
                assertNull(versionHistory);
            }
        });
    }

    /**
     * Given a versionable document with initial version turned off
     * And auto version on type change is set on
     * When I specialise the type of the document
     * Then the version history contains the initial version
     */
    public void testSpecialisedNodeInitialVersionNotCreatedOnTypeChangeOn()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator)
        {
            private ExtendedVersionableAspect extendedVersionableAspect;
            private NodeRef myDocument;

            public void given() throws Exception
            {
                // turn auto version on type change on
                extendedVersionableAspect = (ExtendedVersionableAspect)applicationContext.getBean("rm.extendedVersionableAspect");
                assertNotNull(extendedVersionableAspect);
                extendedVersionableAspect.setAutoVersionOnTypeChange(true);

                // create a document
                myDocument = fileFolderService.create(dmFolder, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();

                // make versionable
                Map<QName, Serializable> props = new HashMap<>(1);
                props.put(ContentModel.PROP_INITIAL_VERSION, false);
                nodeService.addAspect(myDocument, ContentModel.ASPECT_VERSIONABLE, props);
            }

            public void when()
            {
                // specialise document
                nodeService.setType(myDocument, TYPE_CUSTOM_TYPE);
            }

            public void then()
            {
                VersionHistory versionHistory = versionService.getVersionHistory(myDocument);
                assertNotNull(versionHistory);
                assertEquals(1, versionHistory.getAllVersions().size());

                NodeRef frozenState = versionHistory.getHeadVersion().getFrozenStateNodeRef();
                assertEquals(TYPE_CUSTOM_TYPE, nodeService.getType(frozenState));
                assertEquals(TYPE_CUSTOM_TYPE, nodeService.getType(myDocument));
            }

            public void after() throws Exception
            {
                // reset auto version on type to default off
                extendedVersionableAspect.setAutoVersionOnTypeChange(false);
            }
        });
    }

    /**
     * Given a versionable document with initial version turned on
     * And auto version on type change is set on
     * When I specialise the type of the document
     * Then the version history contains the initial version
     */
    public void testSpecialisedNodeInitialVersionCreatedOnTypeChangeOn()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator)
        {
            private ExtendedVersionableAspect extendedVersionableAspect;
            private NodeRef myDocument;

            public void given() throws Exception
            {
                // turn auto version on type change on
                extendedVersionableAspect = (ExtendedVersionableAspect)applicationContext.getBean("rm.extendedVersionableAspect");
                assertNotNull(extendedVersionableAspect);
                extendedVersionableAspect.setAutoVersionOnTypeChange(true);

                // create a document
                myDocument = fileFolderService.create(dmFolder, GUID.generate(), ContentModel.TYPE_CONTENT).getNodeRef();

                // make versionable
                Map<QName, Serializable> props = new HashMap<>(1);
                props.put(ContentModel.PROP_INITIAL_VERSION, true);
                nodeService.addAspect(myDocument, ContentModel.ASPECT_VERSIONABLE, props);
            }

            public void when()
            {
                // specialise document
                nodeService.setType(myDocument, TYPE_CUSTOM_TYPE);
            }

            public void then()
            {
                VersionHistory versionHistory = versionService.getVersionHistory(myDocument);
                assertNotNull(versionHistory);
                assertEquals(2, versionHistory.getAllVersions().size());

                NodeRef frozenState = versionHistory.getHeadVersion().getFrozenStateNodeRef();
                assertEquals(TYPE_CUSTOM_TYPE, nodeService.getType(frozenState));
                assertEquals(TYPE_CUSTOM_TYPE, nodeService.getType(myDocument));

                frozenState = versionHistory.getVersion("1.0").getFrozenStateNodeRef();
                assertEquals(ContentModel.TYPE_CONTENT, nodeService.getType(frozenState));
                assertEquals(TYPE_CUSTOM_TYPE, nodeService.getType(myDocument));
            }

            public void after() throws Exception
            {
                // reset auto version on type to default off
                extendedVersionableAspect.setAutoVersionOnTypeChange(false);
            }
        });
    }


}
