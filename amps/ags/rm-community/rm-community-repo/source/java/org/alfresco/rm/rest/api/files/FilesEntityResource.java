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

package org.alfresco.rm.rest.api.files;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.RMNode;
import org.alfresco.rm.rest.api.model.Record;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * An implementation of an Entity Resource for a file
 *
 * @author Ana Bozianu
 * @since 2.6
 */
@EntityResource(name="files", title = "Files")
public class FilesEntityResource implements InitializingBean
{
    private ApiNodesModelFactory nodesModelFactory;
    private FilePlanComponentsApiUtils apiUtils;
    private AuthenticationUtil authenticationUtil;
    private FilePlanService filePlanService;
    private FileFolderService fileFolderService;
    private RecordService recordService;
    private TransactionService transactionService;

    public void setApiUtils(FilePlanComponentsApiUtils apiUtils)
    {
        this.apiUtils = apiUtils;
    }

    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil)
    {
        this.authenticationUtil = authenticationUtil;
    }

    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    public void setNodesModelFactory(ApiNodesModelFactory nodesModelFactory)
    {
        this.nodesModelFactory = nodesModelFactory;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    @Operation("declare")
    @WebApiDescription(title = "Declare as record", description="Declare a file as record.")
    public Record declareAsRecord(String fileId, Void body, Parameters parameters, WithResponse withResponse)
    {
        // Get fileplan
        NodeRef filePlan = authenticationUtil.runAsSystem(new RunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork()
            {
                return filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            }
        });

        // default false (if not provided)
        boolean hideRecord = Boolean.valueOf(parameters.getParameter(Record.PARAM_HIDE_RECORD));

        // Get record folder, if provided
        final NodeRef targetRecordFolder = extractAndValidateTargetRecordFolder(parameters);

        // Create the record
        NodeRef file = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, fileId);
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                recordService.createRecord(filePlan, file, targetRecordFolder, !hideRecord);
                if (targetRecordFolder != null)
                {
                    recordService.file(file);
                }
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

        // Return record state
        FileInfo info = fileFolderService.getFileInfo(file);
        return nodesModelFactory.createRecord(info, parameters, null, false);
    }

    /* Helper method to determine the target record folder, if given */
    private NodeRef extractAndValidateTargetRecordFolder(Parameters parameters)
    {
        // Get record folder, if provided
        NodeRef targetParent = null;
        final String targetParentId = parameters.getParameter(RMNode.PARAM_PARENT_ID);
        if (targetParentId != null)
        {
            targetParent = apiUtils.lookupAndValidateNodeType(targetParentId, RecordsManagementModel.TYPE_RECORD_FOLDER);
        }
        return targetParent;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        ParameterCheck.mandatory("nodesModelFactory", nodesModelFactory);
        ParameterCheck.mandatory("authenticationUtil", authenticationUtil);
        ParameterCheck.mandatory("filePlanService", filePlanService);
        ParameterCheck.mandatory("fileFolderService", fileFolderService);
        ParameterCheck.mandatory("recordService", recordService);
    }
}
