/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
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
package org.alfresco.repo.transfer.manifest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.transfer.TransferService;
import org.alfresco.service.cmr.transfer.TransferTarget;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.alfresco.util.TempFileProvider;

/**
 * Integration test for Transfer Manifest
 *
 * @author Mark Rogers
 */
public class ManifestIntegrationTest extends BaseAlfrescoSpringTest 
{
    private TransferService transferService;
    private PermissionService permissionService;
    private ContentService contentService;
    private NodeService mlAwareNodeService;
    
    /**
     * Called during the transaction setup
     */
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        
        // Get the required services
        this.transferService = (TransferService)this.applicationContext.getBean("TransferService");
        this.contentService = (ContentService)this.applicationContext.getBean("ContentService");
        this.permissionService = (PermissionService)this.applicationContext.getBean("PermissionService");
        this.mlAwareNodeService = (NodeService) this.applicationContext.getBean("mlAwareNodeService"); 
    }
    
    public void testSnapshot() throws Exception
    {
        // Snapshot a transfer node
        String CONTENT_STRING = "hello world";
        Locale CONTENT_LOCALE = Locale.TAIWAN;
        String CONTENT_TITLE = "the title";
        String CONTENT_NAME = "&the name <\\*";  // nasty name for XML
        String CONTENT_ASSOC_NAME = "&hell+-1we";
                
        String snapshotMe = "snapshotMe";
        String title = "title";
        String description = "description";
        String endpointProtocol = "http";
        String endpointHost = "localhost";
        int endpointPort = 8080;
        String endpointPath = "rhubarb";
        String username = "admin";
        char[] password = "password".toCharArray();
        
        Map<NodeRef, TransferManifestNode> sentNodes = new HashMap<NodeRef, TransferManifestNode>();
        
        TransferManifestNodeFactoryImpl nodeFactory = new TransferManifestNodeFactoryImpl();
        nodeFactory.setNodeService(nodeService);
        nodeFactory.setPermissionService(permissionService);
        nodeFactory.setMlAwareNodeService(mlAwareNodeService);
             
        /**
         * Create our transfer target
         */
        TransferTarget target = transferService.createAndSaveTransferTarget(snapshotMe, title, description, endpointProtocol, endpointHost, endpointPort, endpointPath, username, password);
                
        File snapshotFile = null;
        
        try
        {
            /**
             * Create a test node that we will read and write
             */
            ChildAssociationRef child = nodeService.createNode(target.getNodeRef(), ContentModel.ASSOC_CONTAINS, QName.createQName(CONTENT_ASSOC_NAME), ContentModel.TYPE_CONTENT);
            
            NodeRef childNodeRef = child.getChildRef();
            ContentWriter writer = contentService.getWriter(childNodeRef, ContentModel.PROP_CONTENT, true);
            writer.setLocale(CONTENT_LOCALE);
            writer.putContent(CONTENT_STRING);
            
            nodeService.setProperty(childNodeRef, ContentModel.PROP_TITLE, CONTENT_TITLE);
            
            nodeService.setProperty(childNodeRef, ContentModel.PROP_NAME, CONTENT_NAME);

            snapshotFile = TempFileProvider.createTempFile("xxx", ".xml");
            Writer snapshotWriter = new OutputStreamWriter(new FileOutputStream(snapshotFile), "UTF-8");       
        
            Set<NodeRef> nodes = new HashSet<NodeRef>();
            
            /**
             * Write three nodes
             * a: the root node of the workspace store
             * b: the target node
             * c: child of the target node
             */
            nodes.add(nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE));
            nodes.add(target.getNodeRef());
            nodes.add(childNodeRef);
            
            TransferManifestWriter formatter = new XMLTransferManifestWriter();
            TransferManifestHeader header = new TransferManifestHeader();
            header.setNodeCount(nodes.size());
            header.setCreatedDate(new Date());
            formatter.startTransferManifest(snapshotWriter);
            formatter.writeTransferManifestHeader(header);
            for(NodeRef nodeRef : nodes)
            {
                TransferManifestNode node = nodeFactory.createTransferManifestNode(nodeRef, null);
                formatter.writeTransferManifestNode(node);
                sentNodes.put(nodeRef, node);
            }
            formatter.endTransferManifest();
            snapshotWriter.close();
            
            // Show the snapshot file (For dev purposes)
            outputFile(snapshotFile);
            
            /**
             * Now read the snapshot file
             */
            TestTransferManifestProcessor processor = new TestTransferManifestProcessor(); 
            XMLTransferManifestReader reader = new XMLTransferManifestReader(processor);
            
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser parser = saxParserFactory.newSAXParser();
            parser.parse(snapshotFile, reader);
            
            /**
             * Now validate that we read back what we write out
             */
            assertEquals("did not get back the same number of nodes", nodes.size(), processor.getNodes().size());
            assertNotNull("header is null", processor.getHeader());
            
            for(NodeRef nodeId : nodes)
            {
                System.out.println("Processing node:" + nodeId);
                TransferManifestNormalNode readNode = (TransferManifestNormalNode)processor.getNodes().get(nodeId);
                TransferManifestNormalNode writeNode = (TransferManifestNormalNode)sentNodes.get(nodeId);
                assertNotNull("readNode is null", readNode);
                assertNotNull("writeNode is null", writeNode);
                
                assertEquals("type is different", writeNode.getType(), readNode.getType());
                assertEquals("nodeRef is different", writeNode.getNodeRef(), readNode.getNodeRef());
                assertEquals("parent node ref is different", writeNode.getPrimaryParentAssoc(), readNode.getPrimaryParentAssoc());
                if(writeNode.getParentPath() != null)
                {
                    assertEquals("parent path is different", writeNode.getParentPath().toString(), readNode.getParentPath().toString());
                }

                assertEquals("aspects array different size", writeNode.getAspects().size(), readNode.getAspects().size());
                for(QName aspect : writeNode.getAspects())
                {
                    assertTrue("missing aspect", readNode.getAspects().contains(aspect));
                }
                
                assertEquals("properties array different size", writeNode.getProperties().size(), readNode.getProperties().size());
                for(QName prop : writeNode.getProperties().keySet())
                {
                    assertTrue("missing property", readNode.getProperties().containsKey(prop));
                }
                
                assertEquals("child assocs different", writeNode.getChildAssocs().size(), readNode.getChildAssocs().size());
                assertEquals("parent assocs different", writeNode.getParentAssocs().size(), readNode.getParentAssocs().size());
                assertEquals("source assocs different", writeNode.getSourceAssocs().size(), readNode.getSourceAssocs().size());
                assertEquals("target assocs different", writeNode.getTargetAssocs().size(), readNode.getTargetAssocs().size());
         
                if(readNode.getNodeRef().equals(childNodeRef))
                {
                    /**
                     * Check the child node since we created it at the start of this test this test
                     */
                    ContentData data = (ContentData)readNode.getProperties().get(ContentModel.PROP_CONTENT);
                    assertEquals("content data wrong size", data.getSize(), CONTENT_STRING.length());
                    assertEquals("content locale wrong", data.getLocale(), CONTENT_LOCALE);
                    
                    String childTitle = ((MLText)readNode.getProperties().get(ContentModel.PROP_TITLE)).getDefaultValue();
                    assertEquals("content title wrong", childTitle, CONTENT_TITLE);
                    
                    String childName = (String)readNode.getProperties().get(ContentModel.PROP_NAME);
                    assertEquals("content name wrong", childName, CONTENT_NAME);
                    
                    /**
                     * Check the parent associations, there should be only one primary
                     */
                    assertTrue("one parent assoc", readNode.getParentAssocs().size() == 1);
                    assertTrue("isPrimary", readNode.getParentAssocs().get(0).isPrimary());
                    assertEquals("parent q name", readNode.getParentAssocs().get(0).getQName(), QName.createQName(CONTENT_ASSOC_NAME));
                    assertEquals("parent type q name", readNode.getParentAssocs().get(0).getTypeQName(), ContentModel.ASSOC_CONTAINS);
                    assertEquals("child node ref", readNode.getParentAssocs().get(0).getChildRef(), childNodeRef);
                    assertEquals("parent node ref", readNode.getParentAssocs().get(0).getParentRef(), target.getNodeRef());
                    assertTrue("zero child assoc", readNode.getChildAssocs().size() == 0);   
                    
                    /**
                     * Test Node Helper
                     */
                    assertEquals(readNode.getParentAssocs().get(0), TransferManifestNodeHelper.getPrimaryParentAssoc(readNode));
                    
                    Set<ContentData> content = TransferManifestNodeHelper.getContentData(readNode);
                    assertEquals("content not found", content.size(), 1);
                }
            }
        }
        finally
        {
            if(snapshotFile != null)
            {
                snapshotFile.delete();
            }
            transferService.deleteTransferTarget(snapshotMe);
        }
    }
    
    /**
     * Utility to dump the contents of a file to the console
     * @param file
     */
    private static void outputFile(File file) throws Exception
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String s = reader.readLine();
        while(s != null)
        {
            System.out.println(s);
            s = reader.readLine();
        }
    }
}