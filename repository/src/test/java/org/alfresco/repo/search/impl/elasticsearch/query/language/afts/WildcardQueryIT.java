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

package org.alfresco.repo.search.impl.elasticsearch.query.language.afts;

import org.alfresco.repo.search.impl.elasticsearch.query.BaseWildcardQueryIT;

@SuppressWarnings("PMD.TooManyMethods")
public class WildcardQueryIT extends BaseWildcardQueryIT
{
    /* See https://alfresco.atlassian.net/browse/SEARCH-2862 for wildcards in phrase queries */

    @Override
    public void wildCardsInPhraseQueriesInExplicitContentField()
    {
        assertContainsOnly(aftsSearch("TEXT:\"a b?g y*ow b?n?na\""), bigYellowBanana);
    }

    @Override
    public void wildCardsInPhraseQueriesInImplicitContentField()
    {
        assertContainsOnly(aftsSearch("\"a b?g y*ow b?n?na\""), bigYellowBanana);
    }

    @Override
    public void zeroOrMoreCharactersWildcardMetadataInfixSearch()
    {
        assertContainsOnly(aftsSearch("cm:name:y*ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:ye*llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:yel*low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:yell*ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:yello*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardExplicitContentInfixSearch()
    {
        assertContainsOnly(aftsSearch("TEXT:y*ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:ye*llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:yel*low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:yell*ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:yello*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardImplicitContentInfixSearch()
    {
        assertContainsOnly(aftsSearch("y*ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("ye*llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("yel*low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("yell*ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("yello*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void consecutiveZeroOrMoreCharactersWildcardsMetadataInfixSearch()
    {
        assertContainsOnly(aftsSearch("cm:name:y***ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:ye***llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:yel***low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:yell***ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:yello***w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void consecutiveZeroOrMoreCharactersWildcardsExplicitContentInfixSearch()
    {
        assertContainsOnly(aftsSearch("TEXT:y***ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:ye***llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:yel***low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:yell***ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:yello***w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void consecutiveZeroOrMoreCharactersWildcardsImplicitContentInfixSearch()
    {
        assertContainsOnly(aftsSearch("y***ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("ye***llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("yel***low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("yell***ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("yello***w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void sparseZeroOrMoreCharactersWildcardsMetadataInfixSearch()
    {
        assertContainsOnly(aftsSearch("cm:name:y*e*l*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:ye*l*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:yel*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:yell*o*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void sparseZeroOrMoreCharactersWildcardsExplicitContentInfixSearch()
    {
        assertContainsOnly(aftsSearch("TEXT:y*e*l*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:ye*l*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:yel*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:yell*o*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void sparseZeroOrMoreCharactersWildcardsImplicitContentInfixSearch()
    {
        assertContainsOnly(aftsSearch("y*e*l*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("ye*l*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("yel*l*o*w"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("yell*o*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardMetadataPrefixSearch()
    {
        assertContainsOnly(aftsSearch("cm:name:*yellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:*ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:*llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:*low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:*ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardExplicitContentPrefixSearch()
    {
        assertContainsOnly(aftsSearch("TEXT:*yellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:*ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:*llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:*low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:*ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardImplicitContentPrefixSearch()
    {
        assertContainsOnly(aftsSearch("*yellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("*ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("*llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("*low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("*ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("*w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardsMetadataPrefixSearch()
    {
        assertContainsOnly(aftsSearch("cm:name:***yellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:***ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:***llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:***low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:***ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:***w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardsExplicitContentPrefixSearch()
    {
        assertContainsOnly(aftsSearch("TEXT:***yellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:***ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:***llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:***low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:***ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:***w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardsImplicitContentPrefixSearch()
    {
        assertContainsOnly(aftsSearch("***yellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("***ellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("***llow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("***low"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("***ow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("***w"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardMetadataSuffixSearch()
    {
        assertContainsOnly(aftsSearch("cm:name:yellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:yello*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:yell*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:yel*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:ye*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:y*"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardExplicitContentSuffixSearch()
    {
        assertContainsOnly(aftsSearch("TEXT:yellow"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:yello*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:yell*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:yel*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:ye*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:y*"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardImplicitContentSuffixSearch()
    {
        assertContainsOnly(aftsSearch("yellow*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("yello*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("yell*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("yel*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("ye*"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("y*"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardsMetadataSuffixSearch()
    {
        assertContainsOnly(aftsSearch("cm:name:yellow***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:yello***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:yell***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:yel***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:ye***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("cm:name:y***"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardsExplicitContentSuffixSearch()
    {
        assertContainsOnly(aftsSearch("TEXT:yellow***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:yello***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:yell***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:yel***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:ye***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("TEXT:y***"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void zeroOrMoreCharactersWildcardsImplicitContentSuffixSearch()
    {
        assertContainsOnly(aftsSearch("yellow***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("yello***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("yell***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("yel***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("ye***"), bigYellowBanana, yellowTaxi);
        assertContainsOnly(aftsSearch("y***"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void singleCharacterWildcardInMetadataSearch()
    {
        assertContainsOnly(aftsSearch("cm:name:?ig"), bigYellowBanana);
        assertContainsOnly(aftsSearch("cm:name:b?g"), bigYellowBanana);
        assertContainsOnly(aftsSearch("cm:name:bi?"), bigYellowBanana);
        assertContainsOnly(aftsSearch("cm:name:?i?"), bigYellowBanana);
        assertContainsOnly(aftsSearch("cm:name:??g"), bigYellowBanana);
        assertContainsOnly(aftsSearch("cm:name:b??"), bigYellowBanana);
        assertContainsOnly(aftsSearch("cm:name:???"), bigYellowBanana);
    }

    @Override
    public void singleCharacterWildcardInExplicitContentSearch()
    {
        assertContainsOnly(aftsSearch("TEXT:?ig"), bigYellowBanana);
        assertContainsOnly(aftsSearch("TEXT:b?g"), bigYellowBanana);
        assertContainsOnly(aftsSearch("TEXT:bi?"), bigYellowBanana);
        assertContainsOnly(aftsSearch("TEXT:?i?"), bigYellowBanana);
        assertContainsOnly(aftsSearch("TEXT:??g"), bigYellowBanana);
        assertContainsOnly(aftsSearch("TEXT:b??"), bigYellowBanana);
        assertContainsOnly(aftsSearch("TEXT:???"), bigYellowBanana);
    }

    @Override
    public void singleCharacterWildcardInImplicitContentSearch()
    {
        assertContainsOnly(aftsSearch("?ig"), bigYellowBanana);
        assertContainsOnly(aftsSearch("b?g"), bigYellowBanana);
        assertContainsOnly(aftsSearch("bi?"), bigYellowBanana);
        assertContainsOnly(aftsSearch("?i?"), bigYellowBanana);
        assertContainsOnly(aftsSearch("??g"), bigYellowBanana);
        assertContainsOnly(aftsSearch("b??"), bigYellowBanana);
        assertContainsOnly(aftsSearch("???"), bigYellowBanana);
    }

    @Override
    public void matchAllDocumentSearch()
    {
        assertContains(aftsSearch("*"), bigYellowBanana, yellowTaxi);
    }

    @Override
    public void untokenisedFieldWildcardQuerySearch()
    {
        assertContainsOnly(aftsSearch("acme:contractUntokenisedField:\"A v?ry juicy pear\""), untokenizedFieldDoc);
        assertContainsOnly(aftsSearch("acme:contractUntokenisedField:\"A v?ry juicy p*\""), untokenizedFieldDoc);
        assertContainsOnly(aftsSearch("acme:contractUntokenisedField:\"A v?ry juicy *\""), untokenizedFieldDoc);
        assertContainsOnly(aftsSearch("acme:contractUntokenisedField:\"A very juicy *\""), untokenizedFieldDoc);
        assertContainsOnly(aftsSearch("acme:contractUntokenisedField:\"* juicy pear\""), untokenizedFieldDoc);
        assertContainsOnly(aftsSearch("acme:contractUntokenisedField:\"* juicy *\""), untokenizedFieldDoc);

    }

    @Override
    public void tokenisedFieldWildcardQuerySearch()
    {
        assertContainsOnly(aftsSearch("acme:contractTokenisedField:\"A wild fox in the*\""), tokenizedFieldDoc);
        assertContainsOnly(aftsSearch("acme:contractTokenisedField:\"*in the forest\""), tokenizedFieldDoc);
        assertContainsOnly(aftsSearch("acme:contractTokenisedField:\"A w?ld f*x in the f??est\""), tokenizedFieldDoc);
        assertContainsOnly(aftsSearch("cm:name:\"a b?g y*ow b?n?na\""), bigYellowBanana);
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
