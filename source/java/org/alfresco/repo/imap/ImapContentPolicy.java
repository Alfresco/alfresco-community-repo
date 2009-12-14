/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.imap;

import org.alfresco.model.ImapModel;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.PropertyCheck;


public class ImapContentPolicy
{
    private ActionService actionService;
    private PolicyComponent policyComponent;
    
    /**
     * Init method.  Registered behaviours.
     */
    public void init()
    {  
        PropertyCheck.mandatory(this, "actionService", getActionService());
        PropertyCheck.mandatory(this, "policyComponent", getPolicyComponent());
        
        /**
         * Bind policies
         */
        this.getPolicyComponent().bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onAddAspect"), 
            ImapModel.ASPECT_IMAP_CONTENT, 
            new JavaBehaviour(this, "onAddAspect", NotificationFrequency.TRANSACTION_COMMIT));
    }
    
    /**
     * Called when the imap:imapContent aspect is applied
     * 
     * @param nodeRef The node the aspect is being applied to
     * @param aspectTypeQName The type of aspect being applied (should be imap:imapContent)
     */
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        // ensure the aspect is the one we expect
        if (aspectTypeQName.equals(ImapModel.ASPECT_IMAP_CONTENT))
        {
            Action action = getActionService().createAction("extract-metadata");
            if(action != null)
            {
                getActionService().executeAction(action, nodeRef);
            }
        }
    }

    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    public ActionService getActionService()
    {
        return actionService;
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public PolicyComponent getPolicyComponent()
    {
        return policyComponent;
    }
}
