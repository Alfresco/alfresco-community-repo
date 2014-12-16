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
package org.alfresco.module.org_alfresco_module_rm.action.dm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.AuditableActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Creates a new record from the 'current' document version.
 *
 * Note:  This is a 'normal' dm action, rather than a records management action.
 *
 * @author Roy Wetherall
 */
public class DeclareAsVersionRecordAction extends AuditableActionExecuterAbstractBase
                                implements RecordsManagementModel
{
    /** Logger */
    private static Log logger = LogFactory.getLog(DeclareAsVersionRecordAction.class);

    /** Action name */
    public static final String NAME = "declare-version-record";

    /** Parameter names */
    public static final String PARAM_FILE_PLAN = "file-plan";

    /** Sync Model URI */
    static final String SYNC_MODEL_1_0_URI = "http://www.alfresco.org/model/sync/1.0";
    
    /** Synced aspect */
    static final QName ASPECT_SYNCED = QName.createQName(SYNC_MODEL_1_0_URI, "synced");

    /** Node service */
    private NodeService nodeService;

    /** File plan service */
    private FilePlanService filePlanService;

    /** Dictionary service */
    private DictionaryService dictionaryService;
    
    /** version service */
    private VersionService versionService;

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @param versionService	version service
     */
    public void setVersionService(VersionService versionService) 
    {
		this.versionService = versionService;
	}

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(final Action action, final NodeRef actionedUponNodeRef)
    {
        if (!nodeService.exists(actionedUponNodeRef))
        {
            // do not create record if the actioned upon node does not exist!
            if (logger.isDebugEnabled())
            {
                logger.debug("Can not declare version as record, because " + actionedUponNodeRef.toString() + " does not exist.");
            }
        }
        else if (!dictionaryService.isSubClass(nodeService.getType(actionedUponNodeRef), ContentModel.TYPE_CONTENT))
        {
            // TODO eventually we should support other types .. either as record folders or as composite records
            if (logger.isDebugEnabled())
            {
                logger.debug("Can not declare version as record, because " + actionedUponNodeRef.toString() + " is not a supported type.");
            }
        }
        else if (!nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_VERSIONABLE))
        {
        	if (logger.isDebugEnabled())
        	{
        		logger.debug("Can not declare version record, because " + actionedUponNodeRef.toString() + " does not have the versionable aspect applied.");
        	}
        }
        else if (nodeService.hasAspect(actionedUponNodeRef, ASPECT_RECORD))
        {
            // Do not declare version record if the actioned upon node is already a record!
            if (logger.isDebugEnabled())
            {
                logger.debug("Can not declare version record, because " + actionedUponNodeRef.toString() + " is already a record.");
            }
        }
        else if (nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_WORKING_COPY))
        {
            // We can not create records from working copies
            if (logger.isDebugEnabled())
            {
                logger.debug("Can not declare version record, because " + actionedUponNodeRef.toString() + " is a working copy.");
            }

        }
        else if (nodeService.hasAspect(actionedUponNodeRef, ASPECT_RECORD_REJECTION_DETAILS))
        {
            // can not create a record from a previously rejected one
            if (logger.isDebugEnabled())
            {
                logger.debug("Can not declare version record, because " + actionedUponNodeRef.toString() + " has previously been rejected.");
            }
        }
        else if (nodeService.hasAspect(actionedUponNodeRef, ASPECT_SYNCED))
        {
            // can't declare the record if the node is sync'ed
            if (logger.isDebugEnabled())
            {
                logger.debug("Can't declare version record, because " + actionedUponNodeRef.toString() + " is synched content.");
            }
        }
        else
        {
            NodeRef filePlan = (NodeRef)action.getParameterValue(PARAM_FILE_PLAN);
            if (filePlan == null)
            {
                // TODO .. eventually make the file plan parameter required

                filePlan = AuthenticationUtil.runAs(new RunAsWork<NodeRef>()
                {
                    @Override
                    public NodeRef doWork()
                    {
                        return filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
                    }
                }, AuthenticationUtil.getAdminUserName());

                // if the file plan is still null, raise an exception
                if (filePlan == null)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Can not declare version record, because the default file plan can not be determined.  Make sure at least one file plan has been created.");
                    }
                    throw new AlfrescoRuntimeException("Can not declare version record, because the default file plan can not be determined.");
                }
            }
            else
            {
                // verify that the provided file plan is actually a file plan
                if (!filePlanService.isFilePlan(filePlan))
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Can not declare version record, because the provided file plan node reference is not a file plan.");
                    }
                    throw new AlfrescoRuntimeException("Can not declare version record, because the provided file plan node reference is not a file plan.");
                }
            }
            
            // create version properties
            Map<String, Serializable> versionProperties = new HashMap<String, Serializable>(4);
            versionProperties.put(Version.PROP_DESCRIPTION, "Recorded version");					// TODO make this a configurable option of the action
            versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);				// TODO make this a configurable option of the action
            versionProperties.put(RecordableVersionServiceImpl.KEY_RECORDABLE_VERSION, true);
            versionProperties.put(RecordableVersionServiceImpl.KEY_FILE_PLAN, filePlan);
            
            // create recordable version
            versionService.createVersion(actionedUponNodeRef, versionProperties);
        }
    }

    /**
     * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> params)
    {
        // NOTE:  commented out for now so that it doesn't appear in the UI ... enable later when multi-file plan support is added
        //params.add(new ParameterDefinitionImpl(PARAM_FILE_PLAN, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_FILE_PLAN)));
    }

}
