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
package org.alfresco.service.cmr.audit;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.NotAuditable;
import org.alfresco.service.PublicService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The public API by which applications can create audit entries.
 * This does not affect auditing using method interceptors.
 * The information recorded can not be confused between the two.
 * 
 * This API could be used by an audit action.
 * 
 * @author Andy Hind
 */
@PublicService
public interface AuditService
{
    /**
     * Add an application audit entry.
     * 
     * @param source -
     *            a string that represents the application
     * @param description -
     *            the audit entry
     */
    @NotAuditable
    public void audit(String source, String description);

    /**
     * 
     * @param source -
     *            a string that represents the application
     * @param description -
     *            the audit entry
     * @param key -
     *            a node ref to use as the key for filtering etc
     */
    @NotAuditable
    public void audit(String source, String description, NodeRef key);

    /**
     * 
     * @param source -
     *            a string that represents the application
     * @param description -
     *            the audit entry
     * @param args -
     *            an arbitrary list of parameters
     */
    @NotAuditable
    public void audit(String source, String description, Object... args);

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
    @NotAuditable
    public void audit(String source, String description, NodeRef key, Object... args);
    
    
    /**
     * Get the audit trail for a node ref.
     * 
     * @param nodeRef - the node ref for which to get the audit trail.
     * @return - tha audit trail 
     */
    @NotAuditable
    public List<AuditInfo> getAuditTrail(NodeRef nodeRef);

    /*
     * V3.2 from here on.  Put all fixes to the older audit code before this point, please.
     */
    
    /**
     * @param applicationName   the name of the application to check 
     * @param path              the path to check
     * @return                  Returns <tt>true</tt> if auditing is enabled for the given path
     * 
     * @since 3.2
     */
    boolean isAuditEnabled(String applicationName, String path);
    
    /**
     * Enable auditing for an application path
     * 
     * @param applicationName   the name of the application to check 
     * @param path              the path to enable
     * 
     * @since 3.2
     */
    void enableAudit(String applicationName, String path);
    
    /**
     * Disable auditing for an application path
     * 
     * @param applicationName   the name of the application to check 
     * @param path              the path to disable
     * 
     * @since 3.2
     */
    void disableAudit(String applicationName, String path);
    
    /**
     * Remove all audit entries for the given application
     * 
     * @param applicationName   the name of the application for which to remove entries
     * 
     * @since 3.2
     */
    void clearAudit(String applicationName);
    
    /**
     * The interface that will be used to give query results to the calling code.
     * 
     * @since 3.2
     */
    public static interface AuditQueryCallback
    {
        /**
         * Determines whether this callback requires the values argument to be populated when {@link #handleAuditEntry}
         * is called.
         * 
         * @return <code>true</code> if this callback requires the values argument to be populated
         */
        boolean valuesRequired();
        
        /**
         * Handle a row of audit entry data.
         * 
         * @param entryId                   the unique audit entry ID
         * @param applicationName           the name of the application
         * @param user                      the user that logged the entry
         * @param time                      the time of the entry
         * @param values                    the values map as created
         * @return                          Return <tt>true</tt> to continue processing rows or <tt>false</tt> to stop
         */
        boolean handleAuditEntry(
                Long entryId,
                String applicationName,
                String user,
                long time,
                Map<String, Serializable> values);
        
        /**
         * Handle audit entry failures
         * 
         * @param entryId                   the entry ID
         * @param errorMsg                  the error message
         * @param error                     the exception causing the error (may be <tt>null</tt>)
         * @return                          Return <tt>true</tt> to continue processing rows or <tt>false</tt> to stop
         */
        boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error);
    }
    
    /**
     * Issue an audit query using the given parameters and consuming results in the callback.
     * 
     * @param callback          the callback that will handle results
     * @param parameters        the parameters for the query (may not be <tt>null</tt>)
     * @param maxResults        the maximum number of results to retrieve (zero or negative to ignore)
     * 
     * @since 3.3
     */
    void auditQuery(AuditQueryCallback callback, AuditQueryParameters parameters, int maxResults);
    
    /**
     * Get the audit entries that match the given criteria.
     * 
     * @param callback          the callback that will handle results
     * @param forward           <tt>true</tt> for results to ordered from first to last,
     *                          or <tt>false</tt> to order from last to first
     * @param applicationName   if not <tt>null</tt>, find entries logged against this application 
     * @param user              if not <tt>null</tt>, find entries logged against this user
     * @param from              the start search time (<tt>null</tt> to start at the beginning)
     * @param to                the end search time (<tt>null</tt> for no limit)
     * @param maxResults        the maximum number of results to retrieve (zero or negative to ignore)
     * 
     * @since 3.2
     * @deprecated              Use {@link #auditQuery(AuditQueryCallback, AuditQueryParameters)}
     */
    void auditQuery(
            AuditQueryCallback callback,
            boolean forward,
            String applicationName, String user, Long from, Long to,
            int maxResults);
    
    /**
     * Get the audit entries that match the given criteria.
     * 
     * @param callback          the callback that will handle results
     * @param forward           <tt>true</tt> for results to ordered from first to last,
     *                          or <tt>false</tt> to order from last to first
     * @param applicationName   if not <tt>null</tt>, find entries logged against this application 
     * @param user              if not <tt>null</tt>, find entries logged against this user
     * @param from              the start search time (<tt>null</tt> to start at the beginning)
     * @param to                the end search time (<tt>null</tt> for no limit)
     * @param searchKey         the audit key path that must exist (<tt>null</tt> to ignore)
     * @param searchValue       an audit value that must exist (<tt>null</tt> to ignore)
     * @param maxResults        the maximum number of results to retrieve (zero or negative to ignore)
     * 
     * @since 3.2
     * @deprecated              Use {@link #auditQuery(AuditQueryCallback, AuditQueryParameters)}
     */
    void auditQuery(
            AuditQueryCallback callback,
            boolean forward,
            String applicationName, String user, Long from, Long to,
            String searchKey, Serializable searchValue,
            int maxResults);
}
