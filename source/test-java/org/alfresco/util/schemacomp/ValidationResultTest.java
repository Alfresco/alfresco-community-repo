package org.alfresco.util.schemacomp;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.util.schemacomp.model.Index;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;


/**
 * Tests for the {@link ValidationResult} class.
 * 
 * @author Matt Ward
 */
public class ValidationResultTest
{
    @Before
    public void setUp()
    {
        I18NUtil.registerResourceBundle("alfresco.messages.system-messages");
    }
    
    @Test
    public void describe()
    {        
        DbProperty targetDbProp = mock(DbProperty.class);
        when(targetDbProp.getPath()).thenReturn("alfresco.some_table.idx_table_id.name");
        when(targetDbProp.getPropertyValue()).thenReturn("idx_table_id");
        when(targetDbProp.getDbObject()).thenReturn(new Index(""));
        
        ValidationResult validation = new ValidationResult(targetDbProp, "value must be 'xyz'");
        
        assertEquals("Validation: index alfresco.some_table.idx_table_id.name=\"idx_table_id\" fails to " +
                    "match rule: value must be 'xyz'",
                    validation.describe());
    }
}
