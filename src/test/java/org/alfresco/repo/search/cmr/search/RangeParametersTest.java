package org.alfresco.repo.search.cmr.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.search.RangeParameters;
import org.junit.Assert;
import org.junit.Test;

public class RangeParametersTest
{
    @Test
    public void startInclusiveTest()
    {
        RangeParameters p = new RangeParameters("test", "0", "10", "1", true, null, null, null, null);
        Assert.assertTrue(p.isRangeStartInclusive());
        p = new RangeParameters("test", "0", "10", "1", true, Collections.emptyList(), Collections.emptyList(), null, null);
        Assert.assertTrue(p.isRangeStartInclusive());
        List<String> includes = new ArrayList<String>();
        List<String> other = new ArrayList<String>();
        includes.add("upper");
        p = new RangeParameters("test", "0", "10", "1", true, Collections.emptyList(), includes, null, null);
        Assert.assertTrue(p.isRangeStartInclusive());
        other.add("before");
        p = new RangeParameters("test", "0", "10", "1", true, other, null, null, null);
        Assert.assertFalse(p.isRangeStartInclusive());
        p = new RangeParameters("test", "0", "10", "1", true, other, includes, null, null);
        Assert.assertFalse(p.isRangeStartInclusive());
    }
    @Test
    public void endInclusiveTest()
    {
        RangeParameters p = new RangeParameters("test", "0", "10", "1", true, null, null, null, null);
        Assert.assertFalse(p.isRangeEndInclusive());
        p = new RangeParameters("test", "0", "10", "1", true, Collections.emptyList(), Collections.emptyList(), null, null);
        Assert.assertFalse(p.isRangeEndInclusive());
        List<String> includes = new ArrayList<String>();
        List<String> other = new ArrayList<String>();
        includes.add("upper");
        p = new RangeParameters("test", "0", "10", "1", true, Collections.emptyList(), includes, null, null);
        Assert.assertTrue(p.isRangeEndInclusive());
        other.add("before");
        p = new RangeParameters("test", "0", "10", "1", true, other, null, null, null);
        Assert.assertFalse(p.isRangeEndInclusive());
    }
}
