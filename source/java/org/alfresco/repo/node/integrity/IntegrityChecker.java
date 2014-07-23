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
package org.alfresco.repo.node.integrity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.transaction.TransactionListener;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component that generates and processes integrity events, enforcing the dictionary
 * model's node structure.
 * <p>
 * In order to fulfill the contract of the interface, this class registers to receive notifications
 * pertinent to changes in the node structure.  These are then store away in the persistent
 * store until the request to
 * {@link org.alfresco.repo.integrity.IntegrityService#checkIntegrity(String) check integrity} is
 * made.
 * <p>
 * In order to ensure registration of these events, the {@link #init()} method must be called.
 * <p>
 * By default, this service is enabled, but can be disabled using {@link #setEnabled(boolean)}.<br>
 * Tracing of the event stacks is, for performance reasons, disabled by default but can be enabled
 * using {@link #setTraceOn(boolean)}.<br>
 * When enabled, the integrity check can either fail with a <tt>RuntimeException</tt> or not.  In either
 * case, the integrity violations are logged as warnings or errors.  This behaviour is controleed using
 * {@link #setFailOnViolation(boolean)} and is off by default.  In other words, if not set, this service
 * will only log warnings about integrity violations.
 * <p>
 * Some integrity checks are not performed here as they are dealt with directly during the modification
 * operation in the {@link org.alfresco.service.cmr.repository.NodeService node service}.
 * 
 * @see #setPolicyComponent(PolicyComponent)
 * @see #setDictionaryService(DictionaryService)
 * @see #setIntegrityDaoService(IntegrityDaoService)
 * @see #setMaxErrorsPerTransaction(int)
 * @see #setFlushSize(int)
 * 
 * @author Derek Hulley
 */
public class IntegrityChecker
        implements  NodeServicePolicies.OnCreateNodePolicy,
                    NodeServicePolicies.OnUpdatePropertiesPolicy,
                    NodeServicePolicies.OnDeleteNodePolicy,
                    NodeServicePolicies.OnAddAspectPolicy,
                    NodeServicePolicies.OnRemoveAspectPolicy,
                    NodeServicePolicies.OnCreateChildAssociationPolicy,
                    NodeServicePolicies.OnDeleteChildAssociationPolicy,
                    NodeServicePolicies.OnCreateAssociationPolicy,
                    NodeServicePolicies.OnDeleteAssociationPolicy,
                    TransactionListener
{
    private static Log logger = LogFactory.getLog(IntegrityChecker.class);
    
    /** key against which the set of events is stored in the current transaction */
    private static final String KEY_EVENT_SET = "IntegrityChecker.EventSet";
    /** key to store the local flag to disable integrity errors, i.e. downgrade to warnings */
    private static final String KEY_WARN_IN_TRANSACTION = "IntegrityChecker.WarnInTransaction";
    
    private PolicyComponent policyComponent;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private TenantService tenantService;
    private boolean enabled;
    private boolean failOnViolation;
    private int maxErrorsPerTransaction;
    private boolean traceOn;
    private List<String> storesToIgnore = new ArrayList<String>(0);
    
    /**
     * Downgrade violations to warnings within the current transaction.  This is temporary and
     * is <u>dependent on there being a current transaction</u> active against the
     * current thread.  When set, this will override the global 
     * {@link #setFailOnViolation(boolean) failure behaviour}.
     */
    public static void setWarnInTransaction()
    {
        TransactionSupportUtil.bindResource(KEY_WARN_IN_TRANSACTION, Boolean.TRUE);
    }
    
    /**
     * @return Returns true if the current transaction should only warn on violations.
     *      If <code>false</code>, the global setting will take effect.
     * 
     * @see #setWarnInTransaction()
     */
    public static boolean isWarnInTransaction()
    {
        Boolean warnInTransaction = (Boolean) TransactionSupportUtil.getResource(KEY_WARN_IN_TRANSACTION);
        if (warnInTransaction == null || warnInTransaction == Boolean.FALSE)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
    
    /**
     */
    public IntegrityChecker()
    {
        this.enabled = true;
        this.failOnViolation = false;
        this.maxErrorsPerTransaction = 10;
        this.traceOn = false;
    }

    /**
     * @param policyComponent the component to register behaviour with
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param dictionaryService the dictionary against which to confirm model details
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param nodeService the node service to use for browsing node structures
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    /**
     * @param enabled set to false to disable integrity checking completely
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * @param traceOn set to <code>true</code> to enable stack traces recording
     *      of events
     */
    public void setTraceOn(boolean traceOn)
    {
        this.traceOn = traceOn;
    }

    /**
     * @param failOnViolation set to <code>true</code> to force failure by
     *      <tt>RuntimeException</tt> when a violation occurs.
     */
    public void setFailOnViolation(boolean failOnViolation)
    {
        this.failOnViolation = failOnViolation;
    }

    /**
     * @param maxLogNumberPerTransaction upper limit on how many violations are
     *      logged when multiple violations have been found.
     */
    public void setMaxErrorsPerTransaction(int maxLogNumberPerTransaction)
    {
        this.maxErrorsPerTransaction = maxLogNumberPerTransaction;
    }
    
    /**
     * @param storesToIgnore stores (eg. workspace://version2Store) which will be 
     *      ignored by integrity checker. Note: assumes associations are within a store.
     */
    public void setStoresToIgnore(List<String> storesToIgnore)
    {
        this.storesToIgnore = storesToIgnore;
    }

    /**
     * Registers the system-level policy behaviours
     */
    public void init()
    {
        // check that required properties have been set
        PropertyCheck.mandatory("IntegrityChecker", "dictionaryService", dictionaryService);
        PropertyCheck.mandatory("IntegrityChecker", "nodeService", nodeService);
        PropertyCheck.mandatory("IntegrityChecker", "policyComponent", policyComponent);

        if (enabled)  // only register behaviour if integrity checking is on
        {
            // register behaviour
            policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
                    this,
                    new JavaBehaviour(this, "onCreateNode"));   
            policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                    this,
                    new JavaBehaviour(this, "onUpdateProperties"));   
            policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"),
                    this,
                    new JavaBehaviour(this, "onDeleteNode"));   
            policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"),
                    this,
                    new JavaBehaviour(this, "onAddAspect"));   
            policyComponent.bindClassBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveAspect"),
                    this,
                    new JavaBehaviour(this, "onRemoveAspect"));   
            policyComponent.bindAssociationBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateChildAssociation"),
                    this,
                    new JavaBehaviour(this, "onCreateChildAssociation"));   
            policyComponent.bindAssociationBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteChildAssociation"),
                    this,
                    new JavaBehaviour(this, "onDeleteChildAssociation"));   
            policyComponent.bindAssociationBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateAssociation"),
                    this,
                    new JavaBehaviour(this, "onCreateAssociation"));   
            policyComponent.bindAssociationBehaviour(
                    QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteAssociation"),
                    this,
                    new JavaBehaviour(this, "onDeleteAssociation"));   
        }
    }
    
    /**
     * Ensures that this service is registered with the transaction and saves the event
     * 
     * @param event
     */
    @SuppressWarnings("unchecked")
    private void save(IntegrityEvent event)
    {
        // optionally set trace
        if (traceOn)
        {
            // get a stack trace
            Throwable t = new Throwable();
            t.fillInStackTrace();
            StackTraceElement[] trace = t.getStackTrace();
            
            event.addTrace(trace);
            // done
        }
        
        // register this service
        AlfrescoTransactionSupport.bindIntegrityChecker(this);
        
        // get the event list
        Map<IntegrityEvent, IntegrityEvent> events =
            (Map<IntegrityEvent, IntegrityEvent>) AlfrescoTransactionSupport.getResource(KEY_EVENT_SET);
        if (events == null)
        {
            events = new HashMap<IntegrityEvent, IntegrityEvent>(113, 0.75F);
            AlfrescoTransactionSupport.bindResource(KEY_EVENT_SET, events);
        }
        // check if the event is present
        IntegrityEvent existingEvent = events.get(event);
        if (existingEvent != null)
        {
            // the event (or its equivalent is already present - transfer the trace
            if (traceOn)
            {
                existingEvent.getTraces().addAll(event.getTraces());
            }
        }
        else
        {
            // the event doesn't already exist
            events.put(event, event);
        }
        if (logger.isDebugEnabled())
        {
            logger.debug("" + (existingEvent != null ? "Event already present in" : "Added event to") + " event set: \n" +
                    "   event: " + event);
        }
    }

    /**
     * @see PropertiesIntegrityEvent
     * @see AssocTargetRoleIntegrityEvent
     * @see AssocTargetMultiplicityIntegrityEvent
     */
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        NodeRef childRef = childAssocRef.getChildRef(); 
        if (! storesToIgnore.contains(tenantService.getBaseName(childRef.getStoreRef()).toString()))
        {
            IntegrityEvent event = null;
            // check properties on child node
            event = new PropertiesIntegrityEvent(
                    nodeService,
                    dictionaryService,
                    childRef);
            save(event);
            
            // check that the multiplicity and other properties of the new association are allowed
            onCreateChildAssociation(childAssocRef, false);
            
            // check mandatory aspects
            event = new AspectsIntegrityEvent(nodeService, dictionaryService, childRef);
            save(event);
            
            // check for associations defined on the new node (child)
            QName childNodeTypeQName = nodeService.getType(childRef);
            ClassDefinition nodeTypeDef = dictionaryService.getClass(childNodeTypeQName);
            if (nodeTypeDef == null)
            {
                throw new DictionaryException("The node type is not recognized: " + childNodeTypeQName);
            }
            Map<QName, AssociationDefinition> childAssocDefs = nodeTypeDef.getAssociations();
            
            // check the multiplicity of each association with the node acting as a source
            for (AssociationDefinition assocDef : childAssocDefs.values())
            {
                QName assocTypeQName = assocDef.getName();
                // check target multiplicity
                event = new AssocTargetMultiplicityIntegrityEvent(
                        nodeService,
                        dictionaryService,
                        childRef,
                        assocTypeQName,
                        false);
                save(event);
            }
        }
    }

    /**
     * @see PropertiesIntegrityEvent
     */
    public void onUpdateProperties(
            NodeRef nodeRef,
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        if (! storesToIgnore.contains(tenantService.getBaseName(nodeRef.getStoreRef()).toString()))
        {
            IntegrityEvent event = null;
            // check properties on node
            event = new PropertiesIntegrityEvent(nodeService, dictionaryService, nodeRef);
            save(event);
        }
    }

    /**
     * No checking performed: The association changes will be handled
     */
    public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isArchivedNode)
    {
    }

    /**
     * @see PropertiesIntegrityEvent
     * @see AssocTargetMultiplicityIntegrityEvent
     */
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (! storesToIgnore.contains(tenantService.getBaseName(nodeRef.getStoreRef()).toString()))
        {
            IntegrityEvent event = null;
            // check properties on node
            event = new PropertiesIntegrityEvent(nodeService, dictionaryService, nodeRef);
            save(event);
            
            // check for associations defined on the aspect
            AspectDefinition aspectDef = dictionaryService.getAspect(aspectTypeQName);
            if (aspectDef == null)
            {
                throw new DictionaryException("The aspect type is not recognized: " + aspectTypeQName);
            }
            Map<QName, AssociationDefinition> assocDefs = aspectDef.getAssociations();
            
            // check the multiplicity of each association with the node acting as a source
            for (AssociationDefinition assocDef : assocDefs.values())
            {
                QName assocTypeQName = assocDef.getName();
                // check target multiplicity
                event = new AssocTargetMultiplicityIntegrityEvent(
                        nodeService,
                        dictionaryService,
                        nodeRef,
                        assocTypeQName,
                        false);
                save(event);
            }
        }
    }

    /**
     * @see AspectsIntegrityEvent
     */
    public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (! storesToIgnore.contains(tenantService.getBaseName(nodeRef.getStoreRef()).toString()))
        {
            IntegrityEvent event = null;
            // check mandatory aspects
            event = new AspectsIntegrityEvent(nodeService, dictionaryService, nodeRef);
            save(event);
        }  
    }

    /**
     * This handles the creation of secondary child associations.
     * 
     * @see AssocSourceTypeIntegrityEvent
     * @see AssocTargetTypeIntegrityEvent
     * @see AssocSourceMultiplicityIntegrityEvent
     * @see AssocTargetMultiplicityIntegrityEvent
     * @see AssocTargetRoleIntegrityEvent
     */
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNew)
    {
        if (isNew)
        {
            return;
        }
        
        if (! storesToIgnore.contains(tenantService.getBaseName(childAssocRef.getChildRef().getStoreRef()).toString()))
        {
            IntegrityEvent event = null;
            // check source type
            event = new AssocSourceTypeIntegrityEvent(
                    nodeService,
                    dictionaryService,
                    childAssocRef.getParentRef(),
                    childAssocRef.getTypeQName());
            save(event);
            // check target type
            event = new AssocTargetTypeIntegrityEvent(
                    nodeService,
                    dictionaryService,
                    childAssocRef.getChildRef(),
                    childAssocRef.getTypeQName());
            save(event);
            // check source multiplicity
            event = new AssocSourceMultiplicityIntegrityEvent(
                    nodeService,
                    dictionaryService,
                    childAssocRef.getChildRef(),
                    childAssocRef.getTypeQName(),
                    false);
            save(event);
            // check target multiplicity
            event = new AssocTargetMultiplicityIntegrityEvent(
                    nodeService,
                    dictionaryService,
                    childAssocRef.getParentRef(),
                    childAssocRef.getTypeQName(),
                    false);
            save(event);
            // check target role
            event = new AssocTargetRoleIntegrityEvent(
                    nodeService,
                    dictionaryService,
                    childAssocRef.getParentRef(),
                    childAssocRef.getTypeQName(),
                    childAssocRef.getQName());
            save(event);
        }
    }

    /**
     * @see AssocSourceMultiplicityIntegrityEvent
     * @see AssocTargetMultiplicityIntegrityEvent
     */
    public void onDeleteChildAssociation(ChildAssociationRef childAssocRef)
    {
        if (! storesToIgnore.contains(tenantService.getBaseName(childAssocRef.getChildRef().getStoreRef()).toString()))
        {
            IntegrityEvent event = null;
            // check source multiplicity
            event = new AssocSourceMultiplicityIntegrityEvent(
                    nodeService,
                    dictionaryService,
                    childAssocRef.getChildRef(),
                    childAssocRef.getTypeQName(),
                    true);
            save(event);
            // check target multiplicity
            event = new AssocTargetMultiplicityIntegrityEvent(
                    nodeService,
                    dictionaryService,
                    childAssocRef.getParentRef(),
                    childAssocRef.getTypeQName(),
                    true);
            save(event);
        }
    }

    /**
     * @see AssocSourceTypeIntegrityEvent
     * @see AssocTargetTypeIntegrityEvent
     * @see AssocSourceMultiplicityIntegrityEvent
     * @see AssocTargetMultiplicityIntegrityEvent
     */
    public void onCreateAssociation(AssociationRef nodeAssocRef)
    {
        if (! storesToIgnore.contains(tenantService.getBaseName(nodeAssocRef.getSourceRef().getStoreRef()).toString()))
        {
            IntegrityEvent event = null;
            // check source type
            event = new AssocSourceTypeIntegrityEvent(
                    nodeService,
                    dictionaryService,
                    nodeAssocRef.getSourceRef(),
                    nodeAssocRef.getTypeQName());
            save(event);
            // check target type
            event = new AssocTargetTypeIntegrityEvent(
                    nodeService,
                    dictionaryService,
                    nodeAssocRef.getTargetRef(),
                    nodeAssocRef.getTypeQName());
            save(event);
            // check source multiplicity
            event = new AssocSourceMultiplicityIntegrityEvent(
                    nodeService,
                    dictionaryService,
                    nodeAssocRef.getTargetRef(),
                    nodeAssocRef.getTypeQName(),
                    false);
            save(event);
            // check target multiplicity
            event = new AssocTargetMultiplicityIntegrityEvent(
                    nodeService,
                    dictionaryService,
                    nodeAssocRef.getSourceRef(),
                    nodeAssocRef.getTypeQName(),
                    false);
            save(event);
        }
    }

    /**
     * @see AssocSourceMultiplicityIntegrityEvent
     * @see AssocTargetMultiplicityIntegrityEvent
     */
    public void onDeleteAssociation(AssociationRef nodeAssocRef)
    {
        if (! storesToIgnore.contains(tenantService.getBaseName(nodeAssocRef.getSourceRef().getStoreRef()).toString()))
        {
            IntegrityEvent event = null;
            // check source multiplicity
            event = new AssocSourceMultiplicityIntegrityEvent(
                    nodeService,
                    dictionaryService,
                    nodeAssocRef.getTargetRef(),
                    nodeAssocRef.getTypeQName(),
                    true);
            save(event);
            // check target multiplicity
            event = new AssocTargetMultiplicityIntegrityEvent(
                    nodeService,
                    dictionaryService,
                    nodeAssocRef.getSourceRef(),
                    nodeAssocRef.getTypeQName(),
                    true);
            save(event);
        }
    }
    
    /**
     * Runs several types of checks, querying specifically for events that
     * will necessitate each type of test.
     * <p>
     * The interface contracts also requires that all events for the transaction
     * get cleaned up.
     */
    public void checkIntegrity() throws IntegrityException
    {
        if (!enabled)
        {
            return;
        }
        
        // process events and check for failures
        List<IntegrityRecord> failures = processAllEvents();
        // clear out all events
        AlfrescoTransactionSupport.unbindResource(KEY_EVENT_SET);
        
        // drop out quickly if there are no failures
        if (failures.isEmpty())
        {
            return;
        }
        
        // handle errors according to instance flags
        // firstly, log all failures
        int failureCount = failures.size();
        StringBuilder sb = new StringBuilder(300 * failureCount);
        sb.append("Found ").append(failureCount).append(" integrity violations");
        if (maxErrorsPerTransaction < failureCount)
        {
            sb.append(" - first ").append(maxErrorsPerTransaction);
        }
        sb.append(":");
        int count = 0;
        for (IntegrityRecord failure : failures)
        {
            // break if we exceed the maximum number of log entries
            count++;
            if (count > maxErrorsPerTransaction)
            {
                break;
            }
            sb.append("\n").append(failure);
        }
        boolean warnOnly = IntegrityChecker.isWarnInTransaction();
        if (failOnViolation && !warnOnly)
        {
            logger.error(sb.toString());
            throw new IntegrityException(sb.toString(), failures);
        }
        else
        {
            logger.warn(sb.toString());
            // no exception
        }
    }
    
    /**
     * Loops through all the integrity events and checks integrity.
     * <p>
     * The events are stored in a set, so there are no duplicates.  Since each
     * event performs a particular type of check, this ensures that we don't
     * duplicate checks.
     * 
     * @return Returns a list of integrity violations, up to the
     *      {@link #maxErrorsPerTransaction the maximum defined}
     */
    @SuppressWarnings("unchecked")
    private List<IntegrityRecord> processAllEvents()
    {
        // the results
        ArrayList<IntegrityRecord> allIntegrityResults = new ArrayList<IntegrityRecord>(0); // generally empty

        // get all the events for the transaction (or unit of work)
        // duplicates have been elimiated
        Map<IntegrityEvent, IntegrityEvent> events =
                (Map<IntegrityEvent, IntegrityEvent>) AlfrescoTransactionSupport.getResource(KEY_EVENT_SET);
        if (events == null)
        {
            // no events were registered - nothing of significance happened
            return allIntegrityResults;
        }

        // failure results for the event
        List<IntegrityRecord> integrityRecords = new ArrayList<IntegrityRecord>(0);

        // cycle through the events, performing checking integrity
        for (IntegrityEvent event : events.keySet())
        {
            try
            {
                event.checkIntegrity(integrityRecords);
            }
            catch (Throwable e)
            {
                // This means that integrity checking itself failed.  This is serious.
                // There are some exceptions that can be handled by transaction retries, so
                // we attempt to handle these and let them get out to trigger the retry.
                // Thanks to Carina Lansing.
                Throwable retryThrowable = RetryingTransactionHelper.extractRetryCause(e);
                if (retryThrowable != null)
                {
                    // The transaction will be retrying on this, so there's no need for the aggressive
                    // reporting that would normally happen
                    if (e instanceof RuntimeException) 
                    {
                        throw (RuntimeException) e; 
                    }
                    else
                    {
                        throw new RuntimeException(e); 
                    }
                }
                e.printStackTrace();
                // log it as an error and move to next event
                IntegrityRecord exceptionRecord = new IntegrityRecord("" + e.getMessage());
                exceptionRecord.setTraces(Collections.singletonList(e.getStackTrace()));
                allIntegrityResults.add(exceptionRecord);
                // move on
                continue;
            }

            // keep track of results needing trace added
            if (traceOn)
            {
                // record the current event trace if present
                for (IntegrityRecord integrityRecord : integrityRecords)
                {
                    integrityRecord.setTraces(event.getTraces());
                }
            }
            
            // copy all the event results to the final results
            allIntegrityResults.addAll(integrityRecords);
            // clear the event results
            integrityRecords.clear();
            
            if (allIntegrityResults.size() >= maxErrorsPerTransaction)
            {
                // only so many errors wanted at a time
                break;
            }
        }
        // done
        return allIntegrityResults;
    }

	@Override
    public void beforeCommit(boolean readOnly)
    {
		checkIntegrity();  
    }

	@Override
    public void beforeCompletion()
    {
		// NO-OP
    }

	@Override
    public void afterCommit()
    {
		// NO-OP
    }

	@Override
    public void afterRollback()
    {
		// NO-OP
    }
}
