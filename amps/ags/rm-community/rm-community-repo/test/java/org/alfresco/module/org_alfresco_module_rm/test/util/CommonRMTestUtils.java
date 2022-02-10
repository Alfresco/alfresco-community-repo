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

package org.alfresco.module.org_alfresco_module_rm.test.util;

import static org.alfresco.util.GUID.generate;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CompleteEventAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.CutOffAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.DestroyAction;
import org.alfresco.module.org_alfresco_module_rm.action.impl.TransferAction;
import org.alfresco.module.org_alfresco_module_rm.capability.Capability;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.model.security.ModelSecurityService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.module.org_alfresco_module_rm.role.FilePlanRoleService;
import org.alfresco.module.org_alfresco_module_rm.role.Role;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

/**
 * Common RM test utility methods.
 *
 * @author Roy Wetherall
 */
public class CommonRMTestUtils implements RecordsManagementModel
{
    /**
     * test values
     */
    public static final String DEFAULT_DISPOSITION_AUTHORITY = "disposition authority";
    public static final String DEFAULT_DISPOSITION_INSTRUCTIONS = "disposition instructions";
    public static final String DEFAULT_DISPOSITION_DESCRIPTION = "disposition action description";
    public static final String DEFAULT_EVENT_NAME = "case_closed";
    public static final String SEPARATION_EVENT_NAME = "separation";
    public static final String PERIOD_NONE = "none|0";
    public static final String PERIOD_IMMEDIATELY = "immediately|0";
    public static final String PERIOD_ONE_DAY = "day|1";
    public static final String PERIOD_FIVE_DAYS = "day|5";
    public static final String PERIOD_TEN_DAYS = "day|10";
    public static final String PERIOD_ONE_WEEK = "week|1";
    public static final String PERIOD_ONE_YEAR = "year|1";
    public static final String PERIOD_THREE_YEARS = "year|3";
    private DispositionService dispositionService;
    private NodeService nodeService;
    private ContentService contentService;
    private RecordsManagementActionService actionService;
    private ModelSecurityService modelSecurityService;
    private FilePlanRoleService filePlanRoleService;
    private CapabilityService capabilityService;
    private RecordService recordService;
    private HoldService holdService;

    /**
     * Constructor
     *
     * @param applicationContext application context
     */
    public CommonRMTestUtils(ApplicationContext applicationContext)
    {
        dispositionService = (DispositionService) applicationContext.getBean("DispositionService");
        nodeService = (NodeService) applicationContext.getBean("NodeService");
        contentService = (ContentService) applicationContext.getBean("ContentService");
        actionService = (RecordsManagementActionService) applicationContext.getBean("RecordsManagementActionService");
        modelSecurityService = (ModelSecurityService) applicationContext.getBean("ModelSecurityService");
        filePlanRoleService = (FilePlanRoleService) applicationContext.getBean("FilePlanRoleService");
        capabilityService = (CapabilityService) applicationContext.getBean("CapabilityService");
        recordService = (RecordService) applicationContext.getBean("RecordService");
        holdService = (HoldService) applicationContext.getBean("HoldService");
    }

    /**
     * Create a disposition schedule
     *
     * @param container record category
     * @return {@link DispositionSchedule}  created disposition schedule node reference
     */
    public DispositionSchedule createBasicDispositionSchedule(NodeRef container)
    {
        return createBasicDispositionSchedule(container, DEFAULT_DISPOSITION_INSTRUCTIONS, DEFAULT_DISPOSITION_AUTHORITY, false, true);
    }

    /**
     * Create test disposition schedule
     */
    public DispositionSchedule createBasicDispositionSchedule(
            NodeRef container,
            String dispositionInstructions,
            String dispositionAuthority,
            boolean isRecordLevel,
            boolean defaultDispositionActions)
    {
        return createDispositionSchedule(container, dispositionInstructions, dispositionAuthority, isRecordLevel, defaultDispositionActions, false);
    }

    /**
     * Create test disposition schedule
     */
    public DispositionSchedule createDispositionSchedule(
            NodeRef container,
            String dispositionInstructions,
            String dispositionAuthority,
            boolean isRecordLevel,
            boolean defaultDispositionActions,
            boolean extendedDispositionSchedule)
    {
        return createDispositionSchedule(
                container,
                dispositionInstructions,
                dispositionAuthority,
                isRecordLevel,
                defaultDispositionActions,
                extendedDispositionSchedule,
                DEFAULT_EVENT_NAME);
    }

