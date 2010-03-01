/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.avm.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.util.NameMatcher;

/**
 * NameMatcher that matches a list of extensions (case insensitively).
 * @author britt
 */
public class FileExtensionNameMatcher implements NameMatcher, Serializable 
{
    private static final long serialVersionUID = -1498029477935181998L;

    /**
     * The extensions to match.
     */
    private List<String> fExtensions;
    
    /**
     * Default constructor.
     */
    public FileExtensionNameMatcher()
    {
        fExtensions = new ArrayList<String>();
    }
    
    /**
     * Set the extensions case insensitively.
     * @param extensions
     */
    public void setExtensions(List<String> extensions)
    {
        for (String extension : extensions)
        { 
            fExtensions.add(extension.toLowerCase());
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.util.NameMatcher#matches(java.lang.String)
     */
    public boolean matches(String name) 
    {
        String lcName = name.toLowerCase();
        for (String ext : fExtensions)
        {
            if (lcName.endsWith(ext))
            {
                return true;
            }
        }
        return false;
    }
}
