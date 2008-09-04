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

import org.alfresco.util.GUID;
import org.alfresco.web.scripts.Format;
import org.alfresco.web.scripts.atom.AbderaService;
import org.alfresco.web.scripts.atom.AbderaServiceImpl;
import org.apache.abdera.ext.cmis.CMISExtensionFactory;
import org.apache.abdera.ext.cmis.CMISProperties;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.model.Service;
import org.springframework.mock.web.MockHttpServletResponse;


/**
 * CMIS API Test Harness
 * 
 * @author davidc
 */
public class TestCMIS extends CMISWebScriptTest
{
    private AbderaService abdera;
    
    private static Service service = null;
    private static Entry testFolder = null;
    
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        AbderaServiceImpl abderaImpl = new AbderaServiceImpl();
        abderaImpl.afterPropertiesSet();
        abderaImpl.registerExtensionFactory(new CMISExtensionFactory());
        abdera = abderaImpl;
    }

    private Service getRepository()
        throws Exception
    {
        if (service == null)
        {
            MockHttpServletResponse res = getRequest("/api/repository", 200, null);
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

    private Entry getTestFolder()
        throws Exception
    {
        if (testFolder == null)
        {
            testFolder = createTestFolder();
        }
        return testFolder;
    }

    private Entry getEntry(IRI href)
        throws Exception
    {
        MockHttpServletResponse res = getRequest(href.toString(), 200, "admin");
        assertNotNull(res);
        String xml = res.getContentAsString();
        Entry entry = abdera.parseEntry(new StringReader(xml), null);
        assertNotNull(entry);
        assertEquals(href, entry.getSelfLink().getHref());
        return entry;
    }

    private Feed getFeed(IRI href)
        throws Exception
    {
        MockHttpServletResponse res = getRequest(href.toString(), 200, "admin");
        assertNotNull(res);
        String xml = res.getContentAsString();
        Feed feed = abdera.parseFeed(new StringReader(xml), null);
        assertNotNull(feed);
        assertEquals(href, feed.getSelfLink().getHref());
        return feed;
    }
    
    private Entry createTestFolder()
        throws Exception
    {
        Service service = getRepository();
        IRI rootFolderHREF = getRootCollection(service);
        String createFolder = loadString("/cmis/rest/createtestfolder.atomentry.xml");
        String guid = GUID.generate();
        createFolder = createFolder.replace("${GUID}", guid);
        MockHttpServletResponse res = postRequest(rootFolderHREF.toString(), 201, createFolder, Format.ATOMENTRY.mimetype(), "admin");
        assertNotNull(res);
        String xml = res.getContentAsString();
        Entry entry = abdera.parseEntry(new StringReader(xml), null);
        assertNotNull(entry);
        assertEquals("CMIS Test Folder " + guid, entry.getTitle());
        assertEquals("CMIS Test Folder " + guid + " Summary", entry.getSummary());
        CMISProperties props = entry.getExtension(CMISExtensionFactory.PROPERTIES);
        assertEquals("folder", props.getBaseType());
        String testFolderHREF = (String)res.getHeader("Location");
        assertNotNull(testFolderHREF);
        return entry;
    }

    private Entry createTestDocument(IRI parent)
        throws Exception
    {
        String createFile = loadString("/cmis/rest/createtestdocument.atomentry.xml");
        String guid = GUID.generate();
        createFile = createFile.replace("${GUID}", guid);
        MockHttpServletResponse res = postRequest(parent.toString(), 201, createFile, Format.ATOMENTRY.mimetype(), "admin");
        assertNotNull(res);
        String xml = res.getContentAsString();
        Entry entry = abdera.parseEntry(new StringReader(xml), null);
        assertNotNull(entry);
        assertEquals("Test Document " + guid, entry.getTitle());
        assertEquals("Test Document " + guid + " Summary", entry.getSummary());
        assertNotNull(entry.getContentSrc());
        CMISProperties props = entry.getExtension(CMISExtensionFactory.PROPERTIES);
        assertEquals("document", props.getBaseType());
        String testFileHREF = (String)res.getHeader("Location");
        assertNotNull(testFileHREF);
        return entry;
    }

    public void testRepository()
        throws Exception
    {
        Service service = getRepository();
        IRI rootHREF = getRootCollection(service);
        getRequest(rootHREF.toString(), 200, "admin");
    }
    
    public void testCreateTestFolder()
        throws Exception
    {
        createTestFolder();
    }

    public void testCreateDocument()
        throws Exception
    {
        Entry testFolder = getTestFolder();
        Link childrenLink = testFolder.getLink("cmis-children");
        assertNotNull(childrenLink);
        Feed children = getFeed(childrenLink.getHref());
        assertNotNull(children);
        int entriesBefore = children.getEntries().size();
        Entry document = createTestDocument(children.getSelfLink().getHref());
        Feed feedFolderAfter = getFeed(childrenLink.getHref());
        int entriesAfter = feedFolderAfter.getEntries().size();
        assertEquals(entriesBefore +1, entriesAfter);
        Entry entry = feedFolderAfter.getEntry(document.getId().toString());
        assertNotNull(entry);
    }
    
    public void testGetCheckedOut()
        throws Exception
    {
        // retrieve test folder for checkouts
        Entry testFolder = getTestFolder();
        Link childrenLink = testFolder.getLink("cmis-children");
        Feed scope = getFeed(childrenLink.getHref());
        assertNotNull(scope);
        CMISProperties props = scope.getExtension(CMISExtensionFactory.PROPERTIES);
        String scopeId = props.getObjectId();
        assertNotNull(scopeId);
        
        // retrieve checkouts within scope of test checkout folder
        Service repository = getRepository();
        assertNotNull(repository);
        IRI checkedoutHREF = getCheckedOutCollection(service);
        Feed checkedout = getFeed(new IRI(checkedoutHREF.toString() + "?folderId=" + scopeId));
        assertNotNull(checkedout);
        assertEquals(0, checkedout.getEntries().size());
    }
    
    public void testCheckout()
        throws Exception
    {
        // retrieve test folder for checkouts
        Entry testFolder = getTestFolder();
        Link childrenLink = testFolder.getLink("cmis-children");
        Feed scope = getFeed(childrenLink.getHref());
        
        // create document for checkout
        Entry document = createTestDocument(scope.getSelfLink().getHref());
        MockHttpServletResponse documentRes = getRequest(document.getSelfLink().getHref().toString(), 200, "admin");
        assertNotNull(documentRes);
        String xml = documentRes.getContentAsString();
        assertNotNull(xml);
        
        // checkout
        IRI checkedoutHREF = getCheckedOutCollection(service);
        MockHttpServletResponse checkoutRes = postRequest(checkedoutHREF.toString(), 201, xml, Format.ATOMENTRY.mimetype(), "admin");
        assertNotNull(checkoutRes);
        // TODO: test private working copy properties

        // test getCheckedOut is updated
        CMISProperties props = testFolder.getExtension(CMISExtensionFactory.PROPERTIES);
        String scopeId = props.getObjectId();
        Feed checkedout = getFeed(new IRI(checkedoutHREF.toString() + "?folderId=" + scopeId));
        assertNotNull(checkedout);
        assertEquals(1, checkedout.getEntries().size());
    }
    
}
