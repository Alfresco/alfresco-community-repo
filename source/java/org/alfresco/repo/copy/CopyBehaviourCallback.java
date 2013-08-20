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

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * A callback to modify copy behaviour associated with a given type or aspect.  This
 * callback is called per type and per aspect.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public interface CopyBehaviourCallback
{
    /**
     * Description of how the copy process should handle multiplicity of peer associations
     * at the <b>source</b> end of the association.<br/>
     * The order of this enum denotes the priority when mixing behaviour as well;
     * that is to say that a 'ignore' behaviour will occur even if an 'copy' is
     * also provided by the registered behaviour callbacks.
     * 
     * @author Derek Hulley
     * @since 3.3SP3
     */
    public enum AssocCopySourceAction implements Comparable<AssocCopySourceAction>
    {
        /**
         * Always copy the association.
         * <br/>
         * Note that this can cause duplicate associations when copying over
         * {@link CopyAssociationDetails#isTargetNodeIsNew() existing target nodes}.
         */
        COPY,
        /**
         * Always copy the association but remove the copy-target's matching associations
         * when copying over an existing node.
         * <br/>
         * This is akin to the original CopyService behaviour
         * (see <a href=https://issues.alfresco.com/jira/browse/ALF-958>ALF-958</a>).
         */
        COPY_REMOVE_EXISTING,
        /**
         * Ignore the association
         */
        IGNORE,
    }
    
    /**
     * Description of how the copy process should handle multiplicity of peer associations
     * at the <b>target</b> end of the association.<br/>
     * The order of this enum denotes the priority when mixing behaviour as well;
     * that is to say that a 'ignore' behaviour will occur even if an 'copy' is
     * also provided by the registered behaviour callbacks.
     * 
     * @author Derek Hulley
     * @since 3.3SP3
     */
    public enum AssocCopyTargetAction implements Comparable<AssocCopyTargetAction>
    {
        /**
         * The copied association will use, as target, the original association's target
         * i.e. the target of the association will remain as it was.
         */
        USE_ORIGINAL_TARGET,
        /**
         * The copied association will use, as target, the node <i>copied from</i> the
         * original target; if the original association's target is <i>not</i> copied
         * in the process, then nothing is done.
         */
        USE_COPIED_TARGET,
        /**
         * The copied association will use, as target, the node <i>copied from</i> the
         * original target; if the original association's target is <i>not</i> copied
         * in the original target is used.
         */
        USE_COPIED_OTHERWISE_ORIGINAL_TARGET,
    }
    
    /**
     * Description of how the copy process should traverse a child association.
     * The order of this enum denotes the priority when mixing behaviour as well;
     * that is to say that a 'forced recursion' will occur even if an 'ignore' is
     * also provided by the registered behaviour callbacks.
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    public enum ChildAssocCopyAction implements Comparable<ChildAssocCopyAction>
    {
        /**
         * Ignore the child association
         */
        IGNORE,
        /**
         * Copy the association only, keeping the existing child node
         */
        COPY_ASSOC,
        /**
         * Traverse the child association and <b>copy</b> the child node
         */
        COPY_CHILD,
    }
    
    /**
     * Description of how the copy process should behave when copying an association.
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    public enum ChildAssocRecurseAction implements Comparable<ChildAssocRecurseAction>
    {
        /**
         * Respect the client's recursion decision
         */
        RESPECT_RECURSE_FLAG,
        /**
         * Force all further copies of the source hierarchy to recurse into children.
         * This allows behaviour to force a copy of a subtree that it expects to
         * exist.
         * <p>
         * <b>NOTE</b>: Any part of the source subtree can still terminate the recursion,
         *              so this is mainly useful where the subtree contains the default
         *              behaviour.
         */
        FORCE_RECURSE,
    }
    
    /**
     * A simple bean class to convey information to the callback methods dealing with
     * copying of associations.
     *
     * @see CopyBehaviourCallback#getAssociationCopyAction(QName, CopyDetails, CopyAssociationDetails)
     * 
     * @author Derek Hulley
     * @since 3.3SP3
     */
    public static final class CopyAssociationDetails
    {
        private final AssociationRef assocRef;
        private final NodeRef copyTarget;
        private final boolean copyTargetIsNew;
        
        public CopyAssociationDetails(
                AssociationRef assocRef,
                NodeRef copyTarget,
                boolean copyTargetIsNew)
        {
            this.assocRef = assocRef;
            this.copyTarget = copyTarget;
            this.copyTargetIsNew = copyTargetIsNew;
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder(256);
            sb.append("CopyChildAssociationDetails ")
              .append("[ assocRef=").append(assocRef)
              .append(", copyTarget=").append(copyTarget)
              .append(", copyTargetIsNew=").append(copyTargetIsNew)
              .append("]");
            return sb.toString();
        }

        /**
         * @return          Returns the association being examined
         */
        public final AssociationRef getAssocRef()
        {
            return assocRef;
        }

        /**
         * @return          Returns the node that will be the
         *                  new source if the association is copied
         */
        public final NodeRef getCopyTarget()
        {
            return copyTarget;
        }

        /**
         * 
         * @return          Returns <tt>true</tt> if the {@link #getCopyTarget() copy target node}
         *                  has been newly created by the copy process or <tt>false</tt> if it
         *                  is a node that existed prior to the copy
         */
        public final boolean getCopyTargetIsNew()
        {
            return copyTargetIsNew;
        }
    }
    
    /**
     * A simple bean class to convey information to the callback methods dealing with
     * copying of child associations.
     *
     * @see CopyBehaviourCallback#getChildAssociationCopyAction(QName, CopyDetails, CopyChildAssociationDetails)
     * @see CopyBehaviourCallback#getChildAssociationRecurseAction(QName, CopyDetails, CopyChildAssociationDetails)
     * 
     * @author Derek Hulley
     * @since 3.2
     */
    public static final class CopyChildAssociationDetails
    {
        private final ChildAssociationRef childAssocRef;
        private final NodeRef copyTarget;
        private final boolean copyTargetIsNew;
        private final boolean copyChildren;
        
        public CopyChildAssociationDetails(
                ChildAssociationRef childAssocRef,
                NodeRef copyTarget,
                boolean copyTargetIsNew,
                boolean copyChildren)
        {
            this.childAssocRef = childAssocRef;
            this.copyTarget = copyTarget;
            this.copyTargetIsNew = copyTargetIsNew;
            this.copyChildren = copyChildren;
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder(256);
            sb.append("CopyChildAssociationDetails ")
              .append("[ childAssocRef=").append(childAssocRef)
              .append(", copyTarget=").append(copyTarget)
              .append(", copyTargetIsNew=").append(copyTargetIsNew)
              .append(", copyChildren=").append(copyChildren)
              .append("]");
            return sb.toString();
        }

        /**
         * @return          Returns the association being examined
         */
        public final ChildAssociationRef getChildAssocRef()
        {
            return childAssocRef;
        }

        /**
         * @return          Returns the node that will be the
         *                  new parent if the association is copied
         */
        public final NodeRef getCopyTarget()
        {
            return copyTarget;
        }

        /**
         * 
         * @return          Returns <tt>true</tt> if the {@link #getCopyTarget() target node}
         *                  has been newly created by the copy process or <tt>false</tt> if it
         *                  is a node that existed prior to the copy
         */
        public final boolean getCopyTargetIsNew()
        {
            return copyTargetIsNew;
        }

        /**
         * Get the current recursion behaviour.  This can be ignored and even altered, if required.
         * 
         * @return          Returns <tt>true</tt> if the copy process is currently recursing to
         *                  child associations or <tt>false</tt> if not.
         */
        public final boolean isCopyChildren()
        {
            return copyChildren;
        }
    }
    
    /**
     * Determine if this type or aspect must be copied.  If the callback is for a type
     * (not aspect) then this determines if the node is copied at all.  If the callback
     * is for an aspect, then this determines if the aspect is copied.
     * 
     * @param classQName            the name of the class that this is being invoked for
     * @param copyDetails           the source node's copy details for quick reference
     * @return                      <tt>true</tt> if the type or aspect that this behaviour
     *                              represents must be copied.
     */
    boolean getMustCopy(QName classQName, CopyDetails copyDetails);
    
    /**
     * Determine if this top-level node with type or aspect can be renamed during copy.
     * 
     * @param classQName            the name of the class that this is being invoked for
     * @param copyDetails           the source node's copy details for quick reference
     * @return                      <tt>true</tt> if the top-level node with type or aspect
     *                              can be renamed during copy.
     */
    boolean isTopLevelCanBeRenamed(QName classQName, CopyDetails copyDetails);
    
    /**
     * Determine the copy behaviour associated with a given peer association.
     * 
     * @param classQName            the name of the class that this is being invoked for
     * @param copyDetails           the source node's copy details for quick reference
     * @param assocCopyDetails      all other details relating to the association
     * @return                      Returns the copy actions
     *                              ({@link AssocCopySourceAction source} and {@link AssocCopySourceAction target})
     *                              to take with the given association
     */
    Pair<AssocCopySourceAction, AssocCopyTargetAction> getAssociationCopyAction(
            QName classQName,
            CopyDetails copyDetails,
            CopyAssociationDetails assocCopyDetails);
    
    /**
     * Determine if a copy should copy the child, the association only or do nothing with
     * the given association.
     * <p>
     * This is called regardless of whether 'cascade' copy has been selected by the client
     * of the copy.  Some type and aspect behaviour will mandate a copy of the child
     * associations regardless of whether recursion is on.
     * 
     * @param classQName            the name of the class that this is being invoked for
     * @param copyDetails           the source node's copy details for quick reference
     * @param childAssocCopyDetails all other details relating to the child association
     * @return                      Returns the copy {@link ChildAssocCopyAction action} to take
     *                              with the given child association
     */
    ChildAssocCopyAction getChildAssociationCopyAction(
            QName classQName,
            CopyDetails copyDetails,
            CopyChildAssociationDetails childAssocCopyDetails);
    
    /**
     * Once the child association copy action has been chosen, the policy callback can
     * dictate whether or not to force further recursion.  This cannot prevent
     * behaviour further down the hierarchy from stopping the copy.
     * 
     * @param classQName            the name of the class that this is being invoked for
     * @param copyDetails           the source node's copy details for quick reference
     * @param childAssocCopyDetails all other details relating to the child association
     * @return                      Returns the type of {@link ChildAssocRecurseAction recursion}
     *                              to perform after having copied the child association
     * 
     * @see #getChildAssociationCopyAction(QName, CopyDetails, ChildAssociationRef, boolean)
     */
    ChildAssocRecurseAction getChildAssociationRecurseAction(
            QName classQName,
            CopyDetails copyDetails,
            CopyChildAssociationDetails childAssocCopyDetails);
    
    /**
     * Modify the properties that are copied across.
     * 
     * @param classQName            the name of the class that this is being invoked for
     * @param copyDetails           the source node's copy details for quick reference
     * @param properties            the type- or aspect-specific properties that can be copied.
     *                              The map can be manipulated and returned as required.
     * @return                      Returns the type or aspect properties that should be copied.
     */
    Map<QName, Serializable> getCopyProperties(
            QName classQName,
            CopyDetails copyDetails,
            Map<QName, Serializable> properties);
}
