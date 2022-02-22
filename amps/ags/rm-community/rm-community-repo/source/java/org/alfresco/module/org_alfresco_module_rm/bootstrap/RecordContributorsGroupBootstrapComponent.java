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

import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;

/**
 * Record contributors group bootstrap component
 * 
 * @author Roy Wetherall
 * @since  2.3
 */
public class RecordContributorsGroupBootstrapComponent
{
    // default record contributors group
    public static final String RECORD_CONTRIBUTORS = "RECORD_CONTRIBUTORS";
    public static final String GROUP_RECORD_CONTRIBUTORS = "GROUP_" + RECORD_CONTRIBUTORS;
    
    /** authority service */
    private AuthorityService authorityService;
    
    /** authentication utils */
    private AuthenticationUtil authenticationUtil;
    
    /**
     * @param authorityService  authority service
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * @param authenticationUtil    authentication util
     */
    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil)
    {
        this.authenticationUtil = authenticationUtil;
    }
        
    /**
     * Create record contributor group
     */
    public void createRecordContributorsGroup()
    {
        if (!authorityService.authorityExists(GROUP_RECORD_CONTRIBUTORS))
        {
            // create record contributors group
            authorityService.createAuthority(AuthorityType.GROUP, RECORD_CONTRIBUTORS);       
            
            // add the admin user 
            authorityService.addAuthority(GROUP_RECORD_CONTRIBUTORS, authenticationUtil.getAdminUserName());
        }        
    }
}
