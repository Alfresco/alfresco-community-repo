/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.bundle;

import java.text.Collator;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.AlfrescoCollator;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.I18NUtil;

public class FileInfoPropsComparator implements Comparator<FileInfo>
{
    private List<Pair<QName, Boolean>> sortProps;

    private Collator collator;

    public FileInfoPropsComparator(List<Pair<QName, Boolean>> sortProps)
    {
        this.sortProps = sortProps;
        this.collator = AlfrescoCollator.getInstance(I18NUtil.getContentLocale());
    }

    @Override
    public int compare(FileInfo n1, FileInfo n2)
    {
        return compareImpl(n1,
                           n2,
                           sortProps);
    }

    private int compareImpl(FileInfo node1In, FileInfo node2In, List<Pair<QName, Boolean>> sortProps)
    {
        Object pv1 = null;
        Object pv2 = null;

        QName sortPropQName = (QName) sortProps.get(0).getFirst();
        boolean sortAscending = sortProps.get(0).getSecond();

        FileInfo node1 = node1In;
        FileInfo node2 = node2In;

        if (sortAscending == false)
        {
            node1 = node2In;
            node2 = node1In;
        }

        int result = 0;

        pv1 = node1.getProperties().get(sortPropQName);
        pv2 = node2.getProperties().get(sortPropQName);

        if (pv1 == null)
        {
            if (pv2 == null && sortProps.size() > 1)
            {
                return compareImpl(node1In,
                                   node2In,
                                   sortProps.subList(1,
                                                     sortProps.size()));
            }
            else
            {
                return (pv2 == null ? 0 : -1);
            }
        }
        else if (pv2 == null)
        {
            return 1;
        }

        if (pv1 instanceof String)
        {
            result = collator.compare((String) pv1,
                                      (String) pv2); // TODO: use collation keys
                                                     // (re: performance)
        }
        else if (pv1 instanceof Date)
        {
            result = (((Date) pv1).compareTo((Date) pv2));
        }
        else if (pv1 instanceof Long)
        {
            result = (((Long) pv1).compareTo((Long) pv2));
        }
        else if (pv1 instanceof Integer)
        {
            result = (((Integer) pv1).compareTo((Integer) pv2));
        }
        else if (pv1 instanceof QName)
        {
            result = (((QName) pv1).compareTo((QName) pv2));
        }
        else if (pv1 instanceof Boolean)
        {
            result = (((Boolean) pv1).compareTo((Boolean) pv2));
        }
        else
        {
            throw new RuntimeException("Unsupported sort type: " + pv1.getClass().getName());
        }

        if ((result == 0) && (sortProps.size() > 1))
        {
            return compareImpl(node1In,
                               node2In,
                               sortProps.subList(1,
                                                 sortProps.size()));
        }

        return result;
    }
}