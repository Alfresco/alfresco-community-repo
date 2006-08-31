/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.audit.model;

import org.alfresco.repo.audit.AuditModel;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * The base class for filtering.
 * 
 * This supports negating the filter, ie NOT.
 * 
 * @author Andy Hind
 */
public abstract class AbstractFilter implements XMLModelElement
{
    private static Log s_logger = LogFactory.getLog(AbstractFilter.class);
    
    private boolean invert = false;

    public AbstractFilter()
    {
        super();
    }

    public static AbstractFilter createFilter(Element filterElement, NamespacePrefixResolver namespacePrefixResolver)
    {
        AbstractFilter filter;

        Attribute typeAttribute = filterElement.attribute(AuditModel.AT_TYPE);
        if (typeAttribute == null)
        {
            throw new AuditModelException("A filter must specify it concrete type using xsi:type");
        }
        if (typeAttribute.getStringValue().endsWith("FilterSet"))
        {
            filter = new FilterSet();
        }
        else if (typeAttribute.getStringValue().endsWith("KeyFilter"))
        {
            filter = new KeyFilter();
        }
        else if (typeAttribute.getStringValue().endsWith("ParameterFilter"))
        {
            filter = new ParameterFilter();
        }
        else
        {
            throw new AuditModelException(
                    "Invalid filter type. It must be one of: FilterSet, KeyFilter, ParameterFilter ");
        }

        filter.configure(filterElement, namespacePrefixResolver);
        return filter;
    }

    public void configure(Element element, NamespacePrefixResolver namespacePrefixResolver)
    {
        Attribute invertAttribute = element.attribute(AuditModel.AT_INVERT);
        if (invertAttribute != null)
        {
            invert = Boolean.valueOf(invertAttribute.getStringValue()).booleanValue();
        }
        else
        {
            invert = false;
        }
    }

    /* package */boolean isInvert()
    {
        return invert;
    }
}
