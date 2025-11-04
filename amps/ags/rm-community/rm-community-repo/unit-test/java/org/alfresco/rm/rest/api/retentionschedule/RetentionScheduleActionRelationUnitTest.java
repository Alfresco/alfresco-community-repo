/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.rm.rest.api.retentionschedule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinitionImpl;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionServiceImpl;
import org.alfresco.module.org_alfresco_module_rm.disposition.property.DispositionProperty;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseUnitTest;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.impl.ApiNodesModelFactory;
import org.alfresco.rm.rest.api.impl.FilePlanComponentsApiUtils;
import org.alfresco.rm.rest.api.model.RetentionScheduleActionDefinition;
import org.alfresco.rm.rest.api.model.RetentionSteps;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * Unit tests for RetentionScheduleActionRelation
 */
public class RetentionScheduleActionRelationUnitTest extends BaseUnitTest {

    @Mock
    private FilePlanComponentsApiUtils apiUtils;

    @Mock
    private ApiNodesModelFactory nodesModelFactory;

    @Mock
    private Parameters parameters;

    private RetentionScheduleActionRelation retentionScheduleActionRelation;
    private DispositionService dispositionService;

    private NodeRef retentionScheduleRecordLevelNodeRef = new NodeRef("workspace://SpacesStore/recordLevel");
    private NodeRef retentionScheduleRecordFolderLevelNodeRef = new NodeRef("workspace://SpacesStore/recordFolderLevel");

    @Before
    public void setUp() {

        // Disposition Service
        dispositionService = new DispositionServiceImpl();

        // Disposition Properties
        DispositionProperty publicationDate = createDispositionProperty("dod:publicationDate", false, true, Set.of());
        DispositionProperty cutoffDate = createDispositionProperty("rma:cutOffDate", true, true, Set.of("cutoff"));
        DispositionProperty dispositionAsOf = createDispositionProperty("rma:dispositionAsOf", true, true, Set.of());
        DispositionProperty dateFiled = createDispositionProperty("rma:dateFiled", false, true, Set.of());
        DispositionProperty created = createDispositionProperty("cm:created", true, true, Set.of());

        // Register Disposition Properties
        dispositionService.registerDispositionProperty(publicationDate);
        dispositionService.registerDispositionProperty(cutoffDate);
        dispositionService.registerDispositionProperty(dispositionAsOf);
        dispositionService.registerDispositionProperty(dateFiled);
        dispositionService.registerDispositionProperty(created);

        // Retention Schedule Action Relation
        retentionScheduleActionRelation = new RetentionScheduleActionRelation();
        retentionScheduleActionRelation.setApiUtils(apiUtils);
        retentionScheduleActionRelation.setNodeService(mockedNodeService);
        retentionScheduleActionRelation.setNodesModelFactory(nodesModelFactory);
        retentionScheduleActionRelation.setDispositionService(dispositionService);
    }

    /**
     * Create "cutoff" retention step for a retention schedule with "record" level
     * disposition supplying only VALID disposition properties.
     * 
     * <p>
     * Valid: dod:publicationDate, rma:dispositionAsOf, rma:dateField and cm:created
     * </p>
     */
    @Test
    public void testCreate_RetentionScheduleRecordLevel_Cutoff_Valid() throws Exception {

        // Retention schedule with "record" level disposition
        String retentionScheduleId = useRetentionScheduleWithRecordLevel(true, false);

        // Retention step action
        String actionName = RetentionSteps.CUTOFF.stepName;
        RetentionScheduleActionDefinition actionDef;

        // Cutoff - dod:publicationDate
        actionDef = createAction(actionName, "dod:publicationDate");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);

