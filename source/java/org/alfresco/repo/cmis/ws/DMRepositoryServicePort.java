/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.cmis.ws;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISCardinalityEnum;
import org.alfresco.cmis.CMISChoice;
import org.alfresco.cmis.CMISContentStreamAllowedEnum;
import org.alfresco.cmis.CMISDataTypeEnum;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISJoinEnum;
import org.alfresco.cmis.CMISPropertyDefinition;
import org.alfresco.cmis.CMISQueryEnum;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.CMISUpdatabilityEnum;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.service.descriptor.Descriptor;

/**
 * Port for repository service.
 * 
 * @author Dmitry Lazurkin
 */
@javax.jws.WebService(name = "RepositoryServicePort", serviceName = "RepositoryService", portName = "RepositoryServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200901", endpointInterface = "org.alfresco.repo.cmis.ws.RepositoryServicePort")
public class DMRepositoryServicePort extends DMAbstractServicePort implements RepositoryServicePort
{
    private static Map<CMISJoinEnum, EnumCapabilityJoin> joinEnumMapping;
    private static Map<CMISContentStreamAllowedEnum, EnumContentStreamAllowed> contentStreamAllowedEnumMapping;
    private static Map<CMISUpdatabilityEnum, EnumUpdatability> updatabilityEnumMapping;
    private static Map<CMISCardinalityEnum, EnumCardinality> cardinalityEnumMapping;
    private static Map<CMISDataTypeEnum, EnumPropertyType> propertyTypeEnumMapping;
    private static HashMap<CMISQueryEnum, EnumCapabilityQuery> queryTypeEnumMapping;

    // TODO: Hardcoded! should be reteived using standart mechanism
    private String repositoryUri = " http://localhost:8080/alfresco/cmis";

    static
    {
        joinEnumMapping = new HashMap<CMISJoinEnum, EnumCapabilityJoin>();
        joinEnumMapping.put(CMISJoinEnum.INNER_AND_OUTER_JOIN_SUPPORT, EnumCapabilityJoin.INNERANDOUTER);
        joinEnumMapping.put(CMISJoinEnum.INNER_JOIN_SUPPORT, EnumCapabilityJoin.INNERONLY);
        joinEnumMapping.put(CMISJoinEnum.NO_JOIN_SUPPORT, EnumCapabilityJoin.NONE);

        contentStreamAllowedEnumMapping = new HashMap<CMISContentStreamAllowedEnum, EnumContentStreamAllowed>();
        contentStreamAllowedEnumMapping.put(CMISContentStreamAllowedEnum.ALLOWED, EnumContentStreamAllowed.ALLOWED);
        contentStreamAllowedEnumMapping.put(CMISContentStreamAllowedEnum.NOT_ALLOWED, EnumContentStreamAllowed.NOTALLOWED);
        contentStreamAllowedEnumMapping.put(CMISContentStreamAllowedEnum.REQUIRED, EnumContentStreamAllowed.REQUIRED);

        updatabilityEnumMapping = new HashMap<CMISUpdatabilityEnum, EnumUpdatability>();
        updatabilityEnumMapping.put(CMISUpdatabilityEnum.READ_AND_WRITE, EnumUpdatability.READWRITE);
        updatabilityEnumMapping.put(CMISUpdatabilityEnum.READ_AND_WRITE_WHEN_CHECKED_OUT, EnumUpdatability.WHENCHECKEDOUT);
        updatabilityEnumMapping.put(CMISUpdatabilityEnum.READ_ONLY, EnumUpdatability.READONLY);

        cardinalityEnumMapping = new HashMap<CMISCardinalityEnum, EnumCardinality>();
        cardinalityEnumMapping.put(CMISCardinalityEnum.MULTI_VALUED, EnumCardinality.MULTI);
        cardinalityEnumMapping.put(CMISCardinalityEnum.SINGLE_VALUED, EnumCardinality.SINGLE);

        propertyTypeEnumMapping = new HashMap<CMISDataTypeEnum, EnumPropertyType>();
        propertyTypeEnumMapping.put(CMISDataTypeEnum.BOOLEAN, EnumPropertyType.BOOLEAN);
        propertyTypeEnumMapping.put(CMISDataTypeEnum.DATETIME, EnumPropertyType.DATETIME);
        propertyTypeEnumMapping.put(CMISDataTypeEnum.DECIMAL, EnumPropertyType.DECIMAL);
        propertyTypeEnumMapping.put(CMISDataTypeEnum.HTML, EnumPropertyType.HTML);
        propertyTypeEnumMapping.put(CMISDataTypeEnum.ID, EnumPropertyType.ID);
        propertyTypeEnumMapping.put(CMISDataTypeEnum.INTEGER, EnumPropertyType.INTEGER);
        propertyTypeEnumMapping.put(CMISDataTypeEnum.STRING, EnumPropertyType.STRING);
        propertyTypeEnumMapping.put(CMISDataTypeEnum.URI, EnumPropertyType.URI);
        propertyTypeEnumMapping.put(CMISDataTypeEnum.XML, EnumPropertyType.XML);

        queryTypeEnumMapping = new HashMap<CMISQueryEnum, EnumCapabilityQuery>();
        queryTypeEnumMapping.put(CMISQueryEnum.BOTH_COMBINED, EnumCapabilityQuery.BOTHCOMBINED);
        queryTypeEnumMapping.put(CMISQueryEnum.BOTH_SEPERATE, EnumCapabilityQuery.BOTHSEPARATE);
        queryTypeEnumMapping.put(CMISQueryEnum.FULLTEXT_ONLY, EnumCapabilityQuery.FULLTEXTONLY);
        queryTypeEnumMapping.put(CMISQueryEnum.METADATA_ONLY, EnumCapabilityQuery.METADATAONLY);
        queryTypeEnumMapping.put(CMISQueryEnum.NONE, EnumCapabilityQuery.NONE);
    }

