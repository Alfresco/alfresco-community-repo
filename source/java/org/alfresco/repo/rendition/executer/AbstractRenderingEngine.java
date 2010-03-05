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

package org.alfresco.repo.rendition.executer;

import static org.alfresco.service.cmr.rendition.RenditionService.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.rendition.RenderingEngineDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class adds some new behaviour to the standard ActionExecuterAbstractBase
 * in order to support the RenditionService.
 * 
 * @author Neil McErlean
 * @author Nick Smith
 * @since 3.3
 */
public abstract class AbstractRenderingEngine extends ActionExecuterAbstractBase
{

    /** Logger */
    private static Log logger = LogFactory.getLog(AbstractRenderingEngine.class);

    protected static final String CONTENT_READER_NOT_FOUND_MESSAGE = "Cannot find Content Reader for document. Operation can't be performed";

    // A word on the default* fields below:
    //
    // RenditionExecuters can be executed with or without two optional
    // parameters: "rendition node type"
    // and a "rendition content property" parameter.
    // These parameters can be specified on a per-action basis.
    // If no value is specified, then the default is used.
    // That default can be injected via Spring.
    // If no default is injected via spring, then there is a "default default"
    // for the two params

    /**
     * This is the default default node type for renditions - used if no value
     * is injected from spring.
     */
    private static final QName DEFAULT_DEFAULT_RENDITION_NODE_TYPE = ContentModel.TYPE_CONTENT;

    /**
     * This is the default default property used to specify where rendition
     * content is stored - used if no value is injected from spring.
     */
    private static final QName DEFAULT_DEFAULT_RENDITION_CONTENT_PROP = ContentModel.PROP_CONTENT;

    private static final String DEFAULT_MIMETYPE = MimetypeMap.MIMETYPE_TEXT_PLAIN;
    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * This is the default node type that is used when creating rendition
     * objects.
     */
    private QName defaultRenditionNodeType = DEFAULT_DEFAULT_RENDITION_NODE_TYPE;

    /**
     * This is the default property that is used to store rendition objects'
     * content.
     */
    private QName defaultRenditionContentProp = DEFAULT_DEFAULT_RENDITION_CONTENT_PROP;

    /**
     * This is the default content property.
     */
    private static final QName DEFAULT_CONTENT_PROPERTY = ContentModel.TYPE_CONTENT;

    /* Injected Services */
    protected ContentService contentService;
    protected MimetypeMap mimetypeMap;
    protected NodeService nodeService;

    /* Parameter names common to all Rendering Actions */
    //TODO javadoc these
    public static final String PARAM_PLACEHOLDER_RESOURCE_PATH = "placeHolderResourcePath";
    public static final String PARAM_SOURCE_CONTENT_PROPERTY = "sourceContentProperty";
    public static final String PARAM_TARGET_CONTENT_PROPERTY = "targetContentProperty";
    public static final String PARAM_UPDATE_RENDITIONS_ON_ANY_PROPERTY_CHANGE = "update-renditions-on-any-property-change";
    public static final String PARAM_RUN_AS = "runAs";

    /*
     * mime-type is not a common parameter on all Rendering Actions, but it is
     * common to many and is used in some common handling code in this class.
     */
    public static final String PARAM_MIME_TYPE = "mime-type";
    public static final String PARAM_ENCODING = "encoding";

