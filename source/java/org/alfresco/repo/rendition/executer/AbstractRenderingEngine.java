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

import static org.alfresco.service.cmr.rendition.RenditionService.PARAM_DESTINATION_PATH_TEMPLATE;
import static org.alfresco.service.cmr.rendition.RenditionService.PARAM_IS_COMPONENT_RENDITION;
import static org.alfresco.service.cmr.rendition.RenditionService.PARAM_ORPHAN_EXISTING_RENDITION;
import static org.alfresco.service.cmr.rendition.RenditionService.PARAM_RENDITION_NODETYPE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.rendition.RenderingEngineDefinitionImpl;
import org.alfresco.repo.rendition.RenditionDefinitionImpl;
import org.alfresco.repo.rendition.RenditionLocation;
import org.alfresco.repo.rendition.RenditionLocationResolver;
import org.alfresco.repo.rendition.RenditionNodeManager;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.rendition.NodeLocator;
import org.alfresco.service.cmr.rendition.RenderCallback;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
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
import org.springframework.extensions.surf.util.I18NUtil;

import com.sun.star.lang.NullPointerException;

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
    private static final String DEFAULT_RUN_AS_NAME = AuthenticationUtil.getSystemUserName();

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

    /* Parameter names common to all Rendering Actions */
    /**
     * This optional {@link String} parameter specifies the location of a
     * classpath resource which can be used as a placeholder while a rendition
     * is being generated. For example, this might be a simple icon to indicate
     * a rendition is not yet available. This is intended to be used in
     * conjunction with asynchronous generation of renditions.
     */
    public static final String PARAM_PLACEHOLDER_RESOURCE_PATH = "placeHolderResourcePath";

    /**
     * This optional {@link QName} parameter specifies which property the
     * Rendering Engine uses to read content from the source node in order to
     * create a rendition. By default this property will be cm:content.
     */
    public static final String PARAM_SOURCE_CONTENT_PROPERTY = "sourceContentProperty";

    /**
     * This optional {@link QName} parameter specifies which property the
     * Rendering Engine uses to write content to the rendition node. By default
     * the property used is cm:content.
     */
    public static final String PARAM_TARGET_CONTENT_PROPERTY = "targetContentProperty";

    /**
     * This optional {@link Boolean} flag property specifies whether a rendition
     * should be updated automatically if the source node changes. If set to
     * <code>true</code> then the rendition will be re-rendered any time any
     * property changes occur on the source node. This parameter defaults to
     * <code>false</code>.
     */
    public static final String PARAM_UPDATE_RENDITIONS_ON_ANY_PROPERTY_CHANGE = "update-renditions-on-any-property-change";

    /**
     * This optional {@link String} parameter specifies what user permissions
     * are used when creating a rendition. By default the system user is used.
     */
    public static final String PARAM_RUN_AS = "runAs";

    // mime-type is not a common parameter on all Rendering Actions, but it is
    // common to many and is used in some common handling code in this class.
    /**
     * This optional {@link String} parameter specifies the mime type of the
     * rendition content. This defaults to the mime type of the source node
     * content.
     */
    public static final String PARAM_MIME_TYPE = "mime-type";

    /**
     * This optional {@link String} paramter specifies the encoding used to
     * create the rendition content. The derfault encoding is UTF-8.
     */
    public static final String PARAM_ENCODING = "encoding";

	/**
	 * Default {@link NodeLocator} simply returns the source node.
	 */
	private final static NodeLocator defaultNodeLocator = new NodeLocator()
	{
	    public NodeRef getNode(NodeRef sourceNode, Map<String, Serializable> params)
	    {
	        return sourceNode;
	    }
	};
	
	/*
	 * Injected beans
	 */
	private RenditionLocationResolver renditionLocationResolver;
	protected NodeService nodeService;
	private RenditionService renditionService;
    private BehaviourFilter behaviourFilter;
	
	private final NodeLocator temporaryParentNodeLocator;
	private final QName temporaryRenditionLinkType;

    /**
     * Injects the nodeService bean.
     * 
     * @param nodeService
     *            the nodeService.
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Injects the renditionService bean.
     * 
     * @param renditionService
     */
    public void setRenditionService(RenditionService renditionService)
    {
        this.renditionService = renditionService;
    }

    /**
     * @param behaviourFilter  policy behaviour filter 
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setRenditionLocationResolver(RenditionLocationResolver renditionLocationResolver)
    {
        this.renditionLocationResolver = renditionLocationResolver;
    }
    
    public AbstractRenderingEngine(NodeLocator temporaryParentNodeLocator, QName temporaryRenditionLinkType)
    {
        this.temporaryParentNodeLocator = temporaryParentNodeLocator != null ? temporaryParentNodeLocator
                    : defaultNodeLocator;
        this.temporaryRenditionLinkType = temporaryRenditionLinkType != null ? temporaryRenditionLinkType
                    : RenditionModel.ASSOC_RENDITION;
    }

    public AbstractRenderingEngine()
    {
        this(null, null);
    }


    
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
    protected void executeImpl(final Action action, final NodeRef sourceNode)
    {
    	executeImpl( (RenditionDefinition)action, sourceNode );
    }
    	
    protected void executeImpl(final RenditionDefinition renditionDef, final NodeRef sourceNode)
    {
        if (logger.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Rendering node ").append(sourceNode).append(" with rendition definition ").append(
            		renditionDef.getRenditionName());
            msg.append("\n").append("  parameters:").append("\n");
            if (renditionDef.getParameterValues().isEmpty() == false)
            {
            	for (String paramKey : renditionDef.getParameterValues().keySet())
            	{
            		msg.append("    ").append(paramKey).append("=").append(renditionDef.getParameterValue(paramKey)).append("\n");
            	}
            }
            else
            {
            	msg.append("    [None]");
            }
            logger.debug(msg.toString());
        }

        Serializable runAsParam = renditionDef.getParameterValue(AbstractRenderingEngine.PARAM_RUN_AS);
        String runAsName = runAsParam == null ? DEFAULT_RUN_AS_NAME : (String) runAsParam;

        // Renditions should all be created by system by default.
        // When renditions are created by a user and are to be created under a
        // node
        // other than the source node, it is possible that the user will not
        // have
        // permissions to create content under that node.
        // For that reason, we execute all rendition actions as system
        // by default.
        AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
            	ChildAssociationRef result = null;
            	try
            	{
            	    // Check whether this rendition is a component of a larger CompositeRendition
					boolean isComponentRendition = isComponentRendition(renditionDef);
					if (isComponentRendition == false)
					{
					    // Request that the rendition is initially created
					    //  as a child of the source node
				        setTemporaryRenditionProperties(sourceNode, renditionDef);					    
					    
					    // Add renditioned aspect to the source node
				        tagSourceNodeAsRenditioned(renditionDef, sourceNode);
					}

				    // Have the concrete implementation do the actual rendition
					executeRenditionImpl(renditionDef, sourceNode);

					// 
                    if (isComponentRendition == false)
                    {
                        // Currently the rendition is on a temporary node, which may
                        //  have the wrong name on it, and for path based renditions is
                        //  in the wrong place
                        // So, have the correct node created, and switch everything to use it
                        switchToFinalRenditionNode(renditionDef, sourceNode);
                    }
					
					// Grab a link to the rendition node - it's been saved as a parameter for us
                    // (Wait until now to fetch in case it was moved)
					result = (ChildAssociationRef)renditionDef.getParameterValue(PARAM_RESULT);
				} catch (Throwable t)
	            {
	                notifyCallbackOfException(renditionDef, t);
	                throwWrappedException(t);
	            }
	            if (result != null)
	            {
	                notifyCallbackOfResult(renditionDef, result);
	            }
	            	return null;
	            }
        }, runAsName);
    }

    /**
     * Is this a standalone rendition, or is it a sub-component of
     *  a composite rendition?
     * This is false for standalone renditions, AND ALSO false for
     *  the main part of a composite rendition.
     * This only returns true if we're currently processing a
     *  component of a composite rendition.
     * @param action
     * @return
     */
	private boolean isComponentRendition(Action action) {
		Serializable s = action.getParameterValue(PARAM_IS_COMPONENT_RENDITION);
		boolean result = s == null ? false : (Boolean)s;
		return result;
	}
    
    protected void executeRenditionImpl(Action action, NodeRef sourceNode)
    {
    	if (logger.isDebugEnabled())
    	{
    		StringBuilder msg = new StringBuilder();
    		msg.append("Executing rendering engine; name:")
    		   .append(this.name).append(", class:")
    		   .append(this.getClass().getName());
    		logger.debug(msg.toString());
    	}
    	
        checkParameterValues(action);
        RenditionDefinition renditionDefinition = checkActionIsRenditionDefinition(action);
        checkSourceNodeExists(sourceNode);

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
    protected RenditionDefinition checkActionIsRenditionDefinition(Action action)
    {
        if (action instanceof RenditionDefinition)
        {
            return (RenditionDefinition)action;
        }
        else
        {
            return new RenditionDefinitionImpl(action);
        }
    }

    /**
     * If no rendition node type is specified, then the default is used
     * 
     * @param renditionDefinition
     * @return
     */
    private QName getRenditionNodeType(RenditionDefinition renditionDefinition)
    {
        return getParamWithDefault(PARAM_RENDITION_NODETYPE, defaultRenditionNodeType, renditionDefinition);
    }

    @Override
    final protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.addAll(getParameterDefinitions());
    }

    /**
     * This method gets the parameter definition display label from the properties file.
     * It looks first for a property whose key has a fixed rendition service-specific
     * prefix and if that gets null, it then delegates to the standard bean name-based
     * approach.
     * 
     * @param paramName  the name of the parameter
     * @return           the display label of the parameter
     */
    @Override
    protected String getParamDisplayLabel(String paramName)
    {
        // First we try to get the message using a common prefix for all rendering engines.
        final String commonPropertiesPrefix = "baseRenderingAction";
        String message = I18NUtil.getMessage(commonPropertiesPrefix + "." + paramName + "." + DISPLAY_LABEL);
        
        // And if that doesn't work we delegate to the standard bean name-based approach.
        if (message == null)
        {
            message = super.getParamDisplayLabel(paramName);
        }
        return message;
    }

    /**
     * Supplies the list of parameters required by this rendering engine.
     * 
     * @return
     */
    protected Collection<ParameterDefinition> getParameterDefinitions()
    {
        List<ParameterDefinition> paramList = new ArrayList<ParameterDefinition>();
        
        paramList.add(new ParameterDefinitionImpl(RenditionDefinitionImpl.RENDITION_DEFINITION_NAME, DataTypeDefinition.QNAME, true,
                getParamDisplayLabel(RenditionDefinitionImpl.RENDITION_DEFINITION_NAME)));

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

        paramList.add(new ParameterDefinitionImpl(PARAM_RESULT, DataTypeDefinition.CHILD_ASSOC_REF, false,
                getParamDisplayLabel(PARAM_RESULT)));

        paramList.add(new ParameterDefinitionImpl(PARAM_IS_COMPONENT_RENDITION, DataTypeDefinition.BOOLEAN, false,
        		getParamDisplayLabel(PARAM_IS_COMPONENT_RENDITION)));
        return paramList;
    }

    private ChildAssociationRef createRenditionNodeAssoc(NodeRef sourceNode, RenditionDefinition renditionDefinition)
    {
        QName renditionName = renditionDefinition.getRenditionName();

        // The ThumbnailService puts a cm:name property on its thumbnail nodes.
        Map<QName, Serializable> nodeProps = new HashMap<QName, Serializable>();
        nodeProps.put(ContentModel.PROP_NAME, renditionName.getLocalName());
        nodeProps.put(ContentModel.PROP_CONTENT_PROPERTY_NAME, getRenditionContentProp(renditionDefinition));
        QName assocName = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, GUID.generate());
        NodeRef parentNode = renditionDefinition.getRenditionParent();
        QName assocType = renditionDefinition.getRenditionAssociationType();
        QName nodeType = getRenditionNodeType(renditionDefinition);
        
        // Ensure that the creation of rendition children does not cause updates
        // to the modified, modifier properties on the source node
        behaviourFilter.disableBehaviour(parentNode, ContentModel.ASPECT_AUDITABLE);
        ChildAssociationRef childAssoc = null;
        try
        {
            childAssoc = nodeService.createNode(parentNode, assocType, assocName, nodeType, nodeProps);
            if (logger.isDebugEnabled())
            {
                logger.debug("Created node " + childAssoc);
            }
        }
        finally
        {
            behaviourFilter.enableBehaviour(parentNode, ContentModel.ASPECT_AUDITABLE);
        }
        return childAssoc;
    }

    private Serializable getRenditionContentProp(RenditionDefinition renditionDefinition)
    {
        return getParamWithDefault(PARAM_TARGET_CONTENT_PROPERTY, getDefaultRenditionContentProp(), renditionDefinition);
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
            if(clazz == null)
                throw new RenditionServiceException("The class must not be null!", new NullPointerException());
            Class<? extends Serializable> valueClass = value.getClass();
            if ( !valueClass.isAssignableFrom(clazz))
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
        if(defaultValue == null)
            throw new RenditionServiceException("The defaultValue cannot be null!", new NullPointerException());
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
    
    
    protected void tagSourceNodeAsRenditioned(final RenditionDefinition renditionDef, final NodeRef actionedUponNodeRef)
    {
        // Adds the 'Renditioned' aspect to the source node if it
        // doesn't already have it.
        if (!nodeService.hasAspect(actionedUponNodeRef, RenditionModel.ASPECT_RENDITIONED))
        {
            // Ensure we do not update the 'modifier' due to rendition addition
            behaviourFilter.disableBehaviour(actionedUponNodeRef, ContentModel.ASPECT_AUDITABLE);
            try
            {
                nodeService.addAspect(actionedUponNodeRef, RenditionModel.ASPECT_RENDITIONED, null);
            }
            finally
            {
                behaviourFilter.enableBehaviour(actionedUponNodeRef, ContentModel.ASPECT_AUDITABLE);
            }
        }
    }

    protected void switchToFinalRenditionNode(final RenditionDefinition renditionDef, final NodeRef actionedUponNodeRef)
    {
        ChildAssociationRef tempRendAssoc = (ChildAssociationRef)renditionDef.getParameterValue(PARAM_RESULT);
        ChildAssociationRef result = createOrUpdateRendition(actionedUponNodeRef, tempRendAssoc, renditionDef);
        renditionDef.setParameterValue(PARAM_RESULT, result);
    }

    protected void notifyCallbackOfException(RenditionDefinition renditionDefinition, Throwable t)
    {
    	// Rendition has failed. If there is a callback, it needs to be notified
        if (renditionDefinition != null)
        {
            RenderCallback callback = renditionDefinition.getCallback();
            if (callback != null)
            {
                callback.handleFailedRendition(t);
            }
        }
    }

    protected void throwWrappedException(Throwable t)
    {
    	// and rethrow Exception
        if (t instanceof AlfrescoRuntimeException)
        {
            throw (AlfrescoRuntimeException) t;
        } else
        {
            throw new RenditionServiceException(t.getMessage(), t);
        }
    }

    protected void notifyCallbackOfResult(RenditionDefinition renditionDefinition, ChildAssociationRef result)
    {
        // Rendition was successful. Notify the callback object.
        if (renditionDefinition != null)
        {
            RenderCallback callback = renditionDefinition.getCallback();
            if (callback != null)
            {
                callback.handleSuccessfulRendition(result);
            }
        }
    }

    /**
     * This method sets the temporary rendition parent node and the rendition assocType on the
     * rendition definition.
     * 
     * @param sourceNode
     * @param definition the rendition definition.
     */
    private void setTemporaryRenditionProperties(NodeRef sourceNode, RenditionDefinition definition)
    {
        // Set the parent and assoc type for the temporary rendition to be
        // created.
        NodeRef parent = temporaryParentNodeLocator.getNode(sourceNode, definition.getParameterValues());
        definition.setRenditionParent(parent);
        definition.setRenditionAssociationType(temporaryRenditionLinkType);
    }

    /**
     * 
     * @param sourceNode The node that has been rendered
     * @param tempRendition The relationship between the node and its rendition
     * @param renditionDefinition The definition of the rendition that has just been performed.
     *                            In the case of a composite rendition, this parameter refers
     *                            to that CompositeRendition and not to any of its component renditions.
     * @return
     */
    private ChildAssociationRef createOrUpdateRendition(NodeRef sourceNode, ChildAssociationRef tempRendition,
                RenditionDefinition renditionDefinition)
    {
        NodeRef tempRenditionNode = tempRendition.getChildRef();
        RenditionLocation renditionLocation = resolveRenditionLocation(sourceNode, renditionDefinition, tempRenditionNode);
        QName renditionQName = renditionDefinition.getRenditionName();

        RenditionNodeManager renditionNodeManager = new RenditionNodeManager(sourceNode, tempRenditionNode,
                renditionLocation, renditionDefinition, nodeService, renditionService, behaviourFilter);
        ChildAssociationRef renditionNode = renditionNodeManager.findOrCreateRenditionNode();

        // Copy relevant properties from the temporary node to the new rendition
        // node.
        renditionNodeManager.transferNodeProperties();

        // Set the name property on the rendition if it has not already been
        // set.
        String renditionName = getRenditionName(tempRenditionNode, renditionLocation, renditionDefinition);
        nodeService.setProperty(renditionNode.getChildRef(), ContentModel.PROP_NAME, renditionName); // to manager

        // Delete the temporary rendition.
        nodeService.removeChildAssociation(tempRendition);

        // Handle the rendition aspects
        manageRenditionAspects(sourceNode, renditionNode);
        
        // Verify that everything has gone to plan, and nothing got lost on the way!
        ChildAssociationRef renditionAssoc = renditionService.getRenditionByName(sourceNode, renditionQName);
        if (renditionAssoc == null)
        {
            String msg = "A rendition of type: " + renditionQName + " should have been created for source node: "
                        + sourceNode;
            throw new RenditionServiceException(msg);
        }
        // Return the link between the source and the new, final rendition
        return renditionAssoc;
    }

    /**
     * This method manages the <code>rn:rendition</code> aspects on the rendition node. It applies the
     * correct rendition aspect based on the rendition node's location and removes any out-of-date rendition
     * aspect.
     */
    private void manageRenditionAspects(NodeRef sourceNode, ChildAssociationRef renditionParentAssoc)
    {
        NodeRef renditionNode = renditionParentAssoc.getChildRef();
        NodeRef primaryParent = renditionParentAssoc.getParentRef();

        // If the rendition is located directly underneath its own source node
        if (primaryParent.equals(sourceNode))
        {
            // It should be a 'hidden' rendition.
            // Ensure we do not update the 'modifier' due to rendition addition
            behaviourFilter.disableBehaviour(renditionNode, ContentModel.ASPECT_AUDITABLE);
            try
            {
                nodeService.addAspect(renditionNode, RenditionModel.ASPECT_HIDDEN_RENDITION, null);
                nodeService.removeAspect(renditionNode, RenditionModel.ASPECT_VISIBLE_RENDITION);
            }
            finally
            {
                behaviourFilter.enableBehaviour(renditionNode, ContentModel.ASPECT_AUDITABLE);
            }
            // We remove the other aspect to cover the potential case where a
            // rendition
            // has been updated in a different location.
        } else
        {
            // Renditions stored underneath any node other than their source are
            // 'visible'.
            behaviourFilter.disableBehaviour(renditionNode, ContentModel.ASPECT_AUDITABLE);
            try
            {
                nodeService.addAspect(renditionNode, RenditionModel.ASPECT_VISIBLE_RENDITION, null);
                nodeService.removeAspect(renditionNode, RenditionModel.ASPECT_HIDDEN_RENDITION);
            }
            finally
            {
                behaviourFilter.enableBehaviour(renditionNode, ContentModel.ASPECT_AUDITABLE);
            }
        }
    }

    /**
     * This method calculates the name for a rendition node. The following approaches are attempted in
     * the order given below.
     * <ol>
     *    <li>If a name is defined in the {@link RenditionLocation} then that is used.</li>
     *    <li>If the temporary rendition has a <code>cm:name</code> value, then that is used.</li>
     *    <li>Otherwise use the rendition definition's rendition name.</li>
     * </ol>
     * 
     * @param tempRenditionNode the temporary rendition node.
     * @param location a RenditionLocation struct.
     * @param renditionDefinition the rendition definition.
     * @return the name for the rendition.
     */
    private String getRenditionName(NodeRef tempRenditionNode, RenditionLocation location,
                RenditionDefinition renditionDefinition)
    {
        // If a location name is set then use it.
        String locName = location.getChildName();
        if (locName != null && locName.length() > 0)
        {
            return locName;
        }
        // Else if the temporary rendition specifies a name property use that.
        Serializable tempName = nodeService.getProperty(tempRenditionNode, ContentModel.PROP_NAME);
        if (tempName != null)
        {
            return (String) tempName;
        }
        // Otherwise use the rendition definition local name.
        return renditionDefinition.getRenditionName().getLocalName();
    }

    /**
     * Given a rendition definition, a source node and a temporary rendition node, this method uses a
     * {@link RenditionLocationResolver} to calculate the {@link RenditionLocation} of the rendition.
     */
    protected RenditionLocation resolveRenditionLocation(NodeRef sourceNode, RenditionDefinition definition,
                NodeRef tempRendition)
    {
        return renditionLocationResolver.getRenditionLocation(sourceNode, definition, tempRendition);
    }
}
