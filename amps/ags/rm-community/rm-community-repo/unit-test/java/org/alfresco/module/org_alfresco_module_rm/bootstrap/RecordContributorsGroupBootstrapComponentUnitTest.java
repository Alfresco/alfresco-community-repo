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
