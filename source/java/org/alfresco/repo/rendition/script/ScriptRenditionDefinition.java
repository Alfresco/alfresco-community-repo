package org.alfresco.repo.rendition.script;

import org.alfresco.repo.jscript.ScriptAction;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.rendition.RenderingEngineDefinition;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.namespace.QName;
import org.mozilla.javascript.Scriptable;

/**
 * RenditionDefinition JavaScript Object. This class is a JavaScript-friendly wrapper for
 * the {@link RenditionDefinition renditionDefinition} class.
 * 
 * @author Neil McErlean
 * @see org.alfresco.service.cmr.rendition.RenditionDefinition
 */
public final class ScriptRenditionDefinition extends ScriptAction
{
    private static final long serialVersionUID = 8132935577891455490L;

    public ScriptRenditionDefinition(ServiceRegistry serviceRegistry, Scriptable scope,
            RenderingEngineDefinition engineDefinition, RenditionDefinition renditionDefinition)
    {
    	super(serviceRegistry, renditionDefinition, engineDefinition);
    }
    
    /**
     * Returns the name of this rendition definition in prefix:localName format.
     * 
     * @return the name which uniquely identifies this rendition definition.
     */
    public String getRenditionName()
    {
        QName qname = getRenditionDefinition().getRenditionName();
        return qname.toPrefixString(services.getNamespaceService());
    }
    
    /**
     * Returns the name of the Rendering Engine used by this definition.
     * @return String
     */
    public String getRenderingEngineName()
    {
        return getRenderingEngineDefinition().getName();
    }

    @Override
    public String toString()
    {
        StringBuilder msg = new StringBuilder();
        msg.append(this.getClass().getSimpleName())
            .append("[").append(getRenditionName()).append("]");

        return msg.toString();
    }
    
    RenditionDefinition getRenditionDefinition()
    {
    	this.performParamConversionForRepo();
        return (RenditionDefinition)action;
    }
    
    RenderingEngineDefinition getRenderingEngineDefinition()
    {
        return (RenderingEngineDefinition)actionDef;
    }
    
    @Override
    protected void executeImpl(ScriptNode node)
    {
    	RenditionDefinition renditionDefinition = getRenditionDefinition();
    	this.services.getRenditionService().render(node.getNodeRef(), renditionDefinition);
    }
}
