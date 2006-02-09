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
