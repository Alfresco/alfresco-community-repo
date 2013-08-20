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
package org.alfresco.repo.content.metadata.xml;

import static org.alfresco.repo.content.metadata.AbstractMetadataExtracterTest.QUICK_DESCRIPTION;
import static org.alfresco.repo.content.metadata.AbstractMetadataExtracterTest.QUICK_TITLE;

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
import org.alfresco.repo.content.filestore.FileContentWriter;
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
import org.alfresco.service.cmr.repository.MLText;
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
    private static final String FILE_DITA_FILE = "xml-metadata/dita-concept-quick.xml";
    private static final String FILE_ECLIPSE_PROJECT = "xml-metadata/eclipse-project-sample.xml";
    private static final String FILE_EMPTY = "xml-metadata/empty-sample.xml";
    private static final String FILE_MALFORMED = "xml-metadata/malformed-sample.xml";
    
    private static final String CTX_LOCATION = "classpath:xml-metadata/xml-metadata-test-context.xml";
    private static final ApplicationContext ctx = new ClassPathXmlApplicationContext(CTX_LOCATION);
    
    private ServiceRegistry serviceRegistry;
    private AuthenticationComponent authenticationComponent;
    private XPathMetadataExtracter alfrescoModelMetadataExtracter;
    private XPathMetadataExtracter eclipseProjectMetadataExtracter;
    private XPathMetadataExtracter ditaConceptMetadataExtracter;
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
        ditaConceptMetadataExtracter = (XPathMetadataExtracter) ctx.getBean("extracter.xml.DITAConceptMetadataExtracter");
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
        assertEquals("Gavin Cornwell", getPropertyValue(checkProperties, ContentModel.PROP_AUTHOR));
        assertEquals("fm:forummodel",  getPropertyValue(checkProperties, ContentModel.PROP_TITLE));
        assertEquals("Forum Model",    getPropertyValue(checkProperties, ContentModel.PROP_DESCRIPTION));
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
        assertEquals("Repository",    getPropertyValue(checkProperties, ContentModel.PROP_TITLE));
        assertEquals("JavaCC Nature", getPropertyValue(checkProperties, ContentModel.PROP_DESCRIPTION));
    }
    
    public void testDITAFileWithDoctype() throws Exception
    {
        // Load the file as-is, with it's doctype
        ContentReader reader = getReader(FILE_DITA_FILE);
        assertTrue(reader.exists());
        
        // Check we have the doctype
        String contents = reader.getContentString();
        assertTrue("DOCTYPE should be present but wasn't", contents.indexOf("<!DOCTYPE") > -1);
        
        // Reset ready for the extraction test
        reader = reader.getReader();
        
        // Now test extraction
        doTestDITAFile(reader);
    }
    public void testDITAFileWithoutDoctype() throws Exception
    {
        // Munge the file to skip the doctype
        ContentReader reader = getReader(FILE_DITA_FILE);
        assertTrue(reader.exists());
        String contents = reader.getContentString();
        String noDocType = contents.replaceAll("<!DOCTYPE.*?>", "");
        
        File tmp = File.createTempFile("alfresco", ".xml");
        tmp.deleteOnExit();
        ContentWriter w = new FileContentWriter(tmp);
        w.setEncoding(reader.getEncoding());
        w.setMimetype(reader.getMimetype());
        w.putContent(noDocType);
                
        // Now test extraction
        doTestDITAFile(w.getReader());
    }
    private void doTestDITAFile(ContentReader reader) throws Exception
    {
        // Pass it to the extracter
        PropertyMap checkProperties = new PropertyMap();
        ditaConceptMetadataExtracter.extract(reader, checkProperties);
        
        // Check the values
        assertEquals(QUICK_TITLE,       getPropertyValue(checkProperties, ContentModel.PROP_TITLE));
        assertEquals(QUICK_DESCRIPTION, getPropertyValue(checkProperties, ContentModel.PROP_DESCRIPTION));
    }
    
    public void testEmptyFile() throws Exception
    {
        // Get an empty file
        ContentReader reader = getReader(FILE_EMPTY);
        assertTrue(reader.exists());
        
        // Pass it to the extracter
        PropertyMap checkProperties = new PropertyMap();
        checkProperties.put(ContentModel.PROP_TITLE, getName());
        xmlMetadataExtracter.extract(reader, checkProperties);
        
        // The map should be unaffected
        assertNotNull("Properties changed by empty file extraction", checkProperties.get(ContentModel.PROP_TITLE));
        assertEquals("Properties changed by empty file extraction", getName(), checkProperties.get(ContentModel.PROP_TITLE));
    }
    
    public void testMalformedFile() throws Exception
    {
        // Get an empty file
        ContentReader reader = getReader(FILE_MALFORMED);
        assertTrue(reader.exists());
        
        // Pass it to the extracter
        PropertyMap checkProperties = new PropertyMap();
        checkProperties.put(ContentModel.PROP_TITLE, getName());
        xmlMetadataExtracter.extract(reader, checkProperties);
        
        // The map should be unaffected
        assertNotNull("Properties changed by malformed file extraction", checkProperties.get(ContentModel.PROP_TITLE));
        assertEquals("Properties changed by malformed file extraction", getName(), checkProperties.get(ContentModel.PROP_TITLE));
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
        assertEquals("Gavin Cornwell", getPropertyValue(checkAlfrescoModelProperties, ContentModel.PROP_AUTHOR));
        assertEquals("fm:forummodel",  getPropertyValue(checkAlfrescoModelProperties, ContentModel.PROP_TITLE));
        assertEquals("Forum Model",    getPropertyValue(checkAlfrescoModelProperties, ContentModel.PROP_DESCRIPTION));

        // Pass the Eclipse Project xml to the extractor
        PropertyMap checkEclipseProjectProperties = new PropertyMap();
        xmlMetadataExtracter.extract(eclipseProjectReader, checkEclipseProjectProperties);
        // Check the values
        assertEquals("Repository",    getPropertyValue(checkEclipseProjectProperties, ContentModel.PROP_TITLE));
        assertEquals("JavaCC Nature", getPropertyValue(checkEclipseProjectProperties, ContentModel.PROP_DESCRIPTION));
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
    
    private String getPropertyValue(PropertyMap properties, QName qname)
    {
        Object val = properties.get(qname);
        assertNotNull("Property " + qname + " missing, properties are " + properties.keySet(), val);
        
        if(val instanceof String)
            return (String)val;
        if(val instanceof MLText)
            return ((MLText)val).getDefaultValue();
        return val.toString();
    }
}
