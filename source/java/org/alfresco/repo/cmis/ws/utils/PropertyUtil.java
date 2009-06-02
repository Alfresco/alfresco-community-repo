/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.ws.utils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.alfresco.cmis.CMISDataTypeEnum;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISDictionaryService;
import org.alfresco.cmis.CMISPropertyDefinition;
import org.alfresco.cmis.CMISScope;
import org.alfresco.cmis.CMISServices;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.CMISUpdatabilityEnum;
import org.alfresco.repo.cmis.PropertyFilter;
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
import org.alfresco.repo.cmis.ws.CmisPropertyXml;
import org.alfresco.repo.cmis.ws.EnumServiceException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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

    private static final String BASE_TYPE_PROPERTY_NAME = "BaseType";

    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private CMISServices cmisService;
    private CMISDictionaryService cmisDictionaryService;
    private CmisObjectsUtils cmisObjectsUtils;

    public PropertyUtil()
    {
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
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

    public void setCmisObjectsUtils(CmisObjectsUtils cmisObjectsUtils)
    {
        this.cmisObjectsUtils = cmisObjectsUtils;
    }

    /**
     * @return <b>String</b> value that contains standard not updatable properties filter token
     */
    public String createStandardNotUpdatablePropertiesFilter()
    {
        StringBuilder filter = new StringBuilder(CMISDictionaryModel.PROP_OBJECT_ID);
        filter.append(PropertyFilter.PROPERTY_NAME_TOKENS_DELIMETER);
        filter.append(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);
        filter.append(PropertyFilter.PROPERTY_NAME_TOKENS_DELIMETER);
        filter.append(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS);
        filter.append(PropertyFilter.PROPERTY_NAME_TOKENS_DELIMETER);
        filter.append(CMISDictionaryModel.PROP_PARENT_ID);
        filter.append(PropertyFilter.PROPERTY_NAME_TOKENS_DELIMETER);
        filter.append(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID);
        filter.append(PropertyFilter.PROPERTY_NAME_TOKENS_DELIMETER);
        filter.append(CMISDictionaryModel.PROP_VERSION_SERIES_ID);

        return filter.toString();
    }

    /**
     * Gets property value by its name from Node Reference object
     * 
     * @param objectNodeRef - <b>NodeRef</b> instance that represents Id of the source object
     * @param propertyName - <b>String</b> instance that represents property name
     * @param defaultValue - some value of the appropriate for conversion type. Also <b>null</b> may be accepted by this parameter
     * @return value instance of the appropriate type if specified object has such property and <i>defaultValue</i> if requested property value or <i>objectNodeRef</i> or
     *         <i>propertyName</i> are <b>null</b> or if some exception occurred during property receiving
     */
    public <ResultType> ResultType getProperty(NodeRef objectNodeRef, String propertyName, ResultType defaultValue)
    {
        if ((null == objectNodeRef) || (null == propertyName))
        {
            return defaultValue;
        }

        try
        {
            return convertPropertyValue(cmisService.getProperty(objectNodeRef, propertyName), defaultValue);
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
    public <ResultType> ResultType getCmisPropertyValue(CmisPropertiesType cmisProperties, String property, ResultType defaultValue)
    {
        if ((null == property) || (null == cmisProperties))
        {
            return defaultValue;
        }

        for (CmisProperty cmisProperty : cmisProperties.getProperty())
        {
            if ((null != cmisProperty) && property.equals(cmisProperty.getName()))
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

    private Object getValue(CmisProperty cmisProperty)
    {
        Object value = null;

        if (cmisProperty instanceof CmisPropertyBoolean)
        {
            Collection<Boolean> convertedValue = ((CmisPropertyBoolean) cmisProperty).getValue();

            if (null != convertedValue)
            {
                if (1 == convertedValue.size())
                {
                    value = convertedValue.iterator().next();
                }
                else
                {
                    if (convertedValue.size() > 1)
            {
                        value = convertedValue;
                    }
                }
            }
        }
        else if (cmisProperty instanceof CmisPropertyDateTime)
        {
            Collection<XMLGregorianCalendar> convertedValue = ((CmisPropertyDateTime) cmisProperty).getValue();

            if (null != convertedValue)
            {
                if (1 == convertedValue.size())
                {
                    value = convertedValue.iterator().next();
                }
                else
            {
                    if (convertedValue.size() > 1)
                    {
                        value = convertedValue;
                    }
                }
            }
        }
        else if (cmisProperty instanceof CmisPropertyDecimal)
        {
            Collection<BigDecimal> convertedValue = ((CmisPropertyDecimal) cmisProperty).getValue();

            if (null != convertedValue)
            {
                if (1 == convertedValue.size())
                {
                    value = convertedValue.iterator().next();
                }
                else
                {
                    if (convertedValue.size() > 1)
            {
                        value = convertedValue;
                    }
                }
            }
        }
        else if (cmisProperty instanceof CmisPropertyHtml)
        {
            Collection<CmisPropertyHtml.Value> convertedValue = ((CmisPropertyHtml) cmisProperty).getValue();

            if (null != convertedValue)
            {
                if (1 == convertedValue.size())
                {
                    value = convertedValue.iterator().next();
                }
                else
                {
                    if (convertedValue.size() > 1)
                    {
                        value = convertedValue;
                    }
                }
            }
        }
        else if (cmisProperty instanceof CmisPropertyId)
        {
            Collection<String> convertedValue = ((CmisPropertyId) cmisProperty).getValue();

            if (null != convertedValue)
            {
                if (1 == convertedValue.size())
                {
                    value = convertedValue.iterator().next();
                }
                else
                {
                    if (convertedValue.size() > 1)
            {
                        value = convertedValue;
                    }
                }
            }
        }
        else if (cmisProperty instanceof CmisPropertyInteger)
        {
            Collection<BigInteger> convertedValue = ((CmisPropertyInteger) cmisProperty).getValue();

            if (null != convertedValue)
            {
                if (1 == convertedValue.size())
            {
                    value = convertedValue.iterator().next();
                }
                else
                {
                    if (convertedValue.size() > 1)
                    {
                        value = convertedValue;
                    }
                }
            }
        }
        else if (cmisProperty instanceof CmisPropertyString)
        {
            Collection<String> convertedValue = ((CmisPropertyString) cmisProperty).getValue();

            if (null != convertedValue)
            {
                if (1 == convertedValue.size())
                {
                    value = convertedValue.iterator().next();
                }
                else
                {
                    if (convertedValue.size() > 1)
            {
                        value = convertedValue;
                    }
                }
            }
        }
        else if (cmisProperty instanceof CmisPropertyUri)
        {
            Collection<String> convertedValue = ((CmisPropertyUri) cmisProperty).getValue();

            if (null != convertedValue)
            {
                if (1 == convertedValue.size())
                {
                    value = convertedValue.iterator().next();
                }
                else
                {
                    if (convertedValue.size() > 1)
                    {
                        value = convertedValue;
                    }
                }
            }
        }
        else if (cmisProperty instanceof CmisPropertyXml)
        {
            Collection<CmisPropertyXml.Value> convertedValue = ((CmisPropertyXml) cmisProperty).getValue();

            if (null != convertedValue)
            {
                if (1 == convertedValue.size())
                {
                    value = convertedValue.iterator().next();
                }
                else
                {
                    if (convertedValue.size() > 1)
                    {
                        value = convertedValue;
                    }
                }
            }
        }

        return value;
    }

    /**
     * Converts <b>CmisPropertiesType</b> properties representation to rapidly accessible form
     * 
     * @param cmisProperties - <b>CmisPropertiesType</b> properties representation
     * @return <b>Map</b>&lt;<b>String</b>, <b>Serializable</b>&gt; properties representation
     * @throws <b>CmisException</b>
     */
    public Map<String, Object> getPropertiesMap(CmisPropertiesType cmisProperties) throws CmisException
    {
        Map<String, Object> properties = new HashMap<String, Object>();

        if (null == cmisProperties)
        {
            return properties;
        }

        for (CmisProperty cmisProperty : cmisProperties.getProperty())
        {
            if (null != cmisProperty)
            {
                String name = cmisProperty.getName();
                properties.put(name, getValue(cmisProperty));
            }
        }

        return properties;
    }

    /**
     * Sets and checks all properties' fields for specified node
     * 
     * @param nodeRef - <b>NodeRef</b> for node for those properties must be setted
     * @param properties - <b>CmisPropertiesType</b> instance that contains all the necessary properties' fields
     * @param updateOptional - <b>boolean</b> value. If <b>true</b> - optionally updatable properties will be updated and will be ignored in other case
     */
    public void setProperties(NodeRef nodeRef, CmisPropertiesType properties, PropertyFilter notUpdatablePropertiesFilter) throws CmisException
    {
        // TODO: WARINING!!! This is WRONG behavior!!! Each CMIS object type and each property MUST be described in appropriate CMIS manner
        if ((null == nodeRef) || (null == properties) || (null == properties.getProperty()))
        {
            return;
        }

        String typeId = getProperty(nodeRef, CMISDictionaryModel.PROP_OBJECT_TYPE_ID, null);
        boolean checkedOut = getProperty(nodeRef, CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT, false);
        CMISTypeDefinition cmisObjectType = cmisDictionaryService.findType(typeId);

        TypeDefinition nativeObjectType = dictionaryService.getType(nodeService.getType(nodeRef));

        if ((null == cmisObjectType) && (null == nativeObjectType))
        {
            throw cmisObjectsUtils.createCmisException(("Can't find type definition for current object with \"" + typeId + "\" type Id"), EnumServiceException.INVALID_ARGUMENT);
        }

        for (CmisProperty property : properties.getProperty())
        {
            String propertyName = (null != property) ? (property.getName()) : (null);
            if ((null != propertyName) && !notUpdatablePropertiesFilter.allow(propertyName))
            {
                Object value = getValue(property);
                QName alfrescoPropertyName = null;
                switch (checkProperty(nativeObjectType, cmisObjectType, propertyName, value, checkedOut))
                {
                case PROPERTY_CHECKED:
                    {
                    alfrescoPropertyName = cmisDictionaryService.findProperty(propertyName, cmisObjectType).getPropertyAccessor().getMappedProperty();
                    break;
                }
                case PROPERTY_NATIVE:
                {
                    alfrescoPropertyName = createQName(propertyName);
                    break;
                }
                case PROPERTY_NOT_UPDATABLE:
                {
                    continue;
                    }
                }
                nodeService.setProperty(nodeRef, alfrescoPropertyName, (Serializable) value);
            }
        }
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
     * Checks any CMIS property on constraints conforming
     * 
     * @param type - <b>CMISTypeDefinition</b> instance. This value must not be <b>null</b>
     * @param propertyName - <b>String</b> instance that represents name of the property
     * @param value - instance of a <b>Serializable</b> object that represents value of the property
     * @return <b>true</b> if property was checked and <b>false</b> if property can't be checked
     * @throws <b>CmisException</b> if some constraint is not satisfied
     */
    public PropertyCheckingStateEnum checkProperty(TypeDefinition nativeObjectType, CMISTypeDefinition cmisObjectType, String propertyName, Object value, boolean checkedOut)
            throws CmisException
    {
        CMISPropertyDefinition propertyDefinition = cmisDictionaryService.findProperty(propertyName, cmisObjectType);

        if ((null == propertyDefinition) || ((null != propertyDefinition) && (null == propertyDefinition.getPropertyAccessor().getMappedProperty())))
        {
            // TODO: WARINING!!! This is WRONG behavior!!! Each CMIS object type and each property MUST be described in appropriate CMIS manner
            QName qualifiedName = createQName(propertyName);
            Map<QName, PropertyDefinition> properties = (null != nativeObjectType) ? (nativeObjectType.getProperties()) : (null);
            if ((null == qualifiedName) || (null == properties) || properties.containsKey(qualifiedName))
            {
                return PropertyCheckingStateEnum.PROPERTY_NOT_UPDATABLE;
            }
            return PropertyCheckingStateEnum.PROPERTY_NATIVE;
        }

        boolean updatable = ((checkedOut) ? (CMISUpdatabilityEnum.READ_AND_WRITE_WHEN_CHECKED_OUT == propertyDefinition.getUpdatability())
                : (CMISUpdatabilityEnum.READ_AND_WRITE == propertyDefinition.getUpdatability()));

        if (updatable && propertyDefinition.isRequired() && (value == null))
        {
            throw cmisObjectsUtils.createCmisException((propertyName + " property required"), EnumServiceException.CONSTRAINT);
        }

        switch (propertyDefinition.getDataType())
        {
        case STRING:
        {
            checkStringProperty(propertyDefinition, propertyName, (String) value);
            break;
        }
        case INTEGER:
        case DECIMAL:
        {
            checkNumberProperty(propertyDefinition, propertyName, (Number) value);
            break;
        }
        }

        return PropertyCheckingStateEnum.PROPERTY_CHECKED;
    }

    private enum PropertyCheckingStateEnum
    {
        PROPERTY_CHECKED, PROPERTY_NATIVE, PROPERTY_NOT_UPDATABLE;
    }

    private void checkNumberProperty(CMISPropertyDefinition propertyDefinition, String propertyName, Number value)
    {
        // TODO: if max and min value properties will be added to CMISPropertyDefinition
    }

    private void checkStringProperty(CMISPropertyDefinition propertyDefinition, String propertyName, String value) throws CmisException
    {
        if (value != null && (propertyDefinition.getMaximumLength() > 0) && (value.length() > propertyDefinition.getMaximumLength()))
        {
            throw cmisObjectsUtils.createCmisException((propertyName + " property value is too long"), EnumServiceException.CONSTRAINT);
        }
    }

    /**
     * Get CMIS properties for object
     * 
     * @param nodeRef node reference
     * @param filter property filter
     * @return properties
     */
    public CmisPropertiesType getPropertiesType(String identifier, PropertyFilter filter) throws CmisException
    {
        Map<String, Serializable> properties;
        if (NodeRef.isNodeRef(identifier))
        {
            properties = cmisService.getProperties(new NodeRef(identifier));
        }
        else
        {
            properties = createBaseRelationshipProperties(new AssociationRef(identifier));
        }

        CmisPropertiesType result = new CmisPropertiesType();
        convertToCmisProperties(properties, filter, result);
        return result;
    }

    private Map<String, Serializable> createBaseRelationshipProperties(AssociationRef association)
    {
        Map<String, Serializable> result = new HashMap<String, Serializable>();
        result.put(CMISDictionaryModel.PROP_OBJECT_TYPE_ID, cmisDictionaryService.findTypeForClass(association.getTypeQName(), CMISScope.RELATIONSHIP).getTypeId());
        result.put(CMISDictionaryModel.PROP_OBJECT_ID, association.toString());
        result.put(BASE_TYPE_PROPERTY_NAME, CMISDictionaryModel.RELATIONSHIP_TYPE_ID.getId());
        result.put(CMISDictionaryModel.PROP_CREATED_BY, AuthenticationUtil.getFullyAuthenticatedUser());
        result.put(CMISDictionaryModel.PROP_CREATION_DATE, new Date());
        result.put(CMISDictionaryModel.PROP_SOURCE_ID, association.getSourceRef());
        result.put(CMISDictionaryModel.PROP_TARGET_ID, association.getTargetRef());
        return result;
    }

    private void convertToCmisProperties(Map<String, Serializable> properties, PropertyFilter filter, CmisPropertiesType cmisProperties) throws CmisException
    {
        String typeId = (String) properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID);
        CMISTypeDefinition type = cmisDictionaryService.findType(typeId);

        if (null == type)
        {
            throw cmisObjectsUtils.createCmisException(("Type with " + typeId + " typeId was not found"), EnumServiceException.RUNTIME);
        }

        for (String propertyName : properties.keySet())
        {
            CMISPropertyDefinition propertyTypeDef = cmisDictionaryService.findProperty(propertyName, type);
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
     * @param name - <b>String</b> value that represents CMIS property name
     * @param dataType - <b>CMISDataTypeEnum</b> value that specifies real type of the property
     * @param value - some instance of appropriate type or some <b>Collection</b> that contains several values of the type
     * @return appropriate <b>CmisProperty</b> instance
     */
    @SuppressWarnings("unchecked")
    public CmisProperty createProperty(String name, CMISDataTypeEnum dataType, Serializable value)
    {
        switch (dataType)
        {
        case BOOLEAN:
        {
            CmisPropertyBoolean property = new CmisPropertyBoolean();
            property.setName(name);

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
            property.setName(name);

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
            property.setName(name);

            if (value instanceof Collection)
            {
                for (Object propertyValue : (Collection<Object>) value)
                {
                    property.getValue().add(BigInteger.valueOf((Long) propertyValue));
                }
            }
            else
            {
                property.getValue().add(BigInteger.valueOf((Long) value));
            }

            return property;
        }
        case DATETIME:
        {
            CmisPropertyDateTime property = new CmisPropertyDateTime();
            property.setName(name);

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
            property.setName(name);

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
        case URI:
        {
            CmisPropertyUri property = new CmisPropertyUri();
            property.setName(name);

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
            property.setName(name);

            if (value instanceof Collection)
            {
                for (BigDecimal propertyValue : (Collection<BigDecimal>) value)
                {
                    property.getValue().add(propertyValue);
                }
            }
            else
            {
                property.getValue().add(BigDecimal.valueOf((Double) value));
            }

            return property;
        }
        case XML:
        {
            CmisPropertyXml property = new CmisPropertyXml();
            property.setName(name);

            if (value instanceof Collection)
            {
                for (CmisPropertyXml.Value propertyValue : (Collection<CmisPropertyXml.Value>) value)
                {
                    property.getValue().add(propertyValue);
                }
            }
            else
            {
                property.getValue().add((CmisPropertyXml.Value) value);
            }

            return property;
        }
        case HTML:
        {
            CmisPropertyHtml property = new CmisPropertyHtml();
            property.setName(name);

            if (value instanceof Collection)
            {
                for (CmisPropertyHtml.Value propertyValue : (Collection<CmisPropertyHtml.Value>) value)
                {
                    property.getValue().add(propertyValue);
                }
            }
            else
            {
                property.getValue().add((CmisPropertyHtml.Value) value);
            }

            return property;
        }
        default:
        {
            return null;
        }
        }
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
