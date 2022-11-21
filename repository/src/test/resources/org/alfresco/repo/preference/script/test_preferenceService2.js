function testPreferences()
{
    var preferences = {
		"org.alfresco.share.forum.summary.dashlet.component-1-3.history": "1"
    };

    preferenceService.setPreferences(username, preferences);
}

// Execute tests
testPreferences();