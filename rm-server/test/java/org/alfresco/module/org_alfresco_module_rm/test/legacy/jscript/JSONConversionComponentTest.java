/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_rm.test.legacy.jscript;

import static com.google.common.collect.Sets.newHashSet;
import static org.alfresco.module.org_alfresco_module_rm.classification.ClassificationLevelManager.UNCLASSIFIED_ID;
import static org.alfresco.module.org_alfresco_module_rm.jscript.app.JSONConversionComponent.IS_CLASSIFIED;
import static org.alfresco.util.GUID.generate;

import java.io.Serializable;

import org.alfresco.module.org_alfresco_module_rm.classification.ContentClassificationService;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.jscript.app.JSONConversionComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.ArrayUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Roy Wetherall
 */
public class JSONConversionComponentTest extends BaseRMTestCase
{
    private static final String LEVEL1 = "level1";
    private static final String REASON1 = "Test Reason 1";

    private JSONConversionComponent converter;
    private ContentClassificationService contentClassificationService;

    private NodeRef record;

    @Override
    protected void initServices()
    {
        super.initServices();
        converter = (JSONConversionComponent) applicationContext.getBean("jsonConversionComponent");
        contentClassificationService = (ContentClassificationService) applicationContext.getBean("ContentClassificationService");
    }

