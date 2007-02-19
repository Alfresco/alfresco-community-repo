/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.audit;

import java.util.List;

import org.alfresco.service.cmr.audit.AuditInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.aopalliance.intercept.MethodInvocation;

/**
 * The audit component.
 * 
 * Used by the AuditService and AuditMethodInterceptor to insert audit entries.
 * 
 * @author Andy Hind
 */
public interface AuditComponent
{
    /**
     * Audit entry point for method interceptors.
     * 
     * @param methodInvocation
     */
    public Object audit(MethodInvocation methodInvocation) throws Throwable;

    /**
     * 
     * @param source -
     *            a string that represents the application
     * @param description -
     *            the audit entry *
     * @param key -
     *            a node ref to use as the key for filtering etc
     * @param args -
     *            an arbitrary list of parameters
     */
    public void audit(String source, String description, NodeRef key, Object... args);
    
    /**
     * Get the audit trail for a node.
     * 
     * @param nodeRef
     * @return
     */
    public List<AuditInfo> getAuditTrail(NodeRef nodeRef);


}
