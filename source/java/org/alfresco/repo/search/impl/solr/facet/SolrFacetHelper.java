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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
    private static final String CREATED_FIELD_FACET_QUERY = FQ_NS_PREFIX + "created";
    private static final String MODIFIED_FIELD_FACET_QUERY = FQ_NS_PREFIX + "modified";
    private static final String CONTENT_SIZE_FIELD_FACET_QUERY = FQ_NS_PREFIX + "content.size";

    // Content size buckets
    private static final int KB = 1024;
    private static final int MB = KB * 1024;
    private static final int TINY = 10 * KB;
    private static final int SMALL = 100 * KB;
    private static final int MEDIUM = MB;
    private static final int LARGE = 16 * MB;
    private static final int HUGE = 128 * MB;

    private static final String SIZE_BUCKETS_CACHE_KEY = "sizeBucketsCacheKey";
    
    /** Content size buckets */
    private static final List<String> CONTENT_SIZE_BUCKETS = new ArrayList<>(6);
    static
    {
        CONTENT_SIZE_BUCKETS.add("0 TO " + TINY);
        CONTENT_SIZE_BUCKETS.add(TINY + " TO " + SMALL);
        CONTENT_SIZE_BUCKETS.add(SMALL + " TO " + MEDIUM);
        CONTENT_SIZE_BUCKETS.add(MEDIUM + " TO " + LARGE);
        CONTENT_SIZE_BUCKETS.add(LARGE + " TO " + HUGE);
        CONTENT_SIZE_BUCKETS.add(HUGE + " TO MAX");
    }

    /** Field facet buckets */
    private static final Set<String> BUCKETED_FIELD_FACETS = new HashSet<>(3);
    static
    {
        BUCKETED_FIELD_FACETS.add(CREATED_FIELD_FACET_QUERY);
        BUCKETED_FIELD_FACETS.add(MODIFIED_FIELD_FACET_QUERY);
        BUCKETED_FIELD_FACETS.add(CONTENT_SIZE_FIELD_FACET_QUERY);
    }

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
        DateBucketsDisplayHandler dateBucketsDisplayHandler = new DateBucketsDisplayHandler();
        ContentSizeBucketsDisplayHandler contentSizeBucketsDisplayHandler = new ContentSizeBucketsDisplayHandler();
        
        this.displayHandlers.put("@{http://www.alfresco.org/model/content/1.0}creator.__", userNameDisplayHandler);
        this.displayHandlers.put("@{http://www.alfresco.org/model/content/1.0}modifier.__", userNameDisplayHandler);
        this.displayHandlers.put("@{http://www.alfresco.org/model/content/1.0}content.mimetype", mimetypeDisplayHandler);
        this.displayHandlers.put(CREATED_FIELD_FACET_QUERY, dateBucketsDisplayHandler);
        this.displayHandlers.put(MODIFIED_FIELD_FACET_QUERY, dateBucketsDisplayHandler);
        this.displayHandlers.put(CONTENT_SIZE_FIELD_FACET_QUERY, contentSizeBucketsDisplayHandler);
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
            facetQueries.add(CREATED_FIELD_FACET_QUERY + ":[" + bucket + ']');
            facetQueries.add(MODIFIED_FIELD_FACET_QUERY + ":[" + bucket + ']');
        }

        // Content size facet query
        for (String bucket : CONTENT_SIZE_BUCKETS)
        {
            facetQueries.add(CONTENT_SIZE_FIELD_FACET_QUERY + ":[" + bucket + ']');
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
     * Gets predefined set of field facets which are used to construct bucketing
     * 
     * @return an unmodifiable view of the set of predefined field facets
     */
    public Set<String> getBucketedFieldFacets()
    {
        return Collections.unmodifiableSet(BUCKETED_FIELD_FACETS);
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
     * @return Map of {@literal <date range, (display label key, insertion index)>}
     */
    private static Map<String, Pair<String, Integer>> makeDateBucketsDisplayLabel(LocalDate date)
    {
        List<String> dateBuckets = makeDateBuckets(date);
        Map<String, Pair<String, Integer>> bucketDisplayName = new HashMap<>(5);

        if (dateBuckets.size() != 5)
        {
            throw new AlfrescoRuntimeException("Date buckets size does not match the bucket display label size!");
        }

        bucketDisplayName.put(dateBuckets.get(0), new Pair<String, Integer>("faceted-search.date.one-day.label", 0));
        bucketDisplayName.put(dateBuckets.get(1), new Pair<String, Integer>("faceted-search.date.one-week.label", 1));
        bucketDisplayName.put(dateBuckets.get(2), new Pair<String, Integer>("faceted-search.date.one-month.label", 2));
        bucketDisplayName.put(dateBuckets.get(3), new Pair<String, Integer>("faceted-search.date.six-months.label", 3));
        bucketDisplayName.put(dateBuckets.get(4), new Pair<String, Integer>("faceted-search.date.one-year.label", 4));

        return bucketDisplayName;
    }

    /**
     * Creates display name for the Content size buckets.
     * 
     * @return Map of {@literal <size range, (display label key, insertion index)>}
     */
    private static Map<String, Pair<String, Integer>> makeContentSizeBucketsDisplayLabel()
    {
        Map<String, Pair<String, Integer>> bucketDisplayName = new HashMap<>(6);

        if (CONTENT_SIZE_BUCKETS.size() != 6)
        {
            throw new AlfrescoRuntimeException("Content size buckets size does not match the bucket display label size!");
        }

        bucketDisplayName.put(CONTENT_SIZE_BUCKETS.get(0), new Pair<String, Integer>("faceted-search.size.0-10KB.label", 0));
        bucketDisplayName.put(CONTENT_SIZE_BUCKETS.get(1), new Pair<String, Integer>("faceted-search.size.10-100KB.label", 1));
        bucketDisplayName.put(CONTENT_SIZE_BUCKETS.get(2), new Pair<String, Integer>("faceted-search.size.100KB-1MB.label", 2));
        bucketDisplayName.put(CONTENT_SIZE_BUCKETS.get(3), new Pair<String, Integer>("faceted-search.size.1-16MB.label", 3));
        bucketDisplayName.put(CONTENT_SIZE_BUCKETS.get(4), new Pair<String, Integer>("faceted-search.size.16-128MB.label", 4));
        bucketDisplayName.put(CONTENT_SIZE_BUCKETS.get(5), new Pair<String, Integer>("faceted-search.size.over128.label", 5));

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
    private static class FacetQueryResultDateBuckets implements Buckets<LocalDate, Map<String, Pair<String, Integer>>>
    {
        @Override
        public Map<String, Pair<String, Integer>> compute(LocalDate localDate) throws Exception
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
    private static class FacetQueryResultContentSizeBuckets implements Buckets<String, Map<String, Pair<String, Integer>>>
    {
        @Override
        public Map<String, Pair<String, Integer>> compute(String arg) throws Exception
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
        FacetLabel getDisplayLabel(String value);
    }
    
    /**
     * A class to encapsulate the result of the facet label display handler
     * 
     * @author Jamal Kaabi-Mofrad
     */
    public static class FacetLabel
    {
        private final String value;
        private final String label;
        private final int labelIndex;

        /**
         * @param value
         * @param label
         * @param labelIndex
         */
        public FacetLabel(String value, String label, int labelIndex)
        {
            this.value = value;
            this.label = label;
            this.labelIndex = labelIndex;
        }

        /**
         * Gets the original facet value or a new modified value
         * 
         * @return the original facet value or a new modified value
         */
        public String getValue()
        {
            return this.value;
        }

        /**
         * Gets the facet display label
         * 
         * @return the label
         */
        public String getLabel()
        {
            return this.label;
        }

        /**
         * Gets the label index to be used for sorting. The index only relevant
         * to to Date and Size facets.
         * 
         * @return the index or -1, if it isn't relevant to the facet label
         */
        public int getLabelIndex()
        {
            return this.labelIndex;
        }

        /*
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.label == null) ? 0 : this.label.hashCode());
            result = prime * result + this.labelIndex;
            result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
            return result;
        }

        /*
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof FacetLabel))
                return false;
            FacetLabel other = (FacetLabel) obj;
            if (this.label == null)
            {
                if (other.label != null)
                    return false;
            }
            else if (!this.label.equals(other.label))
                return false;
            if (this.labelIndex != other.labelIndex)
                return false;
            if (this.value == null)
            {
                if (other.value != null)
                    return false;
            }
            else if (!this.value.equals(other.value))
                return false;
            return true;
        }
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
        public FacetLabel getDisplayLabel(String value)
        {
            String name = null;

            final NodeRef personRef = personService.getPersonOrNull(value);
            if (personRef != null)
            {
                final String firstName = (String) nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
                final String lastName = (String) nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);
                name = (firstName != null ? firstName + " " : "") + (lastName != null ? lastName : "");
            }
            return new FacetLabel(value, name == null ? value : name.trim(), -1);
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
        public FacetLabel getDisplayLabel(String value)
        {
            Map<String, String> mimetypes = mimetypeService.getDisplaysByMimetype();
            String displayName = mimetypes.get(value);
            return new FacetLabel(value, displayName == null ? value : displayName.trim(), -1);
        }
    }

    /**
     * A simple handler to get the appropriate display label for the date buckets.
     * 
     * @author Jamal Kaabi-Mofrad
     */
    public static class DateBucketsDisplayHandler implements FacetLabelDisplayHandler
    {
        private final BucketsCache<LocalDate, Map<String, Pair<String, Integer>>> cache = new BucketsCache<>(
                    new FacetQueryResultDateBuckets());

        @Override
        public FacetLabel getDisplayLabel(String value)
        {
            Map<String, Pair<String, Integer>> dateBuckets = null;

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
            String newValue = lowerUpperDates[0] + "\"..\"" + lowerUpperDates[1];
            Pair<String, Integer> labelIndexPair = dateBuckets.get(dateRange);
            return new FacetLabel(newValue, labelIndexPair.getFirst(), labelIndexPair.getSecond());
        }
    }

    /**
     * A simple handler to get the appropriate display label for the content size buckets.
     * 
     * @author Jamal Kaabi-Mofrad
     */
    public static class ContentSizeBucketsDisplayHandler implements FacetLabelDisplayHandler
    {
        private final BucketsCache<String, Map<String, Pair<String, Integer>>> cache = new BucketsCache<>(
                    new FacetQueryResultContentSizeBuckets());

        @Override
        public FacetLabel getDisplayLabel(String value)
        {
            String sizeRange = value.substring(value.indexOf('[') + 1, value.length() - 1);
            String[] lowerUppperSize = sizeRange.split("\\sTO\\s");

            Map<String, Pair<String, Integer>> sizeBuckets;
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

            String newValue = lowerUppperSize[0] + "\"..\"" + lowerUppperSize[1];
            Pair<String, Integer> labelIndexPair = sizeBuckets.get(sizeRange);
            return new FacetLabel(newValue, labelIndexPair.getFirst(), labelIndexPair.getSecond());
        }
    }
}
