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

package org.alfresco.module.org_alfresco_module_rm.test.legacy.jscript;

import java.io.Serializable;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.repo.jscript.app.JSONConversionComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Roy Wetherall
 */
public class JSONConversionComponentTest extends BaseRMTestCase
{
    protected JSONConversionComponent converter;
    protected NodeRef record;

    @Override
    protected void initServices()
    {
        super.initServices();
        converter = (JSONConversionComponent) applicationContext.getBean("jsonConversionComponent");
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
            //System.out.println(json);
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
