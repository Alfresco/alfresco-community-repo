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

import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.codec.binary.Base64;

/**
 * Creates and looks up hashes of '/' paths defining strings.<br>
 * Paths are hashed using {@link HashStore} defined hashes. <br>
 * Store defined hashes are matched for the longest possible sub-path of a given
 * path. The remaining path is encoded using a Base64 encoder. The two resulted
 * strings.
 */
public abstract class HierarchicalPathHasher implements PathHasher
{
    private static String normalizePath(String classpath)
    {
        String normalizedClasspath = classpath.trim();
        if (!normalizedClasspath.startsWith("/"))
        {
            normalizedClasspath = "/" + normalizedClasspath;
        }
        if (normalizedClasspath.endsWith("/"))
        {
            normalizedClasspath = normalizedClasspath.substring(0,
                                                                normalizedClasspath.length() - 1);
        }
        return normalizedClasspath;
    }

    protected abstract String hashSubpath(String subpath);

    protected abstract String lookupSubpathHash(String hash);

    @Override
    public Pair<String, String> hash(String path)
    {
        ParameterCheck.mandatoryString("path",
                                       path);

        String normalClasspath = normalizePath(path);
        String searchedClasspath = normalClasspath;
        String notFoundPath = null;
        String hash = hashSubpath(searchedClasspath);

        while (hash == null)
        {
            int lastSeparator = searchedClasspath.lastIndexOf('/');
            if (lastSeparator < 0)
            {
                String code = new String(Base64.encodeBase64(normalClasspath.getBytes(),
                                                             false));
                return new Pair<String, String>(null,
                                                code);
            }

            if (notFoundPath != null)
            {
                notFoundPath = searchedClasspath.substring(lastSeparator + 1) + "/" + notFoundPath;

            }
            else
            {
                notFoundPath = searchedClasspath.substring(lastSeparator + 1);

            }

            searchedClasspath = searchedClasspath.substring(0,
                                                            lastSeparator);
            hash = hashSubpath(searchedClasspath);

            if (hash != null)
            {
                String notFoundClasspathBase64 = new String(Base64.encodeBase64(notFoundPath.getBytes(),
                                                                                false));

                return new Pair<String, String>(hash,
                                                notFoundClasspathBase64);
            }
        }

        return new Pair<String, String>(hash,
                                        null);

    }

    @Override
    public String lookup(Pair<String, String> hash)
    {
        if (hash.getSecond() == null)
        {
            return lookupSubpathHash(hash.getFirst());
        }
        else if (hash.getFirst() == null)
        {
            return lookupSubpathCode(hash.getSecond());
        }
        else
        {
            String lHash = lookupSubpathHash(hash.getFirst());
            String lCode = lookupSubpathCode(hash.getSecond());
            return lHash + "/" + lCode;
        }
    }

    private String lookupSubpathCode(String code)
    {
        if (code.isEmpty())
        {
            return "/";
        }
        byte[] decodedBytes = Base64.decodeBase64(code.getBytes());
        return new String(decodedBytes);
    }

}
