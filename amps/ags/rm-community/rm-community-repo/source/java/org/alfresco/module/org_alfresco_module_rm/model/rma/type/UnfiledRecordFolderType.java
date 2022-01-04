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

package org.alfresco.module.org_alfresco_module_rm.model.rma.type;

import java.util.Arrays;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.namespace.QName;

/**
 * rma:unfiledRecordFolder behaviour bean
 *
 * @author Silviu Dinuta
 * @since 2.6
 */
@BehaviourBean(defaultType = "rma:unfiledRecordFolder")
public class UnfiledRecordFolderType extends BaseBehaviourBean
            implements NodeServicePolicies.OnCreateChildAssociationPolicy
{
    private final static List<QName> ACCEPTED_NON_UNIQUE_CHILD_TYPES = Arrays.asList(TYPE_UNFILED_RECORD_FOLDER, ContentModel.TYPE_CONTENT, TYPE_NON_ELECTRONIC_DOCUMENT);

    @Override
    @Behaviour(kind = BehaviourKind.ASSOCIATION)
    public void onCreateChildAssociation(ChildAssociationRef childAssocRef, boolean isNewNode)
    {
        // We need to automatically cast the created folder to record folder if it is a plain folder
        // This occurs if the RM folder has been created via IMap, WebDav, etc. Don't check subtypes.
        // Some modules use hidden folder subtypes to store information (see RM-3283).
        QName childType = nodeService.getType(childAssocRef.getChildRef());
        if (childType.equals(ContentModel.TYPE_FOLDER))
        {
            nodeService.setType(childAssocRef.getChildRef(), TYPE_UNFILED_RECORD_FOLDER);
        }

        // check the created child is of an accepted type
        validateNewChildAssociationSubTypesIncluded(childAssocRef.getChildRef(), ACCEPTED_NON_UNIQUE_CHILD_TYPES);
    }
}
