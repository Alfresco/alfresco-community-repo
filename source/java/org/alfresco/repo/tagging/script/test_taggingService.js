function testAddRemoveTag()
{
	var tags = document.tags;
	test.assertNotNull(tags);
	test.assertEquals(0, tags.length);
	
	document.addTag("mouse");
	document.addTag("snake");
	document.addTag("snail");
	
	tags = document.tags;
	test.assertNotNull(tags);
	test.assertEquals(3, tags.length);
	
	document.removeTag("cat");
	document.removeTag("snake");
	
	tags = document.tags;
	test.assertNotNull(tags);
	test.assertEquals(2, tags.length);
}

// Execute test's
testAddRemoveTag();