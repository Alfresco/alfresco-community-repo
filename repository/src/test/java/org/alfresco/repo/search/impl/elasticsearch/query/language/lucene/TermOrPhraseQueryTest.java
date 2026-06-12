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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ACLID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ACLTXCOMMITTIME;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ACLTXID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ALL;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ANCESTOR;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ASPECT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_AUTHORITY;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_AUTHORITYSET;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_CASCADETX;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_CLASS;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_DBID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_DENIED;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_DENYSET;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_EXACTASPECT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_EXISTS;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_FINGERPRINT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_FTSSTATUS;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_INACLTXID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_INTXID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ISCONTAINER;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ISNODE;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ISNOTNULL;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ISNULL;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ISROOT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_OWNER;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_OWNERSET;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PARENT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PATHWITHREPEATS;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PRIMARYASSOCQNAME;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PRIMARYASSOCTYPEQNAME;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PROPERTIES;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_QNAME;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_READER;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_READERSET;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_SOLR4_ID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TAG;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TENANT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TEXT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TX;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TXCOMMITTIME;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TXID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TYPE;
import static org.alfresco.repo.search.adaptor.QueryConstants.PROPERTY_FIELD_PREFIX;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.junit.Assert;
import org.junit.Test;

import org.alfresco.repo.search.adaptor.QueryConstants;
import org.alfresco.repo.search.impl.elasticsearch.shared.translator.AlfrescoQualifiedNameTranslator;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

@SuppressWarnings({"deprecation", "PMD"})
public class TermOrPhraseQueryTest extends BaseQueryTest
{
    @Test
    public void unfieldedTerm() throws ParseException
    {
        Query noFieldQuery = parser.parse(A_TERM);
        Query fieldedQuery = parser.parse(FIELD_TEXT + ":" + A_TERM);

        assertEquals(noFieldQuery, fieldedQuery);
    }

    @Test
    public void unfieldedPhrase() throws ParseException
    {
        Query noFieldQuery = parser.parse(A_PHRASE);
        Query fieldedQuery = parser.parse(FIELD_TEXT + ":" + A_PHRASE);

        assertEquals(noFieldQuery, fieldedQuery);
    }

    @Test
    public void propertyTermQueryWithUnqualifiedPropertyName()
    {
        REQUEST_SPECIFIC_UNQUALIFIED_TEXT_ATTRIBUTES
                .forEach(name -> assertQueryEquals(
                        fieldQueryString(toElasticsearchForm(name), A_TERM),
                        parseAndBuildQuery(fieldQueryString(PROPERTY_FIELD_PREFIX + name, A_TERM))));
    }

    @Test
    public void TYPETermQueryWithUnknownTypeShortQualifiedValue_shouldUseDummyUnknownValue()
    {
        assertQueryEquals(FIELD_TYPE + ":" + LuceneQueryParser.UNKNOWN,
                parseAndBuildQuery(fieldQueryString(FIELD_TYPE, "\"cm:somethingUnknown\"")));
    }

    @Test
    public void TYPETermQueryWithUnknownTypeFullyQualifiedValue_shouldUseDummyUnknownValue()
    {
        assertQueryEquals(FIELD_TYPE + ":" + LuceneQueryParser.UNKNOWN,
                parseAndBuildQuery(fieldQueryString(FIELD_TYPE, "\"{http://www.alfresco.org/model/content/1.0}}somethingUnknown\"")));
    }

    @Test
    public void TYPETermQueryWithShortQualifiedValue_shouldBuildQueryWithDescendants()
    {
        assertQueryEquals("TYPE:cm\\:person TYPE:cm\\:engineer TYPE:cm\\:artist",
                parseAndBuildQuery(fieldQueryString("TYPE", "\"" + A_TYPE_SHORT_PREFIX_QNAME + "\"")));
    }

    @Test
    public void TYPETermQueryWithFullyQualifiedValue_shouldBuildQueryWithDescendants()
    {
        assertQueryEquals("TYPE:cm\\:person TYPE:cm\\:engineer TYPE:cm\\:artist",
                parseAndBuildQuery(fieldQueryString("TYPE", "\"" + A_TYPE_QNAME + "\"")));
    }

