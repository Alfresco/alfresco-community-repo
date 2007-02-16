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
package org.alfresco.repo.descriptor;

import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.debug.NodeStoreInspector;

public class DescriptorServiceTest extends BaseSpringTest
{
    private NodeService nodeService;
    private ImporterBootstrap systemBootstrap;
    private StoreRef storeRef;
    private AuthenticationComponent authenticationComponent;
    
    
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        nodeService = (NodeService)applicationContext.getBean(ServiceRegistry.NODE_SERVICE.getLocalName());
        systemBootstrap = (ImporterBootstrap)applicationContext.getBean("systemBootstrap");
        
        storeRef = new StoreRef("system", "Test_" + System.currentTimeMillis());
        systemBootstrap.setStoreUrl(storeRef.toString());
        systemBootstrap.bootstrap();
        
        this.authenticationComponent = (AuthenticationComponent)this.applicationContext.getBean("authenticationComponent");
        
        this.authenticationComponent.setSystemUserAsCurrentUser();
        
        
      
      
        System.out.println(NodeStoreInspector.dumpNodeStore(nodeService, storeRef));
    } 
    
    @Override
    protected void onTearDownInTransaction() throws Exception
    {
        authenticationComponent.clearCurrentSecurityContext();
        super.onTearDownInTransaction();
    }
    
    
    public void testServerDescriptor()
    {
        ServiceRegistry registry = (ServiceRegistry)applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        DescriptorService descriptorService = registry.getDescriptorService();
        Descriptor serverDescriptor = descriptorService.getServerDescriptor();
        
        String major = serverDescriptor.getVersionMajor();
        String minor = serverDescriptor.getVersionMinor();
        String revision = serverDescriptor.getVersionRevision();
        String label = serverDescriptor.getVersionLabel();
        String build = serverDescriptor.getVersionBuild();
        String version = major + "." + minor + "."  + revision;
        version = buildVersionString(version, label, build);
        
        assertEquals(version, serverDescriptor.getVersion());
        
        int schemaVersion = serverDescriptor.getSchema();
        assertTrue("Server schema version must be greater than 0", schemaVersion > 0);
    }

    public void testRepositoryDescriptor()
    {
        ServiceRegistry registry = (ServiceRegistry)applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        DescriptorService descriptorService = registry.getDescriptorService();
        Descriptor repoDescriptor = descriptorService.getInstalledRepositoryDescriptor();
        
        String major = repoDescriptor.getVersionMajor();
        String minor = repoDescriptor.getVersionMinor();
        String revision = repoDescriptor.getVersionRevision();
        String label = repoDescriptor.getVersionLabel();
        String build = repoDescriptor.getVersionBuild();
        String version = major + "." + minor + "."  + revision;
        version = buildVersionString(version, label, build);
        
        assertEquals(version, repoDescriptor.getVersion());
        
        int schemaVersion = repoDescriptor.getSchema();
        assertTrue("Repository schema version must be greater than -1", schemaVersion > -1);
    }
        
    private String buildVersionString(String version, String label, String build)
    {
       StringBuilder builder = new StringBuilder(version);
       
       boolean hasLabel = (label != null && label.length() > 0);
       boolean hasBuild = (build != null && build.length() > 0);
            
       // add opening bracket if either a label or build number is present
       if (hasLabel || hasBuild)
       {
           builder.append(" (");
       }
      
       // add label if present
       if (hasLabel)
       {
           builder.append(label);
       }
      
       // add build number is present
       if (hasBuild)
       {
           // if there is also a label we need a separating space
           if (hasLabel)
           {
               builder.append(" ");
           }
         
           builder.append(build);
       }
      
       // add closing bracket if either a label or build number is present
       if (hasLabel || hasBuild)
       {
           builder.append(")");
       }
      
       return builder.toString();
    }
}
