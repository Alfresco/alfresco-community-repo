/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.audit;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.module.org_alfresco_module_rm.audit.event.AuditEvent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Records management audit service.
 *
 * @author Gavin Cornwell
 */
@AlfrescoPublicApi
@SuppressWarnings("deprecation")
public interface RecordsManagementAuditService extends RecordsManagementAuditServiceDeprecated
{
    public enum ReportFormat { HTML, JSON }

    /**
     * Retrieves a list of audit events.
     *
     * @return List of audit events
     */
    List<AuditEvent> getAuditEvents();

    /**
     * Register audit event.
     * <p>
     * Creates an instance of a simple audit event and registers it with
     * the service.
     *
     * @param name  name of audit event
     * @param label display label of audit event
     */
    void registerAuditEvent(String name, String label);

    /**
     * Register audit event.
     *
     * @param auditEvent    audit event
     */
    void registerAuditEvent(AuditEvent auditEvent);

    /**
     * Audits an event, assumes no properties where modified and that the event should not be audited
     * immediately.
     *
     * @param nodeRef   node reference
     * @param eventName event name
     */
    void auditEvent(NodeRef nodeRef,
    				String eventName);

    /**
     * Audits an event, assumes that the event should not be audited immediately and not be removed if no property is changed.
     *
     * @param nodeRef   node reference
     * @param eventName event name
     * @param before    property values before event (this must be modifiable and may be changed by the method).
     * @param after     property values after event (this must be modifiable and may be changed by the method).
     */
    void auditEvent(NodeRef nodeRef,
            		String eventName,
            		Map<QName, Serializable> before,
            		Map<QName, Serializable> after);

    /**
     * Audit event, assumes not to be removed if no property is changed.
     *
     * @param nodeRef                      node reference
     * @param eventName                    event name
     * @param before                       property values before event (this must be modifiable and may be changed by the method).
     * @param after                        property values after event (this must be modifiable and may be changed by the method).
     * @param immediate                    true if event is to be audited immediately, false otherwise
     */
    void auditEvent(NodeRef nodeRef,
    		String eventName,
    		Map<QName, Serializable> before,
    		Map<QName, Serializable> after,
            boolean immediate);

    /**
     * Audit event.
     *
     * @param nodeRef                      node reference
     * @param eventName                    event name
     * @param before                       property values before event (this must be modifiable and may be changed by the method).
     * @param after                        property values after event (this must be modifiable and may be changed by the method).
     * @param immediate                    true if event is to be audited immediately, false otherwise
     * @param removeIfNoPropertyChanged    true if event is not audited when there are no properties changed, false otherwise
     */
    void auditEvent(NodeRef nodeRef,
                    String eventName,
                    Map<QName, Serializable> before,
                    Map<QName, Serializable> after,
                    boolean immediate,
                    boolean removeIfNoPropertyChanged);

    /**
     * Supply incremental changes as part of an event. This will either create a new event or update the existing details to put any supplied properties into the map.
     *
     * @param nodeRef                      node reference
     * @param eventName                    event name
     * @param before                       additional property values before event (this must be modifiable and may be changed by the method).
     * @param after                        additional property values after event (this must be modifiable and may be changed by the method).
     * @param removeIfNoPropertyChanged    true if event is not audited when there are no properties changed, false otherwise
     */
    void auditOrUpdateEvent(NodeRef nodeRef,
                            String eventName,
                            Map<QName, Serializable> before,
                            Map<QName, Serializable> after,
                            boolean removeIfNoPropertyChanged);

    /**
     * Determines whether the RM audit log is currently enabled.
     *
     * @param  filePlan	file plan
     * @return true if RM auditing is active false otherwise
     */
    boolean isAuditLogEnabled(NodeRef filePlan);

    /**
     * Start RM auditing.
     *
     * @param filePlan	file plan
     */
    void startAuditLog(NodeRef filePlan);

    /**
     * Stop RM auditing.
     *
     * @param filePlan	file plan
     */
    void stopAuditLog(NodeRef filePlan);

    /**
     * Clears the RM audit.
     *
     * @param filePlan	file plan
     */
    void clearAuditLog(NodeRef filePlan);

    /**
     * Returns the date the RM audit was last started.
     *
     * @param  filePlan		file plan
     * @return Date 		the audit was last started
     */
    Date getDateAuditLogLastStarted(NodeRef filePlan);

    /**
     * Returns the date the RM audit was last stopped.
     *
     * @return Date the audit was last stopped
     */
    Date getDateAuditLogLastStopped(NodeRef filePlan);

    /**
     * Retrieves a list of audit log entries using the provided parameters
     * represented by the RecordsManagementAuditQueryParameters instance.
     * <p>
     * The parameters are all optional so an empty RecordsManagementAuditQueryParameters
     * object will result in ALL audit log entries for the RM system being
     * returned. Setting the various parameters effectively filters the full
     * audit trail.
     *
     * @param params        Parameters to use to retrieve audit trail (never <tt>null</tt>)
     * @param format        The format the report should be produced in
     * @return              File containing JSON representation of audit trail
     */
    File getAuditTrailFile(RecordsManagementAuditQueryParameters params, ReportFormat format);

    /**
     * Retrieves a list of audit log entries using the provided parameters
     * represented by the RecordsManagementAuditQueryParameters instance.
     * <p>
     * The parameters are all optional so an empty RecordsManagementAuditQueryParameters
     * object will result in ALL audit log entries for the RM system being
     * returned. Setting the various parameters effectively filters the full
     * audit trail.
     *
     * @param params        Parameters to use to retrieve audit trail (never <tt>null</tt>)
     * @return              All entries for the audit trail
     */
    List<RecordsManagementAuditEntry> getAuditTrail(RecordsManagementAuditQueryParameters params);

    /**
     * Retrieves a list of audit log entries using the provided parameters
     * represented by the RecordsManagementAuditQueryParameters instance and
     * then files the resulting log as an undeclared record in the record folder
     * represented by the given NodeRef.
     * <p>
     * The parameters are all optional so an empty RecordsManagementAuditQueryParameters
     * object will result in ALL audit log entries for the RM system being
     * returned. Setting the various parameters effectively filters the full
     * audit trail.
     *
     * @param params        Parameters to use to retrieve audit trail (never <tt>null</tt>)
     * @param destination   NodeRef representing a record folder in which to file the audit log
     * @param format        The format the report should be produced in
     * @return              NodeRef of the undeclared record filed
     */
    NodeRef fileAuditTrailAsRecord(RecordsManagementAuditQueryParameters params, NodeRef destination, ReportFormat format);
}