    @Test
    public void EXACTTYPETermQueryWithShortQualifiedValue_shouldBuildQueryWithNODescendants()
    {
        assertQueryEquals("TYPE:cm\\:person",
                parseAndBuildQuery(fieldQueryString("EXACTTYPE", "\"" + A_TYPE_SHORT_PREFIX_QNAME + "\"")));
    }

    @Test
    public void EXACTTYPETermQueryWithFullyQualifiedValue_shouldBuildQueryWithNODescendants()
    {
        assertQueryEquals("TYPE:cm\\:person",
                parseAndBuildQuery(fieldQueryString("EXACTTYPE", "\"" + A_TYPE_QNAME + "\"")));
    }

    @Test
    public void ASPECTTermQueryWithUnknownAspectShortQualifiedValue_shouldUseDummyUnknownValue()
    {
        assertQueryEquals(FIELD_ASPECT + ":" + LuceneQueryParser.UNKNOWN,
                parseAndBuildQuery(fieldQueryString(FIELD_ASPECT, "\"cm:somethingUnknown\"")));
    }

    @Test
    public void ASPECTTermQueryWithUnknownTypeFullyQualifiedValue_shouldUseDummyUnknownValue()
    {
        assertQueryEquals(FIELD_ASPECT + ":" + LuceneQueryParser.UNKNOWN,
                parseAndBuildQuery(fieldQueryString(FIELD_ASPECT, "\"{http://www.alfresco.org/model/content/1.0}}somethingUnknown\"")));
    }

    @Test
    public void ASPECTTermQueryWithShortQualifiedValue_shouldBuildQueryWithDescendants()
    {
        assertQueryEquals("ASPECT:cm\\:parentAspect ASPECT:cm\\:childAspectOne ASPECT:cm\\:childAspectTwo",
                parseAndBuildQuery(fieldQueryString(FIELD_ASPECT, "\"" + AN_ASPECT_SHORT_PREFIX_QNAME + "\"")));
    }

    @Test
    public void ASPECTTermQueryWithFullyQualifiedValue_shouldBuildQueryWithDescendants()
    {
        assertQueryEquals("ASPECT:cm\\:parentAspect ASPECT:cm\\:childAspectOne ASPECT:cm\\:childAspectTwo",
                parseAndBuildQuery(fieldQueryString(FIELD_ASPECT, "\"" + AN_ASPECT_QNAME + "\"")));
    }

    @Test
    public void EXACTASPECTTermQueryWithShortQualifiedValue_shouldBuildQueryWithNODescendants()
    {
        assertQueryEquals("ASPECT:cm\\:parentAspect",
                parseAndBuildQuery(fieldQueryString(FIELD_EXACTASPECT, "\"" + AN_ASPECT_SHORT_PREFIX_QNAME + "\"")));
    }

    @Test
    public void EXACTASPECTTermQueryWithFullyQualifiedValue_shouldBuildQueryWithNODescendants()
    {
        assertQueryEquals("ASPECT:cm\\:parentAspect",
                parseAndBuildQuery(fieldQueryString(FIELD_EXACTASPECT, "\"" + AN_ASPECT_SHORT_PREFIX_QNAME + "\"")));
    }

    @Test
    public void CLASSTermQueryWithTypeShortQualifiedValue_shouldBuildQueryWithDescendants()
    {
        assertQueryEquals("TYPE:cm\\:person TYPE:cm\\:engineer TYPE:cm\\:artist",
                parseAndBuildQuery(fieldQueryString("CLASS", "\"" + A_TYPE_SHORT_PREFIX_QNAME + "\"")));
    }

    @Test
    public void CLASSTermQueryWithTypeFullyQualifiedValue_shouldBuildQueryWithDescendants()
    {
        assertQueryEquals("TYPE:cm\\:person TYPE:cm\\:engineer TYPE:cm\\:artist",
                parseAndBuildQuery(fieldQueryString("CLASS", "\"" + A_TYPE_QNAME + "\"")));
    }

    @Test
    public void CLASSTermQueryWithAspectShortQualifiedValue_shouldBuildQueryWithDescendants()
    {
        assertQueryEquals("ASPECT:cm\\:parentAspect ASPECT:cm\\:childAspectOne ASPECT:cm\\:childAspectTwo",
                parseAndBuildQuery(fieldQueryString(FIELD_CLASS, "\"" + AN_ASPECT_SHORT_PREFIX_QNAME + "\"")));
    }

