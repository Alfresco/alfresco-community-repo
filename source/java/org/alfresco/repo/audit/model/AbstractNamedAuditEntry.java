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

import org.alfresco.repo.audit.AuditModel;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Element;

public abstract class AbstractNamedAuditEntry extends AbstractAuditEntry
{
    private static Log s_logger = LogFactory.getLog(AbstractNamedAuditEntry.class);

    private String name;

    public AbstractNamedAuditEntry()
    {
        super();
    }

    @Override
    void configure(AbstractAuditEntry parent, Element element, NamespacePrefixResolver namespacePrefixResolver)
    {
        Attribute nameAttribute = element.attribute(AuditModel.AT_NAME);
        if (nameAttribute != null)
        {
            name = nameAttribute.getStringValue();
        }
        else
        {
            throw new AuditModelException("The name attribute is mandatory");
        }
        if(s_logger.isDebugEnabled())
        {
            s_logger.debug("Name = "+name);
        }
        
        super.configure(parent, element, namespacePrefixResolver);

    }

    /* package */String getName()
    {
        return name;
    }

}
