package org.alfresco.repo.template;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.api.AlfrescoPublicApi;    
import org.alfresco.service.namespace.QName;

/**
 * Contract for Template API objects that have properties, aspects and children.
 * 
 * @author Kevin Roast
 */
@AlfrescoPublicApi
public interface TemplateProperties extends TemplateNodeRef
{
    /**
     * @return The properties available on this node.
     */
    public Map<String, Serializable> getProperties();
    
    /**
     * @return The list of aspects applied to this node
     */
    public Set<QName> getAspects();
    
    /**
     * @param aspect The aspect name to test for
     * 
     * @return true if the node has the aspect false otherwise
     */
    public boolean hasAspect(String aspect);
    
    /**
     * @return The children of this Node as TemplateNode wrappers
     */
    public List<TemplateProperties> getChildren();
    
    /**
     * @return the primary parent of this node 
     */
    public TemplateProperties getParent();
}
