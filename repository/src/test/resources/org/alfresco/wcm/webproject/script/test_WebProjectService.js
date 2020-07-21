/**
 * Test WCM Web Project Java Script Interface
 */ 

function testListWebProjects()
{
	var service = webprojects;
	test.assertNotNull(service, "Service is null.");
	
	var list1 = service.listWebProjects();
	var length = list1.length;
	
	var newProjA = service.createWebProject("TestA", "test a website", "description", "jsTestA");
	var newProjB = service.createWebProject("TestB", "test b website", "description", "jsTestB");
	var newProjC = service.createWebProject("TestC", "test c website", "description", "jsTestC");
	
	var list2 = service.listWebProjects();
	test.assertNotNull(list2, "list2 is null.");
	test.assertTrue(list2.length >= 3 + length, "list too small");
	
	newProjA.deleteWebProject();
	newProjB.deleteWebProject();
	newProjC.deleteWebProject();
	
}

function testCRUD()
{
	var service = webprojects;
	test.assertNotNull(service, "Service is null.");
	
	// Try and get a web project that doesn't exist.
	var newProj = service.createWebProject("name", "title", "description", "jsTest");
	
	var node = newProj.getNodeRef();
	
	test.assertNotNull(node.id, "node.id is null.");
	
	newProj.deleteWebProject();
}


// Execute test's
testCRUD();
testListWebProjects();
