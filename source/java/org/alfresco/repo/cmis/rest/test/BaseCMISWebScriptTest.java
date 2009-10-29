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
package org.alfresco.repo.cmis.rest.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Validator;

import junit.framework.Test;

import org.alfresco.abdera.ext.cmis.CMISCapabilities;
import org.alfresco.abdera.ext.cmis.CMISConstants;
import org.alfresco.abdera.ext.cmis.CMISExtensionFactory;
import org.alfresco.abdera.ext.cmis.CMISObject;
import org.alfresco.abdera.ext.cmis.CMISRepositoryInfo;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cmis.rest.xsd.CMISValidator;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Base64;
import org.alfresco.web.scripts.Format;
import org.alfresco.web.scripts.TestWebScriptServer.GetRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PostRequest;
import org.alfresco.web.scripts.TestWebScriptServer.Request;
import org.alfresco.web.scripts.TestWebScriptServer.Response;
import org.alfresco.web.scripts.atom.AbderaService;
import org.alfresco.web.scripts.atom.AbderaServiceImpl;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.model.Service;
import org.apache.abdera.model.Workspace;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Base CMIS Web Script Test
 * 
 * @author davidc
 */
public class BaseCMISWebScriptTest extends BaseWebScriptTest
{
    // Repository Access
    private String serviceUrl = "http://localhost:8080/alfresco/service/api/repository";

    // validation support
    private CMISValidator cmisValidator = new CMISValidator();
    private boolean validateResponse = true;
    
    // cached responses
    private AbderaService abdera;
    private Service service = null;
    private String queryCapability = null;
    private Entry testRootFolder = null;
    private Entry testRunFolder = null;
    
    
    /**
     * Sets the Repository Service URL
     * 
     * @param serviceUrl  serviceURL
     */
    public void setServiceUrl(String serviceUrl)
    {
        this.serviceUrl = serviceUrl;
    }
    
    /**
     * Validate Response
     * 
     * @param validateResponse
     */
    protected void setValidateResponse(boolean validateResponse)
    {
        this.validateResponse = validateResponse;
    }

    
    @Override
    protected void setUp()
        throws Exception
    {
        // setup client atom support
        AbderaServiceImpl abderaImpl = new AbderaServiceImpl();
        abderaImpl.afterPropertiesSet();
        abderaImpl.registerExtensionFactory(new CMISExtensionFactory());
        abdera = abderaImpl;

        super.setUp();
    }

    
    /**
     * Gets the Abdera Service
     * 
     * @return  abdera service
     */
    protected AbderaService getAbdera()
    {
        return abdera;
    }
    
    /**
     * Gets CMIS Validator
     * 
     * @return  CMIS Validator
     */
    protected CMISValidator getCMISValidator()
    {
        return cmisValidator;
    }
    
    /**
     * Gets CMIS App Validator
     * 
     * @return  CMIS App Validator
     * 
     * @throws SAXException 
     * @throws IOException 
     */
    protected Validator getAppValidator()
        throws IOException, SAXException
    {
        return getCMISValidator().getAppValidator();
    }

    /**
     * Gets CMIS Atom Validator
     * 
     * @return  CMIS App Validator
     * 
     * @throws SAXException 
     * @throws IOException 
     */
    protected Validator getAtomValidator()
        throws IOException, SAXException
    {
        return getCMISValidator().getCMISAtomValidator();
    }

    /**
     * Asserts XML complies with specified Validator
     * 
     * @param xml  xml to assert
     * @param validator  validator to assert with
     * @throws IOException
     * @throws ParserConfigurationException
     */
    protected void assertValidXML(String xml, Validator validator)
        throws IOException, ParserConfigurationException
    {
        if (validateResponse)
        {
            try
            {
                Document document = cmisValidator.getDocumentBuilder().parse(new InputSource(new StringReader(xml)));
                validator.validate(new DOMSource(document));
            }
            catch (SAXException e)
            {
                fail(cmisValidator.toString(e, xml));
            }
        }
    }
     
    /**
     * Load text from file specified by class path
     * 
     * @param classPath  XML file
     * @return  XML
     * @throws IOException
     */
    protected String loadString(String classPath)
        throws IOException
    {
        InputStream input = getClass().getResourceAsStream(classPath);
        if (input == null)
        {
            throw new IOException(classPath + " not found.");
        }

        InputStreamReader reader = new InputStreamReader(input, "UTF-8");
        StringWriter writer = new StringWriter();

        try
        {
            char[] buffer = new char[4096];
            int bytesRead = -1;
            while ((bytesRead = reader.read(buffer)) != -1)
            {
                writer.write(buffer, 0, bytesRead);
            }
            writer.flush();
        }
        finally
        {
            reader.close();
            writer.close();
        }
        
        return writer.toString();
    }

