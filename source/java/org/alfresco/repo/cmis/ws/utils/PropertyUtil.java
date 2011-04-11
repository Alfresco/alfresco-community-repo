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
package org.alfresco.repo.cmis.ws.utils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.alfresco.cmis.CMISDataTypeEnum;
import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISInvalidArgumentException;
import org.alfresco.cmis.CMISPropertyDefinition;
import org.alfresco.cmis.CMISServiceException;
import org.alfresco.cmis.CMISServices;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.PropertyFilter;
import org.alfresco.repo.cmis.ws.Aspects;
import org.alfresco.repo.cmis.ws.CmisException;
import org.alfresco.repo.cmis.ws.CmisPropertiesType;
import org.alfresco.repo.cmis.ws.CmisProperty;
import org.alfresco.repo.cmis.ws.CmisPropertyBoolean;
import org.alfresco.repo.cmis.ws.CmisPropertyDateTime;
import org.alfresco.repo.cmis.ws.CmisPropertyDecimal;
import org.alfresco.repo.cmis.ws.CmisPropertyHtml;
import org.alfresco.repo.cmis.ws.CmisPropertyId;
import org.alfresco.repo.cmis.ws.CmisPropertyInteger;
import org.alfresco.repo.cmis.ws.CmisPropertyString;
import org.alfresco.repo.cmis.ws.CmisPropertyUri;
import org.alfresco.repo.cmis.ws.EnumServiceException;
import org.alfresco.repo.cmis.ws.SetAspects;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Class for managing access control to CMIS properties
 * 
 * @author Dmitry Lazurkin
 * @author Dmitry Velichkevich
 */
public class PropertyUtil
{
    private static final DatatypeFactory DATATYPE_FACTORY;
    static
    {
        try
        {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        }
        catch (DatatypeConfigurationException e)
        {
            throw new RuntimeException(("Data type instance creation failed! Failed message: " + e.toString()), e);
        }
    }

    private final static String NAMESPACE_BEGIN = "" + QName.NAMESPACE_BEGIN;

    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private CMISServices cmisService;
    private CMISDictionaryService cmisDictionaryService;

