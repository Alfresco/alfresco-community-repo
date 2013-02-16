/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;

import static org.alfresco.repo.content.transform.TransformerConfig.ANY;
import static org.alfresco.repo.content.transform.TransformerConfig.CONTENT;
import static org.alfresco.repo.content.transform.TransformerConfig.PREFIX;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.util.Triple;

/**
 * Provides access to transformer property names and values.
 * 
 * @author Alan Davis
 */
public abstract class TransformerPropertyNameExtractor
{
    /**
     * Returns a set of transformer names, source extensions and target mimetype extensions
     * from property names that defined transformation limits.
     * @param separator after the transformer name and the source mimetype extension.
     *        Must start and end in a '.'.
     * @param suffixes possible endings to the property names after the target mimetype extension.
     *        Must start with a '.' if there is a suffix.
     * @param includeSummary if true will also look for property names without the separator,
     *        source mimetype and target mimetype.
     * @param subsystem that provides the properties
     * @param mimetypeService
     */
    protected Set<Triple<String,String,String>> getTransformerNamesAndExt(String separator, Collection<String> suffixes, boolean includeSummary,
            ChildApplicationContextFactory subsystem, MimetypeService mimetypeService)
    {
        Set<Triple<String,String,String>> transformerNamesAndExtensions =
                new HashSet<Triple<String,String,String>>();
        
        for (String propertyName: subsystem.getPropertyNames())
        {
            if (propertyName.startsWith(PREFIX))
            {
                for (String suffix: suffixes)
                {
                    if (propertyName.endsWith(suffix))
                    {
                        String name = propertyName.substring(CONTENT.length(), propertyName.length()-suffix.length());
                        int i = name.lastIndexOf(separator);
                        if (i != -1)
                        {
                            String[] ext = name.substring(i+separator.length()).split("\\.");
                            if (ext.length == 2)
                            {
                                name = name.substring(0,  i);
                                transformerNamesAndExtensions.add(
                                        new Triple<String,String,String>(name, ext[0], ext[1]));
                                break;
                            }
                        }
                        else if (includeSummary)
                        {
                            transformerNamesAndExtensions.add(new Triple<String,String,String>(name, ANY, ANY));
                            break;
                        }
                    }
                }
            }
        }
        
        return transformerNamesAndExtensions;
    }
}