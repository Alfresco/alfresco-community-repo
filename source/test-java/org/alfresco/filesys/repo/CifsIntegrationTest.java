package org.alfresco.filesys.repo;

import org.alfresco.jlan.server.config.ServerConfigurationAccessor;
import org.alfresco.jlan.server.filesys.DiskSharedDevice;
import org.alfresco.jlan.server.filesys.FilesystemsConfigSection;
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
        ServerConfigurationAccessor config = (ServerConfigurationAccessor) ctx.getBean("fileServerConfiguration");
        assertNotNull("No file server config available", config);
        // the server might, quite legitimately, not start
        if (!config.isServerRunning( "CIFS"))
        {
            return;
        }
        
        // get the server name
        String serverName = config.getServerName();
        assertNotNull("No server name available", serverName);
        assertTrue("No server name available (zero length)", serverName.length() > 0);

        // Get the primary filesystem, might be null if the home folder mapper is configured
        
        FilesystemsConfigSection filesysConfig = (FilesystemsConfigSection) config.getConfigSection(FilesystemsConfigSection.SectionName);
        DiskSharedDevice mainFilesys = (DiskSharedDevice) filesysConfig.getShares().enumerateShares().nextElement();
        
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
