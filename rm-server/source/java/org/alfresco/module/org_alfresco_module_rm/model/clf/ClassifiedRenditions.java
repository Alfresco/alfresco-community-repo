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
package org.alfresco.module.org_alfresco_module_rm.model.clf;

import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferralAdminService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Behaviour bean for classified rendition nodes.
 *
 * @since 2.4.a
 */
@BehaviourBean
(
   defaultType = "rn:rendition"
)
public class ClassifiedRenditions extends BaseBehaviourBean
                                  implements NodeServicePolicies.OnAddAspectPolicy,
                                             ClassifiedContentModel
{
    private ContentClassificationService contentClassificationService;
    private ReferralAdminService         referralAdminService;
    private RenditionService             renditionService;

    public void setContentClassificationService(ContentClassificationService service)
    {
        this.contentClassificationService = service;
    }

    public void setReferralAdminService(ReferralAdminService service)
    {
        this.referralAdminService = service;
    }

    public void setRenditionService(RenditionService service)
    {
        this.renditionService = service;
    }

    /**
     * Behaviour associated with creating a rendition of an already classified node.
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.EVERY_EVENT
    )
    public void onAddAspect(final NodeRef renditionNodeRef, final QName aspectTypeQName)
    {
        // When a rendition is created, set up a metadata link of its classification to the source node.
        authenticationUtil.runAs(new org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork<Void>()
        {
            public Void doWork()
            {
                final ChildAssociationRef chAssRef = renditionService.getSourceNode(renditionNodeRef);
                final NodeRef sourceNode = chAssRef.getParentRef();
                if (contentClassificationService.isClassified(sourceNode) &&
                        referralAdminService.getAttachedReferralFrom(renditionNodeRef, ASPECT_CLASSIFIED) != null)
                {
                    referralAdminService.attachReferrer(renditionNodeRef, sourceNode, ASPECT_CLASSIFIED);
                }

                return null;
            }
        }, authenticationUtil.getSystemUserName());
    }
}
