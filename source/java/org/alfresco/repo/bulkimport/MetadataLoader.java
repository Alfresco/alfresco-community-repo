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
package org.alfresco.repo.bulkimport;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.namespace.QName;

/**
 * Definition of a metadata loader - a class that can load metadata for a file from some other source.
 * Note that metadata loaders can be "chained", so an implementation needs to be careful about how the
 * Metadata object is populated in the populateMetadata method.
 * 
 * Implementors also need to be careful when configuring the bulk import process, as the order in which
 * metadata loaders are configured into a bulk importer is the order of precendence (from lowest to
 * highest).
 *
 * @since 4.0
 */
public interface MetadataLoader
{
    /**
     * Metadata filename suffix (excluding file-type specific ending)
     */
    public final static String METADATA_SUFFIX = ".metadata.";
    
    /**
     * @return The extension for files used to store this metadata, minus the stop character (.) e.g. "properties", "xml", "json", etc.
     */
    String getMetadataFileExtension();
        

    /**
     * Method that populates the type, aspects and properties to attach to a given file or space.
     * 
     * @param contentAndMetadata The contentAndMetadata from which to obtain the metadata <i>(will not be null)</i>.
     * @param metadata           The metadata object to populate <i>(will not be null, and may already be partially populated)</i>.
     */
    void loadMetadata(final ImportableItem.ContentAndMetadata contentAndMetadata, MetadataLoader.Metadata metadata);
    

    /**
     * Class used to encapsulate the type, aspects and property values for a single file or folder.
     */
    public final class Metadata
    {
        private QName                    type;
        private Set<QName>               aspects;
        private Map<QName, Serializable> properties;
        
        
        public Metadata()
        {
            this.type  = null;
            aspects    = new HashSet<QName>();
            properties = new HashMap<QName, Serializable>(); 
        }
        

        /**
         * @return the type
         */
        public QName getType()
        {
            return(type);
        }
        

        /**
         * @param type The type to set in this metadata object <i>(must not be null)</i>.
         */
        public void setType(final QName type)
        {
            // PRECONDITIONS
            assert type != null : "type must not be null.";
            
            // Body
            this.type = type;
        }

        
        /**
         * @return The set of aspects in this metadata object <i>(will not be null, but may be empty)</i>.
         */
        public Set<QName> getAspects()
        {
            return(Collections.unmodifiableSet(aspects));
        }
        
        
        /**
         * @param aspect An aspect to add to this metadata object <i>(must not be null)</i>.
         */
        public void addAspect(final QName aspect)
        {
            // PRECONDITIONS
            assert aspect != null : "aspect must not be null.";
            
            // Body
            aspects.add(aspect);
        }
        

        /**
         * @return The properties in this metadata object <i>(will not be null, but may be empty)</i>.
         */
        public Map<QName, Serializable> getProperties()
        {
            return(Collections.unmodifiableMap(properties));
        }
        
        
        /**
         * Adds a property and its value to this metadata object. 
         * 
         * @param property The property to populate <i>(must not be null)</i>.
         * @param value    The value of the property <i>(may be null)</i>.
         */
        public void addProperty(final QName property, final Serializable value)
        {
            // PRECONDITIONS
            assert property != null : "property must not be null";
            
            // Body
            properties.put(property, value);
        }
        
    }
    
    
}
