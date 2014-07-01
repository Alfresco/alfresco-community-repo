package org.alfresco.repo.search.impl.solr;

import static org.junit.Assert.*;

import org.alfresco.repo.search.impl.lucene.SolrStatsResult;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

public class SolrStatsResultTest
{

    public static String TEST_CREATED = "{\"response\":{\"start\":0,\"docs\":[],\"numFound\":188},\"responseHeader\":{\"status\":0,\"QTime\":13},\"stats\":{\"stats_fields\":{\"contentsize\":{\"missing\":9,\"min\":25,\"sumOfSquares\":2.9213896971782E13,\"max\":3737049,\"count\":179,\"facets\":{\"@{http://www.alfresco.org/model/content/1.0}created\":{\"2011-03-08\":{\"missing\":1,\"min\":3737049,\"sumOfSquares\":1.3965535228401E13,\"max\":3737049,\"count\":1,\"mean\":3737049,\"sum\":3737049,\"stddev\":0},\"2014-05-05\":{\"missing\":0,\"min\":55,\"sumOfSquares\":2.319490622E9,\"max\":10734,\"count\":128,\"mean\":3078.421875,\"sum\":394038,\"stddev\":2951.678320419461},\"2011-02-15\":{\"missing\":8,\"min\":25,\"sumOfSquares\":1.46704329036E11,\"max\":381778,\"count\":15,\"mean\":29187.466666666667,\"sum\":437812,\"stddev\":97806.55319840477},\"2011-02-16\":{\"missing\":0,\"min\":162,\"sumOfSquares\":26244,\"max\":162,\"count\":1,\"mean\":162,\"sum\":162,\"stddev\":0},\"2011-06-14\":{\"missing\":0,\"min\":262,\"sumOfSquares\":1621982,\"max\":797,\"count\":11,\"mean\":356.3636363636364,\"sum\":3920,\"stddev\":150.01218132356632},\"2011-02-24\":{\"missing\":0,\"min\":73728,\"sumOfSquares\":1.6383213568E10,\"max\":74240,\"count\":3,\"mean\":73898.66666666667,\"sum\":221696,\"stddev\":295.6033378250884},\"2011-03-03\":{\"missing\":0,\"min\":34482,\"sumOfSquares\":1.5082953061929E13,\"max\":2898432,\"count\":20,\"mean\":497410.45,\"sum\":9948209,\"stddev\":730342.7438554899}}},\"mean\":82362.49162011173,\"sum\":1.4742886E7,\"stddev\":396612.3128060779}}},\"lastIndexedTx\":17}";
    public static String TEST_MIMETYPE = "{\"responseHeader\":{\"status\":0,\"QTime\":12},\"response\":{\"numFound\":188,\"start\":0,\"docs\":[]},\"stats\":{\"stats_fields\":{\"contentsize\":{\"min\":25.0,\"max\":3737049.0,\"sum\":1.4742886E7,\"count\":179,\"missing\":9,\"sumOfSquares\":2.9213896971782E13,\"mean\":82362.49162011173,\"stddev\":396612.3128060779,\"facets\":{\"@{http://www.alfresco.org/model/content/1.0}content.mimetype\":{\"image/jpeg\":{\"min\":37453.0,\"max\":540412.0,\"sum\":1903731.0,\"count\":8,\"missing\":0,\"sumOfSquares\":7.26285100833E11,\"mean\":237966.375,\"stddev\":197578.6048957568},\"application/vnd.ms-excel\":{\"min\":26112.0,\"max\":26112.0,\"sum\":26112.0,\"count\":1,\"missing\":0,\"sumOfSquares\":6.81836544E8,\"mean\":26112.0,\"stddev\":0.0},\"image/png\":{\"min\":10832.0,\"max\":777461.0,\"sum\":3050831.0,\"count\":12,\"missing\":0,\"sumOfSquares\":1.471646159497E12,\"mean\":254235.91666666666,\"stddev\":251543.47963521618},\"video/mp4\":{\"min\":3737049.0,\"max\":3737049.0,\"sum\":3737049.0,\"count\":1,\"missing\":0,\"sumOfSquares\":1.3965535228401E13,\"mean\":3737049.0,\"stddev\":0.0},\"text/plain\":{\"min\":55.0,\"max\":10734.0,\"sum\":377009.0,\"count\":106,\"missing\":0,\"sumOfSquares\":2.300122507E9,\"mean\":3556.688679245283,\"stddev\":3022.485361304367},\"application/msword\":{\"min\":73728.0,\"max\":74240.0,\"sum\":221696.0,\"count\":3,\"missing\":0,\"sumOfSquares\":1.6383213568E10,\"mean\":73898.66666666667,\"stddev\":295.6033378250884},\"text/xml\":{\"min\":262.0,\"max\":797.0,\"sum\":5701.0,\"count\":16,\"missing\":0,\"sumOfSquares\":2286857.0,\"mean\":356.3125,\"stddev\":130.51677733788352},\"text/html\":{\"min\":25.0,\"max\":3430.0,\"sum\":14437.0,\"count\":18,\"missing\":0,\"sumOfSquares\":2.4602173E7,\"mean\":802.0555555555555,\"stddev\":875.2444009748983},\"application/vnd.ms-powerpoint\":{\"min\":2117632.0,\"max\":2898432.0,\"sum\":5016064.0,\"count\":2,\"missing\":0,\"sumOfSquares\":1.2885273346048E13,\"mean\":2508032.0,\"stddev\":552108.9747504563},\"application/pdf\":{\"min\":381778.0,\"max\":381778.0,\"sum\":381778.0,\"count\":1,\"missing\":0,\"sumOfSquares\":1.45754441284E11,\"mean\":381778.0,\"stddev\":0.0},\"application/x-javascript\":{\"min\":118.0,\"max\":2271.0,\"sum\":8478.0,\"count\":11,\"missing\":0,\"sumOfSquares\":1.063407E7,\"mean\":770.7272727272727,\"stddev\":640.3002562718667}}}}}},\"lastIndexedTx\":17}";
    public static String TEST_CREATOR = "{\"responseHeader\":{\"status\":0,\"QTime\":7},\"response\":{\"numFound\":188,\"start\":0,\"docs\":[]},\"stats\":{\"stats_fields\":{\"contentsize\":{\"min\":25.0,\"max\":3737049.0,\"sum\":1.4742886E7,\"count\":179,\"missing\":9,\"sumOfSquares\":2.9213896971782E13,\"mean\":82362.49162011173,\"stddev\":396612.3128060779,\"facets\":{\"@{http://www.alfresco.org/model/content/1.0}creator\":{\"{en}abeecher\":{\"min\":153.0,\"max\":3737049.0,\"sum\":8073062.0,\"count\":22,\"missing\":9,\"sumOfSquares\":1.5659402413312E13,\"mean\":366957.36363636365,\"stddev\":777570.47958978},\"{en}admin\":{\"min\":262.0,\"max\":797.0,\"sum\":3920.0,\"count\":11,\"missing\":0,\"sumOfSquares\":1621982.0,\"mean\":356.3636363636364,\"stddev\":150.01218132356632},\"{en}system\":{\"min\":55.0,\"max\":10734.0,\"sum\":394038.0,\"count\":128,\"missing\":0,\"sumOfSquares\":2.319490622E9,\"mean\":3078.421875,\"stddev\":2951.678320419461},\"{en}mjackson\":{\"min\":25.0,\"max\":2898432.0,\"sum\":6271866.0,\"count\":18,\"missing\":0,\"sumOfSquares\":1.3552173445866E13,\"mean\":348437.0,\"stddev\":817702.062540975}}}}}},\"lastIndexedTx\":17}";
    public static String TEST_MODIFIER = "{\"responseHeader\":{\"status\":0,\"QTime\":9},\"response\":{\"numFound\":188,\"start\":0,\"docs\":[]},\"stats\":{\"stats_fields\":{\"contentsize\":{\"min\":25.0,\"max\":3737049.0,\"sum\":1.4742886E7,\"count\":179,\"missing\":9,\"sumOfSquares\":2.9213896971782E13,\"mean\":82362.49162011173,\"stddev\":396612.3128060779,\"facets\":{\"@{http://www.alfresco.org/model/content/1.0}modifier\":{\"{en}abeecher\":{\"min\":11585.0,\"max\":3737049.0,\"sum\":7690584.0,\"count\":18,\"missing\":4,\"sumOfSquares\":1.551364779523E13,\"mean\":427254.6666666667,\"stddev\":848105.4974553578},\"{en}admin\":{\"min\":151.0,\"max\":381778.0,\"sum\":392925.0,\"count\":19,\"missing\":5,\"sumOfSquares\":1.45772544831E11,\"mean\":20680.263157894737,\"stddev\":87447.36589314239},\"{en}system\":{\"min\":55.0,\"max\":10734.0,\"sum\":394038.0,\"count\":128,\"missing\":0,\"sumOfSquares\":2.319490622E9,\"mean\":3078.421875,\"stddev\":2951.678320419461},\"{en}mjackson\":{\"min\":25.0,\"max\":2898432.0,\"sum\":6265339.0,\"count\":14,\"missing\":0,\"sumOfSquares\":1.3552157141099E13,\"mean\":447524.21428571426,\"stddev\":909279.7753374479}}}}}},\"lastIndexedTx\":17}";
    public static String TEST_VERSIONLABEL_DOT = "{\"response\":{\"start\":0,\"docs\":[],\"numFound\":190},\"responseHeader\":{\"status\":0,\"QTime\":6},\"stats\":{\"stats_fields\":{\"contentsize\":{\"missing\":9,\"min\":25,\"sumOfSquares\":5.62876356840762E14,\"max\":3737049.0,\"count\":181,\"facets\":{\"@{http://www.alfresco.org/model/content/1.0}versionLabel.\":{}},\"mean\":82362.49162011173,\"sum\":1.4742886E7,\"stddev\":1748900.2773910002}}},\"lastIndexedTx\":44}";

