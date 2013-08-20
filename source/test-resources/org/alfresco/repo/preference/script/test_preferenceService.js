function testPreferences()
{
	var preferences = new Object();
	preferences.myValue = "myValue";
	preferences.comp1 = new Object();
	preferences.comp1.value1 = "value1";
	preferences.comp1.value2 = 12;
	
	preferenceService.setPreferences("userOne", preferences);
	
	var result = preferenceService.getPreferences("userOne");
	
	test.assertNotNull(result);
	test.assertEquals("myValue", result.myValue);
	test.assertEquals("value1", result.comp1.value1);
	test.assertEquals(12, result.comp1.value2);
	
	preferences = new Object();
	preferences.comp2 = new Object();
	preferences.comp2.value1 = "value1";
	preferences.comp2.value2 = 3.142;
	preferences.comp1 = new Object();
	preferences.comp1.value1 = "changed";
	preferences.comp1.value2 = 1001;
	
	preferenceService.setPreferences("userOne", preferences);
	
	result = preferenceService.getPreferences("userOne");	
	test.assertNotNull(result);
	test.assertEquals("myValue", result.myValue);
	test.assertEquals("changed", result.comp1.value1);
	test.assertEquals(1001, result.comp1.value2);
	test.assertEquals("value1", result.comp2.value1);
	test.assertEquals(3.142, result.comp2.value2);
	
	preferenceService.clearPreferences("userOne", "comp1");
	
	result = preferenceService.getPreferences("userOne");	
	test.assertNotNull(result);
	test.assertEquals("myValue", result.myValue);
	test.assertEquals("undefined", result.comp1);
	test.assertEquals("value1", result.comp2.value1);
	test.assertEquals(3.142, result.comp2.value2);
	
	preferenceService.clearPreferences("userOne");
	
	result = preferenceService.getPreferences("userOne");	
	test.assertNotNull(result);
	test.assertEquals("undefined", result.myValue);
	test.assertEquals("undefined", result.comp1);
	test.assertEquals("undefined", result.comp2);
	
}

// Execute test's
testPreferences();