    /**
     * Gets a list of available repositories for this CMIS service endpoint.
     * 
     * @return collection of CmisRepositoryEntryType (repositoryId - repository Id, repositoryName: repository name, repositoryURI: Repository URI)
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME)
     */
    public List<CmisRepositoryEntryType> getRepositories() throws CmisException
    {
        CmisRepositoryEntryType repositoryEntryType = new CmisRepositoryEntryType();
        Descriptor serverDescriptor = descriptorService.getCurrentRepositoryDescriptor();
        repositoryEntryType.setRepositoryId(serverDescriptor.getId());
        repositoryEntryType.setRepositoryName(serverDescriptor.getName());

        // TODO: Hardcoded! repositoryUri should be retrieved using standard mechanism
        repositoryEntryType.setRepositoryURI(repositoryUri);
        List<CmisRepositoryEntryType> result = new LinkedList<CmisRepositoryEntryType>();
        result.add(repositoryEntryType);
        return result;
    }

    /**
     * Gets information about the CMIS repository and the capabilities it supports.
     * 
     * @param parameters repositoryId: repository Id
     * @return CMIS repository Info
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME)
     */
    public CmisRepositoryInfoType getRepositoryInfo(String repositoryId) throws CmisException
    {
        checkRepositoryId(repositoryId);

        Descriptor serverDescriptor = descriptorService.getCurrentRepositoryDescriptor();
        CmisRepositoryInfoType repositoryInfoType = new CmisRepositoryInfoType();
        repositoryInfoType.setRepositoryId(serverDescriptor.getId());
        repositoryInfoType.setRepositoryName(serverDescriptor.getName());
        repositoryInfoType.setRepositoryRelationship("self");
        repositoryInfoType.setRepositoryDescription("");
        repositoryInfoType.setVendorName("Alfresco");
        repositoryInfoType.setProductName("Alfresco Repository (" + serverDescriptor.getEdition() + ")");
        repositoryInfoType.setProductVersion(serverDescriptor.getVersion());
        repositoryInfoType.setRootFolderId(propertiesUtil.getProperty(cmisService.getDefaultRootNodeRef(), CMISDictionaryModel.PROP_OBJECT_ID, (String) null));
        CmisRepositoryCapabilitiesType capabilities = new CmisRepositoryCapabilitiesType();
        capabilities.setCapabilityMultifiling(true);
        capabilities.setCapabilityUnfiling(false);
        capabilities.setCapabilityVersionSpecificFiling(false);
        capabilities.setCapabilityPWCUpdateable(true);
        capabilities.setCapabilityPWCSearchable(cmisQueryService.getPwcSearchable());
        capabilities.setCapabilityAllVersionsSearchable(cmisQueryService.getAllVersionsSearchable());
        capabilities.setCapabilityQuery(queryTypeEnumMapping.get(cmisQueryService.getQuerySupport()));
        capabilities.setCapabilityJoin(joinEnumMapping.get(cmisQueryService.getJoinSupport()));
        repositoryInfoType.setCapabilities(capabilities);
        repositoryInfoType.setCmisVersionSupported(cmisService.getCMISVersion());
        return repositoryInfoType;
    }

