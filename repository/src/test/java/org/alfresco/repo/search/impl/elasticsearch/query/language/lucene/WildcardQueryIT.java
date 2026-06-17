/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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

package org.alfresco.repo.search.impl.elasticsearch.query.language.lucene;

import org.alfresco.repo.search.impl.elasticsearch.query.BaseWildcardQueryIT;

@SuppressWarnings("PMD.TooManyMethods")
public class WildcardQueryIT extends BaseWildcardQueryIT
{
    /* See https://alfresco.atlassian.net/browse/SEARCH-2862 for wildcards in phrase queries */

    @Override
    public void wildCardsInPhraseQueriesInExplicitContentField()
    {
        assertContainsOnly(luceneSearch("TEXT:\"a b?g y*ow b?n?na\""), bigYellowBanana);
    }

    @Override
    public void wildCardsInPhraseQueriesInImplicitContentField()
    {
        assertContainsOnly(luceneSearch("\"a b?g y*ow b?n?na\""), bigYellowBanana);
    }

    @Override
    public void zeroOrMoreCharactersWildcardMetadataInfixSearch()
    {
        assertContainsOnly(luceneSearch("@cm\\:name:y*ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:ye*llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:yel*low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:yell*ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:yello*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardExplicitContentInfixSearch()
    {
        assertContainsOnly(luceneSearch("TEXT:y*ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:ye*llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:yel*low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:yell*ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:yello*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardImplicitContentInfixSearch()
    {
        assertContainsOnly(luceneSearch("y*ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("ye*llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("yel*low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("yell*ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("yello*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void consecutiveZeroOrMoreCharactersWildcardsMetadataInfixSearch()
    {
        assertContainsOnly(luceneSearch("@cm\\:name:y***ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:ye***llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:yel***low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:yell***ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:yello***w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void consecutiveZeroOrMoreCharactersWildcardsExplicitContentInfixSearch()
    {
        assertContainsOnly(luceneSearch("TEXT:y***ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:ye***llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:yel***low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:yell***ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:yello***w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void consecutiveZeroOrMoreCharactersWildcardsImplicitContentInfixSearch()
    {
        assertContainsOnly(luceneSearch("y***ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("ye***llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("yel***low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("yell***ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("yello***w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void sparseZeroOrMoreCharactersWildcardsMetadataInfixSearch()
    {
        assertContainsOnly(luceneSearch("@cm\\:name:y*e*l*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:ye*l*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:yel*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:yell*o*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void sparseZeroOrMoreCharactersWildcardsExplicitContentInfixSearch()
    {
        assertContainsOnly(luceneSearch("TEXT:y*e*l*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:ye*l*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:yel*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:yell*o*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void sparseZeroOrMoreCharactersWildcardsImplicitContentInfixSearch()
    {
        assertContainsOnly(luceneSearch("y*e*l*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("ye*l*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("yel*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("yell*o*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardMetadataPrefixSearch()
    {
        assertContainsOnly(luceneSearch("@cm\\:name:*yellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:*ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:*llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:*low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:*ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardExplicitContentPrefixSearch()
    {
        assertContainsOnly(luceneSearch("TEXT:*yellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:*ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:*llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:*low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:*ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardImplicitContentPrefixSearch()
    {
        assertContainsOnly(luceneSearch("*yellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("*ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("*llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("*low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("*ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardsMetadataPrefixSearch()
    {
        assertContainsOnly(luceneSearch("@cm\\:name:***yellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:***ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:***llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:***low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:***ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:***w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardsExplicitContentPrefixSearch()
    {
        assertContainsOnly(luceneSearch("TEXT:***yellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:***ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:***llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:***low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:***ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:***w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardsImplicitContentPrefixSearch()
    {
        assertContainsOnly(luceneSearch("***yellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("***ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("***llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("***low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("***ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("***w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardMetadataSuffixSearch()
    {
        assertContainsOnly(luceneSearch("@cm\\:name:yellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:yello*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:yell*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:yel*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:ye*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:y*"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardExplicitContentSuffixSearch()
    {
        assertContainsOnly(luceneSearch("TEXT:yellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:yello*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:yell*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:yel*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:ye*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:y*"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardImplicitContentSuffixSearch()
    {
        assertContainsOnly(luceneSearch("yellow*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("yello*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("yell*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("yel*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("ye*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("y*"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardsMetadataSuffixSearch()
    {
        assertContainsOnly(luceneSearch("@cm\\:name:yellow***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:yello***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:yell***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:yel***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:ye***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("@cm\\:name:y***"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardsExplicitContentSuffixSearch()
    {
        assertContainsOnly(luceneSearch("TEXT:yellow***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:yello***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:yell***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:yel***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:ye***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("TEXT:y***"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardsImplicitContentSuffixSearch()
    {
        assertContainsOnly(luceneSearch("yellow***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("yello***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("yell***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("yel***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("ye***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(luceneSearch("y***"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void singleCharacterWildcardInMetadataSearch()
    {
        assertContainsOnly(luceneSearch("@cm\\:name:?ig"), bigYellowBanana);
        assertContainsOnly(luceneSearch("@cm\\:name:b?g"), bigYellowBanana);
        assertContainsOnly(luceneSearch("@cm\\:name:bi?"), bigYellowBanana);
        assertContainsOnly(luceneSearch("@cm\\:name:?i?"), bigYellowBanana);
        assertContainsOnly(luceneSearch("@cm\\:name:??g"), bigYellowBanana);
        assertContainsOnly(luceneSearch("@cm\\:name:b??"), bigYellowBanana);
        assertContainsOnly(luceneSearch("@cm\\:name:???"), bigYellowBanana);
    }

    @Override
    public void singleCharacterWildcardInExplicitContentSearch()
    {
        assertContainsOnly(luceneSearch("TEXT:?ig"), bigYellowBanana);
        assertContainsOnly(luceneSearch("TEXT:b?g"), bigYellowBanana);
        assertContainsOnly(luceneSearch("TEXT:bi?"), bigYellowBanana);
        assertContainsOnly(luceneSearch("TEXT:?i?"), bigYellowBanana);
        assertContainsOnly(luceneSearch("TEXT:??g"), bigYellowBanana);
        assertContainsOnly(luceneSearch("TEXT:b??"), bigYellowBanana);
        assertContainsOnly(luceneSearch("TEXT:???"), bigYellowBanana);
    }

    @Override
    public void singleCharacterWildcardInImplicitContentSearch()
    {
        assertContainsOnly(luceneSearch("?ig"), bigYellowBanana);
        assertContainsOnly(luceneSearch("b?g"), bigYellowBanana);
        assertContainsOnly(luceneSearch("bi?"), bigYellowBanana);
        assertContainsOnly(luceneSearch("?i?"), bigYellowBanana);
        assertContainsOnly(luceneSearch("??g"), bigYellowBanana);
        assertContainsOnly(luceneSearch("b??"), bigYellowBanana);
        assertContainsOnly(luceneSearch("???"), bigYellowBanana);
    }

    @Override
    public void matchAllDocumentSearch()
    {
        assertContains(luceneSearch("*"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void untokenisedFieldWildcardQuerySearch()
    {
        assertContainsOnly(aftsSearch("acme\\:contractUntokenisedField:\"A v?ry juicy pear\""), untokenizedFieldDoc);
        assertContainsOnly(aftsSearch("acme\\:contractUntokenisedField:\"A v?ry juicy p*\""), untokenizedFieldDoc);
        assertContainsOnly(aftsSearch("acme\\:contractUntokenisedField:\"A v?ry juicy *\""), untokenizedFieldDoc);
        assertContainsOnly(aftsSearch("acme\\:contractUntokenisedField:\"A very juicy *\""), untokenizedFieldDoc);
        assertContainsOnly(aftsSearch("acme\\:contractUntokenisedField:\"* juicy pear\""), untokenizedFieldDoc);
        assertContainsOnly(aftsSearch("acme\\:contractUntokenisedField:\"* juicy *\""), untokenizedFieldDoc);

    }

    @Override
    public void tokenisedFieldWildcardQuerySearch()
    {
        assertContainsOnly(aftsSearch("acme\\:contractTokenisedField:\"A wild fox in the*\""), tokenizedFieldDoc);
        assertContainsOnly(aftsSearch("acme\\:contractTokenisedField:\"A w?ld fox in the f??est\""), tokenizedFieldDoc);
        assertContainsOnly(aftsSearch("acme\\:contractTokenisedField:\"*in the forest\""), tokenizedFieldDoc);
        assertContainsOnly(aftsSearch("cm\\:name:\"a b?g y*ow b?n?na\""), bigYellowBanana);
    }

    @Override
    public void stemmedContentWildcardSuffixSearch()
    {
        assertContainsOnly(aftsSearch("TEXT:\"gosling*\""), goslingDocument);
        assertContainsOnly(aftsSearch("cm:content:\"gosling*\""), goslingDocument);

        assertContainsOnly(aftsSearch("TEXT:\"swimming*\""), swimmingDocument);
        assertContainsOnly(aftsSearch("cm:content:\"swimming*\""), swimmingDocument);

        assertContainsOnly(aftsSearch("TEXT:\"gosl*\""), goslingDocument);
        assertContainsOnly(aftsSearch("TEXT:\"swim*\""), swimmingDocument);
    }

    @Override
    public void nonStemmedContentWildcardSuffixSearch()
    {
        assertContainsOnly(aftsSearch("TEXT:\"supersizemyrepo*\""), supersizemyrepoDocument);
        assertContainsOnly(aftsSearch("cm:content:\"supersizemyrepo*\""), supersizemyrepoDocument);

        assertContainsOnly(aftsSearch("TEXT:\"supersizemy*\""), supersizemyrepoDocument);
        assertContainsOnly(aftsSearch("TEXT:\"supersize*\""), supersizemyrepoDocument);
    }
}
