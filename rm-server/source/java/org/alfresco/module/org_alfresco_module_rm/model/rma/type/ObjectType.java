/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.model.rma.type;

import static org.alfresco.util.ParameterCheck.mandatory;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
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
@BehaviourBean
(
   defaultType = "cm:cmobject"
)
public class ObjectType extends BaseBehaviourBean implements NodeServicePolicies.OnMoveNodePolicy//, CopyServicePolicies.BeforeCopyPolicy
{
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy#onMoveNode(org.alfresco.service.cmr.repository.ChildAssociationRef, org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS
    )
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        mandatory("oldChildAssocRef", oldChildAssocRef);
        mandatory("newChildAssocRef", newChildAssocRef);

        NodeRef object = oldChildAssocRef.getChildRef();
        QName objectType = nodeService.getType(object);

        NodeRef target = newChildAssocRef.getParentRef();
        boolean isTargetFilePlanComponent = isFilePlanComponent(target);

        if (!objectType.equals(ContentModel.TYPE_CONTENT) && isTargetFilePlanComponent)
        {
            throw new AlfrescoRuntimeException("Only documents can be moved from a collaboration site into a RM site.");
        }

        if (isTargetFilePlanComponent && !isRecordFolder(target))
        {
            throw new AlfrescoRuntimeException("A document can only be copied into a folder in RM site.");
        }
    }

//    /**
//     * @see org.alfresco.repo.copy.CopyServicePolicies.BeforeCopyPolicy#beforeCopy(org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
//     */
//    @Override
//    @Behaviour
//    (
//       kind = BehaviourKind.CLASS
//    )
//    public void beforeCopy(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef)
//    {
//        mandatory("sourceNodeRef", sourceNodeRef);
//        mandatory("targetNodeRef", targetNodeRef);
//
//        NodeRef sourceParentNodeRef = nodeService.getPrimaryParent(sourceNodeRef).getParentRef();
//        boolean isSourceParentNodeFilePlanComponent = isFilePlanComponent(sourceParentNodeRef);
//        boolean isTargetNodeFilePlanComponent = isFilePlanComponent(targetNodeRef);
//
//        if (!isSourceParentNodeFilePlanComponent && isTargetNodeFilePlanComponent)
//        {
//            throw new AlfrescoRuntimeException("Nothing can be copied from a collaboration site into a RM site.");
//        }
//    }
}
