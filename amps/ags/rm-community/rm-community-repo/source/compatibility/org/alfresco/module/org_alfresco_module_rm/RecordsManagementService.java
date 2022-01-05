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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.freeze.FreezeService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.recordfolder.RecordFolderService;
import org.alfresco.module.org_alfresco_module_rm.transfer.TransferService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Records management service interface.
 *
 * Allows simple creation, manipulation and querying of records management components.
 *
 * @author Roy Wetherall
 * @deprecated as of 2.2
 */
public interface RecordsManagementService
{
    /**
     * @deprecated As of 2.1, see {@link FilePlanService#isFilePlan(NodeRef)}
     */
    @Deprecated
    boolean isFilePlanComponent(NodeRef nodeRef);

    /**
     * @since 2.0
     * @deprecated As of 2.1, see {@link FilePlanService#getFilePlanComponentKind(NodeRef)}
     */
    @Deprecated
    FilePlanComponentKind getFilePlanComponentKind(NodeRef nodeRef);

    /**
     * @since 2.0
     * @deprecated As of 2.1, see {@link FilePlanService#getFilePlanComponentKindFromType(QName)}
     */
    @Deprecated
    FilePlanComponentKind getFilePlanComponentKindFromType(QName type);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#isFilePlanContainer(NodeRef)}
     */
    @Deprecated
    boolean isRecordsManagementContainer(NodeRef nodeRef);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#isFilePlan(NodeRef)}
     */
    @Deprecated
    boolean isFilePlan(NodeRef nodeRef);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#isRecordCategory(NodeRef)}
     */
    @Deprecated
    boolean isRecordCategory(NodeRef nodeRef);

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#isRecordFolder(NodeRef)}
     */
    @Deprecated
    boolean isRecordFolder(NodeRef nodeRef);

    /**
     * @since 2.0
     * @deprecated As of 2.2, see {@link TransferService#isTransfer(NodeRef)}
     */
    @Deprecated
    boolean isTransfer(NodeRef nodeRef);

    /**
     * @since 2.0
     * @deprecated As of 2.2, see {@link RecordService#isMetadataStub(NodeRef)}
     */
    @Deprecated
    boolean isMetadataStub(NodeRef nodeRef);

