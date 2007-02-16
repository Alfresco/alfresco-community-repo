/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.importer;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.view.ImporterService;
import org.alfresco.service.cmr.view.Location;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.debug.NodeStoreInspector;


public class ImporterComponentTest extends BaseSpringTest
{
    private ImporterService importerService;
    private ImporterBootstrap importerBootstrap;
    private NodeService nodeService;
    private StoreRef storeRef;
    private AuthenticationComponent authenticationComponent;

    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        nodeService = (NodeService)applicationContext.getBean(ServiceRegistry.NODE_SERVICE.getLocalName());
        importerService = (ImporterService)applicationContext.getBean(ServiceRegistry.IMPORTER_SERVICE.getLocalName());
        
        importerBootstrap = (ImporterBootstrap)applicationContext.getBean("spacesBootstrap");
        
        this.authenticationComponent = (AuthenticationComponent)this.applicationContext.getBean("authenticationComponent");
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        // Create the store
        this.storeRef = nodeService.createStore(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
    }
    
    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        super.onTearDownInTransaction();
    }
    
    
    public void testImport()
        throws Exception
    {
        InputStream test = getClass().getClassLoader().getResourceAsStream("org/alfresco/repo/importer/importercomponent_test.xml");
        InputStreamReader testReader = new InputStreamReader(test, "UTF-8");
        Location location = new Location(storeRef);
        importerService.importView(testReader, location, null, new ImportTimerProgress());
        System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, storeRef));
    }
    
    public void testBootstrap()
    {
        StoreRef bootstrapStoreRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "Test_" + System.currentTimeMillis());
        importerBootstrap.setStoreUrl(bootstrapStoreRef.toString());
        importerBootstrap.bootstrap();
        authenticationComponent.setSystemUserAsCurrentUser();
        System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, bootstrapStoreRef));
    }
}

