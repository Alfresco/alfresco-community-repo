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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.jscript;

import java.io.StringReader;
import java.util.LinkedHashSet;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.mozilla.javascript.Scriptable;

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
public final class Search extends BaseScriptImplementation implements Scopeable
{
    /** Service registry */
    private ServiceRegistry services;
    
    /** Default store reference */
    private StoreRef storeRef;
    
    /** Root scope for this object */
    private Scriptable scope;
    
    /**
     * Set the default store reference
     * 
     * @param   storeRef the default store reference
     */
    public void setStoreUrl(String storeRef)
    {
        this.storeRef = new StoreRef(storeRef);
    }
    
    /**
     * Set the service registry
     * 
     * @param services  the service registry
     */
    public void setServiceRegistry(ServiceRegistry services)
    {
        this.services = services;
    }
    
    /**
     * @see org.alfresco.repo.jscript.Scopeable#setScope(org.mozilla.javascript.Scriptable)
     */
    public void setScope(Scriptable scope)
    {
        this.scope = scope;
    }
    
    /**
     * Find a single Node by the Node reference
     * 
     * @param ref       The NodeRef of the Node to find
     * 
     * @return the Node if found or null if failed to find
     */
    public Node findNode(NodeRef ref)
    {
        return findNode(ref.toString());
    }
    
    /**
     * Find a single Node by the Node reference
     *  
     * @param ref       The fully qualified NodeRef in String format
     *  
     * @return the Node if found or null if failed to find
     */
    public Node findNode(String ref)
    {
        String query = "ID:" + LuceneQueryParser.escape(ref);
        Node[] result = query(query, SearchService.LANGUAGE_LUCENE);
        if (result.length == 1)
        {
            return result[0];
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Execute a XPath search
     * 
     * @param search        XPath search string to execute
     * 
     * @return Node[] of results from the search - can be empty but not null
     */
    public Node[] xpathSearch(String search)
    {
        if (search != null && search.length() != 0)
        {
           return query(search, SearchService.LANGUAGE_XPATH);
        }
        else
        {
           return new Node[0];
        }
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
        if (search != null && search.length() != 0)
        {
           return query(search, SearchService.LANGUAGE_LUCENE);
        }
        else
        {
           return new Node[0];
        }
    }
    
    /**
     * Execute a saved Lucene search
     * 
     * @param savedSearch   Node that contains the saved search XML content
     * 
     * @return Node[] of results from the search - can be empty but not null
     */
    public Node[] savedSearch(Node savedSearch)
    {
        String search = null;
        
        // read the Saved Search XML on the specified node - and get the Lucene search from it
        try
        {
            if (savedSearch != null)
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
        }
        catch (Throwable err)
        {
            throw new AlfrescoRuntimeException("Failed to find or load saved Search: " + savedSearch.getNodeRef(), err);
        }
        
        return search != null ? query(search, SearchService.LANGUAGE_LUCENE) : new Node[0];
    }
    
    /**
     * Execute a saved Lucene search
     * 
     * @param searchRef    NodeRef string that points to the node containing saved search XML content
     * 
     * @return Node[] of results from the search - can be empty but not null
     */
    public Node[] savedSearch(String searchRef)
    {
        if (searchRef != null)
        {
            return savedSearch(new Node(new NodeRef(searchRef), services, null));
        }
        else
        {
            return new Node[0];
        }
    }

    /**
     * Execute the query
     * 
     * Removes any duplicates that may be present (ID can cause duplicates - it is better to remove them here)
     * 
     * @param search
     * @return
     */
    private Node[] query(String search, String language)
    {   
        LinkedHashSet<Node> set = new LinkedHashSet<Node> ();
        
        // perform the search against the repo
        ResultSet results = null;
        try
        {
            results = this.services.getSearchService().query(
                      this.storeRef,
                      language,
                      search);
            
            if (results.length() != 0)
            {
                for (ResultSetRow row: results)
                {
                    NodeRef nodeRef = row.getNodeRef();
                    set.add(new Node(nodeRef, services, this.scope));
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
     
        return set.toArray(new Node[(set.size())]);
    }
}
