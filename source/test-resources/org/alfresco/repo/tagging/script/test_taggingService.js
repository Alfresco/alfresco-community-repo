
function testGlobalTagMethods()
{
	var tags = taggingService.getTags(store);
	assertNotNull(tags);
	assertEquals(5, tags.length);

	var tags = taggingService.getTags(store, "alpha");
	assertNotNull(tags);
	assertEquals(2, tags.length);	
}

function testAddRemoveTag()
{
	var tags = document.tags;
	test.assertNotNull(tags);
	test.assertNotEquals(undefined, tags);
	test.assertEquals(0, tags.length);
	
	document.properties.title = "A change is as good as a rest!";	
	document.addTag("mouse");
	document.addTag("snake");
	document.addTag("snail");
	document.save();
	
	tags = document.tags;
	test.assertNotNull(tags);
	test.assertEquals(3, tags.length);
	
	document.removeTag("cat");
	document.removeTag("snake");
	
	tags = document.tags;
	test.assertNotNull(tags);
	test.assertEquals(2, tags.length);
	
	document.tags = ["moo", "quack", "squeak"];
	document.properties.title = "A change is as good as a rest!";
	document.save();
	tags = document.tags;
	test.assertNotNull(tags);
	test.assertEquals(3, tags.length);
	
	document.addTags(["woof", "oink"]);
	
	tags = document.tags;
	test.assertNotNull(tags);
	test.assertEquals(5, tags.length);
	
	document.removeTags(["moo", "quack", "oink"]);
	
	tags = document.tags;
	test.assertNotNull(tags);
	test.assertEquals(2, tags.length);
	
	document.clearTags();
	
	tags = document.tags;
	test.assertNotNull(tags);
	test.assertEquals(0, tags.length);	
}

function testTagScope()
{
	test.assertFalse(folder.isTagScope);
	test.assertFalse(subFolder.isTagScope);
	test.assertFalse(subDocument.isTagScope);
	
	test.assertNull(subDocument.tagScope);
	test.assertNull(folder.tagScope);
	test.assertNull(subFolder.tagScope);
	
	subFolder.isTagScope = true;
	
	test.assertFalse(folder.isTagScope);
	test.assertTrue(subFolder.isTagScope);
	test.assertFalse(subDocument.isTagScope);
	
	test.assertNotNull(subDocument.tagScope);
	test.assertNull(folder.tagScope);
	test.assertNotNull(subFolder.tagScope);
	
	folder.isTagScope = true;
	
	test.assertTrue(folder.isTagScope);
	test.assertTrue(subFolder.isTagScope);
	test.assertFalse(subDocument.isTagScope);
	
	test.assertNotNull(subDocument.tagScope);
	test.assertNotNull(folder.tagScope);
	test.assertNotNull(subFolder.tagScope);
	
	subFolder.isTagScope = false;
	
	test.assertTrue(folder.isTagScope);
	test.assertFalse(subFolder.isTagScope);
	test.assertFalse(subDocument.isTagScope);
	
	test.assertNotNull(subDocument.tagScope);
	test.assertNotNull(folder.tagScope);
	test.assertNotNull(subFolder.tagScope);	
}

function testTagScopeObject()
{
	var scope = document.tagScope;
	test.assertNotNull(scope);
	var tags = scope.tags;
	test.assertNotNull(tags);
	test.assertEquals(3, tags.length);
	test.assertEquals("tag one", tags[0].name);
	test.assertEquals("tag two", tags[1].name);
	test.assertEquals("tag three", tags[2].name);
	test.assertEquals(4, tags[0].count);
	test.assertEquals(3, tags[1].count);
	test.assertEquals(1, tags[2].count);
	test.assertEquals(4, scope.getCount("tag one"));
	test.assertEquals(3, scope.getCount("tag two"));
	test.assertEquals(1, scope.getCount("tag three"));

	tags = scope.getTopTags(2);
	test.assertNotNull(tags);
	test.assertEquals(2, tags.length);
	test.assertEquals("tag one", tags[0].name);
	test.assertEquals("tag two", tags[1].name);	
	test.assertEquals(4, tags[0].count);
	test.assertEquals(3, tags[1].count);
	test.assertEquals(4, scope.getCount("tag one"));
	test.assertEquals(3, scope.getCount("tag two"));
	
	// Refresh tag scope
	document.tagScope.refresh();
	scope = document.tagScope;
	test.assertNotNull(scope);
	tags = scope.tags;
	test.assertNotNull(tags);
	test.assertEquals(3, tags.length);
	test.assertEquals("tag one", tags[0].name);
	test.assertEquals("tag two", tags[1].name);
	test.assertEquals("tag three", tags[2].name);
	test.assertEquals(4, tags[0].count);
	test.assertEquals(3, tags[1].count);
	test.assertEquals(1, tags[2].count);
	test.assertEquals(4, scope.getCount("tag one"));
	test.assertEquals(3, scope.getCount("tag two"));
	test.assertEquals(1, scope.getCount("tag three"));
	
}

function testFind()
{
	var nodes = search.tagSearch(store, "rubbish tag");
	test.assertNotNull(nodes);
	test.assertEquals(0, nodes.length);
	
	nodes = search.tagSearch(store, "tAg OnE");
	test.assertNotNull(nodes);
	test.assertTrue(nodes.length != 0);
	
	nodes = search.tagSearch(store, "tag three");
	test.assertNotNull(nodes);
	test.assertTrue(nodes.length != 0);
	
	nodes = folder.childrenByTags("tag one");
	test.assertNotNull(nodes);
	test.assertTrue(nodes.length != 0);
	
	nodes = subFolder.childrenByTags("tag one");
	test.assertNotNull(nodes);
	test.assertTrue(nodes.length != 0);
}

if (tagScopeTest == true)
{
	testTagScopeObject();
	testFind();
}
else
{
	testAddRemoveTag();
	testTagScope();
}