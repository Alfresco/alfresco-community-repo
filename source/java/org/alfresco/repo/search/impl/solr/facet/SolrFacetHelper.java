/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.search.impl.solr.facet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;

/**
 * A helper class to overcome the limitation of Solr 1.4 for dealing with facets.
 * <p>
 * Notice: probably this class or most of its functionalities will be removed
 * when we upgrade to Solr 4
 * 
 * @author Jamal Kaabi-Mofrad
 */
// TODO use Solr4 date math for date buckets...
public class SolrFacetHelper
{
    private static Log logger = LogFactory.getLog(SolrFacetHelper.class);

    private static final String FQ_NS_PREFIX = "@{http://www.alfresco.org/model/content/1.0}";
    private static final String CREATED_FACET_QUERY_PREFIX = FQ_NS_PREFIX + "created:";
    private static final String MODIFIED_FACET_QUERY_PREFIX = FQ_NS_PREFIX + "modified:";
    private static final String CONTENT_SIZE_FACET_QUERY_PREFIX = FQ_NS_PREFIX + "content.size:";

    // Content size buckets
    private static final int KB = 1024;
    private static final int MB = KB * 1024;
    private static final int TINY = 10 * KB;
    private static final int SMALL = 100 * KB;
    private static final int MEDIUM = MB;
    private static final int LARGE = 16 * MB;
    private static final int HUGE = 128 * MB;

    private static final String SIZE_BUCKETS_CACHE_KEY = "sizeBucketsCacheKey";

    /** Facet value and facet query display label handlers */
    private Map<String, FacetLabelDisplayHandler> displayHandlers;

    /** Thread safe cache for storing the Date buckets facet query */
    private BucketsCache<LocalDate, List<String>> fqDateCache = null;
    
    /**
     * Constructor
     * 
     * @param serviceRegistry
     */
    public SolrFacetHelper(ServiceRegistry serviceRegistry)
    {
        this.fqDateCache = new BucketsCache<>(new FacetQueryParamDateBuckets());
        this.displayHandlers = new HashMap<>(6);
        
        UserNameDisplayHandler userNameDisplayHandler = new UserNameDisplayHandler(serviceRegistry);
        MimetypeDisplayHandler mimetypeDisplayHandler = new MimetypeDisplayHandler(serviceRegistry);
        DateBucketsDisplayHandler createdDateBucketsDisplayHandler = new DateBucketsDisplayHandler("cm:created");
        DateBucketsDisplayHandler modifiedDateBucketsDisplayHandler = new DateBucketsDisplayHandler("cm:modified");
        ContentSizeBucketsDisplayHandler contentSizeBucketsDisplayHandler = new ContentSizeBucketsDisplayHandler("cm:content.size");
        
        this.displayHandlers.put("@{http://www.alfresco.org/model/content/1.0}creator.__", userNameDisplayHandler);
        this.displayHandlers.put("@{http://www.alfresco.org/model/content/1.0}modifier.__", userNameDisplayHandler);
        this.displayHandlers.put("@{http://www.alfresco.org/model/content/1.0}content.mimetype", mimetypeDisplayHandler);
        this.displayHandlers.put("@{http://www.alfresco.org/model/content/1.0}created", createdDateBucketsDisplayHandler);
        this.displayHandlers.put("@{http://www.alfresco.org/model/content/1.0}modified", modifiedDateBucketsDisplayHandler);
        this.displayHandlers.put("@{http://www.alfresco.org/model/content/1.0}content.size", contentSizeBucketsDisplayHandler);
    }
    
