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

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaMetadataKeys;

/**
 * Default implementation for the MetadataExtracterConfig;
 * 
 * @author Andrei Rebegea
 */
public class MetadataExtracterConfigImpl implements MetadataExtracterConfig
{
    protected static Log logger = LogFactory.getLog(MetadataExtracterConfigImpl.class);

    private static final String PARSE_SHAPE_PROP_STRING = "content.metadataExtracter.parseShapes";

    private Properties properties;

    @Override
    public void prepareMetadataWithConfigParams(Metadata metadata)
    {
        if (metadata == null)
        {
            return;
        }
        boolean shouldParseShapes = getBooleanProperty(PARSE_SHAPE_PROP_STRING, TIKA_PARSER_PARSE_SHAPES_DEFAULT_VALUE);
        metadata.add(TikaMetadataKeys.TIKA_PARSER_PARSE_SHAPES_KEY, Boolean.toString(shouldParseShapes));
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Tika metadata options passed to tika parser: " + metadata);
        }
    }

    /**
     * The Alfresco global properties.
     */
    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    private boolean getBooleanProperty(String name, boolean defaultValue)
    {
        boolean value = defaultValue;
        if (properties != null)
        {
            String property = properties.getProperty(name);
            if (property != null)
            {
                value = property.trim().equalsIgnoreCase(Boolean.TRUE.toString());
            }
        }
        return value;
    }
}
