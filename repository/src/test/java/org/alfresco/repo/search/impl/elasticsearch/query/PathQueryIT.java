/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

package org.alfresco.repo.search.impl.elasticsearch.query;

import org.junit.Before;
import org.junit.Test;

import org.alfresco.service.cmr.repository.NodeRef;

@SuppressWarnings({"PMD.DetachedTestCase", "PMD.TooManyMethods"})
public class PathQueryIT extends LuceneOrAFTSQueryIT
{
    private NodeRef a;
    private NodeRef a_whitespace_b;
    private NodeRef a_tab_b;
    private NodeRef abcSpecialChars;
    private NodeRef ab;
    private NodeRef abc;
    private NodeRef xyz;
    private NodeRef abcVariantNamespace;
    private NodeRef abcd;
    private NodeRef xycde;
    private NodeRef ayzd;
    private NodeRef abcde;
    private NodeRef abcdefg;
    private NodeRef abcdefgg;
    private NodeRef abcdefgh;
    private NodeRef abdefg;
    private NodeRef abefg;
    private NodeRef root;

    public PathQueryIT(String language)
    {
        super(language);
    }

    @Before
    public void initDocuments()
    {
        root = indexDocument(new IndexDocumentSourceBuilder().withName("a").withPath("/"));
        a = indexDocument(new IndexDocumentSourceBuilder().withName("a").withPath("/n1:a"));
        a_whitespace_b = indexDocument(new IndexDocumentSourceBuilder().withName("a_whitespace_b").withPath("/n1:a/n2:a b"));
        a_tab_b = indexDocument(new IndexDocumentSourceBuilder().withName("a_whitespace_b").withPath("/n1:a/n2:a" + '\t' + "b"));
        abcSpecialChars = indexDocument(new IndexDocumentSourceBuilder().withName("a_special_chars").withPath("/n$1:[@a.]/n#2:{~b&}/n^3:(+c)"));
        ab = indexDocument(new IndexDocumentSourceBuilder().withName("ab").withPath("/n1:a/n2:b"));
        abc = indexDocument(new IndexDocumentSourceBuilder().withName("abc").withPath("/n1:a/n2:b/n3:c"));
        xyz = indexDocument(new IndexDocumentSourceBuilder().withName("zyz").withPath("/n1:x/n2:y/n3:z"));
        abcVariantNamespace = indexDocument(new IndexDocumentSourceBuilder().withName("abc").withPath("/n1v:a/n2v:b/n3v:c"));
        xycde = indexDocument(new IndexDocumentSourceBuilder().withName("xycde").withPath("/n1:x/n2:y/n3:c/n4:d/n5:e"));
        abcd = indexDocument(new IndexDocumentSourceBuilder().withName("abcd").withPath("/n1:a/n2:b/n3:c/n4:d"));
        ayzd = indexDocument(new IndexDocumentSourceBuilder().withName("ayzd").withPath("/n1:a/n2:y/n3:z/n4:d"));
        abcde = indexDocument(new IndexDocumentSourceBuilder().withName("abcde").withPath("/n1:a/n2:b/n3:c/n4:d/n5:e"));
        abcdefg = indexDocument(new IndexDocumentSourceBuilder().withName("abcdefg").withPath("/n1:a/n2:b/n3:c/n4:d/n5:e/n6:f/n7:g"));
        abdefg = indexDocument(new IndexDocumentSourceBuilder().withName("abdefg").withPath("/n1:a/n2:b/n4:d/n5:e/n6:f/n7:g"));
        abefg = indexDocument(new IndexDocumentSourceBuilder().withName("abefg").withPath("/n1:a/n2:b/n5:e/n6:f/n7:g"));
        abcdefgg = indexDocument(new IndexDocumentSourceBuilder().withName("abcdefgg").withPath("/n:a/n:b/n:c/n:d/n:e/n:f/n:g/n:g"));
        abcdefgh = indexDocument(new IndexDocumentSourceBuilder().withName("abcdefgh").withPath("/n:a/n:b/n:c/n:d/n:e/n:f/n:g/n:h"));
    }

    public void whitespacesMustBeEscapedOnRequestorSide()
    {
        assertContainsOnly(searchFor(language, "PATH:\"/n1:a/n2:a_x0020_b\""), a_whitespace_b);
        assertContainsOnly(searchFor(language, "PATH:\"/a/a_x0020_b\""), a_whitespace_b);
    }

    @Test
    public void tabsMustBeEscapedOnRequestorSide()
    {
        assertContainsOnly(searchFor(language, "PATH:\"/n1:a/n2:a_x0009_b\""), a_tab_b);
        assertContainsOnly(searchFor(language, "PATH:\"/a/a_x0009_b\""), a_tab_b);
    }

