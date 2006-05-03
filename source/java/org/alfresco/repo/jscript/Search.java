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
package org.alfresco.repo.jscript;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Search component for use by the ScriptService.
 * <p>
 * Provides access to Lucene search facilities including saved search objects. The results
 * from a search are returned as an array (collection) of scriptable Node wrapper objects.
 * <p>
 * The object is added to the root of the model to provide syntax such as:
 * <code>var results = search.luceneSearch(statement);</code>
 * and
 * <code>var results = search.savedSearch(node);</code>
 * 
 * @author Kevin Roast
 */
public final class Search
{
    private static Log logger = LogFactory.getLog(Search.class);
    
    private ServiceRegistry services;
    private StoreRef storeRef;
    private TemplateImageResolver imageResolver;
    
    
    /**
     * Constructor
     * 
     * @param services      The ServiceRegistry to use
     */
    public Search(ServiceRegistry services, StoreRef storeRef, TemplateImageResolver imageResolver)
    {
        this.services = services;
        this.storeRef = storeRef;
        this.imageResolver = imageResolver;
    }
    
    /**
     * Execute a Lucene search
     * 
     * @param search        Lucene search string to execute
     * 
     * @return Node[] of results from the search - can be empty but not null
     */
    public Node[] luceneSearch(String search)
    {
        return query(search);
    }
    
    /**
     * Execute a saved Lucene search
     * 
     * @param savedSearch   Node that contains the saved lucene search content
     * 
     * @return Node[] of results from the search - can be empty but not null
     */
    public Node[] savedSearch(Node savedSearch)
    {
        String search = null;
        
        // read the Saved Search XML on the specified node - and get the Lucene search from it
        try
        {
            ContentReader content = this.services.getContentService().getReader(
                    savedSearch.getNodeRef(), ContentModel.PROP_CONTENT);
            if (content != null && content.exists())
            {
                // get the root element
                SAXReader reader = new SAXReader();
                Document document = reader.read(new StringReader(content.getContentString()));
                Element rootElement = document.getRootElement();
                
                Element queryElement = rootElement.element("query");
                if (queryElement != null)
                {
                    search = queryElement.getText();
                }
            }
        }
        catch (Throwable err)
        {
            throw new AlfrescoRuntimeException("Failed to find or load saved Search: " + savedSearch.getNodeRef(), err);
        }
        
        return search != null ? query(search) : new Node[0];
    }
    
    private Node[] query(String search)
    {
        Node[] nodes = null;
        
        // perform the search against the repo
        ResultSet results = null;
        try
        {
            results = this.services.getSearchService().query(
                      this.storeRef,
                      SearchService.LANGUAGE_LUCENE,
                      search);
            
            if (results.length() != 0)
            {
                nodes = new Node[results.length()];
                int count = 0;
                for (ResultSetRow row: results)
                {
                    NodeRef nodeRef = row.getNodeRef();
                    nodes[count++] = new Node(nodeRef, services, this.imageResolver);
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
        
        return nodes != null ? nodes : new Node[0];
    }
}
