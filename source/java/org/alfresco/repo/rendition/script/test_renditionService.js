function testRenderNodeUsingRenditionDefinitionNames()
{
	// Produce two different renditions of the same source node
    var renditionDefName1 = "cm:doclib";
    var renditionDefName2 = "cm:imgpreview";
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

    var noSuchRendition = renditionService.getRenditionByName(testSourceNode, "cm:nonsense");
    test.assertNull(noSuchRendition, "noSuchRendition should have been null.");
    
    
    // Get renditions by mimetype
    var imageRenditions = renditionService.getRenditions(testSourceNode, "image");
    test.assertNotNull(imageRenditions, "imageRenditions returned null.");
    test.assertEquals(2, imageRenditions.length);

    var swfRenditions = renditionService.getRenditions(testSourceNode, "application/x-shockwave-flash");
    test.assertNotNull(swfRenditions, "swfRenditions returned null.");
    test.assertEquals(0, swfRenditions.length);
}

// Execute tests
testRenderNodeUsingRenditionDefinitionNames();
testGetRenditions();
