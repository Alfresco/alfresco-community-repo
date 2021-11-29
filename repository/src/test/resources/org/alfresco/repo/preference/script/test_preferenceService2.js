function testPreferences()
{
    var preferences = new Object();
    preferences.org = new Object();
    preferences.org.share = new Object();
    preferences.org.share.forum = new Object();
    preferences.org.share.forum.summary = new Object();
    preferences.org.share.forum.summary.dashlet = new Object();
    preferences.org.share.forum.summary.dashlet["component-1-3"] = new Object();
    preferences.org.share.forum.summary.dashlet["component-1-3"].history = 1;
    
    preferenceService.setPreferences(username, preferences);
}

// Execute tests
testPreferences();