/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.test.integration.version;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionService;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionServiceImpl;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.util.GUID;

/**
 * Declare as record version integration tests
 *  
 * @author Roy Wetherall
 * @since 2.3
 */
public class DeclareAsRecordVersionTest extends RecordableVersionsBaseTest
{
    /** recordable version service */
    private RecordableVersionService recordableVersionService;
   
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#initServices()
     */
    @Override
    protected void initServices()
    {
        super.initServices();
        recordableVersionService = (RecordableVersionService)applicationContext.getBean("RecordableVersionService");
    }
    
    /**
     * Given versionable content with a non-recorded latest version
     * When I declare a version record
     * Then the latest version is recorded and a record is created
     */
    public void testDeclareLatestVersionAsRecord()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator)
        {               
            private NodeRef versionRecord;
            private Map<String, Serializable> versionProperties;    
            
            public void given() throws Exception
            {
                // setup version properties
                versionProperties = new HashMap<String, Serializable>(4);
                versionProperties.put(Version.PROP_DESCRIPTION, DESCRIPTION);
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
                
                // create version
                versionService.createVersion(dmDocument, versionProperties);
                
                // assert that the latest version is not recorded
                assertFalse(recordableVersionService.isCurrentVersionRecorded(dmDocument));
            }
            
            public void when()
            {   
                // create version record from latest version
                versionRecord = recordableVersionService.createRecordFromLatestVersion(filePlan, dmDocument);
            }            
            
            public void then()
            {
                // check the created record
                assertNotNull(versionRecord);
                assertTrue(recordService.isRecord(versionRecord));
                
                // assert the current version is recorded
                assertTrue(recordableVersionService.isCurrentVersionRecorded(dmDocument));
                
                // check the recorded version
                checkRecordedVersion(dmDocument, DESCRIPTION, "0.1");
            }
        });        
    } 
    
    /**
     * Given versionable content with a recorded latest version
     * When I declare a version record
     * Then nothing happens since the latest version is already recorded
     * And a warning is logged
     */
    public void testDeclareLatestVersionAsRecordButAlreadyRecorded()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator)
        {               
            private NodeRef versionRecord;
            private Map<String, Serializable> versionProperties;    
            
            public void given() throws Exception
            {
                // setup version properties
                versionProperties = new HashMap<String, Serializable>(4);
                versionProperties.put(Version.PROP_DESCRIPTION, DESCRIPTION);
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
                versionProperties.put(RecordableVersionServiceImpl.KEY_RECORDABLE_VERSION, true);
                versionProperties.put(RecordableVersionServiceImpl.KEY_FILE_PLAN, filePlan);
                
                // create version
                versionService.createVersion(dmDocument, versionProperties);
                
                // assert that the latest version is not recorded
                assertTrue(recordableVersionService.isCurrentVersionRecorded(dmDocument));
            }
            
            public void when()
            {   
                // create version record from latest version
                versionRecord = recordableVersionService.createRecordFromLatestVersion(filePlan, dmDocument);
            }            
            
            public void then()
            {
                // check that a record was not created
                assertNull(versionRecord);
                
                // assert the current version is recorded 
                assertTrue(recordableVersionService.isCurrentVersionRecorded(dmDocument));
                
                // check the recorded version
                checkRecordedVersion(dmDocument, DESCRIPTION, "0.1");
            }
        });        
    }

    /**
     * Given that a document is a specialized type
     * When version is declared as a record
     * Then the record is the same type as the source document 
     * 
     * @see https://issues.alfresco.com/jira/browse/RM-2194
     */
    public void testSpecializedContentType()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator)
        {               
            private NodeRef customDocument;
            private NodeRef versionRecord;
            private Map<String, Serializable> versionProperties;    
            
            public void given() throws Exception
            {
                // create content 
                customDocument = fileFolderService.create(dmFolder, GUID.generate(), TYPE_CUSTOM_TYPE).getNodeRef();
                prepareContent(customDocument);
                
                // setup version properties
                versionProperties = new HashMap<String, Serializable>(2);
                versionProperties.put(Version.PROP_DESCRIPTION, DESCRIPTION);
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
                
                // create version
                versionService.createVersion(customDocument, versionProperties);
                
                // assert that the latest version is not recorded
                assertFalse(recordableVersionService.isCurrentVersionRecorded(customDocument));
            }
            
            public void when()
            {   
                // create version record from latest version
                versionRecord = recordableVersionService.createRecordFromLatestVersion(filePlan, customDocument);
            }            
            
            public void then()
            {
                // check the created record
                assertNotNull(versionRecord);
                assertTrue(recordService.isRecord(versionRecord));
                
                // check the record type is correct
                assertEquals(TYPE_CUSTOM_TYPE, nodeService.getType(versionRecord));
                
                // assert the current version is recorded
                assertTrue(recordableVersionService.isCurrentVersionRecorded(customDocument));
                
                // check the recorded version
                checkRecordedVersion(customDocument, DESCRIPTION, "0.1");
            }
        });   
        
    }
    
}