    @Test
    public void CLASSTermQueryWithAspectFullyQualifiedValue_shouldBuildQueryWithDescendants()
    {
        assertQueryEquals("ASPECT:cm\\:parentAspect ASPECT:cm\\:childAspectOne ASPECT:cm\\:childAspectTwo",
                parseAndBuildQuery(fieldQueryString(FIELD_CLASS, "\"" + AN_ASPECT_QNAME + "\"")));
    }

    @Test
    public void CLASSTermQueryWithUnknownAspectOrTypeShortQualifiedValue_shouldUseDummyUnknownValue()
    {
        assertQueryEquals(FIELD_TYPE + ":" + LuceneQueryParser.UNKNOWN,
                parseAndBuildQuery(fieldQueryString(FIELD_CLASS, "\"cm:somethingUnknown\"")));
    }

    @Test
    public void CLASSTermQueryWithUnknownAspectOrTypeFullyQualifiedValue_shouldUseDummyUnknownValue()
    {
        assertQueryEquals(FIELD_TYPE + ":" + LuceneQueryParser.UNKNOWN,
                parseAndBuildQuery(fieldQueryString(FIELD_CLASS, "\"{http://www.alfresco.org/model/content/1.0}}somethingUnknown\"")));
    }

    @Test
    public void propertyTermQueryWithPrefixedPropertyName()
    {
        REQUEST_SPECIFIC_PREFIXED_TEXT_ATTRIBUTES
                .forEach(name -> assertQueryEquals(
                        fieldQueryString(toElasticsearchForm(name), A_TERM),
                        parseAndBuildQuery(fieldQueryString(PROPERTY_FIELD_PREFIX + escape(name), A_TERM))));
    }

    @Test
    public void propertyTermQueryWithFullyQualifiedPropertyName()
    {
        REQUEST_SPECIFIC_FULLY_QUALIFIED_TEXT_ATTRIBUTES
                .forEach(name -> assertQueryEquals(
                        fieldQueryString(toElasticsearchForm(name), A_TERM),
                        parseAndBuildQuery(fieldQueryString(PROPERTY_FIELD_PREFIX + escape(name), A_TERM))));
    }

    @Test
    public void propertyPhraseQueryWithUnqualifiedPropertyName()
    {
        REQUEST_SPECIFIC_UNQUALIFIED_TEXT_ATTRIBUTES
                .forEach(name -> assertQueryEquals(
                        fieldQueryString(toElasticsearchForm(name), A_PHRASE),
                        parseAndBuildQuery(fieldQueryString(PROPERTY_FIELD_PREFIX + name, A_PHRASE))));
    }

    @Test
    public void propertyPhraseQueryWithPrefixedPropertyName()
    {
        REQUEST_SPECIFIC_PREFIXED_TEXT_ATTRIBUTES
                .forEach(name -> assertQueryEquals(
                        fieldQueryString(toElasticsearchForm(name), A_PHRASE),
                        parseAndBuildQuery(fieldQueryString(PROPERTY_FIELD_PREFIX + escape(name), A_PHRASE))));
    }

    @Test
    public void propertyPhraseQueryWithFullyQualifiedPropertyName()
    {
        REQUEST_SPECIFIC_FULLY_QUALIFIED_TEXT_ATTRIBUTES
                .forEach(name -> assertQueryEquals(
                        fieldQueryString(toElasticsearchForm(name), A_PHRASE),
                        parseAndBuildQuery(fieldQueryString(PROPERTY_FIELD_PREFIX + escape(name), A_PHRASE))));
    }

    @Test
    public void ALLFieldTermQueryExpansionUsingCustomUnqualifiedAllAttributes()
    {
        allFieldExpansionUsingCustomAttributes(
                REQUEST_SPECIFIC_UNQUALIFIED_TEXT_ATTRIBUTES,
                fieldQueryString(FIELD_ALL, A_TERM),
                fieldName -> fieldQueryString(fieldName, A_TERM));
    }

    @Test
    public void ALLFieldTermQueryExpansionUsingCustomFullyQualifiedAllAttributes()
    {
        allFieldExpansionUsingCustomAttributes(
                REQUEST_SPECIFIC_FULLY_QUALIFIED_TEXT_ATTRIBUTES,
                fieldQueryString(FIELD_ALL, A_TERM),
                fieldName -> fieldQueryString(fieldName, A_TERM));
    }

