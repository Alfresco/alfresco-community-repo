/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.update.tool.dircomp;

import java.nio.file.Path;
import java.util.TreeSet;

/**
 * Sorted set of {@link Path} objects that provides consistent
 * cross-platform sort order.
 * 
 * @see java.util.TreeSet
 * @author Matt Ward
 */
public class SortedPathSet extends TreeSet<Path>
{
    private static final long serialVersionUID = 1L;

    public SortedPathSet()
    {
        super(new CaseSensitivePathComparator());
    }
}
