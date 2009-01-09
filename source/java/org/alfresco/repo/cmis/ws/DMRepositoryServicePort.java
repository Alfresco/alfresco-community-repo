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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.alfresco.cmis.CMISCardinalityEnum;
import org.alfresco.cmis.CMISContentStreamAllowedEnum;
import org.alfresco.cmis.CMISFullTextSearchEnum;
import org.alfresco.cmis.CMISJoinEnum;
import org.alfresco.cmis.CMISPropertyTypeEnum;
import org.alfresco.cmis.CMISUpdatabilityEnum;
import org.alfresco.cmis.dictionary.CMISChoice;
import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.cmis.dictionary.CMISPropertyDefinition;
import org.alfresco.cmis.dictionary.CMISTypeDefinition;
import org.alfresco.cmis.dictionary.CMISTypeId;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.service.descriptor.Descriptor;

/**
 * Port for repository service.
 *
 * @author Dmitry Lazurkin
 */
@javax.jws.WebService(name = "RepositoryServicePort", serviceName = "RepositoryService", portName = "RepositoryServicePort", targetNamespace = "http://www.cmis.org/ns/1.0", endpointInterface = "org.alfresco.repo.cmis.ws.RepositoryServicePort")
public class DMRepositoryServicePort extends DMAbstractServicePort implements RepositoryServicePort
{
    private static Map<CMISFullTextSearchEnum, EnumCapabilityFullText> fulltextEnumMapping;
    private static Map<CMISJoinEnum, EnumCapabilityJoin> joinEnumMapping;
    private static Map<CMISContentStreamAllowedEnum, EnumContentStreamAllowed> contentStreamAllowedEnumMapping;
    private static Map<CMISUpdatabilityEnum, EnumUpdateability> updatabilityEnumMapping;
    private static Map<CMISCardinalityEnum, EnumCardinality> cardinalityEnumMapping;
    private static Map<CMISPropertyTypeEnum, EnumPropertyType> propertyTypeEnumMapping;

