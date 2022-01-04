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

package org.alfresco.module.org_alfresco_module_rm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.transfer.TransferService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * Records management service implementation.
 *
 * @author Roy Wetherall
 * @deprecated as of 2.2
 */
public class RecordsManagementServiceImpl extends ServiceBaseImpl
                                          implements RecordsManagementService,
                                                     RecordsManagementModel 
{
   /** Store that the RM roots are contained within */
    @SuppressWarnings("unused")
    @Deprecated
    private StoreRef defaultStoreRef = StoreRef.STORE_REF_WORKSPACE_SPACESSTORE;

    /** Service registry */
    private RecordsManagementServiceRegistry serviceRegistry;

    /**
     * Set the service registry service
     *
     * @param serviceRegistry   service registry
     */
    public void setRecordsManagementServiceRegistry(RecordsManagementServiceRegistry serviceRegistry)
    {
        // Internal ops use the unprotected services from the voter (e.g. nodeService)
        this.serviceRegistry = serviceRegistry;
        this.dictionaryService = serviceRegistry.getDictionaryService();
    }

    /**
     * Sets the default RM store reference
     * @param defaultStoreRef    store reference
     */
    @Deprecated
    public void setDefaultStoreRef(StoreRef defaultStoreRef)
    {
        this.defaultStoreRef = defaultStoreRef;
    }

    /**
     * @return File plan service
     */
    private FilePlanService getFilePlanService()
    {
    	return serviceRegistry.getFilePlanService();
    }

    /**
     * @return Record Folder Service
     */
    private RecordFolderService getRecordFolderService()
    {
        return serviceRegistry.getRecordFolderService();
    }

    /**
     * @return Record Service
     */
    private RecordService getRecordService()
    {
        return serviceRegistry.getRecordService();
    }

    /**
     * @return Freeze Service
     */
    private FreezeService getFreezeService()
    {
        return serviceRegistry.getFreezeService();
    }

    /**
     * @return Disposition Service
     */
    private DispositionService getDispositionService()
    {
        return serviceRegistry.getDispositionService();
    }

    /**
     * @return Transfer service
     */
    private TransferService getTransferService()
    {
        return serviceRegistry.getTransferService();
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#isFilePlanComponent(NodeRef)}
     */
    @Override
    public boolean isFilePlanComponent(NodeRef nodeRef)
    {
        return getFilePlanService().isFilePlanComponent(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getFilePlanComponentKind(NodeRef)}
     */
    @Override
    public FilePlanComponentKind getFilePlanComponentKind(NodeRef nodeRef)
    {
        return getFilePlanService().getFilePlanComponentKind(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getFilePlanComponentKindFromType(QName)}
     */
    @Override
    public FilePlanComponentKind getFilePlanComponentKindFromType(QName type)
    {
        return getFilePlanService().getFilePlanComponentKindFromType(type);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#isFilePlanContainer(NodeRef)}
     */
    @Override
    public boolean isRecordsManagementContainer(NodeRef nodeRef)
    {
        return getFilePlanService().isFilePlanContainer(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#isFilePlan(NodeRef)}
     */
    @Override
    public boolean isFilePlan(NodeRef nodeRef)
    {
        return getFilePlanService().isFilePlan(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#isRecordCategory(NodeRef)}
     */
    @Override
    public boolean isRecordCategory(NodeRef nodeRef)
    {
        return getFilePlanService().isRecordCategory(nodeRef);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#isRecordFolder(NodeRef)}
     */
    @Override
    public boolean isRecordFolder(NodeRef nodeRef)
    {
        return getRecordFolderService().isRecordFolder(nodeRef);
    }

    /**
     * @deprecated As of 2.2, see {@link TransferService#isTransfer(NodeRef)}
     */
    @Override
    public boolean isTransfer(NodeRef nodeRef)
    {
        return getTransferService().isTransfer(nodeRef);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordService#isMetadataStub(NodeRef)}
     */
    @Override
    public boolean isMetadataStub(NodeRef nodeRef)
    {
        return getRecordService().isMetadataStub(nodeRef);
    }

    /**
     * @deprecated As of 2.2, see {@link DispositionService#isDisposableItemCutoff(NodeRef)}
     */
    @Override
    public boolean isCutoff(NodeRef nodeRef)
    {
        return getDispositionService().isDisposableItemCutoff(nodeRef);
    }

    /**
     * @deprecated as of 2.1, see {@link FilePlanService#getNodeRefPath(NodeRef)}
     */
    @Override
    public List<NodeRef> getNodeRefPath(NodeRef nodeRef)
    {
        return getFilePlanService().getNodeRefPath(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getFilePlan(NodeRef)}
     */
    @Override
    public NodeRef getFilePlan(NodeRef nodeRef)
    {
        return getFilePlanService().getFilePlan(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getFilePlans()}
     */
    @Override
    public List<NodeRef> getFilePlans()
    {
        return new ArrayList<>(getFilePlanService().getFilePlans());
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String, QName)}
     */
    @Override
    public NodeRef createFilePlan(NodeRef parent, String name, QName type)
    {
        return getFilePlanService().createFilePlan(parent, name, type);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String, QName, Map)}
     */
    @Override
    public NodeRef createFilePlan(NodeRef parent, String name, QName type, Map<QName, Serializable> properties)
    {
        return getFilePlanService().createFilePlan(parent, name, type, properties);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String)}
     */
    @Override
    public NodeRef createFilePlan(NodeRef parent, String name)
    {
        return getFilePlanService().createFilePlan(parent, name);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String, Map)}
     */
    @Override
    public NodeRef createFilePlan(NodeRef parent, String name, Map<QName, Serializable> properties)
    {
        return getFilePlanService().createFilePlan(parent, name, properties);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getAllContained(NodeRef, boolean)}
     */
    @Override
    public List<NodeRef> getAllContained(NodeRef container, boolean deep)
    {
        return getFilePlanService().getAllContained(container, deep);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getAllContained(NodeRef)}
     */
    @Override
    public List<NodeRef> getAllContained(NodeRef container)
    {
        return getFilePlanService().getAllContained(container);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getContainedRecordCategories(NodeRef, boolean)}
     */
    @Override
    public List<NodeRef> getContainedRecordCategories(NodeRef container, boolean deep)
    {
        return getFilePlanService().getContainedRecordCategories(container, deep);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getContainedRecordCategories(NodeRef)}
     */
    @Override
    public List<NodeRef> getContainedRecordCategories(NodeRef container)
    {
        return getFilePlanService().getContainedRecordCategories(container);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getContainedRecordFolders(NodeRef, boolean)}
     */
    @Override
    public List<NodeRef> getContainedRecordFolders(NodeRef container, boolean deep)
    {
        return getFilePlanService().getContainedRecordFolders(container, deep);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getContainedRecordFolders(NodeRef)}
     */
    @Override
    public List<NodeRef> getContainedRecordFolders(NodeRef container)
    {
        return getFilePlanService().getContainedRecordFolders(container);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createRecordCategory(NodeRef, String, QName)}
     */
    @Override
    public NodeRef createRecordCategory(NodeRef parent, String name, QName type)
    {
        return getFilePlanService().createRecordCategory(parent, name, type);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createRecordCategory(NodeRef, String, QName, Map)}
     */
    @Override
    public NodeRef createRecordCategory(NodeRef parent, String name, QName type, Map<QName, Serializable> properties)
    {
        return getFilePlanService().createRecordCategory(parent, name, type, properties);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createRecordCategory(NodeRef, String)}
     */
    @Override
    public NodeRef createRecordCategory(NodeRef parent, String name)
    {
        return getFilePlanService().createRecordCategory(parent, name);
    }

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createRecordCategory(NodeRef, String, Map)}
     */
    public NodeRef createRecordCategory(NodeRef parent, String name, Map<QName, Serializable> properties)
    {
        return getFilePlanService().createRecordCategory(parent, name, properties);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#isRecordFolderDeclared(NodeRef)}
     */
    @Override
    public boolean isRecordFolderDeclared(NodeRef recordFolder)
    {
        return getRecordFolderService().isRecordFolderDeclared(recordFolder);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#isRecordFolderClosed(NodeRef)}
     */
    @Override
    public boolean isRecordFolderClosed(NodeRef nodeRef)
    {
        return getRecordFolderService().isRecordFolderClosed(nodeRef);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#createRecordFolder(NodeRef, String, QName)}
     */
    @Override
    public NodeRef createRecordFolder(NodeRef parent, String name, QName type)
    {
        return getRecordFolderService().createRecordFolder(parent, name, type);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#createRecordFolder(NodeRef, String, QName, Map)}
     */
    @Override
    public NodeRef createRecordFolder(NodeRef rmContainer, String name, QName type, Map<QName, Serializable> properties)
    {
        return getRecordFolderService().createRecordFolder(rmContainer, name, type, properties);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#createRecordFolder(NodeRef, String)}
     */
    @Override
    public NodeRef createRecordFolder(NodeRef rmContrainer, String name)
    {
        return getRecordFolderService().createRecordFolder(rmContrainer, name);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#createRecordFolder(NodeRef, String, Map)}
     */
    @Override
    public NodeRef createRecordFolder(NodeRef parent, String name,  Map<QName, Serializable> properties)
    {
        return getRecordFolderService().createRecordFolder(parent, name, properties);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordService#getRecords(NodeRef)}
     */
    @Override
    public List<NodeRef> getRecords(NodeRef recordFolder)
    {
        return getRecordService().getRecords(recordFolder);
    }

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#getRecordFolders(NodeRef)}
     */
    @Override
    public List<NodeRef> getRecordFolders(NodeRef record)
    {
        return getRecordFolderService().getRecordFolders(record);
    }

    /**
     * @deprecated As of 2.1, see {@link RecordService#getRecordMetaDataAspects()}
     */
    @Override
    public Set<QName> getRecordMetaDataAspects()
    {
        return getRecordService().getRecordMetaDataAspects();
    }

    /**
     * @deprecated As of 2.1, see {@link RecordService#isDeclared(NodeRef)}
     */
    @Override
    public boolean isRecordDeclared(NodeRef nodeRef)
    {
        return getRecordService().isDeclared(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FreezeService#isHold(NodeRef)}
     */
    @Override
    public boolean isHold(NodeRef nodeRef)
    {
        return getFreezeService().isHold(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FreezeService#isFrozen(NodeRef)}
     */
    @Override
    public boolean isFrozen(NodeRef nodeRef)
    {
        return getFreezeService().isFrozen(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link FreezeService#hasFrozenChildren(NodeRef)}
     */
    @Override
    public boolean hasFrozenChildren(NodeRef nodeRef)
    {
        return getFreezeService().hasFrozenChildren(nodeRef);
    }

    /**
     * @deprecated As of 2.1, see {@link RecordService#isRecord(NodeRef)}
     */
    @Override
    public boolean isRecord(NodeRef nodeRef)
    {
        return getRecordService().isRecord(nodeRef);
    }
}
