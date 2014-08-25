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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.impl.ExtendedPermissionService;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.Version2ServiceImpl;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.version.ReservedVersionNameException;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
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
    
    /** file plan service */
    protected FilePlanService filePlanService;
    
    /** file folder service */
    protected FileFolderService fileFolderService;
    
    /** extended permission service */
    protected ExtendedPermissionService extendedPermissionService;
    
    /** ownable service */
    protected OwnableService ownableService;
    
    /** extended security service */
    protected ExtendedSecurityService extendedSecurityService;
    
    /** authentication util helper */
    protected AuthenticationUtil authenticationUtil;    
    
    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }   
    
    /**
     * @param fileFolderService file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    /**
     * @param extendedPermissionService extended permission service
     */
    public void setExtendedPermissionService(ExtendedPermissionService extendedPermissionService)
    {
        this.extendedPermissionService = extendedPermissionService;
    }
    
    /**
     * @param ownableService    ownable service
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }
    
    /**
     * @param extendedSecurityService   extended security service
     */
    public void setExtendedSecurityService(ExtendedSecurityService extendedSecurityService)
    {
        this.extendedSecurityService = extendedSecurityService;
    }
    
    /**
     * @param authenticationUtil    authentication util helper
     */
    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil)
    {
        this.authenticationUtil = authenticationUtil;
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
        NodeRef filePlan = filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
        if (filePlan == null)
        {
            throw new AlfrescoRuntimeException("Can't create a recorded version, because there is no file plan.");
        }
        return filePlan;
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
     * @param sourceTypeRef
     * @param versionHistoryRef
     * @param standardVersionProperties
     * @param versionProperties
     * @param versionNumber
     * @param nodeDetails
     * @return
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
            
            // create record
            NodeRef record = createRecord(nodeRef, filePlan);

            // create version nodeRef
            ChildAssociationRef childAssocRef = dbNodeService.createNode(
                    versionHistoryRef, 
                    Version2Model.CHILD_QNAME_VERSIONS,
                    QName.createQName(Version2Model.NAMESPACE_URI, Version2Model.CHILD_VERSIONS + "-" + versionNumber), // TODO - testing - note: all children (of a versioned node) will have the same version number, maybe replace with a version sequence of some sort 001-...00n
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
            // Enable behaviours
            this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_VERSIONABLE);            
            this.policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_MULTILINGUAL_DOCUMENT);
            this.policyBehaviourFilter.enableBehaviour(ContentModel.TYPE_MULTILINGUAL_CONTAINER);
        }
        
        // If the auditable aspect is not there then add it to the 'version' node (after original aspects have been frozen)
        if (dbNodeService.hasAspect(versionNodeRef, ContentModel.ASPECT_AUDITABLE) == false)
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
     * Create record from current version
     * 
     * @param nodeRef               state to freeze
     * @param filePlan              destination file plan
     * @return {@link NodeRef}      versioned record
     */
    private NodeRef createRecord(final NodeRef nodeRef, final NodeRef filePlan)
    {
        return authenticationUtil.runAs(new RunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {                               
                // get the unfiled record folder
                final NodeRef unfiledRecordFolder = filePlanService.getUnfiledContainer(filePlan);
                
                // get the documents readers
                Long aclId = dbNodeService.getNodeAclId(nodeRef);
                Set<String> readers = extendedPermissionService.getReaders(aclId);
                Set<String> writers = extendedPermissionService.getWriters(aclId);
    
                // add the current owner to the list of extended writers
                Set<String> modifiedWrtiers = new HashSet<String>(writers);
                if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_OWNABLE))
                {
                    String owner = ownableService.getOwner(nodeRef);
                    if (owner != null && !owner.isEmpty() && !owner.equals(OwnableService.NO_OWNER))
                    {
                        modifiedWrtiers.add(owner);
                    }
                }
                
                // add the current user as extended writer
                modifiedWrtiers.add(authenticationUtil.getFullyAuthenticatedUser());
    
                // copy version state and create record
                NodeRef record = null;
                try
                {
                    List<AssociationRef> originalAssocs = null;                         
                    if (dbNodeService.hasAspect(nodeRef, ContentModel.ASPECT_COPIEDFROM))
                    {
                        // take a note of any copyFrom information already on the node
                        originalAssocs = dbNodeService.getTargetAssocs(nodeRef, ContentModel.ASSOC_ORIGINAL);
                    }
                    
                    // create a copy of the original state and add it to the unfiled record container
                    FileInfo recordInfo = fileFolderService.copy(nodeRef, unfiledRecordFolder, null);
                    record = recordInfo.getNodeRef();
                    
                    // remove added copy assocs
                    List<AssociationRef> recordAssocs = dbNodeService.getTargetAssocs(record, ContentModel.ASSOC_ORIGINAL);
                    for (AssociationRef recordAssoc : recordAssocs)
                    {
                        dbNodeService.removeAssociation(
                                recordAssoc.getSourceRef(),
                                recordAssoc.getTargetRef(),
                                ContentModel.ASSOC_ORIGINAL);
                    }
                    
                    // re-add origional assocs or remove aspect
                    if (originalAssocs == null)
                    {
                        dbNodeService.removeAspect(record, ContentModel.ASPECT_COPIEDFROM);
                    }
                    else
                    {
                        for (AssociationRef originalAssoc : originalAssocs)
                        {
                            dbNodeService.createAssociation(originalAssoc.getSourceRef(), originalAssoc.getTargetRef(), ContentModel.ASSOC_ORIGINAL);
                        }
                    }                    
                }
                catch (FileNotFoundException e)
                {
                    throw new AlfrescoRuntimeException("Can't create recorded version, because copy fails.", e);
                }
                                                           
                // set extended security on record
                extendedSecurityService.addExtendedSecurity(record, readers, writers);
        
                return record;
            }
        }, authenticationUtil.getAdminUserName());
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
            version.getVersionProperties().put("RecordVersion", record);
        }
        
        return version;
    }
}
