/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;
import static org.apache.lucene.search.BooleanClause.Occur.MUST_NOT;
import static org.apache.lucene.search.BooleanClause.Occur.SHOULD;

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
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ENCODING_SUFFIX;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_EXACTASPECT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_EXACTTYPE;
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
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_ISUNSET;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_LOCALE_SUFFIX;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_MIMETYPE_SUFFIX;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_NPATH;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_OWNER;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_OWNERSET;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PARENT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PATH;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PATHWITHREPEATS;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PNAME;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PRIMARYASSOCQNAME;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PRIMARYASSOCTYPEQNAME;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PRIMARYPARENT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_PROPERTIES;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_QNAME;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_READER;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_READERSET;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_SITE;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_SIZE_SUFFIX;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_SOLR4_ID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TAG;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TENANT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TEXT;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TX;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TXCOMMITTIME;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TXID;
import static org.alfresco.repo.search.adaptor.QueryConstants.FIELD_TYPE;
import static org.alfresco.repo.search.adaptor.QueryConstants.PROPERTY_FIELD_PREFIX;
import static org.alfresco.repo.search.impl.QueryParserUtils.matchDataTypeDefinition;
import static org.alfresco.repo.search.impl.QueryParserUtils.matchPropertyDefinition;
import static org.alfresco.repo.search.impl.elasticsearch.shared.ElasticsearchConstants.ALIVE;
import static org.alfresco.repo.search.impl.elasticsearch.util.CollectionUtils.safe;
import static org.alfresco.repo.site.SiteModel.TYPE_SITE;

import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.saxpath.base.XPathReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.search.impl.QueryParserUtils;
import org.alfresco.repo.search.impl.elasticsearch.contentmodelsync.config.ElasticsearchExactTermSearchConfig;
import org.alfresco.repo.search.impl.elasticsearch.model.FieldName;
import org.alfresco.repo.search.impl.elasticsearch.query.AlfrescoDefaultTextFields;
import org.alfresco.repo.search.impl.elasticsearch.query.language.FieldQueryTransformer;
import org.alfresco.repo.search.impl.elasticsearch.shared.translator.AlfrescoQualifiedNameTranslator;
import org.alfresco.repo.search.impl.elasticsearch.util.ThrowingFunction;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;

public class LuceneQueryParser extends QueryParser
{

    private static final Logger LOGGER = LoggerFactory.getLogger(LuceneQueryParser.class);

    public static final String UNKNOWN = "_unknown_";
    public static final String ID_FIELD = "_id";
    /** The primary hierarchy field name in Elasticsearch. */
    public static final String PRIMARY_HIERARCHY_FIELD = "primaryHierarchy";
    /** The primary parent field name in Elasticsearch. */
    public static final String PRIMARY_PARENT_FIELD = "PRIMARYPARENT";
    public static final String PARENT_FIELD = "PARENT";
    public static final String ANCESTOR_FIELD = "ANCESTOR";

    private static final List<String> DERIVED_FIELDS_SUFFIXES = List.of(FIELD_SIZE_SUFFIX,
            FIELD_MIMETYPE_SUFFIX,
            FIELD_ENCODING_SUFFIX,
            FIELD_LOCALE_SUFFIX);

    private static final List<String> ES_RESERVED_WORDS = List.of("AND", "OR", "NOT");

    private final NamespacePrefixResolver namespaceResolver;
    private final DictionaryService dictionaryService;
    private final SiteService siteService;
    private final ElasticsearchExactTermSearchConfig exactTermSearchConfig;
    private final SearchParameters parameters;
    private final FieldQueryTransformer fieldQueryTransformer = FieldQueryTransformer.DEFAULT;

    private final BiFunction<BooleanQuery.Builder, Query, BooleanQuery.Builder> accumulateDisjunctionClauses = (accumulator, nthQuery) -> accumulator.add(nthQuery, SHOULD);

    private final BinaryOperator<BooleanQuery.Builder> combineBooleanQueries = (builder, anotherBuilder) -> {
        anotherBuilder.build().clauses().forEach(builder::add);
        return builder;
    };

    private final Supplier<BooleanQuery.Builder> booleanQuerySupplier = BooleanQuery.Builder::new;

    public LuceneQueryParser(NamespacePrefixResolver namespaceResolver, DictionaryService dictionaryService, SiteService siteService, SearchParameters parameters)
    {
        this(namespaceResolver, dictionaryService, siteService, parameters, null);
    }

