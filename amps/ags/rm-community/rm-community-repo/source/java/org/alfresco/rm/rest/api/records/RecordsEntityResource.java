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

package org.alfresco.rm.rest.api.records;

import static org.alfresco.module.org_alfresco_module_rm.util.RMParameterCheck.checkNotBlank;
import static org.alfresco.util.ParameterCheck.mandatory;

import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordMissingMetadataException;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.rest.framework.BinaryProperties;
import org.alfresco.rest.framework.Operation;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.WebApiParam;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.BinaryResourceAction;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.webscripts.WithResponse;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.Record;
import org.alfresco.rm.rest.api.model.TargetContainer;
import org.alfresco.service.cmr.activities.ActivityPoster;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.ConcurrencyFailureException;

/**
 * An implementation of an Entity Resource for a record
 *
 * @author Ana Bozianu
 * @author Tuna Aksoy
 * @since 2.6
 */
@EntityResource(name="records", title = "Records")
public class RecordsEntityResource implements BinaryResourceAction.Read,
                                              EntityResourceAction.ReadById<Record>,
                                              EntityResourceAction.Delete,
                                              EntityResourceAction.Update<Record>,
                                              InitializingBean
{

    private ApiNodesModelFactory nodesModelFactory;
    private FilePlanComponentsApiUtils apiUtils;
    private FileFolderService fileFolderService;
    private RecordService recordService;
    private NodeService nodeService;
    private TransactionService transactionService;

    public void setNodesModelFactory(ApiNodesModelFactory nodesModelFactory)
    {
        this.nodesModelFactory = nodesModelFactory;
    }

    public void setApiUtils(FilePlanComponentsApiUtils apiUtils)
    {
        this.apiUtils = apiUtils;
    }

    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    /**
     * Download content
     *
     * @param recordId the id of the record to get the content from
     * @param parameters {@link Parameters}
     * @return binary content resource
     * @throws EntityNotFoundException
     */
    @Override
    @WebApiDescription(title = "Download content", description = "Download content for a record with id 'recordId'")
    @BinaryProperties({"content"})
    public BinaryResource readProperty(String recordId, Parameters parameters) throws EntityNotFoundException
    {
        checkNotBlank("recordId", recordId);
        mandatory("parameters", parameters);

        NodeRef record = apiUtils.validateRecord(recordId);
        if(nodeService.getType(record).equals(RecordsManagementModel.TYPE_NON_ELECTRONIC_DOCUMENT))
        {
            throw new IllegalArgumentException("Cannot read content from Non-electronic record " + recordId + ".");
        }
        BinaryResource content = apiUtils.getContent(record, parameters, true);
        NodeRef primaryParent = nodeService.getPrimaryParent(record).getParentRef();
        FileInfo info = fileFolderService.getFileInfo(record);
        apiUtils.postActivity(info, primaryParent, ActivityPoster.DOWNLOADED);
        return content;
    }

    @Operation("file")
    @WebApiDescription(title = "File record", description="File a record into fileplan.")
    public Record fileRecord(String recordId, TargetContainer target, Parameters parameters, WithResponse withResponse)
    {
        checkNotBlank("recordId", recordId);
        mandatory("target", target);
        mandatory("targetParentId", target.getTargetParentId());
        mandatory("parameters", parameters);

        // Get record and target folder
        NodeRef record = apiUtils.validateRecord(recordId);
        NodeRef targetRecordFolder = apiUtils.lookupAndValidateNodeType(target.getTargetParentId(), RecordsManagementModel.TYPE_RECORD_FOLDER);

        // Get the current parent type to decide if we link or move the record
        NodeRef primaryParent = nodeService.getPrimaryParent(record).getParentRef();
        if(RecordsManagementModel.TYPE_RECORD_FOLDER.equals(nodeService.getType(primaryParent)))
        {    
            recordService.link(record, targetRecordFolder);
        }
        else
        {
            try
            {
                fileFolderService.moveFrom(record, primaryParent, targetRecordFolder, null);
            }
            catch (FileExistsException e)
            {
                throw new IntegrityException(e.getMessage(), null);
            }
            catch (FileNotFoundException e)
            {
                throw new ConcurrencyFailureException("The record was deleted while filing it", e);
            }
        }

        // return record state
        FileInfo info = fileFolderService.getFileInfo(record);
        return nodesModelFactory.createRecord(info, parameters, null, false);
    }

    @WebApiDescription(title = "Get record information", description = "Gets information for a record with id 'recordId'")
    @WebApiParam(name = "recordId", title = "The record id")
    public Record readById(String recordId, Parameters parameters)
    {
        checkNotBlank("recordId", recordId);
        mandatory("parameters", parameters);

        NodeRef record = apiUtils.validateRecord(recordId);
        FileInfo info = fileFolderService.getFileInfo(record);
        return nodesModelFactory.createRecord(info, parameters, null, false);
    }

    @Override
    @WebApiDescription(title="Update record", description = "Updates a record with id 'recordId'")
    public Record update(String recordId, Record recordInfo, Parameters parameters)
    {
        checkNotBlank("recordId", recordId);
        mandatory("recordInfo", recordInfo);
        mandatory("parameters", parameters);

        // Get record
        NodeRef record = apiUtils.validateRecord(recordId);

        // update info
        RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>()
        {
            public Void execute()
            {
                apiUtils.updateNode(record, recordInfo, parameters);
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);

        // return record state
        RetryingTransactionCallback<FileInfo> readCallback = new RetryingTransactionCallback<FileInfo>()
        {
            public FileInfo execute()
            {
                return fileFolderService.getFileInfo(record);
            }
        };
        FileInfo info = transactionService.getRetryingTransactionHelper().doInTransaction(readCallback, false, true);
        
        apiUtils.postActivity(info, recordInfo.getParentId(), ActivityType.FILE_UPDATED);
        return nodesModelFactory.createRecord(info, parameters, null, false);
    }

    @Operation ("complete")
    @WebApiDescription (title = "Complete record", description = "Complete a record.")
    public Record completeRecord(String recordId, Void body, Parameters parameters, WithResponse withResponse)
    {
        checkNotBlank("recordId", recordId);
        mandatory("parameters", parameters);

        // Get record
        NodeRef record = apiUtils.validateRecord(recordId);

        // Complete the record
        try
        {
            recordService.complete(record);
        }
        catch (RecordMissingMetadataException e)
        {
            throw new IntegrityException("The record has missing mandatory properties.", null); 
        }

        // return record state
        FileInfo info = fileFolderService.getFileInfo(record);
        return nodesModelFactory.createRecord(info, parameters, null, false);
    }

    @Override
    @WebApiDescription(title = "Delete record", description="Deletes a record with id 'recordId'")
    public void delete(String recordId, Parameters parameters)
    {
        checkNotBlank("recordId", recordId);
        mandatory("parameters", parameters);

        NodeRef record = apiUtils.validateRecord(recordId);
        fileFolderService.delete(record);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        ParameterCheck.mandatory("nodesModelFactory", nodesModelFactory);
        ParameterCheck.mandatory("apiUtils", apiUtils);
        ParameterCheck.mandatory("fileFolderService", fileFolderService);
        ParameterCheck.mandatory("recordService", recordService);
        ParameterCheck.mandatory("nodeService", nodeService);
    }
}