    @Test
    public void absoluteXpathWithNameSpace_shouldReturnExactNodes()
    {
        assertContainsOnly(searchFor(language, "PATH:\"/n1:a/n2:b/n3:c\""), abc);
        assertContainsOnly(searchFor(language, "PATH:\"/n1:a/n2:b/n3:c/.\""), abc);
    }

    @Test
    public void absoluteXpathWithoutNameSpace_shouldReturnExactNodes()
    {
        assertContainsOnly(searchFor(language, "PATH:\"/a/b/c\""), abc, abcVariantNamespace);
        assertContainsOnly(searchFor(language, "PATH:\"/a/b/c/.\""), abc, abcVariantNamespace);
    }

    @Test
    public void absoluteXpathWithDescendantAxis_shouldReturnExactNodeAndDescendants()
    {
        assertContainsOnly(searchFor(language, "PATH:\"/n1:a/n2:b//*\""), abc, abcd, abcde, abcdefg, abdefg, abefg);
        assertContainsOnly(searchFor(language, "PATH:\"/a/b//*\""), abc, abcVariantNamespace, abcd, abcde, abcdefg, abdefg, abefg, abcdefgg, abcdefgh);
    }

    @Test
    public void absoluteXpathWithMultipleDescendantAxis_shouldReturnExactNode()
    {
        assertContainsOnly(searchFor(language, "PATH:\"/n1:a/n2:b/*/n4:d/*\""), abcde);
        assertContainsOnly(searchFor(language, "PATH:\"/a/b/*/d/*\""), abcde);
    }

    @Test
    public void absoluteXpathWithMultipleDescendantAxis_shouldReturnExactNodeAndDescendants()
    {
        assertContainsOnly(searchFor(language, "PATH:\"/n1:a/n2:b/*/n4:d//*\""), abcde, abcdefg);
        assertContainsOnly(searchFor(language, "PATH:\"/a/b/*/d//*\""), abcde, abcdefg, abcdefgh, abcdefgg);
    }

    @Test
    public void absoluteXpathWithChildAxis_shouldReturnExactNodeChild()
    {
        assertContainsOnly(searchFor(language, "PATH:\"/n1:a/n2:b/*\""), abc);
        assertContainsOnly(searchFor(language, "PATH:\"/a/b/*\""), abc, abcVariantNamespace);
    }

    @Test
    public void absoluteXpathWithLocalNameWildcard_shouldReturnExactNodes()
    {
        assertContainsOnly(searchFor(language, "PATH:\"/n1:*/n2:*/n3:*\""), abc, xyz);
        assertContainsOnly(searchFor(language, "PATH:\"/n1:*/n2:*/n3:*/.\""), abc, xyz);
    }

    @Test
    public void relativeXPathEndingWith_shouldReturnExactNodes()
    {
        assertContainsOnly(searchFor(language, "PATH:\"/n1:a//n4:d\""), abcd, ayzd);
        assertContainsOnly(searchFor(language, "PATH:\"/n1:a//n4:d/.\""), abcd, ayzd);
        assertContainsOnly(searchFor(language, "PATH:\"/a//d\""), abcd, ayzd);
        assertContainsOnly(searchFor(language, "PATH:\"/a//d/.\""), abcd, ayzd);
    }

    @Test
    public void absoluteXpathWithWildcard_shouldReturnExactNodes()
    {
        assertContainsOnly(searchFor(language, "PATH:\"/n1:a/*/*/n4:d\""), abcd, ayzd);
        assertContainsOnly(searchFor(language, "PATH:\"/n1:a/*/*/n4:d/.\""), abcd, ayzd);
        assertContainsOnly(searchFor(language, "PATH:\"/a/*/*/d\""), abcd, ayzd);
        assertContainsOnly(searchFor(language, "PATH:\"/a/*/*/d/.\""), abcd, ayzd);
    }

    @Test
    public void absoluteXpathWithInnerRelatives_shouldReturnExactNodes()
    {
        assertContainsOnly(searchFor(language, "PATH:\"/n1:a/n2:b//*/n5:e//n7:g\""), abdefg, abcdefg);
        assertContainsOnly(searchFor(language, "PATH:\"/a/b//*/e//g\""), abdefg, abcdefg, abcdefgg);
    }

