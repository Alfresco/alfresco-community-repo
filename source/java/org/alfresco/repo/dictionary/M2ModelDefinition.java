/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.dictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

/**
 * Compiled Model Definition
 * 
 * @author David Caruana
 *
 */
public class M2ModelDefinition implements ModelDefinition
{
    private QName name;
    private M2Model model;
    
    
    /*package*/ M2ModelDefinition(M2Model model, NamespacePrefixResolver resolver)
    {
        this.name = QName.createQName(model.getName(), resolver);
        this.model = model;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ModelDefinition#getName()
     */
    public QName getName()
    {
        return name;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ModelDefinition#getDescription()
     */
    public String getDescription()
    {
        String value = M2Label.getLabel(this, null, null, "description"); 
        if (value == null)
        {
            value = model.getDescription();
        }
        return value;
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ModelDefinition#getAuthor()
     */
    public String getAuthor()
    {
        return model.getAuthor();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ModelDefinition#getPublishedDate()
     */
    public Date getPublishedDate()
    {
        return model.getPublishedDate();
    }


    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.ModelDefinition#getVersion()
     */
    public String getVersion()
    {
        return model.getVersion();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.ModelDefinition#getNamespaces()
     */
    public Collection<NamespaceDefinition> getNamespaces()
    {
        List<NamespaceDefinition> namespaces = new ArrayList<NamespaceDefinition>();
        for (M2Namespace namespace : model.getNamespaces())
        {
            namespaces.add(new M2NamespaceDefinition(this, namespace.getUri(), namespace.getPrefix()));
        }
        return namespaces;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.ModelDefinition#isNamespaceDefined(java.lang.String)
     */
    public boolean isNamespaceDefined(String uri)
    {
        for (M2Namespace namespace : model.getNamespaces())
        {
            if (namespace.getUri().equals(uri))
            {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.ModelDefinition#getImportedNamespaces()
     */
    public Collection<NamespaceDefinition> getImportedNamespaces()
    {
        List<NamespaceDefinition> namespaces = new ArrayList<NamespaceDefinition>();
        for (M2Namespace namespace : model.getImports())
        {
            namespaces.add(new M2NamespaceDefinition(this, namespace.getUri(), namespace.getPrefix()));
        }
        return namespaces;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.ModelDefinition#isNamespaceImported(java.lang.String)
     */
    public boolean isNamespaceImported(String uri)
    {
        for (M2Namespace namespace : model.getImports())
        {
            if (namespace.getUri().equals(uri))
            {
                return true;
            }
        }
        return false;
    }
}
