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
            if (value instanceof Date)
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
