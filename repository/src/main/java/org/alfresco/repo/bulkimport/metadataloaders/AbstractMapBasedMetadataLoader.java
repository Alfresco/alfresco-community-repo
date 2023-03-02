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
package org.alfresco.repo.bulkimport.metadataloaders;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.bulkimport.ImportableItem.ContentAndMetadata;
import org.alfresco.repo.bulkimport.MetadataLoader;
import org.alfresco.repo.bulkimport.impl.FileUtils;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract MetadataLoader abstracts out the common features of loading metadata
 * from a <code>java.util.Map</code>, regardless of where it came from.
 *
 * @since 4.0
 * 
 * @see MetadataLoader
 */
abstract class AbstractMapBasedMetadataLoader implements MetadataLoader
{
    private final static Log log = LogFactory.getLog(AbstractMapBasedMetadataLoader.class);
    
    private final static String PROPERTY_NAME_TYPE    = "type";
    private final static String PROPERTY_NAME_ASPECTS = "aspects";
    
    private final static String DEFAULT_MULTI_VALUED_SEPARATOR = ",";
    
    private final List<QName> TYPES_TO_HANDLE_EMPTY_VALUE = List.of(DataTypeDefinition.DATE, DataTypeDefinition.DATETIME,
            DataTypeDefinition.FLOAT, DataTypeDefinition.DOUBLE, DataTypeDefinition.INT, DataTypeDefinition.LONG);

    protected final NamespaceService  namespaceService;
    protected final DictionaryService dictionaryService; 
    protected final String            multiValuedSeparator;
    protected final String            metadataFileExtension;
    
    protected AbstractMapBasedMetadataLoader(final ServiceRegistry serviceRegistry, final String fileExtension)
    {
        this(serviceRegistry, DEFAULT_MULTI_VALUED_SEPARATOR, fileExtension);
    }
    
    protected AbstractMapBasedMetadataLoader(final ServiceRegistry serviceRegistry, final String multiValuedSeparator, final String fileExtension)
    {
        // PRECONDITIONS
        assert serviceRegistry      != null : "serviceRegistry must not be null";
        assert multiValuedSeparator != null : "multiValuedSeparator must not be null";
        
        // Body
        this.namespaceService      = serviceRegistry.getNamespaceService();
        this.dictionaryService     = serviceRegistry.getDictionaryService();
        this.multiValuedSeparator  = multiValuedSeparator;
        this.metadataFileExtension = fileExtension;
    }
    

    /**
     * @see org.alfresco.repo.bulkimport.MetadataLoader#getMetadataFileExtension()
     */
    @Override
    public final String getMetadataFileExtension()
    {
        return(metadataFileExtension);
    }
    
    
    /**
     * Method that actually loads the properties from the file. 
     * @param metadataFile The file to load the properties from <i>(must not be null)</i>.
     * @return A new <code>Properties</code> object loaded from that file.
     */
    abstract protected Map<String,Serializable> loadMetadataFromFile(final Path metadataFile);

    @Override
    public final void loadMetadata(final ContentAndMetadata contentAndMetadata, Metadata metadata)
    {
        if (contentAndMetadata.metadataFileExists())
        {
            final Path metadataFile = contentAndMetadata.getMetadataFile();
            String metadataFilePath = FileUtils.getFileName(metadataFile);
            try
            {
                loadMetadataInternal(metadata, metadataFile);
            }
            catch (Exception e)
            {
                log.error("Error encountered when reading metadata file '" + metadataFilePath + "'.");
                throw new RuntimeException("Exception from reading file: '" + metadataFilePath + "'.", e);
            }
        }
    }

    private void loadMetadataInternal(Metadata metadata, final Path metadataFile)
    {
        final String metadataFilePath = FileUtils.getFileName(metadataFile);
        if (Files.isReadable(metadataFile))
        {
            Map<String, Serializable> metadataProperties = loadMetadataFromFile(metadataFile);

            for (String key : metadataProperties.keySet())
            {
                if (PROPERTY_NAME_TYPE.equals(key))
                {
                    String typeName = (String) metadataProperties.get(key);
                    QName type = QName.createQName(typeName, namespaceService);

                    metadata.setType(type);
                }
                else if (PROPERTY_NAME_ASPECTS.equals(key))
                {
                    String[] aspectNames = ((String) metadataProperties.get(key)).split(",");

                    for (final String aspectName : aspectNames)
                    {
                        QName aspect = QName.createQName(aspectName.trim(), namespaceService);
                        metadata.addAspect(aspect);
                    }
                }
                else // Any other key => property
                {
                    // ####TODO: figure out how to handle properties of type cm:content - they need to be streamed in via a Writer
                    QName name = QName.createQName(key, namespaceService);
                    PropertyDefinition propertyDefinition = dictionaryService.getProperty(name);// TODO: measure performance impact of this API call!!

                    if (propertyDefinition != null)
                    {
                        if (propertyDefinition.isMultiValued())
                        {
                            // Multi-valued property
                            ArrayList<Serializable> values = new ArrayList<Serializable>(
                                    Arrays.asList(((String) metadataProperties.get(key)).split(multiValuedSeparator)));
                            metadata.addProperty(name, values);
                        }
                        else
                        {
                            // Single value property
                            metadata.addProperty(name, handleValue(propertyDefinition, metadataProperties.get(key)));
                        }
                    }
                    else
                    {
                        if (log.isWarnEnabled())
                        {
                            log.warn("Property " + String.valueOf(name) + " from '" + metadataFilePath
                                    + "' doesn't exist in the Data Dictionary.  Ignoring it.");
                        }
                    }
                }
            }
        }
        else
        {
            if (log.isWarnEnabled())
            {
                log.warn("Metadata file '" + metadataFilePath + "' is not readable.");
            }
        }
    }

    private Serializable handleValue(PropertyDefinition pd, Serializable value)
    {
        if (pd != null && TYPES_TO_HANDLE_EMPTY_VALUE.contains(pd.getDataType().getName()))
        {
            if (value != null && value.toString().trim().length() == 0)
            {
                value = null;
            }
        }

        return value;
    }

}
