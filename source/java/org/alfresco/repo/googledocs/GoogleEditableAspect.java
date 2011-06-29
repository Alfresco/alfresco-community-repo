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
package org.alfresco.repo.googledocs;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.BeforeCheckIn;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckIn;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckOut;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Behaviour associated with google editable documents
 * 
 */
public class GoogleEditableAspect implements NodeServicePolicies.OnAddAspectPolicy,
                                             CheckOutCheckInServicePolicies.OnCheckOut,
                                             CheckOutCheckInServicePolicies.BeforeCheckIn,
                                             CheckOutCheckInServicePolicies.OnCheckIn,
                                             NodeServicePolicies.BeforeDeleteNodePolicy
{
    /** Policy component */
    private PolicyComponent policyComponent;
    
    /** Google docs service */
    private GoogleDocsService googleDocsService;
    
    /** Node service */
    private NodeService nodeService;
    
    /** Dictionary service */
    private DictionaryService dictionaryService;
    
    /** Content service */
    private ContentService contentService;
    
    /**
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * @param googleDocsService     google docs service
     */
    public void setGoogleDocsService(GoogleDocsService googleDocsService)
    {
        this.googleDocsService = googleDocsService;
    }
    
    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param dictionaryService     dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @param contentService    content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * Initialise method
     */
    public void init()
    {        
        // GoogleEditable resource behaviours
        policyComponent.bindClassBehaviour(OnAddAspectPolicy.QNAME, 
                                           GoogleDocsModel.ASPECT_GOOGLEEDITABLE , 
                                           new JavaBehaviour(this, "onAddAspect", NotificationFrequency.FIRST_EVENT));        
        policyComponent.bindClassBehaviour(OnCheckOut.QNAME, 
                                           GoogleDocsModel.ASPECT_GOOGLEEDITABLE, 
                                           new JavaBehaviour(this, "onCheckOut", NotificationFrequency.FIRST_EVENT));
       
        // Google resource behaviours
        policyComponent.bindClassBehaviour(BeforeCheckIn.QNAME, 
                                           GoogleDocsModel.ASPECT_GOOGLERESOURCE, 
                                           new JavaBehaviour(this, "beforeCheckIn", NotificationFrequency.FIRST_EVENT));
        policyComponent.bindClassBehaviour(OnCheckIn.QNAME, 
                                           GoogleDocsModel.ASPECT_GOOGLERESOURCE, 
                                           new JavaBehaviour(this, "onCheckIn", NotificationFrequency.FIRST_EVENT));
        policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME,
                                           GoogleDocsModel.ASPECT_GOOGLERESOURCE,
                                           new JavaBehaviour(this, "beforeDeleteNode", NotificationFrequency.FIRST_EVENT));
        
        // Copy behaviours
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                GoogleDocsModel.ASPECT_GOOGLEEDITABLE,
                new JavaBehaviour(this, "getGoogleEditableCopyCallback"));
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"),
                GoogleDocsModel.ASPECT_GOOGLERESOURCE,
                new JavaBehaviour(this, "getGoogleResourceCopyCallback"));    
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (googleDocsService.isEnabled() == true && nodeService.exists(nodeRef) == true)
        {
            // Can only make cm:content descendant google editable
            QName type = nodeService.getType(nodeRef);
            if (dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT) == false)
            {
                // Prevent aspect from being applied
                throw new AlfrescoRuntimeException("The node (" + nodeRef.toString() + ") can not be made google editable, because it is not a sub type of cm:content.");
            }
        }
    }

    /**
     * @see org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckOut#onCheckOut(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void onCheckOut(NodeRef workingCopy)
    {
        if (googleDocsService.isEnabled() == true && nodeService.exists(workingCopy) == true  && isUpload() == false)
        {   
            // Upload the content of the working copy to google docs
            googleDocsService.createGoogleDoc(workingCopy, GoogleDocsPermissionContext.SHARE_WRITE);
        }        
    }
    
    private boolean isUpload()
    {
        boolean result = false;
        String value = (String)AlfrescoTransactionSupport.getResource("checkoutforupload");
        if (value != null)
        {
            result = Boolean.parseBoolean(value);
        }
        return result;
    }

    public void beforeCheckIn(NodeRef workingCopyNodeRef,
            Map<String, Serializable> versionProperties, String contentUrl,
            boolean keepCheckedOut)
    {
        if (googleDocsService.isEnabled() == true && 
            nodeService.exists(workingCopyNodeRef) == true && 
            nodeService.hasAspect(workingCopyNodeRef, GoogleDocsModel.ASPECT_GOOGLERESOURCE) == true &&
            isUpload() == false)
        {
            // Get input stream for the google doc
            InputStream is = googleDocsService.getGoogleDocContent(workingCopyNodeRef);
            if (is == null)
            {
                throw new AlfrescoRuntimeException("Unable to complete check in, because the working copy content could not be retrieved from google docs.");
            }
            
            // Write the google content into the node
            ContentWriter writer = contentService.getWriter(workingCopyNodeRef, ContentModel.PROP_CONTENT, true);
            writer.putContent(is);
        }        
    }    

    /**
     * @see org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckIn#onCheckIn(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void onCheckIn(NodeRef nodeRef)
    {
        if (googleDocsService.isEnabled() == true && nodeService.exists(nodeRef) == true)
        {
            nodeService.removeAspect(nodeRef, GoogleDocsModel.ASPECT_GOOGLERESOURCE);
        }
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void beforeDeleteNode(NodeRef nodeRef)
    {
       if (googleDocsService.isEnabled() == true &&
           nodeService.exists(nodeRef) == true && 
           isUpload() == false)
       {
          // Delete the associated google resource
          googleDocsService.deleteGoogleResource(nodeRef);
       }         
    }
    
    public CopyBehaviourCallback getGoogleEditableCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return GoogleEditableCopyBehaviourCallback.INSTANCE;
    }

    private static class GoogleEditableCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        private static final CopyBehaviourCallback INSTANCE = new GoogleEditableCopyBehaviourCallback();
        
        /**
         * @return          Returns an empty map
         */
        @Override
        public Map<QName, Serializable> getCopyProperties(QName classQName, CopyDetails copyDetails, Map<QName, Serializable> properties)
        {
            return Collections.emptyMap();
        }
    }
    
    public CopyBehaviourCallback getGoogleResourceCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return GoogleResourceCopyBehaviourCallback.INSTANCE;
    }

    private static class GoogleResourceCopyBehaviourCallback extends DefaultCopyBehaviourCallback
    {
        private static final CopyBehaviourCallback INSTANCE = new GoogleEditableCopyBehaviourCallback();
        
        /**
         * @return          Returns an empty map
         */
        @Override
        public Map<QName, Serializable> getCopyProperties(QName classQName, CopyDetails copyDetails, Map<QName, Serializable> properties)
        {
            return Collections.emptyMap();
        }
    }
}
