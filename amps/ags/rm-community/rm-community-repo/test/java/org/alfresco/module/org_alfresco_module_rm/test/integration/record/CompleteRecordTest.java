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

package org.alfresco.module.org_alfresco_module_rm.test.integration.record;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionResult;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementCustomModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordServiceImpl;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Complete record tests.
 *
 * @author Roy Wetherall
 * @since 2.2.1
 */
public class CompleteRecordTest extends BaseRMTestCase
{
    private static final QName ASPECT_TEST = QName.createQName("http://www.alfresco.org/model/rmtest/1.0", "recordMetaDataWithProperty");
    private static final QName PROP_TEST = QName.createQName("http://www.alfresco.org/model/rmtest/1.0", "customMandatoryProperty");
    private static final QName CUSTOM_ELECTRONIC_TEST = QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "rmarecordCustomProperties");
    private static final QName CUSTOM_NON_ELECTRONIC_TEST = QName.createQName(RecordsManagementCustomModel.RM_CUSTOM_URI, "rmanonElectronicDocumentCustomProperties");
    private static final boolean MANDATORY_METADATA = true;
    private static final boolean OPTIONAL_METADATA = false;

    /**
     * Record service impl
     */
    private RecordServiceImpl recordServiceImpl;

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#initServices()
     */
    @Override
    protected void initServices()
    {
        super.initServices();

        // get the record service
        recordServiceImpl = (RecordServiceImpl) applicationContext.getBean("recordService");
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase#tearDownImpl()
     */
    @Override
    protected void tearDownImpl()
    {
        super.tearDownImpl();

        // ensure action is returned to original state
        recordServiceImpl.setCheckMandatoryPropertiesEnabled(true);
    }

    /**
     * Given the the application is configured to check for mandatory values before complete
     * And a filed record is missing mandatory values
     * When I try to complete the record
     * Then the missing properties parameter of the action will be populated
     * And the record will not be complete
     */
    public void testCheckForMandatoryValuesMissing() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef record;
            private RecordsManagementActionResult result;

            public void given()
            {
                // enable mandatory parameter check
                recordServiceImpl.setCheckMandatoryPropertiesEnabled(true);

                // create a record
                record = utils.createRecord(rmFolder, "record.txt", "title");

                // add the record aspect (that has a mandatory property)
                nodeService.addAspect(record, ASPECT_TEST, null);
            }

            public void when()
            {
                // complete record
                result = rmActionService.executeRecordsManagementAction(record, "declareRecord");
            }

            public void then()
            {
                assertNotNull(result);
                assertNotNull(result.getValue());
                assertFalse(nodeService.hasAspect(record, ASPECT_DECLARED_RECORD));
            }
        });
    }

    /**
     * Given the the application is configured to check for mandatory values before complete
     * And a filed record has all mandatory values
     * When I try to complete the record
     * Then the record is completed
     */
    public void testCheckForMandatoryValuePresent() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef record;
            private RecordsManagementActionResult result;

            public void given()
            {
                // enable mandatory parameter check
                recordServiceImpl.setCheckMandatoryPropertiesEnabled(true);

                // create a record
                record = utils.createRecord(rmFolder, "record.txt", "title");

                // add the record aspect (that has a mandatory property)
                Map<QName, Serializable> properties = new HashMap<>(1);
                properties.put(PROP_TEST, "something");
                nodeService.addAspect(record, ASPECT_TEST, properties);
            }

            public void when()
            {
                // complete record
                result = rmActionService.executeRecordsManagementAction(record, "declareRecord");
            }

            public void then()
            {
                assertNotNull(result);
                assertNull(result.getValue());
                assertTrue(nodeService.hasAspect(record, ASPECT_DECLARED_RECORD));
            }
        });
    }

    /**
     * Given the the application is configured to check for mandatory values before complete
     * And a filed record is missing custom mandatory values
     * When I try to complete the record
     * Then the missing properties parameter of the action will be populated
     * And the record will not be complete
     */
    public void testCheckForCustomMandatoryValuesMissing() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef record;
            private RecordsManagementActionResult result;

            public void given() throws Exception
            {
                // enable mandatory parameter check
                recordServiceImpl.setCheckMandatoryPropertiesEnabled(true);

                // create the custom metadata definition (that has a mandatory property) for electronic records
                defineCustomMetadata(CUSTOM_ELECTRONIC_TEST, ASPECT_RECORD, MANDATORY_METADATA);

                // create an electronic record
                record = utils.createRecord(rmFolder, "electronicRecord.txt", "title");
            }

            public void when()
            {
                // complete record
                result = rmActionService.executeRecordsManagementAction(record, "declareRecord");
            }

            public void then()
            {
                assertNotNull(result);
                assertNotNull(result.getValue());
                assertFalse(nodeService.hasAspect(record, ASPECT_DECLARED_RECORD));
            }

            public void after()
            {
                // remove the custom metadata definition
                removeCustomMetadata(CUSTOM_ELECTRONIC_TEST);
            }
        });
    }

    /**
     * Given the the application is configured to check for mandatory values before complete
     * And a filed record has all custom mandatory values
     * When I try to complete the record
     * Then the record is completed
     */
    public void testCheckForCustomMandatoryValuePresent() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef record;
            private RecordsManagementActionResult result;

            public void given() throws Exception
            {
                // enable mandatory parameter check
                recordServiceImpl.setCheckMandatoryPropertiesEnabled(true);

                // define the custom metadata definition (that has a mandatory property)
                defineCustomMetadata(CUSTOM_ELECTRONIC_TEST, ASPECT_RECORD, MANDATORY_METADATA);

                // create a record
                record = utils.createRecord(rmFolder, "customrecord.txt", "title");

                // populate the custom metadata mandatory property for the record
                populateCustomMetadata(record, CUSTOM_ELECTRONIC_TEST);
            }

            public void when()
            {
                // complete record
                result = rmActionService.executeRecordsManagementAction(record, "declareRecord");
            }

            public void then()
            {
                assertNotNull(result);
                assertNull(result.getValue());
                assertTrue(nodeService.hasAspect(record, ASPECT_DECLARED_RECORD));
            }

            public void after()
            {
                // remove the custom metadata definition
                removeCustomMetadata(CUSTOM_ELECTRONIC_TEST);
            }
        });
    }

    /**
     * Given the the application is configured to check for mandatory values before complete
     * And a non-electronic record is missing custom mandatory values
     * When I try to complete the record
     * Then the missing properties parameter of the action will be populated
     * And the record will not be complete
     */
    public void testCheckForCustomMandatoryValuesMissingInNonElectronicRecord() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef nonElectronicRecord;
            private RecordsManagementActionResult result;

            public void given() throws Exception
            {
                // enable mandatory parameter check
                recordServiceImpl.setCheckMandatoryPropertiesEnabled(true);

                // create the custom metadata definition (that has a mandatory property) for non-electronic records
                defineCustomMetadata(CUSTOM_NON_ELECTRONIC_TEST, TYPE_NON_ELECTRONIC_DOCUMENT, MANDATORY_METADATA);

                // create a non-electronic record
                nonElectronicRecord = utils.createNonElectronicRecord(rmFolder, "non-electronicRecord.txt", "title");
            }

            public void when()
            {
                // complete non-electronic record
                result = rmActionService.executeRecordsManagementAction(nonElectronicRecord,
                        "declareRecord");
            }

            public void then()
            {
                assertNotNull(result);
                assertNotNull(result.getValue());
                assertFalse(nodeService.hasAspect(nonElectronicRecord, ASPECT_DECLARED_RECORD));
            }

            public void after()
            {
                // remove the custom metadata definition
                removeCustomMetadata(CUSTOM_NON_ELECTRONIC_TEST);
            }
        });
    }

    /**
     * Given the the application is configured to check for mandatory values before complete
     * And a non-electronic record has all custom mandatory values
     * When I try to complete the record
     * Then the record is completed
     */
    public void testCheckForCustomMandatoryValuePresentInNonElectronicRecord() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef nonElectronicRecord;
            private RecordsManagementActionResult result;

            public void given() throws Exception
            {
                // enable mandatory parameter check
                recordServiceImpl.setCheckMandatoryPropertiesEnabled(true);

                // create the custom metadata definition (that has a mandatory property)
                defineCustomMetadata(CUSTOM_NON_ELECTRONIC_TEST, TYPE_NON_ELECTRONIC_DOCUMENT, MANDATORY_METADATA);

                // create a non-electronic record
                nonElectronicRecord = utils.createNonElectronicRecord(rmFolder, "non-electronicRecord.txt", "title");

                // populate the custom metadata mandatory property for the record
                populateCustomMetadata(nonElectronicRecord, CUSTOM_NON_ELECTRONIC_TEST);
            }

            public void when()
            {
                // complete record
                result = rmActionService.executeRecordsManagementAction(nonElectronicRecord, "declareRecord");
            }

            public void then()
            {
                assertNotNull(result);
                assertNull(result.getValue());
                assertTrue(nodeService.hasAspect(nonElectronicRecord, ASPECT_DECLARED_RECORD));
            }

            public void after()
            {
                // remove the custom metadata definition
                removeCustomMetadata(CUSTOM_NON_ELECTRONIC_TEST);
            }
        });
    }

    /**
     * Given the the application is configured to check for mandatory values before complete
     * And a filed record is missing custom non-mandatory values
     * When I try to complete the record
     * Then the record is completed
     */
    public void testCheckForCustomOptionalValuesMissing() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef record;
            private RecordsManagementActionResult result;

            public void given() throws Exception
            {
                // enable mandatory parameter check
                recordServiceImpl.setCheckMandatoryPropertiesEnabled(true);

                // create the custom metadata definition (that does not have a mandatory property)
                defineCustomMetadata(CUSTOM_ELECTRONIC_TEST, ASPECT_RECORD, OPTIONAL_METADATA);

                // create a record
                record = utils.createRecord(rmFolder, "customrecord.txt", "title");
            }

            public void when()
            {
                // complete record
                result = rmActionService.executeRecordsManagementAction(record, "declareRecord");
            }

            public void then()
            {
                assertNotNull(result);
                assertNull(result.getValue());
                assertTrue(nodeService.hasAspect(record, ASPECT_DECLARED_RECORD));
            }

            public void after()
            {
                // remove the custom metadata definition
                removeCustomMetadata(CUSTOM_ELECTRONIC_TEST);
            }
        });
    }

    /**
     * Given the the application is configured to check for mandatory values before complete
     * And custom mandatory metadata is required for electronic records
     * And a non-electronic record has no custom mandatory values
     * When I try to complete the non-electronic record
     * Then the record is completed
     */
    public void testElectronicRecordCustomMandatoryNotAppliedToNonElectronicRecord() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef nonElectronicRecord;
            private RecordsManagementActionResult result;

            public void given() throws Exception
            {
                // enable mandatory parameter check
                recordServiceImpl.setCheckMandatoryPropertiesEnabled(true);

                // create the record custom metadata definition (that has a mandatory property)
                defineCustomMetadata(CUSTOM_ELECTRONIC_TEST, ASPECT_RECORD, MANDATORY_METADATA);

                // create a non-electronic record
                nonElectronicRecord = utils.createNonElectronicRecord(rmFolder, "non-electronicRecord.txt", "title");
            }

            public void when()
            {
                // complete record
                result = rmActionService.executeRecordsManagementAction(nonElectronicRecord, "declareRecord");
            }

            public void then()
            {
                assertNotNull(result);
                assertNull(result.getValue());
                assertTrue(nodeService.hasAspect(nonElectronicRecord, ASPECT_DECLARED_RECORD));
            }

            public void after()
            {
                // remove the custom metadata definition
                removeCustomMetadata(CUSTOM_ELECTRONIC_TEST);
            }
        });
    }

    /**
     * Given the the application is configured to check for mandatory values before complete
     * And custom mandatory metadata is required for non-electronic records
     * And an electronic record has no custom mandatory values
     * When I try to complete the electronic record
     * Then the record is completed
     */
    public void testNonElectronicRecordCustomMandatoryNotAppliedToElectronicRecord() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef record;
            private RecordsManagementActionResult result;

            public void given() throws Exception
            {
                // enable mandatory parameter check
                recordServiceImpl.setCheckMandatoryPropertiesEnabled(true);

                // create the non-electronic record custom metadata definition (that has a mandatory property)
                defineCustomMetadata(CUSTOM_NON_ELECTRONIC_TEST, TYPE_NON_ELECTRONIC_DOCUMENT, MANDATORY_METADATA);

                // create a electronic record
                record = utils.createRecord(rmFolder, "electronicRecord.txt", "title");
            }

            public void when()
            {
                // complete record
                result = rmActionService.executeRecordsManagementAction(record, "declareRecord");
            }

            public void then()
            {
                assertNotNull(result);
                assertNull(result.getValue());
                assertTrue(nodeService.hasAspect(record, ASPECT_DECLARED_RECORD));
            }

            public void after()
            {
                // remove the custom metadata definition
                removeCustomMetadata(CUSTOM_NON_ELECTRONIC_TEST);
            }
        });
    }

    /**
     * Given the the application is configured not to check for mandatory values before complete
     * And a filed record is missing mandatory values
     * When I try to complete the record
     * Then the record is completed
     */
    public void testDontCheckForMandatoryValuesMissing() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef record;
            private RecordsManagementActionResult result;

            public void given()
            {
                // disable mandatory parameter check
                recordServiceImpl.setCheckMandatoryPropertiesEnabled(false);

                // create a record
                record = utils.createRecord(rmFolder, "record.txt", "title");

                // add the record aspect (that has a mandatory property)
                nodeService.addAspect(record, ASPECT_TEST, null);
            }

            public void when()
            {
                // complete record
                result = rmActionService.executeRecordsManagementAction(record, "declareRecord");
            }

            public void then()
            {
                assertNotNull(result);
                assertNull(result.getValue());
                assertTrue(nodeService.hasAspect(record, ASPECT_DECLARED_RECORD));
            }
        });
    }

    /**
     * Given the the application is configured to not to check for mandatory values before complete
     * And a filed record has all mandatory values
     * When I try to complete the record
     * Then the record is completed
     */
    public void testDontCheckForMandatoryValuePresent() throws Exception
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef record;
            private RecordsManagementActionResult result;

            public void given()
            {
                // enable mandatory parameter check
                recordServiceImpl.setCheckMandatoryPropertiesEnabled(false);

                // create a record
                record = utils.createRecord(rmFolder, "record.txt", "title");

                // add the record aspect (that has a mandatory property)
                Map<QName, Serializable> properties = new HashMap<>(1);
                properties.put(PROP_TEST, "something");
                nodeService.addAspect(record, ASPECT_TEST, properties);
            }

            public void when()
            {
                // complete record
                result = rmActionService.executeRecordsManagementAction(record, "declareRecord");
            }

            public void then()
            {
                assertNotNull(result);
                assertNull(result.getValue());
                assertTrue(nodeService.hasAspect(record, ASPECT_DECLARED_RECORD));
            }
        });
    }

    /**
     * Helper method to create a Custom Metadata definition in the RM custom model
     *
     * @param mandatory specifies if metadata is mandatory
     */
    private void defineCustomMetadata(QName propId, QName typeName, boolean mandatory) throws Exception
    {
        rmAdminService.addCustomPropertyDefinition(
                propId,
                typeName,
                "SomeCustomDefLabel",
                DataTypeDefinition.TEXT,
                null,
                null,
                null,
                false,
                mandatory,
                false,
                null
                                                  );
    }

    /**
     * Helper method to populate the Custom Metadata for the record
     */
    private void populateCustomMetadata(NodeRef record, QName propId)
    {
        Map<QName, Serializable> properties = new HashMap<>(1);
        properties.put(propId, "SomeCustomValue");
        nodeService.addAspect(record, propId, properties);
    }

    /**
     * Helper method to remove the Custom Metadata definition in the RM custom model
     */
    private void removeCustomMetadata(QName propId)
    {
        rmAdminService.removeCustomPropertyDefinition(propId);
    }
}
