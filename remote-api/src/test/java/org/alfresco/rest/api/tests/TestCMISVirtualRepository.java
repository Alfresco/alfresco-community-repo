/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.rest.api.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertNotNull;

import java.util.*;
import java.util.stream.Stream;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.junit.After;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl;
import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.CMISDispatcherRegistry;
import org.alfresco.opencmis.CMISPropertyBasedVirtualRepository;
import org.alfresco.opencmis.CMISVirtualRepository;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.rest.api.tests.client.PublicApiClient.CmisSession;
import org.alfresco.rest.api.tests.client.RequestContext;
import org.alfresco.rest.api.tests.client.data.CMISNode;
import org.alfresco.rest.api.tests.client.data.FolderNode;
import org.alfresco.rest.api.tests.client.data.Person;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;

public class TestCMISVirtualRepository extends EnterpriseTestApi
{
    private NodeService nodeService;
    private TransactionService transactionService;
    private RepoService.TestNetwork testNetwork;
    private CMISConnector cmisConnector;
    private NamespaceService namespaceService;
    private NodeRef rootNode;
    private CMISVirtualRepository virtualRepositoryToRestore;

    @Override
    public void setup() throws Exception
    {
        final ApplicationContext ctx = getTestFixture().getApplicationContext();
        testNetwork = getTestFixture().getRandomNetwork();

        namespaceService = ctx.getBean("NamespaceService", NamespaceService.class);
        nodeService = ctx.getBean("NodeService", NodeService.class);
        transactionService = ctx.getBean("transactionService", TransactionService.class);
        cmisConnector = ctx.getBean("CMISConnector", CMISConnector.class);
        rootNode = TenantUtil.runAsSystemTenant(cmisConnector::getRootNodeRef, testNetwork.getId());

        virtualRepositoryToRestore = cmisConnector.getVirtualRepository();
    }

    @After
    public void tearDown() throws Exception
    {
        cmisConnector.setVirtualRepository(virtualRepositoryToRestore);
    }

    @Test
    public void shouldFailWhenRootNotExposed()
    {
        final CmisSession cmisSession = givenCmisSession();
        final CmisVirtualRepositoryTestSupport testSupport = enableVirtualRepository();
        testSupport.hide(rootNode);

        assertThatExceptionOfType(CmisObjectNotFoundException.class)
                .isThrownBy(() -> cmisSession.getObjectByPath("/"));
    }

    @Test
    public void shouldNotFailWhenRootIsExposed()
    {
        final CmisSession cmisSession = givenCmisSession();
        final CmisVirtualRepositoryTestSupport testSupport = enableVirtualRepository();
        testSupport.expose(rootNode);

        assertNotNull(cmisSession.getObjectByPath("/"));
    }

    @Test
    public void shouldLimitChildren()
    {
        final CmisSession cmisSession = givenCmisSession();
        final FolderNode rootFolder = cmisSession.getRootFolder();

        final List<NodeRef> unrestrictedChildNodeRefs = getNodeRefs(cmisSession.getChildren(rootFolder.getNodeId(), 0, 10_000));
        assertThat(unrestrictedChildNodeRefs)
                .isNotNull()
                .isNotEmpty()
                .hasSizeGreaterThan(2);

        final CmisVirtualRepositoryTestSupport testSupport = enableVirtualRepository();

        testSupport.expose(rootNode);
        final List<NodeRef> noChildrenExposed = getNodeRefs(cmisSession.getChildren(rootFolder.getNodeId(), 0, 10_000));
        assertThat(noChildrenExposed)
                .isNotNull()
                .isEmpty();

        testSupport.expose(unrestrictedChildNodeRefs.get(0), unrestrictedChildNodeRefs.get(1));
        final List<NodeRef> restrictedChildNodeRefs = getNodeRefs(cmisSession.getChildren(rootFolder.getNodeId(), 0, 10_000));
        assertThat(restrictedChildNodeRefs)
                .isNotNull()
                .hasSize(2)
                .contains(unrestrictedChildNodeRefs.get(0), unrestrictedChildNodeRefs.get(1));
    }