    @Test
    public void ALLFieldTermQueryExpansionUsingCustomPrefixedAllAttributes()
    {
        allFieldExpansionUsingCustomAttributes(
                REQUEST_SPECIFIC_PREFIXED_TEXT_ATTRIBUTES,
                fieldQueryString(FIELD_ALL, A_TERM),
                fieldName -> fieldQueryString(fieldName, A_TERM));
    }

    @Test
    public void ALLFieldPhraseQueryExpansionUsingCustomUnqualifiedAllAttributes()
    {
        allFieldExpansionUsingCustomAttributes(
                REQUEST_SPECIFIC_UNQUALIFIED_TEXT_ATTRIBUTES,
                fieldQueryString(FIELD_ALL, A_PHRASE),
                fieldName -> fieldQueryString(fieldName, A_PHRASE));
    }

    @Test
    public void ALLFieldPhraseQueryExpansionUsingCustomFullyQualifiedAllAttributes()
    {
        allFieldExpansionUsingCustomAttributes(
                REQUEST_SPECIFIC_FULLY_QUALIFIED_TEXT_ATTRIBUTES,
                fieldQueryString(FIELD_ALL, A_PHRASE),
                fieldName -> fieldQueryString(fieldName, A_PHRASE));
    }

    @Test
    public void ALLFieldPhraseQueryExpansionUsingCustomPrefixedAllAttributes()
    {
        allFieldExpansionUsingCustomAttributes(
                REQUEST_SPECIFIC_PREFIXED_TEXT_ATTRIBUTES,
                fieldQueryString(FIELD_ALL, A_PHRASE),
                fieldName -> fieldQueryString(fieldName, A_PHRASE));
    }

    @Test
    public void TEXTFieldTermQueryExpansionUsingCustomUnqualifiedTextAttributes()
    {
        textFieldExpansionUsingCustomTextAttributes(
                REQUEST_SPECIFIC_UNQUALIFIED_TEXT_ATTRIBUTES,
                fieldQueryString(QueryConstants.FIELD_TEXT, A_TERM),
                fieldName -> fieldQueryString(fieldName, A_TERM));
    }

    @Test
    public void TEXTFieldTermQueryExpansionUsingCustomPrefixedTextAttributes()
    {
        textFieldExpansionUsingCustomTextAttributes(
                REQUEST_SPECIFIC_PREFIXED_TEXT_ATTRIBUTES,
                fieldQueryString(QueryConstants.FIELD_TEXT, A_TERM),
                fieldName -> fieldQueryString(fieldName, A_TERM));
    }

    @Test
    public void TEXTFieldTermQueryExpansionUsingCustomFullyQualifiedTextAttributes()
    {
        textFieldExpansionUsingCustomTextAttributes(
                REQUEST_SPECIFIC_FULLY_QUALIFIED_TEXT_ATTRIBUTES,
                fieldQueryString(QueryConstants.FIELD_TEXT, A_TERM),
                fieldName -> fieldQueryString(fieldName, A_TERM));
    }

    @Test
    public void TEXTFieldPhraseQueryExpansionUsingCustomUnqualifiedTextAttributes()
    {
        textFieldExpansionUsingCustomTextAttributes(
                REQUEST_SPECIFIC_UNQUALIFIED_TEXT_ATTRIBUTES,
                fieldQueryString(QueryConstants.FIELD_TEXT, A_PHRASE),
                fieldName -> fieldQueryString(fieldName, A_PHRASE));
    }

    @Test
    public void TEXTFieldPhraseQueryExpansionUsingCustomPrefixedTextAttributes()
    {
        textFieldExpansionUsingCustomTextAttributes(
                REQUEST_SPECIFIC_PREFIXED_TEXT_ATTRIBUTES,
                fieldQueryString(QueryConstants.FIELD_TEXT, A_PHRASE),
                fieldName -> fieldQueryString(fieldName, A_PHRASE));
    }

    @Test
    public void TEXTFieldPhraseQueryExpansionUsingCustomFullyQualifiedTextAttributes()
    {
        textFieldExpansionUsingCustomTextAttributes(
                REQUEST_SPECIFIC_FULLY_QUALIFIED_TEXT_ATTRIBUTES,
                fieldQueryString(QueryConstants.FIELD_TEXT, A_PHRASE),
                fieldName -> fieldQueryString(fieldName, A_PHRASE));
    }

