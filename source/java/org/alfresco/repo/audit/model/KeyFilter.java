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

public class KeyFilter extends AbstractFilter
{
    private static Log s_logger = LogFactory.getLog(KeyFilter.class);
    
    private String expression;
    
    private KeyFilterMode keyFilterMode;

    public KeyFilter()
    {
        super();
    }

    @Override
    public void configure(Element element, NamespacePrefixResolver namespacePrefixResolver)
    {
        super.configure(element, namespacePrefixResolver);
        
        // Filter mode
        Attribute keyFilterTypeAttribute = element.attribute(AuditModel.AT_MODE);
        if(keyFilterTypeAttribute != null)
        {
            keyFilterMode = KeyFilterMode.getKeyFilterMode(keyFilterTypeAttribute.getStringValue());
        }
        else
        {
            keyFilterMode = KeyFilterMode.ALL;
        }
        
        // Expression
        
        Element expressionElement = element.element(AuditModel.EL_EXPRESSION);
        if(expressionElement == null)
        {
            throw new AuditModelException("An expression is mandatory for a key filter");
        }
        else
        {
            expression = expressionElement.getText();
        }
    }

    
    
}
