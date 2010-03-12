function testRenderNodeUsingRenditionDefinitionNames()
{
	// Produce two different renditions of the same source node
	// One with a long-form qname and one with a short-form qname
    var renditionDefName1 = "cm:doclib";
    var renditionDefName2 = "{http://www.alfresco.org/model/content/1.0}imgpreview";
    var rendition1 = renditionService.render(testSourceNode, renditionDefName1);
    var rendition2 = renditionService.render(testSourceNode, renditionDefName2);
    
    test.assertNotNull(rendition1, "rendition1 was null.");
    // Renditions created under the source node will be 'hiddenRenditions'.
    test.assertTrue(rendition1.hasAspect("rn:hiddenRendition"));

    test.assertNotNull(rendition2, "rendition2 was null.");
    test.assertTrue(rendition2.hasAspect("rn:hiddenRendition"));
}

function testGetRenditions()
{
    // Get all renditions
    var allRenditions = renditionService.getRenditions(testSourceNode);
    test.assertNotNull(allRenditions, "allRenditions returned null.");
    test.assertEquals(2, allRenditions.length);
    
    // Get named renditions
    var doclibRendition = renditionService.getRenditionByName(testSourceNode, "cm:doclib");
    test.assertNotNull(doclibRendition, "doclibRendition returned null.");

    var noSuchRendition = renditionService.getRenditionByName(testSourceNode, "{http://www.alfresco.org/model/content/1.0}nonsense");
    test.assertNull(noSuchRendition, "noSuchRendition should have been null.");
    
    
    // Get renditions by mimetype
    var imageRenditions = renditionService.getRenditions(testSourceNode, "image");
    test.assertNotNull(imageRenditions, "imageRenditions returned null.");
    test.assertEquals(2, imageRenditions.length);

    var swfRenditions = renditionService.getRenditions(testSourceNode, "application/x-shockwave-flash");
    test.assertNotNull(swfRenditions, "swfRenditions returned null.");
    test.assertEquals(0, swfRenditions.length);
}

function testCreateRenditionDefinitionAndRender()
{
	// Create a simple (non-composite) rendition definition.
	
	// As long as we don't save this renditionDefinition, there should be no need to
	// give it a name which is unique across multiple test executions.
	var renditionDefName = "cm:adHocRenditionDef";
	var renderingEngineName = "imageRenderingEngine";

	var renditionDef = renditionService.createRenditionDefinition(renditionDefName, renderingEngineName);

	test.assertNotNull(renditionDef, "ad hoc rendition definition was null.");
	test.assertEquals(renditionDefName, renditionDef.renditionName);
	test.assertEquals(renderingEngineName, renditionDef.renderingEngineName);

	
	// Set some parameters.
	renditionDef.parameters['rendition-nodetype'] = "cm:content";
	renditionDef.parameters['xsize'] = 99;
	
	// Read them back to check
	test.assertEquals("cm:content", renditionDef.parameters['rendition-nodetype']);
	test.assertEquals(99, renditionDef.parameters['xsize']);
	
	// Now execute this rendition definition
    var rendition = renditionService.render(testSourceNode, renditionDef);
    
    test.assertNotNull(rendition, "rendition was null.");
    test.assertTrue(rendition.hasAspect("rn:hiddenRendition"));
}

// Execute tests
testRenderNodeUsingRenditionDefinitionNames();
testGetRenditions();
testCreateRenditionDefinitionAndRender();
