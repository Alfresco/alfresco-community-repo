/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.audit.event.AuditEvent;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.repo.audit.model.AuditApplication;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.audit.AuditQueryParameters;
import org.alfresco.service.cmr.audit.AuditService;
import org.alfresco.service.cmr.audit.AuditService.AuditQueryCallback;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.PropertyMap;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Records Management Audit Service Implementation.
 *
 * @author Gavin Cornwell
 * @since 3.2
 */
public class RecordsManagementAuditServiceImpl extends AbstractLifecycleBean
                                               implements RecordsManagementAuditService
{
    /** I18N */
    private static final String MSG_TRAIL_FILE_FAIL = "rm.audit.trail-file-fail";
    private static final String MSG_AUDIT_REPORT = "rm.audit.audit-report";

    /** Logger */
    private static Log logger = LogFactory.getLog(RecordsManagementAuditServiceImpl.class);

    private static final String ACCESS_AUDIT_CAPABILITY = "AccessAudit";
    
    private static final String KEY_RM_AUDIT_NODE_RECORDS = "RMAUditNodeRecords";

    protected static final String RM_AUDIT_EVENT_LOGIN_SUCCESS = "Login.Success";
    protected static final String RM_AUDIT_EVENT_LOGIN_FAILURE = "Login.Failure";

    protected static final String RM_AUDIT_APPLICATION_NAME = "RM";
    protected static final String RM_AUDIT_PATH_ROOT = "/RM";
    protected static final String RM_AUDIT_SNIPPET_EVENT = "/event";
    protected static final String RM_AUDIT_SNIPPET_PERSON = "/person";
    protected static final String RM_AUDIT_SNIPPET_NAME = "/name";
    protected static final String RM_AUDIT_SNIPPET_NODE = "/node";
    protected static final String RM_AUDIT_SNIPPET_CHANGES = "/changes";
    protected static final String RM_AUDIT_SNIPPET_BEFORE = "/before";
    protected static final String RM_AUDIT_SNIPPET_AFTER = "/after";

    protected static final String RM_AUDIT_DATA_PERSON_FULLNAME = "/RM/event/person/fullName";
    protected static final String RM_AUDIT_DATA_PERSON_ROLES = "/RM/event/person/roles";
    protected static final String RM_AUDIT_DATA_EVENT_NAME = "/RM/event/name/value";
    protected static final String RM_AUDIT_DATA_NODE_NODEREF = "/RM/event/node/noderef";
    protected static final String RM_AUDIT_DATA_NODE_NAME = "/RM/event/node/name";
    protected static final String RM_AUDIT_DATA_NODE_TYPE = "/RM/event/node/type";
    protected static final String RM_AUDIT_DATA_NODE_IDENTIFIER = "/RM/event/node/identifier";
    protected static final String RM_AUDIT_DATA_NODE_NAMEPATH = "/RM/event/node/namePath";
    protected static final String RM_AUDIT_DATA_NODE_CHANGES_BEFORE = "/RM/event/node/changes/before/value";
    protected static final String RM_AUDIT_DATA_NODE_CHANGES_AFTER = "/RM/event/node/changes/after/value";

    protected static final String RM_AUDIT_DATA_LOGIN_USERNAME = "/RM/login/args/userName/value";
    protected static final String RM_AUDIT_DATA_LOGIN_FULLNAME = "/RM/login/no-error/fullName";
    protected static final String RM_AUDIT_DATA_LOGIN_ERROR = "/RM/login/error/value";

    /* Provide Backward compatibility with DOD5015 Audit Events  RM-904*/
    protected static final String DOD5015_AUDIT_APPLICATION_NAME = "DOD5015";
    protected static final String DOD5015_AUDIT_PATH_ROOT = "/DOD5015";
    protected static final String DOD5015_AUDIT_SNIPPET_EVENT = "/event";
    protected static final String DOD5015_AUDIT_SNIPPET_PERSON = "/person";
    protected static final String DOD5015_AUDIT_SNIPPET_NAME = "/name";
    protected static final String DOD5015_AUDIT_SNIPPET_NODE = "/node";
    protected static final String DOD5015_AUDIT_SNIPPET_CHANGES = "/changes";
    protected static final String DOD5015_AUDIT_SNIPPET_BEFORE = "/before";
    protected static final String DOD5015_AUDIT_SNIPPET_AFTER = "/after";

    protected static final String DOD5015_AUDIT_DATA_PERSON_FULLNAME = "/DOD5015/event/person/fullName";
    protected static final String DOD5015_AUDIT_DATA_PERSON_ROLES = "/DOD5015/event/person/roles";
    protected static final String DOD5015_AUDIT_DATA_EVENT_NAME = "/DOD5015/event/name/value";
    protected static final String DOD5015_AUDIT_DATA_NODE_NODEREF = "/DOD5015/event/node/noderef";
    protected static final String DOD5015_AUDIT_DATA_NODE_NAME = "/DOD5015/event/node/name";
    protected static final String DOD5015_AUDIT_DATA_NODE_TYPE = "/DOD5015/event/node/type";
    protected static final String DOD5015_AUDIT_DATA_NODE_IDENTIFIER = "/DOD5015/event/node/identifier";
    protected static final String DOD5015_AUDIT_DATA_NODE_NAMEPATH = "/DOD5015/event/node/namePath";
    protected static final String DOD5015_AUDIT_DATA_NODE_CHANGES_BEFORE = "/DOD5015/event/node/changes/before/value";
    protected static final String DOD5015_AUDIT_DATA_NODE_CHANGES_AFTER = "/DOD5015/event/node/changes/after/value";

    protected static final String DOD5015_AUDIT_DATA_LOGIN_USERNAME = "/DOD5015/login/args/userName/value";
    protected static final String DOD5015_AUDIT_DATA_LOGIN_FULLNAME = "/DOD5015/login/no-error/fullName";
    protected static final String DOD5015_AUDIT_DATA_LOGIN_ERROR = "/DOD5015/login/error/value";
    /* End Backward compatibility with DOD5015 Audit Events */

    protected static final String AUDIT_TRAIL_FILE_PREFIX = "audit_";
    protected static final String AUDIT_TRAIL_JSON_FILE_SUFFIX = ".json";
    protected static final String AUDIT_TRAIL_HTML_FILE_SUFFIX = ".html";

    /** Audit auditing events */
    private static final String AUDIT_EVENT_START = "audit.start";
    private static final String MSG_AUDIT_START = "rm.audit.audit-start";
    private static final String AUDIT_EVENT_STOP = "audit.stop";
    private static final String MSG_AUDIT_STOP = "rm.audit.audit-stop";
    private static final String AUDIT_EVENT_CLEAR = "audit.clear";
    private static final String MSG_AUDIT_CLEAR = "rm.audit.audit-clear";
    private static final String AUDIT_EVENT_VIEW = "audit.view";
    private static final String MSG_AUDIT_VIEW = "rm.audit.audit-view";

    private PolicyComponent policyComponent;
    private DictionaryService dictionaryService;
    private TransactionService transactionService;
    private NodeService nodeService;
    private ContentService contentService;
    private AuditComponent auditComponent;
    private AuditService auditService;
    private RecordsManagementActionService rmActionService;
    private FilePlanService filePlanService;
    private NamespaceService namespaceService;
    protected CapabilityService capabilityService;

    private boolean shutdown = false;

    private List<String> ignoredAuditProperties;

    private List<QName> propertiesToBeRemoved = new ArrayList<QName>();

    private RMAuditTxnListener txnListener = new RMAuditTxnListener();

    /** Registered and initialised records management auditEvents */
    private Map<String, AuditEvent> auditEvents = new HashMap<String, AuditEvent>();

    /**
     * Set the component used to bind to behaviour callbacks
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Provides user-readable names for types
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Set the component used to start new transactions
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Sets the NodeService instance
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the ContentService instance
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * The component to create audit events
     */
    public void setAuditComponent(AuditComponent auditComponent)
    {
        this.auditComponent = auditComponent;
    }

    /**
     * Sets the AuditService instance
     */
    public void setAuditService(AuditService auditService)
    {
        this.auditService = auditService;
    }

    /**
     * Sets the RecordsManagementActionService instance
     */
    public void setRecordsManagementActionService(RecordsManagementActionService rmActionService)
    {
        this.rmActionService = rmActionService;
    }

    /**
     * @param filePlanService	file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
		this.filePlanService = filePlanService;
	}

    /**
     * @param namespaceService namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }
    
    
    

    /**
     * @param ignoredAuditProperties
     */
    public void setIgnoredAuditProperties(List<String> ignoredAuditProperties)
    {
        this.ignoredAuditProperties = ignoredAuditProperties;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService#registerAuditEvent(java.lang.String, java.lang.String)
     */
    @Override
    public void registerAuditEvent(String name, String label)
    {
        registerAuditEvent(new AuditEvent(name, label));
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService#registerAuditEvent(org.alfresco.module.org_alfresco_module_rm.audit.event.AuditEvent)
     */
    @Override
    public void registerAuditEvent(AuditEvent auditEvent)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Registering audit event " + auditEvent.getName());
        }

        this.auditEvents.put(auditEvent.getName(), auditEvent);
    }

    /**
     * Checks that all necessary properties have been set.
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "contentService", contentService);
        PropertyCheck.mandatory(this, "auditComponent", auditComponent);
        PropertyCheck.mandatory(this, "auditService", auditService);
        PropertyCheck.mandatory(this, "rmActionService", rmActionService);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "filePlanService", filePlanService);

        // register audit auditing events
        registerAuditEvent(AUDIT_EVENT_CLEAR, MSG_AUDIT_CLEAR);
        registerAuditEvent(AUDIT_EVENT_START, MSG_AUDIT_START);
        registerAuditEvent(AUDIT_EVENT_STOP, MSG_AUDIT_STOP);
        registerAuditEvent(AUDIT_EVENT_VIEW, MSG_AUDIT_VIEW);

        // properties to be ignored by audit
        for (String qname : ignoredAuditProperties)
        {
            this.propertiesToBeRemoved.add(QName.createQName(qname, this.namespaceService));
        }
    }

    /**
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onBootstrap(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        shutdown = false;
    }

    /**
     * @see org.springframework.extensions.surf.util.AbstractLifecycleBean#onShutdown(org.springframework.context.ApplicationEvent)
     */
    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        shutdown = true;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService#isAuditLogEnabled(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean isAuditLogEnabled(NodeRef filePlan)
    {
    	ParameterCheck.mandatory("filePlan", filePlan);
    	// TODO use file plan to scope audit log

        return auditService.isAuditEnabled(
                RM_AUDIT_APPLICATION_NAME,
                RM_AUDIT_PATH_ROOT);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService#startAuditLog(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void startAuditLog(NodeRef filePlan)
    {
    	ParameterCheck.mandatory("filePlan", filePlan);
    	// TODO use file plan to scope audit log

        auditService.enableAudit(
                RM_AUDIT_APPLICATION_NAME,
                RM_AUDIT_PATH_ROOT);

        if (logger.isInfoEnabled())
        {
            logger.info("Started Records Management auditing");
        }

        auditEvent(filePlan, AUDIT_EVENT_START, null, null, true);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService#stopAuditLog(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void stopAuditLog(NodeRef filePlan)
    {
    	ParameterCheck.mandatory("filePlan", filePlan);
    	// TODO use file plan to scope audit log

        auditEvent(filePlan, AUDIT_EVENT_STOP, null, null, true);

        auditService.disableAudit(
                RM_AUDIT_APPLICATION_NAME,
                RM_AUDIT_PATH_ROOT);

        if (logger.isInfoEnabled())
        {
            logger.info("Stopped Records Management auditing");
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService#clearAuditLog(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void clearAuditLog(NodeRef filePlan)
    {
    	ParameterCheck.mandatory("filePlan", filePlan);
    	// TODO use file plan to scope audit log

        auditService.clearAudit(RM_AUDIT_APPLICATION_NAME, null, null);
        if (logger.isInfoEnabled())
        {
            logger.debug("Records Management audit log has been cleared");
        }

        auditEvent(filePlan, AUDIT_EVENT_CLEAR, null, null, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getDateAuditLogLastStarted(NodeRef filePlan)
    {
    	ParameterCheck.mandatory("filePlan", filePlan);
    	// TODO use file plan to scope audit log

        // TODO: return proper date, for now it's today's date
        return getStartOfDay(new Date());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService#getDateAuditLogLastStopped(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public Date getDateAuditLogLastStopped(NodeRef filePlan)
    {
    	ParameterCheck.mandatory("filePlan", filePlan);
    	// TODO use file plan to scope audit log

        // TODO: return proper date, for now it's today's date
        return getEndOfDay(new Date());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService#auditEvent(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public void auditEvent(NodeRef nodeRef, String eventName)
    {
        auditEvent(nodeRef, eventName, null, null, false, false);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService#auditEvent(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.util.Map, java.util.Map)
     */
    @Override
    public void auditEvent(NodeRef nodeRef, String eventName, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        auditEvent(nodeRef, eventName, before, after, false, false);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService#auditEvent(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.util.Map, java.util.Map, boolean)
     */
    @Override
    public void auditEvent(NodeRef nodeRef, String eventName, Map<QName, Serializable> before, Map<QName, Serializable> after, boolean immediate)
    {
    	auditEvent(nodeRef, eventName, before, after, immediate, false);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService#auditEvent(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, java.util.Map, java.util.Map, boolean)
     */
    @Override
    public void auditEvent(NodeRef nodeRef, String eventName, Map<QName, Serializable> before, Map<QName, Serializable> after, boolean immediate, boolean removeIfNoPropertyChanged)
    {
        // deal with immediate auditing if required
        if (immediate)
        {
            Map<String, Serializable> auditMap = buildAuditMap(nodeRef, eventName, before, after, removeIfNoPropertyChanged);
            auditComponent.recordAuditValues(RM_AUDIT_PATH_ROOT, auditMap);
        }
        else
        {
            Set<RMAuditNode> auditDetails = TransactionalResourceHelper.getSet(KEY_RM_AUDIT_NODE_RECORDS);
            AlfrescoTransactionSupport.bindListener(txnListener);

            // RM-936: Eliminate multiple audit maps from being generated when events with the same name are required to be fired multiple times in the same transaction.
            // Check if auditDetails already contains an auditedNode with the same combination of nodeRef and eventName.
            boolean auditNodeAlreadyExists = false;
            for (RMAuditNode existingRMAuditNode : auditDetails)
            {
                if (existingRMAuditNode.getNodeRef().equals(nodeRef) && existingRMAuditNode.getEventName().equals(eventName))
                {
                    // If there exists such an auditNode, update its 'after' properties with the latest set of properties and leave its 'before' properties unchanged so that it
                    // retains the original set of properties. The first 'before' and last 'after' will be diff'ed when comes to building the auditMap later when the transaction
                    // commits.
                    existingRMAuditNode.setNodePropertiesAfter(after);
                    auditNodeAlreadyExists = true;
                    break;
                }
            }

            if (!auditNodeAlreadyExists)
            {
                // Create a new auditNode if it doesn't already exist
                RMAuditNode auditedNode = new RMAuditNode();
                auditedNode.setNodeRef(nodeRef);
                auditedNode.setEventName(eventName);
                auditedNode.setNodePropertiesBefore(before);
                auditedNode.setNodePropertiesAfter(after);
                auditedNode.setRemoveIfNoPropertyChanged(removeIfNoPropertyChanged);

                auditDetails.add(auditedNode);
            }
        }
    }

    /**
     * Helper method to build audit map
     *
     * @param nodeRef
     * @param eventName
     * @return
     * @since 2.0.3
     */
    private Map<String, Serializable> buildAuditMap(NodeRef nodeRef, String eventName, Map<QName, Serializable> propertiesBefore, Map<QName, Serializable> propertiesAfter, boolean removeOnNoPropertyChange)
    {
        Map<String, Serializable> auditMap = new HashMap<String, Serializable>(13);
        auditMap.put(
                AuditApplication.buildPath(
                        RM_AUDIT_SNIPPET_EVENT,
                        RM_AUDIT_SNIPPET_NAME),
                        eventName);

        if (nodeRef != null)
        {
            auditMap.put(
                    AuditApplication.buildPath(
                            RM_AUDIT_SNIPPET_EVENT,
                            RM_AUDIT_SNIPPET_NODE),
                            nodeRef);
        }

        // Filter out any properties to be audited if specified in the Spring configuration.
        if (!ignoredAuditProperties.isEmpty())
        {
            removeAuditProperties(propertiesBefore, propertiesAfter);
        }

        // Property changes
        Pair<Map<QName, Serializable>, Map<QName, Serializable>> deltaPair = PropertyMap.getBeforeAndAfterMapsForChanges(propertiesBefore, propertiesAfter);

        // If both the first and second Map in the deltaPair are empty and removeOnNoPropertyChange is true, the entire auditMap is discarded so it won't be audited.
        if (deltaPair.getFirst().isEmpty() && deltaPair.getSecond().isEmpty() && removeOnNoPropertyChange)
        {
            auditMap.clear();
        }
        else
        {
            auditMap.put(
                    AuditApplication.buildPath(
                            RM_AUDIT_SNIPPET_EVENT,
                            RM_AUDIT_SNIPPET_NODE,
                            RM_AUDIT_SNIPPET_CHANGES,
                            RM_AUDIT_SNIPPET_BEFORE),
                    (Serializable) deltaPair.getFirst());
            auditMap.put(
                    AuditApplication.buildPath(
                            RM_AUDIT_SNIPPET_EVENT,
                            RM_AUDIT_SNIPPET_NODE,
                            RM_AUDIT_SNIPPET_CHANGES,
                            RM_AUDIT_SNIPPET_AFTER),
                    (Serializable) deltaPair.getSecond());
        }
        return auditMap;
    }

    /**
     * Helper method to remove system properties from maps
     *
     * @param properties
     */
    private void removeAuditProperties(Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        if (before != null)
        {
            before.keySet().removeAll(this.propertiesToBeRemoved);
        }
        if (after != null)
        {
            after.keySet().removeAll(this.propertiesToBeRemoved);
        }
    }

    /**
     * A <b>stateless</b> transaction listener for RM auditing. This component picks up the data of modified nodes and generates the audit information.
     * <p/>
     * This class is not static so that the instances will have access to the action's implementation.
     *
     * @author Derek Hulley
     * @since 3.2
     */
    private class RMAuditTxnListener extends TransactionListenerAdapter
    {
        private final Log logger = LogFactory.getLog(RecordsManagementAuditServiceImpl.class);

        /*
         * Equality and hashcode generation are left unimplemented; we expect to only have a single
         * instance of this class per action.
         */

        /**
         * Get the action parameters from the transaction and audit them.
         */
        @Override
        public void afterCommit()
        {
            final Set<RMAuditNode> auditedNodes = TransactionalResourceHelper.getSet(KEY_RM_AUDIT_NODE_RECORDS);

            // Start a *new* read-write transaction to audit in
            RetryingTransactionCallback<Void> auditCallback = new RetryingTransactionCallback<Void>()
            {
                @Override
                public Void execute() throws Throwable
                {
                    auditInTxn(auditedNodes);
                    return null;
                }
            };
            transactionService.getRetryingTransactionHelper().doInTransaction(auditCallback, false, true);
        }

        /**
         * Do the actual auditing, assuming the presence of a viable transaction
         *
         * @param auditedNodes              details of the nodes that were modified
         */
        private void auditInTxn(Set<RMAuditNode> auditedNodes) throws Throwable
        {
            // Go through all the audit information and audit it
            boolean auditedSomething = false;
            for (RMAuditNode auditedNode : auditedNodes)
            {
                NodeRef nodeRef = auditedNode.getNodeRef();

                // If the node is gone, then do nothing
                if (nodeRef != null && !nodeService.exists(nodeRef))
                {
                    continue;
                }

                // build the audit map
                Map<String, Serializable> auditMap = buildAuditMap(nodeRef,
                                                                   auditedNode.getEventName(),
                                                                   auditedNode.getNodePropertiesBefore(),
                                                                   auditedNode.getNodePropertiesAfter(),
                                                                   auditedNode.getRemoveIfNoPropertyChanged());
                // Audit it
                if (logger.isDebugEnabled())
                {
                    logger.debug("RM Audit: Auditing values: \n" + auditMap);
                }
                auditMap = auditComponent.recordAuditValues(RM_AUDIT_PATH_ROOT, auditMap);
                if (auditMap.isEmpty())
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("RM Audit: Nothing was audited.");
                    }
                }
                else
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("RM Audit: Audited values: \n" + auditMap);
                    }
                    // We must commit the transaction to get the values in
                    auditedSomething = true;
                }
            }
            // Check if anything was audited
            if (!auditedSomething)
            {
                // Nothing was audited, so do nothing
                RetryingTransactionHelper.getActiveUserTransaction().setRollbackOnly();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getAuditTrailFile(RecordsManagementAuditQueryParameters params, ReportFormat format)
    {
        ParameterCheck.mandatory("params", params);

        Writer fileWriter = null;
        try
        {
            File auditTrailFile = TempFileProvider.createTempFile(AUDIT_TRAIL_FILE_PREFIX,
                format == ReportFormat.HTML ? AUDIT_TRAIL_HTML_FILE_SUFFIX : AUDIT_TRAIL_JSON_FILE_SUFFIX);
            fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(auditTrailFile),"UTF8"));
            // Get the results, dumping to file
            getAuditTrailImpl(params, null, fileWriter, format);
            // Done
            return auditTrailFile;
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException(MSG_TRAIL_FILE_FAIL, e);
        }
        finally
        {
            // close the writer
            if (fileWriter != null)
            {
                try { fileWriter.close(); } catch (IOException closeEx) {}
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RecordsManagementAuditEntry> getAuditTrail(RecordsManagementAuditQueryParameters params)
    {
        ParameterCheck.mandatory("params", params);

        List<RecordsManagementAuditEntry> entries = new ArrayList<RecordsManagementAuditEntry>(50);
        try
        {
            getAuditTrailImpl(params, entries, null, null);
            // Done
            return entries;
        }
        catch (IOException e)
        {
            // Should be
            throw new AlfrescoRuntimeException(MSG_TRAIL_FILE_FAIL, e);
        }
    }

    /**
     * Get the audit trail, optionally dumping the results the the given writer dumping to a list.
     *
     * @param params                the search parameters
     * @param results               the list to which individual results will be dumped
     * @param writer                Writer to write the audit trail
     * @param reportFormat          Format to write the audit trail in, ignored if writer is <code>null</code>
     */
    private void getAuditTrailImpl(
            final RecordsManagementAuditQueryParameters params,
            final List<RecordsManagementAuditEntry> results,
            final Writer writer,
            final ReportFormat reportFormat)
            throws IOException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Retrieving audit trail in '" + reportFormat + "' format using parameters: " + params);
        }

        // define the callback
        AuditQueryCallback callback = new AuditQueryCallback()
        {
            private boolean firstEntry = true;


            @Override
            public boolean valuesRequired()
            {
                return true;
            }

            /**
             * Just log the error, but continue
             */
            @Override
            public boolean handleAuditEntryError(Long entryId, String errorMsg, Throwable error)
            {
                logger.warn(errorMsg, error);
                return true;
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean handleAuditEntry(
                    Long entryId,
                    String applicationName,
                    String user,
                    long time,
                    Map<String, Serializable> values)
            {
                // Check for context shutdown
                if (shutdown)
                {
                    return false;
                }


                Date timestamp = new Date(time);
                String eventName = null;
                String fullName = null;
                String userRoles = null;
                NodeRef nodeRef = null;
                String nodeName = null;
                String nodeType = null;
                String nodeIdentifier = null;
                String namePath = null;
                Map<QName, Serializable> beforeProperties = null;
                Map<QName, Serializable> afterProperties = null;

                if (values.containsKey(RM_AUDIT_DATA_EVENT_NAME))
                {
                    // This data is /RM/event/...
                    eventName = (String) values.get(RM_AUDIT_DATA_EVENT_NAME);
                    fullName = (String) values.get(RM_AUDIT_DATA_PERSON_FULLNAME);
                    userRoles = (String) values.get(RM_AUDIT_DATA_PERSON_ROLES);
                    nodeRef = (NodeRef) values.get(RM_AUDIT_DATA_NODE_NODEREF);
                    nodeName = (String) values.get(RM_AUDIT_DATA_NODE_NAME);
                    QName nodeTypeQname = (QName) values.get(RM_AUDIT_DATA_NODE_TYPE);
                    nodeIdentifier = (String) values.get(RM_AUDIT_DATA_NODE_IDENTIFIER);
                    namePath = (String) values.get(RM_AUDIT_DATA_NODE_NAMEPATH);
                    beforeProperties = (Map<QName, Serializable>) values.get(RM_AUDIT_DATA_NODE_CHANGES_BEFORE);
                    afterProperties = (Map<QName, Serializable>) values.get(RM_AUDIT_DATA_NODE_CHANGES_AFTER);

                    // Convert some of the values to recognizable forms
                    nodeType = null;
                    if (nodeTypeQname != null)
                    {
                        TypeDefinition typeDef = dictionaryService.getType(nodeTypeQname);
                        nodeType = (typeDef != null) ? typeDef.getTitle(dictionaryService) : null;
                    }
                }
                else if (values.containsKey(DOD5015_AUDIT_DATA_EVENT_NAME))
                {
                    // This data is /RM/event/...
                    eventName = (String) values.get(DOD5015_AUDIT_DATA_EVENT_NAME);
                    fullName = (String) values.get(DOD5015_AUDIT_DATA_PERSON_FULLNAME);
                    userRoles = (String) values.get(DOD5015_AUDIT_DATA_PERSON_ROLES);
                    nodeRef = (NodeRef) values.get(DOD5015_AUDIT_DATA_NODE_NODEREF);
                    nodeName = (String) values.get(DOD5015_AUDIT_DATA_NODE_NAME);
                    QName nodeTypeQname = (QName) values.get(DOD5015_AUDIT_DATA_NODE_TYPE);
                    nodeIdentifier = (String) values.get(DOD5015_AUDIT_DATA_NODE_IDENTIFIER);
                    namePath = (String) values.get(DOD5015_AUDIT_DATA_NODE_NAMEPATH);
                    beforeProperties = (Map<QName, Serializable>) values.get( DOD5015_AUDIT_DATA_NODE_CHANGES_BEFORE);
                    afterProperties = (Map<QName, Serializable>) values.get(DOD5015_AUDIT_DATA_NODE_CHANGES_AFTER);

                    // Convert some of the values to recognizable forms
                    nodeType = null;
                    if (nodeTypeQname != null)
                    {
                        TypeDefinition typeDef = dictionaryService.getType(nodeTypeQname);
                        nodeType = (typeDef != null) ? typeDef.getTitle(dictionaryService) : null;
                    }
                }
                else if (values.containsKey(RM_AUDIT_DATA_LOGIN_USERNAME))
                {
                    user = (String) values.get(RM_AUDIT_DATA_LOGIN_USERNAME);
                    if (values.containsKey(RM_AUDIT_DATA_LOGIN_ERROR))
                    {
                        eventName = RM_AUDIT_EVENT_LOGIN_FAILURE;
                        // The user didn't log in
                        fullName = user;
                    }
                    else
                    {
                        eventName = RM_AUDIT_EVENT_LOGIN_SUCCESS;
                        fullName = (String) values.get(RM_AUDIT_DATA_LOGIN_FULLNAME);
                    }
                }
                else if (values.containsKey(DOD5015_AUDIT_DATA_LOGIN_USERNAME))
                {
                    user = (String) values.get(DOD5015_AUDIT_DATA_LOGIN_USERNAME);
                    if (values.containsKey(DOD5015_AUDIT_DATA_LOGIN_ERROR))
                    {
                        eventName = RM_AUDIT_EVENT_LOGIN_FAILURE;
                        // The user didn't log in
                        fullName = user;
                    }
                    else
                    {
                        eventName = RM_AUDIT_EVENT_LOGIN_SUCCESS;
                        fullName = (String) values.get(DOD5015_AUDIT_DATA_LOGIN_FULLNAME);
                    }
                }
                else
                {
                    // This is not recognisable data
                    logger.warn(
                            "Unable to process audit entry for RM.  Unexpected data: \n" +
                            "   Entry: " + entryId + "\n" +
                            "   Data:  " + values);
                    // Skip it
                    return true;
                }
                
                if(nodeRef != null && nodeService.exists(nodeRef) &&
                    !AccessStatus.ALLOWED.equals(
                        capabilityService.getCapabilityAccessState(nodeRef, ACCESS_AUDIT_CAPABILITY)))
                {
                    return true;
                }

                // TODO: Refactor this to use the builder pattern
                RecordsManagementAuditEntry entry = new RecordsManagementAuditEntry(
                        timestamp,
                        user,
                        fullName,
                        // A concatenated string of roles
                        userRoles,
                        nodeRef,
                        nodeName,
                        nodeType,
                        eventName,
                        nodeIdentifier,
                        namePath,
                        beforeProperties,
                        afterProperties);

                // write out the entry to the file in requested format
                writeEntryToFile(entry);

                if (results != null)
                {
                    results.add(entry);
                }

                if (logger.isDebugEnabled())
                {
                    logger.debug("   " + entry);
                }

                // Keep going
                return true;
            }

            private void writeEntryToFile(RecordsManagementAuditEntry entry)
            {
                if (writer == null)
                {
                    return;
                }
                try
                {
                    if (!firstEntry)
                    {
                        if (reportFormat == ReportFormat.HTML)
                        {
                            writer.write("\n");
                        }
                        else
                        {
                            writer.write(",");
                        }
                    }
                    else
                    {
                        firstEntry = false;
                    }

                    // write the entry to the file
                    if (reportFormat == ReportFormat.JSON)
                    {
                        writer.write("\n\t\t");
                    }

                    writeAuditTrailEntry(writer, entry, reportFormat);
                }
                catch (IOException ioe)
                {
                    throw new AlfrescoRuntimeException(MSG_TRAIL_FILE_FAIL, ioe);
                }
            }
        };

        String user = params.getUser();
        Long fromTime = getFromDateTime(params.getDateFrom());
        Long toTime = getToDateTime(params.getDateTo());
        NodeRef nodeRef = params.getNodeRef();
        int maxEntries = params.getMaxEntries();
        // Reverse order if the results are limited
        boolean forward = maxEntries > 0 ? false : true;

        // start the audit trail report
        writeAuditTrailHeader(writer, params, reportFormat);

        if (logger.isDebugEnabled())
        {
            logger.debug("RM Audit: Issuing query: " + params);
        }

        // Build audit query parameters
        AuditQueryParameters dod5015AuditQueryParams = new AuditQueryParameters();
        dod5015AuditQueryParams.setForward(forward);
        dod5015AuditQueryParams.setApplicationName(DOD5015_AUDIT_APPLICATION_NAME);
        dod5015AuditQueryParams.setUser(user);
        dod5015AuditQueryParams.setFromTime(fromTime);
        dod5015AuditQueryParams.setToTime(toTime);
        if (nodeRef != null)
        {
            dod5015AuditQueryParams.addSearchKey(DOD5015_AUDIT_DATA_NODE_NODEREF, nodeRef);
        }

        //
        AuditQueryParameters auditQueryParams = new AuditQueryParameters();
        auditQueryParams.setForward(forward);
        auditQueryParams.setApplicationName(RM_AUDIT_APPLICATION_NAME);
        auditQueryParams.setUser(user);
        auditQueryParams.setFromTime(fromTime);
        auditQueryParams.setToTime(toTime);
        if (nodeRef != null)
        {
            auditQueryParams.addSearchKey(RM_AUDIT_DATA_NODE_NODEREF, nodeRef);
        }
        else if (params.getEvent() != null)
        {
            auditQueryParams.addSearchKey(RM_AUDIT_DATA_EVENT_NAME, params.getEvent());
        }

        // Get audit entries
        auditService.auditQuery(callback, dod5015AuditQueryParams, maxEntries);
        auditService.auditQuery(callback, auditQueryParams, maxEntries);

        // finish off the audit trail report
        writeAuditTrailFooter(writer, reportFormat);

        // audit that the audit has been view'ed
        if (nodeRef == null)
        {
            // grab the default file plan, but don't fail if it can't be found!
            nodeRef = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
        }
        auditEvent(nodeRef, AUDIT_EVENT_VIEW, null, null, true);
    }

    /**
     * Calculates the start of the given date.
     * For example, if you had the date time of 12 Aug 2013 12:10:15.158
     * the result would be 12 Aug 2013 00:00:00.000.
     *
     * @param date The date for which the start should be calculated.
     * @return Returns the start of the given date.
     */
    private Date getStartOfDay(Date date)
    {
        return DateUtils.truncate(date == null ? new Date() : date, Calendar.DATE);
    }

    /**
     * Gets the start of the from date
     * @see org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditServiceImpl.getStartOfDay()
     *
     * @param date The date for which the start should be retrieved.
     * @return Returns null if the given date is null, otherwise the start of the given day.
     */
    private Date getFromDate(Date date)
    {
        return date == null ? null : getStartOfDay(date);
    }

    /**
     * Returns the number of milliseconds for the "from date".
     *
     * @param date The date for which the number of milliseconds should retrieved.
     * @return Returns null if the given date is null, otherwise the number of milliseconds for the given date.
     */
    private Long getFromDateTime(Date date)
    {
        Long fromDateTime = null;
        Date fromDate = getFromDate(date);
        if (fromDate != null)
        {
            fromDateTime = Long.valueOf(fromDate.getTime());
        }
        return fromDateTime;
    }

    /**
     * Calculates the end of the given date.
     * For example, if you had the date time of 12 Aug 2013 12:10:15.158
     * the result would be 12 Aug 2013 23:59:59.999.
     *
     * @param date The date for which the end should be calculated.
     * @return Returns the end of the given date.
     */
    private Date getEndOfDay(Date date)
    {
        return DateUtils.addMilliseconds(DateUtils.ceiling(date == null ? new Date() : date, Calendar.DATE), -1);
    }

    /**
     * Gets the end of the from date
     * @see org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditServiceImpl.getEndOfDay()
     *
     * @param date The date for which the end should be retrieved.
     * @return Returns null if the given date is null, otherwise the end of the given day.
     */
    private Date getToDate(Date date)
    {
        return date == null ? null : getEndOfDay(date);
    }

    /**
     * Returns the number of milliseconds for the "to date".
     *
     * @param date The date for which the number of milliseconds should retrieved.
     * @return Returns null if the given date is null, otherwise the number of milliseconds for the given date.
     */
    private Long getToDateTime(Date date)
    {
        Long toDateTime = null;
        Date toDate = getToDate(date);
        if (toDate != null)
        {
            toDateTime = Long.valueOf(toDate.getTime());
        }
        return toDateTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeRef fileAuditTrailAsRecord(RecordsManagementAuditQueryParameters params,
                NodeRef destination, ReportFormat format)
    {
        ParameterCheck.mandatory("params", params);
        ParameterCheck.mandatory("destination", destination);

        // NOTE: the underlying RM services will check all the remaining pre-conditions

        NodeRef record = null;

        // get the audit trail for the provided parameters
        File auditTrail = this.getAuditTrailFile(params, format);

        if (logger.isDebugEnabled())
        {
            logger.debug("Filing audit trail in file " + auditTrail.getAbsolutePath() +
                        " as a record in record folder: " + destination);
        }

        try
        {
            Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
            properties.put(ContentModel.PROP_NAME, auditTrail.getName());

            // file the audit log as an undeclared record
            record = this.nodeService.createNode(destination,
                        ContentModel.ASSOC_CONTAINS,
                        QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                    QName.createValidLocalName(auditTrail.getName())),
                        ContentModel.TYPE_CONTENT, properties).getChildRef();

            // Set the content
            ContentWriter writer = this.contentService.getWriter(record, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(format == ReportFormat.HTML ? MimetypeMap.MIMETYPE_HTML : MimetypeMap.MIMETYPE_JSON);
            writer.setEncoding("UTF-8");
            writer.putContent(auditTrail);
        }
        finally
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Audit trail report saved to temporary file: " + auditTrail.getAbsolutePath());
            }
            else
            {
                auditTrail.delete();
            }
        }

        return record;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AuditEvent> getAuditEvents()
    {
        List<AuditEvent> listAuditEvents = new ArrayList<AuditEvent>(this.auditEvents.size());
        listAuditEvents.addAll(this.auditEvents.values());
        Collections.sort(listAuditEvents);
        return listAuditEvents;
    }

    /**
     * Writes the start of the audit trail stream to the given writer
     *
     * @param writer The writer to write to
     * @params params The parameters being used
     * @param reportFormat The format to write the header in
     * @throws IOException
     */
    private void writeAuditTrailHeader(Writer writer,
                RecordsManagementAuditQueryParameters params,
                ReportFormat reportFormat) throws IOException
    {
        if (writer == null)
        {
            return;
        }

        if (reportFormat == ReportFormat.HTML)
        {
            // write header as HTML
            writer.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n");
            writer.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n<head>\n");
            writer.write("<title>");
            writer.write(I18NUtil.getMessage(MSG_AUDIT_REPORT));
            writer.write("</title></head>\n");
            writer.write("<style>\n");
            writer.write("body { font-family: arial,verdana; font-size: 81%; color: #333; }\n");
            writer.write(".label { margin-right: 5px; font-weight: bold; }\n");
            writer.write(".value { margin-right: 40px; }\n");
            writer.write(".audit-info { background-color: #efefef; padding: 10px; margin-bottom: 4px; }\n");
            writer.write(".audit-entry { border: 1px solid #bbb; margin-top: 15px; }\n");
            writer.write(".audit-entry-header { background-color: #bbb; padding: 8px; }\n");
            writer.write(".audit-entry-node { padding: 10px; }\n");
            writer.write(".changed-values-table { margin-left: 6px; margin-bottom: 2px;width: 99%; }\n");
            writer.write(".changed-values-table th { text-align: left; background-color: #eee; padding: 4px; }\n");
            writer.write(".changed-values-table td { width: 33%; padding: 4px; border-top: 1px solid #eee; }\n");
            writer.write("</style>\n");
            writer.write("<body>\n<h2>");
            writer.write(I18NUtil.getMessage(MSG_AUDIT_REPORT));
            writer.write("</h2>\n");
            writer.write("<div class=\"audit-info\">\n");

            writer.write("<span class=\"label\">From:</span>");
            writer.write("<span class=\"value\">");
            Date from = params.getDateFrom();
            writer.write(from == null ? "&lt;Not Set&gt;" : StringEscapeUtils.escapeHtml(from.toString()));
            writer.write("</span>");

            writer.write("<span class=\"label\">To:</span>");
            writer.write("<span class=\"value\">");
            Date to = params.getDateTo();
            writer.write(to == null ? "&lt;Not Set&gt;" : StringEscapeUtils.escapeHtml(to.toString()));
            writer.write("</span>");

            writer.write("<span class=\"label\">Property:</span>");
            writer.write("<span class=\"value\">");
            QName prop = params.getProperty();
            writer.write(prop == null ? "All" : StringEscapeUtils.escapeHtml(getPropertyLabel(prop)));
            writer.write("</span>");

            writer.write("<span class=\"label\">User:</span>");
            writer.write("<span class=\"value\">");
            writer.write(params.getUser() == null ? "All" : StringEscapeUtils.escapeHtml(params.getUser()));
            writer.write("</span>");

            writer.write("<span class=\"label\">Event:</span>");
            writer.write("<span class=\"value\">");
            writer.write(params.getEvent() == null ? "All" : StringEscapeUtils.escapeHtml(getAuditEventLabel(params.getEvent())));
            writer.write("</span>\n");

            writer.write("</div>\n");
        }
        else
        {
            // write header as JSON
            writer.write("{\n\t\"data\":\n\t{");
            writer.write("\n\t\t\"started\": \"");
            writer.write(ISO8601DateFormat.format(getStartOfDay(params.getDateFrom())));
            writer.write("\",\n\t\t\"stopped\": \"");
            writer.write(ISO8601DateFormat.format(getEndOfDay(params.getDateTo())));
            writer.write("\",\n\t\t\"enabled\": ");
            writer.write(Boolean.toString(isEnabled()));
            writer.write(",\n\t\t\"entries\":[");
        }
    }

    /**
     * Writes an audit trail entry to the given writer
     *
     * @param writer The writer to write to
     * @param entry The entry to write
     * @param reportFormat The format to write the header in
     * @throws IOException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void writeAuditTrailEntry(Writer writer, RecordsManagementAuditEntry entry,
                ReportFormat reportFormat) throws IOException
    {
        if (writer == null)
        {
            return;
        }

        if (reportFormat == ReportFormat.HTML)
        {
            writer.write("<div class=\"audit-entry\">\n");
            writer.write("<div class=\"audit-entry-header\">");
            writer.write("<span class=\"label\">Timestamp:</span>");
            writer.write("<span class=\"value\">");
            writer.write(StringEscapeUtils.escapeHtml(entry.getTimestamp().toString()));
            writer.write("</span>");
            writer.write("<span class=\"label\">User:</span>");
            writer.write("<span class=\"value\">");
            writer.write(entry.getFullName() != null ?
                            StringEscapeUtils.escapeHtml(entry.getFullName()) :
                            StringEscapeUtils.escapeHtml(entry.getUserName()));
            writer.write("</span>");
            if (entry.getUserRole() != null && entry.getUserRole().length() > 0)
            {
                writer.write("<span class=\"label\">Role:</span>");
                writer.write("<span class=\"value\">");
                writer.write(StringEscapeUtils.escapeHtml(entry.getUserRole()));
                writer.write("</span>");
            }
            if (entry.getEvent() != null && entry.getEvent().length() > 0)
            {
                writer.write("<span class=\"label\">Event:</span>");
                writer.write("<span class=\"value\">");
                writer.write(StringEscapeUtils.escapeHtml(getAuditEventLabel(entry.getEvent())));
                writer.write("</span>\n");
            }
            writer.write("</div>\n");
            writer.write("<div class=\"audit-entry-node\">");
            if (entry.getIdentifier() != null && entry.getIdentifier().length() > 0)
            {
                writer.write("<span class=\"label\">Identifier:</span>");
                writer.write("<span class=\"value\">");
                writer.write(StringEscapeUtils.escapeHtml(entry.getIdentifier()));
                writer.write("</span>");
            }
            if (entry.getNodeType() != null && entry.getNodeType().length() > 0)
            {
                writer.write("<span class=\"label\">Type:</span>");
                writer.write("<span class=\"value\">");
                writer.write(StringEscapeUtils.escapeHtml(entry.getNodeType()));
                writer.write("</span>");
            }
            if (entry.getPath() != null && entry.getPath().length() > 0)
            {
                // we need to strip off the first part of the path
                String path = entry.getPath();
                String displayPath = path;
                int idx = path.indexOf('/', 1);
                if (idx != -1)
                {
                    displayPath = "/File Plan" + path.substring(idx);
                }

                writer.write("<span class=\"label\">Location:</span>");
                writer.write("<span class=\"value\">");
                writer.write(StringEscapeUtils.escapeHtml(displayPath));
                writer.write("</span>");
            }
            writer.write("</div>\n");

            if (entry.getChangedProperties() != null)
            {
                writer.write("<table class=\"changed-values-table\" cellspacing=\"0\">");
                writer.write("<tr><th>Property</th><th>Previous Value</th><th>New Value</th></tr>");

                // create an entry for each property that changed
                for (QName valueName : entry.getChangedProperties().keySet())
                {
                    Pair<Serializable, Serializable> values = entry.getChangedProperties().get(valueName);
                    writer.write("<tr><td>");
                    writer.write(getPropertyLabel(valueName));
                    writer.write("</td><td>");

                    // inspect the property to determine it's data type
                    QName propDataType = DataTypeDefinition.TEXT;
                    PropertyDefinition propDef = dictionaryService.getProperty(valueName);
                    if (propDef != null)
                    {
                        propDataType = propDef.getDataType().getName();
                    }

                    if(DataTypeDefinition.MLTEXT.equals(propDataType))
                    {
                        writer.write(values.getFirst() == null ? "&lt;none&gt;" : StringEscapeUtils.escapeHtml(convertToMlText((Map)values.getFirst()).getDefaultValue()));
                        writer.write("</td><td>");
                        writer.write(values.getSecond() == null ? "&lt;none&gt;" : StringEscapeUtils.escapeHtml(convertToMlText((Map)values.getSecond()).getDefaultValue()));
                    }
                    else
                    {
                        Serializable oldValue = values.getFirst();
                        writer.write(oldValue == null ? "&lt;none&gt;" : StringEscapeUtils.escapeHtml(oldValue.toString()));
                        writer.write("</td><td>");
                        Serializable newValue = values.getSecond();
                        writer.write(newValue == null ? "&lt;none&gt;" : StringEscapeUtils.escapeHtml(newValue.toString()));
                    }

                    writer.write("</td></tr>");
                }

                writer.write("</table>\n");
            }

            writer.write("</div>");
        }
        else
        {
            try
            {
                JSONObject json = new JSONObject();

                json.put("timestamp", entry.getTimestampString());
                json.put("userName", entry.getUserName());
                json.put("userRole", entry.getUserRole() == null ? "": entry.getUserRole());
                json.put("fullName", entry.getFullName() == null ? "": entry.getFullName());
                json.put("nodeRef", entry.getNodeRef() == null ? "": entry.getNodeRef());

                if (entry.getEvent().equals("createPerson") && entry.getNodeRef() != null)
                {
                    NodeRef nodeRef = entry.getNodeRef();
                    String userName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
                    json.put("nodeName", userName == null ? "": userName);
                }
                else
                {
                    json.put("nodeName", entry.getNodeName() == null ? "": entry.getNodeName());
                }

                json.put("nodeType", entry.getNodeType() == null ? "": entry.getNodeType());
                json.put("event", entry.getEvent() == null ? "": getAuditEventLabel(entry.getEvent()));
                json.put("identifier", entry.getIdentifier() == null ? "": entry.getIdentifier());
                json.put("path", entry.getPath() == null ? "": entry.getPath());

                JSONArray changedValues = new JSONArray();

                if (entry.getChangedProperties() != null)
                {
                    // create an entry for each property that changed
                    for (QName valueName : entry.getChangedProperties().keySet())
                    {
                        Pair<Serializable, Serializable> values = entry.getChangedProperties().get(valueName);

                        JSONObject changedValue = new JSONObject();
                        changedValue.put("name", getPropertyLabel(valueName));

                        // inspect the property to determine it's data type
                        QName propDataType = DataTypeDefinition.TEXT;
                        PropertyDefinition propDef = dictionaryService.getProperty(valueName);
                        if (propDef != null)
                        {
                            propDataType = propDef.getDataType().getName();
                        }

                        // handle output of mltext properties
                        if(DataTypeDefinition.MLTEXT.equals(propDataType))
                        {
                            changedValue.put("previous", values.getFirst() == null ? "" : convertToMlText((Map)values.getFirst()).getDefaultValue());
                            changedValue.put("new", values.getSecond() == null ? "" : convertToMlText((Map)values.getSecond()).getDefaultValue());
                        }
                        else
                        {
                            changedValue.put("previous", values.getFirst() == null ? "" : values.getFirst().toString());
                            changedValue.put("new", values.getSecond() == null ? "" : values.getSecond().toString());
                        }

                        changedValues.put(changedValue);
                    }
                }

                json.put("changedValues", changedValues);

                writer.write(json.toString());
            }
            catch (JSONException je)
            {
                writer.write("{}");
            }
        }
    }

    /**
     * Helper method to convert value to MLText
     *
     * @param map   map of locale's and values
     * @return {@link MLText}   multilingual text value
     */
    private MLText convertToMlText(Map<Locale, String> map)
    {
        MLText mlText = new MLText();
        mlText.putAll(map);
        return mlText;
    }

    /**
     * Writes the end of the audit trail stream to the given writer
     *
     * @param writer The writer to write to
     * @param reportFormat The format to write the footer in
     * @throws IOException
     */
    private void writeAuditTrailFooter(Writer writer, ReportFormat reportFormat) throws IOException
    {
        if (writer == null)
        {
            return;
        }

        if (reportFormat == ReportFormat.HTML)
        {
            // write footer as HTML
            writer.write("\n</body></html>");
        }
        else
        {
            // write footer as JSON
            writer.write("\n\t\t]\n\t}\n}");
        }
    }

    /**
     * Returns the display label for a property QName
     *
     * @param property The property to get label for
     * @param ddService DictionaryService instance
     * @param namespaceService NamespaceService instance
     * @return The label
     */
    private String getPropertyLabel(QName property)
    {
        String label = null;

        PropertyDefinition propDef = this.dictionaryService.getProperty(property);
        if (propDef != null)
        {
            label = propDef.getTitle(dictionaryService);
        }

        if (label == null)
        {
            label = property.getLocalName();
        }

        return label;
    }

    /**
     * Returns the display label for the given audit event key
     *
     * @param eventKey The audit event key
     * @return The display label or null if the key does not exist
     */
    private String getAuditEventLabel(String eventKey)
    {
        String label = eventKey;

        AuditEvent event = this.auditEvents.get(eventKey);
        if (event != null)
        {
            label = event.getLabel();
        }

        return label;
    }

    /**
     * A class to carry audit information through the transaction.
     *
     * @author Derek Hulley
     * @since 3.2
     */
    private static class RMAuditNode
    {
        private NodeRef nodeRef;
        private String eventName;
        private Map<QName, Serializable> nodePropertiesBefore;
        private Map<QName, Serializable> nodePropertiesAfter;
        private boolean removeIfNoPropertyChanged = false;

        public NodeRef getNodeRef()
        {
            return nodeRef;
        }

        public void setNodeRef(NodeRef nodeRef)
        {
            this.nodeRef = nodeRef;
        }

        public String getEventName()
        {
            return eventName;
        }

        public void setEventName(String eventName)
        {
            this.eventName = eventName;
        }

        public Map<QName, Serializable> getNodePropertiesBefore()
        {
            return nodePropertiesBefore;
        }

        public void setNodePropertiesBefore(Map<QName, Serializable> nodePropertiesBefore)
        {
            this.nodePropertiesBefore = nodePropertiesBefore;
        }

        public Map<QName, Serializable> getNodePropertiesAfter()
        {
            return nodePropertiesAfter;
        }

        public void setNodePropertiesAfter(Map<QName, Serializable> nodePropertiesAfter)
        {
            this.nodePropertiesAfter = nodePropertiesAfter;
        }

        public boolean getRemoveIfNoPropertyChanged()
        {
            return removeIfNoPropertyChanged;
        }

        public void setRemoveIfNoPropertyChanged(boolean removeIfNoPropertyChanged)
        {
            this.removeIfNoPropertyChanged = removeIfNoPropertyChanged;
        }
    }

    /** Deprecated Method Implementations **/

    /**
     * Helper method to get the default file plan
     *
     * @return  NodRef default file plan
     */
    private NodeRef getDefaultFilePlan()
    {
        NodeRef defaultFilePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
        if (defaultFilePlan == null)
        {
            throw new AlfrescoRuntimeException("Default file plan could not be found.");
        }
        return defaultFilePlan;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public boolean isEnabled()
    {
        return isAuditLogEnabled(getDefaultFilePlan());
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    public void start()
    {
        startAuditLog(getDefaultFilePlan());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public void stop()
    {
        stopAuditLog(getDefaultFilePlan());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public Date getDateLastStarted()
    {
        return getDateAuditLogLastStarted(getDefaultFilePlan());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public Date getDateLastStopped()
    {
        return getDateAuditLogLastStopped(getDefaultFilePlan());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public void clear()
    {
        clearAuditLog(getDefaultFilePlan());
    }

    /**
     * {@inheritDoc}
     * @since 3.2
     * @deprecated since 2.1
     */
    @Override
    @Deprecated
    public void auditRMAction(
            RecordsManagementAction action,
            NodeRef nodeRef,
            Map<String, Serializable> parameters)
    {
        auditEvent(nodeRef, action.getName());
    }
}