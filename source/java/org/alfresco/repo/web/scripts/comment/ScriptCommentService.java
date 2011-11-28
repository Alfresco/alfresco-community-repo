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
package org.alfresco.repo.web.scripts.comment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.Scopeable;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Temporary comment service API to start encapsulation of comment logic.
 * 
 * NOTE:  this has been added to resolve a specific issue and needs re-consideration 
 * 
 * @author Roy Wetherall
 */
public class ScriptCommentService extends BaseScopableProcessorExtension
{
    private static final String COMMENTS_TOPIC_NAME = "Comments";
    
    private ServiceRegistry serviceRegistry;
    private NodeService nodeService;
    private BehaviourFilter behaviourFilter;
    
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
    }
    
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }
    
    public ScriptNode createCommentsFolder(ScriptNode node)
    {
        final NodeRef nodeRef = node.getNodeRef();
        
        NodeRef commentsFolder = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                NodeRef commentsFolder = null;
                
                // ALF-5240: turn off auditing round the discussion node creation to prevent
                // the source document from being modified by the first user leaving a comment
                behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                
                try
                {
                    nodeService.addAspect(nodeRef, QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "discussable"), null);
                    nodeService.addAspect(nodeRef, QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "commentsRollup"), null);
                    List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "discussion"), RegexQNamePattern.MATCH_ALL);
                    if (assocs.size() != 0)
                    {
                        NodeRef forumFolder = assocs.get(0).getChildRef();
                        
                        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1, 1.0f);
                        props.put(ContentModel.PROP_NAME, COMMENTS_TOPIC_NAME);
                        commentsFolder = nodeService.createNode(
                                forumFolder,
                                ContentModel.ASSOC_CONTAINS, 
                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, COMMENTS_TOPIC_NAME), 
                                QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "topic"),
                                props).getChildRef();
                    }
                }
                finally
                {
                    behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                }
                
                return commentsFolder;
            }
    
        }, AuthenticationUtil.getSystemUserName()); 
        
        return new ScriptNode(commentsFolder, serviceRegistry, getScope());
    }
}
