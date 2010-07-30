/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.service.cmr.audit;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.PublicService;

/**
 * The public API by which applications can query the audit logs and enable or disable auditing.
 * 
 * @author Derek Hulley
 */
@PublicService
public interface AuditService
{
    /**
     * @return                  Returns <tt>true</tt> if auditing is globally enabled
     * 
     * @since 3.4
     */
    boolean isAuditEnabled();
    
    /**
     * Enable or disable the global auditing state
     * 
     * @param enable            <tt>true</tt> to enable auditing globally or <tt>false</tt> to disable
     * 
     * @since 3.4
     */
    void setAuditEnabled(boolean enable);
    
    /**
     * Get all registered audit applications
     * 
     * @return                  Returns a set of all available audit applications
     * 
     * @since 3.4
     */
    Set<String> getAuditApplications();
    
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
     * 
     * @deprecated          Use {@link #clearAudit(String, Long, Long)}
     */
    void clearAudit(String applicationName);
    
    /**
     * Remove audit entries for the given application between the time ranges.  If no start
     * time is given then entries are deleted as far back as they exist.  If no end time is
     * given then entries are deleted up until the current time.
     * 
     * @param applicationName   the name of the application for which to remove entries
     * @param fromTime          the start time of entries to remove (inclusive and optional)
     * @param toTime            the end time of entries to remove (exclusive and optional)
     * 
     * @since 3.4
     */
    void clearAudit(String applicationName, Long fromTime, Long toTime);
    
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
}
