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

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.cmis.CMISCardinalityEnum;
import org.alfresco.cmis.CMISContentStreamAllowedEnum;
import org.alfresco.cmis.CMISFullTextSearchEnum;
import org.alfresco.cmis.CMISJoinEnum;
import org.alfresco.cmis.CMISPropertyTypeEnum;
import org.alfresco.cmis.CMISUpdatabilityEnum;
import org.alfresco.cmis.dictionary.CMISPropertyDefinition;
import org.alfresco.cmis.dictionary.CMISTypeDefinition;
import org.alfresco.cmis.dictionary.CMISTypeId;
import org.alfresco.service.descriptor.Descriptor;

/**
 * Port for repository service
 *
 * @author Dmitry Lazurkin
 */

@javax.jws.WebService(name = "RepositoryServicePort", serviceName = "RepositoryService", portName = "RepositoryServicePort", targetNamespace = "http://www.cmis.org/ns/1.0", endpointInterface = "org.alfresco.repo.cmis.ws.RepositoryServicePort")
public class DMRepositoryServicePort extends DMAbstractServicePort implements RepositoryServicePort
{
    private static Map<CMISFullTextSearchEnum, FulltextEnum> fulltextEnumMapping;
    private static Map<CMISJoinEnum, JoinEnum> joinEnumMapping;
    private static Map<CMISContentStreamAllowedEnum, ContentStreamAllowedEnum> contentStreamAllowedEnumMapping;
    private static Map<CMISUpdatabilityEnum, UpdatabilityEnum> updatabilityEnumMapping;
    private static Map<CMISCardinalityEnum, CardinalityEnum> cardinalityEnumMapping;
    private static Map<CMISPropertyTypeEnum, PropertyTypeEnum> propertyTypeEnumMapping;

