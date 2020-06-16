/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2020 Alfresco Software Limited
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
package org.alfresco.repo.content.metadata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ContentMetadataExtracter;
import org.alfresco.repo.content.transform.TransformerDebug;
import org.alfresco.repo.rendition2.RenditionService2;
import org.alfresco.repo.rendition2.TransformDefinition;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.tagging.TaggingService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.transform.client.registry.TransformServiceRegistry;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.dao.ConcurrencyFailureException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;

import static org.alfresco.repo.rendition2.RenditionDefinition2.TIMEOUT;
import static org.alfresco.repo.rendition2.TransformDefinition.getTransformName;

/**
 * Requests an extract of metadata via a remote async transform using
 * {@link RenditionService2#transform(NodeRef, TransformDefinition)}. The properties that will extracted are defined
 * by the transform. This allows out of process metadata extracts to be defined without the need to apply an AMP.
 * The actual transform is a request to go from the source mimetype to {@code "alfresco-metadata-extract"}. The
 * resulting transform is a Map in json of properties and values to be set on the source node.
 * <p>
 * As with other sub-classes of {@link AbstractMappingMetadataExtracter} it also supports embedding of metadata in
 * a source node. In this case the remote async transform states that it supports a transform from a source mimetype
 * to  {@code "alfresco-metadata-embed"}. The resulting transform is a replacement for the content of the node.
 *
 * @author adavis
 */
public class AsynchronousExtractor extends AbstractMappingMetadataExtracter
{
    private static final String EXTRACT = "extract";
    private static final String EMBED = "embed";
    private static final String MIMETYPE_METADATA_EXTRACT = "alfresco-metadata-extract";
    private static final String MIMETYPE_METADATA_EMBED = "alfresco-metadata-embed";
    private static final String METADATA = "metadata";
    private static final Map<String, Serializable> EMPTY_METADATA = Collections.emptyMap();

    private final ObjectMapper jsonObjectMapper = new ObjectMapper();

    private NodeService nodeService;
    private NamespacePrefixResolver namespacePrefixResolver;
    private TransformerDebug transformerDebug;
    private RenditionService2 renditionService2;
    private ContentService contentService;
    private TransactionService transactionService;
    private TransformServiceRegistry transformServiceRegistry;
    private TaggingService taggingService;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public void setTransformerDebug(TransformerDebug transformerDebug)
    {
        this.transformerDebug = transformerDebug;
    }

