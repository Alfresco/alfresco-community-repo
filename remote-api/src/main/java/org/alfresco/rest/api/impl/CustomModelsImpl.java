/*
 * #%L
 * Alfresco Remote API
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

package org.alfresco.rest.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.dictionary.CompiledModel;
import org.alfresco.repo.dictionary.CustomModelDefinitionImpl;
import org.alfresco.repo.dictionary.Facetable;
import org.alfresco.repo.dictionary.M2Aspect;
import org.alfresco.repo.dictionary.M2Class;
import org.alfresco.repo.dictionary.M2Constraint;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Namespace;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.repo.dictionary.M2Type;
import org.alfresco.repo.dictionary.ValueDataTypeValidator;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.api.CustomModels;
import org.alfresco.rest.api.model.AbstractClassModel;
import org.alfresco.rest.api.model.AbstractCommonDetails;
import org.alfresco.rest.api.model.CustomAspect;
import org.alfresco.rest.api.model.CustomModel;
import org.alfresco.rest.api.model.CustomModel.ModelStatus;
import org.alfresco.rest.api.model.CustomModelConstraint;
import org.alfresco.rest.api.model.CustomModelDownload;
import org.alfresco.rest.api.model.CustomModelNamedValue;
import org.alfresco.rest.api.model.CustomModelProperty;
import org.alfresco.rest.api.model.CustomType;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.core.exceptions.ConstraintViolatedException;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.CustomModelDefinition;
import org.alfresco.service.cmr.dictionary.CustomModelException;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.dictionary.CustomModelException.ActiveModelConstraintException;
import org.alfresco.service.cmr.dictionary.CustomModelException.CustomModelConstraintException;
import org.alfresco.service.cmr.dictionary.CustomModelException.InvalidCustomModelException;
import org.alfresco.service.cmr.dictionary.CustomModelException.ModelDoesNotExistException;
import org.alfresco.service.cmr.dictionary.CustomModelException.ModelExistsException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.collections.CollectionUtils;
import org.alfresco.util.collections.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;


/**
 * @author Jamal Kaabi-Mofrad
 */
