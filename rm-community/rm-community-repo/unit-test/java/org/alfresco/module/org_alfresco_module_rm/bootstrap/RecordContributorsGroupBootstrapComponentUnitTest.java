package org.alfresco.module.org_alfresco_module_rm.bootstrap;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.service.cmr.security.AuthorityType;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 * Record contributors group bootstrap component unit test
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public class RecordContributorsGroupBootstrapComponentUnitTest extends BaseUnitTest
{
    @InjectMocks
    private RecordContributorsGroupBootstrapComponent component;
    
    /**
     * Given that the record contributors group already exists
     * When I try and create the group
     * Then nothing happens
     */
    @Test
    public void groupAlreadyExists()
    {
        // group already exists
        doReturn(true).when(mockedAuthorityService).authorityExists(RecordContributorsGroupBootstrapComponent.GROUP_RECORD_CONTRIBUTORS);
        
        // create group
        component.createRecordContributorsGroup();
        
        // group not created
        verify(mockedAuthorityService, times(1)).authorityExists(RecordContributorsGroupBootstrapComponent.GROUP_RECORD_CONTRIBUTORS);
        verifyNoMoreInteractions(mockedAuthorityService);
    }
    
    /**
     * Given that the record contributors group does not exist
     * When I try and create the group
     * Then the group is successfully created
     * And 'everyone' is added to the new group
     */
    @Test
    public void createGroup()
    {
        // group does not exists
        doReturn(false).when(mockedAuthorityService).authorityExists(RecordContributorsGroupBootstrapComponent.GROUP_RECORD_CONTRIBUTORS);
        
        // create group
        component.createRecordContributorsGroup();
        
        // group not created
        verify(mockedAuthorityService, times(1)).createAuthority(AuthorityType.GROUP, RecordContributorsGroupBootstrapComponent.RECORD_CONTRIBUTORS);
        verify(mockedAuthorityService, times(1)).addAuthority(RecordContributorsGroupBootstrapComponent.GROUP_RECORD_CONTRIBUTORS, "admin");
        verify(mockedAuthorityService, times(1)).authorityExists(RecordContributorsGroupBootstrapComponent.GROUP_RECORD_CONTRIBUTORS);
        verifyNoMoreInteractions(mockedAuthorityService);
    }

}
