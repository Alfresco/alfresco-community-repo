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

package org.alfresco.rm.rest.api.impl;

import java.security.InvalidParameterException;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.RMNodes;
import org.alfresco.rm.rest.api.Records;
import org.alfresco.rm.rest.api.model.TargetContainer;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.extensions.surf.util.ParameterCheck;

public class RecordsImpl implements Records, InitializingBean
{
    protected RecordService recordService;
    protected FilePlanService filePlanService;
    protected NodeService nodeService;
    protected FileFolderService fileFolderService;
    protected DictionaryService dictionaryService;
    protected RMNodes nodes;

    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNodes(RMNodes nodes)
    {
        this.nodes = nodes;
    }

    @Override
    public Node declareFileAsRecord(String fileId, Parameters parameters)
    {
        // Parameter check
        if ((fileId == null) || (fileId.isEmpty()))
        {
            throw new InvalidArgumentException("Missing fileId");
        }

        // Get file to be declared
        NodeRef fileNodeRef = nodes.validateNode(fileId) ;

        // Get fileplan
        NodeRef filePlan = AuthenticationUtil.runAsSystem(new RunAsWork<NodeRef>()
        {
            @Override
            public NodeRef doWork()
            {
                return filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID);
            }
        });

        // default false (if not provided)
        boolean hideRecord = Boolean.valueOf(parameters.getParameter(PARAM_HIDE_RECORD));

        // Create the record
        recordService.createRecord(filePlan, fileNodeRef, !hideRecord);

        // Get information about the new record
        return nodes.getFolderOrDocument(fileId, parameters);
    }

    @Override
    public Node fileOrLinkRecord(String recordId, TargetContainer target, Parameters parameters)
    {
        ParameterCheck.mandatoryString("recordId", recordId);

        if((target.getTargetParentId() == null || target.getTargetParentId().isEmpty()) &&
           (target.getRelativePath() == null || target.getRelativePath().isEmpty()))
        {
            throw new InvalidParameterException("No target folder information was provided");
        }

        // Get record
        NodeRef record = nodes.validateNode(recordId);

        // Get record folder to file/link the record to
        String parentContainerId = target.getTargetParentId();
        if(parentContainerId == null || parentContainerId.isEmpty())
        {
            // If target container not provided get fileplan
            parentContainerId = AuthenticationUtil.runAsSystem(new RunAsWork<String>()
            {
                @Override
                public String doWork()
                {
                    return filePlanService.getFilePlanBySiteId(FilePlanService.DEFAULT_RM_SITE_ID).getId();
                }
            });
        }
        NodeRef parentRecordFolder = nodes.getOrCreatePath(parentContainerId, target.getRelativePath(), ContentModel.TYPE_CONTENT);

        // Check if the target is a record folder
        if(!dictionaryService.isSubClass(nodeService.getType(parentRecordFolder), RecordsManagementModel.TYPE_RECORD_FOLDER))
        {
            throw new InvalidArgumentException("The provided target parent is not a record folder");
        }

        // Get the current parent type to decide if we link or move the record
        NodeRef primaryParent = nodeService.getPrimaryParent(record).getParentRef();
        if(dictionaryService.isSubClass(nodeService.getType(primaryParent), RecordsManagementModel.TYPE_RECORD_FOLDER))
        {    
            recordService.link(record, parentRecordFolder);
        }
        else
        {
            try
            {
                fileFolderService.moveFrom(record, primaryParent, parentRecordFolder, null);
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

        // Get the record info
        return nodes.getFolderOrDocument(recordId, parameters);
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        ParameterCheck.mandatory("recordService", recordService);
        ParameterCheck.mandatory("filePlanService", filePlanService);
        ParameterCheck.mandatory("nodes", nodes);
        ParameterCheck.mandatory("nodeService",  nodeService);
        ParameterCheck.mandatory("fileFolderService", fileFolderService);
        ParameterCheck.mandatory("dictionaryService", dictionaryService);
    }
}
