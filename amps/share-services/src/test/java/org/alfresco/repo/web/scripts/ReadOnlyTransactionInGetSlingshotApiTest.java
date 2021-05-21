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
package org.alfresco.repo.web.scripts;

import java.text.MessageFormat;
import java.util.List;

import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.TestWebScriptServer.GetRequest;
import org.springframework.extensions.webscripts.TestWebScriptServer.Response;

/**
 * Set of tests that ensure Slingshot GET APIs are run successfully in a read-only
 * transaction (ALF-10179).
 * 
 * Some webscripts have a side effect of creating a "container" these tests
 * are to ensure this is handled gracefully in a way that allows the main 
 * transaction to be declared as readonly for performance reasons.
 * 
 * @see ReadOnlyTransactionInGetRestApiTest
 * @author Gavin Cornwell
 * @author Matt Ward
 * @since 5.1
 */
public class ReadOnlyTransactionInGetSlingshotApiTest extends BaseWebScriptTest
{
    private static final String TEST_SITE_NAME = "readOnlyTestSite";
    
    private static final String URL_GET_SITE_DATALISTS = "/slingshot/datalists/lists/site/" + TEST_SITE_NAME + "/dataLists";
    private static final String URL_GET_SITE_WIKI = "/slingshot/wiki/pages/" + TEST_SITE_NAME;
    private static final String URL_GET_SITE_WIKI_PAGE = "/slingshot/wiki/page/" + TEST_SITE_NAME + "/AWikiPage";
    private static final String URL_GET_SITE_WIKI_PAGE_VERSION = "/slingshot/wiki/version/" + TEST_SITE_NAME + "/AWikiPage/123456789";
    private static final String URL_GET_DOCLIB_CATEGORYNODE = "/slingshot/doclib/categorynode/node/{0}";
    private static final String URL_GET_DOCLIB_TREENODE = "/slingshot/doclib/treenode/site/" + TEST_SITE_NAME + "/documentLibrary";
    private static final String URL_GET_DOCLIB_NODE = "/slingshot/doclib/node/{0}";
    private static final String URL_GET_DOCLIB2_NODE = "/slingshot/doclib2/node/{0}";
    private static final String URL_GET_DOCLIB_LOCATION = "/slingshot/doclib/node/{0}/location";
    private static final String URL_GET_DOCLIB_DOCLIST = "/slingshot/doclib/doclist/documents/site/" + TEST_SITE_NAME + "/documentLibrary";
    private static final String URL_GET_DOCLIB2_DOCLIST = "/slingshot/doclib2/doclist/documents/site/" + TEST_SITE_NAME + "/documentLibrary";
    private static final String URL_GET_DOCLIB_IMAGES = "/slingshot/doclib/images/site/" + TEST_SITE_NAME + "/documentLibrary";
    
    private SiteService siteService;
    private NodeService nodeService;
    private TransactionService transactionService;
    private NodeArchiveService nodeArchiveService;
    
    private NodeRef testSiteNodeRef;
    private String testSiteNodeRefString;
    
    private boolean logEnabled = false;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ApplicationContext appContext = getServer().getApplicationContext();

        this.siteService = (SiteService)appContext.getBean("SiteService");
        this.nodeService = (NodeService)appContext.getBean("NodeService");
        this.transactionService = (TransactionService)appContext.getBean("TransactionService");
        this.nodeArchiveService = (NodeArchiveService)getServer().getApplicationContext().getBean("nodeArchiveService");
        
        // set admin as current user
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        // delete the test site if it's still hanging around from previous runs
        SiteInfo site = siteService.getSite(TEST_SITE_NAME);
        if (site != null)
        {
            siteService.deleteSite(TEST_SITE_NAME);
            nodeArchiveService.purgeArchivedNode(nodeArchiveService.getArchivedNode(site.getNodeRef()));
        }
        
        // create the test site, this should create a site but it won't have any containers created
        SiteInfo siteInfo = this.siteService.createSite("collaboration", TEST_SITE_NAME, "Read Only Test Site", 
                    "Test site for ReadOnlyTransactionRestApiTest", SiteVisibility.PUBLIC);
        this.testSiteNodeRef = siteInfo.getNodeRef();
        this.testSiteNodeRefString = this.testSiteNodeRef.toString().replace("://", "/");
        
        // ensure there are no containers present at the start of the test
        List<ChildAssociationRef> children = nodeService.getChildAssocs(this.testSiteNodeRef);
        assertTrue("The test site should not have any containers", children.isEmpty());
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        SiteInfo site = siteService.getSite(TEST_SITE_NAME);
        // use retrying transaction to delete the site
        this.transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                // delete the test site
                siteService.deleteSite(TEST_SITE_NAME);
                
