function testPreferences()
{
    var preferences = new Object();
    preferences.myValue = "myValue";
    preferences.comp1 = new Object();
    preferences.comp1.value1 = "value1";
    preferences.comp1.value2 = 12;
    
    preferenceService.setPreferences(username1, preferences);
    
    var result = preferenceService.getPreferences(username1);
    
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
    
    preferenceService.setPreferences(username1, preferences);
    
    result = preferenceService.getPreferences(username1);   
    test.assertNotNull(result);
    test.assertEquals("myValue", result.myValue);
    test.assertEquals("changed", result.comp1.value1);
    test.assertEquals(1001, result.comp1.value2);
    test.assertEquals("value1", result.comp2.value1);
    test.assertEquals(3.142, result.comp2.value2);
    
    preferenceService.clearPreferences(username1, "comp1");
    
    result = preferenceService.getPreferences(username1);   
    test.assertNotNull(result);
    test.assertEquals("myValue", result.myValue);
    test.assertEquals("undefined", result.comp1);
    test.assertEquals("value1", result.comp2.value1);
    test.assertEquals(3.142, result.comp2.value2);
    
    preferenceService.clearPreferences(username1);
    
    result = preferenceService.getPreferences(username1);   
    test.assertNotNull(result);
    test.assertEquals("undefined", result.myValue);
    test.assertEquals("undefined", result.comp1);
    test.assertEquals("undefined", result.comp2);
}

function testGetPreferencesWithFilters_Alf20023()
{
   // Intentionally using a string rather than Date instance below as debugging shows that that is what the Java service receives.
   var preferences =
   {
      "org.alfresco.share.sites.favourites.one" : true,
      "org.alfresco.ext.sites.favourites.one.createdAt" : "2013-09-16T08:45:27.246Z"
   };
   
   preferenceService.setPreferences(username1, preferences);
   
   var result = preferenceService.getPreferences(username1);
   test.assertNotNull(result, 'get preferences returned null');
   // Note that the PreferenceService will restructure some Alfresco-specific JSON keys into deep object structures.
   // See CLOUD-1518 for some details on why this is.
   //
   // 1. Test the restructured data is there
   test.assertEquals(true, result.org.alfresco.share.sites.favourites.one);
   test.assertEquals("2013-09-16T08:45:27.246Z", result.org.alfresco.ext.sites.favourites.one.createdAt);
   // 2. And now test that the API correctly returns data whose key triggered a restructure.
   //    So get by key, specifying a 'flat' key, which still returns a deep JSON object.
   var favouriteSites = preferenceService.getPreferences(username1, "org.alfresco.share.sites.favourites");
   test.assertEquals(true, favouriteSites.org.alfresco.share.sites.favourites.one);
   
   preferences =
   {
      "org.alfresco.share.sites.recent._0" : "one"
   };
   
   preferenceService.setPreferences(username1, preferences);
   result = preferenceService.getPreferences(username1);
   test.assertNotNull(result, 'get preferences returned null');
   test.assertEquals("one", result.org.alfresco.share.sites.recent._0);
   
   // Simply getting these values is enough to ensure ALF-20023 is not regressed.
   var favs = preferenceService.getPreferences(username1, 'org.alfresco.share.sites.favourites');
   var recents = preferenceService.getPreferences(username1, 'org.alfresco.share.sites.recent');
}

function testGettingAnotherUsersPreferencesShouldRaiseAnException()
{
   try
   {
      preferenceService.getPreferences(username2);
   }
   catch (e)
   {
      return;
   }
   test.fail("Expected exception not thrown: should not be able to access other users' preferences.");
}

// Execute tests
testPreferences();
testGettingAnotherUsersPreferencesShouldRaiseAnException();
testGetPreferencesWithFilters_Alf20023();