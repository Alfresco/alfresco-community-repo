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

import org.alfresco.repo.audit.model.AuditApplication;
import org.alfresco.repo.audit.model.AuditModelRegistry;
import org.alfresco.repo.audit.model._3.AuditPath;
import org.alfresco.service.cmr.audit.AuditInfo;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
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
     * Delete audit entries for the given application and time range
     * 
     * @param applicationName   the name of the application being logged to
     * @param fromTime          the start time of entries to remove (inclusive and optional)
     * @param toTime            the end time of entries to remove (exclusive and optional)
     * 
     * @since 3.2
     */
    void deleteAuditEntries(String applicationName, Long fromTime, Long toTime);
    
    /**
     * Check if an audit path is enabled.  The path will be disabled if it or any higher
     * path has been explicitly disabled.  Any disabled path will not be processed when
     * data is audited.
     * 
     * @param applicationName   the name of the application being logged to
     * @param path              the audit path to check
     * @return                  Returns <tt>true</tt> if the audit path has been disabled
     * 
     * @since 3.2
     */
    boolean isAuditPathEnabled(String applicationName, String path);
    
    /**
     * Enable auditing (if it is not already enabled) for all paths that contain the given path.
     * The path is the path as originally logged (see {@link #audit(String, String, Map)}) and
     * not the path that the generated data may contain - although this would be similarly
     * enabled.
     * <p>
     * If the enabled 
     * 
     * @param applicationName   the name of the application being logged to
     * @param path              the audit path to enable auditing on
     * 
     * @since 3.2
     */
    void enableAudit(String applicationName, String path);

    /**
     * Disable auditing (if it is not already disabled) for all paths that contain the given path.
     * The path is the path as originally logged (see {@link #audit(String, String, Map)}) and
     * not the path that the generated data may contain - although this would be similarly
     * disabled.
     * <p>
     * If the path is <b>/x/y</b> then any data paths that start with <b>/x/y</b> will be stripped
     * out <u>before</u> data generators and data recorders are applied.  If the path represents
     * the root path of the application, then auditing for that application is effectively disabled. 
     * 
     * @param applicationName   the name of the application being logged to
     * @param path              the audit path to enable auditing on
     * 
     * @since 3.2
     */
    void disableAudit(String applicationName, String path);
    
    /**
     * Remove all disabled paths i.e. enable all per-path based auditing.  Auditing may still be
     * disabled globally.  This is primarily for test purposes; applications should know which
     * paths need {@link #enableAudit(String, String) enabling} or
     * {@link #disableAudit(String, String) disabling}.
     * 
     * @param applicationName   the name of the application
     * 
     * @since 3.2
     */
    void resetDisabledPaths(String applicationName);

    /**
     * Create an audit entry for the given map of values.  The map key is a path - starting with '/'
     * ({@link AuditApplication#AUDIT_PATH_SEPARATOR}) - relative to the root path provided.
     * 
     * The root path and value keys are combined to produce a map of data keyed by full path.  This
     * fully-pathed map is then passed through the
     * {@link AuditModelRegistry#getAuditPathMapper() audit path mapper}.  The result may yield data
     * destined for several different
     * {@link AuditModelRegistry#getAuditApplicationByKey(String) audit applications}.  depending on
     * the data extraction and generation defined in the applications, values (or derived values) may
     * be recorded against several audit entries (one per application represented).
     * <p/>
     * The return values reflect what was actually persisted and is controlled by the data extractors
     * defined in the audit configuration.
     * <p/>
     * A new read-write transaction is started if there are values to write that there is not a viable
     * transaction present.
     * 
     * @param rootPath          a base path of {@link AuditPath} key entries concatenated with the path separator
     *                          '/' ({@link AuditApplication#AUDIT_PATH_SEPARATOR})
     * @param values            the values to audit mapped by {@link AuditPath} key relative to root path
     *                          (may be <tt>null</tt>)
     * @return                  Returns the values that were actually persisted, keyed by their full path.
     * @throws IllegalStateException if there is not a writable transaction present
     * 
     * @see #startAuditSession()
     * 
     * @since 3.2
     */
    Map<String, Serializable> recordAuditValues(String rootPath, Map<String, Serializable> values);
    
    /**
     * Get the audit entries that match the given criteria.
     * 
     * @param callback          the callback that will handle results
     * @param applicationName   if not <tt>null</tt>, find entries logged against this application 
     * @param user              if not <tt>null</tt>, find entries logged against this user
     * @param from              the start search time (<tt>null</tt> to start at the beginning)
     * @param to                the end search time (<tt>null</tt> for no limit)
     * @param maxResults        the maximum number of results to retrieve (zero or negative to ignore)
     * 
     * @since 3.2
     */
    void auditQuery(
            AuditQueryCallback callback,
            String applicationName, String user, Long from, Long to,
            int maxResults);
    
    /**
     * Get the audit entries that match the given criteria.
     * 
     * @param callback          the callback that will handle results
     * @param applicationName   if not <tt>null</tt>, find entries logged against this application 
     * @param user              if not <tt>null</tt>, find entries logged against this user
     * @param from              the start search time (<tt>null</tt> to start at the beginning)
     * @param to                the end search time (<tt>null</tt> for no limit)
     * @param searchKey         the audit key path that must exist (<tt>null</tt> to ignore)
     * @param searchValue       an audit value that must exist (<tt>null</tt> to ignore)
     * @param maxResults        the maximum number of results to retrieve (zero or negative to ignore)
     * 
     * @since 3.2
     */
    void auditQuery(
            AuditQueryCallback callback,
            String applicationName, String user, Long from, Long to,
            String searchKey, Serializable searchValue,
            int maxResults);
}