    static
    {
        fulltextEnumMapping = new HashMap<CMISFullTextSearchEnum, EnumCapabilityFullText>();
        fulltextEnumMapping.put(CMISFullTextSearchEnum.NO_FULL_TEXT, EnumCapabilityFullText.NONE);
        fulltextEnumMapping.put(CMISFullTextSearchEnum.FULL_TEXT_ONLY, EnumCapabilityFullText.FULLTEXTONLY);
        fulltextEnumMapping.put(CMISFullTextSearchEnum.FULL_TEXT_AND_STRUCTURED, EnumCapabilityFullText.FULLTEXTANDSTRUCTURED);

        joinEnumMapping = new HashMap<CMISJoinEnum, EnumCapabilityJoin>();
        joinEnumMapping.put(CMISJoinEnum.INNER_AND_OUTER_JOIN_SUPPORT, EnumCapabilityJoin.INNERANDOUTER);
        joinEnumMapping.put(CMISJoinEnum.INNER_JOIN_SUPPORT, EnumCapabilityJoin.INNERONLY);
        joinEnumMapping.put(CMISJoinEnum.NO_JOIN_SUPPORT, EnumCapabilityJoin.NOJOIN);

        contentStreamAllowedEnumMapping = new HashMap<CMISContentStreamAllowedEnum, EnumContentStreamAllowed>();
        contentStreamAllowedEnumMapping.put(CMISContentStreamAllowedEnum.ALLOWED, EnumContentStreamAllowed.ALLOWED);
        contentStreamAllowedEnumMapping.put(CMISContentStreamAllowedEnum.NOT_ALLOWED, EnumContentStreamAllowed.NOTALLOWED);
        contentStreamAllowedEnumMapping.put(CMISContentStreamAllowedEnum.REQUIRED, EnumContentStreamAllowed.REQUIRED);

        updatabilityEnumMapping = new HashMap<CMISUpdatabilityEnum, EnumUpdateability>();
        updatabilityEnumMapping.put(CMISUpdatabilityEnum.READ_AND_WRITE, EnumUpdateability.READWRITE);
        updatabilityEnumMapping.put(CMISUpdatabilityEnum.READ_AND_WRITE_WHEN_CHECKED_OUT, EnumUpdateability.WHENCHECKEDOUT);
        updatabilityEnumMapping.put(CMISUpdatabilityEnum.READ_ONLY, EnumUpdateability.READONLY);

        cardinalityEnumMapping = new HashMap<CMISCardinalityEnum, EnumCardinality>();
        cardinalityEnumMapping.put(CMISCardinalityEnum.MULTI_VALUED, EnumCardinality.MULTI);
        cardinalityEnumMapping.put(CMISCardinalityEnum.SINGLE_VALUED, EnumCardinality.SINGLE);

        propertyTypeEnumMapping = new HashMap<CMISPropertyTypeEnum, EnumPropertyType>();
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.BOOLEAN, EnumPropertyType.BOOLEAN);
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.DATETIME, EnumPropertyType.DATETIME);
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.DECIMAL, EnumPropertyType.DECIMAL);
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.HTML, EnumPropertyType.HTML);
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.ID, EnumPropertyType.ID);
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.INTEGER, EnumPropertyType.INTEGER);
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.STRING, EnumPropertyType.STRING);
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.URI, EnumPropertyType.URI);
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.XML, EnumPropertyType.XML);
    }

    /**
     * Gets a list of available repositories for this CMIS service endpoint.
     * 
     * @return collection of CmisRepositoryEntryType (repositoryId - repository Id, repositoryName: repository name, repositoryURI: Repository URI)
     * @throws RuntimeException
     * @throws InvalidArgumentException
     * @throws OperationNotSupportedException
     * @throws UpdateConflictException
     * @throws PermissionDeniedException
     */
    public List<CmisRepositoryEntryType> getRepositories()
        throws RuntimeException, InvalidArgumentException, OperationNotSupportedException, UpdateConflictException, PermissionDeniedException
    {
        CmisRepositoryEntryType repositoryEntryType = new CmisRepositoryEntryType();
        Descriptor serverDescriptor = descriptorService.getServerDescriptor();
        repositoryEntryType.setRepositoryID(serverDescriptor.getId());
        repositoryEntryType.setRepositoryName(serverDescriptor.getName());
        return Collections.singletonList(repositoryEntryType);
    }

    /**
     * Gets information about the CMIS repository and the capabilities it supports.
     * 
     * @param parameters repositoryId: repository Id
     * @return CMIS repository Info
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public CmisRepositoryInfoType getRepositoryInfo(GetRepositoryInfo parameters)
        throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        Descriptor serverDescriptor = descriptorService.getServerDescriptor();
        if (serverDescriptor.getId().equals(parameters.getRepositoryId()) == false)
        {
            throw new InvalidArgumentException("Invalid repository id");
        }

        CmisRepositoryInfoType repositoryInfoType = new CmisRepositoryInfoType();
        repositoryInfoType.setRepositoryId(serverDescriptor.getId());
        repositoryInfoType.setRepositoryName(serverDescriptor.getName());
        repositoryInfoType.setRepositoryRelationship("self");
        repositoryInfoType.setRepositoryDescription("");
        repositoryInfoType.setRootFolderId((String) cmisPropertyService.getProperty(cmisService.getDefaultRootNodeRef(), CMISMapping.PROP_OBJECT_ID));
        repositoryInfoType.setVendorName("Alfresco");
        repositoryInfoType.setProductName("Alfresco Repository (" + serverDescriptor.getEdition() + ")");
        repositoryInfoType.setProductVersion(serverDescriptor.getVersion());

        CmisRepositoryCapabilitiesType capabilities = new CmisRepositoryCapabilitiesType();
        capabilities.setCapabilityMultifiling(true);
        capabilities.setCapabilityUnfiling(false);
        capabilities.setCapabilityVersionSpecificFiling(false);
        capabilities.setCapabilityPWCUpdateable(true);
        capabilities.setCapabilityAllVersionsSearchable(cmisQueryService.getAllVersionsSearchable());
        capabilities.setCapabilityJoin(joinEnumMapping.get(cmisQueryService.getJoinSupport()));
        capabilities.setCapabilityFullText(fulltextEnumMapping.get(cmisQueryService.getFullTextSearchSupport()));
        repositoryInfoType.setCapabilities(capabilities);

        repositoryInfoType.setCmisVersionsSupported(cmisService.getCMISVersion());
        return repositoryInfoType;
    }

    /**
     * Create web service choice object from repository choice object
     *
     * @param choice repository choice
     * @param propertyType type of property
     * @return web service choice
     */
    private JAXBElement<? extends CmisChoiceType> getCmisChoiceType(CMISChoice choice, CMISPropertyTypeEnum propertyType)
    {
        JAXBElement<? extends CmisChoiceType> result = null;

        switch (propertyType)
        {
            case BOOLEAN:
                CmisChoiceBooleanType choiceBooleanType = new CmisChoiceBooleanType();
                choiceBooleanType.setIndex(BigInteger.valueOf(choice.getIndex()));
                choiceBooleanType.setKey(choice.getName());
                choiceBooleanType.setValue((Boolean) choice.getValue());
                result = cmisObjectFactory.createChoiceBoolean(choiceBooleanType);
                break;
            case DATETIME:
                CmisChoiceDateTimeType choiceDateTimeType = new CmisChoiceDateTimeType();
                choiceDateTimeType.setIndex(BigInteger.valueOf(choice.getIndex()));
                choiceDateTimeType.setKey(choice.getName());
                choiceDateTimeType.setValue(convert((Date) choice.getValue()));
                result = cmisObjectFactory.createChoiceDateTime(choiceDateTimeType);
                break;
            case DECIMAL:
                CmisChoiceDecimalType choiceDecimalType = new CmisChoiceDecimalType();
                choiceDecimalType.setIndex(BigInteger.valueOf(choice.getIndex()));
                choiceDecimalType.setKey(choice.getName());
                choiceDecimalType.setValue(BigDecimal.valueOf((Double) choice.getValue()));
                result = cmisObjectFactory.createChoiceDecimal(choiceDecimalType);
                break;
            case HTML:
                break;
            case ID:
                CmisChoiceIdType choiceIdType = new CmisChoiceIdType();
                choiceIdType.setIndex(BigInteger.valueOf(choice.getIndex()));
                choiceIdType.setKey(choice.getName());
                choiceIdType.setValue((String) choice.getValue());
                result = cmisObjectFactory.createChoiceId(choiceIdType);
                break;
            case INTEGER:
                CmisChoiceIntegerType choiceIntegerType = new CmisChoiceIntegerType();
                choiceIntegerType.setIndex(BigInteger.valueOf(choice.getIndex()));
                choiceIntegerType.setKey(choice.getName());
                choiceIntegerType.setValue(BigInteger.valueOf((Integer) choice.getValue()));
                result = cmisObjectFactory.createChoiceInteger(choiceIntegerType);
                break;
            case STRING:
                CmisChoiceStringType choiceStringType = new CmisChoiceStringType();
                choiceStringType.setIndex(BigInteger.valueOf(choice.getIndex()));
                choiceStringType.setKey(choice.getName());
                choiceStringType.setValue((String) choice.getValue());
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
    private void addChoiceChildrens(CMISPropertyTypeEnum propertyType, Collection<CMISChoice> choices, List<JAXBElement<? extends CmisChoiceType>> cmisChoices)
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
    private void addChoices(CMISPropertyTypeEnum propertyType, Collection<CMISChoice> choices, List<CmisChoiceType> cmisChoices)
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
    private void addPropertyDefs(CMISPropertyDefinition propertyDefinition, List<CmisPropertyDefinitionType> wsPropertyDefs)
    {
        CmisPropertyDefinitionType wsPropertyDef = new CmisPropertyDefinitionType();
        wsPropertyDef.setName(propertyDefinition.getPropertyName());
        wsPropertyDef.setId(propertyDefinition.getPropertyId());
        wsPropertyDef.setDisplayName(propertyDefinition.getDisplayName());
        wsPropertyDef.setDescription(propertyDefinition.getDescription());
        wsPropertyDef.setPropertyType(propertyTypeEnumMapping.get(propertyDefinition.getPropertyType()));
        wsPropertyDef.setCardinality(cardinalityEnumMapping.get(propertyDefinition.getCardinality()));
        wsPropertyDef.setUpdateability(updatabilityEnumMapping.get(propertyDefinition.getUpdatability()));
        wsPropertyDef.setInherited(propertyDefinition.isInherited());
        wsPropertyDef.setRequired(propertyDefinition.isRequired());
        wsPropertyDef.setQueryable(propertyDefinition.isQueryable());
        wsPropertyDef.setOrderable(propertyDefinition.isOrderable());
        addChoices(propertyDefinition.getPropertyType(), propertyDefinition.getChoices(), wsPropertyDef.getChoice());
        wsPropertyDef.setOpenChoice(propertyDefinition.isOpenChoice());

        wsPropertyDefs.add(wsPropertyDef);
    }

    /**
     * Set properties for web service type definition
     *
     * @param cmisTypeDefinition web service type definition
     * @param typeDefinition repository type definition
     * @param includeProperties true if need property definitions for type definition
     */
    private void setCmisTypeDefinitionProperties(CmisTypeDefinitionType cmisTypeDefinition, CMISTypeDefinition typeDefinition, boolean includeProperties)
    {
        cmisTypeDefinition.setTypeId(typeDefinition.getObjectTypeId().getTypeId());
        cmisTypeDefinition.setQueryName(typeDefinition.getObjectTypeQueryName());
        cmisTypeDefinition.setDisplayName(typeDefinition.getObjectTypeDisplayName());
        cmisTypeDefinition.setBaseType(EnumObjectType.fromValue(typeDefinition.getRootTypeId().getTypeId()));
        cmisTypeDefinition.setParentId(typeDefinition.getParentTypeId().getTypeId());
        cmisTypeDefinition.setBaseTypeQueryName(typeDefinition.getRootTypeQueryName());
        cmisTypeDefinition.setDescription(typeDefinition.getDescription());
        cmisTypeDefinition.setCreatable(typeDefinition.isCreatable());
        cmisTypeDefinition.setFileable(typeDefinition.isFileable());
        cmisTypeDefinition.setQueryable(typeDefinition.isQueryable());
        cmisTypeDefinition.setControllable(typeDefinition.isControllable());
        cmisTypeDefinition.setIncludedInSupertypeQuery(typeDefinition.isIncludedInSupertypeQuery());

        if (includeProperties)
        {
            List<CmisPropertyDefinitionType> propertyDefs = cmisTypeDefinition.getPropertyDefinition();
            for (CMISPropertyDefinition cmisPropDef : cmisDictionaryService.getPropertyDefinitions(typeDefinition.getObjectTypeId()).values())
            {
                addPropertyDefs(cmisPropDef, propertyDefs);
            }
        }
    }

    /**
     * Create web service type definition for typeId
     *
     * @param typeId type id
     * @param includeProperties true if need property definitions for type definition
     * @return web service type definition
     * @throws ObjectNotFoundException if type id not found
     */
    private JAXBElement<? extends CmisTypeDefinitionType> getCmisTypeDefinition(String typeId, boolean includeProperties) throws ObjectNotFoundException
    {
        CMISTypeId cmisTypeId = cmisDictionaryService.getCMISMapping().getCmisTypeId(typeId);
        CMISTypeDefinition typeDefinition = cmisDictionaryService.getType(cmisTypeId);

        if (typeDefinition.getParentTypeId() == null)
        {
            return null;
        }

        if (typeDefinition == null)
        {
            throw new ObjectNotFoundException("Type not found");
        }

        JAXBElement<? extends CmisTypeDefinitionType> result = null;

        switch (cmisTypeId.getScope())
        {
            case DOCUMENT:
                CmisTypeDocumentDefinitionType documentDefinitionType = new CmisTypeDocumentDefinitionType();
                setCmisTypeDefinitionProperties(documentDefinitionType, typeDefinition, includeProperties);
                documentDefinitionType.setVersionable(typeDefinition.isVersionable());
                documentDefinitionType.setContentStreamAllowed(contentStreamAllowedEnumMapping.get(typeDefinition.getContentStreamAllowed()));
                result = cmisObjectFactory.createDocumentType(documentDefinitionType);
                break;
            case FOLDER:
                CmisTypeFolderDefinitionType folderDefinitionType = new CmisTypeFolderDefinitionType();
                setCmisTypeDefinitionProperties(folderDefinitionType, typeDefinition, includeProperties);
                result = cmisObjectFactory.createFolderType(folderDefinitionType);
                break;
            case POLICY:
                CmisTypePolicyDefinitionType policyDefinitionType = new CmisTypePolicyDefinitionType();
                setCmisTypeDefinitionProperties(policyDefinitionType, typeDefinition, includeProperties);
                result = cmisObjectFactory.createPolicyType(policyDefinitionType);
                break;
            case RELATIONSHIP:
                CmisTypeRelationshipDefinitionType relationshipDefinitionType = new CmisTypeRelationshipDefinitionType();
                setCmisTypeDefinitionProperties(relationshipDefinitionType, typeDefinition, includeProperties);
                result = cmisObjectFactory.createRelationshipType(relationshipDefinitionType);
                break;
            case UNKNOWN:
                throw new ObjectNotFoundException("Unknown CMIS Type");
        }

        return result;
    }

    /**
     * Gets the list of all types in the repository.
     * 
     * @param parameters repositoryId: repository Id; typeId: type Id; returnPropertyDefinitions: false (default); maxItems: 0 = Repository-default number of items(Default);
     *        skipCount: 0 = start;
     * @return collection of CmisTypeDefinitionType and boolean hasMoreItems
     * @throws RuntimeException
     * @throws InvalidArgumentException
     * @throws ObjectNotFoundException
     * @throws ConstraintViolationException
     * @throws OperationNotSupportedException
     * @throws UpdateConflictException
     * @throws PermissionDeniedException
     */
    public GetTypesResponse getTypes(GetTypes parameters)
        throws RuntimeException, InvalidArgumentException, ObjectNotFoundException, ConstraintViolationException, OperationNotSupportedException, UpdateConflictException, PermissionDeniedException
    {
        if (descriptorService.getServerDescriptor().getId().equals(parameters.getRepositoryId()) == false)
        {
            throw new InvalidArgumentException("Invalid repository id");
        }

        Collection<CMISTypeId> typeIds;
        if (parameters.getTypeId() == null)
        {
            typeIds = cmisDictionaryService.getAllObjectTypeIds();
        }
        else
        {
            typeIds = cmisDictionaryService.getChildTypeIds(cmisDictionaryService.getCMISMapping().getCmisTypeId(parameters.getTypeId().getValue()), true);
        }

        GetTypesResponse response = new GetTypesResponse();
        if (parameters.getMaxItems() != null)
        {
            response.setHasMoreItems(parameters.getMaxItems().getValue().intValue() < typeIds.size());
        }

        // skip
        Cursor cursor = createCursor(typeIds.size(), parameters.getSkipCount() != null ? parameters.getSkipCount().getValue() : null, parameters.getMaxItems() != null ? parameters.getMaxItems().getValue() : null);
        Iterator<CMISTypeId> iterTypeIds = typeIds.iterator();
        for (int i = 0; i < cursor.getStartRow(); i++)
        {
            iterTypeIds.next();
        }

        boolean returnPropertyDefinitions = parameters.getReturnPropertyDefinitions() == null ? false : parameters.getReturnPropertyDefinitions().getValue();

        List<JAXBElement<? extends CmisTypeDefinitionType>> types = response.getType();
        for (int i = cursor.getStartRow(); i <= cursor.getEndRow(); i++)
        {
            JAXBElement<? extends CmisTypeDefinitionType> element = getCmisTypeDefinition(iterTypeIds.next().getTypeId(), returnPropertyDefinitions);
            if (element != null)
            {
                types.add(element);
            }
        }

        return response;
    }

    /**
     * Gets the definition for specified object type
     * 
     * @param parameters repositoryId: repository Id; typeId: type Id;
     * @return CMIS type definition
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws TypeNotFoundException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public GetTypeDefinitionResponse getTypeDefinition(GetTypeDefinition parameters)
        throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException, OperationNotSupportedException, TypeNotFoundException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        if (descriptorService.getServerDescriptor().getId().equals(parameters.getRepositoryId()) == false)
        {
            throw new InvalidArgumentException("Invalid repository id");
        }

        GetTypeDefinitionResponse response = new GetTypeDefinitionResponse();
        response.setType(getCmisTypeDefinition(parameters.getTypeId(), true));
        return response;
    }

}
