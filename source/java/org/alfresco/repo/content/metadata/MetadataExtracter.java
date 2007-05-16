/*
 * Copyright (C) 2005 Jesper Steen Møller
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

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;

/**
 * Interface for document property extracters.
 * 
 * @author Jesper Steen Møller
 * @author Derek Hulley
 */
public interface MetadataExtracter
{
    /**
     * A enumeration of functional property overwrite policies.  These determine whether extracted properties are
     * written into the property map or not.
     * 
     * @author Derek Hulley
     * @author Jesper Steen Møller
     */
    public enum OverwritePolicy
    {
        /**
         * This policy puts the new value if:
         * <ul>
         *   <li>the extracted property is not null</li>
         * </ul>
         */
        EAGER
        {
            @Override
            public boolean applyProperties(Map<QName, Serializable> extractedProperties, Map<QName, Serializable> targetProperties)
            {
                boolean modified = false;
                for (Map.Entry<QName, Serializable> entry : extractedProperties.entrySet())
                {
                    QName propertyQName = entry.getKey();
                    Serializable extractedValue = entry.getValue();
                    // Ignore null extracted value
                    if (extractedValue == null)
                    {
                        continue;
                    }
                    targetProperties.put(propertyQName, extractedValue);
                    modified = true;
                }
                return modified;
            }
        },
        /**
         * This policy puts the new value if:
         * <ul>
         *   <li>the extracted property is not null</li>
         *   <li>there is no target key for the property</li>
         *   <li>the target value is null</li>
         *   <li>the string representation of the target value is an empty string</li>
         * </ul>
         */
        PRAGMATIC
        {
            @Override
            public boolean applyProperties(Map<QName, Serializable> extractedProperties, Map<QName, Serializable> targetProperties)
            {
                /*
                 * Negative and positive checks are mixed in the loop.
                 */
                boolean modified = false;
                for (Map.Entry<QName, Serializable> entry : extractedProperties.entrySet())
                {
                    QName propertyQName = entry.getKey();
                    Serializable extractedValue = entry.getValue();
                    // Ignore null extracted value
                    if (extractedValue == null)
                    {
                        continue;
                    }
                    // Handle the shortcut cases where the target value is missing or null
                    if (!targetProperties.containsKey(propertyQName))
                    {
                        // There is nothing currently
                        targetProperties.put(propertyQName, extractedValue);
                        modified = true;
                        continue;
                    }
                    Serializable originalValue = targetProperties.get(propertyQName);
                    if (originalValue == null)
                    {
                        // The current value is null
                        targetProperties.put(propertyQName, extractedValue);
                        modified = true;
                        continue;
                    }
                    // Check the string representation
                    if (originalValue instanceof String)
                    {
                        String originalValueStr = (String) originalValue;
                        if (originalValueStr != null && originalValueStr.length() > 0)
                        {
                            // The original value is non-trivial
                            continue;
                        }
                        else
                        {
                            // The original string is trivial
                            targetProperties.put(propertyQName, extractedValue);
                            modified = true;
                            continue;
                        }
                    }
                    // We have some other object as the original value, so keep it
                }
                return modified;
            }
        },
        /**
         * This policy only puts the extracted value if there is no value (null or otherwise) in the properties map.
         * It is assumed that the mere presence of a property key is enough to inidicate that the target property
         * is as intented.
         * This policy puts the new value if:
         * <ul>
         *   <li>the extracted property is not null</li>
         *   <li>there is no target key for the property</li>
         * </ul>
         */
        CAUTIOUS
        {
            @Override
            public boolean applyProperties(Map<QName, Serializable> extractedProperties, Map<QName, Serializable> targetProperties)
            {
                boolean modified = false;
                for (Map.Entry<QName, Serializable> entry : extractedProperties.entrySet())
                {
                    QName propertyQName = entry.getKey();
                    Serializable extractedValue = entry.getValue();
                    // Ignore null extracted value
                    if (extractedValue == null)
                    {
                        continue;
                    }
                    // Is the key present in the target values
                    if (targetProperties.containsKey(propertyQName))
                    {
                        // Cautiously bypass the value as there is one already
                        continue;
                    }
                    targetProperties.put(propertyQName, extractedValue);
                    modified = true;
                }
                return modified;
            }
        };

        /**
         * Apply the overwrite policy for the extracted properties.
         * 
         * @return Returns true if <i>any</i> properties were set on the target properties
         */
        public boolean applyProperties(Map<QName, Serializable> extractedProperties, Map<QName, Serializable> targetProperties)
        {
            throw new UnsupportedOperationException("Override this method");
        }
    };
    
    /**
     * Get an estimate of the extracter's reliability on a scale from 0.0 to 1.0.
     * 
     * @param mimetype      the mimetype to check
     * @return              Returns a reliability indicator from 0.0 to 1.0
     * 
     * @deprecated  This method is replaced by {@link #isSupported(String)}
     */
    public double getReliability(String mimetype);
    
    /**
     * Determines if the extracter works against the given mimetype.
     * 
     * @param mimetype      the document mimetype
     * @return Returns      <tt>true</tt> if the mimetype is supported, otherwise <tt>false</tt>.
     */
    public boolean isSupported(String mimetype);

    /**
     * Provides an estimate, usually a worst case guess, of how long an
     * extraction will take.
     * <p>
     * This method is used to determine, up front, which of a set of equally
     * reliant transformers will be used for a specific extraction.
     * 
     * @return Returns the approximate number of milliseconds per transformation
     */
    public long getExtractionTime();

    /**
     * Extracts the metadata values from the content provided by the reader and source
     * mimetype to the supplied map.  The internal mapping and {@link OverwritePolicy overwrite policy}
     * between document metadata and system metadata will be used.
     * <p>
     * The extraction viability can be determined by an up front call to {@link #isSupported(String)}.
     * <p>
     * The source mimetype <b>must</b> be available on the
     * {@link org.alfresco.service.cmr.repository.ContentAccessor#getMimetype()} method
     * of the reader.
     * 
     * @param reader                the source of the content
     * @param destination           the map of properties to populate (essentially a return value)
     * @return                      Returns <tt>true</tt> if the destination map was modified
     * @throws ContentIOException   if a detectable error occurs
     * 
     * @see #extract(ContentReader, OverwritePolicy, Map, Map)
     */
    public boolean extract(ContentReader reader, Map<QName, Serializable> destination) throws ContentIOException;

    /**
     * Extracts the metadata from the content provided by the reader and source
     * mimetype to the supplied map.  The mapping from document metadata to system metadata
     * is explicitly provided.  The {@link OverwritePolicy overwrite policy} is also explictly
     * set.
     * <p>
     * The extraction viability can be determined by an up front call to
     * {@link #isSupported(String)}.
     * <p>
     * The source mimetype <b>must</b> be available on the
     * {@link org.alfresco.service.cmr.repository.ContentAccessor#getMimetype()} method
     * of the reader.
     * 
     * @param reader                the source of the content
     * @param overwritePolicy       the policy stipulating how the system properties must be
     *                              overwritten if present
     * @param destination           the map of properties to populate (essentially a return value)
     * @param mapping               a mapping of document-specific properties to system properties.
     * @return                      Returns <tt>true</tt> if the destination map was modified
     * @throws ContentIOException   if a detectable error occurs
     * 
     * @see #extract(ContentReader, Map)
     */
    public boolean extract(
            ContentReader reader,
            OverwritePolicy overwritePolicy,
            Map<QName, Serializable> destination,
            Map<String, Set<QName>> mapping) throws ContentIOException;
}
