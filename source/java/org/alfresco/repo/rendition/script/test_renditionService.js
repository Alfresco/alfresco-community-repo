function testRenderNodeUsingRenditionDefinitionNames()
{
	// Produce two different renditions of the same source node
    var renditionDefName1 = "cm:doclib";
    var renditionDefName2 = "cm:imgpreview";
    var childAssoc1 = renditionService.render(testSourceNode, renditionDefName1);
    var childAssoc2 = renditionService.render(testSourceNode, renditionDefName2);
    
    // Assert that the returned ChildAssociation objects have the correct name and type.
    test.assertNotNull(childAssoc1, "Rendition ChildAssoc1 was null.");
    test.assertEquals(testSourceNode.id, childAssoc1.parent.id, "Rendition 1's parent was not source node");
    test.assertEquals("{http://www.alfresco.org/model/rendition/1.0}rendition", childAssoc1.type);
    test.assertEquals("{http://www.alfresco.org/model/content/1.0}doclib", childAssoc1.name);

    test.assertNotNull(childAssoc2, "Rendition ChildAssoc2 was null.");
    test.assertEquals(testSourceNode.id, childAssoc2.parent.id, "Rendition 2's parent was not source node");
    test.assertEquals("{http://www.alfresco.org/model/rendition/1.0}rendition", childAssoc2.type);
    test.assertEquals("{http://www.alfresco.org/model/content/1.0}imgpreview", childAssoc2.name);
}

function testGetRenditions()
{
    // Get all renditions
    var allRenditionAssocs = renditionService.getRenditions(testSourceNode);
    test.assertNotNull(allRenditionAssocs, "allRenditions returned null.");
    test.assertEquals(2, allRenditionAssocs.length);
    
    // We've already checked the types and names, so there's no point rechecking.


    // Get named renditions
    var doclibRendition = renditionService.getRenditionByName(testSourceNode, "cm:doclib");
    test.assertNotNull(doclibRendition, "doclibRendition returned null.");
    test.assertEquals("{http://www.alfresco.org/model/content/1.0}doclib", doclibRendition.name);

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