public class CustomModelsImpl implements CustomModels
{
    // for consistency the patterns are equivalent to the patterns defined in the cmm-misc.lib.js
    public static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_\\-]+$");
    public static final Pattern URI_PATTERN = Pattern.compile("^[A-Za-z0-9:/_\\.\\-]+$");

    public static final String MODEL_NAME_NULL_ERR = "cmm.rest_api.model_name_null";
    public static final String TYPE_NAME_NULL_ERR = "cmm.rest_api.type_name_null";
    public static final String ASPECT_NAME_NULL_ERR = "cmm.rest_api.aspect_name_null";
    public static final String CONSTRAINT_NAME_NULL_ERR = "cmm.rest_api.constraint_name_null";

    // Services
    protected CustomModelService customModelService;
    protected DictionaryService dictionaryService;
    protected PersonService personService;
    protected NodeService nodeService;
    protected NamespaceService namespaceService;
    protected ValueDataTypeValidator valueDataTypeValidator;

    private static final String DEFAULT_DATA_TYPE = "d:text";
    private static final String BOOLEAN_DATA_TYPE = "d:boolean";
    private static final String SELECT_ALL = "all";
    private static final String SELECT_STATUS = "status";
    private static final String SELECT_PROPS = "props";
    private static final String SELECT_ALL_PROPS = "allProps";
    private static final String PARAM_UPDATE_PROP = "update";
    private static final String PARAM_DELETE_PROP = "delete";
    private static final String PARAM_WITH_EXT_MODULE = "extModule";

    public void setCustomModelService(CustomModelService customModelService)
    {
        this.customModelService = customModelService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setValueDataTypeValidator(ValueDataTypeValidator valueDataTypeValidator)
    {
        this.valueDataTypeValidator = valueDataTypeValidator;
    }

    @Override
    public CustomModel getCustomModel(String modelName, Parameters parameters)
    {
        CustomModelDefinition modelDef = getCustomModelImpl(modelName);

        if (hasSelectProperty(parameters, SELECT_ALL))
        {
            return new CustomModel(modelDef, 
                        convertToCustomTypes(modelDef.getTypeDefinitions(), false),
                        convertToCustomAspects(modelDef.getAspectDefinitions(), false),
                        convertToCustomModelConstraints(modelDef.getModelDefinedConstraints()));
        }

        return new CustomModel(modelDef);
    }

    private CustomModelDefinition getCustomModelImpl(String modelName)
    {
        if(modelName == null)
        {
            throw new InvalidArgumentException(MODEL_NAME_NULL_ERR);
        }

        CustomModelDefinition model = null;
        try
        {
            model = customModelService.getCustomModel(modelName);
        }
        catch (CustomModelException ex)
        {
            throw new EntityNotFoundException(modelName);
        }

        if (model == null)
        {
            throw new EntityNotFoundException(modelName);
        }

        return model;
    }

    @Override
    public CollectionWithPagingInfo<CustomModel> getCustomModels(Parameters parameters)
    {
        Paging paging = parameters.getPaging();
        PagingRequest pagingRequest = Util.getPagingRequest(paging);
        PagingResults<CustomModelDefinition> results = customModelService.getCustomModels(pagingRequest);

        Integer totalItems = results.getTotalResultCount().getFirst();
        List<CustomModelDefinition> page = results.getPage();

        List<CustomModel> models = new ArrayList<>(page.size());
        for (CustomModelDefinition modelDefinition : page)
        {
            models.add(new CustomModel(modelDefinition));
        }

        return CollectionWithPagingInfo.asPaged(paging, models, results.hasMoreItems(), (totalItems == null ? null : totalItems.intValue()));
    }

    @Override
    public CustomModel createCustomModel(CustomModel model)
    {
        // Check the current user is authorised to create a custom model
        validateCurrentUser();
        return createCustomModelImpl(model, true);
    }

    private CustomModel createCustomModelImpl(CustomModel model, boolean basicModelOnly)
    {
        M2Model m2Model = null;
        if (basicModelOnly)
        {
            m2Model = convertToM2Model(model, null, null, null);
        }
        else
        {
            m2Model = convertToM2Model(model, model.getTypes(), model.getAspects(), model.getConstraints());
        }

        boolean activate = ModelStatus.ACTIVE.equals(model.getStatus());
        try
        {
            CustomModelDefinition modelDefinition = customModelService.createCustomModel(m2Model, activate);
            return new CustomModel(modelDefinition);
        }
        catch (ModelExistsException me)
        {
            throw new ConstraintViolatedException(me.getMessage());
        }
        catch (CustomModelConstraintException ncx)
        {
            throw new ConstraintViolatedException(ncx.getMessage());
        }
        catch (InvalidCustomModelException iex)
        {
            throw new InvalidArgumentException(iex.getMessage());
        }
        catch (Exception e)
        {
            throw new ApiException("cmm.rest_api.model_invalid", e);
        }
    }

    @Override
    public CustomModel updateCustomModel(String modelName, CustomModel model, Parameters parameters)
    {
        // Check the current user is authorised to update the custom model
        validateCurrentUser();

        // Check to see if the model exists
        ModelDetails existingModelDetails = new ModelDetails(getCustomModelImpl(modelName));
        CustomModel existingModel = existingModelDetails.getModel();

        // The model just needs to be activated/deactivated (in other words,
        // the other properties should be untouched)
        if (hasSelectProperty(parameters, SELECT_STATUS))
        {
            ModelStatus status = model.getStatus();
            if (status == null)
            {
                throw new InvalidArgumentException("cmm.rest_api.model_status_null");
            }
            try
            {
                if (ModelStatus.ACTIVE.equals(status))
                {
                    customModelService.activateCustomModel(modelName);
                }
                else
                {
                    customModelService.deactivateCustomModel(modelName);
                }
                // update the model's status
                existingModel.setStatus(status);
                return existingModel;
            }
            catch (CustomModelConstraintException mce)
            {
                throw new ConstraintViolatedException(mce.getMessage());
            }
            catch (Exception ex)
            {
                throw new ApiException(ex.getMessage(), ex);
            }
        }
        else
        {
            if (model.getName() != null && !(existingModel.getName().equals(model.getName())))
            {
                throw new InvalidArgumentException("cmm.rest_api.model_name_cannot_update");
            }

            existingModel.setNamespaceUri(model.getNamespaceUri());
            final boolean isNamespacePrefixChanged = !(existingModel.getNamespacePrefix().equals(model.getNamespacePrefix()));
            if(isNamespacePrefixChanged)
            {
                // Change types' and aspects' parents as well as the property constraint's Ref namespace prefix
                replacePrefix(existingModelDetails.getTypes(), existingModel.getNamespacePrefix(), model.getNamespacePrefix());
                replacePrefix(existingModelDetails.getAspects(), existingModel.getNamespacePrefix(), model.getNamespacePrefix());
            }
            existingModel.setNamespacePrefix(model.getNamespacePrefix());
            existingModel.setAuthor(model.getAuthor());
            existingModel.setDescription(model.getDescription());

            CustomModelDefinition modelDef = updateModel(existingModelDetails, "cmm.rest_api.model_update_failure");
            return new CustomModel(modelDef);
        }
    }

    private void replacePrefix(List<? extends AbstractClassModel> existingTypesOrAspects, String modelOldNamespacePrefix, String modelNewNamespacePrefix)
    {
        for(AbstractClassModel classModel : existingTypesOrAspects)
        {
            // Type/Aspect's parent name
            String parentName = classModel.getParentName();
            if(parentName != null)
            {
                Pair<String, String> prefixLocalNamePair = splitPrefixedQName(parentName);
                // Check to see if the parent name prefix, is the namespace prefix of the model being edited.
                // As we don't want to modify the parent name of the imported models.
                if(modelOldNamespacePrefix.equals(prefixLocalNamePair.getFirst()))
                {
                    // Change the parent name prefix, to a new model namespace prefix.
                    String newParentName = constructName(prefixLocalNamePair.getSecond(), modelNewNamespacePrefix);
                    classModel.setParentName(newParentName);
                }
            }

            // Change the property constraint ref
            List<CustomModelProperty> properties = classModel.getProperties();
            for(CustomModelProperty prop : properties)
            {
                List<String> constraintRefs = prop.getConstraintRefs();
                if(constraintRefs.size() > 0)
                {
                    List<String> modifiedRefs = new ArrayList<>(constraintRefs.size());
                    for(String ref : constraintRefs)
                    {
                        // We don't need to check if the prefix is equal to the model prefix here, as it was
                        // done upon adding the constraint refs in the setM2Properties method.
                        Pair<String, String> prefixLocalNamePair = splitPrefixedQName(ref);
                        // Change the constraint ref prefix, to a new model namespace prefix.
                        String newRef = constructName(prefixLocalNamePair.getSecond(), modelNewNamespacePrefix);
                        modifiedRefs.add(newRef);
                    }
                    prop.setConstraintRefs(modifiedRefs);
                }
            }
        }
    }

    @Override
    public void deleteCustomModel(String modelName)
    {
        // Check the current user is authorised to delete the custom model
        validateCurrentUser();

        if(modelName == null)
        {
            throw new InvalidArgumentException(MODEL_NAME_NULL_ERR);
        }

        try
        {
            customModelService.deleteCustomModel(modelName);
        }
        catch (ModelDoesNotExistException ee)
        {
            throw new EntityNotFoundException(modelName);
        }
        catch (ActiveModelConstraintException ae)
        {
            throw new ConstraintViolatedException(ae.getMessage());
        }
        catch (Exception ex)
        {
            throw new ApiException(ex.getMessage(), ex);
        }
    }

    @Override
    public CustomType getCustomType(String modelName, String typeName, Parameters parameters)
    {
        if(typeName == null)
        {
            throw new InvalidArgumentException(TYPE_NAME_NULL_ERR);
        }

        final CustomModelDefinition modelDef = getCustomModelImpl(modelName);
        QName typeQname = QName.createQName(modelDef.getName().getNamespaceURI(), typeName);

        TypeDefinition customTypeDef = customModelService.getCustomType(typeQname);
        if (customTypeDef == null)
        {
            throw new EntityNotFoundException(typeName);
        }
        
        // Check if inherited properties have been requested
        boolean includeInheritedProps = hasSelectProperty(parameters, SELECT_ALL_PROPS);
        return convertToCustomType(customTypeDef, includeInheritedProps);
    }

    @Override
    public CollectionWithPagingInfo<CustomType> getCustomTypes(String modelName, Parameters parameters)
    {
        CustomModelDefinition modelDef = getCustomModelImpl(modelName);
        Collection<TypeDefinition> typeDefinitions = modelDef.getTypeDefinitions();
        // TODO Should we support paging?
        Paging paging = Paging.DEFAULT;

        List<CustomType> customTypes = convertToCustomTypes(typeDefinitions, false);

        return CollectionWithPagingInfo.asPaged(paging, customTypes, false, typeDefinitions.size());

    }

    @Override
    public CustomType createCustomType(String modelName, CustomType type)
    {
        // Check the current user is authorised to update the custom model
        validateCurrentUser();

        ModelDetails existingModelDetails = new ModelDetails(getCustomModelImpl(modelName));

        // Validate type's parent
        validateTypeAspectParent(type, existingModelDetails.getModel());
        existingModelDetails.getTypes().add(type);

        updateModel(existingModelDetails, "cmm.rest_api.type_create_failure");
        return type;
    }

    @Override
    public CustomType updateCustomType(String modelName, CustomType type, Parameters parameters)
    {
        return updateTypeAspect(modelName, type, parameters);
    }

    private <T extends AbstractClassModel> T updateTypeAspect(String modelName, T classDef, Parameters parameters)
    {
        // Check the current user is authorised to update the custom model
        validateCurrentUser();

        final boolean isAspect = classDef instanceof CustomAspect;

        String name = classDef.getName();
        if(name == null)
        {
            String msgId = isAspect ? ASPECT_NAME_NULL_ERR : TYPE_NAME_NULL_ERR;
            throw new InvalidArgumentException(msgId);
        }

        ModelDetails existingModelDetails = new ModelDetails(getCustomModelImpl(modelName));

        List<? extends AbstractClassModel> allClassDefs = isAspect ? existingModelDetails.getAspects() : existingModelDetails.getTypes();

        @SuppressWarnings("unchecked")
        T existingClassDef = (T) getObjectByName(allClassDefs, name);
        if (existingClassDef == null)
        {
            throw new EntityNotFoundException(name);
        }

        if (hasSelectProperty(parameters, SELECT_PROPS))
        {
            String errorMsg = null;
            String propName = parameters.getParameter(PARAM_DELETE_PROP);
            if (propName == null)
            {
                errorMsg = "cmm.rest_api.property_create_update_failure";
                // Add/Update properties
                mergeProperties(existingClassDef, classDef, parameters, existingModelDetails.isActive());
            }
            else //Delete property request
            {
                errorMsg = "cmm.rest_api.property_delete_failure";
                deleteProperty(existingClassDef, propName);
            }

            updateModel(existingModelDetails, errorMsg);
        }
        else
        {
            existingClassDef.setTitle(classDef.getTitle());
            existingClassDef.setDescription(classDef.getDescription());
            final boolean isParentChanged = !(StringUtils.equals(existingClassDef.getParentName(), classDef.getParentName()));
            if (isParentChanged && existingModelDetails.isActive())
            {
                String errMsgId = isAspect ? "cmm.rest_api.aspect_parent_cannot_update" : "cmm.rest_api.type_parent_cannot_update";
                throw new ConstraintViolatedException(errMsgId);
            }
            // Validate type/aspect parent
            validateTypeAspectParent(classDef, existingModelDetails.getModel());
            existingClassDef.setParentName(classDef.getParentName());

            String errMsgId = isAspect ? "cmm.rest_api.aspect_update_failure" : "cmm.rest_api.type_update_failure";
            updateModel(existingModelDetails, errMsgId);
        }
        return existingClassDef;
    }

    @Override
    public void deleteCustomType(String modelName, String typeName)
    {
        // Check the current user is authorised to delete the custom model's type
        validateCurrentUser();

        if(typeName == null)
        {
            throw new InvalidArgumentException(TYPE_NAME_NULL_ERR);
        }

        ModelDetails existingModelDetails = new ModelDetails(getCustomModelImpl(modelName));
        if(existingModelDetails.isActive())
        {
            throw new ConstraintViolatedException("cmm.rest_api.type_cannot_delete");
        }

        Map<String, CustomType> allTypes = transformToMap(existingModelDetails.getTypes(), toNameFunction());
        CustomType typeToBeDeleted = allTypes.get(typeName);

        if(typeToBeDeleted == null)
        {
            throw new EntityNotFoundException(typeName);
        }

        // Validate type's dependency
        validateTypeAspectDelete(allTypes.values(), typeToBeDeleted.getPrefixedName());

        // Remove the validated type
        allTypes.remove(typeName);
        existingModelDetails.setTypes(new ArrayList<>(allTypes.values()));

        updateModel(existingModelDetails, "cmm.rest_api.type_delete_failure");
    }

    @Override
    public CustomAspect getCustomAspect(String modelName, String aspectName, Parameters parameters)
    {
        if(aspectName == null)
        {
            throw new InvalidArgumentException(ASPECT_NAME_NULL_ERR);
        }

        final CustomModelDefinition modelDef = getCustomModelImpl(modelName);
        QName aspectQname = QName.createQName(modelDef.getName().getNamespaceURI(), aspectName);

        AspectDefinition customAspectDef = customModelService.getCustomAspect(aspectQname);
        if (customAspectDef == null)
        {
            throw new EntityNotFoundException(aspectName);
        }
        
        // Check if inherited properties have been requested
        boolean includeInheritedProps = hasSelectProperty(parameters, SELECT_ALL_PROPS);
        return convertToCustomAspect(customAspectDef, includeInheritedProps);
    }

    @Override
    public CollectionWithPagingInfo<CustomAspect> getCustomAspects(String modelName, Parameters parameters)
    {
        CustomModelDefinition modelDef = getCustomModelImpl(modelName);
        Collection<AspectDefinition> aspectDefinitions = modelDef.getAspectDefinitions();
        // TODO Should we support paging?
        Paging paging = Paging.DEFAULT;

        List<CustomAspect> customAspects = convertToCustomAspects(aspectDefinitions, false);

        return CollectionWithPagingInfo.asPaged(paging, customAspects, false, aspectDefinitions.size());
    }

    @Override
    public CustomAspect createCustomAspect(String modelName, CustomAspect aspect)
    {
        // Check the current user is authorised to update the custom model
        validateCurrentUser();

        ModelDetails existingModelDetails = new ModelDetails(getCustomModelImpl(modelName));

        // Validate aspect's parent
        validateTypeAspectParent(aspect, existingModelDetails.getModel());
        existingModelDetails.getAspects().add(aspect);

        updateModel(existingModelDetails, "cmm.rest_api.aspect_create_failure");
        return aspect;
    }

    @Override
    public CustomAspect updateCustomAspect(String modelName, CustomAspect aspect, Parameters parameters)
    {
        return updateTypeAspect(modelName, aspect, parameters);
    }

    @Override
    public void deleteCustomAspect(String modelName, String aspectName)
    {
        // Check the current user is authorised to delete the custom model's aspect
        validateCurrentUser();

        if(aspectName == null)
        {
            throw new InvalidArgumentException(ASPECT_NAME_NULL_ERR);
        }

        ModelDetails existingModelDetails = new ModelDetails(getCustomModelImpl(modelName));
        if(existingModelDetails.isActive())
        {
            throw new ConstraintViolatedException("cmm.rest_api.aspect_cannot_delete");
        }

        Map<String, CustomAspect> allAspects = transformToMap(existingModelDetails.getAspects(), toNameFunction());
        CustomAspect aspectToBeDeleted = allAspects.get(aspectName);

        if(aspectToBeDeleted == null)
        {
            throw new EntityNotFoundException(aspectName);
        }

        // Validate aspect's dependency
        validateTypeAspectDelete(allAspects.values(), aspectToBeDeleted.getPrefixedName());

        // Remove the validated aspect
        allAspects.remove(aspectName);
        existingModelDetails.setAspects(new ArrayList<>(allAspects.values()));

        updateModel(existingModelDetails, "cmm.rest_api.aspect_delete_failure");
    }

    @Override
    public CollectionWithPagingInfo<CustomModelConstraint> getCustomModelConstraints(String modelName, Parameters parameters)
    {
        CustomModelDefinition modelDef = getCustomModelImpl(modelName);
        Collection<ConstraintDefinition> constraintDefinitions = modelDef.getModelDefinedConstraints();
        // TODO Should we support paging?
        Paging paging = Paging.DEFAULT;

        List<CustomModelConstraint> customModelConstraints = convertToCustomModelConstraints(constraintDefinitions);

        return CollectionWithPagingInfo.asPaged(paging, customModelConstraints, false, constraintDefinitions.size());
    }

    @Override
    public CustomModelConstraint getCustomModelConstraint(String modelName, String constraintName, Parameters parameters)
    {
        if (constraintName == null)
        {
            throw new InvalidArgumentException(CONSTRAINT_NAME_NULL_ERR);
        }

        final CustomModelDefinition modelDef = getCustomModelImpl(modelName);
        QName constraintQname = QName.createQName(modelDef.getName().getNamespaceURI(), constraintName);

        ConstraintDefinition constraintDef = customModelService.getCustomConstraint(constraintQname);
        if (constraintDef == null)
        {
            throw new EntityNotFoundException(constraintName);
        }

        return new CustomModelConstraint(constraintDef, dictionaryService);
    }

    @Override
    public CustomModelConstraint createCustomModelConstraint(String modelName, CustomModelConstraint constraint)
    {
        // Check the current user is authorised to create constraints
        validateCurrentUser();

        ModelDetails existingModelDetails = new ModelDetails(getCustomModelImpl(modelName));

        existingModelDetails.getModelDefinedConstraints().add(constraint);

        updateModel(existingModelDetails, "cmm.rest_api.constraint_create_failure");
        return constraint;
    }

    @Override
    public CustomModelDownload createDownload(String modelName, Parameters parameters)
    {
        // Check the current user is authorised to export the model
        validateCurrentUser();

        if (modelName == null)
        {
            throw new InvalidArgumentException(MODEL_NAME_NULL_ERR);
        }

        String propName = parameters.getParameter(PARAM_WITH_EXT_MODULE);
        boolean withForm = Boolean.valueOf(propName);
        try
        {
            NodeRef nodeRef = customModelService.createDownloadNode(modelName, withForm);
            return new CustomModelDownload(nodeRef);
        }
        catch (Exception ex)
        {
            String errorMsg = "cmm.rest_api.model_download_failure";
            if (ex.getMessage() != null)
            {
                errorMsg = ex.getMessage();
            }
            throw new ApiException(errorMsg, ex);
        }
    }

    private CustomType convertToCustomType(TypeDefinition typeDefinition, boolean includeInheritedProps)
    {
        List<CustomModelProperty> properties = convertToCustomModelProperty(typeDefinition, includeInheritedProps);
        return new CustomType(typeDefinition, dictionaryService, properties);
    }

    private List<CustomType> convertToCustomTypes(Collection<TypeDefinition> typeDefinitions, boolean includeInheritedProps)
    {
        // Convert a collection of TypeDefinitions into a list of CustomTypes
        List<CustomType> customTypes = new ArrayList<>(typeDefinitions.size());
        for (TypeDefinition td : typeDefinitions)
        {
            customTypes.add(convertToCustomType(td, includeInheritedProps));
        }

        return customTypes;
    }

    private CustomAspect convertToCustomAspect(AspectDefinition aspectDefinition, boolean includeInheritedProps)
    {
        List<CustomModelProperty> properties = convertToCustomModelProperty(aspectDefinition, includeInheritedProps);
        return new CustomAspect(aspectDefinition, dictionaryService, properties);
    }

    private List<CustomAspect> convertToCustomAspects(Collection<AspectDefinition> aspectDefinitions, boolean includeInheritedProps)
    {
        // Convert a collection of AspectDefinitions into a list of CustomAspect
        List<CustomAspect> customAspects = new ArrayList<>(aspectDefinitions.size());
        for (AspectDefinition ad : aspectDefinitions)
        {
            customAspects.add(convertToCustomAspect(ad, includeInheritedProps));
        }

        return customAspects;
    }

    private List<CustomModelProperty> convertToCustomModelProperty(ClassDefinition classDefinition, boolean includeInherited)
    {
        Collection<PropertyDefinition> ownProperties = null;
        ClassDefinition parentDef = classDefinition.getParentClassDefinition();
        if (!includeInherited && parentDef != null)
        {
            // Remove inherited properties
            ownProperties = removeRightEntries(classDefinition.getProperties(), parentDef.getProperties()).values();
        }
        else
        {
            ownProperties = classDefinition.getProperties().values();
        }

        List<CustomModelProperty> customProperties = new ArrayList<>(ownProperties.size());
        for (PropertyDefinition propDef : ownProperties)
        {
            customProperties.add(new CustomModelProperty(propDef, dictionaryService));
        }

        return customProperties;
    }

    private List<CustomModelConstraint> convertToCustomModelConstraints(Collection<ConstraintDefinition> constraintDefinitions)
    {
        List<CustomModelConstraint> constraints = new ArrayList<>(constraintDefinitions.size());
        for (ConstraintDefinition definition : constraintDefinitions)
        {
            constraints.add(new CustomModelConstraint(definition, dictionaryService));
        }
        return constraints;
    }

    /**
     * Converts the given {@code ModelDetails} object into a {@link M2Model} object
     * 
     * @param modelDetails the custom model details
     * @return {@link M2Model} object
     */
    private M2Model convertToM2Model(ModelDetails modelDetails)
    {
        return convertToM2Model(modelDetails.getModel(), modelDetails.getTypes(), modelDetails.getAspects(), modelDetails.getModelDefinedConstraints());
    }

    /**
     * Converts the given {@code org.alfresco.rest.api.model.CustomModel}
     * object, a collection of {@code org.alfresco.rest.api.model.CustomType}
     * objects, a collection of
     * {@code org.alfresco.rest.api.model.CustomAspect} objects, and a collection of
     * {@code org.alfresco.rest.api.model.CustomModelConstraint} objects into a {@link M2Model} object
     * 
     * @param customModel the custom model
     * @param types the custom types
     * @param aspects the custom aspects
     * @param constraints the custom constraints
     * @return {@link M2Model} object
     */
    private M2Model convertToM2Model(CustomModel customModel, Collection<CustomType> types, Collection<CustomAspect> aspects, Collection<CustomModelConstraint> constraints)
    {
        validateBasicModelInput(customModel);

        Set<Pair<String, String>> namespacesToImport = new LinkedHashSet<>();

        final String namespacePrefix = customModel.getNamespacePrefix();
        final String namespaceURI = customModel.getNamespaceUri();
        // Construct the model name
        final String name = constructName(customModel.getName(), namespacePrefix);

        M2Model model = M2Model.createModel(name);
        model.createNamespace(namespaceURI, namespacePrefix);
        model.setDescription(customModel.getDescription());
        String author = customModel.getAuthor();
        if (author == null)
        {
            author = getCurrentUserFullName();
        }
        model.setAuthor(author);

        // Types
        if(types != null)
        {
            for(CustomType type : types)
            {
               validateName(type.getName(), TYPE_NAME_NULL_ERR);
               M2Type m2Type = model.createType(constructName(type.getName(), namespacePrefix));
               m2Type.setDescription(type.getDescription());
               m2Type.setTitle(type.getTitle());
               setParentName(m2Type, type.getParentName(), namespacesToImport, namespacePrefix);
               setM2Properties(m2Type, type.getProperties(), namespacePrefix, namespacesToImport);
            }
        }

        // Aspects
        if(aspects != null)
        {
            for(CustomAspect aspect : aspects)
            {
                validateName(aspect.getName(), ASPECT_NAME_NULL_ERR);
                M2Aspect m2Aspect = model.createAspect(constructName(aspect.getName(), namespacePrefix));
                m2Aspect.setDescription(aspect.getDescription());
                m2Aspect.setTitle(aspect.getTitle());
                setParentName(m2Aspect, aspect.getParentName(), namespacesToImport, namespacePrefix);
                setM2Properties(m2Aspect, aspect.getProperties(), namespacePrefix, namespacesToImport);
            }
        }

        // Constraints
        if(constraints != null)
        {
            for (CustomModelConstraint constraint : constraints)
            {
                validateName(constraint.getName(), CONSTRAINT_NAME_NULL_ERR);
                final String constraintName = constructName(constraint.getName(), namespacePrefix);
                M2Constraint m2Constraint = model.createConstraint(constraintName, constraint.getType());
                // Set title, desc and parameters
                setConstraintOtherData(constraint, m2Constraint, null);
            }
        }

        // Add imports
        for (Pair<String, String> uriPrefix : namespacesToImport)
        {
            // Don't import the already defined namespace
            if (!namespaceURI.equals(uriPrefix.getFirst()))
            {
                model.createImport(uriPrefix.getFirst(), uriPrefix.getSecond());
            }
        }

        return model;
    }

    private void setConstraintOtherData(CustomModelConstraint constraint, M2Constraint m2Constraint, String propDataType)
    {
        if (m2Constraint.getType() == null)
        {
            throw new InvalidArgumentException("cmm.rest_api.constraint_type_null");
        }

        ConstraintValidator constraintValidator = ConstraintValidator.findByType(m2Constraint.getType());
        if (propDataType != null)
        {
            // Check if the constraint can be used with given data type
            constraintValidator.validateUsage(prefixedStringToQname(propDataType));
        }

        m2Constraint.setTitle(constraint.getTitle());
        m2Constraint.setDescription(constraint.getDescription());
        for (CustomModelNamedValue parameter : constraint.getParameters())
        {
            validateName(parameter.getName(), "cmm.rest_api.constraint_parameter_name_null");
            if (parameter.getListValue() != null)
            {
                if (propDataType != null && "allowedValues".equals(parameter.getName()))
                {
                    validateListConstraint(parameter.getListValue(), propDataType);
                }
                m2Constraint.createParameter(parameter.getName(), parameter.getListValue());
            }
            else
            {
                constraintValidator.validate(parameter.getName(), parameter.getSimpleValue());
                m2Constraint.createParameter(parameter.getName(), parameter.getSimpleValue());
            }
        }
    }

    /*
     * List constraint is a special case, so can't use the ConstraintValidator.
     */
    private void validateListConstraint(List<String> listValue, String propDataType)
    {
        for (String value : listValue)
        {
            try
            {
                // validate list values
                this.valueDataTypeValidator.validateValue(propDataType, value);
            }
            catch (Exception ex)
            {
                throw new InvalidArgumentException(ex.getMessage());
            }
        }
    }

    private void setM2Properties(M2Class m2Class, List<CustomModelProperty> properties, String namespacePrefix,
                Set<Pair<String, String>> namespacesToImport)
    {
        if (properties != null)
        {
            for (CustomModelProperty prop : properties)
            {
                validateName(prop.getName(), "cmm.rest_api.property_name_null");
                M2Property m2Property = m2Class.createProperty(constructName(prop.getName(), namespacePrefix));
                m2Property.setTitle(prop.getTitle());
                m2Property.setDescription(prop.getDescription());
                m2Property.setMandatory(prop.isMandatory());
                m2Property.setMandatoryEnforced(prop.isMandatoryEnforced());
                m2Property.setMultiValued(prop.isMultiValued());

                String dataType = prop.getDataType();
                // Default type is d:text
                if (StringUtils.isBlank(dataType))
                {
                    dataType = DEFAULT_DATA_TYPE;
                }
                else
                {
                    if (!dataType.contains(":"))
                    {
                        throw new InvalidArgumentException("cmm.rest_api.property_datatype_invalid", new Object[] { dataType });
                    }
                }
                namespacesToImport.add(resolveToUriAndPrefix(dataType));
                try
                {
                    // validate default values
                    this.valueDataTypeValidator.validateValue(dataType, prop.getDefaultValue());
                }
                catch (Exception ex)
                {
                    throw new InvalidArgumentException(ex.getMessage());
                }
                m2Property.setType(dataType);
                m2Property.setDefaultValue(prop.getDefaultValue());
 
                // Set indexing options
                m2Property.setIndexed(prop.isIndexed());
                // SHA-1234
                // This 'if' statement can be removed when we fix the Solr schema
                // so it can support boolean data type.
                if (!BOOLEAN_DATA_TYPE.equals(dataType))
                {
                    if (Facetable.TRUE == prop.getFacetable())
                    {
                        m2Property.setFacetable(true);
                    }
                    else if (Facetable.FALSE == prop.getFacetable())
                    {
                        m2Property.setFacetable(false);
                    }
                }
                m2Property.setIndexTokenisationMode(prop.getIndexTokenisationMode());

                // Check for constraints
                List<String> constraintRefs = prop.getConstraintRefs();
                List<CustomModelConstraint> constraints = prop.getConstraints();
                if (constraintRefs.size() > 0)
                {
                    for (String ref : constraintRefs)
                    {
                        Pair<String, String> prefixLocalName = splitPrefixedQName(ref);
                        if (!namespacePrefix.equals(prefixLocalName.getFirst()))
                        {
                            throw new ConstraintViolatedException(I18NUtil.getMessage("cmm.rest_api.constraint_ref_not_defined", ref));
                        }
                        m2Property.addConstraintRef(ref);
                    }
                }
                if(constraints.size() > 0)
                {
                    for (CustomModelConstraint modelConstraint : constraints)
                    {
                        String constraintName = null;
                        if (modelConstraint.getName() != null)
                        {
                            validateName(modelConstraint.getName(), CONSTRAINT_NAME_NULL_ERR);
                            constraintName = constructName(modelConstraint.getName(), namespacePrefix);
                        }
                        M2Constraint m2Constraint = m2Property.addConstraint(constraintName, modelConstraint.getType());
                        // Set title, desc and parameters
                        setConstraintOtherData(modelConstraint, m2Constraint, dataType);
                    }
                }
            }
        }
    }

    private void validateBasicModelInput(CustomModel customModel)
    {
        // validate model name
        validateName(customModel.getName(), MODEL_NAME_NULL_ERR);

        // validate model namespace prefix
        validateName(customModel.getNamespacePrefix(), "cmm.rest_api.model_namespace_prefix_null");

        // validate model namespace URI
        if (customModel.getNamespaceUri() == null)
        {
            throw new InvalidArgumentException("cmm.rest_api.model_namespace_uri_null");
        }
        else
        {
            Matcher matcher = URI_PATTERN.matcher(customModel.getNamespaceUri());
            if (!matcher.find())
            {
                throw new InvalidArgumentException("cmm.rest_api.model_namespace_uri_invalid");
            }
        }
    }

    private void validateName(String name, String errMsgId)
    {
        if (name == null)
        {
            if (errMsgId == null)
            {
                errMsgId = InvalidArgumentException.DEFAULT_MESSAGE_ID;
            }
            throw new InvalidArgumentException(errMsgId);
        }
        else
        {
            Matcher matcher = NAME_PATTERN.matcher(name);
            if (!matcher.find())
            {
                throw new InvalidArgumentException("cmm.rest_api.input_validation_err", new Object [] {name});
            }
        }
    }

    /**
     * Checks the current user access rights and throws
     * {@link PermissionDeniedException} if the user is not a member of the
     * ALFRESCO_MODEL_ADMINISTRATORS group
     */
    private void validateCurrentUser()
    {
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        if (!customModelService.isModelAdmin(currentUser))
        {
            throw new PermissionDeniedException();
        }
    }

    /**
     * Gets the fully authenticated user's full name
     * 
     * @return user's full name or the user's id if the full name dose not exit
     */
    protected String getCurrentUserFullName()
    {
        String userName = AuthenticationUtil.getFullyAuthenticatedUser();
        NodeRef personRef = personService.getPerson(userName, false);
 
        String firstName = (String) nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
        String lastName = (String) nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);

        String fullName = (firstName != null ? firstName + " " : "") + (lastName != null ? lastName : "");

        return ((StringUtils.isBlank(fullName) ? userName : fullName)).trim();
    }

    private String constructName(String name, String prefix)
    {
        return new StringBuilder(100).append(prefix).append(QName.NAMESPACE_PREFIX).append(name).toString();
    }

    /**
     * Gets the namespace URI and prefix from the parent's name, provided that the
     * given name is of a valid format. The valid format consist of a
     * <i>namespace prefix</i>, a <i>colon</i> and a <i>name</i>. <b>E.g. sys:localized</b>
     * 
     * @param parentName the parent name
     * @return a pair of namespace URI and prefix object
     */
    protected Pair<String, String> resolveToUriAndPrefix(String parentName)
    {
        QName qName = prefixedStringToQname(parentName);
        Collection<String> prefixes = namespaceService.getPrefixes(qName.getNamespaceURI());
        if (prefixes.size() == 0)
        {
            throw new InvalidArgumentException("cmm.rest_api.prefix_not_registered", new Object[] { qName.getNamespaceURI() });
        }
        String prefix = prefixes.iterator().next();
        return new Pair<String, String>(qName.getNamespaceURI(), prefix);
    }

    /**
     * Creates {@link QName} from a valid prefixed string.
     */
    private QName prefixedStringToQname(String prefixedQName)
    {
        try
        {
            return QName.createQName(prefixedQName, namespaceService);
        }
        catch (Exception ex)
        {
            String msg = ex.getMessage();
            if (msg == null)
            {
                msg = "";
            }
            throw new InvalidArgumentException("cmm.rest_api.prefixed_qname_invalid", new Object[] { prefixedQName, msg });
        }
    }

    /**
     * Validates and sets the type's or aspect's parent name
     * 
     * @param m2Class the {@link M2Type} or {@link M2Aspect} object
     * @param parentPrefixedName the parent prefixed name. E.g. <code>prefix:localName</code>
     * @param namespacesToImport the {@link Set} of namespace pairs to import
     * @param modelNamespacePrefix the model namespace prefix
     */
    private void setParentName(M2Class m2Class, String parentPrefixedName, Set<Pair<String, String>> namespacesToImport, String modelNamespacePrefix)
    {
        if (StringUtils.isBlank(parentPrefixedName))
        {
            return;
        }

        Pair<String, String> prefixLocaNamePair = splitPrefixedQName(parentPrefixedName);
        if (!modelNamespacePrefix.equals(prefixLocaNamePair.getFirst()))
        {
            // Add to the list of imports
            Pair<String, String> uriPrefixPair = resolveToUriAndPrefix(parentPrefixedName);
            namespacesToImport.add(uriPrefixPair);
        }
        m2Class.setParentName(parentPrefixedName);
    }

    private void validateTypeAspectParent(AbstractClassModel typeAspect, CustomModel existingModel)
    {
        String parentPrefixedName = typeAspect.getParentName();
        if (StringUtils.isBlank(parentPrefixedName))
        {
            return;
        }

        Pair<String, String> prefixLocaNamePair = splitPrefixedQName(parentPrefixedName);
        String parentPrefix = prefixLocaNamePair.getFirst();
        String parentLocalName = prefixLocaNamePair.getSecond();

        // Validate parent prefix and localName
        // We know that the values are not null, we just check against the defined RegEx
        validateName(parentPrefix, null);
        validateName(parentLocalName, null);

        final boolean isAspect = (typeAspect instanceof CustomAspect);
        ClassDefinition classDefinition = null;
        QName qname = null;
        if (existingModel.getNamespacePrefix().equals(parentPrefix))
        {
            // Check for types/aspects within the model
            qname = QName.createQName(existingModel.getNamespaceUri(), parentLocalName);
            classDefinition = (isAspect) ? customModelService.getCustomAspect(qname) : customModelService.getCustomType(qname);
        }
        else
        {
            // Make sure the namespace URI and Prefix are registered
            Pair<String, String> uriPrefixPair = resolveToUriAndPrefix(parentPrefixedName);

            qname = QName.createQName(uriPrefixPair.getFirst(), parentLocalName);
            classDefinition = (isAspect) ? dictionaryService.getAspect(qname) : dictionaryService.getType(qname);
        }

        if (classDefinition == null)
        {
            String msgId = (isAspect) ? "cmm.rest_api.aspect_parent_not_exist" : "cmm.rest_api.type_parent_not_exist";
            throw new ConstraintViolatedException(I18NUtil.getMessage(msgId, parentPrefixedName));
        }
        else
        {
            checkCircularDependency(classDefinition.getModel(), existingModel, parentPrefixedName);
        }
    }

    /**
     * Validates models circular dependencies
     * <p>E.g. if {@literal B -> A} denotes  model B depends on model A, then {@link ConstraintViolatedException} must be thrown for following:
     * <li> if {@literal B -> A}, then {@literal A -> B} must throw exception </li>
     * <li> if {@literal B -> A} and {@literal C -> B}, then {@literal A -> C} must throw exception </li>
     * <li> if {@literal B -> A} and {@literal C -> B} and {@literal D -> C}, then {@literal A -> D} must throw exception </li>
     * @param modelDefinition the model which has a reference to the model containing the {@code parentPrefixedName}
     * @param existingModel the model being updated
     * @param parentPrefixedName the type/aspect parent name
     */
    private void checkCircularDependency(ModelDefinition modelDefinition, CustomModel existingModel, String parentPrefixedName)
    {
        for (NamespaceDefinition importedNamespace : modelDefinition.getImportedNamespaces())
        {
            ModelDefinition md = null;
            if ((md = customModelService.getCustomModelByUri(importedNamespace.getUri())) != null)
            {
                if (existingModel.getNamespaceUri().equals(importedNamespace.getUri()))
                {
                    String msg = I18NUtil.getMessage("cmm.rest_api.circular_dependency_err", parentPrefixedName, existingModel.getName());
                    throw new ConstraintViolatedException(msg);
                }
                checkCircularDependency(md, existingModel, parentPrefixedName);
            }
        }
    }

    /**
     * Returns the qualified name of the following format
     * <code>prefix:localName</code>, as a pair of (prefix, localName)
     * 
     * @param prefixedQName the prefixed name. E.g. <code>prefix:localName</code>
     * @return {@link Pair} of (prefix, localName)
     */
    private Pair<String, String> splitPrefixedQName(String prefixedQName)
    {
        // index 0 => prefix and index 1 => local name
        String[] prefixLocalName = QName.splitPrefixedQName(prefixedQName);

        if (NamespaceService.DEFAULT_PREFIX.equals(prefixLocalName[0]))
        {
            throw new InvalidArgumentException("cmm.rest_api.prefixed_qname_invalid_format", new Object[] { prefixedQName });
        }

        return new Pair<String, String>(prefixLocalName[0], prefixLocalName[1]);
    }

    private CustomModelDefinition updateModel(ModelDetails modelDetails, String errorMsg)
    {
        M2Model m2Model = convertToM2Model(modelDetails);
        try
        {
            CustomModelDefinition modelDef = customModelService.updateCustomModel(modelDetails.getModel().getName(), m2Model, modelDetails.isActive());
            return modelDef;
        }
        catch (CustomModelConstraintException mce)
        {
            throw new ConstraintViolatedException(mce.getMessage());
        }
        catch (InvalidCustomModelException iex)
        {
            throw new InvalidArgumentException(iex.getMessage());
        }
        catch (Exception ex)
        {
            if (ex.getMessage() != null)
            {
                errorMsg = ex.getMessage();
            }
            throw new ApiException(errorMsg, ex);
        }
    }

    private void mergeProperties(AbstractClassModel existingDetails, AbstractClassModel newDetails, Parameters parameters, boolean isModelActive)
    {
        validateList(newDetails.getProperties(), "cmm.rest_api.properties_empty_null");

        // Transform existing properties into a map
        Map<String, CustomModelProperty> existingProperties = transformToMap(existingDetails.getProperties(), toNameFunction());

        // Transform new properties into a map
        Map<String, CustomModelProperty> newProperties = transformToMap(newDetails.getProperties(), toNameFunction());

        String propName = parameters.getParameter(PARAM_UPDATE_PROP);
        // (propName == null) => property create request
        if (propName == null)
        {
            // As this is a create request, check for duplicate properties
            for (String name : newProperties.keySet())
            {
                if (existingProperties.containsKey(name))
                {
                    throw new ConstraintViolatedException(I18NUtil.getMessage("cmm.rest_api.property_create_name_already_in_use", name));
                }
            }
        }
        else
        {// Update request
            CustomModelProperty existingProp = existingProperties.get(propName);
            if (existingProp == null)
            {
                throw new EntityNotFoundException(propName);
            }

            CustomModelProperty modifiedProp = newProperties.get(propName);
            if (modifiedProp == null)
            {
                throw new InvalidArgumentException("cmm.rest_api.property_update_prop_not_found", new Object[] { propName });
            }

            existingProp.setTitle(modifiedProp.getTitle());
            existingProp.setDescription(modifiedProp.getDescription());
            existingProp.setDefaultValue(modifiedProp.getDefaultValue());
            existingProp.setConstraintRefs(modifiedProp.getConstraintRefs());
            existingProp.setConstraints(modifiedProp.getConstraints());
            if (isModelActive)
            {
                validateActivePropertyUpdate(existingProp, modifiedProp);
            }
            existingProp.setDataType(modifiedProp.getDataType());
            existingProp.setMandatory(modifiedProp.isMandatory());
            existingProp.setMandatoryEnforced(modifiedProp.isMandatoryEnforced());
            existingProp.setMultiValued(modifiedProp.isMultiValued());
        }
        // Override properties
        existingProperties.putAll(newProperties);
        existingDetails.setProperties(new ArrayList<>(existingProperties.values()));
    }

    /**
     * A helper method to throw a more informative exception (for an active model) rather than depending on the
     * {@link org.alfresco.repo.dictionary.ModelValidatorImpl#validateModel}
     * generic exception.
     */
    private void validateActivePropertyUpdate(CustomModelProperty existingProp, CustomModelProperty newProp)
    {
        if (!StringUtils.equals(existingProp.getDataType(), newProp.getDataType()))
        {
            throw new ConstraintViolatedException("cmm.rest_api.property_change_datatype_err");
        }
        if (existingProp.isMandatory() != newProp.isMandatory())
        {
            throw new ConstraintViolatedException("cmm.rest_api.property_change_mandatory_opt_err");
        }
        if (existingProp.isMandatoryEnforced() != newProp.isMandatoryEnforced())
        {
            throw new ConstraintViolatedException("cmm.rest_api.property_change_mandatory_enforced_opt_err");
        }
        if (existingProp.isMultiValued() != newProp.isMultiValued())
        {
            throw new ConstraintViolatedException("cmm.rest_api.property_change_multi_valued_opt_err");
        }
    }

    private void deleteProperty(AbstractClassModel existingClassModel, String propertyName)
    {
        // Transform existing properties into a map
        Map<String, CustomModelProperty> existingProperties = transformToMap(existingClassModel.getProperties(), toNameFunction());
        if (!existingProperties.containsKey(propertyName))
        {
            throw new EntityNotFoundException(propertyName);
        }
        existingProperties.remove(propertyName);
        existingClassModel.setProperties(new ArrayList<>(existingProperties.values()));
    }

    private void validateList(List<?> list, String errorMsg)
    {
        if (CollectionUtils.isEmpty(list))
        {
            throw new InvalidArgumentException(errorMsg);
        }
    }

    private static <K, V> Map<K, V> transformToMap(Collection<V> collection, Function<? super V, K> function)
    {
        Map<K, V> map = new HashMap<>(collection.size());

        for (V item : collection)
        {
            map.put(function.apply(item), item);
        }
        return map;
    }

    private static <K, V> Map<K, V> removeRightEntries(Map<K, V> leftMap, Map<K, V> rightMap)
    {
        Map<K, V> result = new HashMap<>(leftMap);
        for (K key : rightMap.keySet())
        {
            result.remove(key);
        }
        return result;
    }

    private void validateTypeAspectDelete(Collection<? extends AbstractClassModel> list, String classPrefixedName)
    {
        for(AbstractClassModel acm : list)
        {
            if(classPrefixedName.equals(acm.getParentName()))
            {
                throw new ConstraintViolatedException(I18NUtil.getMessage("cmm.rest_api.aspect_type_cannot_delete", classPrefixedName, acm.getPrefixedName()));
            }
        }
    }

    private boolean hasSelectProperty(Parameters parameters, String param)
    {
        return parameters.getSelectedProperties().contains(param);
    }

    private static Function<AbstractCommonDetails, String> toNameFunction()
    {
        return new Function<AbstractCommonDetails, String>()
        {
            @Override
            public String apply(AbstractCommonDetails details)
            {
                return details.getName();
            }
        };
    }

    private <T extends AbstractCommonDetails> T getObjectByName(Collection<T> collection, String name)
    {
        for (T details : collection)
        {
            if (details.getName().equals(name))
            {
                return details;
            }
        }
        return null;
    }

    public class ModelDetails
    {
        private CustomModel model;
        private boolean active;
        private List<CustomType> types;
        private List<CustomAspect> aspects;
        private List<CustomModelConstraint> modelDefinedConstraints;

        public ModelDetails(CustomModelDefinition modelDefinition)
        {
            this.model = new CustomModel(modelDefinition);
            this.active = modelDefinition.isActive();
            this.types = convertToCustomTypes(modelDefinition.getTypeDefinitions(), false);
            this.aspects = convertToCustomAspects(modelDefinition.getAspectDefinitions(), false);
            this.modelDefinedConstraints = convertToCustomModelConstraints(modelDefinition.getModelDefinedConstraints());
        }

        public CustomModel getModel()
        {
            return this.model;
        }

        public void setModel(CustomModel model)
        {
            this.model = model;
        }

        public List<CustomType> getTypes()
        {
            return this.types;
        }

        public void setTypes(List<CustomType> types)
        {
            this.types = types;
        }

        public List<CustomAspect> getAspects()
        {
            return this.aspects;
        }

        public void setAspects(List<CustomAspect> aspects)
        {
            this.aspects = aspects;
        }

        public List<CustomModelConstraint> getModelDefinedConstraints()
        {
            return this.modelDefinedConstraints;
        }

        public void setModelDefinedConstraints(List<CustomModelConstraint> modelDefinedConstraints)
        {
            this.modelDefinedConstraints = modelDefinedConstraints;
        }

        public boolean isActive()
        {
            return this.active;
        }
    }

    /**
     * Constraint validator
     *
     * @author Jamal Kaabi-Mofrad
     */
    public enum ConstraintValidator
    {
        REGEX
        {
            @Override
            public void validate(String parameterName, String value)
            {
                if ("expression".equals(parameterName))
                {
                    try
                    {
                        Pattern.compile(value);
                    }
                    catch (Exception ex)
                    {
                        throw new InvalidArgumentException("cmm.rest_api.regex_constraint_invalid_expression", new Object[] { value });
                    }
                }
            }
        },
        MINMAX
        {
            @Override
            public void validate(String parameterName, String value)
            {
                double parsedValue;
                try
                {
                    parsedValue = Double.parseDouble(value);
                }
                catch (Exception ex)
                {
                    throw new InvalidArgumentException("cmm.rest_api.minmax_constraint_invalid_parameter", new Object[] { value, parameterName });
                }
                // SHA-1126. We check for the Double.MIN_VALUE to be consistent with NumericRangeConstraint.minValue
                if("maxValue".equalsIgnoreCase(parameterName) && parsedValue < Double.MIN_VALUE)
                {
                    throw new InvalidArgumentException("cmm.rest_api.minmax_constraint_invalid_max_value");
                }
            }

            @Override
            public void validateUsage(QName propDataType)
            {
                if (propDataType != null && !(DataTypeDefinition.INT.equals(propDataType)
                            || DataTypeDefinition.LONG.equals(propDataType)
                            || DataTypeDefinition.FLOAT.equals(propDataType)
                            || DataTypeDefinition.DOUBLE.equals(propDataType)))
                {
                    throw new InvalidArgumentException("cmm.rest_api.minmax_constraint_invalid_use");
                }
            }
        },
        LENGTH
        {
            @Override
            public void validate(String parameterName, String value)
            {
                try
                {
                    Integer.parseInt(value);
                }
                catch (Exception ex)
                {
                    throw new InvalidArgumentException("cmm.rest_api.length_constraint_invalid_parameter", new Object[] { value, parameterName });
                }
            }

            @Override
            public void validateUsage(QName propDataType)
            {
                if (propDataType != null && !(DataTypeDefinition.TEXT.equals(propDataType)
                            || DataTypeDefinition.MLTEXT.equals(propDataType)
                            || DataTypeDefinition.CONTENT.equals(propDataType)))
                {
                    throw new InvalidArgumentException("cmm.rest_api.length_constraint_invalid_use");
                }
            }
        },
        DUMMY_CONSTRAINT
        {
            @Override
            public void validate(String parameterName, String value)
            {
                // nothing to do
            }
        };

        public abstract void validate(String parameterName, String value);

        public void validateUsage(QName propDataType)
        {
            return; // nothing to do
        }

        public static ConstraintValidator findByType(String constraintType)
        {
            for (ConstraintValidator c : values())
            {
                if (c.name().equals(constraintType))
                {
                    return c;
                }
            }
            return DUMMY_CONSTRAINT;
        }
    }

    @Override
    public CustomModel createCustomModel(M2Model m2Model)
    {
        // Check the current user is authorised to import the custom model
        validateCurrentUser();

        validateImportedM2Model(m2Model);

        CompiledModel compiledModel = null;
        try
        {
            compiledModel = customModelService.compileModel(m2Model);
        }
        catch (CustomModelConstraintException mce)
        {
            throw new ConstraintViolatedException(mce.getMessage());
        }
        catch (InvalidCustomModelException iex)
        {
            throw new InvalidArgumentException(iex.getMessage());
        }

        ModelDefinition modelDefinition = compiledModel.getModelDefinition();
        CustomModel customModel = new CustomModel();
        customModel.setName(modelDefinition.getName().getLocalName());
        customModel.setAuthor(modelDefinition.getAuthor());
        customModel.setDescription(modelDefinition.getDescription(dictionaryService));
        customModel.setStatus(ModelStatus.DRAFT);
        NamespaceDefinition nsd = modelDefinition.getNamespaces().iterator().next();
        customModel.setNamespaceUri(nsd.getUri());
        customModel.setNamespacePrefix(nsd.getPrefix());

        List<CustomType> customTypes = convertToCustomTypes(compiledModel.getTypes(), false);
        List<CustomAspect> customAspects = convertToCustomAspects(compiledModel.getAspects(), false);

        List<ConstraintDefinition> constraintDefinitions = CustomModelDefinitionImpl.removeInlineConstraints(compiledModel);
        List<CustomModelConstraint> customModelConstraints = convertToCustomModelConstraints(constraintDefinitions);

        customModel.setTypes(customTypes);
        customModel.setAspects(customAspects);
        customModel.setConstraints(customModelConstraints);

        return createCustomModelImpl(customModel, false);
    }

    private void validateImportedM2Model(M2Model m2Model)
    {
        List<M2Namespace> namespaces = m2Model.getNamespaces();
        if (namespaces.size() > 1)
        {
            throw new ConstraintViolatedException(I18NUtil.getMessage("cmm.rest_api.model.import_namespace_multiple_found", namespaces.size()));
        }
        else if (namespaces.isEmpty())
        {
            throw new ConstraintViolatedException("cmm.rest_api.model.import_namespace_undefined");
        }

        checkUnsupportedModelElements(m2Model.getTypes());
        checkUnsupportedModelElements(m2Model.getAspects());
    }

    private void checkUnsupportedModelElements(Collection<? extends M2Class> m2Classes)
    {
        for (M2Class cls : m2Classes)
        {
            if (cls.getAssociations().size() > 0)
            {
                throw new ConstraintViolatedException("cmm.rest_api.model.import_associations_unsupported");
            }
            if (cls.getPropertyOverrides().size() > 0)
            {
                throw new ConstraintViolatedException("cmm.rest_api.model.import_overrides_unsupported");
            }
            if (cls.getMandatoryAspects().size() > 0)
            {
                throw new ConstraintViolatedException("cmm.rest_api.model.import_mandatory_aspects_unsupported");
            }
            if(cls.getArchive() != null)
            {
                throw new ConstraintViolatedException("cmm.rest_api.model.import_archive_unsupported");
            }
            if(cls.getIncludedInSuperTypeQuery() != null)
            {
                throw new ConstraintViolatedException("cmm.rest_api.model.import_includedInSuperTQ_unsupported");
            }
        }
    }
}
