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
package org.alfresco.repo.workflow.jscript;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ScriptableHashMap;
import org.alfresco.repo.jscript.ScriptableQNameMap;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.MutableAuthenticationService;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolverProvider;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * This class represents a workflow task (an instance of a workflow task definition)
 * 
 * @author glenj
 * @author Nick Smith
 */
public class JscriptWorkflowTask extends BaseScopableProcessorExtension implements Serializable
{
    static final long serialVersionUID = -8285971359421912313L;

    /** Service Registry object */
    private final ServiceRegistry serviceRegistry;
    private final NodeService nodeService;
    private final WorkflowService workflowService;
    private final DictionaryService dictionaryService;
    private MutableAuthenticationService authenticationService;
    private final DefaultNamespaceProvider namespaceProvider;
    
    private WorkflowTask task;

    
    /**
     * Creates a new instance of a workflow task from a WorkflowTask from the CMR workflow object model
     * 
     * @param task
     *            an instance of WorkflowTask from CMR workflow object model
     * @param serviceRegistry
     *            Service Registry object
     */
    public JscriptWorkflowTask(WorkflowTask task, 
                ServiceRegistry serviceRegistry,
                Scriptable scope)
    {
        this.serviceRegistry = serviceRegistry;
        this.namespaceProvider = new DefaultNamespaceProvider(serviceRegistry.getNamespaceService());
        this.workflowService = serviceRegistry.getWorkflowService();
        this.nodeService = serviceRegistry.getNodeService();
        this.dictionaryService = serviceRegistry.getDictionaryService();
        this.authenticationService = serviceRegistry.getAuthenticationService();
        this.task = task;
        this.setScope(scope);
    }

    /**
     * Gets the value of the <code>id</code> property
     * 
     * @return the id
     */
    public String getId()
    {
        return task.getId();
    }

    /**
     * Gets the value of the <code>name</code> property
     * 
     * @return the name
     */
    public String getName()
    {
        return task.getName();
    }

    /**
     * Gets the value of the <code>title</code> property
     * 
     * @return the title
     */
    public String getTitle()
    {
        return task.getTitle();
    }

    /**
     * Gets the value of the <code>description</code> property
     * 
     * @return the description
     */
    public String getDescription()
    {
        return task.getDescription();
    }

    /**
     * Gets the value of the <code>properties</code> property
     * 
     * @return the properties
     */
    public Scriptable getProperties()
    {
        // instantiate ScriptableQNameMap<String, Serializable> properties
        // from WorkflowTasks's Map<QName, Serializable> properties
        ScriptableQNameMap<String, Serializable> properties = new ScriptableQNameMap<String, Serializable>(namespaceProvider);
        properties.putAll(task.getProperties());
        return properties;
    }

    /**
     * Sets the properties on the underlying {@link WorkflowTask}.
     * 
     * @param properties
     *            the properties to set
     */
    public void setProperties(ScriptableQNameMap<String, Serializable> properties)
    {
        
        Map<QName, Serializable> qNameProps = properties.getMapOfQNames();
        this.task = workflowService.updateTask(task.getId(), qNameProps, null, null);
    }
    
    /**
     * Returns whether the task is complete 'true':complete, 'false':in-progress
     * 
     * @return the complete
     */
    public boolean isComplete()
    {
        return task.getState().equals(WorkflowTaskState.COMPLETED);
    }

    /**
     * Returns whether this task is pooled or not
     * 
     * @return 'true': task is pooled, 'false': task is not pooled
     */
    public boolean isPooled()
    {
        String authority = authenticationService.getCurrentUserName();
        return workflowService.isTaskClaimable(task, authority);
    }

    /**
     * @deprecated pooled state cannot be altered. 
     * 
     */
    @Deprecated
    public void setPooled(boolean pooled)
    {
    	
    }

    /**
     * End the task
     * 
     * @param transition
     *            transition to end the task for
     */
    public void endTask(String transitionId)
    {
        workflowService.endTask(task.getId(), transitionId);
    }

    /**
     * Get the available transition ids.
     * 
     * @return
     */
    public ScriptableHashMap<String, String> getTransitions()
    {
        ScriptableHashMap<String, String> transitions = new ScriptableHashMap<String, String>();
        WorkflowNode workflowNode = task.getPath().getNode();
        if (workflowNode != null)
        {
            for (WorkflowTransition transition : workflowNode.getTransitions())
            {
                transitions.put(transition.getId(), transition.getTitle());
            }
        }
        return transitions;
    }

    /**
     * Get the packe resources (array of noderefs)
     * 
     * @return
     */
    public Scriptable getPackageResources()
    {
        List<NodeRef> contents = workflowService.getPackageContents(task.getId());
        List<ScriptNode> resources = new ArrayList<ScriptNode>(contents.size());
        
        Collection<QName> allowedTypes = getAllowedPackageResourceTypes();
        for (NodeRef node : contents)
        {
            if (isValidResource(node, allowedTypes))
            {
                ScriptNode scriptNode = new ScriptNode(node, serviceRegistry, getScope());
                resources.add(scriptNode);
            }
        }
        return Context.getCurrentContext().newArray(getScope(), resources.toArray());
    }

    private Collection<QName> getAllowedPackageResourceTypes()
    {
        // look for content nodes or links to content
        // NOTE: folders within workflow packages are ignored for now
        Collection<QName> allowedTypes = dictionaryService.getSubTypes(ContentModel.TYPE_CONTENT, true);
        allowedTypes.addAll(dictionaryService.getSubTypes(ApplicationModel.TYPE_FILELINK, true));
        return allowedTypes;
    }

    private boolean isValidResource(NodeRef node, Collection<QName> allowedTypes)
    {
        if (nodeService.exists(node))
        {
            //Check if the node is one of the allowedTypes.
            return allowedTypes.contains(nodeService.getType(node));
        }
        return false;
    }
    
    private static class DefaultNamespaceProvider implements NamespacePrefixResolverProvider
    {
        private static final long serialVersionUID = -7015209142379905617L;
        private final NamespaceService namespaceService;
        
        public DefaultNamespaceProvider(NamespaceService namespaceService)
        {
            this.namespaceService = namespaceService;
        }

        /**
         * {@inheritDoc}
         */
        public NamespacePrefixResolver getNamespacePrefixResolver()
        {
            return namespaceService;
        }
        
    }
}