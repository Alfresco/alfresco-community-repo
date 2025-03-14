/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.version.common;

import java.util.Comparator;

import org.alfresco.service.cmr.version.Version;
import org.alfresco.util.VersionNumber;

/**
 * A comparator to sort a version list according to their version labels in descending order (eg. 2.1, 2.0, 1.1, 1.0)
 *
 * @author Yanick Pignot
 * 
 * @deprecated See {@link org.alfresco.service.cmr.version.VersionHistory}
 */
public class VersionLabelComparator implements Comparator<Version>
{

    public int compare(Version version1, Version version2)
    {
        String labelV1 = version1.getVersionLabel();
        String labelV2 = version2.getVersionLabel();

        // sort the list descending (ie. most recent first)
        return new VersionNumber(labelV2).compareTo(new VersionNumber(labelV1));
    }
}
