/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
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
 * @author yanipig
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
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        /*
         * TODO: Move into MultilingualContentService
         */
        
        Locale localeAfter  = (Locale) after.get(ContentModel.PROP_LOCALE);
        Locale localeBefore = (Locale) before.get(ContentModel.PROP_LOCALE);

        // The locale can be set as null if the container have no children.
        // Normaly, it's ONLY the case at the creation of the container.
        if(localeAfter == null && nodeService.getChildAssocs(nodeRef).size() != 0)
        {
            throw new IllegalArgumentException("A Locale property must be defined for a Multilingual Container and can't be null");
        }

        if(localeAfter != null && !localeAfter.equals(localeBefore))
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
