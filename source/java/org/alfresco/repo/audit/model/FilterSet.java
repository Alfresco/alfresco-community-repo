/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.audit.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.audit.AuditModel;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * This groups a set of filters together using AND or OR. They are evaluated in definition order with short cut evaluation if possible. The default beahviour is to or Filters
 * together.
 * 
 * @author Andy Hind
 */
public class FilterSet extends AbstractFilter implements XMLModelElement
{
    private static Log s_logger = LogFactory.getLog(FilterSet.class);
    
    private List<AbstractFilter> filters = new ArrayList<AbstractFilter>();

    private FilterSetMode mode = FilterSetMode.OR;

    public FilterSet()
    {
        super();
    }

    @Override
    public void configure(Element element, NamespacePrefixResolver namespacePrefixResolver)
    {
        super.configure(element, namespacePrefixResolver);

        // Mode
        Attribute modeAttribute = element.attribute(AuditModel.AT_MODE);
        if (modeAttribute != null)
        {
            mode = FilterSetMode.getFilterSetMode(modeAttribute.getStringValue());
        }

        // Filters

        for (Iterator nsit = element.elementIterator(AuditModel.EL_FILTER); nsit.hasNext(); /**/)
        {
            Element filterElement = (Element) nsit.next();
            AbstractFilter filter = AbstractFilter.createFilter(filterElement, namespacePrefixResolver);
            filters.add(filter);
        }

    }

}
