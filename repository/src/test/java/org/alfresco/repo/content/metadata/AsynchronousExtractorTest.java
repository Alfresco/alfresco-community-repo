/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.action.executer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.metadata.AbstractMappingMetadataExtracter;
import org.alfresco.repo.content.metadata.AsynchronousExtractor;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.content.transform.TransformerDebug;
import org.alfresco.repo.content.transform.UnsupportedTransformationException;
import org.alfresco.repo.rendition2.RenditionDefinition2;
import org.alfresco.repo.rendition2.RenditionService2Impl;
import org.alfresco.repo.rendition2.TransformClient;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.test_category.BaseSpringTestsCategory;
import org.alfresco.transform.client.registry.TransformServiceRegistry;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Arrays.asList;
import static org.alfresco.model.ContentModel.PROP_CONTENT;
import static org.alfresco.model.ContentModel.PROP_CREATED;
import static org.alfresco.model.ContentModel.PROP_CREATOR;
import static org.alfresco.model.ContentModel.PROP_MODIFIED;
import static org.alfresco.model.ContentModel.PROP_MODIFIER;
import static org.alfresco.repo.rendition2.RenditionService2Impl.SOURCE_HAS_NO_CONTENT;

/**
 * Tests the asynchronous extract and embed of metadata. This is normally performed in a T-Engine, but in this test
 * class is mocked using a separate Thread that returns well known values. What make the AsynchronousExtractor
 * different from other {@link AbstractMappingMetadataExtracter} sub classes is that the calling Thread does not
 * do the work of updating properties or the content, as the T-Engine will reply at some later point.
 *
 * @author adavis
 */
@Category(BaseSpringTestsCategory.class)
public class AsynchronousExtractorTest extends BaseSpringTest
{
    private final static String ID = GUID.generate();
    private static final String AFTER_CALLING_EXECUTE = "after calling execute";
    private static final String AFTER_THE_TRANSFORM = "after the transform";
    private static final Integer UNCHANGED_HASHCODE = null;
    private static final Integer CHANGED_HASHCODE = 1234;
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private NodeService nodeService;
    private ContentService contentService;
    private DictionaryService dictionaryService;
    private MimetypeService mimetypeService;
    private MetadataExtracterRegistry metadataExtracterRegistry;
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;
    private AsynchronousExtractor asynchronousExtractor;
    private NamespacePrefixResolver namespacePrefixResolver;
    private TransformerDebug transformerDebug;
    private TransactionService transactionService;
    private TransformServiceRegistry transformServiceRegistry;
    private TaggingService taggingService;
    private ContentMetadataExtracter contentMetadataExtracter;
    private ContentMetadataEmbedder contentMetadataEmbedder;
    private RenditionService2Impl renditionService2;
    private TransformClient transformClient;

    private long origSize;
    private Map<QName, Serializable> origProperties;
    private Map<QName, Serializable> expectedProperties;
    private Map<QName, Serializable> properties;

    private class TestAsynchronousExtractor extends AsynchronousExtractor
    {
        private final String mockResult;
        private final Integer changedHashcode;
        private final Random random = new Random();

        private boolean finished;

        TransformClient mockTransformClient = new TransformClient()
        {
            @Override
            public void checkSupported(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String sourceMimetype, long sourceSizeInBytes, String contentUrl)
            {
            }

            @Override
            public void transform(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, String user, int sourceContentHashCode)
                    throws UnsupportedTransformationException, ContentIOException
            {
                mockTransform(sourceNodeRef, renditionDefinition, sourceContentHashCode);
            }
        };

        /**
         * Creates an AsynchronousExtractor that simulates a extract or embed.
         *
         * @param mockResult      if specified indicates a value was returned. The result is read as a resource from
         *                        the classpath.
         * @param changedHashcode if specified indicates that the source node content changed or was deleted between
         *                        the request to extract or embed and the response.
         */
        TestAsynchronousExtractor(String mockResult, Integer changedHashcode)
        {
            this.mockResult = mockResult;
            this.changedHashcode = changedHashcode;

            setNodeService(nodeService);
            setNamespacePrefixResolver(namespacePrefixResolver);
            setTransformerDebug(transformerDebug);
            setRenditionService2(renditionService2);
            setContentService(contentService);
            setTransactionService(transactionService);
            setTransformServiceRegistry(transformServiceRegistry);
            setTaggingService(taggingService);
            setRegistry(metadataExtracterRegistry);
            setMimetypeService(mimetypeService);
            setDictionaryService(dictionaryService);
            setExecutorService(executorService);
            register();

            renditionService2.setTransformClient(mockTransformClient);
        }

