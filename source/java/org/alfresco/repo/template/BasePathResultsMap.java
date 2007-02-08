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
package org.alfresco.repo.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A special Map that executes an XPath against the parent Node as part of the get()
 * Map interface implementation.
 * 
 * @author Kevin Roast
 */
public abstract class BasePathResultsMap extends BaseTemplateMap
{
    protected static Log logger = LogFactory.getLog(BasePathResultsMap.class);
    
    /**
     * Constructor
     * 
     * @param parent         The parent TemplateNode to execute searches from 
     * @param services       The ServiceRegistry to use
     */
    public BasePathResultsMap(TemplateNode parent, ServiceRegistry services)
    {
        super(parent, services);
    }
    
    /**
     * Return a list or a single Node from executing an xpath against the parent Node.
     * 
     * @param xpath        XPath to execute
     * @param firstOnly    True to return the first result only
     * 
     * @return List<TemplateNode>
     */
    protected List<TemplateNode> getChildrenByXPath(String xpath, boolean firstOnly)
    {
        List<TemplateNode> result = null;
        
        if (xpath.length() != 0)
        {
            if (logger.isDebugEnabled())
                logger.debug("Executing xpath: " + xpath);
            
            List<NodeRef> nodes = this.services.getSearchService().selectNodes(
                    this.parent.getNodeRef(),
                    xpath,
                    null,
                    this.services.getNamespaceService(),
                    false);
            
            // see if we only want the first result
            if (firstOnly == true)
            {
                if (nodes.size() != 0)
                {
                    result = new ArrayList<TemplateNode>(1);
                    result.add(new TemplateNode(nodes.get(0), this.services, this.parent.getImageResolver()));
                }
            }
            // or all the results
            else
            {
                result = new ArrayList<TemplateNode>(nodes.size()); 
                for (NodeRef ref : nodes)
                {
                    result.add(new TemplateNode(ref, this.services, this.parent.getImageResolver()));
                }
            }
        }
        
        return result != null ? result : (List)Collections.emptyList();
    }
}
