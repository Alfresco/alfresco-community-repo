/*
 *
 * This program is free software; you can redistribute it and/or
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
import java.util.List;
import java.util.Map;

import org.alfresco.abdera.ext.cmis.CMISConstants;
import org.alfresco.abdera.ext.cmis.CMISObject;
import org.alfresco.abdera.ext.cmis.CMISProperties;
import org.alfresco.abdera.ext.cmis.CMISProperty;
import org.alfresco.repo.cmis.rest.CMISScript;
import org.alfresco.util.GUID;
import org.alfresco.web.scripts.Format;
import org.alfresco.web.scripts.TestWebScriptServer.DeleteRequest;
import org.alfresco.web.scripts.TestWebScriptServer.GetRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PatchRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PostRequest;
import org.alfresco.web.scripts.TestWebScriptServer.PutRequest;
import org.alfresco.web.scripts.TestWebScriptServer.Response;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;


/**
 * CMIS API Test Harness
 * 
 * @author davidc
 */
public class CMISCustomTypeTest extends BaseCMISWebScriptTest
{
    private static String TEST_NAMESPACE = "http://www.alfresco.org/model/cmis/custom";
    
    
    @Override
    protected void setUp()
        throws Exception
    {
        // Uncomment to change default behaviour of tests
        setCustomContext("classpath:cmis/cmis-test-context.xml");
        setDefaultRunAs("admin");
//      RemoteServer server = new RemoteServer();
//      server.username = "admin";
//      server.password = "admin";
//      setRemoteServer(server);
//      setArgsAsHeaders(false);
//      setValidateResponse(false);
//      setListener(new CMISTestListener(System.out));
//      setTraceReqRes(true);

        super.setUp();
    }

    
    public void testCreateFolder()
        throws Exception
    {
        Entry testFolder = createTestFolder("testCreateCustomFolder");
        Link childrenLink = getChildrenLink(testFolder);
        assertNotNull(childrenLink);
        Feed children = getFeed(childrenLink.getHref());
        assertNotNull(children);
        int entriesBefore = children.getEntries().size();
        Entry folder = createFolder(children.getSelfLink().getHref(), "testCreateCustomFolder", "/org/alfresco/repo/cmis/rest/test/createcustomfolder.atomentry.xml");
        Feed feedFolderAfter = getFeed(childrenLink.getHref());
        int entriesAfter = feedFolderAfter.getEntries().size();
        assertEquals(entriesBefore +1, entriesAfter);
        Entry entry = feedFolderAfter.getEntry(folder.getId().toString());
        CMISObject object = entry.getExtension(CMISConstants.OBJECT);
        assertEquals("F/cmiscustom:folder", object.getObjectTypeId().getStringValue());
        CMISProperty customProp = object.getProperties().find("cmiscustom:folderprop_string");
        assertNotNull(customProp);
        assertEquals("custom string", customProp.getStringValue());
    }

    public void testCreateDocument()
        throws Exception
    {
        Entry testFolder = createTestFolder("testCreateCustomDocument");
        Link childrenLink = getChildrenLink(testFolder);
        assertNotNull(childrenLink);
        Feed children = getFeed(childrenLink.getHref());
        assertNotNull(children);
        int entriesBefore = children.getEntries().size();
        Entry document = createDocument(children.getSelfLink().getHref(), "testCreateCustomDocument", "/org/alfresco/repo/cmis/rest/test/createcustomdocument.atomentry.xml");
        Feed feedFolderAfter = getFeed(childrenLink.getHref());
        int entriesAfter = feedFolderAfter.getEntries().size();
        assertEquals(entriesBefore +1, entriesAfter);
        Entry entry = feedFolderAfter.getEntry(document.getId().toString());
        CMISObject object = entry.getExtension(CMISConstants.OBJECT);
        assertEquals("D/cmiscustom:document", object.getObjectTypeId().getStringValue());
        CMISProperty customProp = object.getProperties().find("cmiscustom:docprop_string");
        assertNotNull(customProp);
        assertEquals("custom string", customProp.getStringValue());
        CMISProperty multiProp = object.getProperties().find("cmiscustom:docprop_boolean_multi");
        assertNotNull(multiProp);
        List<Object> multiValues = multiProp.getNativeValues();
        assertNotNull(multiValues);
        assertEquals(2, multiValues.size());
        assertEquals(true, multiValues.get(0));
        assertEquals(false, multiValues.get(1));
    }

