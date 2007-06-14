/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content.metadata;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 *   Implement the {@link extractInternal} method.  This now returns a raw map of extracted
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
abstract public class AbstractMappingMetadataExtracter implements MetadataExtracter
{
    public static final String NAMESPACE_PROPERTY_PREFIX = "namespace.prefix.";
    
    protected static Log logger = LogFactory.getLog(AbstractMappingMetadataExtracter.class);
    
    private MetadataExtracterRegistry registry;
    private MimetypeService mimetypeService;
    private long extractionTime;
    private boolean initialized;
    
    private Set<String> supportedMimetypes;
    private OverwritePolicy overwritePolicy;
    private Map<String, Set<QName>> mapping;
    private boolean inheritDefaultMapping;

    protected AbstractMappingMetadataExtracter()
    {
        this(Collections.<String>emptySet());
    }

    /**
     * @param supportedMimetypes    the set of mimetypes supported by default
     */
    protected AbstractMappingMetadataExtracter(Set<String> supportedMimetypes)
    {
        this.supportedMimetypes = supportedMimetypes;
        // Set defaults
        overwritePolicy = OverwritePolicy.PRAGMATIC;
        mapping = null;                     // The default will be fetched
        inheritDefaultMapping = false;      // Any overrides are complete 
        initialized = false;
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
     * Set the mimetypes that are supported by the extracter.
     * 
     * @param supportedMimetypes
     */
    public void setSupportedMimetypes(Collection<String> supportedMimetypes)
    {
        this.supportedMimetypes.clear();
        this.supportedMimetypes.addAll(supportedMimetypes);
    }
    
    /**
     * {@inheritDoc}
     * 
     * @see #setSupportedMimetypes(Collection)
     */
    public boolean isSupported(String sourceMimetype)
    {
        return supportedMimetypes.contains(sourceMimetype);
    }
    
    /**
     * @return      Returns <code>1.0</code> if the mimetype is supported, otherwise <tt>0.0</tt>
     * 
     * @see #isSupported(String)
     */
    public double getReliability(String mimetype)
    {
        return isSupported(mimetype) ? 1.0D : 0.0D;
    }

    /**
     * @param overwritePolicy   the policy to apply when there are existing system properties
     */
    public void setOverwritePolicy(OverwritePolicy overwritePolicy)
    {
        this.overwritePolicy = overwritePolicy;
    }

    /**
     * Set if the property mappings augment or override the mapping generically provided by the
     * extracter implementation.  The default is <tt>false</tt>, i.e. any mapping set completely
     * replaces the {@link #getDefaultMapping() default mappings}.
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
     * A utility method to convert mapping properties to the Map form.
     * 
     * @see #setMappingProperties(Properties)
     */
    protected Map<String, Set<QName>> readMappingProperties(Properties mappingProperties)
    {
        Map<String, String> namespacesByPrefix = new HashMap<String, String>(5);
        // Get the namespaces
        for (Map.Entry entry : mappingProperties.entrySet())
        {
            String propertyName = (String) entry.getKey();
            if (propertyName.startsWith("namespace.prefix."))
            {
                String prefix = propertyName.substring(17);
                String namespace = (String) entry.getValue();
                namespacesByPrefix.put(prefix, namespace);
            }
        }
        // Create the mapping
        Map<String, Set<QName>> convertedMapping = new HashMap<String, Set<QName>>(17);
        for (Map.Entry entry : mappingProperties.entrySet())
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
                int index = qnameStr.indexOf(QName.NAMESPACE_PREFIX);
                if (index > -1)
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
            if (logger.isDebugEnabled())
            {
                logger.debug("Added mapping from " + documentProperty + " to " + qnames);
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
        else
        {
            logger.warn("No registry provided.  Not registering: " + this);
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
                }
                Set<QName> defaultQNames = defaultMapping.get(documentKey);
                systemQNames.addAll(defaultQNames);
            }
        }
        
        // Were there any mappings
        if (mapping.size() == 0)
        {
            logger.warn(
                    "There are no property mappings for the metadata extracter.\n" +
                    "  Nothing will be extracted by: " + this);
        }
        // Done
        initialized = true;
    }

