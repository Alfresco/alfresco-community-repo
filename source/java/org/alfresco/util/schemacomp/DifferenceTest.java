/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.util.schemacomp;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.alfresco.util.schemacomp.Difference.Where;
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
       when(refDbProp.getPropertyValue()).thenReturn("node_ref");
       
       DbProperty targetDbProp = mock(DbProperty.class);
       when(targetDbProp.getPath()).thenReturn("alfresco.some_table.some_column.name");
       when(targetDbProp.getPropertyValue()).thenReturn("nood_ref");
       Difference diff = new Difference(Where.IN_BOTH_BUT_DIFFERENCE, refDbProp, targetDbProp);
       
       assertEquals("Difference: IN_BOTH_BUT_DIFFERENCE, reference path:alfresco.some_table.some_column.name " +
                   "(value: node_ref), target path:alfresco.some_table.some_column.name (value: nood_ref)",
                   diff.describe());
   }
   
   @Test
   public void describeRefOnly()
   {
       DbProperty refDbProp = mock(DbProperty.class);
       when(refDbProp.getPath()).thenReturn("alfresco.some_table.some_column.name");
       when(refDbProp.getPropertyValue()).thenReturn("node_ref");
       
       Difference diff = new Difference(Where.ONLY_IN_REFERENCE, refDbProp, null);
       
       assertEquals("Difference: ONLY_IN_REFERENCE, reference path:alfresco.some_table.some_column.name " +
                   "(value: node_ref)",
                   diff.describe());
   }
   
   @Test
   public void describeTargetOnly()
   {
       DbProperty targetDbProp = mock(DbProperty.class);
       when(targetDbProp.getPath()).thenReturn("alfresco.some_table.some_column.name");
       when(targetDbProp.getPropertyValue()).thenReturn("node_ref");
       
       Difference diff = new Difference(Where.ONLY_IN_TARGET, null, targetDbProp);
       
       assertEquals("Difference: ONLY_IN_TARGET, target path:alfresco.some_table.some_column.name " +
                   "(value: node_ref)",
                   diff.describe());
   }
}
