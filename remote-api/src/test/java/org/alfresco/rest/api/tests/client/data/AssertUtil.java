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
package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;

public class AssertUtil
{
	private AssertUtil()
	{		
	}

	public static void assertEquals(String propertyName, String expected, String actual)
	{
		if(expected != null)
		{
			if(!expected.equals(""))
			{
				assertNotNull(propertyName + " expected " + expected + ", but was null", actual);
				Assert.assertEquals(expected, actual);
			}
		}
	}

	public static void assertEquals(String propertyName, Object expected, Object actual)
	{
		if(expected != null)
		{
			assertNotNull(propertyName + " expected " + expected + ", but was null", actual);
			Assert.assertEquals(propertyName + " expected " + expected + ", but was " + actual, expected, actual);
		}
	}
}