    public PropertyUtil()
    {
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setCmisService(CMISServices cmisService)
    {
        this.cmisService = cmisService;
    }

    public void setCmisDictionaryService(CMISDictionaryService cmisDictionaryService)
    {
        this.cmisDictionaryService = cmisDictionaryService;
    }

    /**
     * Gets property value by its name from Node Reference object
     * 
     * @param objectNodeRef - <b>NodeRef</b> instance that represents Id of the source object
     * @param propertyName - <b>String</b> instance that represents property name
     * @param defaultValue - some value of the appropriate for conversion type. Also <b>null</b> may be accepted by this parameter
     * @return value instance of the appropriate type if specified object has such property and <i>defaultValue</i> if requested property value or <i>objectNodeRef</i> or
     *         <i>propertyName</i> are <b>null</b> or if some exception occurred during property receiving
     * @throws CMISInvalidArgumentException 
     */
    public <ResultType> ResultType getProperty(NodeRef objectNodeRef, String propertyName, ResultType defaultValue)
            throws CMISInvalidArgumentException
    {
        if ((null == objectNodeRef) || (null == propertyName))
        {
            return defaultValue;
        }

        Serializable value = cmisService.getProperty(objectNodeRef, propertyName);
        try
        {
            return convertPropertyValue(value, defaultValue);
        }
        catch (Exception exception)
        {
            return defaultValue;
        }
    }

    /**
     * Extracts from <b>CmisPropertiesType</b> instance property by its name and returns casted to appropriate type value of the extracted property
     * 
     * @param cmisProperties - <b>CmisPropertiesType</b> properties instance
     * @param property - <b>String</b> instance that represents property name
     * @param defaultValue - some value of the appropriate for conversion type. Also <b>null</b> may be accepted by this parameter
     * @return value instance of the appropriate type if specified <i>cmisProperties</i> contains specified properties and <i>defaultValue</i> if requested property value or
     *         <i>cmisProperties</i> or <i>property</i> are <b>null</b> or if some exception occurred during property searching and receiving
     */
    public <ResultType> ResultType getCmisPropertyValue(CmisPropertiesType cmisProperties, String property, ResultType defaultValue) throws CmisException
    {
        if ((null == property) || (null == cmisProperties))
        {
            return defaultValue;
        }

        for (CmisProperty cmisProperty : cmisProperties.getProperty())
        {
            if ((null != cmisProperty) && property.equals(getPropertyName(cmisProperty)))
            {
                return convertPropertyValue(getValue(cmisProperty), defaultValue);
            }
        }

        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    private <ResultType> ResultType convertPropertyValue(Object propertyValue, ResultType defaultValue)
    {
        if (null == propertyValue)
        {
            return defaultValue;
        }

        try
        {
            return (ResultType) propertyValue;
        }
        catch (Throwable e)
        {
            return defaultValue;
        }
    }

    private Serializable getValue(CmisProperty cmisProperty) throws CmisException
    {
        Serializable value = null;
        String propertyName = getPropertyName(cmisProperty);
        if ((null == cmisProperty) || (null == propertyName))
        {
            return value;
        }

        PropertyMultiValueStateEnum multivaluedState = getPropertyMultiValuedState(null, propertyName);
        Collection<?> convertedValue = null;

        if (cmisProperty instanceof CmisPropertyBoolean)
        {
            convertedValue = ((CmisPropertyBoolean) cmisProperty).getValue();
        }
        else if (cmisProperty instanceof CmisPropertyDateTime)
        {
            convertedValue = ((CmisPropertyDateTime) cmisProperty).getValue();
            List<GregorianCalendar> dates = new ArrayList<GregorianCalendar>(convertedValue.size());
            for (Object date : convertedValue)
            {
                dates.add(((XMLGregorianCalendar)date).toGregorianCalendar());
            }
            convertedValue = dates;
        }
        else if (cmisProperty instanceof CmisPropertyDecimal)
        {
            convertedValue = ((CmisPropertyDecimal) cmisProperty).getValue();
        }
        else if (cmisProperty instanceof CmisPropertyHtml)
        {
            convertedValue = ((CmisPropertyHtml) cmisProperty).getValue();
        }
        else if (cmisProperty instanceof CmisPropertyId)
        {
            convertedValue = ((CmisPropertyId) cmisProperty).getValue();
        }
        else if (cmisProperty instanceof CmisPropertyInteger)
        {
            convertedValue = ((CmisPropertyInteger) cmisProperty).getValue();
        }
        else if (cmisProperty instanceof CmisPropertyString)
        {
            convertedValue = ((CmisPropertyString) cmisProperty).getValue();
        }
        else if (cmisProperty instanceof CmisPropertyUri)
        {
            convertedValue = ((CmisPropertyUri) cmisProperty).getValue();
        }

        if (null != convertedValue)
        {
            if (isMultiValued(propertyName, multivaluedState, convertedValue))
            {
                value = (convertedValue.size() > 0) ? new ArrayList<Object>((convertedValue)) : (null);
            }
            else
            {
                value = (Serializable)convertedValue.iterator().next();
            }
        }

        return value;
    }

    private boolean isMultiValued(String propertyName, PropertyMultiValueStateEnum state, Collection<?> values) throws CmisException
    {
        // TODO: WARNING!!! This is invalid behavior! Multi valued property state can't be identified in this way!!!
        if (PropertyMultiValueStateEnum.PROPERTY_NOT_FOUND == state)
        {
            return (values.size() > 0) ? (values.size() > 1) : (true);
        }

        if ((PropertyMultiValueStateEnum.PROPERTY_NOT_MULTIVALUED == state) && (values.size() > 1))
        {
            throw ExceptionUtil.createCmisException("\"" + propertyName + "\" property is not Multi Valued", EnumServiceException.INVALID_ARGUMENT);
        }

        return PropertyMultiValueStateEnum.PROPERTY_MULTIVALUED == state;
    }

    private PropertyMultiValueStateEnum getPropertyMultiValuedState(String typeId, String cmisPropertyName)
    {
        if ((null == cmisPropertyName) || cmisPropertyName.equals(""))
        {
            return PropertyMultiValueStateEnum.PROPERTY_NOT_FOUND;
        }

        CMISTypeDefinition typeDefinition = ((null != typeId) && !typeId.equals("")) ? (cmisDictionaryService.findType(typeId)) : (null);
        CMISPropertyDefinition propertyDefinition = null;
        if ((null != typeDefinition) && (null != typeDefinition.getOwnedPropertyDefinitions()))
        {
            propertyDefinition = typeDefinition.getOwnedPropertyDefinitions().get(cmisPropertyName);
        }
        else
        {
            propertyDefinition = cmisDictionaryService.findProperty(cmisPropertyName, null);
        }

        if ((null == propertyDefinition) || (null == propertyDefinition.getPropertyAccessor()))
        {
            return PropertyMultiValueStateEnum.PROPERTY_NOT_FOUND;
        }

        PropertyDefinition nativePropertyDefinition = null;
        if (null != propertyDefinition.getPropertyAccessor().getMappedProperty())
        {
            nativePropertyDefinition = dictionaryService.getProperty(propertyDefinition.getPropertyAccessor().getMappedProperty());
        }
        else
        {
            nativePropertyDefinition = dictionaryService.getProperty(createQName(cmisPropertyName));
        }

        if (null == nativePropertyDefinition)
        {
            return PropertyMultiValueStateEnum.PROPERTY_NOT_FOUND;
        }

        return (nativePropertyDefinition.isMultiValued()) ? (PropertyMultiValueStateEnum.PROPERTY_MULTIVALUED) : (PropertyMultiValueStateEnum.PROPERTY_NOT_MULTIVALUED);
    }

    private enum PropertyMultiValueStateEnum
    {
        PROPERTY_MULTIVALUED, PROPERTY_NOT_MULTIVALUED, PROPERTY_NOT_FOUND;
    }

    /**
     * Converts <b>CmisPropertiesType</b> properties representation to rapidly accessible form
     * 
     * @param cmisProperties - <b>CmisPropertiesType</b> properties representation
     * @return <b>Map</b>&lt;<b>String</b>, <b>Serializable</b>&gt; properties representation
     * @throws <b>CmisException</b>
     */
    public Map<String, Serializable> getPropertiesMap(CmisPropertiesType cmisProperties) throws CmisException
    {
        Map<String, Serializable> properties = new HashMap<String, Serializable>();

        if (null == cmisProperties)
        {
            return properties;
        }

        for (CmisProperty cmisProperty : cmisProperties.getProperty())
        {
            if (null != cmisProperty)
            {
                String pdid = getPropertyName(cmisProperty);
                properties.put(pdid, getValue(cmisProperty));
            }
        }

        return properties;
    }

    /**
     * Sets and checks all properties' fields for specified node
     * 
     * @param nodeRef
     *            - <b>NodeRef</b> for node for those properties must be setted
     * @param properties
     *            - <b>CmisPropertiesType</b> instance that contains all the necessary properties' fields
     * @param ignoringPropertiesFilter
     *            - <b>PropertyFilter</b> instance. This filter determines which properties should be ignored and not
     *            setted without exception. If this parameter is <b>null</b> all properties will be processed in common
     *            flow
     */
    public void setProperties(NodeRef nodeRef, CmisPropertiesType properties, PropertyFilter ignoringPropertiesFilter)
            throws CmisException
    {
        if (nodeRef == null || properties == null)
        {
            return;
        }

        try
        {

            for (CmisProperty property : properties.getProperty())
            {
                String propertyName = getPropertyName(property);
                if ((null == propertyName)
                        || ((null != ignoringPropertiesFilter) && ignoringPropertiesFilter.allow(propertyName)))
                {
                    continue;
                }
                Object value = getValue(property);
                cmisService.setProperty(nodeRef, propertyName, (Serializable) value);
            }

            // Process Alfresco aspect-setting extension
            for (Object extensionObj : properties.getAny())
            {
                if (!(extensionObj instanceof SetAspects))
                {
                    continue;
                }
                SetAspects setAspects = (SetAspects) extensionObj;
                cmisService.setAspects(nodeRef, setAspects.getAspectsToRemove(), setAspects.getAspectsToAdd());
                CmisPropertiesType extensionProperties = setAspects.getProperties();
                if (extensionProperties == null)
                {
                    continue;
                }

                for (CmisProperty property : extensionProperties.getProperty())
                {
                    String propertyName = getPropertyName(property);
                    if ((null == propertyName)
                            || ((null != ignoringPropertiesFilter) && ignoringPropertiesFilter.allow(propertyName)))
                    {
                        continue;
                    }
                    Object value = getValue(property);

                    // This time, call setProperty without constraining the owning type
                    cmisService.setProperty(nodeRef, null, propertyName, (Serializable) value);
                }
            }
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
    }

    public String getPropertyName(CmisProperty property)
    {
        String propertyName = (null != property) ? (property.getPropertyDefinitionId()) : (null);
        if (null == propertyName)
        {
            propertyName = property.getLocalName();
            if (null == propertyName)
            {
                propertyName = property.getDisplayName();
            }
        }
        return propertyName;
    }

    private QName createQName(String s)
    {
        QName qname;
        if (s.indexOf(NAMESPACE_BEGIN) != -1)
        {
            qname = QName.createQName(s);
        }
        else
        {
            qname = QName.createQName(s, namespaceService);
        }
        return qname;
    }

    /**
     * Get CMIS properties for object
     * 
     * @param nodeRef node reference
     * @param filter property filter
     * @return properties
     */
    public CmisPropertiesType getProperties(Object object, PropertyFilter filter) throws CmisException
    {
        if (object instanceof Version)
        {
            object = ((Version) object).getFrozenStateNodeRef();
        }
        try
        {
            CmisPropertiesType result = new CmisPropertiesType();
            Map<String, Serializable> properties;
            if (object instanceof NodeRef)
            {
                properties = cmisService.getProperties((NodeRef) object);
                
                // Handle fetching of aspects and their properties with Alfresco extension
                Aspects extension = new Aspects();
                result.getAny().add(extension);
                List<String> aspects = extension.getAppliedAspects();
                Map<String, Serializable> aspectProperties = new HashMap<String, Serializable>(97);
                for (CMISTypeDefinition typeDef : cmisService.getAspects((NodeRef)object))
                {
                    aspects.add(typeDef.getTypeId().getId());
                    aspectProperties.putAll(cmisService.getProperties((NodeRef)object, typeDef));
                }
                CmisPropertiesType aspectResult = new CmisPropertiesType();
                convertToCmisProperties(aspectProperties, filter, aspectResult);
                extension.setProperties(aspectResult);
            }
            else
            {
                properties = cmisService.getProperties((AssociationRef) object); 
            }
            convertToCmisProperties(    properties, filter, result);                       
            return result;
        }
        catch (CMISInvalidArgumentException e)
        {
            throw ExceptionUtil.createCmisException(e.getMessage(), EnumServiceException.INVALID_ARGUMENT, e);
        }
    }
    
    private void convertToCmisProperties(Map<String, Serializable> properties, PropertyFilter filter, CmisPropertiesType cmisProperties) throws CmisException
    {
        for (String propertyName : properties.keySet())
        {
            CMISPropertyDefinition propertyTypeDef = cmisDictionaryService.findProperty(propertyName, null);
            if ((null != propertyTypeDef) && filter.allow(propertyName))
            {
                CmisProperty property = createProperty(propertyName, propertyTypeDef.getDataType(), properties.get(propertyName));

                if (null != property)
                {
                    cmisProperties.getProperty().add(property);
                }
            }
        }
    }

    /**
     * Creates and initializes appropriate <b>CmisProperty</b> instance by name and data type
     * 
     * @param pdid - <b>String</b> value that represents CMIS property name
     * @param dataType - <b>CMISDataTypeEnum</b> value that specifies real type of the property
     * @param value - some instance of appropriate type or some <b>Collection</b> that contains several values of the type
     * @return appropriate <b>CmisProperty</b> instance
     */
    @SuppressWarnings("unchecked")
    public CmisProperty createProperty(String pdid, CMISDataTypeEnum dataType, Serializable value)
    {
        if (value == null)
        {
            return null;
        }

        switch (dataType)
        {
        case BOOLEAN:
        {
            CmisPropertyBoolean property = new CmisPropertyBoolean();
            property.setPropertyDefinitionId(pdid);

            if (value instanceof Collection)
            {
                for (Boolean propertyValue : (Collection<Boolean>) value)
                {
                    property.getValue().add(propertyValue);
                }
            }
            else
            {
                property.getValue().add((Boolean) value);
            }

            return property;
        }
        case STRING:
        {
            CmisPropertyString property = new CmisPropertyString();
            property.setPropertyDefinitionId(pdid);

            if (value instanceof Collection)
            {
                for (String propertyValue : (Collection<String>) value)
                {
                    property.getValue().add(propertyValue);
                }
            }
            else
            {
                property.getValue().add((String) value);
            }

            return property;
        }
        case INTEGER:
        {
            CmisPropertyInteger property = new CmisPropertyInteger();
            property.setPropertyDefinitionId(pdid);

            if (value instanceof Collection)
            {
                for (Object propertyValue : (Collection<Object>) value)
                {
                    if(propertyValue instanceof Long)
                    {
                        property.getValue().add(BigInteger.valueOf((Long) propertyValue));
                    }
                    else
                    {
                        property.getValue().add(BigInteger.valueOf((Integer) propertyValue));
                    }
                }
            }
            else
            {
                if(value instanceof Long)
                {
                    property.getValue().add(BigInteger.valueOf((Long) value));
                }
                else
                {
                    property.getValue().add(BigInteger.valueOf((Integer) value));
                }
            }

            return property;
        }
        case DATETIME:
        {
            CmisPropertyDateTime property = new CmisPropertyDateTime();
            property.setPropertyDefinitionId(pdid);

            if (value instanceof Collection)
            {
                for (Date propertyValue : (Collection<Date>) value)
                {
                    XMLGregorianCalendar convertedValue = convert(propertyValue);
                    if (null != convertedValue)
                    {
                        property.getValue().add(convertedValue);
                    }
                }
            }
            else
            {
                XMLGregorianCalendar convertedValue = convert((Date) value);
                if (null != convertedValue)
                {
                    property.getValue().add(convert((Date) value));
                }
            }

            return property;
        }
        case ID:
        {
            CmisPropertyId property = new CmisPropertyId();
            property.setPropertyDefinitionId(pdid);

            if (value instanceof Collection)
            {
                for (Serializable propertyValue : (Collection<Serializable>) value)
                {
                    // NOTE: CMIS multi-valued values cannot contain null
                    if (propertyValue != null)
                    {
                        property.getValue().add(propertyValue.toString());
                    }
                }
            }
            else
            {
                property.getValue().add(value != null ? value.toString() : null);
            }

            return property;
        }
        case URI:
        {
            CmisPropertyUri property = new CmisPropertyUri();
            property.setPropertyDefinitionId(pdid);

            if (value instanceof Collection)
            {
                for (String propertyValue : (Collection<String>) value)
                {
                    property.getValue().add(propertyValue);
                }
            }
            else
            {
                property.getValue().add((String) value);
            }

            return property;
        }
        case DECIMAL:
        {
            CmisPropertyDecimal property = new CmisPropertyDecimal();
            property.setPropertyDefinitionId(pdid);

            if (value instanceof Collection) 
            {
                for (Serializable propertyValue : (Collection<Serializable>) value)
                {
                    property.getValue().add(decimalValue(propertyValue));
                }
            }
            else
            {
                property.getValue().add(decimalValue(value));
            }

            return property;
        }
        case HTML:
        {
            CmisPropertyHtml property = new CmisPropertyHtml();
            property.setPropertyDefinitionId(pdid);

            if (value instanceof Collection)
            {
                for (String propertyValue : (Collection<String>) value)
                {
                    property.getValue().add(propertyValue);
                }
            }
            else
            {
                property.getValue().add((String) value);
            }

            return property;
        }
        default:
        {
            return null;
        }
        }
    }

    private BigDecimal decimalValue(Serializable value) 
    { 
        if ((value instanceof Float) || (value instanceof Double)) 
        { 
            return BigDecimal.valueOf(((Number) value).doubleValue()); 
        } 
        return null; 
    } 
    
    /**
     * Converts Date object to XMLGregorianCalendar object
     * 
     * @param date Date object
     * @return XMLGregorianCalendar object
     */
    public XMLGregorianCalendar convert(Date date)
    {
        if (null != date)
        {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            return DATATYPE_FACTORY.newXMLGregorianCalendar(calendar);
        }

        return null;
    }
}
