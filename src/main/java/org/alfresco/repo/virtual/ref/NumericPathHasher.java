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
