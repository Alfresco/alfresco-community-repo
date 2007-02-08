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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
    
}
