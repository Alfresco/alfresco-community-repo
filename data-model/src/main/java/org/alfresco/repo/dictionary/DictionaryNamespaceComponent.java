/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.repo.dictionary;

import java.util.Collection;

import org.alfresco.service.namespace.NamespaceService;


/**
 * Data Dictionary Namespace Service Implementation
 * 
 * @author David Caruana
 */
public class DictionaryNamespaceComponent implements NamespaceService
{
    /**
     * Namespace DAO
     */
    private NamespaceDAO namespaceDAO;


    /**
     * Sets the Namespace DAO
     * 
     * @param namespaceDAO  namespace DAO
     */
    public void setNamespaceDAO(NamespaceDAO namespaceDAO)
    {
        this.namespaceDAO = namespaceDAO;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.NamespaceService#getURIs()
     */
    public Collection<String> getURIs()
    {
        return namespaceDAO.getURIs();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.NamespaceService#getPrefixes()
     */
    public Collection<String> getPrefixes()
    {
        return namespaceDAO.getPrefixes();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.ref.NamespacePrefixResolver#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(String prefix)
    {
        return namespaceDAO.getNamespaceURI(prefix);
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.ref.NamespacePrefixResolver#getPrefixes(java.lang.String)
     */
    public Collection<String> getPrefixes(String namespaceURI)
    {
        return namespaceDAO.getPrefixes(namespaceURI);
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.service.namespace.NamespaceService#registerNamespace(java.lang.String, java.lang.String)
     */
    public void registerNamespace(String prefix, String uri)
    {
        // TODO:
        throw new UnsupportedOperationException();        
    }


    /* (non-Javadoc)
     * @see org.alfresco.service.namespace.NamespaceService#registerNamespace(java.lang.String, java.lang.String)
     */
    public void unregisterNamespace(String prefix)
    {
        // TODO:
        throw new UnsupportedOperationException();        
    }
}