    public void setRenditionService2(RenditionService2 renditionService2)
    {
        this.renditionService2 = renditionService2;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setTransformServiceRegistry(TransformServiceRegistry transformServiceRegistry)
    {
        this.transformServiceRegistry = transformServiceRegistry;
    }

    public void setTaggingService(TaggingService taggingService)
    {
        this.taggingService = taggingService;
    }

    @Override
    protected Map<String, Set<QName>> getDefaultMapping()
    {
        return Collections.emptyMap(); // Mappings are done by the transform, but a non null value must be returned.
    }

    public boolean isSupported(String sourceMimetype, long sourceSizeInBytes)
    {
        return isEnabled(sourceMimetype) && isSupported(sourceMimetype, sourceSizeInBytes, MIMETYPE_METADATA_EXTRACT);
    }

    public boolean isEmbedderSupported(String sourceMimetype, long sourceSizeInBytes)
    {
        return isSupported(sourceMimetype, sourceSizeInBytes, MIMETYPE_METADATA_EMBED);
    }

    private boolean isSupported(String sourceMimetype, long sourceSizeInBytes, String targetMimetype)
    {
        return transformServiceRegistry.isSupported(sourceMimetype, sourceSizeInBytes, targetMimetype, Collections.emptyMap(), targetMimetype);
    }

    public static boolean isMetadataExtractMimetype(String targetMimetype)
    {
        return MIMETYPE_METADATA_EXTRACT.equals(targetMimetype);
    }

    public static boolean isMetadataEmbedMimetype(String targetMimetype)
    {
        return MIMETYPE_METADATA_EMBED.equals(targetMimetype);
    }

    /**
     * Returns a file extension used as the target in a transform. The normal extension is changed if the
     * {@code targetMimetype} is an extraction or embedding type.
     *
     * @param targetMimetype the target mimetype
     * @param sourceExtension normal source extension
     * @param targetExtension current target extension (normally {@code "bin" for embedding and extraction})
     * @return the extension to be used.
     */
    public static String getExtension(String targetMimetype, String sourceExtension, String targetExtension)
    {
        return isMetadataExtractMimetype(targetMimetype)
                ? "json"
                : isMetadataEmbedMimetype(targetMimetype)
                ? sourceExtension
                : targetExtension;
    }

    /**
     * Returns a rendition name used in {@link TransformerDebug}. The normal name is changed if it is a metadata
     * extract or embed. The name in this case is actually the {@code "alfresco-metadata-extract/"}
     * {@code "alfresco-metadata-embed/"} followed by the source mimetype.
     *
     * @param renditionName the normal name, or a special one based on the source mimetype and a prefixed.
     * @return the renditionName to be used.
     */
    public static String getRenditionName(String renditionName)
    {
        String transformName = getTransformName(renditionName);
        return    transformName != null && transformName.startsWith(MIMETYPE_METADATA_EXTRACT)
                ? "metadataExtract"
                : transformName != null && transformName.startsWith(MIMETYPE_METADATA_EMBED)
                ? "metadataEmbed"
                : renditionName;
    }

    @Override
    protected void checkIsSupported(ContentReader reader)
    {
        // Just return, as we have already checked when this extractor was selected.
    }

    @Override
    protected void checkIsEmbedSupported(ContentWriter writer)
    {
        // Just return, as we have already checked when this embedder was selected.
    }

    @Override
    // Not called. Overloaded method with the NodeRef is called.
    protected Map<String, Serializable> extractRaw(ContentReader reader)
    {
        return null;
    }

    @Override
    protected Map<String, Serializable> extractRawInThread(NodeRef nodeRef, ContentReader reader, MetadataExtracterLimits limits)
            throws Throwable
    {
        long timeoutMs = limits.getTimeoutMs();
        Map<String, String> options = Collections.singletonMap(TIMEOUT, Long.toString(timeoutMs));
        transformInBackground(nodeRef, reader, MIMETYPE_METADATA_EXTRACT, EXTRACT, options);
        return EMPTY_METADATA;
    }

    @Override
    protected void embedInternal(NodeRef nodeRef, Map<String, Serializable> metadata, ContentReader reader, ContentWriter writer)
    {
        String metadataAsJson = metadataToString(metadata);
        Map<String, String> options = Collections.singletonMap(METADATA, metadataAsJson);
        transformInBackground(nodeRef, reader, MIMETYPE_METADATA_EMBED, EMBED, options);
    }

    private void transformInBackground(NodeRef nodeRef, ContentReader reader, String targetMimetype,
                                       String embedOrExtract, Map<String, String> options)
    {
        ExecutorService executorService = getExecutorService();
        executorService.execute(() ->
        {
            try
            {
                transform(nodeRef, reader, targetMimetype, embedOrExtract, options);
            }
            finally
            {
                extractRawThreadFinished();
            }
        });
    }

    private void transform(NodeRef nodeRef, ContentReader reader, String targetMimetype,
                           String embedOrExtract, Map<String, String> options)
    {
        String sourceMimetype = reader.getMimetype();

        // This needs to be specific to each source mimetype and the extract or embed as the name
        // is used to cache the transform name that will be used.
        String transformName = targetMimetype + '/' + sourceMimetype;

        TransformDefinition transformDefinition = new TransformDefinition(transformName, targetMimetype,
                options, null, null, null);

        if (logger.isTraceEnabled())
        {
            StringJoiner sj = new StringJoiner("\n");
            sj.add("Request " + embedOrExtract + " transform on " + nodeRef);
            options.forEach((k,v)->sj.add("  "+k+"="+v));
            logger.trace(sj);
        }

        AuthenticationUtil.runAs(
                (AuthenticationUtil.RunAsWork<Void>) () ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                {
                    try
                    {
                        renditionService2.transform(nodeRef, transformDefinition);
                    }
                    catch (IllegalArgumentException e)
                    {
                        if (e.getMessage().endsWith("The supplied sourceNodeRef "+nodeRef+" does not exist."))
                        {
                            throw new ConcurrencyFailureException(
                                    "The original transaction may not have finished. " + e.getMessage());
                        }
                    }
                    return null;
                }), AuthenticationUtil.getSystemUserName());
    }

