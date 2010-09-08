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

package org.alfresco.repo.rendition.executer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseAlfrescoSpringTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Unit tests for the HTML Rendering Engine
 * 
 * @author Nick Burch
 */
public class HTMLRenderingEngineTest extends BaseAlfrescoSpringTest
{
    private final static Log log = LogFactory.getLog(HTMLRenderingEngineTest.class);
    private NodeRef companyHome;
    private RenditionService renditionService;
    private Repository repositoryHelper;
    
    private NodeRef sourceDoc;
    private NodeRef targetFolder;
    private String targetFolderPath;

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.util.BaseAlfrescoSpringTest#onSetUpInTransaction()
     */
    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        super.onSetUpInTransaction();
        this.nodeService = (NodeService) this.applicationContext.getBean("NodeService");
        this.contentService = (ContentService) this.applicationContext.getBean("ContentService");
        this.renditionService = (RenditionService) this.applicationContext.getBean("RenditionService");
        this.repositoryHelper = (Repository) this.applicationContext.getBean("repositoryHelper");
        this.companyHome = repositoryHelper.getCompanyHome();
        
        createTargetFolder();
    }
    
    @Override
    protected void onTearDownInTransaction() throws Exception {
        super.onTearDownInTransaction();
        
        tidyUpSourceDoc();
    }

    private void createTargetFolder()
    {
        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
       
        Map<QName,Serializable> properties = new HashMap<QName,Serializable>();
        properties.put(ContentModel.PROP_NAME, "TestFolder");
        targetFolder = nodeService.createNode(
             companyHome, ContentModel.ASSOC_CONTAINS,
             QName.createQName("TestFolder"), 
             ContentModel.TYPE_FOLDER,
             properties
        ).getChildRef();
       
        targetFolderPath = "/" +
           (String) nodeService.getProperty(companyHome, ContentModel.PROP_NAME) +
           "/" +
           (String) nodeService.getProperty(targetFolder, ContentModel.PROP_NAME)
        ;
    }
    private void tidyUpSourceDoc()
    {
        // Set the current security context as admin
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());
       
        // Clean up the source
        if(sourceDoc != null)
        {
           nodeService.deleteNode(sourceDoc);
        }
      
        // Clean up the target folder
        nodeService.deleteNode(targetFolder);
        targetFolder = null;
        
        // All done
        sourceDoc = null;
        createTargetFolder();
    }
    
    private NodeRef createForDoc(String docname)
    {
       // Create the node
       Map<QName,Serializable> properties = new HashMap<QName,Serializable>();
       properties.put(ContentModel.PROP_NAME, docname);
       
       NodeRef node = nodeService.createNode(
             companyHome, ContentModel.ASSOC_CONTAINS,
             QName.createQName(docname),
             ContentModel.TYPE_CONTENT,
             properties
       ).getChildRef();
       
       // Put the sample doc into it
       ContentWriter writer = contentService.getWriter(
             node, ContentModel.PROP_CONTENT, true
       );
       writer.putContent("TESTING");
          
       // All done
       return node;
    }

    public void testBasics() throws Exception
    {
       RenditionDefinition def = renditionService.createRenditionDefinition(
             QName.createQName("Test"), HTMLRenderingEngine.NAME);
       def.setParameterValue(
             RenditionService.PARAM_DESTINATION_PATH_TEMPLATE,
             targetFolderPath + "/${name}.html"
       );

       sourceDoc = createForDoc("quick.doc");
          
       ChildAssociationRef rendition = renditionService.render(sourceDoc, def);
       assertNotNull(rendition);
          
       // Check it was created
       NodeRef htmlNode = rendition.getChildRef();
       assertEquals(true, nodeService.exists(htmlNode));
          
       // Check it got the right name
       assertEquals(
             "quick.html",
             nodeService.getProperty(htmlNode, ContentModel.PROP_NAME)
       );
       
       // Check it got the right contents
       ContentReader reader = contentService.getReader(
             htmlNode, ContentModel.PROP_CONTENT
       );
       String html = reader.getContentString();
       assertEquals("<?xml", html.substring(0, 5));
    }
    
    /**
     * Test for a .doc and a .docx, neither of which have images
     */
    public void testDocWithoutImages() throws Exception
    {
       RenditionDefinition def = renditionService.createRenditionDefinition(
             QName.createQName("Test"), HTMLRenderingEngine.NAME);
       def.setParameterValue(
             RenditionService.PARAM_DESTINATION_PATH_TEMPLATE,
             targetFolderPath + "/${name}.html"
       );

       for(String name : new String[] {"NoImages.doc","NoImages.docx"})
       {
          sourceDoc = createForDoc(name);
          
          int numItemsStart = nodeService.getChildAssocs(targetFolder).size();
          
          ChildAssociationRef rendition = renditionService.render(sourceDoc, def);
          assertNotNull(rendition);
          
          // Check it was created
          NodeRef htmlNode = rendition.getChildRef();
          assertEquals(true, nodeService.exists(htmlNode));
          
          // Check it got the right name
          assertEquals(
                name.substring(0, name.lastIndexOf('.')) + ".html",
                nodeService.getProperty(htmlNode, ContentModel.PROP_NAME)
          );
          
          // Check it ended up in the right place
          assertEquals(
                "Should have been in " + targetFolderPath + " but was  in" +
                   nodeService.getPath(htmlNode),
                targetFolder,
                nodeService.getPrimaryParent(htmlNode).getParentRef()
          );
          
          // Check it got the right contents
          ContentReader reader = contentService.getReader(
                htmlNode, ContentModel.PROP_CONTENT
          );
          String html = reader.getContentString();
          assertEquals("<?xml", html.substring(0, 5));
          
          // Check we didn't get an image folder, only the html
          int numItems = nodeService.getChildAssocs(targetFolder).size();
          // TODO - Enable this when proper folder stuff is in place
//          assertEquals(numItemsStart+1, numItems);
          
          // Check we didn't get any images
          for(ChildAssociationRef ref : nodeService.getChildAssocs(htmlNode))
          {
             if(ref.getTypeQName().equals(HTMLRenderingEngine.PRIMARY_IMAGE))
                fail("Found unexpected primary image of rendered html");
             if(ref.getTypeQName().equals(HTMLRenderingEngine.SECONDARY_IMAGE))
                fail("Found unexpected secondary image of rendered html");
          }
          
          // All done
          tidyUpSourceDoc();
       }
    }
    
    /**
     * Test for a .doc and a .docx, both of which have 
     *  a single image each
     */
    public void testDocWithOneImages() throws Exception
    {
       
    }
    
    /**
     * Test for a .doc and a .docx, both of which have 
     *  a multiple images each
     */
    public void testDocWithManyImages() throws Exception
    {
       
    }
}