    public void testUpdatePatch()
        throws Exception
    {
        // retrieve test folder for update
        Entry testFolder = createTestFolder("testUpdatePatchCustomDocument");
        Link childrenLink = getChildrenLink(testFolder);
        
        // create document for update
        Entry document = createDocument(childrenLink.getHref(), "testUpdatePatchCustomDocument", "/org/alfresco/repo/cmis/rest/test/createcustomdocument.atomentry.xml");
        assertNotNull(document);
        
        // update
        String updateFile = loadString("/org/alfresco/repo/cmis/rest/test/updatecustomdocument.atomentry.xml");
        String guid = GUID.generate();
        updateFile = updateFile.replace("${NAME}", guid);
        Response res = sendRequest(new PatchRequest(document.getSelfLink().getHref().toString(), updateFile, Format.ATOMENTRY.mimetype()), 200, getAtomValidator());
        assertNotNull(res);
        Entry updated = getAbdera().parseEntry(new StringReader(res.getContentAsString()), null);
        
        // ensure update occurred
        assertEquals(document.getId(), updated.getId());
        assertEquals(document.getPublished(), updated.getPublished());
        assertEquals("Updated Title " + guid, updated.getTitle());
        CMISObject object = updated.getExtension(CMISConstants.OBJECT);
        assertEquals("D/cmiscustom:document", object.getObjectTypeId().getStringValue());
        CMISProperty customProp = object.getProperties().find("cmiscustom:docprop_string");
        assertNotNull(customProp);
        assertEquals("custom " + guid, customProp.getStringValue());
        CMISProperty multiProp = object.getProperties().find("cmiscustom:docprop_boolean_multi");
        assertNotNull(multiProp);
        List<Object> multiValues = multiProp.getNativeValues();
        assertNotNull(multiValues);
        assertEquals(2, multiValues.size());
        assertEquals(false, multiValues.get(0));
        assertEquals(true, multiValues.get(1));
    }
    
    public void testUpdatePut()
        throws Exception
    {
        // retrieve test folder for update
        Entry testFolder = createTestFolder("testUpdatePutCustomDocument");
        Link childrenLink = getChildrenLink(testFolder);
        
        // create document for update
        Entry document = createDocument(childrenLink.getHref(), "testUpdatePutCustomDocument", "/org/alfresco/repo/cmis/rest/test/createcustomdocument.atomentry.xml");
        assertNotNull(document);
        
        // update
        String updateFile = loadString("/org/alfresco/repo/cmis/rest/test/updatecustomdocument.atomentry.xml");
        String guid = GUID.generate();
        updateFile = updateFile.replace("${NAME}", guid);
        Response res = sendRequest(new PutRequest(document.getSelfLink().getHref().toString(), updateFile, Format.ATOMENTRY.mimetype()), 200, getAtomValidator());
        assertNotNull(res);
        Entry updated = getAbdera().parseEntry(new StringReader(res.getContentAsString()), null);
        
        // ensure update occurred
        assertEquals(document.getId(), updated.getId());
        assertEquals(document.getPublished(), updated.getPublished());
        assertEquals("Updated Title " + guid, updated.getTitle());
        CMISObject object = updated.getExtension(CMISConstants.OBJECT);
        assertEquals("D/cmiscustom:document", object.getObjectTypeId().getStringValue());
        CMISProperty customProp = object.getProperties().find("cmiscustom:docprop_string");
        assertNotNull(customProp);
        assertEquals("custom " + guid, customProp.getStringValue());
        CMISProperty multiProp = object.getProperties().find("cmiscustom:docprop_boolean_multi");
        assertNotNull(multiProp);
        List<Object> multiValues = multiProp.getNativeValues();
        assertNotNull(multiValues);
        assertEquals(2, multiValues.size());
        assertEquals(false, multiValues.get(0));
        assertEquals(true, multiValues.get(1));
    }
 
