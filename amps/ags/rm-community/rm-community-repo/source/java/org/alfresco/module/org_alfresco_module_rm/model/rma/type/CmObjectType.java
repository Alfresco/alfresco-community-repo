/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.model.rma.type;

import static org.alfresco.util.ParameterCheck.mandatory;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * cm:cmobject behaviour bean
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
@BehaviourBean(
        defaultType = "cm:cmobject")
public class CmObjectType extends BaseBehaviourBean implements NodeServicePolicies.OnMoveNodePolicy, CopyServicePolicies.BeforeCopyPolicy
{
    /** Move behaviour name */
    private static final String MOVE_BEHAVIOUR_NAME = "onMoveCmObjectType";

    /** Copy behaviour name */
    private static final String COPY_BEHAVIOUR_NAME = "onCopyCmObjectType";

    /**
     * Disable the move behaviour for this transaction
     *
     */
    public void disableMove()
    {
        getBehaviour(MOVE_BEHAVIOUR_NAME).disable();
    }

    /**
     * Enable the move behaviour for this transaction
     *
     */
    public void enableMove()
    {
        getBehaviour(MOVE_BEHAVIOUR_NAME).enable();
    }

    /**
     * Disable the copy behaviour for this transaction
     *
     */
    public void disableCopy()
    {
        getBehaviour(COPY_BEHAVIOUR_NAME).disable();
    }

    /**
     * Enable the copy behaviour for this transaction
     *
     */
    public void enableCopy()
    {
        getBehaviour(COPY_BEHAVIOUR_NAME).enable();
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    @Behaviour(
            kind = BehaviourKind.CLASS,
            name = MOVE_BEHAVIOUR_NAME)
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        mandatory("oldChildAssocRef", oldChildAssocRef);
        mandatory("newChildAssocRef", newChildAssocRef);

        NodeRef sourceParent = oldChildAssocRef.getParentRef();
        NodeRef targetParent = newChildAssocRef.getParentRef();
        boolean sourceIsRm = isFilePlanComponent(sourceParent);
        boolean targetIsRm = isFilePlanComponent(targetParent);

        // This runs for all move operations, if both source and target are within
        // the RM site no additional validation is needed, so we can stop here
        if (sourceIsRm && targetIsRm)
        {
            return;
        }

        if (isMoveIntoRmSite(sourceIsRm, targetIsRm))
        {
            QName objectType = nodeService.getType(oldChildAssocRef.getChildRef());
            validateMoveIntoRm(objectType, targetParent);
        }
    }

    private boolean isMoveIntoRmSite(boolean sourceIsRm, boolean targetIsRm)
    {
        return !sourceIsRm && targetIsRm;
    }

    private void validateMoveIntoRm(QName objectType, NodeRef targetParent)
    {
        if (!instanceOf(objectType, ContentModel.TYPE_CONTENT))
        {
            throw new AlfrescoRuntimeException("Only documents of types cm:content or subtypes can be moved from a collaboration site into a RM site.");
        }

        if (!isRecordFolder(targetParent))
        {
            throw new AlfrescoRuntimeException("A document can only be moved into a folder in RM site.");
        }
    }

    /**
     * @see org.alfresco.repo.copy.CopyServicePolicies.BeforeCopyPolicy#beforeCopy(org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Behaviour(
            kind = BehaviourKind.CLASS,
            name = COPY_BEHAVIOUR_NAME)
    public void beforeCopy(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef)
    {
        mandatory("sourceNodeRef", sourceNodeRef);
        mandatory("targetNodeRef", targetNodeRef);

        NodeRef sourceParentNodeRef = nodeService.getPrimaryParent(sourceNodeRef).getParentRef();
        boolean isSourceParentFilePlanComponent = isFilePlanComponent(sourceParentNodeRef);

        NodeRef targetParentNodeRef = nodeService.getPrimaryParent(targetNodeRef).getParentRef();
        boolean isTargetNodeParentFilePlanComponent = isFilePlanComponent(targetParentNodeRef);

        // If we are doing the copy operation within the RM site then we can stop here
        // The method should just check copy operations from outside of RM into the RM site
        if (isSourceParentFilePlanComponent && isTargetNodeParentFilePlanComponent)
        {
            return;
        }

        // Do not allow to copy anything outside of RM site into the RM site
        if (!isSourceParentFilePlanComponent && isTargetNodeParentFilePlanComponent)
        {
            throw new AlfrescoRuntimeException("Nothing can be copied from a collaboration site into a RM site.");
        }
    }
}