    /**
     * @since 2.0
     * @deprecated As of 2.2, see {@link DispositionService#isDisposableItemCutoff(NodeRef)}
     */
    boolean isCutoff(NodeRef nodeRef);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getNodeRefPath(NodeRef)}
     */
    @Deprecated
    List<NodeRef> getNodeRefPath(NodeRef nodeRef);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getFilePlan(NodeRef)}
     */
    @Deprecated
    NodeRef getFilePlan(NodeRef nodeRef);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getFilePlans()}
     */
    @Deprecated
    List<NodeRef> getFilePlans();

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String, QName)}
     */
    @Deprecated
    NodeRef createFilePlan(NodeRef parent, String name, QName type);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String, QName, Map)}
     */
    @Deprecated
    NodeRef createFilePlan(NodeRef parent, String name, QName type, Map<QName, Serializable> properties);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String)}
     */
    @Deprecated
    NodeRef createFilePlan(NodeRef parent, String name);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createFilePlan(NodeRef, String, Map)}
     */
    @Deprecated
    NodeRef createFilePlan(NodeRef parent, String name, Map<QName, Serializable> properties);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getAllContained(NodeRef, boolean)}
     */
    @Deprecated
    List<NodeRef> getAllContained(NodeRef recordCategory, boolean deep);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getAllContained(NodeRef)}
     */
    @Deprecated
    List<NodeRef> getAllContained(NodeRef recordCategory);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getContainedRecordCategories(NodeRef, boolean)}
     */
    @Deprecated
    List<NodeRef> getContainedRecordCategories(NodeRef recordCategory, boolean deep);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getContainedRecordCategories(NodeRef)}
     */
    @Deprecated
    List<NodeRef> getContainedRecordCategories(NodeRef recordCategory);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getContainedRecordCategories(NodeRef, boolean)}
     */
    @Deprecated
    List<NodeRef> getContainedRecordFolders(NodeRef container, boolean deep);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#getContainedRecordFolders(NodeRef)}
     */
    @Deprecated
    List<NodeRef> getContainedRecordFolders(NodeRef container);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createRecordCategory(NodeRef, String, QName)}
     */
    @Deprecated
    NodeRef createRecordCategory(NodeRef parent, String name, QName type);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createRecordCategory(NodeRef, String, QName, Map)}
     */
    @Deprecated
    NodeRef createRecordCategory(NodeRef parent, String name, QName type, Map<QName, Serializable> properties);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createRecordCategory(NodeRef, String)}
     */
    @Deprecated
    NodeRef createRecordCategory(NodeRef parent, String name);

    /**
     * @deprecated As of 2.1, see {@link FilePlanService#createRecordCategory(NodeRef, String, Map)}
     */
    @Deprecated
    NodeRef createRecordCategory(NodeRef parent, String name, Map<QName, Serializable> properties);

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#isRecordFolderDeclared(NodeRef)}
     */
    @Deprecated
    boolean isRecordFolderDeclared(NodeRef nodeRef);

    /**
     * @since 2.0
     * @deprecated As of 2.2, see {@link RecordFolderService#isRecordFolderClosed(NodeRef)}
     */
    @Deprecated
    boolean isRecordFolderClosed(NodeRef nodeRef);

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#createRecordFolder(NodeRef, String, QName)}
     */
    @Deprecated
    NodeRef createRecordFolder(NodeRef rmContainer, String name, QName type);

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#createRecordFolder(NodeRef, String, QName, Map)}
     */
    @Deprecated
    NodeRef createRecordFolder(NodeRef rmContainer, String name, QName type, Map<QName, Serializable> properties);

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#createRecordFolder(NodeRef, String)}
     */
    @Deprecated
    NodeRef createRecordFolder(NodeRef parent, String name);

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#createRecordFolder(NodeRef, String, Map)
     */
    @Deprecated
    NodeRef createRecordFolder(NodeRef parent, String name, Map<QName, Serializable> properties);

    /**
     * @deprecated As of 2.2, see {@link RecordService#getRecords(NodeRef)}
     */
    @Deprecated
    List<NodeRef> getRecords(NodeRef recordFolder);

    /**
     * @deprecated As of 2.2, see {@link RecordFolderService#getRecordFolders(NodeRef)}
     */
    @Deprecated
    List<NodeRef> getRecordFolders(NodeRef record);

    /**
     * @deprecated As of 2.1, replaced by {@link RecordService#getRecordMetaDataAspects()}
     */
    @Deprecated
    Set<QName> getRecordMetaDataAspects();

    /**
     * @deprecated As of 2.1, replaced by {@link RecordService#isDeclared(NodeRef)}
     */
    @Deprecated
    boolean isRecordDeclared(NodeRef nodeRef);

    /**
     * @since 2.0
     * @deprecated As of 2.1, replaced by {@link FreezeService#isHold(NodeRef)}
     */
    @Deprecated
    boolean isHold(NodeRef nodeRef);

    /**
     * @since 2.0
     * @deprecated As of 2.1, replaced by {@link FreezeService#isFrozen(NodeRef)}
     */
    @Deprecated
    boolean isFrozen(NodeRef nodeRef);

    /**
     * @since 2.0
     * @deprecated As of 2.1, replaced by {@link FreezeService#hasFrozenChildren(NodeRef)}
     */
    @Deprecated
    boolean hasFrozenChildren(NodeRef nodeRef);

    /**
     * @deprecated As of 2.1, replaced by {@link RecordService#isRecord(NodeRef)}
     */
    @Deprecated
    boolean isRecord(NodeRef nodeRef);
}
