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
