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
package org.alfresco.repo.model.filefolder;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;


/**
 * Class containing behaviour for the folder type.
 *
 * {@link ContentModel#TYPE_FOLDER folder type}
 *
 * @author yanipig
 */
public class FolderType implements
        NodeServicePolicies.BeforeDeleteNodePolicy
{

    //     Dependencies
    private PolicyComponent policyComponent;
    private NodeService nodeService;


    /**
     * Initialise the Folder type
     *
     * Ensures that the {@link ContentModel#TYPE_FOLDER} folder type
     */
    public void init()
    {
        this.policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                ContentModel.TYPE_FOLDER,
                new JavaBehaviour(this, "beforeDeleteNode"));

    }

    /**
     * @param policyComponent the policy component to register behaviour with
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param nodeService the Node Service to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }


    /**
     * Since the multilingual document don't keep their <b>mlDocument</b> aspect, the <b>mlEmptyTranslation<b>
     * can't be archived and the deletion of a container doesn't call the right policies, the deletion of a
     * folder must explicitly remove this aspect.
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        List<ChildAssociationRef> childAssociations = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);

        for(ChildAssociationRef childAssoc : childAssociations)
        {
            NodeRef child = childAssoc.getChildRef();

            if(nodeService.exists(child))
            {
                // Remove the mlDocument aspect of each multilingual node contained in the folder.
                // The policies of this aspect perform the right logic.
                if(nodeService.hasAspect(child, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT))
                {
                    nodeService.removeAspect(child, ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
                }
                // Recurse the process if needed
                else if(nodeService.getType(child).equals(ContentModel.TYPE_FOLDER))
                {
                    beforeDeleteNode(child);
                }
            }
        }
    }
}
