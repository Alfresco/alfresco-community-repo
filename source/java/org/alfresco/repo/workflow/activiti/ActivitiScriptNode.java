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
package org.alfresco.repo.workflow.activiti;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * Scriptable Node suitable for Activti Beanshell access
 *
 * @author Frederik Heremans
 * @since 3.4.e
 */
public class ActivitiScriptNode extends ScriptNode
{
    private static final long serialVersionUID = -826970280203254365L;

    /**
     * Construct
     * 
     * @param nodeRef  node reference
     * @param services  services
     */
    public ActivitiScriptNode(NodeRef nodeRef, ServiceRegistry services)
    {
        super(nodeRef, services, null);
    }

    /**
    * {@inheritDoc}
     */
    @Override
    protected NodeValueConverter createValueConverter()
    {
        return new JBPMNodeConverter();
    }

    /**
     * Value converter for beanshell. Dates should be handled differenty since
     * default conversion uses top-level scope which is sometimes missing.
     */
    private class JBPMNodeConverter extends NodeValueConverter
    {
        @Override
        public Serializable convertValueForRepo(Serializable value)
        {
            if (value instanceof Date)
            {
                return value;
            }
            else
            {
                return super.convertValueForRepo(value);
            }
        }

        @Override
        public Serializable convertValueForScript(ServiceRegistry serviceRegistry, Scriptable theScope, QName qname, Serializable value)
        {
        	// ALF-14863: If script-node is used outside of Script-call (eg. Activiti evaluating an expression that contains variables of type ScriptNode)
        	// a scope should be created solely for this conversion. The scope will ALWAYS be set when value-conversion is called from the
        	// ScriptProcessor
        	ensureScopePresent();
        	
            if (value instanceof NodeRef)
            {
                return new ActivitiScriptNode(((NodeRef)value), serviceRegistry);
            }
            else if (value instanceof Date)
            {
                return value;
            }
            else
            {
                return super.convertValueForScript(serviceRegistry, theScope, qname, value);
            }
        }

		private void ensureScopePresent() {
			if(scope == null) {
				// Create a scope for the value conversion. This scope will be an empty scope exposing basic Object and Function, sufficient for value-conversion.
				// In case no context is active for the current thread, we can safely enter end exit one to get hold of a scope
				Context ctx = Context.getCurrentContext();
				boolean closeContext = false;
				if(ctx == null) 
				{
					ctx = Context.enter();
					closeContext = true;
				}
				
				scope = ctx.initStandardObjects();
				scope.setParentScope(null);
				
				if(closeContext) {
					// Only an exit call should be done when context didn't exist before
					Context.exit();
				}
			}
		}
    }
}