/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
        paramDefs.add(new ParameterDefinitionImpl(PARAM_1, DataTypeDefinition.TEXT, false,  PARAM_DISPLAYLABEL));
        paramDefs.add(new ParameterDefinitionImpl(PARAM_2, DataTypeDefinition.TEXT, false,  PARAM_DISPLAYLABEL));
        
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
        for (Map.Entry entry : tempParamValues.entrySet())
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
