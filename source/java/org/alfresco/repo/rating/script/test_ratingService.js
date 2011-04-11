function testRatingSchemes()
{
	var schemeNames = ratingService.getRatingSchemeNames();
    test.assertEquals(2, schemeNames.length);
    test.assertEquals('likesRatingScheme', schemeNames[0]);
    test.assertEquals('fiveStarRatingScheme', schemeNames[1]);
    
    test.assertEquals(1, ratingService.getMin('likesRatingScheme'));
    test.assertEquals(1, ratingService.getMax('likesRatingScheme'));
    test.assertEquals(true, ratingService.isSelfRatingAllowed('likesRatingScheme'));

    test.assertEquals(1, ratingService.getMin('fiveStarRatingScheme'));
    test.assertEquals(5, ratingService.getMax('fiveStarRatingScheme'));
    test.assertEquals(false, ratingService.isSelfRatingAllowed('fiveStarRatingScheme'));
}

function testApplyUpdateDeleteRatings()
{
	// Check the pristine state of the test node.
	test.assertEquals(0, ratingService.getRatingsCount(testNode, 'fiveStarRatingScheme'));
	test.assertEquals(0, ratingService.getTotalRating(testNode, 'fiveStarRatingScheme'));
	test.assertEquals(-1, ratingService.getAverageRating(testNode, 'fiveStarRatingScheme'));
	
	// Now apply some ratings.
	ratingService.applyRating(testNode, 2.0, 'fiveStarRatingScheme');
	test.assertEquals(2.0, ratingService.getRating(testNode, 'fiveStarRatingScheme'));

	test.assertNotNull(ratingService.getRatingAppliedAt(testNode, 'fiveStarRatingScheme'));
	
	test.assertEquals(1, ratingService.getRatingsCount(testNode, 'fiveStarRatingScheme'));
	test.assertEquals(2, ratingService.getTotalRating(testNode, 'fiveStarRatingScheme'));
	test.assertEquals(2, ratingService.getAverageRating(testNode, 'fiveStarRatingScheme'));

	
	// And update them
	ratingService.applyRating(testNode, 4.5, 'fiveStarRatingScheme');
	test.assertEquals(4.5, ratingService.getRating(testNode, 'fiveStarRatingScheme'));

	test.assertNotNull(ratingService.getRatingAppliedAt(testNode, 'fiveStarRatingScheme'));
	
	test.assertEquals(1, ratingService.getRatingsCount(testNode, 'fiveStarRatingScheme'));
	test.assertEquals(4.5, ratingService.getTotalRating(testNode, 'fiveStarRatingScheme'));
	test.assertEquals(4.5, ratingService.getAverageRating(testNode, 'fiveStarRatingScheme'));
	
	
	// Now delete them.
	ratingService.removeRating(testNode, 'fiveStarRatingScheme');
	test.assertEquals(-1, ratingService.getRating(testNode, 'fiveStarRatingScheme'));
}

// Execute tests
testRatingSchemes();
testApplyUpdateDeleteRatings();
