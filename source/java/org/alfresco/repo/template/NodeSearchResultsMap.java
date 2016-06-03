package org.alfresco.repo.template;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Provides functionality to execute a Lucene search for a single node by NodeRef.
 * 
 * @author Kevin Roast
 */
public class NodeSearchResultsMap extends BaseSearchResultsMap
{
    /**
     * Constructor
     * 
     * @param parent         The parent TemplateNode to execute searches from 
     * @param services       The ServiceRegistry to use
     */
    public NodeSearchResultsMap(TemplateNode parent, ServiceRegistry services)
    {
        super(parent, services);
    }

    /**
     * @see org.alfresco.repo.template.BaseTemplateMap#get(java.lang.Object)
     */
    public Object get(Object key)
    {
        TemplateNode result = null;
        if (key != null)
        {
            NodeRef nodeRef = new NodeRef((String)key);
            result = new TemplateNode(nodeRef, services, this.parent.getImageResolver());
        }
        return result;
    }
}
