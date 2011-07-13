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
package org.alfresco.repo.jscript;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Wrapper;

/**
 * Scriptable Action
 * 
 * @author davidc
 */
public class ScriptAction implements Serializable, Scopeable
{
    private static final long serialVersionUID = 5794161358406531996L;

    /** Root scope for this object */
    private Scriptable scope;

    /** Converter with knowledge of action parameter values */
    private ActionValueConverter converter;

    /** Action state */
    protected Action action;

    protected ActionDefinition actionDef;
    
    protected ServiceRegistry services;
    private ActionService actionService;
    private NamespaceService namespaceService;
    private TransactionService transactionService;

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
        this.actionService = services.getActionService();
        this.namespaceService = services.getNamespaceService();
        this.transactionService = services.getTransactionService();
        
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
     * Execute action.  The existing transaction will be joined.
     * 
     * @param node
     *            the node to execute action upon
     */
    @SuppressWarnings("synthetic-access")
    public void execute(ScriptNode node)
    {
        performParamConversionForRepo();
        executeImpl(node);
        
        // Parameters may have been updated by action execution, so reset cache
        this.parameters = null;
        
        // Reset the actioned upon node
        node.reset();
    }
    
    /**
     * Execute action.  The existing transaction will be joined.
     * 
     * @param node
     *            the node to execute action upon
     */
    @SuppressWarnings("synthetic-access")
    public void executeAsynchronously(ScriptNode node)
    {
        performParamConversionForRepo();
        executeAsynchronouslyImpl(node);
        
        // Parameters may have been updated by action execution, so reset cache
        this.parameters = null;
        
        // Reset the actioned upon node
        node.reset();
    }

    protected void executeImpl(ScriptNode node)
    {
        actionService.executeAction(action, node.getNodeRef());
    }
    
    protected void executeAsynchronouslyImpl(ScriptNode node)
    {
        actionService.executeAction(action, node.getNodeRef(), true, true);
    }
    
    /**
     * Execute action, optionally starting a new, potentially read-only transaction.
     * 
     * @param node
     *            the node to execute action upon
     * @param newTxn
     *            <tt>true</tt> to start a new, isolated transaction
     * 
     * @see RetryingTransactionHelper#doInTransaction(RetryingTransactionCallback, boolean, boolean)
     */
    @SuppressWarnings("synthetic-access")
    public void execute(final ScriptNode node, boolean readOnly, boolean newTxn)
    {
        performParamConversionForRepo();
        RetryingTransactionCallback<Object> executionActionCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                executeImpl(node);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(
                executionActionCallback,
                readOnly,
                newTxn);
        
        // Parameters may have been updated by action execution, so reset cache
        this.parameters = null;
        
        // Reset the actioned upon node
        node.reset();
    }

    /**
     * Execute action.  The existing transaction will be joined.
     * 
     * @param nodeRef
     *            the node to execute action upon
     */
    @SuppressWarnings("synthetic-access")
    public void execute(NodeRef nodeRef)
    {
        performParamConversionForRepo();
        actionService.executeAction(action, nodeRef);

        // Parameters may have been updated by action execution, so reset cache
        this.parameters = null;
    }

    /**
     * Execute action, optionally starting a new, potentially read-only transaction.
     * 
     * @param nodeRef
     *            the node to execute action upon
     * @param newTxn
     *            <tt>true</tt> to start a new, isolated transaction
     * 
     * @see RetryingTransactionHelper#doInTransaction(RetryingTransactionCallback, boolean, boolean)
     */
    @SuppressWarnings("synthetic-access")
    public void execute(final NodeRef nodeRef, boolean readOnly, boolean newTxn)
    {
        performParamConversionForRepo();
        RetryingTransactionCallback<Object> executionActionCallback = new RetryingTransactionCallback<Object>()
        {
            public Object execute() throws Throwable
            {
                actionService.executeAction(action, nodeRef);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(
                executionActionCallback,
                readOnly,
                newTxn);

        // Parameters may have been updated by action execution, so reset cache
        this.parameters = null;
    }
    
	protected void performParamConversionForRepo() {
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
                return ((QName) value).toPrefixString(namespaceService);
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
                            return QName.createQName(stringQName, namespaceService);
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
