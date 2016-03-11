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
                versionProperties = new HashMap<String, Serializable>(4);
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
                versionProperties = new HashMap<String, Serializable>(4);
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
                versionProperties = new HashMap<String, Serializable>(4);
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
                versionProperties = new HashMap<String, Serializable>(4);
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
