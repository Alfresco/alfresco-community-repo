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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.BeforeCancelCheckOut;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckIn;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckOut;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Behaviour associated with google editable documents
 * 
 */
public class GoogleEditableAspect implements NodeServicePolicies.OnAddAspectPolicy,
                                             CheckOutCheckInServicePolicies.OnCheckOut,
                                             CheckOutCheckInServicePolicies.OnCheckIn,
                                             CheckOutCheckInServicePolicies.BeforeCancelCheckOut
{
    /** Indicates whether behaviour is enabled or not */
    boolean enabled = false;
    
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
     * @param enabled   true if behaviour enabled, false otherwise
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    
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
        if (enabled == true)
        {
            // Register behaviour with policy component
            policyComponent.bindClassBehaviour(OnAddAspectPolicy.QNAME, 
                                               GoogleDocsModel.ASPECT_GOOGLEEDITABLE , 
                                               new JavaBehaviour(this, "onAddAspect", NotificationFrequency.FIRST_EVENT));        
            policyComponent.bindClassBehaviour(OnCheckOut.QNAME, 
                                               GoogleDocsModel.ASPECT_GOOGLEEDITABLE, 
                                               new JavaBehaviour(this, "onCheckOut", NotificationFrequency.FIRST_EVENT));
            policyComponent.bindClassBehaviour(OnCheckIn.QNAME, 
                                               GoogleDocsModel.ASPECT_GOOGLEEDITABLE, 
                                               new JavaBehaviour(this, "onCheckIn", NotificationFrequency.FIRST_EVENT));
            policyComponent.bindClassBehaviour(BeforeCancelCheckOut.QNAME,
                                               GoogleDocsModel.ASPECT_GOOGLEEDITABLE,
                                               new JavaBehaviour(this, "beforeCancelCheckOut", NotificationFrequency.FIRST_EVENT));
        }
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        if (nodeService.exists(nodeRef) == true)
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
        if (nodeService.exists(workingCopy) == true)
        {
            // Remove the google editable aspect from the working copy
            nodeService.removeAspect(workingCopy, GoogleDocsModel.ASPECT_GOOGLEEDITABLE);
            
            // Upload the content of the working copy to google docs
            googleDocsService.createGoogleDoc(workingCopy, GoogleDocsPermissionContext.SHARE_WRITE);
        }        
    }

    /**
     * @see org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckIn#onCheckIn(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void onCheckIn(NodeRef nodeRef)
    {
        if (nodeService.exists(nodeRef) == true && 
            nodeService.hasAspect(nodeRef, GoogleDocsModel.ASPECT_GOOGLERESOURCE) == true)
        {
            // Get input stream for the google doc
            InputStream is = googleDocsService.getGoogleDocContent(nodeRef);
            if (is == null)
            {
                throw new AlfrescoRuntimeException("Unable to complete check in, because the working copy content could not be retrieved from google docs.");
            }
            
            // Write the google content into the node
            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
            writer.putContent(is);
            
            // Delete the associated google resource
            googleDocsService.deleteGoogleResource(nodeRef);
        }
    }

    /**
     * @see org.alfresco.repo.coci.CheckOutCheckInServicePolicies.BeforeCancelCheckOut#beforeCancelCheckOut(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void beforeCancelCheckOut(NodeRef workingCopyNodeRef)
    {
        if (nodeService.exists(workingCopyNodeRef) == true)
        {
            // Delete the associated google resource
            googleDocsService.deleteGoogleResource(workingCopyNodeRef);
        }        
    }
}
