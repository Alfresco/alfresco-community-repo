/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.model.ml;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Class containing behaviour for the multilingual document aspect.
 *
 * {@link ContentModel#ASPECT_MULTILINGUAL_DOCUMENT ml document aspect}
 *
 * @author yanipig
 */
public class MultilingualDocumentAspect implements
        CopyServicePolicies.OnCopyNodePolicy,
        NodeServicePolicies.BeforeDeleteChildAssociationPolicy,
        NodeServicePolicies.OnUpdatePropertiesPolicy
{

    // Dependencies
    private PolicyComponent policyComponent;
    private MultilingualContentService multilingualContentService;
    private NodeService nodeService;

    /**
     * Initialise the Multilingual Aspect
     *
     * Ensures that the {@link ContentModel#ASPECT_MULTILINGUAL_DOCUMENT ml document aspect}
     */
    public void init()
    {
        this.policyComponent.bindClassBehaviour(
                CopyServicePolicies.OnCopyNodePolicy.QNAME,
                ContentModel.ASPECT_MULTILINGUAL_DOCUMENT,
                new JavaBehaviour(this, "getCopyCallback"));

        this.policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.BeforeDeleteChildAssociationPolicy.QNAME,
                ContentModel.TYPE_MULTILINGUAL_CONTAINER,
                ContentModel.ASSOC_MULTILINGUAL_CHILD,
                new JavaBehaviour(this, "beforeDeleteChildAssociation"));

        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                ContentModel.ASPECT_MULTILINGUAL_DOCUMENT,
                new JavaBehaviour(this, "onUpdateProperties"));

    }

    /**
     * @param policyComponent
     *            the policy component to register behaviour with
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param multilingualContentService
     *            the Multilingual Content Service to set
     */
    public void setMultilingualContentService(MultilingualContentService multilingualContentService)
    {
        this.multilingualContentService = multilingualContentService;
    }

    /**
     * @param nodeService
     *            the Node Service to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * The copy of a <b>cm:mlDocument</b> can't keep the Multilingual aspect.
     * 
     * @return Returns the {@link DoNothingCopyBehaviourCallback}
     */
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return DoNothingCopyBehaviourCallback.getInstance();
    }

    /**
     * Make sure that the child is no longer a translation and that the associated container is cleaned up, if required.
     */
    @Override
    public void beforeDeleteChildAssociation(ChildAssociationRef childAssocRef)
    {
        NodeRef childNodeRef = childAssocRef.getChildRef();
        // This will remove the aspect and take care of the translation container, as required
        multilingualContentService.unmakeTranslation(childNodeRef);
    }

    /**
     * Ensure that the locale is unique inside the <b>mlContainer</b>.
     *
     * If the locale of a pivot translation is modified, the pivot locale reference of the mlContainer must be modified too.
     */
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        Locale localeBefore = (Locale) before.get(ContentModel.PROP_LOCALE);

        Locale localeAfter = null;
        Serializable objLocaleAfter = after.get(ContentModel.PROP_LOCALE);
        if (objLocaleAfter != null)
        {
            localeAfter = DefaultTypeConverter.INSTANCE.convert(Locale.class, objLocaleAfter);
        }

        // if the local has been modified
        if (localeBefore == null || !localeBefore.equals(localeAfter))
        {
            NodeRef mlContainer = multilingualContentService.getTranslationContainer(nodeRef);

            // Since the map returned by the getTranslations doesn't duplicate keys, the size of this map will be
            // different of the size of the number of children of the mlContainer if a duplicate locale is found.
            int transSize = multilingualContentService.getTranslations(mlContainer).size();
            int childSize = nodeService.getChildAssocs(mlContainer, ContentModel.ASSOC_MULTILINGUAL_CHILD, RegexQNamePattern.MATCH_ALL).size();

            // if duplicate locale found
            if (transSize != childSize)
            {
                // throw an exception and the current transaction will be rolled back. The properties will not be
                // longer in an illegal state.
                throw new IllegalArgumentException("The locale " + localeAfter +
                        " can't be changed for the node " + nodeRef +
                        " because this locale is already in use in an other translation of the same " +
                        ContentModel.TYPE_MULTILINGUAL_CONTAINER + ".");
            }

            // get the locale of ML Container
            Locale localMlContainer = (Locale) nodeService.getProperty(
                    mlContainer,
                    ContentModel.PROP_LOCALE);

            // if locale of the container is equals to the locale of
            // the node (before update). The nodeRef is the pivot language
            // and the locale of the mlContainer must be modified
            if (localeBefore != null && localeBefore.equals(localMlContainer))
            {
                nodeService.setProperty(
                        mlContainer,
                        ContentModel.PROP_LOCALE,
                        localeAfter);
            }

        }

        // else no action to perform
    }
}