    /**
     * Create test disposition schedule
     */
    public DispositionSchedule createDispositionSchedule(
            NodeRef container,
            String dispositionInstructions,
            String dispositionAuthority,
            boolean isRecordLevel,
            boolean defaultDispositionActions,
            boolean extendedDispositionSchedule,
            String defaultEvent)
    {
        Map<QName, Serializable> dsProps = new HashMap<>(3);
        dsProps.put(PROP_DISPOSITION_AUTHORITY, dispositionAuthority);
        dsProps.put(PROP_DISPOSITION_INSTRUCTIONS, dispositionInstructions);
        dsProps.put(PROP_RECORD_LEVEL_DISPOSITION, isRecordLevel);
        DispositionSchedule dispositionSchedule = dispositionService.createDispositionSchedule(container, dsProps);

        if (defaultDispositionActions)
        {
            Map<QName, Serializable> adParams = new HashMap<>(3);
            adParams.put(PROP_DISPOSITION_ACTION_NAME, CutOffAction.NAME);
            adParams.put(PROP_DISPOSITION_DESCRIPTION, DEFAULT_DISPOSITION_DESCRIPTION);

            List<String> events = new ArrayList<>(1);
            events.add(defaultEvent);
            adParams.put(PROP_DISPOSITION_EVENT, (Serializable) events);

            dispositionService.addDispositionActionDefinition(dispositionSchedule, adParams);

            if (extendedDispositionSchedule)
            {
                adParams = new HashMap<>(4);
                adParams.put(PROP_DISPOSITION_ACTION_NAME, TransferAction.NAME);
                adParams.put(PROP_DISPOSITION_DESCRIPTION, DEFAULT_DISPOSITION_DESCRIPTION);
                adParams.put(PROP_DISPOSITION_PERIOD, PERIOD_IMMEDIATELY);
                adParams.put(PROP_DISPOSITION_LOCATION, StringUtils.EMPTY);

                dispositionService.addDispositionActionDefinition(dispositionSchedule, adParams);
            }

            adParams = new HashMap<>(3);
            adParams.put(PROP_DISPOSITION_ACTION_NAME, DestroyAction.NAME);
            adParams.put(PROP_DISPOSITION_DESCRIPTION, DEFAULT_DISPOSITION_DESCRIPTION);
            adParams.put(PROP_DISPOSITION_PERIOD, PERIOD_IMMEDIATELY);

            dispositionService.addDispositionActionDefinition(dispositionSchedule, adParams);
        }

        return dispositionSchedule;
    }

    /**
     * Helper method to create a record in a record folder.
     *
     * @param recordFolder record folder
     * @param name         name of record
     * @return {@link NodeRef}  record node reference
     */
    public NodeRef createRecord(NodeRef recordFolder, String name)
    {
        return createRecord(recordFolder, name, null, "Some test content");
    }

    /**
     * Helper method to create a record in a record folder.
     *
     * @param recordFolder record folder
     * @param name         name of the record
     * @param title        title of the record
     * @return {@link NodeRef}  record node reference
     */
    public NodeRef createRecord(NodeRef recordFolder, String name, String title)
    {
        Map<QName, Serializable> props = new HashMap<>(1);
        props.put(ContentModel.PROP_TITLE, title);
        return createRecord(recordFolder, name, props, "Some test content");
    }

