/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.rating;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.rating.RatingNodeProperties.RatingStruct;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit Test class for {@link RatingNodeProperties}.
 * 
 * @author Neil Mc Erlean
 * @since 3.4
 */
public class RatingNodePropertiesTest
{
    private static final String LIKE = "like";
    private static final String FIVESTAR = "fivestar";
    
    // These are declared as ArrayLists in order to be Serializable.
    private ArrayList<String> testSchemes;
    private ArrayList<Float> testScores;
    private ArrayList<Date>    testDates;
    private RatingNodeProperties testProps;
    
    @Before public void initTestData()
    {
        testSchemes = new ArrayList<String>();
        testScores = new ArrayList<Float>();
        testDates = new ArrayList<Date>();
        // These correspond to two ratings:
        // '1' in the 'like' scheme at 'now'.
        // '5' in the 'fivestar' scheme at 'now'.
        testSchemes.add(LIKE);
        testSchemes.add(FIVESTAR);
        
        testScores.add(1.0f);
        testScores.add(5.0f);
        
        testDates.add(new Date());
        testDates.add(new Date());
        
        Map<QName, Serializable> alfrescoStyleProps = new HashMap<QName, Serializable>();
        alfrescoStyleProps.put(ContentModel.PROP_RATING_SCHEME, testSchemes);
        alfrescoStyleProps.put(ContentModel.PROP_RATING_SCORE, testScores);
        alfrescoStyleProps.put(ContentModel.PROP_RATED_AT, testDates);
        
        testProps = RatingNodeProperties.createFrom(alfrescoStyleProps);

    }
    
    /**
     * This test method checks that constructing a RatingNodeProperties with null-valued
     * property lists works.
     */
    @Test public void noNullPropertyLists() throws Exception
    {
        List<String> schemes = null;
        List<Float> scores = null;
        List<Date> dates = null;
        RatingNodeProperties nullProps = new RatingNodeProperties(schemes, scores, dates);
        
        assertEquals(0, nullProps.size());
    }
    
    @Test public void constructAndAccessRatings() throws Exception
    {
        assertEquals(2, testProps.size());
        assertEquals(2, testProps.getAllRatings().size());
        
        assertEquals(0, testProps.getIndexOfRating(LIKE));
        assertEquals(-1, testProps.getIndexOfRating("noSuchScheme"));
        
        final RatingStruct firstRating = testProps.getRatingAt(0);
        assertEquals(LIKE, firstRating.getScheme());
        assertEquals(1.0f, firstRating.getScore(), 0.1f);
        final Date recoveredLikeDate = firstRating.getDate();
        assertNotNull(recoveredLikeDate);
        
        final RatingStruct secondRating = testProps.getRatingAt(1);
        assertEquals(FIVESTAR, secondRating.getScheme());
        assertEquals(5, secondRating.getScore(), 0.1f);
        final Date recoveredSecondDate = secondRating.getDate();
        assertNotNull(recoveredSecondDate);
        
        RatingStruct l = testProps.getRating(LIKE);
        assertNotNull(l);
        assertEquals(LIKE, l.getScheme());
        assertEquals(1, l.getScore(), 0.1f);
        assertEquals(recoveredLikeDate, l.getDate());
    }
    
    @Test public void appendRating()
    {
        // Check all is right before we start
        assertEquals(2, testProps.size());

        testProps.appendRating("appended", 10);
        assertEquals(3, testProps.size());

        assertNotNull(testProps.getRating(LIKE));
        assertNotNull(testProps.getRating(FIVESTAR));
        assertNotNull(testProps.getRating("appended"));
    }

    @Test public void removeRating()
    {
        // Check all is right before we start
        assertEquals(2, testProps.size());

        // Remove the first rating - should be 'like'
        
        testProps.removeRatingAt(0);
        assertEquals(1, testProps.size());

        // Now 'like' should be gone, but 'fivestar' should still be there.
        assertNull(testProps.getRating(LIKE));
        assertNotNull(testProps.getRating(FIVESTAR));
    }

    @Test public void replaceRating()
    {
        // Check all is right before we start
        assertEquals(2, testProps.size());

        // Replace the first rating - should be 'like'
        
        // There's no such rating scheme as 'foo' but for this unit test it doesn't matter.
        testProps.setRatingAt(0, "foo", 42);
        assertEquals(2, testProps.size());

        // Now 'like' should be replaced by 'foo'.
        assertNull(testProps.getRating(LIKE));
        assertNotNull(testProps.getRating(FIVESTAR));
        final RatingStruct fooRating = testProps.getRating("foo");
        assertNotNull(fooRating);
        assertEquals(42, fooRating.getScore(), 0.1f);
    }
    
    @SuppressWarnings("unchecked")
    @Test public void extractAlfrescoNodeProperties()
    {
        Map<QName, Serializable> alfProps = this.testProps.toNodeProperties();
        assertNotNull(alfProps);
        final int numberOfProperties = 3;
        assertEquals(numberOfProperties, alfProps.size());
        final List<String> ratingSchemes = (List<String>)alfProps.get(ContentModel.PROP_RATING_SCHEME);
        final List<Float> ratingScores = (List<Float>)alfProps.get(ContentModel.PROP_RATING_SCORE);
        final List<Date> ratingDates = (List<Date>)alfProps.get(ContentModel.PROP_RATED_AT);
        final int numberOfRatings = 2;
        assertEquals(numberOfRatings, ratingSchemes.size());
        assertEquals(numberOfRatings, ratingScores.size());
        assertEquals(numberOfRatings, ratingDates.size());
        assertEquals(Arrays.asList(new String[]{LIKE, FIVESTAR}), ratingSchemes);
        assertEquals(Arrays.asList(new Float[]{1.0f, 5.0f}), ratingScores);
        // No Date checks
    }
}
