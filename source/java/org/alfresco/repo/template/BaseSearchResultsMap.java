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

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;

/**
 * Class providing the base Search Query services to execute a search returning a list of
 * TemplateNode objects from a Lucene search string.
 * 
 * @author Kevin Roast
 */
public abstract class BaseSearchResultsMap extends BaseTemplateMap
{
    /**
     * Constructor
     * 
     * @param parent         The parent TemplateNode to execute searches from 
     * @param services       The ServiceRegistry to use
     */
    public BaseSearchResultsMap(TemplateNode parent, ServiceRegistry services)
    {
        super(parent, services);
    }

    /**
     * Perform a SearchService query with the given Lucene search string 
     */
    protected List<TemplateNode> query(String search)
    {
        List<TemplateNode> nodes = null;
        
        // check if a full Lucene search string has been supplied or extracted from XML
        if (search != null && search.length() != 0)
        {
            // perform the search against the repo
            ResultSet results = null;
            try
            {
                results = this.services.getSearchService().query(
                        this.parent.getNodeRef().getStoreRef(),
                        SearchService.LANGUAGE_LUCENE,
                        search);
                
                if (results.length() != 0)
                {
                    nodes = new ArrayList<TemplateNode>(results.length());
                    for (ResultSetRow row: results)
                    {
                        NodeRef nodeRef = row.getNodeRef();
                        nodes.add(new TemplateNode(nodeRef, services, this.parent.getImageResolver()));
                    }
                }
            }
            catch (Throwable err)
            {
                throw new AlfrescoRuntimeException("Failed to execute search: " + search, err);
            }
            finally
            {
                if (results != null)
                {
                    results.close();
                }
            }
        }
        
        return nodes != null ? nodes : (List)Collections.emptyList();
    }
}
