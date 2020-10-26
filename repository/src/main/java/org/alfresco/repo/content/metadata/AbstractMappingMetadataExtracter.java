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
package org.alfresco.repo.content.metadata;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.alfresco.api.AlfrescoPublicApi;     
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.StreamAwareContentReaderProxy;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MalformedNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

/**
 * Support class for metadata extracters that support dynamic and config-driven
 * mapping between extracted values and model properties.  Extraction is broken
 * up into two phases:
 * <ul>
 *   <li>Extract ALL available metadata from the document.</li>
 *   <li>Translate the metadata into system properties.</li>
 * </ul>
 * <p>
 * Migrating an existing extracter to use this class is straightforward:
 * <ul>
 *   <li>
 *   Construct the extracter providing a default set of supported mimetypes to this
 *   implementation.  This can be overwritten with configurations.
 *   </li>
 *   <li>
 *   Implement the {@link #extract} method.  This now returns a raw map of extracted
 *   values keyed by document-specific property names.  The <b>trimPut</b> method has
 *   been replaced with an equivalent {@link #putRawValue(String, Serializable, Map)}.
 *   </li>
 *   <li>
 *   Provide the default mapping of the document-specific properties to system-specific
 *   properties as describe by the {@link #getDefaultMapping()} method.  The simplest
 *   is to provide the default mapping in a correlated <i>.properties</i> file.
 *   </li>
 *   <li>
 *   Document, in the class-level javadoc, all the available properties that are extracted
 *   along with their approximate meanings.  Add to this, the default mappings.
 *   </li>
 * </ul>
 * 
 * @see #getDefaultMapping()
 * @see #extractRaw(ContentReader)
 * @see #setMapping(Map)
 * 
 * @since 2.1
 * 
 * @author Jesper Steen Møller
 * @author Derek Hulley
 */
@AlfrescoPublicApi
abstract public class AbstractMappingMetadataExtracter implements MetadataExtracter, MetadataEmbedder, BeanNameAware, ApplicationContextAware
{
    public static final String NAMESPACE_PROPERTY_PREFIX = "namespace.prefix.";
    private static final String ERR_TYPE_CONVERSION = "metadata.extraction.err.type_conversion";
    private static final String PROP_DEFAULT_TIMEOUT = "content.metadataExtracter.default.timeoutMs";
    public static final String PROPERTY_PREFIX_METADATA = "metadata.";
    public static final String PROPERTY_COMPONENT_EXTRACT = ".extract.";
    public static final String PROPERTY_COMPONENT_EMBED = ".embed.";
    public static final int MEGABYTE_SIZE = 1048576;
    
    protected static Log logger = LogFactory.getLog(AbstractMappingMetadataExtracter.class);
    
    private MetadataExtracterRegistry registry;
    private MimetypeService mimetypeService;
    private DictionaryService dictionaryService;
    private boolean initialized;
    
    private Set<String> supportedMimetypes;
    private Set<String> supportedEmbedMimetypes;
    private OverwritePolicy overwritePolicy;
    private boolean failOnTypeConversion;
    private Set<DateTimeFormatter> supportedDateFormatters;
    private Map<String, Set<QName>> mapping;
    private Map<QName, Set<String>> embedMapping;
    private boolean inheritDefaultMapping;
    private boolean inheritDefaultEmbedMapping;
    private boolean enableStringTagging;
    private String beanName;
    private ApplicationContext applicationContext;
    private Properties properties;
    private Map<String, MetadataExtracterLimits> mimetypeLimits;
    private ExecutorService executorService;
    
    private static final AtomicInteger CONCURRENT_EXTRACTIONS_COUNT = new AtomicInteger(0);

    /**
     * Default constructor.  If this is called, then {@link #isSupported(String)} should
     * be implemented.  This is useful when the list of supported mimetypes is not known
     * when the instance is constructed.  Alternatively, once the set becomes known, call
     * {@link #setSupportedMimetypes(Collection)}.
     *
     * @see #isSupported(String)
     * @see #setSupportedMimetypes(Collection)
     */
    protected AbstractMappingMetadataExtracter()
    {
        this(Collections.<String>emptySet());
    }

    /**
     * Constructor that can be used when the list of supported mimetypes is known up front.
     * 
     * @param supportedMimetypes    the set of mimetypes supported by default
     */
    protected AbstractMappingMetadataExtracter(Set<String> supportedMimetypes)
    {
        this.supportedMimetypes = supportedMimetypes;
        // Set defaults
        overwritePolicy = OverwritePolicy.PRAGMATIC;
        failOnTypeConversion = true;
        mapping = null;                     // The default will be fetched
        embedMapping = null;
        inheritDefaultMapping = false;      // Any overrides are complete 
        inheritDefaultEmbedMapping = false;
        initialized = false;
    }

    /**
     * Constructor that can be used when the list of supported extract and embed mimetypes is known up front.
     *
     * @param supportedMimetypes    the set of mimetypes supported for extraction by default
     * @param supportedEmbedMimetypes    the set of mimetypes supported for embedding by default
     */
    protected AbstractMappingMetadataExtracter(Set<String> supportedMimetypes, Set<String> supportedEmbedMimetypes)
    {
        this(supportedMimetypes);
        this.supportedEmbedMimetypes = supportedEmbedMimetypes;
    }

    /**
     * Set the registry to register with.  If this is not set, then the default
     * initialization will not auto-register the extracter for general use.  It
     * can still be used directly.
     * 
     * @param registry a metadata extracter registry
     */
    public void setRegistry(MetadataExtracterRegistry registry)
    {
        this.registry = registry;
    }

    /**
     * @param mimetypeService       the mimetype service.  Set this if required.
     */
    public void setMimetypeService(MimetypeService mimetypeService)
    {
        this.mimetypeService = mimetypeService;
    }

    /**
     * @return Returns the mimetype helper
     */
    protected MimetypeService getMimetypeService()
    {
        return mimetypeService;
    }

    /**
     * @param dictionaryService     the dictionary service to determine which data conversions are necessary
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the mimetypes that are supported by the extracter.
     * 
     */
    public void setSupportedMimetypes(Collection<String> supportedMimetypes)
    {
        this.supportedMimetypes.clear();
        this.supportedMimetypes.addAll(supportedMimetypes);
    }

    /**
     * Set the mimetypes that are supported for embedding.
     *
     */
    public void setSupportedEmbedMimetypes(Collection<String> supportedEmbedMimetypes)
    {
        this.supportedEmbedMimetypes.clear();
        this.supportedEmbedMimetypes.addAll(supportedEmbedMimetypes);
    }

    /**
     * {@inheritDoc}
     * 
     * @see #setSupportedMimetypes(Collection)
     */
    @Override
    public boolean isSupported(String sourceMimetype)
    {
        return supportedMimetypes.contains(sourceMimetype) && isEnabled(sourceMimetype);
    }

    /**
     * {@inheritDoc}
     *
     * @see #setSupportedEmbedMimetypes(Collection)
     */
    @Override
    public boolean isEmbeddingSupported(String sourceMimetype)
    {
        if (supportedEmbedMimetypes == null)
        {
            return false;
        }
        return supportedEmbedMimetypes.contains(sourceMimetype);
    }

    private boolean isEnabled(String mimetype)
    {
        return properties == null || mimetypeService == null ||
               (getBooleanProperty(beanName+".enabled", true) &&
                getBooleanProperty(beanName+'.'+mimetypeService.getExtension(mimetype)+".enabled", true));
    }

    private boolean getBooleanProperty(String name, boolean defaultValue)
    {
        boolean value = defaultValue;
        if (properties != null)
        {
            String property = properties.getProperty(name);
            if (property != null)
            {
                value = property.trim().equalsIgnoreCase("true");
            }
        }
        return value;
    }

    /**
     * Set the policy to use when existing values are encountered.  Depending on how the extractor
     * is called, this may not be relevant, i.e an empty map of existing properties may be passed
     * in by the client code, which may follow its own overwrite strategy.
     * 
     * @param overwritePolicy       the policy to apply when there are existing system properties
     */
    public void setOverwritePolicy(OverwritePolicy overwritePolicy)
    {
        this.overwritePolicy = overwritePolicy;
    }

