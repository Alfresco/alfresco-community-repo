package org.alfresco.util.schemacomp;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.util.schemacomp.Difference.Where;
import org.alfresco.util.schemacomp.model.Column;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;
 
/**
 * Tests for the {@link Difference} class.
 * 
 * @author Matt Ward
 */
public class DifferenceTest
{
   @Before
   public void setUp()
   {
       I18NUtil.registerResourceBundle("alfresco.messages.system-messages");       
   }
   
   @Test
   public void describe()
   {
       
       DbProperty refDbProp = mock(DbProperty.class);
       when(refDbProp.getPath()).thenReturn("alfresco.some_table.some_column.name");
       when(refDbProp.getDbObject()).thenReturn(new Column("some_column"));
       when(refDbProp.getPropertyValue()).thenReturn("node_ref");
       
       DbProperty targetDbProp = mock(DbProperty.class);
       when(targetDbProp.getPath()).thenReturn("alfresco.some_table.some_column.name");
       when(targetDbProp.getDbObject()).thenReturn(new Column("some_column"));
       when(targetDbProp.getPropertyValue()).thenReturn("nood_ref");
       Difference diff = new Difference(Where.IN_BOTH_BUT_DIFFERENCE, refDbProp, targetDbProp);
       
       assertEquals("Difference: expected column alfresco.some_table.some_column.name=\"node_ref\"" +
                   ", but was alfresco.some_table.some_column.name=\"nood_ref\"",
                   diff.describe());
   }
   
   @Test
   public void describeRefOnly()
   {
       DbProperty refDbProp = mock(DbProperty.class);
       when(refDbProp.getPath()).thenReturn("alfresco.some_table.some_column");
       when(refDbProp.getDbObject()).thenReturn(new Column("some_column"));
       
       Difference diff = new Difference(Where.ONLY_IN_REFERENCE, refDbProp, null);
       
       assertEquals("Difference: missing column from database, expected at path: alfresco.some_table.some_column",
                   diff.describe());
   }
   
   @Test
   public void describeTargetOnly()
   {
       DbProperty targetDbProp = mock(DbProperty.class);
       when(targetDbProp.getPath()).thenReturn("alfresco.some_table.some_column");
       when(targetDbProp.getDbObject()).thenReturn(new Column("some_column"));
       
       Difference diff = new Difference(Where.ONLY_IN_TARGET, null, targetDbProp);
       
       assertEquals("Difference: unexpected column found in database with path: alfresco.some_table.some_column",
                   diff.describe());
   }
}
