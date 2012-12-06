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
package org.alfresco.module.org_alfresco_module_rm.test.service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.site.SiteServiceImpl;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;

/**
 * Records Service Implementation Test
 * 
 * @author Roy Wetherall
 * @author Tuna Askoy
 * @since 2.1
 */
public class RecordServiceImplTest extends BaseRMTestCase
{
    protected static final String COLLABORATION_SITE_ID = "collab-site-id";

    protected ActionService dmActionService;

    protected TaggingService taggingService;

    protected PermissionService dmPermissionService;
    
    protected ExtendedSecurityService extendedSecurityService;

    // collaboration site artifacts
    protected SiteInfo collaborationSite;
    protected NodeRef documentLibrary;
    protected NodeRef dmFolder;
    protected NodeRef dmDocument;

    // dm users
    protected String dmConsumer;
    protected NodeRef dmConsumerNodeRef;
    protected String dmCollaborator;
    protected NodeRef dmCollaboratorNodeRef;

    @Override
    protected void initServices()
    {
        super.initServices();

        dmActionService = (ActionService) applicationContext.getBean("ActionService");
        taggingService = (TaggingService) applicationContext.getBean("TaggingService");
        dmPermissionService = (PermissionService) applicationContext.getBean("PermissionService");
        extendedSecurityService = (ExtendedSecurityService) applicationContext.getBean("ExtendedSecurityService");
    }

    /**
     * This is a user test
     * 
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isUserTest()
     */
    @Override
    protected boolean isUserTest()
    {
        return true;
    }

    /**
     * This is a record test
     * 
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#isRecordTest()
     */
    @Override
    protected boolean isRecordTest()
    {
        return true;
    }

    /**
     * Setup the collaboration site test data
     */
    @Override
    protected void setupTestData()
    {
        super.setupTestData();

        doTestInTransaction(new Test<Void>()
        {
            public Void run()
            {
                setupCollaborationSiteTestDataImpl();
                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    protected void setupCollaborationSiteTestDataImpl()
    {
        // create collaboration site
        collaborationSite = siteService.createSite("preset", COLLABORATION_SITE_ID, "title", "description", SiteVisibility.PRIVATE);
        documentLibrary = SiteServiceImpl.getSiteContainer(
                COLLABORATION_SITE_ID, 
                SiteService.DOCUMENT_LIBRARY, 
                true,
                siteService, 
                transactionService, 
                taggingService);

        assertNotNull("Collaboration site document library component was not successfully created.", documentLibrary);

        // create a folder and documents
        dmFolder = fileFolderService.create(documentLibrary, "collabFolder", ContentModel.TYPE_FOLDER).getNodeRef();
        dmDocument = fileFolderService.create(dmFolder, "collabDocument.txt", ContentModel.TYPE_CONTENT).getNodeRef();

    }

    @Override
    protected void setupTestUsersImpl(NodeRef filePlan)
    {
        super.setupTestUsersImpl(filePlan);

        dmConsumer = GUID.generate();
        dmConsumerNodeRef = createPerson(dmConsumer);
        siteService.setMembership(COLLABORATION_SITE_ID, dmConsumer, SiteModel.SITE_CONSUMER);
        
        dmCollaborator = GUID.generate();
        dmCollaboratorNodeRef = createPerson(dmCollaborator);
        siteService.setMembership(COLLABORATION_SITE_ID, dmCollaborator, SiteModel.SITE_COLLABORATOR);
    }

    @Override
    protected void tearDownImpl()
    {
        super.tearDownImpl();
        siteService.deleteSite(COLLABORATION_SITE_ID);
    }

    /**
     * @see RecordService#getRecordMetaDataAspects()
     */
    public void testGetRecordMetaDataAspects() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                Set<QName> aspects = recordService.getRecordMetaDataAspects();
                assertNotNull(aspects);
                assertEquals(5, aspects.size());
                assertTrue(aspects.containsAll(getAspectList()));

                return null;
            }

            /**
             * Helper method for getting a list of record meta data aspects
             * 
             * @return Record meta data aspects as list
             */
            private List<QName> getAspectList()
            {
                QName[] aspects = new QName[] 
                { 
                    DOD5015Model.ASPECT_DIGITAL_PHOTOGRAPH_RECORD,
                    DOD5015Model.ASPECT_PDF_RECORD, 
                    DOD5015Model.ASPECT_WEB_RECORD,
                    DOD5015Model.ASPECT_SCANNED_RECORD, 
                    ASPECT_RECORD_META_DATA 
                };

                return Arrays.asList(aspects);
            }
        });
    }

