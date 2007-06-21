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
package org.alfresco.repo.content.metadata.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.action.executer.ActionExecuter;
import org.alfresco.repo.action.executer.SetPropertyValueActionExecuter;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.repo.content.selector.RootElementNameContentWorkerSelector;
import org.alfresco.repo.content.selector.XPathContentWorkerSelector;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Tests various aspects of XML metadata extraction.
 * 
 * @see XPathMetadataExtracter
 * 
 * @author Derek Hulley
 */
public class XmlMetadataExtracterTest extends TestCase
{
    private static final String FILE_ALFRESCO_MODEL = "xml-metadata/alfresco-model-sample.xml";
    private static final String FILE_ECLIPSE_PROJECT = "xml-metadata/eclipse-project-sample.xml";
    
    private static final String CTX_LOCATION = "classpath:xml-metadata/xml-metadata-test-context.xml";
    private static final ApplicationContext ctx = new ClassPathXmlApplicationContext(CTX_LOCATION);
    
    private ServiceRegistry serviceRegistry;
    private AuthenticationComponent authenticationComponent;
    private XPathMetadataExtracter alfrescoModelMetadataExtracter;
    private XPathMetadataExtracter eclipseProjectMetadataExtracter;
    private RootElementNameContentWorkerSelector<MetadataExtracter> rootElementNameMetadataExtracterSelector;
    private XPathContentWorkerSelector<MetadataExtracter> xpathMetadataExtracterSelector;
    private XmlMetadataExtracter xmlMetadataExtracter;

    /**
     * Get a reader for a file that should be on the classpath.
     */
    private static final ContentReader getReader(String fileName) throws FileNotFoundException
    {
        URL url = AbstractContentTransformerTest.class.getClassLoader().getResource(fileName);
        if (url == null)
        {
            throw new FileNotFoundException("Could not find file on classpath: " + fileName);
        }
        File file = new File(url.getFile());
        if (!file.exists())
        {
            throw new FileNotFoundException("Could not find file on classpath: " + fileName);
        }
        ContentReader reader = new FileContentReader(file);
        reader.setMimetype(MimetypeMap.MIMETYPE_XML);
        return reader;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        authenticationComponent = (AuthenticationComponent) ctx.getBean("authenticationComponent");
        alfrescoModelMetadataExtracter = (XPathMetadataExtracter) ctx.getBean("extracter.xml.AlfrescoModelMetadataExtracter");
        eclipseProjectMetadataExtracter = (XPathMetadataExtracter) ctx.getBean("extracter.xml.EclipseProjectMetadataExtracter");
        rootElementNameMetadataExtracterSelector = (RootElementNameContentWorkerSelector<MetadataExtracter>) ctx.getBean("extracter.xml.selector.RootElementSelector");
        xpathMetadataExtracterSelector = (XPathContentWorkerSelector<MetadataExtracter>) ctx.getBean("extracter.xml.selector.XPathSelector");
        xmlMetadataExtracter = (XmlMetadataExtracter) ctx.getBean("extracter.xml.XMLMetadataExtracter");
        
        authenticationComponent.setSystemUserAsCurrentUser();
    }
    
    @Override
    public void tearDown() throws Exception
    {
        try { authenticationComponent.clearCurrentSecurityContext(); } catch (Throwable e) {}
    }

    public void testSetUp()
    {
        assertNotNull(alfrescoModelMetadataExtracter);
        assertNotNull(eclipseProjectMetadataExtracter);
    }
    
    public void testExtractAlfresocModel() throws Exception
    {
        // Load the example file
        ContentReader reader = getReader(FILE_ALFRESCO_MODEL);
        assertTrue(reader.exists());
        
        // Pass it to the extracter
        PropertyMap checkProperties = new PropertyMap();
        alfrescoModelMetadataExtracter.extract(reader, checkProperties);
        
        // Check the values
        assertEquals("Gavin Cornwell", checkProperties.get(ContentModel.PROP_AUTHOR));
        assertEquals("fm:forummodel", checkProperties.get(ContentModel.PROP_TITLE));
        assertEquals("Forum Model", checkProperties.get(ContentModel.PROP_DESCRIPTION));
    }
    
    public void testExtractEclipseProject() throws Exception
    {
        // Load the example file
        ContentReader reader = getReader(FILE_ECLIPSE_PROJECT);
        assertTrue(reader.exists());
        
        // Pass it to the extracter
        PropertyMap checkProperties = new PropertyMap();
        eclipseProjectMetadataExtracter.extract(reader, checkProperties);
        
        // Check the values
        assertEquals("Repository", checkProperties.get(ContentModel.PROP_TITLE));
        assertEquals("JavaCC Nature", checkProperties.get(ContentModel.PROP_DESCRIPTION));
    }
    
    public void testRootElementNameSelector() throws Exception
    {
        // Load the example files
        ContentReader alfrescoModelReader = getReader(FILE_ALFRESCO_MODEL);
        assertTrue(alfrescoModelReader.exists());
        ContentReader eclipseProjectReader = getReader(FILE_ECLIPSE_PROJECT);
        assertTrue(eclipseProjectReader.exists());
        
        // Check with an alfresco model document
        MetadataExtracter alfrescoModelExtracter = rootElementNameMetadataExtracterSelector.getWorker(alfrescoModelReader);
        assertNotNull("Failed to select correct extracter", alfrescoModelExtracter);
        assertTrue("Incorrect extracter instance selected", alfrescoModelMetadataExtracter == alfrescoModelExtracter);
        assertFalse("Read channel not closed", alfrescoModelReader.isChannelOpen());
        
        // Check with an eclipse project document
        MetadataExtracter eclipseProjectExtracter = rootElementNameMetadataExtracterSelector.getWorker(eclipseProjectReader);
        assertNotNull("Failed to select correct extracter", eclipseProjectExtracter);
        assertTrue("Incorrect extracter instance selected", eclipseProjectMetadataExtracter == eclipseProjectExtracter);
        assertFalse("Read channel not closed", eclipseProjectReader.isChannelOpen());
    }
    
