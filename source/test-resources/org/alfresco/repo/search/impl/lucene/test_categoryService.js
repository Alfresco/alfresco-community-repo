// Checks the finding and adding of Root Categories
function testRootCategories()
{
	// TODO
	// CategoryNode[] getAllCategoryNodes(String aspect)
    // string[] getAllClassificationAspects()
    // CategoryNode createRootCategory(string aspect, string name)
    // CategoryNode[] getRootCategories(string aspect) 
}

// Checks that we can correctly query a test category
//  that ADMLuceneCategoryTest setup for us
function testCategoryListings()
{
	// Check things are correctly detected as categories
	test.assertEquals(true, catACBase.isCategory);
	test.assertEquals(true, catACOne.isCategory);
	test.assertEquals(true, catACTwo.isCategory);
	test.assertEquals(true, catACThree.isCategory);
	
	// These tests are taken from testCategoryServiceImpl()
	
    //result = impl.getChildren(catACBase , CategoryService.Mode.MEMBERS, CategoryService.Depth.IMMEDIATE);
	test.assertEquals(1, catACBase.immediateCategoryMembers.length);
			
    //result = impl.getChildren(catACBase , CategoryService.Mode.ALL, CategoryService.Depth.IMMEDIATE);
    test.assertEquals(3, catACBase.immediateMembersAndSubCategories.length);

    //result = impl.getChildren(catACBase , CategoryService.Mode.SUB_CATEGORIES, CategoryService.Depth.IMMEDIATE);
    test.assertEquals(2, catACBase.immediateSubCategories.length);
    
    //result = impl.getChildren(catACBase , CategoryService.Mode.MEMBERS, CategoryService.Depth.ANY);
    test.assertEquals(14, catACBase.categoryMembers.length);
    
    //result = impl.getChildren(catACBase , CategoryService.Mode.ALL, CategoryService.Depth.ANY);
    test.assertEquals(17, catACBase.membersAndSubCategories.length);
   
    //result = impl.getChildren(catACBase , CategoryService.Mode.SUB_CATEGORIES, CategoryService.Depth.ANY);
    test.assertEquals(3, catACBase.subCategories.length);
}

// Tests that we can add and remove sub-categories
function testSubCategories()
{
	// catACBase starts with 3
	test.assertEquals(3, catACBase.subCategories.length);
	
	// Add a 4th
	var testCat = catACBase.createSubCategory("testCat");
	test.assertEquals(4, catACBase.subCategories.length);
	
	// Delete it
	testCat.removeCategory();
	test.assertEquals(3, catACBase.subCategories.length);
	
	// Add 2 more, as parent/child
	var testCatA = catACBase.createSubCategory("testCatA");
	var testCatB = testCatA.createSubCategory("testCatB");
	test.assertEquals(5, catACBase.subCategories.length);
	
	// Delete the parent
	testCatA.removeCategory();
	test.assertEquals(3, catACBase.subCategories.length);
}

// Execute Tests
testRootCategories();
testCategoryListings();
testSubCategories();