    @Test
    public void relativeXpath_shouldReturnNodesEndingWithIt()
    {
        assertContainsOnly(searchFor(language, "PATH:\".//n3:c/n4:d\""), abcd);
        assertContainsOnly(searchFor(language, "PATH:\"//n3:c/n4:d\""), abcd);
        assertContainsOnly(searchFor(language, "PATH:\".//c/d\""), abcd);
        assertContainsOnly(searchFor(language, "PATH:\"//c/d\""), abcd);
    }

    @Test
    public void relativeXpathWithDescendantAxis_shouldReturnNodeAndDescendants()
    {
        assertContainsOnly(searchFor(language, "PATH:\".//n3:c/n4:d//*\""), xycde, abcde, abcdefg);
        assertContainsOnly(searchFor(language, "PATH:\".//c/d//*\""), xycde, abcde, abcdefg, abcdefgh, abcdefgg);
    }

    @Test
    public void relativeXpathWithChildAxis_shouldReturnNodeAndDescendants()
    {
        assertContainsOnly(searchFor(language, "PATH:\".//n3:c/n4:d/*\""), xycde, abcde);
        assertContainsOnly(searchFor(language, "PATH:\".//c/d/*\""), xycde, abcde);
    }

    @Test
    public void absoluteXpathForLength_shouldReturnNodesWithExactLengthPath()
    {
        assertContainsOnly(searchFor(language, "PATH:\"/*/*/*\""), abc, xyz, abcVariantNamespace, abcSpecialChars);
    }

    @Test
    public void getAllXPath_shouldReturnAllNodes()
    {
        assertContainsOnly(searchFor(language, "PATH:\"//*\""), a, ab, abc, abcd, abcde, abcdefg, xyz,
                abcVariantNamespace, abdefg, abefg, ayzd, xycde, a_tab_b, a_whitespace_b, abcdefgg, abcdefgh, abcSpecialChars);
    }

    @Test
    public void getRootPath_shouldReturnOnlyRoot_1()
    {
        assertContainsOnly(searchFor(language, "PATH:\"/*\""), a);
    }

    @Test
    public void getRootPath_shouldReturnOnlyRoot_2()
    {
        assertContainsOnly(searchFor(language, "PATH:\"/\""), root);
    }

    @Test
    public void absoluteXpathWithDescendantAxisAndNameClause_shouldReturnSingleNode()
    {
        if (language.equals("afts"))
        {
            assertContainsOnly(searchFor(language, "PATH:\".//n3:c/n4:d/*\"  AND cm:name:abcde "), abcde);
            assertContainsOnly(searchFor(language, "PATH:\".//c/d/*\"  AND cm:name:abcde "), abcde);
        }
        else
        {
            assertContainsOnly(searchFor(language, "+PATH:\".//n3:c/n4:d/*\"  +cm\\:name:abcde "), abcde);
            assertContainsOnly(searchFor(language, "+PATH:\".//c/d/*\"  +cm\\:name:abcde "), abcde);
        }

    }

    @Test
    public void givenSameNodeNameInPathExists_wildcardSearchShouldReturnItInResults()
    {
        assertContainsOnly(searchFor(language, "PATH:\"/n:a/*/*/n:d/*/*/n:g//*\""), abcdefgg, abcdefgh);
        assertContainsOnly(searchFor(language, "PATH:\"/a/*/*/d/*/*/g//*\""), abcdefgg, abcdefgh);
    }

    @Test
    public void anyAncestorsQuery_shouldReturnExactNode()
    {
        assertContainsOnly(searchFor(language, "PATH:\"//n4:d\""), abcd, ayzd);
        assertContainsOnly(searchFor(language, "PATH:\"//d\""), abcd, ayzd);
    }

    @Test
    public void givenOnlyDescendantOrSelfInPath_shouldReturnExactNodes()
    {
        assertContainsOnly(searchFor(language, "PATH:\"//n1:a//n2:b\""), ab);
        assertContainsOnly(searchFor(language, "PATH:\"//n1:a//n2:b//n4:d\""), abcd);
    }

    @Test
    public void pathWithNotStartingWithSlash_shouldReturnResults()
    {
        assertContainsOnly(searchFor(language, "PATH:\"n1:a/n2:b/n3:c\""), abc);
        assertContainsOnly(searchFor(language, "PATH:\"n:a/*/*/n:d/*/*/n:g//*\""), abcdefgg, abcdefgh);
        assertContainsOnly(searchFor(language, "PATH:\"*/*/*\""), abc, xyz, abcVariantNamespace, abcSpecialChars);
        assertContainsOnly(searchFor(language, "PATH:\"a/b/c\""), abc, abcVariantNamespace);
        assertContainsOnly(searchFor(language, "PATH:\"a/*/*/d/*/*/g//*\""), abcdefgg, abcdefgh);
    }
}
