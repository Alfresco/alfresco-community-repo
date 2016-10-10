/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.record;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.BeforeFileRecord;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnFileRecord;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierService;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.rma.type.RecordsManagementContainerType;
import org.alfresco.module.org_alfresco_module_rm.model.security.ModelAccessDeniedException;
import org.alfresco.module.org_alfresco_module_rm.notification.RecordsManagementNotificationHelper;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.alfresco.module.org_alfresco_module_rm.report.ReportModel;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionService;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.ClassPolicyDelegate;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.security.permissions.impl.ExtendedPermissionService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Record service implementation.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
@BehaviourBean
public class RecordServiceImpl extends BaseBehaviourBean
                               implements RecordService,
                                          RecordsManagementModel,
                                          RecordsManagementCustomModel,
                                          NodeServicePolicies.OnCreateChildAssociationPolicy,
                                          NodeServicePolicies.OnAddAspectPolicy,
                                          NodeServicePolicies.OnRemoveAspectPolicy,
                                          NodeServicePolicies.OnUpdatePropertiesPolicy
{
    /** Logger */
    private static Log logger = LogFactory.getLog(RecordServiceImpl.class);

    /** transation data key */
    private static final String KEY_IGNORE_ON_UPDATE = "ignoreOnUpdate";
    private static final String KEY_PENDING_FILLING = "pendingFilling";
    public static final String KEY_NEW_RECORDS = "newRecords";

    /** I18N */
    private static final String MSG_NODE_HAS_ASPECT = "rm.service.node-has-aspect";
    private static final String FINAL_VERSION = "rm.service.final-version";
    private static final String FINAL_DESCRIPTION = "rm.service.final-version-description";

    /** Always edit property array */
    private static final QName[] ALWAYS_EDIT_PROPERTIES = new QName[]
    {
       ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA
    };

    /** always edit model URI's */
    protected List<String> getAlwaysEditURIs()
    {
        return newArrayList(
            NamespaceService.SECURITY_MODEL_1_0_URI,
            NamespaceService.SYSTEM_MODEL_1_0_URI,
            NamespaceService.WORKFLOW_MODEL_1_0_URI,
            NamespaceService.APP_MODEL_1_0_URI,
            NamespaceService.DATALIST_MODEL_1_0_URI,
            NamespaceService.DICTIONARY_MODEL_1_0_URI,
            NamespaceService.BPM_MODEL_1_0_URI,
            NamespaceService.RENDITION_MODEL_1_0_URI
        );
    }

    /** record model URI's */
    public static final List<String> RECORD_MODEL_URIS = Collections.unmodifiableList(
        Arrays.asList(
            RM_URI,
            RM_CUSTOM_URI,
            ReportModel.RMR_URI,
            RecordableVersionModel.RMV_URI,
            DOD5015Model.DOD_URI
    ));

    /** non-record model URI's */
    private static final String[] NON_RECORD_MODEL_URIS = new String[]
    {
        NamespaceService.AUDIO_MODEL_1_0_URI,
        NamespaceService.CONTENT_MODEL_1_0_URI,
        NamespaceService.EMAILSERVER_MODEL_URI,
        NamespaceService.EXIF_MODEL_1_0_URI,
        NamespaceService.FORUMS_MODEL_1_0_URI,
        NamespaceService.LINKS_MODEL_1_0_URI,
        NamespaceService.REPOSITORY_VIEW_1_0_URI
    };

    /** Indentity service */
    private IdentifierService identifierService;

    /** Extended permission service */
    private ExtendedPermissionService extendedPermissionService;

    /** Extended security service */
    private ExtendedSecurityService extendedSecurityService;

    /** File plan service */
    private FilePlanService filePlanService;

    /** Records management notification helper */
    private RecordsManagementNotificationHelper notificationHelper;

    /** Policy component */
    private PolicyComponent policyComponent;

    /** Ownable service */
    private OwnableService ownableService;

    /** Capability service */
    private CapabilityService capabilityService;

    /** Rule service */
    private RuleService ruleService;

    /** File folder service */
    private FileFolderService fileFolderService;

    /** Record folder service */
    private RecordFolderService recordFolderService;

    /** File plan role service */
    private FilePlanRoleService filePlanRoleService;

    /** Permission service */
    private PermissionService permissionService;

    /** Version service */
    private VersionService versionService;

    /** Relationship service */
    private RelationshipService relationshipService;

    /** Disposition service */
    private DispositionService dispositionService;

    /** records management container type */
    private RecordsManagementContainerType recordsManagementContainerType;
    
    /** recordable version service */
    private RecordableVersionService recordableVersionService;

    /** list of available record meta-data aspects and the file plan types the are applicable to */
    private Map<QName, Set<QName>> recordMetaDataAspects;

    /** policies */
    private ClassPolicyDelegate<BeforeFileRecord> beforeFileRecord;
    private ClassPolicyDelegate<OnFileRecord> onFileRecord;

    /** Behaviours */
    private JavaBehaviour onCreateChildAssociation = new JavaBehaviour(
                                                            this,
                                                            "onCreateChildAssociation",
                                                            NotificationFrequency.FIRST_EVENT);

    /**
     * @param identifierService identifier service
     */
    public void setIdentifierService(IdentifierService identifierService)
    {
        this.identifierService = identifierService;
    }

    /**
     * @param extendedPermissionService extended permission service
     */
    public void setExtendedPermissionService(ExtendedPermissionService extendedPermissionService)
    {
        this.extendedPermissionService = extendedPermissionService;
    }

    /**
     * @param extendedSecurityService   extended security service
     */
    public void setExtendedSecurityService(ExtendedSecurityService extendedSecurityService)
    {
        this.extendedSecurityService = extendedSecurityService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param notificationHelper notification helper
     */
    public void setNotificationHelper(RecordsManagementNotificationHelper notificationHelper)
    {
        this.notificationHelper = notificationHelper;
    }

    /**
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * @param ownableService    ownable service
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }

    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    /**
     * @param ruleService   rule service
     */
    public void setRuleService(RuleService ruleService)
    {
        this.ruleService = ruleService;
    }

    /**
     * @param fileFolderService file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * @param recordFolderService record folder service
     */
    public void setRecordFolderService(RecordFolderService recordFolderService)
    {
        this.recordFolderService = recordFolderService;
    }

    /**
     * @param filePlanRoleService file plan role service
     */
    public void setFilePlanRoleService(FilePlanRoleService filePlanRoleService)
    {
        this.filePlanRoleService = filePlanRoleService;
    }

    /**
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * @param versionService version service
     */
    public void setVersionService(VersionService versionService)
    {
        this.versionService = versionService;
    }

    /**
     * @param relationshipService   relationship service
     */
    public void setRelationshipService(RelationshipService relationshipService)
    {
        this.relationshipService = relationshipService;
    }

    /**
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * @param recordsManagementContainerType    records management container type
     */
    public void setRecordsManagementContainerType(RecordsManagementContainerType recordsManagementContainerType)
    {
		this.recordsManagementContainerType = recordsManagementContainerType;
	}
    
    /**
     * @param recordableVersionService  recordable version service
     */
    public void setRecordableVersionService(RecordableVersionService recordableVersionService)
    {
        this.recordableVersionService = recordableVersionService;
    }

    /**
     * Init method
     */
    public void init()
    {
        // bind policies
        beforeFileRecord = policyComponent.registerClassPolicy(BeforeFileRecord.class);
        onFileRecord = policyComponent.registerClassPolicy(OnFileRecord.class);

        // bind behaviours
        policyComponent.bindAssociationBehaviour(
                NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME,
                TYPE_RECORD_FOLDER,
                ContentModel.ASSOC_CONTAINS,
                onCreateChildAssociation);
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnRemoveAspectPolicy#onRemoveAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            type = "sys:noContent"
    )
    public void onRemoveAspect(NodeRef nodeRef, QName aspect)
    {

        if (nodeService.hasAspect(nodeRef, ASPECT_RECORD))
        {
            ContentData contentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);

            // Only switch name back to the format of "name (identifierId)" if content size is non-zero, else leave it as the original name to avoid CIFS shuffling issues.
            if (contentData != null && contentData.getSize() > 0)
            {
                switchNames(nodeRef);
            }
        }
        else
        {
            // check whether filling is pending aspect removal
            Set<NodeRef> pendingFilling = transactionalResourceHelper.getSet(KEY_PENDING_FILLING);
            if (pendingFilling.contains(nodeRef))
            {
                file(nodeRef);
            }
        }
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            type = "sys:noContent"
    )
    public void onAddAspect(NodeRef nodeRef, QName aspect)
    {
        switchNames(nodeRef);
    }

    /**
     * Helper method to switch the name of the record around.  Used to support record creation via
     * file protocols.
     *
     * @param nodeRef   node reference (record)
     */
    private void switchNames(NodeRef nodeRef)
    {
        try
        {
            if (nodeService.hasAspect(nodeRef, ASPECT_RECORD))
            {
                String origionalName =  (String)nodeService.getProperty(nodeRef, PROP_ORIGIONAL_NAME);
                if (origionalName != null)
                {
                    String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                    fileFolderService.rename(nodeRef, origionalName);
                    nodeService.setProperty(nodeRef, PROP_ORIGIONAL_NAME, name);
                }
            }
        }
        catch (FileExistsException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(e.getMessage());
            }
        }
        catch (InvalidNodeRefException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(e.getMessage());
            }
        }
        catch (FileNotFoundException e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug(e.getMessage());
            }
        }
    }

    /**
     * Behaviour executed when a new item is added to a record folder.
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Override
    public void onCreateChildAssociation(final ChildAssociationRef childAssocRef, final boolean bNew)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                onCreateChildAssociation.disable();
                try
                {
                    NodeRef nodeRef = childAssocRef.getChildRef();
                    if (nodeService.exists(nodeRef)   &&
                        !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY) &&
                        !nodeService.getType(nodeRef).equals(TYPE_RECORD_FOLDER) &&
                        !nodeService.getType(nodeRef).equals(TYPE_RECORD_CATEGORY))
                    {
                        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_NO_CONTENT))
                        {
                            // we need to postpone filling until the NO_CONTENT aspect is removed
                            Set<NodeRef> pendingFilling = transactionalResourceHelper.getSet(KEY_PENDING_FILLING);
                            pendingFilling.add(nodeRef);
                        }
                        else
                        {
                            // store information about the 'new' record in the transaction
                            // @since 2.3
                            // @see https://issues.alfresco.com/jira/browse/RM-1956
                            if (bNew)
                            {
                                Set<NodeRef> newRecords = transactionalResourceHelper.getSet(KEY_NEW_RECORDS);
                                newRecords.add(nodeRef);
                            }
                            else
                            {
                                // if we are linking a record
                                NodeRef parentNodeRef = childAssocRef.getParentRef();
                                if (isRecord(nodeRef) && isRecordFolder(parentNodeRef))
                                {
                                    // validate the link conditions
                                    validateLinkConditions(nodeRef, parentNodeRef);
                                }
                            }

                            // create and file the content as a record
                            file(nodeRef);
                        }
                    }
                }
                catch (RecordLinkRuntimeException e)
                {
                    // rethrow exception
                    throw e;
                }
                catch (AlfrescoRuntimeException e)
                {
                    // do nothing but log error
                    if (logger.isWarnEnabled())
                    {
                        logger.warn("Unable to file pending record.", e);
                    }
                }
                finally
                {
                    onCreateChildAssociation.enable();
                }

                return null;
            }
        }, AuthenticationUtil.getSystemUserName());
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#disablePropertyEditableCheck()
     */
    @Override
    public void disablePropertyEditableCheck()
    {
        org.alfresco.repo.policy.Behaviour behaviour = getBehaviour("onUpdateProperties");
        if (behaviour != null)
        {
            getBehaviour("onUpdateProperties").disable();
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#disablePropertyEditableCheck(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void disablePropertyEditableCheck(NodeRef nodeRef)
    {
        Set<NodeRef> ignoreOnUpdate = transactionalResourceHelper.getSet(KEY_IGNORE_ON_UPDATE);
        ignoreOnUpdate.add(nodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#enablePropertyEditableCheck()
     */
    @Override
    public void enablePropertyEditableCheck()
    {
        org.alfresco.repo.policy.Behaviour behaviour = getBehaviour("onUpdateProperties");
        if (behaviour != null)
        {
            behaviour.enable();
        }
    }

    /**
     * Ensure that the user only updates record properties that they have permission to.
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    @Behaviour
    (
            name = "onUpdateProperties",
            kind = BehaviourKind.CLASS,
            type= "rma:record"
    )
    public void onUpdateProperties(final NodeRef nodeRef, final Map<QName, Serializable> before, final Map<QName, Serializable> after)
    {
        if (AuthenticationUtil.getFullyAuthenticatedUser() != null &&
            !AuthenticationUtil.isRunAsUserTheSystemUser() &&
            nodeService.exists(nodeRef) &&
            isRecord(nodeRef) &&
            !transactionalResourceHelper.getSet(KEY_IGNORE_ON_UPDATE).contains(nodeRef))
        {
            for (Map.Entry<QName, Serializable> entry : after.entrySet())
            {
                Serializable beforeValue = null;
                QName property = entry.getKey();
                if (before != null)
                {
                    beforeValue = before.get(property);
                }

                Serializable afterValue = entry.getValue();
                boolean propertyUnchanged = false;
                if (beforeValue instanceof Date && afterValue instanceof Date)
                {
                    // deal with date values, remove the seconds and milliseconds for the
                	// comparison as they are removed from the submitted for data
                	Calendar beforeCal = Calendar.getInstance();
                	beforeCal.setTime((Date)beforeValue);
                	Calendar afterCal = Calendar.getInstance();
                	afterCal.setTime((Date)afterValue);
                	beforeCal.set(Calendar.SECOND, 0);
                	beforeCal.set(Calendar.MILLISECOND, 0);
                	afterCal.set(Calendar.SECOND, 0);
                	afterCal.set(Calendar.MILLISECOND, 0);
                	propertyUnchanged = (beforeCal.compareTo(afterCal) == 0);
                }
                else if ((afterValue instanceof Boolean) && (beforeValue == null) && (afterValue.equals(Boolean.FALSE)))
                {
            		propertyUnchanged = true;
                }
                else
                {
                    // otherwise
                    propertyUnchanged = EqualsHelper.nullSafeEquals(beforeValue, afterValue);
                }

                if (!propertyUnchanged &&
                    !(ContentModel.PROP_CONTENT.equals(property) && beforeValue == null) &&
                    !isPropertyEditable(nodeRef, property))
                {
                    // the user can't edit the record property
                    throw new ModelAccessDeniedException(
                            "The user " + AuthenticationUtil.getFullyAuthenticatedUser() +
                            " does not have the permission to edit the record property " + property.toString() +
                            " on the node " + nodeRef.toString());
                }
            }
        }
    }

    /**
     * Get map containing record metadata aspects.
     *
     * @return  {@link Map}<{@link QName}, {@link Set}<{@link QName}>>  map containing record metadata aspects
     *
     * @since 2.2
     */
    protected Map<QName, Set<QName>> getRecordMetadataAspectsMap()
    {
        if (recordMetaDataAspects == null)
        {
            // create map
            recordMetaDataAspects = new HashMap<QName, Set<QName>>();

            // init with legacy aspects
            initRecordMetaDataMap();
        }

        return recordMetaDataAspects;
    }

    /**
     * Initialises the record meta-data map.
     * <p>
     * This is here to support backwards compatibility in case an existing
     * customization (pre 2.2) is still using the record meta-data aspect.
     *
     * @since 2.2
     */
    private void initRecordMetaDataMap()
    {
        // populate the inital set of record meta-data aspects .. this is here for legacy reasons
        Collection<QName> aspects = dictionaryService.getAllAspects();
        for (QName aspect : aspects)
        {
            AspectDefinition def = dictionaryService.getAspect(aspect);
            if (def != null)
            {
                QName parent = def.getParentName();
                if (parent != null && ASPECT_RECORD_META_DATA.equals(parent))
                {
                    recordMetaDataAspects.put(aspect, Collections.singleton(TYPE_FILE_PLAN));
                }
            }
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#registerRecordMetadataAspect(org.alfresco.service.namespace.QName, org.alfresco.service.namespace.QName)
     */
    @Override
    public void registerRecordMetadataAspect(QName recordMetadataAspect, QName filePlanType)
    {
        ParameterCheck.mandatory("recordMetadataAspect", recordMetadataAspect);
        ParameterCheck.mandatory("filePlanType", filePlanType);

        Set<QName> filePlanTypes = null;

        if (getRecordMetadataAspectsMap().containsKey(recordMetadataAspect))
        {
            // get the current set of file plan types for this aspect
            filePlanTypes = getRecordMetadataAspectsMap().get(recordMetadataAspect);
        }
        else
        {
            // create a new set for the file plan type
            filePlanTypes = new HashSet<QName>(1);
            getRecordMetadataAspectsMap().put(recordMetadataAspect, filePlanTypes);
        }

        // add the file plan type
        filePlanTypes.add(filePlanType);
    }

    /**
     * @deprecated since 2.2, file plan is required to provide context
     */
    @Override
    @Deprecated
    public Set<QName> getRecordMetaDataAspects()
    {
        return getRecordMetadataAspects(TYPE_FILE_PLAN);
    }

    /**
     *  @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#isRecordMetadataAspect(org.alfresco.service.namespace.QName)
     */
    @Override
    public boolean isRecordMetadataAspect(QName aspect)
    {
        return getRecordMetadataAspectsMap().containsKey(aspect);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#isRecordMetadataProperty(org.alfresco.service.namespace.QName)
     */
    @Override
    public boolean isRecordMetadataProperty(QName property)
    {
        boolean result = false;
        PropertyDefinition propertyDefinition = dictionaryService.getProperty(property);
        if (propertyDefinition != null)
        {
            ClassDefinition classDefinition = propertyDefinition.getContainerClass();
            if (classDefinition != null &&
                getRecordMetadataAspectsMap().containsKey(classDefinition.getName()))
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#getRecordMetaDataAspects(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public Set<QName> getRecordMetadataAspects(NodeRef nodeRef)
    {
        QName filePlanType = TYPE_FILE_PLAN;

        if (nodeRef != null)
        {
            NodeRef filePlan = getFilePlan(nodeRef);
            filePlanType = nodeService.getType(filePlan);
        }

        return getRecordMetadataAspects(filePlanType);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#getRecordMetadataAspects(org.alfresco.service.namespace.QName)
     */
    @Override
    public Set<QName> getRecordMetadataAspects(QName filePlanType)
    {
        Set<QName> result = new HashSet<QName>(getRecordMetadataAspectsMap().size());

        for (Entry<QName, Set<QName>> entry : getRecordMetadataAspectsMap().entrySet())
        {
            if (entry.getValue().contains(filePlanType))
            {
                result.add(entry.getKey());
            }
        }

        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#createRecord(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void createRecord(NodeRef filePlan, NodeRef nodeRef)
    {
        createRecord(filePlan, nodeRef, true);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#createRecord(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override
    public void createRecord(final NodeRef filePlan, final NodeRef nodeRef, final boolean isLinked)
    {
        ParameterCheck.mandatory("filePlan", filePlan);
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("isLinked", isLinked);

        // first we do a sanity check to ensure that the user has at least write permissions on the document
        if (extendedPermissionService.hasPermission(nodeRef, PermissionService.WRITE) != AccessStatus.ALLOWED)
        {
            throw new AccessDeniedException("Can not create record from document, because the user " +
                                            AuthenticationUtil.getRunAsUser() +
                                            " does not have Write permissions on the doucment " +
                                            nodeRef.toString());
        }


        // do the work of creating the record as the system user
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                if (!nodeService.hasAspect(nodeRef, ASPECT_RECORD))
                {
                    // disable delete rules
                    ruleService.disableRuleType("outbound");
                    try
                    {
                        // get the new record container for the file plan
                        NodeRef newRecordContainer = filePlanService.getUnfiledContainer(filePlan);
                        if (newRecordContainer == null)
                        {
                            throw new AlfrescoRuntimeException("Unable to create record, because new record container could not be found.");
                        }

                        // get the documents readers and writers
                        Pair<Set<String>, Set<String>> readersAndWriters = extendedPermissionService.getReadersAndWriters(nodeRef);

                        // get the current owner
                        String owner = ownableService.getOwner(nodeRef);

                        // get the documents primary parent assoc
                        ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(nodeRef);

                        // get the latest version record, if there is one
                        NodeRef latestVersionRecord = getLatestVersionRecord(nodeRef);

                        behaviourFilter.disableBehaviour();
                        try
                        {
                        	// move the document into the file plan
                        	nodeService.moveNode(nodeRef, newRecordContainer, ContentModel.ASSOC_CONTAINS, parentAssoc.getQName());
                        }
                        finally
                        {
                        	behaviourFilter.enableBehaviour();
                        }

                        // save the information about the originating details
                        Map<QName, Serializable> aspectProperties = new HashMap<QName, Serializable>(3);
                        aspectProperties.put(PROP_RECORD_ORIGINATING_LOCATION, parentAssoc.getParentRef());
                        aspectProperties.put(PROP_RECORD_ORIGINATING_USER_ID, owner);
                        aspectProperties.put(PROP_RECORD_ORIGINATING_CREATION_DATE, new Date());
                        nodeService.addAspect(nodeRef, ASPECT_RECORD_ORIGINATING_DETAILS, aspectProperties);

                        // make the document a record
                        makeRecord(nodeRef);

                        if (latestVersionRecord != null)
                        {
                            // indicate that this is the 'final' record version
                            PropertyMap versionRecordProps = new PropertyMap(2);
                            versionRecordProps.put(RecordableVersionModel.PROP_VERSION_LABEL, I18NUtil.getMessage(FINAL_VERSION));
                            versionRecordProps.put(RecordableVersionModel.PROP_VERSION_DESCRIPTION, I18NUtil.getMessage(FINAL_DESCRIPTION));
                            nodeService.addAspect(nodeRef, RecordableVersionModel.ASPECT_VERSION_RECORD, versionRecordProps);

                            // link to previous version
                            relationshipService.addRelationship(CUSTOM_REF_VERSIONS.getLocalName(), nodeRef, latestVersionRecord);
                        }

                        if (isLinked)
                        {
                            // turn off rules
                            ruleService.disableRules();
                            try
                            {
                                // maintain the original primary location
                                nodeService.addChild(parentAssoc.getParentRef(), nodeRef, parentAssoc.getTypeQName(), parentAssoc.getQName());

                                // set the extended security
                                extendedSecurityService.set(nodeRef, readersAndWriters);
                            }
                            finally
                            {
                                ruleService.enableRules();
                            }
                        }
                    }
                    finally
                    {
                        ruleService.enableRuleType("outbound");
                    }
                }

                return null;
            }
        });
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#createRecordFromCopy(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public NodeRef createRecordFromCopy(final NodeRef filePlan, final NodeRef nodeRef)
    {
        return authenticationUtil.runAsSystem(new RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                // get the unfiled record folder
                final NodeRef unfiledRecordFolder = filePlanService.getUnfiledContainer(filePlan);

                // get the documents readers and writers
                Pair<Set<String>, Set<String>> readersAndWriters = extendedPermissionService.getReadersAndWriters(nodeRef);

                // copy version state and create record
                NodeRef record = null;
                try
                {
                    List<AssociationRef> originalAssocs = null;
                    if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_COPIEDFROM))
                    {
                        // take a note of any copyFrom information already on the node
                        originalAssocs = nodeService.getTargetAssocs(nodeRef, ContentModel.ASSOC_ORIGINAL);
                    }

                    recordsManagementContainerType.disable();
                    try
                    {
	                    // create a copy of the original state and add it to the unfiled record container
	                    FileInfo recordInfo = fileFolderService.copy(nodeRef, unfiledRecordFolder, null);
	                    record = recordInfo.getNodeRef();
                    }
                    finally
                    {
                    	recordsManagementContainerType.enable();
                    }
                    
                    // if versionable, then remove without destroying version history,
                    // because it is being shared with the originating document
                    behaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);
                    try
                    {
                        nodeService.removeAspect(record, ContentModel.ASPECT_VERSIONABLE);
                    }
                    finally
                    {
                        behaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
                    }
                    
                    // make record
                    makeRecord(record);

                    // remove added copy assocs
                    List<AssociationRef> recordAssocs = nodeService.getTargetAssocs(record, ContentModel.ASSOC_ORIGINAL);
                    for (AssociationRef recordAssoc : recordAssocs)
                    {
                        nodeService.removeAssociation(
                                recordAssoc.getSourceRef(),
                                recordAssoc.getTargetRef(),
                                ContentModel.ASSOC_ORIGINAL);
                    }

                    // re-add origional assocs or remove aspect
                    if (originalAssocs == null)
                    {
                        nodeService.removeAspect(record, ContentModel.ASPECT_COPIEDFROM);
                    }
                    else
                    {
                        for (AssociationRef originalAssoc : originalAssocs)
                        {
                            nodeService.createAssociation(record, originalAssoc.getTargetRef(), ContentModel.ASSOC_ORIGINAL);
                        }
                    }
                }
                catch (FileNotFoundException e)
                {
                    throw new AlfrescoRuntimeException("Can't create recorded version, because copy fails.", e);
                }

                // set extended security on record
                extendedSecurityService.set(record, readersAndWriters);

                return record;
            }
        });
    }

    /**
     * Helper to get the latest version record for a given document (ie non-record)
     *
     * @param nodeRef   node reference
     * @return NodeRef  latest version record, null otherwise
     */
    private NodeRef getLatestVersionRecord(NodeRef nodeRef)
    {
        NodeRef versionRecord = null;
       
 
        recordableVersionService.createSnapshotVersion(nodeRef);
        // wire record up to previous record
        VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
        if (versionHistory != null)
        {
            Collection<Version> previousVersions = versionHistory.getAllVersions();
            for (Version previousVersion : previousVersions)
            {
                // look for the associated record
                final NodeRef previousRecord = recordableVersionService.getVersionRecord(previousVersion);
                if (previousRecord != null)
                {
                    versionRecord = previousRecord;
                    break;
                }
            }
        }

        return versionRecord;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#createNewRecord(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.namespace.QName, java.util.Map, org.alfresco.service.cmr.repository.ContentReader)
     */
    @Override
    public NodeRef createRecordFromContent(NodeRef parent, String name, QName type, Map<QName, Serializable> properties, ContentReader reader)
    {
        ParameterCheck.mandatory("nodeRef", parent);
        ParameterCheck.mandatory("name", name);

        NodeRef result = null;
        NodeRef destination = parent;

        if (isFilePlan(parent))
        {
            // get the unfiled record container for the file plan
            destination = filePlanService.getUnfiledContainer(parent);
            if (destination == null)
            {
                throw new AlfrescoRuntimeException("Unable to create record, because unfiled container could not be found.");
            }
        }

        // if none set the default record type is cm:content
        if (type == null)
        {
            type = ContentModel.TYPE_CONTENT;
        }
        else if (!dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT))
        {
            throw new AlfrescoRuntimeException("Record can only be created from a sub-type of cm:content.");
        }

        disablePropertyEditableCheck();
        try
        {
            // create the new record
            final NodeRef record = fileFolderService.create(destination, name, type).getNodeRef();

            // set the properties
            if (properties != null)
            {
                nodeService.addProperties(record, properties);
            }

            // set the content
            if (reader != null)
            {
                ContentWriter writer = fileFolderService.getWriter(record);
                writer.setEncoding(reader.getEncoding());
                writer.setMimetype(reader.getMimetype());
                writer.putContent(reader);
            }

            result = authenticationUtil.runAsSystem(new RunAsWork<NodeRef>()
            {
    			public NodeRef doWork() throws Exception
    			{
    				// Check if the "record" aspect has been applied already.
    		        // In case of filing a report the created node will be made
    		        // a record within the "onCreateChildAssociation" method if
    		        // a destination for the report has been selected.
    		        if (!nodeService.hasAspect(record, ASPECT_RECORD))
    		        {
    		            // make record
    		            makeRecord(record);
    		        }

    				return record;
    			}

            });
        }
        finally
        {
            enablePropertyEditableCheck();
        }

        return result;
    }

    /**
     * Creates a record from the given document
     *
     * @param document the document from which a record will be created
     */
    @Override
    public void makeRecord(NodeRef document)
    {
        ParameterCheck.mandatory("document", document);

        ruleService.disableRules();
        disablePropertyEditableCheck();
        try
        {
            // get the record id
            String recordId = identifierService.generateIdentifier(ASPECT_RECORD,
                                                                   nodeService.getPrimaryParent(document).getParentRef());

            // get the record name
            String name = (String)nodeService.getProperty(document, ContentModel.PROP_NAME);

            // rename the record
            int dotIndex = name.lastIndexOf('.');
            String prefix = name;
            String postfix = "";
            if (dotIndex != -1)
            {
                prefix = name.substring(0, dotIndex);
                postfix = name.substring(dotIndex);
            }
            String recordName = prefix + " (" + recordId + ")" + postfix;
            behaviourFilter.disableBehaviour();
            try
            {
            	fileFolderService.rename(document, recordName);
            }
            finally
            {
            	behaviourFilter.enableBehaviour();
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Rename " + name + " to " + recordName);
            }

            // add the record aspect
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
            props.put(PROP_IDENTIFIER, recordId);
            props.put(PROP_ORIGIONAL_NAME, name);
            nodeService.addAspect(document, RecordsManagementModel.ASPECT_RECORD, props);

            // remove versionable aspect(s)
            nodeService.removeAspect(document, RecordableVersionModel.ASPECT_VERSIONABLE);

            // remove the owner
            ownableService.setOwner(document, OwnableService.NO_OWNER);
        }
        catch (FileNotFoundException e)
        {
            throw new AlfrescoRuntimeException("Unable to make record, because rename failed.", e);
        }
        finally
        {
            ruleService.enableRules();
            enablePropertyEditableCheck();
        }

    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.disposableitem.RecordService#isFiled(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean isFiled(final NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        boolean result = false;

        if (isRecord(nodeRef))
        {
        	result = AuthenticationUtil.runAsSystem(new RunAsWork<Boolean>()
        	{
				public Boolean doWork() throws Exception
				{
		            return (null != nodeService.getProperty(nodeRef, PROP_DATE_FILED));
				}
        	});
        }

        return result;
    }

    /**
     * Helper method to 'file' a new document that arrived in the file plan structure.
     *
     *  TODO atm we only 'file' content as a record .. may need to consider other types if we
     *       are to support the notion of composite records.
     *
     * @param record node reference to record (or soon to be record!)
     */
    @Override
    public void file(NodeRef record)
    {
        ParameterCheck.mandatory("item", record);

        // we only support filling of content items
        // TODO composite record support needs to file containers too
        QName type = nodeService.getType(record);
        if (ContentModel.TYPE_CONTENT.equals(type)  ||
            dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT))
        {
            // fire before file record policy
            beforeFileRecord.get(getTypeAndApsects(record)).beforeFileRecord(record);

            // check whether this item is already an item or not
            if (!isRecord(record))
            {
                // make the item a record
                makeRecord(record);
            }

            // set filed date
            if (nodeService.getProperty(record, PROP_DATE_FILED) == null)
            {
                Calendar fileCalendar = Calendar.getInstance();
                nodeService.setProperty(record, PROP_DATE_FILED, fileCalendar.getTime());
            }

            // file on file record policy
            onFileRecord.get(getTypeAndApsects(record)).onFileRecord(record);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#rejectRecord(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
     */
    @Override
    public void rejectRecord(final NodeRef nodeRef, final String reason)
    {
        ParameterCheck.mandatory("NodeRef", nodeRef);
        ParameterCheck.mandatoryString("Reason", reason);

        // Save the id of the currently logged in user
        final String userId = AuthenticationUtil.getFullyAuthenticatedUser();

        // do the work of rejecting the record as the system user
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                ruleService.disableRules();
                try
                {
                    // get the latest version record, if there is one
                    NodeRef latestVersionRecord = getLatestVersionRecord(nodeRef);

                    if (latestVersionRecord != null)
                    {
                        relationshipService.removeRelationship(CUSTOM_REF_VERSIONS.getLocalName(), nodeRef, latestVersionRecord);
                    }

                    // get record property values
                    final Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
                    final String recordId = (String)properties.get(PROP_IDENTIFIER);
                    final String documentOwner = (String)properties.get(PROP_RECORD_ORIGINATING_USER_ID);
                    final String originalName = (String)properties.get(PROP_ORIGIONAL_NAME);
                    final NodeRef originatingLocation = (NodeRef)properties.get(PROP_RECORD_ORIGINATING_LOCATION);

                    // we can only reject if the originating location is present
                    if (originatingLocation != null)
                    {
                        // first remove the secondary link association
                        final List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(nodeRef);
                        for (ChildAssociationRef childAssociationRef : parentAssocs)
                        {
                            if (!childAssociationRef.isPrimary() && childAssociationRef.getParentRef().equals(originatingLocation))
                            {
                                nodeService.removeChildAssociation(childAssociationRef);
                                break;
                            }
                        }

                        removeRmAspectsFrom(nodeRef);

                        // get the records primary parent association
                        final ChildAssociationRef parentAssoc = nodeService.getPrimaryParent(nodeRef);

                        // move the record into the collaboration site
                        nodeService.moveNode(nodeRef, originatingLocation, ContentModel.ASSOC_CONTAINS, parentAssoc.getQName());

                        // rename to the original name
                        if (originalName != null)
                        {
                            fileFolderService.rename(nodeRef, originalName);

                            if (logger.isDebugEnabled())
                            {
                                String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                                logger.debug("Rename " + name + " to " + originalName);
                            }
                        }

                        // save the information about the rejection details
                        final Map<QName, Serializable> aspectProperties = new HashMap<>(3);
                        aspectProperties.put(PROP_RECORD_REJECTION_USER_ID, userId);
                        aspectProperties.put(PROP_RECORD_REJECTION_DATE, new Date());
                        aspectProperties.put(PROP_RECORD_REJECTION_REASON, reason);
                        nodeService.addAspect(nodeRef, ASPECT_RECORD_REJECTION_DETAILS, aspectProperties);

                        // Restore the owner of the document
                        if (StringUtils.isBlank(documentOwner))
                        {
                            throw new AlfrescoRuntimeException("Unable to find the creator of document.");
                        }
                        ownableService.setOwner(nodeRef, documentOwner);

                        // clear the existing permissions
                        permissionService.clearPermission(nodeRef, null);

                        // restore permission inheritance
                        permissionService.setInheritParentPermissions(nodeRef, true);

                        // send an email to the record creator
                        notificationHelper.recordRejectedEmailNotification(nodeRef, recordId, documentOwner);
                    }
                }
                finally
                {
                    ruleService.enableRules();
                }

                return null;
            }

            /** Removes all RM related aspects from the specified node and any rendition children. */
            private void removeRmAspectsFrom(NodeRef nodeRef)
            {
                // Note that when folder records are supported, we will need to recursively
                // remove aspects from their descendants.
                final Set<QName> aspects = nodeService.getAspects(nodeRef);
                for (QName aspect : aspects)
                {
                    if (RM_URI.equals(aspect.getNamespaceURI()) ||
                        RecordableVersionModel.RMV_URI.equals(aspect.getNamespaceURI()))
                    {
                        nodeService.removeAspect(nodeRef, aspect);
                    }
                }
                for (ChildAssociationRef renditionAssoc : renditionService.getRenditions(nodeRef))
                {
                    final NodeRef renditionNode = renditionAssoc.getChildRef();

                    // Do not attempt to clean up rendition nodes which are not children of their source node.
                    final boolean renditionRequiresCleaning = nodeService.exists(renditionNode) &&
                                                              renditionAssoc.isPrimary();

                    if (renditionRequiresCleaning)
                    {
                        removeRmAspectsFrom(renditionNode);
                    }
                }
            }
        });
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#isPropertyEditable(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    public boolean isPropertyEditable(NodeRef record, QName property)
    {
        ParameterCheck.mandatory("record", record);
        ParameterCheck.mandatory("property", property);

        if (!isRecord(record))
        {
            throw new AlfrescoRuntimeException("Can not check if the property " + property.toString() + " is editable, because node reference is not a record.");
        }

        NodeRef filePlan = getFilePlan(record);

        // DEBUG ...
        boolean debugEnabled = logger.isDebugEnabled();
        if (debugEnabled)
        {
            logger.debug("Checking whether property " + property.toString() + " is editable for user " + AuthenticationUtil.getRunAsUser());

            Set<Role> roles = filePlanRoleService.getRolesByUser(filePlan, AuthenticationUtil.getRunAsUser());

            logger.debug(" ... users roles");

            for (Role role : roles)
            {
                logger.debug("     ... user has role " + role.getName() + " with capabilities ");

                for (Capability cap : role.getCapabilities())
                {
                    logger.debug("         ... " + cap.getName());
                }
            }

            logger.debug(" ... user has the following set permissions on the file plan");

            Set<AccessPermission> perms = permissionService.getAllSetPermissions(filePlan);
            for (AccessPermission perm : perms)
            {
                if ((perm.getPermission().contains(RMPermissionModel.EDIT_NON_RECORD_METADATA) ||
                     perm.getPermission().contains(RMPermissionModel.EDIT_RECORD_METADATA)))
                {
                    logger.debug("     ... " + perm.getAuthority() + " - " + perm.getPermission() + " - " + perm.getAccessStatus().toString());
                }
            }

            if (permissionService.hasPermission(filePlan, RMPermissionModel.EDIT_NON_RECORD_METADATA).equals(AccessStatus.ALLOWED))
            {
                logger.debug(" ... user has the edit non record metadata permission on the file plan");
            }
        }
        // END DEBUG ...

        boolean result = alwaysEditProperty(property);
        if (result)
        {
            if (debugEnabled)
            {
                logger.debug(" ... property marked as always editable.");
            }
        }
        else
        {
            boolean allowRecordEdit = false;
            boolean allowNonRecordEdit = false;

            AccessStatus accessNonRecord = capabilityService.getCapabilityAccessState(record, RMPermissionModel.EDIT_NON_RECORD_METADATA);
            AccessStatus accessDeclaredRecord = capabilityService.getCapabilityAccessState(record, RMPermissionModel.EDIT_DECLARED_RECORD_METADATA);
            AccessStatus accessRecord = capabilityService.getCapabilityAccessState(record, RMPermissionModel.EDIT_RECORD_METADATA);

            if (AccessStatus.ALLOWED.equals(accessNonRecord))
            {
                if (debugEnabled)
                {
                    logger.debug(" ... user has edit nonrecord metadata capability");
                }

                allowNonRecordEdit = true;
            }

            if (AccessStatus.ALLOWED.equals(accessRecord)  ||
                AccessStatus.ALLOWED.equals(accessDeclaredRecord))
            {
                if (debugEnabled)
                {
                    logger.debug(" ... user has edit record or declared metadata capability");
                }

                allowRecordEdit = true;
            }

            if (allowNonRecordEdit && allowRecordEdit)
            {
                if (debugEnabled)
                {
                    logger.debug(" ... so all properties can be edited.");
                }

                result = true;
            }
            else if (allowNonRecordEdit && !allowRecordEdit)
            {
                // can only edit non record properties
                if (!isRecordMetadata(filePlan, property))
                {
                    if (debugEnabled)
                    {
                        logger.debug(" ... property is not considered record metadata so editable.");
                    }

                    result = true;
                }
                else
                {
                    if (debugEnabled)
                    {
                        logger.debug(" ... property is considered record metadata so not editable.");
                    }
                }
            }
            else if (!allowNonRecordEdit && allowRecordEdit)
            {
                // can only edit record properties
                if (isRecordMetadata(filePlan, property))
                {
                    if (debugEnabled)
                    {
                        logger.debug(" ... property is considered record metadata so editable.");
                    }

                    result = true;
                }
                else
                {
                    if (debugEnabled)
                    {
                        logger.debug(" ... property is not considered record metadata so not editable.");
                    }
                }
            }
            // otherwise we can't edit any properties so just return the empty set
        }
        return result;
    }

    /**
     * Helper method that indicates whether a property is considered record metadata or not.
     *
     * @param property  property
     * @return boolea   true if record metadata, false otherwise
     */
    private boolean isRecordMetadata(NodeRef filePlan, QName property)
    {
        boolean result = false;

        // grab the information about the properties parent type
        ClassDefinition parent = null;
        PropertyDefinition def = dictionaryService.getProperty(property);
        if (def != null)
        {
            parent = def.getContainerClass();
        }

        // non-electronic record is considered a special case
        // TODO move non-electronic record support to a separate model namespace
        if (parent != null && TYPE_NON_ELECTRONIC_DOCUMENT.equals(parent.getName()))
        {
            result = false;
        }
        else
        {
            // check the URI's
            result = RECORD_MODEL_URIS.contains(property.getNamespaceURI());

            // check the custom model
            if (!result && !ArrayUtils.contains(NON_RECORD_MODEL_URIS, property.getNamespaceURI()))
            {
                if (parent != null && parent.isAspect())
                {
                    result = getRecordMetadataAspects(filePlan).contains(parent.getName());
                }
            }
        }

        return result;
    }

    /**
     * Determines whether the property should always be allowed to be edited or not.
     *
     * @param property
     * @return
     */
    private boolean alwaysEditProperty(QName property)
    {
        return (getAlwaysEditURIs().contains(property.getNamespaceURI()) ||
                ArrayUtils.contains(ALWAYS_EDIT_PROPERTIES, property) ||
                isProtectedProperty(property));
    }

    /**
     * Helper method to determine whether a property is protected at a dictionary definition
     * level.
     *
     * @param property  property qualified name
     * @return booelan  true if protected, false otherwise
     */
    private boolean isProtectedProperty(QName property)
    {
        boolean result = false;
        PropertyDefinition def = dictionaryService.getProperty(property);
        if (def != null)
        {
            result = def.isProtected();
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#isMetadataStub(NodeRef)
     */
    @Override
    public boolean isMetadataStub(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        return nodeService.hasAspect(nodeRef, ASPECT_GHOSTED);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#getRecords(NodeRef)
     */
    @Override
    public List<NodeRef> getRecords(NodeRef recordFolder)
    {
        ParameterCheck.mandatory("recordFolder", recordFolder);

        List<NodeRef> result = new ArrayList<NodeRef>(1);
        if (recordFolderService.isRecordFolder(recordFolder))
        {
            List<ChildAssociationRef> assocs = nodeService.getChildAssocs(recordFolder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef assoc : assocs)
            {
                NodeRef child = assoc.getChildRef();
                if (isRecord(child))
                {
                    result.add(child);
                }
            }
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#addRecordType(NodeRef, QName)
     */
    @Override
    public void addRecordType(NodeRef nodeRef, QName typeQName)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("typeQName", typeQName);

        if (!nodeService.hasAspect(nodeRef, typeQName))
        {
            nodeService.addAspect(nodeRef, typeQName, null);
        }
        else
        {
            logger.info(I18NUtil.getMessage(MSG_NODE_HAS_ASPECT, nodeRef.toString(), typeQName.toString()));
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#link(NodeRef, NodeRef)
     */
    @Override
    public void link(NodeRef record, NodeRef recordFolder)
    {
        ParameterCheck.mandatory("record", record);
        ParameterCheck.mandatory("recordFolder", recordFolder);

        // ensure we are linking a record to a record folder
        if(isRecord(record) && isRecordFolder(recordFolder))
        {
            // ensure that we are not linking a record to an exisiting location
            List<ChildAssociationRef> parents = nodeService.getParentAssocs(record);
            for (ChildAssociationRef parent : parents)
            {
                if (parent.getParentRef().equals(recordFolder))
                {
                    // we can not link a record to the same location more than once
                    throw new RecordLinkRuntimeException("Can not link a record to the same record folder more than once");
                }
            }

            // validate link conditions
            validateLinkConditions(record, recordFolder);

            // get the current name of the record
            String name = nodeService.getProperty(record, ContentModel.PROP_NAME).toString();

            // create a secondary link to the record folder
            nodeService.addChild(
                recordFolder,
                record,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name));
        }
        else
        {
            // can only link a record to a record folder
            throw new RecordLinkRuntimeException("Can only link a record to a record folder.");
        }
    }

    /**
     *
     * @param record
     * @param recordFolder
     */
    private void validateLinkConditions(NodeRef record, NodeRef recordFolder)
    {
        // ensure that the linking record folders have compatible disposition schedules
        DispositionSchedule recordDispositionSchedule = dispositionService.getDispositionSchedule(record);
        if (recordDispositionSchedule != null)
        {
            DispositionSchedule recordFolderDispositionSchedule = dispositionService.getDispositionSchedule(recordFolder);
            if (recordFolderDispositionSchedule != null)
            {
                if (recordDispositionSchedule.isRecordLevelDisposition() != recordFolderDispositionSchedule.isRecordLevelDisposition())
                {
                    // we can't link a record to an incompatible disposition schedule
                    throw new RecordLinkRuntimeException("Can not link a record to a record folder with an incompatible disposition schedule.  "
                                                     + "They must either both be record level or record folder level dispositions.");
                }
            }
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#unlink(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void unlink(NodeRef record, NodeRef recordFolder)
    {
        ParameterCheck.mandatory("record", record);
        ParameterCheck.mandatory("recordFolder", recordFolder);

        // ensure we are unlinking a record from a record folder
        if(isRecord(record) && isRecordFolder(recordFolder))
        {
            // check that we are not trying to unlink the primary parent
            NodeRef primaryParent = nodeService.getPrimaryParent(record).getParentRef();
            if (primaryParent.equals(recordFolder))
            {
                throw new RecordLinkRuntimeException("Can't unlink a record from it's owning record folder.");
            }

            // remove the link
            nodeService.removeChild(recordFolder, record);
        }
        else
        {
            // can only unlink a record from a record folder
            throw new RecordLinkRuntimeException("Can only unlink a record from a record folder.");
        }
    }    
}