    /**
     * Sets the default rendition-node type.
     * 
     * @param type
     */
    public void setDefaultRenditionNodeType(String type)
    {
        QName qname;
        try
        {
            qname = QName.createQName(type);
        }
        catch (NamespaceException nx)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("Error when setting default rendition node type: ", nx);
            }
            throw nx;
        }

        if (logger.isInfoEnabled())
        {
            logger.info("Using default rendition node type: " + qname);
        }
        this.defaultRenditionNodeType = qname;
    }

    /**
     * This method returns the type of the default rendition node type.
     * 
     * @return the QName representing the type of the default rendition node
     *         type.
     */
    protected QName getDefaultRenditionNodeType()
    {
        return defaultRenditionNodeType;
    }

    protected String getTargetMimeType(RenderingContext context)
    {
        return context.getParamWithDefault(PARAM_MIME_TYPE, DEFAULT_MIMETYPE);
    }

    protected String getTargetEncoding(RenderingContext context)
    {
        return context.getParamWithDefault(PARAM_ENCODING, DEFAULT_ENCODING);
    }

    /**
     * Sets the default rendition content property.
     * 
     * @param prop
     */
    public void setDefaultRenditionContentProp(String prop)
    {
        QName qname;
        try
        {
            qname = QName.createQName(prop);
        }
        catch (NamespaceException nx)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("Error when setting default rendition content property: ", nx);
            }
            throw nx;
        }

        if (logger.isInfoEnabled())
        {
            logger.info("Using default rendition content property: " + qname);
        }
        this.defaultRenditionContentProp = qname;
    }

    /**
     * This method returns the QName of the property that defines the location
     * of the rendition content. An example would be cm:content.
     * 
     * @return the QName the property defining the location of the rendition
     *         content.
     */
    protected QName getDefaultRenditionContentProp()
    {
        return defaultRenditionContentProp;
    }

    /**
     * Set the content service
     * 
     * @param contentService the content service
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * This method sets the nodeService.
     * 
     * @param nodeService the namespaceService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setMimetypeMap(MimetypeMap mimetypeMap)
    {
        this.mimetypeMap = mimetypeMap;
    }

    @Override
    protected ActionDefinition createActionDefinition(String definitionName)
    {
        return new RenderingEngineDefinitionImpl(definitionName);
    }

    @Override
    protected void executeImpl(Action action, NodeRef sourceNode)
    {
        checkParameterValues(action);
        checkActionIsRenditionDefinition(action);
        checkSourceNodeExists(sourceNode);
        RenditionDefinition renditionDefinition = (RenditionDefinition) action;

        ChildAssociationRef renditionAssoc = createRenditionNodeAssoc(sourceNode, renditionDefinition);
        QName targetContentProp = getRenditionContentProperty(renditionDefinition);
        NodeRef destinationNode = renditionAssoc.getChildRef();
        RenderingContext context = new RenderingContext(sourceNode,//
                    destinationNode,//
                    renditionDefinition,//
                    targetContentProp);
        render(context);
        // This is a workaround for the fact that actions don't have return
        // values.
        action.getParameterValues().put(PARAM_RESULT, renditionAssoc);
    }

    /**
     * This method can be overridden by subclasses to provide checking of parameter
     * values.
     * If a parameter value is illegal or inappropriate, an exception
     * should be thrown.
     */
    protected void checkParameterValues(Action action)
    {
        // Intentionally empty
    }

    /**
     * @param renditionDefinition
     * @return
     */
    protected QName getRenditionContentProperty(RenditionDefinition renditionDefinition)
    {
        return getParamWithDefault(PARAM_TARGET_CONTENT_PROPERTY, defaultRenditionContentProp, renditionDefinition);
    }

    protected abstract void render(RenderingContext context);

    /**
     * @param actionedUponNodeRef
     */
    protected void checkSourceNodeExists(NodeRef actionedUponNodeRef)
    {
        if (nodeService.exists(actionedUponNodeRef) == false)
        {
            String msg = "Cannot execute action as node does not exist: " + actionedUponNodeRef;
            logger.warn(msg);
            throw new RenditionServiceException(msg);
        }
    }

    /**
     * @param action
     */
    protected void checkActionIsRenditionDefinition(Action action)
    {
        if (action instanceof RenditionDefinition == false)
        {
            String msg = "Cannot execute action as it is not a RenditionDefinition: " + action;
            logger.warn(msg);
            throw new RenditionServiceException(msg);
        }
    }

    /**
     * If no rendition node type is specified, then the default is used
     * 
     * @param renditionDefinition
     * @return
     */
    public QName getRenditionNodeType(RenditionDefinition renditionDefinition)
    {
        return getParamWithDefault(PARAM_RENDITION_NODETYPE, defaultRenditionNodeType, renditionDefinition);
    }

    @Override
    final protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.addAll(getParameterDefinitions());
    }

    /**
     * Supplies the list of parameters required by this rendering engine.
     * 
     * @return
     */
    protected Collection<ParameterDefinition> getParameterDefinitions()
    {
        List<ParameterDefinition> paramList = new ArrayList<ParameterDefinition>();
        paramList.add(new ParameterDefinitionImpl(PARAM_RUN_AS, DataTypeDefinition.TEXT, false,
                getParamDisplayLabel(PARAM_RUN_AS)));

        paramList.add(new ParameterDefinitionImpl(PARAM_UPDATE_RENDITIONS_ON_ANY_PROPERTY_CHANGE, DataTypeDefinition.BOOLEAN, false,
                getParamDisplayLabel(PARAM_UPDATE_RENDITIONS_ON_ANY_PROPERTY_CHANGE)));

        paramList.add(new ParameterDefinitionImpl(PARAM_RENDITION_NODETYPE, DataTypeDefinition.QNAME, false,
                    getParamDisplayLabel(PARAM_RENDITION_NODETYPE)));

        paramList.add(new ParameterDefinitionImpl(PARAM_PLACEHOLDER_RESOURCE_PATH, DataTypeDefinition.TEXT, false,
                    getParamDisplayLabel(PARAM_PLACEHOLDER_RESOURCE_PATH)));

        paramList.add(new ParameterDefinitionImpl(PARAM_SOURCE_CONTENT_PROPERTY, DataTypeDefinition.QNAME, false,
                    getParamDisplayLabel(PARAM_SOURCE_CONTENT_PROPERTY)));

        paramList.add(new ParameterDefinitionImpl(PARAM_TARGET_CONTENT_PROPERTY, DataTypeDefinition.QNAME, false,
                    getParamDisplayLabel(PARAM_TARGET_CONTENT_PROPERTY)));

        paramList.add(new ParameterDefinitionImpl(PARAM_DESTINATION_PATH_TEMPLATE, DataTypeDefinition.TEXT, false,
                    getParamDisplayLabel(PARAM_DESTINATION_PATH_TEMPLATE)));

        paramList.add(new ParameterDefinitionImpl(PARAM_ORPHAN_EXISTING_RENDITION, DataTypeDefinition.BOOLEAN, false,
        		getParamDisplayLabel(PARAM_ORPHAN_EXISTING_RENDITION)));
        return paramList;
    }

    private ChildAssociationRef createRenditionNodeAssoc(NodeRef sourceNode, RenditionDefinition renditionDefinition)
    {
        QName renditionName = renditionDefinition.getRenditionName();

        // The ThumbnailService puts a cm:name property on its thumbnail nodes.
        Map<QName, Serializable> nodeProps = new HashMap<QName, Serializable>();
        nodeProps.put(ContentModel.PROP_NAME, renditionName.getLocalName());
        nodeProps.put(ContentModel.PROP_CONTENT_PROPERTY_NAME, getDefaultRenditionContentProp());
        QName assocName = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, GUID.generate());
        NodeRef parentNode = renditionDefinition.getRenditionParent();
        QName assocType = renditionDefinition.getRenditionAssociationType();
        QName nodeType = getRenditionNodeType(renditionDefinition);
        ChildAssociationRef childAssoc = nodeService.createNode(parentNode, assocType, assocName, nodeType, nodeProps);
        return childAssoc;
    }

    /**
     * Gets the value for the named parameter. Checks the type of the parameter
     * is correct and throws a {@link RenditionServiceException} if it isn't.
     * Returns <code>null</code> if the parameter value is <code>null</code>
     * 
     * @param paramName the name of the parameter being checked.
     * @param clazz the expected {@link Class} of the parameter value.
     * @param definition the {@link RenditionDefinition} containing the
     *            parameters.
     * @return the parameter value or <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getCheckedParam(String paramName, Class<T> clazz, RenditionDefinition definition)
    {
        Serializable value = definition.getParameterValue(paramName);
        if (value == null)
            return null;
        else
        {
            Class<? extends Serializable> valueClass = value.getClass();
            if (!valueClass.isAssignableFrom(clazz))
            {
                throw new RenditionServiceException("The parameter: " + paramName + " must be of type: "
                            + clazz.getName() + "but was of type: " + valueClass.getName());
            }
            else
                return (T) value;
        }
    }

    /**
     * Gets the value for the named parameter. Checks the type of the parameter
     * is the same as the type of <code>defaultValue</code> and throws a
     * {@link RenditionServiceException} if it isn't. Returns
     * <code>defaultValue</code> if the parameter value is <code>null</code>
     * 
     * @param <T>
     * @param paramName
     * @param defaultValue
     * @param definition
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getParamWithDefault(String paramName, T defaultValue, RenditionDefinition definition)
    {
        Class<? extends T> clazz = (Class<? extends T>) defaultValue.getClass();
        T result = getCheckedParam(paramName, clazz, definition);
        if (result == null)
            result = defaultValue;
        return result;
    }

    protected class RenderingContext
    {
        private final NodeRef sourceNode;
        private final NodeRef destinationNode;
        private final RenditionDefinition definition;
        private final QName renditionContentProperty;

        /**
         * @param sourceNode
         * @param destinationNode
         * @param definition
         * @param renditionContentProperty
         */
        public RenderingContext(NodeRef sourceNode,//
                    NodeRef destinationNode,//
                    RenditionDefinition definition,//
                    QName renditionContentProperty)
        {
            this.sourceNode = sourceNode;
            this.destinationNode = destinationNode;
            this.definition = definition;
            this.renditionContentProperty = renditionContentProperty;
        }

        /**
         * @return the sourceNode
         */
        public NodeRef getSourceNode()
        {
            return this.sourceNode;
        }

        /**
         * @return the destinationNode
         */
        public NodeRef getDestinationNode()
        {
            return this.destinationNode;
        }

        /**
         * @return the definition
         */
        public RenditionDefinition getDefinition()
        {
            return this.definition;
        }

        /**
         * Gets the value for the named parameter from the . Checks the type of
         * the parameter is correct and throws and Exception if it isn't.
         * Returns <code>null</code> if the parameter value is <code>null</code>
         * 
         * @param paramName the name of the parameter being checked.
         * @param clazz the expected {@link Class} of the parameter value.
         * @return the parameter value or <code>null</code>.
         */
        public <T> T getCheckedParam(String paramName, Class<T> clazz)
        {
            return AbstractRenderingEngine.getCheckedParam(paramName, clazz, definition);
        }

        /**
         * Gets the value for the named parameter. Checks the type of the
         * parameter is the same as the type of <code>defaultValue</code> and
         * throws a {@link RenditionServiceException} if it isn't. Returns
         * <code>defaultValue</code> if the parameter value is <code>null</code>
         * 
         * @param <T>
         * @param paramName
         * @param defaultValue
         * @return
         */
        public <T> T getParamWithDefault(String paramName, T defaultValue)
        {
            return AbstractRenderingEngine.getParamWithDefault(paramName, defaultValue, definition);
        }

        public ContentReader makeContentReader()
        {
            QName srcContentProp = getParamWithDefault(PARAM_SOURCE_CONTENT_PROPERTY, DEFAULT_CONTENT_PROPERTY);
            ContentReader contentReader = contentService.getReader(sourceNode, srcContentProp);
            if (contentReader == null || !contentReader.exists())
            {
                throw new RenditionServiceException(CONTENT_READER_NOT_FOUND_MESSAGE);
            }
            return contentReader;
        }

        public ContentWriter makeContentWriter()
        {
            ContentWriter contentWriter = contentService.getWriter(destinationNode, renditionContentProperty, true);
            String mimetype = getTargetMimeType(this);
            contentWriter.setMimetype(mimetype);
            String encoding = getTargetEncoding(this);
            contentWriter.setEncoding(encoding);
            return contentWriter;
        }

        public int getIntegerParam(String key, int defaultValue)
        {
            Serializable serializable = definition.getParameterValue(key);
            if (serializable == null)
                return defaultValue;
            else
            {
                Number number = (Number) serializable;
                return number.intValue();
            }
        }
    }
}