    /**
     * Gets predefined set of facet queries. Currently the facet queries are:
     * <li>Created date buckets</li>
     * <li>Modified date buckets</li>
     * <li>Content size buckets</li>
     * 
     * @return list of facet queries
     */
    public List<String> getFacetQueries()
    {
        List<String> facetQueries = new ArrayList<>();
        List<String> dateBuckets = null;

        try
        {
            dateBuckets = fqDateCache.getRangeBuckets(LocalDate.now());
        }
        catch (Exception e)
        {
            logger.error(
                        "Error occured while trying to get the date buckets from the cache. Calculating the dates without the cache.", e);
            dateBuckets = makeDateBuckets(LocalDate.now());
        }

        // Created and Modified dates facet queries
        for (String bucket : dateBuckets)
        {
            facetQueries.add(CREATED_FACET_QUERY_PREFIX + '[' + bucket + ']');
            facetQueries.add(MODIFIED_FACET_QUERY_PREFIX + '[' + bucket + ']');
        }

        // Content size facet query
        for (String bucket : makeContentSizeBuckets())
        {
            facetQueries.add(CONTENT_SIZE_FACET_QUERY_PREFIX + '[' + bucket + ']');
        }

        return facetQueries;
    }

    /**
     * Gets the appropriate facet display label handler
     * 
     * @param qName
     * @return the diplayHandler object or null if there is no handler
     *         registered for the given @{code qName}
     */
    public FacetLabelDisplayHandler getDisplayHandler(String qName)
    {
        return displayHandlers.get(qName);
    }

    /**
     * Creates Date buckets. The dates are in ISO8601 format (yyyy-MM-dd)
     * 
     * @return list of date ranges. e.g. "2014-04-28 TO 2014-04-29"
     */
    private static List<String> makeDateBuckets(LocalDate currentDate)
    {
        List<String> list = new ArrayList<>(5);

        String nowStr = " TO " + currentDate.toString();

        // Bucket => yesterday TO today
        list.add(currentDate.minusDays(1).toString() + nowStr);

        // Bucket => Last week TO today
        list.add(currentDate.minusWeeks(1).toString() + nowStr);

        // Bucket => Last month TO today
        list.add(currentDate.minusMonths(1).toString() + nowStr);

        // Bucket => Last 6 months TO today
        list.add(currentDate.minusMonths(6).toString() + nowStr);

        // Bucket => Last year TO today
        list.add(currentDate.minusYears(1).toString() + nowStr);

        return list;
    }

    /**
     * Creates display name for the Date buckets.
     * 
     * @return Map of {@literal <date range, display label key>}
     */
    private static Map<String, String> makeDateBucketsDisplayLabel(LocalDate date)
    {
        List<String> dateBuckets = makeDateBuckets(date);
        Map<String, String> bucketDisplayName = new HashMap<>(5);

        if (dateBuckets.size() != 5)
        {
            throw new AlfrescoRuntimeException("Date buckets size does not match the bucket display label size!");
        }

        bucketDisplayName.put(dateBuckets.get(0), "faceted-search.date.one-day.label");
        bucketDisplayName.put(dateBuckets.get(1), "faceted-search.date.one-week.label");
        bucketDisplayName.put(dateBuckets.get(2), "faceted-search.date.one-month.label");
        bucketDisplayName.put(dateBuckets.get(3), "faceted-search.date.six-months.label");
        bucketDisplayName.put(dateBuckets.get(4), "faceted-search.date.one-year.label");

        return bucketDisplayName;
    }

    /**
     * Creates Content size buckets
     * 
     * @return list of size ranges. e.g. "0 TO 1024"
     */
    private static List<String> makeContentSizeBuckets()
    {
        List<String> sizeBuckets = new ArrayList<>(6);
        sizeBuckets.add("0 TO " + TINY);
        sizeBuckets.add(TINY + " TO " + SMALL);
        sizeBuckets.add(SMALL + " TO " + MEDIUM);
        sizeBuckets.add(MEDIUM + " TO " + LARGE);
        sizeBuckets.add(LARGE + " TO " + HUGE);
        sizeBuckets.add(HUGE + " TO MAX");

        return sizeBuckets;
    }

    /**
     * Creates display name for the Content size buckets.
     * 
     * @return Map of {@literal <size range, display label key>}
     */
    private static Map<String, String> makeContentSizeBucketsDisplayLabel()
    {
        List<String> sizeBuckets = makeContentSizeBuckets();
        Map<String, String> bucketDisplayName = new HashMap<>(6);

        if (sizeBuckets.size() != 6)
        {
            throw new AlfrescoRuntimeException("Content size buckets size does not match the bucket display label size!");
        }

        bucketDisplayName.put(sizeBuckets.get(0), "faceted-search.size.0-10KB.label");
        bucketDisplayName.put(sizeBuckets.get(1), "faceted-search.size.10-100KB.label");
        bucketDisplayName.put(sizeBuckets.get(2), "faceted-search.size.100KB-1MB.label");
        bucketDisplayName.put(sizeBuckets.get(3), "faceted-search.size.1-16MB.label");
        bucketDisplayName.put(sizeBuckets.get(4), "faceted-search.size.16-128MB.label");
        bucketDisplayName.put(sizeBuckets.get(5), "faceted-search.size.over128.label");

        return bucketDisplayName;
    }

