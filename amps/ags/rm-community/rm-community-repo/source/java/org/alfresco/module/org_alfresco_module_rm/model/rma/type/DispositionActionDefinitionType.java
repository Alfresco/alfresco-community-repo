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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * rma:dispositionActionDefinition behaviour bean
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   defaultType = "rma:dispositionActionDefinition"
)
public class DispositionActionDefinitionType extends    BaseBehaviourBean
                                             implements NodeServicePolicies.OnUpdatePropertiesPolicy
{
    /** I18N */
    private static final String MSG_UPDATE_DISP_ACT_DEF = "rm.service.update-disposition-action-def";

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        if (nodeService.exists(nodeRef))
        {
            // Determine the properties that have changed
            Set<QName> changedProps = new HashSet<>(PropertyMap.getChangedProperties(before, after).keySet());
            changedProps.addAll(PropertyMap.getAddedProperties(before, after).keySet());

            if (!nodeService.hasAspect(nodeRef, ASPECT_UNPUBLISHED_UPDATE))
            {
                // Apply the unpublished aspect
                Map<QName, Serializable> props = new HashMap<>();
                props.put(PROP_UPDATE_TO, UPDATE_TO_DISPOSITION_ACTION_DEFINITION);
                props.put(PROP_UPDATED_PROPERTIES, (Serializable)changedProps);
                nodeService.addAspect(nodeRef, ASPECT_UNPUBLISHED_UPDATE, props);
            }
            else
            {
                Map<QName, Serializable> props = nodeService.getProperties(nodeRef);

                // Check that there isn't a update currently being published
                if ((Boolean)props.get(PROP_PUBLISH_IN_PROGRESS).equals(Boolean.TRUE))
                {
                    // Can not update the disposition schedule since there is an outstanding update being published
                    throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_UPDATE_DISP_ACT_DEF));
                }

                // Update the update information
                props.put(PROP_UPDATE_TO, UPDATE_TO_DISPOSITION_ACTION_DEFINITION);
                props.put(PROP_UPDATED_PROPERTIES, (Serializable)changedProps);
                nodeService.setProperties(nodeRef, props);
            }
        }

    }
}
