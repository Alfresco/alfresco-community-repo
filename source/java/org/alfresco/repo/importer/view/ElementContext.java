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
package org.alfresco.repo.importer.view;

import org.alfresco.repo.importer.Importer;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;


/**
 * Maintains state about the currently imported element.
 * 
 * @author David Caruana
 *
 */
public class ElementContext
{
    // Dictionary Service
    private DictionaryService dictionary;
    
    // Element Name
    private QName elementName;
    
    // Importer
    private Importer importer;
    
    
    /**
     * Construct
     * 
     * @param dictionary
     * @param elementName
     * @param progress
     */
    public ElementContext(QName elementName, DictionaryService dictionary, Importer importer)
    {
        this.elementName = elementName;
        this.dictionary = dictionary;
        this.importer = importer;
    }
    
    /**
     * @return  the element name
     */
    public QName getElementName()
    {
        return elementName;
    }
    
    /**
     * @return  the dictionary service
     */
    public DictionaryService getDictionaryService()
    {
        return dictionary;
    }
    
    /**
     * @return  the importer
     */
    public Importer getImporter()
    {
        return importer;
    }
}
