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

package org.alfresco.module.org_alfresco_module_rm.test.integration.record;

import static org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel.PROP_FILE_PLAN;
import static org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel.PROP_RECORDABLE_VERSION_POLICY;
import static org.alfresco.service.cmr.version.VersionType.MINOR;
import static org.springframework.extensions.webscripts.GUID.generate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.relationship.Relationship;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.GUID;

/**
 * reject record tests.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public class RejectRecordTest extends BaseRMTestCase
{
    private VersionService versionService;
    private CheckOutCheckInService checkOutCheckInService;

    private static final String REASON = GUID.generate();
    private static final String FINAL_VERSION = "rm.service.final-version";

    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    @Override
    protected void initServices()
    {
        super.initServices();

        versionService = (VersionService) applicationContext.getBean("VersionService");
        checkOutCheckInService = (CheckOutCheckInService) applicationContext.getBean("CheckOutCheckInService");
    }

    /**
     *
     */
    public void testRejectedRecordInCorrectState() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            public void given()
            {
                assertFalse(recordService.isRecord(dmDocument));
                ownableService.setOwner(dmDocument, userName);

                // document is declared as a record by user
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        // declare record
                        recordService.createRecord(filePlan, dmDocument);
                        return null;
                    }
                 }, userName);
            }

            public void when()
            {
                // sanity checks
                assertTrue(recordService.isRecord(dmDocument));
                assertTrue(permissionService.getInheritParentPermissions(dmDocument));

                // reject record
                recordService.rejectRecord(dmDocument, REASON);
            }

            public void then()
            {
                // document is no longer a record
                assertFalse(recordService.isRecord(dmDocument));

                // expected owner has be re-set
                assertEquals(userName, ownableService.getOwner(dmDocument));
                assertTrue(permissionService.getInheritParentPermissions(dmDocument));
                assertFalse(nodeService.hasAspect(dmDocument, ASPECT_FILE_PLAN_COMPONENT));
            }
        });
    }

    /**
     *
     */
    public void testRevertAfterReject() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef document;

            public void given()
            {
                NodeRef folder = fileFolderService.create(documentLibrary, GUID.generate(), TYPE_FOLDER).getNodeRef();
                document = fileFolderService.create(folder, GUID.generate(), TYPE_CONTENT).getNodeRef();

                assertFalse(recordService.isRecord(document));
                ownableService.setOwner(document, userName);
                versionService.ensureVersioningEnabled(document, null);

                // document is declared as a record by user
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        // declare record
                        recordService.createRecord(filePlan, document);
                        return null;
                    }
                 }, userName);

                assertTrue(nodeService.hasAspect(document, ASPECT_FILE_PLAN_COMPONENT));
            }

            public void when()
            {
                // reject the record
                recordService.rejectRecord(document, REASON);
                assertFalse(nodeService.hasAspect(document, ASPECT_FILE_PLAN_COMPONENT));

                // upload a new version of the document
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        ContentWriter writer = contentService.getWriter(document, ContentModel.PROP_CONTENT, true);
                        writer.putContent("This is a change to the content and should force a new version");
                        versionService.createVersion(document, null);

                        return null;
                    }
                }, userName);

                assertFalse(nodeService.hasAspect(document, ASPECT_FILE_PLAN_COMPONENT));

                VersionHistory history = versionService.getVersionHistory(document);
                assertEquals(2, history.getAllVersions().size());
                final Version initial = history.getRootVersion();

                assertFalse(nodeService.hasAspect(initial.getFrozenStateNodeRef(), ASPECT_FILE_PLAN_COMPONENT));

                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        // revert the document to a previous version
                        versionService.revert(document, initial);

                        return null;
                    }
                 }, userName);
            }

            public void then()
            {
                // document is no longer a record
                assertFalse(recordService.isRecord(document));

                // expected owner has be re-set
                assertEquals(userName, ownableService.getOwner(document));
            }
        });
    }

    public void testRelationshipAfterRevertingRecord()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            // Test document
            private NodeRef document;

            public void given()
            {
                // Create a test document
                NodeRef folder = fileFolderService.create(documentLibrary, generate(), TYPE_FOLDER).getNodeRef();
                document = fileFolderService.create(folder, generate(), TYPE_CONTENT).getNodeRef();

                // Set Auto-Declare Versions to "For all major and minor versions"
                PropertyMap recordableVersionProperties = new PropertyMap(2);
                recordableVersionProperties.put(PROP_RECORDABLE_VERSION_POLICY, RecordableVersionPolicy.ALL);
                recordableVersionProperties.put(PROP_FILE_PLAN, filePlan);
                nodeService.addAspect(document, ASPECT_VERSIONABLE, recordableVersionProperties);

                // Upload New Version
                document = checkOutCheckInService.checkout(document);
                Map<String, Serializable> props = new HashMap<>(2);
                props.put(Version.PROP_DESCRIPTION, generate());
                props.put(VersionModel.PROP_VERSION_TYPE, MINOR);
                document = checkOutCheckInService.checkin(document, props);

                // Check the declared version
                List<ChildAssociationRef> childAssocs = nodeService.getChildAssocs(unfiledContainer);
                assertEquals(1, childAssocs.size());

                // Declare document as record
                recordService.createRecord(filePlan, document);

                // Check the declared versions
                childAssocs = nodeService.getChildAssocs(unfiledContainer);
                assertEquals(2, childAssocs.size());

                // Check that the document is a file plan component
                assertTrue(nodeService.hasAspect(document, ASPECT_FILE_PLAN_COMPONENT));

                // Get the final version
                NodeRef finalVersion = null;
                for (ChildAssociationRef childAssociationRef : nodeService.getChildAssocs(unfiledContainer))
                {
                    NodeRef childRef = childAssociationRef.getChildRef();
                    String label = (String) nodeService.getProperty(document, RecordableVersionModel.PROP_VERSION_LABEL);

                    if (label.equals(I18NUtil.getMessage(FINAL_VERSION)))
                    {
                        finalVersion = childRef;
                        break;
                    }
                }

                // The final version should be the declared record
                assertEquals(document, finalVersion);

                // Check the relationship
                Set<Relationship> relationships = relationshipService.getRelationshipsFrom(document);
                assertEquals(1, relationships.size());
                Relationship relationship = relationships.iterator().next();
                assertEquals(CUSTOM_REF_VERSIONS.getLocalName(), relationship.getUniqueName());
            }

            public void when()
            {
                // Reject record
                recordService.rejectRecord(document, generate());
            }

            public void then()
            {
                // Check the relationship
                Set<Relationship> relationships = relationshipService.getRelationshipsFrom(document);
                assertEquals(0, relationships.size());
            }
        });
    }
}
