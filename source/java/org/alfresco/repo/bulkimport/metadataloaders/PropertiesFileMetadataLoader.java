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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.alfresco.repo.bulkimport.MetadataLoader;
import org.alfresco.repo.bulkimport.impl.FileUtils;
import org.alfresco.service.ServiceRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * MetadataLoader that loads metadata from an (optional) "shadow" properties
 * file.  This shadow properties file must have <strong>exactly</strong> the
 * same name and extension as the file for whom it is storing metadata, but
 * with the suffix ".metadata.properties".  So for example, if there is a file
 * called "IMG_1967.jpg", the "shadow" properties metadata file for it would
 * be called "IMG_1967.jpg.metadata.properties". 
 * 
 * The metadata file itself follows the usual rules for Java properties files,
 * with a property with the key "type" containing the qualified name of the
 * content type to use for the file, a property with the key "aspects"
 * containing a comma-delimited list of qualified names of the aspects to
 * attach to the file, and then one Java property per metadata property, with
 * the key being the Alfresco property QName and the value being the value of
 * that property.
 * 
 * For example (note escaping rules for namespace separator!):
 * 
 * <code>
 *   type=cm:content
 *   aspects=cm:versionable, custom:myAspect
 *   cm\:title=This is the value of the cm:title field.
 *   cm\:description=This is the value of the cm:description field.
 *   cm\:taggable=workspace://SpacesStore/3da6c395-3a4b-4a57-836d-8e5
 *   custom\:myProperty=This is the value of the custom:myProperty field.
 *   custom\:aDateProperty=2001-01-01T12:00:00.000+01:00
 * </code>
 * 
 * Notes:
 * <ul>
 *   <li>Java properties files do not support Unicode characters - all values
 *       are loaded assuming an ISO-8859-1 character set.  For Unicode
 *       metadata, you should use <code>XmlPropertiesFileMetadataLoader</code>
 *       instead.</li>
 *   <li>the metadata must conform to the type and aspect definitions
 *       configured in Alfresco (including mandatory fields, constraints and data
 *       types).  Any violations will terminate the bulk import process.</li>
 *   <li>associations are not yet supported</li>
 *   <li>dates, times and date times <u>must</u> be stored in ISO8601 format
 *       (although note that Alfresco ignores timezone modifiers)</li>
 * </ul>
 *
 * @since 4.0
 * 
 * @see MetadataLoader
 */
public final class PropertiesFileMetadataLoader
extends AbstractMapBasedMetadataLoader
{
    private final static Log log = LogFactory.getLog(PropertiesFileMetadataLoader.class);
    
    private final static String METADATA_FILE_EXTENSION = "properties.xml";

    
    public PropertiesFileMetadataLoader(final ServiceRegistry serviceRegistry)
    {
        super(serviceRegistry, METADATA_FILE_EXTENSION);
    }
    
    
    public PropertiesFileMetadataLoader(final ServiceRegistry serviceRegistry, final String multiValuedSeparator)
    {
        super(serviceRegistry, multiValuedSeparator, METADATA_FILE_EXTENSION);
    }

    
    /**
     * @see org.alfresco.extension.bulkfilesystemimport.metadataloaders.AbstractMapBasedMetadataLoader#loadMetadataFromFile(java.io.File)
     */
    @Override
    protected Map<String,Serializable> loadMetadataFromFile(File metadataFile)
    {
        Map<String,Serializable> result = null;
        
        try
        {
            Properties props = new Properties();
            props.load(new BufferedInputStream(new FileInputStream(metadataFile)));
            result = new HashMap<String,Serializable>((Map)props);
        }
        catch (final IOException ioe)
        {
            if (log.isWarnEnabled()) log.warn("Metadata file '" + FileUtils.getFileName(metadataFile) + "' could not be read.", ioe);
        }
        
        return(result);
    }

}