    @Test
    public void ISNODEFieldSetTo1MatchesEverything()
    {
        assertQueryEquals("*:*", parseAndBuildQuery(fieldQueryString(FIELD_ISNODE, "1")));
    }

    @Test
    public void ISNODEFieldSetToTMatchesEverything()
    {
        assertQueryEquals("*:*", parseAndBuildQuery(fieldQueryString(FIELD_ISNODE, "t")));
        assertQueryEquals("*:*", parseAndBuildQuery(fieldQueryString(FIELD_ISNODE, "T")));
    }

    @Test
    public void ISNODEFieldSetToTrueMatchesEverything()
    {
        assertQueryEquals("*:*", parseAndBuildQuery(fieldQueryString(FIELD_ISNODE, "tRuE")));
        assertQueryEquals("*:*", parseAndBuildQuery(fieldQueryString(FIELD_ISNODE, "true")));
        assertQueryEquals("*:*", parseAndBuildQuery(fieldQueryString(FIELD_ISNODE, "TRUE")));
    }

    @Test
    public void ISNODEFieldSetTo0MatchesNothing()
    {
        assertQueryEquals("-*:*", parseAndBuildQuery(fieldQueryString(FIELD_ISNODE, "0")));
    }

    @Test
    public void ISNODEFieldSetToFMatchesNothing()
    {
        assertQueryEquals("-*:*", parseAndBuildQuery(fieldQueryString(FIELD_ISNODE, "f")));
        assertQueryEquals("-*:*", parseAndBuildQuery(fieldQueryString(FIELD_ISNODE, "F")));
    }

    @Test
    public void ISNODEFieldSetToFalseMatchesEverything()
    {
        assertQueryEquals("-*:*", parseAndBuildQuery(fieldQueryString(FIELD_ISNODE, "fAlSe")));
        assertQueryEquals("-*:*", parseAndBuildQuery(fieldQueryString(FIELD_ISNODE, "false")));
        assertQueryEquals("-*:*", parseAndBuildQuery(fieldQueryString(FIELD_ISNODE, "FALSE")));
    }

    /**
     * <li>
     * <ul>
     * If the field name is {@link QueryConstants#FIELD_ID}
     * </ul>
     * <ul>
     * If the value is not a NodeRef (i.e. {@link NodeRef#isNodeRef(String)} returns false)
     * </ul>
     * )</li>
     *
     * the built query must be a {@link org.apache.lucene.search.TermQuery} having
     *
     * <ul>
     * <li>QueryConstants#FIELD_ID as field</li>
     * <li>the input value as value</li>
     * </ul>
     */
    @Test
    public void IDFieldTermQuery()
    {
        BooleanQuery booleanQuery = (BooleanQuery) parseAndBuildQuery(fieldQueryString(QueryConstants.FIELD_ID, AN_ID));
        Assert.assertEquals(2, booleanQuery.clauses().size());
        assertQueryEquals(fieldQueryString("_id", AN_ID), booleanQuery.clauses().get(1).getQuery());
    }

    /**
     * <li>
     * <ul>
     * If the field name is {@link QueryConstants#FIELD_ID}
     * </ul>
     * <ul>
     * If the value is not a NodeRef (i.e. {@link NodeRef#isNodeRef(String)} returns false)
     * </ul>
     * )</li>
     *
     * the built query must be a {@link org.apache.lucene.search.TermQuery} having
     *
     * <ul>
     * <li>QueryConstants#FIELD_ID as field</li>
     * <li>the input value as value</li>
     * </ul>
     *
     * Note this test uses a phrase query.
     */
    @Test
    public void IDFieldPhraseQuery()
    {
        BooleanQuery booleanQuery = (BooleanQuery) parseAndBuildQuery(fieldQueryString(FIELD_ID, quoted(AN_ID)));
        Assert.assertEquals(2, booleanQuery.clauses().size());
        assertQueryEquals(fieldQueryString("_id", AN_ID), booleanQuery.clauses().get(1).getQuery());
    }

