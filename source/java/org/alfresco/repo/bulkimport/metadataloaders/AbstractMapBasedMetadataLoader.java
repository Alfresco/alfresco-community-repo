/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.bulkimport.metadataloaders;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.alfresco.repo.bulkimport.ImportableItem.ContentAndMetadata;
import org.alfresco.repo.bulkimport.MetadataLoader;
import org.alfresco.repo.bulkimport.impl.FileUtils;
import org.alfresco.service.ServiceRegistry;
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
     * @see org.alfresco.extension.bulkfilesystemimport.MetadataLoader#getMetadataFileExtension()
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
    abstract protected Map<String,Serializable> loadMetadataFromFile(final File metadataFile);


    /**
     * @see org.alfresco.extension.bulkfilesystemimport.MetadataLoader#loadMetadata(org.alfresco.extension.bulkfilesystemimport.ImportableItem.ContentAndMetadata, org.alfresco.extension.bulkfilesystemimport.MetadataLoader.Metadata)
     */
    @Override
    public final void loadMetadata(final ContentAndMetadata contentAndMetadata, Metadata metadata)
    {
        if (contentAndMetadata.metadataFileExists())
        {
            final File metadataFile = contentAndMetadata.getMetadataFile();

            if (metadataFile.canRead())
            {
                Map<String,Serializable> metadataProperties = loadMetadataFromFile(metadataFile);
                
                for (String key : metadataProperties.keySet())
                {
                    if (PROPERTY_NAME_TYPE.equals(key))
                    {
                        String typeName = (String)metadataProperties.get(key);
                        QName  type     = QName.createQName(typeName, namespaceService);
                        
                        metadata.setType(type);
                    }
                    else if (PROPERTY_NAME_ASPECTS.equals(key))
                    {
                        String[] aspectNames = ((String)metadataProperties.get(key)).split(",");
                        
                        for (final String aspectName : aspectNames)
                        {
                            QName aspect = QName.createQName(aspectName.trim(), namespaceService);
                            metadata.addAspect(aspect);
                        }
                    }
                    else  // Any other key => property
                    {
                        //####TODO: figure out how to handle properties of type cm:content - they need to be streamed in via a Writer 
                    	QName              name               = QName.createQName(key, namespaceService);
                    	PropertyDefinition propertyDefinition = dictionaryService.getProperty(name);  // TODO: measure performance impact of this API call!!
                    	
                    	if (propertyDefinition != null)
                    	{
                        	if (propertyDefinition.isMultiValued())
                        	{
                                // Multi-valued property
                        		ArrayList<Serializable> values = new ArrayList<Serializable>(Arrays.asList(((String)metadataProperties.get(key)).split(multiValuedSeparator)));
                        	    metadata.addProperty(name, values);
                        	}
                        	else
                        	{
                        	    // Single value property
                        		metadata.addProperty(name, metadataProperties.get(key));
                        	}
                    	}
                    	else
                    	{
                    	    if (log.isWarnEnabled()) log.warn("Property " + String.valueOf(name) + " doesn't exist in the Data Dictionary.  Ignoring it.");
                    	}
                    }
                }
            }
            else
            {
                if (log.isWarnEnabled()) log.warn("Metadata file '" + FileUtils.getFileName(metadataFile) + "' is not readable.");
            }
        }
    }

}
