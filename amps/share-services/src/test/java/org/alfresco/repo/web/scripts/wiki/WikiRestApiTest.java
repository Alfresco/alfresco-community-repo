/*
 * Copyright 2005 - 2020 Alfresco Software Limited.
 *
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of the paid license agreement will prevail.
 * Otherwise, the software is provided under the following open source license terms:
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
package org.alfresco.repo.web.scripts.wiki;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteRole;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.wiki.WikiPageInfo;
import org.alfresco.service.cmr.wiki.WikiService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.DeleteRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PostRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.PutRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Unit Test to test the Wiki Web Script API
 * 
 * @author Nick Burch
 * @since 4.0
 */
public class WikiRestApiTest extends BaseWebScriptTest
{
    @SuppressWarnings("unused")
    private static Log logger = LogFactory.getLog(WikiRestApiTest.class);

    private MutableAuthenticationService authenticationService;
    private AuthenticationComponent authenticationComponent;
    private TransactionService transactionService;
    private BehaviourFilter policyBehaviourFilter;
    private PersonService personService;
    private NodeService nodeService;
    private NodeService internalNodeService;
    private SiteService siteService;
    private WikiService wikiService;
    private NodeArchiveService nodeArchiveService;
    
    private static final String USER_ONE = "UserOneSecondToo";
    private static final String USER_TWO = "UserTwoSecondToo";
    private static final String USERDETAILS_FIRSTNAME = "FirstName123";
    private static final String USERDETAILS_LASTNAME = "LastName123";
    private static final String SITE_SHORT_NAME_WIKI = "WikiSiteShortNameTest";
    
    private static final String PAGE_TITLE_ONE   = "TestPageOne";
    private static final String PAGE_TITLE_TWO   = "Test_Page_Two";
    private static final String PAGE_TITLE_THREE = "Still_Test_Page_Three";
    private static final String PAGE_CONTENTS_ONE   = "http://google.com/";
    private static final String PAGE_CONTENTS_TWO   = "http://alfresco.com/";
    private static final String PAGE_CONTENTS_THREE = "http://share.alfresco.com/";
    private static final String PAGE_CONTENTS_LINK  = "Text text [[TestPageOne|P1]] [[Test_Page_Two|P2]] [[Invalid|Invalid]] text";

    private static final String URL_WIKI_BASE = "/slingshot/wiki/page";
    private static final String URL_WIKI_LIST = URL_WIKI_BASE + "s/" + SITE_SHORT_NAME_WIKI;
    private static final String URL_WIKI_FETCH = URL_WIKI_BASE + "/" + SITE_SHORT_NAME_WIKI + "/"; // plus title
    private static final String URL_WIKI_UPDATE = URL_WIKI_BASE + "/" + SITE_SHORT_NAME_WIKI + "/"; // plus title
    private static final String URL_WIKI_DELETE = URL_WIKI_BASE + "/" + SITE_SHORT_NAME_WIKI + "/"; // plus title
    private static final String URL_WIKI_RENAME = URL_WIKI_BASE + "/" + SITE_SHORT_NAME_WIKI + "/"; // plus title
    private static final String URL_WIKI_VERSION = "/slingshot/wiki/version/" + SITE_SHORT_NAME_WIKI + "/";
    
    
    // General methods

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.authenticationService = (MutableAuthenticationService)getServer().getApplicationContext().getBean("AuthenticationService");
        this.authenticationComponent = (AuthenticationComponent)getServer().getApplicationContext().getBean("authenticationComponent");
        this.policyBehaviourFilter = (BehaviourFilter)getServer().getApplicationContext().getBean("policyBehaviourFilter");
        this.transactionService = (TransactionService)getServer().getApplicationContext().getBean("transactionService");
        this.personService = (PersonService)getServer().getApplicationContext().getBean("PersonService");
        this.nodeService = (NodeService)getServer().getApplicationContext().getBean("NodeService");
        this.siteService = (SiteService)getServer().getApplicationContext().getBean("SiteService");
        this.wikiService = (WikiService)getServer().getApplicationContext().getBean("WikiService");
        this.internalNodeService = (NodeService)getServer().getApplicationContext().getBean("nodeService");
        this.nodeArchiveService = (NodeArchiveService)getServer().getApplicationContext().getBean("nodeArchiveService");
        
        // Authenticate as user
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        // Create test site
        // - only create the site if it doesn't already exist
        SiteInfo siteInfo = this.siteService.getSite(SITE_SHORT_NAME_WIKI);
        if (siteInfo == null)
        {
            this.siteService.createSite("WikiSitePreset", SITE_SHORT_NAME_WIKI, "WikiSiteTitle", "TestDescription", SiteVisibility.PUBLIC);
        }
        
        // Ensure the links container is there
        if(!siteService.hasContainer(SITE_SHORT_NAME_WIKI, "wiki"))
        {
            siteService.createContainer(SITE_SHORT_NAME_WIKI, "wiki", null, null);
        }
        
