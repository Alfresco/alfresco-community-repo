package org.alfresco.update.tool.dircomp;

import java.util.ArrayList;
import java.util.List;

/*
 * Copyright 2015-2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
public class ResultSet
{
    final List<Result> results = new ArrayList<>();
    public final Stats stats = new Stats();

    /**
     * Class for aggregating basic statistics relating to the directory tree comparisons.
     * <p>
     * For all counts, unless specified otherwise, if a file appears in both trees, it is
     * counted only once, as it is relative files we are tracking regardless of which
     * tree(s) they exist in.
     */
    public static class Stats
    {
        /**
         * The number of files (including directories) examined.
         */
        public int resultCount;
        /**
         * The number of files discovered to have differences that are not allowed or ignored.
         */
        public int differenceCount;
        /**
         * The number of files discovered to have differences, but the difference is allowed.
         */
        public int suppressedDifferenceCount;
        /**
         * The number of files that were completely ignored due to being in the ignore list.
         */
        public int ignoredFileCount;
    }
}
