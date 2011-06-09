/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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

package org.alfresco.repo.publishing;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;

import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Brian
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:alfresco/application-context.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class EnvironmentHelperTest
{
    @Autowired
    protected ApplicationContext applicationContext;
    protected ServiceRegistry serviceRegistry;
    protected RetryingTransactionHelper retryingTransactionHelper;
    protected NodeService nodeService;
    protected WorkflowService workflowService;
    protected FileFolderService fileFolderService;
    protected SiteService siteService;

    protected AuthenticationComponent authenticationComponent;
    private String siteId;
    private EnvironmentHelper environmentHelper;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        serviceRegistry = (ServiceRegistry) applicationContext.getBean(ServiceRegistry.SERVICE_REGISTRY);
        serviceRegistry.getAuthenticationService().authenticate("admin", "admin".toCharArray());

        retryingTransactionHelper = serviceRegistry.getRetryingTransactionHelper();
        fileFolderService = serviceRegistry.getFileFolderService();
        workflowService = serviceRegistry.getWorkflowService();
        nodeService = serviceRegistry.getNodeService();
        siteService = serviceRegistry.getSiteService();

        environmentHelper = (EnvironmentHelper) applicationContext.getBean("environmentHelper");
        siteId = GUID.generate();
        siteService.createSite("test", siteId, "Test site created by ChannelServiceImplIntegratedTest",
                "Test site created by ChannelServiceImplIntegratedTest", SiteVisibility.PUBLIC);

    }

    @Test
    public void testGetEnvironments() throws Exception
    {
        Map<String, NodeRef> environments = environmentHelper.getEnvironments(siteId);
        assertTrue(environments.size() == 1);
        NodeRef liveEnvironment = environments.get(EnvironmentHelper.LIVE_ENVIRONMENT_NAME);
        assertNotNull(liveEnvironment);
        assertTrue(nodeService.exists(liveEnvironment));
        assertEquals(PublishingModel.TYPE_ENVIRONMENT, nodeService.getType(liveEnvironment));
    }

    @Test
    public void testGetEnvironmentByName() throws Exception
    {
        NodeRef liveEnvironment = environmentHelper.getEnvironment(siteId, EnvironmentHelper.LIVE_ENVIRONMENT_NAME);
        assertNotNull(liveEnvironment);
        assertTrue(nodeService.exists(liveEnvironment));
        assertEquals(PublishingModel.TYPE_ENVIRONMENT, nodeService.getType(liveEnvironment));
    }

    @Test
    public void testGetPublishingQueue() throws Exception
    {
        NodeRef liveEnvironment = environmentHelper.getEnvironment(siteId, EnvironmentHelper.LIVE_ENVIRONMENT_NAME);
        NodeRef publishingQueue = environmentHelper.getPublishingQueue(liveEnvironment);
        assertNotNull(publishingQueue);
        assertTrue(nodeService.exists(publishingQueue));
        assertEquals(PublishingModel.TYPE_PUBLISHING_QUEUE, nodeService.getType(publishingQueue));
        assertEquals(PublishingModel.ASSOC_PUBLISHING_QUEUE, nodeService.getPrimaryParent(publishingQueue)
                .getTypeQName());
    }

    @Test
    public void testMapNodeRef() throws Exception
    {
        String guid = GUID.generate();
        NodeRef testNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, guid);
        NodeRef liveEnvironmentNode = environmentHelper.getEnvironment(siteId, EnvironmentHelper.LIVE_ENVIRONMENT_NAME);
        NodeRef mappedNodeRef = environmentHelper.mapEditorialToEnvironment(liveEnvironmentNode, testNodeRef);
        assertNotSame(mappedNodeRef, testNodeRef);
        NodeRef unmappedNodeRef = environmentHelper.mapEnvironmentToEditorial(liveEnvironmentNode, mappedNodeRef);
        assertEquals(testNodeRef, unmappedNodeRef);
    }
}
