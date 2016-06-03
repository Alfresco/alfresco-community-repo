package org.alfresco.repo.workflow.jbpm;

import java.io.Serializable;
import java.util.Date;

import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Context;
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
public class JBPMNode extends ScriptNode
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
        super(nodeRef, services, NullScope.instance());
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
     * Value converter for beanshell.
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
