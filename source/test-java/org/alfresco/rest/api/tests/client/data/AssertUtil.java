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