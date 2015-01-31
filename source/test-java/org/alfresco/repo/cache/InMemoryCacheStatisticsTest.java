package org.alfresco.repo.cache;

import static org.junit.Assert.*;

import java.util.Map;

import org.alfresco.repo.cache.TransactionStats.OpType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

/**
 * Tests for the {@link InMemoryCacheStatistics} class.
 * 
 * @since 5.0
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class InMemoryCacheStatisticsTest
{
    InMemoryCacheStatistics cacheStats;
    @Mock ApplicationContext appCtx;
    
    @Before
    public void setUp() throws Exception
    {
        cacheStats = new InMemoryCacheStatistics();
        cacheStats.setApplicationContext(appCtx);
    }

    @Test
    public void readOperationsThrowNoCacheStatsException()
    {
        try
        {
            cacheStats.count("cache1", OpType.GET_HIT);
            fail("NoStatsForCache should have been thrown.");
        }
        catch(NoStatsForCache e)
        {
            // Good.
        }
        try
        {
            cacheStats.hitMissRatio("cache1");
            fail("NoStatsForCache should have been thrown.");
        }
        catch(NoStatsForCache e)
        {
            // Good.
        }
        try
        {
            cacheStats.meanTime("cache1", OpType.GET_HIT);
            fail("NoStatsForCache should have been thrown.");
        }
        catch(NoStatsForCache e)
        {
            // Good.
        }
        try
        {
            cacheStats.numGets("cache1");
            fail("NoStatsForCache should have been thrown.");
        }
        catch(NoStatsForCache e)
        {
            // Good.
        }
        try
        {
            cacheStats.allStats("cache1");
            fail("NoStatsForCache should have been thrown.");
        }
        catch(NoStatsForCache e)
        {
            // Good.
        }
    }
    
    @Test
    public void canAccumulateStatisticsPerCache()
    {
        TransactionStats txStats = new TransactionStats();
        txStats.record(0, 1000, OpType.GET_HIT);
        txStats.record(0, 2000, OpType.GET_HIT);
        txStats.record(0, 3000, OpType.GET_HIT);
        
        // Add first statistical datapoint.
        cacheStats.add("cache1", txStats);
        
        Mockito.verify(appCtx).publishEvent(Mockito.any(CacheStatisticsCreated.class));
        
        // Cache stats should be visible
        assertEquals(3, cacheStats.count("cache1", OpType.GET_HIT));
        assertEquals(2000, cacheStats.meanTime("cache1", OpType.GET_HIT), 0.0d);
        
        // A new transaction
        txStats = new TransactionStats();
        txStats.record(0, 4000, OpType.GET_HIT);
        txStats.record(0, 5000, OpType.GET_HIT);
        
        // Central cache stats should not yet include new transaction's stats.
        assertEquals(3, cacheStats.count("cache1", OpType.GET_HIT));
        
        cacheStats.add("cache1", txStats);
        
        // New TX's stats should now be visible
        assertEquals(5, cacheStats.count("cache1", OpType.GET_HIT));
        assertEquals(3000, cacheStats.meanTime("cache1", OpType.GET_HIT), 0.0d);
        
        // A different cache
        try
        {
            cacheStats.count("cache2", OpType.GET_HIT);
            fail("Expected NoStatsForCache error.");
        }
        catch(NoStatsForCache e)
        {
            // Good
        }
        txStats = new TransactionStats();
        txStats.record(0, 4000, OpType.GET_HIT);
        txStats.record(8000, 9000, OpType.GET_HIT);
        cacheStats.add("cache2", txStats);
        assertEquals(2, cacheStats.count("cache2", OpType.GET_HIT));
        assertEquals(2500, cacheStats.meanTime("cache2", OpType.GET_HIT), 0.0d);
        // cache2 should NOT affect cache1
        assertEquals(5, cacheStats.count("cache1", OpType.GET_HIT));
        assertEquals(3000, cacheStats.meanTime("cache1", OpType.GET_HIT), 0.0d);
        
        // Some non-hit statistics
        txStats = new TransactionStats();
        txStats.record(0, 810, OpType.GET_MISS);
        txStats.record(1000, 1820, OpType.PUT);
        txStats.record(3000, 3830, OpType.REMOVE);
        txStats.record(4000, 4840, OpType.CLEAR);
        cacheStats.add("cache1", txStats);
        // Hits haven't changed
        assertEquals(5, cacheStats.count("cache1", OpType.GET_HIT));
        assertEquals(3000, cacheStats.meanTime("cache1", OpType.GET_HIT), 0.0d);
        
        // Other stats have changed
        assertEquals(1, cacheStats.count("cache1", OpType.GET_MISS));
        assertEquals(810, cacheStats.meanTime("cache1", OpType.GET_MISS), 0.01d);
        
        assertEquals(1, cacheStats.count("cache1", OpType.PUT));
        assertEquals(820, cacheStats.meanTime("cache1", OpType.PUT), 0.01d);
        
        assertEquals(1, cacheStats.count("cache1", OpType.REMOVE));
        assertEquals(830, cacheStats.meanTime("cache1", OpType.REMOVE), 0.01d);
        
        assertEquals(1, cacheStats.count("cache1", OpType.CLEAR));
        assertEquals(840, cacheStats.meanTime("cache1", OpType.CLEAR), 0.01d);
        
        // Check hit ratio
        assertEquals(5, cacheStats.count("cache1", OpType.GET_HIT));
        assertEquals(1, cacheStats.count("cache1", OpType.GET_MISS));
        assertEquals(0.83, cacheStats.hitMissRatio("cache1"), 0.01d);
        
        // Check hit+miss count
        assertEquals(6, cacheStats.numGets("cache1"));
        
        // Stats snapshot map
        Map<OpType, OperationStats> snapshot = cacheStats.allStats("cache1");
        assertEquals(5, snapshot.get(OpType.GET_HIT).getCount());
        assertEquals(1, snapshot.get(OpType.GET_MISS).getCount());
    }
    
    @Test
    public void canRetrieveSnapshotOfAllStats()
    {
        TransactionStats txStats = new TransactionStats();
        txStats.record(0, 1000, OpType.GET_HIT);
        
        // Add first statistical datapoint.
        cacheStats.add("cache1", txStats);
        
        // Cache stats should be visible
        Map<OpType, OperationStats> snapshot1 = cacheStats.allStats("cache1");
        assertEquals(1, snapshot1.get(OpType.GET_HIT).getCount());
        assertEquals(1000, snapshot1.get(OpType.GET_HIT).getTotalTime(), 0.0d);
        // Map is fully populated
        assertEquals(0, snapshot1.get(OpType.CLEAR).getCount());
        assertEquals(0, snapshot1.get(OpType.PUT).getCount());
        assertEquals(0, snapshot1.get(OpType.REMOVE).getCount());
        assertEquals(0, snapshot1.get(OpType.GET_MISS).getCount());
        
        // Record further data
        txStats = new TransactionStats();
        txStats.record(0, 2000, OpType.GET_HIT);
        cacheStats.add("cache1", txStats);
        
        // Check new snapshot reflects update
        Map<OpType, OperationStats> snapshot2 = cacheStats.allStats("cache1");
        assertEquals(2, snapshot2.get(OpType.GET_HIT).getCount());
        assertEquals(3000, snapshot2.get(OpType.GET_HIT).getTotalTime(), 0.0d);
        
        // Check old snapshot is not affected
        assertEquals(1, snapshot1.get(OpType.GET_HIT).getCount());
        assertEquals(1000, snapshot1.get(OpType.GET_HIT).getTotalTime(), 0.0d);
    }
}
