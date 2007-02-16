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
