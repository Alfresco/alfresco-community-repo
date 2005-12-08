/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.action;

import junit.framework.TestCase;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;

/**
 * Parameter definition implementation unit test.
 * 
 * @author Roy Wetherall
 */
public class ParameterDefinitionImplTest extends TestCase
{
    private static final String NAME = "param-name";
    private static final String DISPLAY_LABEL = "The display label.";
    
    public void testConstructor()
    {
        create();
    }
   
    private ParameterDefinitionImpl create()
    {
        ParameterDefinitionImpl paramDef = new ParameterDefinitionImpl(
                NAME,
                DataTypeDefinition.TEXT,
                true,
                DISPLAY_LABEL);
        assertNotNull(paramDef);
        return paramDef;
    }
    
    public void testGetName()
    {
        ParameterDefinitionImpl temp = create();
        assertEquals(NAME, temp.getName());
    }
    
    public void testGetClass()
    {
        ParameterDefinitionImpl temp = create();
        assertEquals(DataTypeDefinition.TEXT, temp.getType());
    }
	
	public void testIsMandatory()
	{
		ParameterDefinitionImpl temp = create();
		assertTrue(temp.isMandatory());
	}
    
    public void testGetDisplayLabel()
    {
        ParameterDefinitionImpl temp = create();
        assertEquals(DISPLAY_LABEL, temp.getDisplayLabel());    
    }
}
