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
