/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.rest.test;

import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Request;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;
import org.xml.sax.InputSource;

public class CMISAtomTemplatesTest extends BaseCMISTest
{
    private NodeService nodeService;
    
    static String docName;

    static String xmlResponse;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        // create document
        docName = "Test 1                                        _" + System.currentTimeMillis();
        Link children = cmisClient.getChildrenLink(testCaseFolder);
        Entry document = createObject(children.getHref(), docName, "cmis:document");
        Request documentReq = new GetRequest(document.getSelfLink().getHref().toString());
        Response documentRes = sendRequest(documentReq, 200);
        Assert.assertNotNull(documentRes);
        xmlResponse = documentRes.getContentAsString();
        Assert.assertNotNull(xmlResponse);
        
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
    }

    @Test
    public void testChildrenGetAtomFeed() throws Exception
    {
        // retrieve the list of nodes
        String id = testCaseFolder.getId().toString().replace("urn:uuid:", "workspace:SpacesStore/i/");
        Request get = new GetRequest("/cmis/s/" + id + "/children");
        Response res = sendRequest(get, 200);
        String xml = res.getContentAsString();

        // check document name with repeatable spaces in xml response.
        assertEquals("Probably, children.get.atomfeed.ftl template has compress dirrective", true, xml.contains(docName));
    }

    @Test
    public void testItemGetAtomentryTemplate() throws Exception
    {
        // check document name with repeatable spaces in xml response.
        assertEquals("Probably, item.get.atomentry.ftl template has compress dirrective", true, xmlResponse.contains(docName));
    }
    
    /*
     * Get children, if parent has object of non-cmis type
     * cm:folderlink is non-cmis type.
     */
    @Test
    public void testChildrenWithLink() throws Exception
    {
        String testFolderRefStr = testCaseFolder.getId().toString().replace("urn:uuid:", "workspace://SpacesStore/");
        NodeRef testFolderRef = new NodeRef(testFolderRefStr);
        
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(2, 1.0f);
        String linkName = "link " + testCaseFolder.getTitle() + ".url";
        props.put(ContentModel.PROP_NAME, linkName);
        props.put(ContentModel.PROP_LINK_DESTINATION, testFolderRef);
        
        AuthenticationUtil.pushAuthentication();;
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        
        NodeRef linkRef = null;
        
        try
        {
            ChildAssociationRef childRef = nodeService.createNode(
                    testFolderRef,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(testCaseFolder.getTitle()),
                    ApplicationModel.TYPE_FOLDERLINK,
                    props); 
            linkRef = childRef.getChildRef();
            
            String id = testCaseFolder.getId().toString().replace("urn:uuid:", "workspace:SpacesStore/i/");
            Request get = new GetRequest("/cmis/s/" + id + "/children");
            sendRequest(get, 200);
        }
        finally
        {
            if (linkRef != null)
            {
                nodeService.deleteNode(linkRef);
            }
            AuthenticationUtil.popAuthentication();
        }
        

        
    }
    
    @Test
    public void testCheckXmlResponse() throws Exception
    {
        String testFolderRefStr = testCaseFolder.getId().toString().replace("urn:uuid:", "workspace://SpacesStore/");
        NodeRef testFolderRef = new NodeRef(testFolderRefStr);
        
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_NAME, "testcontent");
        properties.put(ContentModel.PROP_DESCRIPTION, "content - test doc for test");

        AuthenticationUtil.pushAuthentication();
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
        
        NodeRef testDocNodeRef = null;
        try
        {
            ChildAssociationRef testDoc = nodeService.createNode(testFolderRef, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testcontent"), ContentModel.TYPE_CONTENT, properties);
            testDocNodeRef = testDoc.getChildRef();
            properties.clear();
            properties.put(ApplicationModel.PROP_EDITINLINE, true);
            nodeService.addAspect(testDocNodeRef, ApplicationModel.ASPECT_INLINEEDITABLE, properties);
            
            String id = testDocNodeRef.toString().replace("workspace://SpacesStore/", "workspace:SpacesStore/i/");
            Request get = new GetRequest("/cmis/s/" + id);
            Response res = sendRequest(get, 200);
            String xml = res.getContentAsString();
            
            try
            {
                validateXmlResponse(xml);
            }
            catch (Exception e)
            {
                fail(e.getMessage());
            }

        }
        finally
        {
            if (testDocNodeRef != null)
            {
                nodeService.deleteNode(testDocNodeRef);
            }
            AuthenticationUtil.popAuthentication();
        }
    }
    
    private void validateXmlResponse(String xml) throws Exception
    {
        Source[] shemas = new Source[] { 
                new StreamSource(new StringReader(localTemplates.load("xml.xsd"))),
                new StreamSource(new StringReader(localTemplates.load("atom.xsd"))),
                new StreamSource(new StringReader(localTemplates.load("app.xsd"))),
                new StreamSource(new StringReader(localTemplates.load("CMIS-Core.xsd"))), 
                new StreamSource(new StringReader(localTemplates.load("envelope.xsd"))), 
                new StreamSource(new StringReader(localTemplates.load("Alfresco-Core.xsd")))
                };
        
        Schema schema = null;
        String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
        SchemaFactory factory = SchemaFactory.newInstance(language);
        schema = factory.newSchema(shemas);

        // creating a Validator instance
        Validator validator = schema.newValidator();

        // preparing the XML file as a SAX source
        SAXSource source = new SAXSource(new InputSource(new StringReader(xml)));

        // validating the SAX source against the schema
        validator.validate(source);
    }
}