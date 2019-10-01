/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts;

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
 * Set of tests that ensure GET REST APIs are run successfully in a read-only
 * transaction (ALF-10179).
 * 
 * Some webscripts have a side effect of creating a "container" these tests
 * are to ensure this is handled gracefully in a way that allows the main 
 * transaction to be declared as readonly for performance reasons.
 * 
 * @author Gavin Cornwell
 * @since 4.0
 */
public class ReadOnlyTransactionInGetRestApiTest extends BaseWebScriptTest
{
    private static final String TEST_SITE_NAME = "readOnlyTestSite";
    

    private static final String URL_GET_SITE_FORUM_POSTS = "/api/forum/site/" + TEST_SITE_NAME + "/discussions/posts";
    private static final String URL_GET_SITE_BLOG = "/api/blog/site/" + TEST_SITE_NAME + "/blog";
    private static final String URL_GET_SITE_LINKS = "/api/links/site/" + TEST_SITE_NAME + "/links?page=1&pageSize=10";
    private static final String URL_GET_SITE_LINK = "/api/links/link/site/" + TEST_SITE_NAME + "/links/123456789";
    private static final String URL_GET_SITE_TAGS = "/api/tagscopes/site/" + TEST_SITE_NAME + "/tags";

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
       
    public void testGetSiteForumPosts() throws Exception
    {
        Response response = sendRequest(new GetRequest(URL_GET_SITE_FORUM_POSTS), 200);
        logResponse(response);
        assertEquals(Status.STATUS_OK, response.getStatus());
    }
    
    public void testGetSiteBlog() throws Exception
    {
        // TODO: Fixme - This REST API still requires a readwrite transaction to be successful
        //       Also add tests for all other blog GET REST APIs
        
        Response response = sendRequest(new GetRequest(URL_GET_SITE_BLOG), 200);
        logResponse(response);
        assertEquals(Status.STATUS_OK, response.getStatus());
    }
    
    public void testGetSiteLinks() throws Exception
    {
        Response response = sendRequest(new GetRequest(URL_GET_SITE_LINKS), 200);
        logResponse(response);
        assertEquals(Status.STATUS_OK, response.getStatus());
    }
    
    public void testGetSiteLink() throws Exception
    {
        Response response = sendRequest(new GetRequest(URL_GET_SITE_LINK), 404);
        logResponse(response);
        assertEquals(Status.STATUS_NOT_FOUND, response.getStatus());
    }
    
    public void testGetSiteTags() throws Exception
    {
        Response response = sendRequest(new GetRequest(URL_GET_SITE_TAGS), 200);
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
