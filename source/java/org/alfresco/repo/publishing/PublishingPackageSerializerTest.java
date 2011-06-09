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

import static org.mockito.Mockito.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.publishing.PublishingPackageImpl.PublishingPackageEntryImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transfer.manifest.TransferManifestNode;
import org.alfresco.repo.transfer.manifest.TransferManifestNodeFactory;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.publishing.PublishingQueue;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.site.SiteVisibility;
import org.alfresco.service.cmr.transfer.TransferDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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
public class PublishingPackageSerializerTest
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
    private PublishingService publishingService;
    private StandardPublishingPackageSerializer serializer;
    private TransferManifestNormalNode normalNode1;

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
        publishingService = (PublishingService) applicationContext.getBean("publishingService");
        serializer = (StandardPublishingPackageSerializer) applicationContext.getBean("publishingPackageSerializer");
        siteId = GUID.generate();
        siteService.createSite("test", siteId, "Test site created by PublishingPackageSerializerTest",
                "Test site created by PublishingPackageSerializerTest", SiteVisibility.PUBLIC);

        normalNode1 = new TransferManifestNormalNode();
        normalNode1.setAccessControl(null);

        Set<QName> aspects = new HashSet<QName>();
        aspects.add(ContentModel.ASPECT_AUDITABLE);
        aspects.add(ContentModel.ASPECT_TITLED);
        normalNode1.setAspects(aspects);

        List<ChildAssociationRef> childAssocs = new ArrayList<ChildAssociationRef>();
        normalNode1.setChildAssocs(childAssocs);

        String guid = GUID.generate();
        NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, guid);
        normalNode1.setNodeRef(nodeRef);

        ChildAssociationRef primaryParentAssoc = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, new NodeRef(
                StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "MY_PARENT_NODEREF"), QName.createQName(
                        NamespaceService.CONTENT_MODEL_1_0_URI, "localname"), nodeRef, true, -1);
        List<ChildAssociationRef> parentAssocs = new ArrayList<ChildAssociationRef>();
        parentAssocs.add(primaryParentAssoc);
        normalNode1.setParentAssocs(parentAssocs);
        
        Path path = new Path();
        path.append(new Path.ChildAssocElement(primaryParentAssoc));
        normalNode1.setParentPath(path);
        
        normalNode1.setPrimaryParentAssoc(primaryParentAssoc);
        
        Map<QName,Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, guid);
        normalNode1.setProperties(props);
        
        List<AssociationRef> sourceAssocs = new ArrayList<AssociationRef>();
        sourceAssocs.add(new AssociationRef(nodeRef, ContentModel.ASSOC_ATTACHMENTS, nodeRef));
        sourceAssocs.add(new AssociationRef(nodeRef, ContentModel.ASSOC_REFERENCES, nodeRef));
        normalNode1.setSourceAssocs(sourceAssocs);

        List<AssociationRef> targetAssocs = new ArrayList<AssociationRef>();
        targetAssocs.add(new AssociationRef(nodeRef, ContentModel.ASSOC_ATTACHMENTS, nodeRef));
        targetAssocs.add(new AssociationRef(nodeRef, ContentModel.ASSOC_REFERENCES, nodeRef));
        normalNode1.setTargetAssocs(targetAssocs);
        
        normalNode1.setType(ContentModel.TYPE_CONTENT);
        normalNode1.setUuid(guid);
    }

    @Test
    public void testSerializer() throws Exception
    {
        PublishingQueue queue = publishingService.getEnvironment(siteId, EnvironmentHelper.LIVE_ENVIRONMENT_NAME)
                .getPublishingQueue();

        MutablePublishingPackageImpl packageImpl = (MutablePublishingPackageImpl) queue.createPublishingPackage();
        TransferManifestNodeFactory mockTMNFactory = mock(TransferManifestNodeFactory.class);
        packageImpl.setTransferManifestNodeFactory(mockTMNFactory);

        doAnswer(new Answer<TransferManifestNode>()
        {
            @Override
            public TransferManifestNode answer(InvocationOnMock invocation) throws Throwable
            {
                return normalNode1;
            }
        }).when(mockTMNFactory).createTransferManifestNode(any(NodeRef.class), any(TransferDefinition.class));

        packageImpl.addNodesToPublish(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE,"Hello"));

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        serializer.serialize(packageImpl, os);
        os.close();
        
        byte[] output = os.toByteArray();
        
        ByteArrayInputStream is = new ByteArrayInputStream(output);
        PublishingPackageImpl deserializedPublishingPackage = (PublishingPackageImpl) serializer.deserialize(is);
        Map<NodeRef,PublishingPackageEntry> entryMap = deserializedPublishingPackage.getEntryMap();
        assertEquals(1, entryMap.size());
        assertTrue(entryMap.containsKey(normalNode1.getNodeRef()));
        PublishingPackageEntryImpl entry = (PublishingPackageEntryImpl) entryMap.get(normalNode1.getNodeRef());
        assertEquals(TransferManifestNormalNode.class, entry.getPayload().getClass());
        TransferManifestNormalNode deserializedNode = (TransferManifestNormalNode) entry.getPayload();
        assertEquals(normalNode1.getType(), deserializedNode.getType());
    }
}
