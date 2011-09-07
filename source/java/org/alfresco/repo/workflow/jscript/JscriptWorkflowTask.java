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
import java.util.List;
import java.util.Set;

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
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTransition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.NamespacePrefixResolverProvider;
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

    /** Unique ID for workflow task */
    private final String id;

    /** Name for workflow task */
    private final String name;

    /** Title for workflow task */
    private final String title;

    /** Description of workflow task */
    private final String description;

    /** Properties (key/value pairs) for this Workflow Task */
    private ScriptableQNameMap<String, Serializable> properties;

    /** Whether task is complete or not - 'true':complete, 'false':in-progress */
    private boolean complete = false;

    /** Whether task is pooled or not */
    private boolean pooled = false;

    /** Service Registry object */
    private ServiceRegistry serviceRegistry;

    /** Available transitions * */
    private ScriptableHashMap<String, String> transitions;

    /** Package resources * */
    private Scriptable packageResources;

    /**
     * Creates a new instance of a workflow task (instance of a workflow task definition)
     * 
     * @param id
     *            workflow task ID
     * @param name
     *            workflow task name
     * @param title
     *            workflow task title
     * @param description
     *            workflow task description
     * @param serviceRegistry
     *            Service Registry object
     * @param properties
     * @param transitions
     * @param packageResources
     */
    public JscriptWorkflowTask(final String id, final String name, final String title, final String description, final ServiceRegistry serviceRegistry,
            final ScriptableQNameMap<String, Serializable> properties, final ScriptableHashMap<String, String> transitions, Scriptable packageResources,
            Scriptable scope)
    {
        this.id = id;
        this.name = name;
        this.title = title;
        this.description = description;
        this.serviceRegistry = serviceRegistry;
        this.properties = properties;
        this.transitions = transitions;
        this.packageResources = packageResources;
        this.setScope(scope);
    }

    /**
     * Creates a new instance of a workflow task from a WorkflowTask from the CMR workflow object model
     * 
     * @param cmrWorkflowTask
     *            an instance of WorkflowTask from CMR workflow object model
     * @param serviceRegistry
     *            Service Registry object
     */
    public JscriptWorkflowTask(final WorkflowTask cmrWorkflowTask, final ServiceRegistry serviceRegistry, Scriptable scope)
    {
        this.id = cmrWorkflowTask.getId();
        this.name = cmrWorkflowTask.getName();
        this.title = cmrWorkflowTask.getTitle();
        this.description = cmrWorkflowTask.getDescription();
        this.serviceRegistry = serviceRegistry;
        this.setScope(scope);

        // instantiate ScriptableQNameMap<String, Serializable> properties
        // from WorkflowTasks's Map<QName, Serializable> properties
        this.properties = new ScriptableQNameMap<String, Serializable>(new NamespacePrefixResolverProvider()
        {
            private static final long serialVersionUID = 4218645978524914678L;

            public NamespacePrefixResolver getNamespacePrefixResolver()
            {
                return serviceRegistry.getNamespaceService();
            }
        });

        Set<QName> keys = cmrWorkflowTask.getProperties().keySet();
        for (QName key : keys)
        {
            Serializable value = cmrWorkflowTask.getProperties().get(key);
            this.properties.put(key.toString(), value);
        }

        transitions = new ScriptableHashMap<String, String>();
        for (WorkflowTransition transition : cmrWorkflowTask.getPath().getNode().getTransitions())
        {
            transitions.put(transition.getId(), transition.getTitle());
        }

        // build package context .... should be centralised... YUK
        // Needs to match org.alfresco.repo.template.Workflow.WorkflowTaskItem.getPackageResources

        List<NodeRef> contents = serviceRegistry.getWorkflowService().getPackageContents(cmrWorkflowTask.getId());
        List<NodeRef> resources = new ArrayList<NodeRef>(contents.size());

        NodeService nodeService = serviceRegistry.getNodeService();
        DictionaryService ddService = serviceRegistry.getDictionaryService();

        for (NodeRef nodeRef : contents)
        {
            if (nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_AVM))
            {
                resources.add(nodeRef);
            }
            else
            {
                if (nodeService.exists(nodeRef))
                {
                    // find it's type so we can see if it's a node we are interested in
                    QName type = nodeService.getType(nodeRef);

                    // make sure the type is defined in the data dictionary
                    if (ddService.getType(type) != null)
                    {
                        // look for content nodes or links to content
                        // NOTE: folders within workflow packages are ignored for now
                        if (ddService.isSubClass(type, ContentModel.TYPE_CONTENT) || ApplicationModel.TYPE_FILELINK.equals(type))
                        {
                            resources.add(nodeRef);
                        }
                    }
                }
            }
        }

        Object[] answer = new Object[resources.size()];
        for (int i = 0; i < resources.size(); i++)
        {
            // create our Node representation from the NodeRef
            answer[i] = new ScriptNode(resources.get(i), serviceRegistry, getScope());
        }
        packageResources = Context.getCurrentContext().newArray(getScope(), answer);

    }

    /**
     * Gets the value of the <code>id</code> property
     * 
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * Gets the value of the <code>name</code> property
     * 
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the value of the <code>title</code> property
     * 
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Gets the value of the <code>description</code> property
     * 
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Gets the value of the <code>properties</code> property
     * 
     * @return the properties
     */
    public Scriptable getProperties()
    {
        return properties;
    }

    /**
     * Sets the value of the <code>properties</code> property
     * 
     * @param properties
     *            the properties to set
     */
    public void setProperties(ScriptableQNameMap<String, Serializable> properties)
    {
        this.properties = properties;
    }

    /**
     * Returns whether the task is complete 'true':complete, 'false':in-progress
     * 
     * @return the complete
     */
    public boolean isComplete()
    {
        return complete;
    }

    /**
     * Returns whether this task is pooled or not
     * 
     * @return 'true': task is pooled, 'false': task is not pooled
     */
    public boolean isPooled()
    {
        return pooled;
    }

    /**
     * Sets whether task is pooled('true') or not('false')
     * 
     * @param pooled
     *            the pooled to set
     */
    public void setPooled(boolean pooled)
    {
        this.pooled = pooled;
    }

    /**
     * End the task
     * 
     * @param transition
     *            transition to end the task for
     */
    public void endTask(String transitionId)
    {
        serviceRegistry.getWorkflowService().endTask(this.id, transitionId);
    }

    /**
     * Get the available transition ids.
     * 
     * @return
     */
    public ScriptableHashMap<String, String> getTransitions()
    {
        return transitions;
    }

    /**
     * Get the packe resources (array of noderefs)
     * 
     * @return
     */
    public Scriptable getPackageResources()
    {
        return packageResources;
    }

}