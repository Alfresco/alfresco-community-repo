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
