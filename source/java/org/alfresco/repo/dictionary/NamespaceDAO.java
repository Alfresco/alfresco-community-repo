/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.dictionary;

import org.alfresco.service.namespace.NamespacePrefixResolver;


/**
 * Namespace DAO Interface.
 * 
 * This DAO is responsible for retrieving and creating Namespace definitions.
 * 
 * @author David Caruana
 */
public interface NamespaceDAO extends NamespacePrefixResolver
{
    
    /**
     * Add a namespace URI
     * 
     * @param uri the namespace uri to add
     */
    public void addURI(String uri);

    /**
     * Remove the specified URI
     * 
     * @param uri the uri to remove
     */
    public void removeURI(String uri);

    /**
     * Add a namespace prefix
     * 
     * @param prefix the prefix
     * @param uri the uri to prefix
     */    
    public void addPrefix(String prefix, String uri);

    /**
     * Remove a namspace prefix
     * 
     * @param prefix the prefix to remove
     */
    public void removePrefix(String prefix);
    
    /**
     * Initialise Namespaces
     */
    public void init();
    
    /**
     * Destroy Namespaces
     */
    public void destroy();
    
    /**
     * Register with the Dictionary
     */
    public void registerDictionary(DictionaryDAO dictionaryDAO);
}