        // Create users
        createUser(USER_ONE, SiteModel.SITE_COLLABORATOR);
        createUser(USER_TWO, SiteModel.SITE_COLLABORATOR);

        // Do tests as inviter user
        this.authenticationComponent.setCurrentUser(USER_ONE);
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        // admin user required to delete user
        this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
        
        SiteInfo siteInfo = this.siteService.getSite(SITE_SHORT_NAME_WIKI);
        if (siteInfo != null)
        {
            // delete the site
            siteService.deleteSite(SITE_SHORT_NAME_WIKI);
            nodeArchiveService.purgeArchivedNode(nodeArchiveService.getArchivedNode(siteInfo.getNodeRef()));
        }
        
        // delete the users
        if(personService.personExists(USER_ONE))
        {
           personService.deletePerson(USER_ONE);
        }
        if(this.authenticationService.authenticationExists(USER_ONE))
        {
           this.authenticationService.deleteAuthentication(USER_ONE);
        }
        
        if(personService.personExists(USER_TWO))
        {
           personService.deletePerson(USER_TWO);
        }
        if(this.authenticationService.authenticationExists(USER_TWO))
        {
           this.authenticationService.deleteAuthentication(USER_TWO);
        }
    }
    
    private void createUser(String userName, String role)
    {
        // if user with given user name doesn't already exist then create user
        if (this.authenticationService.authenticationExists(userName) == false)
        {
            // create user
            this.authenticationService.createAuthentication(userName, "password".toCharArray());
            
            // create person properties
            PropertyMap personProps = new PropertyMap();
            personProps.put(ContentModel.PROP_USERNAME, userName);
            personProps.put(ContentModel.PROP_FIRSTNAME, USERDETAILS_FIRSTNAME);
            personProps.put(ContentModel.PROP_LASTNAME, USERDETAILS_LASTNAME);
            personProps.put(ContentModel.PROP_EMAIL, "FirstName123.LastName123@email.com");
            personProps.put(ContentModel.PROP_JOBTITLE, "JobTitle123");
            personProps.put(ContentModel.PROP_JOBTITLE, "Organisation123");
            
            // create person node for user
            this.personService.createPerson(personProps);
        }
        
        // add the user as a member with the given role
        this.siteService.setMembership(SITE_SHORT_NAME_WIKI, userName, role);
    }
    
    
    // Test helper methods
    
    private JSONObject getPages(String filter, String username) throws Exception
    {
       String origUser = this.authenticationComponent.getCurrentUserName();
       if (username != null)
       {
          this.authenticationComponent.setCurrentUser(username);
          filter = "myPages";
       }
       
       String url = URL_WIKI_LIST;
       if (filter == null)
       {
          filter = "all";
       }
       url += "?filter=" + filter;
       url += "&startIndex=0&page=1&pageSize=4";
       
       Response response = sendRequest(new GetRequest(url), 200);
       JSONObject result = new JSONObject(response.getContentAsString());
       
       if (username != null)
       {
          this.authenticationComponent.setCurrentUser(origUser);
       }
       
       return result;
    }
    
    private JSONObject getPage(String name, int expectedStatus) throws Exception
    {
       Response response = sendRequest(new GetRequest(URL_WIKI_FETCH + name), expectedStatus);
       if (expectedStatus == Status.STATUS_OK)
       {
          JSONObject result = new JSONObject(response.getContentAsString());
          if (result.has("page"))
          {
             return result.getJSONObject("page");
          }
          return result;
       }
       else if (expectedStatus == Status.STATUS_NOT_FOUND)
       {
          JSONObject result = new JSONObject(response.getContentAsString());
          return result;
       }
       else
       {
          return null;
       }
    }
    
    /**
     * Fetches the content of a page at a given version
     * Note - not JSON based.
     */
    private String getPageAtVersion(String name, String version, int expectedStatus) throws Exception
    {
       Response response = sendRequest(new GetRequest(URL_WIKI_VERSION + name + "/" + version), expectedStatus);
       if (expectedStatus == Status.STATUS_OK)
       {
          return response.getContentAsString();
       }
       else if (expectedStatus == Status.STATUS_NOT_FOUND)
       {
          return response.getContentAsString();
       }
       else
       {
          return null;
       }
    }
    
    /**
     * Creates a single wiki page based on the supplied details
     */
    private JSONObject createOrUpdatePage(String title, String contents, String version, int expectedStatus)
    throws Exception
    {
       return createOrUpdatePage(null, title, contents, version, expectedStatus);
    }
    
    /**
     * Creates a single wiki page based on the supplied details
     */
    private JSONObject createOrUpdatePage(String pageName, String title, String contents, String version, int expectedStatus) throws Exception
    {
        String name = null;
        if (pageName == null)
        {
            name = title.replace(' ', '_');
        }
        else
        {
            name = pageName;
        }

        JSONObject json = new JSONObject();
        json.put("site", SITE_SHORT_NAME_WIKI);
        json.put("title", title);
        json.put("pagecontent", contents);
        json.put("tags", "");
        json.put("page", "wiki-page"); // TODO Is this really needed?

        if (version == null || "force".equals(version))
        {
            // Allow the save as-is, no versioning check
            json.put("forceSave", "true"); // Allow the save as-is
        }
        else
        {
            if ("none".equals(version))
            {
                // No versioning
            }
            else
            {
                json.put("currentVersion", version);
            }
        }

        Response response = sendRequest(new PutRequest(URL_WIKI_UPDATE + name, json.toString(), "application/json"), expectedStatus);
        if (expectedStatus == Status.STATUS_OK)
        {
            JSONObject result = new JSONObject(response.getContentAsString());
            if (result.has("page"))
            {
                return result.getJSONObject("page");
            }
            return result;
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Renames the page
     */
    private JSONObject renamePage(String oldTitle, String newTitle, int expectedStatus) throws Exception
    {
       String name = oldTitle.replace(' ', '_');
       
       JSONObject json = new JSONObject();
       json.put("site", SITE_SHORT_NAME_WIKI);
       json.put("name", newTitle);
       json.put("page", "wiki-page"); // TODO Is this really needed?
       
       Response response = sendRequest(new PostRequest(URL_WIKI_RENAME + name, json.toString(), "application/json"), expectedStatus);
       if (expectedStatus == Status.STATUS_OK)
       {
          JSONObject result = new JSONObject(response.getContentAsString());
          return result;
       }
       else
       {
          return null;
       }
    }
    
    /**
     * Deletes the page
     */
    private JSONObject deletePage(String title, int expectedStatus) throws Exception
    {
       String name = title.replace(' ', '_');
       
       Response response = sendRequest(new DeleteRequest(URL_WIKI_DELETE + name), expectedStatus);
       if (expectedStatus == Status.STATUS_OK)
       {
          JSONObject result = new JSONObject(response.getContentAsString());
          return result;
       }
       else
       {
          return null;
       }
    }
    
    /**
     * Monkeys with the created date on a wiki page
     */
    private void pushPageCreatedDateBack(String name, int daysAgo) throws Exception
    {
       NodeRef container = siteService.getContainer(SITE_SHORT_NAME_WIKI, "wiki");
       NodeRef node = nodeService.getChildByName(container, ContentModel.ASSOC_CONTAINS, name);
       
       Date created = (Date)nodeService.getProperty(node, ContentModel.PROP_CREATED);
       Date newCreated = new Date(created.getTime() - daysAgo*24*60*60*1000);
       
       UserTransaction txn = transactionService.getUserTransaction();
       txn.begin();

       this.policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
       internalNodeService.setProperty(node, ContentModel.PROP_CREATED, newCreated);
       this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
       
       txn.commit();
       
       // Now chance something else on the node to have it re-indexed
       nodeService.setProperty(node, ContentModel.PROP_CREATED, newCreated);
       nodeService.setProperty(node, ContentModel.PROP_DESCRIPTION, "Forced change");
    }
    
    
    // Tests
    
    /**
     * Creating, editing, fetching and deleting a link
     */
    public void testCreateEditDeleteEntry() throws Exception
    {
       JSONObject page;
       JSONObject permissions;
       String name;
       
       
       // None to start with
       page = getPages(null, null);
       assertEquals("Incorrect JSON: " + page.toString(), true, page.has("totalPages"));
       assertEquals(0, page.getInt("totalPages"));
       
       
       // Won't be there to start with
       page = getPage(PAGE_TITLE_ONE, Status.STATUS_NOT_FOUND);
       
       // Create
       page = createOrUpdatePage(PAGE_TITLE_ONE, PAGE_CONTENTS_ONE, null, Status.STATUS_OK);
       name = PAGE_TITLE_ONE.replace(' ', '_');
       assertEquals("Incorrect JSON: " + page.toString(), true, page.has("title"));
       
       assertEquals(name, page.getString("name"));
       assertEquals(PAGE_TITLE_ONE, page.getString("title"));
       assertEquals(PAGE_CONTENTS_ONE, page.getString("pagetext"));
       assertEquals(0, page.getJSONArray("tags").length());

       
       // Fetch
       page = getPage(name, Status.STATUS_OK);
       
       assertEquals(name, page.getString("name"));
       assertEquals(PAGE_TITLE_ONE, page.getString("title"));
       assertEquals(PAGE_CONTENTS_ONE, page.getString("pagetext"));
       assertEquals(0, page.getJSONArray("tags").length());
       assertEquals(0, page.getJSONArray("links").length());

       // Check the permissions
       assertEquals(true, page.has("permissions"));
       permissions = page.getJSONObject("permissions");
       assertEquals(true, permissions.getBoolean("create"));
       assertEquals(true, permissions.getBoolean("edit"));
       assertEquals(true, permissions.getBoolean("delete"));
       
       
       // Edit
       // We should get a simple message
       page = createOrUpdatePage(PAGE_TITLE_ONE, "M"+PAGE_CONTENTS_ONE, null, Status.STATUS_OK);
       assertEquals(name, page.getString("name"));
       assertEquals(PAGE_TITLE_ONE, page.getString("title"));
       assertEquals("M"+PAGE_CONTENTS_ONE, page.getString("pagetext"));
       assertEquals(0, page.getJSONArray("tags").length());
       
       
       
       // Fetch
       page = getPage(name, Status.STATUS_OK);
       
       assertEquals(name, page.getString("name"));
       assertEquals(PAGE_TITLE_ONE, page.getString("title"));
       assertEquals("M"+PAGE_CONTENTS_ONE, page.getString("pagetext"));
       assertEquals(0, page.getJSONArray("tags").length());
       assertEquals(0, page.getJSONArray("links").length());
       
       
       // Fetch as a different user, permissions different
       this.authenticationComponent.setCurrentUser(USER_TWO);
       page = getPage(name, Status.STATUS_OK);
       
       // Check the basics
       assertEquals(name, page.getString("name"));
       assertEquals(PAGE_TITLE_ONE, page.getString("title"));
       assertEquals("M"+PAGE_CONTENTS_ONE, page.getString("pagetext"));
       assertEquals(0, page.getJSONArray("tags").length());
       assertEquals(0, page.getJSONArray("links").length());
       
       // Different user in the site, can edit but not delete
       assertEquals(true, page.has("permissions"));
       permissions = page.getJSONObject("permissions");
       assertEquals(true, permissions.getBoolean("create"));
       assertEquals(true, permissions.getBoolean("edit"));
       assertEquals(false, permissions.getBoolean("delete"));
       
       this.authenticationComponent.setCurrentUser(USER_ONE);

       
       // Delete
       page = deletePage(name, Status.STATUS_NO_CONTENT);
       assertEquals(null, page); // No response
       
       
       // Fetch, will have gone
       page = getPage(name, Status.STATUS_NOT_FOUND);
       
       // On a page that isn't there, you do get permissions
       assertEquals(true, page.has("permissions"));
       permissions = page.getJSONObject("permissions");
       assertEquals(true, permissions.getBoolean("create"));
       assertEquals(true, permissions.getBoolean("edit"));
       assertEquals(false, permissions.has("delete")); // No delete for non existing page
       
       
       // Can't delete again
       deletePage(name, Status.STATUS_NOT_FOUND);
    }
    
    public void testRenaming() throws Exception
    {
       JSONObject page;
       String name;
       String name2;

       
       // Create a page
       page = createOrUpdatePage(PAGE_TITLE_TWO, PAGE_CONTENTS_ONE, null, Status.STATUS_OK);
       name = PAGE_TITLE_TWO.replace(' ', '_');
       assertEquals("Incorrect JSON: " + page.toString(), true, page.has("title"));
       
       
       // Fetch it and check
       page = getPage(name, Status.STATUS_OK);
       assertEquals(name, page.getString("name"));
       assertEquals(PAGE_TITLE_TWO, page.getString("title"));
       
       
       // Have it renamed
       page = renamePage(PAGE_TITLE_TWO, PAGE_TITLE_THREE, Status.STATUS_OK);
       name2 = PAGE_TITLE_THREE.replace(' ', '_');
       assertEquals(PAGE_TITLE_THREE, page.getString("title"));
       
       
       // Fetch it at the new address
       page = getPage(name2, Status.STATUS_OK);
       assertEquals(name2, page.getString("name"));
       assertEquals(PAGE_TITLE_THREE, page.getString("title"));
       
       
       // Get the old one, and ensure we see the "has been moved"
       page = getPage(name, Status.STATUS_OK);
       assertEquals(name, page.getString("name"));
       assertEquals(PAGE_TITLE_TWO, page.getString("title"));
       assertEquals("This page has been moved [["+PAGE_TITLE_THREE+"|here]].", page.getString("pagetext"));
       
       
       // Ensure you can't rename onto an existing page
       page = createOrUpdatePage(PAGE_TITLE_ONE, PAGE_CONTENTS_THREE, null, Status.STATUS_OK);
       String name1 = PAGE_TITLE_ONE.replace(' ', '_');
       
       // Check the rename won't work
       renamePage(PAGE_TITLE_THREE, PAGE_TITLE_ONE, Status.STATUS_CONFLICT);
       
       // Check that the pages weren't changed
       page = getPage(name2, Status.STATUS_OK);
       assertEquals(name2, page.getString("name"));
       assertEquals(PAGE_TITLE_THREE, page.getString("title"));
       
       page = getPage(name1, Status.STATUS_OK);
       assertEquals(name1, page.getString("name"));
       assertEquals(PAGE_TITLE_ONE, page.getString("title"));
    }
    
    public void testRenamePageWithEmptyTitle() throws Exception
    {
        JSONObject page;
        String name = System.currentTimeMillis()+"";
        String name2 = System.currentTimeMillis()+1+"";
        
        // Create a page
        page = createOrUpdatePage(name, name, null, Status.STATUS_OK);
        assertEquals("Incorrect JSON: " + page.toString(), true, page.has("title"));
        
     // Fetch it and check
        page = getPage(name, Status.STATUS_OK);
        assertEquals(name, page.getString("name"));
        assertEquals(name, page.getString("title"));
        
        //update title
        SiteInfo site = siteService.getSite(SITE_SHORT_NAME_WIKI);
        WikiPageInfo pageInfo = wikiService.getWikiPage(site.getShortName(), name);
        NodeRef pageRef = pageInfo.getNodeRef();
        nodeService.setProperty(pageRef, ContentModel.PROP_TITLE, "");
        
     // Fetch it and check
        page = getPage(name, Status.STATUS_OK);
        JSONArray versions = page.getJSONArray("versionhistory");
        
        int maxVersionIndex = versions.length()-1;
        double vNum = Double.parseDouble(versions.getJSONObject(maxVersionIndex).getString("version"));
        String newTitle =versions.getJSONObject(maxVersionIndex).getString("title");
        for (int i = versions.length()-2; i>=0; i--)
        {
            JSONObject version = versions.getJSONObject(i);
            String ver = version.getString("version");
            if (Double.parseDouble(ver)>vNum)
            {
                maxVersionIndex = i;
                vNum = Double.parseDouble(ver);
                newTitle =versions.getJSONObject(maxVersionIndex).getString("title");
            }
        }
        assertEquals(name, page.getString("name"));
        assertEquals("", newTitle);
        
        renamePage(name, name2, Status.STATUS_OK);
        
        // Fetch it at the new address
        page = getPage(name2, Status.STATUS_OK);
        assertEquals(name2, page.getString("name"));
        assertEquals(name2, page.getString("title"));
    }
    
    public void testVersioning() throws Exception
    {
       WikiPageInfo wikiInfo;
       JSONObject page;
       JSONArray versions;
       String name;
       
       // Create a page
       page = createOrUpdatePage(PAGE_TITLE_TWO, PAGE_CONTENTS_ONE, null, Status.STATUS_OK);
       name = PAGE_TITLE_TWO.replace(' ', '_');
       assertEquals("Incorrect JSON: " + page.toString(), true, page.has("title"));

       
       // Check it was versioned by default
       wikiInfo = wikiService.getWikiPage(SITE_SHORT_NAME_WIKI, name);
       assertNotNull(wikiInfo);
       assertEquals(true, nodeService.hasAspect(wikiInfo.getNodeRef(), ContentModel.ASPECT_VERSIONABLE));
     
       // Check the JSON for versioning
       page = getPage(name, Status.STATUS_OK);
       assertEquals("Incorrect JSON: " + page.toString(), true, page.has("versionhistory"));
       
       versions = page.getJSONArray("versionhistory");
       assertEquals(1, versions.length());
       assertEquals("1.0",    versions.getJSONObject(0).get("version"));
       assertEquals(USER_ONE, versions.getJSONObject(0).get("author"));
       
       
       // Fetch it at this version
       String content = getPageAtVersion(name, "1.0", Status.STATUS_OK);
       assertEquals(PAGE_CONTENTS_ONE, content);
       
       
       // Upload a new copy without a version flag, denied
       createOrUpdatePage(PAGE_TITLE_TWO, "Changed Contents", "none", Status.STATUS_CONFLICT);
       
       
       // Upload a new copy with the appropriate version, allowed
       String PAGE_CONTENTS_CHANGED = "Changed Contents 2";
       page = createOrUpdatePage(PAGE_TITLE_TWO, PAGE_CONTENTS_CHANGED, "1.0", Status.STATUS_OK);
       
       page = getPage(name, Status.STATUS_OK);
       assertEquals("Incorrect JSON: " + page.toString(), true, page.has("versionhistory"));
       
       versions = page.getJSONArray("versionhistory");
       assertEquals(2, versions.length());
       assertEquals("1.1",    versions.getJSONObject(0).get("version"));
       assertEquals(USER_ONE, versions.getJSONObject(0).get("author"));
       assertEquals("1.0",    versions.getJSONObject(1).get("version"));
       assertEquals(USER_ONE, versions.getJSONObject(1).get("author"));
       
       
       // Check the version contents
       content = getPageAtVersion(name, "1.1", Status.STATUS_OK);
       assertEquals(PAGE_CONTENTS_CHANGED, content);
       content = getPageAtVersion(name, "1.0", Status.STATUS_OK);
       assertEquals(PAGE_CONTENTS_ONE, content);
       
       
       // Upload a new copy with the force flag, allowed
       String PAGE_CONTENTS_CHANGED3 = "Changed Contents 3";
       page = createOrUpdatePage(PAGE_TITLE_TWO, PAGE_CONTENTS_CHANGED3, "force", Status.STATUS_OK);
       
       page = getPage(name, Status.STATUS_OK);
       assertEquals("Incorrect JSON: " + page.toString(), true, page.has("versionhistory"));
       
       versions = page.getJSONArray("versionhistory");
       assertEquals(3, versions.length());
       assertEquals("1.2",    versions.getJSONObject(0).get("version"));
       assertEquals(USER_ONE, versions.getJSONObject(0).get("author"));
       assertEquals("1.1",    versions.getJSONObject(1).get("version"));
       assertEquals(USER_ONE, versions.getJSONObject(1).get("author"));
       assertEquals("1.0",    versions.getJSONObject(2).get("version"));
       assertEquals(USER_ONE, versions.getJSONObject(2).get("author"));
       
       // Check the version contents
       content = getPageAtVersion(name, "1.2", Status.STATUS_OK);
       assertEquals(PAGE_CONTENTS_CHANGED3, content);
       content = getPageAtVersion(name, "1.1", Status.STATUS_OK);
       assertEquals(PAGE_CONTENTS_CHANGED, content);
       content = getPageAtVersion(name, "1.0", Status.STATUS_OK);
       assertEquals(PAGE_CONTENTS_ONE, content);
       
       // You get an empty string back for invalid versions
       content = getPageAtVersion(name, "1.4", Status.STATUS_OK);
       assertEquals("", content);
    }
    
    public void testLinks() throws Exception
    {
       JSONObject page;
       JSONArray links;
       String name;
       String name2;
       
       // Create a page with no links
       page = createOrUpdatePage(PAGE_TITLE_TWO, PAGE_CONTENTS_TWO, null, Status.STATUS_OK);
       name = PAGE_TITLE_TWO.replace(' ', '_');
       assertEquals("Incorrect JSON: " + page.toString(), true, page.has("title"));
       
       
       // Check, won't have any links shown
       page = getPage(name, Status.STATUS_OK);
       assertEquals("Incorrect JSON: " + page.toString(), true, page.has("links"));
       links = page.getJSONArray("links");
       assertEquals(0, links.length());
       
       
       // Create a page with links
       // Should have links to pages 1 and 2
       page = createOrUpdatePage(PAGE_TITLE_THREE, PAGE_CONTENTS_LINK, null, Status.STATUS_OK);
       name2 = PAGE_TITLE_THREE.replace(' ', '_');
       assertEquals("Incorrect JSON: " + page.toString(), true, page.has("title"));
       
       // Check 
       page = getPage(name2, Status.STATUS_OK);
       assertEquals("Incorrect JSON: " + page.toString(), true, page.has("links"));
       
       links = page.getJSONArray("links");
       assertEquals(3, links.length());
       assertEquals(PAGE_TITLE_ONE, links.getString(0));
       assertEquals(name,      links.getString(1));
       assertEquals("Invalid", links.getString(2));
       
       
       // Create the 1st page, now change
       page = createOrUpdatePage(PAGE_TITLE_ONE, PAGE_CONTENTS_ONE, null, Status.STATUS_OK);
       
       page = getPage(name2, Status.STATUS_OK);
       assertEquals("Incorrect JSON: " + page.toString(), true, page.has("links"));
       
       links = page.getJSONArray("links");
       assertEquals(3, links.length());
       assertEquals(PAGE_TITLE_ONE, links.getString(0));
       assertEquals(name,      links.getString(1));
       assertEquals("Invalid", links.getString(2));
    }
    
    /**
     * Listing
     */
    public void testOverallListing() throws Exception
    {
       JSONObject pages;
       JSONArray entries;
       
       // Initially, there are no events
       pages = getPages(null, null);
       assertEquals("Incorrect JSON: " + pages.toString(), true, pages.has("totalPages"));
       assertEquals(0, pages.getInt("totalPages"));
       
       
       // Add two links to get started with
       createOrUpdatePage(PAGE_TITLE_ONE, PAGE_CONTENTS_ONE, null, Status.STATUS_OK);
       createOrUpdatePage(PAGE_TITLE_TWO, PAGE_CONTENTS_TWO, null, Status.STATUS_OK);
       
       // Check again
       pages = getPages(null, null);
       
       // Should have two links
       assertEquals("Incorrect JSON: " + pages.toString(), true, pages.has("totalPages"));
       assertEquals(2, pages.getInt("totalPages"));
     
       entries = pages.getJSONArray("pages");
       assertEquals(2, entries.length());
       // Sorted by newest created first
       assertEquals(PAGE_TITLE_TWO, entries.getJSONObject(0).getString("title"));
       assertEquals(PAGE_TITLE_ONE, entries.getJSONObject(1).getString("title"));
       
       
       // Add a third, which is internal, and created by the other user
       this.authenticationComponent.setCurrentUser(USER_TWO);
       JSONObject page3 = createOrUpdatePage(PAGE_TITLE_THREE, PAGE_CONTENTS_THREE, null, Status.STATUS_OK);
       String name3 = PAGE_TITLE_THREE.replace(' ', '_');
       createOrUpdatePage(PAGE_TITLE_THREE, "UD"+PAGE_CONTENTS_THREE, null, Status.STATUS_OK);
       this.authenticationComponent.setCurrentUser(USER_ONE);
       
       
       // Check now, should have three links
       pages = getPages(null, null);
       assertEquals(3, pages.getInt("totalPages"));
       
       entries = pages.getJSONArray("pages");
       assertEquals(3, entries.length());
       assertEquals(PAGE_TITLE_THREE, entries.getJSONObject(0).getString("title"));
       assertEquals(PAGE_TITLE_TWO, entries.getJSONObject(1).getString("title"));
       assertEquals(PAGE_TITLE_ONE, entries.getJSONObject(2).getString("title"));
       
       
       // Ask for filtering by user
       pages = getPages(null, USER_ONE);
       assertEquals(2, pages.getInt("totalPages"));
       
       entries = pages.getJSONArray("pages");
       assertEquals(2, entries.length());
       assertEquals(PAGE_TITLE_TWO, entries.getJSONObject(0).getString("title"));
       assertEquals(PAGE_TITLE_ONE, entries.getJSONObject(1).getString("title"));
       
       pages = getPages(null, USER_TWO);
       assertEquals(1, pages.getInt("totalPages"));
       
       entries = pages.getJSONArray("pages");
       assertEquals(1, entries.length());
       assertEquals(PAGE_TITLE_THREE, entries.getJSONObject(0).getString("title"));

       
       
       // Ask for filtering by recently added docs
       pages = getPages("recentlyAdded", null);
       assertEquals(3, pages.getInt("totalPages"));
       
       entries = pages.getJSONArray("pages");
       assertEquals(3, entries.length());
       assertEquals(PAGE_TITLE_THREE, entries.getJSONObject(0).getString("title"));
       assertEquals(PAGE_TITLE_TWO, entries.getJSONObject(1).getString("title"));
       assertEquals(PAGE_TITLE_ONE, entries.getJSONObject(2).getString("title"));
       
       // Push one back into the past
       pushPageCreatedDateBack(name3, 10);
       
       pages = getPages("recentlyAdded", null);
       assertEquals(2, pages.getInt("totalPages"));
       
       entries = pages.getJSONArray("pages");
       assertEquals(2, entries.length());
       assertEquals(PAGE_TITLE_TWO, entries.getJSONObject(0).getString("title"));
       assertEquals(PAGE_TITLE_ONE, entries.getJSONObject(1).getString("title"));
       
       
       // Now for recently modified ones
       pages = getPages("recentlyModified", null);
       assertEquals(3, pages.getInt("totalPages"));
       
       entries = pages.getJSONArray("pages");
       assertEquals(3, entries.length());
       assertEquals(PAGE_TITLE_THREE, entries.getJSONObject(0).getString("title"));
       assertEquals(PAGE_TITLE_TWO, entries.getJSONObject(1).getString("title"));
       assertEquals(PAGE_TITLE_ONE, entries.getJSONObject(2).getString("title"));
//       assertEquals(PAGE_TITLE_THREE, entries.getJSONObject(2).getString("title"));
       
       
       // Change the owner+creator of one of the pages to System, ensure that 
       //  this doesn't break anything in the process
       String pageTwoName = entries.getJSONObject(1).getString("name");
       WikiPageInfo pageTwo = wikiService.getWikiPage(SITE_SHORT_NAME_WIKI, pageTwoName);
       nodeService.setProperty(pageTwo.getNodeRef(), ContentModel.PROP_OWNER, AuthenticationUtil.SYSTEM_USER_NAME);
       nodeService.setProperty(pageTwo.getNodeRef(), ContentModel.PROP_CREATOR, AuthenticationUtil.SYSTEM_USER_NAME);
       nodeService.setProperty(pageTwo.getNodeRef(), ContentModel.PROP_MODIFIER, AuthenticationUtil.SYSTEM_USER_NAME);
       
       // Check the listing still works (note - order will have changed)
       pages = getPages("recentlyModified", null);
       assertEquals(3, pages.getInt("totalPages"));
       
       entries = pages.getJSONArray("pages");
       assertEquals(3, entries.length());
       assertEquals(PAGE_TITLE_TWO, entries.getJSONObject(0).getString("title"));
       assertEquals(PAGE_TITLE_THREE, entries.getJSONObject(1).getString("title"));
       assertEquals(PAGE_TITLE_ONE, entries.getJSONObject(2).getString("title"));

       
       // Delete User Two, who owns the 3rd page, and ensure that this
       //  doesn't break anything
       this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
       personService.deletePerson(USER_TWO);
       this.authenticationComponent.setCurrentUser(USER_ONE);
       
       // Check the listing still works
       pages = getPages("recentlyModified", null);
       assertEquals(3, pages.getInt("totalPages"));
       
       entries = pages.getJSONArray("pages");
       assertEquals(3, entries.length());
       assertEquals(PAGE_TITLE_TWO, entries.getJSONObject(0).getString("title"));
       assertEquals(PAGE_TITLE_THREE, entries.getJSONObject(1).getString("title"));
       assertEquals(PAGE_TITLE_ONE, entries.getJSONObject(2).getString("title"));

       
       // Now hide the site, and remove the user from it, won't be allowed to see it
       this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());
       SiteInfo site = siteService.getSite(SITE_SHORT_NAME_WIKI);
       site.setVisibility(SiteVisibility.PRIVATE);
       siteService.updateSite(site);
       siteService.removeMembership(SITE_SHORT_NAME_WIKI, USER_ONE);
       this.authenticationComponent.setCurrentUser(USER_ONE);
       
       sendRequest(new GetRequest(URL_WIKI_LIST), Status.STATUS_NOT_FOUND);
    }
    
    public void test_MNT11595() throws Exception
    {
        final String user = "wikiUser";

        try
        {
            // admin authentication
            this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

            MutableAuthenticationService mas = (MutableAuthenticationService) getServer().getApplicationContext().getBean("authenticationService");

            // create user
            createUser(user, SiteModel.SITE_MANAGER);

            assertTrue(personService.personExists(user));

            // invite user to a site with 'Manager' role
            siteService.setMembership(SITE_SHORT_NAME_WIKI, user, SiteRole.SiteManager.toString());

            // user authentication
            this.authenticationComponent.setCurrentUser(user);

            // create wiki page by user ('Manager' role)
            WikiPageInfo wikiPage = this.wikiService.createWikiPage(SITE_SHORT_NAME_WIKI, "test wiki page",
                    "I like pigs. Dogs look up to us. Cats look down on us. Pigs treat us as equals. Sir Winston Churchill");

            String uri = "/slingshot/wiki/page/" + SITE_SHORT_NAME_WIKI + "/Main_Page?alf_ticket=" + mas.getCurrentTicket() + "application/json";

            Response responseManagerRole = sendRequest(new GetRequest(uri), 404);
            JSONObject resultManagerRole = new JSONObject(responseManagerRole.getContentAsString());
            JSONObject permissionsManagerRole = resultManagerRole.getJSONObject("permissions");
            assertTrue(permissionsManagerRole.getBoolean("create"));
            assertTrue(permissionsManagerRole.getBoolean("edit"));

            // admin authentication
            this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

            // change user role - 'Consumer' role
            siteService.setMembership(SITE_SHORT_NAME_WIKI, user, SiteRole.SiteConsumer.toString());

            // user authentication
            this.authenticationComponent.setCurrentUser(user);

            Response responseConsumerRole = sendRequest(new GetRequest(uri), 404);
            JSONObject resultConsumerRole = new JSONObject(responseConsumerRole.getContentAsString());
            JSONObject permissionsConsumerRole = resultConsumerRole.getJSONObject("permissions");
            assertFalse(permissionsConsumerRole.getBoolean("create"));
            assertFalse(permissionsConsumerRole.getBoolean("edit"));
        }
        finally
        {
            this.authenticationComponent.setCurrentUser(AuthenticationUtil.getAdminUserName());

            if (personService.personExists(user))
            {
                personService.deletePerson(user);
            }

            if (this.authenticationService.authenticationExists(user))
            {
                this.authenticationService.deleteAuthentication(user);
            }
        }
    }
    
    public void testXSSInjection() throws Exception
    { 
    	WikiPageInfo wikiPage = null;
    	WikiPageInfo wikiPageNew = null;
    	try{
    		wikiPage = this.wikiService.createWikiPage(SITE_SHORT_NAME_WIKI, "test_wiki", "[[Test]]");
    		wikiPageNew = this.wikiService.createWikiPage(SITE_SHORT_NAME_WIKI, "Test", "test_content");
    		Pattern LINK_PATTERN_MATCH = Pattern.compile("\\[\\[([^\\|\\]]+)");
    		Matcher m = LINK_PATTERN_MATCH.matcher(wikiPage.getContents());
    		while (m.find())
    		{
    			String link = m.group(1); 
    			link += "?title=<script>alert('xss');</script>";
    			WikiPageInfo wikiPage2 = this.wikiService.getWikiPage(SITE_SHORT_NAME_WIKI, link);
    			WikiPageInfo wikiPage1 = this.wikiService.getWikiPage(SITE_SHORT_NAME_WIKI, StringEscapeUtils.unescapeHtml(link));
    			assertEquals(wikiPage2, wikiPage1);
    		}
      
    	}
    	finally{
    		 this.wikiService.deleteWikiPage(wikiPage); 
    		 this.wikiService.deleteWikiPage(wikiPageNew); 
    	}
    }
}