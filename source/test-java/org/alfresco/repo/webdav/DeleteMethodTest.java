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
package org.alfresco.repo.webdav;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** 
 * 
 * Class for webdav delete method unit tests
 * 
 * @author jcule
 *
 */
public class DeleteMethodTest
{
    //protected static Log logger = LogFactory.getLog("org.alfresco.webdav.protocol");
    
    private static ApplicationContext ctx;
    
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private DeleteMethod method;
    private UserTransaction txn = null;
    
    private SearchService searchService;
    private TransactionService transactionService;
    private NodeService nodeService;
    private ContentService contentService;
    private WebDAVHelper webDAVHelper;
    
    private NodeRef companyHomeNodeRef;
    private NodeRef versionableDoc;
    private String versionableDocName;
    private StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");


    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        ctx = ApplicationContextHelper.getApplicationContext(new String[]
            {
                "classpath:alfresco/application-context.xml",
                "classpath:alfresco/web-scripts-application-context.xml",
                "classpath:alfresco/remote-api-context.xml"
            });
    }

    @Before
    public void setUp() throws Exception
    {
        transactionService = ctx.getBean("transactionService", TransactionService.class);
        searchService = ctx.getBean("SearchService", SearchService.class);
        nodeService = ctx.getBean("NodeService", NodeService.class);
        contentService = ctx.getBean("contentService", ContentService.class);
        webDAVHelper = ctx.getBean("webDAVHelper", WebDAVHelper.class);         
    }
    
    @After
    public void tearDown() throws Exception
    {
        method = null;
        request = null;
        response = null;

        if (txn.getStatus() == Status.STATUS_MARKED_ROLLBACK)
        {
            txn.rollback();
        }
        else
        {
            txn.commit();
        }
        
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
 
        nodeService.deleteNode(versionableDoc);

        // As per MNT-10037 try to create a node and delete it in the next txn
        txn = transactionService.getUserTransaction();
        txn.begin();

        Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
        String nodeName = "leak-session-doc-" + GUID.generate();
        properties.put(ContentModel.PROP_NAME, nodeName);

        NodeRef nodeRef = nodeService.createNode(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(ContentModel.USER_MODEL_URI, nodeName),
                ContentModel.TYPE_CONTENT, properties).getChildRef();
        contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true).putContent("WebDAVTestContent");

        txn.commit();

        txn = transactionService.getUserTransaction();
        txn.begin();

        nodeService.deleteNode(nodeRef);

        txn.commit();

        AuthenticationUtil.clearCurrentSecurityContext();
    }

    /**
     * Checks that file with the versionable aspect applied can be deleted   
     * 
     * @throws Exception
     */
    @Test
    public void testDeleteFileWithVersionableAspect() throws Exception
    {
        //create file
        createFileWithVersionableAscpect();

        //delete file 
        method = new DeleteMethod();
        request = new MockHttpServletRequest(WebDAV.METHOD_DELETE, "/alfresco/webdav/" + versionableDocName);
        response = new MockHttpServletResponse();
        request.setServerPort(8080);
        request.setServletPath("/webdav");
        method.setDetails(request, response, webDAVHelper, companyHomeNodeRef);
        method.execute(); 
       
        //check the response
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    }
    
    /**
     * Create file with versionable aspect
     * 
     * @throws Exception
     */
    private void createFileWithVersionableAscpect() throws Exception
    {
        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getAdminUserName());

        companyHomeNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Throwable
            {
                // find "Company Home"
                ResultSet resultSet = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home\"");
                NodeRef result = resultSet.getNodeRef(0);
                resultSet.close();

                return result;
            }
         });
         txn = transactionService.getUserTransaction();
         txn.begin();
         
         // Create a test file with versionable aspect and content
         Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
         versionableDocName = "doc-" + GUID.generate();
         properties.put(ContentModel.PROP_NAME, versionableDocName);

         versionableDoc = nodeService.createNode(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(ContentModel.USER_MODEL_URI, versionableDocName),
         ContentModel.TYPE_CONTENT, properties).getChildRef();
         contentService.getWriter(versionableDoc, ContentModel.PROP_CONTENT, true).putContent("WebDAVTestContent");
         nodeService.addAspect(versionableDoc, ContentModel.ASPECT_VERSIONABLE, null);

         txn.commit();

         txn = transactionService.getUserTransaction();
         txn.begin();
    }

}