    /**
     * Send Request to Test Web Script Server (as admin)
     * 
     * @param req
     * @param expectedStatus
     * @return response
     * @throws IOException
     */
    protected Response sendRequest(Request req, int expectedStatus, Validator responseValidator)
        throws IOException
    {
        return sendRequest(req, expectedStatus, responseValidator, null);
    }
    
    /**
     * Send Request
     * 
     * @param req
     * @param expectedStatus
     * @param asUser
     * @return response
     * @throws IOException
     */
    protected Response sendRequest(Request req, int expectedStatus, Validator responseValidator, String asUser)
        throws IOException
    {
        Response res = sendRequest(req, expectedStatus, asUser);
        if (responseValidator != null)
        {
            try
            {
                // Validate response according to validator
                String resXML = res.getContentAsString();
                assertValidXML(resXML, responseValidator);
            }
            catch (ParserConfigurationException e)
            {
                throw new AlfrescoRuntimeException("Failed to validate", e);
            }
        }
        return res;
    }
    
    /**
     * Default Test Listener
     */
    public static class CMISTestListener extends BaseWebScriptTestListener implements WebScriptTestListener
    {
        /**
         * Construct
         * 
         * @param writer
         */
        public CMISTestListener(PrintStream writer)
        {
            super(writer);
        }

        /* (non-Javadoc)
         * @see junit.textui.ResultPrinter#startTest(junit.framework.Test)
         */
        @Override
        public void startTest(Test test)
        {
            BaseCMISWebScriptTest cmisTest = (BaseCMISWebScriptTest)test;
            getWriter().println();
            getWriter().println("*** Test started: " + cmisTest.getName() + " (remote: " + (cmisTest.getRemoteServer() != null) + ")");
        }
    }
    
    
    //
    // General Abdera Helpers
    //
    protected Entry getEntry(IRI href)
        throws Exception
    {
        return getEntry(href, null);
    }

    protected Entry getEntry(IRI href, Map<String, String> args)
        throws Exception
    {
        Request get = new GetRequest(href.toString()).setArgs(args);
        Response res = sendRequest(get, 200, getAtomValidator());
        assertNotNull(res);
        String xml = res.getContentAsString();
        Entry entry = abdera.parseEntry(new StringReader(xml), null);
        assertNotNull(entry);
        // TODO: fix up self links with arguments
        if (args == null)
        {
            assertEquals(get.getFullUri(), entry.getSelfLink().getHref().toString());
        }
        return entry;
    }
    
    protected Feed getFeed(IRI href)
        throws Exception
    {
        return getFeed(href, null);
    }
    
    protected Feed getFeed(IRI href, Map<String, String> args)
        throws Exception
    {
        Request get = new GetRequest(href.toString()).setArgs(args);
        Response res = sendRequest(get, 200, getAtomValidator());
        assertNotNull(res);
        String xml = res.getContentAsString();
        Feed feed = abdera.parseFeed(new StringReader(xml), null);
        assertNotNull(feed);
        assertEquals(get.getFullUri(), feed.getSelfLink().getHref().toString());
        return feed;
    }

    //
    // General CMIS Helpers
    //
    
    protected Service getRepository()
        throws Exception
    {
        if (service == null)
        {
            Response res = sendRequest(new GetRequest(serviceUrl), 200, getAppValidator());
            String xml = res.getContentAsString();
            assertNotNull(xml);
            assertTrue(xml.length() > 0);
            service = abdera.parseService(new StringReader(xml), null);
            assertNotNull(service);
        }
        return service;
    }
    
    protected String getQueryCapability()
        throws Exception
    {
        if (queryCapability == null)
        {
            Service repo = getRepository();
            Workspace workspace = getWorkspace(repo);
            CMISRepositoryInfo repoInfo = workspace.getExtension(CMISConstants.REPOSITORY_INFO);
            assertNotNull(repoInfo);
            CMISCapabilities capabilities = repoInfo.getCapabilities();
            assertNotNull(repoInfo);
            queryCapability = capabilities.getQuery();
            assertNotNull(queryCapability);
        }
        return queryCapability;
    }
    
    protected Workspace getWorkspace(Service service)
    {
        return service.getWorkspaces().get(0);
    }
    
