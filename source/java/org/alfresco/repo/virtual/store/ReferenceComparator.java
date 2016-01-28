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

package org.alfresco.repo.virtual.store;

import java.io.Serializable;
import java.text.Collator;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.AlfrescoCollator;
import org.alfresco.util.Pair;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Property value based virtual nodes {@link Comparator}. Compares two
 * virtualized nodes based on an ordered collection of property names. The
 * property with the lower index in the collection of properties is the more
 * significant (with the highest-order) one in the comparison process.
 * 
 * @author Silviu Dinuta
 */
public class ReferenceComparator implements Comparator<Reference>
{
    private VirtualStore smartStore;

    private List<Pair<QName, Boolean>> sortProps;

    private Collator collator;

    public ReferenceComparator(VirtualStore smartStore, List<Pair<QName, Boolean>> sortProps)
    {
        this.sortProps = sortProps;
        this.collator = AlfrescoCollator.getInstance(I18NUtil.getContentLocale());
        this.smartStore = smartStore;
    }

    @Override
    public int compare(Reference r1, Reference r2)
    {
        try
        {
            return compareImpl(r1,
                               r2,
                               sortProps);
        }
        catch (VirtualizationException e)
        {
            throw new RuntimeException(e);
        }
    }

    private int compareImpl(Reference ref1In, Reference ref2In, List<Pair<QName, Boolean>> sortProps)
                throws VirtualizationException
    {
        Object pv1 = null;
        Object pv2 = null;

        QName sortPropQName = (QName) sortProps.get(0).getFirst();
        boolean sortAscending = sortProps.get(0).getSecond();

        Reference ref1 = ref1In;
        Reference ref2 = ref2In;

        if (sortAscending == false)
        {
            ref1 = ref2In;
            ref2 = ref1In;
        }

        int result = 0;

        Map<QName, Serializable> properties1 = smartStore.getProperties(ref1);
        pv1 = properties1.get(sortPropQName);

        Map<QName, Serializable> properties2 = smartStore.getProperties(ref2);
        pv2 = properties2.get(sortPropQName);

        if (pv1 == null)
        {
            if (pv2 == null && sortProps.size() > 1)
            {
                return compareImpl(ref1In,
                                   ref2In,
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
            // TODO: use collation keys (re: performance)
            result = collator.compare((String) pv1,
                                      (String) pv2);
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
            return compareImpl(ref1In,
                               ref2In,
                               sortProps.subList(1,
                                                 sortProps.size()));
        }

        return result;
    }
}
