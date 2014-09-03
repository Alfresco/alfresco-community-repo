/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.alfresco.service.cmr.repository.TransformationOptionLimits;
import org.alfresco.service.cmr.repository.TransformationOptions;

/**
 * Provides access to transformer configuration and current performance data.
 * 
 * @author Alan Davis
 */
public interface TransformerConfig
{
    /**
     * Wild card mimetype and mimetype extension.
     */
    public static final String ANY = "*";
    
    /**
     * Prefix before the transformer name of all property names that contain transformer
     * information
     */
    static final String CONTENT = "content.";
    
    /**
     * Prefix for all transformer names
     */
    static final String TRANSFORMER = "transformer.";
    
    /**
     * The combined content and transformer name prefix of for all property names that contain
     * transformer information
     */
    static final String PREFIX = CONTENT+TRANSFORMER;
    
    /**
     * The 'transformer name' for system wide defaults for all transformers
     */
    static final String DEFAULT_TRANSFORMER = TRANSFORMER+"default";
    
    /**
     * Name given to the 'SUMMARY' dummy (does not exist) transformer that gathers data
     * from all root level transformations 
     */
    static final String SUMMARY_TRANSFORMER_NAME = "SUMMARY";
    
    /**
     * An optional separator appended after the normal suffix and following value that
     * identifies the 'use' or 'application' of the property. Example uses include 'index'
     * 'doclib' and 'preview'. The corresponding configuration value is only used in the
     * context of the specified usage.
     */
    static final String USE = ".use.";
    
    /**
     * The separator between the transformer name and two mimetype extensions in a property name.
     */
    static final String EXTENSIONS = ".extensions.";
    
    /**
     * The separator between the transformer name and wildcarded mimetypes rather than extensions in a property name.
     * Effectively equivalent to multiple properties using the {@link #EXTENSIONS}.
     */
    static final String MIMETYPES = ".mimetypes.";
    
    /**
     * Both extension and minetype separators.
     */
    public static String[] SEPARATORS = new String[] {EXTENSIONS , MIMETYPES};

    /**
     * The suffix to property names for creating dynamic complex transformers
     */
    public static final String PIPELINE = ".pipeline";

    /**
     * The suffix to property names for creating dynamic failover transformers
     */
    public static final String FAILOVER = ".failover";

    /**
     * The suffix to property names to indicate that a transformer is available.
     * If not specified, defaults to true, indicating it may be selected rather
     * only being available as a component of another transformer.
     */
    public static final String AVAILABLE = ".available";

    /**
     * Separator between transformers and mimetype extensions in a dynamic compound property value.
     */
    public static final char PIPE = '|';

    /**
     * The suffix to property names for supported and unsupported combinations.
     */
    static final String SUPPORTED = ".supported";
    
    /**
     * The suffix to property names for the priority.
     */
    static final String PRIORITY = ".priority";
    
    /**
     * The suffix to property names to indicate which Alfresco version the transformer is
     * available with. If not specified it is not restricted. So if set to "Enterprise" it
     * is not available to Community.
     * @see AMP
     */
    static final String EDITION = ".edition";
    
    /**
     * The suffix to property names to indicate which Alfresco AMPs the transformer is
     * available with. The value should be the AMP's ID. If not specified it is not restricted.
     * @see #EDITION
     */
    static final String AMP = ".amp";
    
    /**
     * The suffix to property names for the threshold count.
     */
    static final String THRESHOLD_COUNT = ".thresholdCount";
    
    /**
     * The suffix to property names for the error time.
     */
    static final String ERROR_TIME = ".errorTime";
    
    /**
     * The suffix to property names for the the average time. Only used in the initial setup of
     * TransformerData. This is a historical property used by the 'Transformation Server' to set
     * an effective priority.
     */
    static final String INITIAL_TIME = ".time";
    
    /**
     * The suffix to property names for the the average count. Only used in the initial setup of
     * TransformerData. This is a historical property used by the 'Transformation Server' to set
     * an effective priority.
     */
    static final String INITIAL_COUNT = ".count";
    
    /**
     * Suffixes for limits.
     */
    static final String MAX_SOURCE_SIZE_K_BYTES = '.'+TransformationOptionLimits.OPT_MAX_SOURCE_SIZE_K_BYTES;
    static final String TIMEOUT_MS = '.'+TransformationOptionLimits.OPT_TIMEOUT_MS;
    static final String MAX_PAGES = '.'+TransformationOptionLimits.OPT_MAX_PAGES;
    static final String READ_LIMIT_K_BYTES = '.'+TransformationOptionLimits.OPT_READ_LIMIT_K_BYTES;
    static final String READ_LIMIT_TIME_MS = '.'+TransformationOptionLimits.OPT_READ_LIMIT_TIME_MS;
    static final String PAGE_LIMIT = '.'+TransformationOptionLimits.OPT_PAGE_LIMIT;

    /**
     * To support the historical concept of EXPLICIT transformers, all such transformers
     * are given a {@link PRIORITY_EXPLICIT} (50). By default transformers have a default of 10.
     * A value of 5 allows better transformers to be added later.
     */
    public int PRIORITY_EXPLICIT = 50;
    
    /**
     * By default transformers have a priority of 100.
     */
    public int PRIORITY_DEFAULT = 100;
    
