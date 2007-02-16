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
package org.alfresco.filesys.smb.server.repo;

import org.alfresco.filesys.CIFSServer;
import org.alfresco.filesys.server.filesys.DiskSharedDevice;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.BaseAlfrescoTestCase;

/**
 * Checks that the required configuration details are obtainable from the CIFS components.
 * 
 * @author Derek Hulley
 */
public class CifsIntegrationTest extends BaseAlfrescoTestCase
{
    
    public void testGetServerName()
    {
        CIFSServer cifsServer = (CIFSServer) ctx.getBean("cifsServer");
        assertNotNull("No CIFS server available", cifsServer);
        // the server might, quite legitimately, not start
        if (!cifsServer.isStarted())
        {
            return;
        }
        
        // get the server name
        String serverName = cifsServer.getConfiguration().getServerName();
        assertNotNull("No server name available", serverName);
        assertTrue("No server name available (zero length)", serverName.length() > 0);

        // Get the primary filesystem, might be null if the home folder mapper is configured
        
        DiskSharedDevice mainFilesys = cifsServer.getConfiguration().getPrimaryFilesystem();
        
        if ( mainFilesys != null)
        {
            // Check the share name
            
            String shareName = mainFilesys.getName();
            assertNotNull("No share name available", shareName);
            assertTrue("No share name available (zero length)", shareName.length() > 0);

            // Check that the context is valid
            
            ContentContext filesysCtx = (ContentContext) mainFilesys.getContext();
            assertNotNull("Content context is null", filesysCtx);
            assertNotNull("Store id is null", filesysCtx.getStoreName());
            assertNotNull("Root path is null", filesysCtx.getRootPath());
            assertNotNull("Root node is null", filesysCtx.getRootNode());
            
            // Check the root node
            
            NodeService nodeService = (NodeService) ctx.getBean(ServiceRegistry.NODE_SERVICE.getLocalName());
            // get the share root node and check that it exists
            NodeRef shareNodeRef = filesysCtx.getRootNode();
            assertNotNull("No share root node available", shareNodeRef);
            assertTrue("Share root node doesn't exist", nodeService.exists(shareNodeRef));
        }
    }
}