    /**
     * Create web service choice object from repository choice object
     * 
     * @param choice repository choice
     * @param propertyType type of property
     * @return web service choice
     */
    private JAXBElement<? extends CmisChoiceType> getCmisChoiceType(CMISChoice choice, CMISDataTypeEnum propertyType)
    {
        JAXBElement<? extends CmisChoiceType> result = null;

        switch (propertyType)
        {
        case BOOLEAN:
            CmisChoiceBooleanType choiceBooleanType = new CmisChoiceBooleanType();
            choiceBooleanType.setKey(choice.getName());
            choiceBooleanType.getValue().add(Boolean.parseBoolean(choice.getValue().toString()));
            result = cmisObjectFactory.createChoiceBoolean(choiceBooleanType);
            break;
        case DATETIME:
            CmisChoiceDateTimeType choiceDateTimeType = new CmisChoiceDateTimeType();
            choiceDateTimeType.setKey(choice.getName());
            choiceDateTimeType.getValue().add(propertiesUtil.convert((Date) choice.getValue()));
            result = cmisObjectFactory.createChoiceDateTime(choiceDateTimeType);
            break;
        case DECIMAL:
            CmisChoiceDecimalType choiceDecimalType = new CmisChoiceDecimalType();
            choiceDecimalType.setKey(choice.getName());
            choiceDecimalType.getValue().add(BigDecimal.valueOf(Double.parseDouble(choice.getValue().toString())));
            result = cmisObjectFactory.createChoiceDecimal(choiceDecimalType);
            break;
        case HTML:
            break;
        case ID:
            CmisChoiceIdType choiceIdType = new CmisChoiceIdType();
            choiceIdType.setKey(choice.getName());
            choiceIdType.getValue().add(choice.getValue().toString());
            result = cmisObjectFactory.createChoiceId(choiceIdType);
            break;
        case INTEGER:
            CmisChoiceIntegerType choiceIntegerType = new CmisChoiceIntegerType();
            choiceIntegerType.setKey(choice.getName());
            choiceIntegerType.getValue().add(BigInteger.valueOf(Integer.parseInt(choice.getValue().toString())));
            result = cmisObjectFactory.createChoiceInteger(choiceIntegerType);
            break;
        case STRING:
            CmisChoiceStringType choiceStringType = new CmisChoiceStringType();
            choiceStringType.setKey(choice.getName());
            choiceStringType.getValue().add(choice.getValue().toString());
            result = cmisObjectFactory.createChoiceString(choiceStringType);
            break;
        case URI:
            break;
        case XML:
            break;
        }

        return result;
    }

    /**
     * Add choices childrens to list of JAXBElements
     * 
     * @param propertyType type of property
     * @param choices repository choice object
     * @param cmisChoices web service choice object
     */
    private void addChoiceChildrens(CMISDataTypeEnum propertyType, Collection<CMISChoice> choices, List<JAXBElement<? extends CmisChoiceType>> cmisChoices)
    {
        for (CMISChoice choice : choices)
        {
            JAXBElement<? extends CmisChoiceType> cmisChoiceType = getCmisChoiceType(choice, propertyType);
            cmisChoices.add(cmisChoiceType);

            if (choice.getChildren().isEmpty() == false)
            {
                addChoiceChildrens(propertyType, choice.getChildren(), cmisChoiceType.getValue().getChoice());
            }
        }
    }

    /**
     * Add root choices to list of choices
     * 
     * @param propertyType type of property
     * @param choices repository choice object
     * @param cmisChoices web service choice object
     */
    private void addChoices(CMISDataTypeEnum propertyType, Collection<CMISChoice> choices, List<CmisChoiceType> cmisChoices)
    {
        for (CMISChoice choice : choices)
        {
            JAXBElement<? extends CmisChoiceType> cmisChoiceType = getCmisChoiceType(choice, propertyType);
            cmisChoices.add(cmisChoiceType.getValue());

            if (choice.getChildren().isEmpty() == false)
            {
                addChoiceChildrens(propertyType, choice.getChildren(), cmisChoiceType.getValue().getChoice());
            }
        }
    }

