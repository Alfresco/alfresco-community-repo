/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;

/**
 * @author Roy Wetherall
 */
public abstract class BaseParameterizedItemImplTest extends TestCase
{
    protected List<ParameterDefinition> paramDefs = new ArrayList<ParameterDefinition>();
    protected Map<String, Serializable> paramValues = new HashMap<String, Serializable>();

    protected static final String ID = "id";
    protected static final String NAME = "name";
    protected static final String TITLE = "title";
    protected static final String DESCRIPTION = "description";

    private static final String PARAM_1 = "param1";
    private static final String VALUE_1 = "value1";
    private static final String PARAM_2 = "param2";
    private static final String VALUE_2 = "value2";
    private static final String PARAM_DISPLAYLABEL = "displayLabel";

    @Override
    protected void setUp() throws Exception
    {
        // Create param defs
        paramDefs.add(new ParameterDefinitionImpl(PARAM_1, DataTypeDefinition.TEXT, false, PARAM_DISPLAYLABEL));
        paramDefs.add(new ParameterDefinitionImpl(PARAM_2, DataTypeDefinition.TEXT, false, PARAM_DISPLAYLABEL));

        // Create param values
        paramValues.put(PARAM_1, VALUE_1);
        paramValues.put(PARAM_2, VALUE_2);
    }

    public void testConstructor()
    {
        create();
    }

    protected abstract ParameterizedItemImpl create();

    public void testGetParameterValues()
    {
        ParameterizedItemImpl temp = create();
        Map<String, Serializable> tempParamValues = temp.getParameterValues();
        assertNotNull(tempParamValues);
        assertEquals(2, tempParamValues.size());
        for (Map.Entry<String, Serializable> entry : tempParamValues.entrySet())
        {
            if (entry.getKey() == PARAM_1)
            {
                assertEquals(VALUE_1, entry.getValue());
            }
            else if (entry.getKey() == PARAM_2)
            {
                assertEquals(VALUE_2, entry.getValue());
            }
            else
            {
                fail("There is an unexpected entry here.");
            }
        }
    }

    public void testGetParameterValue()
    {
        ParameterizedItemImpl temp = create();
        assertNull(temp.getParameterValue("bobbins"));
        assertEquals(VALUE_1, temp.getParameterValue(PARAM_1));
    }

    public void testSetParameterValue()
    {
        ParameterizedItemImpl temp = create();
        temp.setParameterValue("bobbins", "value");
        assertEquals("value", temp.getParameterValue("bobbins"));
    }

    public void testGetId()
    {
        ParameterizedItemImpl temp = create();
        assertEquals(ID, temp.getId());
    }
}
