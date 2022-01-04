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

package org.alfresco.module.org_alfresco_module_rm.action.dm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.AuditableActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.action.dm.RecordActionUtils.Services;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionService;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

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
    public static final String PARAM_PATH = "path";

    private static final String FILE_VERSION_RECORDS_CAPABILITY = "FileVersionRecords";

    /** Sync Model URI */
    private static final String SYNC_MODEL_1_0_URI = "http://www.alfresco.org/model/sync/1.0";
    
    /** Synced aspect */
    private static final QName ASPECT_SYNCED = QName.createQName(SYNC_MODEL_1_0_URI, "synced");

    /** Node service */
    private NodeService nodeService;

    /** File plan service */
    private FilePlanService filePlanService;

    /** Dictionary service */
    private DictionaryService dictionaryService;
    
    /** Recordable version service */
    private RecordableVersionService recordableVersionService;
    
    /** Authentication util */
    private AuthenticationUtil authenticationUtil;

    /** Record service */
    private RecordService recordService;

    /** Capability service */
    private CapabilityService capabilityService;

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
     * @param recordableVersionService  recordable version service
     */
    public void setRecordableVersionService(RecordableVersionService recordableVersionService)
    {
        this.recordableVersionService = recordableVersionService;
    }
    
    /**
     * @param authenticationUtil authentication util
     */
    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil)
    {
        this.authenticationUtil = authenticationUtil;
    }

    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
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
        else if (isActionEligible(actionedUponNodeRef))
        {
            NodeRef filePlan = (NodeRef)action.getParameterValue(PARAM_FILE_PLAN);
            if (filePlan == null)
            {
                filePlan = RecordActionUtils.getDefaultFilePlan(authenticationUtil, filePlanService, NAME);
            }
            // verify that the provided file plan is actually a file plan
            else if (!filePlanService.isFilePlan(filePlan))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Can not declare version record, because the provided file plan node reference is not a file plan.");
                }
                throw new AlfrescoRuntimeException("Can not declare version record, because the provided file plan node reference is not a file plan.");
            }

            // resolve destination record folder if path supplied
            NodeRef destinationRecordFolder = null;
            String pathParameter = (String) action.getParameterValue(PARAM_PATH);
            if (pathParameter != null && !pathParameter.isEmpty())
            {
                RecordActionUtils.Services services = new Services(nodeService, filePlanService, authenticationUtil);
                destinationRecordFolder = RecordActionUtils.resolvePath(services, filePlan, pathParameter, NAME);
            }

            // create record from latest version
            if (destinationRecordFolder != null)
            {
                boolean hasCapability = capabilityService.hasCapability(destinationRecordFolder, FILE_VERSION_RECORDS_CAPABILITY);
                // validate destination record folder
                if (hasCapability)
                {
                    NodeRef recordedVersion = recordableVersionService.createRecordFromLatestVersion(destinationRecordFolder, actionedUponNodeRef);
                    recordService.file(recordedVersion);
                }
                else
                {
                    throw new AccessDeniedException(I18NUtil.getMessage("permissions.err_access_denied"));
                }
            }
            else
            {
                recordableVersionService.createRecordFromLatestVersion(filePlan, actionedUponNodeRef);
            }
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
        params.add(new ParameterDefinitionImpl(PARAM_PATH, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_PATH)));
    }

    /* Check aspects that stop declaring the version as record.*/
    private boolean isActionEligible(NodeRef actionedUponNodeRef)
    {
        Map<QName, String> mappedAspects = new HashMap<>();

        mappedAspects.put(ASPECT_RECORD, " is already a record.");
        mappedAspects.put(ContentModel.ASPECT_WORKING_COPY, " is a working copy.");
        mappedAspects.put(ASPECT_RECORD_REJECTION_DETAILS, " has previously been rejected.");
        mappedAspects.put(ASPECT_SYNCED, " is synched content.");

        for (Map.Entry<QName, String> aspect : mappedAspects.entrySet())
        {
            if (nodeService.hasAspect(actionedUponNodeRef, aspect.getKey()))
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Can not declare version record, because " + actionedUponNodeRef.toString() + aspect.getValue());
                }
                return false;
            }
        }
        if (!nodeService.hasAspect(actionedUponNodeRef, ContentModel.ASPECT_VERSIONABLE))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Can not declare version record, because " + actionedUponNodeRef.toString() + " does not have the versionable aspect applied.");
            }
            return false;
        }
        return true;
    }
}
