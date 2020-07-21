/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.DefaultPropertiesPersister;
import org.springframework.util.StringUtils;

/**
 * Simple extension to the{@link DefaultPropertiesPersister} to strip trailing whitespace 
 * from incoming properties. 
 * 
 * @author shane frensley
 * @see org.springframework.util.DefaultPropertiesPersister
 */
public class AlfrescoPropertiesPersister extends DefaultPropertiesPersister 
{
    
    private static Log logger = LogFactory.getLog(AlfrescoPropertiesPersister.class);

    @Override
    public void load(Properties props, InputStream is) throws IOException 
    {
        super.load(props, is);
        strip(props);
    }
    
    @Override
    public void load(Properties props, Reader reader) throws IOException 
    {
        super.load(props, reader);
        strip(props);
    }
    
    public void loadFromXml(Properties props, InputStream is) throws IOException 
    {
        super.loadFromXml(props, is);
        strip(props);
    }
    
    private void strip(Properties props) 
    {
        for (Enumeration<Object> keys = props.keys(); keys.hasMoreElements();) 
        {
            String key = (String) keys.nextElement();
            String val = StringUtils.trimTrailingWhitespace(props.getProperty(key));
            if (logger.isTraceEnabled()) 
            {
                logger.trace("Trimmed trailing whitespace for property " + key + " = " + val);
            }
            props.setProperty(key, val);
        }
    }
}
