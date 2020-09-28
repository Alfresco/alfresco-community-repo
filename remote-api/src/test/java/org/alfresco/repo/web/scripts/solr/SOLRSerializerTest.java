/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.repo.web.scripts.solr;

import static org.junit.Assert.*;

import java.util.Date;

import org.alfresco.repo.web.scripts.solr.SOLRSerializer.SOLRTypeConverter;
import org.alfresco.util.ISO8601DateFormat;
import org.junit.Test;

public class SOLRSerializerTest 
{
	@Test
    public void testDateSerializer()
    {
		  SOLRTypeConverter typeConverter = new SOLRTypeConverter(null);
		  
		  trip(typeConverter, "1912-01-01T00:40:00-06:00", "1912-01-01T06:40:00.000Z");
		  trip(typeConverter, "1812-01-01T00:40:00-06:00", "1812-01-01T06:40:00.000Z");
		  trip(typeConverter, "1845-01-01T00:40:00-06:00", "1845-01-01T06:40:00.000Z");
		  trip(typeConverter, "1846-01-01T00:40:00-06:00", "1846-01-01T06:40:00.000Z");
		  trip(typeConverter, "1847-01-01T00:40:00-06:00", "1847-01-01T06:40:00.000Z");
		  trip(typeConverter, "1848-01-01T00:40:00-06:00", "1848-01-01T06:40:00.000Z");
        
		  
    }
	
	
	private void trip( SOLRTypeConverter typeConverter, String iso, String zulu)
	{
          Date testDate = ISO8601DateFormat.parse(iso);
          String strDate = typeConverter.INSTANCE.convert(String.class, testDate);
          assertEquals(zulu, strDate);
	}
}
