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

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.service.cmr.ml.MultilingualContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
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
        CopyServicePolicies.OnCopyCompletePolicy,
        NodeServicePolicies.BeforeDeleteNodePolicy,
        NodeServicePolicies.BeforeRemoveAspectPolicy,
        NodeServicePolicies.OnRemoveAspectPolicy,
        NodeServicePolicies.OnUpdatePropertiesPolicy
{

    //     Dependencies
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
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyNode"), 
                ContentModel.ASPECT_MULTILINGUAL_DOCUMENT, 
                new JavaBehaviour(this, "onCopyNode"));
        
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"), 
                ContentModel.ASPECT_MULTILINGUAL_DOCUMENT, 
                new JavaBehaviour(this, "beforeDeleteNode"));   
        
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeRemoveAspect"), 
                ContentModel.ASPECT_MULTILINGUAL_DOCUMENT,
                new JavaBehaviour(this, "beforeRemoveAspect"));
        
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateProperties"), 
                ContentModel.ASPECT_MULTILINGUAL_DOCUMENT,
                new JavaBehaviour(this, "onUpdateProperties"));
        
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onCopyComplete"), 
                ContentModel.ASPECT_MULTILINGUAL_DOCUMENT,
                new JavaBehaviour(this, "onCopyComplete"));
        
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveAspect"), 
                ContentModel.ASPECT_MULTILINGUAL_DOCUMENT,
                new JavaBehaviour(this, "onRemoveAspect"));
        
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
     * The copy of a <b>cm:mlDocument</b> can't keep the Multilingual aspect.
     * 
     * @see org.alfresco.repo.copy.CopyServicePolicies.OnCopyNodePolicy#onCopyNode(org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.StoreRef, boolean, org.alfresco.repo.policy.PolicyScope)
     */
    public void onCopyNode(QName classRef, NodeRef sourceNodeRef, StoreRef destinationStoreRef, boolean copyToNewNode, PolicyScope copyDetails) 
    {
        copyDetails.removeAspect(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);        
    }
    
    /**
     * The copy of <b>mlDocument</b> don't keep the 'locale' property. 
     * 
     * @see org.alfresco.repo.copy.CopyServicePolicies.OnCopyCompletePolicy#onCopyComplete(org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, boolean, java.util.Map)
     */
    public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef destinationRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) 
    {
        Map<QName, Serializable> copiedProp = nodeService.getProperties(destinationRef);
        copiedProp.remove(ContentModel.PROP_LOCALE);
        nodeService.setProperties(destinationRef, copiedProp);
    }
    
    /**
     * A <b>cm:mlDocument</b> is pivot translation it is a multilingual document (non empty) if it's language matches the language 
     * of its <b>cm:mlDocument</b>. And a pivot translation can't be removed if it's not the last translation of the <b>cm:mlContainer</b>.
     * 
     * If a translation is deleted, it's multilingual aspect is lost.
     * 
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void beforeDeleteNode(NodeRef nodeRef) 
    {    
        nodeService.removeAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
    }
    
    /**
     * When a pivot translation is deleted, it's <b>cm:mlContainer is deleted too.</b> 
     * 
     * <i>Note: When the pivot translation and the mlContainer are deleted, the mlDocument apsect is removed from
     * the other translations. It will be better to don't allow the deletion of the pivot if other translation is 
     * available at the client side level.</i>
     * 
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeRemoveAspectPolicy#beforeRemoveAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void beforeRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) 
    {
        if(aspectTypeQName.equals(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
        {
            NodeRef mlContainer = multilingualContentService.getTranslationContainer(nodeRef);
            
            //    nothing to do if the mlContainer is in a deletion process
            Boolean inDeleteProcess = (Boolean) nodeService.getProperty(mlContainer, MLContainerType.PROP_NAME_DELETION_RUNNING); 
            if(inDeleteProcess != null && inDeleteProcess == true)
            {
                return;
            }
                        
            Locale mlContainerLocale = (Locale) nodeService.getProperty(mlContainer, ContentModel.PROP_LOCALE);
            Locale nodeRefLocale     = (Locale) nodeService.getProperty(nodeRef,     ContentModel.PROP_LOCALE);
            
            nodeService.removeChild(mlContainer, nodeRef);
            
            // if last translation or if nodeRef is pivot translation
            if(multilingualContentService.getTranslations(mlContainer).size() == 0
                    || mlContainerLocale.equals(nodeRefLocale))
            {
                // delete the mlContainer
                nodeService.deleteNode(mlContainer);
            }            
        }        
    }
    
    /** 
     * After removing the <b>cm:mlDocument</b> aspect : 
     *         -    the node is removed is it's a <b>cm:mlEmptyTranslation</b>
     *         -   if not, only the locale property is removed 
     * 
     * @see org.alfresco.repo.node.NodeServicePolicies.OnRemoveAspectPolicy#onRemoveAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) 
    {
        if(aspectTypeQName.equals(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
        {
            
            if(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION))
            {
                // note: if the node has the temporary aspect and the mlEmptyTranslation in a same 
                // time, it means that the node is being deleted by another process...
                if(!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY))
                {
                    nodeService.deleteNode(nodeRef);    
                }    
            }
            else
            {
                Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
                props.remove(ContentModel.PROP_LOCALE);
                nodeService.setProperties(nodeRef, props);

            }
        }
    }
    
    /**
     * Ensure that the locale is unique inside the <b>mlContainer</b>.
     * 
     * If the locale of a pivot translation is modified, the pivot locale reference of the mlContainer
     * must be modified too.
     * 
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) 
    {
        Locale localeBefore = (Locale) before.get(ContentModel.PROP_LOCALE);
        Locale localeAfter;
        
        // the after local property type can be either Locale or String  
        Serializable objLocaleAfter = after.get(ContentModel.PROP_LOCALE);
        if (objLocaleAfter instanceof Locale ) 
        {
            localeAfter = (Locale) objLocaleAfter;
            
        }
        else
        {
            localeAfter = I18NUtil.parseLocale(objLocaleAfter.toString());
        }
        
        // if the local has been modified
        if(!localeBefore.equals(localeAfter))
        {
            NodeRef mlContainer = multilingualContentService.getTranslationContainer(nodeRef);
            
            // Since the map returned by the getTranslations doesn't duplicate keys, the size of this map will be 
            // different of the size of the number of children of the mlContainer if a duplicate locale is found.
            int transSize = multilingualContentService.getTranslations(mlContainer).size();
            int childSize = nodeService.getChildAssocs(mlContainer, ContentModel.ASSOC_MULTILINGUAL_CHILD, RegexQNamePattern.MATCH_ALL).size();
            
            // if duplicate locale found
            if(transSize != childSize)
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
            if(localeBefore.equals(localMlContainer))
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
