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

package org.alfresco.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.ChildAssocElement;

/**
 * Alfresco path-related utility functions.
 * 
 * @since 3.5
 */
public class PathUtil
{
    /**
     * Return the human readable form of the specified node Path. Fast version of the method that simply converts QName localname components to Strings.
     * 
     * @param path
     *            Path to extract readable form from
     * @param showLeaf
     *            Whether to process the final leaf element of the path
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
            }
            else
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

    /**
     * Return the node ids from the specified node Path, so that the first element is the immediate parent.
     *
     * @param path
     *            the node's path object
     * @param showLeaf
     *            whether to process the final leaf element of the path
     * @return list of node ids
     */
    public static List<String> getNodeIdsInReverse(Path path, boolean showLeaf)
    {
        int count = path.size() - (showLeaf ? 1 : 2);
        if (count < 0)
        {
            return Collections.emptyList();
        }

        List<String> nodeIds = new ArrayList<>(count);
        // Add in reverse order (so the first element is the immediate parent)
        for (int i = count; i >= 0; i--)
        {
            Path.Element element = path.get(i);
            if (element instanceof ChildAssocElement)
            {
                ChildAssocElement childAssocElem = (ChildAssocElement) element;
                NodeRef childNodeRef = childAssocElem.getRef().getChildRef();
                nodeIds.add(childNodeRef.getId());
            }
        }

        return nodeIds;
    }
}
