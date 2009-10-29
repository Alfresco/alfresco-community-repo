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
import java.util.Queue;

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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.web.util.paging.Cursor;
import org.alfresco.service.descriptor.Descriptor;

/**
 * Port for repository service.
 * 
 * @author Dmitry Lazurkin
 */
@javax.jws.WebService(name = "RepositoryServicePort", serviceName = "RepositoryService", portName = "RepositoryServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200908/", endpointInterface = "org.alfresco.repo.cmis.ws.RepositoryServicePort")
public class DMRepositoryServicePort extends DMAbstractServicePort implements RepositoryServicePort
{
    private static Map<CMISJoinEnum, EnumCapabilityJoin> joinEnumMapping;
    private static Map<CMISContentStreamAllowedEnum, EnumContentStreamAllowed> contentStreamAllowedEnumMapping;
    private static Map<CMISUpdatabilityEnum, EnumUpdatability> updatabilityEnumMapping;
    private static Map<CMISCardinalityEnum, EnumCardinality> cardinalityEnumMapping;
    private static Map<CMISDataTypeEnum, EnumPropertyType> propertyTypeEnumMapping;
    private static HashMap<CMISQueryEnum, EnumCapabilityQuery> queryTypeEnumMapping;

    // FIXME: Hard-coded! should be retrieved using standard mechanism
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
     * Add property definitions to list of definitions
     * 
     * @param propertyDefinition repository property definition
     * @param wsPropertyDefs web service property definition
     */
    private void addPropertyDefs(CMISTypeDefinition typeDefinition, CMISPropertyDefinition propertyDefinition, List<CmisPropertyDefinitionType> wsPropertyDefs)
            throws CmisException
    {
        CmisPropertyDefinitionType wsPropertyDef = createPropertyDefinitionType(propertyDefinition.getDataType());
        wsPropertyDef.setLocalName(propertyDefinition.getPropertyId().getLocalName());
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
        addChoices(propertyDefinition.getDataType(), propertyDefinition.getChoices(), getChoices(wsPropertyDef));
        wsPropertyDef.setOpenChoice(propertyDefinition.isOpenChoice());
        wsPropertyDefs.add(wsPropertyDef);
    }

