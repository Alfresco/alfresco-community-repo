
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
