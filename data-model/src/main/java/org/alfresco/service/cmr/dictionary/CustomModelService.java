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

package org.alfresco.service.cmr.dictionary;

import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.dictionary.CompiledModel;
import org.alfresco.repo.dictionary.CustomModelsInfo;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Custom model service configuration API.
 *
 * @author Jamal Kaabi-Mofrad
 */
public interface CustomModelService
{
    /**
     * Gets custom model
     *
     * @param modelFileName the name of the custom model to retrieve
     * @return the {@code CustomModelDefinition} (or null, if it doesn't exist)
     */
    public CustomModelDefinition getCustomModel(String modelFileName);

    /**
     * Gets custom model
     *
     * @param namespaceUri the namespace URI defined within the model
     * @return the {@code ModelDefinition} (or null, if it doesn't exist)
     */
    public ModelDefinition getCustomModelByUri(String namespaceUri);

    /**
     * Gets a paged list of custom models
     *
     * @param pagingRequest paging request
     * @return paged list of {@code CustomModelDefinition}
     */
    public PagingResults<CustomModelDefinition> getCustomModels(PagingRequest pagingRequest);

    /**
     * Gets custom aspect
     *
     * @param name the name of the custom aspect to retrieve
     * @return the {@code AspectDefinition} (or null, if it doesn't exist)
     */
    public AspectDefinition getCustomAspect(QName name);

    /**
     * Get a paged list of custom aspects
     *
     * @param pagingRequest paging request
     * @return paged list of {@code AspectDefinition}
     */
    public PagingResults<AspectDefinition> getAllCustomAspects(PagingRequest pagingRequest);

    /**
     * Gets custom type
     *
     * @param name the name of the custom type to retrieve
     * @return the {@code TypeDefinition} (or null, if it doesn't exist)
     */
    public TypeDefinition getCustomType(QName name);

    /**
     * Gets a paged list of custom types
     *
     * @param pagingRequest paging request
     * @return paged list of {@code TypeDefinition}
     */
    public PagingResults<TypeDefinition> getAllCustomTypes(PagingRequest pagingRequest);

    /**
     * Gets custom model node reference
     *
     * @param modelFileName the name of the custom model to retrieve
     * @return the {@code NodeRef} of the custom model (or null, if it doesn't exist)
     */
    public NodeRef getModelNodeRef(String modelFileName);

    /**
     * Creates custom model
     *
     * @param m2Model the {@code M2Model} object
     * @param activate whether the model should be activated or not
     * @return the created model definition as a {@code CustomModelDefinition} object
     */
    public CustomModelDefinition createCustomModel(M2Model m2Model, boolean activate);

    /**
     * Indicates whether the specified user is a model-administrator or not.
     * <p>
     * Note: The super/repo admin is considered to be a model-administrator too.
     *
     * @param userName The user name
     * @return true if the specified user is a model-administrator, false otherwise
     */
    public boolean isModelAdmin(String userName);

    /**
     * Activates custom model
     * @see {@link org.alfresco.service.cmr.admin.RepoAdminService#activateModel(String)}
     *
     * @param modelFileName the name of the custom model
     */
    public void activateCustomModel(String modelFileName);

    /**
     * Deactivates custom model
     * @see {@link org.alfresco.service.cmr.admin.RepoAdminService#deactivateModel(String)}
     *
     * @param modelFileName the name of the custom model
     */
    public void deactivateCustomModel(String modelFileName);

    /**
     * Updates custom model
     *
     * @param modelFileName the model name
     * @param m2Model the {@code M2Model} object
     * @param activate whether the model should be activated or not
     * @return the updated model definition as a {@code CustomModelDefinition} object
     */
    public CustomModelDefinition updateCustomModel(String modelFileName, M2Model m2Model, boolean activate);

    /**
     * Deletes custom model.
     * <p>
     * The model can only be deleted if it is inactive
     *
     * @param modelFileName the model name
     */
    public void deleteCustomModel(String modelFileName);

    /**
     * Whether the given URI has already been used or not
     *
     * @param modelNamespaceUri the model namespace URI
     * @return true if the URI has been used, false otherwise
     */
    public boolean isNamespaceUriExists(String modelNamespaceUri);

    /**
     * Whether a model with the given name exists or not
     *
     * @param modelFileName the model name
     * @return true if the model exists, false otherwise
     */
    public boolean isModelExists(String modelFileName);

    /**
     * Whether the given namespace prefix has already been used or not
     *
     * @param modelNamespaceUri the model namespace prefix
     * @return true if the prefix has been used, false otherwise
     */
    public boolean isNamespacePrefixExists(String modelNamespacePrefix);

    /**
     * Gets custom constraint
     *
     * @param name the name of the custom constraint to retrieve
     * @return the {@code ConstraintDefinition} (or null, if it doesn't exist)
     */
    public ConstraintDefinition getCustomConstraint(QName name);

    /**
     * Creates a downloadable archive file containing the custom model file and
     * if specified, its associated Share extension module file.
     *
     * @param modelName the model name to be exported
     * @param withAssociatedForm whether Share extension module file should be
     *            included or not
     * @return reference to the node which will contain the archive file
     */
    public NodeRef createDownloadNode(String modelName, boolean withAssociatedForm);

    /**
     * Compiles the {@link M2Model}.
     *
     * @param m2Model the model
     * @return the compiled model of the given m2model
     * @throws CustomModelConstraintException
     * @throws InvalidCustomModelException
     */
    public CompiledModel compileModel(M2Model m2Model);

    /**
     * Gets custom models' information.
     *
     * @return {@code CustomModelsInfo} containing:
     *         <li>number of active models</li>
     *         <li>number of active models' types</li>
     *         <li>number of active models' aspects</li>
     */
    public CustomModelsInfo getCustomModelsInfo();
}