    public LuceneQueryParser(NamespacePrefixResolver namespaceResolver, DictionaryService dictionaryService, SiteService siteService, SearchParameters parameters, ElasticsearchExactTermSearchConfig exactTermSearchConfig)
    {
        super(FIELD_TEXT, new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName)
            {
                return new TokenStreamComponents(new WhitespaceTokenizer());
            }
        });

        this.dictionaryService = dictionaryService;
        this.siteService = siteService;
        this.parameters = parameters;
        this.namespaceResolver = namespaceResolver;
        this.exactTermSearchConfig = exactTermSearchConfig;
        this.setSplitOnWhitespace(false);
        this.setAllowLeadingWildcard(true);
        this.setDefaultOperator(ofNullable(parameters.getDefaultOperator())
                .map(operator -> Operator.valueOf(operator.name())).orElse(OR_OPERATOR));
    }

    @Override
    public Query getFieldQuery(String field, String queryText, int slop)
    {
        try (PhraseSlopData data = PhraseSlopData.set(queryText, slop))
        {
            return super.getFieldQuery(field, queryText, slop);
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Query getFieldQuery(String field, String queryText, boolean quoted)
    {
        boolean exactTermSearch = false;
        boolean untokenisedSearch = false;
        final String fieldName;
        if (FieldName.isExactTermSearch(field))
        {
            fieldName = FieldName.fromExactTermSearch(field).raw();
            exactTermSearch = true;
        }
        else if (FieldName.isUntokenized(field))
        {
            fieldName = FieldName.fromUntokenized(field).raw();
            untokenisedSearch = true;
        }
        else
        {
            fieldName = field;
        }

        switch (fieldName)
        {
        case FIELD_PATH:
            try
            {
                validatePathQuery(queryText, FIELD_PATH);
                return PathQueryConverter.toLucenePathQuery(queryText);
            }
            catch (SAXPathException e)
            {
                logIgnoringField("XPath syntax not supported:" + queryText);
                return null;
            }
        case FIELD_ANCESTOR:
            // Note this does not yet support querying ancestors in category paths (ACS-3831).
            return nodeRefTermQuery(queryText, ANCESTOR_FIELD);
        case FIELD_PARENT:
            // Note this does not yet support querying parents in category paths (ACS-3832).
            return nodeRefTermQuery(queryText, PARENT_FIELD);
        case FIELD_PRIMARYPARENT:
            return nodeRefTermQuery(queryText, PRIMARY_PARENT_FIELD);
        case FIELD_TEXT:
            return textFieldQuery(textFieldName -> getFieldQuery(textFieldName, queryText, quoted), exactTermSearch);
        case FIELD_ALL:
            return allFieldQuery(allFieldName -> getFieldQuery(allFieldName, queryText, quoted), exactTermSearch);
        case FIELD_ID:
            return nodeRefTermQuery(queryText, ID_FIELD);
        case FIELD_ISNODE:
            return isNodeFieldQuery(queryText);
        case FIELD_CLASS:
            return classDefinitionQuery(queryText, quoted);
        case FIELD_TYPE:
            return typeQuery(queryText, false);
        case FIELD_EXACTTYPE:
            return typeQuery(queryText, true);
        case FIELD_ASPECT:
            return aspectQuery(queryText, false);
        case FIELD_EXACTASPECT:
            return aspectQuery(queryText, true);
        case FIELD_ISNOTNULL:
        case FIELD_EXISTS:
            return existsQuery(queryText);
        case FIELD_ISNULL:
            return isNullQuery(queryText);
        case FIELD_OWNER:
        case FIELD_READER:
        case FIELD_DENIED:
        case FIELD_AUTHORITY:
        case FIELD_TAG:
            return luceneTermQuery(fieldName, escape(queryText, true));
        case FIELD_SITE:
            return siteQuery(queryText);
        case FIELD_ISUNSET:
            return isUnsetQuery(queryText);
        case FIELD_PATHWITHREPEATS:
        case FIELD_PNAME:
        case FIELD_NPATH:
        case FIELD_QNAME:
        case FIELD_PRIMARYASSOCQNAME:
        case FIELD_PRIMARYASSOCTYPEQNAME:
        case FIELD_OWNERSET:
        case FIELD_READERSET:
        case FIELD_DENYSET:
        case FIELD_AUTHORITYSET:
        case FIELD_SOLR4_ID:
        case FIELD_CASCADETX:
        case FIELD_DBID:
        case FIELD_TX:
        case FIELD_TXID:
        case FIELD_INTXID:
        case FIELD_TXCOMMITTIME:
        case FIELD_ACLID:
        case FIELD_INACLTXID:
        case FIELD_ACLTXID:
        case FIELD_ACLTXCOMMITTIME:
        case FIELD_FTSSTATUS:
        case FIELD_ISROOT:
        case FIELD_ISCONTAINER:
        case FIELD_TENANT:
        case FIELD_FINGERPRINT:
            return unsupportedField(fieldName);
        default:
            return getPropertyQuery(fieldName, escape(queryText, true), quoted, exactTermSearch, untokenisedSearch)
                    .or(() -> datatypeDefinition(fieldName).map(def -> dataTypeDefinitionQuery(def, propertyName -> getFieldQuery(propertyName, queryText, quoted))))
                    .orElseGet(() -> luceneFieldQuery(fieldName, queryText, quoted));
        }
    }

    /**
     * If field is untokenized and contains a wildcard, build wildcard query instead of a string query
     */

    private Optional<Query> getPropertyQuery(String fieldName, String queryText, boolean quoted, boolean exactTermSearch, boolean untokenisedSearch)
    {
        if (containsWildcard(queryText))
        {
            // Untokenised fields need to use a WildcardQuery
            if (isUntokenizedField(fieldName) || untokenisedSearch)
            {
                return propertyWildcardQuery(fieldName, queryText, exactTermSearch, untokenisedSearch);
            }

            // If the field is tokenised both or true, we need
            // to split the terms to be able to use the wildcard in the term
            this.setDefaultOperator(Operator.AND);
            quoted = false;
        }

        return propertyFieldQuery(fieldName, queryText, quoted, exactTermSearch, untokenisedSearch);
    }

    public boolean containsWildcard(String queryText)
    {
        // Contains unescaped * or ? or %
        return queryText.matches(".*(?<!\\\\)[*?%].*");
    }

    public boolean isUntokenizedField(String fieldName)
    {
        return Optional.ofNullable(fieldName)
                .map(name -> name.startsWith(PROPERTY_FIELD_PREFIX) ? name.substring(1) : name)
                .map(name -> QName.resolveToQName(namespaceResolver, name))
                .map(qname -> matchPropertyDefinition(qname.getNamespaceURI(), namespaceResolver, dictionaryService, qname.getLocalName()))
                .map(PropertyDefinition::getIndexTokenisationMode)
                .map(IndexTokenisationMode.FALSE::equals)
                .orElse(false);
    }

    public boolean isTokenizedOnlyField(String fieldName)
    {
        return ofNullable(fieldName)
                .map(name -> name.startsWith(PROPERTY_FIELD_PREFIX) ? name.substring(1) : name)
                .map(name -> QName.resolveToQName(namespaceResolver, name))
                .map(qname -> matchPropertyDefinition(qname.getNamespaceURI(), namespaceResolver, dictionaryService,
                        qname.getLocalName()))
                .map(PropertyDefinition::getIndexTokenisationMode)
                .map(IndexTokenisationMode.TRUE::equals)
                .orElse(false);
    }

    /**
     * This method will validate if PATH query is correct XPath
     * 
     * @param queryText
     *            query text
     * @param field
     *            queried field
     * @throws SAXPathException
     */
    private void validatePathQuery(String queryText, String field) throws SAXPathException
    {
        XPathReader reader = new XPathReader();
        LuceneXPathHandler handler = new LuceneXPathHandler(field);
        reader.setXPathHandler(handler);
        reader.parse(queryText);
    }

    @Override
    public Query getPrefixQuery(String field, String queryText)
    {
        boolean exactTermSearch = false;
        final String fieldName;
        if (FieldName.isExactTermSearch(field))
        {
            fieldName = FieldName.fromExactTermSearch(field).raw();
            exactTermSearch = true;
        }
        else
        {
            fieldName = field;
        }

        switch (fieldName)
        {
        case FIELD_SOLR4_ID:
        case FIELD_PATH:
        case FIELD_PATHWITHREPEATS:
        case FIELD_ANCESTOR:
        case FIELD_PARENT:
        case FIELD_PRIMARYPARENT:
        case FIELD_QNAME:
        case FIELD_PRIMARYASSOCQNAME:
        case FIELD_PRIMARYASSOCTYPEQNAME:
        case FIELD_ISROOT:
        case FIELD_ISUNSET:
        case FIELD_ISNULL:
        case FIELD_ISCONTAINER:
        case FIELD_ISNOTNULL:
        case FIELD_EXISTS:
        case FIELD_FTSSTATUS:
        case FIELD_CASCADETX:
        case FIELD_DBID:
        case FIELD_TX:
        case FIELD_TXID:
        case FIELD_INTXID:
        case FIELD_ACLID:
        case FIELD_ACLTXID:
        case FIELD_INACLTXID:
        case FIELD_TXCOMMITTIME:
        case FIELD_ACLTXCOMMITTIME:
        case FIELD_TENANT:
        case FIELD_FINGERPRINT:
        case FIELD_OWNERSET:
        case FIELD_READERSET:
        case FIELD_DENYSET:
        case FIELD_AUTHORITYSET:
        case FIELD_CLASS:
            return unsupportedWithMessage("Prefix Queries are not supported for " + fieldName);
        case FIELD_SITE:
            return siteQuery(queryText);
        case FIELD_TYPE:
        case FIELD_EXACTTYPE:
            return qnamePrefixQuery(FIELD_TYPE, queryText);
        case FIELD_ASPECT:
        case FIELD_EXACTASPECT:
            return qnamePrefixQuery(FIELD_ASPECT, queryText);
        case FIELD_TEXT:
            return textFieldQuery(textFieldName -> getPrefixQuery(textFieldName, queryText), exactTermSearch);
        case FIELD_ID:
            return uuidPrefixQuery(queryText);
        case FIELD_ALL:
            return allFieldQuery(propertyName -> getPrefixQuery(propertyName, queryText), exactTermSearch);
        default:
            return propertyPrefixQuery(fieldName, escape(queryText, true), exactTermSearch)
                    .or(() -> datatypeDefinition(fieldName).map(def -> dataTypeDefinitionQuery(def, propertyName -> getPrefixQuery(propertyName, queryText))))
                    .orElseGet(() -> lucenePrefixQuery(fieldName, queryText));
        }
    }

    @Override
    public Query getRangeQuery(String field, String part1, String part2, boolean startInclusive, boolean endInclusive)
    {
        boolean exactTermSearch = false;
        final String fieldName;
        if (FieldName.isExactTermSearch(field))
        {
            fieldName = FieldName.fromExactTermSearch(field).raw();
            exactTermSearch = true;
        }
        else
        {
            fieldName = field;
        }

        switch (fieldName)
        {
        case FIELD_TEXT:
            return textFieldQuery(textPropertyName -> getRangeQuery(textPropertyName, part1, part2, startInclusive, endInclusive), exactTermSearch);
        case FIELD_ALL:
            return allFieldQuery(propertyName -> getRangeQuery(propertyName, part1, part2, startInclusive, endInclusive), exactTermSearch);
        default:
            return propertyRangeQuery(fieldName, part1, part2, startInclusive, endInclusive, exactTermSearch)
                    .or(() -> datatypeDefinition(fieldName).map(def -> dataTypeDefinitionQuery(def, propertyName -> getRangeQuery(propertyName, part1, part2, startInclusive, endInclusive))))
                    .orElseGet(() -> unsupportedWithMessage("Range Queries are not support for " + fieldName));
        }
    }

    @Override
    public Query getWildcardQuery(String field, String queryText)
    {
        boolean exactTermSearch = false;
        final String fieldName;
        if (FieldName.isExactTermSearch(field))
        {
            fieldName = FieldName.fromExactTermSearch(field).raw();
            exactTermSearch = true;
        }
        else
        {
            fieldName = field;
        }
        switch (fieldName)
        {
        case FIELD_SOLR4_ID:
        case FIELD_PATH:
        case FIELD_PATHWITHREPEATS:
        case FIELD_ANCESTOR:
        case FIELD_PARENT:
        case FIELD_PRIMARYPARENT:
        case FIELD_QNAME:
        case FIELD_PRIMARYASSOCQNAME:
        case FIELD_PRIMARYASSOCTYPEQNAME:
        case FIELD_ISROOT:
        case FIELD_ISUNSET:
        case FIELD_ISNULL:
        case FIELD_ISCONTAINER:
        case FIELD_ISNOTNULL:
        case FIELD_EXISTS:
        case FIELD_FTSSTATUS:
        case FIELD_CASCADETX:
        case FIELD_DBID:
        case FIELD_TX:
        case FIELD_TXID:
        case FIELD_INTXID:
        case FIELD_ACLID:
        case FIELD_ACLTXID:
        case FIELD_INACLTXID:
        case FIELD_TXCOMMITTIME:
        case FIELD_ACLTXCOMMITTIME:
        case FIELD_TENANT:
        case FIELD_FINGERPRINT:
        case FIELD_OWNERSET:
        case FIELD_READERSET:
        case FIELD_DENYSET:
        case FIELD_AUTHORITYSET:
        case FIELD_CLASS:
            return unsupportedWithMessage("Wildcard Queries are not supported for " + field);
        case FIELD_SITE:
            return siteQuery(queryText);
        case FIELD_TYPE:
        case FIELD_EXACTTYPE:
            return qnameWildcardQuery(FIELD_TYPE, queryText);
        case FIELD_ASPECT:
        case FIELD_EXACTASPECT:
            return qnameWildcardQuery(FIELD_ASPECT, queryText);
        case FIELD_ALL:
            return allFieldQuery(propertyName -> getWildcardQuery(propertyName, queryText), exactTermSearch);
        case FIELD_TEXT:
            return textFieldQuery(textFieldName -> getWildcardQuery(textFieldName, queryText), exactTermSearch);
        default:
            return propertyWildcardQuery(fieldName, queryText, exactTermSearch)
                    .or(() -> datatypeDefinition(fieldName).map(def -> dataTypeDefinitionQuery(def, propertyName -> getWildcardQuery(propertyName, queryText))))
                    .orElseGet(() -> luceneWildcardQuery(fieldName, queryText));
        }
    }

    @Override
    public Query getFuzzyQuery(String field, String termStr, float minSimilarity)
    {
        boolean exactTermSearch = false;
        final String fieldName;
        if (FieldName.isExactTermSearch(field))
        {
            fieldName = FieldName.fromExactTermSearch(field).raw();
            exactTermSearch = true;
        }
        else
        {
            fieldName = field;
        }

        switch (fieldName)
        {
        case FIELD_SOLR4_ID:
        case FIELD_PATH:
        case FIELD_PATHWITHREPEATS:
        case FIELD_ANCESTOR:
        case FIELD_PARENT:
        case FIELD_PRIMARYPARENT:
        case FIELD_QNAME:
        case FIELD_PRIMARYASSOCQNAME:
        case FIELD_PRIMARYASSOCTYPEQNAME:
        case FIELD_SITE:
        case FIELD_TYPE:
        case FIELD_EXACTTYPE:
        case FIELD_ASPECT:
        case FIELD_EXACTASPECT:
        case FIELD_ISNODE:
        case FIELD_ISROOT:
        case FIELD_ISUNSET:
        case FIELD_ISNULL:
        case FIELD_ISCONTAINER:
        case FIELD_ISNOTNULL:
        case FIELD_EXISTS:
        case FIELD_FTSSTATUS:
        case FIELD_CASCADETX:
        case FIELD_DBID:
        case FIELD_TX:
        case FIELD_TXID:
        case FIELD_INTXID:
        case FIELD_ACLID:
        case FIELD_ACLTXID:
        case FIELD_INACLTXID:
        case FIELD_TXCOMMITTIME:
        case FIELD_ACLTXCOMMITTIME:
        case FIELD_TENANT:
        case FIELD_FINGERPRINT:
        case FIELD_OWNERSET:
        case FIELD_READERSET:
        case FIELD_DENYSET:
        case FIELD_AUTHORITYSET:
            return unsupportedField("Fuzzy queries are not supported for " + field);
        case FIELD_CLASS:
            throw new UnsupportedOperationException("Fuzzy queries are not supported for " + field);
        case FIELD_ALL:
            return allFieldQuery(textFieldName -> getFuzzyQuery(textFieldName, termStr, minSimilarity), exactTermSearch);
        case FIELD_TEXT:
            return textFieldQuery(textFieldName -> getFuzzyQuery(textFieldName, termStr, minSimilarity), exactTermSearch);
        default:
            return propertyFuzzyQuery(fieldName, termStr, minSimilarity, exactTermSearch)
                    .or(() -> datatypeDefinition(fieldName).map(def -> dataTypeDefinitionQuery(def, propertyName -> getFuzzyQuery(propertyName, termStr, minSimilarity))))
                    .orElseGet(() -> luceneFuzzyQuery(fieldName, termStr, minSimilarity));
        }
    }

    @Override
    protected Query getRegexpQuery(String field, String termStr)
    {
        return unsupportedWithMessage("Regexp queries are not supported for " + field);
    }

    protected Optional<Query> propertyFieldQuery(String fieldName, String text, boolean quoted, boolean exactTermSearch)
    {
        return propertyFieldQuery(fieldName, text, quoted, exactTermSearch, false);
    }

    protected Optional<Query> propertyFieldQuery(String fieldName, String text, boolean quoted, boolean exactTermSearch, boolean untokenisedSearch)
    {
        return propertyQuery(fieldName, elasticsearchField -> luceneFieldQuery(elasticsearchField, text, quoted), exactTermSearch, untokenisedSearch);
    }

    protected Optional<Query> propertyWildcardQuery(String fieldName, String termStr, boolean exactTermSearch)
    {
        return propertyQuery(fieldName, elasticsearchField -> luceneWildcardQuery(elasticsearchField, termStr), exactTermSearch);
    }

    protected Optional<Query> propertyWildcardQuery(String fieldName, String termStr, boolean exactTermSearch, boolean untokenisedSearch)
    {
        return propertyQuery(fieldName, elasticsearchField -> luceneWildcardQuery(elasticsearchField, termStr), exactTermSearch, untokenisedSearch);
    }

    protected Optional<Query> propertyFuzzyQuery(String fieldName, String termStr, float minSimilarity, boolean exactTermSearch)
    {
        return propertyQuery(fieldName, elasticsearchField -> luceneFuzzyQuery(elasticsearchField, termStr, minSimilarity), exactTermSearch);
    }

    protected Optional<Query> propertyRangeQuery(String fieldName, String part1, String part2, boolean startInclusive, boolean endInclusive, boolean exactTermSearch)
    {
        return propertyQuery(fieldName, elasticsearchField -> luceneRangeQuery(elasticsearchField, part1, part2, startInclusive, endInclusive), exactTermSearch);
    }

    protected Optional<Query> propertyPrefixQuery(String fieldName, String text, boolean exactTermSearch)
    {
        return propertyQuery(fieldName, elasticsearchField -> new PrefixQuery(new Term(elasticsearchField, text)), exactTermSearch);
    }

    /**
     * Creates a boolean disjunction query using the properties associated to the input field, which is supposed to be data type.
     */
    Query dataTypeDefinitionQuery(DataTypeDefinition definition, Function<String, Query> clauseBuilder)
    {
        var query = safe(dictionaryService.getAllProperties(definition.getName())).stream()
                .map(qname -> qname.toPrefixString(namespaceResolver))
                .map(propertyPrefixedName -> clauseBuilder.apply(PROPERTY_FIELD_PREFIX + propertyPrefixedName))
                .reduce(booleanQuerySupplier.get(), accumulateDisjunctionClauses, combineBooleanQueries)
                .build();

        return safe(query.clauses()).isEmpty() ? null : query;
    }

    Query textFieldQuery(ThrowingFunction<String, Query, ParseException> queryClauseBuilder, boolean exactTermSearch)
    {
        return booleanQuerySupplier.get()
                .add(of(safe(parameters.getTextAttributes()))
                        .filter(not(Collection::isEmpty))
                        .orElse(AlfrescoDefaultTextFields.FULLY_QUALIFIED_NAME_SET)
                        .stream()
                        .map(attribute -> QName.resolveToQName(namespaceResolver, attribute))
                        .map(qname -> PROPERTY_FIELD_PREFIX + qname.toPrefixString(namespaceResolver))
                        .map(name -> exactTermSearch ? queryClauseBuilder.apply(FieldName.exactTermSearch(name)) : queryClauseBuilder.apply(name))
                        .filter(Objects::nonNull)
                        .reduce(booleanQuerySupplier.get(), accumulateDisjunctionClauses, combineBooleanQueries)
                        .build(), getDefaultOperator() == Operator.AND ? MUST : SHOULD)
                .build();
    }

    Query isNodeFieldQuery(String text)
    {
        return "T".equalsIgnoreCase(text) || "true".equalsIgnoreCase(text) || "1".equals(text)
                ? new MatchAllDocsQuery()
                : booleanQuerySupplier.get().add(new MatchAllDocsQuery(), BooleanClause.Occur.MUST_NOT).build();
    }

    Query isNullQuery(String queryText)
    {
        return booleanQuerySupplier.get().add(existsQuery(queryText), BooleanClause.Occur.MUST_NOT).build();
    }

    Query uuidPrefixQuery(String id)
    {
        String field = ID_FIELD;
        return booleanQuerySupplier.get().add(luceneTermQuery(ALIVE, "true"), BooleanClause.Occur.MUST)
                .add((NodeRef.isNodeRef(id)
                        ? lucenePrefixQuery(field, id.substring(id.lastIndexOf("/") + 1))
                        : lucenePrefixQuery(field, id)), BooleanClause.Occur.MUST)
                .build();
    }

    private Query nodeRefTermQuery(String id, String field)
    {
        String nodeId = NodeRef.isNodeRef(id) ? id.substring(id.lastIndexOf("/") + 1) : id;
        return booleanQuerySupplier.get()
                .add(luceneTermQuery(ALIVE, "true"), MUST)
                .add(luceneTermQuery(field, escape(nodeId, true)), MUST)
                .build();
    }

    Query classDefinitionQuery(String text, boolean quoted)
    {
        ClassDefinition valueToSearch = QueryParserUtils.matchClassDefinition(
                parameters.getNamespace(),
                namespaceResolver,
                dictionaryService,
                text);
        return ofNullable(valueToSearch)
                .map(classToSearch -> classToSearch.isAspect() ? FIELD_ASPECT : FIELD_TYPE)
                .map(fieldName -> getFieldQuery(fieldName, text, quoted))
                .orElseGet(() -> luceneTermQuery(FIELD_TYPE, UNKNOWN));
    }

    Query qnameWildcardQuery(String fieldName, String queryText)
    {
        /* the reason for discard escape characters is to be found in: /org/apache/lucene/queryparser/classic/QueryParserBase.class:505 where in the case of wildcard queries, in opposition to what happens for prefix queries no discard of escaping characters happens. */
        String escapeCharsDiscarded = queryText.replace("\\", "");
        QName fullyQualifiedQueryText = QName.createQName(QueryParserUtils.expandQName(parameters.getNamespace(), namespaceResolver,
                escapeCharsDiscarded));

        String shortQualifiedQueryText = fullyQualifiedQueryText.toPrefixString(namespaceResolver);
        return new WildcardQuery(new Term(fieldName, escape(shortQualifiedQueryText, true)));
    }

    Query qnamePrefixQuery(String fieldName, String queryText)
    {
        QName fullyQualifiedQueryText = QName.createQName(QueryParserUtils.expandQName(parameters.getNamespace(), namespaceResolver, queryText));
        String shortQualifiedQueryText = fullyQualifiedQueryText.toPrefixString(namespaceResolver);
        return new PrefixQuery(new Term(fieldName, escape(shortQualifiedQueryText, true)));
    }

    Query typeQuery(String queryText, boolean excludeSubclasses)
    {
        var typeClassDefinition = QueryParserUtils.matchTypeDefinition(
                parameters.getNamespace(),
                namespaceResolver,
                dictionaryService, queryText);

        return aspectOrTypeQuery(excludeSubclasses, FIELD_TYPE, typeClassDefinition);
    }

    Query aspectQuery(String queryText, boolean excludeSubclasses)
    {
        var aspectClassDefinition = QueryParserUtils.matchAspectDefinition(
                parameters.getNamespace(),
                namespaceResolver,
                dictionaryService,
                queryText);
        return aspectOrTypeQuery(excludeSubclasses, FIELD_ASPECT, aspectClassDefinition);
    }

    /**
     * Compose method for ASPECT & TYPE queries, which provides the same behaviour in terms of
     *
     * <ul>
     * <li>querying only the exact type / aspect the query text refers to</li>
     * <li>querying the exact type / aspect the query text refers to + the direct subclasses</li>
     * </ul>
     */
    Query aspectOrTypeQuery(boolean excludeSubclasses, String fieldName, ClassDefinition classDefinition)
    {
        if (classDefinition == null)
        {
            return luceneTermQuery(fieldName, UNKNOWN);
        }

        return excludeSubclasses
                ? luceneTermQuery(fieldName, escape(classDefinition.getName().toPrefixString(namespaceResolver), false))
                : subclasses(classDefinition, fieldName)
                        .filter(qname -> {
                            var subclassDefinition = FIELD_ASPECT.equals(fieldName)
                                    ? dictionaryService.getAspect(qname)
                                    : dictionaryService.getType(qname);
                            return subclassDefinition.getName().equals(subclassDefinition.getName())
                                    || subclassDefinition.getIncludedInSuperTypeQuery();
                        })
                        .map(qname -> qname.toPrefixString(namespaceResolver))
                        .map(qname -> luceneTermQuery(fieldName, escape(qname, false)))
                        .reduce(booleanQuerySupplier.get(), accumulateDisjunctionClauses, combineBooleanQueries)
                        .build();
    }

    Stream<QName> subclasses(ClassDefinition classDefinition, String fieldName)
    {
        return safe(FIELD_ASPECT.equals(fieldName)
                ? dictionaryService.getSubAspects(classDefinition.getName(), true)
                : dictionaryService.getSubTypes(classDefinition.getName(), true)).stream();
    }

    Query isUnsetQuery(String queryText)
    {
        var propertyDefinition = matchPropertyDefinition(
                parameters.getNamespace(),
                namespaceResolver,
                dictionaryService,
                queryText);

        return ofNullable(propertyDefinition)
                .map(PropertyDefinition::getContainerClass)
                .map(containerClass -> {
                    var fieldName = containerClass.isAspect() ? FIELD_ASPECT : FIELD_TYPE;
                    var typeSubQuery = getFieldQuery(fieldName, containerClass.getName().toString(), false);
                    var presenceSubQuery = luceneTermQuery(FIELD_PROPERTIES, escape(propertyDefinition.getName().toPrefixString()));
                    return booleanQuerySupplier.get()
                            .add(typeSubQuery, MUST)
                            .add(presenceSubQuery, BooleanClause.Occur.MUST_NOT)
                            .build();
                })
                .orElseGet(() -> booleanQuerySupplier.get()
                        .add(new MatchAllDocsQuery(), MUST)
                        .build());
    }

    Query existsQuery(String queryText)
    {
        var propertyDefinition = matchPropertyDefinition(
                parameters.getNamespace(),
                namespaceResolver,
                dictionaryService,
                queryText);

        return ofNullable(propertyDefinition)
                .map(PropertyDefinition::getName)
                .map(qname -> qname.toPrefixString(namespaceResolver))
                .map(propertyPrefixedName -> luceneTermQuery(FIELD_PROPERTIES, escape(propertyPrefixedName, false)))
                .orElseGet(() -> getWildcardQuery(queryText, "*"));
    }

    /**
     * A field name is interpreted as a property if it is prefixed by @
     *
     * @param fieldName
     *            the field name.
     * @return true if the field name is a property field, false otherwise.
     */
    boolean isPropertyField(String fieldName)
    {
        return (fieldName.startsWith(PROPERTY_FIELD_PREFIX));
    }

    /**
     * The ALL field is a placeholder for multiple fields. Those fields can be (in priority order)
     *
     * <ul>
     * <li>indicated per-request in the {@link SearchParameters#getAllAttributes()}</li>
     * <li>{@link DictionaryService#getAllProperties(QName)}</li>
     * </ul>
     *
     * The input builder is applied in order to build a SHOULD clause for each field.
     */
    Query allFieldQuery(Function<String, Query> clauseBuilder, boolean exactTermSearch)
    {
        var allAttributes = safe(parameters.getAllAttributes()).isEmpty()
                ? safe(dictionaryService.getAllProperties(null)
                        .stream()
                        .map(qname -> qname.toPrefixString(namespaceResolver))
                        .collect(toList()))
                : parameters.getAllAttributes();

        return booleanQuerySupplier.get()
                .add(allAttributes.stream()
                        .map(qname -> exactTermSearch ? clauseBuilder.apply(FieldName.exactTermSearch(PROPERTY_FIELD_PREFIX + qname)) : clauseBuilder.apply(PROPERTY_FIELD_PREFIX + qname))
                        .reduce(booleanQuerySupplier.get(), accumulateDisjunctionClauses, combineBooleanQueries)
                        .build(), getDefaultOperator() == Operator.AND ? MUST : SHOULD)
                .build();
    }

    Optional<Query> propertyQuery(String fieldName, Function<String, Query> toQuery, boolean exactTermSearch)
    {
        return propertyQuery(fieldName, toQuery, exactTermSearch, false);
    }

    Optional<Query> propertyQuery(String fieldName, Function<String, Query> toQuery, boolean exactTermSearch, boolean untokenisedSearch)
    {
        var matchingSuffix = getMatchingSuffix(fieldName);

        return of(fieldName)
                .flatMap(this::stripPrefixAndSuffix)
                .map(name -> matchPropertyDefinition(parameters.getNamespace(), namespaceResolver, dictionaryService, name))
                .map(propertyDefinition -> validateExactTermSearch(exactTermSearch, propertyDefinition))
                .map(PropertyDefinition::getName)
                .map(qname -> qname.toPrefixString(namespaceResolver))
                .map(prefixedName -> matchingSuffix.map(suffix -> prefixedName + suffix).orElse(prefixedName))
                .map(name -> exactTermSearch ? FieldName.exactTermSearch(name) : untokenisedSearch ? FieldName.untokenized(name) : AlfrescoQualifiedNameTranslator.encode(name))
                .map(toQuery);
    }

    private static Optional<String> getMatchingSuffix(String fieldName)
    {
        return DERIVED_FIELDS_SUFFIXES.stream()
                .filter(fieldName::endsWith)
                .findAny();
    }

    public Optional<String> stripPrefixAndSuffix(String fieldName)
    {
        return of(fieldName)
                .map(name -> name.startsWith(PROPERTY_FIELD_PREFIX) ? name.substring(1) : name)
                .map(name -> getMatchingSuffix(fieldName).map(suffix -> name.replace(suffix, "")).orElse(name));
    }

    private PropertyDefinition validateExactTermSearch(boolean exactTermSearch, PropertyDefinition property)
    {
        if (exactTermSearch && !exactTermSearchConfig.isExactTermSearchEnabled(property))
        {
            throw new UnsupportedOperationException("Exact term search is not supported for property: " + property.getName().toString());
        }
        else
        {
            return property;
        }
    }

    /**
     * Returns the {@link DataTypeDefinition} instance associated to the input field name.
     *
     * @param fieldName
     *            the field name.
     * @return an optional wrapping the datatype definition of the given field, an empty optional in case no datatype can be found associated to the input field.
     */
    Optional<DataTypeDefinition> datatypeDefinition(String fieldName)
    {
        return ofNullable(matchDataTypeDefinition(
                parameters.getNamespace(),
                namespaceResolver,
                dictionaryService,
                fieldName));
    }

    // The methods below are plain wrappers of the Lucene methods
    // Their only purpose is to avoid catching checked exceptions in the calling code.
    Query luceneFieldQuery(String field, String queryText, boolean quoted)
    {
        try
        {
            final PhraseSlopData currentPhraseSlopData = PhraseSlopData.current();
            if (currentPhraseSlopData.matches(queryText))
            {
                return super.createPhraseQuery(field, queryText, currentPhraseSlopData.getSlop());
            }
            return super.getFieldQuery(field, queryText, quoted);
        }
        catch (ParseException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    Query luceneTermQuery(String field, String queryText)
    {
        queryText = fieldQueryTransformer.transformTerm(field, queryText);
        return new TermQuery(new Term(field, queryText));
    }

    Query lucenePrefixQuery(String field, String queryText)
    {
        try
        {
            return super.getPrefixQuery(field, queryText);
        }
        catch (ParseException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    Query luceneWildcardQuery(String field, String termStr)
    {
        try
        {
            return super.getWildcardQuery(field, termStr);
        }
        catch (ParseException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    Query luceneFuzzyQuery(String field, String termStr, float minSimilarity)
    {
        try
        {
            return super.getFuzzyQuery(field, termStr, minSimilarity);
        }
        catch (ParseException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    Query luceneRangeQuery(String field, String part1, String part2, boolean startInclusive, boolean endInclusive)
    {
        try
        {
            return super.getRangeQuery(field, part1, part2, startInclusive, endInclusive);
        }
        catch (ParseException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    Query siteQuery(String queryText)
    {
        if ("_EVERYTHING_".equals(queryText))
        {
            return isNodeFieldQuery(Boolean.TRUE.toString());
        }
        if ("_ALL_SITES_".equals(queryText))
        {
            return allSitesQuery();
        }

        final String nodeId = getSiteHierarchyNode(queryText).orElse(UNKNOWN);
        return luceneTermQuery(PRIMARY_HIERARCHY_FIELD, nodeId);
    }

    private Query allSitesQuery()
    {
        final NodeRef siteRoot = siteService.getSiteRoot();
        if (siteRoot == null || siteRoot.getId() == null)
        {
            return luceneTermQuery(PRIMARY_HIERARCHY_FIELD, UNKNOWN);
        }

        Builder builder = booleanQuerySupplier.get()
                .add(luceneTermQuery(PRIMARY_HIERARCHY_FIELD, siteRoot.getId()), MUST)
                .add(typeQuery(TYPE_SITE.toString(), true), Occur.MUST_NOT);

        Optional.ofNullable(siteService.getSite("surf-config"))
                .map(SiteInfo::getNodeRef)
                .map(NodeRef::getId)
                .ifPresent(id -> builder.add(luceneTermQuery(PRIMARY_HIERARCHY_FIELD, id), MUST_NOT));

        return builder.build();

    }

    private Optional<String> getSiteHierarchyNode(String queryText)
    {
        return Optional.of(queryText).map(siteService::getSite).map(SiteInfo::getNodeRef).map(NodeRef::getId);
    }

    /**
     * This parser represents a first-round parser so any special character or reserved word arriving from the user query is escaped (i.e. removed) and the query string is parsed. Since the toString() version of the built query is then passed to Elasticsearch query_string parser we have to make sure those characters are escaped again.
     */
    String escape(String value, boolean excludeWildCards)
    {
        // If value is a reserved word, escape it
        if (ES_RESERVED_WORDS.contains(value))
        {
            return "\\" + value;
        }

        var t1 = escapeSpecialCharacters(value);

        if (!excludeWildCards)
        {
            t1 = t1.replace("*", "\\*")
                    .replace("%", "\\%");
        }

        var t2 = value.startsWith("-") ? "\\-" + t1.substring(1) : t1;
        return t2.startsWith("+") ? "\\+" + t2.substring(1) : t2;
    }

    /* Replace single backslashes with double backslashes if they are not preceeding a special character */
    public String escapeSpecialCharacters(String value)
    {
        return value.replaceAll("(?<!\\\\)(\\\\)(?![\\\\/!\\[\\]{}()^~:\\*%\\?])", "\\\\\\\\")
                .replace("/", "\\/")
                .replace("!", "\\!")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("^", "\\^")
                .replace("~", "\\~")
                .replace(":", "\\:")
                .replace("\"", "\\\"");
    }

    private Query unsupportedWithMessage(String message)
    {
        logIgnoringField(message);
        return null;
    }

    private Query unsupportedField(String name)
    {
        logIgnoringField("The query contains a not supported property \"" + name + "\"");
        return null;
    }

    private void logIgnoringField(String message)
    {
        LOGGER.warn("Ignoring query condition because: {}", message);
    }

    private static class PhraseSlopData implements Closeable
    {
        private static final ThreadLocal<PhraseSlopData> DATA_TL = new ThreadLocal();
        private static final PhraseSlopData EMPTY = new PhraseSlopData();

        private final String phrase;
        private final int slop;

        private PhraseSlopData()
        {
            this.phrase = null;
            this.slop = 0;
        }

        private PhraseSlopData(String phrase, int slop)
        {
            this.phrase = Objects.requireNonNull(phrase, "phrase mustn't be null.");
            if (slop <= 0)
            {
                throw new IllegalArgumentException("slop must be positive.");
            }
            this.slop = slop;
        }

        public static PhraseSlopData set(String phrase, int slop)
        {
            if (DATA_TL.get() != null)
            {
                throw new IllegalStateException("Can't override already set data.");
            }

            final PhraseSlopData data;
            if (phrase == null || slop <= 0)
            {
                data = EMPTY;
            }
            else
            {
                data = new PhraseSlopData(phrase, slop);
            }

            DATA_TL.set(data);
            return data;
        }

        public static PhraseSlopData current()
        {
            return Optional.ofNullable(DATA_TL.get()).orElse(EMPTY);
        }

        @Override
        public void close()
        {
            final PhraseSlopData toRemove = DATA_TL.get();
            if (toRemove != this)
            {
                throw new IllegalStateException("The same instance must be used to remove data.");
            }
            DATA_TL.remove();
        }

        public boolean matches(String phrase)
        {
            return this.phrase != null && this.phrase.equals(phrase);
        }

        public int getSlop()
        {
            return slop;
        }
    }
}
