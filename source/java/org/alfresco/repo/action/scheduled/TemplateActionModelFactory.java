package org.alfresco.repo.action.scheduled;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A factory that builds models to use with a particular template engine for use with scheduled actions built
 * from action templates.
 * 
 * @author Andy Hind
 */
public interface TemplateActionModelFactory
{
    /**
     * Get the name of the template engine for which this factory applies
     * 
     * @return - the template engine.
     */
    public String getTemplateEngine();
    
    /**
     * Build a model with no default node context.
     * 
     * @return - the model for the template engine.
     */
    public Map<String, Object> getModel();
    
    /**
     * Build a model with a default node context.
     * 
     * @param nodeRef NodeRef
     * @return - the model (with nodeRef as its context).
     */
    public Map<String, Object> getModel(NodeRef nodeRef);
}