    /**
     * <li>
     * <ul>
     * If the field name is {@link QueryConstants#FIELD_ID}
     * </ul>
     * <ul>
     * If the value is a NodeRef (i.e. {@link NodeRef#isNodeRef(String)} returns true)
     * </ul>
     * )</li>
     *
     * the built query must be a {@link org.apache.lucene.search.TermQuery} having
     *
     * <ul>
     * <li>QueryConstants#FIELD_ID as field</li>
     * <li>the identifier part (i.e. the part after the workspace://SpacesStore/) as value</li>
     * </ul>
     *
     * Note this test uses a phrase query.
     */
    @Test
    public void IDFieldPhraseQueryContainsNodeRef()
    {
        BooleanQuery booleanQuery = (BooleanQuery) parseAndBuildQuery(fieldQueryString(QueryConstants.FIELD_ID, quoted(A_NODEREF)));
        Assert.assertEquals(2, booleanQuery.clauses().size());
        assertQueryEquals(fieldQueryString("_id", AN_ID), booleanQuery.clauses().get(1).getQuery());
    }

    @Test
    public void sysNodeUuidFieldTermQueryIsRewrittenToIdQuery()
    {
        BooleanQuery booleanQuery = (BooleanQuery) parseAndBuildQuery(fieldQueryString(escape("sys:node-uuid"), AN_ID));
        Assert.assertEquals(2, booleanQuery.clauses().size());
        assertQueryEquals(fieldQueryString(LuceneQueryParser.ID_FIELD, AN_ID), booleanQuery.clauses().get(1).getQuery());
    }

    @Test
    public void sysNodeUuidFieldQueryWithNodeRefIsRewrittenToIdQuery()
    {
        BooleanQuery booleanQuery = (BooleanQuery) parseAndBuildQuery(fieldQueryString(escape("sys:node-uuid"), quoted(A_NODEREF)));
        Assert.assertEquals(2, booleanQuery.clauses().size());
        assertQueryEquals(fieldQueryString(LuceneQueryParser.ID_FIELD, AN_ID), booleanQuery.clauses().get(1).getQuery());
    }

    @Test
    public void ISNULLFieldWildcardQuery()
    {
        assertQueryEquals("-field:*", parseAndBuildQuery(fieldQueryString(FIELD_ISNULL, "field")));
    }

    @Test
    public void ISNOTNULLFieldWildcardQuery()
    {
        assertQueryEquals("field:*", parseAndBuildQuery(fieldQueryString(FIELD_ISNOTNULL, "field")));
    }

    @Test
    public void testPARENTFieldTermQuery()
    {
        BooleanQuery booleanQuery = (BooleanQuery) parseAndBuildQuery(fieldQueryString(FIELD_PARENT, AN_ID));
        Assert.assertEquals(2, booleanQuery.clauses().size());
        assertQueryEquals(fieldQueryString(FIELD_PARENT, AN_ID), booleanQuery.clauses().get(1).getQuery());
    }

    @Test
    public void testPARENTFieldPhraseQueryWithNodeRef()
    {
        BooleanQuery booleanQuery = (BooleanQuery) parseAndBuildQuery(fieldQueryString(FIELD_PARENT, quoted(A_NODEREF)));
        Assert.assertEquals(2, booleanQuery.clauses().size());
        assertQueryEquals(fieldQueryString(FIELD_PARENT, AN_ID), booleanQuery.clauses().get(1).getQuery());
    }

    @Test
    public void testANCESTORFieldTermQuery()
    {
        BooleanQuery booleanQuery = (BooleanQuery) parseAndBuildQuery(fieldQueryString(FIELD_ANCESTOR, AN_ID));
        Assert.assertEquals(2, booleanQuery.clauses().size());
        assertQueryEquals(fieldQueryString(FIELD_ANCESTOR, AN_ID), booleanQuery.clauses().get(1).getQuery());
    }

    @Test
    public void testANCESTORFieldPhraseQueryWithNodeRef()
    {
        BooleanQuery booleanQuery = (BooleanQuery) parseAndBuildQuery(fieldQueryString(FIELD_ANCESTOR, quoted(A_NODEREF)));
        Assert.assertEquals(2, booleanQuery.clauses().size());
        assertQueryEquals(fieldQueryString(FIELD_ANCESTOR, AN_ID), booleanQuery.clauses().get(1).getQuery());
    }