    /**
     * Set whether the extractor should discard metadata that fails to convert to the target type
     * defined in the data dictionary model.  This is <tt>true</tt> by default i.e. if the data
     * extracted is not compatible with the target model then the extraction will fail.  If this is
     * <tt>false</tt> then any extracted data that fails to convert will be discarded.
     * 
     * @param failOnTypeConversion      <tt>false</tt> to discard properties that can't get converted
     *                                  to the dictionary-defined type, or <tt>true</tt> (default)
     *                                  to fail the extraction if the type doesn't convert
     */
    public void setFailOnTypeConversion(boolean failOnTypeConversion)
    {
        this.failOnTypeConversion = failOnTypeConversion;
    }

    /**
     * Set the date formats, over and above the {@link ISO8601DateFormat ISO8601 format}, that will
     * be supported for string to date conversions.  The supported syntax is described by the
     * <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/text/SimpleDateFormat.html">SimpleDateFormat Javadocs</a>.
     * 
     * @param supportedDateFormats      a list of supported date formats.
     */
    public void setSupportedDateFormats(List<String> supportedDateFormats)
    {
        supportedDateFormatters = new HashSet<DateTimeFormatter>();
        
        // Note: The previous version attempted to create a single DateTimeFormatter from
        // multiple DateTimeFormatters, but that does not work as the time zone part is lost.
        // Now have a set of them.
        for (String dateFormatStr : supportedDateFormats)
        {
            try
            {
                supportedDateFormatters.add(DateTimeFormat.forPattern(dateFormatStr));
            }
            catch (Throwable e)
            {
                // No good
                throw new AlfrescoRuntimeException("Unable to set supported date format: " + dateFormatStr, e);
            }
        }
    }

    /**
     * Set if the property mappings augment or override the mapping generically provided by the
     * extracter implementation.  The default is <tt>false</tt>, i.e. any mapping set completely
     * replaces the {@link #getDefaultMapping() default mappings}.
     * <p>
     * Note that even when set to <tt>true</tt> an individual property mapping entry replaces the
     * entry provided by the extracter implementation.
     * 
     * @param inheritDefaultMapping <tt>true</tt> to add the configured mapping
     *                              to the list of default mappings.
     * 
     * @see #getDefaultMapping()
     * @see #setMapping(Map)
     * @see #setMappingProperties(Properties)
     */
    public void setInheritDefaultMapping(boolean inheritDefaultMapping)
    {
        this.inheritDefaultMapping = inheritDefaultMapping;
    }

    @Override
    public void setBeanName(String beanName)
    {
        this.beanName = beanName;
    }
    
