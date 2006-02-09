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

import java.io.StringReader;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Provides functionality to load a saved search and execute it to return TemplateNode objects.
 * 
 * @author Kevin Roast
 */
public class SavedSearchResultsMap extends BaseSearchResultsMap
{
    private static final String ELEMENT_QUERY = "query";
    
    /**
     * Constructor
     * 
     * @param parent         The parent TemplateNode to execute searches from 
     * @param services       The ServiceRegistry to use
     */
    public SavedSearchResultsMap(TemplateNode parent, ServiceRegistry services)
    {
        super(parent, services);
    }

    /**
     * @see org.alfresco.repo.template.BaseTemplateMap#get(java.lang.Object)
     */
    public Object get(Object key)
    {
        String search = null;
        
        if (key != null && key.toString().length() != 0)
        {
            // read the Saved Search XML on the specified node - and get the Lucene search from it
            try
            {
                NodeRef ref = new NodeRef(key.toString());
                
                ContentReader content = services.getContentService().getReader(ref, ContentModel.PROP_CONTENT);
                if (content != null && content.exists())
                {
                    // get the root element
                    SAXReader reader = new SAXReader();
                    Document document = reader.read(new StringReader(content.getContentString()));
                    Element rootElement = document.getRootElement();
                    
                    Element queryElement = rootElement.element(ELEMENT_QUERY);
                    if (queryElement != null)
                    {
                        search = queryElement.getText();
                    }
                }
            }
            catch (Throwable err)
            {
                throw new AlfrescoRuntimeException("Failed to find or load saved Search: " + key, err);
            }
        }
        
        // execute the search
        return query(search);
    }
}