    @Test
    public void OWNERFieldShouldDelegateToDefaultQueryParser()
    {
        Stream.of("agazzarini", "maxred", "a1s3s83a", "383e8dj-")
                .forEach(value -> assertQueryEquals(
                        fieldQueryString(FIELD_OWNER, value),
                        parseAndBuildQuery(fieldQueryString(FIELD_OWNER, value))));
    }

    @Test
    public void TAGFieldShouldDelegateToDefaultQueryParser()
    {
        Stream.of("agazzarini", "maxred", "a1s3s83a", "383e8dj-")
                .forEach(value -> assertQueryEquals(
                        fieldQueryString(FIELD_TAG, value),
                        parseAndBuildQuery(fieldQueryString(FIELD_TAG, value))));
    }

    @Test
    public void READERFieldShouldDelegateToDefaultQueryParser()
    {
        Stream.of("agazzarini", "maxred", "a1s3s83a", "383e8dj-")
                .forEach(value -> assertQueryEquals(
                        fieldQueryString(FIELD_READER, value),
                        parseAndBuildQuery(fieldQueryString(FIELD_READER, value))));
    }

    @Test
    public void DENIEDFieldShouldDelegateToDefaultQueryParser()
    {
        Stream.of("agazzarini", "maxred", "a1s3s83a", "383e8dj-")
                .forEach(value -> assertQueryEquals(
                        fieldQueryString(FIELD_DENIED, value),
                        parseAndBuildQuery(fieldQueryString(FIELD_DENIED, value))));
    }

    @Test
    public void AUTHORITYFieldShouldDelegateToDefaultQueryParser()
    {
        Stream.of("agazzarini", "maxred", "a1s3s83a", "383e8dj-")
                .forEach(value -> assertQueryEquals(
                        fieldQueryString(FIELD_AUTHORITY, value),
                        parseAndBuildQuery(fieldQueryString(FIELD_AUTHORITY, value))));
    }

    @Test
    public void EXISTSFieldWithMatchingUnqualifiedPropertyName()
    {
        REQUEST_SPECIFIC_UNQUALIFIED_TEXT_ATTRIBUTES
                .forEach(propertyName -> assertQueryEquals(
                        fieldQueryString(FIELD_PROPERTIES, escape(toPrefixedForm(propertyName))),
                        parseAndBuildQuery(fieldQueryString(FIELD_EXISTS, escape(propertyName)))));
    }

    @Test
    public void EXISTSFieldWithMatchingPrefixedPropertyName()
    {
        REQUEST_SPECIFIC_PREFIXED_TEXT_ATTRIBUTES
                .forEach(prefixedPropertyName -> assertQueryEquals(
                        fieldQueryString(FIELD_PROPERTIES, escape(prefixedPropertyName)),
                        parseAndBuildQuery(fieldQueryString(FIELD_EXISTS, escape(prefixedPropertyName)))));
    }

    @Test
    public void EXISTSFieldWithMatchingFullyQualifiedPropertyName()
    {
        REQUEST_SPECIFIC_FULLY_QUALIFIED_TEXT_ATTRIBUTES
                .forEach(propertyName -> assertQueryEquals(
                        fieldQueryString(FIELD_PROPERTIES, escape(toPrefixedForm(propertyName))),
                        parseAndBuildQuery(
                                fieldQueryString(
                                        FIELD_EXISTS,
                                        escape(propertyName)))));
    }

    @Test
    public void EXISTSFieldWithUnmatchingProperty()
    {
        assertQueryEquals(
                fieldQueryString(FIELD_ID, "*"),
                parseAndBuildQuery(fieldQueryString(FIELD_EXISTS, FIELD_ID)));
    }

    @Test
    public void datatypeDefinitionQueryUsingFullyQualifiedName()
    {
        var allAttributesOfGivenType = REQUEST_SPECIFIC_FULLY_QUALIFIED_TEXT_ATTRIBUTES.stream()
                .map(QName::createQName)
                .collect(toList());

        when(dictionaryService.getAllProperties(DataTypeDefinition.DATE))
                .thenReturn(allAttributesOfGivenType);

        parser = new LuceneQueryParserUnderTest() {
            Optional<DataTypeDefinition> datatypeDefinition(String fieldName)
            {
                var dataTypeDefinition = mock(DataTypeDefinition.class);
                when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.DATE);
                return Optional.of(dataTypeDefinition);
            }
        };

        var query = parseAndBuildQuery(fieldQueryString(escape(DataTypeDefinition.DATE.toString()), A_TERM));