    protected Collection getCMISCollection(Workspace workspace, String collectionId)
    {
        List<Collection> collections = workspace.getCollections();
        for (Collection collection : collections)
        {
            String id = collection.getAttributeValue(CMISConstants.COLLECTION_TYPE);
            if (id != null && id.equals(collectionId))
            {
                return collection;
            }
        }
        return null;
    }
    
    protected IRI getRootCollection(Workspace workspace)
    {
        Collection root = getCMISCollection(workspace, CMISConstants.COLLECTION_ROOT);
        assertNotNull(root);
        IRI rootHREF = root.getHref();
        assertNotNull(rootHREF);
        return rootHREF;
    }

    protected IRI getCheckedOutCollection(Workspace workspace)
    {
        Collection root = getCMISCollection(workspace, CMISConstants.COLLECTION_CHECKEDOUT);
        assertNotNull(root);
        IRI rootHREF = root.getHref();
        assertNotNull(rootHREF);
        return rootHREF;
    }

    protected IRI getTypesChildrenCollection(Workspace workspace)
    {
        Collection root = getCMISCollection(workspace, CMISConstants.COLLECTION_TYPES);
        assertNotNull(root);
        IRI rootHREF = root.getHref();
        assertNotNull(rootHREF);
        return rootHREF;
    }

    protected IRI getQueryCollection(Workspace workspace)
    {
        Collection root = getCMISCollection(workspace, CMISConstants.COLLECTION_QUERY);
        assertNotNull(root);
        IRI rootHREF = root.getHref();
        assertNotNull(rootHREF);
        return rootHREF;
    }

    protected Link getLink(Entry entry, String rel, String... matchesMimetypes)
    {
        List<Link> links = entry.getLinks(rel);
        if (links != null)
        {
            for (Link link : links)
            {
                MimeType mimetype = link.getMimeType();
                if (matchesMimetypes.length == 0)
                {
                    if (links.size() == 1)
                    {
                        // take the single link regardless of type
                        return link;
                    }
                    else if (mimetype == null)
                    {
                        // take the link if it doesn't have a type
                        return link;
                    }
                }
                for (String matchesMimetype : matchesMimetypes)
                {
                    try
                    {
                        if (mimetype != null && mimetype.match(matchesMimetype))
                        {
                            return link;
                        }
                    }
                    catch (MimeTypeParseException e)
                    {
                        // note: not a match
                    }
                }
            }
        }
        return null;
    }

    protected Link getChildrenLink(Entry entry)
    {
        return getLink(entry, CMISConstants.REL_DOWN, CMISConstants.MIMETYPE_FEED);
    }

    protected Link getDescendantsLink(Entry entry)
    {
        return getLink(entry, CMISConstants.REL_DOWN, CMISConstants.MIMETYPE_CMISTREE);
    }

    protected Link getFolderTreeLink(Entry entry)
    {
        return getLink(entry, CMISConstants.REL_FOLDER_TREE, CMISConstants.MIMETYPE_CMISTREE);
    }

    protected Link getObjectParentsLink(Entry entry)
    {
        return getLink(entry, CMISConstants.REL_UP, CMISConstants.MIMETYPE_FEED);
    }

    protected Link getFolderParentLink(Entry entry)
    {
        return getLink(entry, CMISConstants.REL_UP, CMISConstants.MIMETYPE_ENTRY);
    }

    protected Entry createFolder(IRI parent, String name)
        throws Exception
    {
        return createFolder(parent, name, "/org/alfresco/repo/cmis/rest/test/createfolder.atomentry.xml");
    }
    
    protected Entry createFolder(IRI parent, String name, String atomEntryFile)
        throws Exception
    {
        String createFolder = loadString(atomEntryFile);
        createFolder = createFolder.replace("${NAME}", name);
        Response res = sendRequest(new PostRequest(parent.toString(), createFolder, Format.ATOMENTRY.mimetype()), 201, getAtomValidator());
        assertNotNull(res);
        String xml = res.getContentAsString();
        Entry entry = abdera.parseEntry(new StringReader(xml), null);
        assertNotNull(entry);
        assertEquals(name, entry.getTitle());
        //assertEquals(name + " (summary)", entry.getSummary());
        CMISObject object = entry.getExtension(CMISConstants.OBJECT);
        assertEquals(CMISConstants.TYPE_FOLDER, object.getBaseTypeId().getStringValue());
        String testFolderHREF = (String)res.getHeader("Location");
        assertNotNull(testFolderHREF);
        return entry;
    }
    
    protected Entry createDocument(IRI parent, String name)
        throws Exception
    {
        return createDocument(parent, name, "/org/alfresco/repo/cmis/rest/test/createdocument.atomentry.xml");
    }
    
