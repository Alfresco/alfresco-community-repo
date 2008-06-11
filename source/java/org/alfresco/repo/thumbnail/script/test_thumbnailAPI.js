function testCreateThumbnail()
{
	var thumbs = jpgOrig.getThumbnails();
	test.assertNotNull(thumbs);
	test.assertEquals(0, thumbs.length);

	// Create a thumbnail
	var thumbnail = jpgOrig.createThumbnail("medium");	
	test.assertNotNull(thumbnail);
	
	thumbs = jpgOrig.getThumbnails();
	test.assertNotNull(thumbs);
	test.assertEquals(1, thumbs.length);
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