    /**
     * Add root choices to list of choices
     * 
     * @param propertyType type of property
     * @param choices repository choice object
     * @param cmisChoices web service choice object
     */
    private void addChoices(CMISDataTypeEnum propertyType, Collection<CMISChoice> choices, List<CmisChoice> cmisChoices)
    {
        for (CMISChoice choice : choices)
        {
            CmisChoice cmisChoiceType = getCmisChoiceType(choice, propertyType);
            cmisChoices.add(cmisChoiceType);
            if (choice.getChildren().isEmpty() == false)
            {
                addChoiceChildrens(propertyType, choice.getChildren(), cmisChoices);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<CmisChoice> getChoices(CmisPropertyDefinitionType propertyDef)
    {
        List<CmisChoice> result = null;
        if (propertyDef != null)
        {
            try
            {
                result = (List<CmisChoice>) propertyDef.getClass().getMethod("getChoice").invoke(propertyDef);
            }
            catch (Exception e)
            {
            }
        }
        return result;
    }

    /**
     * Create web service choice object from repository choice object
     * 
     * @param choice repository choice
     * @param propertyType type of property
     * @return web service choice
     */
    private CmisChoice getCmisChoiceType(CMISChoice choice, CMISDataTypeEnum propertyType)
    {
        CmisChoice result = null;

        switch (propertyType)
        {
        case BOOLEAN:
            CmisChoiceBoolean choiceBooleanType = new CmisChoiceBoolean();
            choiceBooleanType.setDisplayName(choice.getName());
            choiceBooleanType.getValue().add(Boolean.parseBoolean(choice.getValue().toString()));
            result = choiceBooleanType;
            break;
        case DATETIME:
            CmisChoiceDateTime choiceDateTimeType = new CmisChoiceDateTime();
            choiceDateTimeType.setDisplayName(choice.getName());
            choiceDateTimeType.getValue().add(propertiesUtil.convert((Date) choice.getValue()));
            result = choiceDateTimeType;
            break;
        case DECIMAL:
            CmisChoiceDecimal choiceDecimalType = new CmisChoiceDecimal();
            choiceDecimalType.setDisplayName(choice.getName());
            choiceDecimalType.getValue().add(BigDecimal.valueOf(Double.parseDouble(choice.getValue().toString())));
            result = choiceDecimalType;
            break;
        case HTML:
            break;
        case ID:
            CmisChoiceId choiceIdType = new CmisChoiceId();
            choiceIdType.setDisplayName(choice.getName());
            choiceIdType.getValue().add(choice.getValue().toString());
            result = choiceIdType;
            break;
        case INTEGER:
            CmisChoiceInteger choiceIntegerType = new CmisChoiceInteger();
            choiceIntegerType.setDisplayName(choice.getName());
            choiceIntegerType.getValue().add(BigInteger.valueOf(Integer.parseInt(choice.getValue().toString())));
            result = choiceIntegerType;
            break;
        case STRING:
            CmisChoiceString choiceStringType = new CmisChoiceString();
            choiceStringType.setDisplayName(choice.getName());
            choiceStringType.getValue().add(choice.getValue().toString());
            result = choiceStringType;
            break;
        case URI:
            break;
        case XML:
            break;
        }
        return result;
    }

    /**
     * Add choices children to list of JAXBElements
     * 
     * @param propertyType type of property
     * @param choices repository choice object
     * @param cmisChoices web service choice object
     */
    private void addChoiceChildrens(CMISDataTypeEnum propertyType, Collection<CMISChoice> choices, List<CmisChoice> cmisChoices)
    {
        for (CMISChoice choice : choices)
        {
            CmisChoice cmisChoiceType = getCmisChoiceType(choice, propertyType);
            cmisChoices.add(cmisChoiceType);

            if (choice.getChildren().isEmpty() == false)
            {
                addChoiceChildrens(propertyType, choice.getChildren(), cmisChoices);
            }
        }
    }

    // private CmisTypeContainer getTypeDescendants(CMISTypeDefinition typeDef, long depth, boolean includePropertyDefs) throws CmisException
    // {
    //     if (depth < 0)
    //     {
    //         return null;
    //     }
    //     CmisTypeContainer container = new CmisTypeContainer();
    //     CmisTypeDefinitionType targetTypeDef = getCmisTypeDefinition(typeDef, includePropertyDefs);
    //     if (targetTypeDef != null)
    //     {
    //         container.setType(targetTypeDef);
    //         Collection<CMISTypeDefinition> subTypes = typeDef.getSubTypes(false);
    //         if (subTypes != null)
    //         {
    //             for (CMISTypeDefinition subType : subTypes)
    //             {
    //                 CmisTypeContainer child = getTypeDescendants(subType, depth - 1, includePropertyDefs);
    //                 if (child != null)
    //                 {
    //                     container.getChildren().add(child);
    //                 }
    //             }
    //         }
    //     }
    //     else
    //     {
    //         throw cmisObjectsUtils.createCmisException(("Subtypes collection is corrupted. Type id: " + targetTypeDef), EnumServiceException.STORAGE);
    //     }
    //     return container;
    // }

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
        cmisTypeDefinition.setId(typeDefinition.getTypeId().getId());
        cmisTypeDefinition.setQueryName(typeDefinition.getQueryName());
        cmisTypeDefinition.setDisplayName(typeDefinition.getDisplayName());
        cmisTypeDefinition.setBaseId(EnumBaseObjectTypeIds.fromValue(typeDefinition.getBaseType().getTypeId().getId()));

        if ((null != typeDefinition.getParentType()) && (null != typeDefinition.getParentType().getTypeId()))
        {
            cmisTypeDefinition.setParentId(typeDefinition.getParentType().getTypeId().getId());
        }

        cmisTypeDefinition.setQueryName(typeDefinition.getBaseType().getQueryName());
        cmisTypeDefinition.setDescription(typeDefinition.getDescription());
        cmisTypeDefinition.setCreatable(typeDefinition.isCreatable());
        cmisTypeDefinition.setFileable(typeDefinition.isFileable());
        cmisTypeDefinition.setQueryable(typeDefinition.isQueryable());
        cmisTypeDefinition.setControllableACL(typeDefinition.isControllableACL());
        cmisTypeDefinition.setControllablePolicy(typeDefinition.isControllablePolicy());
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
        if (typeDef == null)
        {
            throw cmisObjectsUtils.createCmisException("Type not found", EnumServiceException.OBJECT_NOT_FOUND);
        }

        CmisTypeDefinitionType result = null;

        switch (typeDef.getTypeId().getScope())
        {
        case DOCUMENT:
            CmisTypeDocumentDefinitionType documentDefinitionType = new CmisTypeDocumentDefinitionType();
            setCmisTypeDefinitionProperties(documentDefinitionType, typeDef, includeProperties);
            if ((null != typeDef.getParentType()) && (null != typeDef.getParentType().getTypeId()))
            {
                documentDefinitionType.setParentId(typeDef.getParentType().getTypeId().getId());
            }
            documentDefinitionType.setVersionable(true); // FIXME: this attribute MUST be setted with typeDef.isVersionable()
            documentDefinitionType.setContentStreamAllowed(contentStreamAllowedEnumMapping.get(typeDef.getContentStreamAllowed()));
            result = documentDefinitionType;
            break;
        case FOLDER:
            CmisTypeFolderDefinitionType folderDefinitionType = new CmisTypeFolderDefinitionType();
            if ((null != typeDef.getParentType()) && (null != typeDef.getParentType().getTypeId()))
            {
                folderDefinitionType.setParentId(typeDef.getParentType().getTypeId().getId());
            }
            setCmisTypeDefinitionProperties(folderDefinitionType, typeDef, includeProperties);
            result = folderDefinitionType;
            break;
        case POLICY:
            CmisTypePolicyDefinitionType policyDefinitionType = new CmisTypePolicyDefinitionType();
            if ((null != typeDef.getParentType()) && (null != typeDef.getParentType().getTypeId()))
            {
                policyDefinitionType.setParentId(typeDef.getParentType().getTypeId().getId());
            }
            setCmisTypeDefinitionProperties(policyDefinitionType, typeDef, includeProperties);
            result = policyDefinitionType;
            break;
        case RELATIONSHIP:
            CmisTypeRelationshipDefinitionType relationshipDefinitionType = new CmisTypeRelationshipDefinitionType();
            if ((null != typeDef.getParentType()) && (null != typeDef.getParentType().getTypeId()))
            {
                relationshipDefinitionType.setParentId(typeDef.getParentType().getTypeId().getId());
            }
            setCmisTypeDefinitionProperties(relationshipDefinitionType, typeDef, includeProperties);
            result = relationshipDefinitionType;
            break;
        case UNKNOWN:
            throw cmisObjectsUtils.createCmisException("Unknown CMIS Type", EnumServiceException.INVALID_ARGUMENT);
        }

        return result;
    }

    /**
     * Gets a list of available repositories for this CMIS service endpoint.
     * 
     * @return collection of CmisRepositoryEntryType (repositoryId - repository Id, repositoryName: repository name, repositoryURI: Repository URI)
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME)
     */
    public List<CmisRepositoryEntryType> getRepositories(CmisExtensionType extension) throws CmisException
    {
        CmisRepositoryEntryType repositoryEntryType = new CmisRepositoryEntryType();
        Descriptor serverDescriptor = descriptorService.getCurrentRepositoryDescriptor();
        repositoryEntryType.setRepositoryId(serverDescriptor.getId());
        repositoryEntryType.setRepositoryName(serverDescriptor.getName());

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
    public CmisRepositoryInfoType getRepositoryInfo(String repositoryId, CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);

        Descriptor serverDescriptor = descriptorService.getCurrentRepositoryDescriptor();
        CmisRepositoryInfoType repositoryInfoType = new CmisRepositoryInfoType();
        repositoryInfoType.setRepositoryId(serverDescriptor.getId());
        repositoryInfoType.setRepositoryName(serverDescriptor.getName());
        repositoryInfoType.setRepositoryDescription("");
        repositoryInfoType.setVendorName("Alfresco");
        repositoryInfoType.setProductName("Alfresco Repository (" + serverDescriptor.getEdition() + ")");
        repositoryInfoType.setProductVersion(serverDescriptor.getVersion());
        repositoryInfoType.setRootFolderId(propertiesUtil.getProperty(cmisService.getDefaultRootNodeRef(), CMISDictionaryModel.PROP_OBJECT_ID, (String) null));
        repositoryInfoType.setLatestChangeLogToken(""); // TODO
        // TODO: cmisVersionSupported is different in stubs and specification
        repositoryInfoType.setCmisVersionSupported(BigDecimal.valueOf(0.723)); // TODO: ask how to specify supported version like '0.7b3'
        repositoryInfoType.setThinClientURI(repositoryUri);
        repositoryInfoType.setChangesIncomplete(true); // TODO
        // TODO: set chnagedOnType via getChangesOnType().add()...
        // repositoryInfoType.getChangesOnType();
        // FIXME [BUG]: 'supportedPermissions' defined in spec. is absent in schema
        // FIXME [BUG]: 'propagate' defined in spec. is absent in schema
        // FIXME [BUG]: 'repositoryPermissions' defined in spec. is absent in schema
        // FIXME [BUG]: 'mappings' defined in spec. is absent in schema
        repositoryInfoType.setPrincipalAnonymous(AuthenticationUtil.getGuestUserName());
        repositoryInfoType.setPrincipalAnyone(""); // TODO

        CmisRepositoryCapabilitiesType capabilities = new CmisRepositoryCapabilitiesType();
        capabilities.setCapabilityGetDescendants(true);
        capabilities.setCapabilityGetFolderTree(true);

        capabilities.setCapabilityContentStreamUpdatability(EnumCapabilityContentStreamUpdates.ANYTIME);
        capabilities.setCapabilityChanges(EnumCapabilityChanges.NONE); // TODO
        capabilities.setCapabilityRenditions(EnumCapabilityRendition.NONE); // TODO

        capabilities.setCapabilityMultifiling(true);
        capabilities.setCapabilityUnfiling(false);
        capabilities.setCapabilityVersionSpecificFiling(false);

        capabilities.setCapabilityPWCUpdatable(true);
        capabilities.setCapabilityPWCSearchable(cmisQueryService.getPwcSearchable());
        capabilities.setCapabilityAllVersionsSearchable(cmisQueryService.getAllVersionsSearchable());

        capabilities.setCapabilityQuery(queryTypeEnumMapping.get(cmisQueryService.getQuerySupport()));
        capabilities.setCapabilityJoin(joinEnumMapping.get(cmisQueryService.getJoinSupport()));

        capabilities.setCapabilityACL(EnumCapabilityACL.NONE); // TODO

        repositoryInfoType.setCapabilities(capabilities);
        repositoryInfoType.setCmisVersionSupported(BigDecimal.valueOf(0.62));
        return repositoryInfoType;
    }

    /**
     * Returns the list of Object-Types defined for the Repository under the specified Type.
     * 
     * @param parameters repositoryId: repository Id; typeId: type Id; returnPropertyDefinitions: false (default); maxItems: 0 = Repository-default number of items(Default);
     *        skipCount: 0 = start;
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME)
     */
    public CmisTypeDefinitionListType getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount,
            CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        Collection<CMISTypeDefinition> typeDefs = getBaseTypesCollection(typeId, true);
        // skip
        Cursor cursor = createCursor(typeDefs.size(), skipCount, maxItems);
        Iterator<CMISTypeDefinition> iterTypeDefs = typeDefs.iterator();
        for (int i = 0; i < cursor.getStartRow(); i++)
        {
            iterTypeDefs.next();
        }

        boolean includePropertyDefinitionsVal = includePropertyDefinitions == null ? false : includePropertyDefinitions.booleanValue();

        CmisTypeDefinitionListType result = new CmisTypeDefinitionListType();
        for (int i = cursor.getStartRow(); i <= cursor.getEndRow(); i++)
        {
            CmisTypeDefinitionType element = getCmisTypeDefinition(iterTypeDefs.next(), includePropertyDefinitionsVal);
            if (null != element)
            {
                result.getTypes().add(element);
            }
            else
            {
                throw cmisObjectsUtils.createCmisException(("Subtypes collection is corrupted. Type id: " + typeId), EnumServiceException.STORAGE);
            }
        }
        result.setHasMoreItems(((maxItems == null) || (0 == maxItems.intValue())) ? (false) : ((cursor.getEndRow() < (typeDefs.size() - 1))));
        result.setNumItems(BigInteger.valueOf(result.getTypes().size()));
        return result;
    }

    /**
     * Gets the definition for specified object type
     * 
     * @param parameters repositoryId: repository Id; typeId: type Id;
     * @return CMIS type definition
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME)
     */
    public CmisTypeDefinitionType getTypeDefinition(String repositoryId, String typeId, CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        CMISTypeDefinition typeDef;
        try
        {
            typeDef = cmisDictionaryService.findType(typeId);
        }
        catch (Exception e)
        {
            throw cmisObjectsUtils.createCmisException(e.toString(), EnumServiceException.INVALID_ARGUMENT);
        }
        return getCmisTypeDefinition(typeDef, true);
    }

    /**
     * Returns the set of descendant Object-Types defined for the Repository under the specified Type.
     * 
     * @param parameters srepositoryId: repository Id; typeId: type Id; includePropertyDefinitions: false (default); depth: The number of levels of depth in the type hierarchy from
     *        which to return results;
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME)
     */
    public List<CmisTypeContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth, Boolean includePropertyDefinitions, CmisExtensionType extension)
            throws CmisException
    {
        checkRepositoryId(repositoryId);
        long depthLong = (null == depth) ? (-1) : (depth.longValue());
        boolean includePropertyDefsBool = (null == includePropertyDefinitions) ? (false) : (includePropertyDefinitions);
        List<CmisTypeContainer> result = new LinkedList<CmisTypeContainer>();
        Queue<TypeElement> typesQueue = new LinkedList<TypeElement>();
        Collection<CMISTypeDefinition> typeDefs = getBaseTypesCollection(typeId, false);
        for (CMISTypeDefinition typeDef : typeDefs)
        {
            typesQueue.add(new TypeElement(0L, typeDef, createTypeContainer(typeDef, includePropertyDefsBool)));
        }
        for (TypeElement element = typesQueue.peek(); !typesQueue.isEmpty(); element = (typeDefs.isEmpty()) ? (null) : (typesQueue.peek()))
        {
            typesQueue.poll();
            result.add(element.getContainer());
            long nextLevel = element.getLevel() + 1;
            Collection<CMISTypeDefinition> subTypes = element.getTypeDefinition().getSubTypes(false);
            if (null != subTypes)
            {
                for (CMISTypeDefinition typeDef : subTypes)
                {
                    CmisTypeContainer childContainer = createTypeContainer(typeDef, includePropertyDefsBool);
                    element.getContainer().getChildren().add(childContainer);
                    if ((-1 == depthLong) || (nextLevel <= depthLong))
                    {
                        typesQueue.add(new TypeElement(nextLevel, typeDef, childContainer));
                    }
                }
            }
        }
        return result;
    }

