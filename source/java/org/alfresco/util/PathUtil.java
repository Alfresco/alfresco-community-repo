/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.util;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.Path;

/**
 * Alfresco path-related utility functions.
 * 
 * @since 3.5
 */
public class PathUtil
{
    /**
     * Return the human readable form of the specified node Path. Fast version
     * of the method that simply converts QName localname components to Strings.
     * 
     * @param path Path to extract readable form from
     * @param showLeaf Whether to process the final leaf element of the path
     * 
     * @return human readable form of the Path
     */
    public static String getDisplayPath(Path path, boolean showLeaf)
    {
        // This method was moved here from org.alfresco.web.bean.repository.Repository
        StringBuilder buf = new StringBuilder(64);

        int count = path.size() - (showLeaf ? 0 : 1);
        for (int i = 0; i < count; i++)
        {
            String elementString = null;
            Path.Element element = path.get(i);
            if (element instanceof Path.ChildAssocElement)
            {
                ChildAssociationRef elementRef = ((Path.ChildAssocElement) element).getRef();
                if (elementRef.getParentRef() != null)
                {
                    elementString = elementRef.getQName().getLocalName();
                }
            } else
            {
                elementString = element.getElementString();
            }

            if (elementString != null)
            {
                buf.append("/");
                buf.append(elementString);
            }
        }

        return buf.toString();
    }
}
