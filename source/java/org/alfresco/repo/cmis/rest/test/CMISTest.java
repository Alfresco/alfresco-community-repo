/*
 * This program is free software; you can redistribute it and/or
 *
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * Copyright (C) 2005-2008 Alfresco Software Limited.
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

import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.util.GUID;
import org.alfresco.web.scripts.Format;
import org.alfresco.web.scripts.TestWebScriptServer.DeleteRequest;
import org.alfresco.web.scripts.TestWebScriptServer.GetRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PostRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PutRequest;
import org.alfresco.web.scripts.TestWebScriptServer.Response;
import org.apache.abdera.ext.cmis.CMISConstants;
import org.apache.abdera.ext.cmis.CMISObject;
import org.apache.abdera.i18n.iri.IRI;
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

    @Override
    protected void setUp()
        throws Exception
    {
        // Uncomment to change default behaviour of tests  
        setDefaultRunAs("admin");
//        RemoteServer server = new RemoteServer();
//        server.username = "admin";
//        server.password = "admin";
//        setRemoteServer(server);
//        setArgsAsHeaders(false);
//        setValidateResponse(false);
//        setListener(new CMISTestListener(System.out));
//        setTraceReqRes(true);
        
        super.setUp();
    }

    
    public void testRepository()
        throws Exception
    {
        IRI rootHREF = getRootChildrenCollection(getWorkspace(getRepository()));
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
        assertEquals(document.getTitle(), resContent);
        Feed feedFolderAfter = getFeed(childrenLink.getHref());
        int entriesAfter = feedFolderAfter.getEntries().size();
        assertEquals(entriesBefore +1, entriesAfter);
        Entry entry = feedFolderAfter.getEntry(document.getId().toString());
        assertNotNull(entry);
    }

    // TODO: check why this test is here
//    public void testCreateDocument2()
//        throws Exception
//    {
//        Entry testFolder = createTestFolder("testCreateDocument2");
//        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
//        assertNotNull(childrenLink);
//        String createFile = loadString("/org/alfresco/repo/cmis/rest/test/createdocument2.atomentry.xml");
//        Response res = sendRequest(new PostRequest(childrenLink.getHref().toString(), createFile, Format.ATOM.mimetype()), 201, getAtomValidator());
//        String xml = res.getContentAsString();
//        Entry entry = abdera.parseEntry(new StringReader(xml), null);
//        Response documentContentRes = sendRequest(new GetRequest(entry.getContentSrc().toString()), 200);
//        String resContent = documentContentRes.getContentAsString();
//        assertEquals("1", resContent);
//    }

    // TODO: Test creation of document via Atom Entry containing plain text (non Base64 encoded)
//    public void testCreateDocumentBase64()
//        throws Exception
//    {
//        Entry testFolder = createTestFolder("testCreateDocumentBase64");
//        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
//        assertNotNull(childrenLink);
//        Feed children = getFeed(childrenLink.getHref());
//        assertNotNull(children);
//        int entriesBefore = children.getEntries().size();
//        Entry document = createDocument(children.getSelfLink().getHref(), "testCreateDocument", "/org/alfresco/repo/cmis/rest/test/createdocumentBase64.atomentry.xml");
//        Response documentContentRes = sendRequest(new GetRequest(document.getContentSrc().toString()), 200);
//        String testContent = loadString("/org/alfresco/repo/cmis/rest/test/createdocumentBase64.txt");
//        String resContent = documentContentRes.getContentAsString();
//        assertEquals(testContent, resContent);
//        Feed feedFolderAfter = getFeed(childrenLink.getHref());
//        int entriesAfter = feedFolderAfter.getEntries().size();
//        assertEquals(entriesBefore +1, entriesAfter);
//        Entry entry = feedFolderAfter.getEntry(document.getId().toString());
//        assertNotNull(entry);
//    }
    
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
    
    public void testCreateDocumentViaDescendants()
        throws Exception
    {
        Entry testFolder = createTestFolder("testCreateDocumentViaDescendants");
        Link descendantsLink = testFolder.getLink(CMISConstants.REL_DESCENDANTS);
        assertNotNull(descendantsLink);
        Feed descendants = getFeed(descendantsLink.getHref());
        assertNotNull(descendants);
        int entriesBefore = descendants.getEntries().size();
        Entry document = createDocument(descendants.getSelfLink().getHref(), "testCreateDocumentViaDescendants");
        Response documentContentRes = sendRequest(new GetRequest(document.getContentSrc().toString()), 200);
        String resContent = documentContentRes.getContentAsString();
        assertEquals(document.getTitle(), resContent);
        Feed feedFolderAfter = getFeed(descendantsLink.getHref());
        int entriesAfter = feedFolderAfter.getEntries().size();
        assertEquals(entriesBefore +1, entriesAfter);
        Entry entry = feedFolderAfter.getEntry(document.getId().toString());
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
        //assertEquals(testDocument.getSummary(), testDocumentFromGet.getSummary());
        
        // get something that doesn't exist
        Response res = sendRequest(new GetRequest(testDocument.getSelfLink().getHref().toString() + GUID.generate()), 404);
        assertNotNull(res);
    }

    public void testGetChildren()
        throws Exception
    {
        // create multiple children
        Entry testFolder = createTestFolder("testGetChildren");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Entry document1 = createDocument(childrenLink.getHref(), "testGetChildren1");
        assertNotNull(document1);
        Entry document2 = createDocument(childrenLink.getHref(), "testGetChildren2");
        assertNotNull(document2);
        Entry document3 = createDocument(childrenLink.getHref(), "testGetChildren3");
        assertNotNull(document3);
        
        // checkout one of the children to ensure private working copy isn't included
        Response documentRes = sendRequest(new GetRequest(document2.getSelfLink().getHref().toString()), 200, getAtomValidator());
        assertNotNull(documentRes);
        String documentXML = documentRes.getContentAsString();
        assertNotNull(documentXML);
        IRI checkedoutHREF = getCheckedOutCollection(getWorkspace(getRepository()));
        Response pwcRes = sendRequest(new PostRequest(checkedoutHREF.toString(), documentXML, Format.ATOMENTRY.mimetype()), 201, getAtomValidator());
        assertNotNull(pwcRes);
        Entry pwc = getAbdera().parseEntry(new StringReader(pwcRes.getContentAsString()), null);
        
        // get children, ensure they exist (but not private working copy)
        Feed children = getFeed(childrenLink.getHref());
        assertNotNull(children);
        assertEquals(3, children.getEntries().size());
        assertNotNull(children.getEntry(document1.getId().toString()));
        assertNotNull(children.getEntry(document2.getId().toString()));
        assertNotNull(children.getEntry(document3.getId().toString()));
        assertNull(children.getEntry(pwc.getId().toString()));
    }

    public void testGetChildrenPaging()
        throws Exception
    {
        // create multiple children
        Set<IRI> docIds = new HashSet<IRI>();
        Entry testFolder = createTestFolder("testGetChildrenPaging");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        for (int i = 0; i < 15; i++)
        {
            Entry document = createDocument(childrenLink.getHref(), "testGetChildrenPaging" + i);
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
            if (nextCount < 4)
            {
                assertNotNull(nextLink);
            }
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
        // TODO: potential spec issue
//        Response invalidRes = sendRequest(new GetRequest(childrenLink.getHref().toString()).setArgs(args), 400);
//        assertNotNull(invalidRes);

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

    public void testGetChildrenPropertyFilter()
        throws Exception
    {
        // create children
        Entry testFolder = createTestFolder("testGetChildrenPropertyFilter");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Entry document1 = createDocument(childrenLink.getHref(), "testGetChildrenPropertyFilter1");
        assertNotNull(document1);

        {
            // get children with all properties
            Feed children = getFeed(childrenLink.getHref());
            for (Entry entry : children.getEntries())
            {
                CMISObject object = entry.getExtension(CMISConstants.OBJECT);
                assertNotNull(object.getObjectId().getValue());
                assertNotNull(object.getObjectTypeId().getValue());
            }
        }

        {
            // get children with object_id only
            Map<String, String> args = new HashMap<String, String>();
            args.put("filter", "ObjectId");
            Feed children = getFeed(childrenLink.getHref(), args);
            for (Entry entry : children.getEntries())
            {
                CMISObject object = entry.getExtension(CMISConstants.OBJECT);
                assertNotNull(object.getObjectId().getValue());
                assertNull(object.getObjectTypeId());
            }
        }
    }

    public void testGetDescendants()
        throws Exception
    {
        // create multiple nested children
        Entry testFolder = createTestFolder("testGetDescendants");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        Entry document1 = createDocument(childrenLink.getHref(), "testGetDescendants1");
        assertNotNull(document1);
        Entry folder2 = createFolder(childrenLink.getHref(), "testGetDescendants2");
        assertNotNull(folder2);
        Link childrenLink2 = folder2.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink2);
        Entry document3 = createDocument(childrenLink2.getHref(), "testGetDescendants3");
        assertNotNull(document3);
        
        {
            // get descendants (depth = 1, equivalent to getChildren)
            Map<String, String> args = new HashMap<String, String>();
            args.put("depth", "1");
            Link descendantsLink = testFolder.getLink(CMISConstants.REL_DESCENDANTS);
            Feed descendants = getFeed(descendantsLink.getHref(), args);
            assertNotNull(descendants);
            assertEquals(2, descendants.getEntries().size());
            assertNotNull(descendants.getEntry(document1.getId().toString()));
            assertNotNull(descendants.getEntry(folder2.getId().toString()));
            
            Entry getFolder2 = descendants.getEntry(folder2.getId().toString());
            Entry getFolder2Child = getFolder2.getFirstChild(CMISConstants.NESTED_ENTRY);
            assertNull(getFolder2Child);
        }
        
        {
            // get nested children
            Map<String, String> args = new HashMap<String, String>();
            args.put("depth", "2");
            Link descendantsLink = testFolder.getLink(CMISConstants.REL_DESCENDANTS);
            Feed descendants = getFeed(descendantsLink.getHref(), args);
            assertNotNull(descendants);
            assertEquals(2, descendants.getEntries().size());
            assertNotNull(descendants.getEntry(document1.getId().toString()));
            assertNotNull(descendants.getEntry(folder2.getId().toString()));
            
            Entry getFolder2 = descendants.getEntry(folder2.getId().toString());
            List<Entry> getFolder2Children = getFolder2.getExtensions(CMISConstants.NESTED_ENTRY);
            assertNotNull(getFolder2Children);
            assertEquals(1, getFolder2Children.size());
            Entry getFolder2Child = getFolder2Children.get(0);
            assertEquals(document3.getId(), getFolder2Child.getId());
            assertEquals(document3.getEditLink().getHref().toString(), getFolder2Child.getEditLink().getHref().toString());
        }
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

        // TODO: compare identity using OBJECT_ID property, not atom:id
        
        // ensure there are ancestors 'testParent', "test run folder", "tests folder" and "root folder"
        Map<String, String> args = new HashMap<String, String>();
        args.put("returnToRoot", "true");
        Feed parentsToRoot = getFeed(new IRI(parentLink.getHref().toString()), args);
        assertNotNull(parentsToRoot);
        assertEquals(4, parentsToRoot.getEntries().size());
        assertEquals(testFolder.getId(), parentsToRoot.getEntries().get(0).getId());
        assertNotNull(parentsToRoot.getEntries().get(0).getLink(CMISConstants.REL_PARENT));
        assertEquals(getTestRunFolder().getId(), parentsToRoot.getEntries().get(1).getId());
        assertNotNull(parentsToRoot.getEntries().get(1).getLink(CMISConstants.REL_PARENT));
        assertEquals(getTestRootFolder().getId(), parentsToRoot.getEntries().get(2).getId());
        assertNotNull(parentsToRoot.getEntries().get(2).getLink(CMISConstants.REL_PARENT));
        Feed root = getFeed(getRootChildrenCollection(getWorkspace(getRepository())));
        Entry rootEntry = getEntry(root.getLink(CMISConstants.REL_SOURCE).getHref());
        assertEquals(rootEntry.getId(), parentsToRoot.getEntries().get(3).getId());
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
        //assertNotNull(parentsToRoot.getEntries().get(0).getLink(CMISConstants.REL_PARENT));
        assertEquals(getTestRunFolder().getId(), parentsToRoot.getEntries().get(1).getId());
        //assertNotNull(parentsToRoot.getEntries().get(1).getLink(CMISConstants.REL_PARENT));
        assertEquals(getTestRootFolder().getId(), parentsToRoot.getEntries().get(2).getId());
        //assertNotNull(parentsToRoot.getEntries().get(2).getLink(CMISConstants.REL_PARENT));
        Feed root = getFeed(getRootChildrenCollection(getWorkspace(getRepository())));
        Entry rootEntry = getEntry(root.getLink(CMISConstants.REL_SOURCE).getHref());
        assertEquals(rootEntry.getId(), parentsToRoot.getEntries().get(3).getId());
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
        String mimetype = (document.getContentMimeType() != null) ? document.getContentMimeType().toString() : null;
        if (mimetype != null)
        {
            assertEquals("text/html", mimetype);
        }

        // TODO: check for content update allowable action
        //       if update allowed, perform update, else update and check for appropriate error
        
        // update
        String updateFile = loadString("/org/alfresco/repo/cmis/rest/test/updatedocument.atomentry.xml");
        String guid = GUID.generate();
        updateFile = updateFile.replace("${NAME}", guid);
        Response res = sendRequest(new PutRequest(document.getSelfLink().getHref().toString(), updateFile, Format.ATOMENTRY.mimetype()), 200, getAtomValidator());
        assertNotNull(res);
        Entry updated = getAbdera().parseEntry(new StringReader(res.getContentAsString()), null);
        
        // ensure update occurred
        assertEquals(document.getId(), updated.getId());
        assertEquals(document.getPublished(), updated.getPublished());
        assertEquals("Updated Title " + guid, updated.getTitle());
        // TODO: why is this testing for text/plain? it should be test/html
        assertEquals("text/plain", updated.getContentMimeType().toString());
        Response contentRes = sendRequest(new GetRequest(updated.getContentSrc().toString()), 200);
        assertEquals("updated content " + guid, contentRes.getContentAsString());
    }

    public void testAllowableActions()
        throws Exception
    {
        // retrieve test folder for allowable actions
        Entry testFolder = createTestFolder("testAllowableActions");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        
        // test allowable actions for folder
        {
            Entry child = createFolder(childrenLink.getHref(), "testFolderAllowableActions");
            assertNotNull(child);
            Link allowableActions = child.getLink(CMISConstants.REL_ALLOWABLEACTIONS);
            Response allowableActionsRes = sendRequest(new GetRequest(allowableActions.getHref().toString()), 200, getAtomValidator());
            assertNotNull(allowableActionsRes);
            // TODO: parse response with Abdera extension

            // retrieve getProperties() with includeAllowableActions flag
            Map<String, String> args = new HashMap<String, String>();
            args.put("includeAllowableActions", "true");
            Response getPropertiesRes = sendRequest(new GetRequest(child.getSelfLink().getHref().toString()).setArgs(args), 200, getAtomValidator());
            assertNotNull(getPropertiesRes);
            // TODO: parse response with Abdera extension
            
            // TODO: test equality between getAllowableActions and getProperties
        }

        // test allowable actions for document
        {
            Entry child = createDocument(childrenLink.getHref(), "testDocumentAllowableActions");
            assertNotNull(child);
            Link allowableActions = child.getLink(CMISConstants.REL_ALLOWABLEACTIONS);
            Response allowableActionsRes = sendRequest(new GetRequest(allowableActions.getHref().toString()), 200, getAtomValidator());
            assertNotNull(allowableActionsRes);
            // TODO: parse response with Abdera extension
            
            // retrieve getProperties() with includeAllowableActions flag
            Map<String, String> args = new HashMap<String, String>();
            args.put("includeAllowableActions", "true");
            Response getPropertiesRes = sendRequest(new GetRequest(child.getSelfLink().getHref().toString()).setArgs(args), 200, getAtomValidator());
            assertNotNull(getPropertiesRes);
            // TODO: parse response with Abdera extension
            
            // TODO: test equality between getAllowableActions and getProperties
        }
        
        // test allowable actions for children
        {
            Map<String, String> args = new HashMap<String, String>();
            args.put("includeAllowableActions", "true");
            Response allowableActionsRes = sendRequest(new GetRequest(childrenLink.getHref().toString()).setArgs(args), 200, getAtomValidator());
            assertNotNull(allowableActionsRes);
            // TODO: parse response with Abdera extension
        }
    }
    
    public void testGetCheckedOut()
        throws Exception
    {
        // retrieve test folder for checkouts
        Entry testFolder = createTestFolder("testGetCheckedOut");
        CMISObject object = testFolder.getExtension(CMISConstants.OBJECT);
        String scopeId = object.getObjectId().getValue();
        assertNotNull(scopeId);
        
        // retrieve checkouts within scope of test checkout folder
        Service repository = getRepository();
        assertNotNull(repository);
        IRI checkedoutHREF = getCheckedOutCollection(getWorkspace(getRepository()));
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
        CMISObject docObject = document.getExtension(CMISConstants.OBJECT);
        Response documentRes = sendRequest(new GetRequest(document.getSelfLink().getHref().toString()), 200, getAtomValidator());
        assertNotNull(documentRes);
        String documentXML = documentRes.getContentAsString();
        assertNotNull(documentXML);
        
        // checkout
        IRI checkedoutHREF = getCheckedOutCollection(getWorkspace(getRepository()));
        Response pwcRes = sendRequest(new PostRequest(checkedoutHREF.toString(), documentXML, Format.ATOMENTRY.mimetype()), 201, getAtomValidator());
        assertNotNull(pwcRes);
        String pwcXml = pwcRes.getContentAsString();
        assertNotNull(pwcXml);
        Entry pwc = getAbdera().parseEntry(new StringReader(pwcXml), null);
        assertNotNull(pwc);
        CMISObject pwcObject = pwc.getExtension(CMISConstants.OBJECT);
        assertNotNull(pwcObject);
        assertTrue(pwcObject.isVersionSeriesCheckedOut().getBooleanValue());
        assertEquals(docObject.getObjectId().getValue(), pwcObject.getVersionSeriesId().getValue());
        assertEquals(pwcObject.getObjectId().getValue(), pwcObject.getVersionSeriesCheckedOutId().getValue());
        assertNotNull(pwcObject.getVersionSeriesCheckedOutBy().getValue());
        
        // retrieve pwc directly
        Response pwcGetRes = sendRequest(new GetRequest(pwc.getSelfLink().getHref().toString()), 200);
        assertNotNull(pwcGetRes);
        String pwcGetXml = pwcRes.getContentAsString();
        Entry pwcGet = getAbdera().parseEntry(new StringReader(pwcGetXml), null);
        assertNotNull(pwcGet);
        CMISObject pwcGetObject = pwc.getExtension(CMISConstants.OBJECT);
        assertNotNull(pwcGetObject);
        assertTrue(pwcGetObject.isVersionSeriesCheckedOut().getBooleanValue());
        assertEquals(docObject.getObjectId().getValue(), pwcGetObject.getVersionSeriesId().getValue());
        assertEquals(pwcGetObject.getObjectId().getValue(), pwcGetObject.getVersionSeriesCheckedOutId().getValue());
        assertNotNull(pwcGetObject.getVersionSeriesCheckedOutBy().getValue());

        // test getCheckedOut is updated
        CMISObject object = testFolder.getExtension(CMISConstants.OBJECT);
        String scopeId = object.getObjectId().getValue();
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
        IRI checkedoutHREF = getCheckedOutCollection(getWorkspace(getRepository()));
        Response pwcRes = sendRequest(new PostRequest(checkedoutHREF.toString(), xml, Format.ATOMENTRY.mimetype()), 201, getAtomValidator());
        assertNotNull(pwcRes);
        String pwcXml = pwcRes.getContentAsString();
        
        // test getCheckedOut is updated
        CMISObject object = testFolder.getExtension(CMISConstants.OBJECT);
        String scopeId = object.getObjectId().getValue();
        Map<String, String> args = new HashMap<String, String>();
        args.put("folderId", scopeId);
        Feed checkedout = getFeed(new IRI(checkedoutHREF.toString()), args);
        assertNotNull(checkedout);
        assertEquals(1, checkedout.getEntries().size());
        
        // cancel checkout
        Entry pwc = getAbdera().parseEntry(new StringReader(pwcXml), null);
        assertNotNull(pwc);
        Response cancelRes = sendRequest(new DeleteRequest(pwc.getSelfLink().getHref().toString()), 204);
        assertNotNull(cancelRes);

        // test getCheckedOut is updated
        CMISObject object2 = testFolder.getExtension(CMISConstants.OBJECT);
        String scopeId2 = object2.getObjectId().getValue();
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
        IRI checkedoutHREF = getCheckedOutCollection(getWorkspace(getRepository()));
        Response pwcRes = sendRequest(new PostRequest(checkedoutHREF.toString(), xml, Format.ATOMENTRY.mimetype()), 201, getAtomValidator());
        assertNotNull(pwcRes);
        Entry pwc = getAbdera().parseEntry(new StringReader(pwcRes.getContentAsString()), null);
        assertNotNull(pwc);
        
        // test getCheckedOut is updated
        CMISObject object = testFolder.getExtension(CMISConstants.OBJECT);
        String scopeId = object.getObjectId().getValue();
        Map<String, String> args = new HashMap<String, String>();
        args.put("folderId", scopeId);
        Feed checkedout = getFeed(new IRI(checkedoutHREF.toString()), args);
        assertNotNull(checkedout);
        assertEquals(1, checkedout.getEntries().size());

        // test version properties of checked-out item
        // test checked-in version properties
        Entry checkedoutdoc = getEntry(document.getSelfLink().getHref());
        CMISObject checkedoutdocObject = checkedoutdoc.getExtension(CMISConstants.OBJECT);
        assertNotNull(checkedoutdocObject);
        assertTrue(checkedoutdocObject.isVersionSeriesCheckedOut().getBooleanValue());
        //assertEquals(checkedoutdocObject.getObjectId().getValue(), checkedoutdocObject.getVersionSeriesId().getValue());
        assertNotNull(checkedoutdocObject.getVersionSeriesCheckedOutId().getValue());
        assertNotNull(checkedoutdocObject.getVersionSeriesCheckedOutBy().getValue());
        
        // test update of private working copy
        String updateFile = loadString("/org/alfresco/repo/cmis/rest/test/updatedocument.atomentry.xml");
        String guid = GUID.generate();
        updateFile = updateFile.replace("${NAME}", guid);
        Response pwcUpdatedres = sendRequest(new PutRequest(pwc.getEditLink().getHref().toString(), updateFile, Format.ATOMENTRY.mimetype()), 200, getAtomValidator());
        assertNotNull(pwcUpdatedres);
        Entry updated = getAbdera().parseEntry(new StringReader(pwcUpdatedres.getContentAsString()), null);
        // ensure update occurred
        assertEquals(pwc.getId(), updated.getId());
        assertEquals(pwc.getPublished(), updated.getPublished());
        assertEquals("Updated Title " + guid, updated.getTitle());
        assertEquals("text/plain", updated.getContentMimeType().toString());
        Response pwcContentRes = sendRequest(new GetRequest(pwc.getContentSrc().toString()), 200);
        assertEquals("updated content " + guid, pwcContentRes.getContentAsString());
        
        // checkin
        String checkinFile = loadString("/org/alfresco/repo/cmis/rest/test/checkindocument.atomentry.xml");
        String checkinUrl = pwc.getSelfLink().getHref().toString();
        Map<String, String> args2 = new HashMap<String, String>();
        args2.put("checkinComment", guid);
        args2.put("checkin", "true");
        Response checkinRes = sendRequest(new PutRequest(checkinUrl, checkinFile, Format.ATOMENTRY.mimetype()).setArgs(args2), 200, getAtomValidator());
        assertNotNull(checkinRes);
        String checkinResXML = checkinRes.getContentAsString();
    
        // test getCheckedOut is updated
        CMISObject object2 = testFolder.getExtension(CMISConstants.OBJECT);
        String scopeId2 = object2.getObjectId().getValue();
        Map<String, String> args3 = new HashMap<String, String>();
        args3.put("folderId", scopeId2);
        Feed checkedout2 = getFeed(new IRI(checkedoutHREF.toString()), args3);
        assertNotNull(checkedout2);
        assertEquals(0, checkedout2.getEntries().size());
        
        // test checked-in doc has new updates
        Entry checkedIn = getAbdera().parseEntry(new StringReader(checkinResXML), null);
        Entry updatedDoc = getEntry(checkedIn.getSelfLink().getHref());
        // TODO: issue with updating name on PWC and it not reflecting on checked-in document
        //assertEquals("Updated Title " + guid, updatedDoc.getTitle());
        assertEquals("text/plain", updatedDoc.getContentMimeType().toString());
        Response updatedContentRes = sendRequest(new GetRequest(updatedDoc.getContentSrc().toString()), 200);
        assertEquals("updated content " + guid, updatedContentRes.getContentAsString());
        
        // test checked-in version properties
        CMISObject updatedObject = updatedDoc.getExtension(CMISConstants.OBJECT);
        assertNotNull(updatedObject);
        assertFalse(updatedObject.isVersionSeriesCheckedOut().getBooleanValue());
        //assertEquals(updatedObject.getObjectId().getValue(), updatedObject.getVersionSeriesId().getValue());
        assertNull(updatedObject.getVersionSeriesCheckedOutId().getValue());
        assertNull(updatedObject.getVersionSeriesCheckedOutBy().getValue());
        assertEquals(guid, updatedObject.getCheckinComment().getValue());
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
        IRI checkedoutHREF = getCheckedOutCollection(getWorkspace(getRepository()));
        Response pwcRes = sendRequest(new PostRequest(checkedoutHREF.toString(), xml, Format.ATOMENTRY.mimetype()), 201, getAtomValidator());
        assertNotNull(pwcRes);
        Entry pwc = getAbdera().parseEntry(new StringReader(pwcRes.getContentAsString()), null);
        assertNotNull(pwc);
        
        // test getCheckedOut is updated
        CMISObject object = testFolder.getExtension(CMISConstants.OBJECT);
        String scopeId = object.getObjectId().getValue();
        Map<String, String> args = new HashMap<String, String>();
        args.put("folderId", scopeId);
        Feed checkedout = getFeed(new IRI(checkedoutHREF.toString()), args);
        assertNotNull(checkedout);
        assertEquals(1, checkedout.getEntries().size());
    
        // checkin (with update)
        String checkinFile = loadString("/org/alfresco/repo/cmis/rest/test/checkinandupdatedocument.atomentry.xml");
        String guid = GUID.generate();
        checkinFile = checkinFile.replace("${NAME}", guid);
        String checkinUrl = pwc.getSelfLink().getHref().toString();
        Map<String, String> args2 = new HashMap<String, String>();
        args2.put("checkinComment", guid);
        args2.put("checkin", "true");
        Response checkinRes = sendRequest(new PutRequest(checkinUrl, checkinFile, Format.ATOMENTRY.mimetype()).setArgs(args2), 200, getAtomValidator());
        assertNotNull(checkinRes);
        String checkinResXML = checkinRes.getContentAsString();
    
        // test getCheckedOut is updated
        CMISObject object2 = testFolder.getExtension(CMISConstants.OBJECT);
        String scopeId2 = object2.getObjectId().getValue();
        Map<String, String> args3 = new HashMap<String, String>();
        args3.put("folderId", scopeId2);
        Feed checkedout2 = getFeed(new IRI(checkedoutHREF.toString()), args3);
        assertNotNull(checkedout2);
        assertEquals(0, checkedout2.getEntries().size());
        
        // test checked-in doc has new updates
        Entry checkedIn = getAbdera().parseEntry(new StringReader(checkinResXML), null);
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

        IRI checkedoutHREF = getCheckedOutCollection(getWorkspace(getRepository()));
        for (int i = 0; i < NUMBER_OF_VERSIONS; i++)
        {
            // checkout
            Response pwcRes = sendRequest(new PostRequest(checkedoutHREF.toString(), xml, Format.ATOMENTRY.mimetype()), 201, getAtomValidator());
            assertNotNull(pwcRes);
            Entry pwc = getAbdera().parseEntry(new StringReader(pwcRes.getContentAsString()), null);
            assertNotNull(pwc);
    
            // checkin
            String checkinFile = loadString("/org/alfresco/repo/cmis/rest/test/checkinandupdatedocument.atomentry.xml");
            checkinFile = checkinFile.replace("${NAME}", "checkin " + i);
            String checkinUrl = pwc.getSelfLink().getHref().toString();
            Map<String, String> args2 = new HashMap<String, String>();
            args2.put("checkinComment", "checkin" + i);
            args2.put("checkin", "true");
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
            CMISObject versionObject = version.getExtension(CMISConstants.OBJECT);
            assertNotNull(versionObject);
            assertEquals("checkin" + + (NUMBER_OF_VERSIONS -1 - i), versionObject.getCheckinComment().getValue());
        }
    }
    
    public void testGetAllTypeDefinitions()
        throws Exception
    {
        IRI typesHREF = getTypesChildrenCollection(getWorkspace(getRepository()));
        Feed types = getFeed(typesHREF);
        assertNotNull(types);
        Feed typesWithProps = getFeed(typesHREF);
        assertNotNull(typesWithProps);
        for (Entry type : types.getEntries())
        {
            Entry retrievedType = getEntry(type.getSelfLink().getHref());
            assertEquals(type.getId(), retrievedType.getId());
            assertEquals(type.getTitle(), retrievedType.getTitle());
            // TODO: type specific properties - extension to abdera
        }
    }

    public void testGetHierarchyTypeDefinitions()
        throws Exception
    {
        IRI typesHREF = getTypesChildrenCollection(getWorkspace(getRepository()));
        Map<String, String> args = new HashMap<String, String>();
        args.put("type", "folder");
        args.put("includePropertyDefinitions", "true");
        args.put("maxItems", "5");
        while (typesHREF != null)
        {
            Feed types = getFeed(typesHREF, args);
            
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
        // retrieve test folder for type definitions
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
            Entry type = getEntry(typeLink.getHref());
            assertNotNull(type);
            // TODO: test correct type for entry & properties of type
        }
    }
    
    public void testQuery()
        throws Exception
    {
        // retrieve query collection
        IRI queryHREF = getQueryCollection(getWorkspace(getRepository()));
        
        // retrieve test folder for query
        Entry testFolder = createTestFolder("testQuery");
        CMISObject testFolderObject = testFolder.getExtension(CMISConstants.OBJECT);
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        
        // create documents to query
        Entry document1 = createDocument(childrenLink.getHref(), "apple1");
        assertNotNull(document1);
        CMISObject document1Object = document1.getExtension(CMISConstants.OBJECT);
        assertNotNull(document1Object);
        String doc2name = "name" + System.currentTimeMillis();
        Entry document2 = createDocument(childrenLink.getHref(), doc2name);
        assertNotNull(document2);
        CMISObject document2Object = document2.getExtension(CMISConstants.OBJECT);
        assertNotNull(document2Object);
        Entry document3 = createDocument(childrenLink.getHref(), "banana1");
        assertNotNull(document3);

        // retrieve query request document
        String queryDoc = loadString("/org/alfresco/repo/cmis/rest/test/query.cmisquery.xml");

        {
            // construct structured query
            String query = "SELECT * FROM Folder " +
                           "WHERE ObjectId = '" + testFolderObject.getObjectId().getValue() + "'";
            String queryReq = queryDoc.replace("${STATEMENT}", query);
            queryReq = queryReq.replace("${SKIPCOUNT}", "0");
            queryReq = queryReq.replace("${PAGESIZE}", "5");
    
            // issue structured query
            Response queryRes = sendRequest(new PostRequest(queryHREF.toString(), queryReq.getBytes(), CMISConstants.MIMETYPE_QUERY), 200);
            assertNotNull(queryRes);
            Feed queryFeed = getAbdera().parseFeed(new StringReader(queryRes.getContentAsString()), null);
            assertNotNull(queryFeed);
            assertEquals(1, queryFeed.getEntries().size());
            assertNotNull(queryFeed.getEntry(testFolder.getId().toString()));
            CMISObject result1 = queryFeed.getEntry(testFolder.getId().toString()).getExtension(CMISConstants.OBJECT);
            assertEquals(testFolderObject.getName().getValue(), result1.getName().getValue());
            assertEquals(testFolderObject.getObjectId().getValue(), result1.getObjectId().getValue());
            assertEquals(testFolderObject.getObjectTypeId().getValue(), result1.getObjectTypeId().getValue());
        }

        {
            // construct structured query
            String query = "SELECT * FROM Document " +
                           "WHERE IN_FOLDER('" + testFolderObject.getObjectId().getValue() + "') " +
                           "AND Name = 'apple1'";
            String queryReq = queryDoc.replace("${STATEMENT}", query);
            queryReq = queryReq.replace("${SKIPCOUNT}", "0");
            queryReq = queryReq.replace("${PAGESIZE}", "5");
    
            // issue structured query
            Response queryRes = sendRequest(new PostRequest(queryHREF.toString(), queryReq.getBytes(), CMISConstants.MIMETYPE_QUERY), 200);
            assertNotNull(queryRes);
            Feed queryFeed = getAbdera().parseFeed(new StringReader(queryRes.getContentAsString()), null);
            assertNotNull(queryFeed);
            assertEquals(1, queryFeed.getEntries().size());
            assertNotNull(queryFeed.getEntry(document1.getId().toString()));
            CMISObject result1 = queryFeed.getEntry(document1.getId().toString()).getExtension(CMISConstants.OBJECT);
            assertEquals(document1Object.getName().getValue(), result1.getName().getValue());
            assertEquals(document1Object.getObjectId().getValue(), result1.getObjectId().getValue());
            assertEquals(document1Object.getObjectTypeId().getValue(), result1.getObjectTypeId().getValue());
        }

        String fullTextCapability = getFullTextCapability();
        if (fullTextCapability.equals("fulltextonly") || fullTextCapability.equals("fulltextandstructured"))
        {
            // construct fulltext query
            String query = "SELECT ObjectId, ObjectTypeId, Name FROM Document " +
                           "WHERE CONTAINS('" + doc2name + "')";
            String queryReq = queryDoc.replace("${STATEMENT}", query);
            queryReq = queryReq.replace("${SKIPCOUNT}", "0");
            queryReq = queryReq.replace("${PAGESIZE}", "5");
    
            // issue fulltext query
            Response queryRes = sendRequest(new PostRequest(queryHREF.toString(), queryReq.getBytes(), CMISConstants.MIMETYPE_QUERY), 200);
            assertNotNull(queryRes);
            Feed queryFeed = getAbdera().parseFeed(new StringReader(queryRes.getContentAsString()), null);
            assertNotNull(queryFeed);
            assertEquals(1, queryFeed.getEntries().size());
            assertNotNull(queryFeed.getEntry(document2.getId().toString()));
            CMISObject result1 = queryFeed.getEntry(document2.getId().toString()).getExtension(CMISConstants.OBJECT);
            assertEquals(document2Object.getName().getValue(), result1.getName().getValue());
            assertEquals(document2Object.getObjectId().getValue(), result1.getObjectId().getValue());
            assertEquals(document2Object.getObjectTypeId().getValue(), result1.getObjectTypeId().getValue());
        }

        if (fullTextCapability.equals("fulltextandstructured"))
        {
            // construct fulltext and structured query
            String query = "SELECT ObjectId, ObjectTypeId, Name FROM Document " +
                           "WHERE IN_FOLDER('" + testFolderObject.getObjectId().getValue() + "') " +
                           "AND Name = 'apple1' " +
                           "AND CONTAINS('apple1')";
            String queryReq = queryDoc.replace("${STATEMENT}", query);
            queryReq = queryReq.replace("${SKIPCOUNT}", "0");
            queryReq = queryReq.replace("${PAGESIZE}", "5");
    
            // issue structured query
            Response queryRes = sendRequest(new PostRequest(queryHREF.toString(), queryReq.getBytes(), CMISConstants.MIMETYPE_QUERY), 200);
            assertNotNull(queryRes);
            Feed queryFeed = getAbdera().parseFeed(new StringReader(queryRes.getContentAsString()), null);
            assertNotNull(queryFeed);
            assertEquals(1, queryFeed.getEntries().size());
            assertNotNull(queryFeed.getEntry(document1.getId().toString()));
            CMISObject result1 = queryFeed.getEntry(document1.getId().toString()).getExtension(CMISConstants.OBJECT);
            assertEquals(document1Object.getName().getValue(), result1.getName().getValue());
            assertEquals(document1Object.getObjectId().getValue(), result1.getObjectId().getValue());
            assertEquals(document1Object.getObjectTypeId().getValue(), result1.getObjectTypeId().getValue());
        }
    }
    
    public void testQueryPaging() throws Exception
    {
        // retrieve query collection
        IRI queryHREF = getQueryCollection(getWorkspace(getRepository()));

        // create multiple children
        Set<IRI> docIds = new HashSet<IRI>();
        Entry testFolder = createTestFolder("testQueryPaging");
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        assertNotNull(childrenLink);
        for (int i = 0; i < 15; i++)
        {
            Entry document = createDocument(childrenLink.getHref(), "testQueryPaging" + i);
            assertNotNull(document);
            docIds.add(document.getId());
        }
        assertEquals(15, docIds.size());

        // query children
        String queryDoc = loadString("/org/alfresco/repo/cmis/rest/test/query.cmisquery.xml");
        CMISObject testFolderObject = testFolder.getExtension(CMISConstants.OBJECT);
        String query = "SELECT ObjectId, ObjectTypeId, Name FROM Document " + "WHERE IN_FOLDER('" + testFolderObject.getObjectId().getValue() + "')";

        for (int page = 0; page < 4; page++)
        {
            String queryReq = queryDoc.replace("${STATEMENT}", query);
            queryReq = queryReq.replace("${SKIPCOUNT}", new Integer(page * 4).toString());
            queryReq = queryReq.replace("${PAGESIZE}", "4");
            Response queryRes = sendRequest(new PostRequest(queryHREF.toString(), queryReq.getBytes(), CMISConstants.MIMETYPE_QUERY), 200);
            assertNotNull(queryRes);

            Feed queryFeed = getAbdera().parseFeed(new StringReader(queryRes.getContentAsString()), null);
            assertNotNull(queryFeed);
            assertEquals(page < 3 ? 4 : 3, queryFeed.getEntries().size());

            for (Entry entry : queryFeed.getEntries())
            {
                docIds.remove(entry.getId());
            }
        }
        
        assertEquals(0, docIds.size());
    }

    public void testQueryAllowableActions()
        throws Exception
    {
        // retrieve query collection
        IRI queryHREF = getQueryCollection(getWorkspace(getRepository()));
        
        // retrieve test folder for query
        Entry testFolder = createTestFolder("testQueryAllowableAcrtions");
        CMISObject testFolderObject = testFolder.getExtension(CMISConstants.OBJECT);
        Link childrenLink = testFolder.getLink(CMISConstants.REL_CHILDREN);
        
        // create documents to query
        Entry document1 = createDocument(childrenLink.getHref(), "apple1");
        assertNotNull(document1);
        CMISObject document1Object = document1.getExtension(CMISConstants.OBJECT);
        assertNotNull(document1Object);
        String doc2name = "name" + System.currentTimeMillis();
        Entry document2 = createDocument(childrenLink.getHref(), doc2name);
        assertNotNull(document2);
        CMISObject document2Object = document2.getExtension(CMISConstants.OBJECT);
        assertNotNull(document2Object);
        Entry document3 = createDocument(childrenLink.getHref(), "banana1");
        assertNotNull(document3);
    
        // retrieve query request document
        String queryDoc = loadString("/org/alfresco/repo/cmis/rest/test/queryallowableactions.cmisquery.xml");
    
        {
            // construct structured query
            String query = "SELECT * FROM Document " +
                           "WHERE IN_FOLDER('" + testFolderObject.getObjectId().getValue() + "') ";
            String queryReq = queryDoc.replace("${STATEMENT}", query);
            queryReq = queryReq.replace("${INCLUDEALLOWABLEACTIONS}", "true");
            queryReq = queryReq.replace("${SKIPCOUNT}", "0");
            queryReq = queryReq.replace("${PAGESIZE}", "5");
    
            // issue structured query
            Response queryRes = sendRequest(new PostRequest(queryHREF.toString(), queryReq.getBytes(), CMISConstants.MIMETYPE_QUERY), 200);
            assertNotNull(queryRes);
            Feed queryFeed = getAbdera().parseFeed(new StringReader(queryRes.getContentAsString()), null);
            assertNotNull(queryFeed);
            assertEquals(3, queryFeed.getEntries().size());
            
            //for (Entry entry : queryFeed.getEntries())
            //{
                // TODO: parse response with Abdera extension
                // TODO: test against cmis-allowableactions link
            //}
            
        }
    }

    
//    public void testUnfiled()
//    {
//    }
    
}
