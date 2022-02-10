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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.test.util.TestModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;

/**
 * AdHoc Recordable Versions Integration Test
 *  
 * @author Roy Wetherall
 * @since 2.3
 */
public class AdHocRecordableVersionsTest extends RecordableVersionsBaseTest
{
    /**
     * Adhoc recorded version creation, with no policy defined as site collaborator
     */
    public void testRecordAdHocVersionNoPolicy()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator)
        {
            private Map<String, Serializable> versionProperties;    
            
            public void given() throws Exception
            {
                // setup version properties
                versionProperties = new HashMap<>(4);
                versionProperties.put(Version.PROP_DESCRIPTION, DESCRIPTION);
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
                versionProperties.put(RecordableVersionServiceImpl.KEY_RECORDABLE_VERSION, true);
                versionProperties.put(RecordableVersionServiceImpl.KEY_FILE_PLAN, filePlan);
            }
            
            public void when()
            {                
                // create version
                versionService.createVersion(dmDocument, versionProperties);
            }            
            
            public void then()
            {
                // check that the record has been recorded
                checkRecordedVersion(dmDocument, DESCRIPTION, "0.1");
            }
        });        
    } 
    
    /**
     * Adhoc recordable version with recordable set as false
     */
    public void testRecordableVersionFalseNoPolicy()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator)
        {
            private Map<String, Serializable> versionProperties;    
            
            public void given() throws Exception
            {
                // setup version properties
                versionProperties = new HashMap<>(4);
                versionProperties.put(Version.PROP_DESCRIPTION, DESCRIPTION);
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
                versionProperties.put(RecordableVersionServiceImpl.KEY_RECORDABLE_VERSION, false);
                versionProperties.put(RecordableVersionServiceImpl.KEY_FILE_PLAN, filePlan);
            }
            
            public void when()
            {                
                // create version
                versionService.createVersion(dmDocument, versionProperties);
            }            
            
            public void then()
            {
                // check that the record has been recorded
                checkNotRecordedAspect(dmDocument, DESCRIPTION, "0.1");
            }
        });         
    }
    
    /**
     * Test no file plan specified (and no default available)
     */
    public void testNoFilePlan()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(AlfrescoRuntimeException.class, dmCollaborator)
        {
            private Map<String, Serializable> versionProperties;    
            
            public void given() throws Exception
            {
                // setup version properties
                versionProperties = new HashMap<>(4);
                versionProperties.put(Version.PROP_DESCRIPTION, DESCRIPTION);
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
                versionProperties.put(RecordableVersionServiceImpl.KEY_RECORDABLE_VERSION, true);
            }
            
            public void when()
            {                
                // create version
                versionService.createVersion(dmDocument, versionProperties);
            }           
            
            public void then()
            {
                // check that the record has been recorded
                checkRecordedVersion(dmDocument, DESCRIPTION, "0.1");
            }          
      
        });            
    }
    
    /**
     * Test recorded version with record metadata aspect (want to ensure additional non-rm URI properties and aspects
     * don't find their way into the frozen state)
     */
    public void testRecordedVersionWithRecordMetadataAspect()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator)
        {
            private Map<String, Serializable> versionProperties;    
            
            public void given() throws Exception
            {
                // setup version properties
                versionProperties = new HashMap<>(4);
                versionProperties.put(Version.PROP_DESCRIPTION, DESCRIPTION);
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
                versionProperties.put(RecordableVersionServiceImpl.KEY_RECORDABLE_VERSION, true);
                versionProperties.put(RecordableVersionServiceImpl.KEY_FILE_PLAN, filePlan);
            }
            
            public void when()
            {                
                // create version
                final Version version = versionService.createVersion(dmDocument, versionProperties);
                
                AuthenticationUtil.runAs(new RunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        // add custom meta-data to record
                        NodeRef record = recordableVersionService.getVersionRecord(version);
                        assertNotNull(record);
                        recordService.addRecordType(record, TestModel.ASPECT_RECORD_METADATA);
                        nodeService.setProperty(record, TestModel.PROPERTY_RECORD_METADATA, "Peter Wetherall");
                        
                        return null;
                    }
                }, AuthenticationUtil.getAdminUserName());                
            }            
            
            public void then()
            {
                // check that the record has been recorded
                checkRecordedVersion(dmDocument, DESCRIPTION, "0.1");
            }
        });  
    }
    
}