        // Cutoff - rma:dispositionAsOf
        actionDef = createAction(actionName, "rma:dispositionAsOf");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);

        // Cutoff - rma:dateFiled
        actionDef = createAction(actionName, "rma:dateFiled");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);

        // Cutoff - cm:created
        actionDef = createAction(actionName, "cm:created");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);
    }

    /**
     * Create "cutoff" retention step for a retention schedule with "record" level
     * disposition supplying only INVALID disposition properties.
     * 
     * <p>
     * Invalid: rma:cutOffDate
     * </p>
     */
    @Test(expected = InvalidArgumentException.class)
    public void testCreate_RetentionScheduleRecordLevel_Cutoff_Invalid() throws Exception {

        // Retention schedule with "record" level disposition
        String retentionScheduleId = useRetentionScheduleWithRecordLevel(true, false);

        // Retention step action
        String actionName = RetentionSteps.CUTOFF.stepName;
        RetentionScheduleActionDefinition actionDef;

        // Cutoff - rma:cutOffDate
        actionDef = createAction(actionName, "rma:cutOffDate");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);
    }

    /**
     * Create "cutoff" retention step for a retention schedule with "record folder"
     * level disposition supplying only VALID disposition properties.
     * 
     * <p>
     * Valid: rma:dispositionAsOf and cm:created
     * </p>
     */
    @Test
    public void testCreate_RetentionScheduleRecordFolderLevel_Cutoff_Valid() throws Exception {

        // Retention schedule with "record folder" level disposition
        String retentionScheduleId = useRetentionScheduleWithRecordLevel(false, false);

        // Retention step action
        String actionName = RetentionSteps.CUTOFF.stepName;
        RetentionScheduleActionDefinition actionDef;

        // Cutoff - rma:dispositionAsOf
        actionDef = createAction(actionName, "rma:dispositionAsOf");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);

        // Cutoff - cm:created
        actionDef = createAction(actionName, "cm:created");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);
    }

    /**
     * Create "cutoff" retention step for a retention schedule with "record folder"
     * level disposition supplying only INVALID disposition properties.
     * 
     * <p>
     * Invalid: dod:publicationDate, rma:cutOffDate and rma:dateFiled
     * </p>
     */
    @Test(expected = InvalidArgumentException.class)
    public void testCreate_RetentionScheduleRecordFolderLevel_Cutoff_Invalid() throws Exception {

        // Retention schedule with "record folder" level disposition
        String retentionScheduleId = useRetentionScheduleWithRecordLevel(false, false);

        // Retention step action
        String actionName = RetentionSteps.CUTOFF.stepName;
        RetentionScheduleActionDefinition actionDef;

        // Cutoff - dod:publicationDate
        actionDef = createAction(actionName, "dod:publicationDate");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);

        // Cutoff - rma:cutOffDate
        actionDef = createAction(actionName, "rma:cutOffDate");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);

        // Cutoff - rma:dateFiled
        actionDef = createAction(actionName, "rma:dateFiled");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);
    }

    /**
     * Create "transfer" retention step for a retention schedule with "record" level
     * disposition supplying only VALID disposition properties.
     * 
     * <p>
     * Valid: dod:publicationDate, rma:cutOffDate, rma:dispositionAsOf,
     * rma:dateField and cm:created
     * </p>
     */
    @Test
    public void testCreate_RetentionScheduleRecordLevel_Transfer_Valid() throws Exception {

        // Retention schedule with "record" level disposition
        String retentionScheduleId = useRetentionScheduleWithRecordLevel(true, true);

        // Retention step action
        String actionName = RetentionSteps.TRANSFER.stepName;
        RetentionScheduleActionDefinition actionDef;

        // Transfer - dod:publicationDate
        actionDef = createAction(actionName, "dod:publicationDate");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);

        // Transfer - rma:cutOffDate
        actionDef = createAction(actionName, "rma:cutOffDate");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);

        // Transfer - rma:dispositionAsOf
        actionDef = createAction(actionName, "rma:dispositionAsOf");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);

        // Transfer - rma:dateFiled
        actionDef = createAction(actionName, "rma:dateFiled");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);

        // Transfer - cm:created
        actionDef = createAction(actionName, "cm:created");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);
    }

    /**
     * Create "transfer" retention step for a retention schedule with "record" level
     * disposition supplying only INVALID disposition properties.
     * 
     * <p>
     * Invalid: any other property that is not dod:publicationDate, rma:cutOffDate,
     * rma:dispositionAsOf, rma:dateField and cm:created.
     * </p>
     */
    @Test(expected = InvalidArgumentException.class)
    public void testCreate_RetentionScheduleRecordLevel_Transfer_Invalid() throws Exception {

        // Retention schedule with "record" level disposition
        String retentionScheduleId = useRetentionScheduleWithRecordLevel(true, true);

        // Retention step action
        String actionName = RetentionSteps.TRANSFER.stepName;
        RetentionScheduleActionDefinition actionDef;

        // Transfer - bad:property
        actionDef = createAction(actionName, "bad:property");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);
    }

    /**
     * Create "transfer" retention step for a retention schedule with "record
     * folder" level disposition supplying only VALID disposition properties.
     * 
     * <p>
     * Valid: rma:cutOffDate, rma:dispositionAsOf and cm:created
     * </p>
     */
    @Test
    public void testCreate_RetentionScheduleRecordFolderLevel_Transfer_Valid() throws Exception {

        // Retention schedule with "record" level disposition
        String retentionScheduleId = useRetentionScheduleWithRecordLevel(false, true);

        // Retention step action
        String actionName = RetentionSteps.TRANSFER.stepName;
        RetentionScheduleActionDefinition actionDef;

        // Transfer - rma:cutOffDate
        actionDef = createAction(actionName, "rma:cutOffDate");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);

        // Transfer - rma:dispositionAsOf
        actionDef = createAction(actionName, "rma:dispositionAsOf");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);

        // Transfer - cm:created
        actionDef = createAction(actionName, "cm:created");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);
    }

    /**
     * Create "transfer" retention step for a retention schedule with "record
     * folder" level disposition supplying only INVALID disposition properties.
     * 
     * <p>
     * Invalid: dod:publicationDate and rma:dateFiled
     * </p>
     */
    @Test(expected = InvalidArgumentException.class)
    public void testCreate_RetentionScheduleRecordFolderLevel_Transfer_Invalid() throws Exception {

        // Retention schedule with "record" level disposition
        String retentionScheduleId = useRetentionScheduleWithRecordLevel(false, true);

        // Retention step action
        String actionName = RetentionSteps.TRANSFER.stepName;
        RetentionScheduleActionDefinition actionDef;

        // Transfer - dod:publicationDate
        actionDef = createAction(actionName, "dod:publicationDate");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);

        // Transfer - rma:dateFiled
        actionDef = createAction(actionName, "rma:dateFiled");
        retentionScheduleActionRelation.create(retentionScheduleId, Arrays.asList(actionDef), parameters);
    } 

    private String useRetentionScheduleWithRecordLevel(Boolean withRecordLevel, boolean hasCompletedActions) {

        NodeRef retentionScheduleNodeRef = withRecordLevel ? 
                retentionScheduleRecordLevelNodeRef
                : retentionScheduleRecordFolderLevelNodeRef;

        String retentionScheduleId = retentionScheduleNodeRef.getId();

        ChildAssociationRef retentionScheduleAssocRef = mock(ChildAssociationRef.class);

        NodeRef cutOffActionNodeRef = mock(NodeRef.class);
        DispositionActionDefinition cutoffAction = new DispositionActionDefinitionImpl(mockedRecordsManagementEventService, mockedRecordsManagementActionService, mockedNodeService, cutOffActionNodeRef, 0);
        List<DispositionActionDefinition> completedActions = hasCompletedActions ? Arrays.asList(cutoffAction) : Collections.emptyList();

        when(retentionScheduleAssocRef.getChildRef()).thenReturn(new NodeRef("workspace://SpacesStore/123"));

        when(apiUtils.lookupAndValidateNodeType(eq(retentionScheduleId), any(QName.class))).thenReturn(retentionScheduleNodeRef);
        when(mockedNodeService.getProperty(retentionScheduleNodeRef, RecordsManagementModel.PROP_RECORD_LEVEL_DISPOSITION)).thenReturn(withRecordLevel);


        when(nodesModelFactory.getRetentionActions(retentionScheduleNodeRef)).thenReturn(completedActions);
        when(mockedNodeService.createNode(any(), any(), any(), any(), any())).thenReturn(retentionScheduleAssocRef);

        return retentionScheduleId;
    }

    private RetentionScheduleActionDefinition createAction(String name, String periodProperty) {
        RetentionScheduleActionDefinition actionDef = mock(RetentionScheduleActionDefinition.class);
        when(actionDef.getName()).thenReturn(name);
        when(actionDef.getPeriodProperty()).thenReturn(periodProperty);
        when(actionDef.getPeriodAmount()).thenReturn(2);
        when(actionDef.getPeriod()).thenReturn("day");
        when(actionDef.getEvents()).thenReturn(Collections.singletonList("versioned"));
        when(actionDef.isCombineRetentionStepConditions()).thenReturn(false);
        return actionDef;
    }

    private DispositionProperty createDispositionProperty(String name, Boolean appliesToFolderLevel,
            Boolean appliesToRecordLevel, Set<String> excludedActions) {
        when(mockedNamespaceService.getNamespaceURI(any())).thenReturn(name.split(":")[0]);
        DispositionProperty dp = new DispositionProperty();
        dp.setNamespaceService(mockedNamespaceService);
        dp.setName(name);
        dp.setAppliesToRecordLevel(appliesToRecordLevel);
        dp.setAppliesToFolderLevel(appliesToFolderLevel);
        dp.setExcludedDispositionActions(excludedActions);
        return dp;
    }
}