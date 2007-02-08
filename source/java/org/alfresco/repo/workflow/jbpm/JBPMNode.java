/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.workflow.jbpm;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Scriptable;


/**
 * Scriptable Node suitable for JBPM Beanshell access
 *
 * TODO: This implementation derives from the JavaScript Alfresco Node.  At
 * some point we should look to having a script-independent node with various
 * script-specific sub-types (and value conversions).
 * 
 * @author davidc
 */
public class JBPMNode extends org.alfresco.repo.jscript.Node
{
    private static final long serialVersionUID = -826970280203254365L;

    /**
     * Construct
     * 
     * @param nodeRef  node reference
     * @param services  services
     */
    public JBPMNode(NodeRef nodeRef, ServiceRegistry services)
    {
        super(nodeRef, services, null);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.jscript.Node#createValueConverter()
     */
    @Override
    protected NodeValueConverter createValueConverter()
    {
        return new JBPMNodeConverter();
    }

    /**
     * Value converter for beanshell.
     */
    private class JBPMNodeConverter extends org.alfresco.repo.jscript.Node.NodeValueConverter
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
        public Serializable convertValueForScript(ServiceRegistry services, Scriptable scope, QName qname, Serializable value)
        {
            if (value instanceof NodeRef)
            {
                return new JBPMNode(((NodeRef)value), services);
            }
            else if (value instanceof Date)
            {
                return value;
            }
            else
            {
                return super.convertValueForScript(services, scope, qname, value);
            }
        }
    }
}
