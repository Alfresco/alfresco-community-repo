/**
 * 
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
	public void testAddingBundles()
	{
		// Check that the string's are not added to the bundle
		ResourceBundle before = ResourceBundleWrapper.getResourceBundle(null, "alfresco.messages.webclient", Locale.US);
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
		ResourceBundle after = ResourceBundleWrapper.getResourceBundle(null, "alfresco.messages.webclient", Locale.US);
		Enumeration<String> keys2 = after.getKeys();
		assertTrue(containsValue(keys2, KEY_1));
		assertEquals(after.getString(KEY_1), MSG_1);
		assertEquals(after.getString(KEY_2), MSG_2);
	}
	
	/**
	 * Test the bootstrap bean
	 */
	public void testBootstrap()
	{
		// Use the bootstrap bean to add the bundles
		List<String> bundles = new ArrayList<String>(1);
		bundles.add(BUNDLE_NAME);
		ResourceBundleBootstrap bootstrap = new ResourceBundleBootstrap();
		bootstrap.setResourceBundles(bundles);
		
		// Check that the string's are now added to the bundle
		ResourceBundle after = ResourceBundleWrapper.getResourceBundle(null, "alfresco.messages.webclient", Locale.US);
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
