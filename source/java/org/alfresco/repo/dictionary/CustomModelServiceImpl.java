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

package org.alfresco.repo.dictionary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.PageDetails;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.admin.RepoAdminServiceImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.download.DownloadModel;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.admin.RepoAdminService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.CustomModelDefinition;
import org.alfresco.service.cmr.dictionary.CustomModelException;
import org.alfresco.service.cmr.dictionary.CustomModelService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException.DuplicateDefinitionException;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.XMLUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Custom Model Service Implementation
 *
 * @author Jamal Kaabi-Mofrad
 */
public class CustomModelServiceImpl extends AbstractLifecycleBean implements CustomModelService
{
    private static final Log logger = LogFactory.getLog(CustomModelServiceImpl.class);

    public static final String DEFAULT_CUSTOM_MODEL_ASPECT = "hasAspect('cmm:customModelManagement')";

    public static final QName ASPECT_CUSTOM_MODEL = QName.createQName("http://www.alfresco.org/model/custommodelmanagement/1.0", "customModelManagement");

    public static final String ALFRESCO_MODEL_ADMINISTRATORS_AUTHORITY = "ALFRESCO_MODEL_ADMINISTRATORS";
    public static final String GROUP_ALFRESCO_MODEL_ADMINISTRATORS_AUTHORITY = PermissionService.GROUP_PREFIX
                + ALFRESCO_MODEL_ADMINISTRATORS_AUTHORITY;

    public static final String SHARE_EXT_MODULE_SUFFIX = "_module.xml";

    /** Messages */
    private static final String MSG_NAME_ALREADY_IN_USE = "cmm.service.name_already_in_use";
    private static final String MSG_CREATE_MODEL_ERR = "cmm.service.create_model_err";
    private static final String MSG_UPDATE_MODEL_ERR = "cmm.service.update_model_err";
    private static final String MSG_MULTIPLE_MODELS = "cmm.service.multiple_models";
    private static final String MSG_RETRIEVE_MODEL = "cmm.service.retrieve_model";
    private static final String MSG_MODEL_NOT_EXISTS = "cmm.service.model_not_exists";
    private static final String MSG_NAMESPACE_NOT_EXISTS = "cmm.service.namespace_not_exists";
    private static final String MSG_NAMESPACE_MANY_EXIST = "cmm.service.namespace_many_exist";
    private static final String MSG_NAMESPACE_URI_ALREADY_IN_USE = "cmm.service.namespace_uri_already_in_use";
    private static final String MSG_NAMESPACE_PREFIX_ALREADY_IN_USE = "cmm.service.namespace_prefix_already_in_use";
    private static final String MSG_UNABLE_DELETE_ACTIVE_MODEL = "cmm.service.unable_delete_active_model";
    private static final String MSG_UNABLE_MODEL_DELETE = "cmm.service.unable_model_delete";
    private static final String MSG_UNABLE_MODEL_DEACTIVATE = "cmm.service.unable_model_deactivate";
    private static final String MSG_UNABLE_MODEL_ACTIVATE = "cmm.service.unable_model_activate";
    private static final String MSG_INVALID_MODEL = "cmm.service.invalid_model";
    private static final String MSG_NAMESPACE_ACTIVE_MODEL = "cmm.service.namespace_active_model";
    private static final String MSG_FAILED_DEACTIVATION_TYPE_DEPENDENCY = "cmm.service.failed.deactivation.type.dependency";
    private static final String MSG_FAILED_DEACTIVATION_ASPECT_DEPENDENCY = "cmm.service.failed.deactivation.aspect.dependency";
    private static final String MSG_DOWNLOAD_COPY_MODEL_ERR = "cmm.service.download.create_model_copy_err";
    private static final String MSG_DOWNLOAD_CREATE_SHARE_EXT_ERR = "cmm.service.download.create_share_ext_err";

    private NodeService nodeService;
    private DictionaryDAOImpl dictionaryDAO;
    private ContentService contentService;
    private SearchService searchService;
    private RepositoryLocation repoModelsLocation;
    private NamespaceDAO namespaceDAO;
    private DictionaryService dictionaryService;
    private RetryingTransactionHelper retryingTransactionHelper;
    private RepoAdminService repoAdminService;
    private AuthorityService authorityService;
    private HiddenAspect hiddenAspect;
    private DownloadService downloadService;
    private DownloadStorage downloadStorage;

