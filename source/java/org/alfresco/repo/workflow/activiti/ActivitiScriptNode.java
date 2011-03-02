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
package org.alfresco.repo.workflow.activiti;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.jbpm.JBPMNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Scriptable;

/**
 * Scriptable Node suitable for Activti Beanshell access
 *
 * @author Frederik Heremans
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
            if (value instanceof NodeRef)
            {
                return new JBPMNode(((NodeRef)value), serviceRegistry);
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
    }
}