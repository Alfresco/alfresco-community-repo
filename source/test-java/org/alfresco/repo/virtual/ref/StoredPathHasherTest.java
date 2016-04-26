
package org.alfresco.repo.virtual.ref;

import junit.framework.TestCase;

import org.alfresco.util.Pair;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

public class StoredPathHasherTest extends TestCase
{
    private HashStore hashStore;

    public void setUp()
    {
        hashStore = new HashStore();
        hashStore.put("/com/alfresco",
                      "1");
    }

    @Test
    public void testHash() throws Exception
    {

        hashStore.put("/org/alfresco",
                      "2");

        StoredPathHasher hasher = new StoredPathHasher(hashStore);

        assertEquals(new Pair<String, String>("1",
                                              null),
                     hasher.hash("/com/alfresco/"));

        assertEquals(new Pair<String, String>("1",
                                              null),
                     hasher.hash("/com/alfresco"));

        assertEquals(new Pair<String, String>(null,
                                              Base64.encodeBase64String("/com".getBytes())),
                     hasher.hash("/com"));

        assertEquals(new Pair<String, String>(null,
                                              Base64.encodeBase64String("/com".getBytes())),
                     hasher.hash("/com/"));

        assertEquals(new Pair<String, String>("1",
                                              Base64.encodeBase64String("foo/bar".getBytes())),
                     hasher.hash("/com/alfresco/foo/bar"));

        assertEquals(new Pair<String, String>("2",
                                              Base64.encodeBase64String("foo/bar".getBytes())),
                     hasher.hash("/org/alfresco/foo/bar"));
    }

    @Test
    public void testHashRoot() throws Exception
    {
        StoredPathHasher hasher = new StoredPathHasher(hashStore);

        assertEquals(new Pair<String, String>(null,
                                              ""),
                     hasher.hash("/"));
    }

    @Test
    public void testNullPaths() throws Exception
    {

        {

            StoredPathHasher hasher = new StoredPathHasher(hashStore);

            try
            {
                hasher.hash("");
                fail("Shold not be able to hash empty paths.");
            }
            catch (IllegalArgumentException e)
            {
                // as expected
            }
        }

        {

            StoredPathHasher hasher = new StoredPathHasher(hashStore);

            try
            {
                hasher.hash(null);
                fail("Shold not be able to hash null paths.");
            }
            catch (IllegalArgumentException e)
            {
                // as expected
            }
        }
    }

}