    /**
     * Add property definitions to list of definitions
     * 
     * @param propertyDefinition repository property definition
     * @param wsPropertyDefs web service property definition
     */
    private void addPropertyDefs(CMISTypeDefinition typeDefinition, CMISPropertyDefinition propertyDefinition, List<CmisPropertyDefinitionType> wsPropertyDefs)
            throws CmisException
    {
        CmisPropertyDefinitionType wsPropertyDef = createPropertyDefinitionType(propertyDefinition.getDataType());
        wsPropertyDef.setId(propertyDefinition.getPropertyId().getId());
        wsPropertyDef.setDisplayName(propertyDefinition.getDisplayName());
        wsPropertyDef.setDescription(propertyDefinition.getDescription());
        wsPropertyDef.setPropertyType(propertyTypeEnumMapping.get(propertyDefinition.getDataType()));
        wsPropertyDef.setCardinality(cardinalityEnumMapping.get(propertyDefinition.getCardinality()));
        wsPropertyDef.setUpdatability(updatabilityEnumMapping.get(propertyDefinition.getUpdatability()));
        wsPropertyDef.setInherited(!typeDefinition.getOwnedPropertyDefinitions().containsKey(propertyDefinition.getPropertyId()));
        wsPropertyDef.setRequired(propertyDefinition.isRequired());
        wsPropertyDef.setQueryable(propertyDefinition.isQueryable());
        wsPropertyDef.setOrderable(propertyDefinition.isOrderable());
        addChoices(propertyDefinition.getDataType(), propertyDefinition.getChoices(), wsPropertyDef.getChoice());
        wsPropertyDef.setOpenChoice(propertyDefinition.isOpenChoice());

        wsPropertyDefs.add(wsPropertyDef);
    }

    private CmisPropertyDefinitionType createPropertyDefinitionType(CMISDataTypeEnum type) throws CmisException
    {
        switch (type)
        {
        case BOOLEAN:
        {
            return new CmisPropertyBooleanDefinitionType();
        }
        case DATETIME:
        {
            return new CmisPropertyDateTimeDefinitionType();
        }
        case DECIMAL:
        {
            return new CmisPropertyDecimalDefinitionType();
        }
        case HTML:
        {
            return new CmisPropertyHtmlDefinitionType();
        }
        case ID:
        {
            return new CmisPropertyIdDefinitionType();
        }
        case INTEGER:
        {
            return new CmisPropertyIntegerDefinitionType();
        }
        case STRING:
        {
            return new CmisPropertyStringDefinitionType();
        }
        case URI:
        {
            return new CmisPropertyUriDefinitionType();
        }
        case XML:
        {
            return new CmisPropertyXmlDefinitionType();
        }
        default:
        {
            throw cmisObjectsUtils.createCmisException(type.getLabel(), EnumServiceException.OBJECT_NOT_FOUND);
        }
        }
    }

    /**
     * Set properties for web service type definition
     * 
     * @param cmisTypeDefinition web service type definition
     * @param typeDefinition repository type definition
     * @param includeProperties true if need property definitions for type definition
     */
    private void setCmisTypeDefinitionProperties(CmisTypeDefinitionType cmisTypeDefinition, CMISTypeDefinition typeDefinition, boolean includeProperties) throws CmisException
    {
        cmisTypeDefinition.setTypeId(typeDefinition.getTypeId().getId());
        cmisTypeDefinition.setQueryName(typeDefinition.getQueryName());
        cmisTypeDefinition.setDisplayName(typeDefinition.getDisplayName());
        cmisTypeDefinition.setBaseType(EnumBaseObjectType.fromValue(typeDefinition.getBaseType().getTypeId().getId()));
        cmisTypeDefinition.setParentId(typeDefinition.getParentType().getTypeId().getId());
        cmisTypeDefinition.setBaseTypeQueryName(typeDefinition.getBaseType().getQueryName());
        cmisTypeDefinition.setDescription(typeDefinition.getDescription());
        cmisTypeDefinition.setCreatable(typeDefinition.isCreatable());
        cmisTypeDefinition.setFileable(typeDefinition.isFileable());
        cmisTypeDefinition.setQueryable(typeDefinition.isQueryable());
        cmisTypeDefinition.setControllable(typeDefinition.isControllablePolicy());
        cmisTypeDefinition.setIncludedInSupertypeQuery(typeDefinition.isIncludeInSuperTypeQuery());

        if (includeProperties)
        {
            List<CmisPropertyDefinitionType> propertyDefs = cmisTypeDefinition.getPropertyDefinition();
            for (CMISPropertyDefinition cmisPropDef : typeDefinition.getPropertyDefinitions().values())
            {
                addPropertyDefs(typeDefinition, cmisPropDef, propertyDefs);
            }
        }
    }

