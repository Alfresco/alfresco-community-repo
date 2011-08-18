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
package org.alfresco.repo.audit.access;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.repo.audit.model.AuditApplication;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCancelCheckOut;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckIn;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckOut;
import org.alfresco.repo.content.ContentServicePolicies.OnContentReadPolicy;
import org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy;
import org.alfresco.repo.copy.CopyServicePolicies.OnCopyCompletePolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnRemoveAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.repo.version.VersionServicePolicies.OnCreateVersionPolicy;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Creates high level audit records on the creation, deletion, modification and access
 * of content and folders. Lower level events are grouped together by transaction
 * and node.<p>
 * 
 * To turn on auditing of these events and sub events add the following property to
 * alfresco-global.properties:
 * <pre>
 *    # Enable audit in general
 *    audit.enabled=true
 *
 *    # Enable the alfresco-access audit application
 *    audit.alfresco-access.enabled=true
 *
 *    # Enable the auditing of sub-actions. Normally disabled as these values are
 *    # not normally needed by audit configurations, but may be useful to developers
 *    audit.alfresco-access.sub-actions.enabled=true
 * </pre>
 * 
 * The following properties are set by default to discard events where the user is
 * 'null' or 'System', the node path is '/sys:archivedItem' or under '/ver:' or 
 * the node type is not 'cm:folder', 'cm:content' or 'st:site'. These values result
 * in events only being recorded for common actions initiated by users of the system.
 * These vales may be overridden if required.
 * <pre>
 *    audit.filter.alfresco-access.default.enabled=true
 *    audit.filter.alfresco-access.transaction.user=~System;~null;.*
 *    audit.filter.alfresco-access.transaction.type=cm:folder;cm:content;st:site
 *    audit.filter.alfresco-access.transaction.path=~/sys:archivedItem;~/ver:;.*
 * </pre>
 * 
 * Node and Content changes generate the following audit structure. Elements are omitted
 * if not changed by the transaction. The {@code /sub-action/<sequence>} structure holds
 * cut down details of each sub-action, but are only included if the global property
 * {@code audit.alfresco-access.sub-actions.enabled=true}.
 * <pre>
 *    /alfresco-access
 *     /transaction
 *       /action=&lt;actionName&gt;
 *       /sub-actions=&lt;sub action list&gt;
 *       /path=&lt;prefixPath&gt;
 *       /type=&lt;prefixType&gt;
 *       /node=&lt;nodeRef&gt;
 *       /user=&lt;user&gt;
 *       /copy
 *         /from
 *           /node=&lt;nodeRef&gt;
 *           /path=&lt;prefixPath&gt;
 *           /type=&lt;prefixType&gt;
 *       /move
 *         /from
 *           /node=&lt;nodeRef&gt;
 *           /path=&lt;prefixPath&gt;
 *           /type=&lt;prefixType&gt;
 *       /properties
 *          /from=&lt;mapOfValues&gt;
 *            /&lt;propertyName&gt;=&lt;propertyValue&gt;
 *          /to=&lt;mapOfValues&gt;
 *            /&lt;propertyName&gt;=&lt;propertyValue&gt;
 *          /add=&lt;mapOfValues&gt;
 *            /&lt;propertyName&gt;=&lt;propertyValue&gt;
 *          /delete=&lt;mapOfValues&gt;
 *            /&lt;propertyName&gt;=&lt;propertyValue&gt;
 *        /aspects
 *          /add=&lt;mapOfNames&gt;
 *            /&lt;aspectName&gt;=null
 *          /delete=&lt;mapOfNames&gt;
 *            /&lt;aspectName&gt;=null
 *        /version-properties=&lt;mapOfValues&gt;
 *        /sub-action/&lt;sequence&gt;
 *          /action=&lt;actionName&gt;
 *          /move
 *            ...
 *          /properties
 *            ...
 *          /aspects
 *            ...
 *            
 *  Example data:
 *    /alfresco-access/transaction/action=MOVE
 *    /alfresco-access/transaction/node=workspace://SpacesStore/74a5985a-45dd-4698-82db-8eaeff9df8d7
 *    /alfresco-access/transaction/move/from/node=workspace://SpacesStore/d8a0dfd8-fe45-47da-acc2-fd8df9ea2b2e
 *    /alfresco-access/transaction/move/from/path=/app:company_home/st:sites/cm:abc/cm:documentLibrary/cm:folder1/cm:Word 123.docx
 *    /alfresco-access/transaction/move/from/type=cm:folder
 *    /alfresco-access/transaction/path=/app:company_home/st:sites/cm:abc/cm:documentLibrary/cm:folder2/cm:Word 123.docx
 *    /alfresco-access/transaction/sub-actions=moveNode readContent
 *    /alfresco-access/transaction/type=cm:content
 *    /alfresco-access/transaction/user=admin
 *    /alfresco-access/transaction/sub-action/00/action=moveNode
 *    /alfresco-access/transaction/sub-action/00/move/from/node=workspace://SpacesStore/d8a0dfd8-fe45-47da-acc2-fd8df9ea2b2e
 *    /alfresco-access/transaction/sub-action/00/move/from/path=/app:company_home/st:sites/cm:abc/cm:documentLibrary/cm:folder1/cm:Word 123.docx
 *    /alfresco-access/transaction/sub-action/00/move/from/type=cm:folder
 *    /alfresco-access/transaction/sub-action/01/action=readContent
 * </pre>
 * The trace output from this class may be useful to developers as it logs method
 * calls grouped by transaction. The debug output is of the audit records written
 * and full inbound audit data. However for developers trace will provide a more
 * readable form. Setting the following dev-log4j.properties:
 * <pre>
 *    log4j.appender.File.Threshold=trace
 *    log4j.logger.org.alfresco.repo.audit.access.AccessAuditor=trace
 * </pre>
 * 
 * @author Alan Davis
 */