    /**
     * @see RecordService#isRecord(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void testIsRecord() throws Exception
    {
        doTestInTransaction(new VoidTest()
        {
            @Override
            public void runImpl()
            {
                assertFalse(recordService.isRecord(filePlan));
                assertFalse(recordService.isRecord(rmContainer));
                assertFalse(recordService.isRecord(rmFolder));
                assertTrue(recordService.isRecord(recordOne));
                assertTrue(recordService.isRecord(recordDeclaredOne));
            }
        });
    }

    /**
     * @see RecordService#isDeclared(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void testIsDeclared() throws Exception
    {
        doTestInTransaction(new VoidTest()
        {
            @Override
            public void runImpl()
            {
                assertFalse(recordService.isRecord(filePlan));
                assertFalse(recordService.isRecord(rmContainer));
                assertFalse(recordService.isRecord(rmFolder));
                assertTrue(recordService.isRecord(recordOne));
                assertTrue(recordService.isRecord(recordDeclaredOne));
            }
        });
    }
    
    public void testUnfiled() throws Exception
    {
        doTestInTransaction(new VoidTest()
        {
            @Override
            public void runImpl()
            {
                assertFalse(recordService.isFiled(filePlan));
                assertFalse(recordService.isFiled(rmContainer));
                assertFalse(recordService.isFiled(rmFolder));
                assertTrue(recordService.isFiled(recordOne));
                assertTrue(recordService.isFiled(recordDeclaredOne));
            }
        });        
    }

    /**
     * @see RecordService#createRecord(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    public void testCreateRecord() throws Exception
    {
        // show that users without WRITE can not create a record from a document
        doTestInTransaction(new FailureTest
        (
            "Can not create a record from a document if you do not have WRITE permissions.",
            AccessDeniedException.class
        )
        {            
            public void run() throws Exception
            {
                recordService.createRecord(filePlan, dmDocument);
            }
         }, dmConsumer);
        
        // create record from document
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertFalse(recordService.isRecord(dmDocument));
                assertFalse(extendedSecurityService.hasExtendedReaders(dmDocument));
                
                checkPermissions(READ_RECORDS, 
                                 AccessStatus.DENIED,   // file plan 
                                 AccessStatus.DENIED,   // unfiled container
                                 AccessStatus.DENIED,   // record category
                                 AccessStatus.DENIED,   // record folder
                                 AccessStatus.DENIED);  // doc/record
                
                assertEquals(AccessStatus.DENIED, 
                        dmPermissionService.hasPermission(filePlan, RMPermissionModel.VIEW_RECORDS));
                
                checkPermissions(FILING, 
                        AccessStatus.DENIED,   // file plan 
                        AccessStatus.DENIED,   // unfiled container
                        AccessStatus.DENIED,   // record category
                        AccessStatus.DENIED,   // record folder
                        AccessStatus.DENIED);  // doc/record

                recordService.createRecord(filePlan, dmDocument);
                
                checkPermissions(READ_RECORDS, 
                        AccessStatus.ALLOWED,   // file plan 
                        AccessStatus.ALLOWED,   // unfiled container
                        AccessStatus.DENIED,   // record category
                        AccessStatus.DENIED,   // record folder
                        AccessStatus.ALLOWED);  // doc/record  
                
                assertEquals(AccessStatus.ALLOWED, 
                        dmPermissionService.hasPermission(filePlan, RMPermissionModel.VIEW_RECORDS));
                
                checkPermissions(FILING, 
                        AccessStatus.DENIED,   // file plan 
                        AccessStatus.DENIED,   // unfiled container
                        AccessStatus.DENIED,   // record category
                        AccessStatus.DENIED,   // record folder
                        AccessStatus.DENIED);  // doc/record  
                
                assertTrue(recordService.isRecord(dmDocument));
                assertTrue(extendedSecurityService.hasExtendedReaders(dmDocument));
                assertFalse(recordService.isFiled(dmDocument));

                return null;
            }
        }, dmCollaborator);
        
        

    }
    
    private void checkPermissions(String permission, AccessStatus filePlanExpected, 
                                                     AccessStatus unfiledExpected, 
                                                     AccessStatus recordCatExpected,
                                                     AccessStatus recordFolderExpected,
                                                     AccessStatus recordExpected)
    {
        assertEquals(filePlanExpected, 
                    dmPermissionService.hasPermission(filePlan, permission));       
        assertEquals(unfiledExpected, 
                    dmPermissionService.hasPermission(unfiledContainer, permission));
        assertEquals(recordCatExpected, 
                    dmPermissionService.hasPermission(rmContainer, permission));
        assertEquals(recordFolderExpected, 
                    dmPermissionService.hasPermission(rmFolder, permission));
        assertEquals(recordExpected, 
                dmPermissionService.hasPermission(dmDocument, permission));   
    }

    /**
     * @see RecordService#getUnfiledContainer(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void testGetUnfiledContainer() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run()
            {
                assertNotNull(recordService.getUnfiledContainer(filePlan));

                return null;
            }
        });
    }
}