    static
    {
        fulltextEnumMapping = new HashMap<CMISFullTextSearchEnum, FulltextEnum>();
        fulltextEnumMapping.put(CMISFullTextSearchEnum.NO_FULL_TEXT, FulltextEnum.NO_FULLTEXT);
        fulltextEnumMapping.put(CMISFullTextSearchEnum.FULL_TEXT_ONLY, FulltextEnum.FULLTEXT_ONLY);
        fulltextEnumMapping.put(CMISFullTextSearchEnum.FULL_TEXT_AND_STRUCTURED, FulltextEnum.FULLTEXT_AND_STRUCTURED);

        joinEnumMapping = new HashMap<CMISJoinEnum, JoinEnum>();
        joinEnumMapping.put(CMISJoinEnum.INNER_AND_OUTER_JOIN_SUPPORT, JoinEnum.INNER_AND_OUTER);
        joinEnumMapping.put(CMISJoinEnum.INNER_JOIN_SUPPORT, JoinEnum.INNER_ONLY);
        joinEnumMapping.put(CMISJoinEnum.NO_JOIN_SUPPORT, JoinEnum.NO_JOIN);

        contentStreamAllowedEnumMapping = new HashMap<CMISContentStreamAllowedEnum, ContentStreamAllowedEnum>();
        contentStreamAllowedEnumMapping.put(CMISContentStreamAllowedEnum.ALLOWED, ContentStreamAllowedEnum.ALLOWED);
        contentStreamAllowedEnumMapping.put(CMISContentStreamAllowedEnum.NOT_ALLOWED, ContentStreamAllowedEnum.NOT_ALLOWED);
        contentStreamAllowedEnumMapping.put(CMISContentStreamAllowedEnum.REQUIRED, ContentStreamAllowedEnum.REQUIRED);

        updatabilityEnumMapping = new HashMap<CMISUpdatabilityEnum, UpdatabilityEnum>();
        updatabilityEnumMapping.put(CMISUpdatabilityEnum.READ_AND_WRITE, UpdatabilityEnum.READ_WRITE);
        updatabilityEnumMapping.put(CMISUpdatabilityEnum.READ_AND_WRITE_WHEN_CHECKED_OUT, UpdatabilityEnum.READ_WRITE_WHEN_CHECKED_OUT);
        updatabilityEnumMapping.put(CMISUpdatabilityEnum.READ_ONLY, UpdatabilityEnum.READ_ONLY);

        cardinalityEnumMapping = new HashMap<CMISCardinalityEnum, CardinalityEnum>();
        cardinalityEnumMapping.put(CMISCardinalityEnum.MULTI_VALUED, CardinalityEnum.MULTI_VALUED);
        cardinalityEnumMapping.put(CMISCardinalityEnum.SINGLE_VALUED, CardinalityEnum.SINGLE_VALUED);

        propertyTypeEnumMapping = new HashMap<CMISPropertyTypeEnum, PropertyTypeEnum>();
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.BOOLEAN, PropertyTypeEnum.BOOLEAN);
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.DATETIME, PropertyTypeEnum.DATE_TIME);
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.DECIMAL, PropertyTypeEnum.DECIMAL);
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.HTML, PropertyTypeEnum.HTML);
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.ID, PropertyTypeEnum.ID);
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.INTEGER, PropertyTypeEnum.INTEGER);
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.STRING, PropertyTypeEnum.STRING);
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.URI, PropertyTypeEnum.URI);
        propertyTypeEnumMapping.put(CMISPropertyTypeEnum.XML, PropertyTypeEnum.XML);
    }

    public List<RepositoryType> getRepositories() throws RuntimeException, InvalidArgumentException, OperationNotSupportedException, UpdateConflictException,
            PermissionDeniedException
    {
        RepositoryType repositoryType = new RepositoryType();
        Descriptor serverDescriptor = descriptorService.getCurrentRepositoryDescriptor();
        repositoryType.setRepositoryID(serverDescriptor.getId());
        repositoryType.setRepositoryName(serverDescriptor.getName());
        return Collections.singletonList(repositoryType);
    }

    public RepositoryInfoType getRepositoryInfo(String repositoryId) throws RuntimeException, InvalidArgumentException, ObjectNotFoundException, ConstraintViolationException,
            OperationNotSupportedException, UpdateConflictException, PermissionDeniedException
    {
        Descriptor serverDescriptor = descriptorService.getCurrentRepositoryDescriptor();

        if (serverDescriptor.getId().equals(repositoryId) == false)
        {
            // TODO: error code
            throw new InvalidArgumentException("Invalid repository id", ExceptionUtils.createBasicFault(null, "Invalid repository id"));
        }

        RepositoryInfoType repositoryInfoType = new RepositoryInfoType();
        repositoryInfoType.setRepositoryId(serverDescriptor.getId());
        repositoryInfoType.setRepositoryName(serverDescriptor.getName());
        repositoryInfoType.setRepositoryDescription("");
        repositoryInfoType.setRootFolderId(cmisService.getDefaultRootNodeRef().toString());
        repositoryInfoType.setVendorName("Alfresco");
        repositoryInfoType.setProductName("Alfresco Repository (" + serverDescriptor.getEdition() + ")");
        repositoryInfoType.setProductVersion(serverDescriptor.getVersion());
        CapabilitiesType capabilities = new CapabilitiesType();
        capabilities.setCapabilityMultifiling(true);
        capabilities.setCapabilityUnfiling(false);
        capabilities.setCapabilityVersionSpecificFiling(false);
        capabilities.setCapabilityPWCUpdatable(true);
        capabilities.setCapabilityAllVersionsSearchable(cmisQueryService.getAllVersionsSearchable());
        capabilities.setCapabilityJoin(joinEnumMapping.get(cmisQueryService.getJoinSupport()));
        capabilities.setCapabilityFulltext(fulltextEnumMapping.get(cmisQueryService.getFullTextSearchSupport()));
        repositoryInfoType.setCapabilities(capabilities);
        repositoryInfoType.setCmisVersionsSupported(cmisService.getCMISVersion());

        return repositoryInfoType;
    }

    /**
     * @param allowedTypes collection of CMISTypeId
     * @param allowedTypesResult output list of strings
     */
    private void setAllowedTypes(Collection<CMISTypeId> allowedTypes, List<String> allowedTypesResult)
    {
        for(CMISTypeId typeId : allowedTypes)
        {
            allowedTypesResult.add(typeId.getTypeId());
        }
    }

    /**
     * @param propertyDefinition
     * @param propertyDefs
     */
    private void addPropertyDef(CMISPropertyDefinition propertyDefinition, List<PropertyAttributesType> propertyDefs)
    {
        PropertyAttributesType propertyAttributes = new PropertyAttributesType();

        propertyAttributes.setPropertyName(propertyDefinition.getPropertyName());
        propertyAttributes.setPropertyId(propertyDefinition.getPropertyId());
        propertyAttributes.setDisplayName(propertyDefinition.getDisplayName());
        propertyAttributes.setDescription(propertyDefinition.getDescription());
        propertyAttributes.setIsInherited(propertyDefinition.isInherited());
        propertyAttributes.setPropertyType(propertyTypeEnumMapping.get(propertyDefinition.getPropertyType()));
        propertyAttributes.setCardinality(cardinalityEnumMapping.get(propertyDefinition.getCardinality()));
        propertyAttributes.setMaximumLength(BigInteger.valueOf(propertyDefinition.getMaximumLength()));
        propertyAttributes.setSchemaURI(propertyDefinition.getSchemaURI());
        propertyAttributes.setEncoding(propertyDefinition.getEncoding());
        // TODO: add choices
//        List<ChoiceType> choices = propertyAttributes.getChoice();
//        for(CMISChoice cmisChoice : propertyDefinition.getChoices())
//        {
//            ChoiceType choice = new ChoiceType();
//            choice.setIndex(BigInteger.valueOf(cmisChoice.getIndex()));
//            choice.setKey(cmisChoice.getName());
//            choices.add(choice);
//        }
        propertyAttributes.setOpenChoice(propertyDefinition.isOpenChoice());
        propertyAttributes.setRequired(propertyDefinition.isRequired());
        propertyAttributes.setDefaultValue(propertyDefinition.getDefaultValue());
        propertyAttributes.setUpdatability(updatabilityEnumMapping.get(propertyDefinition.getUpdatability()));
        propertyAttributes.setQueryable(propertyDefinition.isQueryable());
        propertyAttributes.setOrderable(propertyDefinition.isOrderable());

        propertyDefs.add(propertyAttributes);
    }

    public ObjectTypeDefinitionType getTypeDefinition(String repositoryId, String typeId) throws RuntimeException, InvalidArgumentException, TypeNotFoundException,
            ObjectNotFoundException, ConstraintViolationException, OperationNotSupportedException, UpdateConflictException, PermissionDeniedException
    {
        CMISTypeId cmisTypeId = cmisDictionaryService.getCMISMapping().getCmisTypeId(typeId);
        CMISTypeDefinition typeDefinition = cmisDictionaryService.getType(cmisTypeId);

        if (typeDefinition == null)
        {
            // TODO: error code
            throw new ObjectNotFoundException("Type not found", ExceptionUtils.createBasicFault(null, "Type not found"));
        }

        ObjectTypeDefinitionType objectTypeDefinitionType = new ObjectTypeDefinitionType();
        objectTypeDefinitionType.setObjectTypeID(typeDefinition.getObjectTypeId().getTypeId());
        objectTypeDefinitionType.setObjectTypeQueryName(typeDefinition.getObjectTypeQueryName());
        objectTypeDefinitionType.setObjectTypeDisplayName(typeDefinition.getObjectTypeDisplayName());
        objectTypeDefinitionType.setParentTypeID(typeDefinition.getParentTypeId() == null ? null : typeDefinition.getParentTypeId().getTypeId());
        objectTypeDefinitionType.setRootTypeQueryName(typeDefinition.getRootTypeQueryName());
        objectTypeDefinitionType.setDescription(typeDefinition.getDescription());
        objectTypeDefinitionType.setCreatable(typeDefinition.isCreatable());
        objectTypeDefinitionType.setFileable(typeDefinition.isFileable());
        objectTypeDefinitionType.setQueryable(typeDefinition.isQueryable());
        objectTypeDefinitionType.setControllable(typeDefinition.isControllable());
        objectTypeDefinitionType.setVersionable(typeDefinition.isVersionable());
        objectTypeDefinitionType.setContentStreamAllowed(contentStreamAllowedEnumMapping.get(typeDefinition.getContentStreamAllowed()));
        setAllowedTypes(typeDefinition.getAllowedSourceTypes(), objectTypeDefinitionType.getAllowedSourceType());
        setAllowedTypes(typeDefinition.getAllowedTargetTypes(), objectTypeDefinitionType.getAllowedTargetType());

        List<PropertyAttributesType> propertyDefs = objectTypeDefinitionType.getProperty();

        for (CMISPropertyDefinition propDef : cmisDictionaryService.getPropertyDefinitions(typeDefinition.getObjectTypeId()).values())
        {
            addPropertyDef(propDef, propertyDefs);
        }

        return objectTypeDefinitionType;
    }

    public GetTypesResponse getTypes(GetTypes parameters) throws RuntimeException, InvalidArgumentException, ObjectNotFoundException, ConstraintViolationException,
            OperationNotSupportedException, UpdateConflictException, PermissionDeniedException
    {
        return null;
    }

}
