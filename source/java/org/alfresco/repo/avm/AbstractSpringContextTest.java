/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.avm;

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
        
        authenticationService.authenticate("admin", "admin".toCharArray());
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
