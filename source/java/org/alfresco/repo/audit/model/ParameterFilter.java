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
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

public class ParameterFilter extends KeyFilter implements XMLModelElement 
{
    private static Log s_logger = LogFactory.getLog(ParameterFilter.class);
    
    private QName parameterName;
    
    public ParameterFilter()
    {
        super();
    }

    @Override
    public void configure(Element element, NamespacePrefixResolver namespacePrefixResolver)
    {
        super.configure(element, namespacePrefixResolver);
        
        Element parameterNameElement = element.element(AuditModel.EL_PARAMETER_NAME);
        if(parameterNameElement == null)
        {
            throw new AuditModelException("A parameter is mandatory for a parameter filter");
        }
        else
        {
            String stringQName = parameterNameElement.getStringValue();
            if (stringQName.charAt(1) == '{')
            {
                parameterName = QName.createQName(stringQName);
            }
            else
            {
                parameterName = QName.createQName(stringQName);
            }
        }
    }

    
}
