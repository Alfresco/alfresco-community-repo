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

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.TemplateNode;

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
            String ref = key.toString().replace(":", "\\:");
            ref = ref.replace("/", "\\/");
            
            List<TemplateNode> results = query(ref);
            
            if (results.size() == 1)
            {
                result = results.get(0);
            }
        }
        return result;
    }
}
