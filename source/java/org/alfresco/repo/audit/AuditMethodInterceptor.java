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
package org.alfresco.repo.audit;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/** 
 * A method interceptor to wrap method invocations with auditing.
 * 
 * A single instance is used to wrap all services. If the single instance is disabled
 * no auditing will be carried out and there will be minimal overhead. 
 * 
 * @author Andy Hind
 */
public class AuditMethodInterceptor implements MethodInterceptor
{
    //private static Log s_logger = LogFactory.getLog(AuditMethodInterceptor.class);

    private AuditComponent auditComponent;

    private boolean disabled = false;
    
    public AuditMethodInterceptor()
    {
        super();
    }

    public void setDisabled(boolean disabled)
    {
        this.disabled = disabled;
    }

    public void setAuditComponent(AuditComponent auditComponent)
    {
        this.auditComponent = auditComponent;
    }

    public Object invoke(MethodInvocation mi) throws Throwable
    {
        if(disabled)
        {
            return mi.proceed();
        }
        else
        {
            return auditComponent.audit(mi);
        }
        
    }
}