    @Override
    protected void setupTestDataImpl()
    {
        super.setupTestDataImpl();

        // Create records
        record = utils.createRecord(rmFolder, "testRecord.txt");
    }

    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }

    /**
     * Given a record exists in the RM site
     * When I classify the record with a level not equal to "Unclassified" and convert it
     * Then the result should include an attribute "isClassified" with the value "true"
     */
    public void testClassifyRecord_classified()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef record;
            private String jsonString;

            @Override
            public void given() throws Exception
            {
                NodeRef category = filePlanService.createRecordCategory(filePlan, generate());
                NodeRef folder = recordFolderService.createRecordFolder(category, generate());
                record = utils.createRecord(folder, generate());
            }

            @Override
            public void when() throws Exception
            {
                contentClassificationService.classifyContent(LEVEL1, generate(), newHashSet(REASON1), record);
                jsonString = converter.toJSON(record, true);
            }

            @Override
            public void then() throws Exception
            {
                 assertNotNull(jsonString);
                 JSONObject jsonObject = new JSONObject(jsonString);
                 Object isClassifiedObject = jsonObject.get(IS_CLASSIFIED);
                 assertNotNull(isClassifiedObject);
                 assertTrue(((Boolean) isClassifiedObject).booleanValue());
            }
        });
    }

    /**
     * Given a record exists in the RM site
     * When I classify the record with the level "Unclassified" and convert it
     * Then the result should include an attribute "isClassified" with the value "false"
     */
    public void testClassifyRecord_unclassified()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef record;
            private String jsonString;

            @Override
            public void given() throws Exception
            {
                NodeRef category = filePlanService.createRecordCategory(filePlan, generate());
                NodeRef folder = recordFolderService.createRecordFolder(category, generate());
                record = utils.createRecord(folder, generate());
            }

            @Override
            public void when() throws Exception
            {
                contentClassificationService.classifyContent(UNCLASSIFIED_ID, generate(), newHashSet(REASON1), record);
                jsonString = converter.toJSON(record, true);
            }

            @Override
            public void then() throws Exception
            {
                 assertNotNull(jsonString);
                 JSONObject jsonObject = new JSONObject(jsonString);
                 Object isClassifiedObject = jsonObject.get(IS_CLASSIFIED);
                 assertNotNull(isClassifiedObject);
                 assertFalse(((Boolean) isClassifiedObject).booleanValue());
            }
        });
    }

    /**
     * Given a record exists in the RM site
     * When I classify the record with the level "Unclassified" and convert it
     * Then the result should include an attribute "isClassified" with the value "false"
     */
    public void testClassifyRecord_notclassified()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef record;
            private String jsonString;

            @Override
            public void given() throws Exception
            {
                NodeRef category = filePlanService.createRecordCategory(filePlan, generate());
                NodeRef folder = recordFolderService.createRecordFolder(category, generate());
                record = utils.createRecord(folder, generate());
            }

            @Override
            public void when() throws Exception
            {
                jsonString = converter.toJSON(record, true);
            }

            @Override
            public void then() throws Exception
            {
                assertNotNull(jsonString);
                JSONObject jsonObject = new JSONObject(jsonString);
                Object isClassifiedObject = jsonObject.get(IS_CLASSIFIED);
                assertNotNull(isClassifiedObject);
                assertFalse(((Boolean) isClassifiedObject).booleanValue());
            }
        });
    }

    /**
     * Given a file exists in a collaboration site
     * When I classify the file with a level not equal to "Unclassified" and convert it
     * Then the result should include an attribute "isClassified" with the value "true"
     */
    public void testClassifyFile_classified()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef file;
            private String jsonString;

            @Override
            public void given() throws Exception
            {
                file = fileFolderService.create(documentLibrary, generate(), TYPE_CONTENT).getNodeRef();
            }

            @Override
            public void when() throws Exception
            {
                contentClassificationService.classifyContent(LEVEL1, generate(), newHashSet(REASON1), file);
                jsonString = converter.toJSON(file, true);
            }

            @Override
            public void then() throws Exception
            {
                 assertNotNull(jsonString);
                 JSONObject jsonObject = new JSONObject(jsonString);
                 Object isClassifiedObject = jsonObject.get(IS_CLASSIFIED);
                 assertNotNull(isClassifiedObject);
                 assertTrue(((Boolean) isClassifiedObject).booleanValue());
            }
        });
    }

    /**
     * Given a file exists in a collaboration site
     * When I classify the file with the level "Unclassified" and convert it
     * Then the result should include an attribute "isClassified" with the value "false"
     */
    public void testClassifyFile_unclassified()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef file;
            private String jsonString;

            @Override
            public void given() throws Exception
            {
                file = fileFolderService.create(documentLibrary, generate(), TYPE_CONTENT).getNodeRef();
            }

            @Override
            public void when() throws Exception
            {
                contentClassificationService.classifyContent(UNCLASSIFIED_ID, generate(), newHashSet(REASON1), file);
                jsonString = converter.toJSON(file, true);
            }

            @Override
            public void then() throws Exception
            {
                 assertNotNull(jsonString);
                 JSONObject jsonObject = new JSONObject(jsonString);
                 Object isClassifiedObject = jsonObject.get(IS_CLASSIFIED);
                 assertNotNull(isClassifiedObject);
                 assertFalse(((Boolean) isClassifiedObject).booleanValue());
            }
        });
    }

    /**
     * Given a file exists in a collaboration site
     * When I classify the file with the level "Unclassified" and convert it
     * Then the result should include an attribute "isClassified" with the value "false"
     */
    public void testClassifyFile_notclassified()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private NodeRef file;
            private String jsonString;

            @Override
            public void given() throws Exception
            {
                NodeRef category = filePlanService.createRecordCategory(filePlan, generate());
                NodeRef folder = recordFolderService.createRecordFolder(category, generate());
                file = utils.createRecord(folder, generate());
            }

            @Override
            public void when() throws Exception
            {
                jsonString = converter.toJSON(file, true);
            }

            @Override
            public void then() throws Exception
            {
                assertNotNull(jsonString);
                JSONObject jsonObject = new JSONObject(jsonString);
                Object isClassifiedObject = jsonObject.get(IS_CLASSIFIED);
                assertNotNull(isClassifiedObject);
                assertFalse(((Boolean) isClassifiedObject).booleanValue());
            }
        });
    }

    public void testJSON() throws Exception
    {
        doTestInTransaction(new JSONTest
        (
           filePlan,
           new String[]{"isRmNode", "true", "boolean"},
           new String[]{"rmNode.kind", "FILE_PLAN"}
        ){});

        doTestInTransaction(new JSONTest
        (
           rmContainer,
           new String[]{"isRmNode", "true", "boolean"},
           new String[]{"rmNode.kind", "RECORD_CATEGORY"}
        ){});

        doTestInTransaction(new JSONTest
        (
           rmFolder,
           new String[]{"isRmNode", "true", "boolean"},
           new String[]{"rmNode.kind", "RECORD_FOLDER"}
        ){});

        doTestInTransaction(new JSONTest
        (
           record,
           new String[]{"isRmNode", "true", "boolean"},
           new String[]{"rmNode.kind", "RECORD"}
        ){});
    }

    class JSONTest extends Test<JSONObject>
    {
        private NodeRef nodeRef;
        private String[][] testValues;

        public JSONTest(NodeRef nodeRef, String[] ... testValues)
        {
            this.nodeRef = nodeRef;
            this.testValues = testValues;
        }

        @Override
        public JSONObject run() throws Exception
        {
            String json = converter.toJSON(nodeRef, true);
            System.out.println(json);
            return new JSONObject(json);
        }

        @Override
        public void test(JSONObject result) throws Exception
        {
            for (String[] testValue : testValues)
            {
                String key = testValue[0];
                String type = "string";
                if (testValue.length == 3)
                {
                    type = testValue[2];
                }
                Serializable value = convertValue(testValue[1], type);
                Serializable actualValue = (Serializable)getValue(result, key);

                assertEquals("The key " + key + " did not have the expected value.", value, actualValue);
            }
        }

        private Serializable convertValue(String stringValue, String type)
        {
            Serializable value = stringValue;
            if (type.equals("boolean"))
            {
                value = new Boolean(stringValue);
            }
            return value;
        }

        private Object getValue(JSONObject jsonObject, String key) throws JSONException
        {
            return getValue(jsonObject, key.split("\\."));
        }

        private Object getValue(JSONObject jsonObject, String[] key) throws JSONException
        {
            if (key.length == 1)
            {
                return jsonObject.get(key[0]);
            }
            else
            {
                return getValue(jsonObject.getJSONObject(key[0]),
                                (String[])ArrayUtils.subarray(key, 1, key.length));
            }
        }
    }
}
