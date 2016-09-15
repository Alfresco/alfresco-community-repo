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
package org.alfresco.opencmis.dictionary;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.opencmis.mapping.CMISMapping;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.dictionary.constraint.NumericRangeConstraint;
import org.alfresco.repo.dictionary.constraint.StringLengthConstraint;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.ISO9075;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.definitions.Choice;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractTypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ChoiceImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriDefinitionImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base class for type definition wrappers.
 * 
 * @author florian.mueller
 */
public abstract class AbstractTypeDefinitionWrapper implements TypeDefinitionWrapper, Serializable
{
    private static final long serialVersionUID = 1L;
    private Log logger = LogFactory.getLog(AbstractTypeDefinitionWrapper.class);
    
    protected AbstractTypeDefinition typeDef;
    protected AbstractTypeDefinition typeDefInclProperties;

    protected TypeDefinitionWrapper parent;
//    protected List<TypeDefinitionWrapper> children;

    private String tenantId;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    
    @Override
    public String getTenantId()
    {
		return tenantId;
	}

	protected QName alfrescoName = null;
    protected QName alfrescoClass = null;
    protected Map<Action, CMISActionEvaluator> actionEvaluators;

    protected Map<String, PropertyDefinitionWrapper> propertiesById = new HashMap<String, PropertyDefinitionWrapper>();
    protected Map<String, PropertyDefinitionWrapper> propertiesByQueryName = new HashMap<String, PropertyDefinitionWrapper>();
    protected Map<QName, PropertyDefinitionWrapper> propertiesByQName = new HashMap<QName, PropertyDefinitionWrapper>();

    // interface

