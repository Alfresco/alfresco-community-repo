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

package org.alfresco.module.org_alfresco_module_rm.recordfolder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Record Folder Service Implementation
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class RecordFolderServiceImpl extends    ServiceBaseImpl
                                     implements RecordFolderService,
                                                RecordsManagementModel
{
    /** Logger */
    private static Log logger = LogFactory.getLog(RecordFolderServiceImpl.class);

    /** I18N */
    private static final String MSG_RECORD_FOLDER_EXPECTED = "rm.service.record-folder-expected";
    private static final String MSG_PARENT_RECORD_FOLDER_ROOT = "rm.service.parent-record-folder-root";
    private static final String MSG_PARENT_RECORD_FOLDER_TYPE = "rm.service.parent-record-folder-type";
    private static final String MSG_RECORD_FOLDER_TYPE = "rm.service.record-folder-type";
    private static final String MSG_CLOSE_RECORD_FOLDER_NOT_FOLDER = "rm.service.close-record-folder-not-folder";

    /** Disposition service */
    private DispositionService dispositionService;

    /** Record Service */
    private RecordService recordService;

    /** File Plan Service */
    private FilePlanService filePlanService;

    /**
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * @param recordService     record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @param filePlanService   file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService#setupRecordFolder(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void setupRecordFolder(NodeRef nodeRef)
    {
        // initialise disposition details
        if (!nodeService.hasAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE))
        {
            DispositionSchedule di = dispositionService.getDispositionSchedule(nodeRef);
            if (di != null && !di.isRecordLevelDisposition())
            {
                nodeService.addAspect(nodeRef, ASPECT_DISPOSITION_LIFECYCLE, null);
            }
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService#isRecordFolderDeclared(NodeRef)
     */
    @Override
    public boolean isRecordFolderDeclared(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        // Check we have a record folder
        if (!isRecordFolder(nodeRef))
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_RECORD_FOLDER_EXPECTED));
        }

        boolean result = true;

        // Check that each record in the record folder in declared
        List<NodeRef> records = recordService.getRecords(nodeRef);
        for (NodeRef record : records)
        {
            if (!recordService.isDeclared(record))
            {
                result = false;
                break;
            }
        }

        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService#isRecordFolderClosed(NodeRef)
     */
    @Override
    public boolean isRecordFolderClosed(final NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        // Check we have a record folder
        if (!isRecordFolder(nodeRef))
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_RECORD_FOLDER_EXPECTED));
        }

        return AuthenticationUtil.runAsSystem(new RunAsWork<Boolean>()
        {
            public Boolean doWork() throws Exception
            {
                return ((Boolean) nodeService.getProperty(nodeRef, PROP_IS_CLOSED));
            }
        });
    }

    @Override
    public NodeRef createRecordFolder(NodeRef rmContainer, String name,
            QName type)
    {
        ParameterCheck.mandatory("rmContainer", rmContainer);
        ParameterCheck.mandatoryString("name", name);
        ParameterCheck.mandatory("type", type);

        return createRecordFolder(rmContainer, name, type, null);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService#createRecordFolder(NodeRef, String, QName, Map)
     */
    @Override
    public NodeRef createRecordFolder(NodeRef rmContainer, String name,
            QName type, Map<QName, Serializable> properties)
    {
        ParameterCheck.mandatory("rmContainer", rmContainer);
        ParameterCheck.mandatoryString("name", name);
        ParameterCheck.mandatory("type", type);
        // "properties" is not mandatory

        // Check that we are not trying to create a record folder in a root container
        if (filePlanService.isFilePlan(rmContainer))
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_PARENT_RECORD_FOLDER_ROOT));
        }

        // Check that the parent is a container
        QName parentType = nodeService.getType(rmContainer);
        if (!TYPE_RECORD_CATEGORY.equals(parentType) &&
            !dictionaryService.isSubClass(parentType, TYPE_RECORD_CATEGORY))
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_PARENT_RECORD_FOLDER_TYPE, parentType.toString()));
        }

        // Check that the the provided type is a sub-type of rm:recordFolder
        if (!TYPE_RECORD_FOLDER.equals(type) &&
            !dictionaryService.isSubClass(type, TYPE_RECORD_FOLDER))
        {
            throw new AlfrescoRuntimeException(I18NUtil.getMessage(MSG_RECORD_FOLDER_TYPE, type.toString()));
        }

        Map<QName, Serializable> props = new HashMap<>(1);
        if (properties != null && properties.size() != 0)
        {
            props.putAll(properties);
        }
        props.put(ContentModel.PROP_NAME, name);

        return nodeService.createNode(
                rmContainer,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (name.length() > QName.MAX_LENGTH ? name.substring(0, QName.MAX_LENGTH) : name)),
                type,
                props).getChildRef();
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService#createRecordFolder(NodeRef, String)
     */
    @Override
    public NodeRef createRecordFolder(NodeRef rmContainer, String name)
    {
        ParameterCheck.mandatory("rmContainer", rmContainer);
        ParameterCheck.mandatoryString("name", name);

        // TODO defaults to rm:recordFolder, but in future could auto-detect sub-type of folder based on context
        return createRecordFolder(rmContainer, name, TYPE_RECORD_FOLDER);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService#createRecordFolder(NodeRef, String, Map)
     */
    @Override
    public NodeRef createRecordFolder(NodeRef rmContainer, String name,
            Map<QName, Serializable> properties)
    {
        ParameterCheck.mandatory("rmContainer", rmContainer);
        ParameterCheck.mandatoryString("name", name);
        ParameterCheck.mandatory("properties", properties);

        return createRecordFolder(rmContainer, name, TYPE_RECORD_FOLDER, properties);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService#getRecordFolders(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public List<NodeRef> getRecordFolders(NodeRef record)
    {
        ParameterCheck.mandatory("record", record);

        List<NodeRef> result = new ArrayList<>(1);
        if (recordService.isRecord(record))
        {
            List<ChildAssociationRef> assocs = nodeService.getParentAssocs(record, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef assoc : assocs)
            {
                NodeRef parent = assoc.getParentRef();
                if (isRecordFolder(parent))
                {
                    result.add(parent);
                }
            }
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService#closeRecordFolder(NodeRef)
     */
    @Override
    public void closeRecordFolder(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        if (isRecord(nodeRef))
        {
            ChildAssociationRef assocRef = nodeService.getPrimaryParent(nodeRef);
            if (assocRef != null)
            {
                nodeRef = assocRef.getParentRef();
            }
        }

        if (isRecordFolder(nodeRef))
        {
            if (!isRecordFolderClosed(nodeRef))
            {
                nodeService.setProperty(nodeRef, PROP_IS_CLOSED, true);
            }
        }
        else
        {
            if (logger.isWarnEnabled())
            {
                logger.warn(I18NUtil.getMessage(MSG_CLOSE_RECORD_FOLDER_NOT_FOLDER, nodeRef.toString()));
            }
        }
    }
}