    public String getBeanName()
    {
        return beanName;
    }
    
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }
    
    /**
     * The Alfresco global properties.
     */
    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }
    
    /**
     * Whether or not to enable the pass through of simple strings to cm:taggable tags
     * 
     * @param enableStringTagging       <tt>true</tt> find or create tags for each string 
     *                                  mapped to cm:taggable.  <tt>false</tt> (default) 
     *                                  ignore mapping strings to tags.
     */
    public void setEnableStringTagging(boolean enableStringTagging)
    {
        this.enableStringTagging = enableStringTagging;
    }

    /**
     * Set if the embed property mappings augment or override the mapping generically provided by the
     * extracter implementation.  The default is <tt>false</tt>, i.e. any mapping set completely
     * replaces the {@link #getDefaultEmbedMapping() default mappings}.
     * <p>
     * Note that even when set to <tt>true</tt> an individual property mapping entry replaces the
     * entry provided by the extracter implementation.
     *
     * @param inheritDefaultEmbedMapping <tt>true</tt> to add the configured embed mapping
     *                              to the list of default embed mappings.
     *
     * @see #getDefaultEmbedMapping()
     * @see #setEmbedMapping(Map)
     * @see #setEmbedMappingProperties(Properties)
     */
    public void setInheritDefaultEmbedMapping(boolean inheritDefaultEmbedMapping)
    {
        this.inheritDefaultEmbedMapping = inheritDefaultEmbedMapping;
    }

    /**
     * Sets the map of source mimetypes to metadata extracter limits.
     * 
     */
    public void setMimetypeLimits(Map<String, MetadataExtracterLimits> mimetypeLimits)
    {
        this.mimetypeLimits = mimetypeLimits;
    }

    /**
     * Gets the <code>ExecutorService</code> to be used for timeout-aware
     * extraction.
     * <p>
     * If no <code>ExecutorService</code> has been defined a default
     * of <code>Executors.newCachedThreadPool()</code> is used during
     * {@link AbstractMappingMetadataExtracter#init()}.
     * 
     * @return the defined or default <code>ExecutorService</code>
     */
    protected ExecutorService getExecutorService()
    {
        return executorService;
    }

    /**
     * Sets the <code>ExecutorService</code> to be used for timeout-aware
     * extraction.
     * 
     * @param executorService the <code>ExecutorService</code> for timeouts
     */
    public void setExecutorService(ExecutorService executorService)
    {
        this.executorService = executorService;
    }

    /**
     * Set the mapping from document metadata to system metadata.  It is possible to direct
     * an extracted document property to several system properties.  The conversion between
     * the document property types and the system property types will be done by the
     * {@link org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter default converter}.
     * 
     * @param mapping       a mapping from document metadata to system metadata
     */
    public void setMapping(Map<String, Set<QName>> mapping)
    {
        this.mapping = mapping;
    }

    /**
     * Set the embed mapping from document metadata to system metadata.  It is possible to direct
     * an model properties to several content file metadata keys.  The conversion between
     * the model property types and the content file metadata keys types will be done by the
     * {@link org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter default converter}.
     *
     * @param embedMapping       an embed mapping from model properties to content file metadata keys
     */
    public void setEmbedMapping(Map<QName, Set<String>> embedMapping)
    {
        this.embedMapping = embedMapping;
    }

    /**
     * Set the properties that contain the mapping from document metadata to system metadata.
     * This is an alternative to the {@link #setMapping(Map)} method.  Any mappings already
     * present will be cleared out.
     * 
     * The property mapping is of the form:
     * <pre>
     * # Namespaces prefixes
     * namespace.prefix.cm=http://www.alfresco.org/model/content/1.0
     * namespace.prefix.my=http://www....com/alfresco/1.0
     * 
     * # Mapping
     * editor=cm:author, my:editor
     * title=cm:title
     * user1=cm:summary
     * user2=cm:description
     * </pre>
     * The mapping can therefore be from a single document property onto several system properties.
     * 
     * @param mappingProperties     the properties that map document properties to system properties
     */
    public void setMappingProperties(Properties mappingProperties)
    {
        mapping = readMappingProperties(mappingProperties);
    }

    /**
     * Set the properties that contain the embed mapping from model properties to content file metadata.
     * This is an alternative to the {@link #setEmbedMapping(Map)} method.  Any mappings already
     * present will be cleared out.
     *
     * The property mapping is of the form:
     * <pre>
     * # Namespaces prefixes
     * namespace.prefix.cm=http://www.alfresco.org/model/content/1.0
     * namespace.prefix.my=http://www....com/alfresco/1.0
     *
     * # Mapping
     * cm\:author=editor
     * cm\:title=title
     * cm\:summary=user1
     * cm\:description=description,user2
     * </pre>
     * The embed mapping can therefore be from a model property onto several content file metadata properties.
     *
     * @param embedMappingProperties     the properties that map model properties to content file metadata properties
     */
    public void setEmbedMappingProperties(Properties embedMappingProperties)
    {
        embedMapping = readEmbedMappingProperties(embedMappingProperties);
    }

    /**
     * Helper method for derived classes to obtain the mappings that will be applied to raw
     * values.  This should be called after initialization in order to guarantee the complete
     * map is given.
     * <p>
     * Normally, the list of properties that can be extracted from a document is fixed and
     * well-known - in that case, just extract everything.  But Some implementations may have
     * an extra, indeterminate set of values available for extraction.  If the extraction of
     * these runtime parameters is expensive, then the keys provided by the return value can
     * be used to extract values from the documents.  The metadata extraction becomes fully
     * configuration-driven, i.e. declaring further mappings will result in more values being
     * extracted from the documents.
     * <p>
     * Most extractors will not be using this method.  For an example of its use, see the
     * {@linkplain OpenDocumentMetadataExtracter OpenDocument extractor}, which uses the mapping
     * to select specific user properties from a document.
     */
    protected final Map<String, Set<QName>> getMapping()
    {
        if (!initialized)
        {
            throw new UnsupportedOperationException("The complete mapping is only available after initialization.");
        }
        return Collections.unmodifiableMap(mapping);
    }

    /**
     * Helper method for derived classes to obtain the embed mappings.
     * This should be called after initialization in order to guarantee the complete
     * map is given.
     * <p>
     * Normally, the list of properties that can be embedded in a document is fixed and
     * well-known..  But some implementations may have
     * an extra, indeterminate set of values available for embedding.  If the embedding of
     * these runtime parameters is expensive, then the keys provided by the return value can
     * be used to embed values in the documents.  The metadata embedding becomes fully
     * configuration-driven, i.e. declaring further mappings will result in more values being
     * embedded in the documents.
     */
    protected final Map<QName, Set<String>> getEmbedMapping()
    {
        if (!initialized)
        {
            throw new UnsupportedOperationException("The complete embed mapping is only available after initialization.");
        }
        return Collections.unmodifiableMap(embedMapping);
    }

    /**
     * A utility method to read mapping properties from a resource file and convert to the map form.
     * 
     * @param propertiesUrl     A standard Properties file URL location
     * 
     * @see #setMappingProperties(Properties)
     */
    protected Map<String, Set<QName>> readMappingProperties(String propertiesUrl)
    {
        InputStream is = null;
        try
        {
            is = getClass().getClassLoader().getResourceAsStream(propertiesUrl);
            if(is == null)
            {
                throw new AlfrescoRuntimeException(
                        "Metadata Extracter mapping properties not found: \n" +
                        "   Extracter:  " + this + "\n" +
                        "   Bundle:     " + propertiesUrl);
            }
            Properties props = new Properties();
            props.load(is);
            // Process it
            Map<String, Set<QName>> map = readMappingProperties(props);
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("Loaded mapping properties from resource: " + propertiesUrl);
            }
            return map;
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException(
                    "Unable to load properties file to read extracter mapping properties: \n" +
                    "   Extracter:  " + this + "\n" +
                    "   Bundle:     " + propertiesUrl,
                    e);
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (Throwable e) {}
            }
        }
    }
    
    /**
     * A utility method to convert global properties to the Map form for the given
     * propertyComponent.
     * <p>
     * Mappings can be specified using the same method defined for
     * normal mapping properties files but with a prefix of
     * <code>metadata.extracter</code>, the extracter bean name, and the propertyComponent.
     * For example:
     * 
     *     metadata.extracter.TikaAuto.extract.namespace.prefix.my=http://DummyMappingMetadataExtracter
     *     metadata.extracter.TikaAuto.extract.namespace.prefix.cm=http://www.alfresco.org/model/content/1.0
     *     metadata.extracter.TikaAuto.extract.dc\:description=cm:description, my:customDescription
     * 
     */
    private Map<Object, Object> getRelevantGlobalProperties(String propertyComponent)
    {
        if (applicationContext == null)
        {
            logger.info("ApplicationContext not set");
            return null;
        }
        Properties globalProperties = (Properties) applicationContext.getBean("global-properties");
        if (globalProperties == null)
        {
            logger.info("Could not get global-properties");
            return null;
        }
        Map<Object, Object> relevantGlobalPropertiesMap = 
                new HashMap<Object, Object>();
        String propertyPrefix = PROPERTY_PREFIX_METADATA + beanName + propertyComponent;
        for (Entry<Object, Object> globalEntry : globalProperties.entrySet())
        {
            if (((String) globalEntry.getKey()).startsWith(propertyPrefix))
            {
                relevantGlobalPropertiesMap.put(
                        ((String) globalEntry.getKey()).replace(propertyPrefix, ""),
                        globalEntry.getValue());
            }
        }
        return relevantGlobalPropertiesMap;
    }
    
    /**
     * A utility method to convert global properties to the Map form for the given
     * propertyComponent.
     * <p>
     * Mappings can be specified using the same method defined for
     * normal mapping properties files but with a prefix of
     * <code>metadata.extracter</code>, the extracter bean name, and the extract component.
     * For example:
     * 
     *     metadata.extracter.TikaAuto.extract.namespace.prefix.my=http://DummyMappingMetadataExtracter
     *     metadata.extracter.TikaAuto.extract.namespace.prefix.cm=http://www.alfresco.org/model/content/1.0
     *     metadata.extracter.TikaAuto.extract.dc\:description=cm:description, my:customDescription
     * 
     */
    protected Map<String, Set<QName>> readGlobalExtractMappingProperties()
    {
        Map<Object, Object> relevantGlobalPropertiesMap = getRelevantGlobalProperties(PROPERTY_COMPONENT_EXTRACT);
        if (relevantGlobalPropertiesMap == null)
        {
            return null;
        }
        return readMappingProperties(relevantGlobalPropertiesMap.entrySet());
    }
    
    /**
     * A utility method to convert mapping properties to the Map form.
     * 
     * @see #setMappingProperties(Properties)
     */
    protected Map<String, Set<QName>> readMappingProperties(Properties mappingProperties)
    {
        return readMappingProperties(mappingProperties.entrySet());
    }
    
    /**
     * A utility method to convert mapping properties entries to the Map form.
     * 
     * @see #setMappingProperties(Properties)
     */
    private Map<String, Set<QName>> readMappingProperties(Set<Entry<Object, Object>> mappingPropertiesEntries)
    {
        Map<String, String> namespacesByPrefix = new HashMap<String, String>(5);
        // Get the namespaces
        for (Map.Entry<Object, Object> entry : mappingPropertiesEntries)
        {
            String propertyName = (String) entry.getKey();
            if (propertyName.startsWith(NAMESPACE_PROPERTY_PREFIX))
            {
                String prefix = propertyName.substring(17);
                String namespace = (String) entry.getValue();
                namespacesByPrefix.put(prefix, namespace);
            }
        }
        // Create the mapping
        Map<String, Set<QName>> convertedMapping = new HashMap<String, Set<QName>>(17);
        for (Map.Entry<Object, Object> entry : mappingPropertiesEntries)
        {
            String documentProperty = (String) entry.getKey();
            String qnamesStr = (String) entry.getValue();
            if (documentProperty.startsWith(NAMESPACE_PROPERTY_PREFIX))
            {
                // Ignore these now
                continue;
            }
            // Create the entry
            Set<QName> qnames = new HashSet<QName>(3);
            convertedMapping.put(documentProperty, qnames);
            // The to value can be a list of QNames
            StringTokenizer tokenizer = new StringTokenizer(qnamesStr, ",");
            while (tokenizer.hasMoreTokens())
            {
                String qnameStr = tokenizer.nextToken().trim();
                // Check if we need to resolve a namespace reference
                int index = qnameStr.indexOf(QName.NAMESPACE_PREFIX);
                if (index > -1 && qnameStr.charAt(0) != QName.NAMESPACE_BEGIN)
                {
                    String prefix = qnameStr.substring(0, index);
                    String suffix = qnameStr.substring(index + 1);
                    // It is prefixed
                    String uri = namespacesByPrefix.get(prefix);
                    if (uri == null)
                    {
                        throw new AlfrescoRuntimeException(
                                "No prefix mapping for extracter property mapping: \n" +
                                "   Extracter: " + this + "\n" +
                                "   Mapping: " + entry);
                    }
                    qnameStr = QName.NAMESPACE_BEGIN + uri + QName.NAMESPACE_END + suffix;
                }
                try
                {
                    QName qname = QName.createQName(qnameStr);
                    // Add it to the mapping
                    qnames.add(qname);
                }
                catch (InvalidQNameException e)
                {
                    throw new AlfrescoRuntimeException(
                            "Can't create metadata extracter property mapping: \n" +
                            "   Extracter: " + this + "\n" +
                            "   Mapping: " + entry);
                }
            }
            if (logger.isTraceEnabled())
            {
                logger.trace("Added mapping from " + documentProperty + " to " + qnames);
            }
        }
        // Done
        return convertedMapping;
    }

    /**
     * A utility method to read embed mapping properties from a resource file and convert to the map form.
     *
     * @param propertiesUrl     A standard Properties file URL location
     *
     * @see #setEmbedMappingProperties(Properties)
     */
    protected Map<QName, Set<String>> readEmbedMappingProperties(String propertiesUrl)
    {
        InputStream is = null;
        try
        {
            is = getClass().getClassLoader().getResourceAsStream(propertiesUrl);
            if(is == null)
            {
                return null;
            }
            Properties props = new Properties();
            props.load(is);
            // Process it
            Map<QName, Set<String>> map = readEmbedMappingProperties(props);
            // Done
            if (logger.isDebugEnabled())
            {
                logger.debug("Loaded embed mapping properties from resource: " + propertiesUrl);
            }
            return map;
        }
        catch (Throwable e)
        {
            throw new AlfrescoRuntimeException(
                    "Unable to load properties file to read extracter embed mapping properties: \n" +
                    "   Extracter:  " + this + "\n" +
                    "   Bundle:     " + propertiesUrl,
                    e);
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (Throwable e) {}
            }
        }
    }
    
    /**
     * A utility method to convert global mapping properties to the Map form.
     * <p>
     * Different from readGlobalExtractMappingProperties in that keys are the Alfresco QNames
     * and values are file metadata properties.
     * <p>
     * Mappings can be specified using the same method defined for
     * normal embed mapping properties files but with a prefix of
     * <code>metadata.extracter</code>, the extracter bean name, and the embed component.
     * For example:
     * 
     *     metadata.extracter.TikaAuto.embed.namespace.prefix.cm=http://www.alfresco.org/model/content/1.0
     *     metadata.extracter.TikaAuto.embed.cm\:description=description
     *
     * @see #setMappingProperties(Properties)
     */
    protected Map<QName, Set<String>> readGlobalEmbedMappingProperties()
    {
        Map<Object, Object> relevantGlobalPropertiesMap = getRelevantGlobalProperties(PROPERTY_COMPONENT_EMBED);
        if (relevantGlobalPropertiesMap == null)
        {
            return null;
        }
        return readEmbedMappingProperties(relevantGlobalPropertiesMap.entrySet());
    }

    /**
     * A utility method to convert mapping properties to the Map form.
     * <p>
     * Different from readMappingProperties in that keys are the Alfresco QNames
     * and values are file metadata properties.
     *
     * @see #setMappingProperties(Properties)
     */
    protected Map<QName, Set<String>> readEmbedMappingProperties(Properties mappingProperties)
    {
        return readEmbedMappingProperties(mappingProperties.entrySet());
    }
    
    /**
     * A utility method to convert mapping properties entries to the Map form.
     * <p>
     * Different from readMappingProperties in that keys are the Alfresco QNames
     * and values are file metadata properties.
     *
     * @see #setMappingProperties(Properties)
     */
    private Map<QName, Set<String>> readEmbedMappingProperties(Set<Entry<Object, Object>> mappingPropertiesEntries)
    {
        Map<String, String> namespacesByPrefix = new HashMap<String, String>(5);
        // Get the namespaces
        for (Map.Entry<Object, Object> entry : mappingPropertiesEntries)
        {
            String propertyName = (String) entry.getKey();
            if (propertyName.startsWith(NAMESPACE_PROPERTY_PREFIX))
            {
                String prefix = propertyName.substring(17);
                String namespace = (String) entry.getValue();
                namespacesByPrefix.put(prefix, namespace);
            }
        }
        // Create the mapping
        Map<QName, Set<String>> convertedMapping = new HashMap<QName, Set<String>>(17);
        for (Map.Entry<Object, Object> entry : mappingPropertiesEntries)
        {
            String modelProperty = (String) entry.getKey();
            String metadataKeysString = (String) entry.getValue();
            if (modelProperty.startsWith(NAMESPACE_PROPERTY_PREFIX))
            {
                // Ignore these now
                continue;
            }

                int index = modelProperty.indexOf(QName.NAMESPACE_PREFIX);
                if (index > -1 && modelProperty.charAt(0) != QName.NAMESPACE_BEGIN)
                {
                    String prefix = modelProperty.substring(0, index);
                    String suffix = modelProperty.substring(index + 1);
                    // It is prefixed
                    String uri = namespacesByPrefix.get(prefix);
                    if (uri == null)
                    {
                        throw new AlfrescoRuntimeException(
                                "No prefix mapping for embed property mapping: \n" +
                                "   Extracter: " + this + "\n" +
                                "   Mapping: " + entry);
                    }
                    modelProperty = QName.NAMESPACE_BEGIN + uri + QName.NAMESPACE_END + suffix;
                }
                try
                {
                    QName qname = QName.createQName(modelProperty);
                    String[] metadataKeysArray = metadataKeysString.split(",");
                    Set<String> metadataKeys = new HashSet<String>(metadataKeysArray.length);
                    for (String metadataKey : metadataKeysArray) {
                        metadataKeys.add(metadataKey.trim());
                    }
                    // Create the entry
                    convertedMapping.put(qname, metadataKeys);
                }
                catch (InvalidQNameException e)
                {
                    throw new AlfrescoRuntimeException(
                            "Can't create metadata embedding property mapping: \n" +
                            "   Extracter: " + this + "\n" +
                            "   Mapping: " + entry);
                }
            if (logger.isTraceEnabled())
            {
                logger.trace("Added mapping from " + modelProperty + " to " + metadataKeysString);
            }
        }
        // Done
        return convertedMapping;
    }

    /**
     * Registers this instance of the extracter with the registry.  This will call the
     * {@link #init()} method and then register if the registry is available.
     * 
     * @see #setRegistry(MetadataExtracterRegistry)
     * @see #init()
     */
    public final void register()
    {
        init();
        
        // Register the extracter, if necessary
        if (registry != null)
        {
            registry.register(this);
        }
    }
    
    /**
     * Provides a hook point for implementations to perform initialization.  The base
     * implementation must be invoked or the extracter will fail during extraction.
     * The {@link #getDefaultMapping() default mappings} will be requested during
     * initialization.
     */
    protected void init()
    {
        Map<String, Set<QName>> defaultMapping = getDefaultMapping();
        if (defaultMapping == null)
        {
            throw new AlfrescoRuntimeException("The metadata extracter must provide a default mapping: " + this);
        }
        
        // Was a mapping explicitly provided
        if (mapping == null)
        {
            // No mapping, so use the default
            mapping = defaultMapping;
        }
        else if (inheritDefaultMapping)
        {
            // Merge the default mapping into the configured mapping
            for (String documentKey : defaultMapping.keySet())
            {
                Set<QName> systemQNames = mapping.get(documentKey);
                if (systemQNames == null)
                {
                    systemQNames = new HashSet<QName>(3);
                    mapping.put(documentKey, systemQNames);
                    Set<QName> defaultQNames = defaultMapping.get(documentKey);
                    systemQNames.addAll(defaultQNames);
                }
            }
        }
        
        // Override with any extract mappings specified in global properties
        Map<String, Set<QName>> globalExtractMapping = readGlobalExtractMappingProperties();
        if (globalExtractMapping != null && globalExtractMapping.size() > 0)
        {
            for (String documentKey : globalExtractMapping.keySet())
            {
                mapping.put(documentKey, globalExtractMapping.get(documentKey));
            }
        }
        
        // The configured mappings are empty, but there were default mappings
        if (mapping.size() == 0 && defaultMapping.size() > 0)
        {
            logger.warn(
                    "There are no property mappings for the metadata extracter.\n" +
                    "  Nothing will be extracted by: " + this);
        }

        if (executorService == null)
        {
            executorService = Executors.newCachedThreadPool();
        }
        
        if (mimetypeLimits == null)
        {
            if (properties != null)
            {
                String property = properties.getProperty(PROP_DEFAULT_TIMEOUT);
                if (property != null)
                {
                    Long value = Long.parseLong(property);
                    if (value != null)
                    {
                        MetadataExtracterLimits limits = new MetadataExtracterLimits();
                        limits.setTimeoutMs(value);
                        mimetypeLimits = new HashMap<String, MetadataExtracterLimits>(1);
                        mimetypeLimits.put("*", limits);
                    }
                }
            }
        }

        Map<QName, Set<String>> defaultEmbedMapping = getDefaultEmbedMapping();

        // Was a mapping explicitly provided
        if (embedMapping == null)
        {
            // No mapping, so use the default
            embedMapping = defaultEmbedMapping;
        }
        
        else if (inheritDefaultEmbedMapping)
        {
            // Merge the default mapping into the configured mapping
            for (QName modelProperty : defaultEmbedMapping.keySet())
            {
                Set<String> metadataKeys = embedMapping.get(modelProperty);
                if (metadataKeys == null)
                {
                    metadataKeys = new HashSet<String>(3);
                    embedMapping.put(modelProperty, metadataKeys);
                    Set<String> defaultMetadataKeys = defaultEmbedMapping.get(modelProperty);
                    metadataKeys.addAll(defaultMetadataKeys);
                }
            }
        }
        
        // Override with any embed mappings specified in global properties
        Map<QName, Set<String>> globalEmbedMapping = readGlobalEmbedMappingProperties();
        if (globalEmbedMapping != null && globalEmbedMapping.size() > 0)
        {
            for (QName modelProperty : globalEmbedMapping.keySet())
            {
                embedMapping.put(modelProperty, globalEmbedMapping.get(modelProperty));
            }
        }
        // Done
        initialized = true;
    }

    /**
     * Checks if the mimetype is supported.
     * 
     * @param reader the reader to check
     * @throws AlfrescoRuntimeException if the mimetype is not supported
     */
    protected void checkIsSupported(ContentReader reader)
    {
        String mimetype = reader.getMimetype();
        if (!isSupported(mimetype))
        {
            throw new AlfrescoRuntimeException(
                    "Metadata extracter does not support mimetype: " + mimetype + "\n" +
                    "   reader: " + reader + "\n" +
                    "   supported: " + supportedMimetypes + "\n" +
                    "   extracter: " + this);
        }
    }

    /**
     * Checks if embedding for the mimetype is supported.
     *
     * @param writer the writer to check
     * @throws AlfrescoRuntimeException if embedding for the mimetype is not supported
     */
    protected void checkIsEmbedSupported(ContentWriter writer)
    {
        String mimetype = writer.getMimetype();
        if (!isEmbeddingSupported(mimetype))
        {
            throw new AlfrescoRuntimeException(
                    "Metadata extracter does not support embedding mimetype: \n" +
                    "   writer: " + writer + "\n" +
                    "   supported: " + supportedEmbedMimetypes + "\n" +
                    "   extracter: " + this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Map<QName, Serializable> extract(ContentReader reader, Map<QName, Serializable> destination)
    {
        return extract(reader, this.overwritePolicy, destination, this.mapping);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Map<QName, Serializable> extract(
            ContentReader reader,
            OverwritePolicy overwritePolicy,
            Map<QName, Serializable> destination)
    {
        return extract(reader, overwritePolicy, destination, this.mapping);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<QName, Serializable> extract(
            ContentReader reader,
            OverwritePolicy overwritePolicy,
            Map<QName, Serializable> destination,
            Map<String, Set<QName>> mapping)
    {
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Starting metadata extraction: \n" +
                    "   reader: " + reader + "\n" +
                    "   extracter: " + this);
        }

        if (!initialized)
        {
            throw new AlfrescoRuntimeException(
                    "Metadata extracter not initialized.\n" +
                    "  Call the 'register' method on: " + this + "\n" +
                    "  Implementations of the 'init' method must call the base implementation.");
        }
        // check the reliability
        checkIsSupported(reader);
        
        Map<QName, Serializable> changedProperties = null;
        try
        {
            Map<String, Serializable> rawMetadata = null;
            // Check that the content has some meat
            if (reader.getSize() > 0 && reader.exists())
            {
                rawMetadata = extractRaw(reader, getLimits(reader.getMimetype()));
            }
            else
            {
                rawMetadata = new HashMap<String, Serializable>(1);
            }
            // Convert to system properties (standalone)
            Map<QName, Serializable> systemProperties = mapRawToSystem(rawMetadata);
            // Convert the properties according to the dictionary types
            systemProperties = convertSystemPropertyValues(systemProperties);
            // Last chance to filter the system properties map before applying them            
            filterSystemProperties(systemProperties, destination);            
            // Now use the proper overwrite policy
            changedProperties = overwritePolicy.applyProperties(systemProperties, destination);
            
            if(logger.isDebugEnabled())
            {
               logger.debug("Extracted Metadata from " + reader + "\n  Found: " +
                            rawMetadata + "\n  Mapped and Accepted: " + changedProperties);
            }
        }
        catch (LimitExceededException e)
        {
            logger.warn("Metadata extraction rejected: \n" + 
                    "   Extracter: " + this + "\n" + 
                    "   Reason:   " + e.getMessage());
        }
        catch (Throwable e)
        {
            // Ask Tika to detect the document, and report back on if
            //  the current mime type is plausible
            String typeErrorMessage = null;
            String differentType = null;
            if(mimetypeService != null)
            {
               differentType = mimetypeService.getMimetypeIfNotMatches(reader.getReader());
            }
            else
            {
               logger.info("Unable to verify mimetype of " + reader.getReader() + 
                           " as no MimetypeService available to " + getClass().getName());
            }
            if(differentType != null)
            {
               typeErrorMessage = "\n" +
                  "   claimed mime type: " + reader.getMimetype() + "\n" +
                  "   detected mime type: " + differentType;
            }
           
            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Metadata extraction failed: \n" +
                        "   Extracter: " + this + "\n" +
                        "   Content:   " + reader +
                        typeErrorMessage,
                        e);
            }
            else
            {
                logger.warn(
                        "Metadata extraction failed (turn on DEBUG for full error): \n" +
                        "   Extracter: " + this + "\n" +
                        "   Content:   " + reader + "\n" +
                        "   Failure:   " + e.getMessage() +
                        typeErrorMessage);
            }
        }
        finally
        {
            // check that the reader was closed (if used)
            if (reader.isChannelOpen())
            {
                logger.error("Content reader not closed by metadata extracter: \n" +
                        "   reader: " + reader + "\n" +
                        "   extracter: " + this);
            }
            // Make sure that we have something to return
            if (changedProperties == null)
            {
                changedProperties = new HashMap<QName, Serializable>(0);
            }
        }
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Completed metadata extraction: \n" +
                    "   reader:    " + reader + "\n" +
                    "   extracter: " + this + "\n" +
                    "   changed:   " + changedProperties);
        }
        return changedProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void embed(
            Map<QName, Serializable> properties,
            ContentReader reader,
            ContentWriter writer)
    {
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Starting metadata embedding: \n" +
                    "   reader: " + reader + "\n" +
                    "   writer: " + writer + "\n" +
                    "   extracter: " + this);
        }

        if (!initialized)
        {
            throw new AlfrescoRuntimeException(
                    "Metadata extracter not initialized.\n" +
                    "  Call the 'register' method on: " + this + "\n" +
                    "  Implementations of the 'init' method must call the base implementation.");
        }
        // check the reliability
        checkIsEmbedSupported(writer);

        try
        {
            embedInternal(mapSystemToRaw(properties), reader, writer);
            if(logger.isDebugEnabled())
            {
               logger.debug("Embedded Metadata into " + writer);
            }
        }
        catch (Throwable e)
        {
            // Ask Tika to detect the document, and report back on if
            //  the current mime type is plausible
            String typeErrorMessage = "";
            String differentType = null;
            if(mimetypeService != null)
            {
               try
               {
                   differentType = mimetypeService.getMimetypeIfNotMatches(writer.getReader());
               }
               catch (ContentIOException cioe)
               {
                   // Embedding failed and writer is empty
               }
            }
            else
            {
               logger.info("Unable to verify mimetype of " + writer.getReader() +
                           " as no MimetypeService available to " + getClass().getName());
            }
            if(differentType != null)
            {
               typeErrorMessage = "\n" +
                  "   claimed mime type: " + writer.getMimetype() + "\n" +
                  "   detected mime type: " + differentType;
            }

            if (logger.isDebugEnabled())
            {
                logger.debug(
                        "Metadata embedding failed: \n" +
                        "   Extracter: " + this + "\n" +
                        "   Content:   " + writer +
                        typeErrorMessage,
                        e);
            }
            else
            {
                logger.error(
                        "Metadata embedding failed (turn on DEBUG for full error): \n" +
                        "   Extracter: " + this + "\n" +
                        "   Content:   " + writer + "\n" +
                        "   Failure:   " + e.getMessage() +
                        typeErrorMessage);
            }
        }
        finally
        {
            // check that the writer was closed (if used)
            if (writer.isChannelOpen())
            {
                logger.error("Content writer not closed by metadata extracter: \n" +
                        "   writer: " + writer + "\n" +
                        "   extracter: " + this);
            }
        }

        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Completed metadata embedding: \n" +
                    "   writer:    " + writer + "\n" +
                    "   extracter: " + this);
        }
    }

    /**
     * 
     * @param rawMetadata   Metadata keyed by document properties
     * @return              Returns the metadata keyed by the system properties
     */
    private Map<QName, Serializable> mapRawToSystem(Map<String, Serializable> rawMetadata)
    {
        Map<QName, Serializable> systemProperties = new HashMap<QName, Serializable>(rawMetadata.size() * 2 + 1);
        for (Map.Entry<String, Serializable> entry : rawMetadata.entrySet())
        {
            String documentKey = entry.getKey();
            // Check if there is a mapping for this
            if (!mapping.containsKey(documentKey))
            {
                // No mapping - ignore
                continue;
            }
            Serializable documentValue = entry.getValue();
            Set<QName> systemQNames = mapping.get(documentKey);
            for (QName systemQName : systemQNames)
            {
                systemProperties.put(systemQName, documentValue);                
            }
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Converted extracted raw values to system values: \n" +
                    "   Raw Properties:    " + rawMetadata + "\n" +
                    "   System Properties: " + systemProperties);
        }
        return systemProperties;
    }

    /**
     *
     * @param systemMetadata   Metadata keyed by system properties
     * @return              Returns the metadata keyed by the content file metadata properties
     */
    private Map<String, Serializable> mapSystemToRaw(Map<QName, Serializable> systemMetadata)
    {
        Map<String, Serializable> metadataProperties = new HashMap<String, Serializable>(systemMetadata.size() * 2 + 1);
        for (Map.Entry<QName, Serializable> entry : systemMetadata.entrySet())
        {
            QName modelProperty = entry.getKey();
            // Check if there is a mapping for this
            if (!embedMapping.containsKey(modelProperty))
            {
                // No mapping - ignore
                continue;
            }
            Serializable documentValue = entry.getValue();
            Set<String> metadataKeys = embedMapping.get(modelProperty);
            for (String metadataKey : metadataKeys)
            {
                metadataProperties.put(metadataKey, documentValue);
            }
        }
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug(
                    "Converted system model values to metadata values: \n" +
                    "   System Properties:    " + systemMetadata + "\n" +
                    "   Metadata Properties: " + metadataProperties);
        }
        return metadataProperties;
    }

    /**
     * Filters the system properties that are going to be applied.  Gives the metadata extracter an 
     * opportunity to remove properties that may not be appropriate in a given context.
     * 
     * @param systemProperties  map of system properties to be applied
     * @param targetProperties  map of target properties, may be used to provide to the context requried
     */
    protected void filterSystemProperties(Map<QName, Serializable> systemProperties, Map<QName, Serializable> targetProperties)
    {
        // Default implementation does nothing
    }
    
    /**
     * Converts all values according to their dictionary-defined type.  This uses the
     * {@link #setFailOnTypeConversion(boolean) failOnTypeConversion flag} to determine how failures
     * are handled i.e. if values fail to convert, the process may discard the property.
     * 
     * @param systemProperties  the values keyed to system property names
     * @return                  Returns a modified map of properties that have been converted.
     */
    @SuppressWarnings("unchecked")
    private Map<QName, Serializable> convertSystemPropertyValues(Map<QName, Serializable> systemProperties)
    {
        Map<QName, Serializable> convertedProperties = new HashMap<QName, Serializable>(systemProperties.size() + 7);
        for (Map.Entry<QName, Serializable> entry : systemProperties.entrySet())
        {
            QName propertyQName = entry.getKey();
            Serializable propertyValue = entry.getValue();
            // Get the property definition
            PropertyDefinition propertyDef = (dictionaryService == null) ? null : dictionaryService.getProperty(propertyQName);
            if (propertyDef == null)
            {
                // There is nothing in the DD about this so just transfer it
                convertedProperties.put(propertyQName, propertyValue);
                continue;
            }
            // It is in the DD, so attempt the conversion
            DataTypeDefinition propertyTypeDef = propertyDef.getDataType();
            Serializable convertedPropertyValue = null;
            
            try
            {
                // Attempt to make any date conversions
                if (propertyTypeDef.getName().equals(DataTypeDefinition.DATE) || propertyTypeDef.getName().equals(DataTypeDefinition.DATETIME))
                {
                    if (propertyValue instanceof Date)
                    {
                        convertedPropertyValue = propertyValue;
                    }
                    else if (propertyValue instanceof Collection)
                    {
                        convertedPropertyValue = (Serializable) makeDates((Collection<String>) propertyValue);
                    }
                    else if (propertyValue instanceof String)
                    {
                        convertedPropertyValue = makeDate((String) propertyValue);
                    }
                    else if (propertyValue == null)
                    {
                        convertedPropertyValue = null;
                    }
                    else
                    {
                        if (logger.isWarnEnabled())
                        {
                            StringBuilder mesg = new StringBuilder();
                            mesg.append("Unable to convert Date property: ").append(propertyQName)
                                .append(", value: ").append(propertyValue).append(", type: ").append(propertyTypeDef.getName());
                            logger.warn(mesg.toString());
                        }
                    }
                }
                else
                {
                    if (propertyValue instanceof Collection)
                    {
                        convertedPropertyValue = (Serializable) DefaultTypeConverter.INSTANCE.convert(
                                propertyTypeDef,
                                (Collection<?>) propertyValue);
                    }
                    else if (propertyValue instanceof Object[])
                    {
                       convertedPropertyValue = (Serializable) DefaultTypeConverter.INSTANCE.convert(
                             propertyTypeDef,
                             (Object[]) propertyValue);
                    }
                    else
                    {
                        convertedPropertyValue = (Serializable) DefaultTypeConverter.INSTANCE.convert(
                                propertyTypeDef,
                                propertyValue);
                    }
                }
                convertedProperties.put(propertyQName, convertedPropertyValue);
            }
            catch (TypeConversionException e)
            {
                logger.warn(
                        "Type conversion failed during metadata extraction: \n" + 
                        "   Failure:   " + e.getMessage() + "\n" +
                        "   Type:      " + propertyTypeDef + "\n" +
                        "   Value:     " + propertyValue);
                // Do we just absorb this or is it a problem?
                if (failOnTypeConversion)
                {
                    throw AlfrescoRuntimeException.create(
                            e,
                            ERR_TYPE_CONVERSION,
                            this,
                            propertyQName,
                            propertyTypeDef.getName(),
                            propertyValue);
                }
            }
            catch (MalformedNodeRefException e)
            {
                if (propertyQName.equals(ContentModel.PROP_TAGS))
                {
                    if (enableStringTagging)
                    {
                        // We must want to map tag string values instead of nodeRefs
                        // ContentMetadataExtracter will take care of tagging by string
                        ArrayList<Object> list = new ArrayList<Object>(1);
                        if (propertyValue instanceof Object[])
                        {
                            for (Object value : (Object[]) propertyValue)
                            {
                                list.add(value);
                            }
                        }
                        else
                        {
                            list.add(propertyValue);
                        }
                        convertedProperties.put(propertyQName, list);
                    }
                    else
                    {
                        if (logger.isInfoEnabled())
                        {
                            logger.info("enableStringTagging is false and could not convert " + 
                                    propertyQName.toString() + ": " + e.getMessage());
                        }
                    }
                }
                else
                {
                    if (failOnTypeConversion)
                    {
                        throw e;
                    }
                }
            }
        }
        // Done
        return convertedProperties;
    }

    /**
     * Convert a collection of date <tt>String</tt> to <tt>Date</tt> objects
     */
    private Collection<Date> makeDates(Collection<String> dateStrs)
    {
        List<Date> dates = new ArrayList<Date>(dateStrs.size());
        for (String dateStr : dateStrs)
        {
            Date date = makeDate(dateStr);
            dates.add(date);
        }
        return dates;
    }
    
    /**
     * Convert a date <tt>String</tt> to a <tt>Date</tt> object
     */
    protected Date makeDate(String dateStr)
    {
        if (dateStr == null || dateStr.length() == 0)
        {
            return null;
        }
        
        Date date = null;
        try
        {
            date = DefaultTypeConverter.INSTANCE.convert(Date.class, dateStr);
        }
        catch (TypeConversionException e)
        {
            // Try one of the other formats
            if (this.supportedDateFormatters != null)
            {
                // Remove text such as " (PDT)" which cannot be parsed.
                String dateStr2 = (dateStr == null || dateStr.indexOf('(') == -1)
                        ? dateStr : dateStr.replaceAll(" \\(.*\\)", "");
                for (DateTimeFormatter supportedDateFormatter: supportedDateFormatters)
                {
                    // supported DateFormats were defined
                    /**
                     * Regional date format
                     */
                    try
                    {
                        DateTime dateTime = supportedDateFormatter.parseDateTime(dateStr2);
                        if (dateTime.getCenturyOfEra() > 0)
                        {
                            return dateTime.toDate();
                        }
                    }
                    catch (IllegalArgumentException e1)
                    {
                        // Didn't work
                    }

                    /**
                     * Date format can be locale specific - make sure English format always works
                     */
                    /* 
                     * TODO MER 25 May 2010 - Added this as a quick fix for IMAP date parsing which is always 
                     * English regardless of Locale.  Some more thought and/or code is required to configure 
                     * the relationship between properties, format and locale.
                     */
                    try
                    {
                        DateTime dateTime = supportedDateFormatter.withLocale(Locale.US).parseDateTime(dateStr2);
                        if (dateTime.getCenturyOfEra() > 0)
                        {
                            return dateTime.toDate();
                        }
                    }
                    catch (IllegalArgumentException e1)
                    {
                        // Didn't work
                    }
                }
            }

            if (date == null)
            {
                // Still no luck
                throw new TypeConversionException("Unable to convert string to date: " + dateStr);
            }
        }
        return date;
    }
    
    /**
     * Adds a value to the map, conserving null values.  Values are converted to null if:
     * <ul>
     *   <li>it is an empty string value after trimming</li>
     *   <li>it is an empty collection</li>
     *   <li>it is an empty array</li>
     * </ul>
     * String values are trimmed before being put into the map.
     * Otherwise, it is up to the extracter to ensure that the value is a <tt>Serializable</tt>.
     * It is not appropriate to implicitly convert values in order to make them <tt>Serializable</tt>
     * - the best conversion method will depend on the value's specific meaning.
     * 
     * @param key           the destination key
     * @param value         the serializable value
     * @param destination   the map to put values into
     * @return              Returns <tt>true</tt> if set, otherwise <tt>false</tt>
     */
    protected boolean putRawValue(String key, Serializable value, Map<String, Serializable> destination)
    {
        if (value == null)
        {
            // Just keep this
        }
        else if (value instanceof String)
        {
            String valueStr = ((String) value).trim();
            if (valueStr.length() == 0)
            {
                value = null;
            }
            else
            {
                if(valueStr.indexOf("\u0000") != -1)
                {
                    valueStr = valueStr.replaceAll("\u0000", "");
                }
                // Keep the trimmed value
                value = valueStr;
            }
        }
        else if (value instanceof Collection)
        {
            Collection<?> valueCollection = (Collection<?>) value;
            if (valueCollection.isEmpty())
            {
                value = null;
            }
        }
        else if (value.getClass().isArray())
        {
            if (Array.getLength(value) == 0)
            {
                value = null;
            }
        }
        // It passed all the tests
        destination.put(key, value);
        return true;
    }

    /**
     * Helper method to fetch a clean map into which raw values can be dumped.
     * 
     * @return          Returns an empty map
     */
    protected final Map<String, Serializable> newRawMap()
    {
        return new HashMap<String, Serializable>(17);
    }

    /**
     * This method provides a <i>best guess</i> of where to store the values extracted
     * from the documents.  The list of properties mapped by default need <b>not</b>
     * include all properties extracted from the document; just the obvious set of mappings
     * need be supplied.
     * Implementations must either provide the default mapping properties in the expected
     * location or override the method to provide the default mapping.
     * <p>
     * The default implementation looks for the default mapping file in the location
     * given by the class name and <i>.properties</i>.  If the extracter's class is
     * <b>x.y.z.MyExtracter</b> then the default properties will be picked up at
     * <b>classpath:/alfresco/metadata/MyExtracter.properties</b>.
     * The previous location of <b>classpath:/x/y/z/MyExtracter.properties</b> is
     * still supported but may be removed in a future release.
     * Inner classes are supported, but the '$' in the class name is replaced with '-', so
     * default properties for <b>x.y.z.MyStuff$MyExtracter</b> will be located using
     * <b>classpath:/alfresco/metadata/MyStuff-MyExtracter.properties</b>.
     * <p>
     * The default mapping implementation should include thorough Javadocs so that the
     * system administrators can accurately determine how to best enhance or override the
     * default mapping.
     * <p>
     * If the default mapping is declared in a properties file other than the one named after
     * the class, then the {@link #readMappingProperties(String)} method can be used to quickly
     * generate the return value:
     * <pre><code>
     *      {
     *          return readMappingProperties(DEFAULT_MAPPING);
     *      }
     * </code></pre>
     * The map can also be created in code either statically or during the call.
     * 
     * @return              Returns the default, static mapping.  It may not be null.
     * 
     * @see #setInheritDefaultMapping(boolean inherit)
     */
    protected Map<String, Set<QName>> getDefaultMapping()
    {
        AlfrescoRuntimeException metadataLocationReadException = null;
        try
        {
            // Can't use getSimpleName here because we lose inner class $ processing
            String className = this.getClass().getName();
            String shortClassName = className.split("\\.")[className.split("\\.").length - 1];
            // Replace $
            shortClassName = shortClassName.replace('$', '-');
            // Append .properties
            String metadataPropertiesUrl = "alfresco/metadata/" + shortClassName + ".properties";
            // Attempt to load the properties
            return readMappingProperties(metadataPropertiesUrl);
        }
        catch (AlfrescoRuntimeException e)
        {
            // We'll save this to throw at someone later
            metadataLocationReadException = e;
        }
        // Try package location
        try
        {
            String canonicalClassName = this.getClass().getName();
            // Replace $
            canonicalClassName = canonicalClassName.replace('$', '-');
            // Replace .
            canonicalClassName = canonicalClassName.replace('.', '/');
            // Append .properties
            String packagePropertiesUrl = canonicalClassName + ".properties";
            // Attempt to load the properties
            return readMappingProperties(packagePropertiesUrl);
        }
        catch (AlfrescoRuntimeException e)
        {
            // Not found in either location, but we want to throw the error for the new metadata location
            throw metadataLocationReadException;
        }
    }

    /**
     * This method provides a <i>best guess</i> of what model properties should be embedded
     * in content.  The list of properties mapped by default need <b>not</b>
     * include all properties to be embedded in the document; just the obvious set of mappings
     * need be supplied.
     * Implementations must either provide the default mapping properties in the expected
     * location or override the method to provide the default mapping.
     * <p>
     * The default implementation looks for the default mapping file in the location
     * given by the class name and <i>.embed.properties</i>.  If the extracter's class is
     * <b>x.y.z.MyExtracter</b> then the default properties will be picked up at
     * <b>classpath:/x/y/z/MyExtracter.embed.properties</b>.
     * Inner classes are supported, but the '$' in the class name is replaced with '-', so
     * default properties for <b>x.y.z.MyStuff$MyExtracter</b> will be located using
     * <b>x.y.z.MyStuff-MyExtracter.embed.properties</b>.
     * <p>
     * The default mapping implementation should include thorough Javadocs so that the
     * system administrators can accurately determine how to best enhance or override the
     * default mapping.
     * <p>
     * If the default mapping is declared in a properties file other than the one named after
     * the class, then the {@link #readEmbedMappingProperties(String)} method can be used to quickly
     * generate the return value:
     * <pre><code>
     *      protected Map<<String, Set<QName>> getDefaultMapping()
     *      {
     *          return readEmbedMappingProperties(DEFAULT_MAPPING);
     *      }
     * </code></pre>
     * The map can also be created in code either statically or during the call.
     * <p>
     * If no embed mapping properties file is found a reverse of the extract
     * mapping in {@link #getDefaultMapping()} will be assumed with the first QName in each
     * value used as the key for this mapping and a last win approach for duplicates.
     *
     * @return              Returns the default, static embed mapping.  It may not be null.
     *
     * @see #setInheritDefaultMapping(boolean inherit)
     */
    protected Map<QName, Set<String>> getDefaultEmbedMapping()
    {
        Map<QName, Set<String>> embedMapping = null;
        String metadataPropertiesUrl = null;
        try
        {
            // Can't use getSimpleName here because we lose inner class $ processing
            String className = this.getClass().getName();
            String shortClassName = className.split("\\.")[className.split("\\.").length - 1];
            // Replace $
            shortClassName = shortClassName.replace('$', '-');
            // Append .properties
            metadataPropertiesUrl = "alfresco/metadata/" + shortClassName + ".embed.properties";
            // Attempt to load the properties
            embedMapping = readEmbedMappingProperties(metadataPropertiesUrl);
        }
        catch (AlfrescoRuntimeException e)
        {
            // No embed mapping found at default location
        }
        // Try package location
        try
        {
            String canonicalClassName = this.getClass().getName();
            // Replace $
            canonicalClassName = canonicalClassName.replace('$', '-');
            // Replace .
            canonicalClassName = canonicalClassName.replace('.', '/');
            // Append .properties
            String packagePropertiesUrl = canonicalClassName + ".embed.properties";
            // Attempt to load the properties
            embedMapping = readEmbedMappingProperties(packagePropertiesUrl);
        }
        catch (AlfrescoRuntimeException e)
        {
            // No embed mapping found at legacy location
        }
        if (embedMapping == null)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("No explicit embed mapping properties found at: " + metadataPropertiesUrl + ", assuming reverse of extract mapping");
            }
            Map<String, Set<QName>> extractMapping = this.mapping;
            if (extractMapping == null || extractMapping.size() == 0)
            {
                extractMapping = getDefaultMapping();
            }
            embedMapping = new HashMap<QName, Set<String>>(extractMapping.size());
            for (String metadataKey : extractMapping.keySet())
            {
                if (extractMapping.get(metadataKey) != null && extractMapping.get(metadataKey).size() > 0)
                {
                    QName modelProperty = extractMapping.get(metadataKey).iterator().next();
                    Set<String> metadataKeys = embedMapping.get(modelProperty);
                    if (metadataKeys == null)
                    {
                        metadataKeys = new HashSet<String>(1);
                        embedMapping.put(modelProperty, metadataKeys);
                    }
                    metadataKeys.add(metadataKey);
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Added mapping from " + modelProperty + " to " + metadataKeys.toString());
                    }
                }
            }
        }
        return embedMapping;
    }

    /**
     * Gets the metadata extracter limits for the given mimetype.
     * <p>
     * A specific match for the given mimetype is tried first and
     * if none is found a wildcard of "*" is tried, if still not found 
     * defaults value will be used
     * 
     * @param mimetype String
     * @return the found limits or default values
     */
    protected MetadataExtracterLimits getLimits(String mimetype)
    {
        if (mimetypeLimits == null)
        {
            return new MetadataExtracterLimits();
        }
        MetadataExtracterLimits limits = null;
        limits = mimetypeLimits.get(mimetype);
        if (limits == null)
        {
            limits = mimetypeLimits.get("*");
        }
        if (limits == null)
        {
            limits = new MetadataExtracterLimits();
        }
        
        return limits;
    }
    
    /**
     * <code>Callable</code> wrapper for the 
     * {@link AbstractMappingMetadataExtracter#extractRaw(ContentReader)} method
     * to handle timeouts.
     */
    private class ExtractRawCallable implements Callable<Map<String,Serializable>>
    {
        private ContentReader contentReader;
        
        public ExtractRawCallable(ContentReader reader)
        {
            this.contentReader = reader;
        }
        
        @Override
        public Map<String, Serializable> call() throws Exception
        {
            try
            {
                return extractRaw(contentReader);
            }
            catch (Throwable e)
            {
                throw new ExtractRawCallableException(e);
            }
        }
    }
    
    /**
     * Exception wrapper to handle any {@link Throwable} from 
     * {@link AbstractMappingMetadataExtracter#extractRaw(ContentReader)}
     */
    private class ExtractRawCallableException extends Exception
    {
        private static final long serialVersionUID = 1813857091767321624L;
        public ExtractRawCallableException(Throwable cause)
        {
            super(cause);
        }
    }
    
    /**
     * Exception wrapper to handle exceeded limits imposed by {@link MetadataExtracterLimits}
     * {@link AbstractMappingMetadataExtracter#extractRaw(ContentReader, MetadataExtracterLimits)}
     */
    private class LimitExceededException extends Exception
    {
        private static final long serialVersionUID = 702554119174770130L;
        public LimitExceededException(String message)
        {
            super(message);
        }
    }
    
    /**
     * Calls the {@link AbstractMappingMetadataExtracter#extractRaw(ContentReader)} method
     * using the given limits.
     * <p>
     * Currently the only limit supported by {@link MetadataExtracterLimits} is a timeout
     * so this method uses {@link AbstractMappingMetadataExtracter#getExecutorService()}
     * to execute a {@link FutureTask} with any timeout defined.
     * <p>
     * If no timeout limit is defined or is unlimited (-1),
     * the <code>extractRaw</code> method is called directly.
     * 
     * @param reader        the document to extract the values from.  This stream provided by
     *                      the reader must be closed if accessed directly.
     * @param limits        the limits to impose on the extraction
     * @return              Returns a map of document property values keyed by property name.
     * @throws Throwable    All exception conditions can be handled.
     */
    private Map<String, Serializable> extractRaw(
            ContentReader reader, MetadataExtracterLimits limits) throws Throwable
    {
        FutureTask<Map<String, Serializable>> task = null;
        StreamAwareContentReaderProxy proxiedReader = null;
        
        if (reader.getSize() > limits.getMaxDocumentSizeMB() * MEGABYTE_SIZE)
        {
            throw new LimitExceededException("Max doc size exceeded " + limits.getMaxDocumentSizeMB() + " MB");
        }
        
        synchronized (CONCURRENT_EXTRACTIONS_COUNT)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Concurrent extractions : " + CONCURRENT_EXTRACTIONS_COUNT.get());
            }
            if (CONCURRENT_EXTRACTIONS_COUNT.get() < limits.getMaxConcurrentExtractionsCount())
            {
                int totalDocCount = CONCURRENT_EXTRACTIONS_COUNT.incrementAndGet();
                if (logger.isDebugEnabled())
                {
                    logger.debug("New extraction accepted. Concurrent extractions : " + totalDocCount);
                }
            }
            else
            {
                throw new LimitExceededException("Reached concurrent extractions limit - " + limits.getMaxConcurrentExtractionsCount());
            }
        }
        
        try
        {
            proxiedReader = new StreamAwareContentReaderProxy(reader);
            task = new FutureTask<Map<String,Serializable>>(new ExtractRawCallable(proxiedReader));
            getExecutorService().execute(task);
            return task.get(limits.getTimeoutMs(), TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException e)
        {
            task.cancel(true);
            if (null != proxiedReader)
            {
                proxiedReader.release();
            }
            throw e;
        }
        catch (InterruptedException e)
        {
            // We were asked to stop
            task.cancel(true);
            return null;
        }
        catch (ExecutionException e)
        {
            // Unwrap our cause and throw that
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof ExtractRawCallableException)
            {
                cause = ((ExtractRawCallableException) cause).getCause();
            }
            throw cause;
        }
        finally
        {
            int totalDocCount = CONCURRENT_EXTRACTIONS_COUNT.decrementAndGet();
            if (logger.isDebugEnabled())
            {
                logger.debug("Extraction finalized. Remaining concurrent extraction : " + totalDocCount);
            }
        }
    }
    
    /**
     * Override to provide the raw extracted metadata values.  An extracter should extract
     * as many of the available properties as is realistically possible.  Even if the
     * {@link #getDefaultMapping() default mapping} doesn't handle all properties, it is
     * possible for each instance of the extracter to be configured differently and more or
     * less of the properties may be used in different installations.
     * <p>
     * Raw values must not be trimmed or removed for any reason.  Null values and empty
     * strings are 
     * <ul>
     *    <li><b>Null:</b>              Removed</li>
     *    <li><b>Empty String:</b>      Passed to the OverwritePolicy</li>
     *    <li><b>Non Serializable:</b>  Converted to String or fails if that is not possible</li>
     * </ul>
     * <p>
     * Properties extracted and their meanings and types should be thoroughly described in
     * the class-level javadocs of the extracter implementation, for example:
     * <pre>
     * <b>editor:</b> - the document editor        -->  cm:author
     * <b>title:</b>  - the document title         -->  cm:title
     * <b>user1:</b>  - the document summary
     * <b>user2:</b>  - the document description   -->  cm:description
     * <b>user3:</b>  -
     * <b>user4:</b>  -
     * </pre>
     * 
     * @param reader        the document to extract the values from.  This stream provided by
     *                      the reader must be closed if accessed directly.
     * @return              Returns a map of document property values keyed by property name.
     * @throws Throwable    All exception conditions can be handled.
     * 
     * @see #getDefaultMapping()
     */
    protected abstract Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable;

    /**
     * Override to embed metadata values.  An extracter should embed
     * as many of the available properties as is realistically possible.  Even if the
     * {@link #getDefaultEmbedMapping() default mapping} doesn't handle all properties, it is
     * possible for each instance of the extracter to be configured differently and more or
     * less of the properties may be used in different installations.
     *
     * @param metadata		the metadata keys and values to embed in the content file
     * @param reader		the reader for the original document.  This stream provided by
     *                      the reader must be closed if accessed directly.
     * @param writer        the writer for the document to embed the values in.  This stream provided by
     *                      the writer must be closed if accessed directly.
     * @throws Throwable    All exception conditions can be handled.
     *
     * @see #getDefaultEmbedMapping()
     */
    protected void embedInternal(Map<String, Serializable> metadata, ContentReader reader, ContentWriter writer) throws Throwable
    {
        // TODO make this an abstract method once more extracters support embedding
    }
}