    protected Entry createDocument(IRI parent, String name, String atomEntryFile)
        throws Exception
    {
        String createFile = loadString(atomEntryFile);
        createFile = createFile.replace("${NAME}", name);
        // determine if creating content via mediatype
        Entry createEntry = abdera.parseEntry(new StringReader(createFile), null);
        MimeType mimeType = createEntry.getContentMimeType();
        boolean mediaType = (mimeType != null);
        createFile = createFile.replace("${CONTENT}", mediaType ? Base64.encodeBytes(name.getBytes()) : name);
        Response res = sendRequest(new PostRequest(parent.toString(), createFile, Format.ATOMENTRY.mimetype()), 201, getAtomValidator());
        assertNotNull(res);
        String xml = res.getContentAsString();
        Entry entry = abdera.parseEntry(new StringReader(xml), null);
        assertNotNull(entry);
        assertEquals(name, entry.getTitle());
        //assertEquals(name + " (summary)", entry.getSummary());
        assertNotNull(entry.getContentSrc());
        CMISObject object = entry.getExtension(CMISConstants.OBJECT);
        assertEquals(CMISConstants.TYPE_DOCUMENT, object.getBaseTypeId().getStringValue());
        String testFileHREF = (String)res.getHeader("Location");
        assertNotNull(testFileHREF);
        return entry;
    }

    protected Entry createRelationship(IRI parent, String type, String targetId)
        throws Exception
    {
        return createRelationship(parent, type, targetId, "/org/alfresco/repo/cmis/rest/test/createrelationship.atomentry.xml");
    }

    protected Entry createRelationship(IRI parent, String type, String targetId, String atomEntryFile)
        throws Exception
    {
        String createFile = loadString(atomEntryFile);
        createFile = createFile.replace("${RELTYPE}", type);
        createFile = createFile.replace("${TARGETID}", targetId);
        Response res = sendRequest(new PostRequest(parent.toString(), createFile, Format.ATOMENTRY.mimetype()), 201, getAtomValidator());
        assertNotNull(res);
        String xml = res.getContentAsString();
        Entry entry = abdera.parseEntry(new StringReader(xml), null);
        assertNotNull(entry);
        CMISObject object = entry.getExtension(CMISConstants.OBJECT);
        assertEquals(CMISConstants.TYPE_RELATIONSHIP, object.getBaseTypeId().getStringValue());
        assertEquals(targetId, object.getTargetId().getStringValue());
        String testFileHREF = (String)res.getHeader("Location");
        assertNotNull(testFileHREF);
        return entry;
    }
    
    //
    // General Test Helpers
    //

    protected Entry getTestRootFolder()
        throws Exception
    {
        if (testRootFolder == null)
        {
            testRootFolder = createTestRootFolder();
        }
        return testRootFolder;
    }
    
    protected Entry createTestRootFolder()
        throws Exception
    {
        Service service = getRepository();
        IRI rootFolderHREF = getRootCollection(getWorkspace(service));
        
        // TODO: Convert to query
        Feed children = getFeed(rootFolderHREF);
        for (Entry child : children.getEntries())
        {
            if (child.getTitle().equals("CMIS Tests"))
            {
                return child;
            }
        }
        
        // not found, create it
        return createFolder(rootFolderHREF, "CMIS Tests");
    }
    
    protected Entry getTestRunFolder()
        throws Exception
    {
        if (testRunFolder == null)
        {
            testRunFolder = createTestRunFolder();
        }
        return testRunFolder;
    }
    
    protected Entry createTestRunFolder()
        throws Exception
    {
        Entry testRootFolder = getTestRootFolder();
        Link testsChildrenLink = getChildrenLink(testRootFolder);
        return createFolder(testsChildrenLink.getHref(), "Test Run " + System.currentTimeMillis());
    }
    
    protected Entry createTestFolder(String name)
        throws Exception
    {
        Entry testRunFolder = getTestRunFolder();
        Link childrenLink = getChildrenLink(testRunFolder);
        assertNotNull(childrenLink);
        Entry testFolder = createFolder(childrenLink.getHref(), name + " " + System.currentTimeMillis());
        return testFolder;
    }
    
    //
    // Alfresco specific helpers
    //
    
    protected NodeRef getNodeRef(Entry entry)
    {
        NodeRef nodeRef = null;
        CMISObject object = entry.getExtension(CMISConstants.OBJECT);
        if (object != null)
        {
            String strNodeRef = object.getObjectId().getStringValue();
            if (strNodeRef != null)
            {
                nodeRef = new NodeRef(strNodeRef);
            }
        }
        return nodeRef;
    }

}
