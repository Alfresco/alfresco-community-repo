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
package org.alfresco.repo.jscript;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;

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

    private ServiceRegistry services;
    
    /** Action state */
    private Action action;

    private ActionDefinition actionDef;

    private ScriptableParameterMap<String, Serializable> parameters = null;

    /**
     * Construct
     * 
     * @param action
     *            Alfresco action
     */
    public ScriptAction(ServiceRegistry services, Action action, ActionDefinition actionDef)
    {
        this.services = services;
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
     * @return action name
     */
    public String getName()
    {
        return this.actionDef.getName();
    }

    /**
     * Return all the properties known about this node. The Map returned implements the Scriptable interface to allow access to the properties via JavaScript associative array
     * access. This means properties of a node can be access thus: <code>node.properties["name"]</code>
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

    /**
     * Execute action
     * 
     * @param node
     *            the node to execute action upon
     */
    @SuppressWarnings("synthetic-access")
    public void execute(ScriptNode node)
    {
        if (this.parameters != null && this.parameters.isModified())
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
        
        // Parameters may have been updated by action execution, so reset cache
        this.parameters = null;
        
        // Reset the actioned upon node
        node.reset();
    }
    
    /**
     * Execute action
     * 
     * @param nodeRef
     *            the node to execute action upon
     */
    @SuppressWarnings("synthetic-access")
    public void execute(NodeRef nodeRef)
    {
        if (this.parameters != null && this.parameters.isModified())
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
        services.getActionService().executeAction(action, nodeRef);

        // Parameters may have been updated by action execution, so reset cache
        this.parameters = null;
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
         * @param paramName
         *            parameter name
         * @param value
         *            value to convert
         * @return converted value
         */
        @SuppressWarnings("synthetic-access")
        public Serializable convertActionParamForScript(String paramName, Serializable value)
        {
            ParameterDefinition paramDef = actionDef.getParameterDefintion(paramName);
            if (paramDef != null && paramDef.getType().equals(DataTypeDefinition.QNAME))
            {
                return ((QName) value).toPrefixString(services.getNamespaceService());
            }
            else
            {
                return convertValueForScript(services, scope, null, value);
            }
        }

        /**
         * Convert Action Parameter for Java usage
         * 
         * @param paramName
         *            parameter name
         * @param value
         *            value to convert
         * @return converted value
         */
        @SuppressWarnings("synthetic-access")
        public Serializable convertActionParamForRepo(String paramName, Serializable value)
        {
            ParameterDefinition paramDef = actionDef.getParameterDefintion(paramName);

            if (paramDef != null && paramDef.getType().equals(DataTypeDefinition.QNAME))
            {
                if (value instanceof Wrapper)
                {
                    // unwrap a Java object from a JavaScript wrapper
                    // recursively call this method to convert the unwrapped value
                    return convertActionParamForRepo(paramName, (Serializable) ((Wrapper) value).unwrap());
                }
                else
                {
                    if (value instanceof String)
                    {
                        String stringQName = (String) value;
                        if (stringQName.startsWith("{"))
                        {
                            return QName.createQName(stringQName);
                           
                        }
                        else
                        {
                            return QName.createQName(stringQName, services.getNamespaceService());
                        }
                    }
                    else
                    {
                        return value;
                    }
                }
            }
            else
            {
                return convertValueForRepo(value);
            }
        }
    }
    
    /**
     * Scripted Parameter map with modified flag.
     * 
     * @author davidc
     */
    public static final class ScriptableParameterMap<K, V> extends ScriptableHashMap<K, V>
    {
        private static final long serialVersionUID = 574661815973241554L;

        private boolean modified = false;

        /**
         * Is this a modified parameter map?
         * 
         * @return true => modified
         */
        /* package */boolean isModified()
        {
            return modified;
        }

        /**
         * Set explicitly whether this map is modified
         * 
         * @param modified
         *            true => modified, false => not modified
         */
        /* package */void setModified(boolean modified)
        {
            this.modified = modified;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.mozilla.javascript.Scriptable#getClassName()
         */
        @Override
        public String getClassName()
        {
            return "ScriptableParameterMap";
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.mozilla.javascript.Scriptable#delete(java.lang.String)
         */
        @Override
        public void delete(String name)
        {
            super.delete(name);
            setModified(true);
        }

        /*
         * (non-Javadoc)
         * 
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
