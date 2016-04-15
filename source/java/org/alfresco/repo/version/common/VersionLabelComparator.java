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
