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

import org.alfresco.cmis.CMISAclCapabilityEnum;
import org.alfresco.cmis.CMISAclPropagationEnum;
import org.alfresco.cmis.CMISAclSupportedPermissionEnum;
import org.alfresco.cmis.CMISBaseObjectTypeIds;
import org.alfresco.cmis.CMISCapabilityChanges;
import org.alfresco.cmis.CMISCardinalityEnum;
import org.alfresco.cmis.CMISChoice;
import org.alfresco.cmis.CMISContentStreamAllowedEnum;
import org.alfresco.cmis.CMISDataTypeEnum;
import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISInvalidArgumentException;
import org.alfresco.cmis.CMISJoinEnum;
import org.alfresco.cmis.CMISPermissionDefinition;
import org.alfresco.cmis.CMISPermissionMapping;
import org.alfresco.cmis.CMISPropertyDefinition;
import org.alfresco.cmis.CMISQueryEnum;
import org.alfresco.cmis.CMISServiceException;
import org.alfresco.cmis.CMISTypeDefinition;
import org.alfresco.cmis.CMISUpdatabilityEnum;
import org.alfresco.repo.cmis.ws.utils.ExceptionUtil;
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
    private static final Map<CMISJoinEnum, EnumCapabilityJoin> JOIN_ENUM_MAPPING;
    private static final Map<CMISContentStreamAllowedEnum, EnumContentStreamAllowed> CONTENT_STREAM_ALLOWED_ENUM_MAPPING;
    private static final Map<CMISUpdatabilityEnum, EnumUpdatability> UPDATABILITY_ENUM_MAPPING;
    private static final Map<CMISCardinalityEnum, EnumCardinality> CARDINALITY_ENUM_MAPPING;
    private static final Map<CMISDataTypeEnum, EnumPropertyType> PROPERTY_TYPE_ENUM_MAPPING;
    private static final Map<CMISQueryEnum, EnumCapabilityQuery> QUERY_TYPE_ENUM_MAPPING;
    private static final Map<CMISCapabilityChanges, EnumCapabilityChanges> CHANGES_TYPE_ENUM_MAPPING;
    private static final Map<CMISBaseObjectTypeIds, EnumBaseObjectTypeIds> BASE_IDS_TYPE_ENUM_MAPPING;
    private static final Map<CMISAclCapabilityEnum, EnumCapabilityACL> ACL_CAPABILITY_ENUM_MAPPING;
    private static final Map<CMISAclSupportedPermissionEnum, EnumSupportedPermissions> ACL_SUPPORTED_PERMISSION_ENUM_MAPPING;
    private static final Map<CMISAclPropagationEnum, EnumACLPropagation> ACL_PROPAGATION_ENUM_MAPPGIN;

    static
    {
        JOIN_ENUM_MAPPING = new HashMap<CMISJoinEnum, EnumCapabilityJoin>();
        JOIN_ENUM_MAPPING.put(CMISJoinEnum.INNER_AND_OUTER_JOIN_SUPPORT, EnumCapabilityJoin.INNERANDOUTER);
        JOIN_ENUM_MAPPING.put(CMISJoinEnum.INNER_JOIN_SUPPORT, EnumCapabilityJoin.INNERONLY);
        JOIN_ENUM_MAPPING.put(CMISJoinEnum.NO_JOIN_SUPPORT, EnumCapabilityJoin.NONE);

        CONTENT_STREAM_ALLOWED_ENUM_MAPPING = new HashMap<CMISContentStreamAllowedEnum, EnumContentStreamAllowed>();
        CONTENT_STREAM_ALLOWED_ENUM_MAPPING.put(CMISContentStreamAllowedEnum.ALLOWED, EnumContentStreamAllowed.ALLOWED);
        CONTENT_STREAM_ALLOWED_ENUM_MAPPING.put(CMISContentStreamAllowedEnum.NOT_ALLOWED, EnumContentStreamAllowed.NOTALLOWED);
        CONTENT_STREAM_ALLOWED_ENUM_MAPPING.put(CMISContentStreamAllowedEnum.REQUIRED, EnumContentStreamAllowed.REQUIRED);

        UPDATABILITY_ENUM_MAPPING = new HashMap<CMISUpdatabilityEnum, EnumUpdatability>();
        UPDATABILITY_ENUM_MAPPING.put(CMISUpdatabilityEnum.READ_AND_WRITE, EnumUpdatability.READWRITE);
        UPDATABILITY_ENUM_MAPPING.put(CMISUpdatabilityEnum.READ_AND_WRITE_WHEN_CHECKED_OUT, EnumUpdatability.WHENCHECKEDOUT);
        UPDATABILITY_ENUM_MAPPING.put(CMISUpdatabilityEnum.READ_ONLY, EnumUpdatability.READONLY);
        UPDATABILITY_ENUM_MAPPING.put(CMISUpdatabilityEnum.ON_CREATE, EnumUpdatability.ONCREATE);

        CARDINALITY_ENUM_MAPPING = new HashMap<CMISCardinalityEnum, EnumCardinality>();
        CARDINALITY_ENUM_MAPPING.put(CMISCardinalityEnum.MULTI_VALUED, EnumCardinality.MULTI);
        CARDINALITY_ENUM_MAPPING.put(CMISCardinalityEnum.SINGLE_VALUED, EnumCardinality.SINGLE);

        PROPERTY_TYPE_ENUM_MAPPING = new HashMap<CMISDataTypeEnum, EnumPropertyType>();
        PROPERTY_TYPE_ENUM_MAPPING.put(CMISDataTypeEnum.BOOLEAN, EnumPropertyType.BOOLEAN);
        PROPERTY_TYPE_ENUM_MAPPING.put(CMISDataTypeEnum.DATETIME, EnumPropertyType.DATETIME);
        PROPERTY_TYPE_ENUM_MAPPING.put(CMISDataTypeEnum.DECIMAL, EnumPropertyType.DECIMAL);
        PROPERTY_TYPE_ENUM_MAPPING.put(CMISDataTypeEnum.HTML, EnumPropertyType.HTML);
        PROPERTY_TYPE_ENUM_MAPPING.put(CMISDataTypeEnum.ID, EnumPropertyType.ID);
        PROPERTY_TYPE_ENUM_MAPPING.put(CMISDataTypeEnum.INTEGER, EnumPropertyType.INTEGER);
        PROPERTY_TYPE_ENUM_MAPPING.put(CMISDataTypeEnum.STRING, EnumPropertyType.STRING);
        PROPERTY_TYPE_ENUM_MAPPING.put(CMISDataTypeEnum.URI, EnumPropertyType.URI);

        QUERY_TYPE_ENUM_MAPPING = new HashMap<CMISQueryEnum, EnumCapabilityQuery>();
        QUERY_TYPE_ENUM_MAPPING.put(CMISQueryEnum.BOTH_COMBINED, EnumCapabilityQuery.BOTHCOMBINED);
        QUERY_TYPE_ENUM_MAPPING.put(CMISQueryEnum.BOTH_SEPERATE, EnumCapabilityQuery.BOTHSEPARATE);
        QUERY_TYPE_ENUM_MAPPING.put(CMISQueryEnum.FULLTEXT_ONLY, EnumCapabilityQuery.FULLTEXTONLY);
        QUERY_TYPE_ENUM_MAPPING.put(CMISQueryEnum.METADATA_ONLY, EnumCapabilityQuery.METADATAONLY);
        QUERY_TYPE_ENUM_MAPPING.put(CMISQueryEnum.NONE, EnumCapabilityQuery.NONE);

        CHANGES_TYPE_ENUM_MAPPING = new HashMap<CMISCapabilityChanges, EnumCapabilityChanges>();
        CHANGES_TYPE_ENUM_MAPPING.put(CMISCapabilityChanges.NONE, EnumCapabilityChanges.NONE);
        CHANGES_TYPE_ENUM_MAPPING.put(CMISCapabilityChanges.OBJECTIDSONLY, EnumCapabilityChanges.OBJECTIDSONLY);

        BASE_IDS_TYPE_ENUM_MAPPING = new HashMap<CMISBaseObjectTypeIds, EnumBaseObjectTypeIds>();
        BASE_IDS_TYPE_ENUM_MAPPING.put(CMISBaseObjectTypeIds.DOCUMENT, EnumBaseObjectTypeIds.CMIS_DOCUMENT);
        BASE_IDS_TYPE_ENUM_MAPPING.put(CMISBaseObjectTypeIds.FOLDER, EnumBaseObjectTypeIds.CMIS_FOLDER);
        BASE_IDS_TYPE_ENUM_MAPPING.put(CMISBaseObjectTypeIds.RELATIONSHIP, EnumBaseObjectTypeIds.CMIS_RELATIONSHIP);
        BASE_IDS_TYPE_ENUM_MAPPING.put(CMISBaseObjectTypeIds.POLICY, EnumBaseObjectTypeIds.CMIS_POLICY);

        ACL_CAPABILITY_ENUM_MAPPING = new HashMap<CMISAclCapabilityEnum, EnumCapabilityACL>();
        ACL_CAPABILITY_ENUM_MAPPING.put(CMISAclCapabilityEnum.DISCOVER, EnumCapabilityACL.DISCOVER);
        ACL_CAPABILITY_ENUM_MAPPING.put(CMISAclCapabilityEnum.MANAGE, EnumCapabilityACL.MANAGE);
        ACL_CAPABILITY_ENUM_MAPPING.put(CMISAclCapabilityEnum.NONE, EnumCapabilityACL.NONE);

        ACL_PROPAGATION_ENUM_MAPPGIN = new HashMap<CMISAclPropagationEnum, EnumACLPropagation>();
        ACL_PROPAGATION_ENUM_MAPPGIN.put(CMISAclPropagationEnum.OBJECT_ONLY, EnumACLPropagation.OBJECTONLY);
        ACL_PROPAGATION_ENUM_MAPPGIN.put(CMISAclPropagationEnum.PROPAGATE, EnumACLPropagation.PROPAGATE);
        ACL_PROPAGATION_ENUM_MAPPGIN.put(CMISAclPropagationEnum.REPOSITORY_DETERMINED, EnumACLPropagation.REPOSITORYDETERMINED);

        ACL_SUPPORTED_PERMISSION_ENUM_MAPPING = new HashMap<CMISAclSupportedPermissionEnum, EnumSupportedPermissions>();
        ACL_SUPPORTED_PERMISSION_ENUM_MAPPING.put(CMISAclSupportedPermissionEnum.BASIC, EnumSupportedPermissions.BASIC);
        ACL_SUPPORTED_PERMISSION_ENUM_MAPPING.put(CMISAclSupportedPermissionEnum.REPOSITORY, EnumSupportedPermissions.REPOSITORY);
        ACL_SUPPORTED_PERMISSION_ENUM_MAPPING.put(CMISAclSupportedPermissionEnum.BOTH, EnumSupportedPermissions.BOTH);
    }

    private List<CmisPermissionDefinition> permissionDefinitions;
    private List<CmisPermissionMapping> permissionMapping;

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
        wsPropertyDef.setLocalNamespace(propertyDefinition.getPropertyId().getLocalNamespace());
        wsPropertyDef.setId(propertyDefinition.getPropertyId().getId());
        wsPropertyDef.setQueryName(propertyDefinition.getQueryName());
        wsPropertyDef.setDisplayName(propertyDefinition.getDisplayName());
        wsPropertyDef.setDescription(propertyDefinition.getDescription());
        wsPropertyDef.setPropertyType(PROPERTY_TYPE_ENUM_MAPPING.get(propertyDefinition.getDataType()));
        wsPropertyDef.setCardinality(CARDINALITY_ENUM_MAPPING.get(propertyDefinition.getCardinality()));
        wsPropertyDef.setUpdatability(UPDATABILITY_ENUM_MAPPING.get(propertyDefinition.getUpdatability()));
        wsPropertyDef.setInherited(!typeDefinition.getOwnedPropertyDefinitions().containsKey(propertyDefinition.getPropertyId().getId()));
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
        default:
        {
            throw ExceptionUtil.createCmisException(type.getLabel(), EnumServiceException.OBJECT_NOT_FOUND);
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
        cmisTypeDefinition.setLocalNamespace(typeDefinition.getTypeId().getLocalNamespace());
        cmisTypeDefinition.setLocalName(typeDefinition.getTypeId().getLocalName());

        if ((null != typeDefinition.getParentType()) && (null != typeDefinition.getParentType().getTypeId()))
        {
            cmisTypeDefinition.setParentId(typeDefinition.getParentType().getTypeId().getId());
        }

        cmisTypeDefinition.setDescription(typeDefinition.getDescription());
        cmisTypeDefinition.setCreatable(typeDefinition.isCreatable());
        cmisTypeDefinition.setFileable(typeDefinition.isFileable());
        cmisTypeDefinition.setQueryable(typeDefinition.isQueryable());
        cmisTypeDefinition.setControllableACL(typeDefinition.isControllableACL());
        cmisTypeDefinition.setControllablePolicy(typeDefinition.isControllablePolicy());
        cmisTypeDefinition.setIncludedInSupertypeQuery(typeDefinition.isIncludedInSuperTypeQuery());

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
            throw ExceptionUtil.createCmisException("Type not found", EnumServiceException.OBJECT_NOT_FOUND);
        }

        CmisTypeDefinitionType result = null;

        switch (typeDef.getTypeId().getScope())
        {
        case DOCUMENT:
            CmisTypeDocumentDefinitionType documentDefinitionType = new CmisTypeDocumentDefinitionType();
            documentDefinitionType.setVersionable(typeDef.isVersionable());
            documentDefinitionType.setContentStreamAllowed(CONTENT_STREAM_ALLOWED_ENUM_MAPPING.get(typeDef.getContentStreamAllowed()));
            result = documentDefinitionType;
            break;
        case FOLDER:
            result = new CmisTypeFolderDefinitionType();
            break;
        case POLICY:
            result = new CmisTypePolicyDefinitionType();
            break;
        case RELATIONSHIP:
            CmisTypeRelationshipDefinitionType relationshipDefinitionType = new CmisTypeRelationshipDefinitionType();
            if (typeDef.getAllowedSourceTypes() != null)
            {
                for (CMISTypeDefinition definition : typeDef.getAllowedSourceTypes())
                {
                    relationshipDefinitionType.getAllowedSourceTypes().add(definition.getTypeId().getId());
                }
            }
            if (typeDef.getAllowedTargetTypes() != null)
            {
                for (CMISTypeDefinition definition : typeDef.getAllowedTargetTypes())
                {
                    relationshipDefinitionType.getAllowedTargetTypes().add(definition.getTypeId().getId());
                }
            }
            result = relationshipDefinitionType;
            break;
        case UNKNOWN:
            throw ExceptionUtil.createCmisException("Unknown CMIS Type", EnumServiceException.INVALID_ARGUMENT);
        }
        if ((null != typeDef.getParentType()) && (null != typeDef.getParentType().getTypeId()))
        {
            result.setParentId(typeDef.getParentType().getTypeId().getId());
        }
        setCmisTypeDefinitionProperties(result, typeDef, includeProperties);        

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

        Descriptor currentDescriptor = descriptorService.getCurrentRepositoryDescriptor();
        Descriptor serverDescriptor = descriptorService.getServerDescriptor();
        CmisRepositoryInfoType repositoryInfoType = new CmisRepositoryInfoType();
        repositoryInfoType.setRepositoryId(currentDescriptor.getId());
        repositoryInfoType.setRepositoryName(currentDescriptor.getName());
        repositoryInfoType.setRepositoryDescription("");
        repositoryInfoType.setVendorName("Alfresco");
        repositoryInfoType.setProductName("Alfresco Repository (" + serverDescriptor.getEdition() + ")");
        repositoryInfoType.setProductVersion(currentDescriptor.getVersion());
        try
        {
            repositoryInfoType.setRootFolderId(propertiesUtil.getProperty(cmisService.getDefaultRootNodeRef(), CMISDictionaryModel.PROP_OBJECT_ID, (String) null));
        }
        catch (CMISInvalidArgumentException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
        repositoryInfoType.setLatestChangeLogToken(cmisChangeLogService.getLastChangeLogToken());
        // TODO: cmisVersionSupported is different in stubs and specification
        repositoryInfoType.setCmisVersionSupported("1.0");
        repositoryInfoType.setChangesIncomplete(cmisChangeLogService.getChangesIncomplete());
        // TODO: getFolderTree capability
        List<CMISBaseObjectTypeIds> changesOnTypeCapability = cmisChangeLogService.getChangesOnTypeCapability();
        for (CMISBaseObjectTypeIds baseId : changesOnTypeCapability)
        {
            repositoryInfoType.getChangesOnType().add(BASE_IDS_TYPE_ENUM_MAPPING.get(baseId));
        }
        repositoryInfoType.setPrincipalAnonymous(cmisAclService.getPrincipalAnonymous());
        repositoryInfoType.setPrincipalAnyone(cmisAclService.getPrincipalAnyone());

        CmisACLCapabilityType aclCapability = new CmisACLCapabilityType();
        aclCapability.setSupportedPermissions(ACL_SUPPORTED_PERMISSION_ENUM_MAPPING.get(cmisAclService.getSupportedPermissions()));
        aclCapability.setPropagation(ACL_PROPAGATION_ENUM_MAPPGIN.get(cmisAclService.getAclPropagation()));
        aclCapability.getMapping().addAll(getPermissionMapping());
        aclCapability.getPermissions().addAll(getPermissionDefinitions());
        repositoryInfoType.setAclCapability(aclCapability);

        CmisRepositoryCapabilitiesType capabilities = new CmisRepositoryCapabilitiesType();
        capabilities.setCapabilityGetDescendants(true);
        capabilities.setCapabilityGetFolderTree(true);

        capabilities.setCapabilityContentStreamUpdatability(EnumCapabilityContentStreamUpdates.ANYTIME);
        capabilities.setCapabilityChanges(CHANGES_TYPE_ENUM_MAPPING.get(cmisChangeLogService.getCapability()));
        capabilities.setCapabilityRenditions(EnumCapabilityRendition.READ);

        capabilities.setCapabilityMultifiling(true);
        capabilities.setCapabilityUnfiling(false);
        capabilities.setCapabilityVersionSpecificFiling(false);

        capabilities.setCapabilityPWCUpdatable(true);
        capabilities.setCapabilityPWCSearchable(cmisQueryService.getPwcSearchable());
        capabilities.setCapabilityAllVersionsSearchable(cmisQueryService.getAllVersionsSearchable());

        capabilities.setCapabilityQuery(QUERY_TYPE_ENUM_MAPPING.get(cmisQueryService.getQuerySupport()));
        capabilities.setCapabilityJoin(JOIN_ENUM_MAPPING.get(cmisQueryService.getJoinSupport()));

        capabilities.setCapabilityACL(ACL_CAPABILITY_ENUM_MAPPING.get(cmisAclService.getAclCapability()));

        repositoryInfoType.setCapabilities(capabilities);
        return repositoryInfoType;
    }

    private List<CmisPermissionDefinition> getPermissionDefinitions()
    {
        if (null == permissionDefinitions)
        {
            permissionDefinitions = new LinkedList<CmisPermissionDefinition>();
            for (CMISPermissionDefinition definition : cmisAclService.getRepositoryPermissions())
            {
                CmisPermissionDefinition cmisDefinition = new CmisPermissionDefinition();
                cmisDefinition.setDescription(definition.getDescription());
                cmisDefinition.setPermission(definition.getPermission());
                permissionDefinitions.add(cmisDefinition);
            }
        }
        return permissionDefinitions;
    }

    private List<CmisPermissionMapping> getPermissionMapping()
    {
        if (null == permissionMapping)
        {
            permissionMapping = new LinkedList<CmisPermissionMapping>();
            for (CMISPermissionMapping mapping : cmisAclService.getPermissionMappings())
            {
                CmisPermissionMapping cmisMapping = new CmisPermissionMapping();
                cmisMapping.getPermission().addAll(mapping.getPermissions());
                cmisMapping.setKey(EnumAllowableActionsKey.fromValue(mapping.getKey()));
                permissionMapping.add(cmisMapping);
            }
        }
        return permissionMapping;
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
        Collection<CMISTypeDefinition> typeDefs;
        try
        {
            typeDefs = typeId == null ? cmisService.getBaseTypes() : cmisService.getTypeDefinition(typeId).getSubTypes(false);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
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
                throw ExceptionUtil.createCmisException(("Subtypes collection is corrupted. Type id: " + typeId), EnumServiceException.STORAGE);
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
            typeDef = cmisService.getTypeDefinition(typeId);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
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
        long depthLong = (null == depth || null == typeId) ? (-1) : (depth.longValue());
        if (0 == depthLong)
        {
            throw ExceptionUtil.createCmisException("Invalid depth '0'", EnumServiceException.INVALID_ARGUMENT);
        }
        List<CmisTypeContainer> result = new LinkedList<CmisTypeContainer>();
        CMISTypeDefinition typeDef;
        try
        {
            typeDef = typeId == null ? null : cmisService.getTypeDefinition(typeId);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
        getTypeDescendants(typeDef, result, includePropertyDefinitions != null && includePropertyDefinitions, 1, depthLong);
        return result;
    }

    private void getTypeDescendants(CMISTypeDefinition parent, List<CmisTypeContainer> result, boolean includePropertyDefs, long depth, long maxDepth) throws CmisException
    {
        Collection<CMISTypeDefinition> subtypes = parent == null ? cmisService.getBaseTypes() : parent.getSubTypes(false);
        for (CMISTypeDefinition typeDef : subtypes)
        {
            result.add(createTypeContainer(typeDef, includePropertyDefs));
        }
        if (maxDepth == -1 || depth + 1 <= maxDepth)
        {
            for (CMISTypeDefinition typeDef : subtypes)
            {
                getTypeDescendants(typeDef, result, includePropertyDefs, depth + 1, maxDepth);
            }
        }
    }

    private CmisTypeContainer createTypeContainer(CMISTypeDefinition parentType, boolean includeProperties) throws CmisException
    {
        CmisTypeContainer result = new CmisTypeContainer();
        result.setType(getCmisTypeDefinition(parentType, includeProperties));
        return result;
    }
}
