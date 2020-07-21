function testTagScopeALF_21875()
{
	var site = siteService.getSite(customSiteName);
	test.assertNotNull(site);
	var siteNode = site.node;
	siteNode.properties.title = 'Test';
	siteNode.save();
	
	site = siteService.getSite(customSiteName);

}

//executeTest
testTagScopeALF_21875();