    /** {@inheritDoc} */
    public long getExtractionTime()
    {
        return extractionTime;
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
                    "Metadata extracter does not support mimetype: \n" +
                    "   reader: " + reader + "\n" +
                    "   supported: " + supportedMimetypes + "\n" +
                    "   extracter: " + this);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final boolean extract(ContentReader reader, Map<QName, Serializable> destination) throws ContentIOException
    {
        return extract(reader, this.overwritePolicy, destination, this.mapping);
    }

    /**
     * {@inheritDoc}
     */
    public final boolean extract(
            ContentReader reader,
            OverwritePolicy overwritePolicy,
            Map<QName, Serializable> destination,
            Map<String, Set<QName>> mapping) throws ContentIOException
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
        
        boolean changed = false;
        try
        {
            Map<String, Serializable> rawMetadata = extractRaw(reader);
            // Convert to system properties (standalone)
            Map<QName, Serializable> systemProperties = mapRawToSystem(rawMetadata);
            // Now use the proper overwrite policy
            changed = overwritePolicy.applyProperties(systemProperties, destination);
        }
        catch (Throwable e)
        {
            throw new ContentIOException("Metadata extraction failed: \n" +
                    "   reader: " + reader,
                    e);
        }
        finally
        {
            // check that the reader was closed
            if (!reader.isClosed())
            {
                logger.error("Content reader not closed by metadata extracter: \n" +
                        "   reader: " + reader + "\n" +
                        "   extracter: " + this);
            }
        }
        
        // Done
        if (logger.isDebugEnabled())
        {
            logger.debug("Completed metadata extraction: \n" +
                    "   reader:    " + reader + "\n" +
                    "   extracter: " + this + "\n" +
                    "   changed:   " + changed);
        }
        return changed;
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
     * Adds a value to the map if it is non-trivial.  A value is trivial if
     * <ul>
     *   <li>it is null</li>
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
            return false;
        }
        if (value instanceof String)
        {
            String valueStr = ((String) value).trim();
            if (valueStr.length() == 0)
            {
                return false;
            }
            else
            {
                // Keep the trimmed value
                value = valueStr;
            }
        }
        else if (value instanceof Collection)
        {
            Collection valueCollection = (Collection) value;
            if (valueCollection.isEmpty())
            {
                return false;
            }
        }
        else if (value.getClass().isArray())
        {
            if (Array.getLength(value) == 0)
            {
                return false;
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
     * <b>classpath:/x/y/z/MyExtracter.properties</b>.
     * Inner classes are supported, but the '$' in the class name is replaced with '-', so
     * default properties for <b>x.y.z.MyStuff$MyExtracter</b> will be located using
     * <b>x.y.z.MyStuff-MyExtracter.properties</b>.
     * <p>
     * The default mapping implementation should include thorough Javadocs so that the
     * system administrators can accurately determine how to best enhance or override the
     * default mapping.
     * <p>
     * If the default mapping is declared in a properties file, then the
     * {@link #readMappingProperties(String)} method can be used to quickly generate the
     * return value:
     * <pre>
     *      protected Map<String, Set<QName>> getDefaultMapping()
     *      {
     *          return readMappingProperties(DEFAULT_MAPPING);
     *      }
     * </pre>
     * The map can also be created in code either statically or during the call.
     * 
     * @return              Returns the default, static mapping.  It may not be null.
     * 
     * @see #setInheritDefaultMapping(boolean inherit)
     */
    protected Map<String, Set<QName>> getDefaultMapping()
    {
        String className = this.getClass().getName();
        // Replace $
        className = className.replace('$', '-');
        // Replace .
        className = className.replace('.', '/');
        // Append .properties
        String propertiesUrl = className + ".properties";
        // Attempt to load the properties
        return readMappingProperties(propertiesUrl);
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
     *    <li><b>Empty String:</b>      Passed to the {@link OverwritePolicy}</li>
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
     * @throws              All exception conditions can be handled.
     * 
     * @see #getDefaultMapping()
     */
    protected abstract Map<String, Serializable> extractRaw(ContentReader reader) throws Throwable;
}