    private String shareExtModulePath;
    private ConcurrentMap<String, CompiledModel> uriToModelCache = new ConcurrentHashMap<>();


    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setDictionaryDAO(DictionaryDAOImpl dictionaryDAO)
    {
        this.dictionaryDAO = dictionaryDAO;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setRepositoryModelsLocation(RepositoryLocation repoModelsLocation)
    {
        this.repoModelsLocation = repoModelsLocation;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespaceDAO(NamespaceDAO namespaceDAO)
    {
        this.namespaceDAO = namespaceDAO;
    }

    public void setRetryingTransactionHelper(RetryingTransactionHelper retryingTransactionHelper)
    {
        this.retryingTransactionHelper = retryingTransactionHelper;
    }

    public void setRepoAdminService(RepoAdminService repoAdminService)
    {
        this.repoAdminService = repoAdminService;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    public void setHiddenAspect(HiddenAspect hiddenAspect)
    {
        this.hiddenAspect = hiddenAspect;
    }

    public void setDownloadService(DownloadService downloadSerivce)
    {
        this.downloadService = downloadSerivce;
    }

    public void setDownloadStorage(DownloadStorage downloadStorage)
    {
        this.downloadStorage = downloadStorage;
    }

    public void setShareExtModulePath(String shareExtModulePath)
    {
        this.shareExtModulePath = shareExtModulePath;
    }

    /**
     * Checks that all necessary properties and services have been provided.
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "dictionaryDAO", dictionaryDAO);
        PropertyCheck.mandatory(this, "contentService", contentService);
        PropertyCheck.mandatory(this, "searchService", searchService);
        PropertyCheck.mandatory(this, "repoModelsLocation", repoModelsLocation);
        PropertyCheck.mandatory(this, "namespaceDAO", namespaceDAO);
        PropertyCheck.mandatory(this, "dictionaryServicee", dictionaryService);
        PropertyCheck.mandatory(this, "retryingTransactionHelper", retryingTransactionHelper);
        PropertyCheck.mandatory(this, "repoAdminService", repoAdminService);
        PropertyCheck.mandatory(this, "authorityService", authorityService);
        PropertyCheck.mandatory(this, "hiddenAspect", hiddenAspect);
        PropertyCheck.mandatory(this, "shareExtModulePath", shareExtModulePath);
        PropertyCheck.mandatory(this, "downloadService", downloadService);
        PropertyCheck.mandatory(this, "downloadStorage", downloadStorage);
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // Load custom models
        retryingTransactionHelper.doInTransaction(new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                List<CompiledModel> inactiveModels = getAllCustomM2Models(false);
                for (CompiledModel model : inactiveModels)
                {
                    String uri = getModelNamespaceUriPrefix(model.getM2Model()).getFirst();
                    uriToModelCache.putIfAbsent(uri, model);
                }
                return null;
            }
        }, true);
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
        // Nothing to do
    }

    private NodeRef getRootNode()
    {
        StoreRef storeRef = repoModelsLocation.getStoreRef();
        NodeRef rootNode = nodeService.getRootNode(storeRef);
        return rootNode;
    }

    @Override
    public NodeRef getModelNodeRef(String modelName)
    {
        ParameterCheck.mandatoryString("modelName", modelName);

        StringBuilder builder = new StringBuilder(120);
        builder.append(repoModelsLocation.getPath()).append("//.[@cm:name='").append(modelName).append("' and ")
                    .append(RepoAdminServiceImpl.defaultSubtypeOfDictionaryModel).append(']');

        List<NodeRef> nodeRefs = searchService.selectNodes(getRootNode(), builder.toString(), null, namespaceDAO, false);

        if (nodeRefs.size() == 0)
        {
            return null;
        }
        else if (nodeRefs.size() > 1)
        {
            // unexpected: should not find multiple nodes with same name
            throw new CustomModelException(MSG_MULTIPLE_MODELS, new Object[] { modelName });
        }

        NodeRef modelNodeRef = nodeRefs.get(0);
        return modelNodeRef;
    }

    private M2Model getM2Model(final NodeRef modelNodeRef)
    {
        ContentReader reader = contentService.getReader(modelNodeRef, ContentModel.PROP_CONTENT);
        if (reader == null)
        {
            return null;
        }
        InputStream in = reader.getContentInputStream();
        try
        {
            return M2Model.createModel(in);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {
                    logger.error("Failed to close input stream for " + modelNodeRef);
                }
            }
        }
    }

    @Override
    public CustomModelDefinition getCustomModel(String modelName)
    {
        ParameterCheck.mandatoryString("modelName", modelName);

        Pair<CompiledModel, Boolean> compiledModelPair = getCustomCompiledModel(modelName);
        CustomModelDefinition result = (compiledModelPair == null) ? null : new CustomModelDefinitionImpl(
                    compiledModelPair.getFirst(), compiledModelPair.getSecond(), dictionaryService);

        return result;
    }

    @Override
    public ModelDefinition getCustomModelByUri(String namespaceUri)
    {
        ParameterCheck.mandatoryString("namespaceUri", namespaceUri);
        CompiledModel compiledModel = uriToModelCache.get(namespaceUri);

        if (compiledModel != null)
        {
            return compiledModel.getModelDefinition();
        }

        return null;
    }

    /**
     * Returns compiled custom model and whether the model is active or not as a {@code Pair} object
     *
     * @param modelName the name of the custom model to retrieve
     * @return the {@code Pair<CompiledModel, Boolean>} (or null, if it doesn't exist)
     */
    protected Pair<CompiledModel, Boolean> getCustomCompiledModel(String modelName)
    {
        ParameterCheck.mandatoryString("modelName", modelName);

        //TODO add cache
        final NodeRef modelNodeRef = getModelNodeRef(modelName);

        if (modelNodeRef == null || !nodeService.exists(modelNodeRef))
        {
            return null;
        }

        M2Model model = null;
        final boolean isActive = Boolean.TRUE.equals(nodeService.getProperty(modelNodeRef, ContentModel.PROP_MODEL_ACTIVE));
        if (isActive)
        {
            QName modelQName = (QName) nodeService.getProperty(modelNodeRef, ContentModel.PROP_MODEL_NAME);
            if (modelQName == null)
            {
                return null;
            }
            try
            {
                CompiledModel compiledModel = dictionaryDAO.getCompiledModel(modelQName);
                model = compiledModel.getM2Model();
            }
            catch (Exception e)
            {
                throw new CustomModelException(MSG_RETRIEVE_MODEL, new Object[] { modelName }, e);
            }
        }
        else
        {
            model = getM2Model(modelNodeRef);
        }

        Pair<CompiledModel, Boolean> result = (model == null) ? null : new Pair<>(compileModel(model), isActive);

        return result;
    }

    @Override
    public PagingResults<CustomModelDefinition> getCustomModels(PagingRequest pagingRequest)
    {
        ParameterCheck.mandatory("pagingRequest", pagingRequest);

        List<CustomModelDefinition> result = getAllCustomModels();
        return result.isEmpty() ? new EmptyPagingResults<CustomModelDefinition>() : wrapResult(pagingRequest, result);
    }

    protected List<CustomModelDefinition> getAllCustomModels()
    {
        List<CustomModelDefinition> result = new ArrayList<>();

        Collection<QName> models = dictionaryDAO.getModels(true);

        List<String> dictionaryModels = new ArrayList<String>();
        for (QName model : models)
        {
            dictionaryModels.add(model.toPrefixString());
        }

        List<CompiledModel> compiledModels = getAllCustomM2Models(false);
        if (compiledModels.size() > 0)
        {
            for (CompiledModel model : compiledModels)
            {
                // check against models loaded in dictionary
                boolean isActive = false;
                if (dictionaryModels.contains(model.getM2Model().getName()))
                {
                    isActive = true;
                }
                result.add(new CustomModelDefinitionImpl(model, isActive, dictionaryService));
            }
        }

        return result;
    }

    private List<CompiledModel> getAllCustomM2Models(boolean onlyInactiveModels)
    {
        List<CompiledModel> result = new ArrayList<>();

        StringBuilder builder = new StringBuilder(160);
        builder.append(repoModelsLocation.getPath()).append(RepoAdminServiceImpl.CRITERIA_ALL).append("[(")
                    .append(RepoAdminServiceImpl.defaultSubtypeOfDictionaryModel).append(" and ").append(DEFAULT_CUSTOM_MODEL_ASPECT);
        if (onlyInactiveModels)
        {
            builder.append(" and @cm:modelActive='false'");
        }
        builder.append(")]");

        List<NodeRef> nodeRefs = searchService.selectNodes(getRootNode(), builder.toString(), null, namespaceDAO, false,
                    SearchService.LANGUAGE_XPATH);

        if (nodeRefs.size() > 0)
        {
            for (NodeRef nodeRef : nodeRefs)
            {
                try
                {
                    M2Model m2Model = getM2Model(nodeRef);
                    if (m2Model == null)
                    {
                        logger.warn("Couldn't construct M2Model from nodeRef:" + nodeRef);
                        continue;
                    }
                    result.add(compileModel(m2Model));
                }
                catch (Throwable t)
                {
                    logger.warn("Skip model (" + t.getMessage() + ")");
                }
            }
        }

        return result;
    }

    @Override
    public AspectDefinition getCustomAspect(QName name)
    {
        ParameterCheck.mandatory("name", name);

        CompiledModel compiledModel = uriToModelCache.get(name.getNamespaceURI());
        if (compiledModel != null)
        {
            return compiledModel.getAspect(name);
        }

        return null;
    }

    @Override
    public PagingResults<AspectDefinition> getAllCustomAspects(PagingRequest pagingRequest)
    {
        ParameterCheck.mandatory("pagingRequest", pagingRequest);

        List<AspectDefinition> result = new ArrayList<>();
        List<CompiledModel> list = getAllCustomM2Models(false);
        for (CompiledModel model : list)
        {
            result.addAll(model.getAspects());
        }
        return wrapResult(pagingRequest, result);
    }

    @Override
    public TypeDefinition getCustomType(QName name)
    {
        ParameterCheck.mandatory("name", name);

        CompiledModel compiledModel = uriToModelCache.get(name.getNamespaceURI());
        if (compiledModel != null)
        {
            return compiledModel.getType(name);
        }

        return null;
    }

    @Override
    public PagingResults<TypeDefinition> getAllCustomTypes(PagingRequest pagingRequest)
    {
        ParameterCheck.mandatory("pagingRequest", pagingRequest);

        List<TypeDefinition> result = new ArrayList<>();
        List<CompiledModel> list = getAllCustomM2Models(false);
        for (CompiledModel model : list)
        {
            result.addAll(model.getTypes());
        }
        return wrapResult(pagingRequest, result);
    }

    @Override
    public ConstraintDefinition getCustomConstraint(QName name)
    {
        ParameterCheck.mandatory("name", name);

        CompiledModel compiledModel = uriToModelCache.get(name.getNamespaceURI());
        if (compiledModel != null)
        {
            return compiledModel.getConstraint(name);
        }

        return null;
    }

    @Override
    public CustomModelDefinition createCustomModel(M2Model m2Model, boolean activate)
    {
        ParameterCheck.mandatory("m2Model", m2Model);

        // TODO make it more robust
        String modelName = m2Model.getName();
        int colonIndex = modelName.indexOf(QName.NAMESPACE_PREFIX);
        final String modelFileName = (colonIndex == -1) ? modelName : modelName.substring(colonIndex + 1);

        if (isModelExists(modelFileName))
        {
            throw new CustomModelException.ModelExistsException(MSG_NAME_ALREADY_IN_USE, new Object[] { modelFileName });
        }

        // Validate the model namespace URI
        validateModelNamespaceUri(getModelNamespaceUriPrefix(m2Model).getFirst());
        // Validate the model namespace prefix
        validateModelNamespacePrefix(getModelNamespaceUriPrefix(m2Model).getSecond());

        // Return the created model definition
        CompiledModel compiledModel = createUpdateModel(modelFileName, m2Model, activate, MSG_CREATE_MODEL_ERR, false);
        // Add the created model into the cache
        // Note: for now, we only allow one namespace per model (see org.alfresco.rest.api.impl.CustomModelsImpl)
        NamespaceDefinition nsd = compiledModel.getModelDefinition().getNamespaces().iterator().next();
        uriToModelCache.putIfAbsent(nsd.getUri(), compiledModel);

        CustomModelDefinition modelDef = new CustomModelDefinitionImpl(compiledModel, activate, dictionaryService);

        if (logger.isDebugEnabled())
        {
            logger.debug(modelFileName + " model has been created.");
        }
        return modelDef;
    }

    @Override
    public CustomModelDefinition updateCustomModel(String modelFileName, M2Model m2Model, boolean activate)
    {
        ParameterCheck.mandatory("m2Model", m2Model);

        final NodeRef existingModelNodeRef = getModelNodeRef(modelFileName);
        if (existingModelNodeRef == null)
        {
            throw new CustomModelException.ModelDoesNotExistException(MSG_MODEL_NOT_EXISTS, new Object[] { modelFileName });
        }
        // Existing model property and namespace uri-prefix pair
        final boolean isActive = Boolean.TRUE.equals(nodeService.getProperty(existingModelNodeRef, ContentModel.PROP_MODEL_ACTIVE));
        final M2Model existingModel = getM2Model(existingModelNodeRef);
        final Pair<String, String> existingNamespacePair = getModelNamespaceUriPrefix(existingModel);
        // New model namespace uri-prefix pair
        final Pair<String, String> newNamespacePair = getModelNamespaceUriPrefix(m2Model);

        if (isActive && !(existingNamespacePair.equals(newNamespacePair)))
        {
            throw new CustomModelException.ActiveModelConstraintException(MSG_NAMESPACE_ACTIVE_MODEL);
        }

        // if the prefix has changed, then check the new prefix is not in use.
        if (!existingNamespacePair.getSecond().equals(newNamespacePair.getSecond()))
        {
            validateModelNamespacePrefix(newNamespacePair.getSecond());
        }

        // if the URI has changed, then check the new URI is not in use.
        CompiledModel removedCachedModel = null;
        if (!existingNamespacePair.getFirst().equals(newNamespacePair.getFirst()))
        {
            validateModelNamespaceUri(newNamespacePair.getFirst());
            // As the URI has changed and it's valid, remove the old one.
            removedCachedModel = uriToModelCache.remove(existingNamespacePair.getFirst());
        }

        CompiledModel compiledModel = null;
        try
        {
            /*
             * We set the requiresNewTx = true, in order to catch any exception
             * thrown within the low level content model management.
             * For example, deleting a property of an active model, where the
             * property has been applied to a node will cause the
             * ModelValidatorImpl to throw an exception.
             * Without starting a new TX, we can't catch that exception.
             */
            compiledModel = createUpdateModel(modelFileName, m2Model, activate, MSG_UPDATE_MODEL_ERR, true);
        }
        catch (Exception ex)
        {
            // Put back the existing model if it was removed
            if (removedCachedModel != null)
            {
                uriToModelCache.put(existingNamespacePair.getFirst(), removedCachedModel);

                if (logger.isDebugEnabled())
                {
                    logger.debug("Couldn't updated the model [" + modelFileName + "].");
                }
            }
            throw ex;
        }

        NamespaceDefinition nsd = compiledModel.getModelDefinition().getNamespaces().iterator().next();
        uriToModelCache.put(nsd.getUri(), compiledModel);

        CustomModelDefinition modelDef = new CustomModelDefinitionImpl(compiledModel, activate, dictionaryService);

        if (logger.isDebugEnabled())
        {
            logger.debug(modelFileName + " model has been updated.");
        }
        return modelDef;
    }

    private CompiledModel createUpdateModel(final String modelFileName, final M2Model m2Model, final boolean activate, String errMsgId, boolean requiresNewTx)
    {
        // Validate model
        CompiledModel compiledModel = compileModel(m2Model);

        // Validate properties default values
        validatePropsDefaultValues(compiledModel);

        ByteArrayOutputStream xml = new ByteArrayOutputStream();
        m2Model.toXML(xml);
        final InputStream modelStream = new ByteArrayInputStream(xml.toByteArray());

        // Create the model node
        NodeRef nodeRef = doInTransaction(errMsgId, requiresNewTx, new RetryingTransactionCallback<NodeRef>()
        {
            public NodeRef execute() throws Exception
            {
                return repoAdminService.deployModel(modelStream, modelFileName, activate);
            }
        });

        if (!nodeService.hasAspect(nodeRef, ASPECT_CUSTOM_MODEL))
        {
            // Add the 'customModelManagement' marker aspect, to
            // indicate that this model has been created dynamically by this service
            nodeService.addAspect(nodeRef, ASPECT_CUSTOM_MODEL, null);
        }
        // Add hidden aspect
        if (!hiddenAspect.hasHiddenAspect(nodeRef))
        {
            hiddenAspect.hideNode(nodeRef, false, false, false);
        }

        return compiledModel;
    }

    /**
     * Validates the properties' non-null default values against the defined property constraints.
     *
     * @param compiledModel the compiled model
     * @throws CustomModelConstraintException if there is constraint evaluation
     *             exception
     */
    private void validatePropsDefaultValues(CompiledModel compiledModel)
    {
        for (PropertyDefinition propertyDef : compiledModel.getProperties())
        {
            if (propertyDef.getDefaultValue() != null && propertyDef.getConstraints().size() > 0)
            {
                for (ConstraintDefinition constraintDef : propertyDef.getConstraints())
                {
                    Constraint constraint = constraintDef.getConstraint();
                    try
                    {
                        constraint.evaluate(propertyDef.getDefaultValue());
                    }
                    catch (AlfrescoRuntimeException ex)
                    {
                        String message = getRootCauseMsg(ex, false, "cmm.service.constraint.default_prop_value_err");
                        throw new CustomModelException.CustomModelConstraintException(message);
                    }
                }
            }
        }
    }

    @Override
    public CompiledModel compileModel(M2Model m2Model)
    {
        try
        {
            // Validate model dependencies, constraints and etc. before creating a node
            CompiledModel compiledModel = m2Model.compile(dictionaryDAO, namespaceDAO, true);
            return compiledModel;
        }
        catch (Exception ex)
        {
            AlfrescoRuntimeException alf = null;
            if (ex instanceof AlfrescoRuntimeException)
            {
                alf = (AlfrescoRuntimeException) ex;
            }
            else
            {
                alf = AlfrescoRuntimeException.create(ex, ex.getMessage());
            }

            Throwable cause = alf.getRootCause();
            String message = null;

            if (cause instanceof DuplicateDefinitionException)
            {
                message = getRootCauseMsg(cause, false, MSG_INVALID_MODEL);
                throw new CustomModelException.CustomModelConstraintException(message);
            }
            else
            {
                message = getRootCauseMsg(cause, true, null);
                throw new CustomModelException.InvalidCustomModelException(MSG_INVALID_MODEL, new Object[] { message }, ex);
            }
        }
    }

    protected <T> PagingResults<T> wrapResult(PagingRequest pagingRequest, List<T> result)
    {
        final int totalSize = result.size();
        final PageDetails pageDetails = PageDetails.getPageDetails(pagingRequest, totalSize);

        final List<T> page = new ArrayList<>(pageDetails.getPageSize());
        Iterator<T> it = result.iterator();
        for (int counter = 0; counter < pageDetails.getEnd() && it.hasNext(); counter++)
        {
            T element = it.next();
            if (counter < pageDetails.getSkipCount())
            {
                continue;
            }
            if (counter > pageDetails.getEnd() - 1)
            {
                break;
            }
            page.add(element);
        }

        return new PagingResults<T>()
        {
            @Override
            public List<T> getPage()
            {
                return page;
            }

            @Override
            public boolean hasMoreItems()
            {
                return pageDetails.hasMoreItems();
            }

            @Override
            public Pair<Integer, Integer> getTotalResultCount()
            {
                Integer total = Integer.valueOf(totalSize);
                return new Pair<Integer, Integer>(total, total);
            }

            @Override
            public String getQueryExecutionId()
            {
                return null;
            }
        };
    }

    @Override
    public boolean isModelAdmin(String userName)
    {
        if (userName == null)
        {
            return false;
        }
        return this.authorityService.isAdminAuthority(userName)
                    || this.authorityService.getAuthoritiesForUser(userName).contains(GROUP_ALFRESCO_MODEL_ADMINISTRATORS_AUTHORITY);
    }

    @Override
    public void activateCustomModel(String modelName)
    {
        try
        {
            repoAdminService.activateModel(modelName);
        }
        catch (Exception ex)
        {
            throw new CustomModelException(MSG_UNABLE_MODEL_ACTIVATE, new Object[] { modelName }, ex);
        }
    }

    @Override
    public void deactivateCustomModel(final String modelName)
    {
        CustomModelDefinition customModelDefinition = getCustomModel(modelName);
        if (customModelDefinition == null)
        {
            throw new CustomModelException.ModelDoesNotExistException(MSG_MODEL_NOT_EXISTS, new Object[] { modelName });
        }

        Collection<TypeDefinition> modelTypes = customModelDefinition.getTypeDefinitions();
        Collection<AspectDefinition> modelAspects = customModelDefinition.getAspectDefinitions();

        Collection<CompiledModel> allModels = uriToModelCache.values();
        for (CompiledModel cm : allModels)
        {
            // Ignore type/aspect dependency check within the model itself
            if (!customModelDefinition.getName().equals(cm.getModelDefinition().getName()))
            {
                // Check if the type of the model being deactivated is the parent of another model's type
                validateTypeAspectDependency(modelTypes, cm.getTypes());

                // Check if the aspect of the model being deactivated is the parent of another model's aspect
                validateTypeAspectDependency(modelAspects, cm.getAspects());
            }
        }

        // requiresNewTx = true, in order to catch any exception thrown within
        // "DictionaryModelType$DictionaryModelTypeTransactionListener" model validation.
        doInTransaction(MSG_UNABLE_MODEL_DEACTIVATE, true, new RetryingTransactionCallback<Void>()
        {
            public Void execute() throws Exception
            {
                repoAdminService.deactivateModel(modelName);
                return null;
            }
        });
    }

    private void validateTypeAspectDependency(Collection<? extends ClassDefinition> parentDefs, Collection<? extends ClassDefinition> childDefs)
    {
        for (ClassDefinition parentClassDef : parentDefs)
        {
            for (ClassDefinition childClassDef : childDefs)
            {
                if (parentClassDef.getName().equals(childClassDef.getParentName()))
                {
                    Object[] msgParams = new Object[] { parentClassDef.getName().toPrefixString(),
                                childClassDef.getName().toPrefixString(),
                                childClassDef.getModel().getName().getLocalName() };

                    if (parentClassDef instanceof TypeDefinition)
                    {
                        throw new CustomModelException.CustomModelConstraintException(MSG_FAILED_DEACTIVATION_TYPE_DEPENDENCY, msgParams);
                    }
                    else
                    {
                        throw new CustomModelException.CustomModelConstraintException(MSG_FAILED_DEACTIVATION_ASPECT_DEPENDENCY, msgParams);
                    }
                }
            }
        }
    }

    @Override
    public void deleteCustomModel(String modelName)
    {
        NodeRef nodeRef = getModelNodeRef(modelName);
        if (nodeRef == null)
        {
            throw new CustomModelException.ModelDoesNotExistException(MSG_MODEL_NOT_EXISTS, new Object[] { modelName });
        }

        final boolean isActive = Boolean.TRUE.equals(nodeService.getProperty(nodeRef, ContentModel.PROP_MODEL_ACTIVE));
        if (isActive)
        {
            throw new CustomModelException.ActiveModelConstraintException(MSG_UNABLE_DELETE_ACTIVE_MODEL);
        }

        // Get the model first, so it can be used to remove the compiled model from cache
        final M2Model model = getM2Model(nodeRef);
        try
        {
            repoAdminService.undeployModel(modelName);
        }
        catch (Exception ex)
        {
            throw new CustomModelException(MSG_UNABLE_MODEL_DELETE, new Object[] { modelName }, ex);
        }

       // Remove from the cache
        uriToModelCache.remove(getModelNamespaceUriPrefix(model).getFirst());
    }

    @Override
    public boolean isNamespaceUriExists(String modelNamespaceUri)
    {
        ParameterCheck.mandatoryString("modelNamespaceUri", modelNamespaceUri);

        Collection<String> uris = namespaceDAO.getURIs();
        if (uris.contains(modelNamespaceUri))
        {
            return true;
        }

        return uriToModelCache.containsKey(modelNamespaceUri);
    }

    @Override
    public boolean isNamespacePrefixExists(String modelNamespacePrefix)
    {
        ParameterCheck.mandatoryString("modelNamespacePrefix", modelNamespacePrefix);

        Collection<String> uris = namespaceDAO.getPrefixes();
        if (uris.contains(modelNamespacePrefix))
        {
            return true;
        }

        // TODO Should we add a namespace prefix cache here?
        for(CompiledModel cm : uriToModelCache.values())
        {
            Pair<String, String> namespacePair = getModelNamespaceUriPrefix(cm.getM2Model());
            if(modelNamespacePrefix.equals(namespacePair.getSecond()))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isModelExists(String modelFileName)
    {
        NodeRef nodeRef = getModelNodeRef(modelFileName);
        if (nodeRef != null)
        {
            return true;
        }
        // Also check against the bootstrapped models
        for (QName qname : dictionaryService.getAllModels())
        {
            if (qname.getLocalName().equalsIgnoreCase(modelFileName))
            {
                return true;
            }
        }

        return false;
    }

    private Pair<String, String> getModelNamespaceUriPrefix(M2Model model)
    {
        ParameterCheck.mandatory("model", model);

        List<M2Namespace> namespaces = model.getNamespaces();
        if (namespaces.isEmpty())
        {
            throw new CustomModelException.InvalidNamespaceException(MSG_NAMESPACE_NOT_EXISTS, new Object[] { model.getName() });
        }
        if (namespaces.size() > 1)
        {
            throw new CustomModelException.InvalidNamespaceException(MSG_NAMESPACE_MANY_EXIST, new Object[] { model.getName() });
        }
        M2Namespace ns = namespaces.iterator().next();

        return new Pair<String, String>(ns.getUri(), ns.getPrefix());
    }

    private void validateModelNamespaceUri(String uri)
    {
        if (isNamespaceUriExists(uri))
        {
            throw new CustomModelException.NamespaceConstraintException(MSG_NAMESPACE_URI_ALREADY_IN_USE, new Object[] { uri });
        }
    }

    private void validateModelNamespacePrefix(String prefix)
    {
        if (isNamespacePrefixExists(prefix))
        {
            throw new CustomModelException.NamespaceConstraintException(MSG_NAMESPACE_PREFIX_ALREADY_IN_USE, new Object[] { prefix });
        }
    }

    /**
     * A helper method to run a unit of work in a transaction.
     *
     * @param errMsgId message id for the new wrapper exception ({@link CustomModelException})
     *            when an exception occurs
     * @param requiresNewTx <tt>true</tt> to force a new transaction or
     *            <tt>false</tt> to partake in any existing transaction
     * @param cb The callback containing the unit of work
     * @return Returns the result of the unit of work
     */
    private <R> R doInTransaction(String errMsgId, boolean requiresNewTx, RetryingTransactionCallback<R> cb)
    {
        try
        {
            return retryingTransactionHelper.doInTransaction(cb, false, requiresNewTx);
        }
        catch (Exception ex)
        {
            AlfrescoRuntimeException alf = null;
            if (ex instanceof AlfrescoRuntimeException)
            {
                alf = (AlfrescoRuntimeException) ex;
            }
            else
            {
                alf = AlfrescoRuntimeException.create(ex, ex.getMessage());
            }

            Throwable cause = alf.getRootCause();
            String message = getRootCauseMsg(cause, true, null);

            throw new CustomModelException(errMsgId, new Object[] { message }, ex);
        }
    }

    private static String getRootCauseMsg(Throwable cause, boolean withAlfLogNum, String defaultMsg)
    {
        if (defaultMsg == null)
        {
            defaultMsg = "";
        }

        String message = cause.getMessage();
        if(message == null)
        {
            return defaultMsg;
        }
        else
        {
            return ((withAlfLogNum) ? message : message.replaceFirst("\\d+", "").trim());
        }
    }

    @Override
    public NodeRef createDownloadNode(final String modelFileName, boolean withAssociatedForm)
    {
        List<NodeRef> nodesToBeDownloaded = new ArrayList<>(2);

        NodeRef customModelNodeRef = getModelNodeRef(modelFileName);
        if(customModelNodeRef == null)
        {
            throw new CustomModelException.ModelDoesNotExistException(MSG_MODEL_NOT_EXISTS, new Object[] { modelFileName });
        }
        // We create a copy of the model, so we can rename it, change its
        // content type and move it to the download container in order to be
        // cleaned by the download service cleanup job.
        customModelNodeRef = createCustomModelCopy(modelFileName + ".xml", customModelNodeRef);
        nodesToBeDownloaded.add(customModelNodeRef);

        if (withAssociatedForm)
        {
            NodeRef shareExtModuleNodeRef = null;
            try
            {
                shareExtModuleNodeRef = createCustomModelShareExtModuleRef(modelFileName);
                nodesToBeDownloaded.add(shareExtModuleNodeRef);

                if (logger.isDebugEnabled())
                {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Temp nodes created for download: Custom model nodeRef [")
                                .append(customModelNodeRef)
                                .append("] and its associated Share form nodeRef [")
                                .append(shareExtModuleNodeRef).append(']');
                    logger.debug(msg.toString());
                }
            }
            catch (CustomModelException ex)
            {
                // We don't throw the exception as the Model might be a
                // draft model and might have never been activated or never had any forms created for it.
                // So in this case we just construct the zip containing only the model.
                StringBuilder msg = new StringBuilder();
                msg.append("Constructing CMM zip file containing only the model [")
                            .append(modelFileName)
                            .append(".xml] without its associated share extension module, because: ")
                            .append(ex.getMessage());

                logger.warn(msg.toString());
            }

        }
        else
        {
            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Temp node created for download: Custom model nodeRef [")
                            .append(customModelNodeRef).append(']');
                logger.debug(msg.toString());
            }
        }

        try
        {
            NodeRef archiveNodeRef = downloadService.createDownload(nodesToBeDownloaded.toArray(new NodeRef[nodesToBeDownloaded.size()]), false);

            if (logger.isDebugEnabled())
            {
                StringBuilder msg = new StringBuilder();
                msg.append("Created download nodeRef [").append(archiveNodeRef).append(']');
                logger.debug(msg.toString());
            }

            return archiveNodeRef;
        }
        catch (Exception ex)
        {
            throw new CustomModelException("cmm.service.download.create_err", ex);
        }
    }

    /**
     * Finds the {@code module} element within the Share persisted-extension
     * XML file and then writes the XML fragment as the content of a newly created node.
     *
     * @param modelName the model name
     * @return the created nodeRef
     */
    protected NodeRef createCustomModelShareExtModuleRef(final String modelName)
    {
        final String moduleId = "CMM_" + modelName;

        final NodeRef formNodeRef = getShareExtModule();
        ContentReader reader = contentService.getReader(formNodeRef, ContentModel.PROP_CONTENT);

        if (reader == null)
        {
            throw new CustomModelException("cmm.service.download.share_ext_node_read_err");
        }

        InputStream in = reader.getContentInputStream();
        Node moduleIdXmlNode = null;
        try
        {
            Document document = XMLUtil.parse(in); // the stream will be closed

            final String xpathQuery = "/extension//modules//module//id[.= '" + moduleId + "']";

            XPath xPath = XPathFactory.newInstance().newXPath();
            XPathExpression expression = xPath.compile(xpathQuery);

            moduleIdXmlNode = (Node) expression.evaluate(document, XPathConstants.NODE);
        }
        catch (Exception ex)
        {
            throw new CustomModelException("cmm.service.download.share_ext_file_parse_err", ex);
        }

        if (moduleIdXmlNode == null)
        {
            throw new CustomModelException("cmm.service.download.share_ext_module_not_found", new Object[] { moduleId });
        }

        final File moduleFile = TempFileProvider.createTempFile(moduleId, ".xml");
        try
        {
            XMLUtil.print(moduleIdXmlNode.getParentNode(), moduleFile);
        }
        catch (IOException error)
        {
            throw new CustomModelException("cmm.service.download.share_ext_write_err", new Object[] { moduleId }, error);
        }

        return doInTransaction(MSG_DOWNLOAD_CREATE_SHARE_EXT_ERR, true, new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Exception
            {
                final NodeRef nodeRef = createDownloadTypeNode(moduleId + SHARE_EXT_MODULE_SUFFIX);
                ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
                writer.setMimetype(MimetypeMap.MIMETYPE_XML);
                writer.setEncoding("UTF-8");
                writer.putContent(moduleFile);

                return nodeRef;
            }
        });
    }

    /**
     * Gets Share persisted-extension nodeRef
     */
    protected NodeRef getShareExtModule()
    {
        List<NodeRef> results = searchService.selectNodes(getRootNode(), this.shareExtModulePath, null, this.namespaceDAO, false,
                    SearchService.LANGUAGE_XPATH);

        if (results.isEmpty())
        {
            throw new CustomModelException("cmm.service.download.share_ext_file_not_found");
        }

        return results.get(0);
    }

    /**
     * Creates a copy of the custom model where the created node will be a child
     * of download container.
     *
     * @param newName the model new name
     * @param modelNodeRef existing model nodeRef
     * @return the created nodeRef
     */
    protected NodeRef createCustomModelCopy(final String newName, final NodeRef modelNodeRef)
    {
        return doInTransaction(MSG_DOWNLOAD_COPY_MODEL_ERR, true, new RetryingTransactionCallback<NodeRef>()
        {
            @Override
            public NodeRef execute() throws Exception
            {
                final NodeRef newNodeRef = createDownloadTypeNode(newName);
                Serializable content = nodeService.getProperty(modelNodeRef, ContentModel.PROP_CONTENT);
                nodeService.setProperty(newNodeRef, ContentModel.PROP_CONTENT, content);

               return newNodeRef;
            }
        });
    }

    /**
     * Creates node with a type {@link DownloadModel#TYPE_DOWNLOAD} within the
     * download container (see
     * {@link DownloadStorage#getOrCreateDowloadContainer()} )
     * <p>
     * Also, the {@code IndexControlAspect} is applied to the created node.
     *
     * @param name the node name
     * @return the created nodeRef
     */
    private NodeRef createDownloadTypeNode(final String name)
    {
        final NodeRef newNodeRef = nodeService.createNode(
                    downloadStorage.getOrCreateDowloadContainer(),
                    ContentModel.ASSOC_CHILDREN,
                    ContentModel.ASSOC_CHILDREN,
                    DownloadModel.TYPE_DOWNLOAD,
                    Collections.<QName, Serializable> singletonMap(ContentModel.PROP_NAME, name)).getChildRef();

        Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>(2);
        aspectProperties.put(ContentModel.PROP_IS_INDEXED, Boolean.FALSE);
        aspectProperties.put(ContentModel.PROP_IS_CONTENT_INDEXED, Boolean.FALSE);
        nodeService.addAspect(newNodeRef, ContentModel.ASPECT_INDEX_CONTROL, aspectProperties);

        return newNodeRef;
    }

    @Override
    public CustomModelsInfo getCustomModelsInfo()
    {
        List<CustomModelDefinition> page = getCustomModels(new PagingRequest(0, Integer.MAX_VALUE)).getPage();

        int activeModels = 0;
        int activeTypes = 0;
        int activeAspects = 0;
        for (CustomModelDefinition cm : page)
        {
            if (cm.isActive())
            {
                activeModels++;
                activeTypes += cm.getTypeDefinitions().size();
                activeAspects += cm.getAspectDefinitions().size();
            }
        }

        CustomModelsInfo info = new CustomModelsInfo();
        info.setNumberOfActiveModels(activeModels);
        info.setNumberOfActiveTypes(activeTypes);
        info.setNumberOfActiveAspects(activeAspects);
        return info;
    }
}
