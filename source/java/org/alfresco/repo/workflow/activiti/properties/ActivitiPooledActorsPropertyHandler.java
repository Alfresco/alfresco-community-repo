/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.workflow.activiti.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.IdentityLinkType;
import org.activiti.engine.task.Task;
import org.alfresco.repo.workflow.WorkflowAuthorityManager;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.activiti.ActivitiTaskPropertyHandler;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public class ActivitiPooledActorsPropertyHandler extends ActivitiTaskPropertyHandler
{
    private TaskService taskService;
    private WorkflowAuthorityManager authorityManager;
    
    /**
    * {@inheritDoc}
    */
    @Override
    protected Object handleTaskProperty(Task task, TypeDefinition type, QName key, Serializable value)
    {
        List<IdentityLink> links = taskService.getIdentityLinksForTask(task.getId());
        UserAndGroupUpdates updates = getUserAndGroupUpdates(value, links);
        updateTaskCandidates(task.getId(), updates);
        return DO_NOT_ADD;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    protected Object handleDelegateTaskProperty(DelegateTask task, TypeDefinition type, QName key, Serializable value)
    {
        List<IdentityLinkEntity> links = ((TaskEntity)task).getIdentityLinks();
        UserAndGroupUpdates updates = getUserAndGroupUpdates(value, links);
        updateTaskCandidates(task, updates);
        return DO_NOT_ADD;
    }

    @SuppressWarnings("unchecked")
    private Collection<NodeRef> getNodes(Serializable value)
    {
        if (value instanceof Collection<?>)
        {
            return (Collection<NodeRef>) value;
        }
        if (value instanceof NodeRef)
        {
            return Collections.singletonList((NodeRef)value);
        }
        throw getInvalidPropertyValueException(WorkflowModel.ASSOC_POOLED_ACTORS, value);
    }

    private void updateTaskCandidates(String taskId, UserAndGroupUpdates updates)
    {
        // Only new candidates are present in pooledUsers and pooledGroups, create Links for these
        for (String user : updates.getUsers())
        {
            taskService.addCandidateUser(taskId, user);
        }
        for (String group : updates.getGroups())
        {
            taskService.addCandidateGroup(taskId, group);
        }
        

        // Remove all candidates which have been removed
        for (IdentityLink link : updates.getLinksToRemove())
        {
            if (link.getUserId() != null)
            {
                taskService.deleteUserIdentityLink(link.getTaskId(),
                            link.getUserId(), link.getType());
            }
            else
            {
                taskService.deleteGroupIdentityLink(link.getTaskId(), 
                            link.getGroupId(), link.getType());
            }
        }
    }
    
    private void updateTaskCandidates(DelegateTask task, UserAndGroupUpdates updates)
    {
        // Only new candidates are present in pooledUsers and pooledGroups, create Links for these
        for (String user : updates.getUsers())
        {
            task.addCandidateUser( user);
        }
        for (String group : updates.getGroups())
        {
            task.addCandidateGroup( group);
        }
        
        // Remove all candidates which have been removed
        for (IdentityLink link : updates.linksToRemove)
        {
            if (link.getUserId() != null)
            {
                task.deleteUserIdentityLink(link.getUserId(), link.getType());
            }
            else
            {
                task.deleteGroupIdentityLink(link.getGroupId(), link.getType());
            }
        }
    }

    /**
     * Returns a DTO containing the users and groups to add and the links to remove.
     * 
     * @param actors
     * @param links
     * @return
     */
    private UserAndGroupUpdates getUserAndGroupUpdates(Serializable value, Collection<? extends IdentityLink> links)
    {
        Collection<NodeRef> actors = getNodes(value);

        List<String> users = new ArrayList<String>();
        List<String> groups = new ArrayList<String>();
        for (NodeRef actor : actors)
        {
            String authorityName = authorityManager.getAuthorityName(actor);
            List<String> pooledAuthorities = authorityManager.isUser(authorityName)? users : groups;
            pooledAuthorities.add(authorityName);
        }
        
        // Removes all users and groups that are already links.
        // Also records links that are not part of the new set of users and
        // groups, in order to delete them.
        List<IdentityLink> linksToRemove = new LinkedList<IdentityLink>();
        for (IdentityLink link : links)
        {
            if (IdentityLinkType.CANDIDATE.equals(link.getType())) 
            {
                String userId = link.getUserId();
                if (userId != null)
                {
                    if (users.remove(userId)==false)
                    {
                        linksToRemove.add(link);
                    }
                }
                else 
                {
                    String groupId = link.getGroupId();
                    if (groupId != null && groups.remove(groupId) == false)
                    {
                        linksToRemove.add(link);
                    }
                }
            }
        }
        return new UserAndGroupUpdates(users, groups, linksToRemove);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    protected QName getKey()
    {
        return WorkflowModel.ASSOC_POOLED_ACTORS;
    }
    
    /**
     * @param taskService the taskService to set
     */
    public void setTaskService(TaskService taskService)
    {
        this.taskService = taskService;
    }
    
    /**
     * @param authorityManager the authorityManager to set
     */
    public void setWorkflowAuthorityManager(WorkflowAuthorityManager authorityManager)
    {
        this.authorityManager = authorityManager;
    }
    
    private static class UserAndGroupUpdates
    {
        private final List<String> users;
        private final List<String> groups;

        private final List<IdentityLink> linksToRemove;

        public UserAndGroupUpdates(List<String> users, List<String> groups, List<IdentityLink> linksToRemove)
        {
            this.users = users;
            this.groups = groups;
            this.linksToRemove = linksToRemove;
        }

        /**
         * @return the users
         */
        public List<String> getUsers()
        {
            return users;
        }

        /**
         * @return the groups
         */
        public List<String> getGroups()
        {
            return groups;
        }

        /**
         * @return the linksToRemove
         */
        public List<IdentityLink> getLinksToRemove()
        {
            return linksToRemove;
        }
    }
}
