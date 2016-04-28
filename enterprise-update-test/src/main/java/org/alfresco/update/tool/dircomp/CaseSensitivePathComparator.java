/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.update.tool.dircomp;

import java.nio.file.Path;
import java.util.Comparator;

/**
 * Provides a platform agnostic and consistent sorting mechanism
 * for {@link java.nio.file.Path} objects.
 * 
 * @author Matt Ward
 */
public class CaseSensitivePathComparator implements Comparator<Path>
{
    @Override
    public int compare(Path p1, Path p2)
    {
        String pathStr1 = p1.toString();
        String pathStr2 = p2.toString();
        return pathStr1.compareTo(pathStr2);
    }
}
