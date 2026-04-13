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

import java.util.Locale;
import java.util.ResourceBundle;

import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StringUtils;
import org.alfresco.repo.i18n.StaticMessageLookup;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
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
    private static final String ERR_NOT_DEFINED_NAMESPACE = "d_dictionary.data_type.namespace_not_defined";
    private static final String ERR_JAVA_CLASS_NOT_SPECIFIED = "d_dictionary.data_type.java_class_not_specified";
    private static final String ERR_JAVA_CLASS_INVALID = "d_dictionary.data_type.java_class_invalid";

    private ModelDefinition model;
    private QName name;
    private M2DataType dataType;
    private String  analyserResourceBundleName;
    private transient MessageLookup staticMessageLookup = new StaticMessageLookup();
    
    
    /*package*/ M2DataTypeDefinition(ModelDefinition model, M2DataType propertyType, NamespacePrefixResolver resolver)
    {
        this.model = model;
        this.name = QName.createQName(propertyType.getName(), resolver);
        if (!model.isNamespaceDefined(name.getNamespaceURI()))
        {
            throw new DictionaryException(ERR_NOT_DEFINED_NAMESPACE, name.toPrefixString(), name.getNamespaceURI(), model.getName().toPrefixString());
        }
        this.dataType = propertyType;
    }


    /*package*/ void resolveDependencies(ModelQuery query)
    {
        // Ensure java class has been specified
        String javaClass = dataType.getJavaClassName();
        if (javaClass == null)
        {
            throw new DictionaryException(ERR_JAVA_CLASS_NOT_SPECIFIED, name.toPrefixString());
        }
        
        // Ensure java class is valid and referenceable
        try
        {
            Class.forName(javaClass);
        }
        catch (ClassNotFoundException e)
        {
            throw new DictionaryException(ERR_JAVA_CLASS_INVALID, javaClass, name.toPrefixString(), e);
        }
    }
    
    /**
     * @see #getName()
     */
    @Override
    public String toString()
    {
        return getName().toString();
    }
    
    @Override
    public ModelDefinition getModel()
    {
        return model;
    }

    @Override
    public QName getName()
    {
        return name;
    }

    @Override
    public String getTitle()
    {
        return getTitle(staticMessageLookup);
    }
    
    @Override
    public String getDescription()
    {
        return getDescription(staticMessageLookup);
    }

    @Override
    public String getTitle(MessageLookup messageLookup)
    {
        String value = M2Label.getLabel(model, messageLookup, "datatype", name, "title"); 
        if (value == null)
        {
            value = dataType.getTitle();
        }
        return value;
    }
    
    @Override
    public String getDescription(MessageLookup messageLookup)
    {
        String value = M2Label.getLabel(model, messageLookup, "datatype", name, "description"); 
        if (value == null)
        {
            value = dataType.getDescription();
        }
        return value;
    }
   
    @Override
    public String getJavaClassName()
    {
        return dataType.getJavaClassName();
    }
}
