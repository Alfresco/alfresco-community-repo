/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.audit;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Records management audit service.
 * 
 * @author Gavin Cornwell
 */
public interface RecordsManagementAuditService
{
    public enum ReportFormat { HTML, JSON }
    
    public static final String RM_AUDIT_EVENT_UPDATE_RM_OBJECT = "Update RM Object";
    public static final String RM_AUDIT_EVENT_CREATE_RM_OBJECT = "Create RM Object";
    public static final String RM_AUDIT_EVENT_DELETE_RM_OBJECT = "Delete RM Object";
    public static final String RM_AUDIT_EVENT_LOGIN_SUCCESS = "Login.Success";
    public static final String RM_AUDIT_EVENT_LOGIN_FAILURE = "Login.Failure";
    
    public static final String RM_AUDIT_APPLICATION_NAME = "RM";
    public static final String RM_AUDIT_PATH_ROOT = "/RM";
    public static final String RM_AUDIT_SNIPPET_EVENT = "/event";
    public static final String RM_AUDIT_SNIPPET_PERSON = "/person";
    public static final String RM_AUDIT_SNIPPET_NAME = "/name";
    public static final String RM_AUDIT_SNIPPET_NODE = "/node";
    public static final String RM_AUDIT_SNIPPET_CHANGES = "/changes";
    public static final String RM_AUDIT_SNIPPET_BEFORE = "/before";
    public static final String RM_AUDIT_SNIPPET_AFTER = "/after";

    public static final String RM_AUDIT_DATA_PERSON_FULLNAME = "/RM/event/person/fullName";
    public static final String RM_AUDIT_DATA_PERSON_ROLES = "/RM/event/person/roles";
    public static final String RM_AUDIT_DATA_EVENT_NAME = "/RM/event/name/value";
    public static final String RM_AUDIT_DATA_NODE_NODEREF = "/RM/event/node/noderef";
    public static final String RM_AUDIT_DATA_NODE_NAME = "/RM/event/node/name";
    public static final String RM_AUDIT_DATA_NODE_TYPE = "/RM/event/node/type";
    public static final String RM_AUDIT_DATA_NODE_IDENTIFIER = "/RM/event/node/identifier";
    public static final String RM_AUDIT_DATA_NODE_NAMEPATH = "/RM/event/node/namePath";
    public static final String RM_AUDIT_DATA_NODE_CHANGES_BEFORE = "/RM/event/node/changes/before/value";
    public static final String RM_AUDIT_DATA_NODE_CHANGES_AFTER = "/RM/event/node/changes/after/value";

    public static final String RM_AUDIT_DATA_LOGIN_USERNAME = "/RM/login/args/userName/value";
    public static final String RM_AUDIT_DATA_LOGIN_FULLNAME = "/RM/login/no-error/fullName";
    public static final String RM_AUDIT_DATA_LOGIN_ERROR = "/RM/login/error/value";
    
    /**
     * Starts RM auditing.
     */
    void start();
    
    /**
     * Stops RM auditing.
     */
    void stop();
    
    /**
     * Clears the RM audit trail.
     */
    void clear();
    
    /**
     * Determines whether the RM audit log is currently enabled.
     * 
     * @return true if RM auditing is active false otherwise
     */
    boolean isEnabled();
    
    /**
     * Returns the date the RM audit was last started.
     * 
     * @return Date the audit was last started
     */
    Date getDateLastStarted();
    
    /**
     * Returns the date the RM audit was last stopped.
     * 
     * @return Date the audit was last stopped
     */
    Date getDateLastStopped();
    
    /**
     * An explicit call that RM actions can make to have the events logged.
     * 
     * @param action                    the action that will be performed
     * @param nodeRef                   the component being acted on
     * @param parameters                the action's parameters
     */
    void auditRMAction(RecordsManagementAction action, NodeRef nodeRef, Map<String, Serializable> parameters);
    
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
    NodeRef fileAuditTrailAsRecord(RecordsManagementAuditQueryParameters params, 
                NodeRef destination, ReportFormat format);
    
    /**
     * Retrieves a list of audit events.
     * 
     * @return List of audit events
     */
    List<AuditEvent> getAuditEvents();
}
