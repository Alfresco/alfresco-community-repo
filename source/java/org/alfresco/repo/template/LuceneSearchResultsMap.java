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
 * Provides functionality to execute a Lucene search string and return TemplateNode objects.
 * 
 * @author Kevin Roast
 */
public class LuceneSearchResultsMap extends BaseSearchResultsMap
{
    /**
     * Constructor
     * 
     * @param parent         The parent TemplateNode to execute searches from 
     * @param services       The ServiceRegistry to use
     */
    public LuceneSearchResultsMap(TemplateNode parent, ServiceRegistry services)
    {
        super(parent, services);
    }

    /**
     * @see org.alfresco.repo.template.BaseTemplateMap#get(java.lang.Object)
     */
    public Object get(Object key)
    {
        // execute the search
        return query(key.toString());
    }
}
