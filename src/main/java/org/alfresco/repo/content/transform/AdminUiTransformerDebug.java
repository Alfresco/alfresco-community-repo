/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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
package org.alfresco.repo.content.transform;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.rendition2.SynchronousTransformClient;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.transform.client.registry.SupportedTransform;
import org.alfresco.transform.client.registry.TransformServiceRegistry;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.TempFileProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Provides methods to support the Admin UI Test Transform actions.
 *
 * @author adavis
 */
public class AdminUiTransformerDebug extends TransformerDebug implements ApplicationContextAware
{
    protected TransformServiceRegistry remoteTransformServiceRegistry;
    protected LocalTransformServiceRegistry localTransformServiceRegistryImpl;
    private ApplicationContext applicationContext;
    private ContentService contentService;
    private SynchronousTransformClient synchronousTransformClient;
    private Repository repositoryHelper;
    private TransactionService transactionService;

    public void setLocalTransformServiceRegistryImpl(LocalTransformServiceRegistry localTransformServiceRegistryImpl)
    {
        this.localTransformServiceRegistryImpl = localTransformServiceRegistryImpl;
    }

    public void setRemoteTransformServiceRegistry(TransformServiceRegistry remoteTransformServiceRegistry)
    {
        this.remoteTransformServiceRegistry = remoteTransformServiceRegistry;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    private ContentService getContentService()
    {
        if (contentService == null)
        {
            contentService = (ContentService) applicationContext.getBean("contentService");
        }
        return contentService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    private SynchronousTransformClient getSynchronousTransformClient()
    {
        if (synchronousTransformClient == null)
        {
            synchronousTransformClient = (SynchronousTransformClient) applicationContext.getBean("legacySynchronousTransformClient");
        }
        return synchronousTransformClient;
    }

    public void setSynchronousTransformClient(SynchronousTransformClient transformClient)
    {
        this.synchronousTransformClient = transformClient;
    }

    public Repository getRepositoryHelper()
    {
        if (repositoryHelper == null)
        {
            repositoryHelper = (Repository) applicationContext.getBean("repositoryHelper");
        }
        return repositoryHelper;
    }

    public void setRepositoryHelper(Repository repositoryHelper)
    {
        this.repositoryHelper = repositoryHelper;
    }

    public TransactionService getTransactionService()
    {
        if (transactionService == null)
        {
            transactionService = (TransactionService) applicationContext.getBean("transactionService");
        }
        return transactionService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
        PropertyCheck.mandatory(this, "localTransformServiceRegistryImpl", localTransformServiceRegistryImpl);
        PropertyCheck.mandatory(this, "remoteTransformServiceRegistry", remoteTransformServiceRegistry);
    }

    /**
     * Returns a String and /or debug that provides a list of supported transformations
     * sorted by source and target mimetype extension. Used in the Test Transforms Admin UI.
     * @param sourceExtension restricts the list to one source extension. Unrestricted if null.
     * @param targetExtension restricts the list to one target extension. Unrestricted if null.
     * @param toString indicates that a String value should be returned in addition to any debug.
     * @param format42 ignored
     * @param onlyNonDeterministic ignored
     * @param renditionName ignored
     */
    public String transformationsByExtension(String sourceExtension, String targetExtension, boolean toString,
                                             boolean format42, boolean onlyNonDeterministic, String renditionName)
    {
        // Do not generate this type of debug if already generating other debug to a StringBuilder
        // (for example a test transform).
        if (getStringBuilder() != null)
        {
            return null;
        }

        Collection<String> sourceMimetypes = format42 || sourceExtension != null
                ? getSourceMimetypes(sourceExtension)
                : mimetypeService.getMimetypes();
        Collection<String> targetMimetypes = format42 || targetExtension != null
                ? getTargetMimetypes(sourceExtension, targetExtension, sourceMimetypes)
                : mimetypeService.getMimetypes();

        StringBuilder sb = null;
        try
        {
            if (toString)
            {
                sb = new StringBuilder();
                setStringBuilder(sb);
            }
            pushMisc();
            for (String sourceMimetype: sourceMimetypes)
            {
                for (String targetMimetype: targetMimetypes)
                {
                    // Log the transformers
                    boolean supportedByTransformService = remoteTransformServiceRegistry == null ||
                                    remoteTransformServiceRegistry instanceof DummyTransformServiceRegistry
                            ? false
                            : remoteTransformServiceRegistry.isSupported(sourceMimetype,
                            -1, targetMimetype, Collections.emptyMap(), null);
                    List<SupportedTransform> localTransformers = localTransformServiceRegistryImpl == null
                            ? Collections.emptyList()
                            : localTransformServiceRegistryImpl.findTransformers(sourceMimetype,
                                    targetMimetype, Collections.emptyMap(), null);
                    if (!localTransformers.isEmpty() || supportedByTransformService)
                    {
                        try
                        {
                            pushMisc();
                            int transformerCount = 0;
                            if (supportedByTransformService)
                            {
                                long maxSourceSizeKBytes = remoteTransformServiceRegistry.findMaxSize(sourceMimetype,
                                        targetMimetype, Collections.emptyMap(), null);
                                activeTransformer(sourceMimetype, targetMimetype, transformerCount, "     ",
                                        TRANSFORM_SERVICE_NAME, maxSourceSizeKBytes, transformerCount++ == 0);
                            }
                            for (SupportedTransform localTransformer : localTransformers)
                            {
                                long maxSourceSizeKBytes = localTransformer.getMaxSourceSizeBytes();
                                String transformName = "Local:" + localTransformer.getName();
                                String transformerPriority = "[" + localTransformer.getPriority() + ']';
                                transformerPriority = spaces(5-transformerPriority.length())+transformerPriority;
                                activeTransformer(sourceMimetype, targetMimetype, transformerCount, transformerPriority,
                                        transformName, maxSourceSizeKBytes, transformerCount++ == 0);
                            }
                        }
                        finally
                        {
                            popMisc();
                        }
                    }
                }
            }
        }
        finally
        {
            popMisc();
            setStringBuilder(null);
        }
        stripFinishedLine(sb);
        return stripLeadingNumber(sb);
    }

    protected void activeTransformer(String sourceMimetype, String targetMimetype, int transformerCount,
                                     String priority, String transformName, long maxSourceSizeKBytes,
                                     boolean firstTransformer)
    {
        String mimetypes = firstTransformer
                ? getMimetypeExt(sourceMimetype)+getMimetypeExt(targetMimetype)
                : spaces(10);
        char c = (char)('a'+transformerCount);
        log(mimetypes+
                "  "+c+") " + priority + ' '+transformName+' '+
                fileSize((maxSourceSizeKBytes > 0) ? maxSourceSizeKBytes*1024 : maxSourceSizeKBytes)+
                (maxSourceSizeKBytes == 0 ? " disabled" : ""));
    }

    /**
     * Removes the final "Finished in..." message from a StringBuilder
     * @param sb which contains the debug message.
     */
    void stripFinishedLine(StringBuilder sb)
    {
        if (sb != null)
        {
            int i = sb.lastIndexOf(FINISHED_IN);
            if (i != -1)
            {
                sb.setLength(i);
                i = sb.lastIndexOf("\n", i);
                sb.setLength(i != -1 ? i : 0);
            }
        }
    }

    /**
     * Strips the leading number in a reference
     * @param sb which contains the debug message.
     */
    String stripLeadingNumber(StringBuilder sb)
    {
        return sb == null
                ? null
                : Pattern.compile("^\\d+\\.", Pattern.MULTILINE).matcher(sb).replaceAll("");
    }

    /**
     * Returns a collection of mimetypes ordered by extension, but unlike the version in MimetypeService
     * throws an exception if the sourceExtension is supplied but does not match a mimetype.
     * @param sourceExtension to restrict the collection to one entry
     * @throws IllegalArgumentException if there is no match. The message indicates this.
     */
    public Collection<String> getSourceMimetypes(String sourceExtension)
    {
        Collection<String> sourceMimetypes = mimetypeService.getMimetypes(sourceExtension);
        if (sourceMimetypes.isEmpty())
        {
            throw new IllegalArgumentException("Unknown source extension "+sourceExtension);
        }
        return sourceMimetypes;
    }

    /**
     * Identical to getSourceMimetypes for the target, but avoids doing the look up if the sourceExtension
     * is the same as the tragetExtension, so will have the same result.
     * @param sourceExtension used to restrict the sourceMimetypes
     * @param targetExtension to restrict the collection to one entry
     * @param sourceMimetypes that match the sourceExtension
     * @throws IllegalArgumentException if there is no match. The message indicates this.
     */
    public Collection<String> getTargetMimetypes(String sourceExtension, String targetExtension,
                                                 Collection<String> sourceMimetypes)
    {
        Collection<String> targetMimetypes =
                (targetExtension == null && sourceExtension == null) ||
                        (targetExtension != null && targetExtension.equals(sourceExtension))
                        ? sourceMimetypes
                        : mimetypeService.getMimetypes(targetExtension);
        if (targetMimetypes.isEmpty())
        {
            throw new IllegalArgumentException("Unknown target extension "+targetExtension);
        }
        return targetMimetypes;
    }


    public String testTransform(String sourceExtension, String targetExtension, String renditionName)
    {
        return new TestTransform().run(sourceExtension, targetExtension, renditionName);
    }

    public String[] getTestFileExtensionsAndMimetypes()
    {
        List<String> sourceExtensions = new ArrayList<String>();
        Collection<String> sourceMimetypes = mimetypeService.getMimetypes(null);
        for (String sourceMimetype: sourceMimetypes)
        {
            String sourceExtension = mimetypeService.getExtension(sourceMimetype);
            if (loadQuickTestFile(sourceExtension) != null)
            {
                sourceExtensions.add(sourceExtension+" - "+sourceMimetype);
            }
        }

        return sourceExtensions.toArray(new String[sourceExtensions.size()]);
    }

    /**
     * Load one of the "The quick brown fox" files from the classpath.
     * @param extension required, eg <b>txt</b> for the file quick.txt
     * @return Returns a test resource loaded from the classpath or <tt>null</tt> if
     *      no resource could be found.
     */
    private URL loadQuickTestFile(String extension)
    {
        final URL result;

        URL url = this.getClass().getClassLoader().getResource("quick/quick." + extension);
        // Note that this URL may point to a file on the filesystem or to an entry in a jar file.
        // The handling should be the same either way.
        return url == null ? null : url;
    }

    @Deprecated
    private class TestTransform
    {
        protected LinkedList<NodeRef> nodesToDeleteAfterTest = new LinkedList<NodeRef>();

        String run(String sourceExtension, String targetExtension, String renditionName)
        {
            RetryingTransactionHelper.RetryingTransactionCallback<String> makeNodeCallback = new RetryingTransactionHelper.RetryingTransactionCallback<String>()
            {
                public String execute() throws Throwable
                {
                    return runWithinTransaction(sourceExtension, targetExtension);
                }
            };
            return getTransactionService().getRetryingTransactionHelper().doInTransaction(makeNodeCallback, false, true);
        }

        private String runWithinTransaction(String sourceExtension, String targetExtension)
        {
            String targetMimetype = getMimetype(targetExtension, false);
            String sourceMimetype = getMimetype(sourceExtension, true);
            File tempFile = TempFileProvider.createTempFile(
                    "TestTransform_" + sourceExtension + "_", "." + targetExtension);
            ContentWriter writer = new FileContentWriter(tempFile);
            writer.setMimetype(targetMimetype);

            NodeRef sourceNodeRef = null;
            StringBuilder sb = new StringBuilder();
            try
            {
                setStringBuilder(sb);
                sourceNodeRef = createSourceNode(sourceExtension, sourceMimetype);
                ContentReader reader = contentService.getReader(sourceNodeRef, ContentModel.PROP_CONTENT);
                SynchronousTransformClient synchronousTransformClient = getSynchronousTransformClient();
                Map<String, String> actualOptions = Collections.emptyMap();
                synchronousTransformClient.transform(reader, writer, actualOptions, null, sourceNodeRef);
            }
            catch (Exception e)
            {
                logger.debug("Unexpected test transform error", e);
            }
            finally
            {
                setStringBuilder(null);
                deleteSourceNode(sourceNodeRef);
            }
            return sb.toString();
        }

        private String getMimetype(String extension, boolean isSource)
        {
            String mimetype = null;
            if (extension != null)
            {
                Iterator<String> iterator = mimetypeService.getMimetypes(extension).iterator();
                if (iterator.hasNext())
                {
                    mimetype = iterator.next();
                }
            }
            if (mimetype == null)
            {
                throw new IllegalArgumentException("Unknown "+(isSource ? "source" : "target")+" extension: "+extension);
            }
            return mimetype;
        }

        public NodeRef createSourceNode(String extension, String sourceMimetype)
        {
            // Create a content node which will serve as test data for our transformations.
            RetryingTransactionHelper.RetryingTransactionCallback<NodeRef> makeNodeCallback = new RetryingTransactionHelper.RetryingTransactionCallback<NodeRef>()
            {
                public NodeRef execute() throws Throwable
                {
                    // Create a source node loaded with a quick file.
                    URL url = loadQuickTestFile(extension);
                    URI uri = url.toURI();
                    File sourceFile = new File(uri);

                    final NodeRef companyHome = getRepositoryHelper().getCompanyHome();

                    Map<QName, Serializable> props = new HashMap<QName, Serializable>();
                    String localName = "TestTransform." + extension;
                    props.put(ContentModel.PROP_NAME, localName);
                    NodeRef node = nodeService.createNode(
                            companyHome,
                            ContentModel.ASSOC_CONTAINS,
                            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, localName),
                            ContentModel.TYPE_CONTENT,
                            props).getChildRef();

                    ContentWriter writer = getContentService().getWriter(node, ContentModel.PROP_CONTENT, true);
                    writer.setMimetype(sourceMimetype);
                    writer.setEncoding("UTF-8");
                    writer.putContent(sourceFile);

                    return node;
                }
            };
            NodeRef contentNodeRef = getTransactionService().getRetryingTransactionHelper().doInTransaction(makeNodeCallback);
            this.nodesToDeleteAfterTest.add(contentNodeRef);
            return contentNodeRef;
        }

        public void deleteSourceNode(NodeRef sourceNodeRef)
        {
            if (sourceNodeRef != null)
            {
                getTransactionService().getRetryingTransactionHelper().doInTransaction(
                        (RetryingTransactionHelper.RetryingTransactionCallback<Void>) () ->
                        {
                            if (nodeService.exists(sourceNodeRef))
                            {
                                nodeService.deleteNode(sourceNodeRef);
                            }
                            return null;
                        });
            }
        }
    }
}