        assertQueryEquals(
                allAttributesOfGivenType.stream()
                        .map(qname -> qname.toPrefixString(resolver))
                        .map(AlfrescoQualifiedNameTranslator::encode)
                        .map(fieldName -> fieldQueryString(fieldName, A_TERM))
                        .collect(joining(" ")),
                query);
    }

    @Test
    public void datatypeDefinitionQueryUsingPrefixedName()
    {
        var allAttributesOfGivenType = REQUEST_SPECIFIC_FULLY_QUALIFIED_TEXT_ATTRIBUTES.stream()
                .map(QName::createQName)
                .collect(toList());

        when(dictionaryService.getAllProperties(DataTypeDefinition.DATE))
                .thenReturn(allAttributesOfGivenType);

        parser = new LuceneQueryParserUnderTest() {
            Optional<DataTypeDefinition> datatypeDefinition(String fieldName)
            {
                var dataTypeDefinition = mock(DataTypeDefinition.class);
                when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.DATE);
                return Optional.of(dataTypeDefinition);
            }
        };

        var query = parseAndBuildQuery(fieldQueryString(escape(DataTypeDefinition.DATE.toPrefixString(resolver)), A_TERM));

        assertQueryEquals(
                allAttributesOfGivenType.stream()
                        .map(qname -> qname.toPrefixString(resolver))
                        .map(AlfrescoQualifiedNameTranslator::encode)
                        .map(fieldName -> fieldQueryString(fieldName, A_TERM))
                        .collect(joining(" ")),
                query);
    }

    @Test
    public void datatypeDefinitionQueryUsingUnknownDatatypeName()
    {
        parser = new LuceneQueryParserUnderTest() {
            Optional<DataTypeDefinition> datatypeDefinition(String fieldName)
            {
                return Optional.empty();
            }
        };

        var query = parseAndBuildQuery(fieldQueryString(escape(DataTypeDefinition.DATE.toPrefixString(resolver)), A_TERM));

        assertQueryEquals(
                query.toString(),
                query);
    }

    @Test
    public void datatypeDefinitionQueryUsingDatatypeWithNoAssociatedProperties()
    {
        List<QName> allAttributesOfGivenType = emptyList();

        when(dictionaryService.getAllProperties(DataTypeDefinition.DATE))
                .thenReturn(allAttributesOfGivenType);

        parser = new LuceneQueryParserUnderTest() {
            Optional<DataTypeDefinition> datatypeDefinition(String fieldName)
            {
                var dataTypeDefinition = mock(DataTypeDefinition.class);
                when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.DATE);
                return Optional.of(dataTypeDefinition);
            }
        };

        var query = parseAndBuildQuery(fieldQueryString(escape(DataTypeDefinition.DATE.toPrefixString(resolver)), A_TERM));
        assertQueryEquals(query.toString(), query);
    }

    @Override
    protected Stream<String> unsupportedFields()
    {
        return Stream.of(
                // PATH QUERIES
                FIELD_PATHWITHREPEATS,
                FIELD_QNAME,
                FIELD_PRIMARYASSOCQNAME,
                FIELD_PRIMARYASSOCTYPEQNAME,
                FIELD_ISROOT,
                FIELD_ISCONTAINER,

                // No longer valid in Elasticsearch context
                FIELD_SOLR4_ID,
                FIELD_CASCADETX,
                FIELD_DBID,
                FIELD_TX,
                FIELD_TXID,
                FIELD_INTXID,
                FIELD_TXCOMMITTIME,
                FIELD_ACLID,
                FIELD_INACLTXID,
                FIELD_ACLTXID,
                FIELD_ACLTXCOMMITTIME,
                FIELD_TENANT,
                FIELD_OWNERSET,
                FIELD_READERSET,
                FIELD_AUTHORITYSET,
                FIELD_DENYSET,
                FIELD_FTSSTATUS,
                FIELD_FINGERPRINT);
    }

    @Override
    protected Stream<Function<String, Query>> queryBuilderFunctions()
    {
        return Stream.of(
                fieldName -> parser.getFieldQuery(fieldName, A_TERM, false),
                fieldName -> parser.getFieldQuery(fieldName, A_TERM, true));
    }

    @Override
    protected String queryTypeUnderTest()
    {
        return "Term/Phrase Query";
    }
}
