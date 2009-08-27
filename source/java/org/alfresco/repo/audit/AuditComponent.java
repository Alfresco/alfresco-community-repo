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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.audit.model._3.AuditPath;
import org.alfresco.service.cmr.audit.AuditInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.aopalliance.intercept.MethodInvocation;

/**
 * The audit component. Used by the AuditService and AuditMethodInterceptor to insert audit entries.
 * <p/>
 * The V3.2 audit functionality is contained within the same component.  When the newer audit
 * implementation has been tested and approved, then older ones will be deprecated as necessary.
 * 
 * @author Andy Hind
 * @author Derek Hulley
 */
public interface AuditComponent
{
    /**
     * Audit entry point for method interceptors.
     * 
     * @return - the return onbject from the normal invocation of the audited method.
     * 
     * @since 2.1
     */
    public Object audit(MethodInvocation methodInvocation) throws Throwable;

    /**
     * @param source -
     *            a string that represents the application
     * @param description -
     *            the audit entry *
     * @param key -
     *            a node ref to use as the key for filtering etc
     * @param args -
     *            an arbitrary list of parameters
     * 
     * @since 2.1
     */
    public void audit(String source, String description, NodeRef key, Object... args);

    /**
     * Get the audit trail for a node.
     * 
     * @param nodeRef -
     *            the node ref for which we want the audit trail
     * @return - a list of AuditInfo objects that represent the audit trail for the given node reference.
     * 
     * @since 2.1
     */
    public List<AuditInfo> getAuditTrail(NodeRef nodeRef);

    /*
     * V3.2 from here on.  Put all fixes to the older audit code before this point, please.
     */

    /**
     * Start an audit session for the given root path.  All later audit values must start with
     * the same root path.
     * <p/>
     * The name of the application controls part of the audit model will be used.  The root path must
     * start with the matching <b>key</b> attribute that was declared for the matching
     * <b>Application</b> element in the audit configuration.
     * <p/>
     * This is a read-write method.  Client code must wrap calls in the appropriate transactional wrappers.
     * 
     * @param applicationName   the name of the application to log against
     * @param rootPath          a base path of {@link AuditPath} key entries concatenated with <b>.</b> (period)
     * @return                  Returns the unique session
     * @throws IllegalStateException if there is not a writable transaction present
     */
    AuditSession startAuditSession(String applicationName, String rootPath);
    
    /**
     * {@inheritDoc #startAuditSession(String, String)}

     * @param values            values to associate with the session.  These values will override or
     *                          complement generated session-specific values
     * @throws IllegalStateException if there is not a writable transaction present
     */
    AuditSession startAuditSession(String applicationName, String rootPath, Map<String, Serializable> values);
    
    /**
     * Record a set of values against the given session.  The map is a path (starting with '/') relative
     * to the root path given when {@link #startAuditSession(String, String) starting the session}.  All
     * resulting path values (session root path + map entry paths) must have data recorder entries and
     * be enabled for data to be recorded.
     * <p/>
     * The return values reflect what was actually persisted and is controlled by the data extractors
     * defined in the audit configuration.
     * <p/>
     * This is a read-write method.  Client code must wrap calls in the appropriate transactional wrappers.
     * 
     * @param session           a pre-existing audit session to continue with
     * @param values            the values to audit mapped by {@link AuditPath} key relative to the session
     *                          root path
     * @return                  Returns the values that were actually persisted, keyed by their full path.
     * @throws IllegalStateException if there is not a writable transaction present
     * 
     * @see #startAuditSession()
     * 
     * @since 3.2
     */
    Map<String, Serializable> audit(AuditSession session, Map<String, Serializable> values);
}