        @Override
        public boolean isSupported(String sourceMimetype, long sourceSizeInBytes)
        {
            return true;
        }

        @Override
        public boolean isEmbedderSupported(String sourceMimetype, long sourceSizeInBytes)
        {
            return true;
        }

        private void mockTransform(NodeRef sourceNodeRef, RenditionDefinition2 renditionDefinition, int sourceContentHashCode)
        {
            try
            {
                transformerDebug.pushMisc();
                wait(50, 700);
            }
            finally
            {
                transformerDebug.popMisc();
            }

            int transformContentHashCode = changedHashcode == null ? sourceContentHashCode : changedHashcode;
            if (mockResult != null)
            {
                try (InputStream transformInputStream = getClass().getClassLoader().getResourceAsStream(mockResult))
                {
                    renditionService2.consume(sourceNodeRef, transformInputStream, renditionDefinition, transformContentHashCode);
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Could not read '" + mockResult + "' from the classpath.", e);
                }
            }
            else
            {
                renditionService2.failure(sourceNodeRef, renditionDefinition, transformContentHashCode);
            }

            synchronized (this)
            {
                finished = true;
                notifyAll();
            }
        }

        /**
         * Wait for a few milliseconds or until the finished flag is set.
         *
         * @param from inclusive lower bound. If negative, there is only an upper bound.
         * @param to   exclusive upper bound.
         * @return the wait.
         */
        public synchronized void wait(int from, int to)
        {
            long start = System.currentTimeMillis();
            long end = start + (from < 0 ? to : from + random.nextInt(to - from));

            while (!finished && System.currentTimeMillis() < end)
            {
                try
                {
                    long ms = end - System.currentTimeMillis();
                    if (ms > 0)
                    {
                        wait(ms);
                    }
                }
                catch (InterruptedException ignore)
                {
                }
            }
        }
    }

