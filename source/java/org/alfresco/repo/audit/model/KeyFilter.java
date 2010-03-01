/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
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
