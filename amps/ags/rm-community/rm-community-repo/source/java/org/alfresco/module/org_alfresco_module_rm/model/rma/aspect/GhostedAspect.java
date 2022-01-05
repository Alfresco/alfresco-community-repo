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

package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * rma:ghosted behaviour bean
 *
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   // optional
   defaultType = "rma:ghosted"
)
public class GhostedAspect extends    BaseBehaviourBean
                           implements ContentServicePolicies.OnContentUpdatePolicy
{
    /** I18N */
    private static final String MSG_GHOSTED_PROP_UPDATE = "rm.action.ghosted-prop-update";

    /**
     * Ensure that the content of a ghosted node can not be updated.
     *
     * @see org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy#onContentUpdate(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override
    @Behaviour
    (
       // required, use ASSOC for association behaviors
       kind = BehaviourKind.CLASS,
       // (defaults to EVERY_EVENT)
       notificationFrequency = NotificationFrequency.EVERY_EVENT,
       // (defaults to alf:<methodname>)
       policy = "alf:onContentUpdate",
       // required, unless defaultType set
       type = "rma:ghosted"

       // isService (default false)
       // name (only needs to specified if associated behvaiour object needs to be accessed)
       // assocType (defaults to cm:contains, used with BehaviourKind.ASSOC)
    )
    public void onContentUpdate(NodeRef content, boolean bNew)
    {
        throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_GHOSTED_PROP_UPDATE));
    }
}
