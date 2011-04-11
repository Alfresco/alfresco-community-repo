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
package org.alfresco.repo.audit;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.audit.model.AuditApplication;
import org.alfresco.repo.audit.model.AuditModelRegistry;
import org.alfresco.repo.audit.model._3.AuditPath;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;

/**
 * The audit component. Used by the AuditService and AuditMethodInterceptor to insert audit entries.
 * 
 * @author Derek Hulley
 */
public interface AuditComponent
{
    /**
     * Determines whether audit is globally enabled or disabled.
     * 
     * @return                  Returns <code>true</code> if audit is enabled
     * 
     * @since 3.3
     */
    public boolean isAuditEnabled();
    
    /**
     * Switch auditing on or off
     * 
     * @param enable            <tt>true</tt> to enable auditing or <tt>false</tt> to disable
     * 
     * @since 3.4
     */
    public void setAuditEnabled(boolean enable);

    /**
     * Get all registered audit applications, whether active or not.
     * 
     * @return                  Returns a map of registered audit applications keyed by name
     * 
     * @since 3.4
     */
    public Map<String, AuditApplication> getAuditApplications();
    
    /**
     * Determine whether the audit infrastructure expects audit values to be passed in.
     * This is a helper method to allow optimizations in the client code.  Reasons why
     * this method might return <tt>false</tt> are: auditing is disabled; no audit applications
     * have been registered.  Sometimes, depending on the log level, this method may always
     * return <tt>true</tt>.
     * <p/>
     * <tt>false</tt> will always be returned if the server is read-only.
     * 
     * 
     * @return                  Returns <code>true</code> if the calling code (data producers)
     *                          should go ahead and generate the data for
     *                          {@link #recordAuditValues(String, Map) recording}.
     * 
     * @since 3.3
     */
    public boolean areAuditValuesRequired();
    
    /**
     * Determine whether there are any audit applications registered to record data for the given
     * path.  This helper method gives data producers a shortcut in the event that nothing would
     * be recorded in any event.
     * 
     * @param path              the audit path
     * @return                  Returns <tt>true</tt> if there is at least one audit application
     *                          registered to handle the given path.
     * 
     * @since 3.4                         
     */
    public boolean areAuditValuesRequired(String path);
    
    /**
     * Delete audit entries for the given application and time range
     * 
     * @param applicationName   the name of the application being logged to
     * @param fromTime          the start time of entries to remove (inclusive and optional)
     * @param toTime            the end time of entries to remove (exclusive and optional)
     * @return                  Returns the number of entries deleted
     * 
     * @since 3.2
     */
    int deleteAuditEntries(String applicationName, Long fromTime, Long toTime);
    
    /**
     * Delete a discrete list of audit entries based on ID
     * 
     * @param auditEntryIds     the audit entry IDs to delete
     * @return                  Returns the number of entries deleted
     */
    int deleteAuditEntries(List<Long> auditEntryIds);
    
    /**
     * Check if an audit path is enabled.  The path will be disabled if it or any higher
     * path has been explicitly disabled.  Any disabled path will not be processed when
     * data is audited.
     * 
     * @param applicationName   the name of the application being logged to
     * @param path              the audit path to check or <tt>null</tt> to assume the
     *                          application's root path
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
     * @param path              the audit path to check or <tt>null</tt> to assume the
     *                          application's root path
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
     * @param path              the audit path to check or <tt>null</tt> to assume the
     *                          application's root path
     * 
     * @since 3.2
     */
    void disableAudit(String applicationName, String path);
    
    /**
     * Remove all disabled paths i.e. enable all per-path based auditing.  Auditing may still be
     * disabled globally.  This is primarily for test purposes; applications should know which
     * paths need {@link #enableAudit(String, String) enabling} or
     * {@link #disableAudit(String, String) disabled}.
     * 
     * @param applicationName   the name of the application
     * 
     * @since 3.2
     */
    void resetDisabledPaths(String applicationName);

    /**
     * Create an audit entry for the given map of values.  The map key is a path - starting with '/'
     * ({@link AuditApplication#AUDIT_PATH_SEPARATOR}) - relative to the root path provided.
     * <p/>
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
     * @throws IllegalStateException if the transaction state could not be determined
     * 
     * @since 3.2
     */
    Map<String, Serializable> recordAuditValues(String rootPath, Map<String, Serializable> values);
    
    /**
     * Find audit entries using the given parameters
     * 
     * @param callback          the data callback per entry
     * @param parameters        the parameters for the query (may not be <tt>null</tt>)
     * @param maxResults        the maximum number of results to retrieve (zero or negative to ignore)
     * 
     * @since 3.2
     */
    void auditQuery(AuditQueryCallback callback, AuditQueryParameters parameters, int maxResults);
}