    @Test
    public void shouldLimitQuery()
    {
        final CmisSession cmisSession = givenCmisSession();

        final List<NodeRef> unrestrictedResult = getNodeRefs(cmisSession.query("SELECT * FROM cmis:folder", false, 0, 10_000));
        assertThat(unrestrictedResult)
                .isNotNull()
                .isNotEmpty()
                .hasSizeGreaterThan(10);

        final CmisVirtualRepositoryTestSupport testSupport = enableVirtualRepository();

        testSupport.expose(rootNode);
        final List<NodeRef> onlyRootFolder = getNodeRefs(cmisSession.query("SELECT * FROM cmis:folder", false, 0, 10_000));
        assertThat(onlyRootFolder)
                .isNotNull()
                .hasSize(1)
                .contains(rootNode);

        final Set<NodeRef> toExpose = new HashSet<>();
        toExpose.add(rootNode);
        toExpose.addAll(unrestrictedResult.subList(0, 10));
        testSupport.expose(toExpose.toArray(new NodeRef[10]));

        final List<NodeRef> restrictedResult = getNodeRefs(cmisSession.query("SELECT * FROM cmis:folder", false, 0, 10_000));
        assertThat(restrictedResult)
                .isNotNull()
                .isNotEmpty()
                .hasSameElementsAs(toExpose);
    }

    private CmisSession givenCmisSession()
    {
        final String personId = testNetwork.getPersonIds().stream().filter(Objects::nonNull).findFirst().orElse(null);
        assertNotNull(personId);

        Person person = repoService.getPerson(personId);
        assertNotNull(person);

        publicApiClient.setRequestContext(new RequestContext(testNetwork.getId(), person.getUserName()));
        publicApiClient.setCmisSessionAdjuster(this::disableCmisClientCache);
        return publicApiClient.createPublicApiCMISSession(CMISDispatcherRegistry.Binding.browser, "1.1", AlfrescoObjectFactoryImpl.class.getName());
    }

    private void disableCmisClientCache(Session session)
    {
        session.getDefaultContext().setCacheEnabled(false);
    }

    private List<NodeRef> getNodeRefs(FolderNode folderNode)
    {
        return getNodeRefs(Stream.concat(
                folderNode.getDocumentNodes().values().stream(),
                folderNode.getFolderNodes().values().stream()));
    }

    private List<NodeRef> getNodeRefs(Collection<CMISNode> cmisNodes)
    {
        return getNodeRefs(cmisNodes.stream());
    }

    private List<NodeRef> getNodeRefs(Stream<CMISNode> cmisNodes)
    {
        return cmisNodes
                .map(CMISNode::getNodeId)
                .map(id -> new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id))
                .toList();
    }

    private CmisVirtualRepositoryTestSupport enableVirtualRepository()
    {
        return enableVirtualRepository("test" + System.currentTimeMillis());
    }

    private CmisVirtualRepositoryTestSupport enableVirtualRepository(String id)
    {
        final CMISVirtualRepository virtualRepository = new CMISPropertyBasedVirtualRepository(nodeService, namespaceService,
                CmisVirtualRepositoryTestSupport.ASPECT, CmisVirtualRepositoryTestSupport.PROPERTY, id);

        cmisConnector.setVirtualRepository(virtualRepository);

        return new CmisVirtualRepositoryTestSupport(nodeService, transactionService, testNetwork.getId(), id);
    }

    private static class CmisVirtualRepositoryTestSupport
    {
        private static final QName ASPECT = QName.createQName("http://www.alfresco.org/test/virtcmis/1.0", "VirtualRepository");
        private static final QName PROPERTY = QName.createQName("http://www.alfresco.org/test/virtcmis/1.0", "repositoryId");
        private final NodeService nodeService;
        private final TransactionService transactionService;

        private final String tenantId;
        private final String repoId;

        public CmisVirtualRepositoryTestSupport(NodeService nodeService, TransactionService transactionService, String tenantId, String repoId)
        {
            this.nodeService = Objects.requireNonNull(nodeService);
            this.transactionService = Objects.requireNonNull(transactionService);
            this.tenantId = Objects.requireNonNull(tenantId);
            this.repoId = Objects.requireNonNull(repoId);
        }

        public void hide(NodeRef... nodes)
        {
            TenantUtil.runAsSystemTenant(() -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
                Stream.of(nodes).forEach(node -> nodeService.removeAspect(node, ASPECT));
                return null;
            }, false, true), tenantId);
        }

        public void expose(NodeRef... nodes)
        {
            TenantUtil.runAsSystemTenant(() -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
                Stream.of(nodes).forEach(node -> nodeService.addAspect(node, ASPECT, Map.of(PROPERTY, repoId)));
                return null;
            }, false, true), tenantId);
        }
    }
}
