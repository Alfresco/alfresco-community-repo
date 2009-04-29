// called by ActivityServiceImplTest.java (test_JSAPI)

var failure = "did not complete script";

// invalid
// activities.postActivity("my activity type", null, null, null);
// activities.postActivity(null, "my site", "my app tool", '{ 000 }');

// valid
activities.postActivity("test activity type 4", null, null, '{ "item1" : 123 }');
activities.postActivity("test activity type 5", "my site", null, '{ "item2" : 456 }');
activities.postActivity("test activity type 6", "my site", "my app tool", '{ "item3" : 789 }');
activities.postActivity("test activity type 7", "my site", "my app tool", '{ invalidJSON }');

// user feed controls
var feedControls = activities.getFeedControls();
test.assertEquals(0, feedControls.length);

activities.setFeedControl("my site", "my app tool");

feedControls = activities.getFeedControls();
test.assertEquals(1, feedControls.length);

activities.setFeedControl("my site", null);

feedControls = activities.getFeedControls();
test.assertEquals(2, feedControls.length);

activities.setFeedControl("", "my app tool");

feedControls = activities.getFeedControls();
test.assertEquals(3, feedControls.length);


activities.unsetFeedControl("my site", "my app tool");
activities.unsetFeedControl("my site", "");
activities.unsetFeedControl(null, "my app tool");

feedControls = activities.getFeedControls();
test.assertEquals(0, feedControls.length);


failure = "";

// Return the failure message
failure;