    /**
     * Single value cache for date and size buckets.
     * 
     * @author Jamal Kaabi-Mofrad
     */
    private static class BucketsCache<K, V>
    {
        private final ConcurrentMap<K, Future<V>> cache = new ConcurrentHashMap<>();

        private final Buckets<K, V> buckets;

        public BucketsCache(Buckets<K, V> buckets)
        {
            this.buckets = buckets;
        }

        public V getRangeBuckets(final K arg) throws Exception
        {
            while (true)
            {
                Future<V> future = cache.get(arg);
                // first checks to see if the buckets computation has been started
                if (future == null)
                {
                    Callable<V> result = new Callable<V>()
                    {
                        public V call() throws Exception
                        {
                            // remove the previous entry
                            Set<K> keys = cache.keySet();
                            for (K key : keys)
                            {
                                if (!key.equals(arg))
                                {
                                    cache.remove(key);
                                }
                            }
                            return buckets.compute(arg);
                        }
                    };
                    // If the calculation has been started, creates a
                    // FutureTask, registers it in the Map, and starts the computation
                    FutureTask<V> futureTask = new FutureTask<>(result);

                    future = cache.putIfAbsent(arg, futureTask);
                    if (future == null)
                    {
                        future = futureTask;
                        futureTask.run();
                    }
                }
                try
                {
                    return future.get();
                }
                catch (CancellationException ce)
                {
                    // Removes cache pollution. If the calculation is cancelled
                    // or failed. As caching a Future instead of a value creates
                    // the possibility of cache pollution
                    cache.remove(arg, future);
                }
                catch (ExecutionException e)
                {
                    new IllegalStateException(e);
                }
            }
        }
    }

    /**
     * Interface to be implemented by classes that wish to create buckets. 
     *
     * @author Jamal Kaabi-Mofrad
     */
    private static interface Buckets<K, V>
    {
        V compute(K arg) throws Exception;
    }

    /**
     * A simple implementation which creates Date buckets for the facet query.
     * 
     * @author Jamal Kaabi-Mofrad
     */
    private static class FacetQueryParamDateBuckets implements Buckets<LocalDate, List<String>>
    {
        @Override
        public List<String> compute(LocalDate localDate) throws Exception
        {
            return makeDateBuckets(localDate);
        }
    }

    /**
     * A simple implementation which creates display label for the Date buckets
     * from the facet query result.
     * 
     * @author Jamal Kaabi-Mofrad
     */
    private static class FacetQueryResultDateBuckets implements Buckets<LocalDate, Map<String, String>>
    {
        @Override
        public Map<String, String> compute(LocalDate localDate) throws Exception
        {
            return makeDateBucketsDisplayLabel(localDate);
        }
    }

    /**
     * A simple implementation which creates display label for the Content size
     * buckets from the facet query result.
     * 
     * @author Jamal Kaabi-Mofrad
     */
    private static class FacetQueryResultContentSizeBuckets implements Buckets<String, Map<String, String>>
    {
        @Override
        public Map<String, String> compute(String arg) throws Exception
        {
            return makeContentSizeBucketsDisplayLabel();
        }
    }

    /**
     * Solr facet value and facet query result display label handler
     * 
     * @author Jamal Kaabi-Mofrad
     */
    public static interface FacetLabelDisplayHandler
    {
        Pair<String, String> getDisplayLabel(String value);
    }

    /**
     * A simple handler to get the full user name from the userID
     * 
     * @author Jamal Kaabi-Mofrad
     */
    public static class UserNameDisplayHandler implements FacetLabelDisplayHandler
    {
        private final PersonService personService;
        private final NodeService nodeService;

