function testCreateThumbnail()
{
	// Create a thumbnail
	var thumbnail = jpgOrig.createThumbnail("medium");	
	test.assertNotNull(thumbnail);
	
	// Create async thumbnail
//	var thumbnail2 = gifOrig.createThumbnail("medium", true);
//	test.assertNull(thumbnail2);
	
	// Try and get the created thumbnail
//	var count = 0;
//	while (true)
//	{
//		thumbnail2 = gifOrig.getThumbnail("medium");
		
//		if (thumbnail2 != null)
//		{
//			break;
//		}
		//else if (count > 1000)
		//{
		//	test.fail("Async thumbanil wasn't created");
		//}
		
//		count++;
//	}
}

// Execute the tests
testCreateThumbnail();