    public void testDelete()
        throws Exception
    {
        // retrieve test folder for deletes
        Entry testFolder = createTestFolder("testDeleteCustom");
        Link childrenLink = getChildrenLink(testFolder);
        Feed children = getFeed(childrenLink.getHref());
        int entriesBefore = children.getEntries().size();
        
        // create document for delete
        Entry document = createDocument(childrenLink.getHref(), "testDeleteCustomDocument", "/org/alfresco/repo/cmis/rest/test/createcustomdocument.atomentry.xml");
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
    
    public void testQuery()
        throws Exception
    {
        // retrieve query collection
        IRI queryHREF = getQueryCollection(getWorkspace(getRepository()));
        
        // retrieve test folder for query
        Entry testFolder = createTestFolder("testQueryCustom");
        CMISObject testFolderObject = testFolder.getExtension(CMISConstants.OBJECT);
        Link childrenLink = getChildrenLink(testFolder);
        
        // create documents to query
        // Standard root document
        Entry document1 = createDocument(childrenLink.getHref(), "apple1");
        assertNotNull(document1);
        CMISObject document1Object = document1.getExtension(CMISConstants.OBJECT);
        assertNotNull(document1Object);
        String doc2name = "name" + System.currentTimeMillis();
        // Custom documents
        Entry document2 = createDocument(childrenLink.getHref(), doc2name, "/org/alfresco/repo/cmis/rest/test/createcustomdocument.atomentry.xml");
        assertNotNull(document2);
        CMISObject document2Object = document2.getExtension(CMISConstants.OBJECT);
        assertNotNull(document2Object);
        Entry document3 = createDocument(childrenLink.getHref(), "banana1", "/org/alfresco/repo/cmis/rest/test/createcustomdocument.atomentry.xml");
        assertNotNull(document3);
        CMISObject document3Object = document3.getExtension(CMISConstants.OBJECT);
        assertNotNull(document3Object);
    
        // retrieve query request document
        String queryDoc = loadString("/org/alfresco/repo/cmis/rest/test/query.cmisquery.xml");
    
        {
            // construct structured query
            String query = "SELECT ObjectId, Name, ObjectTypeId, cmiscustom_docprop_string, cmiscustom_docprop_boolean_multi FROM cmiscustom_document " +
                           "WHERE IN_FOLDER('" + testFolderObject.getObjectId().getStringValue() + "') " +
                           "AND cmiscustom_docprop_string = 'custom string' ";
            String queryReq = queryDoc.replace("${STATEMENT}", query);
            queryReq = queryReq.replace("${SKIPCOUNT}", "0");
            queryReq = queryReq.replace("${PAGESIZE}", "5");
    
            // issue structured query
            Response queryRes = sendRequest(new PostRequest(queryHREF.toString(), queryReq, CMISConstants.MIMETYPE_CMIS_QUERY), 200);
            assertNotNull(queryRes);
            Feed queryFeed = getAbdera().parseFeed(new StringReader(queryRes.getContentAsString()), null);
            assertNotNull(queryFeed);
            assertEquals(2, queryFeed.getEntries().size());
            
            assertNotNull(queryFeed.getEntry(document2.getId().toString()));
            CMISObject result1 = queryFeed.getEntry(document2.getId().toString()).getExtension(CMISConstants.OBJECT);
            assertNotNull(result1);
            assertEquals(document2Object.getName().getStringValue(), result1.getName().getStringValue());
            assertEquals(document2Object.getObjectId().getStringValue(), result1.getObjectId().getStringValue());
            assertEquals(document2Object.getObjectTypeId().getStringValue(), result1.getObjectTypeId().getStringValue());
            CMISProperties result1properties = result1.getProperties();
            assertNotNull(result1properties);
            CMISProperty result1property = result1properties.find("cmiscustom:docprop_string");
            assertNotNull(result1property);
            assertEquals("custom string", result1property.getStringValue());
            CMISProperty result1multiproperty = result1properties.find("cmiscustom:docprop_boolean_multi");
            assertNotNull(result1multiproperty);
            List<Object> result1multiValues = result1multiproperty.getNativeValues();
            assertNotNull(result1multiValues);
            assertEquals(2, result1multiValues.size());
            assertEquals(true, result1multiValues.get(0));
            assertEquals(false, result1multiValues.get(1));
            
            assertNotNull(queryFeed.getEntry(document3.getId().toString()));
            CMISObject result2 = queryFeed.getEntry(document3.getId().toString()).getExtension(CMISConstants.OBJECT);
            assertNotNull(result2);
            assertEquals(document3Object.getName().getStringValue(), result2.getName().getStringValue());
            assertEquals(document3Object.getObjectId().getStringValue(), result2.getObjectId().getStringValue());
            assertEquals(document3Object.getObjectTypeId().getStringValue(), result2.getObjectTypeId().getStringValue());
            CMISProperties result2properties = result2.getProperties();
            assertNotNull(result2properties);
            CMISProperty result2property = result2properties.find("cmiscustom:docprop_string");
            assertNotNull(result2property);
            assertEquals("custom string", result2property.getStringValue());
            CMISProperty result2multiproperty = result1properties.find("cmiscustom:docprop_boolean_multi");
            assertNotNull(result2multiproperty);
            List<Object> result2multiValues = result2multiproperty.getNativeValues();
            assertNotNull(result2multiValues);
            assertEquals(2, result2multiValues.size());
            assertEquals(true, result2multiValues.get(0));
            assertEquals(false, result2multiValues.get(1));
        }
    }

    public void testCreateRelationship()
        throws Exception
    {
        Entry testFolder = createTestFolder("testCreateCustomRelationship");
        Link childrenLink = getChildrenLink(testFolder);
        assertNotNull(childrenLink);
        Feed children = getFeed(childrenLink.getHref());
        assertNotNull(children);
        Entry source = createDocument(children.getSelfLink().getHref(), "testSource", "/org/alfresco/repo/cmis/rest/test/createcustomdocument.atomentry.xml");
        assertNotNull(source);
        Entry target = createDocument(children.getSelfLink().getHref(), "testTarget", "/org/alfresco/repo/cmis/rest/test/createcustomdocument.atomentry.xml");
        assertNotNull(target);

        // retrieve relationships feed on source
        Link relsLink = source.getLink(CMISConstants.REL_RELATIONSHIPS);
        assertNotNull(relsLink);
        Feed relsBefore = getFeed(relsLink.getHref());
        assertNotNull(relsBefore);
        assertEquals(0, relsBefore.getEntries().size());
        
        // create relationship between source and target documents
        CMISObject targetObject = target.getExtension(CMISConstants.OBJECT);
        assertNotNull(targetObject);
        String targetId = targetObject.getObjectId().getStringValue();
        assertNotNull(targetId);
        Entry rel = createRelationship(relsLink.getHref(), "R/cmiscustom:assoc", targetId);
        assertNotNull(rel);

        // check created relationship
        CMISObject sourceObject = source.getExtension(CMISConstants.OBJECT);
        assertNotNull(sourceObject);
        String sourceId = sourceObject.getObjectId().getStringValue();
        assertNotNull(sourceId);
        CMISObject relObject = rel.getExtension(CMISConstants.OBJECT);
        assertNotNull(relObject);
        assertEquals("R/cmiscustom:assoc", relObject.getObjectTypeId().getStringValue());
        assertEquals(sourceId, relObject.getSourceId().getStringValue());
        assertEquals(targetId, relObject.getTargetId().getStringValue());
        assertEquals(source.getSelfLink().getHref(), rel.getLink(CMISConstants.REL_ASSOC_SOURCE).getHref());
        assertEquals(target.getSelfLink().getHref(), rel.getLink(CMISConstants.REL_ASSOC_TARGET).getHref());

        // check relationships for created item
        Map<String, String> args = new HashMap<String, String>();
        args.put(CMISScript.ARG_INCLUDE_SUB_RELATIONSHIP_TYPES, "true");
        Feed relsAfter = getFeed(relsLink.getHref(), args);
        assertNotNull(relsAfter);
        assertEquals(1, relsAfter.getEntries().size());
    }

    public void testGetRelationship()
        throws Exception
    {
        Entry testFolder = createTestFolder("testGetCustomRelationship");
        Link childrenLink = getChildrenLink(testFolder);
        assertNotNull(childrenLink);
        Feed children = getFeed(childrenLink.getHref());
        assertNotNull(children);
        Entry source = createDocument(children.getSelfLink().getHref(), "testSource", "/org/alfresco/repo/cmis/rest/test/createcustomdocument.atomentry.xml");
        assertNotNull(source);
        Entry target = createDocument(children.getSelfLink().getHref(), "testTarget", "/org/alfresco/repo/cmis/rest/test/createcustomdocument.atomentry.xml");
        assertNotNull(target);
    
        // retrieve relationships feed on source
        Link relsLink = source.getLink(CMISConstants.REL_RELATIONSHIPS);
        assertNotNull(relsLink);
        
        // create relationship between source and target documents
        CMISObject targetObject = target.getExtension(CMISConstants.OBJECT);
        assertNotNull(targetObject);
        String targetId = targetObject.getObjectId().getStringValue();
        assertNotNull(targetId);
        Entry rel = createRelationship(relsLink.getHref(), "R/cmiscustom:assoc", targetId);
        assertNotNull(rel);
    
        // get created relationship
        Entry relEntry = getEntry(rel.getSelfLink().getHref());
        CMISObject relEntryObject = rel.getExtension(CMISConstants.OBJECT);
        CMISObject relObject = rel.getExtension(CMISConstants.OBJECT);
        assertNotNull(relObject);
        assertEquals(relObject.getObjectTypeId().getStringValue(), relEntryObject.getObjectTypeId().getStringValue());
        assertEquals(relObject.getSourceId().getStringValue(), relEntryObject.getSourceId().getStringValue());
        assertEquals(relObject.getTargetId().getStringValue(), relEntryObject.getTargetId().getStringValue());
        assertEquals(source.getSelfLink().getHref(), relEntry.getLink(CMISConstants.REL_ASSOC_SOURCE).getHref());
        assertEquals(target.getSelfLink().getHref(), relEntry.getLink(CMISConstants.REL_ASSOC_TARGET).getHref());
    }
    
    public void testDeleteRelationship()
        throws Exception
    {
        Entry testFolder = createTestFolder("testDeleteCustomRelationship");
        Link childrenLink = getChildrenLink(testFolder);
        assertNotNull(childrenLink);
        Feed children = getFeed(childrenLink.getHref());
        assertNotNull(children);
        Entry source = createDocument(children.getSelfLink().getHref(), "testSource", "/org/alfresco/repo/cmis/rest/test/createcustomdocument.atomentry.xml");
        assertNotNull(source);
        Entry target = createDocument(children.getSelfLink().getHref(), "testTarget", "/org/alfresco/repo/cmis/rest/test/createcustomdocument.atomentry.xml");
        assertNotNull(target);
    
        // retrieve relationships feed on source
        Link relsLink = source.getLink(CMISConstants.REL_RELATIONSHIPS);
        assertNotNull(relsLink);
        Feed relsBefore = getFeed(relsLink.getHref());
        assertNotNull(relsBefore);
        assertEquals(0, relsBefore.getEntries().size());
        
        // create relationship between source and target documents
        CMISObject targetObject = target.getExtension(CMISConstants.OBJECT);
        assertNotNull(targetObject);
        String targetId = targetObject.getObjectId().getStringValue();
        assertNotNull(targetId);
        Entry rel = createRelationship(relsLink.getHref(), "R/cmiscustom:assoc", targetId);
        assertNotNull(rel);
    
        // check relationships for created item
        Map<String, String> args = new HashMap<String, String>();
        args.put(CMISScript.ARG_INCLUDE_SUB_RELATIONSHIP_TYPES, "true");
        Feed relsAfterCreate = getFeed(relsLink.getHref(), args);
        assertNotNull(relsAfterCreate);
        assertEquals(1, relsAfterCreate.getEntries().size());
        
        // delete relationship
        Response deleteRes = sendRequest(new DeleteRequest(rel.getSelfLink().getHref().toString()), 204);
        assertNotNull(deleteRes);
        
        // check relationships for deleted item
        Feed relsAfterDelete = getFeed(relsLink.getHref(), args);
        assertNotNull(relsAfterDelete);
        assertEquals(0, relsAfterDelete.getEntries().size());
    }
    
}
