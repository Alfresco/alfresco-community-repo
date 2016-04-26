
package org.alfresco.repo.virtual.ref;

import org.apache.commons.lang.StringUtils;

/**
 * Creates string-pair hashes of single digit element numeric paths.<br>
 * @see HierarchicalPathHasher
 */
public class NumericPathHasher extends HierarchicalPathHasher
{

    @Override
    protected String hashSubpath(String subpath)
    {
        try
        {
            if (subpath.trim().isEmpty())
            {
                return null;
            }

            String[] numericPathElements = subpath.split("/");

            if (numericPathElements == null || numericPathElements.length == 0)
            {
                return "0";
            }

            long lHash = 0;

            for (int i = numericPathElements[0].isEmpty() ? 1 : 0; i < numericPathElements.length; i++)
            {
                if (numericPathElements[i].length() == 1)
                {
                    long intPathElement = Long.parseLong(numericPathElements[i]);
                    if (intPathElement > 0)
                    {
                        lHash = lHash * 10 + intPathElement;
                        if (lHash < 0)
                        {
                            return null;
                        }
                        else
                        {
                            continue;
                        }
                    }
                    else
                    {
                        return null;
                    }
                }
                else
                {
                    return null;
                }
            }

            return "" + lHash;
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    @Override
    protected String lookupSubpathHash(String hash)
    {
        String[] digits = hash.split("(?<=.)");
        return "/" + StringUtils.join(digits,
                                      '/');
    }
}