    /**
     * Helper method to create a record in a record folder.
     *
     * @param recordFolder record folder
     * @param name         name of record
     * @param properties   properties of the record
     * @param content      content of the record
     * @return {@link NodeRef}  record node reference
     */
    public NodeRef createRecord(NodeRef recordFolder, String name, Map<QName, Serializable> properties, String content)
    {
        // Create the record
        NodeRef record = createRecordImpl(recordFolder, name, properties);

        // Set the content
        ContentWriter writer = contentService.getWriter(record, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(content);

        return record;
    }

    /**
     * Helper method to create a record in a record folder.
     *
     * @param recordFolder record folder
     * @param name         name of record
     * @param properties   properties of the record
     * @param content      content of the record
     * @return {@link NodeRef}  record node reference
     */
    public NodeRef createRecord(NodeRef recordFolder, String name, Map<QName, Serializable> properties, String mimetype, InputStream content)
    {
        // Create the record
        NodeRef record = createRecordImpl(recordFolder, name, properties);

        // Set the content
        ContentWriter writer = contentService.getWriter(record, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(mimetype);
        writer.setEncoding("UTF-8");
        writer.putContent(content);

        return record;
    }

    /**
     * Helper to consolidate creation of contentless record
     */
    private NodeRef createRecordImpl(NodeRef recordFolder, String name, Map<QName, Serializable> properties)
    {
        // Create the document
        if (properties == null)
        {
            properties = new HashMap<>(1);
        }
        if (!properties.containsKey(ContentModel.PROP_NAME))
        {
            properties.put(ContentModel.PROP_NAME, name);
        }
        return nodeService.createNode(recordFolder,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                ContentModel.TYPE_CONTENT,
                properties).getChildRef();
    }

    /**
     * Helper method to create a non-electronic record in a record folder.
     *
     * @param recordFolder record folder
     * @param name         name of the non-electronic record
     * @param title        title of the non-electronic record
     * @return {@link NodeRef}  non-electronic record node reference
     */
    public NodeRef createNonElectronicRecord(NodeRef recordFolder, String name, String title)
    {
        Map<QName, Serializable> props = new HashMap<>(1);
        props.put(ContentModel.PROP_TITLE, title);
        props.put(ContentModel.PROP_NAME, name);

        // Create the document
        NodeRef record = nodeService.createNode(recordFolder,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name),
                RecordsManagementModel.TYPE_NON_ELECTRONIC_DOCUMENT,
                props).getChildRef();
        return record;
    }

    /**
     * Helper method to complete record.
     */
    public void completeRecord(final NodeRef record)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                modelSecurityService.setEnabled(false);
                try
                {
                    nodeService.setProperty(record, RecordsManagementModel.PROP_DATE_FILED, new Date());
                    nodeService.setProperty(record, ContentModel.PROP_TITLE, "titleValue");
                    actionService.executeRecordsManagementAction(record, "declareRecord");
                }
                finally
                {
                    modelSecurityService.setEnabled(true);
                }

                return null;
            }

        }, AuthenticationUtil.getAdminUserName());

    }

    public void closeFolder(final NodeRef recordFolder)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                modelSecurityService.setEnabled(false);
                try
                {
                    actionService.executeRecordsManagementAction(recordFolder, "closeRecordFolder");
                }
                finally
                {
                    modelSecurityService.setEnabled(true);
                }
                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
    }

    public Role createRole(NodeRef filePlan, String roleName, String... capabilityNames)
    {
        Set<Capability> capabilities = new HashSet<>(capabilityNames.length);
        for (String name : capabilityNames)
        {
            Capability capability = capabilityService.getCapability(name);
            if (capability == null)
            {
                throw new AlfrescoRuntimeException("capability " + name + " not found.");
            }
            capabilities.add(capability);
        }

        return filePlanRoleService.createRole(filePlan, roleName, roleName, capabilities);
    }

    /**
     * Helper method to complete event on disposable item
     *
     * @param disposableItem disposable item (record or record folder)
     * @param eventName      event name
     */
    public void completeEvent(NodeRef disposableItem, String eventName)
    {
        // build action properties
        Map<String, Serializable> params = new HashMap<>(1);
        params.put(CompleteEventAction.PARAM_EVENT_NAME, eventName);

        // complete event
        actionService.executeRecordsManagementAction(disposableItem, CompleteEventAction.NAME, params);
    }

    /**
     * Helper method to create a hold.
     *
     * @param holdName   hold name
     * @param holdReason hold reason
     * @return NodeRef  hold node reference
     */
    public NodeRef createHold(NodeRef filePlan, String holdName, String holdReason)
    {
        return holdService.createHold(filePlan, holdName, holdReason, generate());
    }

    /**
     * Helper method to delete a hold.
     *
     * @param nodeRef hold node reference
     */
    public void deleteHold(NodeRef nodeRef)
    {
        holdService.deleteHold(nodeRef);
    }

    /**
     * Util method to add content to a hold.
     *
     * @param holdNodeRef    hold node reference
     * @param contentNodeRef content node reference
     */
    public void addItemToHold(NodeRef holdNodeRef, NodeRef contentNodeRef)
    {
        holdService.addToHold(holdNodeRef, contentNodeRef);
    }

    /**
     * Util method to remove content from a hold.
     *
     * @param holdNodeRef    hold node reference
     * @param contentNodeRef content node reference
     */
    public void removeItemFromHold(NodeRef holdNodeRef, NodeRef contentNodeRef)
    {
        holdService.removeFromHold(holdNodeRef, contentNodeRef);
    }

    /**
     * Util method to remove items from holds.
     *
     * @param holdNodeRefs   the list {@link NodeRef}s of the holds
     * @param contentNodeRef the list of items which will be removed from the given holds
     */
    public void removeItemsFromHolds(List<NodeRef> holdNodeRefs, List<NodeRef> contentNodeRef)
    {
        holdService.removeFromHolds(holdNodeRefs, contentNodeRef);
    }

}
