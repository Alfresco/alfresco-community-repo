/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.admin;

import static org.springframework.extensions.surf.util.ParameterCheck.mandatory;
import static org.springframework.extensions.surf.util.ParameterCheck.mandatoryString;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.caveat.RMListOfValuesConstraint;
import org.alfresco.module.org_alfresco_module_rm.caveat.RMListOfValuesConstraint.MatchLogic;
import org.alfresco.module.org_alfresco_module_rm.compatibility.CompatibilityModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDefinition;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipDisplayName;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.dictionary.M2Aspect;
import org.alfresco.repo.dictionary.M2Constraint;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.GUID;
import org.alfresco.util.LockCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.surf.util.URLDecoder;

/**
 * Records Management AdminService Implementation.
 *
 * @author Neil McErlean, janv
 */
@BehaviourBean
public class RecordsManagementAdminServiceImpl extends RecordsManagementAdminBase
											   implements RecordsManagementAdminService,
														  RecordsManagementCustomModel,
														  NodeServicePolicies.OnAddAspectPolicy,
														  NodeServicePolicies.OnRemoveAspectPolicy,	
														  NodeServicePolicies.OnCreateNodePolicy,
														  ApplicationListener<ContextRefreshedEvent>, 
														  Ordered
{

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordsManagementAdminServiceImpl.class);

    /** I18N messages*/
    private static final String MSG_SERVICE_NOT_INIT = "rm.admin.service-not-init";
    private static final String MSG_PROP_EXIST = "rm.admin.prop-exist";
    private static final String MSG_CUSTOM_PROP_EXIST = "rm.admin.custom-prop-exist";
    private static final String MSG_UNKNOWN_ASPECT = "rm.admin.unknown-aspect";
    private static final String MSG_CONSTRAINT_EXISTS = "rm.admin.constraint-exists";
    private static final String MSG_CANNOT_FIND_CONSTRAINT = "rm.admin.contraint-cannot-find";
    private static final String MSG_UNEXPECTED_TYPE_CONSTRAINT = "rm.admin.unexpected_type_constraint";
    private static final String MSG_ERROR_CLIENT_ID = "rm.admin.error-client-id";

    /** Constants */
    private static final String CUSTOM_CONSTRAINT_TYPE = org.alfresco.module.org_alfresco_module_rm.caveat.RMListOfValuesConstraint.class.getName();
    private static final String CAPABILITY_CUSTOM_CONSTRAINT_TYPE = org.alfresco.module.org_alfresco_module_dod5015.caveat.RMListOfValuesConstraint.class.getName();
    private static final String PARAM_ALLOWED_VALUES = "allowedValues";
    private static final String PARAM_CASE_SENSITIVE = "caseSensitive";
    private static final String PARAM_MATCH_LOGIC = "matchLogic";

    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "RecordsManagementAdminServiceImpl");
    private static final long DEFAULT_TIME = 30000L;

    /** Relationship service */
    private RelationshipService relationshipService;
    
    /** Transaction service */
    private TransactionService transactionService;

    /** Job Lock service */
    private JobLockService jobLockService;

    /** List of types that can be customisable */
    private List<QName> pendingCustomisableTypes;
    private Map<QName, QName> customisableTypes;
    
    /** indicates whether the custom map has been initialised or not */
    private boolean isCustomMapInit = false;
	
	/**
	 * @param transactionService   transaction service
	 */
	public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * @param relationshipService The relationship service instance
     */
    public void setRelationshipService(RelationshipService relationshipService)
    {
        this.relationshipService = relationshipService;
    }

    /**
     * Gets the relationship service instance
     *
     * @return The relationship service instance
     */
    protected RelationshipService getRelationshipService()
    {
        return this.relationshipService;
    }

    /**
     * @param  jobLockService The Job Lock service
     */
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }

    /**
	 * Indicate that this application content listener must be executed with the lowest 
	 * precedence. (ie last)
	 * 
	 * @see Ordered#getOrder()
	 */
    @Override
    public int getOrder()
    {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Load the custom properties map
     * 
     * @see ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event)
    {
        final LockCallback lockCallback = new LockCallback();

        // try and get the lock
        String lockToken = getLock();
        if (lockToken != null)
        {
            try
            {
                jobLockService.refreshLock(lockToken, LOCK_QNAME, DEFAULT_TIME, lockCallback);

                if (!isCustomMapInit && getDictionaryService().getAllModels().contains(RM_CUSTOM_MODEL))
                {
                    // run as System on bootstrap
                    AuthenticationUtil.runAsSystem((RunAsWork<Void>) () -> {
                        transactionService.getRetryingTransactionHelper()
                                          .doInTransaction((RetryingTransactionCallback<Void>) () -> {
                            // initialise custom properties
                            initCustomMap();
                            return null;
                        });
                        return null;
                    });
                }
            }
            finally
            {
                try
                {
                    lockCallback.setIsRunning(false);
                    jobLockService.releaseLock(lockToken, LOCK_QNAME);
                }
                catch (LockAcquisitionException e)
                {
                    LOGGER.debug("Lock release failed: {}: {}", LOCK_QNAME, lockToken, e);
                }
            }
        }
    }

    /**
     * Attempts to get the lock. If the lock couldn't be taken, then <tt>null</tt> is returned.
     *
     * @return Returns the lock token or <tt>null</tt>
     */
    private String getLock()
    {
        try
        {
            return jobLockService.getLock(LOCK_QNAME, DEFAULT_TIME);
        }
        catch (LockAcquisitionException e)
        {
            return null;
        }
    }


    /**
     * Helper method to indicate whether the custom map is initialised or not.
     * 
     * @return  boolean true if initialised, false otherwise
     */
    public boolean isCustomMapInit()
    {
        return isCustomMapInit;
    }   

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            isService = true,
            notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    public void onAddAspect(final NodeRef nodeRef, final QName aspectTypeQName)
    {
        if (isCustomMapInit)
        {
            AuthenticationUtil.runAs(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    if (getNodeService().exists(nodeRef) &&
                        getDictionaryService().getAllModels().contains(RM_CUSTOM_MODEL) &&
                        isCustomisable(aspectTypeQName))
                    {
                        QName customPropertyAspect = getCustomAspect(aspectTypeQName);
                        getNodeService().addAspect(nodeRef, customPropertyAspect, null);
                    }
    
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnRemoveAspectPolicy#onRemoveAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            isService = true,
            notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    public void onRemoveAspect(final NodeRef nodeRef, final QName aspectTypeQName)
    {
        if (isCustomMapInit)
        {
            AuthenticationUtil.runAs(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    if (getNodeService().exists(nodeRef) &&
                        isCustomisable(aspectTypeQName))
                    {
                        QName customPropertyAspect = getCustomAspect(aspectTypeQName);
                        getNodeService().removeAspect(nodeRef, customPropertyAspect);
                    }
    
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
    }

    /**
     * Make sure any custom property aspects are applied to newly created nodes.
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            isService = true,
            notificationFrequency = NotificationFrequency.FIRST_EVENT
    )
    public void onCreateNode(final ChildAssociationRef childAssocRef)
    {
        if (isCustomMapInit)
        {            
            AuthenticationUtil.runAs(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    if (getDictionaryService().getAllModels().contains(RecordsManagementCustomModel.RM_CUSTOM_MODEL))
                    {
                        NodeRef nodeRef = childAssocRef.getChildRef();
                        QName type = getNodeService().getType(nodeRef);
                        while (type != null && !ContentModel.TYPE_CMOBJECT.equals(type))
                        {
                            if (isCustomisable(type))
                            {
                                QName customPropertyAspect = getCustomAspect(type);
                                getNodeService().addAspect(nodeRef, customPropertyAspect, null);
                            }
    
                            TypeDefinition def = getDictionaryService().getType(type);
                            if (def != null)
                            {
                                type = def.getParentName();
                            }
                            else
                            {
                                type = null;
                            }
                        }
                    }
    
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
    }

    /**
     * @param customisableTypes		list of string representations of the type qnames that are customisable
     */
    public void setCustomisableTypes(List<String> customisableTypes)
    {
        mandatory("customisableTypes", customisableTypes);

        pendingCustomisableTypes = new ArrayList<>();
        for (String customisableType : customisableTypes)
        {
            pendingCustomisableTypes.add(QName.createQName(customisableType, getNamespaceService()));
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#getCustomisable()
     */
    public Set<QName> getCustomisable()
    {
        return getCustomisableMap().keySet();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#getCustomisable(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public Set<QName> getCustomisable(NodeRef nodeRef)
    {
        mandatory("nodeRef", nodeRef);

        Set<QName> result = new HashSet<>(5);

        // Check the nodes hierarchy for customisable types
        QName type = getNodeService().getType(nodeRef);
        while (type != null && !ContentModel.TYPE_CMOBJECT.equals(type))
        {
            // Add to the list if the type is customisable
            if (isCustomisable(type))
            {
                result.add(type);
            }

            // Type and get the types parent
            TypeDefinition def = getDictionaryService().getType(type);
            if (def != null)
            {
                type = def.getParentName();
            }
            else
            {
                type = null;
            }
        }

        // Get all the nodes aspects
        Set<QName> aspects = getNodeService().getAspects(nodeRef);
        for (QName aspect : aspects)
        {
            QName tempAspect = QName.createQName(aspect.toString());
            while (tempAspect != null)
            {
                // Add to the list if the aspect is customisable
                if (isCustomisable(tempAspect))
                {
                    result.add(tempAspect);
                }

                // Try and get the parent aspect
                AspectDefinition aspectDef = getDictionaryService().getAspect(tempAspect);
                if (aspectDef != null)
                {
                    tempAspect = aspectDef.getParentName();
                }
                else
                {
                    tempAspect = null;
                }
            }
        }

        return result;
    }
    
    /**
     * Initialise custom type map
     */
    private void initCustomMap()
    {
        customisableTypes = new HashMap<>(7);
        Collection<QName> aspects = getDictionaryService().getAspects(RM_CUSTOM_MODEL);
        for (QName aspect : aspects)
        {
            AspectDefinition aspectDef = getDictionaryService().getAspect(aspect);
            String name = aspectDef.getName().getLocalName();
            if (name.endsWith("Properties"))
            {
                QName type = null;
                String prefixString = aspectDef.getDescription(getDictionaryService());
                if (prefixString == null)
                {
                    // Backward compatibility from previous RM V1.0 custom models
                    if (CompatibilityModel.NAME_CUSTOM_RECORD_PROPERTIES.equals(name))
                    {
                        type = RecordsManagementModel.ASPECT_RECORD;
                    }
                    else if (CompatibilityModel.NAME_CUSTOM_RECORD_FOLDER_PROPERTIES.equals(name))
                    {
                        type = RecordsManagementModel.TYPE_RECORD_FOLDER;
                    }
                    else if (CompatibilityModel.NAME_CUSTOM_RECORD_CATEGORY_PROPERTIES.equals(name))
                    {
                        type = RecordsManagementModel.TYPE_RECORD_CATEGORY;
                    }
                    else if (CompatibilityModel.NAME_CUSTOM_RECORD_SERIES_PROPERTIES.equals(name) &&
                            // Only add the deprecated record series type as customisable if
                            // a v1.0 installation has added custom properties
                            aspectDef.getProperties().size() != 0)
                    {
                        type = CompatibilityModel.TYPE_RECORD_SERIES;
                    }
                }
                else
                {
                    type = QName.createQName(prefixString, getNamespaceService());
                }

                // Add the customisable type to the map
                if (type != null)
                {
                    customisableTypes.put(type, aspect);

                    // Remove customisable type from the pending list
                    if (pendingCustomisableTypes != null && pendingCustomisableTypes.contains(type))
                    {
                        pendingCustomisableTypes.remove(type);
                    }
                }
            }
        }

        // Deal with any pending types left over
        if (pendingCustomisableTypes != null && pendingCustomisableTypes.size() != 0)
        {
            NodeRef modelRef = getCustomModelRef(RecordsManagementModel.RM_CUSTOM_URI);
            M2Model model = readCustomContentModel(modelRef);
            try
            {
                for (QName customisableType : pendingCustomisableTypes)
                {
                    QName customAspect = getCustomAspectImpl(customisableType);

                    // Create the new aspect to hold the custom properties
                    M2Aspect aspect = model.createAspect(customAspect.toPrefixString(getNamespaceService()));
                    aspect.setDescription(customisableType.toPrefixString(getNamespaceService()));

                    // Make a record of the customisable type
                    customisableTypes.put(customisableType, customAspect);
                }
            }
            finally
            {
                writeCustomContentModel(modelRef, model);
            }
        }
        
        // indicate map is initialised
        isCustomMapInit = true;
    }

    /**
     * Gets a map containing all the customisable types
     *
     * @return  map from the customisable type to its custom aspect
     */
    private Map<QName, QName> getCustomisableMap()
    {
    	if (customisableTypes == null)
    	{
    		throw AlfrescoRuntimeException.create("Customisable map has not been initialised correctly.");
    	}
    	return customisableTypes;
    }

    /**
     * Gets the QName of the custom aspect given the customisable type QName
     *
     * @param customisableType
     * @return
     */
    private QName getCustomAspect(QName customisableType)
    {
        Map<QName, QName> map = getCustomisableMap();
        QName result = map.get(customisableType);
        if (result == null)
        {
            result = getCustomAspectImpl(customisableType);
        }
        return result;
    }

    /**
     * Builds a custom aspect QName from a customisable type/aspect QName
     *
     * @param customisableType
     * @return
     */
    private QName getCustomAspectImpl(QName customisableType)
    {
        String localName = customisableType.toPrefixString(getNamespaceService()).replace(":", "");
        localName = MessageFormat.format("{0}CustomProperties", localName);
        return QName.createQName(RM_CUSTOM_URI, localName);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#isCustomisable(org.alfresco.service.namespace.QName)
     */
    @Override
    public boolean isCustomisable(QName type)
    {
        mandatory("type", type);

        return getCustomisable().contains(type);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#makeCustomisable(org.alfresco.service.namespace.QName)
     */
    @Override
    public void makeCustomisable(QName type)
    {
        mandatory("type", type);

        if (customisableTypes == null)
        {
            // Add the type to the pending list
            pendingCustomisableTypes.add(type);
        }
        else
        {
            QName customAspect = getCustomAspect(type);
            if (getDictionaryService().getAspect(customAspect) == null)
            {
                NodeRef modelRef = getCustomModelRef(customAspect.getNamespaceURI());
                M2Model model = readCustomContentModel(modelRef);
                try
                {
                    // Create the new aspect to hold the custom properties
                    M2Aspect aspect = model.createAspect(customAspect.toPrefixString(getNamespaceService()));
                    aspect.setDescription(type.toPrefixString(getNamespaceService()));
                }
                finally
                {
                    writeCustomContentModel(modelRef, model);
                }
                customisableTypes.put(type, customAspect);
            }
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#unmakeCustomisable(org.alfresco.service.namespace.QName)
     */
    @Override
    public void unmakeCustomisable(QName type)
    {
        mandatory("type", type);

        if (customisableTypes == null)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_SERVICE_NOT_INIT));
        }

        QName customAspect = getCustomAspect(type);
        if (getDictionaryService().getAspect(customAspect) != null)
        {
            // TODO need to confirm that the custom properties are not being used!

            NodeRef modelRef = getCustomModelRef(customAspect.getNamespaceURI());
            M2Model model = readCustomContentModel(modelRef);
            try
            {
                // Create the new aspect to hold the custom properties
                model.removeAspect(customAspect.toPrefixString(getNamespaceService()));
            }
            finally
            {
                writeCustomContentModel(modelRef, model);
            }
            customisableTypes.remove(type);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#existsCustomProperty(org.alfresco.service.namespace.QName)
     */
    @Override
    public boolean existsCustomProperty(QName propertyName)
    {
        mandatory("propertyName", propertyName);

        boolean result = false;
        if (RM_CUSTOM_URI.equals(propertyName.getNamespaceURI()) &&
                getDictionaryService().getProperty(propertyName) != null)
        {
            result = true;
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#getCustomPropertyDefinitions()
     */
    public Map<QName, PropertyDefinition> getCustomPropertyDefinitions()
    {
        Map<QName, PropertyDefinition> result = new HashMap<>();
        for (QName customisableType : getCustomisable())
        {
            Map<QName, PropertyDefinition> props = getCustomPropertyDefinitions(customisableType);
            if (props != null)
            {
                result.putAll(props);
            }
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#getCustomPropertyDefinitions(QName)
     */
    public Map<QName, PropertyDefinition> getCustomPropertyDefinitions(QName customisableType)
    {
        mandatory("customisableType", customisableType);

        Map<QName, PropertyDefinition> propDefns = null;
        QName relevantAspectQName = getCustomAspect(customisableType);
        AspectDefinition aspectDefn = getDictionaryService().getAspect(relevantAspectQName);
        if (aspectDefn != null)
        {
            propDefns = aspectDefn.getProperties();
        }

        return propDefns;
    }

    /**
     * @throws CustomMetadataException
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#addCustomPropertyDefinition(org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName, java.lang.String, org.alfresco.service.namespace.QName, java.lang.String, java.lang.String)
     */
    public QName addCustomPropertyDefinition(QName propId, QName aspectName, String label, QName dataType, String title, String description) throws CustomMetadataException
    {
        return addCustomPropertyDefinition(propId, aspectName, label, dataType, title, description, null, false, false, false, null);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#addCustomPropertyDefinition(org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName, java.lang.String, org.alfresco.service.namespace.QName, java.lang.String, java.lang.String, java.lang.String, boolean, boolean, boolean, org.alfresco.service.namespace.QName)
     */
    public QName addCustomPropertyDefinition(QName propId,
                                             QName aspectName,
                                             String label,
                                             QName dataType,
                                             String title,
                                             String description,
                                             String defaultValue,
                                             boolean multiValued,
                                             boolean mandatory,
                                             boolean isProtected,
                                             QName lovConstraint) throws CustomMetadataException
    {
        if (!isCustomisable(aspectName))
        {
            throw new NotCustomisableMetadataException(aspectName.toPrefixString(getNamespaceService()));
        }

        // title parameter is currently ignored. Intentionally.
        if (propId == null)
        {
            // Generate a propId
            propId = this.generateQNameFor(label);
        }

        mandatory("aspectName", aspectName);
        mandatory("label", label);
        mandatory("dataType", dataType);

        NodeRef modelRef = getCustomModelRef(propId.getNamespaceURI());
        M2Model deserializedModel = readCustomContentModel(modelRef);

        QName customAspect = getCustomAspect(aspectName);
        M2Aspect customPropsAspect = deserializedModel.getAspect(customAspect.toPrefixString(getNamespaceService()));

        if (customPropsAspect == null)
        {
            throw new InvalidCustomAspectMetadataException(customAspect, aspectName.toPrefixString(getNamespaceService()));
        }

        String propIdAsString = propId.toPrefixString(getNamespaceService());
        M2Property customProp = customPropsAspect.getProperty(propIdAsString);
        if (customProp != null)
        {
            throw new PropertyAlreadyExistsMetadataException(propIdAsString);
        }

        M2Property newProp = customPropsAspect.createProperty(propIdAsString);
        newProp.setName(propIdAsString);
        newProp.setType(dataType.toPrefixString(getNamespaceService()));

        // Note that the title is used to store the RM 'label'.
        newProp.setTitle(label);
        newProp.setDescription(description);
        newProp.setDefaultValue(defaultValue);

        newProp.setMandatory(mandatory);
        newProp.setProtected(isProtected);
        newProp.setMultiValued(multiValued);

        newProp.setIndexed(true);
        newProp.setIndexedAtomically(true);
        newProp.setStoredInIndex(false);
        newProp.setIndexTokenisationMode(IndexTokenisationMode.FALSE);

        if (lovConstraint != null)
        {
            if (! dataType.equals(DataTypeDefinition.TEXT))
            {
                throw new CannotApplyConstraintMetadataException(lovConstraint, propIdAsString, dataType);
            }

            String lovConstraintQNameAsString = lovConstraint.toPrefixString(getNamespaceService());
            newProp.addConstraintRef(lovConstraintQNameAsString);
        }

        writeCustomContentModel(modelRef, deserializedModel);

        if (logger.isInfoEnabled())
        {
            logger.info("addCustomPropertyDefinition: "+label+
                    "=" + propIdAsString + " to aspect: "+aspectName);
        }

        return propId;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#updateCustomPropertyDefinitionName(org.alfresco.service.namespace.QName, java.lang.String)
     */
    public QName updateCustomPropertyDefinitionName(QName propQName, String newName) throws CustomMetadataException
    {
        mandatory("propQName", propQName);

        PropertyDefinition propDefn = getDictionaryService().getProperty(propQName);
        if (propDefn == null)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_PROP_EXIST, propQName));
        }

        if (newName == null)
        {
            return propQName;
        }

        QName newPropQName = getQNameForClientId(newName);
        if (newPropQName != null)
        {
           PropertyDefinition newPropDefn = getDictionaryService().getProperty(newPropQName);
           if (newPropDefn != null && !propDefn.equals(newPropDefn))
           {
              // The requested QName is already in use
              String propIdAsString = newPropQName.toPrefixString(getNamespaceService());
              throw new PropertyAlreadyExistsMetadataException(propIdAsString);
           }
        }

        NodeRef modelRef = getCustomModelRef(propQName.getNamespaceURI());
        M2Model deserializedModel = readCustomContentModel(modelRef);

        M2Property targetProperty = findProperty(propQName, deserializedModel);
        targetProperty.setName(new StringBuilder().append(RecordsManagementCustomModel.RM_CUSTOM_PREFIX).append(QName.NAMESPACE_PREFIX).append(newName).toString());
        targetProperty.setTitle(URLDecoder.decode(newName));
        writeCustomContentModel(modelRef, deserializedModel);

        if (logger.isInfoEnabled())
        {
            logger.info("setCustomPropertyDefinitionLabel: "+propQName+
                    "=" + newName);
        }

        return propQName;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#setCustomPropertyDefinitionLabel(org.alfresco.service.namespace.QName, java.lang.String)
     */
    public QName setCustomPropertyDefinitionLabel(QName propQName, String newLabel)
    {
        mandatory("propQName", propQName);

        PropertyDefinition propDefn = getDictionaryService().getProperty(propQName);
        if (propDefn == null)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_PROP_EXIST, propQName));
        }

        if (newLabel == null)
        {
            return propQName;
        }

        NodeRef modelRef = getCustomModelRef(propQName.getNamespaceURI());
        M2Model deserializedModel = readCustomContentModel(modelRef);

        M2Property targetProperty = findProperty(propQName, deserializedModel);

        targetProperty.setTitle(newLabel);
        writeCustomContentModel(modelRef, deserializedModel);

        if (logger.isInfoEnabled())
        {
            logger.info("setCustomPropertyDefinitionLabel: "+propQName+
                    "=" + newLabel);
        }

        return propQName;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#setCustomPropertyDefinitionConstraint(org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName)
     */
    public QName setCustomPropertyDefinitionConstraint(QName propQName, QName newLovConstraint)
    {
        mandatory("propQName", propQName);
        mandatory("newLovConstraint", newLovConstraint);

        PropertyDefinition propDefn = getDictionaryService().getProperty(propQName);
        if (propDefn == null)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_PROP_EXIST, propQName));
        }

        NodeRef modelRef = getCustomModelRef(propQName.getNamespaceURI());
        M2Model deserializedModel = readCustomContentModel(modelRef);

        M2Property targetProp = findProperty(propQName, deserializedModel);
        String dataType = targetProp.getType();

        if (! dataType.equals(DataTypeDefinition.TEXT.toPrefixString(getNamespaceService())))
        {

            throw new AlfrescoRuntimeException(I18NUtil.getMessage(CannotApplyConstraintMetadataException.MSG_CANNOT_APPLY_CONSTRAINT, newLovConstraint, targetProp.getName(), dataType));
        }
        String lovConstraintQNameAsString = newLovConstraint.toPrefixString(getNamespaceService());

        // Add the constraint - if it isn't already there (there should only be one constraint).
        String refOfExistingConstraint = (targetProp.getConstraints().isEmpty() ?
                    null :
                    targetProp.getConstraints().get(0).getRef());

        if (refOfExistingConstraint != null)
        {
            targetProp.removeConstraintRef(refOfExistingConstraint);
        }
        targetProp.addConstraintRef(lovConstraintQNameAsString);

        writeCustomContentModel(modelRef, deserializedModel);

        if (logger.isInfoEnabled())
        {
            logger.info("addCustomPropertyDefinitionConstraint: "+lovConstraintQNameAsString);
        }

        return propQName;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#removeCustomPropertyDefinitionConstraints(org.alfresco.service.namespace.QName)
     */
    public QName removeCustomPropertyDefinitionConstraints(QName propQName)
    {
        mandatory("propQName", propQName);

        PropertyDefinition propDefn = getDictionaryService().getProperty(propQName);
        if (propDefn == null)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_PROP_EXIST, propQName));
        }

        NodeRef modelRef = getCustomModelRef(propQName.getNamespaceURI());
        M2Model deserializedModel = readCustomContentModel(modelRef);

        M2Property targetProperty = findProperty(propQName, deserializedModel);

        // Need to count backwards to remove constraints
        for (int i = targetProperty.getConstraints().size() - 1; i >= 0; i--) 
        {
            String ref = targetProperty.getConstraints().get(i).getRef();
            targetProperty.removeConstraintRef(ref);
        }

        writeCustomContentModel(modelRef, deserializedModel);

        if (logger.isInfoEnabled())
        {
            logger.info("removeCustomPropertyDefinitionConstraints: "+propQName);
        }

        return propQName;
    }

    /**
     *
     * @param propQName
     * @param deserializedModel
     * @return
     */
    private M2Property findProperty(QName propQName, M2Model deserializedModel)
    {
        List<M2Aspect> aspects = deserializedModel.getAspects();
        // Search through the aspects looking for the custom property
        for (M2Aspect aspect : aspects)
        {
            for (M2Property prop : aspect.getProperties())
            {
                if (propQName.toPrefixString(getNamespaceService()).equals(prop.getName()))
                {
                    return prop;
                }
            }
        }
        throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_CUSTOM_PROP_EXIST, propQName));
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#removeCustomPropertyDefinition(org.alfresco.service.namespace.QName)
     */
    public void removeCustomPropertyDefinition(QName propQName)
    {
        mandatory("propQName", propQName);

        NodeRef modelRef = getCustomModelRef(propQName.getNamespaceURI());
        M2Model deserializedModel = readCustomContentModel(modelRef);

        String propQNameAsString = propQName.toPrefixString(getNamespaceService());

        String aspectName = null;

        boolean found = false;

        // Need to select the correct aspect in the customModel from which we'll
        // attempt to delete the property definition.
        for (QName customisableType : getCustomisable())
        {
        	aspectName = getCustomAspect(customisableType).toPrefixString(getNamespaceService());
            M2Aspect customPropsAspect = deserializedModel.getAspect(aspectName);

            if (customPropsAspect == null)
            {
                throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_UNKNOWN_ASPECT, aspectName));
            }

            M2Property prop = customPropsAspect.getProperty(propQNameAsString);
            if (prop != null)
            {
                if (logger.isDebugEnabled())
                {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Attempting to delete custom property: ");
                    msg.append(propQNameAsString);
                    logger.debug(msg.toString());
                }

                found = true;
                customPropsAspect.removeProperty(propQNameAsString);
                break;
            }
        }

        if (!found)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_PROP_EXIST, propQNameAsString));
        }

        writeCustomContentModel(modelRef, deserializedModel);

        if (logger.isInfoEnabled())
        {
            logger.info("deleteCustomPropertyDefinition: "+propQNameAsString+" from aspect: "+aspectName);
        }
    }

	/**
	 * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#getCustomReferenceDefinitions()
	 */
    public Map<QName, AssociationDefinition> getCustomReferenceDefinitions()
    {
        return getCustomAssociations();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService#addCustomReference(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void addCustomReference(NodeRef fromNode, NodeRef toNode, QName refId)
    {
        mandatory("fromNode", fromNode);
        mandatory("toNode", toNode);
        mandatory("refId", refId);

        getRelationshipService().addRelationship(refId.getLocalName(), fromNode, toNode);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#removeCustomReference(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void removeCustomReference(final NodeRef fromNode, final NodeRef toNode, final QName assocId)
    {
        mandatory("fromNode", fromNode);
        mandatory("toNode", toNode);
        mandatory("assocId",assocId);

        getRelationshipService().removeRelationship(assocId.getLocalName(), fromNode, toNode);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#getCustomReferencesFrom(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<AssociationRef> getCustomReferencesFrom(NodeRef node)
    {
        mandatory("node", node);

        return getNodeService().getTargetAssocs(node, RegexQNamePattern.MATCH_ALL);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#getCustomChildReferences(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<ChildAssociationRef> getCustomChildReferences(NodeRef node)
    {
        mandatory("node", node);

        return getNodeService().getChildAssocs(node);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#getCustomReferencesTo(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<AssociationRef> getCustomReferencesTo(NodeRef node)
    {
        mandatory("node", node);

        return getNodeService().getSourceAssocs(node, RegexQNamePattern.MATCH_ALL);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#getCustomParentReferences(org.alfresco.service.cmr.repository.NodeRef)
     */
    public List<ChildAssociationRef> getCustomParentReferences(NodeRef node)
    {
        mandatory("node", node);

        return getNodeService().getParentAssocs(node);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#addCustomAssocDefinition(java.lang.String)
     *
     * note: currently RMC custom assocs only
     */
    public QName addCustomAssocDefinition(String label)
    {
        mandatoryString("label", label);

        return addCustomChildAssocDefinition(label, label);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#addCustomChildAssocDefinition(java.lang.String, java.lang.String)
     *
     * note: currently RMC custom assocs only
     */
    public QName addCustomChildAssocDefinition(String source, String target)
    {
        mandatoryString("source", source);
        mandatoryString("target", target);

        RelationshipDisplayName displayName = new RelationshipDisplayName(source, target);
        RelationshipDefinition relationshipDefinition = getRelationshipService().createRelationshipDefinition(displayName);

        return QName.createQName(RM_CUSTOM_PREFIX, relationshipDefinition.getUniqueName(), getNamespaceService());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#updateCustomChildAssocDefinition(org.alfresco.service.namespace.QName, java.lang.String, java.lang.String)
     *
     * note: currently RMC custom assocs only
     */
    public QName updateCustomChildAssocDefinition(QName refQName, String newSource, String newTarget)
    {
        mandatory("refQName", refQName);
        mandatoryString("newSource", newSource);
        mandatoryString("newTarget", newTarget);

        RelationshipDisplayName displayName = new RelationshipDisplayName(newSource, newTarget);
        String localName = refQName.getLocalName();
        RelationshipDefinition relationshipDefinition = getRelationshipService().updateRelationshipDefinition(localName, displayName);
        return QName.createQName(RM_CUSTOM_PREFIX, relationshipDefinition.getUniqueName(), getNamespaceService());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#updateCustomAssocDefinition(org.alfresco.service.namespace.QName, java.lang.String)
     *
     * note: currently RMC custom assocs only
     */
    public QName updateCustomAssocDefinition(QName refQName, String newLabel)
    {
        mandatory("refQName", refQName);
        mandatoryString("newLabel", newLabel);

        return updateCustomChildAssocDefinition(refQName, newLabel, newLabel);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#addCustomConstraintDefinition(org.alfresco.service.namespace.QName, java.lang.String, boolean, java.util.List, org.alfresco.module.org_alfresco_module_rm.caveat.RMListOfValuesConstraint.MatchLogic)
     */
    public void addCustomConstraintDefinition(QName constraintName, String title, boolean caseSensitive, List<String> allowedValues, MatchLogic matchLogic)
    {
        mandatory("constraintName", constraintName);
        mandatoryString("title", title);
        mandatory("allowedValues", allowedValues);
        mandatory("matchLogic", matchLogic);

        NodeRef modelRef = getCustomModelRef(constraintName.getNamespaceURI());
        M2Model deserializedModel = readCustomContentModel(modelRef);

        String constraintNameAsPrefixString = constraintName.toPrefixString(getNamespaceService());

        M2Constraint customConstraint = deserializedModel.getConstraint(constraintNameAsPrefixString);
        if (customConstraint != null)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_CONSTRAINT_EXISTS, constraintNameAsPrefixString));
        }

        M2Constraint newCon = deserializedModel.createConstraint(constraintNameAsPrefixString, CUSTOM_CONSTRAINT_TYPE);

        newCon.setTitle(title);
        newCon.createParameter(PARAM_ALLOWED_VALUES, allowedValues);
        newCon.createParameter(PARAM_CASE_SENSITIVE, caseSensitive ? "true" : "false");
        newCon.createParameter(PARAM_MATCH_LOGIC, matchLogic.toString());

        writeCustomContentModel(modelRef, deserializedModel);

        if (logger.isInfoEnabled())
        {
            logger.info("addCustomConstraintDefinition: "+constraintNameAsPrefixString+" (valueCnt: "+allowedValues.size()+")");
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#changeCustomConstraintValues(org.alfresco.service.namespace.QName, java.util.List)
     */
    public void changeCustomConstraintValues(QName constraintName, List<String> newAllowedValues)
    {
        mandatory("constraintName", constraintName);
        mandatory("newAllowedValues", newAllowedValues);

        NodeRef modelRef = getCustomModelRef(constraintName.getNamespaceURI());
        M2Model deserializedModel = readCustomContentModel(modelRef);

        String constraintNameAsPrefixString = constraintName.toPrefixString(getNamespaceService());

        M2Constraint customConstraint = deserializedModel.getConstraint(constraintNameAsPrefixString);
        if (customConstraint == null)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_CANNOT_FIND_CONSTRAINT, constraintNameAsPrefixString));
        }

        String type = customConstraint.getType();
        if (type == null ||
            (!type.equals(CUSTOM_CONSTRAINT_TYPE) &&
             !type.equals(CAPABILITY_CUSTOM_CONSTRAINT_TYPE)))
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_UNEXPECTED_TYPE_CONSTRAINT, type, constraintNameAsPrefixString, CUSTOM_CONSTRAINT_TYPE));
        }

        customConstraint.removeParameter(PARAM_ALLOWED_VALUES);
        customConstraint.createParameter(PARAM_ALLOWED_VALUES, newAllowedValues);

        writeCustomContentModel(modelRef, deserializedModel);

        if (logger.isInfoEnabled())
        {
            logger.info("changeCustomConstraintValues: "+constraintNameAsPrefixString+" (valueCnt: "+newAllowedValues.size()+")");
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#changeCustomConstraintTitle(org.alfresco.service.namespace.QName, java.lang.String)
     */
    public void changeCustomConstraintTitle(QName constraintName, String title)
    {
        mandatory("constraintName", constraintName);
        mandatoryString("title", title);

        NodeRef modelRef = getCustomModelRef(constraintName.getNamespaceURI());
        M2Model deserializedModel = readCustomContentModel(modelRef);

        String constraintNameAsPrefixString = constraintName.toPrefixString(getNamespaceService());

        M2Constraint customConstraint = deserializedModel.getConstraint(constraintNameAsPrefixString);
        if (customConstraint == null)
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_CANNOT_FIND_CONSTRAINT, constraintNameAsPrefixString));
        }

        String type = customConstraint.getType();
        if ((type == null) || (! type.equals(CUSTOM_CONSTRAINT_TYPE)))
        {

            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_UNEXPECTED_TYPE_CONSTRAINT, type, constraintNameAsPrefixString, CUSTOM_CONSTRAINT_TYPE));
        }

        customConstraint.setTitle(title);

        writeCustomContentModel(modelRef, deserializedModel);

        if (logger.isInfoEnabled())
        {
            logger.info("changeCustomConstraintTitle: "+constraintNameAsPrefixString+" (title: "+title+")");
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#getCustomConstraintDefinitions(org.alfresco.service.namespace.QName)
     */
    public List<ConstraintDefinition> getCustomConstraintDefinitions(QName modelQName)
    {
        mandatory("modelQName", modelQName);

        Collection<ConstraintDefinition> conDefs = getDictionaryService().getConstraints(modelQName, true);

        for (ConstraintDefinition conDef : conDefs)
        {
            Constraint con = conDef.getConstraint();
            if (! (con instanceof RMListOfValuesConstraint))
            {
                conDefs.remove(conDef);
            }
        }

        return new ArrayList<>(conDefs);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#removeCustomConstraintDefinition(org.alfresco.service.namespace.QName)
     */
    public void removeCustomConstraintDefinition(QName constraintName)
    {
        mandatory("constraintName", constraintName);

        NodeRef modelRef = getCustomModelRef(constraintName.getNamespaceURI());
        M2Model deserializedModel = readCustomContentModel(modelRef);

        String constraintNameAsPrefixString = constraintName.toPrefixString(getNamespaceService());

        M2Constraint customConstraint = deserializedModel.getConstraint(constraintNameAsPrefixString);
        if (customConstraint == null)
        {

            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_CANNOT_FIND_CONSTRAINT, constraintNameAsPrefixString));
        }

        deserializedModel.removeConstraint(constraintNameAsPrefixString);

        writeCustomContentModel(modelRef, deserializedModel);

        if (logger.isInfoEnabled())
        {
            logger.info("deleteCustomConstraintDefinition: "+constraintNameAsPrefixString);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#getQNameForClientId(java.lang.String)
     */
    public QName getQNameForClientId(String localName)
    {
        //TODO 1. After certification. This implementation currently does not support reference,
        // property, constraints definitions with the same names, which is technically allowed by Alfresco.

        //TODO 2. Note the implicit assumption here that all custom references will have
        // unique titles. This is, in fact, not guaranteed.

        QName propertyResult = null;
        for (QName qn : getCustomPropertyDefinitions().keySet())
        {
            if (localName != null && localName.equals(qn.getLocalName()))
            {
                propertyResult = qn;
            }
        }

        if (propertyResult != null)
        {
            return propertyResult;
        }

        QName referenceResult = null;
        for (QName refQn : getCustomReferenceDefinitions().keySet())
        {
            if (localName != null && localName.equals(refQn.getLocalName()))
            {
                referenceResult = refQn;
            }
        }

        // TODO Handle the case where both are not null
        return referenceResult;
    }

    /**
     * @param clientId
     * @return
     */
    private QName generateQNameFor(String clientId)
    {
        if (getQNameForClientId(clientId) != null)
        {
            // TODO log it's already taken. What to do?
            throw new IllegalArgumentException(I18NUtil.getMessage(MSG_ERROR_CLIENT_ID, clientId));
        }

        String newGUID = GUID.generate();
        QName newQName = QName.createQName(RM_CUSTOM_PREFIX, newGUID, getNamespaceService());

        return newQName;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#splitSourceTargetId(java.lang.String)
     */
    public String[] splitSourceTargetId(String sourceTargetId)
    {
        mandatoryString("sourceTargetId", sourceTargetId);

        return splitAssociationDefinitionTitle(sourceTargetId);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService#getCompoundIdFor(java.lang.String, java.lang.String)
     */
    public String getCompoundIdFor(String sourceId, String targetId)
    {
        mandatoryString("sourceId", sourceId);
        mandatoryString("targetId", targetId);

        return composeAssociationDefinitionTitle(sourceId, targetId);
    }
}
