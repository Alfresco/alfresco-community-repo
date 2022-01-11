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

package org.alfresco.module.org_alfresco_module_rm.record;

import static java.util.Arrays.asList;

import static org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model.DOD_URI;
import static org.alfresco.module.org_alfresco_module_rm.record.RecordUtils.appendIdentifierToName;
import static org.alfresco.module.org_alfresco_module_rm.record.RecordUtils.generateRecordIdentifier;
import static org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel.RMV_URI;
import static org.alfresco.repo.policy.Behaviour.NotificationFrequency.FIRST_EVENT;
import static org.alfresco.repo.policy.Behaviour.NotificationFrequency.TRANSACTION_COMMIT;
import static org.alfresco.repo.policy.annotation.BehaviourKind.ASSOCIATION;

import java.io.Serializable;
import java.text.MessageFormat;
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
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.BeforeRecordDeclaration;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.BeforeRecordRejection;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnFileRecord;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnRecordDeclaration;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementPolicies.OnRecordRejection;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.RMPermissionModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
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
import org.alfresco.module.org_alfresco_module_rm.util.PoliciesUtil;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionService;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.integrity.IncompleteNodeTagger;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.ClassPolicyDelegate;
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
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                                          NodeServicePolicies.OnAddAspectPolicy,
                                          NodeServicePolicies.OnCreateChildAssociationPolicy,
                                          NodeServicePolicies.OnRemoveAspectPolicy,
                                          NodeServicePolicies.OnUpdatePropertiesPolicy,
                                          ContentServicePolicies.OnContentUpdatePolicy
{
    /** Logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(RecordServiceImpl.class);

    /** Sync Model URI */
    private static final String SYNC_MODEL_1_0_URI = "http://www.alfresco.org/model/sync/1.0";

    /** Synced aspect */
    private static final QName ASPECT_SYNCED = QName.createQName(SYNC_MODEL_1_0_URI, "synced");

    /** transation data key */
    private static final String KEY_IGNORE_ON_UPDATE = "ignoreOnUpdate";
    public static final String KEY_NEW_RECORDS = "newRecords";

    /** I18N */
    private static final String MSG_NODE_HAS_ASPECT = "rm.service.node-has-aspect";
    private static final String FINAL_VERSION = "rm.service.final-version";
    private static final String FINAL_DESCRIPTION = "rm.service.final-version-description";
    private static final String MSG_UNDECLARED_ONLY_RECORDS = "rm.action.undeclared-only-records";
    private static final String MSG_NO_DECLARE_MAND_PROP = "rm.action.no-declare-mand-prop";
    private static final String MSG_CANNOT_CREATE_CHILDREN_IN_CLOSED_RECORD_FOLDER = "rm.service.add-children-to-closed-record-folder";

    /** Always edit property array */
    private static final QName[] ALWAYS_EDIT_PROPERTIES = new QName[]
    {
       ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA
    };

    /** always edit model URI's */
    private List<String> alwaysEditURIs;

    /**
     * check mandatory properties
     */
    private boolean checkMandatoryPropertiesEnabled = true;

    /**
     * @param alwaysEditURIs the alwaysEditURIs to set
     */
    public void setAlwaysEditURIs(List<String> alwaysEditURIs)
    {
        this.alwaysEditURIs = alwaysEditURIs;
    }

    /**
     * @return the alwaysEditURIs
     */
    protected List<String> getAlwaysEditURIs()
    {
        return this.alwaysEditURIs;
    }

    /** record model URI's */
    private  List<String> recordModelURIs;

    /**
     * @param recordModelURIs namespaces specific to records
     */
    public void setRecordModelURIs(List<String> recordModelURIs)
    {
        this.recordModelURIs = recordModelURIs;
    }

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

    public RecordableVersionService getRecordableVersionService()
    {
        return recordableVersionService;
    }

    /** recordable version service */
    private RecordableVersionService recordableVersionService;

    /** list of available record meta-data aspects and the file plan types the are applicable to */
    private Map<QName, Set<QName>> recordMetaDataAspects;

    /** Freeze service */
    private FreezeService freezeService;

    /** Namespace service */
    private NamespaceService namespaceService;

    /** policies */
    private ClassPolicyDelegate<BeforeFileRecord> beforeFileRecord;
    private ClassPolicyDelegate<OnFileRecord> onFileRecord;
    private ClassPolicyDelegate<BeforeRecordDeclaration> beforeRecordDeclarationDelegate;
    private ClassPolicyDelegate<OnRecordDeclaration> onRecordDeclarationDelegate;
    private ClassPolicyDelegate<BeforeRecordRejection> beforeRecordRejectionDelegate;
    private ClassPolicyDelegate<OnRecordRejection> onRecordRejectionDelegate;

    private IncompleteNodeTagger incompleteNodeTagger;

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

    public void setIncompleteNodeTagger(IncompleteNodeTagger incompleteNodeTagger)
    {
        this.incompleteNodeTagger = incompleteNodeTagger;
    }

    /**
     * @param freezeService freeze service
     */
    public void setFreezeService(FreezeService freezeService)
    {
        this.freezeService = freezeService;
    }

    /**
     * @param namespaceService namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * @param checkMandatoryPropertiesEnabled true if check mandatory properties is enabled, false otherwise
     */
    public void setCheckMandatoryPropertiesEnabled(boolean checkMandatoryPropertiesEnabled)
    {
        this.checkMandatoryPropertiesEnabled = checkMandatoryPropertiesEnabled;
    }

    /**
     * Init method
     */
    public void init()
    {
        // bind policies
        beforeFileRecord = policyComponent.registerClassPolicy(BeforeFileRecord.class);
        onFileRecord = policyComponent.registerClassPolicy(OnFileRecord.class);
        beforeRecordDeclarationDelegate = policyComponent.registerClassPolicy(BeforeRecordDeclaration.class);
        onRecordDeclarationDelegate = policyComponent.registerClassPolicy(OnRecordDeclaration.class);
        beforeRecordRejectionDelegate = policyComponent.registerClassPolicy(BeforeRecordRejection.class);
        onRecordRejectionDelegate = policyComponent.registerClassPolicy(OnRecordRejection.class);
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy#onAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            type = "rma:record",
            notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onAddAspect(NodeRef nodeRef, QName aspect)
    {
        authenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                if (nodeService.exists(nodeRef) && nodeService.hasAspect(nodeRef, ASPECT_RECORD))
                {
                    generateRecordIdentifier(nodeService, identifierService, nodeRef);
                    reevaluateIncompleteTag(nodeRef);
                }
                return null;
            }
        });
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
            if (ContentData.hasContent(contentData) && contentData.getSize() > 0)
            {
                appendIdentifierToName(nodeService, nodeRef);
                reevaluateIncompleteTag(nodeRef);
            }
        }
    }

    /**
     * Behaviour executed when a new item is added to a record folder.
     *
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateChildAssociationPolicy#onCreateChildAssociation(org.alfresco.service.cmr.repository.ChildAssociationRef, boolean)
     */
    @Override
    @Behaviour
    (
       kind = ASSOCIATION,
       type = "rma:recordFolder",
       notificationFrequency = FIRST_EVENT
    )
    public void onCreateChildAssociation(final ChildAssociationRef childAssocRef, final boolean bNew)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                try
                {
                    NodeRef nodeRef = childAssocRef.getChildRef();
                    if (nodeService.exists(nodeRef)   &&
                        !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_TEMPORARY) &&
                        !nodeService.getType(nodeRef).equals(TYPE_RECORD_FOLDER) &&
                        !nodeService.getType(nodeRef).equals(TYPE_RECORD_CATEGORY))
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

                        //create and file the content as a record
                        file(nodeRef);
                        // recalculate disposition schedule for the record when linking it
                        dispositionService.recalculateNextDispositionStep(nodeRef);
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
                    LOGGER.warn("Unable to file pending record.", e);
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
     * @return  {@link Map}&lt;{@link QName}, {@link Set}&lt;{@link QName} &gt;&gt;  map containing record metadata aspects
     *
     * @since 2.2
     */
    protected Map<QName, Set<QName>> getRecordMetadataAspectsMap()
    {
        if (recordMetaDataAspects == null)
        {
            // create map
            recordMetaDataAspects = new HashMap<>();

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
            filePlanTypes = new HashSet<>(1);
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
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#getRecordMetadataAspects(NodeRef)
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
        Set<QName> result = new HashSet<>(getRecordMetadataAspectsMap().size());

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
        createRecord(filePlan, nodeRef, null, isLinked);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#createRecord(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void createRecord(final NodeRef filePlan, final NodeRef nodeRef, final NodeRef destinationNodeRef)
    {
        createRecord(filePlan, nodeRef, destinationNodeRef, true);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#createRecord(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override
    public void createRecord(final NodeRef filePlan, final NodeRef nodeRef, final NodeRef destinationNodeRef, final boolean isLinked)
    {
        // filePlan can be null. In this case the default RM site will be used.
        // destinationNodeRef can be null. In this case the unfiled record container will be used
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("isLinked", isLinked);

        recordCreationSanityCheckOnNode(nodeRef);
        final NodeRef newRecordContainer = recordCreationSanityCheckOnDestinationNode(destinationNodeRef, filePlan);

        invokeBeforeRecordDeclaration(nodeRef);
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
                        Map<QName, Serializable> aspectProperties = new HashMap<>(3);
                        aspectProperties.put(PROP_RECORD_ORIGINATING_LOCATION, parentAssoc.getParentRef());
                        aspectProperties.put(PROP_RECORD_ORIGINATING_USER_ID, owner);
                        aspectProperties.put(PROP_RECORD_ORIGINATING_CREATION_DATE, new Date());
                        nodeService.addAspect(nodeRef, ASPECT_RECORD_ORIGINATING_DETAILS, aspectProperties);

                        // make the document a record
                        makeRecord(nodeRef);
                        generateRecordIdentifier(nodeService, identifierService, nodeRef);

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
        invokeOnRecordDeclaration(nodeRef);
    }

    /**
     * Helper method to check the given file plan before trying to determine the unfiled records container.
     *
     * @param filePlan The reference of the file plan node
     */
    private NodeRef recordCreationSanityCheckOnFilePlan(NodeRef filePlan)
    {
        NodeRef result = null;

        if (filePlan == null)
        {
            // TODO .. eventually make the file plan parameter required

            result = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
            {
                @Override
                public NodeRef doWork()
                {
                    return filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
                }
            }, AuthenticationUtil.getAdminUserName());

            // if the file plan is still null, raise an exception
            if (result == null)
            {
                String msg = "Cannot create record, because the default file plan cannot be determined. Make sure at least one file plan has been created.";
                LOGGER.debug(msg);
                throw new RecordCreationException(msg);
            }
        }
        else
        {
            // verify that the provided file plan is actually a file plan
            if (!filePlanService.isFilePlan(filePlan))
            {
                String msg = "Cannot create record, because the provided file plan node reference is not a file plan.";
                LOGGER.debug(msg);
                throw new RecordCreationException(msg);
            }

            result = filePlan;
        }

        return result;
    }

    /**
     * Helper method to check the given destination before trying to declare a record in it.
     *
     * @param destinationNodeRef The reference of the container in which the record will be created
     * @param filePlan           The reference of the file plan node
     */
    private NodeRef recordCreationSanityCheckOnDestinationNode(NodeRef destinationNodeRef, final NodeRef filePlan)
    {
        final NodeRef checkedFilePlan = recordCreationSanityCheckOnFilePlan(filePlan);
        NodeRef newRecordContainer = destinationNodeRef;
        // if optional location not specified, use the unfiledContainer
        if (newRecordContainer == null)
        {
            // get the unfiled record container node for the file plan
            newRecordContainer = AuthenticationUtil.runAsSystem(() -> filePlanService.getUnfiledContainer(checkedFilePlan));

            if (newRecordContainer == null)
            {
                throw new AlfrescoRuntimeException("Unable to create record, because record container could not be found.");
            }
        }
        // if optional location supplied, check that it is a valid record folder, unfiled record container or folder
        else
        {
            final QName nodeType = nodeService.getType(newRecordContainer);
            if (!(nodeType.equals(RecordsManagementModel.TYPE_RECORD_FOLDER) ||
                    nodeType.equals(RecordsManagementModel.TYPE_UNFILED_RECORD_FOLDER) ||
                    nodeType.equals(RecordsManagementModel.TYPE_UNFILED_RECORD_CONTAINER)))
            {
                throw new AlfrescoRuntimeException("Unable to create record, because container is not a valid type for new record.");
            }

            Boolean isClosed = (Boolean) nodeService.getProperty(newRecordContainer, PROP_IS_CLOSED);
            if (isClosed != null && isClosed)
            {
                throw new IntegrityException(I18NUtil.getMessage(MSG_CANNOT_CREATE_CHILDREN_IN_CLOSED_RECORD_FOLDER), null);
            }

            if (extendedPermissionService.hasPermission(newRecordContainer, RMPermissionModel.FILING) == AccessStatus.DENIED)
            {
                throw new AccessDeniedException(I18NUtil.getMessage("permissions.err_access_denied"));
            }

            if (freezeService.isFrozen(newRecordContainer))
            {
                throw new IntegrityException(I18NUtil.getMessage("rm.service.add-children-to-frozen-record-folder"),null);
            }
        }

        return newRecordContainer;
    }

    /**
     * Helper method to check the given node before trying to declare it as record
     *
     * @param nodeRef The reference of the node which will be declared as record
     */
    private void recordCreationSanityCheckOnNode(NodeRef nodeRef)
    {
        // first we do a sanity check to ensure that the user has at least write permissions on the document
        if (extendedPermissionService.hasPermission(nodeRef, PermissionService.WRITE) != AccessStatus.ALLOWED)
        {
            String msg = "Cannot create record from document, because the user " +
                    AuthenticationUtil.getRunAsUser() +
                    " does not have Write permissions on the doucment " +
                    nodeRef.toString();
            LOGGER.debug(msg);
            throw new AccessDeniedException(msg);
        }

        // do not create record if the node does not exist!
        if (!nodeService.exists(nodeRef))
        {
            String msg = "Cannot create record, because " + nodeRef.toString() + " does not exist.";
            LOGGER.debug(msg);
            throw new RecordCreationException(msg);
        }

        // TODO eventually we should support other types .. either as record folders or as composite records
        if (!dictionaryService.isSubClass(nodeService.getType(nodeRef), ContentModel.TYPE_CONTENT))
        {
            String msg = "Cannot create record, because " + nodeRef.toString() + " is not a supported type.";
            LOGGER.debug(msg);
            throw new RecordCreationException(msg);
        }

        // Do not create record if the node is already a record!
        if (nodeService.hasAspect(nodeRef, ASPECT_RECORD))
        {
            String msg = "Cannot create record, because " + nodeRef.toString() + " is already a record.";
            LOGGER.debug(msg);
            throw new RecordCreationException(msg);
        }

        // We cannot create records from working copies
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            String msg = "Can node create record, because " + nodeRef.toString() + " is a working copy.";
            LOGGER.debug(msg);
            throw new RecordCreationException(msg);
        }

        // Cannot create a record from a previously rejected one
        if (nodeService.hasAspect(nodeRef, ASPECT_RECORD_REJECTION_DETAILS))
        {
            String msg = "Cannot create record, because " + nodeRef.toString() + " has previously been rejected.";
            LOGGER.debug(msg);
            throw new RecordCreationException(msg);
        }

        // can't declare the record if the node is sync'ed
        if (nodeService.hasAspect(nodeRef, ASPECT_SYNCED))
        {
            String msg = "Can't declare as record, because " + nodeRef.toString() + " is synched content.";
            LOGGER.debug(msg);
            throw new RecordCreationException(msg);
        }
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
                    generateRecordIdentifier(nodeService, identifierService, record);

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
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#createRecordFromContent(org.alfresco.service.cmr.repository.NodeRef, java.lang.String, org.alfresco.service.namespace.QName, java.util.Map, org.alfresco.service.cmr.repository.ContentReader)
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
                        generateRecordIdentifier(nodeService, identifierService, record);
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
            authenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    nodeService.addAspect(document, RecordsManagementModel.ASPECT_RECORD, null);

                    // remove versionable aspect(s)
                    nodeService.removeAspect(document, RecordableVersionModel.ASPECT_VERSIONABLE);

                    // remove the owner
                    ownableService.setOwner(document, OwnableService.NO_OWNER);

                    return null;
                }
            });
        }
        finally
        {
            ruleService.enableRules();
            enablePropertyEditableCheck();
        }

    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#isFiled(org.alfresco.service.cmr.repository.NodeRef)
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

        // invoke policy
        invokeBeforeRecordRejection(nodeRef);

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
                            if (!childAssociationRef.isPrimary() &&
                                    (childAssociationRef.getParentRef().equals(originatingLocation) ||
                                            nodeService.getType(childAssociationRef.getParentRef()).equals(TYPE_RECORD_FOLDER)))
                            {
                                nodeService.removeChildAssociation(childAssociationRef);
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

                            String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                            LOGGER.debug("Rename {} to {}", name, originalName);
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
                final List<String> rmURIs = asList(RM_URI, DOD_URI, RM_CUSTOM_URI, RMV_URI);
                for (QName aspect : aspects)
                {
                    if (rmURIs.contains(aspect.getNamespaceURI()))
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

        // invoke policy
        invokeOnRecordRejection(nodeRef);
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
            throw new AlfrescoRuntimeException("Cannot check if the property " + property.toString() + " is editable, because node reference is not a record.");
        }

        NodeRef filePlan = getFilePlan(record);

        // DEBUG ...
        boolean debugEnabled = LOGGER.isDebugEnabled();
        if (debugEnabled)
        {
            LOGGER.debug("Checking whether property " + property.toString() + " is editable for user " + AuthenticationUtil.getRunAsUser());

            Set<Role> roles = filePlanRoleService.getRolesByUser(filePlan, AuthenticationUtil.getRunAsUser());

            LOGGER.debug(" ... users roles");

            for (Role role : roles)
            {
                LOGGER.debug("     ... user has role " + role.getName() + " with capabilities ");

                for (Capability cap : role.getCapabilities())
                {
                    LOGGER.debug("         ... " + cap.getName());
                }
            }

            LOGGER.debug(" ... user has the following set permissions on the file plan");

            Set<AccessPermission> perms = permissionService.getAllSetPermissions(filePlan);
            for (AccessPermission perm : perms)
            {
                if ((perm.getPermission().contains(RMPermissionModel.EDIT_NON_RECORD_METADATA) ||
                     perm.getPermission().contains(RMPermissionModel.EDIT_RECORD_METADATA)))
                {
                    LOGGER.debug("     ... " + perm.getAuthority() + " - " + perm.getPermission() + " - " + perm.getAccessStatus().toString());
                }
            }

            if (permissionService.hasPermission(filePlan, RMPermissionModel.EDIT_NON_RECORD_METADATA).equals(AccessStatus.ALLOWED))
            {
                LOGGER.debug(" ... user has the edit non record metadata permission on the file plan");
            }
        }
        // END DEBUG ...

        boolean result = alwaysEditProperty(property);
        if (result)
        {
            LOGGER.debug(" ... property marked as always editable.");
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
                LOGGER.debug(" ... user has edit nonrecord metadata capability");
                allowNonRecordEdit = true;
            }

            if (AccessStatus.ALLOWED.equals(accessRecord)  ||
                AccessStatus.ALLOWED.equals(accessDeclaredRecord))
            {
                LOGGER.debug(" ... user has edit record or declared metadata capability");
                allowRecordEdit = true;
            }

            if (allowNonRecordEdit && allowRecordEdit)
            {
                LOGGER.debug(" ... so all properties can be edited.");
                result = true;
            }
            else if (allowNonRecordEdit && !allowRecordEdit)
            {
                // can only edit non record properties
                if (!isRecordMetadata(filePlan, property))
                {
                    LOGGER.debug(" ... property is not considered record metadata so editable.");
                    result = true;
                }
                else
                {
                    LOGGER.debug(" ... property is considered record metadata so not editable.");
                }
            }
            else if (!allowNonRecordEdit && allowRecordEdit)
            {
                // can only edit record properties
                if (isRecordMetadata(filePlan, property))
                {
                    LOGGER.debug(" ... property is considered record metadata so editable.");
                    result = true;
                }
                else
                {
                    LOGGER.debug(" ... property is not considered record metadata so not editable.");
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
            result = recordModelURIs.contains(property.getNamespaceURI());

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

        List<NodeRef> result = new ArrayList<>(1);
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
            LOGGER.info(I18NUtil.getMessage(MSG_NODE_HAS_ASPECT, nodeRef.toString(), typeQName.toString()));
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
            // ensure that we are not linking a record to an existing location
            List<ChildAssociationRef> parents = nodeService.getParentAssocs(record);
            for (ChildAssociationRef parent : parents)
            {
                if (parent.getParentRef().equals(recordFolder))
                {
                    // we cannot link a record to the same location more than once
                    throw new RecordLinkRuntimeException("Cannot link a record to the same record folder more than once");
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

            // recalculate disposition schedule for the record when linking it
            dispositionService.recalculateNextDispositionStep(record);
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

        // get the origin disposition schedule for the record, not the calculated one
        DispositionSchedule recordDispositionSchedule = dispositionService.getOriginDispositionSchedule(record);

        if (recordDispositionSchedule != null)
        {
            DispositionSchedule recordFolderDispositionSchedule = dispositionService.getDispositionSchedule(recordFolder);
            if (recordFolderDispositionSchedule != null)
            {
                if (recordDispositionSchedule.isRecordLevelDisposition() != recordFolderDispositionSchedule.isRecordLevelDisposition())
                {
                    // we can't link a record to an incompatible disposition schedule
                    throw new RecordLinkRuntimeException("Cannot link a record to a record folder with an incompatible retention schedule.  "
                                                     + "They must either both be record level or record folder level retentions.");
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

            // recalculate disposition schedule for record after unlinking it
            dispositionService.recalculateNextDispositionStep(record);
        }
        else
        {
            // can only unlink a record from a record folder
            throw new RecordLinkRuntimeException("Can only unlink a record from a record folder.");
        }
    }

    /*
     * @see org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy#onContentUpdate(org.alfresco.service.cmr.repository.NodeRef, boolean)
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS,
            type = "rma:record",
            notificationFrequency = TRANSACTION_COMMIT
    )
    public void onContentUpdate(NodeRef nodeRef, boolean newContent)
    {
        if (nodeService.exists(nodeRef) && !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_HIDDEN) && !nodeService.hasAspect(nodeRef, ContentModel.ASPECT_LOCKABLE))
        {
            generateRecordIdentifier(nodeService, identifierService, nodeRef);
            reevaluateIncompleteTag(nodeRef);
        }
    }

    /**
     * Invoke invokeBeforeRecordDeclaration policy
     *
     * @param nodeRef       node reference
     */
    protected void invokeBeforeRecordDeclaration(NodeRef nodeRef)
    {
        // get qnames to invoke against
        Set<QName> qnames = PoliciesUtil.getTypeAndAspectQNames(nodeService, nodeRef);
        // execute policy for node type and aspects
        BeforeRecordDeclaration policy = beforeRecordDeclarationDelegate.get(qnames);
        policy.beforeRecordDeclaration(nodeRef);
    }

    /**
     * Invoke invokeOnRecordDeclaration policy
     *
     * @param nodeRef       node reference
     */
    protected void invokeOnRecordDeclaration(NodeRef nodeRef)
    {
        // get qnames to invoke against
        Set<QName> qnames = PoliciesUtil.getTypeAndAspectQNames(nodeService, nodeRef);
        // execute policy for node type and aspects
        OnRecordDeclaration policy = onRecordDeclarationDelegate.get(qnames);
        policy.onRecordDeclaration(nodeRef);
    }

    /**
     * Invoke invokeBeforeRecordRejection policy
     *
     * @param nodeRef       node reference
     */
    protected void invokeBeforeRecordRejection(NodeRef nodeRef)
    {
        // get qnames to invoke against
        Set<QName> qnames = PoliciesUtil.getTypeAndAspectQNames(nodeService, nodeRef);
        // execute policy for node type and aspects
        BeforeRecordRejection policy = beforeRecordRejectionDelegate.get(qnames);
        policy.beforeRecordRejection(nodeRef);
    }

    /**
     * Invoke invokeOnRecordRejection policy
     *
     * @param nodeRef       node reference
     */
    protected void invokeOnRecordRejection(NodeRef nodeRef)
    {
        // get qnames to invoke against
        Set<QName> qnames = PoliciesUtil.getTypeAndAspectQNames(nodeService, nodeRef);
        // execute policy for node type and aspects
        OnRecordRejection policy = onRecordRejectionDelegate.get(qnames);
        policy.onRecordRejection(nodeRef);
    }

    /**
     * RM-5244 - workaround to make sure the incomplete aspect is removed
     *
     * @param nodeRef the node to reevaluate for
     */
    private void reevaluateIncompleteTag(NodeRef nodeRef)
    {
        /*
         * Check if the node has the aspect because the reevaluation is expensive.
         * If the node doesn't have the aspect it means IncompleteNodeTagger didn't load before TransactionBehaviourQueue
         * and we don't need to reevaluate.
         */
        if(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_INCOMPLETE))
        {
            incompleteNodeTagger.beforeCommit(false);
        }
    }

    /**
     * Completes a record
     *
     * @param nodeRef Record node reference
     */
    @Override
    public void complete(NodeRef nodeRef)
    {
        validateForCompletion(nodeRef);
        disablePropertyEditableCheck();
        try
        {
            // Add the declared aspect
            Map<QName, Serializable> declaredProps = new HashMap<>(2);
            declaredProps.put(PROP_DECLARED_AT, new Date());
            declaredProps.put(PROP_DECLARED_BY, AuthenticationUtil.getRunAsUser());
            nodeService.addAspect(nodeRef, ASPECT_DECLARED_RECORD, declaredProps);

            AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork()
                {
                    // remove all owner related rights
                    ownableService.setOwner(nodeRef, OwnableService.NO_OWNER);
                    return null;
                }
            });
        }
        finally
        {
            enablePropertyEditableCheck();
        }
    }

    /**
     * Helper method to validate whether the node is in a state suitable for completion
     *
     * @param nodeRef node reference
     * @throws Exception if node not valid for completion
     */
    private void validateForCompletion(NodeRef nodeRef) {
        if (!nodeService.exists(nodeRef))
        {
            LOGGER.warn(I18NUtil.getMessage(MSG_UNDECLARED_ONLY_RECORDS, nodeRef.toString()));
            throw new IntegrityException("The record does not exist.", null);
        }

        if (!isRecord(nodeRef))
        {
            LOGGER.warn(I18NUtil.getMessage(MSG_UNDECLARED_ONLY_RECORDS, nodeRef.toString()));
            throw new IntegrityException("The node is not a record.", null);
        }

        if (freezeService.isFrozen(nodeRef))
        {
            LOGGER.warn(I18NUtil.getMessage(MSG_UNDECLARED_ONLY_RECORDS, nodeRef.toString()));
            throw new IntegrityException("The record is frozen.", null);
        }

        if (isDeclared(nodeRef))
        {
            throw new IntegrityException("The record is already completed.", null);
        }

        // if the record is newly created make sure the record identifier is set before completing the record
        Set<NodeRef> newRecords = transactionalResourceHelper.getSet(RecordServiceImpl.KEY_NEW_RECORDS);
        if (newRecords.contains(nodeRef))
        {
            generateRecordIdentifier(nodeService, identifierService, nodeRef);
        }

        // Validate that all mandatory properties, if any, are present
        List<String> missingProperties = new ArrayList<>(5);
        // Aspect not already defined - check mandatory properties then add
        if (checkMandatoryPropertiesEnabled)
        {
            Map<QName, Serializable> nodeRefProps = nodeService.getProperties(nodeRef);
            QName nodeRefType = nodeService.getType(nodeRef);

            // check for missing mandatory metadata from type definitions
            TypeDefinition typeDef = dictionaryService.getType(nodeRefType);
            checkDefinitionMandatoryPropsSet(typeDef, nodeRefProps, missingProperties);

            // check for missing mandatory metadata from aspect definitions
            Set<QName> aspects = nodeService.getAspects(nodeRef);
            for (QName aspect : aspects)
            {
                AspectDefinition aspectDef = dictionaryService.getAspect(aspect);
                checkDefinitionMandatoryPropsSet(aspectDef, nodeRefProps, missingProperties);
            }

            // check for missing mandatory metadata from custom aspect definitions
            QName customAspect = getCustomAspectImpl(nodeRefType);
            AspectDefinition aspectDef = dictionaryService.getAspect(customAspect);
            checkDefinitionMandatoryPropsSet(aspectDef, nodeRefProps, missingProperties);

            if (!missingProperties.isEmpty())
            {
                LOGGER.debug(buildMissingPropertiesErrorString(missingProperties));
                throw new RecordMissingMetadataException("The record has missing mandatory properties.");
            }
        }
    }

    /**
     * Helper method to build single string containing list of missing properties
     *
     * @param missingProperties list of missing properties
     * @return String of missing properties
     */
    private String buildMissingPropertiesErrorString(List<String> missingProperties)
    {
        StringBuilder builder = new StringBuilder(255);
        builder.append(I18NUtil.getMessage(MSG_NO_DECLARE_MAND_PROP));
        builder.append("  ");
        for (String missingProperty : missingProperties)
        {
            builder.append(missingProperty).append(", ");
        }
        return builder.toString();
    }

    /**
     * Helper method to check whether all the definition mandatory properties of the node have been set
     *
     * @param classDef          the ClassDefinition defining the properties to be checked
     * @param nodeRefProps      the properties of the node to be checked
     * @param missingProperties the list of mandatory properties found to be missing (currently only the first one)
     * @return boolean true if all mandatory properties are set, false otherwise
     */
    private void checkDefinitionMandatoryPropsSet(final ClassDefinition classDef, final Map<QName, Serializable> nodeRefProps,
                                                  final List<String> missingProperties)
    {
        for (PropertyDefinition propDef : classDef.getProperties().values())
        {
            if (propDef.isMandatory() && nodeRefProps.get(propDef.getName()) == null)
            {
                if (LOGGER.isWarnEnabled())
                {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Mandatory property missing: ").append(propDef.getName());
                    LOGGER.warn(msg.toString());
                }
                missingProperties.add(propDef.getName().toString());
            }
        }
    }

    /**
     * Helper method to get the custom aspect for a given nodeRef type
     *
     * @param nodeRefType the node type for which to return custom aspect QName
     * @return QName    custom aspect
     */
    private QName getCustomAspectImpl(QName nodeRefType)
    {
        QName aspect = ASPECT_RECORD;
        if (nodeRefType.equals(TYPE_NON_ELECTRONIC_DOCUMENT))
        {
            aspect = TYPE_NON_ELECTRONIC_DOCUMENT;
        }

        // get customAspectImpl
        String localName = aspect.toPrefixString(namespaceService).replace(":", "");
        localName = MessageFormat.format("{0}CustomProperties", localName);
        return QName.createQName(RM_CUSTOM_URI, localName);
    }
}