    public TypeDefinition getTypeDefinition(boolean includePropertyDefinitions)
    {
        lock.readLock().lock();
        try
        {
            if (includePropertyDefinitions)
            {
                return typeDefInclProperties;
            }
            else
            {
                return typeDef;
            }
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    protected void setTypeDefinition(AbstractTypeDefinition typeDef, AbstractTypeDefinition typeDefInclProperties)
    {
        this.typeDef = typeDef;
        this.typeDefInclProperties = typeDefInclProperties;
    }

    @Override
    public String getTypeId()
    {
        return typeDef.getId();
    }

    @Override
    public BaseTypeId getBaseTypeId()
    {
        return typeDef.getBaseTypeId();
    }

    @Override
    public boolean isBaseType()
    {
        return typeDef.getId().equals(typeDef.getBaseTypeId().value());
    }

    @Override
    public QName getAlfrescoName()
    {
        return alfrescoName;
    }

    @Override
    public QName getAlfrescoClass()
    {
        return alfrescoClass;
    }

    @Override
    public TypeDefinitionWrapper getParent()
    {
        return parent;
    }

//    @Override
//    public List<TypeDefinitionWrapper> getChildren()
//    {
//        return children;
//    }

    @Override
    public Map<Action, CMISActionEvaluator> getActionEvaluators()
    {
        return actionEvaluators;
    }

    @Override
    public Collection<PropertyDefinitionWrapper> getProperties()
    {
        return propertiesById.values();
    }

    @Override
    public PropertyDefinitionWrapper getPropertyById(String propertyId)
    {
        return propertiesById.get(propertyId);
    }

    @Override
    public PropertyDefinitionWrapper getPropertyByQueryName(String queryName)
    {
        return propertiesByQueryName.get(queryName);
    }

    @Override
    public PropertyDefinitionWrapper getPropertyByQName(QName name)
    {
        return propertiesByQName.get(name);
    }

    @Override
    public void updateDefinition(DictionaryService dictionaryService)
    {
        String name = null;
        String description = null;
        ClassDefinition definition = dictionaryService.getClass(alfrescoName);

        if (definition != null)
        {
            name = definition.getTitle(dictionaryService);
            description = definition.getDescription(dictionaryService);
        }
        setTypeDefDisplayName(name);
        setTypeDefDescription(description);
    }

    public void updateProperties(DictionaryService dictionaryService)
    {
        for (PropertyDefinitionWrapper propertyDefWrap : propertiesById.values())
        {
            updateProperty(dictionaryService, propertyDefWrap);
        }
    }
    
    public void updateProperty(DictionaryService dictionaryService, PropertyDefinitionWrapper propertyDefWrap)
    {
        if (propertyDefWrap != null && propertyDefWrap.getPropertyDefinition().getDisplayName() == null)
        {
            AbstractPropertyDefinition<?> property = (AbstractPropertyDefinition<?>) propertyDefWrap.getPropertyDefinition();
            org.alfresco.service.cmr.dictionary.PropertyDefinition propDef = dictionaryService
                    .getProperty(QName.createQName(property.getLocalNamespace(), property.getLocalName()));
            if (propDef != null)
            {
                String displayName = propDef.getTitle(dictionaryService);
                String description = propDef.getDescription(dictionaryService);
                property.setDisplayName(displayName == null ? property.getId() : displayName);
                property.setDescription(description == null ? property.getDisplayName() : description);
            }
        }
    }
    
    public void updateTypeDefInclProperties()
    {
        for (PropertyDefinition<?> property : typeDefInclProperties.getPropertyDefinitions().values())
        {
            if (property.getDisplayName() == null)
            {
                typeDefInclProperties.addPropertyDefinition(getPropertyById(property.getId()).getPropertyDefinition());
            }
        }
    }

    public void setTypeDefDisplayName(String name)
    {
        lock.writeLock().lock();
        try
        {
            typeDef.setDisplayName(name != null ? name : typeDef.getId());
            typeDefInclProperties.setDisplayName(name != null ? name : typeDef.getId());
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public void setTypeDefDescription(String desc)
    {
        lock.writeLock().lock();
        try
        {
            typeDef.setDescription(desc != null ? desc : typeDef.getId());
            typeDefInclProperties.setDescription(desc != null ? desc : typeDef.getId());
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
    
    // create

    public abstract List<TypeDefinitionWrapper> connectParentAndSubTypes(CMISMapping cmisMapping, CMISDictionaryRegistry registry,
            DictionaryService dictionaryService);

    public abstract void resolveInheritance(CMISMapping cmisMapping, CMISDictionaryRegistry registry,
            DictionaryService dictionaryService);

    public void assertComplete()
    {
        if (typeDef == null)
            throw new IllegalStateException("typeDef is not set");
        if (typeDefInclProperties == null)
            throw new IllegalStateException("typeDefInclProperties is not set");
        if (alfrescoName == null)
            throw new IllegalStateException("alfrescoName is not set");
        if (alfrescoClass == null)
            throw new IllegalStateException("alfrescoClass is not set");
        if (propertiesById == null)
            throw new IllegalStateException("propertiesById is not set");
        if (propertiesByQueryName == null)
            throw new IllegalStateException("propertiesByQueryName is not set");
        if (propertiesByQName == null)
            throw new IllegalStateException("propertiesByQName is not set");
//        if (propertiesById.size() == 0)
//            throw new IllegalStateException("property map empty");
        if (propertiesById.size() != propertiesByQueryName.size())
            throw new IllegalStateException("property map mismatch");
        if (propertiesById.size() != propertiesByQName.size())
            throw new IllegalStateException("property map mismatch");
    }

    /**
     * Adds all property definitions owned by that type.
     */
    protected void createOwningPropertyDefinitions(CMISMapping cmisMapping,
            PropertyAccessorMapping propertyAccessorMapping, PropertyLuceneBuilderMapping luceneBuilderMapping,
            DictionaryService dictionaryService, ClassDefinition cmisClassDef)
    {
        PropertyDefinition<?> propertyDefintion;

        for (org.alfresco.service.cmr.dictionary.PropertyDefinition alfrescoPropDef : cmisClassDef.getProperties()
                .values())
        {
            if (!isBaseType())
            {
                if (!alfrescoPropDef.getContainerClass().equals(cmisClassDef))
                {
                    continue;
                }
            }

            // compile property id
            String propertyId = cmisMapping.buildPrefixEncodedString(alfrescoPropDef.getName());

            if(propertyId.equals("cmis:secondaryObjectTypeIds") && cmisMapping.getCmisVersion() == CmisVersion.CMIS_1_0)
            {
            	continue;
            }

            // create property definition
            propertyDefintion = createPropertyDefinition(cmisMapping, propertyId, alfrescoPropDef.getName(),
                    dictionaryService, alfrescoPropDef, false);

            // if the datatype is not supported, the property defintion will be
            // null
            if (propertyDefintion != null)
            {
                CMISPropertyAccessor propertyAccessor = null;
                if (propertyAccessorMapping != null)
                {
                    propertyAccessor = propertyAccessorMapping.getPropertyAccessor(propertyId);
                    if (propertyAccessor == null)
                    {
                        propertyAccessor = propertyAccessorMapping.createDirectPropertyAccessor(propertyId,
                                alfrescoPropDef.getName());
                    }
                }

                CMISPropertyLuceneBuilder luceneBuilder = null;
                if (luceneBuilderMapping != null)
                {
                    luceneBuilder = luceneBuilderMapping.getPropertyLuceneBuilder(propertyId);
                    if (luceneBuilder == null)
                    {
                        luceneBuilder = luceneBuilderMapping.createDirectPropertyLuceneBuilder(alfrescoPropDef
                                .getName());
                    }
                }

                registerProperty(new BasePropertyDefintionWrapper(propertyDefintion, alfrescoPropDef.getName(), this,
                        propertyAccessor, luceneBuilder));
            }
        }
    }

    /**
     * Registers a property definition with this type
     */
    protected void registerProperty(PropertyDefinitionWrapper propDefWrapper)
    {
        if (propDefWrapper == null)
        {
            return;
        }

        if (propertiesById.containsKey(propDefWrapper.getPropertyId()))
        {
            throw new AlfrescoRuntimeException("Property defintion " + propDefWrapper.getPropertyId()
                    + " already exists on type " + typeDef.getId());
        }

        propertiesById.put(propDefWrapper.getPropertyId(), propDefWrapper);
        propertiesByQueryName.put(propDefWrapper.getPropertyDefinition().getQueryName(), propDefWrapper);
        propertiesByQName.put(propDefWrapper.getAlfrescoName(), propDefWrapper);
        typeDefInclProperties.addPropertyDefinition(propDefWrapper.getPropertyDefinition());
    }

    /**
     * Creates a property definition object.
     */
    protected PropertyDefinition<?> createPropertyDefinition(CMISMapping cmisMapping, String id,
            QName alfrescoPropName, DictionaryService dictionaryService, org.alfresco.service.cmr.dictionary.PropertyDefinition propDef, boolean inherited)
    {
        PropertyType datatype = cmisMapping.getDataType(propDef.getDataType());
        if (datatype == null)
        {
            return null;
        }

        AbstractPropertyDefinition<?> result = null;

        switch (datatype)
        {
        case BOOLEAN:
            result = new PropertyBooleanDefinitionImpl();
            break;
        case DATETIME:
            result = new PropertyDateTimeDefinitionImpl();
            break;
        case DECIMAL:
            result = new PropertyDecimalDefinitionImpl();
            break;
        case HTML:
            result = new PropertyHtmlDefinitionImpl();
            break;
        case ID:
            result = new PropertyIdDefinitionImpl();
            break;
        case INTEGER:
            result = new PropertyIntegerDefinitionImpl();
            break;
        case STRING:
            result = new PropertyStringDefinitionImpl();
            break;
        case URI:
            result = new PropertyUriDefinitionImpl();
            break;
        default:
            throw new RuntimeException("Unknown datatype! Spec change?");
        }

        if (id.equals(PropertyIds.OBJECT_TYPE_ID) || id.equals(PropertyIds.SOURCE_ID)
                || id.equals(PropertyIds.TARGET_ID))
        {
            // the CMIS spec requirement
            result.setUpdatability(Updatability.ONCREATE);
        } else
        {
            result.setUpdatability(propDef.isProtected() ? Updatability.READONLY : Updatability.READWRITE);
        }

        result.setId(id);
        result.setLocalName(alfrescoPropName.getLocalName());
        result.setLocalNamespace(alfrescoPropName.getNamespaceURI());
        result.setDisplayName(null);
        result.setDescription(null);
        result.setPropertyType(datatype);
        result.setCardinality(propDef.isMultiValued() ? Cardinality.MULTI : Cardinality.SINGLE);
        result.setIsInherited(inherited);
        result.setIsRequired(propDef.isMandatory());
        addDefaultValue(propDef.getDefaultValue(), result);

        // query and order
        result.setQueryName(ISO9075.encodeSQL(cmisMapping.buildPrefixEncodedString(alfrescoPropName)));
        result.setIsQueryable(propDef.isIndexed());
        result.setIsOrderable(false);

        if (result.isQueryable())
        {
            if (result.getCardinality() == Cardinality.SINGLE)
            {
                IndexTokenisationMode indexTokenisationMode = IndexTokenisationMode.TRUE;
                if (propDef.getIndexTokenisationMode() != null)
                {
                    indexTokenisationMode = propDef.getIndexTokenisationMode();
                }

                switch (indexTokenisationMode)
                {
                case BOTH:
                case FALSE:
                    result.setIsOrderable(true);
                    break;
                case TRUE:
                default:
                    if (propDef.getDataType().getName().equals(DataTypeDefinition.BOOLEAN)
                            || propDef.getDataType().getName().equals(DataTypeDefinition.DATE)
                            || propDef.getDataType().getName().equals(DataTypeDefinition.DATETIME)
                            || propDef.getDataType().getName().equals(DataTypeDefinition.DOUBLE)
                            || propDef.getDataType().getName().equals(DataTypeDefinition.FLOAT)
                            || propDef.getDataType().getName().equals(DataTypeDefinition.INT)
                            || propDef.getDataType().getName().equals(DataTypeDefinition.LONG)
                            || propDef.getDataType().getName().equals(DataTypeDefinition.PATH)
                            )
                    {
                        result.setIsOrderable(true);
                    } 
                }
            }
        }

        // MNT-9089 fix, set min/max values for numeric properties
        // MNT-11304 fix, use default boundaries only for numeric types
        if (result instanceof PropertyIntegerDefinitionImpl)
        {
            if (propDef.getDataType().getName().equals(DataTypeDefinition.INT))
            {
                ((PropertyIntegerDefinitionImpl) result).setMinValue(BigInteger.valueOf(Integer.MIN_VALUE));
                ((PropertyIntegerDefinitionImpl) result).setMaxValue(BigInteger.valueOf(Integer.MAX_VALUE));
            }
            if (propDef.getDataType().getName().equals(DataTypeDefinition.LONG))
            {
                ((PropertyIntegerDefinitionImpl) result).setMinValue(BigInteger.valueOf(Long.MIN_VALUE));
                ((PropertyIntegerDefinitionImpl) result).setMaxValue(BigInteger.valueOf(Long.MAX_VALUE));
            }
        }
        // end MNT-9089
        
        // constraints and choices
        for (ConstraintDefinition constraintDef : propDef.getConstraints())
        {
            Constraint constraint = constraintDef.getConstraint();
            if (constraint instanceof ListOfValuesConstraint)
            {
                addChoiceList((ListOfValuesConstraint) constraint, result);
            }

            if ((constraint instanceof StringLengthConstraint) && (result instanceof PropertyStringDefinitionImpl))
            {
                StringLengthConstraint slc = (StringLengthConstraint) constraint;
                ((PropertyStringDefinitionImpl) result).setMaxLength(BigInteger.valueOf(slc.getMaxLength()));
            }

            if (constraint instanceof NumericRangeConstraint)
            {
                NumericRangeConstraint nrc = (NumericRangeConstraint) constraint;
                if (result instanceof PropertyIntegerDefinitionImpl)
                {
                    ((PropertyIntegerDefinitionImpl) result)
                            .setMinValue(BigInteger.valueOf(((Double) nrc.getMinValue()).longValue()));
                    ((PropertyIntegerDefinitionImpl) result)
                            .setMaxValue(BigInteger.valueOf(((Double) nrc.getMaxValue()).longValue()));
                }
                if (result instanceof PropertyDecimalDefinitionImpl)
                {
                    ((PropertyDecimalDefinitionImpl) result).setMinValue(BigDecimal.valueOf(nrc.getMinValue()));
                    ((PropertyDecimalDefinitionImpl) result).setMaxValue(BigDecimal.valueOf(nrc.getMaxValue()));
                }
            }
        }

        return result;
    }

    protected void createActionEvaluators(PropertyAccessorMapping propertyAccessorMapping, BaseTypeId baseTypeId)
    {
        if (propertyAccessorMapping != null)
        {
            actionEvaluators = propertyAccessorMapping.getActionEvaluators(baseTypeId);
        } else
        {
            actionEvaluators = Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T convertValueFromString(String value, PropertyType datatype)
    {
        if (value == null)
        {
            return null;
        }

        try
        {
            switch (datatype)
            {
            case BOOLEAN:
                return (T) Boolean.valueOf(value);
            case DATETIME:
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(ISO8601DateFormat.parse(value));
                return (T) cal;
            case DECIMAL:
                return (T) new BigDecimal(value);
            case HTML:
                return (T) value;
            case ID:
                return (T) value;
            case INTEGER:
                return (T) new BigInteger(value);
            case STRING:
                return (T) value;
            case URI:
                return (T) value;
            default: ;
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to convert value " + value + " to " + datatype, e);
            return null;
        }
        
        throw new RuntimeException("Unknown datatype! Spec change?");
    }

    /**
     * Adds the default value to a property definition.
     */
    private void addDefaultValue(String value, PropertyDefinition<?> propDef)
    {
        if (value == null)
        {
            return;
        }

        if (propDef instanceof PropertyBooleanDefinitionImpl)
        {
            PropertyBooleanDefinitionImpl propDefImpl = (PropertyBooleanDefinitionImpl) propDef;
            propDefImpl.setDefaultValue(Collections.singletonList((Boolean) convertValueFromString(value,
                    PropertyType.BOOLEAN)));
        } else if (propDef instanceof PropertyDateTimeDefinitionImpl)
        {
            PropertyDateTimeDefinitionImpl propDefImpl = (PropertyDateTimeDefinitionImpl) propDef;
            propDefImpl.setDefaultValue(Collections.singletonList((GregorianCalendar) convertValueFromString(value,
                    PropertyType.DATETIME)));
        } else if (propDef instanceof PropertyDecimalDefinitionImpl)
        {
            PropertyDecimalDefinitionImpl propDefImpl = (PropertyDecimalDefinitionImpl) propDef;
            propDefImpl.setDefaultValue(Collections.singletonList((BigDecimal) convertValueFromString(value,
                    PropertyType.DECIMAL)));
        } else if (propDef instanceof PropertyHtmlDefinitionImpl)
        {
            PropertyHtmlDefinitionImpl propDefImpl = (PropertyHtmlDefinitionImpl) propDef;
            propDefImpl.setDefaultValue(Collections.singletonList((String) convertValueFromString(value,
                    PropertyType.HTML)));
        } else if (propDef instanceof PropertyIdDefinitionImpl)
        {
            PropertyIdDefinitionImpl propDefImpl = (PropertyIdDefinitionImpl) propDef;
            propDefImpl.setDefaultValue(Collections.singletonList((String) convertValueFromString(value,
                    PropertyType.ID)));
        } else if (propDef instanceof PropertyIntegerDefinitionImpl)
        {
            PropertyIntegerDefinitionImpl propDefImpl = (PropertyIntegerDefinitionImpl) propDef;
            propDefImpl.setDefaultValue(Collections.singletonList((BigInteger) convertValueFromString(value,
                    PropertyType.INTEGER)));
        } else if (propDef instanceof PropertyStringDefinitionImpl)
        {
            PropertyStringDefinitionImpl propDefImpl = (PropertyStringDefinitionImpl) propDef;
            propDefImpl.setDefaultValue(Collections.singletonList((String) convertValueFromString(value,
                    PropertyType.STRING)));
        } else if (propDef instanceof PropertyUriDefinitionImpl)
        {
            PropertyUriDefinitionImpl propDefImpl = (PropertyUriDefinitionImpl) propDef;
            propDefImpl.setDefaultValue(Collections.singletonList((String) convertValueFromString(value,
                    PropertyType.URI)));
        }
    }

    /**
     * Adds choices to the property defintion.
     */
    private void addChoiceList(ListOfValuesConstraint lovc, PropertyDefinition<?> propDef)
    {
        if (propDef instanceof PropertyBooleanDefinitionImpl)
        {
            PropertyBooleanDefinitionImpl propDefImpl = (PropertyBooleanDefinitionImpl) propDef;
            propDefImpl.setIsOpenChoice(false);

            List<Choice<Boolean>> choiceList = new ArrayList<Choice<Boolean>>();
            for (String allowed : lovc.getAllowedValues())
            {
                ChoiceImpl<Boolean> choice = new ChoiceImpl<Boolean>();
                choice.setDisplayName(allowed);
                choice.setValue(Collections.singletonList((Boolean) convertValueFromString(allowed,
                        PropertyType.BOOLEAN)));

                choiceList.add(choice);
            }

            propDefImpl.setChoices(choiceList);
        } else if (propDef instanceof PropertyDateTimeDefinitionImpl)
        {
            PropertyDateTimeDefinitionImpl propDefImpl = (PropertyDateTimeDefinitionImpl) propDef;
            propDefImpl.setIsOpenChoice(false);

            List<Choice<GregorianCalendar>> choiceList = new ArrayList<Choice<GregorianCalendar>>();
            for (String allowed : lovc.getAllowedValues())
            {
                ChoiceImpl<GregorianCalendar> choice = new ChoiceImpl<GregorianCalendar>();
                choice.setDisplayName(allowed);
                choice.setValue(Collections.singletonList((GregorianCalendar) convertValueFromString(allowed,
                        PropertyType.DATETIME)));

                choiceList.add(choice);
            }

            propDefImpl.setChoices(choiceList);
        } else if (propDef instanceof PropertyDecimalDefinitionImpl)
        {
            PropertyDecimalDefinitionImpl propDefImpl = (PropertyDecimalDefinitionImpl) propDef;
            propDefImpl.setIsOpenChoice(false);

            List<Choice<BigDecimal>> choiceList = new ArrayList<Choice<BigDecimal>>();
            for (String allowed : lovc.getAllowedValues())
            {
                ChoiceImpl<BigDecimal> choice = new ChoiceImpl<BigDecimal>();
                choice.setDisplayName(allowed);
                choice.setValue(Collections.singletonList((BigDecimal) convertValueFromString(allowed,
                        PropertyType.DECIMAL)));

                choiceList.add(choice);
            }

            propDefImpl.setChoices(choiceList);
        } else if (propDef instanceof PropertyHtmlDefinitionImpl)
        {
            PropertyHtmlDefinitionImpl propDefImpl = (PropertyHtmlDefinitionImpl) propDef;
            propDefImpl.setIsOpenChoice(false);

            List<Choice<String>> choiceList = new ArrayList<Choice<String>>();
            for (String allowed : lovc.getAllowedValues())
            {
                ChoiceImpl<String> choice = new ChoiceImpl<String>();
                choice.setDisplayName(allowed);
                choice.setValue(Collections.singletonList((String) convertValueFromString(allowed, PropertyType.HTML)));

                choiceList.add(choice);
            }

            propDefImpl.setChoices(choiceList);
        } else if (propDef instanceof PropertyIdDefinitionImpl)
        {
            PropertyIdDefinitionImpl propDefImpl = (PropertyIdDefinitionImpl) propDef;
            propDefImpl.setIsOpenChoice(false);

            List<Choice<String>> choiceList = new ArrayList<Choice<String>>();
            for (String allowed : lovc.getAllowedValues())
            {
                ChoiceImpl<String> choice = new ChoiceImpl<String>();
                choice.setDisplayName(allowed);
                choice.setValue(Collections.singletonList((String) convertValueFromString(allowed, PropertyType.ID)));

                choiceList.add(choice);
            }

            propDefImpl.setChoices(choiceList);
        } else if (propDef instanceof PropertyIntegerDefinitionImpl)
        {
            PropertyIntegerDefinitionImpl propDefImpl = (PropertyIntegerDefinitionImpl) propDef;
            propDefImpl.setIsOpenChoice(false);

            List<Choice<BigInteger>> choiceList = new ArrayList<Choice<BigInteger>>();
            for (String allowed : lovc.getAllowedValues())
            {
                ChoiceImpl<BigInteger> choice = new ChoiceImpl<BigInteger>();
                choice.setDisplayName(allowed);
                choice.setValue(Collections.singletonList((BigInteger) convertValueFromString(allowed,
                        PropertyType.INTEGER)));

                choiceList.add(choice);
            }

            propDefImpl.setChoices(choiceList);
        } else if (propDef instanceof PropertyStringDefinitionImpl)
        {
            PropertyStringDefinitionImpl propDefImpl = (PropertyStringDefinitionImpl) propDef;
            propDefImpl.setIsOpenChoice(false);

            List<Choice<String>> choiceList = new ArrayList<Choice<String>>();
            for (String allowed : lovc.getAllowedValues())
            {
                ChoiceImpl<String> choice = new ChoiceImpl<String>();
                choice.setDisplayName(allowed);
                choice.setValue(Collections
                        .singletonList((String) convertValueFromString(allowed, PropertyType.STRING)));

                choiceList.add(choice);
            }

            propDefImpl.setChoices(choiceList);
        } else if (propDef instanceof PropertyUriDefinitionImpl)
        {
            PropertyUriDefinitionImpl propDefImpl = (PropertyUriDefinitionImpl) propDef;
            propDefImpl.setIsOpenChoice(false);

            List<Choice<String>> choiceList = new ArrayList<Choice<String>>();
            for (String allowed : lovc.getAllowedValues())
            {
                ChoiceImpl<String> choice = new ChoiceImpl<String>();
                choice.setDisplayName(allowed);
                choice.setValue(Collections.singletonList((String) convertValueFromString(allowed, PropertyType.URI)));

                choiceList.add(choice);
            }

            propDefImpl.setChoices(choiceList);
        }
    }
}
