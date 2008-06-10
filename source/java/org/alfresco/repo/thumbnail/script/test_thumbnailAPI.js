function testCreateThumbnail()
{
	// Create a thumbnail
	var thumbnail = jpgOrig.createThumbnail("medium");	
	test.assertNotNull(thumbnail);
}

function testThumbnailService()
{
	test.assertFalse(thumbnailService.isThumbnailNameRegistered("rubbish"));
	test.assertTrue(thumbnailService.isThumbnailNameRegistered("medium"));
	
	test.assertNull(thumbnailService.getPlaceHolderResourcePath("rubbish"));
	test.assertNull(thumbnailService.getPlaceHolderResourcePath("webpreview"));
	test.assertNotNull(thumbnailService.getPlaceHolderResourcePath("medium"));
}

// Execute the tests
testCreateThumbnail();
testThumbnailService();