    public void testXpathSelector() throws Exception
    {
        // Load the example files
        ContentReader alfrescoModelReader = getReader(FILE_ALFRESCO_MODEL);
        assertTrue(alfrescoModelReader.exists());
        ContentReader eclipseProjectReader = getReader(FILE_ECLIPSE_PROJECT);
        assertTrue(eclipseProjectReader.exists());
        
        // Check with an alfresco model document
        MetadataExtracter alfrescoModelExtracter = xpathMetadataExtracterSelector.getWorker(alfrescoModelReader);
        assertNotNull("Failed to select correct extracter", alfrescoModelExtracter);
        assertTrue("Incorrect extracter instance selected", alfrescoModelMetadataExtracter == alfrescoModelExtracter);
        assertFalse("Read channel not closed", alfrescoModelReader.isChannelOpen());
        
        // Check with an eclipse project document
        MetadataExtracter eclipseProjectExtracter = xpathMetadataExtracterSelector.getWorker(eclipseProjectReader);
        assertNotNull("Failed to select correct extracter", eclipseProjectExtracter);
        assertTrue("Incorrect extracter instance selected", eclipseProjectMetadataExtracter == eclipseProjectExtracter);
        assertFalse("Read channel not closed", eclipseProjectReader.isChannelOpen());
    }
    
    public void testXmlMetadataExtracter() throws Exception
    {
        // Load the example files
        ContentReader alfrescoModelReader = getReader(FILE_ALFRESCO_MODEL);
        assertTrue(alfrescoModelReader.exists());
        ContentReader eclipseProjectReader = getReader(FILE_ECLIPSE_PROJECT);
        assertTrue(eclipseProjectReader.exists());
        
        // Pass the Alfresco Model xml to the extractor
        PropertyMap checkAlfrescoModelProperties = new PropertyMap();
        xmlMetadataExtracter.extract(alfrescoModelReader, checkAlfrescoModelProperties);
        // Check the values
        assertEquals("Gavin Cornwell", checkAlfrescoModelProperties.get(ContentModel.PROP_AUTHOR));
        assertEquals("fm:forummodel", checkAlfrescoModelProperties.get(ContentModel.PROP_TITLE));
        assertEquals("Forum Model", checkAlfrescoModelProperties.get(ContentModel.PROP_DESCRIPTION));

        // Pass the Eclipse Project xml to the extractor
        PropertyMap checkEclipseProjectProperties = new PropertyMap();
        xmlMetadataExtracter.extract(eclipseProjectReader, checkEclipseProjectProperties);
        // Check the values
        assertEquals("Repository", checkEclipseProjectProperties.get(ContentModel.PROP_TITLE));
        assertEquals("JavaCC Nature", checkEclipseProjectProperties.get(ContentModel.PROP_DESCRIPTION));
    }
    
    /**
     * Tests metadata extraction using an action with an EAGER MetadataExtracter for XML.
     */
    public void testLifecycleOfXmlMetadataExtraction() throws Exception
    {
        NodeService nodeService = serviceRegistry.getNodeService();
        ContentService contentService = serviceRegistry.getContentService();
        ActionExecuter executer = (ActionExecuter) ctx.getBean("extract-metadata");
        Action action = new ActionImpl(null, GUID.generate(), SetPropertyValueActionExecuter.NAME, null);
        
        StoreRef storeRef = new StoreRef("test", getName());
        NodeRef rootNodeRef = null;
        if (nodeService.exists(storeRef))
        {
            rootNodeRef = nodeService.getRootNode(storeRef);
        }
        else
        {
            nodeService.createStore("test", getName());
            rootNodeRef = nodeService.getRootNode(storeRef);
        }
        
        // Set up some properties
        PropertyMap properties = new PropertyMap();
        properties.put(ContentModel.PROP_TITLE, "My title");
        properties.put(ContentModel.PROP_DESCRIPTION, "My description");
        
        NodeRef contentNodeRef = nodeService.createNode(
                rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, getName()),
                ContentModel.TYPE_CONTENT,
                properties).getChildRef();
        // Add some content
        ContentReader alfrescoModelReader = getReader(FILE_ALFRESCO_MODEL);
        assertTrue(alfrescoModelReader.exists());
        ContentWriter writer = contentService.getWriter(contentNodeRef, ContentModel.PROP_CONTENT, true);
        writer.setEncoding("UTF-8");
        writer.setMimetype(MimetypeMap.MIMETYPE_XML);
        writer.putContent(alfrescoModelReader);
        
        // Execute the action
        executer.execute(action, contentNodeRef);
        
        // Check the node's properties.  The EAGER overwrite policy should have replaced the required
        // properties.
        String checkTitle = (String) nodeService.getProperty(contentNodeRef, ContentModel.PROP_TITLE);
        String checkDescription = (String) nodeService.getProperty(contentNodeRef, ContentModel.PROP_DESCRIPTION);
        assertEquals("fm:forummodel", checkTitle);
        assertEquals("Forum Model", checkDescription);
    }
}
