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


failure = "";

// Return the failure message
failure;
