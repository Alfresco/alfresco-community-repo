/*
 * Copyright (C) 2005 Jesper Steen Møller
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.content.ContentWorker;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Interface for document property extracters.
 * <p>
 * Please pardon the incorrect spelling of <i>extractor</i>.
 * 
 * @author Jesper Steen Møller
 * @author Derek Hulley
 */
public interface MetadataExtracter extends ContentWorker
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
         * <tt>null</tt> extracted values are return in the 'modified' map.
         */
        EAGER
        {
            @Override
            public Map<QName, Serializable> applyProperties(Map<QName, Serializable> extractedProperties, Map<QName, Serializable> targetProperties)
            {
                Map<QName, Serializable> modifiedProperties = new HashMap<QName, Serializable>(7);
                for (Map.Entry<QName, Serializable> entry : extractedProperties.entrySet())
                {
                    QName propertyQName = entry.getKey();
                    Serializable extractedValue = entry.getValue();
                    // Ignore null extracted value
                    if (extractedValue != null)
                    {
                        targetProperties.put(propertyQName, extractedValue);
                    }
                    modifiedProperties.put(propertyQName, extractedValue);
                }
                return modifiedProperties;
            }
        },
        
        /**
         * This policy puts the new value if:
         * <ul>
         *   <li>the extracted property is not null</li>
         *   <li>either:
         *     <ul>
         *      <li>there is no target key for the property</li>
         *      <li>the target value is null</li>
         *      <li>the string representation of the target value is an empty string</li>
         *     </ul>
         *     or:
         *     <ul>
         *       <li>the extracted property is a media related one (eg Image, Audio or Video)</li>
         *     </ul>
         *   </li>
         * </ul>
         * <tt>null</tt> extracted values are return in the 'modified' map.
         */
        PRAGMATIC
        {
            @Override
            public Map<QName, Serializable> applyProperties(Map<QName, Serializable> extractedProperties, Map<QName, Serializable> targetProperties)
            {
                /*
                 * Negative and positive checks are mixed in the loop.
                 */
                Map<QName, Serializable> modifiedProperties = new HashMap<QName, Serializable>(7);
                for (Map.Entry<QName, Serializable> entry : extractedProperties.entrySet())
                {
                    QName propertyQName = entry.getKey();
                    Serializable extractedValue = entry.getValue();
                    
                    // Ignore null extracted value
                    if (extractedValue == null)
                    {
                        modifiedProperties.put(propertyQName, extractedValue);
                        continue;
                    }
                    
                    // If the property is media related, always extract
                    String propertyNS = propertyQName.getNamespaceURI();
                    if(propertyNS.equals(NamespaceService.EXIF_MODEL_1_0_URI) ||
                       propertyNS.equals(NamespaceService.AUDIO_MODEL_1_0_URI))
                    {
                       targetProperties.put(propertyQName, extractedValue);
                       modifiedProperties.put(propertyQName, extractedValue);
                       continue;
                    }
                    
                    // Handle the shortcut cases where the target value is missing or null
                    if (!targetProperties.containsKey(propertyQName))
                    {
                        // There is nothing currently
                        targetProperties.put(propertyQName, extractedValue);
                        modifiedProperties.put(propertyQName, extractedValue);
                        continue;
                    }
                    
                    // Look at the old value, and decide based on that
                    Serializable originalValue = targetProperties.get(propertyQName);
                    if (originalValue == null)
                    {
                        // The previous value is null, extract
                        targetProperties.put(propertyQName, extractedValue);
                        modifiedProperties.put(propertyQName, extractedValue);
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
                            modifiedProperties.put(propertyQName, extractedValue);
                            continue;
                        }
                    }
                    
                    // We have some other object as the original value, so keep it
                }
                return modifiedProperties;
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
         * <tt>null</tt> extracted values are return in the 'modified' map.
         */
        PRUDENT
        {
           @Override
           public Map<QName, Serializable> applyProperties(Map<QName, Serializable> extractedProperties, Map<QName, Serializable> targetProperties)
           {
               /*
                * Negative and positive checks are mixed in the loop.
                */
               Map<QName, Serializable> modifiedProperties = new HashMap<QName, Serializable>(7);
               for (Map.Entry<QName, Serializable> entry : extractedProperties.entrySet())
               {
                   QName propertyQName = entry.getKey();
                   Serializable extractedValue = entry.getValue();
                   // Ignore null extracted value
                   if (extractedValue == null)
                   {
                       modifiedProperties.put(propertyQName, extractedValue);
                       continue;
                   }
                   // Handle the shortcut cases where the target value is missing or null
                   if (!targetProperties.containsKey(propertyQName))
                   {
                       // There is nothing currently
                       targetProperties.put(propertyQName, extractedValue);
                       modifiedProperties.put(propertyQName, extractedValue);
                       continue;
                   }
                   Serializable originalValue = targetProperties.get(propertyQName);
                   if (originalValue == null)
                   {
                       // The current value is null
                       targetProperties.put(propertyQName, extractedValue);
                       modifiedProperties.put(propertyQName, extractedValue);
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
                           modifiedProperties.put(propertyQName, extractedValue);
                           continue;
                       }
                   }
                   // We have some other object as the original value, so keep it
               }
               return modifiedProperties;
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
         * <tt>null</tt> extracted values are return in the 'modified' map.
         */
        CAUTIOUS
        {
            @Override
            public Map<QName, Serializable> applyProperties(Map<QName, Serializable> extractedProperties, Map<QName, Serializable> targetProperties)
            {
                Map<QName, Serializable> modifiedProperties = new HashMap<QName, Serializable>(7);
                for (Map.Entry<QName, Serializable> entry : extractedProperties.entrySet())
                {
                    QName propertyQName = entry.getKey();
                    Serializable extractedValue = entry.getValue();
                    // Ignore null extracted value
                    if (extractedValue == null)
                    {
                        modifiedProperties.put(propertyQName, extractedValue);
                        continue;
                    }
                    // Is the key present in the target values
                    if (targetProperties.containsKey(propertyQName))
                    {
                        // Cautiously bypass the value as there is one already
                        continue;
                    }
                    targetProperties.put(propertyQName, extractedValue);
                    modifiedProperties.put(propertyQName, extractedValue);
                }
                return modifiedProperties;
            }
        };

        /**
         * Apply the overwrite policy for the extracted properties.
         * 
         * @return
         *          Returns a map of all properties that were applied to the target map
         *          as well as any null values that weren't applied but were present.
         */
        public Map<QName, Serializable> applyProperties(Map<QName, Serializable> extractedProperties, Map<QName, Serializable> targetProperties)
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
     * 
     * @deprecated          Generally not useful or used.  Extraction is normally specifically configured.
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
     * @return                      Returns a map of all properties on the destination map that were
     *                              added or modified.  If the return map is empty, then no properties
     *                              were modified.
     * @throws ContentIOException   if a detectable error occurs
     * 
     * @see #extract(ContentReader, OverwritePolicy, Map, Map)
     */
    public Map<QName, Serializable> extract(ContentReader reader, Map<QName, Serializable> destination);

    /**
     * Extracts the metadata values from the content provided by the reader and source
     * mimetype to the supplied map.
     * <p>
     * The extraction viability can be determined by an up front call to {@link #isSupported(String)}.
     * <p>
     * The source mimetype <b>must</b> be available on the
     * {@link org.alfresco.service.cmr.repository.ContentAccessor#getMimetype()} method
     * of the reader.
     * 
     * @param reader                the source of the content
     * @param overwritePolicy       the policy stipulating how the system properties must be
     *                              overwritten if present
     * @param destination           the map of properties to populate (essentially a return value)
     * @return                      Returns a map of all properties on the destination map that were
     *                              added or modified.  If the return map is empty, then no properties
     *                              were modified.
     * @throws ContentIOException   if a detectable error occurs
     * 
     * @see #extract(ContentReader, OverwritePolicy, Map, Map)
     */
    public Map<QName, Serializable> extract(
            ContentReader reader,
            OverwritePolicy overwritePolicy,
            Map<QName, Serializable> destination);

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
     * @return                      Returns a map of all properties on the destination map that were
     *                              added or modified.  If the return map is empty, then no properties
     *                              were modified.
     * @throws ContentIOException   if a detectable error occurs
     * 
     * @see #extract(ContentReader, Map)
     */
    public Map<QName, Serializable> extract(
            ContentReader reader,
            OverwritePolicy overwritePolicy,
            Map<QName, Serializable> destination,
            Map<String, Set<QName>> mapping);
}
