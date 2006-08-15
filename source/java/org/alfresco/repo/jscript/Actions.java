/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.jscript;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Scriptable;


/**
 * Scripted Action service for describing and executing actions against Nodes.
 *  
 * @author davidc
 */
public final class Actions implements Scopeable
{
    /** Repository Service Registry */
    private ServiceRegistry services;
    
    /** Root scope for this object */
    private Scriptable scope;


    /**
     * Constructor
     * 
     * @param services   repository service registry
     */
    public Actions(ServiceRegistry services)
    {
        this.services = services;
    }
    
    /**
     * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
     */
    public void setScope(Scriptable scope)
    {
        this.scope = scope;
    }
    
    /**
     * Gets the list of registered action names
     * 
     * @return  the registered action names
     */
    public String[] getRegistered()
    {
        ActionService actionService = services.getActionService();
        List<ActionDefinition> defs = actionService.getActionDefinitions();
        String[] registered = new String[defs.size()];
        int i = 0;
        for (ActionDefinition def : defs)
        {
            registered[i++] = def.getName();
        }
        return registered;
    }
    
    public String[] jsGet_registered()
    {
        return getRegistered();
    }
    
    /**
     * Create an Action
     * 
     * @param actionName  the action name
     * @return  the action
     */
    public ScriptAction create(String actionName)
    {
        ScriptAction scriptAction = null;
        ActionService actionService = services.getActionService();
        ActionDefinition actionDef = actionService.getActionDefinition(actionName);
        if (actionDef != null)
        {
            Action action = actionService.createAction(actionName);
            scriptAction = new ScriptAction(action, actionDef);
            scriptAction.setScope(scope);
        }
        return scriptAction;
    }
    
    
    /**
     * Scriptable Action
     * 
     * @author davidc
     */
    public final class ScriptAction implements Serializable, Scopeable
    {
        private static final long serialVersionUID = 5794161358406531996L;
        
        /** Root scope for this object */
        private Scriptable scope;        

        /** Converter with knowledge of action parameter values */
        private ActionValueConverter converter;
        
        /** Action state */
        private Action action;
        private ActionDefinition actionDef;
        private ScriptableParameterMap<String, Serializable> parameters = null;
        
        
        /**
         * Construct
         * 
         * @param action  Alfresco action
         */
        public ScriptAction(Action action, ActionDefinition actionDef)
        {
            this.action = action;
            this.actionDef = actionDef;
            this.converter = new ActionValueConverter();
        }
        
        /**
         * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
         */
        public void setScope(Scriptable scope)
        {
            this.scope = scope;
        }
        
        /**
         * Returns the action name
         * 
         * @return  action name
         */
        public String getName()
        {
            return this.actionDef.getName();
        }
        
        public String jsGet_name()
        {
            return getName();
        }
        
        /**
         * Return all the properties known about this node.
         * 
         * The Map returned implements the Scriptable interface to allow access to the properties via
         * JavaScript associative array access. This means properties of a node can be access thus:
         * <code>node.properties["name"]</code>
         * 
         * @return Map of properties for this Node.
         */
        @SuppressWarnings("synthetic-access")
        public Map<String, Serializable> getParameters()
        {
            if (this.parameters == null)
            {
                // this Map implements the Scriptable interface for native JS syntax property access
                this.parameters = new ScriptableParameterMap<String, Serializable>();
                Map<String, Serializable> actionParams = this.action.getParameterValues();
                for (Map.Entry<String, Serializable> entry : actionParams.entrySet())
                {
                    String name = entry.getKey();
                    this.parameters.put(name, converter.convertActionParamForScript(name, entry.getValue()));
                }
                this.parameters.setModified(false);
            }
            return this.parameters;
        }        
        
        public Map<String, Serializable> jsGet_parameters()
        {
            return getParameters();
        }
        
        /**
         * Execute action
         * 
         * @param node  the node to execute action upon
         */
        @SuppressWarnings("synthetic-access")
        public void execute(Node node)
        {
            if (this.parameters.isModified())
            {
                Map<String, Serializable> actionParams = action.getParameterValues();
                actionParams.clear();
                
                for (Map.Entry<String, Serializable> entry : this.parameters.entrySet())
                {
                    // perform the conversion from script wrapper object to repo serializable values
                    String name = entry.getKey();
                    Serializable value = converter.convertActionParamForRepo(name, entry.getValue());
                    actionParams.put(name, value);
                }
            }
            services.getActionService().executeAction(action, node.getNodeRef());
        }
        
        /**
         * Value converter with specific knowledge of action parameters
         * 
         * @author davidc
         */
        private class ActionValueConverter extends ValueConverter
        {
            /**
             * Convert Action Parameter for Script usage
             *  
             * @param paramName  parameter name
             * @param value  value to convert
             * @return  converted value
             */
            @SuppressWarnings("synthetic-access")
            public Serializable convertActionParamForScript(String paramName, Serializable value)
            {
                ParameterDefinition paramDef = actionDef.getParameterDefintion(paramName);
                if (paramDef != null && paramDef.getType().equals(DataTypeDefinition.QNAME))
                {
                    return ((QName)value).toPrefixString(services.getNamespaceService());
                }
                else
                {
                    return convertValueForScript(services, scope, null, value);
                }
            }

            /**
             * Convert Action Parameter for Java usage
             * 
             * @param paramName  parameter name
             * @param value  value to convert
             * @return  converted value
             */
            @SuppressWarnings("synthetic-access")
            public Serializable convertActionParamForRepo(String paramName, Serializable value)
            {
                ParameterDefinition paramDef = actionDef.getParameterDefintion(paramName);
                if (paramDef != null && paramDef.getType().equals(DataTypeDefinition.QNAME))
                {
                    return QName.createQName((String)value, services.getNamespaceService());
                }
                else
                {
                    return convertValueForRepo(value);
                }
            }
        }
    }

    
    /**
     * Scripted Parameter map with modified flag.
     *
     * @author davidc
     */
    public static final class ScriptableParameterMap<K,V> extends ScriptableHashMap<K,V> 
    {
        private static final long serialVersionUID = 574661815973241554L;
        private boolean modified = false;


        /**
         * Is this a modified parameter map?
         * 
         * @return  true => modified
         */
        /*package*/ boolean isModified()
        {
            return modified;
        }
        
        /**
         * Set explicitly whether this map is modified
         * 
         * @param modified   true => modified, false => not modified
         */
        /*package*/ void setModified(boolean modified)
        {
            this.modified = modified;
        }
        
        /* (non-Javadoc)
         * @see org.mozilla.javascript.Scriptable#getClassName()
         */
        @Override
        public String getClassName()
        {
            return "ScriptableParameterMap";
        }

        /* (non-Javadoc)
         * @see org.mozilla.javascript.Scriptable#delete(java.lang.String)
         */
        @Override
        public void delete(String name)
        {
            super.delete(name);
            setModified(true);
        }

        /* (non-Javadoc)
         * @see org.mozilla.javascript.Scriptable#put(java.lang.String, org.mozilla.javascript.Scriptable, java.lang.Object)
         */
        @Override
        public void put(String name, Scriptable start, Object value)
        {
            super.put(name, start, value);
            setModified(true);
        }
    }
 
}
