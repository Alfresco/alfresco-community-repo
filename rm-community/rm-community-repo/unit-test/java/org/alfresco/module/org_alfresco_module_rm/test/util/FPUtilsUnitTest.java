package org.alfresco.module.org_alfresco_module_rm.test.util;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import static org.alfresco.module.org_alfresco_module_rm.test.util.FPUtils.asListFrom;
import static org.alfresco.module.org_alfresco_module_rm.test.util.FPUtils.asSet;
import static org.alfresco.module.org_alfresco_module_rm.test.util.FPUtils.asSetFrom;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * Unit tests for {@link FPUtils}.
 *
 * @author Neil Mc Erlean
 * @since 2.4.a
 */
public class FPUtilsUnitTest
{
    @Test public void asListShouldProduceList()
    {
        List<String> l = asListFrom(() -> "hello",
                                    () -> "world",
                                    () -> {
                                        String s1 = "abc";
                                        String s2 = "xyz";
                                        return s1 + s2;
                                    });
        assertEquals(asList("hello", "world", "abcxyz"), l);
    }

    @Test public void asListShouldWorkForEmptyVarArgs()
    {
        assertEquals(emptyList(), FPUtils.<String>asListFrom());
    }

    @Test public void asSetShouldProduceSet()
    {
        assertEquals(new HashSet<>(asList("hello", "world")),
                     asSet("hello", "hello", "world"));
    }

    @Test public void asSetFromShouldWork()
    {
        Set<String> s = asSetFrom(() -> "hello",
                                  () -> "hello",
                                  () -> "world",
                                  () -> {
                                      String s1 = "wo";
                                      String s2 = "rld";
                                      return s1 + s2;
                                  });
        assertEquals(new HashSet<>(asList("hello", "world")), s);
    }
}
