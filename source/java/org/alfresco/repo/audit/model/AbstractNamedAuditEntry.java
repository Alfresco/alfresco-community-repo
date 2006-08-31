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
