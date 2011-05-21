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
package org.alfresco.repo.copy;

import java.util.Map;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Policies for the CopyService.
 * <p>
 * A typical registration and invocation would look like this:
 *  <code><pre>
 *  public void init()
 *  {
 *      this.policyComponent.bindClassBehaviour(
 *              OnCopyNodePolicy.QNAME,
 *              ActionModel.ASPECT_ACTIONS,
 *              new JavaBehaviour(this, "getCopyCallback"));
 *      this.policyComponent.bindClassBehaviour(
 *              OnCopyCompletePolicy.QNAME,
 *              ActionModel.ASPECT_ACTIONS,
 *              new JavaBehaviour(this, "onCopyComplete"));
 *      ...
 *  }
 *  
 *  public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
 *  {
 *      return new XyzAspectCopyBehaviourCallback();
 *  }
 *      
 *  private static class XyzAspectCopyBehaviourCallback extends DefaultCopyBehaviourCallback
 *  {
 *      // Override methods any to achieve the desired behaviour
 *      
 *      public boolean mustCopyChildAssociation(QName classQName, CopyDetails copyDetails, ChildAssociationRef childAssocRef)
 *      {
 *          ...
 *      }
 *  }
 *  
 *  public void onCopyComplete(
 *          NodeRef sourceNodeRef,
 *          NodeRef targetNodeRef,
 *          boolean copyToNewNode,
 *          Map<NodeRef,NodeRef> copyMap)
 *  {
 *      ...
 *  }
 *  </pre></code>
 * 
 * @author Derek Hulley
 */
public interface CopyServicePolicies 
{
    /**
     * Policy invoked when a <b>node</b> is copied.
     * <p>
     * <b>Note:</b> Copy policies are used to modify the copy behaviour.  Rather than attempt to
     *              determine, up front, the behaviour that applies for all types and aspects,
     *              the callbacks are used to lazily adjust the behaviour. 
     * <p>
     * Implementing this policy is particularly important if aspects want to partake in the copy process.
     * The behaviour can change whether or not the aspect is copied and which of the properties to carry
     * to the new node.
     * <p>
     * If no behaviour is registered or no callback is given, then
     * the {@link DefaultCopyBehaviourCallback default behaviour} is assumed.  Several pre-defined behaviours
     * exist to simplify the callbacks, including:
     * <ul>
     *   <li>Do nothing: {@link DoNothingCopyBehaviourCallback}</li>
     *   <li>Default:    {@link DefaultCopyBehaviourCallback}</li>
     * </ul>
     * The {@link DefaultCopyBehaviourCallback} is probably the best starting point for further
     * callback implementations; overriding the class allows the behaviour to be overridden, provided
     * that this policy method is implemented.
     * <p>
     * <b>Note: </b> A 'class' is either a type or an aspect.
     */
    public interface OnCopyNodePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback");
       
        /**
         * Called for all types and aspects before copying a node.
         * 
         * @param classRef                the type or aspect qualified name
         * @param copyDetails             the details of the impending copy
         * @return                        Return the callback that will be used to modify the copy behaviour for this
         *                                dictionary class.  Return <tt>null</tt> to assume the default.
         * 
         * @see CopyServicePolicies
         * 
         * @since V3.2
         */
        CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails);
        
        static Arg ARG_0 = Arg.KEY;
        static Arg ARG_1 = Arg.KEY;
    }
    
    /**
     * The intermediate copy callback, which occurs once it has been decided which properties
     *  and aspects will be copied, but before the copy occurs.
     * This allows you to remove cached data based on the destination node, before it is
     *  overwritten. You are unable to make changes to what gets copied though, that must
     *  be done earlier via a {@link OnCopyNodePolicy}.
     */
    public interface BeforeCopyPolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeCopy");
        
        /**
         * @param classRef          the type of the node that will be copied
         * @param sourceNodeRef     the original node
         * @param targetNodeRef     the destination node
         */
        public void beforeCopy(
                QName classRef,
                NodeRef sourceNodeRef,
                NodeRef targetNodeRef);
    }
    
    /**
     * Final callback after the copy (including any cascading) has been completed.  This should
     * be used where post-copy manipulation of nodes is required in order to enforce adherence
     * to a particular dictionary or business model.
     * <p>
     * The copy map contains all the nodes created during the copy, this helps to re-map
     * any potentially relative associations.
     */
    public interface OnCopyCompletePolicy extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyComplete");
        
        /**
         * @param classRef          the type of the node that was copied
         * @param sourceNodeRef     the original node
         * @param targetNodeRef     the destination node
         * @param copyMap           a map containing all the nodes that have been created during the copy
         */
        public void onCopyComplete(
                QName classRef,
                NodeRef sourceNodeRef,
                NodeRef targetNodeRef,
                boolean copyToNewNode,
                Map<NodeRef, NodeRef> copyMap);
    }
}
