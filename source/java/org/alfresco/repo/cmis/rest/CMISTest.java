/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.rest;

import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.util.GUID;
import org.alfresco.web.scripts.Format;
import org.alfresco.web.scripts.TestWebScriptServer.DeleteRequest;
import org.alfresco.web.scripts.TestWebScriptServer.GetRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PostRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PutRequest;
import org.alfresco.web.scripts.TestWebScriptServer.Request;
import org.alfresco.web.scripts.TestWebScriptServer.Response;
import org.alfresco.web.scripts.atom.AbderaService;
import org.alfresco.web.scripts.atom.AbderaServiceImpl;
import org.apache.abdera.ext.cmis.CMISConstants;
import org.apache.abdera.ext.cmis.CMISExtensionFactory;
import org.apache.abdera.ext.cmis.CMISProperties;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.model.Service;


/**
 * CMIS API Test Harness
 * 
 * @author davidc
 */
public class CMISTest extends BaseCMISWebScriptTest
{
    private AbderaService abdera;
    
    // test context
    private String repositoryUrl = "http://localhost:8080/alfresco/service/api/repository";
    
    // cached responses
    private static Service service = null;
    private static Entry testsFolder = null;
    private static Entry testRunFolder = null;

    
    /**
     * Sets the Repository "service" URL
     * 
     * @param repositoryUrl  repository service url
     */
    public void setRepositoryUrl(String repositoryUrl)
    {
        this.repositoryUrl = repositoryUrl;
    }
    
    
    // TODO: checkout/checkin tests need to perform version property assertions
    
    
    @Override
    protected void setUp()
        throws Exception
    {
        // setup client atom support
        AbderaServiceImpl abderaImpl = new AbderaServiceImpl();
        abderaImpl.afterPropertiesSet();
        abderaImpl.registerExtensionFactory(new CMISExtensionFactory());
        abdera = abderaImpl;

        // setup user
        setDefaultRunAs("admin");
        
        super.setUp();
    }

    private Service getRepository()
        throws Exception
    {
        if (service == null)
        {
            Response res = sendRequest(new GetRequest(repositoryUrl), 200, getAppValidator());
            String xml = res.getContentAsString();
            assertNotNull(xml);
            assertTrue(xml.length() > 0);
            //assertValidXML(xml, getCMISValidator().getAppValidator());
            
            service = abdera.parseService(new StringReader(xml), null);
            assertNotNull(service);
        }
        return service;
    }
    
    private IRI getRootCollection(Service service)
    {
        Collection root = service.getCollection("Main Repository", "root collection");
        assertNotNull(root);
        IRI rootHREF = root.getHref();
        assertNotNull(rootHREF);
        return rootHREF;
    }

    private IRI getCheckedOutCollection(Service service)
    {
        Collection root = service.getCollection("Main Repository", "checkedout collection");
        assertNotNull(root);
        IRI rootHREF = root.getHref();
        assertNotNull(rootHREF);
        return rootHREF;
    }

    private IRI getTypesCollection(Service service)
    {
        Collection root = service.getCollection("Main Repository", "type collection");
        assertNotNull(root);
        IRI rootHREF = root.getHref();
        assertNotNull(rootHREF);
        return rootHREF;
    }

    private Entry createFolder(IRI parent, String name)
        throws Exception
    {
        return createFolder(parent, name, "/cmis/rest/createfolder.atomentry.xml");
    }

