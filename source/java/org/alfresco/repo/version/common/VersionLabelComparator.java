/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.version.common;

import java.util.Comparator;

import org.alfresco.service.cmr.version.Version;

/**
 * A comparator to sort a version list according theires version labels ascending
 *
 * @author Yanick Pignot
 */
public class VersionLabelComparator implements Comparator
{

    public int compare(Object version1, Object version2)
    {
        String labelV1 = ((Version) version1).getVersionLabel();
        String labelV2 = ((Version) version2).getVersionLabel();

        // sort the list ascending
        return labelV2.compareTo(labelV1);
    }
}