    private CmisTypeContainer createTypeContainer(CMISTypeDefinition parentType, boolean includeProperties) throws CmisException
    {
        CmisTypeContainer result = new CmisTypeContainer();
        result.setType(getCmisTypeDefinition(parentType, includeProperties));
        return result;
    }

    private class TypeElement
    {
        private long level;
        private CMISTypeDefinition typeDefinition;
        private CmisTypeContainer container;

        public TypeElement(long level, CMISTypeDefinition typeDefinition, CmisTypeContainer container)
        {
            this.level = level;
            this.typeDefinition = typeDefinition;
            this.container = container;
        }

        public long getLevel()
        {
            return level;
        }

        public CMISTypeDefinition getTypeDefinition()
        {
            return typeDefinition;
        }

        public CmisTypeContainer getContainer()
        {
            return container;
        }
    }

    private Collection<CMISTypeDefinition> getBaseTypesCollection(String typeId, boolean includeSubtypes) throws CmisException
    {
        Collection<CMISTypeDefinition> typeDefs = new LinkedList<CMISTypeDefinition>();
        if ((null == typeId) || "".equals(typeId))
        {
            typeDefs = cmisDictionaryService.getBaseTypes();
        }
        else
        {
            CMISTypeDefinition typeDef = null;
            try
            {
                typeDef = cmisDictionaryService.findType(typeId);
            }
            catch (Exception e)
            {
                throw cmisObjectsUtils.createCmisException(e.toString(), EnumServiceException.INVALID_ARGUMENT);
            }

            if (null == typeDef)
            {
                throw cmisObjectsUtils.createCmisException(("Invalid type id: \"" + typeId + "\""), EnumServiceException.INVALID_ARGUMENT);
            }
            typeDefs.add(typeDef);

            if (includeSubtypes)
            {
                Collection<CMISTypeDefinition> subTypes = typeDef.getSubTypes(false);
                if (null != subTypes)
                {
                    typeDefs.addAll(subTypes);
                }
            }
        }
        return typeDefs;
    }
}