    @Test
    public void testSolrStatsResult() throws JSONException
    {
        SolrStatsResult resultCreated = testProcessing(TEST_CREATED, 13, 7, 188);
        SolrStatsResult resultMimetype = testProcessing(TEST_MIMETYPE, 12, 11, 188);
        SolrStatsResult resultCreator = testProcessing(TEST_CREATOR, 7, 4, 188);
        SolrStatsResult resultMod = testProcessing(TEST_MODIFIER, 9, 4, 188);
        SolrStatsResult resultV = testProcessing(TEST_VERSIONLABEL_DOT, 6, 0, 190);
    }
    private SolrStatsResult testProcessing(String testData, long queryTime, int statsSize, long numberFound) throws JSONException
    {
        JSONObject json = new JSONObject(new JSONTokener(testData));
        SolrStatsResult result = new SolrStatsResult(json);

        assertNotNull(result);
        assertEquals(numberFound, result.getNumberFound());
        assertTrue(result.getStatus()==0);
        assertTrue(result.getQueryTime()==queryTime);
        assertTrue(result.getSum()==14742886);
        assertTrue(result.getMax()==3737049);
        assertTrue(result.getMean()==82362);
        assertEquals(statsSize, result.getStats().size());
        System.out.println(result);
        return result;
    }

}
