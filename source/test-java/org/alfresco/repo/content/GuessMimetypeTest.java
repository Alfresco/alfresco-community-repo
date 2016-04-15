/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.content;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Tests for guess mimetype for file
 * 
 * This includes a test for apple specific hidden files
 * 
 * @author rneamtu
 */

public class GuessMimetypeTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private NodeService nodeService;
    private ContentService contentService;
    private RetryingTransactionHelper retryingTransactionHelper;
    private StoreRef storeRef;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;

    @Override
    public void setUp() throws Exception
    {
        this.nodeService = (NodeService) ctx.getBean("nodeService");
        this.contentService = (ContentService) ctx.getBean("ContentService");

        this.retryingTransactionHelper = (RetryingTransactionHelper) ctx.getBean("retryingTransactionHelper");

        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                // As system user
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

                storeRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

                rootNodeRef = nodeService.getRootNode(storeRef);

                return null;
            }
        });
    }

    public void testAppleMimetype() throws Exception
    {
        String content = "This is some content";
        String fileName = "._myfile.pdf";
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

                Map<QName, Serializable> properties = new HashMap<QName, Serializable>(13);
                properties.put(ContentModel.PROP_NAME, (Serializable) "test.txt");

                nodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}testnode"),
                        ContentModel.TYPE_CONTENT).getChildRef();
                return null;
            }
        });

        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);

        writer.putContent(content);
        writer.guessMimetype(fileName);

        assertEquals(MimetypeMap.MIMETYPE_APPLEFILE, writer.getMimetype());

        fileName = "myfile.pdf";
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Object>()
        {
            @Override
            public Object execute() throws Throwable
            {
                AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());

                Map<QName, Serializable> properties = new HashMap<QName, Serializable>(13);
                properties.put(ContentModel.PROP_NAME, (Serializable) "test.txt");

                nodeRef = nodeService.createNode(rootNodeRef, ContentModel.ASSOC_CHILDREN, QName.createQName("{test}testnode"),
                        ContentModel.TYPE_CONTENT).getChildRef();
                return null;
            }
        });

        ContentWriter writer2 = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
        content = "This is other content";
        writer2.putContent(content);
        writer2.guessMimetype(fileName);

        assertNotSame(MimetypeMap.MIMETYPE_APPLEFILE, writer2.getMimetype());

    }
}
