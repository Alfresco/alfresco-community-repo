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
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
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
                    NodeServicePolicies.OnDeleteAssociationPolicy
{
    private static Log logger = LogFactory.getLog(IntegrityChecker.class);
    
    /** key against which the set of events is stored in the current transaction */
    private static final String KEY_EVENT_SET = "IntegrityChecker.EventSet";
    /** key to store the local flag to disable integrity errors, i.e. downgrade to warnings */
    private static final String KEY_WARN_IN_TRANSACTION = "IntegrityChecker.WarnInTransaction";
    
    private PolicyComponent policyComponent;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private boolean enabled;
    private boolean failOnViolation;
    private int maxErrorsPerTransaction;
    private boolean traceOn;
    
    /**
     * Downgrade violations to warnings within the current transaction.  This is temporary and
     * is <u>dependent on there being a current transaction</u> active against the
     * current thread.  When set, this will override the global 
     * {@link #setFailOnViolation(boolean) failure behaviour}.
     */
    public static void setWarnInTransaction()
    {
        AlfrescoTransactionSupport.bindResource(KEY_WARN_IN_TRANSACTION, Boolean.TRUE);
    }
    
    /**
     * @return Returns true if the current transaction should only warn on violations.
     *      If <code>false</code>, the global setting will take effect.
     * 
     * @see #setWarnInTransaction()
     */
    public static boolean isWarnInTransaction()
    {
        Boolean warnInTransaction = (Boolean) AlfrescoTransactionSupport.getResource(KEY_WARN_IN_TRANSACTION);
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

    /**
     * @see PropertiesIntegrityEvent
     */
    public void onUpdateProperties(
            NodeRef nodeRef,
            Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        IntegrityEvent event = null;
        // check properties on node
        event = new PropertiesIntegrityEvent(nodeService, dictionaryService, nodeRef);
        save(event);
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

    /**
     * @see AspectsIntegrityEvent
     */
    public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        IntegrityEvent event = null;
        // check mandatory aspects
        event = new AspectsIntegrityEvent(nodeService, dictionaryService, nodeRef);
        save(event);
        
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

    /**
     * @see AssocSourceMultiplicityIntegrityEvent
     * @see AssocTargetMultiplicityIntegrityEvent
     */
    public void onDeleteChildAssociation(ChildAssociationRef childAssocRef)
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

    /**
     * @see AssocSourceTypeIntegrityEvent
     * @see AssocTargetTypeIntegrityEvent
     * @see AssocSourceMultiplicityIntegrityEvent
     * @see AssocTargetMultiplicityIntegrityEvent
     */
    public void onCreateAssociation(AssociationRef nodeAssocRef)
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

    /**
     * @see AssocSourceMultiplicityIntegrityEvent
     * @see AssocTargetMultiplicityIntegrityEvent
     */
    public void onDeleteAssociation(AssociationRef nodeAssocRef)
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
}
