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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.jscript.ValueConverter;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JscriptWorkflowDefinition implements Serializable
{
	static final long serialVersionUID = 1641614201321129544L;	
	
	/** Service Registry */
	private ServiceRegistry serviceRegistry;
	
	/** Workflow definition id */
	private final String id;
	
	/** Workflow definition name */
	private final String name;
	
	/** Workflow definition version */
	private final String version;
	
	/** Workflow definition title */
	private final String title;
	
	/** Workflow definition description */
	private final String description;
	
	/** Root scripting scope for this object */
	private final Scriptable scope;

    /** Node Value Converter */
    private ValueConverter converter = null;

	/**
	 * Create a new instance of <code>WorkflowDefinition</code> from a
	 * CMR workflow object model WorkflowDefinition instance
	 * 
	 * @param cmrWorkflowDefinition an instance of WorkflowDefinition from the CMR workflow object model
	 * @param serviceRegistry reference to the Service Registry
	 * @param scope the root scripting scope for this object 
	 */
	public JscriptWorkflowDefinition(final WorkflowDefinition cmrWorkflowDefinition,
				final ServiceRegistry serviceRegistry, final Scriptable scope)
	{
		this.id = cmrWorkflowDefinition.id;
		this.name = cmrWorkflowDefinition.name;
		this.version = cmrWorkflowDefinition.version;
		this.title = cmrWorkflowDefinition.title;
		this.description = cmrWorkflowDefinition.description;
		this.serviceRegistry = serviceRegistry;
		this.scope = scope;
	}
	
	/**
	 * Creates a new instance of WorkflowDefinition from scratch
	 * 
	 * @param id workflow definition ID
	 * @param name name of workflow definition
	 * @param version version of workflow definition
	 * @param title title of workflow definition
	 * @param description description of workflow definition
	 * @param serviceRegistry reference to the Service Registry
	 * @param scope root scripting scope for this object
	 */
	public JscriptWorkflowDefinition(final String id, final String name, final String version,
			final String title, final String description, ServiceRegistry serviceRegistry,
			final Scriptable scope)
	{
		this.id = id;
		this.name = name;
		this.version = version;
		this.title = title;
		this.description = description;
		this.serviceRegistry = serviceRegistry;
		this.scope = scope;
	}
	
	/**
	 * Get value of <code>id</code> property
	 * 
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}
	
	/**
	 * Get value of <code>name</code> property
	 * 
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Get value of <code>version</code> property
	 * 
	 * @return the version
	 */
	public String getVersion()
	{
		return version;
	}
	
	/**
	 * Get value of <code>title</code> property
	 * 
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}
	
    /**
     * Gets the value converter
     * 
     * @return the value converter
     */
    protected ValueConverter getValueConverter()
    {
        if (converter == null)
        {
            converter = new ValueConverter();
        }
        return converter;
    }

    /**
	 * Get value of <code>description</code> property
	 * 
	 * @return the description
	 */
	public String getDescription()
	{
		return description;
	}
	
	/**
	 * Start workflow instance from workflow definition without
	 * attaching any package node to the workflow
	 * 
     * @param properties Associative array of properties used to populate the 
     *      start task properties
     * @return the initial workflow path
	 */
    @SuppressWarnings("unchecked")
    public JscriptWorkflowPath startWorkflow(Object properties)
    {
        return startWorkflow(null, properties);
    }
	
	/**
	 * Start workflow instance from workflow definition
	 * 
	 * @param workflowPackage workflow package node to 'attach' to the new workflow
	 * 		instance
	 * @param properties Associative array of properties used to populate the 
	 * 		start task properties
	 * @return the initial workflow path
	 */
	@SuppressWarnings("unchecked")
	public JscriptWorkflowPath startWorkflow(ScriptNode workflowPackage,
		Object properties)
	{
		WorkflowService workflowService = this.serviceRegistry.getWorkflowService();
		
		// if properties object is a scriptable object, then extract property name/value pairs
		// into property Map<QName, Serializable>, otherwise leave property map as null
		Map<QName, Serializable> workflowParameters = null;
        if (properties instanceof ScriptableObject)
        {
            ScriptableObject scriptableProps = (ScriptableObject)properties;
            workflowParameters = new HashMap<QName, Serializable>(scriptableProps.getIds().length);
            extractScriptablePropertiesToMap(scriptableProps, workflowParameters);
        }
		
		// attach given workflow package node if it is not null
        if (workflowPackage != null)
        {
            if (workflowParameters == null)
            {
                workflowParameters = new HashMap<QName, Serializable>(1);
            }
            workflowParameters.put(WorkflowModel.ASSOC_PACKAGE, getValueConverter().convertValueForRepo(workflowPackage));
        }        

        // provide a default context, if one is not specified
        Serializable context = workflowParameters.get(WorkflowModel.PROP_CONTEXT);
        if (context == null)
        {
            workflowParameters.put(WorkflowModel.PROP_CONTEXT, workflowPackage.getNodeRef());
        }

		WorkflowPath cmrWorkflowPath = workflowService.startWorkflow(this.id, workflowParameters);
		
		return new JscriptWorkflowPath(cmrWorkflowPath, this.serviceRegistry, this.scope);
	}
	
	/**
	 * Get active workflow instances of this workflow definition
	 * 
	 * @return the active workflow instances spawned from this workflow definition
	 */
	public synchronized Scriptable getActiveInstances()
	{
		WorkflowService workflowService = this.serviceRegistry.getWorkflowService();
		
		List<WorkflowInstance> cmrWorkflowInstances = workflowService.getActiveWorkflows(this.id);
		ArrayList<Serializable> activeInstances = new ArrayList<Serializable>();
		for (WorkflowInstance cmrWorkflowInstance : cmrWorkflowInstances)
		{
			activeInstances.add(new JscriptWorkflowInstance(cmrWorkflowInstance, this.serviceRegistry, this.scope));
		}
		
		Scriptable activeInstancesScriptable =
			(Scriptable)getValueConverter().convertValueForScript(this.serviceRegistry, this.scope, null, activeInstances);
		
		return activeInstancesScriptable;
	}
	
    /**
     * Helper to create a QName from either a fully qualified or short-name QName string
     * 
     * @param s Fully qualified or short-name QName string
     * 
     * @return QName
     */
    private QName createQName(String s)
    {
        QName qname;
        if (s.indexOf("" + QName.NAMESPACE_BEGIN) != -1)
        {
            qname = QName.createQName(s);
        }
        else
        {
            qname = QName.createQName(s, this.serviceRegistry.getNamespaceService());
        }
        return qname;
    }
    
    /**
     * Helper to extract a map of properties from a scriptable object (generally an associative array)
     * 
     * @param scriptable    The scriptable object to extract name/value pairs from.
     * @param map           The map to add the converted name/value pairs to.
     */
    private void extractScriptablePropertiesToMap(ScriptableObject scriptable, Map<QName, Serializable> map)
    {
        // get all the keys to the provided properties
        // and convert them to a Map of QName to Serializable objects
        Object[] propIds = scriptable.getIds();
        for (int i = 0; i < propIds.length; i++)
        {
            // work on each key in turn
            Object propId = propIds[i];
            
            // we are only interested in keys that are formed of Strings i.e. QName.toString()
            if (propId instanceof String)
            {
                // get the value out for the specified key - it must be Serializable
                String key = (String)propId;
                Object value = scriptable.get(key, scriptable);
                if (value instanceof Serializable)
                {
                    value = getValueConverter().convertValueForRepo((Serializable)value);
                    map.put(createQName(key), (Serializable)value);
                }
            }
        }
    }
}
