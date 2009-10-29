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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.abdera.ext.cmis.CMISAllowableAction;
import org.alfresco.abdera.ext.cmis.CMISAllowableActions;
import org.alfresco.abdera.ext.cmis.CMISChildren;
import org.alfresco.abdera.ext.cmis.CMISConstants;
import org.alfresco.abdera.ext.cmis.CMISObject;
import org.alfresco.util.GUID;
import org.alfresco.web.scripts.Format;
import org.alfresco.web.scripts.TestWebScriptServer.DeleteRequest;
import org.alfresco.web.scripts.TestWebScriptServer.GetRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PatchRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PostRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PutRequest;
import org.alfresco.web.scripts.TestWebScriptServer.Response;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Element;
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
//        setServiceUrl("http://localhost:8080/alfresco/service/api/cmis");
//        setValidateResponse(false);
        setListener(new CMISTestListener(System.out));
        setTraceReqRes(true);
        
        super.setUp();
    }

    
    public void testRepository()
        throws Exception
    {
        IRI rootHREF = getRootCollection(getWorkspace(getRepository()));
        sendRequest(new GetRequest(rootHREF.toString()), 200, getAtomValidator());
    }
    
    public void testCreateDocument()
        throws Exception
    {
        Entry testFolder = createTestFolder("testCreateDocument");
        Link childrenLink = getChildrenLink(testFolder);
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

    public void testCreateAtomEntry()
        throws Exception
    {
        Entry testFolder = createTestFolder("testCreateAtomEntry");
        Link childrenLink = getChildrenLink(testFolder);
        assertNotNull(childrenLink);
        Feed children = getFeed(childrenLink.getHref());
        assertNotNull(children);
        int entriesBefore = children.getEntries().size();
        Entry document = createDocument(children.getSelfLink().getHref(), "Iñtërnâtiônàlizætiøn - 1.html", "/org/alfresco/repo/cmis/rest/test/createatomentry.atomentry.xml");
        Response documentContentRes = sendRequest(new GetRequest(document.getContentSrc().toString()), 200);
        String resContent = documentContentRes.getContentAsString();
        assertEquals(document.getTitle(), resContent);
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
        Link childrenLink = getChildrenLink(testFolder);
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
        Link childrenLink = getChildrenLink(testFolder);
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
        Link childrenLink = getChildrenLink(testFolder);
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
        Link childrenLink = getChildrenLink(testFolder);
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
        Link childrenLink = getChildrenLink(testFolder);
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
        Link childrenLink = getChildrenLink(testFolder);
        assertNotNull(childrenLink);
        Entry document1 = createDocument(childrenLink.getHref(), "testGetChildrenPropertyFilter1");
        assertNotNull(document1);

        {
            // get children with all properties
            Feed children = getFeed(childrenLink.getHref());
            for (Entry entry : children.getEntries())
            {
                CMISObject object = entry.getExtension(CMISConstants.OBJECT);
                assertNotNull(object.getObjectId().getStringValue());
                assertNotNull(object.getObjectTypeId().getStringValue());
            }
        }

        {
            // get children with object_id only
            Map<String, String> args = new HashMap<String, String>();
            args.put("filter", CMISConstants.PROP_OBJECT_ID);
            Feed children = getFeed(childrenLink.getHref(), args);
            for (Entry entry : children.getEntries())
            {
                CMISObject object = entry.getExtension(CMISConstants.OBJECT);
                assertNotNull(object.getObjectId().getStringValue());
                assertNull(object.getObjectTypeId());
            }
        }
    }

    public void testGetDescendants()
        throws Exception
    {
        // create multiple nested children
        Entry testFolder = createTestFolder("testGetDescendants");
        Link childrenLink = getChildrenLink(testFolder);
        assertNotNull(childrenLink);
        Entry document1 = createDocument(childrenLink.getHref(), "testGetDescendants1");
        assertNotNull(document1);
        Entry folder2 = createFolder(childrenLink.getHref(), "testGetDescendants2");
        assertNotNull(folder2);
        Link childrenLink2 = getChildrenLink(folder2);
        assertNotNull(childrenLink2);
        Entry document3 = createDocument(childrenLink2.getHref(), "testGetDescendants3");
        assertNotNull(document3);
        
        {
            // get descendants (depth = 1, equivalent to getChildren)
            Map<String, String> args = new HashMap<String, String>();
            args.put("depth", "1");
            Link descendantsLink = getDescendantsLink(testFolder);
            assertNotNull(descendantsLink);
            
            Feed descendants = getFeed(descendantsLink.getHref(), args);
            assertNotNull(descendants);
            assertEquals(2, descendants.getEntries().size());
            assertNotNull(descendants.getEntry(document1.getId().toString()));
            assertNotNull(descendants.getEntry(folder2.getId().toString()));
            
            Entry getFolder2 = descendants.getEntry(folder2.getId().toString());
            CMISChildren folder2Children = getFolder2.getFirstChild(CMISConstants.CHILDREN);
            assertNull(folder2Children);
        }
        
        {
            // get nested children
            Map<String, String> args = new HashMap<String, String>();
            args.put("depth", "2");
            Link descendantsLink = getDescendantsLink(testFolder);
            Feed descendants = getFeed(descendantsLink.getHref(), args);
            assertNotNull(descendants);
            assertEquals(2, descendants.getEntries().size());
            assertNotNull(descendants.getEntry(document1.getId().toString()));
            assertNotNull(descendants.getEntry(folder2.getId().toString()));
            Entry getFolder2 = descendants.getEntry(folder2.getId().toString());
            CMISChildren folder2Children = getFolder2.getFirstChild(CMISConstants.CHILDREN);
            assertNotNull(folder2Children);
            assertEquals(1, folder2Children.size());
            Entry getFolder2Child = folder2Children.getEntries().get(0);
            assertEquals(document3.getId(), getFolder2Child.getId());
            assertEquals(document3.getEditLink().getHref().toString(), getFolder2Child.getEditLink().getHref().toString());
        }
    }

    public void testGetFolderTree()
        throws Exception
    {
        // create multiple nested children
        Entry testFolder = createTestFolder("testGetFolderTree");
        Link childrenLink = getChildrenLink(testFolder);
        assertNotNull(childrenLink);
        Entry document1 = createDocument(childrenLink.getHref(), "testGetFolderTree1");
        assertNotNull(document1);
        Entry folder2 = createFolder(childrenLink.getHref(), "testGetFolderTree2");
        assertNotNull(folder2);
        Link childrenLink2 = getChildrenLink(folder2);
        assertNotNull(childrenLink2);
        Entry folder3 = createFolder(childrenLink2.getHref(), "testGetFolderTree2");
        assertNotNull(folder3);
        Entry document4 = createDocument(childrenLink2.getHref(), "testGetFolderTree3");
        assertNotNull(document4);
        
        {
            // get tree (depth = 1, equivalent to getChildren)
            Map<String, String> args = new HashMap<String, String>();
            args.put("depth", "1");
            Link treeLink = getFolderTreeLink(testFolder);
            assertNotNull(treeLink);
            
            Feed tree = getFeed(treeLink.getHref(), args);
            assertNotNull(tree);
            assertEquals(1, tree.getEntries().size());
            assertNotNull(tree.getEntry(folder2.getId().toString()));
            assertNull(tree.getEntry(document1.getId().toString()));
            
            Entry getFolder2 = tree.getEntry(folder2.getId().toString());
            CMISChildren folder2Children = getFolder2.getFirstChild(CMISConstants.CHILDREN);
            assertNull(folder2Children);
        }
        
        {
            // get full tree
            Map<String, String> args = new HashMap<String, String>();
            args.put("depth", "-1");
            Link treeLink = getFolderTreeLink(testFolder);
            Feed tree = getFeed(treeLink.getHref(), args);
            assertNotNull(treeLink);
            assertEquals(1, tree.getEntries().size());
            assertNotNull(tree.getEntry(folder2.getId().toString()));
            assertNull(tree.getEntry(document1.getId().toString()));
            
            Entry getFolder2 = tree.getEntry(folder2.getId().toString());
            CMISChildren folder2Children = getFolder2.getFirstChild(CMISConstants.CHILDREN);
            assertNotNull(folder2Children);
            assertEquals(1, folder2Children.size());
            assertNotNull(folder2Children.getEntry(folder3.getId().toString()));
            assertNull(folder2Children.getEntry(document4.getId().toString()));
        }
    }
    
    public void testGetParent()
        throws Exception
    {
        Entry testFolder = createTestFolder("testParent");
        Link childrenLink = getChildrenLink(testFolder);
        assertNotNull(childrenLink);
        Entry childFolder = createFolder(childrenLink.getHref(), "testParentChild");
        assertNotNull(childFolder);
        Link parentLink = getFolderParentLink(childFolder);
        assertNotNull(parentLink);

        // ensure there is parent 'testParent'
        Entry parent = getEntry(parentLink.getHref());
        assertNotNull(parent);
        assertEquals(testFolder.getId(), parent.getId());
    }

    public void testGetParents()
        throws Exception
    {
        Entry testFolder = createTestFolder("testParents");
        Link childrenLink = getChildrenLink(testFolder);
        assertNotNull(childrenLink);
        Entry childDocs = createDocument(childrenLink.getHref(), "testParentsChild");
        assertNotNull(childDocs);
        Link parentsLink = getObjectParentsLink(childDocs);
        assertNotNull(parentsLink);
        
        // ensure there is parent 'testParent'
        Feed parent = getFeed(parentsLink.getHref());
        assertNotNull(parent);
        assertEquals(1, parent.getEntries().size());
        assertEquals(testFolder.getId(), parent.getEntries().get(0).getId());
    }
    
    public void testDelete()
        throws Exception
    {
        // retrieve test folder for deletes
        Entry testFolder = createTestFolder("testDelete");
        Link childrenLink = getChildrenLink(testFolder);
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

    public void testDeleteDescendants()
        throws Exception
    {
        // create multiple nested children
        Entry testFolder = createTestFolder("testDeleteDescendants");
        Link childrenLink = getChildrenLink(testFolder);
        assertNotNull(childrenLink);
        Entry document1 = createDocument(childrenLink.getHref(), "testDeleteDescendants1");
        assertNotNull(document1);
        Entry folder2 = createFolder(childrenLink.getHref(), "testDeleteDescendants2");
        assertNotNull(folder2);
        Link childrenLink2 = getChildrenLink(folder2);
        assertNotNull(childrenLink2);
        Entry document3 = createDocument(childrenLink2.getHref(), "testDeleteDescendants3");
        assertNotNull(document3);
        
        // delete on root of created tree
        Link descendantsLink = getDescendantsLink(testFolder);
        assertNotNull(descendantsLink);
        Response deleteRes = sendRequest(new DeleteRequest(descendantsLink.getHref().toString()), 204);
        assertNotNull(deleteRes);
    
        // ensure all have been deleted
        Response getRes1 = sendRequest(new GetRequest(testFolder.getSelfLink().getHref().toString()), 404);
        assertNotNull(getRes1);
        Response getRes2 = sendRequest(new GetRequest(document1.getSelfLink().getHref().toString()), 404);
        assertNotNull(getRes2);
        Response getRes3 = sendRequest(new GetRequest(folder2.getSelfLink().getHref().toString()), 404);
        assertNotNull(getRes3);
        Response getRes4 = sendRequest(new GetRequest(document3.getSelfLink().getHref().toString()), 404);
        assertNotNull(getRes4);
    }

    public void testDeleteFolderTree()
        throws Exception
    {
        // create multiple nested children
        Entry testFolder = createTestFolder("testDeleteFolderTree");
        Link childrenLink = getChildrenLink(testFolder);
        assertNotNull(childrenLink);
        Entry document1 = createDocument(childrenLink.getHref(), "testDeleteDescendants1");
        assertNotNull(document1);
        Entry folder2 = createFolder(childrenLink.getHref(), "testDeleteDescendants2");
        assertNotNull(folder2);
        Link childrenLink2 = getChildrenLink(folder2);
        assertNotNull(childrenLink2);
        Entry document3 = createDocument(childrenLink2.getHref(), "testDeleteDescendants3");
        assertNotNull(document3);
        
        // delete on root of created tree
        Link treeLink = getFolderTreeLink(testFolder);
        assertNotNull(treeLink);
        Response deleteRes = sendRequest(new DeleteRequest(treeLink.getHref().toString()), 204);
        assertNotNull(deleteRes);
    
        // ensure all have been deleted
        Response getRes1 = sendRequest(new GetRequest(testFolder.getSelfLink().getHref().toString()), 404);
        assertNotNull(getRes1);
        Response getRes2 = sendRequest(new GetRequest(document1.getSelfLink().getHref().toString()), 404);
        assertNotNull(getRes2);
        Response getRes3 = sendRequest(new GetRequest(folder2.getSelfLink().getHref().toString()), 404);
        assertNotNull(getRes3);
        Response getRes4 = sendRequest(new GetRequest(document3.getSelfLink().getHref().toString()), 404);
        assertNotNull(getRes4);
    }
    
    public void testUpdatePatch()
        throws Exception
    {
        // retrieve test folder for update
        Entry testFolder = createTestFolder("testUpdatePatch");
        Link childrenLink = getChildrenLink(testFolder);
        
        // create document for update
        Entry document = createDocument(childrenLink.getHref(), "testUpdatePatch");
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
        Response res = sendRequest(new PatchRequest(document.getSelfLink().getHref().toString(), updateFile, Format.ATOMENTRY.mimetype()), 200, getAtomValidator());
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
    
    public void testUpdatePut()
        throws Exception
    {
        // retrieve test folder for update
        Entry testFolder = createTestFolder("testUpdatePut");
        Link childrenLink = getChildrenLink(testFolder);
        
        // create document for update
        Entry document = createDocument(childrenLink.getHref(), "testUpdatePut");
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

    public void testUpdatePutAtomEntry()
        throws Exception
    {
        // retrieve test folder for update
        Entry testFolder = createTestFolder("testUpdatePutAtomEntry");
        Link childrenLink = getChildrenLink(testFolder);
        
        // create document for update
        Entry document = createDocument(childrenLink.getHref(), "testUpdatePutAtomEntry");
        assertNotNull(document);

        // edit title
        String updatedTitle = "Iñtërnâtiônàlizætiøn - 2";
        document.setTitle(updatedTitle);
        StringWriter writer = new StringWriter();
        document.writeTo(writer);

        // put document
        Response res = sendRequest(new PutRequest(document.getSelfLink().getHref().toString(), writer.toString(), Format.ATOMENTRY.mimetype()), 200, getAtomValidator());
        assertNotNull(res);
        Entry updated = getAbdera().parseEntry(new StringReader(res.getContentAsString()), null);
        assertEquals(updatedTitle, updated.getTitle());
    }

    public void testContentStreamEmpty()
        throws Exception
    {
        // retrieve test folder for content stream tests
        Entry testFolder = createTestFolder("testContentStreamEmpty");
        Link childrenLink = getChildrenLink(testFolder);
        
        // create document for setting / getting content
        Entry document = createDocument(childrenLink.getHref(), "testContent", "createdocumentNoContent.atomentry.xml");
    
        // retrieve content
        sendRequest(new GetRequest(document.getContentSrc().toString()), 404);
    }
    
    public void testContentStream()
        throws Exception
    {
        // retrieve test folder for content stream tests
        Entry testFolder = createTestFolder("testContentStream");
        Link childrenLink = getChildrenLink(testFolder);
        
        // create document for setting / getting content
        Entry document = createDocument(childrenLink.getHref(), "testContent");

        // retrieve content
        Response documentContentRes = sendRequest(new GetRequest(document.getContentSrc().toString()), 200);
        String resContent = documentContentRes.getContentAsString();
        assertEquals(document.getTitle(), resContent);

        // set content
        String UPDATED_CONTENT = "Updated via SetContentStream()";
        Link editMediaLink = document.getEditMediaLink();
        assertNotNull(editMediaLink);
        Response res = sendRequest(new PutRequest(editMediaLink.getHref().toString(), UPDATED_CONTENT, Format.TEXT.mimetype()), 200);
        assertNotNull(res);
        
        // retrieve updated content
        Response documentUpdatedContentRes = sendRequest(new GetRequest(document.getContentSrc().toString()), 200);
        String resUpdatedContent = documentUpdatedContentRes.getContentAsString();
        assertEquals(UPDATED_CONTENT, resUpdatedContent);
    }
    
    public void testDeleteContentStream()
        throws Exception
    {
        // retrieve test folder for content stream tests
        Entry testFolder = createTestFolder("testContentStreamEmpty");
        Link childrenLink = getChildrenLink(testFolder);

        // create document for setting / getting content
        Entry document = createDocument(childrenLink.getHref(), "testContent");

        // retrieve content
        Response documentContentRes = sendRequest(new GetRequest(document.getContentSrc().toString()), 200);
        String resContent = documentContentRes.getContentAsString();
        assertEquals(document.getTitle(), resContent);

        // delete content
        Link editMediaLink = document.getEditMediaLink();
        assertNotNull(editMediaLink);
        Response res = sendRequest(new DeleteRequest(editMediaLink.getHref().toString()), 204);
        assertNotNull(res);
        
        // retrieve deleted content
        sendRequest(new GetRequest(document.getContentSrc().toString()), 404);
    }
    
    public void testAllowableActions()
        throws Exception
    {
        // retrieve test folder for allowable actions
        Entry testFolder = createTestFolder("testAllowableActions");
        Link childrenLink = getChildrenLink(testFolder);
        assertNotNull(childrenLink);
        
        // test allowable actions for folder
        {
            Entry child = createFolder(childrenLink.getHref(), "testFolderAllowableActions");
            assertNotNull(child);
            Link allowableActionsLink = child.getLink(CMISConstants.REL_ALLOWABLE_ACTIONS);
            Response allowableActionsRes = sendRequest(new GetRequest(allowableActionsLink.getHref().toString()), 200, getAtomValidator());
            assertNotNull(allowableActionsRes);
            Element allowableActions = getAbdera().parse(new StringReader(allowableActionsRes.getContentAsString()), null);
            assertNotNull(allowableActions);
            assertTrue(allowableActions instanceof CMISAllowableActions);
            CMISObject childObject = child.getExtension(CMISConstants.OBJECT);
            assertNotNull(childObject);
            CMISAllowableActions objectAllowableActions = childObject.getExtension(CMISConstants.ALLOWABLE_ACTIONS);
            assertNotNull(objectAllowableActions);
            compareAllowableActions((CMISAllowableActions)allowableActions, objectAllowableActions);

            // retrieve getProperties() with includeAllowableActions flag
            Map<String, String> args = new HashMap<String, String>();
            args.put("includeAllowableActions", "true");
            Entry properties = getEntry(child.getSelfLink().getHref(), args);
            assertNotNull(properties);
            CMISObject propObject = properties.getExtension(CMISConstants.OBJECT);
            assertNotNull(propObject);
            CMISAllowableActions propAllowableActions = propObject.getExtension(CMISConstants.ALLOWABLE_ACTIONS);
            assertNotNull(propAllowableActions);
            compareAllowableActions((CMISAllowableActions)allowableActions, propAllowableActions);
        }

        // test allowable actions for document
        {
            Entry child = createDocument(childrenLink.getHref(), "testDocumentAllowableActions");
            assertNotNull(child);
            Link allowableActionsLink = child.getLink(CMISConstants.REL_ALLOWABLE_ACTIONS);
            Response allowableActionsRes = sendRequest(new GetRequest(allowableActionsLink.getHref().toString()), 200, getAtomValidator());
            assertNotNull(allowableActionsRes);
            Element allowableActions = getAbdera().parse(new StringReader(allowableActionsRes.getContentAsString()), null);
            assertNotNull(allowableActions);
            assertTrue(allowableActions instanceof CMISAllowableActions);
            CMISObject childObject = child.getExtension(CMISConstants.OBJECT);
            assertNotNull(childObject);
            CMISAllowableActions objectAllowableActions = childObject.getExtension(CMISConstants.ALLOWABLE_ACTIONS);
            assertNotNull(objectAllowableActions);
            compareAllowableActions((CMISAllowableActions)allowableActions, objectAllowableActions);

            // retrieve getProperties() with includeAllowableActions flag
            Map<String, String> args = new HashMap<String, String>();
            args.put("includeAllowableActions", "true");
            Entry properties = getEntry(child.getSelfLink().getHref(), args);
            assertNotNull(properties);
            CMISObject propObject = properties.getExtension(CMISConstants.OBJECT);
            assertNotNull(propObject);
            CMISAllowableActions propAllowableActions = propObject.getExtension(CMISConstants.ALLOWABLE_ACTIONS);
            assertNotNull(propAllowableActions);
            compareAllowableActions((CMISAllowableActions)allowableActions, propAllowableActions);
        }
        
        // test allowable actions for children
        {
            Map<String, String> args = new HashMap<String, String>();
            args.put("includeAllowableActions", "true");
            Feed children = getFeed(childrenLink.getHref(), args);
            assertNotNull(children);
            for (Entry child : children.getEntries())
            {
                // extract allowable actions from child
                CMISObject childObject = child.getExtension(CMISConstants.OBJECT);
                assertNotNull(childObject);
                CMISAllowableActions objectAllowableActions = childObject.getExtension(CMISConstants.ALLOWABLE_ACTIONS);
                assertNotNull(objectAllowableActions);
                
                // retrieve allowable actions from link
                Link allowableActionsLink = child.getLink(CMISConstants.REL_ALLOWABLE_ACTIONS);
                Response allowableActionsRes = sendRequest(new GetRequest(allowableActionsLink.getHref().toString()), 200, getAtomValidator());
                assertNotNull(allowableActionsRes);
                Element allowableActions = getAbdera().parse(new StringReader(allowableActionsRes.getContentAsString()), null);
                assertNotNull(allowableActions);
                assertTrue(allowableActions instanceof CMISAllowableActions);
                
                // compare the two
                compareAllowableActions((CMISAllowableActions)allowableActions, objectAllowableActions);
            }
        }
    }
    
    public void testGetCheckedOut()
        throws Exception
    {
        // retrieve test folder for checkouts
        Entry testFolder = createTestFolder("testGetCheckedOut");
        CMISObject object = testFolder.getExtension(CMISConstants.OBJECT);
        String scopeId = object.getObjectId().getStringValue();
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
        Link childrenLink = getChildrenLink(testFolder);
        
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
        assertEquals(docObject.getObjectId().getStringValue(), pwcObject.getVersionSeriesId().getStringValue());
        assertEquals(pwcObject.getObjectId().getStringValue(), pwcObject.getVersionSeriesCheckedOutId().getStringValue());
        assertNotNull(pwcObject.getVersionSeriesCheckedOutBy().getStringValue());
        
        // retrieve pwc directly
        Response pwcGetRes = sendRequest(new GetRequest(pwc.getSelfLink().getHref().toString()), 200);
        assertNotNull(pwcGetRes);
        String pwcGetXml = pwcRes.getContentAsString();
        Entry pwcGet = getAbdera().parseEntry(new StringReader(pwcGetXml), null);
        assertNotNull(pwcGet);
        CMISObject pwcGetObject = pwc.getExtension(CMISConstants.OBJECT);
        assertNotNull(pwcGetObject);
        assertTrue(pwcGetObject.isVersionSeriesCheckedOut().getBooleanValue());
        assertEquals(docObject.getObjectId().getStringValue(), pwcGetObject.getVersionSeriesId().getStringValue());
        assertEquals(pwcGetObject.getObjectId().getStringValue(), pwcGetObject.getVersionSeriesCheckedOutId().getStringValue());
        assertNotNull(pwcGetObject.getVersionSeriesCheckedOutBy().getStringValue());

        // test getCheckedOut is updated
        CMISObject object = testFolder.getExtension(CMISConstants.OBJECT);
        String scopeId = object.getObjectId().getStringValue();
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
        Link childrenLink = getChildrenLink(testFolder);
        
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
        String scopeId = object.getObjectId().getStringValue();
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
        String scopeId2 = object2.getObjectId().getStringValue();
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
        Link childrenLink = getChildrenLink(testFolder);
        
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
        String scopeId = object.getObjectId().getStringValue();
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
        //assertEquals(checkedoutdocObject.getObjectId().getStringValue(), checkedoutdocObject.getVersionSeriesId().getStringValue());
        assertNotNull(checkedoutdocObject.getVersionSeriesCheckedOutId().getStringValue());
        assertNotNull(checkedoutdocObject.getVersionSeriesCheckedOutBy().getStringValue());
        
        // test update of private working copy
        String updateFile = loadString("/org/alfresco/repo/cmis/rest/test/updatedocument.atomentry.xml");
        String guid = GUID.generate();
        updateFile = updateFile.replace("${NAME}", guid);
        Response pwcUpdatedres = sendRequest(new PatchRequest(pwc.getEditLink().getHref().toString(), updateFile, Format.ATOMENTRY.mimetype()), 200, getAtomValidator());
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
        Response checkinRes = sendRequest(new PatchRequest(checkinUrl, checkinFile, Format.ATOMENTRY.mimetype()).setArgs(args2), 200, getAtomValidator());
        assertNotNull(checkinRes);
        String checkinResXML = checkinRes.getContentAsString();
    
        // test getCheckedOut is updated
        CMISObject object2 = testFolder.getExtension(CMISConstants.OBJECT);
        String scopeId2 = object2.getObjectId().getStringValue();
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
        //assertEquals(updatedObject.getObjectId().getStringValue(), updatedObject.getVersionSeriesId().getStringValue());
        assertNull(updatedObject.getVersionSeriesCheckedOutId().getStringValue());
        assertNull(updatedObject.getVersionSeriesCheckedOutBy().getStringValue());
        assertEquals(guid, updatedObject.getCheckinComment().getStringValue());
    }

    public void testUpdateOnCheckIn()
        throws Exception
    {
        // retrieve test folder for checkins
        Entry testFolder = createTestFolder("testUpdateOnCheckIn");
        Link childrenLink = getChildrenLink(testFolder);
        
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
        String scopeId = object.getObjectId().getStringValue();
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
        Response checkinRes = sendRequest(new PatchRequest(checkinUrl, checkinFile, Format.ATOMENTRY.mimetype()).setArgs(args2), 200, getAtomValidator());
        assertNotNull(checkinRes);
        String checkinResXML = checkinRes.getContentAsString();
    
        // test getCheckedOut is updated
        CMISObject object2 = testFolder.getExtension(CMISConstants.OBJECT);
        String scopeId2 = object2.getObjectId().getStringValue();
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
        Link childrenLink = getChildrenLink(testFolder);
        
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
            
            // use result of checkin (i.e. document returned), for next checkout
            xml = checkinRes.getContentAsString();
            assertNotNull(xml);
        }

        // get all versions
        Link allVersionsLink = document.getLink(CMISConstants.REL_VERSION_HISTORY);
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
            assertEquals("checkin" + + (NUMBER_OF_VERSIONS -1 - i), versionObject.getCheckinComment().getStringValue());
        }
    }
    
    public void testGetTypeDefinitionsAll()
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

    public void testGetTypeDefinitionHierarchy()
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
        Link childrenLink = getChildrenLink(testFolder);
        
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
            Link typeLink = entry.getLink(CMISConstants.REL_DESCRIBED_BY);
            assertNotNull(typeLink);
            Entry type = getEntry(typeLink.getHref());
            assertNotNull(type);
            // TODO: test correct type for entry & properties of type
        }
    }
    
    public void testQuery()
        throws Exception
    {
        String queryCapability = getQueryCapability();
        if (queryCapability.equals("none"))
        {
            return;
        }
        
        // retrieve query collection
        IRI queryHREF = getQueryCollection(getWorkspace(getRepository()));
        
        // retrieve test folder for query
        Entry testFolder = createTestFolder("testQuery");
        CMISObject testFolderObject = testFolder.getExtension(CMISConstants.OBJECT);
        Link childrenLink = getChildrenLink(testFolder);
        
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

        if (queryCapability.equals("metadataonly") || queryCapability.equals("bothseperate") || queryCapability.equals("bothcombined"))
        {
            {
                // meta data only query against folder
                String query = "SELECT * FROM Folder " +
                               "WHERE ObjectId = '" + testFolderObject.getObjectId().getStringValue() + "'";
                String queryReq = queryDoc.replace("${STATEMENT}", query);
                queryReq = queryReq.replace("${SKIPCOUNT}", "0");
                queryReq = queryReq.replace("${PAGESIZE}", "5");
        
                Response queryRes = sendRequest(new PostRequest(queryHREF.toString(), queryReq, CMISConstants.MIMETYPE_QUERY), 200);
                assertNotNull(queryRes);
                Feed queryFeed = getAbdera().parseFeed(new StringReader(queryRes.getContentAsString()), null);
                assertNotNull(queryFeed);
                assertEquals(1, queryFeed.getEntries().size());
                assertNotNull(queryFeed.getEntry(testFolder.getId().toString()));
                CMISObject result1 = queryFeed.getEntry(testFolder.getId().toString()).getExtension(CMISConstants.OBJECT);
                assertEquals(testFolderObject.getName().getStringValue(), result1.getName().getStringValue());
                assertEquals(testFolderObject.getObjectId().getStringValue(), result1.getObjectId().getStringValue());
                assertEquals(testFolderObject.getObjectTypeId().getStringValue(), result1.getObjectTypeId().getStringValue());
            }
    
            {
                // meta data only query against document
                String query = "SELECT * FROM Document " +
                               "WHERE IN_FOLDER('" + testFolderObject.getObjectId().getStringValue() + "') " +
                               "AND Name = 'apple1'";
                String queryReq = queryDoc.replace("${STATEMENT}", query);
                queryReq = queryReq.replace("${SKIPCOUNT}", "0");
                queryReq = queryReq.replace("${PAGESIZE}", "5");
        
                Response queryRes = sendRequest(new PostRequest(queryHREF.toString(), queryReq, CMISConstants.MIMETYPE_QUERY), 200);
                assertNotNull(queryRes);
                Feed queryFeed = getAbdera().parseFeed(new StringReader(queryRes.getContentAsString()), null);
                assertNotNull(queryFeed);
                assertEquals(1, queryFeed.getEntries().size());
                assertNotNull(queryFeed.getEntry(document1.getId().toString()));
                CMISObject result1 = queryFeed.getEntry(document1.getId().toString()).getExtension(CMISConstants.OBJECT);
                assertEquals(document1Object.getName().getStringValue(), result1.getName().getStringValue());
                assertEquals(document1Object.getObjectId().getStringValue(), result1.getObjectId().getStringValue());
                assertEquals(document1Object.getObjectTypeId().getStringValue(), result1.getObjectTypeId().getStringValue());
            }
        }
        
        if (queryCapability.equals("fulltextonly") || queryCapability.equals("bothseperate") || queryCapability.equals("bothcombined"))
        {
            // full text only query
            String query = "SELECT ObjectId, ObjectTypeId, Name FROM Document " +
                           "WHERE CONTAINS('" + doc2name + "')";
            String queryReq = queryDoc.replace("${STATEMENT}", query);
            queryReq = queryReq.replace("${SKIPCOUNT}", "0");
            queryReq = queryReq.replace("${PAGESIZE}", "5");
    
            Response queryRes = sendRequest(new PostRequest(queryHREF.toString(), queryReq, CMISConstants.MIMETYPE_QUERY), 200);
            assertNotNull(queryRes);
            Feed queryFeed = getAbdera().parseFeed(new StringReader(queryRes.getContentAsString()), null);
            assertNotNull(queryFeed);
            assertEquals(1, queryFeed.getEntries().size());
            assertNotNull(queryFeed.getEntry(document2.getId().toString()));
            CMISObject result1 = queryFeed.getEntry(document2.getId().toString()).getExtension(CMISConstants.OBJECT);
            assertEquals(document2Object.getName().getStringValue(), result1.getName().getStringValue());
            assertEquals(document2Object.getObjectId().getStringValue(), result1.getObjectId().getStringValue());
            assertEquals(document2Object.getObjectTypeId().getStringValue(), result1.getObjectTypeId().getStringValue());
        }

        if (queryCapability.equals("bothcombined"))
        {
            // combined meta data and full text
            String query = "SELECT ObjectId, ObjectTypeId, Name FROM Document " +
                           "WHERE IN_FOLDER('" + testFolderObject.getObjectId().getStringValue() + "') " +
                           "AND Name = 'apple1' " +
                           "AND CONTAINS('apple1')";
            String queryReq = queryDoc.replace("${STATEMENT}", query);
            queryReq = queryReq.replace("${SKIPCOUNT}", "0");
            queryReq = queryReq.replace("${PAGESIZE}", "5");
    
            Response queryRes = sendRequest(new PostRequest(queryHREF.toString(), queryReq, CMISConstants.MIMETYPE_QUERY), 200);
            assertNotNull(queryRes);
            Feed queryFeed = getAbdera().parseFeed(new StringReader(queryRes.getContentAsString()), null);
            assertNotNull(queryFeed);
            assertEquals(1, queryFeed.getEntries().size());
            assertNotNull(queryFeed.getEntry(document1.getId().toString()));
            CMISObject result1 = queryFeed.getEntry(document1.getId().toString()).getExtension(CMISConstants.OBJECT);
            assertEquals(document1Object.getName().getStringValue(), result1.getName().getStringValue());
            assertEquals(document1Object.getObjectId().getStringValue(), result1.getObjectId().getStringValue());
            assertEquals(document1Object.getObjectTypeId().getStringValue(), result1.getObjectTypeId().getStringValue());
        }
    }
    
    public void testQueryPaging() throws Exception
    {
        String queryCapability = getQueryCapability();
        if (queryCapability.equals("none"))
        {
            return;
        }
        
        // retrieve query collection
        IRI queryHREF = getQueryCollection(getWorkspace(getRepository()));

        // create multiple children
        Set<IRI> docIds = new HashSet<IRI>();
        Entry testFolder = createTestFolder("testQueryPaging");
        Link childrenLink = getChildrenLink(testFolder);
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
        String query = "SELECT ObjectId, ObjectTypeId, Name FROM Document " + "WHERE IN_FOLDER('" + testFolderObject.getObjectId().getStringValue() + "')";

        for (int page = 0; page < 4; page++)
        {
            String queryReq = queryDoc.replace("${STATEMENT}", query);
            queryReq = queryReq.replace("${SKIPCOUNT}", new Integer(page * 4).toString());
            queryReq = queryReq.replace("${PAGESIZE}", "4");
            Response queryRes = sendRequest(new PostRequest(queryHREF.toString(), queryReq, CMISConstants.MIMETYPE_QUERY), 200);
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
        String queryCapability = getQueryCapability();
        if (queryCapability.equals("none"))
        {
            return;
        }
        
        // retrieve query collection
        IRI queryHREF = getQueryCollection(getWorkspace(getRepository()));
        
        // retrieve test folder for query
        Entry testFolder = createTestFolder("testQueryAllowableAcrtions");
        CMISObject testFolderObject = testFolder.getExtension(CMISConstants.OBJECT);
        Link childrenLink = getChildrenLink(testFolder);
        
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
                           "WHERE IN_FOLDER('" + testFolderObject.getObjectId().getStringValue() + "') ";
            String queryReq = queryDoc.replace("${STATEMENT}", query);
            queryReq = queryReq.replace("${INCLUDEALLOWABLEACTIONS}", "true");
            queryReq = queryReq.replace("${SKIPCOUNT}", "0");
            queryReq = queryReq.replace("${PAGESIZE}", "5");
    
            // issue structured query
            Response queryRes = sendRequest(new PostRequest(queryHREF.toString(), queryReq, CMISConstants.MIMETYPE_QUERY), 200);
            assertNotNull(queryRes);
            Feed queryFeed = getAbdera().parseFeed(new StringReader(queryRes.getContentAsString()), null);
            assertNotNull(queryFeed);
            assertEquals(3, queryFeed.getEntries().size());
            
            for (Entry child : queryFeed.getEntries())
            {
                // extract allowable actions from child
                CMISObject childObject = child.getExtension(CMISConstants.OBJECT);
                assertNotNull(childObject);
                CMISAllowableActions childAllowableActions = childObject.getExtension(CMISConstants.ALLOWABLE_ACTIONS);
                assertNotNull(childAllowableActions);
                
                // retrieve allowable actions from link
                Map<String, String> args = new HashMap<String, String>();
                args.put("includeAllowableActions", "true");
                Entry entry = getEntry(child.getSelfLink().getHref(), args);
                CMISObject entryObject = entry.getExtension(CMISConstants.OBJECT);
                assertNotNull(entryObject);
                CMISAllowableActions entryAllowableActions = entryObject.getExtension(CMISConstants.ALLOWABLE_ACTIONS);
                
                // compare the two
                compareAllowableActions(childAllowableActions, entryAllowableActions);
            }
        }
    }

    
//    public void testUnfiled()
//    {
//    }

    
    /**
     * Compare two sets of allowable actions
     */
    private void compareAllowableActions(CMISAllowableActions left, CMISAllowableActions right)
    {
        List<String> rightactions = new ArrayList<String>(right.getNames());
        for (String action : left.getNames())
        {
            assertTrue(rightactions.contains(action));
            CMISAllowableAction leftAction = left.find(action);
            assertNotNull(leftAction);
            CMISAllowableAction rightAction = right.find(action);
            assertNotNull(rightAction);
            assertEquals(leftAction.isAllowed(), rightAction.isAllowed());
            rightactions.remove(action);
        }
        assertTrue(rightactions.size() == 0);
    }
    
}