    private Entry createFolder(IRI parent, String name, String atomEntryFile)
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
        assertEquals(name + " (summary)", entry.getSummary());
        CMISProperties props = entry.getExtension(CMISConstants.PROPERTIES);
        assertEquals("folder", props.getBaseType());
        String testFolderHREF = (String)res.getHeader("Location");
        assertNotNull(testFolderHREF);
        return entry;
    }
    
    private Entry createDocument(IRI parent, String name)
        throws Exception
    {
        return createDocument(parent, name, "/cmis/rest/createdocument.atomentry.xml");
    }

    private Entry createDocument(IRI parent, String name, String atomEntryFile)
        throws Exception
    {
        String createFile = loadString(atomEntryFile);
        name = name + " " + System.currentTimeMillis();
        createFile = createFile.replace("${NAME}", name);
        Response res = sendRequest(new PostRequest(parent.toString(), createFile, Format.ATOMENTRY.mimetype()), 201, getAtomValidator());
        assertNotNull(res);
        String xml = res.getContentAsString();
        Entry entry = abdera.parseEntry(new StringReader(xml), null);
        assertNotNull(entry);
        assertEquals(name, entry.getTitle());
        assertEquals(name + " (summary)", entry.getSummary());
        assertNotNull(entry.getContentSrc());
        CMISProperties props = entry.getExtension(CMISConstants.PROPERTIES);
        assertEquals("document", props.getBaseType());
        String testFileHREF = (String)res.getHeader("Location");
        assertNotNull(testFileHREF);
        return entry;
    }

    private Entry createTestsFolder(IRI rootFolder)
        throws Exception
    {
        // TODO: Convert to query
        Feed children = getFeed(rootFolder);
        for (Entry child : children.getEntries())
        {
            if (child.getTitle().equals("CMIS Tests"))
            {
                return child;
            }
        }
        
        // not found, create it
        return createFolder(rootFolder, "CMIS Tests");
    }
    
    private Entry createTestFolder(String name)
        throws Exception
    {
        if (testRunFolder == null)
        {
            Service service = getRepository();
            IRI rootFolderHREF = getRootCollection(service);
            testsFolder = createTestsFolder(rootFolderHREF);
            Link testsChildrenLink = testsFolder.getLink(CMISConstants.REL_CHILDREN);
            testRunFolder = createFolder(testsChildrenLink.getHref(), "Test Run " + System.currentTimeMillis());
        }
        Link childrenLink = testRunFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Entry testFolder = createFolder(childrenLink.getHref(), name + " " + System.currentTimeMillis());
        return testFolder;
    }

    private Entry getEntry(IRI href)
        throws Exception
    {
        return getEntry(href, null);
    }

    private Entry getEntry(IRI href, Map<String, String> args)
        throws Exception
    {
        Request get = new GetRequest(href.toString()).setArgs(args);
        Response res = sendRequest(get, 200, getAtomValidator());
        assertNotNull(res);
        String xml = res.getContentAsString();
        Entry entry = abdera.parseEntry(new StringReader(xml), null);
        assertNotNull(entry);
        assertEquals(getArgsAsHeaders() ? get.getUri() : get.getFullUri(), entry.getSelfLink().getHref().toString());
        return entry;
    }

    private Feed getFeed(IRI href)
        throws Exception
    {
        return getFeed(href, null);
    }
    
    private Feed getFeed(IRI href, Map<String, String> args)
        throws Exception
    {
        Request get = new GetRequest(href.toString()).setArgs(args);
        Response res = sendRequest(get, 200, getAtomValidator());
        assertNotNull(res);
        String xml = res.getContentAsString();
        Feed feed = abdera.parseFeed(new StringReader(xml), null);
        assertNotNull(feed);
        assertEquals(getArgsAsHeaders() ? get.getUri() : get.getFullUri(), feed.getSelfLink().getHref().toString());
        return feed;
    }
    
    public void testRepository()
        throws Exception
    {
        IRI rootHREF = getRootCollection(getRepository());
        sendRequest(new GetRequest(rootHREF.toString()), 200, getAtomValidator());
    }
    
    public void testCreateDocument()
        throws Exception
    {
        Entry testFolder = createTestFolder("testCreateDocument");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Feed children = getFeed(childrenLink.getHref());
        assertNotNull(children);
        int entriesBefore = children.getEntries().size();
        Entry document = createDocument(children.getSelfLink().getHref(), "testCreateDocument");
        Response documentContentRes = sendRequest(new GetRequest(document.getContentSrc().toString()), 200);
        String resContent = documentContentRes.getContentAsString();
        assertEquals("test content " + document.getTitle(), resContent);
        Feed feedFolderAfter = getFeed(childrenLink.getHref());
        int entriesAfter = feedFolderAfter.getEntries().size();
        assertEquals(entriesBefore +1, entriesAfter);
        Entry entry = feedFolderAfter.getEntry(document.getId().toString());
        assertNotNull(entry);
    }
    
    public void testCreateDocumentBase64()
        throws Exception
    {
        Entry testFolder = createTestFolder("testCreateDocumentBase64");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Feed children = getFeed(childrenLink.getHref());
        assertNotNull(children);
        int entriesBefore = children.getEntries().size();
        Entry document = createDocument(children.getSelfLink().getHref(), "testCreateDocument", "/cmis/rest/createdocumentBase64.atomentry.xml");
        Response documentContentRes = sendRequest(new GetRequest(document.getContentSrc().toString()), 200);
        String testContent = loadString("/cmis/rest/createdocumentBase64.txt");
        String resContent = documentContentRes.getContentAsString();
        assertEquals(testContent, resContent);
        Feed feedFolderAfter = getFeed(childrenLink.getHref());
        int entriesAfter = feedFolderAfter.getEntries().size();
        assertEquals(entriesBefore +1, entriesAfter);
        Entry entry = feedFolderAfter.getEntry(document.getId().toString());
        assertNotNull(entry);
    }
    
    public void testCreateFolder()
        throws Exception
    {
        Entry testFolder = createTestFolder("testCreateFolder");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Feed children = getFeed(childrenLink.getHref());
        assertNotNull(children);
        int entriesBefore = children.getEntries().size();
        Entry folder = createFolder(children.getSelfLink().getHref(), "testCreateFolder");
        Feed feedFolderAfter = getFeed(childrenLink.getHref());
        int entriesAfter = feedFolderAfter.getEntries().size();
        assertEquals(entriesBefore +1, entriesAfter);
        Entry entry = feedFolderAfter.getEntry(folder.getId().toString());
        assertNotNull(entry);
    }
    
    public void testGet()
        throws Exception
    {
        // get folder
        Entry testFolder = createTestFolder("testGet");
        assertNotNull(testFolder);
        Entry testFolderFromGet = getEntry(testFolder.getSelfLink().getHref());
        assertEquals(testFolder.getId(), testFolderFromGet.getId());
        assertEquals(testFolder.getTitle(), testFolderFromGet.getTitle());
        assertEquals(testFolder.getSummary(), testFolderFromGet.getSummary());
        
        // get document
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Entry testDocument = createDocument(childrenLink.getHref(), "testGet");
        assertNotNull(testDocument);
        Entry testDocumentFromGet = getEntry(testDocument.getSelfLink().getHref());
        assertEquals(testDocument.getId(), testDocumentFromGet.getId());
        assertEquals(testDocument.getTitle(), testDocumentFromGet.getTitle());
        assertEquals(testDocument.getSummary(), testDocumentFromGet.getSummary());
        
        // get something that doesn't exist
        Response res = sendRequest(new GetRequest(testDocument.getSelfLink().getHref().toString() + GUID.generate()), 404);
        assertNotNull(res);
    }

    public void testChildren()
        throws Exception
    {
        // create multiple children
        Entry testFolder = createTestFolder("testChildren");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Entry document1 = createDocument(childrenLink.getHref(), "testChildren1");
        assertNotNull(document1);
        Entry document2 = createDocument(childrenLink.getHref(), "testChildren2");
        assertNotNull(document2);
        Entry document3 = createDocument(childrenLink.getHref(), "testChildren3");
        assertNotNull(document3);
        
        // checkout one of the children to ensure private working copy isn't included
        Response documentRes = sendRequest(new GetRequest(document2.getSelfLink().getHref().toString()), 200, getAtomValidator());
        assertNotNull(documentRes);
        String documentXML = documentRes.getContentAsString();
        assertNotNull(documentXML);
        IRI checkedoutHREF = getCheckedOutCollection(getRepository());
        Response pwcRes = sendRequest(new PostRequest(checkedoutHREF.toString(), documentXML, Format.ATOMENTRY.mimetype()), 201, getAtomValidator());
        assertNotNull(pwcRes);
        Entry pwc = abdera.parseEntry(new StringReader(pwcRes.getContentAsString()), null);
        
        // get children, ensure they exist (but not private working copy)
        Feed children = getFeed(childrenLink.getHref());
        assertNotNull(children);
        assertEquals(3, children.getEntries().size());
        assertNotNull(children.getEntry(document1.getId().toString()));
        assertNotNull(children.getEntry(document2.getId().toString()));
        assertNotNull(children.getEntry(document3.getId().toString()));
        assertNull(children.getEntry(pwc.getId().toString()));
    }

    public void testChildrenPaging()
        throws Exception
    {
        // create multiple children
        Set<IRI> docIds = new HashSet<IRI>();
        Entry testFolder = createTestFolder("testChildrenPaging");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        for (int i = 0; i < 15; i++)
        {
            Entry document = createDocument(childrenLink.getHref(), "testChildrenPaging" + i);
            assertNotNull(document);
            docIds.add(document.getId());
        }
        assertEquals(15, docIds.size());
        
        // get children, ensure they exist (but not private working copy)
        int nextCount = 0;
        Map<String, String> args = new HashMap<String, String>();
        args.put("maxItems", "4");
        IRI childrenHREF = childrenLink.getHref();
        while (childrenHREF != null)
        {
            nextCount++;
            Feed types = getFeed(childrenHREF, args);
            assertNotNull(types);
            assertEquals(nextCount < 4 ? 4 : 3, types.getEntries().size());
            for (Entry entry : types.getEntries())
            {
                docIds.remove(entry.getId());
            }
            
            // next page
            Link nextLink = types.getLink("next");
            childrenHREF = (nextLink != null) ? nextLink.getHref() : null;
            args = null;
        };
        assertEquals(4, nextCount);
        assertEquals(0, docIds.size());
    }
    
    public void testGetChildrenTypeFilter()
        throws Exception
    {
        // create multiple children
        Entry testFolder = createTestFolder("testChildrenTypeFilter");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Entry document = createDocument(childrenLink.getHref(), "testChildren1");
        assertNotNull(document);
        Entry folder = createFolder(childrenLink.getHref(), "testChildren2");
        assertNotNull(folder);
        
        // invalid type filter
        Map<String, String> args = new HashMap<String, String>();
        args.put("types", "Invalid");
        Response invalidRes = sendRequest(new GetRequest(childrenLink.getHref().toString()).setArgs(args), 400);
        assertNotNull(invalidRes);

        // no filter
        Feed noFilters = getFeed(childrenLink.getHref());
        assertNotNull(noFilters);
        assertEquals(2, noFilters.getEntries().size());

        // any filter
        args.put("types", "Any");
        Feed any = getFeed(childrenLink.getHref(), args);
        assertNotNull(any);
        assertEquals(2, any.getEntries().size());

        // folders filter
        args.put("types", "Folders");
        Feed folders = getFeed(childrenLink.getHref(), args);
        assertNotNull(folders);
        assertEquals(1, folders.getEntries().size());
        assertNotNull(folders.getEntry(folder.getId().toString()));

        // documents filter
        args.put("types", "Documents");
        Feed documents = getFeed(childrenLink.getHref(), args);
        assertNotNull(documents);
        assertEquals(1, documents.getEntries().size());
        assertNotNull(documents.getEntry(document.getId().toString()));
    }

    public void testGetParent()
        throws Exception
    {
        Entry testFolder = createTestFolder("testParent");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Entry childFolder = createFolder(childrenLink.getHref(), "testParentChild");
        assertNotNull(childFolder);
        Link parentLink = childFolder.getLink(CMISConstants.REL_FOLDERPARENT);
        assertNotNull(parentLink);

        // ensure there is parent 'testParent'
        Feed parent = getFeed(parentLink.getHref());
        assertNotNull(parent);
        assertEquals(1, parent.getEntries().size());
        assertEquals(testFolder.getId(), parent.getEntries().get(0).getId());

        // ensure there are ancestors 'testParent', "test run folder", "tests folder" and "root folder"
        Map<String, String> args = new HashMap<String, String>();
        args.put("returnToRoot", "true");
        Feed parentsToRoot = getFeed(new IRI(parentLink.getHref().toString()), args);
        assertNotNull(parentsToRoot);
        assertEquals(4, parentsToRoot.getEntries().size());
        assertEquals(testFolder.getId(), parentsToRoot.getEntries().get(0).getId());
        assertNotNull(parentsToRoot.getEntries().get(0).getLink(CMISConstants.REL_PARENT));
        assertEquals(testRunFolder.getId(), parentsToRoot.getEntries().get(1).getId());
        assertNotNull(parentsToRoot.getEntries().get(1).getLink(CMISConstants.REL_PARENT));
        assertEquals(testsFolder.getId(), parentsToRoot.getEntries().get(2).getId());
        assertNotNull(parentsToRoot.getEntries().get(2).getLink(CMISConstants.REL_PARENT));
        Feed root = getFeed(getRootCollection(getRepository()));
        assertEquals(root.getId(), parentsToRoot.getEntries().get(3).getId());
        assertNull(parentsToRoot.getEntries().get(3).getLink(CMISConstants.REL_PARENT));
    }

    public void testGetParents()
        throws Exception
    {
        Entry testFolder = createTestFolder("testParents");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Entry childDocs = createDocument(childrenLink.getHref(), "testParentsChild");
        assertNotNull(childDocs);
        Link parentLink = childDocs.getLink(CMISConstants.REL_PARENTS);
        assertNotNull(parentLink);
        
        // ensure there is parent 'testParent'
        Feed parent = getFeed(parentLink.getHref());
        assertNotNull(parent);
        assertEquals(1, parent.getEntries().size());
        assertEquals(testFolder.getId(), parent.getEntries().get(0).getId());

        // ensure there are ancestors 'testParent', "test run folder" and "root folder"
        Map<String, String> args = new HashMap<String, String>();
        args.put("returnToRoot", "true");
        Feed parentsToRoot = getFeed(new IRI(parentLink.getHref().toString()), args);
        assertNotNull(parentsToRoot);
        assertEquals(4, parentsToRoot.getEntries().size());
        assertEquals(testFolder.getId(), parentsToRoot.getEntries().get(0).getId());
        assertNotNull(parentsToRoot.getEntries().get(0).getLink(CMISConstants.REL_PARENT));
        assertEquals(testRunFolder.getId(), parentsToRoot.getEntries().get(1).getId());
        assertNotNull(parentsToRoot.getEntries().get(1).getLink(CMISConstants.REL_PARENT));
        assertEquals(testsFolder.getId(), parentsToRoot.getEntries().get(2).getId());
        assertNotNull(parentsToRoot.getEntries().get(2).getLink(CMISConstants.REL_PARENT));
        Feed root = getFeed(getRootCollection(getRepository()));
        assertEquals(root.getId(), parentsToRoot.getEntries().get(3).getId());
        assertNull(parentsToRoot.getEntries().get(3).getLink(CMISConstants.REL_PARENT));
    }
    
    public void testDelete()
        throws Exception
    {
        // retrieve test folder for deletes
        Entry testFolder = createTestFolder("testDelete");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        Feed children = getFeed(childrenLink.getHref());
        int entriesBefore = children.getEntries().size();
        
        // create document for delete
        Entry document = createDocument(childrenLink.getHref(), "testDelete");
        Response documentRes = sendRequest(new GetRequest(document.getSelfLink().getHref().toString()), 200, getAtomValidator());
        assertNotNull(documentRes);

        // ensure document has been created
        Feed children2 = getFeed(childrenLink.getHref());
        assertNotNull(children2);
        int entriesAfterCreate = children2.getEntries().size();
        assertEquals(entriesAfterCreate, entriesBefore +1);

        // delete
        Response deleteRes = sendRequest(new DeleteRequest(document.getSelfLink().getHref().toString()), 204);
        assertNotNull(deleteRes);

        // ensure document has been deleted
        Feed children3 = getFeed(childrenLink.getHref());
        assertNotNull(children3);
        int entriesAfterDelete = children3.getEntries().size();
        assertEquals(entriesBefore, entriesAfterDelete);
    }

    public void testUpdate()
        throws Exception
    {
        // retrieve test folder for update
        Entry testFolder = createTestFolder("testUpdate");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        
        // create document for update
        Entry document = createDocument(childrenLink.getHref(), "testUpdate");
        assertNotNull(document);
        assertEquals("text/html", document.getContentMimeType().toString());

        // update
        String updateFile = loadString("/cmis/rest/updatedocument.atomentry.xml");
        String guid = GUID.generate();
        updateFile = updateFile.replace("${NAME}", guid);
        Response res = sendRequest(new PutRequest(document.getSelfLink().getHref().toString(), updateFile, Format.ATOMENTRY.mimetype()), 200, getAtomValidator());
        assertNotNull(res);
        Entry updated = abdera.parseEntry(new StringReader(res.getContentAsString()), null);
        
        // ensure update occurred
        assertEquals(document.getId(), updated.getId());
        assertEquals(document.getPublished(), updated.getPublished());
        assertEquals("Updated Title " + guid, updated.getTitle());
        assertEquals("text/plain", updated.getContentMimeType().toString());
        Response contentRes = sendRequest(new GetRequest(updated.getContentSrc().toString()), 200);
        assertEquals("updated content " + guid, contentRes.getContentAsString());
    }

    public void testGetCheckedOut()
        throws Exception
    {
        // retrieve test folder for checkouts
        Entry testFolder = createTestFolder("testGetCheckedOut");
        CMISProperties props = testFolder.getExtension(CMISConstants.PROPERTIES);
        String scopeId = props.getObjectId();
        assertNotNull(scopeId);
        
        // retrieve checkouts within scope of test checkout folder
        Service repository = getRepository();
        assertNotNull(repository);
        IRI checkedoutHREF = getCheckedOutCollection(getRepository());
        Map<String, String> args = new HashMap<String, String>();
        args.put("folderId", scopeId);
        Feed checkedout = getFeed(new IRI(checkedoutHREF.toString()), args);
        assertNotNull(checkedout);
        assertEquals(0, checkedout.getEntries().size());
    }
    
    public void testCheckout()
        throws Exception
    {
        // retrieve test folder for checkouts
        Entry testFolder = createTestFolder("testCheckout");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        
        // create document for checkout
        Entry document = createDocument(childrenLink.getHref(), "testCheckout");
        CMISProperties docProps = document.getExtension(CMISConstants.PROPERTIES);
        Response documentRes = sendRequest(new GetRequest(document.getSelfLink().getHref().toString()), 200, getAtomValidator());
        assertNotNull(documentRes);
        String documentXML = documentRes.getContentAsString();
        assertNotNull(documentXML);
        
        // checkout
        IRI checkedoutHREF = getCheckedOutCollection(getRepository());
        Response pwcRes = sendRequest(new PostRequest(checkedoutHREF.toString(), documentXML, Format.ATOMENTRY.mimetype()), 201, getAtomValidator());
        assertNotNull(pwcRes);
        String pwcXml = pwcRes.getContentAsString();
        assertNotNull(pwcXml);
        Entry pwc = abdera.parseEntry(new StringReader(pwcXml), null);
        assertNotNull(pwc);
        CMISProperties pwcProps = pwc.getExtension(CMISConstants.PROPERTIES);
        assertNotNull(pwcProps);
        assertTrue(pwcProps.isVersionSeriesCheckedOut());
        assertEquals(docProps.getObjectId(), pwcProps.getVersionSeriesId());
        assertEquals(pwcProps.getObjectId(), pwcProps.getVersionSeriesCheckedOutId());
        assertNotNull(pwcProps.getVersionSeriesCheckedOutBy());

        // test getCheckedOut is updated
        CMISProperties props = testFolder.getExtension(CMISConstants.PROPERTIES);
        String scopeId = props.getObjectId();
        Map<String, String> args = new HashMap<String, String>();
        args.put("folderId", scopeId);
        Feed checkedout = getFeed(new IRI(checkedoutHREF.toString()), args);
        assertNotNull(checkedout);
        assertEquals(1, checkedout.getEntries().size());
    }
    
    public void testCancelCheckout()
        throws Exception
    {
        // retrieve test folder for checkouts
        Entry testFolder = createTestFolder("testCancelCheckout");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        
        // create document for checkout
        Entry document = createDocument(childrenLink.getHref(), "testCancelCheckout");
        Response documentRes = sendRequest(new GetRequest(document.getSelfLink().getHref().toString()), 200, getAtomValidator());
        assertNotNull(documentRes);
        String xml = documentRes.getContentAsString();
        assertNotNull(xml);
        
        // checkout
        IRI checkedoutHREF = getCheckedOutCollection(getRepository());
        Response pwcRes = sendRequest(new PostRequest(checkedoutHREF.toString(), xml, Format.ATOMENTRY.mimetype()), 201, getAtomValidator());
        assertNotNull(pwcRes);
        String pwcXml = pwcRes.getContentAsString();
        
        // test getCheckedOut is updated
        CMISProperties props = testFolder.getExtension(CMISConstants.PROPERTIES);
        String scopeId = props.getObjectId();
        Map<String, String> args = new HashMap<String, String>();
        args.put("folderId", scopeId);
        Feed checkedout = getFeed(new IRI(checkedoutHREF.toString()), args);
        assertNotNull(checkedout);
        assertEquals(1, checkedout.getEntries().size());
        
        // cancel checkout
        Entry pwc = abdera.parseEntry(new StringReader(pwcXml), null);
        assertNotNull(pwc);
        Response cancelRes = sendRequest(new DeleteRequest(pwc.getSelfLink().getHref().toString()), 204);
        assertNotNull(cancelRes);

        // test getCheckedOut is updated
        CMISProperties props2 = testFolder.getExtension(CMISConstants.PROPERTIES);
        String scopeId2 = props2.getObjectId();
        Map<String, String> args2 = new HashMap<String, String>();
        args2.put("folderId", scopeId2);
        Feed checkedout2 = getFeed(new IRI(checkedoutHREF.toString()), args2);
        assertNotNull(checkedout2);
        assertEquals(0, checkedout2.getEntries().size());
    }

    public void testCheckIn()
        throws Exception
    {
        // retrieve test folder for checkins
        Entry testFolder = createTestFolder("testCheckIn");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        
        // create document for checkout
        Entry document = createDocument(childrenLink.getHref(), "testCheckin");
        Response documentRes = sendRequest(new GetRequest(document.getSelfLink().getHref().toString()), 200, getAtomValidator());
        assertNotNull(documentRes);
        String xml = documentRes.getContentAsString();
        assertNotNull(xml);
        
        // checkout
        IRI checkedoutHREF = getCheckedOutCollection(getRepository());
        Response pwcRes = sendRequest(new PostRequest(checkedoutHREF.toString(), xml, Format.ATOMENTRY.mimetype()), 201, getAtomValidator());
        assertNotNull(pwcRes);
        Entry pwc = abdera.parseEntry(new StringReader(pwcRes.getContentAsString()), null);
        assertNotNull(pwc);
        
        // test getCheckedOut is updated
        CMISProperties props = testFolder.getExtension(CMISConstants.PROPERTIES);
        String scopeId = props.getObjectId();
        Map<String, String> args = new HashMap<String, String>();
        args.put("folderId", scopeId);
        Feed checkedout = getFeed(new IRI(checkedoutHREF.toString()), args);
        assertNotNull(checkedout);
        assertEquals(1, checkedout.getEntries().size());

        // test version properties of checked-out item
        // test checked-in version properties
        Entry checkedoutdoc = getEntry(document.getSelfLink().getHref());
        CMISProperties checkedoutdocProps = checkedoutdoc.getExtension(CMISConstants.PROPERTIES);
        assertNotNull(checkedoutdocProps);
        assertTrue(checkedoutdocProps.isVersionSeriesCheckedOut());
        assertEquals(checkedoutdocProps.getObjectId(), checkedoutdocProps.getVersionSeriesId());
        assertNotNull(checkedoutdocProps.getVersionSeriesCheckedOutId());
        assertNotNull(checkedoutdocProps.getVersionSeriesCheckedOutBy());
        
        // test update of private working copy
        String updateFile = loadString("/cmis/rest/updatedocument.atomentry.xml");
        String guid = GUID.generate();
        updateFile = updateFile.replace("${NAME}", guid);
        Response pwcUpdatedres = sendRequest(new PutRequest(pwc.getEditLink().getHref().toString(), updateFile, Format.ATOMENTRY.mimetype()), 200, getAtomValidator());
        assertNotNull(pwcUpdatedres);
        Entry updated = abdera.parseEntry(new StringReader(pwcUpdatedres.getContentAsString()), null);
        // ensure update occurred
        assertEquals(pwc.getId(), updated.getId());
        assertEquals(pwc.getPublished(), updated.getPublished());
        assertEquals("Updated Title " + guid, updated.getTitle());
        assertEquals("text/plain", updated.getContentMimeType().toString());
        Response pwcContentRes = sendRequest(new GetRequest(pwc.getContentSrc().toString()), 200);
        assertEquals("updated content " + guid, pwcContentRes.getContentAsString());
        
        // checkin
        String checkinFile = loadString("/cmis/rest/checkindocument.atomentry.xml");
        String checkinUrl = pwc.getSelfLink().getHref().toString();
        Map<String, String> args2 = new HashMap<String, String>();
        args2.put("checkinComment", guid);
        Response checkinRes = sendRequest(new PutRequest(checkinUrl, checkinFile, Format.ATOMENTRY.mimetype()).setArgs(args2), 200, getAtomValidator());
        assertNotNull(checkinRes);
        String checkinResXML = checkinRes.getContentAsString();
    
        // test getCheckedOut is updated
        CMISProperties props2 = testFolder.getExtension(CMISConstants.PROPERTIES);
        String scopeId2 = props2.getObjectId();
        Map<String, String> args3 = new HashMap<String, String>();
        args3.put("folderId", scopeId2);
        Feed checkedout2 = getFeed(new IRI(checkedoutHREF.toString()), args3);
        assertNotNull(checkedout2);
        assertEquals(0, checkedout2.getEntries().size());
        
        // test checked-in doc has new updates
        Entry checkedIn = abdera.parseEntry(new StringReader(checkinResXML), null);
        Entry updatedDoc = getEntry(checkedIn.getSelfLink().getHref());
        // TODO: issue with updating name on PWC and it not reflecting on checked-in document
        //assertEquals("Updated Title " + guid, updatedDoc.getTitle());
        assertEquals("text/plain", updatedDoc.getContentMimeType().toString());
        Response updatedContentRes = sendRequest(new GetRequest(updatedDoc.getContentSrc().toString()), 200);
        assertEquals("updated content " + guid, updatedContentRes.getContentAsString());
        
        // test checked-in version properties
        CMISProperties updatedProps = updatedDoc.getExtension(CMISConstants.PROPERTIES);
        assertNotNull(updatedProps);
        assertFalse(updatedProps.isVersionSeriesCheckedOut());
        assertEquals(updatedProps.getObjectId(), updatedProps.getVersionSeriesId());
        assertEquals("", updatedProps.getVersionSeriesCheckedOutId());
        assertEquals("", updatedProps.getVersionSeriesCheckedOutBy());
        assertEquals(guid, updatedProps.getCheckinComment());
    }

    public void testUpdateOnCheckIn()
        throws Exception
    {
        // retrieve test folder for checkins
        Entry testFolder = createTestFolder("testUpdateOnCheckIn");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        
        // create document for checkout
        Entry document = createDocument(childrenLink.getHref(), "testUpdateOnCheckIn");
        Response documentRes = sendRequest(new GetRequest(document.getSelfLink().getHref().toString()), 200, getAtomValidator());
        assertNotNull(documentRes);
        String xml = documentRes.getContentAsString();
        assertNotNull(xml);
        
        // checkout
        IRI checkedoutHREF = getCheckedOutCollection(getRepository());
        Response pwcRes = sendRequest(new PostRequest(checkedoutHREF.toString(), xml, Format.ATOMENTRY.mimetype()), 201, getAtomValidator());
        assertNotNull(pwcRes);
        Entry pwc = abdera.parseEntry(new StringReader(pwcRes.getContentAsString()), null);
        assertNotNull(pwc);
        
        // test getCheckedOut is updated
        CMISProperties props = testFolder.getExtension(CMISConstants.PROPERTIES);
        String scopeId = props.getObjectId();
        Map<String, String> args = new HashMap<String, String>();
        args.put("folderId", scopeId);
        Feed checkedout = getFeed(new IRI(checkedoutHREF.toString()), args);
        assertNotNull(checkedout);
        assertEquals(1, checkedout.getEntries().size());
    
        // checkin (with update)
        String checkinFile = loadString("/cmis/rest/checkinandupdatedocument.atomentry.xml");
        String guid = GUID.generate();
        checkinFile = checkinFile.replace("${NAME}", guid);
        String checkinUrl = pwc.getSelfLink().getHref().toString();
        Map<String, String> args2 = new HashMap<String, String>();
        args2.put("checkinComment", guid);
        Response checkinRes = sendRequest(new PutRequest(checkinUrl, checkinFile, Format.ATOMENTRY.mimetype()).setArgs(args2), 200, getAtomValidator());
        assertNotNull(checkinRes);
        String checkinResXML = checkinRes.getContentAsString();
    
        // test getCheckedOut is updated
        CMISProperties props2 = testFolder.getExtension(CMISConstants.PROPERTIES);
        String scopeId2 = props2.getObjectId();
        Map<String, String> args3 = new HashMap<String, String>();
        args3.put("folderId", scopeId2);
        Feed checkedout2 = getFeed(new IRI(checkedoutHREF.toString()), args3);
        assertNotNull(checkedout2);
        assertEquals(0, checkedout2.getEntries().size());
        
        // test checked-in doc has new updates
        Entry checkedIn = abdera.parseEntry(new StringReader(checkinResXML), null);
        Entry updatedDoc = getEntry(checkedIn.getSelfLink().getHref());
        // TODO: issue with updating name on PWC and it not reflecting on checked-in document
        //assertEquals("Updated Title " + guid, updatedDoc.getTitle());
        assertEquals("text/plain", updatedDoc.getContentMimeType().toString());
        Response updatedContentRes = sendRequest(new GetRequest(updatedDoc.getContentSrc().toString()), 200);
        assertEquals("updated content " + guid, updatedContentRes.getContentAsString());
    }

    public void testGetAllVersions()
        throws Exception
    {
        int NUMBER_OF_VERSIONS = 3;
        
        // retrieve test folder for checkins
        Entry testFolder = createTestFolder("testGetAllVersions");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        
        // create document for checkout
        Entry document = createDocument(childrenLink.getHref(), "testGetAllVersions");
        Response documentRes = sendRequest(new GetRequest(document.getSelfLink().getHref().toString()), 200, getAtomValidator());
        assertNotNull(documentRes);
        String xml = documentRes.getContentAsString();
        assertNotNull(xml);

        IRI checkedoutHREF = getCheckedOutCollection(getRepository());
        for (int i = 0; i < NUMBER_OF_VERSIONS; i++)
        {
            // checkout
            Response pwcRes = sendRequest(new PostRequest(checkedoutHREF.toString(), xml, Format.ATOMENTRY.mimetype()), 201, getAtomValidator());
            assertNotNull(pwcRes);
            Entry pwc = abdera.parseEntry(new StringReader(pwcRes.getContentAsString()), null);
            assertNotNull(pwc);
    
            // checkin
            String checkinFile = loadString("/cmis/rest/checkinandupdatedocument.atomentry.xml");
            checkinFile = checkinFile.replace("${NAME}", "checkin " + i);
            String checkinUrl = pwc.getSelfLink().getHref().toString();
            Map<String, String> args2 = new HashMap<String, String>();
            args2.put("checkinComment", "checkin " + i);
            Response checkinRes = sendRequest(new PutRequest(checkinUrl, checkinFile, Format.ATOMENTRY.mimetype()).setArgs(args2), 200, getAtomValidator());
            assertNotNull(checkinRes);
        }

        // get all versions
        Link allVersionsLink = document.getLink(CMISConstants.REL_ALLVERSIONS);
        assertNotNull(allVersionsLink);
        Feed allVersions = getFeed(allVersionsLink.getHref());
        assertNotNull(allVersions);
        assertEquals(NUMBER_OF_VERSIONS + 1 /** initial version */, allVersions.getEntries().size());
        for (int i = 0; i < NUMBER_OF_VERSIONS; i++)
        {
            Link versionLink = allVersions.getEntries().get(i).getSelfLink();
            assertNotNull(versionLink);
            Entry version = getEntry(versionLink.getHref());
            assertNotNull(version);
            // TODO: issue with updating name on PWC and it not reflecting on checked-in document
            //assertEquals("Update Title checkin " + i, version.getTitle());
            Response versionContentRes = sendRequest(new GetRequest(version.getContentSrc().toString()), 200);
            assertEquals("updated content checkin " + (NUMBER_OF_VERSIONS -1 - i), versionContentRes.getContentAsString());
            // TODO: test version specific properties e.g. checkin comment
        }
    }
    
    public void testGetAllTypeDefinitions()
        throws Exception
    {
        IRI typesHREF = getTypesCollection(getRepository());
        Feed types = getFeed(typesHREF);
        assertNotNull(types);
        Feed typesWithProps = getFeed(typesHREF);
        assertNotNull(typesWithProps);
        // TODO: spec issue 40
        for (Entry type : types.getEntries())
        {
            Entry retrievedType = getEntry(type.getSelfLink().getHref());
            assertEquals(type.getId(), retrievedType.getId());
            assertEquals(type.getTitle(), retrievedType.getTitle());
            // TODO: type specific properties - extension to Abdera
        }
    }

    public void testGetHierarchyTypeDefinitions()
        throws Exception
    {
        IRI typesHREF = getTypesCollection(getRepository());
        Map<String, String> args = new HashMap<String, String>();
        args.put("type", "FOLDER_OBJECT_TYPE");
        args.put("includePropertyDefinitions", "true");
        args.put("maxItems", "5");
        while (typesHREF != null)
        {
            Feed types = getFeed(typesHREF, args);
            
            // TODO: spec issue 40
            for (Entry type : types.getEntries())
            {
                Entry retrievedType = getEntry(type.getSelfLink().getHref());
                assertEquals(type.getId(), retrievedType.getId());
                assertEquals(type.getTitle(), retrievedType.getTitle());
                // TODO: type specific properties - extension to Abdera
            }
         
            // next page
            Link nextLink = types.getLink("next");
            typesHREF = (nextLink != null) ? nextLink.getHref() : null;
            args.remove("maxItems");
        };
    }

    public void testGetTypeDefinition()
        throws Exception
    {
        // retrieve test folder for checkins
        Entry testFolder = createTestFolder("testGetEntryTypeDefinition");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        
        // create document
        Entry document = createDocument(childrenLink.getHref(), "testGetEntryTypeDefinitionDoc");
        Response documentRes = sendRequest(new GetRequest(document.getSelfLink().getHref().toString()), 200, getAtomValidator());
        assertNotNull(documentRes);

        // create folder
        Entry folder = createFolder(childrenLink.getHref(), "testGetEntryTypeDefinitionFolder");
        Response folderRes = sendRequest(new GetRequest(folder.getSelfLink().getHref().toString()), 200, getAtomValidator());
        assertNotNull(folderRes);
        
        // retrieve children
        Feed children = getFeed(childrenLink.getHref());
        for (Entry entry : children.getEntries())
        {
            // get type definition
            Link typeLink = entry.getLink(CMISConstants.REL_TYPE);
            assertNotNull(typeLink);
            // TODO: spec issue 40 - use getEntry when wrapped as entry
            Response type = sendRequest(new GetRequest(typeLink.getHref().toString()), 200, getAtomValidator());
            //Entry type = getEntry(typeLink.getHref());
            assertNotNull(type);
            // TODO: test correct type for entry & properties of type
        }
    }
    
//    public void testUnfiled()
//    {
//    }
    
}
