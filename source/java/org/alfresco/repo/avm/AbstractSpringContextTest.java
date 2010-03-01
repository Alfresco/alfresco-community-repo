/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.avm;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public abstract class AbstractSpringContextTest extends AbstractDependencyInjectionSpringContextTests
{
    protected AVMService avmService;
    protected AuthenticationService authenticationService;
    protected ServiceRegistry servReg;
    protected PermissionService permissionService;

    @Override
    protected void onSetUp() throws Exception
    {
        super.onSetUp();
        servReg = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        avmService = servReg.getAVMService();
        assertNotNull(avmService);
        authenticationService = servReg.getAuthenticationService();
        assertNotNull(authenticationService);
        permissionService = servReg.getPermissionService();
        assertNotNull(permissionService);
        
        authenticationService.authenticate(AuthenticationUtil.getAdminUserName(), "admin".toCharArray());
    }

    @Override
    protected void onTearDown() throws Exception
    {
        super.onTearDown();
    }

    @Override
    protected String[] getConfigLocations()
    {
        return ApplicationContextHelper.CONFIG_LOCATIONS;
    }
}
