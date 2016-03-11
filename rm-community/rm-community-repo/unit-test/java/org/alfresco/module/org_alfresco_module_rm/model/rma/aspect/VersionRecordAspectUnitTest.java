package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Version record aspect unit tests
 * 
 * @author Roy Wetherall
 * @since 2.3.1
 */
public class VersionRecordAspectUnitTest extends BaseUnitTest
{
    /** service mocks */
    private @Mock VersionHistory mockedVersionHistory;
    private @Mock Version mockedVersion;    
    private @Mock VersionService mockedVersionService;
    private @Mock RelationshipService mockedRelationshipService;
    
    /** test object */
    private @InjectMocks VersionRecordAspect versionRecordAspect;
    
    /**
     * given that there is no recorded version
     * before delete of record
     * then nothing happens
     */
    @Test
    public void beforeDeleteNoVersionNodeRef()
    {
        NodeRef nodeRef = generateNodeRef();
        
        when(mockedRecordableVersionService.getRecordedVersion(nodeRef))
            .thenReturn(null);
        
        versionRecordAspect.beforeDeleteNode(nodeRef);
        
        verify(mockedNodeService, never()).getProperty(nodeRef, RecordableVersionModel.PROP_VERSION_LABEL);        
        verify(mockedRecordableVersionService, never()).destroyRecordedVersion(any(Version.class));        
    }
     
    /**
     * given that there is a recorded version
     * before delete of record
     * then the version is marked as destroyed
     */
    @Test
    public void beforeDeleteMarkVersionDestroyed()
    {
        NodeRef nodeRef = generateNodeRef();
        
        when(mockedRecordableVersionService.getRecordedVersion(nodeRef))
            .thenReturn(mockedVersion);
        
        versionRecordAspect.beforeDeleteNode(nodeRef);
           
        verify(mockedRecordableVersionService).destroyRecordedVersion(mockedVersion); 
    }
}
