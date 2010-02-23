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

    private Object getValue(CmisProperty cmisProperty) throws CmisException
    {
        Object value = null;
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
                value = (convertedValue.size() > 0) ? (convertedValue) : (null);
            }
            else
            {
                value = convertedValue.iterator().next();
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
            throw cmisObjectsUtils.createCmisException("\"" + propertyName + "\" property is not Multi Valued", EnumServiceException.INVALID_ARGUMENT);
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
                String pdid = getPropertyName(cmisProperty);
                properties.put(pdid, getValue(cmisProperty));
            }
        }

        return properties;
    }

    /**
     * Sets and checks all properties' fields for specified node
     * 
     * @param nodeRef - <b>NodeRef</b> for node for those properties must be setted
     * @param properties - <b>CmisPropertiesType</b> instance that contains all the necessary properties' fields
     * @param ignoringPropertiesFilter - <b>PropertyFilter</b> instance. This filter determines which properties should be ignored and not setted without exception. If this
     *        parameter is <b>null</b> all properties will be processed in common flow
     */
    public void setProperties(NodeRef nodeRef, CmisPropertiesType properties, PropertyFilter ignoringPropertiesFilter) throws CmisException
    {
        // TODO: WARINING!!! This is WRONG behavior!!! Each CMIS object type and each property MUST be described in appropriate to CMIS manner
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
            String propertyName = getPropertyName(property);
            if ((null == propertyName) || ((null != ignoringPropertiesFilter) && ignoringPropertiesFilter.allow(propertyName)))
            {
                continue;
            }

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
                throw cmisObjectsUtils.createCmisException(("\"" + propertyName + "\" property is not updatable by repository for specified Object id"),
                        EnumServiceException.CONSTRAINT);
            }
            }
            nodeService.setProperty(nodeRef, alfrescoPropertyName, (Serializable) value);
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

        if (null == propertyDefinition)
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

        // [FIXED BUG] condition and first calculating value were swapped
        boolean updatable = ((CMISUpdatabilityEnum.READ_AND_WRITE_WHEN_CHECKED_OUT == propertyDefinition.getUpdatability()) ? (checkedOut)
                : (CMISUpdatabilityEnum.READ_AND_WRITE == propertyDefinition.getUpdatability()));

        if (!updatable)
        {
            return PropertyCheckingStateEnum.PROPERTY_NOT_UPDATABLE;
        }

        if (propertyDefinition.isRequired() && (null == value))
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

    public enum PropertyCheckingStateEnum
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
        if (!identifier.contains("|"))
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
        String typeId = properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID) != null ? properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID).toString() : null;
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
     * @param pdid - <b>String</b> value that represents CMIS property name
     * @param dataType - <b>CMISDataTypeEnum</b> value that specifies real type of the property
     * @param value - some instance of appropriate type or some <b>Collection</b> that contains several values of the type
     * @return appropriate <b>CmisProperty</b> instance
     */
    @SuppressWarnings("unchecked")
    public CmisProperty createProperty(String pdid, CMISDataTypeEnum dataType, Serializable value)
    {
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
                for (String propertyValue : (Collection<String>) value)
                {
                    property.getValue().add(propertyValue);
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
