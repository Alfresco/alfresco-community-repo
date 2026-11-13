/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2026 Alfresco Software Limited
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
package org.alfresco.repo.rendition2;

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.awaitility.Awaitility.await;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ContentMetadataExtracter;
import org.alfresco.repo.content.metadata.AsynchronousExtractor;
import org.alfresco.repo.content.transform.UnsupportedTransformationException;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.rawevents.TestBroker;
import org.alfresco.repo.rawevents.TransactionAwareEventProducer;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.thumbnail.CreateThumbnailActionExecuter;
import org.alfresco.repo.thumbnail.ThumbnailRegistry;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.transform.config.CoreFunction;
import org.alfresco.transform.registry.TransformServiceRegistry;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * Every way of requesting a rendition or transform keeps working while the message broker is unreachable: the requesting thread returns, the request waits on a background thread that holds no transaction or DB connection, and it is sent once the broker is back.
 * <p>
 * The broker is a {@link TestBroker} and transforms are supported for {@link #TEST_MIMETYPE} without a T-Engine, so no network is involved. Each wait has an upper bound that is reached only when the test fails.
 */
public class RenditionRequestsDuringBrokerOutageTest extends BaseSpringTest
{
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private static final String TEST_MIMETYPE = "application/x-broker-outage-test";
    private static final String DISPATCH_THREAD_PREFIX = "rendition-dispatch-";
    private static final String DOCLIB = "doclib";
    private static final String IMGPREVIEW = "imgpreview";
    private static final String ADMIN = "admin";

    @Autowired
    private RenditionService2Impl renditionService2;
    @Autowired
    private TransformClient transformClient;
    @Autowired
    private TransformServiceRegistry transformServiceRegistry;
    @Autowired
    private ThumbnailRegistry thumbnailRegistry;
    @Autowired
    @Qualifier("extractor.Asynchronous") private AsynchronousExtractor asynchronousExtractor;
    @Autowired
    private ServiceRegistry serviceRegistry;
    @Autowired
    private ActionService actionService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private ContentService contentService;
    @Autowired
    private Repository repositoryHelper;
    @Autowired
    private MutableAuthenticationService authenticationService;
    @Autowired
    private PersonService personService;
    @Autowired
    private PermissionService permissionService;

    private final TestBroker broker = new TestBroker();
    private final ExecutorService requestThreads = Executors.newCachedThreadPool();
    private final Set<NodeRef> testDocuments = ConcurrentHashMap.newKeySet();
    private final Map<String, String> transformUsers = new ConcurrentHashMap<>();
    private volatile boolean createRenditions;
    private NodeRef folder;
    private String user;

    @Before
    public void before()
    {
        TransactionAwareEventProducer eventProducer = broker.eventProducer();
        renditionService2.setTransformClient(new BrokerTransformClient(eventProducer));
        TransformServiceRegistry supportsTestMimetype = new TestMimetypeTransformServiceRegistry(transformServiceRegistry);
        thumbnailRegistry.setTransformServiceRegistry(supportsTestMimetype);
        asynchronousExtractor.setTransformServiceRegistry(supportsTestMimetype);

        user = "brokerOutageUser" + GUID.generate();
        folder = asAdmin(() -> {
            NodeRef newFolder = nodeService.createNode(repositoryHelper.getCompanyHome(), ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "brokerOutage" + GUID.generate()),
                    ContentModel.TYPE_FOLDER, properties(ContentModel.PROP_NAME, "RenditionRequestsDuringBrokerOutageTest " + GUID.generate()))
                    .getChildRef();
            authenticationService.createAuthentication(user, "password".toCharArray());
            personService.createPerson(properties(ContentModel.PROP_USERNAME, user));
            permissionService.setPermission(newFolder, user, PermissionService.COORDINATOR, true);
            return newFolder;
        });
    }

    @After
    public void after()
    {
        broker.start();
        requestThreads.shutdownNow();
        renditionService2.setTransformClient(transformClient);
        thumbnailRegistry.setTransformServiceRegistry(transformServiceRegistry);
        asynchronousExtractor.setTransformServiceRegistry(transformServiceRegistry);
        try
        {
            asAdmin(() -> {
                nodeService.deleteNode(folder);
                personService.deletePerson(user);
                return null;
            });
        }
        catch (RuntimeException e)
        {
            logger.warn("Test data was not cleaned up: " + e.getMessage(), e);
        }
    }

    /** Share folder view: Share requests each missing doclib thumbnail through thumbnail.get.js. */
    @Test
    public void shareThumbnailRequest() throws Exception
    {
        NodeRef document = createDocument();
        broker.stop();

        request(ADMIN, () -> new ScriptNode(document, serviceRegistry, scriptScope()).createThumbnail(DOCLIB, true));

        assertWaitingThenSent(message(document, DOCLIB));
    }

    /** Upload from Share, ADW or the REST API: the upload runs the extract-metadata action. */
    @Test
    public void metadataExtractionOnUpload() throws Exception
    {
        NodeRef document = createDocument();
        broker.stop();

        request(ADMIN, () -> actionService.executeAction(actionService.createAction(ContentMetadataExtracter.EXECUTOR_NAME), document));

        assertWaitingThenSent(message(document, TransformDefinition.convertToRenditionName("alfresco-metadata-extract/" + TEST_MIMETYPE)));
    }

    /** REST upload with renditions=..., and POST /nodes/{id}/renditions. */
    @Test
    public void restRenditionRequests() throws Exception
    {
        NodeRef document = createDocument();
        broker.stop();

        request(ADMIN, () -> {
            serviceRegistry.getRenditionService2().render(document, DOCLIB);
            serviceRegistry.getRenditionService2().render(document, IMGPREVIEW);
        });

        assertWaitingThenSent(message(document, DOCLIB), message(document, IMGPREVIEW));
    }

    /** New version of a document that already has a thumbnail, saved by a non-admin user: re-rendered as that user. */
    @Test
    public void newVersionOfARenderedDocument() throws Exception
    {
        NodeRef document = createDocument();
        createRenditions = true;
        request(ADMIN, () -> renditionService2.render(document, DOCLIB));
        await().atMost(TIMEOUT).until(() -> asAdmin(() -> renditionService2.getRenditionByName(document, DOCLIB)) != null);
        createRenditions = false;
        broker.stop();

        request(user, () -> writeContent(document, "new version"));

        assertWaitingThenSent(message(document, DOCLIB));
        assertEquals(user, transformUsers.get(message(document, DOCLIB)));
    }

    /** Rules and scripts that run the create-thumbnail action in the background. */
    @Test
    public void createThumbnailAction() throws Exception
    {
        NodeRef document = createDocument();
        broker.stop();

        request(ADMIN, () -> {
            Action action = actionService.createAction(CreateThumbnailActionExecuter.NAME);
            action.setParameterValue(CreateThumbnailActionExecuter.PARAM_THUMBANIL_NAME, DOCLIB);
            action.setExecuteAsynchronously(true);
            actionService.executeAction(action, document, false, false);
        });

        assertWaitingThenSent(message(document, DOCLIB));
    }

    /** Transform API requests (own request id) and transforms requested by custom code with their own reply queue. */
    @Test
    public void transformRequestsWithTheirOwnReplyQueue() throws Exception
    {
        NodeRef document = createDocument();
        broker.stop();

        request(ADMIN, () -> {
            renditionService2.transform(document, new TransformDefinition("transformApi", "image/png", Map.of(), "clientData", "replyQueue", "requestId", null));
            renditionService2.transform(document, new TransformDefinition("image/png", Map.of(), "customClientData", "customReplyQueue", null));
        });

        assertWaitingThenSent(message(document, TransformDefinition.convertToRenditionName("transformApi")), message(document, null));
    }

    /** Many more requests than background workers: none of the requesting threads waits. */
    @Test
    public void moreRequestsThanBackgroundWorkers() throws Exception
    {
        List<String> messages = new ArrayList<>();
        List<NodeRef> documents = new ArrayList<>();
        for (int i = 0; i < 20; i++)
        {
            NodeRef document = createDocument();
            documents.add(document);
            messages.add(message(document, DOCLIB));
        }
        broker.stop();

        for (NodeRef document : documents)
        {
            request(ADMIN, () -> renditionService2.render(document, DOCLIB));
        }

        assertWaitingThenSent(messages.toArray(new String[0]));
    }

    /**
     * Waits until the broker has received at least one of the messages while it is down, checks that every received one waits on a background thread outside any transaction, then starts the broker and waits until all of them are sent.
     */
    private void assertWaitingThenSent(String... messages)
    {
        Collection<String> expected = List.of(messages);
        await().atMost(TIMEOUT).until(() -> broker.receivedBodies().stream().anyMatch(expected::contains));
        assertTrue(broker.delivered().stream().noneMatch(expected::contains));

        broker.start();
        await().atMost(TIMEOUT).until(() -> broker.delivered().containsAll(expected));
        for (TestBroker.Send send : broker.received())
        {
            if (expected.contains(send.body()))
            {
                assertTrue(send.toString(), send.thread().startsWith(DISPATCH_THREAD_PREFIX));
                assertTrue(send.toString(), send.outsideTransaction());
            }
        }
    }

    /** Runs the work as the user in a transaction on another thread, like a request thread, and fails instead of hanging if the thread does not return. */
    private void request(String runAsUser, Runnable work) throws Exception
    {
        Future<?> future = requestThreads.submit(() -> AuthenticationUtil.runAs(() -> transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
            work.run();
            return null;
        }), runAsUser));
        try
        {
            future.get(TIMEOUT.getSeconds(), SECONDS);
        }
        catch (TimeoutException e)
        {
            fail("The requesting thread did not return within " + TIMEOUT.getSeconds() + " s");
        }
    }

    private NodeRef createDocument()
    {
        NodeRef document = asAdmin(() -> {
            NodeRef newDocument = nodeService.createNode(folder, ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "document" + GUID.generate()),
                    ContentModel.TYPE_CONTENT, properties(ContentModel.PROP_NAME, "document " + GUID.generate()))
                    .getChildRef();
            writeContent(newDocument, "first version");
            return newDocument;
        });
        testDocuments.add(document);
        return document;
    }

    private void writeContent(NodeRef document, String content)
    {
        ContentWriter writer = contentService.getWriter(document, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(TEST_MIMETYPE);
        writer.putContent(content + " " + GUID.generate());
    }

    private <T> T asAdmin(AuthenticationUtil.RunAsWork<T> work)
    {
        return AuthenticationUtil.runAs(() -> transactionService.getRetryingTransactionHelper().doInTransaction(work::doWork), ADMIN);
    }

    private static Map<QName, Serializable> properties(QName name, Serializable value)
    {
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(name, value);
        return properties;
    }

    /** An empty JavaScript scope, as the web script gives ScriptNode. */
    private static Scriptable scriptScope()
    {
        Context context = Context.enter();
        try
        {
            return context.initStandardObjects();
        }
        finally
        {
            Context.exit();
        }
    }

    private static String message(NodeRef document, String renditionName)
    {
        return document + "|" + renditionName;
    }

    /** Sends a transform request for the test documents, like the Transform Service client does; delegates everything else. */
    private class BrokerTransformClient implements TransformClient
    {
        private final TransactionAwareEventProducer eventProducer;

        BrokerTransformClient(TransactionAwareEventProducer eventProducer)
        {
            this.eventProducer = eventProducer;
        }

        @Override
        public void checkSupported(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String sourceMimetype, long sourceSizeInBytes, String contentUrl)
        {
            if (!testDocuments.contains(sourceNodeRef))
            {
                transformClient.checkSupported(sourceNodeRef, renditionDefinition, sourceMimetype, sourceSizeInBytes, contentUrl);
            }
        }

        @Override
        public void transform(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String user, int sourceContentHashCode)
                throws UnsupportedTransformationException, ContentIOException
        {
            if (!testDocuments.contains(sourceNodeRef))
            {
                transformClient.transform(sourceNodeRef, renditionDefinition, user, sourceContentHashCode);
            }
            else if (createRenditions)
            {
                renditionService2.consume(sourceNodeRef, new ByteArrayInputStream("rendition".getBytes(StandardCharsets.UTF_8)),
                        renditionDefinition, sourceContentHashCode);
            }
            else
            {
                String message = message(sourceNodeRef, renditionDefinition.getRenditionName());
                transformUsers.put(message, user);
                eventProducer.send("test:transform-requests", message);
            }
        }
    }

    /** Supports every transform from {@link #TEST_MIMETYPE}; delegates everything else. */
    private static class TestMimetypeTransformServiceRegistry implements TransformServiceRegistry
    {
        private final TransformServiceRegistry delegate;

        TestMimetypeTransformServiceRegistry(TransformServiceRegistry delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public boolean isSupported(String sourceMimetype, long sourceSizeInBytes, String targetMimetype, Map<String, String> actualOptions, String transformName)
        {
            return TEST_MIMETYPE.equals(sourceMimetype) || delegate.isSupported(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions, transformName);
        }

        @Override
        public long findMaxSize(String sourceMimetype, String targetMimetype, Map<String, String> actualOptions, String transformName)
        {
            return TEST_MIMETYPE.equals(sourceMimetype) ? -1 : delegate.findMaxSize(sourceMimetype, targetMimetype, actualOptions, transformName);
        }

        @Override
        public String findTransformerName(String sourceMimetype, long sourceSizeInBytes, String targetMimetype, Map<String, String> actualOptions, String renditionName)
        {
            return TEST_MIMETYPE.equals(sourceMimetype) ? "test" : delegate.findTransformerName(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions, renditionName);
        }

        @Override
        public boolean isSupported(CoreFunction function, String transformerName)
        {
            return delegate.isSupported(function, transformerName);
        }
    }
}
