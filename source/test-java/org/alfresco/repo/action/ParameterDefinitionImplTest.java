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
