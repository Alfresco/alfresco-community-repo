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
package org.alfresco.module.org_alfresco_module_rm.model.clf.aspect;

import static org.alfresco.module.org_alfresco_module_rm.util.RMCollectionUtils.diffKey;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationException.MissingDowngradeInstructions;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevel;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationSchemeService;
import org.alfresco.module.org_alfresco_module_rm.classification.ClassificationSchemeService.Reclassification;
import org.alfresco.module.org_alfresco_module_rm.classification.model.ClassifiedContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.referredmetadata.ReferralAdminService;
import org.alfresco.module.org_alfresco_module_rm.util.RMCollectionUtils.Difference;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * clf:classification behaviour bean
 *
 * @since 2.4.a
 */
@BehaviourBean
(
   defaultType = "clf:classified"
)
public class ClassifiedAspect extends BaseBehaviourBean implements NodeServicePolicies.OnUpdatePropertiesPolicy,
                                                                   NodeServicePolicies.OnAddAspectPolicy,
                                                                   NodeServicePolicies.OnRemoveAspectPolicy,
                                                                   ClassifiedContentModel
{
    private ClassificationSchemeService classificationSchemeService;
    private ReferralAdminService        referralAdminService;
    private RenditionService            renditionService;

    public void setClassificationSchemeService(ClassificationSchemeService service)
    {
        this.classificationSchemeService = service;
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
     * Behaviour associated with updating the classified aspect properties.
     * <p>
     * Validates the consistency of the properties.
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.EVERY_EVENT
    )
    public void onUpdateProperties(final NodeRef classifiedNode,
                                   final Map<QName, Serializable> before,
                                   final Map<QName, Serializable> after)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork()
            {
                final Difference classificationChange = diffKey(before, after, PROP_CURRENT_CLASSIFICATION);

                if (classificationChange == Difference.CHANGED && nodeService.hasAspect(classifiedNode, ASPECT_CLASSIFIED))
                {
                    final String oldValue = (String)before.get(PROP_CURRENT_CLASSIFICATION);
                    final String newValue = (String)after.get(PROP_CURRENT_CLASSIFICATION);

                    final ClassificationLevel oldLevel = classificationSchemeService.getClassificationLevelById(oldValue);
                    final ClassificationLevel newLevel = classificationSchemeService.getClassificationLevelById(newValue);

                    Reclassification reclassification = classificationSchemeService.getReclassification(oldLevel, newLevel);

                    if (reclassification != null)
                    {
                        nodeService.setProperty(classifiedNode, PROP_LAST_RECLASSIFICATION_ACTION, reclassification.toModelString());
                        nodeService.setProperty(classifiedNode, PROP_LAST_RECLASSIFY_AT, new Date());
                    }
                }

                checkConsistencyOfProperties(classifiedNode);

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Behaviour associated with adding the classified aspect.
     * <p>
     * Validates the consistency of the properties.
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    public void onAddAspect(final NodeRef classifiedNode, final QName aspectTypeQName)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork()
            {
                checkConsistencyOfProperties(classifiedNode);

                // If this node has any renditions, we must ensure that they inherit the classification
                // from their source node.
                final List<ChildAssociationRef> renditions = renditionService.getRenditions(classifiedNode);
                for (ChildAssociationRef chAssRef : renditions)
                {
                    final NodeRef renditionNode = chAssRef.getChildRef();
                    if (referralAdminService.getAttachedReferralFrom(renditionNode, ASPECT_CLASSIFIED) == null)
                    {
                        referralAdminService.attachReferrer(renditionNode, classifiedNode, ASPECT_CLASSIFIED);
                    }
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Behaviour associated with removing the classified aspect.
     * <p>
     * Validates the consistency of the properties.
     */
    @Override
    @Behaviour
    (
        kind = BehaviourKind.CLASS,
        notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    public void onRemoveAspect(final NodeRef classifiedNode, final QName aspectTypeQName)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            public Void doWork()
            {
                // If this node has any renditions, we should remove the metadata link
                final List<ChildAssociationRef> renditions = renditionService.getRenditions(classifiedNode);
                for (ChildAssociationRef chAssRef : renditions)
                {
                    // In RM, renditions are only attached to one metadata referent - the source node.
                    // Therefore it is safe to (and we must) remove the aspect from the rendition node.
                    nodeService.removeAspect(chAssRef.getChildRef(), ASPECT_CLASSIFIED_RENDITION);
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * Check the consistency of the classification properties and throw an exception if they are invalid.
     *
     * @param nodeRef The classified node.
     */
    protected void checkConsistencyOfProperties(NodeRef nodeRef) throws MissingDowngradeInstructions
    {
        if (nodeService.hasAspect(nodeRef, ASPECT_CLASSIFIED))
        {
            Serializable downgradeDate = nodeService.getProperty(nodeRef, PROP_DOWNGRADE_DATE);
            Serializable downgradeEvent = nodeService.getProperty(nodeRef, PROP_DOWNGRADE_EVENT);
            Serializable downgradeInstructions = nodeService.getProperty(nodeRef, PROP_DOWNGRADE_INSTRUCTIONS);
            if (isEmpty(downgradeInstructions) && !(isEmpty(downgradeDate) && isEmpty(downgradeEvent)))
            {
                throw new MissingDowngradeInstructions(nodeRef);
            }
        }
    }

    /**
     * Check if a property is null or the empty string. Note that this is the same as
     * {@link org.apache.commons.lang.StringUtils#isEmpty(String)}, except that it takes a Serializable rather than a
     * String. This avoids awkward casting exceptions when working with properties.
     *
     * @param value The (probably String) value to check.
     * @return true if the supplied value is null or the empty string.
     */
    private boolean isEmpty(Serializable value)
    {
        boolean isEmpty;

        if (value instanceof String)
        {
            isEmpty = isBlank((String) value);
        }
        else
        {
            isEmpty = (value == null || value.equals(""));
        }

        return isEmpty;
    }
}
