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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.repo.audit.AuditMode;
import org.alfresco.repo.audit.AuditModel;
import org.alfresco.repo.audit.MethodAuditModel;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

public class ServiceAuditEntry extends AbstractNamedAuditEntry implements MethodAuditModel
{
    private static Log s_logger = LogFactory.getLog(ServiceAuditEntry.class);
    
    private Map<String, MethodAuditEntry> methods = new HashMap<String, MethodAuditEntry>();

    public ServiceAuditEntry()
    {
        super();
    }

    @Override
    void configure(AbstractAuditEntry parent, Element element, NamespacePrefixResolver namespacePrefixResolver)
    {
        super.configure(parent, element, namespacePrefixResolver);

        // Add Methods

        if(s_logger.isDebugEnabled())
        {
            s_logger.debug("Adding methods to service "+getName());
        }
        for (Iterator nsit = element.elementIterator(AuditModel.EL_METHOD); nsit.hasNext(); /**/)
        {
            Element methodElement = (Element) nsit.next();
            MethodAuditEntry method = new MethodAuditEntry();
            method.configure(this, methodElement, namespacePrefixResolver);
            methods.put(method.getName(), method);
        }
        if(s_logger.isDebugEnabled())
        {
            s_logger.debug("...added methods for service "+getName());
        }
    }

    public AuditMode beforeExecution(AuditMode auditMode, MethodInvocation mi)
    {
        String methodName = mi.getMethod().getName();
        MethodAuditEntry method = methods.get(methodName);
        if (method != null)
        {
            return method.beforeExecution(auditMode, mi);
        }
        else
        {
            if(s_logger.isDebugEnabled())
            {
                s_logger.debug("Evaluating if service is audited (no specific setting) for "+getName()+"."+methodName);
            }
            return getEffectiveAuditMode();
        }
    }

    public AuditMode afterExecution(AuditMode auditMode, MethodInvocation mi)
    {
        throw new UnsupportedOperationException();
    }

    public AuditMode onError(AuditMode auditMode, MethodInvocation mi)
    {
        throw new UnsupportedOperationException();
    }

    public RecordOptionsImpl getAuditRecordOptions(MethodInvocation mi)
    {
        throw new UnsupportedOperationException();
    }

}
