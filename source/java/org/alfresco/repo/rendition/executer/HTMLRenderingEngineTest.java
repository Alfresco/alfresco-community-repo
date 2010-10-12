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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.rendition.RenditionDefinitionPersisterImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
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
    private DictionaryService dictionaryService;
    private RenditionService renditionService;
    private Repository repositoryHelper;
    
    private NodeRef sourceDoc;
    private NodeRef targetFolder;
    private String targetFolderPath;
    
    private RenditionDefinition def;
    
    private static final String MIMETYPE_DOC = "application/msword";
    private static final String MIMETYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

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
        this.dictionaryService = (DictionaryService) this.applicationContext.getBean("dictionaryService");
        this.companyHome = repositoryHelper.getCompanyHome();
        
        createTargetFolder();
        
        // Setup the basic rendition definition
        QName renditionName = QName.createQName("Test");
        RenditionDefinition rd = renditionService.loadRenditionDefinition(renditionName); 
        if(rd != null)
        {
           RenditionDefinitionPersisterImpl rdp = new RenditionDefinitionPersisterImpl();
           rdp.setNodeService(nodeService);
           rdp.deleteRenditionDefinition(rd);
        }
        def = renditionService.createRenditionDefinition(renditionName, HTMLRenderingEngine.NAME);
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
    
    private NodeRef createForDoc(String docname) throws IOException
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
       File f = AbstractContentTransformerTest.loadNamedQuickTestFile(docname);
       if(f == null) {
          fail("Unable to find test file for " + docname);
       }
       
       ContentWriter writer = contentService.getWriter(
             node, ContentModel.PROP_CONTENT, true
       );
       if(docname.endsWith(".doc")) {
          writer.setMimetype(MIMETYPE_DOC);
       }
       if(docname.endsWith(".docx")) {
          writer.setMimetype(MIMETYPE_DOCX);
       }
       writer.putContent(f);
          
       if (log.isDebugEnabled())
       {
           log.debug("Created document with name: " + docname + ", nodeRef: " + node + ", mimetype: " + writer.getMimetype());
       }

       // All done
       return node;
    }

    public void testBasics() throws Exception
    {
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
       assertTrue("HTML wrong:\n"+html, html.contains("<html"));
       assertTrue("HTML wrong:\n"+html, html.contains("<head>"));
       assertTrue("HTML wrong:\n"+html, html.contains("<body>"));
       
       assertTrue("HTML wrong:\n"+html, html.contains("<p>The quick brown fox"));
       
       
       // Now do a body-only one, check that we still got the 
       //  contents, but not the html surround
       def.setParameterValue(
             HTMLRenderingEngine.PARAM_BODY_CONTENTS_ONLY, Boolean.TRUE
       );
       rendition = renditionService.render(sourceDoc, def);
       assertNotNull(rendition);
       
       htmlNode = rendition.getChildRef();
       assertEquals(true, nodeService.exists(htmlNode));
       
       reader = contentService.getReader(
             htmlNode, ContentModel.PROP_CONTENT
       );
       html = reader.getContentString();
       assertEquals("<?xml", html.substring(0, 5));
       assertFalse("Body wrong:\n"+html, html.contains("<html"));
       assertFalse("Body wrong:\n"+html, html.contains("<head>"));
       assertFalse("Body wrong:\n"+html, html.contains("<body>"));
       
       assertTrue("HTML wrong:\n"+html, html.contains("<p>The quick brown fox"));
       assertTrue("HTML wrong:\n"+html, html.contains("</p>"));
    }
    
    /**
     * Test for a .doc and a .docx, neither of which have images
     */
    public void testDocWithoutImages() throws Exception
    {
       def.setParameterValue(
             RenditionService.PARAM_DESTINATION_PATH_TEMPLATE,
             targetFolderPath + "/${name}.html"
       );

       for(String name : new String[] {"quick.doc","quick.docx"})
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
          assertEquals(numItemsStart+1, numItems);
          
          // Check that the html lacks img tags
          assertEquals(
                "Unexpected img tag in html:\n" + html,
                false, html.contains("<img")
          );
          
          // Check we didn't get any images
          for(ChildAssociationRef ref : nodeService.getChildAssocs(htmlNode))
          {
             // TODO Check against composite content associations when present 
//             if(ref.getTypeQName().equals(HTMLRenderingEngine.PRIMARY_IMAGE))
//                fail("Found unexpected primary image of rendered html");
//             if(ref.getTypeQName().equals(HTMLRenderingEngine.SECONDARY_IMAGE))
//                fail("Found unexpected secondary image of rendered html");
          }
          
          // All done
          tidyUpSourceDoc();
       }
    }
    
    /**
     * Test for a .doc and a .docx, both of which have 
     *  images in them
     */
    public void testDocWithImages() throws Exception
    {
       def.setParameterValue(
             RenditionService.PARAM_DESTINATION_PATH_TEMPLATE,
             targetFolderPath + "/${name}.html"
       );
       
       String[] files = new String[] {"quickImg1.doc","quickImg1.docx", "quickImg3.doc","quickImg3.docx"};
       int[] imgCounts = new int[] {1,1, 3,3};

       for(int i=0; i<files.length; i++)
       {
          String name = files[i];
          sourceDoc = createForDoc(name);
          
          String baseName = name.substring(0, name.lastIndexOf('.'));
          
          int numItemsStart = nodeService.getChildAssocs(targetFolder).size();
          
          ChildAssociationRef rendition = renditionService.render(sourceDoc, def);
          assertNotNull(rendition);
          
          // Check it was created
          NodeRef htmlNode = rendition.getChildRef();
          assertEquals(true, nodeService.exists(htmlNode));
          
          // Check it got the right name
          assertEquals(
                baseName + ".html",
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
          
          // Check that the html has the img tags
          assertEquals(
                "Couldn't find img tag in html:\n" + html,
                true, html.contains("<img")
          );
          
          // Check that it has the right img src
          String expSource = "src=\""+ baseName + "_files" + "/image";
          assertEquals(
                "Couldn't find correct img src in html:\n" + expSource + "\n" + html,
                true, html.contains(expSource)
          );
          
          // Check we got an image folder
          int numItems = nodeService.getChildAssocs(targetFolder).size();
          assertEquals(numItemsStart+2, numItems);
          
          // Check the name of the image folder
          NodeRef imgFolder = null;
          for(ChildAssociationRef ref : nodeService.getChildAssocs(targetFolder)) {
             if(nodeService.getProperty(ref.getChildRef(), ContentModel.PROP_NAME).equals(
                   baseName + "_files"
             )) {
                imgFolder = ref.getChildRef();
             }
          }
          assertNotNull("Couldn't find new folder named " + baseName + "_files", imgFolder);
          
          // Check the contents
          assertEquals(imgCounts[i], nodeService.getChildAssocs(imgFolder).size());
          
          
          // TODO Check against composite content associations when present 
          // Check the associations if supported
//          if(dictionaryService.getAssociation(HTMLRenderingEngine.PRIMARY_IMAGE) != null)
//          {
//             boolean hasPrimary = false;
//             boolean hasSecondary = false;
//             for(ChildAssociationRef ref : nodeService.getChildAssocs(htmlNode))
//             {
//                if(ref.getTypeQName().equals(HTMLRenderingEngine.PRIMARY_IMAGE))
//                   hasPrimary = true;
//                if(ref.getTypeQName().equals(HTMLRenderingEngine.SECONDARY_IMAGE))
//                   hasSecondary = true;
//             }
//             assertEquals(true, hasPrimary);
//             assertEquals(false, hasSecondary);
//          }
          
          // All done
          tidyUpSourceDoc();
       }
    }
    
    /**
     * Test for the option to have the images written to the
     *  same folder as the html, with a name prefix to them.
     *  
     * TODO Re-enable when we've figured out why the rendition service sulkts
     */
    public void DISABLEDtestImagesSameFolder() throws Exception
    {
       def.setParameterValue(
             RenditionService.PARAM_DESTINATION_PATH_TEMPLATE,
             targetFolderPath + "/${name}.html"
       );
       def.setParameterValue(
             HTMLRenderingEngine.PARAM_IMAGES_SAME_FOLDER,
             true
       );

       // The documents listed below have 3 embedded images each.
       final int expectedImageCount = 3;
       for(String name : new String[] {"quickImg3.doc","quickImg3.docx"})
       {
          sourceDoc = createForDoc(name);
          String baseName = name.substring(0, name.lastIndexOf('.'));
          
          int numItemsStart = nodeService.getChildAssocs(targetFolder).size();
          
          ChildAssociationRef rendition = renditionService.render(sourceDoc, def);
          assertNotNull(rendition);
          
          // Check it was created
          NodeRef htmlNode = rendition.getChildRef();
          assertEquals(true, nodeService.exists(htmlNode));
          
          // Check it got the right name
          assertEquals(
                baseName + ".html",
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
          
          // Check that the html has the img tags
          assertEquals(
                "Couldn't find img tag in html:\n" + html,
                true, html.contains("<img")
          );
          
          // Check that it has the right img src
          String expSource = "src=\""+ baseName + "_image";
          assertEquals(
                "Couldn't find correct img src in html:\n" + expSource + "\n" + html,
                true, html.contains(expSource)
          );
          
          // Check we got an image folder
          int numItems = nodeService.getChildAssocs(targetFolder).size();
          
          // We expect a number of images and one text/html node to be created.
          final int additionalItems = expectedImageCount + 1;
          assertEquals(numItemsStart+additionalItems, numItems);
          
          // There shouldn't be an image folder created
          for(ChildAssociationRef ref : nodeService.getChildAssocs(targetFolder)) {
             if(nodeService.getProperty(ref.getChildRef(), ContentModel.PROP_NAME).equals(
                   baseName + "_files"
             )) {
                fail("Image folder was created but shouldn't be there");
             }
          }
          
          // Check we got the images in the same directory as the html
          int images = 0;
          for(ChildAssociationRef ref : nodeService.getChildAssocs(targetFolder)) {
             String childName = (String)nodeService.getProperty(ref.getChildRef(), ContentModel.PROP_NAME);
             if(childName.startsWith(baseName + "_image")) {
                images++;
             }
          }
          assertEquals(expectedImageCount, images);
       }
    }
}
