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

import java.util.Locale;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;


/**
 * Compiled Property Type Definition
 * 
 * @author David Caruana
 *
 */
/*package*/ class M2DataTypeDefinition implements DataTypeDefinition
{
    private ModelDefinition model;
    private QName name;
    private M2DataType dataType;
    
    
    /*package*/ M2DataTypeDefinition(ModelDefinition model, M2DataType propertyType, NamespacePrefixResolver resolver)
    {
        this.model = model;
        this.name = QName.createQName(propertyType.getName(), resolver);
        this.dataType = propertyType;
    }


    /*package*/ void resolveDependencies(ModelQuery query)
    {
        // Ensure java class has been specified
        String javaClass = dataType.getJavaClassName();
        if (javaClass == null)
        {
            throw new DictionaryException("Java class of data type " + name.toPrefixString() + " must be specified");
        }
        
        // Ensure java class is valid and referenceable
        try
        {
            Class.forName(javaClass);
        }
        catch (ClassNotFoundException e)
        {
            throw new DictionaryException("Java class " + javaClass + " of data type " + name.toPrefixString() + " is invalid", e);
        }
    }
    
    /**
     * @see #getName()
     */
    public String toString()
    {
        return getName().toString();
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.DataTypeDefinition#getModel()
     */
    public ModelDefinition getModel()
    {
        return model;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyTypeDefinition#getName()
     */
    public QName getName()
    {
        return name;
    }

    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyTypeDefinition#getTitle()
     */
    public String getTitle()
    {
        String value = M2Label.getLabel(model, "datatype", name, "title"); 
        if (value == null)
        {
            value = dataType.getTitle();
        }
        return value;
    }
    

    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyTypeDefinition#getDescription()
     */
    public String getDescription()
    {
        String value = M2Label.getLabel(model, "datatype", name, "description"); 
        if (value == null)
        {
            value = dataType.getDescription();
        }
        return value;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.dictionary.PropertyTypeDefinition#getAnalyserClassName()
     */
    public String getAnalyserClassName()
    {
        return getAnalyserClassName(I18NUtil.getLocale());
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.DataTypeDefinition#getAnalyserClassName(java.util.Locale)
     */
    public String getAnalyserClassName(Locale locale)
    {
        String value = M2Label.getLabel(locale, model, "datatype", name, "analyzer");
        if (value == null)
        {
            value = dataType.getAnalyserClassName();
        }
        return value;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.PropertyTypeDefinition#getJavaClassName()
     */
    public String getJavaClassName()
    {
        return dataType.getJavaClassName();
    }
    
}
