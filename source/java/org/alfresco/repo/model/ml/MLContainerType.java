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
package org.alfresco.repo.model.ml;

import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Class containing behaviour for the multilingual multilingual container type.
 * A multilingual container type is fonctionally named '<b>Logical Document</b>'.
 * It links each translation of a semantical message together
 *
 * {@link ContentModel#TYPE_MULTILINGUAL_CONTAINER multilingual container type}
 *
 * @author Yannick Pignot
 */
public class MLContainerType implements
        NodeServicePolicies.OnUpdatePropertiesPolicy
{
    private PolicyComponent policyComponent;
    private NodeService nodeService;
    private MultilingualContentService multilingualContentService;

    /**
     * Initialise the Multilingual Container Type
     * <p>
     * Ensures that the {@link ContentModel#ASPECT_MULTILINGUAL_EMPTY_TRANSLATION ml empty document aspect}
     */
    public void init()
    {
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"),
                ContentModel.TYPE_MULTILINGUAL_CONTAINER,
                new JavaBehaviour(this, "onUpdateProperties"));
    }

    /**
     * @param policyComponent the policy component to register behaviour with
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param multilingualContentService the Multilingual Content Service to set
     */
    public void setMultilingualContentService(
            MultilingualContentService multilingualContentService)
    {
        this.multilingualContentService = multilingualContentService;
    }

    /**
     * @param nodeService the Node Service to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * The property <b>locale</b> of a <b>cm:mlContainer</b> represents the locale of the pivot translation.
     *
     * Since the pivot must be an existing translation and the pivot can t be empty, some tests must be performed when
     * the locale of the mlContainer is updated.
     */
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        Locale localeAfter  = (Locale) after.get(ContentModel.PROP_LOCALE);
        Locale localeBefore = (Locale) before.get(ContentModel.PROP_LOCALE);

        if (localeAfter == null)
        {
            throw new IllegalArgumentException("The ML container cannot have a null locale.");
        }
        
        // If the locale is changing, then ensure that the pivot translation is present and matches
        if (localeBefore != null && !localeAfter.equals(localeBefore))
        {
            Map<Locale, NodeRef> translations = multilingualContentService.getTranslations(nodeRef);

            //get the new pivot translation
            NodeRef pivot = translations.get(localeAfter);

            if(pivot == null)
            {
                throw new IllegalArgumentException("The pivot translation must be an existing translation");
            }
            else if(nodeService.hasAspect(pivot, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
            {
                throw new IllegalArgumentException("The pivot translation can't be an empty translation");
            }
        }
    }
}