        public UserNameDisplayHandler(ServiceRegistry services)
        {
            this.personService = services.getPersonService();
            this.nodeService = services.getNodeService();
        }

        @Override
        public Pair<String, String> getDisplayLabel(String value)
        {
            String name = null;

            final NodeRef personRef = personService.getPersonOrNull(value);
            if (personRef != null)
            {
                final String firstName = (String) nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
                final String lastName = (String) nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);
                name = (firstName != null ? firstName + " " : "") + (lastName != null ? lastName : "");
            }
            return new Pair<String, String>(value, name == null ? value : name.trim());
        }
    }

    /**
     * A simple handler to get the Mimetype display label.
     * 
     * @author Jamal Kaabi-Mofrad
     */
    public static class MimetypeDisplayHandler implements FacetLabelDisplayHandler
    {
        private final MimetypeService mimetypeService;

        public MimetypeDisplayHandler(ServiceRegistry services)
        {
            this.mimetypeService = services.getMimetypeService();
        }

        @Override
        public Pair<String, String> getDisplayLabel(String value)
        {
            Map<String, String> mimetypes = mimetypeService.getDisplaysByMimetype();
            String displayName = mimetypes.get(value);
            return new Pair<String, String>(value, displayName == null ? value : displayName.trim());
        }
    }

    /**
     * A simple handler to get the appropriate display label for the date buckets.
     * 
     * @author Jamal Kaabi-Mofrad
     */
    public static class DateBucketsDisplayHandler implements FacetLabelDisplayHandler
    {
        private final BucketsCache<LocalDate, Map<String, String>> cache = new BucketsCache<>(
                    new FacetQueryResultDateBuckets());
        private final String fq;

        public DateBucketsDisplayHandler(String fq)
        {
            this.fq = fq;
        }

        @Override
        public Pair<String, String> getDisplayLabel(String value)
        {
            Map<String, String> dateBuckets = null;

            String dateRange = value.substring(value.indexOf('[') + 1, value.length() - 1);
            String[] lowerUpperDates = dateRange.split("\\sTO\\s");
            LocalDate date = LocalDate.parse(lowerUpperDates[1]);
            try
            {
                dateBuckets = cache.getRangeBuckets(date);
            }
            catch (Exception e)
            {
                logger.error(
                            "Error occurred while trying to get the date buckets from the cache. Calculating the dates without the cache.", e);
                dateBuckets = makeDateBucketsDisplayLabel(date);
            }
            String newValue = fq + ":\"" + lowerUpperDates[0] + "\"..\"" + lowerUpperDates[1] + '"';
            String label = dateBuckets.get(dateRange);
            return new Pair<String, String>(newValue, label);
        }
    }

    /**
     * A simple handler to get the appropriate display label for the content size buckets.
     * 
     * @author Jamal Kaabi-Mofrad
     */
    public static class ContentSizeBucketsDisplayHandler implements FacetLabelDisplayHandler
    {
        private final BucketsCache<String, Map<String, String>> cache = new BucketsCache<>(
                    new FacetQueryResultContentSizeBuckets());
        private final String fq;

        public ContentSizeBucketsDisplayHandler(String fq)
        {
            this.fq = fq;
        }

        @Override
        public Pair<String, String> getDisplayLabel(String value)
        {
            String sizeRange = value.substring(value.indexOf('[') + 1, value.length() - 1);
            String[] lowerUppperSize = sizeRange.split("\\sTO\\s");

            Map<String, String> sizeBuckets;
            try
            {
                sizeBuckets = cache.getRangeBuckets(SIZE_BUCKETS_CACHE_KEY);
            }
            catch (Exception e)
            {
                logger.error(
                            "Error occurred while trying to get the content size buckets from the cache. Calculating the size without the cache.", e);
                sizeBuckets = makeContentSizeBucketsDisplayLabel();
            }

            String newValue = fq + ":\"" + lowerUppperSize[0] + "\"..\"" + lowerUppperSize[1] + '"';
            String label = sizeBuckets.get(sizeRange);
            return new Pair<String, String>(newValue, label);
        }
    }
}