    /**
     * Create web service type definition for typeId
     * 
     * @param typeId type id
     * @param includeProperties true if need property definitions for type definition
     * @return web service type definition
     * @throws CmisException if type id not found
     */
    private CmisTypeDefinitionType getCmisTypeDefinition(CMISTypeDefinition typeDef, boolean includeProperties) throws CmisException
    {
        if (typeDef.getParentType() == null)
        {
            return null;
        }

        if (typeDef == null)
        {
            throw cmisObjectsUtils.createCmisException("Type not found", EnumServiceException.OBJECT_NOT_FOUND);
        }

        JAXBElement<? extends CmisTypeDefinitionType> result = null;

        switch (typeDef.getTypeId().getScope())
        {
        case DOCUMENT:
            CmisTypeDocumentDefinitionType documentDefinitionType = new CmisTypeDocumentDefinitionType();
            setCmisTypeDefinitionProperties(documentDefinitionType, typeDef, includeProperties);
            documentDefinitionType.setVersionable(typeDef.isVersionable());
            documentDefinitionType.setContentStreamAllowed(contentStreamAllowedEnumMapping.get(typeDef.getContentStreamAllowed()));
            result = cmisObjectFactory.createDocumentType(documentDefinitionType);
            break;
        case FOLDER:
            CmisTypeFolderDefinitionType folderDefinitionType = new CmisTypeFolderDefinitionType();
            setCmisTypeDefinitionProperties(folderDefinitionType, typeDef, includeProperties);
            result = cmisObjectFactory.createFolderType(folderDefinitionType);
            break;
        case POLICY:
            CmisTypePolicyDefinitionType policyDefinitionType = new CmisTypePolicyDefinitionType();
            setCmisTypeDefinitionProperties(policyDefinitionType, typeDef, includeProperties);
            result = cmisObjectFactory.createPolicyType(policyDefinitionType);
            break;
        case RELATIONSHIP:
            CmisTypeRelationshipDefinitionType relationshipDefinitionType = new CmisTypeRelationshipDefinitionType();
            setCmisTypeDefinitionProperties(relationshipDefinitionType, typeDef, includeProperties);
            result = cmisObjectFactory.createRelationshipType(relationshipDefinitionType);
            break;
        case UNKNOWN:
            throw cmisObjectsUtils.createCmisException("Unknown CMIS Type", EnumServiceException.INVALID_ARGUMENT);
        }

        return result.getValue();
    }

    /**
     * Gets the list of all types in the repository.
     * 
     * @param parameters repositoryId: repository Id; typeId: type Id; returnPropertyDefinitions: false (default); maxItems: 0 = Repository-default number of items(Default);
     *        skipCount: 0 = start;
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME)
     */
    public void getTypes(String repositoryId, String typeId, Boolean returnPropertyDefinitions, BigInteger maxItems, BigInteger skipCount,
            Holder<List<CmisTypeDefinitionType>> type, Holder<Boolean> hasMoreItems) throws CmisException
    {
        checkRepositoryId(repositoryId);

        Collection<CMISTypeDefinition> typeDefs;
        if ((typeId == null) || typeId.equals(""))
        {
            typeDefs = cmisDictionaryService.getAllTypes();
        }
        else
        {
            CMISTypeDefinition typeDef = cmisDictionaryService.findType(typeId);
            typeDefs = typeDef.getSubTypes(true);
        }

        // skip
        Cursor cursor = createCursor(typeDefs.size(), skipCount, maxItems);
        Iterator<CMISTypeDefinition> iterTypeDefs = typeDefs.iterator();
        for (int i = 0; i < cursor.getStartRow(); i++)
        {
            iterTypeDefs.next();
        }

        boolean returnPropertyDefinitionsVal = returnPropertyDefinitions == null ? false : returnPropertyDefinitions.booleanValue();

        type.value = new LinkedList<CmisTypeDefinitionType>();
        for (int i = cursor.getStartRow(); i <= cursor.getEndRow(); i++)
        {
            CmisTypeDefinitionType element = getCmisTypeDefinition(iterTypeDefs.next(), returnPropertyDefinitionsVal);
            if (element != null)
            {
                type.value.add(element);
            }
        }

        hasMoreItems.value = ((skipCount == null) || (maxItems == null)) ? (false) : ((cursor.getEndRow() < typeDefs.size()));
    }

    /**
     * Gets the definition for specified object type
     * 
     * @param parameters repositoryId: repository Id; typeId: type Id;
     * @return CMIS type definition
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME)
     */
    public CmisTypeDefinitionType getTypeDefinition(String repositoryId, String typeId) throws CmisException
    {
        checkRepositoryId(repositoryId);
        CMISTypeDefinition typeDef = cmisDictionaryService.findType(typeId);
        return getCmisTypeDefinition(typeDef, true);
    }
}
