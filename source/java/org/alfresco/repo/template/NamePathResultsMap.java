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

import java.util.List;
import java.util.StringTokenizer;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.TemplateNode;

/**
 * A special Map that executes an XPath against the parent Node as part of the get()
 * Map interface implementation.
 * 
 * @author Kevin Roast
 */
public final class NamePathResultsMap extends BasePathResultsMap
{
    /**
     * Constructor
     * 
     * @param parent         The parent TemplateNode to execute searches from 
     * @param services       The ServiceRegistry to use
     */
    public NamePathResultsMap(TemplateNode parent, ServiceRegistry services)
    {
        super(parent, services);
    }
    
    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key)
    {
        StringBuilder xpath = new StringBuilder(128);
        for (StringTokenizer t = new StringTokenizer(key.toString(), "/"); t.hasMoreTokens(); /**/)
        {
            if (xpath.length() != 0)
            {
                xpath.append('/');
            }
            xpath.append("*[@cm:name='")
                 .append(t.nextToken())   // TODO: use QueryParameterDefinition see FileFolderService.search()
                 .append("']");
        }
        
        List<TemplateNode> nodes = getChildrenByXPath(xpath.toString(), true);
        return (nodes.size() != 0) ? nodes.get(0) : null;
    }
}
