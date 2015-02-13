/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.version;

import static org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel.CUSTOM_REF_VERSIONS;
import static org.codehaus.plexus.util.StringUtils.isNotBlank;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.rma.type.CmObjectType;
import org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.relationship.RelationshipService;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.Version2ServiceImpl;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.ReservedVersionNameException;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Recordable version service implementation
 *
 * @author Roy Wetherall
 * @since 2.3
 */
public class RecordableVersionServiceImpl extends    Version2ServiceImpl
                                          implements RecordableVersionModel
{
    /** share logger with version2ServiceImpl */
    private static Log logger = LogFactory.getLog(Version2ServiceImpl.class);

    /** key used to indicate a recordable version */
    public static final String KEY_RECORDABLE_VERSION = "recordable-version";
    public static final String KEY_FILE_PLAN = "file-plan";

    /** version record property */
    public static final String PROP_VERSION_RECORD = "RecordVersion";

    /** file plan service */
    private FilePlanService filePlanService;

    /** authentication util helper */
    private AuthenticationUtil authenticationUtil;

    /** relationship service */
    private RelationshipService relationshipService;

    /** record service */
    private RecordService recordService;

    /** model security service */
    private ModelSecurityService modelSecurityService;

    /** cm object type */
    private CmObjectType cmObjectType;

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param authenticationUtil    authentication util helper
     */
    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil)
    {
        this.authenticationUtil = authenticationUtil;
    }

    /**
     * @param relationshipService   relationship service
     */
    public void setRelationshipService(RelationshipService relationshipService)
    {
        this.relationshipService = relationshipService;
    }

    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @param modelSecurityService  model security service
     */
    public void setModelSecurityService(ModelSecurityService modelSecurityService)
    {
        this.modelSecurityService = modelSecurityService;
    }

    /**
     * @param cmObjectType the cmObjectType to set
     */
    public void setCmObjectType(CmObjectType cmObjectType)
    {
        this.cmObjectType = cmObjectType;
    }

    /**
     * @see org.alfresco.repo.version.Version2ServiceImpl#createVersion(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, int)
     */
    @Override
    protected Version createVersion(NodeRef nodeRef, Map<String, Serializable> origVersionProperties, int versionNumber) throws ReservedVersionNameException
    {
        // TODO we only support recorded versions for sub types of cm:content

        // create version properties if null
        if (origVersionProperties == null)
        {
            origVersionProperties = new HashMap<String, Serializable>(2);
        }

        // only need to check the recordable version policy when the recordable version indicator is missing from the version properties
        if (!origVersionProperties.containsKey(KEY_RECORDABLE_VERSION))
        {
            // get the version type
            VersionType versionType = null;
            if (origVersionProperties != null)
            {
                versionType = (VersionType)origVersionProperties.get(VersionModel.PROP_VERSION_TYPE);
            }

            // determine whether this is a recorded version or not
            if (isCreateRecordedVersion(nodeRef, versionType))
            {
                origVersionProperties.put(KEY_RECORDABLE_VERSION, true);
                if (!origVersionProperties.containsKey(KEY_FILE_PLAN))
                {
                    // make sure the file plan is set to the default if not specified
                    origVersionProperties.put(KEY_FILE_PLAN, getFilePlan(nodeRef));
                }
            }
        }
        else
        {
            if (!origVersionProperties.containsKey(KEY_FILE_PLAN))
            {
                // make sure the file plan is set to the default if not specified
                origVersionProperties.put(KEY_FILE_PLAN, getFilePlan(nodeRef));
            }
        }

        return super.createVersion(nodeRef, origVersionProperties, versionNumber);
    }

    /**
     * @param nodeRef           node reference
     * @return {@link NodeRef}  associated file plan, default if none
     */
    private NodeRef getFilePlan(NodeRef nodeRef)
    {
        NodeRef filePlan = (NodeRef)nodeService.getProperty(nodeRef, PROP_FILE_PLAN);
        if (filePlan == null)
        {
            filePlan = getFilePlan();
        }
        return filePlan;
    }

    /**
     * @return {@link NodeRef}  default file plan, exception if none
     */
    private NodeRef getFilePlan()
    {
        return authenticationUtil.runAsSystem(new RunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork() throws Exception
            {
                NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
                if (filePlan == null)
                {
                    throw new AlfrescoRuntimeException("Can't create a recorded version, because there is no file plan.");
                }
                return filePlan;
            }
        });
    }

    /**
     * Determine whether this is a recorded version or not.
     *
     * @param nodeRef
     * @return
     */
    private boolean isCreateRecordedVersion(NodeRef nodeRef, VersionType versionType)
    {
        boolean result = false;
        if (nodeService.hasAspect(nodeRef, RecordableVersionModel.ASPECT_VERSIONABLE))
        {
            String policyString = (String)nodeService.getProperty(nodeRef, PROP_RECORDABLE_VERSION_POLICY);
            if (policyString != null)
            {
                RecordableVersionPolicy policy = RecordableVersionPolicy.valueOf(policyString.toUpperCase());
                if (RecordableVersionPolicy.ALL.equals(policy) ||
                    (RecordableVersionPolicy.MAJOR_ONLY.equals(policy) &&
                     VersionType.MAJOR.equals(versionType)))
                {
                    result = true;
                }
            }
        }

        return result;
    }

    /**
     * @see org.alfresco.repo.version.Version2ServiceImpl#createNewVersion(org.alfresco.service.namespace.QName, org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map, int, org.alfresco.repo.policy.PolicyScope)
     */
    @Override
    protected NodeRef createNewVersion( QName sourceTypeRef,
                                        NodeRef versionHistoryRef,
                                        Map<QName, Serializable> standardVersionProperties,
                                        Map<String, Serializable> versionProperties,
                                        int versionNumber,
                                        PolicyScope nodeDetails)
    {
        NodeRef version = null;

        if (versionProperties.containsKey(KEY_RECORDABLE_VERSION) &&
            ((Boolean)versionProperties.get(KEY_RECORDABLE_VERSION)).booleanValue())
        {
            // create a recorded version
            version = createNewRecordedVersion(sourceTypeRef, versionHistoryRef, standardVersionProperties, versionProperties, versionNumber, nodeDetails);
        }
        else
        {
            // create a normal version
            version = super.createNewVersion(sourceTypeRef, versionHistoryRef, standardVersionProperties, versionProperties, versionNumber, nodeDetails);
        }

        return version;
    }

    /**
     * Creates a new recorded version
     *
     * @param sourceTypeRef                 source type name
     * @param versionHistoryRef             version history reference
     * @param standardVersionProperties     standard version properties
     * @param versionProperties             version properties
     * @param versionNumber                 version number
     * @param nodeDetails                   policy scope
     * @return {@link NodeRef}              record version
     */
    protected NodeRef createNewRecordedVersion(QName sourceTypeRef,
                                               NodeRef versionHistoryRef,
                                               Map<QName, Serializable> standardVersionProperties,
                                               Map<String, Serializable> versionProperties,
                                               int versionNumber,
                                               PolicyScope nodeDetails)
    {
        NodeRef versionNodeRef = null;

        // Disable auto-version behaviour
        policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_VERSIONABLE);

        // disable other behaviours that we don't want to trigger during this process
        policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
        policyBehaviourFilter.disableBehaviour(ContentModel.TYPE_MULTILINGUAL_CONTAINER);

        // disable model security check
        modelSecurityService.disable();

        // disable property editable check
        recordService.disablePropertyEditableCheck();

        try
        {
            // get the destination file plan
            final NodeRef filePlan = (NodeRef)versionProperties.get(KEY_FILE_PLAN);
            if (filePlan == null)
            {
                throw new AlfrescoRuntimeException("Can't create a new recorded version, because no file plan has been specified in the version properties.");
            }

            // create a copy of the source node and place in the file plan
            final NodeRef nodeRef = (NodeRef)standardVersionProperties.get(Version2Model.PROP_QNAME_FROZEN_NODE_REF);

            cmObjectType.disableCopy();
            try
            {
                // create record
                final NodeRef record = recordService.createRecordFromCopy(filePlan, nodeRef);

                // apply version record aspect to record
                PropertyMap versionRecordProps = new PropertyMap(3);
                versionRecordProps.put(PROP_VERSIONED_NODEREF, nodeRef);
                versionRecordProps.put(RecordableVersionModel.PROP_VERSION_LABEL,
                        standardVersionProperties.get(
                                QName.createQName(Version2Model.NAMESPACE_URI,
                                        Version2Model.PROP_VERSION_LABEL)));
                versionRecordProps.put(RecordableVersionModel.PROP_VERSION_DESCRIPTION,
                        standardVersionProperties.get(
                                QName.createQName(Version2Model.NAMESPACE_URI,
                                        Version2Model.PROP_VERSION_DESCRIPTION)));
                nodeService.addAspect(record, ASPECT_VERSION_RECORD, versionRecordProps);

                // wire record up to previous record
                linkToPreviousVersionRecord(nodeRef, record);

                // create version nodeRef
                ChildAssociationRef childAssocRef = dbNodeService.createNode(
                        versionHistoryRef,
                        Version2Model.CHILD_QNAME_VERSIONS,
                        QName.createQName(Version2Model.NAMESPACE_URI, Version2Model.CHILD_VERSIONS + "-" + versionNumber),
                        sourceTypeRef,
                        null);
                versionNodeRef = childAssocRef.getChildRef();

                // add aspect with the standard version properties to the 'version' node
                nodeService.addAspect(versionNodeRef, Version2Model.ASPECT_VERSION, standardVersionProperties);

                // add the recordedVersion aspect with link to record
                nodeService.addAspect(versionNodeRef, ASPECT_RECORDED_VERSION, Collections.singletonMap(PROP_RECORD_NODE_REF, (Serializable)record));

                // freeze auditable aspect information
                freezeAuditableAspect(nodeRef, versionNodeRef);
            }
            finally
            {
                cmObjectType.enableCopy();
            }
        }
        finally
        {
            // enable model security check
            modelSecurityService.enable();

            // enable property editable check
            recordService.enablePropertyEditableCheck();

            // Enable behaviours
            this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);
            this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
            this.policyBehaviourFilter.enableBehaviour(ContentModel.TYPE_MULTILINGUAL_CONTAINER);
        }

        // If the auditable aspect is not there then add it to the 'version' node (after original aspects have been frozen)
        if (!dbNodeService.hasAspect(versionNodeRef, ContentModel.ASPECT_AUDITABLE))
        {
            dbNodeService.addAspect(versionNodeRef, ContentModel.ASPECT_AUDITABLE, null);
        }

        if (logger.isTraceEnabled())
        {
            logger.trace("createNewRecordedVersion created (" + versionNumber + ") " + versionNodeRef);
        }

        return versionNodeRef;
    }

    /**
     * Helper method to link the record to the previous version record
     *
     * @param nodeRef   noderef source node reference
     * @param record    record  record node reference
     */
    private void linkToPreviousVersionRecord(final NodeRef nodeRef, final NodeRef record)
    {
        final NodeRef latestRecordVersion = getLatestVersionRecord(nodeRef);
        if (latestRecordVersion != null)
        {
            authenticationUtil.runAsSystem(new RunAsWork<Void>()
            {
                @Override
                public Void doWork() throws Exception
                {
                    // indicate that the new record versions the previous record
                    relationshipService.addRelationship(CUSTOM_REF_VERSIONS.getLocalName(), record, latestRecordVersion);
                    return null;
                }
            });
        }
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

        // wire record up to previous record
        VersionHistory versionHistory = getVersionHistory(nodeRef);
        if (versionHistory != null)
        {
            Collection<Version> previousVersions = versionHistory.getAllVersions();
            for (Version previousVersion : previousVersions)
            {
                // look for the associated record
                final NodeRef previousRecord = (NodeRef)previousVersion.getVersionProperties().get(PROP_VERSION_RECORD);
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
     * Freezes audit aspect properties.
     *
     * @param nodeRef
     * @param versionNodeRef
     */
    private void freezeAuditableAspect(NodeRef nodeRef, NodeRef versionNodeRef)
    {
        if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_AUDITABLE))
        {
            Map<QName, Serializable> properties = dbNodeService.getProperties(nodeRef);
            dbNodeService.setProperty(versionNodeRef, Version2Model.PROP_QNAME_FROZEN_CREATOR, properties.get(ContentModel.PROP_CREATOR));
            dbNodeService.setProperty(versionNodeRef, Version2Model.PROP_QNAME_FROZEN_CREATED, properties.get(ContentModel.PROP_CREATED));
            dbNodeService.setProperty(versionNodeRef, Version2Model.PROP_QNAME_FROZEN_MODIFIER, properties.get(ContentModel.PROP_MODIFIER));
            dbNodeService.setProperty(versionNodeRef, Version2Model.PROP_QNAME_FROZEN_MODIFIED, properties.get(ContentModel.PROP_MODIFIED));
            dbNodeService.setProperty(versionNodeRef, Version2Model.PROP_QNAME_FROZEN_ACCESSED, properties.get(ContentModel.PROP_ACCESSED));
            if (properties.get(ContentModel.PROP_OWNER) != null)
            {
                dbNodeService.setProperty(versionNodeRef, PROP_FROZEN_OWNER, properties.get(ContentModel.PROP_OWNER));
            }
        }
    }

    /**
     * @see org.alfresco.repo.version.Version2ServiceImpl#getVersion(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected Version getVersion(NodeRef versionRef)
    {
        Version version = super.getVersion(versionRef);

        NodeRef record = (NodeRef)dbNodeService.getProperty(versionRef, PROP_RECORD_NODE_REF);
        if (record != null)
        {
            version.getVersionProperties().put(PROP_VERSION_RECORD, record);
        }

        return version;
    }

    /**
     * @see org.alfresco.repo.version.Version2ServiceImpl#revert(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.version.Version, boolean)
     */
    @Override
    public void revert(NodeRef nodeRef, Version version, boolean deep)
    {
        String versionPolicy = (String) dbNodeService.getProperty(nodeRef, PROP_RECORDABLE_VERSION_POLICY);

        super.revert(nodeRef, version, deep);

        if (isNotBlank(versionPolicy))
        {
            dbNodeService.setProperty(nodeRef, PROP_RECORDABLE_VERSION_POLICY, versionPolicy);
        }
    }
}
