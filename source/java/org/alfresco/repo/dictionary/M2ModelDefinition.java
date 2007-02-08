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

import java.util.Date;

import org.alfresco.service.cmr.dictionary.ModelDefinition;
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
    
}
