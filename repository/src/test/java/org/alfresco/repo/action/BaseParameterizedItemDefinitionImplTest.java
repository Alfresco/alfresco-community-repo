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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 */
public abstract class BaseParameterizedItemDefinitionImplTest extends TestCase
{
    protected static final String NAME = "name";
    protected static final String TITLE = "title";
    protected static final String DESCRIPTION = "description";
    protected List<ParameterDefinition> paramDefs = new ArrayList<ParameterDefinition>();
    protected List<ParameterDefinition> duplicateParamDefs = new ArrayList<ParameterDefinition>();

    private static final String PARAM1_DISPLAYNAME = "param1-displayname";
    private static final String PARAM1_NAME = "param1-name";
    private static final QName PARAM1_TYPE = DataTypeDefinition.TEXT;
    private static final QName PARAM2_TYPE = DataTypeDefinition.TEXT;
    private static final String PARAM2_DISPLAYNAME = "param2-displaname";
    private static final String PARAM2_NAME = "param2-name";

    @Override
    protected void setUp() throws Exception
    {
        // Create param def lists
        this.paramDefs.add(new ParameterDefinitionImpl(PARAM1_NAME, PARAM1_TYPE, false, PARAM1_DISPLAYNAME));
        this.paramDefs.add(new ParameterDefinitionImpl(PARAM2_NAME, PARAM2_TYPE, false, PARAM2_DISPLAYNAME));
        this.duplicateParamDefs.add(new ParameterDefinitionImpl(PARAM1_NAME, PARAM1_TYPE, false, PARAM1_DISPLAYNAME));
        this.duplicateParamDefs.add(new ParameterDefinitionImpl(PARAM1_NAME, PARAM1_TYPE, false, PARAM1_DISPLAYNAME));
    }

    public void testConstructor()
    {
        create();
    }

    protected abstract ParameterizedItemDefinitionImpl create();

    public void testGetName()
    {
        ParameterizedItemDefinitionImpl temp = create();
        assertEquals(NAME, temp.getName());
    }

    public void testGetParameterDefintions()
    {
        ParameterizedItemDefinitionImpl temp = create();
        List<ParameterDefinition> params = temp.getParameterDefinitions();
        assertNotNull(params);
        assertEquals(2, params.size());
        int i = 0;
        for (ParameterDefinition definition : params)
        {
            if (i == 0)
            {
                assertEquals(PARAM1_NAME, definition.getName());
                assertEquals(PARAM1_TYPE, definition.getType());
                assertEquals(PARAM1_DISPLAYNAME, definition.getDisplayLabel());
            }
            else
            {
                assertEquals(PARAM2_NAME, definition.getName());
                assertEquals(PARAM2_TYPE, definition.getType());
                assertEquals(PARAM2_DISPLAYNAME, definition.getDisplayLabel());
            }
            i++;
        }
    }

    public void testGetParameterDefinition()
    {
        ParameterizedItemDefinitionImpl temp = create();
        ParameterDefinition definition = temp.getParameterDefintion(PARAM1_NAME);
        assertNotNull(definition);
        assertEquals(PARAM1_NAME, definition.getName());
        assertEquals(PARAM1_TYPE, definition.getType());
        assertEquals(PARAM1_DISPLAYNAME, definition.getDisplayLabel());

        ParameterDefinition nullDef = temp.getParameterDefintion("bobbins");
        assertNull(nullDef);
    }
}
