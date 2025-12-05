/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.repo.nuxeo.converter;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts between Alfresco and Nuxeo data types.
 * Provides mapping rules for common data types used in both systems.
 *
 * @author Alfresco Data Model Migration Team
 */
public class DataTypeConverter
{
    private static final Map<String, String> TYPE_MAPPINGS = new HashMap<>();

    static
    {
        // Text types
        TYPE_MAPPINGS.put("d:text", "string");
        TYPE_MAPPINGS.put("d:mltext", "string"); // Multi-lingual text becomes string
        
        // Numeric types
        // Note: d:int is mapped to long because Nuxeo uses long as its standard integer type
        // This widening conversion is safe and prevents overflow issues
        TYPE_MAPPINGS.put("d:int", "long");
        TYPE_MAPPINGS.put("d:long", "long");
        // Note: d:float is mapped to double for better precision in Nuxeo
        TYPE_MAPPINGS.put("d:float", "double");
        TYPE_MAPPINGS.put("d:double", "double");
        
        // Boolean
        TYPE_MAPPINGS.put("d:boolean", "boolean");
        
        // Date/Time types
        TYPE_MAPPINGS.put("d:date", "date");
        TYPE_MAPPINGS.put("d:datetime", "date");
        
        // Content and binary
        TYPE_MAPPINGS.put("d:content", "blob");
        
        // Reference types - mapped to strings
        TYPE_MAPPINGS.put("d:noderef", "string");
        TYPE_MAPPINGS.put("d:qname", "string");
        TYPE_MAPPINGS.put("d:path", "string");
        TYPE_MAPPINGS.put("d:category", "string");
        TYPE_MAPPINGS.put("d:locale", "string");
        
        // Generic type
        TYPE_MAPPINGS.put("d:any", "string");
    }

    /**
     * Converts an Alfresco data type to a Nuxeo data type.
     *
     * @param alfrescoType Alfresco data type (e.g., "d:text", "d:int")
     * @return Nuxeo data type (e.g., "string", "long")
     */
    public static String convertType(String alfrescoType)
    {
        if (alfrescoType == null || alfrescoType.isEmpty())
        {
            return "string"; // Default to string
        }

        // Remove namespace prefix if present and not standard
        String normalizedType = normalizeType(alfrescoType);
        
        String nuxeoType = TYPE_MAPPINGS.get(normalizedType);
        if (nuxeoType != null)
        {
            return nuxeoType;
        }

        // If no mapping found, default to string
        return "string";
    }

    /**
     * Normalizes an Alfresco type to a standard format.
     * Handles types with or without namespace prefixes.
     *
     * @param type type to normalize
     * @return normalized type
     */
    private static String normalizeType(String type)
    {
        if (type == null)
        {
            return null;
        }

        // If type doesn't have a namespace prefix, add d:
        if (!type.contains(":"))
        {
            return "d:" + type;
        }

        return type;
    }

    /**
     * Checks if a type conversion will result in data loss.
     *
     * @param alfrescoType Alfresco data type
     * @return true if conversion may lose data, false otherwise
     */
    public static boolean hasDataLoss(String alfrescoType)
    {
        if (alfrescoType == null)
        {
            return false;
        }

        String normalizedType = normalizeType(alfrescoType);
        
        // Multi-lingual text loses language information
        if ("d:mltext".equals(normalizedType))
        {
            return true;
        }

        // NodeRef loses type safety
        if ("d:noderef".equals(normalizedType))
        {
            return true;
        }

        // QName loses namespace resolution
        if ("d:qname".equals(normalizedType))
        {
            return true;
        }

        return false;
    }

    /**
     * Gets a description of potential data loss for a type conversion.
     *
     * @param alfrescoType Alfresco data type
     * @return description of data loss, or null if no loss
     */
    public static String getDataLossDescription(String alfrescoType)
    {
        if (alfrescoType == null)
        {
            return null;
        }

        String normalizedType = normalizeType(alfrescoType);
        
        switch (normalizedType)
        {
            case "d:mltext":
                return "Multi-lingual text loses language-specific variants; use separate fields per language";
            case "d:noderef":
                return "NodeRef loses type safety and referential integrity; implement custom validation";
            case "d:qname":
                return "QName loses namespace resolution; serialize as prefix:localName or {uri}localName";
            case "d:category":
                return "Category loses category hierarchy information; store as category ID or name";
            default:
                return null;
        }
    }

    /**
     * Gets all supported type mappings.
     *
     * @return map of Alfresco types to Nuxeo types
     */
    public static Map<String, String> getTypeMappings()
    {
        return new HashMap<>(TYPE_MAPPINGS);
    }

    /**
     * Checks if an Alfresco type is supported.
     *
     * @param alfrescoType Alfresco data type
     * @return true if supported, false otherwise
     */
    public static boolean isSupported(String alfrescoType)
    {
        if (alfrescoType == null)
        {
            return false;
        }
        
        String normalizedType = normalizeType(alfrescoType);
        return TYPE_MAPPINGS.containsKey(normalizedType);
    }
}