    @Before
    public void before() throws Exception
    {
        nodeService = (NodeService) applicationContext.getBean("nodeService");
        contentService = (ContentService) applicationContext.getBean("contentService");
        dictionaryService = (DictionaryService) applicationContext.getBean("dictionaryService");
        mimetypeService = (MimetypeService) applicationContext.getBean("mimetypeService");
        namespacePrefixResolver = (NamespacePrefixResolver) applicationContext.getBean("namespaceService");
        transformerDebug = (TransformerDebug) applicationContext.getBean("transformerDebug");
        renditionService2 = (RenditionService2Impl) applicationContext.getBean("renditionService2");
        transactionService = (TransactionService) applicationContext.getBean("transactionService");
        transformServiceRegistry = (TransformServiceRegistry) applicationContext.getBean("transformServiceRegistry");
        taggingService = (TaggingService) applicationContext.getBean("taggingService");
        transformClient = (TransformClient) applicationContext.getBean("transformClient");

        // Create an empty metadata extractor registry, so that if we add one it will be used
        metadataExtracterRegistry = new MetadataExtracterRegistry();

        contentMetadataExtracter = new ContentMetadataExtracter();
        contentMetadataExtracter.setNodeService(nodeService);
        contentMetadataExtracter.setContentService(contentService);
        contentMetadataExtracter.setDictionaryService(dictionaryService);
        contentMetadataExtracter.setMetadataExtracterRegistry(metadataExtracterRegistry);
        contentMetadataExtracter.setApplicableTypes(new String[]{ContentModel.TYPE_CONTENT.toString()});
        contentMetadataExtracter.setCarryAspectProperties(true);

        contentMetadataEmbedder = new ContentMetadataEmbedder();
        contentMetadataEmbedder.setNodeService(nodeService);
        contentMetadataEmbedder.setContentService(contentService);
        contentMetadataEmbedder.setMetadataExtracterRegistry(metadataExtracterRegistry);
        contentMetadataEmbedder.setApplicableTypes(new String[]{ContentModel.TYPE_CONTENT.toString()});

        transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionHelper.RetryingTransactionCallback<Void>()
        {
            @Override
            public Void execute() throws Throwable
            {
                AuthenticationComponent authenticationComponent = (AuthenticationComponent) applicationContext.getBean("authenticationComponent");
                authenticationComponent.setSystemUserAsCurrentUser();

                // Create the store and get the root node
                testStoreRef = nodeService.createStore(
                        StoreRef.PROTOCOL_WORKSPACE,
                        "Test_" + System.currentTimeMillis());
                rootNodeRef = nodeService.getRootNode(testStoreRef);

                // Create the node used for tests
                nodeRef = nodeService.createNode(
                        rootNodeRef, ContentModel.ASSOC_CHILDREN,
                        QName.createQName("{test}testnode"),
                        ContentModel.TYPE_CONTENT).getChildRef();

                // Authenticate as the system user
                authenticationComponent.setSystemUserAsCurrentUser();

                ContentWriter cw = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                cw.setMimetype(MimetypeMap.MIMETYPE_PDF);
                cw.putContent(AbstractContentTransformerTest.loadQuickTestFile("pdf"));

                origProperties = nodeService.getProperties(nodeRef);
                nodeService.setProperties(nodeRef, origProperties);
                origProperties = new HashMap<>(origProperties); // just in case the contents changed.
                expectedProperties = new HashMap<>(origProperties); // ready to be modified.

                origSize = getSize(nodeRef);

                return null;
            }
        });
    }

    @After
    public void after() throws Exception
    {
        renditionService2.setTransformClient(transformClient);
    }

    private void assertAsyncMetadataExecute(ActionExecuterAbstractBase executor, String mockResult,
                                            Integer changedHashcode, long expectedSize,
                                            Map<QName, Serializable> expectedProperties,
                                            QName... ignoreProperties) throws Exception
    {
        TestAsynchronousExtractor extractor = new TestAsynchronousExtractor(mockResult, changedHashcode);

        executeAction(executor, extractor);
        assertContentSize(nodeRef, origSize, AFTER_CALLING_EXECUTE);
        assertProperties(nodeRef, origProperties, AFTER_CALLING_EXECUTE, ignoreProperties);

        extractor.wait(-1, 10000);
        assertContentSize(nodeRef, expectedSize, AFTER_THE_TRANSFORM);
        assertProperties(nodeRef, expectedProperties, AFTER_THE_TRANSFORM, ignoreProperties);
    }

    private void executeAction(ActionExecuterAbstractBase extractor, TestAsynchronousExtractor asynchronousExtractor)
            throws SystemException, NotSupportedException, HeuristicRollbackException, HeuristicMixedException, RollbackException
    {
        UserTransaction txn = transactionService.getUserTransaction();
        txn.begin();
        ActionImpl action = new ActionImpl(null, ID, SetPropertyValueActionExecuter.NAME, null);
        extractor.execute(action, nodeRef);
        txn.commit();
    }

    void assertContentSize(NodeRef nodeRef, long expectSize, String state)
    {
        long size = getSize(nodeRef);
        if (expectSize == origSize)
        {
            assertEquals("The content should remain unchanged " + state, origSize, size);
        }
        else
        {
            assertEquals("The content should have changed " + state, expectSize, size);
        }
    }

    private long getSize(NodeRef nodeRef)
    {
        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        return reader.getSize();
    }

    private void assertProperties(NodeRef nodeRef, Map<QName, Serializable> expectProperties, String state,
                                  QName[] ignoreProperties)
    {
        properties = nodeService.getProperties(nodeRef);

        // Work out the difference in a human readable form and ignore the 5 system set properties (as they always
        // change) plus any the caller has requested.
        StringJoiner sj = new StringJoiner("\n");
        List<QName> ignoreKeys = new ArrayList<>(asList(PROP_MODIFIED, PROP_MODIFIER, PROP_CONTENT, PROP_CREATED, PROP_CREATOR));
        ignoreKeys.addAll(asList(ignoreProperties));
        for (Map.Entry<QName, Serializable> entry : expectProperties.entrySet())
        {
            QName k = entry.getKey();
            Serializable v = entry.getValue();
            Serializable actual = properties.get(k);
            if (!ignoreKeys.contains(k) && !v.equals(actual))
            {
                sj.add(k + "\n  Expected: " + v + "\n       Was: " + actual);
            }
        }
        for (QName k : properties.keySet())
        {
            Serializable actual = properties.get(k);
            if (!ignoreKeys.contains(k) && !expectProperties.containsKey(k))
            {
                sj.add(k + "\n  Expected: null\n       Was: " + actual);
            }
        }

        if (sj.length() != 0)
        {
            if (expectProperties.equals(origProperties))
            {
                fail("The properties should remain unchanged " + state + "\n" + sj);
            }
            else
            {
                fail("The properties should have changed " + state + "\n" + sj);
            }
        }
    }

    @Test
    public void testExtractHtml() throws Exception
    {
        expectedProperties.put(QName.createQName("cm:author", namespacePrefixResolver), "Nevin Nollop");
        expectedProperties.put(QName.createQName("cm:description", namespacePrefixResolver), "Gym class featuring a brown fox and lazy dog");
        expectedProperties.put(QName.createQName("cm:title", namespacePrefixResolver), "The quick brown fox jumps over the lazy dog");

        assertAsyncMetadataExecute(contentMetadataExtracter, "quick/quick.html_metadata.json",
                UNCHANGED_HASHCODE, origSize, expectedProperties);
    }

    @Test
    public void testExtractNodeDeleted() throws Exception
    {
        assertAsyncMetadataExecute(contentMetadataExtracter, "quick/quick.html_metadata.json",
                SOURCE_HAS_NO_CONTENT, origSize, origProperties);
    }

    @Test
    public void testExtractContentChanged() throws Exception
    {
        assertAsyncMetadataExecute(contentMetadataExtracter, "quick/quick.html_metadata.json",
                1234, origSize, origProperties);
    }

    @Test
    public void testExtractTransformFailure() throws Exception
    {
        assertAsyncMetadataExecute(contentMetadataExtracter, null,
                UNCHANGED_HASHCODE, origSize, origProperties);
    }

    @Test
    public void testExtractTransformCorrupt() throws Exception
    {
        assertAsyncMetadataExecute(contentMetadataExtracter, "quick.html", // not json
                UNCHANGED_HASHCODE, origSize, origProperties);
    }

    @Test
    public void testUnknownNamespaceInResponse() throws Exception
    {
        // "sys:overwritePolicy": "PRAGMATIC" - is used
        // "{http://www.unknown}name": "ignored" - is reported in an ERROR log
        expectedProperties.put(QName.createQName("cm:author", namespacePrefixResolver), "Used");
        assertAsyncMetadataExecute(contentMetadataExtracter, "quick/unknown_namespace_metadata.json",
                UNCHANGED_HASHCODE, origSize, expectedProperties);
    }

    @Test
    public void testExtractMsg() throws Exception // has dates as RFC822
    {
        expectedProperties.put(QName.createQName("cm:addressee", namespacePrefixResolver), "mark.rogers@alfresco.com");
        expectedProperties.put(QName.createQName("cm:description", namespacePrefixResolver), "This is a quick test");
        expectedProperties.put(QName.createQName("cm:addressees", namespacePrefixResolver),
                new ArrayList<>(asList("mark.rogers@alfresco.com", "speedy@quick.com", "mrquick@nowhere.com")));

        expectedProperties.put(QName.createQName("cm:sentdate", namespacePrefixResolver), SIMPLE_DATE_FORMAT.parse("Fri Jan 18 13:44:20 GMT 2013")); // 2013-01-18T13:44:20Z
        expectedProperties.put(QName.createQName("cm:subjectline", namespacePrefixResolver), "This is a quick test");
        expectedProperties.put(QName.createQName("cm:author", namespacePrefixResolver), "Mark Rogers");
        expectedProperties.put(QName.createQName("cm:originator", namespacePrefixResolver), "Mark Rogers");

        assertAsyncMetadataExecute(contentMetadataExtracter, "quick/quick.msg_metadata.json",
                UNCHANGED_HASHCODE, origSize, expectedProperties);

        Serializable sentDate = properties.get(QName.createQName("cm:sentdate", namespacePrefixResolver));
    }

    @Test
    public void testExtractEml() throws Exception // has dates as longs since 1970
    {
        expectedProperties.put(QName.createQName("cm:addressee", namespacePrefixResolver), "Nevin Nollop <nevin.nollop@gmail.com>");
        expectedProperties.put(QName.createQName("cm:description", namespacePrefixResolver), "The quick brown fox jumps over the lazy dog");
        expectedProperties.put(QName.createQName("cm:addressees", namespacePrefixResolver),
                new ArrayList<>(asList("Nevin Nollop <nevinn@alfresco.com>")));
        expectedProperties.put(QName.createQName("imap:dateSent", namespacePrefixResolver), SIMPLE_DATE_FORMAT.parse("Fri Jun 04 13:23:22 BST 2004"));
        expectedProperties.put(QName.createQName("imap:messageTo", namespacePrefixResolver), "Nevin Nollop <nevin.nollop@gmail.com>");
        expectedProperties.put(QName.createQName("imap:messageId", namespacePrefixResolver), "<20040604122322.GV1905@phoenix.home>");
        expectedProperties.put(QName.createQName("cm:title", namespacePrefixResolver), "The quick brown fox jumps over the lazy dog");
        expectedProperties.put(QName.createQName("imap:messageSubject", namespacePrefixResolver), "The quick brown fox jumps over the lazy dog");
        expectedProperties.put(QName.createQName("imap:messageCc", namespacePrefixResolver), "Nevin Nollop <nevinn@alfresco.com>");
        expectedProperties.put(QName.createQName("cm:sentdate", namespacePrefixResolver), SIMPLE_DATE_FORMAT.parse("Fri Jun 04 13:23:22 BST 2004"));
        expectedProperties.put(QName.createQName("cm:subjectline", namespacePrefixResolver), "The quick brown fox jumps over the lazy dog");
        expectedProperties.put(QName.createQName("imap:messageFrom", namespacePrefixResolver), "Nevin Nollop <nevin.nollop@alfresco.com>");
        expectedProperties.put(QName.createQName("cm:originator", namespacePrefixResolver), "Nevin Nollop <nevin.nollop@alfresco.com>");

        // Note: As the metadata is for eml, an aspect gets added resulting in a second extract because of
        // ImapContentPolicy.onAddAspect. I cannot see a good way to avoid this.
        assertAsyncMetadataExecute(contentMetadataExtracter, "quick/quick.eml_metadata.json",
                UNCHANGED_HASHCODE, origSize, expectedProperties,
                // cm:author is not in the quick.eml_metadata.json but is being added by the second extract which thinks
                // the source mimetype is MimetypeMap.MIMETYPE_PDF, because that is what the before() method sets the
                // content to. As a result the PdfBox metadata extractor is called, which extracts cm:author. Given that
                // we don't know when this will take place, we simply ignore this property. We could fix this up, but it
                // does not add anything to the test.
                QName.createQName("cm:author", namespacePrefixResolver));
    }


    @Test
    public void testEmbed() throws Exception
    {
        assertAsyncMetadataExecute(contentMetadataEmbedder, "quick/quick.html", // just replace the pdf with html!
                UNCHANGED_HASHCODE, 428, expectedProperties);
    }
    @Test
    public void testEmbedNodeDeleted() throws Exception
    {
        assertAsyncMetadataExecute(contentMetadataEmbedder, "quick/quick.html",
                SOURCE_HAS_NO_CONTENT, origSize, origProperties);
    }

    @Test
    public void testEmbedContentChanged() throws Exception
    {
        assertAsyncMetadataExecute(contentMetadataEmbedder, "quick/quick.html",
                1234, origSize, origProperties);
    }

    @Test
    public void testEmbedTransformFailure() throws Exception
    {
        assertAsyncMetadataExecute(contentMetadataEmbedder, null,
                UNCHANGED_HASHCODE, origSize, origProperties);
    }

    // TODO Write tests for: overwritePolicy, enableStringTagging and carryAspectProperties.
    //      Values are set in AsynchronousExtractor.setMetadata(...) but make use of original code within
    //      MetadataExtracter and AbstractMappingMetadataExtracter.
    //      As the tests for exiting extractors are to be removed in ACS 7.0, it is possible that they were being used
    //      to test these values.
}