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
