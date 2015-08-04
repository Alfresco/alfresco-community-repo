/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.rest.api;

import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.rest.api.model.CustomAspect;
import org.alfresco.rest.api.model.CustomModel;
import org.alfresco.rest.api.model.CustomModelConstraint;
import org.alfresco.rest.api.model.CustomModelDownload;
import org.alfresco.rest.api.model.CustomType;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * @author Jamal Kaabi-Mofrad
 */
public interface CustomModels
{
    /**
     * Gets the {@code org.alfresco.rest.api.model.CustomModel} representation for the given model
     * 
     * @param modelName the model name
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @return {@code org.alfresco.rest.api.model.CustomModel} object
     */
    public CustomModel getCustomModel(String modelName, Parameters parameters);

    /**
     * Gets a paged list of all custom models
     * 
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @return a paged list of {@code org.alfresco.rest.api.model.CustomModel} objects
     */
    public CollectionWithPagingInfo<CustomModel> getCustomModels(Parameters parameters);

    /**
     * Creates custom model
     * 
     * @param model the custom model to create
     * @return {@code org.alfresco.rest.api.model.CustomModel} object
     */
    public CustomModel createCustomModel(CustomModel model);

    /**
     * Creates custom model from the imported {@link M2Model}.
     * 
     * @param m2Model the model
     * @return {@code org.alfresco.rest.api.model.CustomModel} object
     */
    public CustomModel createCustomModel(M2Model m2Model);

    /**
     * Updates or activates/deactivates the custom model
     * 
     * @param modelName the model name
     * @param model the custom model to update (JSON payload)
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @return {@code org.alfresco.rest.api.model.CustomModel} object
     */
    public CustomModel updateCustomModel(String modelName, CustomModel model, Parameters parameters);

    /**
     * Deletes the custom model
     * 
     * @param modelName the model name
     */
    public void deleteCustomModel(String modelName);

    /**
     * Gets the {@code org.alfresco.rest.api.model.CustomType} representation of
     * the given model's type
     *
     * @param modelName the model name
     * @param typeName the model's type name
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @return {@code org.alfresco.rest.api.model.CustomType} object
     */
    public CustomType getCustomType(String modelName, String typeName, Parameters parameters);

    /**
     * Gets a paged list of all the given custom model's types
     * 
     * @param modelName the model name
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @return a paged list of {@code org.alfresco.rest.api.model.CustomType} objects
     */
    public CollectionWithPagingInfo<CustomType> getCustomTypes(String modelName, Parameters parameters);

    /**
     * Creates custom model's type
     * 
     * @param modelName the model name
     * @param type the custom type to create within the given model
     * @return {@code org.alfresco.rest.api.model.CustomType} object
     */
    public CustomType createCustomType(String modelName, CustomType type);

    /**
     * Updates the custom model's type
     * 
     * @param modelName the model name
     * @param type the custom model's type to update (JSON payload)
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @return {@code org.alfresco.rest.api.model.CustomType} object
     */
    public CustomType updateCustomType(String modelName, CustomType type, Parameters parameters);

    /**
     * Deletes the custom model's type
     * 
     * @param modelName the model name
     * @param typeName the model's type name
     */
    public void deleteCustomType(String modelName, String typeName);

    /**
     * Gets the {@code org.alfresco.rest.api.model.CustomAspect} representation of
     * the given model's aspect
     *
     * @param modelName the model name
     * @param aspectName the model's aspect name
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @return {@code org.alfresco.rest.api.model.CustomAspect} object
     */
    public CustomAspect getCustomAspect(String modelName, String aspectName, Parameters parameters);

    /**
     * Gets a paged list of all the given custom model's aspects
     * 
     * @param modelName the model name
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @return a paged list of {@code org.alfresco.rest.api.model.CustomAspect} objects
     */
    public CollectionWithPagingInfo<CustomAspect> getCustomAspects(String modelName, Parameters parameters);

    /**
     * Creates custom model's aspect
     * 
     * @param modelName the model name
     * @param aspect the custom aspect to create within the given model
     * @return {@code org.alfresco.rest.api.model.CustomAspect} object
     */
    public CustomAspect createCustomAspect(String modelName, CustomAspect aspect);

    /**
     * Updates the custom model's aspect
     * 
     * @param modelName the model name
     * @param aspect the custom model's aspect to update (JSON payload)
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @return {@code org.alfresco.rest.api.model.CustomAspect} object
     */
    public CustomAspect updateCustomAspect(String modelName, CustomAspect aspect, Parameters parameters);

    /**
     * Deletes the custom model's aspect
     * 
     * @param modelName the model name
     * @param aspectName the model's aspect name
     */
    public void deleteCustomAspect(String modelName, String aspectName);

    /**
     * Gets the {@code org.alfresco.rest.api.model.CustomModelConstraint}
     * representation of the given model's constraint
     * 
     * @param modelName the model name
     * @param constraintName the model's constraint name
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @return {@code org.alfresco.rest.api.model.CustomModelConstraint} object
     */
    public CustomModelConstraint getCustomModelConstraint(String modelName, String constraintName, Parameters parameters);

    /**
     * Gets a paged list of all of the given custom model's constraints
     * 
     * @param modelName the model name
     * @param parameters the {@link Parameters} object to get the parameters passed into the request
     * @return a paged list of {@code org.alfresco.rest.api.model.CustomModelConstraint} objects
     */
    public CollectionWithPagingInfo<CustomModelConstraint> getCustomModelConstraints(String modelName, Parameters parameters);

    /**
     * Creates custom model's constraint
     * 
     * @param modelName the model name
     * @param constraint the custom constraint to create within the given model
     * @return {@code org.alfresco.rest.api.model.CustomModelConstraint} object
     */
    public CustomModelConstraint createCustomModelConstraint(String modelName, CustomModelConstraint constraint);

    /**
     * Starts the creation of a downloadable archive file containing the
     * custom model file and its associated Share extension module file (if requested).
     * 
     * @param modelName the model name
     * @param parameters the {@link Parameters} object to get the parameters
     *            passed into the request
     * @return {@code org.alfresco.rest.api.model.CustomModelDownload} object
     *         containing the archive node reference
     */
    public CustomModelDownload createDownload(String modelName, Parameters parameters);
}