public class AccessAuditor implements InitializingBean, 

        BeforeDeleteNodePolicy, OnAddAspectPolicy, OnCreateNodePolicy, OnMoveNodePolicy,
        OnRemoveAspectPolicy, OnUpdatePropertiesPolicy,
        
        OnContentReadPolicy, OnContentUpdatePolicy,
        
        OnCreateVersionPolicy,
        
        OnCopyCompletePolicy,
        
        OnCheckOut, OnCheckIn, OnCancelCheckOut
{
    /** Logger */
    private static Log logger = LogFactory.getLog(AccessAuditor.class);
    
    private static final String ROOT_PATH = "/alfresco-access";
    private static final String TRANSACTION = "transaction";
    private static final String AUDIT_SUB_ACTIONS = "audit.alfresco-access.sub-actions.enabled";
    
    private Properties properties;
    private PolicyComponent policyComponent;
    private AuditComponent auditComponent;
    private TransactionService transactionService;
    private NodeInfoFactory nodeInfoFactory;
    private NamespaceService namespaceService;

    private TransactionListener transactionListener = new AccessTransactionListener();
    private boolean auditSubActions = false;

    /**
     * Set the properties object holding filter configuration
     * @since 3.2
     */
    public void setProperties(Properties properties)
    {
        this.properties = properties;
        auditSubActions = properties.getProperty(AUDIT_SUB_ACTIONS, "false").equalsIgnoreCase("true");
    }

    /**
     * Set the component used to bind to behaviour callbacks
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * The component to create audit events
     */
    public void setAuditComponent(AuditComponent auditComponent)
    {
        this.auditComponent = auditComponent;
    }

    /**
     * Set the component used to start new transactions
     */
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Set the component used to resolve namespaces.
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * Set the component used to create {@link NodeInfo} objects.
     */
    public void setNodeInfoFactory(NodeInfoFactory nodeInfoFactory)
    {
        this.nodeInfoFactory = nodeInfoFactory;
    }

    /**
     * Checks that all necessary properties have been set and binds with the policy component.
     */
    public void afterPropertiesSet()
    {
        PropertyCheck.mandatory(this, "properties", properties);
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "auditComponent", auditComponent);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "namespaceService", namespaceService);
        PropertyCheck.mandatory(this, "nodeInfoFactory", nodeInfoFactory);
        
        policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME, this, new JavaBehaviour(this, "beforeDeleteNode"));
        policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, this, new JavaBehaviour(this, "onCreateNode"));
        policyComponent.bindClassBehaviour(OnMoveNodePolicy.QNAME, this, new JavaBehaviour(this, "onMoveNode"));
        policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, this, new JavaBehaviour(this, "onUpdateProperties"));
        policyComponent.bindClassBehaviour(OnAddAspectPolicy.QNAME, this, new JavaBehaviour(this, "onAddAspect"));
        policyComponent.bindClassBehaviour(OnRemoveAspectPolicy.QNAME, this, new JavaBehaviour(this, "onRemoveAspect"));

        policyComponent.bindClassBehaviour(OnContentUpdatePolicy.QNAME, this, new JavaBehaviour(this, "onContentUpdate"));
        policyComponent.bindClassBehaviour(OnContentReadPolicy.QNAME, this, new JavaBehaviour(this, "onContentRead"));

        policyComponent.bindClassBehaviour(OnCreateVersionPolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateVersion"));
        policyComponent.bindClassBehaviour(OnCreateVersionPolicy.QNAME, ContentModel.TYPE_FOLDER, new JavaBehaviour(this, "onCreateVersion"));

        policyComponent.bindClassBehaviour(OnCopyCompletePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCopyComplete"));
        policyComponent.bindClassBehaviour(OnCopyCompletePolicy.QNAME, ContentModel.TYPE_FOLDER, new JavaBehaviour(this, "onCopyComplete"));

        policyComponent.bindClassBehaviour(OnCheckOut.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCheckOut"));
        policyComponent.bindClassBehaviour(OnCheckIn.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCheckIn"));
        policyComponent.bindClassBehaviour(OnCancelCheckOut.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCancelCheckOut"));
    }
    
    private boolean auditEnabled()
    {
        return transactionService.getAllowWrite() &&
               auditComponent.areAuditValuesRequired(ROOT_PATH);
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        if (auditEnabled())
        {
            getNodeChange(nodeRef).beforeDeleteNode(nodeRef);
        }
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        if (auditEnabled())
        {
            getNodeChange(childAssocRef.getChildRef()).onCreateNode(childAssocRef);
        }
    }

    @Override
    public void onMoveNode(ChildAssociationRef fromChildAssocRef,
            ChildAssociationRef toChildAssocRef)
    {
        if (auditEnabled())
        {
            getNodeChange(toChildAssocRef.getChildRef()).onMoveNode(fromChildAssocRef, toChildAssocRef);
        }
    }  

    @Override
    public void onUpdateProperties(NodeRef nodeRef,
            Map<QName, Serializable> fromProperties, Map<QName, Serializable> toProperties)
    {
        if (auditEnabled())
        {
            getNodeChange(nodeRef).onUpdateProperties(nodeRef, fromProperties, toProperties);
        }
    }

    @Override
    public void onRemoveAspect(NodeRef nodeRef, QName aspect)
    {
        if (auditEnabled())
        {
            getNodeChange(nodeRef).onRemoveAspect(nodeRef, aspect);
        }
    }

    @Override
    public void onAddAspect(NodeRef nodeRef, QName aspect)
    {
        if (auditEnabled())
        {
            getNodeChange(nodeRef).onAddAspect(nodeRef, aspect);
        }

    }
    
    @Override
    public void onContentUpdate(NodeRef nodeRef, boolean newContent)
    {
        if (auditEnabled())
        {
            getNodeChange(nodeRef).onContentUpdate(nodeRef, newContent);
        }
    }

    @Override
    public void onContentRead(NodeRef nodeRef)
    {
        if (auditEnabled())
        {
            getNodeChange(nodeRef).onContentRead(nodeRef);
        }
    }

    @Override
    public void onCreateVersion(QName classRef, NodeRef nodeRef,
            Map<String, Serializable> versionProperties, PolicyScope nodeDetails)
    {
        if (auditEnabled())
        {
            getNodeChange(nodeRef).onCreateVersion(classRef, nodeRef, versionProperties, nodeDetails);
        }
    }
    
    public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef,
            boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap)
    {
        if (auditEnabled())
        {
            getNodeChange(targetNodeRef).onCopyComplete(classRef, sourceNodeRef, targetNodeRef,
                    copyToNewNode, copyMap);
        }
    }
    
    public void onCheckOut(NodeRef workingCopy)
    {
        if (auditEnabled())
        {
            getNodeChange(workingCopy).onCheckOut(workingCopy);
        }
    }
    
    public void onCheckIn(NodeRef nodeRef)
    {
        if (auditEnabled())
        {
            getNodeChange(nodeRef).onCheckIn(nodeRef);
        }
    }
    
    public void onCancelCheckOut(NodeRef nodeRef)
    {
        if (auditEnabled())
        {
            getNodeChange(nodeRef).onCancelCheckOut(nodeRef);
        }
    }

    /**
     * @return the {@link NodeChange} for the supplied {@code nodeRef} from
     *         the current transaction context or create one if required.
     */
    private NodeChange getNodeChange(NodeRef nodeRef)
    {
        Map<NodeRef, NodeChange> accessAuditNodes =
            TransactionalResourceHelper.getMap(transactionListener);
        
        if (accessAuditNodes.isEmpty())
        {
            AlfrescoTransactionSupport.bindListener(transactionListener);
        }
        
        NodeChange nodeChange = accessAuditNodes.get(nodeRef);
        if (nodeChange == null)
        {
            nodeChange = new NodeChange(nodeInfoFactory, namespaceService, nodeRef);
            nodeChange.setAuditSubActions(auditSubActions);
            accessAuditNodes.put(nodeRef, nodeChange);
        }
        
        return nodeChange;
    }

    /**
     * Record audit values and log trace and debug messages.
     * @param action String giving the action performed. Becomes the second component
     *        of the audit path after the root path.
     * @param auditMap Map of values to be audited.
     * @return {@code true} if any values were audited.
     */
    private boolean recordAuditValues(String action, Map<String, Serializable> auditMap)
    {
        String rootPath = AuditApplication.buildPath(ROOT_PATH, action);
        Map<String, Serializable> recordedAuditMap = auditComponent.recordAuditValues(rootPath, auditMap);

        if (!recordedAuditMap.isEmpty())
        {
            if (logger.isDebugEnabled())
            {
                // Trace is used by a developer to produce a cut down log output that is simpler
                // to read (no audit data section or summary keys)
                boolean devOutput = logger.isTraceEnabled();
                
                StringBuilder sb = new StringBuilder();
                StringBuilder subActions = new StringBuilder("");
                if (!devOutput)
                {
                    sb.append("\n\tAudit data:");
                    for (String key : new TreeSet<String>(recordedAuditMap.keySet()))
                    {
                        sb.append("\n\t\t").append(key).append('=');
                        appendAuditMapValue(sb, recordedAuditMap.get(key));
                    }

                    sb.append("\n\n\tInbound audit values: ");
                }

                for (String key : new TreeSet<String>(auditMap.keySet()))
                {
                    if (!devOutput || !NodeChange.SUMMARY_KEYS.contains(key))
                    {
                        StringBuilder output = (key.startsWith(NodeChange.SUB_ACTION_PREFIX))
                            ? subActions : sb;
                        
                        output.append("\n\t\t").append(rootPath).append('/').append(key).append('=');
                        appendAuditMapValue(output, auditMap.get(key));
                    }
                }
                if (subActions.length() > 0)
                {
                    sb.append("\n\t\t--- sub actions ---");
                    sb.append(subActions.toString());
                }
                if (devOutput)
                {
                    logger.trace(sb.toString());
                }
                else
                {
                    logger.debug(sb.toString());
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Appends a more readable version of an audit map value. The prefix is used for
     * {@link QName} values, including when used in Maps, Sets and Lists.
     */
    private void appendAuditMapValue(StringBuilder sb, Serializable value)
    {
        if (value instanceof QName)
        {
            // Note there is no need to use the toPrefixString(namespace) method
            // as all QNames will have a prefix by this stage.
            sb.append(((QName)value).toPrefixString());
        }
        else if (value instanceof Map)
        {
            sb.append('{');
            boolean first = true;
            Map<?,?> map = (Map<?,?>)value;
            for (Map.Entry<?,?> entry: map.entrySet())
            {
                if (!first)
                {
                    sb.append(", ");
                }
                else
                {
                    first = false;
                }
                Serializable key = (Serializable)entry.getKey();
                Serializable val = (Serializable)entry.getValue();
                appendAuditMapValue(sb, key);
                sb.append('=');
                appendAuditMapValue(sb, val);
            }
            sb.append('}');
        }
        else if (value instanceof List)
        {
            sb.append('[');
            boolean first = true;
            List<?> list = (List<?>)value;
            for (Object element: list)
            {
                if (!first)
                {
                    sb.append(", ");
                }
                else
                {
                    first = false;
                }
                appendAuditMapValue(sb, (Serializable)element);
            }
            sb.append(']');
        }
        else if (value instanceof Set)
        {
            sb.append('[');
            boolean first = true;
            Set<?> set = (Set<?>)value;
            for (Object element: set)
            {
                if (!first)
                {
                    sb.append(", ");
                }
                else
                {
                    first = false;
                }
                appendAuditMapValue(sb, (Serializable)element);
            }
            sb.append(']');
        }
        else
        {
            sb.append(value);
        }
    }
    
    /**
     * Listen for commit to audit gathered audit activity for the current user transaction.
     */
    private class AccessTransactionListener extends TransactionListenerAdapter
    {
        @Override
        public void afterCommit()
        {
            // Note: auditComponent.recordAuditValues(...) creates a transaction to record
            //       audit messages, so there is no need to create our own. dod5015 still
            //       does (not sure why).
            
            final Map<NodeRef, NodeChange> changedNodes = TransactionalResourceHelper.getMap(this); 
            for (Map.Entry<NodeRef, NodeChange> entry : changedNodes.entrySet())
            {              
                NodeChange nodeChange = entry.getValue();
                if (!nodeChange.isTemporaryNode())
                {
                    Map<String, Serializable> auditMap = nodeChange.getAuditData(false);
                    recordAuditValues(TRANSACTION, auditMap);
                }
            }
        }
    }
}
