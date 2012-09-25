/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.web.app;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import junit.framework.TestCase;

/**
 * Unit test for resource bundle wrapper
 * 
 * @author Roy Wetherall
 */
public class ResourceBundleWrapperTest extends TestCase 
{
	private static final String BUNDLE_NAME = "org.alfresco.web.app.resourceBundleWrapperTest";	
	private static final String KEY_1 = "test_key_one";
	private static final String KEY_2 = "test_key_two";
	private static final String MSG_1 = "Test Key One";
	private static final String MSG_2 = "Test Key Two";
    
	/**
	 * Test adding the bundles
	 */
	public void test1AddingBundles()
	{
		// Check that the string's are not added to the bundle
		ResourceBundle before = ResourceBundleWrapper.getResourceBundle("alfresco.messages.webclient", Locale.US);
		Enumeration<String> keys = before.getKeys();
		assertFalse(containsValue(keys, KEY_1));
		assertFalse(containsValue(keys, KEY_2));
		try
		{
			before.getString(KEY_1);
			fail("Not expecting the key to be there");
		}
		catch (Throwable exception){};
		try
		{
			before.getString(KEY_2);
			fail("Not expecting the key to be there");
		}
		catch (Throwable exception){};
		
		// Add an additional resource bundle
		ResourceBundleWrapper.addResourceBundle(BUNDLE_NAME);
		
		// Check that the string's are now added to the bundle
		ResourceBundle after = ResourceBundleWrapper.getResourceBundle("alfresco.messages.webclient", Locale.US);
		Enumeration<String> keys2 = after.getKeys();
		assertTrue(containsValue(keys2, KEY_1));
		assertEquals(after.getString(KEY_1), MSG_1);
		assertEquals(after.getString(KEY_2), MSG_2);
	}
	
	/**
	 * Test the bootstrap bean
	 */
	public void test2Bootstrap()
	{
		// Use the bootstrap bean to add the bundles
		List<String> bundles = new ArrayList<String>(1);
		bundles.add(BUNDLE_NAME);
		ResourceBundleBootstrap bootstrap = new ResourceBundleBootstrap();
		bootstrap.setResourceBundles(bundles);
		
		// Check that the string's are now added to the bundle
		ResourceBundle after = ResourceBundleWrapper.getResourceBundle("alfresco.messages.webclient", Locale.US);
		Enumeration<String> keys2 = after.getKeys();
		assertTrue(containsValue(keys2, KEY_1));
		assertTrue(containsValue(keys2, KEY_2));
		assertEquals(after.getString(KEY_1), MSG_1);
		assertEquals(after.getString(KEY_2), MSG_2);		
	}

	/**
	 * Check whether the list contains the values
	 * 
	 * @param values	list of values to check
	 * @param value		value to look for
	 * @return boolean  true if value contained, false otherwise
	 */
	private boolean containsValue(Enumeration<String> values, String value)
	{
		boolean result = false;
		while (values.hasMoreElements() == true)
		{
			if (values.nextElement().equals(value) == true)
			{
				result = true;
				break;
			}
		}
		return result;
	}
}