    /**
     * Suffixes to property names used to define transformation limits 
     */
    static final Collection<String> LIMIT_SUFFIXES = Arrays.asList(new String [] {
            MAX_SOURCE_SIZE_K_BYTES,
            TIMEOUT_MS,
            MAX_PAGES,
            READ_LIMIT_K_BYTES,
            READ_LIMIT_TIME_MS,
            PAGE_LIMIT
    });
    
    /**
     * Suffix pairs (max and limit values) to property names used to define transformation limits 
     */
    public final String[][] LIMIT_PAIR_SUFFIXES = new String[][]
    {
            {MAX_SOURCE_SIZE_K_BYTES, READ_LIMIT_K_BYTES},
            {TIMEOUT_MS, READ_LIMIT_TIME_MS},
            {MAX_PAGES, PAGE_LIMIT}
    };
    
    /**
     * All suffixes to property names used to transformer configuration 
     */
    static final Collection<String> ALL_SUFFIXES = Arrays.asList(new String [] {
            MAX_SOURCE_SIZE_K_BYTES,
            TIMEOUT_MS,
            MAX_PAGES,
            READ_LIMIT_K_BYTES,
            READ_LIMIT_TIME_MS,
            PAGE_LIMIT,
            SUPPORTED,
            AVAILABLE,
            PRIORITY,
            ERROR_TIME,
            INITIAL_TIME,
            INITIAL_COUNT,
            THRESHOLD_COUNT,
            FAILOVER,
            PIPELINE
    });

    /**
     * No suffixes to property names used to define transformer settings. 
     */
    public static final Collection<String> NO_SUFFIXES = Collections.singletonList("");
    
    static final String ENTRIES = "entries";

    /**
     * The number of debug lines to keep. Turned off if <= 0.
     */
    public static final String DEBUG_ENTRIES = TRANSFORMER+"debug."+ENTRIES;

    /**
     * The number of log lines to keep. Turned off if <= 0.
     */
    public static final String LOG_ENTRIES = TRANSFORMER+"log."+ENTRIES;

    /**
     * Returns a transformer property value. 
     * @param name of the property.
     * @return a transformer property or {@code null} if not set.
     */
    String getProperty(String name);

    /**
     * Returns a sorted set of all transformer properties, their values and includes
     * comments about the properties. 
     * @param changesOnly only custom values will be included.
     * @return a multi-line String which is never {code null}.
     */
    String getProperties(boolean changesOnly);

    /**
     * Removes transformer properties.
     * 
     * @param propertyNames new line separated names. Any values will be ignored.
     * @return the number of properties removed.
     * @throws IllegalArgumentException if the properties were not set or the
     *         list contains errors.
     */
    int removeProperties(String propertyNames);

    /**
     * Sets a transformer property values. These will be stored in the database but on an MBean
     * reset is cleared.
     * 
     * @param propertyNamesAndValues new line separated name and values
     * @return the number of properties set.
     * @throws IllegalArgumentException the list contains errors.
     */
    int setProperties(String propertyNamesAndValues);

    /**
     * Returns and creates if needed the {@link TransformerStatistics} object for the combination of
     * transformer, sourceMimetype and targetMimetype. When transformer is null this is the
     * system wide summary object for a combination of sourceMimetype and targetMimetype.
     * When both sourceMimetype and targetMimetype are null this is the transformer's summary
     * object. When all three parameters are null this is the system wide summary for all
     * transformers.
     * @param transformer the transformer for which data is being recorded.
     * @param sourceMimetype the source mimetype.
     * @param targetMimetype the source mimetype.
     * @param createNew indicates if a new object should be created if it does not exist.
     * @return the requested {@link TransformerStatistics}.
     */
    public TransformerStatistics getStatistics(ContentTransformer transformer, String sourceMimetype, String targetMimetype, boolean createNew);

    /**
     * Returns the limits defined for the combination of transformer, sourceMimetype and targetMimetype.
     * When the transformer is null, this is a default value. When both sourceMimetype and targetMimetype
     * are null this is a default for the specified transformer.
     * @param transformer
     * @param sourceMimetype
     * @param targetMimetype
     * @param use to which the limits will be put. For example "index", "webpreview", "doclib", "syncRule",
     *        "aysncRule". {@code null} is the default.
     * @return the combined (takes into account defaults from higher levels) limits for the combination.
     */
    public TransformationOptionLimits getLimits(ContentTransformer transformer, String sourceMimetype, String targetMimetype, String use);

    /**
     * Returns true if the supplied mimetype transformation pair is allowed by the list of supported
     * and unsupported transformations.
     * @param transformer
     * @param sourceMimetype
     * @param targetMimetype
     * @param options not currently used
     */
    public boolean isSupportedTransformation(ContentTransformer transformer, String sourceMimetype,
            String targetMimetype, TransformationOptions options);

    /**
     * Returns the priority of the specified transformer for the the combination of source and target mimetype.
     * @param transformer
     * @param sourceMimetype
     * @param targetMimetype
     * @return the priority. To support the historical concept of EXPLICIT transformers, all such transformers
     *         are given a {@link PRIORITY_EXPLICIT} (50). By default transformers have a default of 100.
     */
    public int getPriority(ContentTransformer contentTransformerHelper,
            String sourceMimetype, String targetMimetype);

    /**
     * Returns the threshold of the transformer. It is only after this number of transformation attempts
     * that the average time is used.
     * @param transformer
     * @param sourceMimetype
     * @param targetMimetype
     * @return the threshold.
     */

    public int getThresholdCount(ContentTransformer contentTransformerHelper, String sourceMimetype,
            String targetMimetype);
}