                return null;
            }
        });
        nodeArchiveService.purgeArchivedNode(nodeArchiveService.getArchivedNode(site.getNodeRef()));
        
        AuthenticationUtil.clearCurrentSecurityContext();
    }
    
    public void testGetSiteDataLists() throws Exception
    {
        // TODO: Fixme - This REST API still requires a readwrite transaction to be successful
        
        Response response = sendRequest(new GetRequest(URL_GET_SITE_DATALISTS), 200);
        logResponse(response);
        assertEquals(Status.STATUS_OK, response.getStatus());
    }
    
    public void testGetSiteWiki() throws Exception
    {
        Response response = sendRequest(new GetRequest(URL_GET_SITE_WIKI), 200);
        logResponse(response);
        assertEquals(Status.STATUS_OK, response.getStatus());
    }
    
    public void testGetSiteWikiPage() throws Exception
    {
        Response response = sendRequest(new GetRequest(URL_GET_SITE_WIKI_PAGE), 404);
        logResponse(response);
        assertEquals(Status.STATUS_NOT_FOUND, response.getStatus());
    }
    
    public void testGetSiteWikiPageVersion() throws Exception
    {
        Response response = sendRequest(new GetRequest(URL_GET_SITE_WIKI_PAGE_VERSION), 404);
        logResponse(response);
        assertEquals(Status.STATUS_NOT_FOUND, response.getStatus());
    }
    
    public void testGetDoclibTreeNode() throws Exception
    {
        // TODO: Fixme - This REST API still requires a readwrite transaction to be successful
        
        Response response = sendRequest(new GetRequest(URL_GET_DOCLIB_TREENODE), 200);
        logResponse(response);
        assertEquals(Status.STATUS_OK, response.getStatus());
    }
    
    public void testGetDoclibDoclist() throws Exception
    {
        // TODO: Fixme - This REST API still requires a readwrite transaction to be successful
        
        Response response = sendRequest(new GetRequest(URL_GET_DOCLIB_DOCLIST), 200);
        logResponse(response);
        assertEquals(Status.STATUS_OK, response.getStatus());
    }
    
    public void testGetDoclib2Doclist() throws Exception
    {
        // TODO: Fixme - This REST API still requires a readwrite transaction to be successful
        
        Response response = sendRequest(new GetRequest(URL_GET_DOCLIB2_DOCLIST), 200);
        logResponse(response);
        assertEquals(Status.STATUS_OK, response.getStatus());
    }
    
    public void testGetDoclibImages() throws Exception
    {
        // TODO: Fixme - This REST API still requires a readwrite transaction to be successful
        
        Response response = sendRequest(new GetRequest(URL_GET_DOCLIB_IMAGES), 200);
        logResponse(response);
        assertEquals(Status.STATUS_OK, response.getStatus());
    }
    
    public void testGetDoclibCategoryNode() throws Exception
    {
        Response response = sendRequest(new GetRequest(MessageFormat.format(URL_GET_DOCLIB_CATEGORYNODE,
                    testSiteNodeRefString)), 200);
        logResponse(response);
        assertEquals(Status.STATUS_OK, response.getStatus());
    }
    
    public void testGetDoclibNode() throws Exception
    {
        Response response = sendRequest(new GetRequest(MessageFormat.format(URL_GET_DOCLIB_NODE, 
                    testSiteNodeRefString)), 200);
        logResponse(response);
        assertEquals(Status.STATUS_OK, response.getStatus());
    }
    
    public void testGetDoclib2Node() throws Exception
    {
        Response response = sendRequest(new GetRequest(MessageFormat.format(URL_GET_DOCLIB2_NODE, 
                    testSiteNodeRefString)), 200);
        logResponse(response);
        assertEquals(Status.STATUS_OK, response.getStatus());
    }
    
    public void testGetDoclibNodeLocation() throws Exception
    {
        Response response = sendRequest(new GetRequest(MessageFormat.format(URL_GET_DOCLIB_LOCATION,
                    testSiteNodeRefString)), 200);
        logResponse(response);
        assertEquals(Status.STATUS_OK, response.getStatus());
    }
    
    private void logResponse(Response response)
    {
        if (this.logEnabled)
        {
            try
            {
                System.out.println(response.getContentAsString());
            }
            catch (Exception e)
            {
                System.err.println("Unable to log response: " + e.toString());
            }
        }
    }
}