    public void setMetadata(NodeRef nodeRef, InputStream transformInputStream)
    {
        if (logger.isTraceEnabled())
        {
            logger.trace("Update metadata on " + nodeRef);
        }

        Map<String, Serializable> metadata = readMetadata(transformInputStream);
        if (metadata == null)
        {
            return; // Error state.
        }

        // Remove well know entries from the map that drive how the real metadata is applied.
        OverwritePolicy overwritePolicy = removeOverwritePolicy(metadata, "sys:overwritePolicy", OverwritePolicy.PRAGMATIC);
        Boolean enableStringTagging = removeBoolean(metadata, "sys:enableStringTagging", false);
        Boolean carryAspectProperties = removeBoolean(metadata, "sys:carryAspectProperties", true);
        List<String> stringTaggingSeparators = removeTaggingSeparators(metadata, "sys:stringTaggingSeparators",
                ContentMetadataExtracter.DEFAULT_STRING_TAGGING_SEPARATORS);
        if (overwritePolicy == null ||
            enableStringTagging == null ||
            carryAspectProperties == null ||
            stringTaggingSeparators == null)
        {
            return; // Error state.
        }

        AuthenticationUtil.runAsSystem((AuthenticationUtil.RunAsWork<Void>) () ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                {
                    // Based on: AbstractMappingMetadataExtracter.extract
                    Map<QName, Serializable> nodeProperties = nodeService.getProperties(nodeRef);
                    // Convert to system properties (standalone)
                    Map<QName, Serializable> systemProperties = convertKeysToQNames(metadata);
                    // Convert the properties according to the dictionary types
                    systemProperties = convertSystemPropertyValues(systemProperties);
                    // There is no last filter in the AsynchronousExtractor.
                    // Now use the proper overwrite policy
                    Map<QName, Serializable> changedProperties = overwritePolicy.applyProperties(systemProperties, nodeProperties);

                    // Based on: ContentMetadataExtracter.executeImpl
                    // If none of the properties where changed, then there is nothing more to do
                    if (changedProperties.size() == 0)
                    {
                        return null;
                    }
                    boolean transformerDebugEnabled = transformerDebug.isEnabled();
                    boolean debugEnabled = logger.isDebugEnabled();
                    if (transformerDebugEnabled || debugEnabled)
                    {
                        for (Map.Entry<QName, Serializable> entry : changedProperties.entrySet())
                        {
                            QName qname = entry.getKey();
                            Serializable value = entry.getValue();
                            String prefixString = qname.toPrefixString(namespacePrefixResolver);
                            String debugMessage = prefixString + "=" + (value == null ? "" : value);
                            if (transformerDebugEnabled)
                            {
                                transformerDebug.debugUsingPreviousReference("  "+debugMessage);
                            }
                            if (debugEnabled)
                            {
                                logger.debug(debugMessage);
                            }
                        }
                    }
                    ContentMetadataExtracter.addExtractedMetadataToNode(nodeRef, nodeProperties, changedProperties,
                            nodeService, dictionaryService, taggingService,
                            enableStringTagging, carryAspectProperties, stringTaggingSeparators);

                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Extraction of Metadata from " + nodeRef + " complete " + changedProperties);
                    }

                    return null;
                }, false, true));
    }

    private Map<String, Serializable> readMetadata(InputStream transformInputStream)
    {
        try
        {
            TypeReference<HashMap<String, Serializable>> typeRef = new TypeReference<HashMap<String, Serializable>>() {};
            return jsonObjectMapper.readValue(transformInputStream, typeRef);
        }
        catch (IOException e)
        {
            logger.error("Failed to read metadata from transform result", e);
            return null;
        }
    }

    private String metadataToString(Map<String, Serializable> metadata)
    {
        Map<String, String> metadataAsStrings = AbstractMappingMetadataExtracter.convertMetadataToStrings(metadata);
        try
        {
            return jsonObjectMapper.writeValueAsString(metadataAsStrings);
        }
        catch (JsonProcessingException e)
        {
            logger.error("Failed to save metadata as Json", e);
            return null;
        }
    }

    private OverwritePolicy removeOverwritePolicy(Map<String, Serializable> map, String key, OverwritePolicy defaultValue)
    {
        Serializable value = map.remove(key);
        if (value == null)
        {
            return defaultValue;
        }
        try
        {
            return OverwritePolicy.valueOf((String)value);
        }
        catch (IllegalArgumentException|ClassCastException e)
        {
            logger.error(key + "=" + value + " is invalid");
            return null;
        }
    }

    private Boolean removeBoolean(Map<String, Serializable> map, Serializable key, boolean defaultValue)
    {
        @SuppressWarnings("SuspiciousMethodCalls") Serializable value = map.remove(key);
        if (value != null &&
            (!(value instanceof String) ||
             (!(Boolean.FALSE.toString().equals(value) || Boolean.TRUE.toString().equals(value)))))
        {
            logger.error(key + "=" + value + " is invalid. Must be " + Boolean.TRUE + " or " + Boolean.FALSE);
            return null; // no flexibility of parseBoolean(...). It is just invalid
        }
        return value == null ? defaultValue : Boolean.parseBoolean((String)value);
    }

    private List<String> removeTaggingSeparators(Map<String, Serializable> map, String key, List<String> defaultValue)
    {
        Serializable value = map.remove(key);
        if (value == null)
        {
            return defaultValue;
        }
        if (!(value instanceof String))
        {
            logger.error(key + "=" + value + " is invalid.");
            return null;
        }

        List<String> list = new ArrayList<>();
        try (CSVParser parser = CSVParser.parse((String)value, CSVFormat.RFC4180))
        {
            Iterator<CSVRecord> iterator = parser.iterator();
            CSVRecord record = iterator.next();
            if (iterator.hasNext())
            {
                logger.error(key + "=" + value + " is invalid. Should only have one record");
                return null;
            }
            record.forEach(list::add);
        }
        catch (IOException|NoSuchElementException e)
        {
            logger.error(key + "=" + value + " is invalid. Must be a CSV using CSVFormat.RFC4180");
            return null;
        }
        return list;
    }

    private Map<QName, Serializable> convertKeysToQNames(Map<String, Serializable> documentMetadata)
    {
        Map<QName, Serializable> properties = new HashMap<>();
        for (Map.Entry<String, Serializable> entry : documentMetadata.entrySet())
        {
            String key = entry.getKey();
            Serializable value = entry.getValue();
            try
            {
                QName qName = QName.createQName(key);
                try
                {
                    qName.toPrefixString(namespacePrefixResolver);
                    properties.put(qName, value);
                }
                catch (NamespaceException e)
                {
                    logger.error("Error unregistered namespace in " + qName);
                }
            }
            catch (NamespaceException e)
            {
                logger.error("Error creating qName from "+key);
            }
        }
        return properties;
    }

    public void setEmbeddedMetadata(NodeRef nodeRef, InputStream transformInputStream)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Update of content to include metadata on " + nodeRef);
        }
        AuthenticationUtil.runAsSystem(() ->
                transactionService.getRetryingTransactionHelper().doInTransaction(() ->
                {
                    try
                    {
                        // Set or replace content
                        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                        String mimetype = reader.getMimetype();
                        String encoding = reader.getEncoding();
                        ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                        writer.setMimetype(mimetype);
                        writer.setEncoding(encoding);
                        writer.putContent(transformInputStream);

                        if (logger.isTraceEnabled())
                        {
                            logger.trace("Embedded Metadata on " + nodeRef + " complete");
                        }
                    }
                    catch (Exception e)
                    {
                        logger.error("Failed to copy embedded metadata transform InputStream into " + nodeRef);
                        throw e;
                    }

                    return null;
                }, false, true));
    }
}
