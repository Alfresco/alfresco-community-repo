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

package org.alfresco.repo.forms.processor.node;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;

/**
 * Mock implementation of the repository ClassDefinition.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public class MockClassAttributeDefinition implements PropertyDefinition, AssociationDefinition
{

    private final QName name;
    private DataTypeDefinition dataType = mock(DataTypeDefinition.class);
    private ClassDefinition targetClass = mock(ClassDefinition.class);
    private String description = null;
    private String defaultValue = null;
    private String title = null;

    private boolean targetMandatory = false;
    private boolean targetMany = false;
    private boolean isProtected = false;
    private boolean mandatory = false;
    private boolean multiValued = false;

    private MockClassAttributeDefinition(QName name)
    {
        this.name = name;
    }

    private MockClassAttributeDefinition(QName name, String title, String description, boolean isProtected)
    {
        this(name);
        this.title = title;
        this.description = description;
        this.isProtected = isProtected;
    }

    public static MockClassAttributeDefinition mockPropertyDefinition(QName name, QName dataTypeName)
    {
        MockClassAttributeDefinition mock = new MockClassAttributeDefinition(name);
        mockDataTypeName(mock, dataTypeName, null);
        return mock;
    }
    
    public static MockClassAttributeDefinition mockPropertyDefinition(QName name, QName dataTypeName, String defaultValue)
    {
        return mockPropertyDefinition(name, dataTypeName, null, defaultValue);
    }
    
    public static MockClassAttributeDefinition mockPropertyDefinition(QName name, QName dataTypeName, Class<?> typeClass, String defaultValue)
    {
        MockClassAttributeDefinition mock = new MockClassAttributeDefinition(name);
        mockDataTypeName(mock, dataTypeName, typeClass);
        mock.defaultValue = defaultValue;
        return mock;
    }

    public static MockClassAttributeDefinition mockPropertyDefinition(QName name,// 
                QName dataTypeName,//
                String title,//
                String description,//
                boolean isProtected,//
                String defaultValue,//
                boolean Mandatory,//
                boolean multiValued)
    {
        MockClassAttributeDefinition mock = new MockClassAttributeDefinition(name, title, description, isProtected);
        mockDataTypeName(mock, dataTypeName, null);
        mock.defaultValue = defaultValue;
        mock.mandatory = Mandatory;
        mock.multiValued = multiValued;
        return mock;
    }

    public static MockClassAttributeDefinition mockAssociationDefinition(QName name, QName targetClassName)
    {
        MockClassAttributeDefinition mock = new MockClassAttributeDefinition(name);
        mockTargetClassName(targetClassName, mock);
        return mock;
    }

    public static MockClassAttributeDefinition mockAssociationDefinition(QName name,// 
                QName targetClassName,//
                String title,//
                String description,//
                boolean isProtected,//
                boolean targetMandatory,//
                boolean targetMany)
    {
        MockClassAttributeDefinition mock = new MockClassAttributeDefinition(name, title, description, isProtected);
        mockTargetClassName(targetClassName, mock);
        mock.targetMandatory = targetMandatory;
        mock.targetMany = targetMany;
        return mock;
    }

    private static void mockDataTypeName(MockClassAttributeDefinition mock, QName dataTypeName, Class<?> javaClass)
    {
        when(mock.dataType.getName()).thenReturn(dataTypeName);
        if (javaClass!=null)
        {
            when(mock.dataType.getJavaClassName()).thenReturn(javaClass.getName());
        }
    }

    private static void mockTargetClassName(QName targetClassName, MockClassAttributeDefinition mock)
    {
        when(mock.targetClass.getName()).thenReturn(targetClassName);
    }

    @Override
    public List<ConstraintDefinition> getConstraints()
    {
        return null;
    }

    @Override
    public ClassDefinition getContainerClass()
    {
        return null;
    }

    @Override
    public DataTypeDefinition getDataType()
    {
        return dataType;
    }

    @Override
    public String getDefaultValue()
    {
        return defaultValue;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public IndexTokenisationMode getIndexTokenisationMode()
    {
        return null;
    }

    @Override
    public ModelDefinition getModel()
    {
        return null;
    }

    @Override
    public QName getName()
    {
        return name;
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public boolean isIndexed()
    {
        return false;
    }

    @Override
    public boolean isIndexedAtomically()
    {
        return false;
    }

    @Override
    public boolean isMandatory()
    {
        return mandatory;
    }

    @Override
    public boolean isMandatoryEnforced()
    {
        return false;
    }

    @Override
    public boolean isMultiValued()
    {
        return multiValued;
    }

    @Override
    public boolean isOverride()
    {
        return false;
    }

    @Override
    public boolean isProtected()
    {
        return isProtected;
    }

    @Override
    public boolean isStoredInIndex()
    {
        return false;
    }

    @Override
    public ClassDefinition getSourceClass()
    {
        return null;
    }

    @Override
    public QName getSourceRoleName()
    {
        return null;
    }

    @Override
    public ClassDefinition getTargetClass()
    {
        return targetClass;
    }

    @Override
    public QName getTargetRoleName()
    {
        return null;
    }

    @Override
    public boolean isChild()
    {
        return false;
    }

    @Override
    public boolean isSourceMandatory()
    {
        return false;
    }

    @Override
    public boolean isSourceMany()
    {
        return false;
    }

    @Override
    public boolean isTargetMandatory()
    {
        return targetMandatory;
    }

    @Override
    public boolean isTargetMandatoryEnforced()
    {
        return false;
    }

    @Override
    public boolean isTargetMany()
    {
        return targetMany;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.PropertyDefinition#getAnalyserResourceBundleName()
     */
    @Override
    public String getAnalyserResourceBundleName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.dictionary.PropertyDefinition#resolveAnalyserClassName(java.lang.String, java.util.Locale, java.lang.ClassLoader)
     */
    @Override
    public String resolveAnalyserClassName(Locale locale)
    {
        return null;
    }
    
    @Override
    public String resolveAnalyserClassName()
    {
        return null;
    }
}
