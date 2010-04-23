/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.search.impl.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.lucene.analysis.DateTimeAnalyser;
import org.alfresco.repo.search.impl.lucene.analysis.MLTokenDuplicator;
import org.alfresco.repo.search.impl.lucene.query.CaseInsensitiveFieldQuery;
import org.alfresco.repo.search.impl.lucene.query.CaseInsensitiveFieldRangeQuery;
import org.alfresco.repo.search.impl.lucene.query.PathQuery;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.CachingDateFormat;
import org.alfresco.util.SearchLanguageConversion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.CharStream;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.QueryParserTokenManager;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardTermEnum;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.regex.RegexQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.saxpath.SAXPathException;
import org.springframework.extensions.surf.util.I18NUtil;

import com.werken.saxpath.XPathReader;

/**
 * Extensions to the standard lucene query parser.
 * <p>
 * Covers:
 * <ul>
 * <li>special fields;
 * <li>range expansion;
 * <li>adds wild card support for phrases;
 * <li>exposes more helper methods to build lucene queries and request tokneisation bahviour.
 * </ul>
 * TODO: Locale loop should not include tokenisation expansion
 * 
 * @author andyh
 */
public class LuceneQueryParser extends QueryParser
{
    private static Log s_logger = LogFactory.getLog(LuceneQueryParser.class);

    private NamespacePrefixResolver namespacePrefixResolver;

    private DictionaryService dictionaryService;

    private TenantService tenantService;

    private SearchParameters searchParameters;

    private LuceneConfig config;

    private IndexReader indexReader;

    private int internalSlop = 0;

    private LuceneAnalyser luceneAnalyser;

    /**
     * Parses a query string, returning a {@link org.apache.lucene.search.Query}.
     * 
     * @param query
     *            the query string to be parsed.
     * @param field
     *            the default field for query terms.
     * @param analyzer
     *            used to find terms in the query text.
     * @param namespacePrefixResolver
     * @param dictionaryService
     * @param tenantService
     * @param defaultOperator
     * @param searchParameters
     * @param config
     * @param indexReader
     * @return - the query
     * @throws ParseException
     *             if the parsing fails
     */
    static public Query parse(String query, String field, Analyzer analyzer, NamespacePrefixResolver namespacePrefixResolver, DictionaryService dictionaryService,
            TenantService tenantService, Operator defaultOperator, SearchParameters searchParameters, LuceneConfig config, IndexReader indexReader) throws ParseException
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Using Alfresco Lucene Query Parser for query: " + query);
        }
        LuceneQueryParser parser = new LuceneQueryParser(field, analyzer);
        parser.setDefaultOperator(defaultOperator);
        parser.setNamespacePrefixResolver(namespacePrefixResolver);
        parser.setDictionaryService(dictionaryService);
        parser.setTenantService(tenantService);
        parser.setSearchParameters(searchParameters);
        parser.setLuceneConfig(config);
        parser.setIndexReader(indexReader);
        parser.setAllowLeadingWildcard(true);
        // TODO: Apply locale contstraints at the top level if required for the non ML doc types.
        Query result = parser.parse(query);
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Query " + query + "                             is\n\t" + result.toString());
        }
        return result;
    }

    /**
     * @param config
     */
    public void setLuceneConfig(LuceneConfig config)
    {
        this.config = config;
    }

    /**
     * @param indexReader
     */
    public void setIndexReader(IndexReader indexReader)
    {
        this.indexReader = indexReader;
    }

    /**
     * @param searchParameters
     */
    public void setSearchParameters(SearchParameters searchParameters)
    {
        this.searchParameters = searchParameters;
    }

    /**
     * @param namespacePrefixResolver
     */
    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    /**
     * @param tenantService
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    public SearchParameters getSearchParameters()
    {
        return searchParameters;
    }

    public IndexReader getIndexReader()
    {
        return indexReader;
    }

    public LuceneConfig getConfig()
    {
        return config;
    }

    /**
     * Lucene default constructor
     * 
     * @param arg0
     * @param arg1
     */
    public LuceneQueryParser(String arg0, Analyzer arg1)
    {
        super(arg0, arg1);
        if (arg1 instanceof LuceneAnalyser)
        {
            luceneAnalyser = (LuceneAnalyser) arg1;
        }
    }

    /**
     * Lucene default constructor
     * 
     * @param arg0
     */
    public LuceneQueryParser(CharStream arg0)
    {
        super(arg0);
    }

    /**
     * Lucene default constructor
     * 
     * @param arg0
     */
    public LuceneQueryParser(QueryParserTokenManager arg0)
    {
        super(arg0);
    }

    protected Query getFieldQuery(String field, String queryText, int slop) throws ParseException
    {
        try
        {
            internalSlop = slop;
            Query query = getFieldQuery(field, queryText);
            return query;
        }
        finally
        {
            internalSlop = 0;
        }

    }

    /**
     * @param field
     * @param queryText
     * @param analysisMode
     * @param slop
     * @param luceneFunction
     * @return the query
     * @throws ParseException
     */
    public Query getFieldQuery(String field, String queryText, AnalysisMode analysisMode, int slop, LuceneFunction luceneFunction) throws ParseException
    {
        try
        {
            internalSlop = slop;
            Query query = getFieldQuery(field, queryText, analysisMode, luceneFunction);
            return query;
        }
        finally
        {
            internalSlop = 0;
        }

    }

    /**
     * @param field
     * @param sqlLikeClause
     * @param analysisMode
     * @return the query
     * @throws ParseException
     */
    public Query getLikeQuery(String field, String sqlLikeClause, AnalysisMode analysisMode) throws ParseException
    {
        String luceneWildCardExpression = SearchLanguageConversion.convert(SearchLanguageConversion.DEF_SQL_LIKE, SearchLanguageConversion.DEF_LUCENE, sqlLikeClause);
        return getWildcardQuery(field, luceneWildCardExpression, AnalysisMode.LIKE);
    }

    /**
     * @param field
     * @param queryText
     * @param analysisMode
     * @param luceneFunction
     * @return the query
     * @throws ParseException
     */
    public Query getDoesNotMatchFieldQuery(String field, String queryText, AnalysisMode analysisMode, LuceneFunction luceneFunction) throws ParseException
    {
        BooleanQuery query = new BooleanQuery();
        Query allQuery = new MatchAllDocsQuery();
        Query matchQuery = getFieldQuery(field, queryText, analysisMode, luceneFunction);
        if ((matchQuery != null))
        {
            query.add(allQuery, Occur.MUST);
            query.add(matchQuery, Occur.MUST_NOT);
        }
        else
        {
            throw new UnsupportedOperationException();
        }
        return query;
    }

    public Query getFieldQuery(String field, String queryText) throws ParseException
    {
        return getFieldQuery(field, queryText, AnalysisMode.DEFAULT, LuceneFunction.FIELD);
    }

    /**
     * @param field
     * @param first
     * @param last
     * @param slop
     * @param inOrder
     * @return the query
     */
    public Query getSpanQuery(String field, String first, String last, int slop, boolean inOrder)
    {
        if (field.equals("TEXT"))
        {
            Set<String> text = searchParameters.getTextAttributes();
            if ((text == null) || (text.size() == 0))
            {
                Collection<QName> contentAttributes = dictionaryService.getAllProperties(DataTypeDefinition.CONTENT);
                BooleanQuery query = new BooleanQuery();
                for (QName qname : contentAttributes)
                {
                    Query part = getSpanQuery("@" + qname.toString(), first, last, slop, inOrder);
                    query.add(part, Occur.SHOULD);
                }
                return query;
            }
            else
            {
                BooleanQuery query = new BooleanQuery();
                for (String fieldName : text)
                {
                    Query part = getSpanQuery(fieldName, first, last, slop, inOrder);
                    query.add(part, Occur.SHOULD);
                }
                return query;
            }
        }
        else if (field.startsWith("@"))
        {
            SpanQuery firstTerm = new SpanTermQuery(new Term(field, first));
            SpanQuery lastTerm = new SpanTermQuery(new Term(field, last));
            return new SpanNearQuery(new SpanQuery[] { firstTerm, lastTerm }, slop, inOrder);
        }
        else if (field.equals("ALL"))
        {
            Set<String> all = searchParameters.getAllAttributes();
            if ((all == null) || (all.size() == 0))
            {
                Collection<QName> contentAttributes = dictionaryService.getAllProperties(null);
                BooleanQuery query = new BooleanQuery();
                for (QName qname : contentAttributes)
                {
                    Query part = getSpanQuery("@" + qname.toString(), first, last, slop, inOrder);
                    query.add(part, Occur.SHOULD);
                }
                return query;
            }
            else
            {
                BooleanQuery query = new BooleanQuery();
                for (String fieldName : all)
                {
                    Query part = getSpanQuery(fieldName, first, last, slop, inOrder);
                    query.add(part, Occur.SHOULD);
                }
                return query;
            }

        }
        else if (matchDataTypeDefinition(field) != null)
        {
            Collection<QName> contentAttributes = dictionaryService.getAllProperties(matchDataTypeDefinition(field).getName());
            BooleanQuery query = new BooleanQuery();
            for (QName qname : contentAttributes)
            {
                Query part = getSpanQuery("@" + qname.toString(), first, last, slop, inOrder);
                query.add(part, Occur.SHOULD);
            }
            return query;
        }
        else
        {
            SpanQuery firstTerm = new SpanTermQuery(new Term(field, first));
            SpanQuery lastTerm = new SpanTermQuery(new Term(field, last));
            return new SpanNearQuery(new SpanQuery[] { firstTerm, lastTerm }, slop, inOrder);
        }

    }

    private DataTypeDefinition matchDataTypeDefinition(String string)
    {
        QName search = QName.createQName(expandQName(string));
        DataTypeDefinition dataTypeDefinition = dictionaryService.getDataType(QName.createQName(expandQName(string)));
        QName match = null;
        if(dataTypeDefinition == null)
        {
            for(QName definition : dictionaryService.getAllDataTypes())
            {
                if(definition.getNamespaceURI().equalsIgnoreCase(search.getNamespaceURI()))
                {
                    if(definition.getLocalName().equalsIgnoreCase(search.getLocalName()))
                    {
                        if(match == null)
                        {
                            match = definition;
                        }
                        else
                        {
                            throw new LuceneIndexException("Ambiguous data datype "+string);
                        }
                    }
                }
                        
            }
        }
        else
        {
            return dataTypeDefinition;
        }
        if(match == null)
        {
            return null;
        }
        else
        {
            return dictionaryService.getDataType(match);
        }
    }
    
    private PropertyDefinition matchPropertyDefinition(String string)
    {
        QName search = QName.createQName(expandQName(string));
        PropertyDefinition propertyDefinition = dictionaryService.getProperty(QName.createQName(expandQName(string)));
        QName match = null;
        if(propertyDefinition == null)
        {
            for(QName definition : dictionaryService.getAllProperties(null))
            {
                if(definition.getNamespaceURI().equalsIgnoreCase(search.getNamespaceURI()))
                {
                    if(definition.getLocalName().equalsIgnoreCase(search.getLocalName()))
                    {
                        if(match == null)
                        {
                            match = definition;
                        }
                        else
                        {
                            throw new LuceneIndexException("Ambiguous data datype "+string);
                        }
                    }
                }
                        
            }
        }
        else
        {
            return propertyDefinition;
        }
        if(match == null)
        {
            return null;
        }
        else
        {
            return dictionaryService.getProperty(match);
        }
    }
    
    private AspectDefinition matchAspectDefinition(String string)
    {
        QName search = QName.createQName(expandQName(string));
        AspectDefinition aspectDefinition = dictionaryService.getAspect(QName.createQName(expandQName(string)));
        QName match = null;
        if(aspectDefinition == null)
        {
            for(QName definition : dictionaryService.getAllAspects())
            {
                if(definition.getNamespaceURI().equalsIgnoreCase(search.getNamespaceURI()))
                {
                    if(definition.getLocalName().equalsIgnoreCase(search.getLocalName()))
                    {
                        if(match == null)
                        {
                            match = definition;
                        }
                        else
                        {
                            throw new LuceneIndexException("Ambiguous data datype "+string);
                        }
                    }
                }
                        
            }
        }
        else
        {
            return aspectDefinition;
        }
        if(match == null)
        {
            return null;
        }
        else
        {
            return dictionaryService.getAspect(match);
        }
    }
    
    private TypeDefinition matchTypeDefinition(String string)
    {
        QName search = QName.createQName(expandQName(string));
        TypeDefinition typeDefinition = dictionaryService.getType(QName.createQName(expandQName(string)));
        QName match = null;
        if(typeDefinition == null)
        {
            for(QName definition : dictionaryService.getAllTypes())
            {
                if(definition.getNamespaceURI().equalsIgnoreCase(search.getNamespaceURI()))
                {
                    if(definition.getLocalName().equalsIgnoreCase(search.getLocalName()))
                    {
                        if(match == null)
                        {
                            match = definition;
                        }
                        else
                        {
                            throw new LuceneIndexException("Ambiguous data datype "+string);
                        }
                    }
                }
                        
            }
        }
        else
        {
            return typeDefinition;
        }
        if(match == null)
        {
            return null;
        }
        else
        {
            return dictionaryService.getType(match);
        }
    }
    
    private ClassDefinition matchClassDefinition(String string)
    {
        TypeDefinition match = matchTypeDefinition(string);
        if(match != null)
        {
            return match;
        }
        else
        {
            return matchAspectDefinition(string);
        }
    }
    
   
    
    /**
     * @param field
     * @param queryText
     * @param analysisMode
     * @param luceneFunction
     * @return the query
     * @throws ParseException
     */
    public Query getFieldQuery(String field, String queryText, AnalysisMode analysisMode, LuceneFunction luceneFunction) throws ParseException
    {
        try
        {
            if (field.equals("PATH"))
            {
                XPathReader reader = new XPathReader();
                LuceneXPathHandler handler = new LuceneXPathHandler();
                handler.setNamespacePrefixResolver(namespacePrefixResolver);
                handler.setDictionaryService(dictionaryService);
                reader.setXPathHandler(handler);
                reader.parse(queryText);
                PathQuery pathQuery = handler.getQuery();
                pathQuery.setRepeats(false);
                return pathQuery;
            }
            else if (field.equals("PATH_WITH_REPEATS"))
            {
                XPathReader reader = new XPathReader();
                LuceneXPathHandler handler = new LuceneXPathHandler();
                handler.setNamespacePrefixResolver(namespacePrefixResolver);
                handler.setDictionaryService(dictionaryService);
                reader.setXPathHandler(handler);
                reader.parse(queryText);
                PathQuery pathQuery = handler.getQuery();
                pathQuery.setRepeats(true);
                return pathQuery;
            }
            else if (field.equals("TEXT"))
            {
                Set<String> text = searchParameters.getTextAttributes();
                if ((text == null) || (text.size() == 0))
                {
                    Collection<QName> contentAttributes = dictionaryService.getAllProperties(DataTypeDefinition.CONTENT);
                    BooleanQuery query = new BooleanQuery();
                    for (QName qname : contentAttributes)
                    {
                        // The super implementation will create phrase queries etc if required
                        Query part = getFieldQuery("@" + qname.toString(), queryText, analysisMode, luceneFunction);
                        if (part != null)
                        {
                            query.add(part, Occur.SHOULD);
                        }
                        else
                        {
                            query.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
                        }
                    }
                    return query;
                }
                else
                {
                    BooleanQuery query = new BooleanQuery();
                    for (String fieldName : text)
                    {
                        Query part = getFieldQuery(fieldName, queryText, analysisMode, luceneFunction);
                        if (part != null)
                        {
                            query.add(part, Occur.SHOULD);
                        }
                        else
                        {
                            query.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
                        }
                    }
                    return query;
                }

            }
            else if (field.equals("ID"))
            {
                if (tenantService.isTenantUser() && (queryText.contains(StoreRef.URI_FILLER)))
                {
                    // assume NodeRef, since it contains StorRef URI filler
                    queryText = tenantService.getName(new NodeRef(queryText)).toString();
                }
                TermQuery termQuery = new TermQuery(new Term(field, queryText));
                return termQuery;
            }
            else if (field.equals("ISROOT"))
            {
                TermQuery termQuery = new TermQuery(new Term(field, queryText));
                return termQuery;
            }
            else if (field.equals("ISCONTAINER"))
            {
                TermQuery termQuery = new TermQuery(new Term(field, queryText));
                return termQuery;
            }
            else if (field.equals("ISNODE"))
            {
                TermQuery termQuery = new TermQuery(new Term(field, queryText));
                return termQuery;
            }
            else if (field.equals("TX"))
            {
                TermQuery termQuery = new TermQuery(new Term(field, queryText));
                return termQuery;
            }
            else if (field.equals("PARENT"))
            {
                if (tenantService.isTenantUser() && (queryText.contains(StoreRef.URI_FILLER)))
                {
                    // assume NodeRef, since it contains StoreRef URI filler
                    queryText = tenantService.getName(new NodeRef(queryText)).toString();
                }
                TermQuery termQuery = new TermQuery(new Term(field, queryText));
                return termQuery;
            }
            else if (field.equals("PRIMARYPARENT"))
            {
                if (tenantService.isTenantUser() && (queryText.contains(StoreRef.URI_FILLER)))
                {
                    // assume NodeRef, since it contains StoreRef URI filler
                    queryText = tenantService.getName(new NodeRef(queryText)).toString();
                }
                TermQuery termQuery = new TermQuery(new Term(field, queryText));
                return termQuery;
            }
            else if (field.equals("QNAME"))
            {
                XPathReader reader = new XPathReader();
                LuceneXPathHandler handler = new LuceneXPathHandler();
                handler.setNamespacePrefixResolver(namespacePrefixResolver);
                handler.setDictionaryService(dictionaryService);
                reader.setXPathHandler(handler);
                reader.parse("//" + queryText);
                return handler.getQuery();
            }
            else if (field.equals("PRIMARYASSOCTYPEQNAME"))
            {
                XPathReader reader = new XPathReader();
                LuceneXPathHandler handler = new LuceneXPathHandler();
                handler.setNamespacePrefixResolver(namespacePrefixResolver);
                handler.setDictionaryService(dictionaryService);
                reader.setXPathHandler(handler);
                reader.parse("//" + queryText);
                PathQuery query = handler.getQuery();
                query.setPathField("PATH");
                query.setQnameField("PRIMARYASSOCTYPEQNAME");
                return query;
            }
            else if (field.equals("ASSOCTYPEQNAME"))
            {
                XPathReader reader = new XPathReader();
                LuceneXPathHandler handler = new LuceneXPathHandler();
                handler.setNamespacePrefixResolver(namespacePrefixResolver);
                handler.setDictionaryService(dictionaryService);
                reader.setXPathHandler(handler);
                reader.parse("//" + queryText);
                PathQuery query = handler.getQuery();
                query.setPathField("PATH");
                query.setQnameField("PRIMARYASSOCTYPEQNAME");
                return query;
            }
            else if (field.equals("CLASS"))
            {
                ClassDefinition target = matchClassDefinition(queryText);
                if (target == null)
                {
                    throw new SearcherException("Invalid type: " + queryText);
                }
                return getFieldQuery(target.isAspect() ? "ASPECT" : "TYPE", queryText, analysisMode, luceneFunction);
            }
            else if (field.equals("TYPE"))
            {
                TypeDefinition target = matchTypeDefinition(queryText);
                if (target == null)
                {
                    throw new SearcherException("Invalid type: " + queryText);
                }
                Collection<QName> subclasses = dictionaryService.getSubTypes(target.getName(), true);
                BooleanQuery booleanQuery = new BooleanQuery();
                for (QName qname : subclasses)
                {
                    TypeDefinition current = dictionaryService.getType(qname);
                    if (target.getName().equals(current.getName()) || current.getIncludedInSuperTypeQuery())
                    {
                        TermQuery termQuery = new TermQuery(new Term(field, qname.toString()));
                        if (termQuery != null)
                        {
                            booleanQuery.add(termQuery, Occur.SHOULD);
                        }
                    }
                }
                return booleanQuery;
            }
            else if (field.equals("EXACTTYPE"))
            {
                TypeDefinition target = matchTypeDefinition(queryText);
                if (target == null)
                {
                    throw new SearcherException("Invalid type: " + queryText);
                }
                QName targetQName = target.getName();
                TermQuery termQuery = new TermQuery(new Term("TYPE", targetQName.toString()));
                return termQuery;

            }
            else if (field.equals("ASPECT"))
            {
                AspectDefinition target = matchAspectDefinition(queryText);
                if (target == null)
                {
                    // failed to find the aspect in the dictionary
                    throw new AlfrescoRuntimeException("Unknown aspect specified in query: " + queryText);
                }

                Collection<QName> subclasses = dictionaryService.getSubAspects(target.getName(), true);

                BooleanQuery booleanQuery = new BooleanQuery();
                for (QName qname : subclasses)
                {
                    AspectDefinition current = dictionaryService.getAspect(qname);
                    if (target.getName().equals(current.getName()) || current.getIncludedInSuperTypeQuery())
                    {
                        TermQuery termQuery = new TermQuery(new Term(field, qname.toString()));
                        if (termQuery != null)
                        {
                            booleanQuery.add(termQuery, Occur.SHOULD);
                        }
                    }
                }
                return booleanQuery;
            }
            else if (field.equals("EXACTASPECT"))
            {
                AspectDefinition target = matchAspectDefinition(queryText);
                if (target == null)
                {
                    // failed to find the aspect in the dictionary
                    throw new AlfrescoRuntimeException("Unknown aspect specified in query: " + queryText);
                }

                QName targetQName = target.getName();
                TermQuery termQuery = new TermQuery(new Term("ASPECT", targetQName.toString()));

                return termQuery;
            }
            else if (field.startsWith("@"))
            {
                Query query = attributeQueryBuilder(field, queryText, new FieldQuery(), analysisMode, luceneFunction);
                return query;
            }
            else if (field.equals("ALL"))
            {
                Set<String> all = searchParameters.getAllAttributes();
                if ((all == null) || (all.size() == 0))
                {
                    Collection<QName> contentAttributes = dictionaryService.getAllProperties(null);
                    BooleanQuery query = new BooleanQuery();
                    for (QName qname : contentAttributes)
                    {
                        // The super implementation will create phrase queries etc if required
                        Query part = getFieldQuery("@" + qname.toString(), queryText, analysisMode, luceneFunction);
                        if (part != null)
                        {
                            query.add(part, Occur.SHOULD);
                        }
                        else
                        {
                            query.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
                        }
                    }
                    return query;
                }
                else
                {
                    BooleanQuery query = new BooleanQuery();
                    for (String fieldName : all)
                    {
                        Query part = getFieldQuery(fieldName, queryText, analysisMode, luceneFunction);
                        if (part != null)
                        {
                            query.add(part, Occur.SHOULD);
                        }
                        else
                        {
                            query.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
                        }
                    }
                    return query;
                }

            }
            else if (field.equals("ISUNSET"))
            {
                PropertyDefinition pd = matchPropertyDefinition(queryText);
                if (pd != null)
                {
                    ClassDefinition containerClass = pd.getContainerClass();
                    QName container = containerClass.getName();
                    BooleanQuery query = new BooleanQuery();
                    String classType = containerClass.isAspect() ? "ASPECT" : "TYPE";
                    Query typeQuery = getFieldQuery(classType, container.toString(), analysisMode, luceneFunction);
                    Query presenceQuery = getWildcardQuery("@" + pd.getName().toString(), "*");
                    if ((typeQuery != null) && (presenceQuery != null))
                    {
                        query.add(typeQuery, Occur.MUST);
                        query.add(presenceQuery, Occur.MUST_NOT);
                    }
                    return query;
                }
                else
                {
                    return getFieldQueryImpl(field, queryText, analysisMode, luceneFunction);
                }

            }
            else if (field.equals("ISNULL"))
            {
                PropertyDefinition pd = matchPropertyDefinition(queryText);
                if (pd != null)
                {
                    BooleanQuery query = new BooleanQuery();
                    Query presenceQuery = getWildcardQuery("@" + pd.getName().toString(), "*");
                    if (presenceQuery != null)
                    {
                        query.add(new MatchAllDocsQuery(), Occur.MUST);
                        query.add(presenceQuery, Occur.MUST_NOT);
                    }
                    return query;
                }
                else
                {
                    return getFieldQueryImpl(field, queryText, analysisMode, luceneFunction);
                }

            }
            else if (field.equals("ISNOTNULL"))
            {
                PropertyDefinition pd = matchPropertyDefinition(queryText);
                if (pd != null)
                {
                    ClassDefinition containerClass = pd.getContainerClass();
                    QName container = containerClass.getName();
                    BooleanQuery query = new BooleanQuery();
                    String classType = containerClass.isAspect() ? "ASPECT" : "TYPE";
                    Query typeQuery = getFieldQuery(classType, container.toString(), analysisMode, luceneFunction);
                    Query presenceQuery = getWildcardQuery("@" + pd.getName().toString(), "*");
                    if ((typeQuery != null) && (presenceQuery != null))
                    {
                        // query.add(typeQuery, Occur.MUST);
                        query.add(presenceQuery, Occur.MUST);
                    }
                    return query;
                }
                else
                {
                    return getFieldQueryImpl(field, queryText, analysisMode, luceneFunction);
                }

            }
            else if (matchDataTypeDefinition(field) != null)
            {
                Collection<QName> contentAttributes = dictionaryService.getAllProperties(matchDataTypeDefinition(field).getName());
                BooleanQuery query = new BooleanQuery();
                for (QName qname : contentAttributes)
                {
                    // The super implementation will create phrase queries etc if required
                    Query part = getFieldQuery("@" + qname.toString(), queryText, analysisMode, luceneFunction);
                    if (part != null)
                    {
                        query.add(part, Occur.SHOULD);
                    }
                    else
                    {
                        query.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
                    }
                }
                return query;
            }
            else if (field.equals("FTSSTATUS"))
            {
                TermQuery termQuery = new TermQuery(new Term(field, queryText));
                return termQuery;
            }
            else
            {
                return getFieldQueryImpl(field, queryText, analysisMode, luceneFunction);
            }

        }
        catch (SAXPathException e)
        {
            throw new ParseException("Failed to parse XPath...\n" + e.getMessage());
        }

    }

    private Query getFieldQueryImpl(String field, String queryText, AnalysisMode analysisMode, LuceneFunction luceneFunction) throws ParseException
    {
        // Use the analyzer to get all the tokens, and then build a TermQuery,
        // PhraseQuery, or noth

        // TODO: Untokenised columns with functions require special handling

        if (luceneFunction != LuceneFunction.FIELD)
        {
            throw new UnsupportedOperationException("Field queries are not supported on lucene functions (UPPER, LOWER, etc)");
        }

        boolean requiresMLTokenDuplication = false;
        String testText = queryText;
        String localeString = null;
        if (field.startsWith("@"))
        {
            if ((queryText.length() > 0) && (queryText.charAt(0) == '\u0000'))
            {
                int position = queryText.indexOf("\u0000", 1);
                testText = queryText.substring(position + 1);
                requiresMLTokenDuplication = true;
                localeString = queryText.substring(1, position);
            }
        }

        TokenStream source = getAnalyzer().tokenStream(field, new StringReader(queryText), analysisMode);

        ArrayList<org.apache.lucene.analysis.Token> list = new ArrayList<org.apache.lucene.analysis.Token>();
        org.apache.lucene.analysis.Token reusableToken = new org.apache.lucene.analysis.Token();
        org.apache.lucene.analysis.Token nextToken;
        int positionCount = 0;
        boolean severalTokensAtSamePosition = false;

        while (true)
        {
            try
            {
                nextToken = source.next(reusableToken);
            }
            catch (IOException e)
            {
                nextToken = null;
            }
            if (nextToken == null)
                break;
            list.add((org.apache.lucene.analysis.Token) nextToken.clone());
            if (nextToken.getPositionIncrement() != 0)
                positionCount += nextToken.getPositionIncrement();
            else
                severalTokensAtSamePosition = true;
        }
        try
        {
            source.close();
        }
        catch (IOException e)
        {
            // ignore
        }

        // add any alpha numeric wildcards that have been missed
        // Fixes most stop word and wild card issues

        for (int index = 0; index < testText.length(); index++)
        {
            char current = testText.charAt(index);
            if ((current == '*') || (current == '?'))
            {
                StringBuilder pre = new StringBuilder(10);
                if (index > 0)
                {
                    for (int i = index - 1; i >= 0; i--)
                    {
                        char c = testText.charAt(i);
                        if (Character.isLetterOrDigit(c))
                        {
                            boolean found = false;
                            for (int j = 0; j < list.size(); j++)
                            {
                                org.apache.lucene.analysis.Token test = list.get(j);
                                if ((test.startOffset() <= i) && (i <= test.endOffset()))
                                {
                                    found = true;
                                    break;
                                }
                            }
                            if (found)
                            {
                                break;
                            }
                            else
                            {
                                pre.insert(0, c);
                            }
                        }
                    }
                    if (pre.length() > 0)
                    {
                        // Add new token followed by * not given by the tokeniser
                        org.apache.lucene.analysis.Token newToken = new org.apache.lucene.analysis.Token(index - pre.length(), index);
                        newToken.setTermBuffer(pre.toString());
                        newToken.setType("ALPHANUM");
                        if (requiresMLTokenDuplication)
                        {
                            Locale locale = I18NUtil.parseLocale(localeString);
                            MLAnalysisMode mlAnalysisMode = searchParameters.getMlAnalaysisMode() == null ? config.getDefaultMLSearchAnalysisMode() : searchParameters
                                    .getMlAnalaysisMode();
                            MLTokenDuplicator duplicator = new MLTokenDuplicator(locale, mlAnalysisMode);
                            Iterator<org.apache.lucene.analysis.Token> it = duplicator.buildIterator(newToken);
                            if (it != null)
                            {
                                int count = 0;
                                while (it.hasNext())
                                {
                                    list.add(it.next());
                                    count++;
                                    if (count > 1)
                                    {
                                        severalTokensAtSamePosition = true;
                                    }
                                }
                            }
                        }
                        // content
                        else
                        {
                            list.add(newToken);
                        }
                    }
                }

                StringBuilder post = new StringBuilder(10);
                if (index > 0)
                {
                    for (int i = index + 1; i < testText.length(); i++)
                    {
                        char c = testText.charAt(i);
                        if (Character.isLetterOrDigit(c))
                        {
                            boolean found = false;
                            for (int j = 0; j < list.size(); j++)
                            {
                                org.apache.lucene.analysis.Token test = list.get(j);
                                if ((test.startOffset() <= i) && (i <= test.endOffset()))
                                {
                                    found = true;
                                    break;
                                }
                            }
                            if (found)
                            {
                                break;
                            }
                            else
                            {
                                post.append(c);
                            }
                        }
                    }
                    if (post.length() > 0)
                    {
                        // Add new token followed by * not given by the tokeniser
                        org.apache.lucene.analysis.Token newToken = new org.apache.lucene.analysis.Token(index + 1, index + 1 + post.length());
                        newToken.setTermBuffer(post.toString());
                        newToken.setType("ALPHANUM");
                        if (requiresMLTokenDuplication)
                        {
                            Locale locale = I18NUtil.parseLocale(localeString);
                            MLAnalysisMode mlAnalysisMode = searchParameters.getMlAnalaysisMode() == null ? config.getDefaultMLSearchAnalysisMode() : searchParameters
                                    .getMlAnalaysisMode();
                            MLTokenDuplicator duplicator = new MLTokenDuplicator(locale, mlAnalysisMode);
                            Iterator<org.apache.lucene.analysis.Token> it = duplicator.buildIterator(newToken);
                            if (it != null)
                            {
                                int count = 0;
                                while (it.hasNext())
                                {
                                    list.add(it.next());
                                    count++;
                                    if (count > 1)
                                    {
                                        severalTokensAtSamePosition = true;
                                    }
                                }
                            }
                        }
                        // content
                        else
                        {
                            list.add(newToken);
                        }
                    }
                }

            }
        }

        Collections.sort(list, new Comparator<org.apache.lucene.analysis.Token>()
        {

            public int compare(Token o1, Token o2)
            {
                int dif = o1.startOffset() - o2.startOffset();
                if (dif != 0)
                {
                    return dif;
                }
                else
                {
                    return o2.getPositionIncrement() - o1.getPositionIncrement();
                }
            }
        });

        // Combined * and ? based strings - should redo the tokeniser

        // Assume we only string together tokens for the same position

        int max = 0;
        int current = 0;
        for (org.apache.lucene.analysis.Token c : list)
        {
            if (c.getPositionIncrement() == 0)
            {
                current++;
            }
            else
            {
                if (current > max)
                {
                    max = current;
                }
                current = 0;
            }
        }
        if (current > max)
        {
            max = current;
        }

        ArrayList<org.apache.lucene.analysis.Token> fixed = new ArrayList<org.apache.lucene.analysis.Token>();
        for (int repeat = 0; repeat <= max; repeat++)
        {
            org.apache.lucene.analysis.Token replace = null;
            current = 0;
            for (org.apache.lucene.analysis.Token c : list)
            {
                if (c.getPositionIncrement() == 0)
                {
                    current++;
                }
                else
                {
                    current = 0;
                }

                if (current == repeat)
                {

                    if (replace == null)
                    {
                        StringBuilder prefix = new StringBuilder();
                        for (int i = c.startOffset() - 1; i >= 0; i--)
                        {
                            char test = testText.charAt(i);
                            if ((test == '*') || (test == '?'))
                            {
                                prefix.insert(0, test);
                            }
                            else
                            {
                                break;
                            }
                        }
                        String pre = prefix.toString();
                        if (requiresMLTokenDuplication)
                        {
                            String termText = new String(c.termBuffer(), 0, c.termLength());
                            int position = termText.indexOf("}");
                            String language = termText.substring(0, position + 1);
                            String token = termText.substring(position + 1);
                            replace = new org.apache.lucene.analysis.Token(c.startOffset() - pre.length(), c.endOffset());
                            replace.setTermBuffer(language + pre + token);
                            replace.setType(c.type());
                            replace.setPositionIncrement(c.getPositionIncrement());
                        }
                        else
                        {
                            String termText = new String(c.termBuffer(), 0, c.termLength());
                            replace = new org.apache.lucene.analysis.Token(c.startOffset() - pre.length(), c.endOffset());
                            replace.setTermBuffer(pre + termText);
                            replace.setType(c.type());
                            replace.setPositionIncrement(c.getPositionIncrement());
                        }
                    }
                    else
                    {
                        StringBuilder prefix = new StringBuilder();
                        StringBuilder postfix = new StringBuilder();
                        StringBuilder builder = prefix;
                        for (int i = c.startOffset() - 1; i >= replace.endOffset(); i--)
                        {
                            char test = testText.charAt(i);
                            if ((test == '*') || (test == '?'))
                            {
                                builder.insert(0, test);
                            }
                            else
                            {
                                builder = postfix;
                                postfix.setLength(0);
                            }
                        }
                        String pre = prefix.toString();
                        String post = postfix.toString();

                        // Does it bridge?
                        if ((pre.length() > 0) && (replace.endOffset() + pre.length()) == c.startOffset())
                        {
                            String termText = new String(c.termBuffer(), 0, c.termLength());
                            if (requiresMLTokenDuplication)
                            {
                                int position = termText.indexOf("}");
                                @SuppressWarnings("unused")
                                String language = termText.substring(0, position + 1);
                                String token = termText.substring(position + 1);
                                int oldPositionIncrement = replace.getPositionIncrement();
                                String replaceTermText = new String(replace.termBuffer(), 0, replace.termLength());
                                replace = new org.apache.lucene.analysis.Token(replace.startOffset(), c.endOffset());
                                replace.setTermBuffer(replaceTermText + pre + token);
                                replace.setType(replace.type());
                                replace.setPositionIncrement(oldPositionIncrement);
                            }
                            else
                            {
                                int oldPositionIncrement = replace.getPositionIncrement();
                                String replaceTermText = new String(replace.termBuffer(), 0, replace.termLength());
                                replace = new org.apache.lucene.analysis.Token(replace.startOffset(), c.endOffset());
                                replace.setTermBuffer(replaceTermText + pre + termText);
                                replace.setType(replace.type());
                                replace.setPositionIncrement(oldPositionIncrement);
                            }
                        }
                        else
                        {
                            String termText = new String(c.termBuffer(), 0, c.termLength());
                            if (requiresMLTokenDuplication)
                            {
                                int position = termText.indexOf("}");
                                String language = termText.substring(0, position + 1);
                                String token = termText.substring(position + 1);
                                String replaceTermText = new String(replace.termBuffer(), 0, replace.termLength());
                                org.apache.lucene.analysis.Token last = new org.apache.lucene.analysis.Token(replace.startOffset(), replace.endOffset() + post.length());
                                last.setTermBuffer(replaceTermText + post);
                                last.setType(replace.type());
                                last.setPositionIncrement(replace.getPositionIncrement());
                                fixed.add(last);
                                replace = new org.apache.lucene.analysis.Token(c.startOffset() - pre.length(), c.endOffset());
                                replace.setTermBuffer(language + pre + token);
                                replace.setType(c.type());
                                replace.setPositionIncrement(c.getPositionIncrement());
                            }
                            else
                            {
                                String replaceTermText = new String(replace.termBuffer(), 0, replace.termLength());
                                org.apache.lucene.analysis.Token last = new org.apache.lucene.analysis.Token(replace.startOffset(), replace.endOffset() + post.length());
                                last.setTermBuffer(replaceTermText + post);
                                last.setType(replace.type());
                                last.setPositionIncrement(replace.getPositionIncrement());
                                fixed.add(last);
                                replace = new org.apache.lucene.analysis.Token(c.startOffset() - pre.length(), c.endOffset());
                                replace.setTermBuffer(pre + termText);
                                replace.setType(c.type());
                                replace.setPositionIncrement(c.getPositionIncrement());
                            }
                        }
                    }
                }
            }
            // finish last
            if (replace != null)
            {
                StringBuilder postfix = new StringBuilder();
                if ((replace.endOffset() >= 0) && (replace.endOffset() < testText.length()))
                {
                    for (int i = replace.endOffset(); i < testText.length(); i++)
                    {
                        char test = testText.charAt(i);
                        if ((test == '*') || (test == '?'))
                        {
                            postfix.append(test);
                        }
                        else
                        {
                            break;
                        }
                    }
                }
                String post = postfix.toString();
                int oldPositionIncrement = replace.getPositionIncrement();
                String replaceTermText = new String(replace.termBuffer(), 0, replace.termLength());
                replace = new org.apache.lucene.analysis.Token(replace.startOffset(), replace.endOffset() + post.length());
                replace.setTermBuffer(replaceTermText + post);
                replace.setType(replace.type());
                replace.setPositionIncrement(oldPositionIncrement);
                fixed.add(replace);

            }
        }

        // Add in any missing words containsing * and ?

        // reorder by start position and increment

        Collections.sort(fixed, new Comparator<org.apache.lucene.analysis.Token>()
        {

            public int compare(Token o1, Token o2)
            {
                int dif = o1.startOffset() - o2.startOffset();
                if (dif != 0)
                {
                    return dif;
                }
                else
                {
                    return o2.getPositionIncrement() - o1.getPositionIncrement();
                }
            }
        });

        list = fixed;

        if (list.size() == 0)
            return null;
        else if (list.size() == 1)
        {
            nextToken = (org.apache.lucene.analysis.Token) list.get(0);
            String termText = new String(nextToken.termBuffer(), 0, nextToken.termLength());
            if (termText.contains("*") || termText.contains("?"))
            {
                return newWildcardQuery(new Term(field, termText));
            }
            else
            {
                return newTermQuery(new Term(field, termText));
            }
        }
        else
        {
            if (severalTokensAtSamePosition)
            {
                if (positionCount == 1)
                {
                    // no phrase query:
                    BooleanQuery q = newBooleanQuery(true);
                    for (int i = 0; i < list.size(); i++)
                    {
                        Query currentQuery;
                        nextToken = (org.apache.lucene.analysis.Token) list.get(i);
                        String termText = new String(nextToken.termBuffer(), 0, nextToken.termLength());
                        if (termText.contains("*") || termText.contains("?"))
                        {
                            currentQuery = newWildcardQuery(new Term(field, termText));
                        }
                        else
                        {
                            currentQuery = newTermQuery(new Term(field, termText));
                        }
                        q.add(currentQuery, BooleanClause.Occur.SHOULD);
                    }
                    return q;
                }
                else
                {
                    // phrase query:
                    MultiPhraseQuery mpq = newMultiPhraseQuery();
                    mpq.setSlop(internalSlop);
                    ArrayList<Term> multiTerms = new ArrayList<Term>();
                    int position = -1;
                    for (int i = 0; i < list.size(); i++)
                    {
                        nextToken = (org.apache.lucene.analysis.Token) list.get(i);
                        String termText = new String(nextToken.termBuffer(), 0, nextToken.termLength());
                        if (nextToken.getPositionIncrement() > 0 && multiTerms.size() > 0)
                        {
                            if (getEnablePositionIncrements())
                            {
                                mpq.add((Term[]) multiTerms.toArray(new Term[0]), position);
                            }
                            else
                            {
                                mpq.add((Term[]) multiTerms.toArray(new Term[0]));
                            }
                            multiTerms.clear();
                        }
                        position += nextToken.getPositionIncrement();

                        Term term = new Term(field, termText);
                        if ((termText != null) && (termText.contains("*") || termText.contains("?")))
                        {
                            addWildcardTerms(multiTerms, term);
                        }
                        else
                        {
                            multiTerms.add(term);
                        }
                    }
                    if (getEnablePositionIncrements())
                    {
                        if (multiTerms.size() > 0)
                        {
                            mpq.add((Term[]) multiTerms.toArray(new Term[0]), position);
                        }
                        else
                        {
                            mpq.add(new Term[] { new Term(field, "\u0000") }, position);
                        }
                    }
                    else
                    {
                        if (multiTerms.size() > 0)
                        {
                            mpq.add((Term[]) multiTerms.toArray(new Term[0]));
                        }
                        else
                        {
                            mpq.add(new Term[] { new Term(field, "\u0000") });
                        }
                    }
                    return mpq;
                }
            }
            else
            {
                MultiPhraseQuery q = new MultiPhraseQuery();
                q.setSlop(internalSlop);
                int position = -1;
                for (int i = 0; i < list.size(); i++)
                {
                    nextToken = (org.apache.lucene.analysis.Token) list.get(i);
                    String termText = new String(nextToken.termBuffer(), 0, nextToken.termLength());
                    Term term = new Term(field, termText);
                    if (getEnablePositionIncrements())
                    {
                        position += nextToken.getPositionIncrement();
                        if ((termText != null) && (termText.contains("*") || termText.contains("?")))
                        {
                            q.add(getMatchingTerms(field, term), position);
                        }
                        else
                        {
                            q.add(new Term[] { term }, position);
                        }
                    }
                    else
                    {
                        if ((termText != null) && (termText.contains("*") || termText.contains("?")))
                        {
                            q.add(getMatchingTerms(field, term));
                        }
                        else
                        {
                            q.add(term);
                        }
                    }
                }
                return q;
            }
        }
    }

    private Term[] getMatchingTerms(String field, Term term) throws ParseException
    {
        ArrayList<Term> terms = new ArrayList<Term>();
        addWildcardTerms(terms, term);
        if (terms.size() == 0)
        {
            return new Term[] { new Term(field, "\u0000") };
        }
        else
        {
            return terms.toArray(new Term[0]);
        }

    }

    private void addWildcardTerms(ArrayList<Term> terms, Term term) throws ParseException
    {
        try
        {
            WildcardTermEnum wcte = new WildcardTermEnum(indexReader, term);

            while (!wcte.endEnum())
            {
                Term current = wcte.term();
                if ((current.text() != null) && (current.text().length() > 0) && (current.text().charAt(0) == '{'))
                {
                    if ((term != null) && (term.text().length() > 0) && (term.text().charAt(0) == '{'))
                    {
                        terms.add(current);
                    }
                    // If not, we cod not add so wildcards do not match the locale prefix
                }
                else
                {
                    terms.add(current);
                }

                wcte.next();
            }
        }
        catch (IOException e)
        {
            throw new ParseException("IO error generating phares wildcards " + e.getMessage());
        }
    }

    /**
     * @exception ParseException
     *                throw in overridden method to disallow
     */
    protected Query getRangeQuery(String field, String part1, String part2, boolean inclusive) throws ParseException
    {
        return getRangeQuery(field, part1, part2, inclusive, inclusive, AnalysisMode.DEFAULT, LuceneFunction.FIELD);
    }

    /**
     * @param field
     * @param part1
     * @param part2
     * @param includeLower
     * @param includeUpper
     * @param analysisMode
     * @param luceneFunction
     * @return the query
     * @exception ParseException
     *                throw in overridden method to disallow
     */
    public Query getRangeQuery(String field, String part1, String part2, boolean includeLower, boolean includeUpper, AnalysisMode analysisMode, LuceneFunction luceneFunction)
            throws ParseException
    {

        if (field.startsWith("@"))
        {
            String fieldName;
            PropertyDefinition propertyDef = matchPropertyDefinition(field.substring(1));
            if(propertyDef != null)
            {
                fieldName = "@" + propertyDef.getName();
            }
            else
            {
                fieldName = expandAttributeFieldNamex(field);
            }
            
            IndexTokenisationMode tokenisationMode = IndexTokenisationMode.TRUE;
            if (propertyDef != null)
            {
                tokenisationMode = propertyDef.getIndexTokenisationMode();
                if (tokenisationMode == null)
                {
                    tokenisationMode = IndexTokenisationMode.TRUE;
                }
            }

            if (propertyDef != null)
            {
                if (luceneFunction != LuceneFunction.FIELD)
                {
                    if (propertyDef.getDataType().getName().equals(DataTypeDefinition.TEXT))
                    {
                        BooleanQuery booleanQuery = new BooleanQuery();
                        MLAnalysisMode mlAnalysisMode = searchParameters.getMlAnalaysisMode() == null ? config.getDefaultMLSearchAnalysisMode() : searchParameters
                                .getMlAnalaysisMode();
                        List<Locale> locales = searchParameters.getLocales();
                        List<Locale> expandedLocales = new ArrayList<Locale>();
                        for (Locale locale : (((locales == null) || (locales.size() == 0)) ? Collections.singletonList(I18NUtil.getLocale()) : locales))
                        {
                            expandedLocales.addAll(MLAnalysisMode.getLocales(mlAnalysisMode, locale, false));
                        }
                        for (Locale locale : (((expandedLocales == null) || (expandedLocales.size() == 0)) ? Collections.singletonList(I18NUtil.getLocale()) : expandedLocales))
                        {
                            if (locale.toString().length() == 0)
                            {
                                continue;
                            }

                            String textFieldName = fieldName;

                            if (tokenisationMode == IndexTokenisationMode.BOTH)
                            {
                                textFieldName = textFieldName + "." + locale + ".sort";
                            }

                            addLocaleSpecificUntokenisedTextRangeFunction(field, part1, part2, includeLower, includeUpper, luceneFunction, booleanQuery, mlAnalysisMode, locale,
                                    textFieldName);

                        }
                        return booleanQuery;
                    }
                    else
                    {
                        throw new UnsupportedOperationException("Lucene Function");
                    }
                }

                if (propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT))
                {
                    throw new UnsupportedOperationException("Range is not supported against ml-text");
                }
                else if (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
                {
                    throw new UnsupportedOperationException("Range is not supported against content");
                }
                else if (propertyDef.getDataType().getName().equals(DataTypeDefinition.TEXT))
                {
                    BooleanQuery booleanQuery = new BooleanQuery();
                    MLAnalysisMode mlAnalysisMode = searchParameters.getMlAnalaysisMode() == null ? config.getDefaultMLSearchAnalysisMode() : searchParameters.getMlAnalaysisMode();
                    List<Locale> locales = searchParameters.getLocales();
                    List<Locale> expandedLocales = new ArrayList<Locale>();
                    for (Locale locale : (((locales == null) || (locales.size() == 0)) ? Collections.singletonList(I18NUtil.getLocale()) : locales))
                    {
                        expandedLocales.addAll(MLAnalysisMode.getLocales(mlAnalysisMode, locale, false));
                    }
                    for (Locale locale : (((expandedLocales == null) || (expandedLocales.size() == 0)) ? Collections.singletonList(I18NUtil.getLocale()) : expandedLocales))
                    {

                        String textFieldName = fieldName;

                        if ((analysisMode == AnalysisMode.IDENTIFIER) || (analysisMode == AnalysisMode.LIKE))
                        {
                            {
                                // text and ml text need locale
                                IndexTokenisationMode tm = propertyDef.getIndexTokenisationMode();
                                if ((tm != null) && (tm == IndexTokenisationMode.BOTH))
                                {
                                    if (locale.toString().length() == 0)
                                    {
                                        textFieldName = textFieldName + ".no_locale";
                                    }
                                    else
                                    {
                                        textFieldName = textFieldName + "." + locale + ".sort";
                                    }

                                }

                            }
                        }
                        switch (tokenisationMode)
                        {
                        case BOTH:
                            switch (analysisMode)
                            {
                            case DEFAULT:
                            case TOKENISE:
                                addLocaleSpecificTokenisedTextRange(part1, part2, includeLower, includeUpper, analysisMode, fieldName, booleanQuery, locale, textFieldName);
                                break;
                            case IDENTIFIER:
                                addLocaleSpecificUntokenisedTextRange(field, part1, part2, includeLower, includeUpper, booleanQuery, mlAnalysisMode, locale, textFieldName);
                                break;
                            case WILD:
                            case LIKE:
                            case PREFIX:
                            case FUZZY:
                            default:
                                throw new UnsupportedOperationException();
                            }
                            break;
                        case FALSE:
                            addLocaleSpecificUntokenisedTextRange(field, part1, part2, includeLower, includeUpper, booleanQuery, mlAnalysisMode, locale, textFieldName);

                            break;
                        case TRUE:
                            addLocaleSpecificTokenisedTextRange(part1, part2, includeLower, includeUpper, analysisMode, fieldName, booleanQuery, locale, textFieldName);
                            break;
                        default:
                        }

                    }
                    return booleanQuery;
                }
                else if (propertyDef.getDataType().getName().equals(DataTypeDefinition.DATETIME))
                {
                    DataTypeDefinition dataType = propertyDef.getDataType();
                    String analyserClassName = dataType.getAnalyserClassName();
                    boolean usesDateTimeAnalyser = analyserClassName.equals(DateTimeAnalyser.class.getCanonicalName());
                    // Expand query for internal date time format

                    if (usesDateTimeAnalyser)
                    {
                        Calendar start = Calendar.getInstance();
                        Calendar end = Calendar.getInstance();
                        try
                        {
                            Date date = CachingDateFormat.lenientParse(part1);
                            start.setTime(date);
                        }
                        catch (java.text.ParseException e)
                        {
                            SimpleDateFormat oldDf = CachingDateFormat.getDateFormat();
                            try
                            {
                                Date date = oldDf.parse(part1);
                                start.setTime(date);
                                start.set(Calendar.MILLISECOND, 0);
                            }
                            catch (java.text.ParseException ee)
                            {
                                if (part1.equalsIgnoreCase("min"))
                                {
                                    start.set(Calendar.YEAR, start.getMinimum(Calendar.YEAR));
                                    start.set(Calendar.DAY_OF_YEAR, start.getMinimum(Calendar.DAY_OF_YEAR));
                                    start.set(Calendar.HOUR_OF_DAY, start.getMinimum(Calendar.HOUR_OF_DAY));
                                    start.set(Calendar.MINUTE, start.getMinimum(Calendar.MINUTE));
                                    start.set(Calendar.SECOND, start.getMinimum(Calendar.SECOND));
                                    start.set(Calendar.MILLISECOND, start.getMinimum(Calendar.MILLISECOND));
                                }
                                else if (part1.equalsIgnoreCase("now"))
                                {
                                    start.setTime(new Date());
                                }
                                else if (part1.equalsIgnoreCase("today"))
                                {
                                    start.setTime(new Date());
                                    start.set(Calendar.HOUR_OF_DAY, start.getMinimum(Calendar.HOUR_OF_DAY));
                                    start.set(Calendar.MINUTE, start.getMinimum(Calendar.MINUTE));
                                    start.set(Calendar.SECOND, start.getMinimum(Calendar.SECOND));
                                    start.set(Calendar.MILLISECOND, start.getMinimum(Calendar.MILLISECOND));

                                }
                                else
                                {
                                    return new TermQuery(new Term("NO_TOKENS", "__"));
                                }
                            }
                        }
                        try
                        {
                            Date date = CachingDateFormat.lenientParse(part2);
                            end.setTime(date);
                        }
                        catch (java.text.ParseException e)
                        {
                            SimpleDateFormat oldDf = CachingDateFormat.getDateFormat();
                            try
                            {
                                Date date = oldDf.parse(part2);
                                end.setTime(date);
                                end.set(Calendar.MILLISECOND, 0);
                            }
                            catch (java.text.ParseException ee)
                            {
                                if (part2.equalsIgnoreCase("max"))
                                {
                                    end.set(Calendar.YEAR, end.getMaximum(Calendar.YEAR));
                                    end.set(Calendar.DAY_OF_YEAR, end.getMaximum(Calendar.DAY_OF_YEAR));
                                    end.set(Calendar.HOUR_OF_DAY, end.getMaximum(Calendar.HOUR_OF_DAY));
                                    end.set(Calendar.MINUTE, end.getMaximum(Calendar.MINUTE));
                                    end.set(Calendar.SECOND, end.getMaximum(Calendar.SECOND));
                                    end.set(Calendar.MILLISECOND, end.getMaximum(Calendar.MILLISECOND));
                                }
                                else if (part2.equalsIgnoreCase("now"))
                                {
                                    end.setTime(new Date());
                                }
                                else if (part1.equalsIgnoreCase("today"))
                                {
                                    end.setTime(new Date());
                                    end.set(Calendar.HOUR_OF_DAY, end.getMinimum(Calendar.HOUR_OF_DAY));
                                    end.set(Calendar.MINUTE, end.getMinimum(Calendar.MINUTE));
                                    end.set(Calendar.SECOND, end.getMinimum(Calendar.SECOND));
                                    end.set(Calendar.MILLISECOND, end.getMinimum(Calendar.MILLISECOND));

                                }
                                else
                                {
                                    return new TermQuery(new Term("NO_TOKENS", "__"));
                                }
                            }
                        }

                        // Build a composite query for all the bits
                        Query rq = buildDateTimeRange(fieldName, start, end, includeLower, includeUpper);
                        return rq;
                    }
                    else
                    {
                        // Old Date time
                        String first = getToken(fieldName, part1, AnalysisMode.DEFAULT);
                        String last = getToken(fieldName, part2, AnalysisMode.DEFAULT);
                        return new ConstantScoreRangeQuery(fieldName, first, last, includeLower, includeUpper);
                    }
                }
                else
                {
                    // Default property behaviour
                    String first = getToken(fieldName, part1, AnalysisMode.DEFAULT);
                    String last = getToken(fieldName, part2, AnalysisMode.DEFAULT);
                    return new ConstantScoreRangeQuery(fieldName, first, last, includeLower, includeUpper);
                }
            }
            else
            {
                // No DD def
                String first = getToken(fieldName, part1, AnalysisMode.DEFAULT);
                String last = getToken(fieldName, part2, AnalysisMode.DEFAULT);
                return new ConstantScoreRangeQuery(fieldName, first, last, includeLower, includeUpper);
            }
        }
        else
        {
            // None property - leave alone
            if (getLowercaseExpandedTerms())
            {
                part1 = part1.toLowerCase();
                part2 = part2.toLowerCase();
            }
            return new ConstantScoreRangeQuery(field, part1, part2, includeLower, includeUpper);
        }
    }

    private void addLocaleSpecificUntokenisedTextRangeFunction(String expandedFieldName, String lower, String upper, boolean includeLower, boolean includeUpper,
            LuceneFunction luceneFunction, BooleanQuery booleanQuery, MLAnalysisMode mlAnalysisMode, Locale locale, String textFieldName)
    {
        String lowerTermText = lower;
        if (locale.toString().length() > 0)
        {
            lowerTermText = "{" + locale + "}" + lower;
        }
        String upperTermText = upper;
        if (locale.toString().length() > 0)
        {
            upperTermText = "{" + locale + "}" + upper;
        }
        Query subQuery = buildRangeFunctionQuery(textFieldName, lowerTermText, upperTermText, includeLower, includeUpper, luceneFunction);
        booleanQuery.add(subQuery, Occur.SHOULD);

        if (booleanQuery.getClauses().length == 0)
        {
            booleanQuery.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
        }
    }

    private Query buildRangeFunctionQuery(String expandedFieldName, String lowerTermText, String upperTermText, boolean includeLower, boolean includeUpper,
            LuceneFunction luceneFunction)
    {
        String testLowerTermText = lowerTermText;
        if (testLowerTermText.startsWith("{"))
        {
            int index = lowerTermText.indexOf("}");
            testLowerTermText = lowerTermText.substring(index + 1);
        }

        String testUpperTermText = upperTermText;
        if (testUpperTermText.startsWith("{"))
        {
            int index = upperTermText.indexOf("}");
            testUpperTermText = upperTermText.substring(index + 1);
        }

        switch (luceneFunction)
        {
        case LOWER:
            if (testLowerTermText.equals(testLowerTermText.toLowerCase()) && testUpperTermText.equals(testUpperTermText.toLowerCase()))
            {
                return new CaseInsensitiveFieldRangeQuery(expandedFieldName, lowerTermText, upperTermText, includeLower, includeUpper);
            }
            else
            {
                // No match
                return new TermQuery(new Term("NO_TOKENS", "__"));
            }
        case UPPER:
            if (testLowerTermText.equals(testLowerTermText.toUpperCase()) && testUpperTermText.equals(testUpperTermText.toUpperCase()))
            {
                return new CaseInsensitiveFieldRangeQuery(expandedFieldName, lowerTermText, upperTermText, includeLower, includeUpper);
            }
            else
            {
                // No match
                return new TermQuery(new Term("NO_TOKENS", "__"));
            }
        default:
            throw new UnsupportedOperationException("Unsupported Lucene Function " + luceneFunction);

        }
    }

    private void addLocaleSpecificTokenisedTextRange(String part1, String part2, boolean includeLower, boolean includeUpper, AnalysisMode analysisMode, String fieldName,
            BooleanQuery booleanQuery, Locale locale, String textFieldName) throws ParseException
    {
        StringBuilder builder = new StringBuilder();
        builder.append("\u0000").append(locale.toString()).append("\u0000").append(part1);
        String first = getToken(fieldName, builder.toString(), analysisMode);

        builder = new StringBuilder();
        builder.append("\u0000").append(locale.toString()).append("\u0000").append(part2);
        String last = getToken(fieldName, builder.toString(), analysisMode);

        Query query = new ConstantScoreRangeQuery(textFieldName, first, last, includeLower, includeUpper);
        booleanQuery.add(query, Occur.SHOULD);
    }

    private void addLocaleSpecificUntokenisedTextRange(String field, String part1, String part2, boolean includeLower, boolean includeUpper, BooleanQuery booleanQuery,
            MLAnalysisMode mlAnalysisMode, Locale locale, String textFieldName)
    {
        String lower = part1;
        String upper = part2;
        if (locale.toString().length() > 0)
        {
            lower = "{" + locale + "}" + part1;
            upper = "{" + locale + "}" + part2;
        }

        Query subQuery = new ConstantScoreRangeQuery(textFieldName, lower, upper, includeLower, includeUpper);
        booleanQuery.add(subQuery, Occur.SHOULD);

        if (booleanQuery.getClauses().length == 0)
        {
            booleanQuery.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
        }
    }

    private Query buildDateTimeRange(String field, Calendar start, Calendar end, boolean includeLower, boolean includeUpper) throws ParseException
    {
        BooleanQuery query = new BooleanQuery();
        Query part;
        if (start.get(Calendar.YEAR) == end.get(Calendar.YEAR))
        {
            part = new TermQuery(new Term(field, "YE" + start.get(Calendar.YEAR)));
            query.add(part, Occur.MUST);
            if (start.get(Calendar.MONTH) == end.get(Calendar.MONTH))
            {
                part = new TermQuery(new Term(field, build2SF("MO", start.get(Calendar.MONTH))));
                query.add(part, Occur.MUST);
                if (start.get(Calendar.DAY_OF_MONTH) == end.get(Calendar.DAY_OF_MONTH))
                {
                    part = new TermQuery(new Term(field, build2SF("DA", start.get(Calendar.DAY_OF_MONTH))));
                    query.add(part, Occur.MUST);
                    if (start.get(Calendar.HOUR_OF_DAY) == end.get(Calendar.HOUR_OF_DAY))
                    {
                        part = new TermQuery(new Term(field, build2SF("HO", start.get(Calendar.HOUR_OF_DAY))));
                        query.add(part, Occur.MUST);
                        if (start.get(Calendar.MINUTE) == end.get(Calendar.MINUTE))
                        {
                            part = new TermQuery(new Term(field, build2SF("MI", start.get(Calendar.MINUTE))));
                            query.add(part, Occur.MUST);
                            if (start.get(Calendar.SECOND) == end.get(Calendar.SECOND))
                            {
                                part = new TermQuery(new Term(field, build2SF("SE", start.get(Calendar.SECOND))));
                                query.add(part, Occur.MUST);
                                if (start.get(Calendar.MILLISECOND) == end.get(Calendar.MILLISECOND))
                                {
                                    if (includeLower && includeUpper)
                                    {
                                        part = new TermQuery(new Term(field, build3SF("MS", start.get(Calendar.MILLISECOND))));
                                        query.add(part, Occur.MUST);
                                    }
                                    else
                                    {
                                        return new TermQuery(new Term("NO_TOKENS", "__"));
                                    }
                                }
                                else
                                {
                                    // only ms
                                    part = new ConstantScoreRangeQuery(field, build3SF("MS", start.get(Calendar.MILLISECOND)), build3SF("MS", end.get(Calendar.MILLISECOND)),
                                            includeLower, includeUpper);
                                    query.add(part, Occur.MUST);
                                }
                            }
                            else
                            {
                                // s + ms

                                BooleanQuery subQuery = new BooleanQuery();
                                Query subPart;

                                subPart = buildStart(field, start, includeLower, Calendar.SECOND, Calendar.MILLISECOND);
                                if (subPart != null)
                                {
                                    subQuery.add(subPart, Occur.SHOULD);
                                }

                                if ((end.get(Calendar.SECOND) - start.get(Calendar.SECOND)) > 1)
                                {
                                    subPart = new ConstantScoreRangeQuery(field, build2SF("SE", start.get(Calendar.SECOND)), build2SF("SE", end.get(Calendar.SECOND)), false, false);
                                    subQuery.add(subPart, Occur.SHOULD);
                                }

                                subPart = buildEnd(field, end, includeUpper, Calendar.SECOND, Calendar.MILLISECOND);
                                if (subPart != null)
                                {
                                    subQuery.add(subPart, Occur.SHOULD);
                                }

                                if (subQuery.clauses().size() > 0)
                                {
                                    query.add(subQuery, Occur.MUST);
                                }

                            }
                        }
                        else
                        {
                            // min + s + ms

                            BooleanQuery subQuery = new BooleanQuery();
                            Query subPart;

                            for (int i : new int[] { Calendar.MILLISECOND, Calendar.SECOND })
                            {
                                subPart = buildStart(field, start, includeLower, Calendar.MINUTE, i);
                                if (subPart != null)
                                {
                                    subQuery.add(subPart, Occur.SHOULD);
                                }
                            }

                            if ((end.get(Calendar.MINUTE) - start.get(Calendar.MINUTE)) > 1)
                            {
                                subPart = new ConstantScoreRangeQuery(field, build2SF("MI", start.get(Calendar.MINUTE)), build2SF("MI", end.get(Calendar.MINUTE)), false, false);
                                subQuery.add(subPart, Occur.SHOULD);
                            }

                            for (int i : new int[] { Calendar.SECOND, Calendar.MILLISECOND })
                            {
                                subPart = buildEnd(field, end, includeUpper, Calendar.MINUTE, i);
                                if (subPart != null)
                                {
                                    subQuery.add(subPart, Occur.SHOULD);
                                }
                            }

                            if (subQuery.clauses().size() > 0)
                            {
                                query.add(subQuery, Occur.MUST);
                            }
                        }
                    }
                    else
                    {
                        // hr + min + s + ms

                        BooleanQuery subQuery = new BooleanQuery();
                        Query subPart;

                        for (int i : new int[] { Calendar.MILLISECOND, Calendar.SECOND, Calendar.MINUTE })
                        {
                            subPart = buildStart(field, start, includeLower, Calendar.HOUR_OF_DAY, i);
                            if (subPart != null)
                            {
                                subQuery.add(subPart, Occur.SHOULD);
                            }
                        }

                        if ((end.get(Calendar.HOUR_OF_DAY) - start.get(Calendar.HOUR_OF_DAY)) > 1)
                        {
                            subPart = new ConstantScoreRangeQuery(field, build2SF("HO", start.get(Calendar.HOUR_OF_DAY)), build2SF("HO", end.get(Calendar.HOUR_OF_DAY)), false,
                                    false);
                            subQuery.add(subPart, Occur.SHOULD);
                        }

                        for (int i : new int[] { Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND })
                        {
                            subPart = buildEnd(field, end, includeUpper, Calendar.HOUR_OF_DAY, i);
                            if (subPart != null)
                            {
                                subQuery.add(subPart, Occur.SHOULD);
                            }
                        }

                        if (subQuery.clauses().size() > 0)
                        {
                            query.add(subQuery, Occur.MUST);
                        }
                    }
                }
                else
                {
                    // day + hr + min + s + ms

                    BooleanQuery subQuery = new BooleanQuery();
                    Query subPart;

                    for (int i : new int[] { Calendar.MILLISECOND, Calendar.SECOND, Calendar.MINUTE, Calendar.HOUR_OF_DAY })
                    {
                        subPart = buildStart(field, start, includeLower, Calendar.DAY_OF_MONTH, i);
                        if (subPart != null)
                        {
                            subQuery.add(subPart, Occur.SHOULD);
                        }
                    }

                    if ((end.get(Calendar.DAY_OF_MONTH) - start.get(Calendar.DAY_OF_MONTH)) > 1)
                    {
                        subPart = new ConstantScoreRangeQuery(field, build2SF("DA", start.get(Calendar.DAY_OF_MONTH)), build2SF("DA", end.get(Calendar.DAY_OF_MONTH)), false, false);
                        subQuery.add(subPart, Occur.SHOULD);
                    }

                    for (int i : new int[] { Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND })
                    {
                        subPart = buildEnd(field, end, includeUpper, Calendar.DAY_OF_MONTH, i);
                        if (subPart != null)
                        {
                            subQuery.add(subPart, Occur.SHOULD);
                        }
                    }

                    if (subQuery.clauses().size() > 0)
                    {
                        query.add(subQuery, Occur.MUST);
                    }

                }
            }
            else
            {
                // month + day + hr + min + s + ms

                BooleanQuery subQuery = new BooleanQuery();
                Query subPart;

                for (int i : new int[] { Calendar.MILLISECOND, Calendar.SECOND, Calendar.MINUTE, Calendar.HOUR_OF_DAY, Calendar.DAY_OF_MONTH })
                {
                    subPart = buildStart(field, start, includeLower, Calendar.MONTH, i);
                    if (subPart != null)
                    {
                        subQuery.add(subPart, Occur.SHOULD);
                    }
                }

                if ((end.get(Calendar.MONTH) - start.get(Calendar.MONTH)) > 1)
                {
                    subPart = new ConstantScoreRangeQuery(field, build2SF("MO", start.get(Calendar.MONTH)), build2SF("MO", end.get(Calendar.MONTH)), false, false);
                    subQuery.add(subPart, Occur.SHOULD);
                }

                for (int i : new int[] { Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND })
                {
                    subPart = buildEnd(field, end, includeUpper, Calendar.MONTH, i);
                    if (subPart != null)
                    {
                        subQuery.add(subPart, Occur.SHOULD);
                    }
                }

                if (subQuery.clauses().size() > 0)
                {
                    query.add(subQuery, Occur.MUST);
                }
            }
        }
        else
        {
            // year + month + day + hr + min + s + ms

            BooleanQuery subQuery = new BooleanQuery();
            Query subPart;

            for (int i : new int[] { Calendar.MILLISECOND, Calendar.SECOND, Calendar.MINUTE, Calendar.HOUR_OF_DAY, Calendar.DAY_OF_MONTH, Calendar.MONTH })
            {
                subPart = buildStart(field, start, includeLower, Calendar.YEAR, i);
                if (subPart != null)
                {
                    subQuery.add(subPart, Occur.SHOULD);
                }
            }

            if ((end.get(Calendar.YEAR) - start.get(Calendar.YEAR)) > 1)
            {
                subPart = new ConstantScoreRangeQuery(field, "YE" + start.get(Calendar.YEAR), "YE" + end.get(Calendar.YEAR), false, false);
                subQuery.add(subPart, Occur.SHOULD);
            }

            for (int i : new int[] { Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND })
            {
                subPart = buildEnd(field, end, includeUpper, Calendar.YEAR, i);
                if (subPart != null)
                {
                    subQuery.add(subPart, Occur.SHOULD);
                }
            }

            if (subQuery.clauses().size() > 0)
            {
                query.add(subQuery, Occur.MUST);
            }
        }
        return query;
    }

    private Query buildStart(String field, Calendar cal, boolean inclusive, int startField, int padField)
    {
        BooleanQuery range = new BooleanQuery();
        // only ms difference
        Query part;

        int ms = cal.get(Calendar.MILLISECOND) + (inclusive ? 0 : 1);

        switch (startField)
        {
        case Calendar.YEAR:
            part = new TermQuery(new Term(field, "YE" + cal.get(Calendar.YEAR)));
            range.add(part, Occur.MUST);
        case Calendar.MONTH:
            if ((cal.get(Calendar.MONTH) == 0)
                    && (cal.get(Calendar.DAY_OF_MONTH) == 1) && (cal.get(Calendar.HOUR_OF_DAY) == 0) && (cal.get(Calendar.MINUTE) == 0) && (cal.get(Calendar.SECOND) == 0)
                    && (ms == 0))
            {
                if (padField == Calendar.DAY_OF_MONTH)
                {
                    break;
                }
                else
                {
                    return null;
                }
            }
            else if (padField == Calendar.MONTH)
            {
                if (cal.get(Calendar.MONTH) < cal.getMaximum(Calendar.MONTH))
                {
                    part = new ConstantScoreRangeQuery(field, build2SF("MO", (cal.get(Calendar.MONTH) + 1)), "MO" + cal.getMaximum(Calendar.MONTH), true, true);
                    range.add(part, Occur.MUST);
                    break;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                part = new TermQuery(new Term(field, build2SF("MO", cal.get(Calendar.MONTH))));
                range.add(part, Occur.MUST);
            }
        case Calendar.DAY_OF_MONTH:
            if ((cal.get(Calendar.DAY_OF_MONTH) == 1) && (cal.get(Calendar.HOUR_OF_DAY) == 0) && (cal.get(Calendar.MINUTE) == 0) && (cal.get(Calendar.SECOND) == 0) && (ms == 0))
            {
                if (padField == Calendar.HOUR_OF_DAY)
                {
                    break;
                }
                else
                {
                    return null;
                }
            }
            else if (padField == Calendar.DAY_OF_MONTH)
            {
                if (cal.get(Calendar.DAY_OF_MONTH) < cal.getMaximum(Calendar.DAY_OF_MONTH))
                {
                    part = new ConstantScoreRangeQuery(field, build2SF("DA", (cal.get(Calendar.DAY_OF_MONTH) + 1)), "DA" + cal.getMaximum(Calendar.DAY_OF_MONTH), true, true);
                    range.add(part, Occur.MUST);
                    break;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                part = new TermQuery(new Term(field, build2SF("DA", cal.get(Calendar.DAY_OF_MONTH))));
                range.add(part, Occur.MUST);
            }
        case Calendar.HOUR_OF_DAY:
            if ((cal.get(Calendar.HOUR_OF_DAY) == 0) && (cal.get(Calendar.MINUTE) == 0) && (cal.get(Calendar.SECOND) == 0) && (ms == 0))
            {
                if (padField == Calendar.MINUTE)
                {
                    break;
                }
                else
                {
                    return null;
                }
            }
            else if (padField == Calendar.HOUR_OF_DAY)
            {
                if (cal.get(Calendar.HOUR_OF_DAY) < cal.getMaximum(Calendar.HOUR_OF_DAY))
                {
                    part = new ConstantScoreRangeQuery(field, build2SF("HO", (cal.get(Calendar.HOUR_OF_DAY) + 1)), "HO" + cal.getMaximum(Calendar.HOUR_OF_DAY), true, true);
                    range.add(part, Occur.MUST);
                    break;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                part = new TermQuery(new Term(field, build2SF("HO", cal.get(Calendar.HOUR_OF_DAY))));
                range.add(part, Occur.MUST);
            }
        case Calendar.MINUTE:
            if ((cal.get(Calendar.MINUTE) == 0) && (cal.get(Calendar.SECOND) == 0) && (ms == 0))
            {
                if (padField == Calendar.SECOND)
                {
                    break;
                }
                else
                {
                    return null;
                }
            }
            else if (padField == Calendar.MINUTE)
            {
                if (cal.get(Calendar.MINUTE) < cal.getMaximum(Calendar.MINUTE))
                {
                    part = new ConstantScoreRangeQuery(field, build2SF("MI", (cal.get(Calendar.MINUTE) + 1)), "MI" + cal.getMaximum(Calendar.MINUTE), true, true);
                    range.add(part, Occur.MUST);
                    break;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                part = new TermQuery(new Term(field, build2SF("MI", cal.get(Calendar.MINUTE))));
                range.add(part, Occur.MUST);
            }
        case Calendar.SECOND:
            if ((cal.get(Calendar.SECOND) == 0) && (ms == 0))
            {
                if (padField == Calendar.MILLISECOND)
                {
                    break;
                }
                else
                {
                    return null;
                }
            }
            else if (padField == Calendar.SECOND)
            {
                if (cal.get(Calendar.SECOND) < cal.getMaximum(Calendar.SECOND))
                {
                    part = new ConstantScoreRangeQuery(field, build2SF("SE", (cal.get(Calendar.SECOND) + 1)), "SE" + cal.getMaximum(Calendar.SECOND), true, true);
                    range.add(part, Occur.MUST);
                    break;
                }
                else
                {
                    return null;
                }

            }
            else
            {
                part = new TermQuery(new Term(field, build2SF("SE", cal.get(Calendar.SECOND))));
                range.add(part, Occur.MUST);
            }
        default:
            if ((ms > 0) && (ms <= cal.getMaximum(Calendar.MILLISECOND)))
            {
                part = new ConstantScoreRangeQuery(field, build3SF("MS", ms), "MS" + cal.getMaximum(Calendar.MILLISECOND), true, true);
                range.add(part, Occur.MUST);
            }
            else
            {
                return null;
            }
        }

        if (range.clauses().size() > 0)
        {
            return range;
        }
        else
        {
            return null;
        }
    }

    private Query buildEnd(String field, Calendar cal, boolean inclusive, int startField, int padField)
    {
        BooleanQuery range = new BooleanQuery();
        // only ms difference
        Query part;

        int ms = cal.get(Calendar.MILLISECOND) - (inclusive ? 0 : 1);

        switch (startField)
        {
        case Calendar.YEAR:
            part = new TermQuery(new Term(field, "YE" + cal.get(Calendar.YEAR)));
            range.add(part, Occur.MUST);
        case Calendar.MONTH:
            if ((cal.get(Calendar.MONTH) == 0)
                    && (cal.get(Calendar.DAY_OF_MONTH) == 1) && (cal.get(Calendar.HOUR_OF_DAY) == 0) && (cal.get(Calendar.MINUTE) == 0) && (cal.get(Calendar.SECOND) == 0)
                    && (ms == 0))
            {
                if (padField == Calendar.MONTH)
                {
                    return null;
                }
            }

            if (padField == Calendar.MONTH)
            {
                if (cal.get(Calendar.MONTH) > cal.getMinimum(Calendar.MONTH))
                {
                    part = new ConstantScoreRangeQuery(field, build2SF("MO", cal.getMinimum(Calendar.MONTH)), build2SF("MO", (cal.get(Calendar.MONTH) - 1)), true, true);
                    range.add(part, Occur.MUST);
                    break;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                part = new TermQuery(new Term(field, build2SF("MO", cal.get(Calendar.MONTH))));
                range.add(part, Occur.MUST);
            }
        case Calendar.DAY_OF_MONTH:
            if ((cal.get(Calendar.DAY_OF_MONTH) == 1) && (cal.get(Calendar.HOUR_OF_DAY) == 0) && (cal.get(Calendar.MINUTE) == 0) && (cal.get(Calendar.SECOND) == 0) && (ms == 0))
            {
                if (padField == Calendar.DAY_OF_MONTH)
                {
                    return null;
                }
            }

            if (padField == Calendar.DAY_OF_MONTH)
            {
                if (cal.get(Calendar.DAY_OF_MONTH) > cal.getMinimum(Calendar.DAY_OF_MONTH))
                {
                    part = new ConstantScoreRangeQuery(field, build2SF("DA", cal.getMinimum(Calendar.DAY_OF_MONTH)), build2SF("DA", (cal.get(Calendar.DAY_OF_MONTH) - 1)), true,
                            true);
                    range.add(part, Occur.MUST);
                    break;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                part = new TermQuery(new Term(field, build2SF("DA", cal.get(Calendar.DAY_OF_MONTH))));
                range.add(part, Occur.MUST);
            }
        case Calendar.HOUR_OF_DAY:
            if ((cal.get(Calendar.HOUR_OF_DAY) == 0) && (cal.get(Calendar.MINUTE) == 0) && (cal.get(Calendar.SECOND) == 0) && (ms == 0))
            {
                if (padField == Calendar.HOUR_OF_DAY)
                {
                    return null;
                }
            }

            if (padField == Calendar.HOUR_OF_DAY)
            {
                if (cal.get(Calendar.HOUR_OF_DAY) > cal.getMinimum(Calendar.HOUR_OF_DAY))
                {
                    part = new ConstantScoreRangeQuery(field, build2SF("HO", cal.getMinimum(Calendar.HOUR_OF_DAY)), build2SF("HO", (cal.get(Calendar.HOUR_OF_DAY) - 1)), true, true);
                    range.add(part, Occur.MUST);
                    break;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                part = new TermQuery(new Term(field, build2SF("HO", cal.get(Calendar.HOUR_OF_DAY))));
                range.add(part, Occur.MUST);
            }
        case Calendar.MINUTE:
            if ((cal.get(Calendar.MINUTE) == 0) && (cal.get(Calendar.SECOND) == 0) && (ms == 0))
            {
                if (padField == Calendar.MINUTE)
                {
                    return null;
                }
            }

            if (padField == Calendar.MINUTE)
            {
                if (cal.get(Calendar.MINUTE) > cal.getMinimum(Calendar.MINUTE))
                {
                    part = new ConstantScoreRangeQuery(field, build2SF("MI", cal.getMinimum(Calendar.MINUTE)), build2SF("MI", (cal.get(Calendar.MINUTE) - 1)), true, true);
                    range.add(part, Occur.MUST);
                    break;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                part = new TermQuery(new Term(field, build2SF("MI", cal.get(Calendar.MINUTE))));
                range.add(part, Occur.MUST);
            }
        case Calendar.SECOND:
            if ((cal.get(Calendar.SECOND) == 0) && (ms == 0))
            {
                if (padField == Calendar.SECOND)
                {
                    return null;
                }
            }

            if (padField == Calendar.SECOND)
            {
                if (cal.get(Calendar.SECOND) > cal.getMinimum(Calendar.SECOND))
                {
                    part = new ConstantScoreRangeQuery(field, build2SF("SE", cal.getMinimum(Calendar.SECOND)), build2SF("SE", (cal.get(Calendar.SECOND) - 1)), true, true);
                    range.add(part, Occur.MUST);
                    break;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                part = new TermQuery(new Term(field, build2SF("SE", cal.get(Calendar.SECOND))));
                range.add(part, Occur.MUST);
            }
        default:
            if ((ms >= cal.getMinimum(Calendar.MILLISECOND)) && (ms < cal.getMaximum(Calendar.MILLISECOND)))
            {
                part = new ConstantScoreRangeQuery(field, build3SF("MS", cal.getMinimum(Calendar.MILLISECOND)), build3SF("MS", ms), true, true);
                range.add(part, Occur.MUST);
            }
            else
            {
                return null;
            }
        }

        if (range.clauses().size() > 0)
        {
            return range;
        }
        else
        {
            return null;
        }
    }

    private String build2SF(String prefix, int value)
    {
        if (value < 10)
        {
            return prefix + "0" + value;
        }
        else
        {
            return prefix + value;
        }
    }

    private String build3SF(String prefix, int value)
    {
        if (value < 10)
        {
            return prefix + "00" + value;
        }
        else if (value < 100)
        {
            return prefix + "0" + value;
        }
        else
        {
            return prefix + value;
        }
    }

    private String expandAttributeFieldNamex(String field)
    {
        return "@"+expandQName(field.substring(1));
    }

    private String expandQName(String qnameString)
    {
        String fieldName = qnameString;
        // Check for any prefixes and expand to the full uri
        if (qnameString.charAt(0) != '{')
        {
            int colonPosition = qnameString.indexOf(':');
            if (colonPosition == -1)
            {
                // use the default namespace
                fieldName = "{" + searchParameters.getNamespace() + "}" + qnameString;
            }
            else
            {
                String prefix = qnameString.substring(0, colonPosition);
                String uri = matchURI(prefix);
                if(uri == null)
                {
                    fieldName = "{" + searchParameters.getNamespace() + "}" + qnameString;
                }
                else
                {
                    fieldName = "{" + uri + "}" + qnameString.substring(colonPosition + 1);
                }
                
            }
        }
        return fieldName;
    }
    
    private String matchURI(String prefix)
    {
        HashSet<String> prefixes = new HashSet<String>(namespacePrefixResolver.getPrefixes());
        if(prefixes.contains(prefix))
        {
            return namespacePrefixResolver.getNamespaceURI(prefix);
        }
        String match = null;
        for(String candidate : prefixes)
        {
            if(candidate.equalsIgnoreCase(prefix))
            {
                if(match == null)
                {
                    match = candidate;
                }
                else
                {
                    
                    throw new LuceneIndexException("Ambiguous namespace prefix "+prefix);
                    
                }
            }
        }
        if(match == null)
        {
            return null;
        }
        else
        {
            return namespacePrefixResolver.getNamespaceURI(match);
        }
    }

    private String getToken(String field, String value, AnalysisMode analysisMode) throws ParseException
    {
        TokenStream source = getAnalyzer().tokenStream(field, new StringReader(value), analysisMode);
        org.apache.lucene.analysis.Token reusableToken = new org.apache.lucene.analysis.Token();
        org.apache.lucene.analysis.Token nextToken;
        String tokenised = null;

        while (true)
        {
            try
            {
                nextToken = source.next(reusableToken);
            }
            catch (IOException e)
            {
                nextToken = null;
            }
            if (nextToken == null)
                break;
            tokenised = new String(nextToken.termBuffer(), 0, nextToken.termLength());
        }
        try
        {
            source.close();
        }
        catch (IOException e)
        {

        }

        return tokenised;
    }

    @Override
    public Query getPrefixQuery(String field, String termStr) throws ParseException
    {
        if (field.startsWith("@"))
        {
            return attributeQueryBuilder(field, termStr, new PrefixQuery(), AnalysisMode.PREFIX, LuceneFunction.FIELD);
        }
        else if (field.equals("TEXT"))
        {
            Set<String> text = searchParameters.getTextAttributes();
            if ((text == null) || (text.size() == 0))
            {
                Collection<QName> contentAttributes = dictionaryService.getAllProperties(DataTypeDefinition.CONTENT);
                BooleanQuery query = new BooleanQuery();
                for (QName qname : contentAttributes)
                {
                    // The super implementation will create phrase queries etc if required
                    Query part = getPrefixQuery("@" + qname.toString(), termStr);
                    if (part != null)
                    {
                        query.add(part, Occur.SHOULD);
                    }
                    else
                    {
                        query.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
                    }
                }
                return query;
            }
            else
            {
                BooleanQuery query = new BooleanQuery();
                for (String fieldName : text)
                {
                    Query part = getPrefixQuery(fieldName, termStr);
                    if (part != null)
                    {
                        query.add(part, Occur.SHOULD);
                    }
                    else
                    {
                        query.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
                    }
                }
                return query;
            }
        }
        else if (field.equals("ID"))
        {
            boolean lowercaseExpandedTerms = getLowercaseExpandedTerms();
            try
            {
                setLowercaseExpandedTerms(false);
                return super.getPrefixQuery(field, termStr);
            }
            finally
            {
                setLowercaseExpandedTerms(lowercaseExpandedTerms);
            }
        }
        else if (field.equals("PARENT"))
        {
            boolean lowercaseExpandedTerms = getLowercaseExpandedTerms();
            try
            {
                setLowercaseExpandedTerms(false);
                return super.getPrefixQuery(field, termStr);
            }
            finally
            {
                setLowercaseExpandedTerms(lowercaseExpandedTerms);
            }
        }
        else
        {
            return super.getPrefixQuery(field, termStr);
        }
    }

    @Override
    public Query getWildcardQuery(String field, String termStr) throws ParseException
    {
        return getWildcardQuery(field, termStr, AnalysisMode.WILD);
    }

    private Query getWildcardQuery(String field, String termStr, AnalysisMode analysisMode) throws ParseException
    {
        if (field.startsWith("@"))
        {
            return attributeQueryBuilder(field, termStr, new WildcardQuery(), analysisMode, LuceneFunction.FIELD);
        }

        else if (field.equals("TEXT"))
        {
            Set<String> text = searchParameters.getTextAttributes();
            if ((text == null) || (text.size() == 0))
            {
                Collection<QName> contentAttributes = dictionaryService.getAllProperties(DataTypeDefinition.CONTENT);
                BooleanQuery query = new BooleanQuery();
                for (QName qname : contentAttributes)
                {
                    // The super implementation will create phrase queries etc if required
                    Query part = getWildcardQuery("@" + qname.toString(), termStr);
                    if (part != null)
                    {
                        query.add(part, Occur.SHOULD);
                    }
                    else
                    {
                        query.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
                    }
                }
                return query;
            }
            else
            {
                BooleanQuery query = new BooleanQuery();
                for (String fieldName : text)
                {
                    Query part = getWildcardQuery(fieldName, termStr);
                    if (part != null)
                    {
                        query.add(part, Occur.SHOULD);
                    }
                    else
                    {
                        query.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
                    }
                }
                return query;
            }
        }
        else if (field.equals("ID"))
        {
            boolean lowercaseExpandedTerms = getLowercaseExpandedTerms();
            try
            {
                setLowercaseExpandedTerms(false);
                return super.getWildcardQuery(field, termStr);
            }
            finally
            {
                setLowercaseExpandedTerms(lowercaseExpandedTerms);
            }
        }
        else if (field.equals("PARENT"))
        {
            boolean lowercaseExpandedTerms = getLowercaseExpandedTerms();
            try
            {
                setLowercaseExpandedTerms(false);
                return super.getWildcardQuery(field, termStr);
            }
            finally
            {
                setLowercaseExpandedTerms(lowercaseExpandedTerms);
            }
        }
        else
        {
            return super.getWildcardQuery(field, termStr);
        }
    }

    @Override
    public Query getFuzzyQuery(String field, String termStr, float minSimilarity) throws ParseException
    {
        if (field.startsWith("@"))
        {
            return attributeQueryBuilder(field, termStr, new FuzzyQuery(minSimilarity), AnalysisMode.FUZZY, LuceneFunction.FIELD);
        }

        else if (field.equals("TEXT"))
        {
            Set<String> text = searchParameters.getTextAttributes();
            if ((text == null) || (text.size() == 0))
            {
                Collection<QName> contentAttributes = dictionaryService.getAllProperties(DataTypeDefinition.CONTENT);
                BooleanQuery query = new BooleanQuery();
                for (QName qname : contentAttributes)
                {
                    // The super implementation will create phrase queries etc if required
                    Query part = getFuzzyQuery("@" + qname.toString(), termStr, minSimilarity);
                    if (part != null)
                    {
                        query.add(part, Occur.SHOULD);
                    }
                    else
                    {
                        query.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
                    }
                }
                return query;
            }
            else
            {
                BooleanQuery query = new BooleanQuery();
                for (String fieldName : text)
                {
                    Query part = getFuzzyQuery(fieldName, termStr, minSimilarity);
                    if (part != null)
                    {
                        query.add(part, Occur.SHOULD);
                    }
                    else
                    {
                        query.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
                    }
                }
                return query;
            }
        }
        else if (field.equals("ID"))
        {
            boolean lowercaseExpandedTerms = getLowercaseExpandedTerms();
            try
            {
                setLowercaseExpandedTerms(false);
                return super.getFuzzyQuery(field, termStr, minSimilarity);
            }
            finally
            {
                setLowercaseExpandedTerms(lowercaseExpandedTerms);
            }
        }
        else if (field.equals("PARENT"))
        {
            boolean lowercaseExpandedTerms = getLowercaseExpandedTerms();
            try
            {
                setLowercaseExpandedTerms(false);
                return super.getFuzzyQuery(field, termStr, minSimilarity);
            }
            finally
            {
                setLowercaseExpandedTerms(lowercaseExpandedTerms);
            }
        }
        else
        {
            return super.getFuzzyQuery(field, termStr, minSimilarity);
        }
    }

    /**
     * @param dictionaryService
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param field
     * @param queryText
     * @param analysisMode
     * @param luceneFunction
     * @return the query
     * @throws ParseException
     */
    public Query getSuperFieldQuery(String field, String queryText, AnalysisMode analysisMode, LuceneFunction luceneFunction) throws ParseException
    {
        return getFieldQueryImpl(field, queryText, analysisMode, luceneFunction);
    }

    /**
     * @param field
     * @param termStr
     * @param minSimilarity
     * @return the query
     * @throws ParseException
     */
    public Query getSuperFuzzyQuery(String field, String termStr, float minSimilarity) throws ParseException
    {
        return super.getFuzzyQuery(field, termStr, minSimilarity);
    }

    /**
     * @param field
     * @param termStr
     * @return the query
     * @throws ParseException
     */
    public Query getSuperPrefixQuery(String field, String termStr) throws ParseException
    {
        return super.getPrefixQuery(field, termStr);
    }

    /**
     * @param field
     * @param termStr
     * @return the query
     * @throws ParseException
     */
    public Query getSuperWildcardQuery(String field, String termStr) throws ParseException
    {
        return super.getWildcardQuery(field, termStr);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.lucene.queryParser.QueryParser#newWildcardQuery(org.apache.lucene.index.Term)
     */
    @Override
    protected Query newWildcardQuery(Term t)
    {
        if (t.text().contains("\\"))
        {
            String regexp = SearchLanguageConversion.convert(SearchLanguageConversion.DEF_LUCENE, SearchLanguageConversion.DEF_REGEX, t.text());
            return new RegexQuery(new Term(t.field(), regexp));
        }
        else
        {
            return super.newWildcardQuery(t);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.lucene.queryParser.QueryParser#newPrefixQuery(org.apache.lucene.index.Term)
     */
    @Override
    protected Query newPrefixQuery(Term prefix)
    {
        if (prefix.text().contains("\\"))
        {
            String regexp = SearchLanguageConversion.convert(SearchLanguageConversion.DEF_LUCENE, SearchLanguageConversion.DEF_REGEX, prefix.text());
            return new RegexQuery(new Term(prefix.field(), regexp));
        }
        else
        {
            return super.newPrefixQuery(prefix);
        }

    }

    interface SubQuery
    {
        /**
         * @param field
         * @param queryText
         * @param analysisMode
         * @param luceneFunction
         * @return the query
         * @throws ParseException
         */
        Query getQuery(String field, String queryText, AnalysisMode analysisMode, LuceneFunction luceneFunction) throws ParseException;
    }

    class FieldQuery implements SubQuery
    {
        public Query getQuery(String field, String queryText, AnalysisMode analysisMode, LuceneFunction luceneFunction) throws ParseException
        {
            return getSuperFieldQuery(field, queryText, analysisMode, luceneFunction);
        }
    }

    class FuzzyQuery implements SubQuery
    {
        float minSimilarity;

        FuzzyQuery(float minSimilarity)
        {
            this.minSimilarity = minSimilarity;
        }

        public Query getQuery(String field, String termStr, AnalysisMode analysisMode, LuceneFunction luceneFunction) throws ParseException
        {
            return getSuperFuzzyQuery(field, termStr, minSimilarity);
        }
    }

    class PrefixQuery implements SubQuery
    {
        public Query getQuery(String field, String termStr, AnalysisMode analysisMode, LuceneFunction luceneFunction) throws ParseException
        {
            return getSuperPrefixQuery(field, termStr);
        }
    }

    class WildcardQuery implements SubQuery
    {
        public Query getQuery(String field, String termStr, AnalysisMode analysisMode, LuceneFunction luceneFunction) throws ParseException
        {
            return getSuperWildcardQuery(field, termStr);
        }
    }

    private Query attributeQueryBuilder(String field, String queryText, SubQuery subQueryBuilder, AnalysisMode analysisMode, LuceneFunction luceneFunction) throws ParseException
    {
        // TODO: Fix duplicate token generation for mltext, content and text.
        // -locale expansion here and in tokeisation -> duplicates

        // Get type info etc
        String propertyFieldName = null;
        String ending = "";
        if (field.endsWith(".mimetype"))
        {
            propertyFieldName = field.substring(1, field.length() - 9);
            ending = ".mimetype";
        }
        else if (field.endsWith(".size"))
        {
            propertyFieldName = field.substring(1, field.length() - 5);
            ending = ".size";
        }
        else if (field.endsWith(".locale"))
        {
            propertyFieldName = field.substring(1, field.length() - 7);
            ending = ".locale";
        }
        else
        {
            propertyFieldName = field.substring(1);
        }
        
        String expandedFieldName;
        QName propertyQName;
        PropertyDefinition propertyDef = matchPropertyDefinition(propertyFieldName);
        IndexTokenisationMode tokenisationMode = IndexTokenisationMode.TRUE;
        if (propertyDef != null)
        {
            tokenisationMode = propertyDef.getIndexTokenisationMode();
            if (tokenisationMode == null)
            {
                tokenisationMode = IndexTokenisationMode.TRUE;
            }
            expandedFieldName = "@"+propertyDef.getName()+ending;
            propertyQName = propertyDef.getName();
        }
        else
        {
            expandedFieldName = expandAttributeFieldNamex(field);
            propertyQName = QName.createQName(propertyFieldName);
        }
        

        if (luceneFunction != LuceneFunction.FIELD)
        {
            if ((tokenisationMode == IndexTokenisationMode.FALSE) || (tokenisationMode == IndexTokenisationMode.BOTH))
            {
                return functionQueryBuilder(expandedFieldName, propertyQName, propertyDef, tokenisationMode, queryText, luceneFunction);
            }
        }

        // Mime type
        if (expandedFieldName.endsWith(".mimetype"))
        {
            if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
            {
                return subQueryBuilder.getQuery(expandedFieldName, queryText, analysisMode, luceneFunction);
            }

        }
        else if (expandedFieldName.endsWith(".size"))
        {
            if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
            {
                return subQueryBuilder.getQuery(expandedFieldName, queryText, analysisMode, luceneFunction);
            }

        }
        else if (expandedFieldName.endsWith(".locale"))
        {
            if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
            {
                return subQueryBuilder.getQuery(expandedFieldName, queryText, analysisMode, luceneFunction);
            }

        }

        // Already in expanded form

        // ML

        if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT)))
        {
            // Build a sub query for each locale and or the results together - the analysis will take care of
            // cross language matching for each entry
            BooleanQuery booleanQuery = new BooleanQuery();
            MLAnalysisMode mlAnalysisMode = searchParameters.getMlAnalaysisMode() == null ? config.getDefaultMLSearchAnalysisMode() : searchParameters.getMlAnalaysisMode();
            List<Locale> locales = searchParameters.getLocales();
            List<Locale> expandedLocales = new ArrayList<Locale>();
            for (Locale locale : (((locales == null) || (locales.size() == 0)) ? Collections.singletonList(I18NUtil.getLocale()) : locales))
            {
                expandedLocales.addAll(MLAnalysisMode.getLocales(mlAnalysisMode, locale, false));
            }
            for (Locale locale : (((expandedLocales == null) || (expandedLocales.size() == 0)) ? Collections.singletonList(I18NUtil.getLocale()) : expandedLocales))
            {
                String mlFieldName = expandedFieldName;

                if ((tokenisationMode == IndexTokenisationMode.BOTH) && ((analysisMode == AnalysisMode.IDENTIFIER) || (analysisMode == AnalysisMode.LIKE)))
                {
                    {
                        // text and ml text need locale
                        IndexTokenisationMode tm = propertyDef.getIndexTokenisationMode();
                        if ((tm != null) && (tm == IndexTokenisationMode.BOTH))
                        {
                            if (locale.toString().length() == 0)
                            {
                                mlFieldName = mlFieldName + ".no_locale";
                            }
                            else
                            {
                                mlFieldName = mlFieldName + "." + locale + ".sort";
                            }
                        }

                    }
                }

                boolean lowercaseExpandedTerms = getLowercaseExpandedTerms();
                try
                {
                    switch (tokenisationMode)
                    {
                    case BOTH:
                        switch (analysisMode)
                        {
                        default:
                        case DEFAULT:
                        case TOKENISE:
                            addLocaleSpecificTokenisedMLOrTextAttribute(queryText, subQueryBuilder, analysisMode, luceneFunction, booleanQuery, locale, expandedFieldName);
                            break;
                        case IDENTIFIER:
                        case FUZZY:
                        case PREFIX:
                        case WILD:
                        case LIKE:
                            setLowercaseExpandedTerms(false);
                            addLocaleSpecificUntokenisedMLOrTextAttribute(field, queryText, subQueryBuilder, analysisMode, luceneFunction, booleanQuery, mlAnalysisMode, locale,
                                    mlFieldName);

                            break;
                        }
                        break;
                    case FALSE:
                        setLowercaseExpandedTerms(false);
                        addLocaleSpecificUntokenisedMLOrTextAttribute(field, queryText, subQueryBuilder, analysisMode, luceneFunction, booleanQuery, mlAnalysisMode, locale,
                                mlFieldName);
                        break;
                    case TRUE:
                    default:
                        switch (analysisMode)
                        {
                        default:
                        case DEFAULT:
                        case TOKENISE:
                        case IDENTIFIER:
                            addLocaleSpecificTokenisedMLOrTextAttribute(queryText, subQueryBuilder, analysisMode, luceneFunction, booleanQuery, locale, expandedFieldName);
                            break;
                        case FUZZY:
                        case PREFIX:
                        case WILD:
                        case LIKE:
                            addLocaleSpecificUntokenisedMLOrTextAttribute(field, queryText, subQueryBuilder, analysisMode, luceneFunction, booleanQuery, mlAnalysisMode, locale,
                                    mlFieldName);
                            break;
                        }
                    }
                }
                finally
                {
                    setLowercaseExpandedTerms(lowercaseExpandedTerms);
                }
            }
            return booleanQuery;
        }
        // Content
        else if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
        {
            // Identifier request are ignored for content

            // Build a sub query for each locale and or the results together -
            // - add an explicit condition for the locale

            MLAnalysisMode mlAnalysisMode = searchParameters.getMlAnalaysisMode() == null ? config.getDefaultMLSearchAnalysisMode() : searchParameters.getMlAnalaysisMode();

            if (mlAnalysisMode.includesAll())
            {
                return subQueryBuilder.getQuery(expandedFieldName, queryText, analysisMode, luceneFunction);
            }

            List<Locale> locales = searchParameters.getLocales();
            List<Locale> expandedLocales = new ArrayList<Locale>();
            for (Locale locale : (((locales == null) || (locales.size() == 0)) ? Collections.singletonList(I18NUtil.getLocale()) : locales))
            {
                expandedLocales.addAll(MLAnalysisMode.getLocales(mlAnalysisMode, locale, true));
            }

            if (expandedLocales.size() > 0)
            {
                BooleanQuery booleanQuery = new BooleanQuery();
                Query contentQuery = subQueryBuilder.getQuery(expandedFieldName, queryText, analysisMode, luceneFunction);
                if (contentQuery != null)
                {
                    booleanQuery.add(contentQuery, Occur.MUST);
                    BooleanQuery subQuery = new BooleanQuery();
                    for (Locale locale : (expandedLocales))
                    {
                        StringBuilder builder = new StringBuilder();
                        builder.append(expandedFieldName).append(".locale");
                        String localeString = locale.toString();
                        if (localeString.indexOf("*") == -1)
                        {
                            Query localeQuery = getFieldQuery(builder.toString(), localeString);
                            if (localeQuery != null)
                            {
                                subQuery.add(localeQuery, Occur.SHOULD);
                            }
                            else
                            {
                                subQuery.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
                            }
                        }
                        else
                        {
                            Query localeQuery = getWildcardQuery(builder.toString(), localeString);
                            if (localeQuery != null)
                            {
                                subQuery.add(localeQuery, Occur.SHOULD);
                            }
                            else
                            {
                                subQuery.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
                            }
                        }
                    }
                    booleanQuery.add(subQuery, Occur.MUST);
                }
                return booleanQuery;
            }
            else
            {
                Query query = subQueryBuilder.getQuery(expandedFieldName, queryText, analysisMode, luceneFunction);
                if (query != null)
                {
                    return query;
                }
                else
                {
                    return new TermQuery(new Term("NO_TOKENS", "__"));
                }
            }

        }
        else if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.TEXT)))
        {
            if (propertyQName.equals(ContentModel.PROP_USER_USERNAME) || propertyQName.equals(ContentModel.PROP_USERNAME) || propertyQName.equals(ContentModel.PROP_AUTHORITY_NAME))
            {
                return subQueryBuilder.getQuery(expandedFieldName, queryText, analysisMode, luceneFunction);
            }

            BooleanQuery booleanQuery = new BooleanQuery();
            MLAnalysisMode mlAnalysisMode = searchParameters.getMlAnalaysisMode() == null ? config.getDefaultMLSearchAnalysisMode() : searchParameters.getMlAnalaysisMode();
            List<Locale> locales = searchParameters.getLocales();
            List<Locale> expandedLocales = new ArrayList<Locale>();
            for (Locale locale : (((locales == null) || (locales.size() == 0)) ? Collections.singletonList(I18NUtil.getLocale()) : locales))
            {
                expandedLocales.addAll(MLAnalysisMode.getLocales(mlAnalysisMode, locale, false));
            }
            for (Locale locale : (((expandedLocales == null) || (expandedLocales.size() == 0)) ? Collections.singletonList(I18NUtil.getLocale()) : expandedLocales))
            {
                String textFieldName = expandedFieldName;

                if ((tokenisationMode == IndexTokenisationMode.BOTH) && ((analysisMode == AnalysisMode.IDENTIFIER) || (analysisMode == AnalysisMode.LIKE)))
                {
                    {
                        // text and ml text need locale
                        IndexTokenisationMode tm = propertyDef.getIndexTokenisationMode();
                        if ((tm != null) && (tm == IndexTokenisationMode.BOTH))
                        {
                            textFieldName = textFieldName + "." + locale + ".sort";
                        }

                    }
                }

                boolean lowercaseExpandedTerms = getLowercaseExpandedTerms();
                try
                {
                    switch (tokenisationMode)
                    {
                    case BOTH:
                        switch (analysisMode)
                        {
                        default:
                        case DEFAULT:
                        case TOKENISE:
                            addLocaleSpecificTokenisedMLOrTextAttribute(queryText, subQueryBuilder, analysisMode, luceneFunction, booleanQuery, locale, textFieldName);
                            break;
                        case IDENTIFIER:
                        case FUZZY:
                        case PREFIX:
                        case WILD:
                        case LIKE:
                            setLowercaseExpandedTerms(false);
                            addLocaleSpecificUntokenisedMLOrTextAttribute(field, queryText, subQueryBuilder, analysisMode, luceneFunction, booleanQuery, mlAnalysisMode, locale,
                                    textFieldName);
                            break;
                        }
                        break;
                    case FALSE:
                        setLowercaseExpandedTerms(false);
                        addLocaleSpecificUntokenisedMLOrTextAttribute(field, queryText, subQueryBuilder, analysisMode, luceneFunction, booleanQuery, mlAnalysisMode, locale,
                                textFieldName);
                        break;
                    case TRUE:
                    default:
                        switch (analysisMode)
                        {
                        case DEFAULT:
                        case TOKENISE:
                        case IDENTIFIER:
                            addLocaleSpecificTokenisedMLOrTextAttribute(queryText, subQueryBuilder, analysisMode, luceneFunction, booleanQuery, locale, expandedFieldName);
                            break;
                        case FUZZY:
                        case PREFIX:
                        case WILD:
                        case LIKE:
                            addLocaleSpecificUntokenisedMLOrTextAttribute(field, queryText, subQueryBuilder, analysisMode, luceneFunction, booleanQuery, mlAnalysisMode, locale,
                                    textFieldName);
                            break;
                        }
                        break;
                    }
                }
                finally
                {
                    setLowercaseExpandedTerms(lowercaseExpandedTerms);
                }

            }
            return booleanQuery;
        }
        else
        {
            // Date does not support like
            if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.DATETIME)))
            {
                if (analysisMode == AnalysisMode.LIKE)
                {
                    throw new UnsupportedOperationException("Wild cards are not supported for the datetime type");
                }
            }

            // Sort and id is only special for MLText, text, and content
            // Dates are not special in this case
            Query query = subQueryBuilder.getQuery(expandedFieldName, queryText, AnalysisMode.DEFAULT, luceneFunction);
            if (query != null)
            {
                return query;
            }
            else
            {
                return new TermQuery(new Term("NO_TOKENS", "__"));
            }
        }
    }

    private void addLocaleSpecificUntokenisedMLOrTextAttribute(String sourceField, String queryText, SubQuery subQueryBuilder, AnalysisMode analysisMode,
            LuceneFunction luceneFunction, BooleanQuery booleanQuery, MLAnalysisMode mlAnalysisMode, Locale locale, String actualField) throws ParseException
    {

        String termText = queryText;
        if (locale.toString().length() > 0)
        {
            termText = "{" + locale + "}" + queryText;
        }
        Query subQuery = subQueryBuilder.getQuery(actualField, termText, analysisMode, luceneFunction);
        booleanQuery.add(subQuery, Occur.SHOULD);

        if (booleanQuery.getClauses().length == 0)
        {
            booleanQuery.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
        }
    }

    private void addLocaleSpecificTokenisedMLOrTextAttribute(String queryText, SubQuery subQueryBuilder, AnalysisMode analysisMode, LuceneFunction luceneFunction,
            BooleanQuery booleanQuery, Locale locale, String actualField) throws ParseException
    {
        StringBuilder builder = new StringBuilder(queryText.length() + 10);
        builder.append("\u0000").append(locale.toString()).append("\u0000").append(queryText);
        Query subQuery = subQueryBuilder.getQuery(actualField, builder.toString(), analysisMode, luceneFunction);
        if (subQuery != null)
        {
            booleanQuery.add(subQuery, Occur.SHOULD);
        }
        else
        {
            booleanQuery.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
        }
    }

    private Query functionQueryBuilder(String expandedFieldName, QName propertyQName, PropertyDefinition propertyDef, IndexTokenisationMode tokenisationMode, String queryText,
            LuceneFunction luceneFunction) throws ParseException
    {

        // Mime type
        if (expandedFieldName.endsWith(".mimetype"))
        {
            if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
            {
                throw new UnsupportedOperationException("Lucene Function");
            }

        }
        else if (expandedFieldName.endsWith(".size"))
        {
            if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
            {
                throw new UnsupportedOperationException("Lucene Function");
            }

        }
        else if (expandedFieldName.endsWith(".locale"))
        {
            if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
            {
                throw new UnsupportedOperationException("Lucene Function");
            }

        }

        // Already in expanded form

        // ML

        if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT)))
        {
            // Build a sub query for each locale and or the results together - the analysis will take care of
            // cross language matching for each entry
            BooleanQuery booleanQuery = new BooleanQuery();
            MLAnalysisMode mlAnalysisMode = searchParameters.getMlAnalaysisMode() == null ? config.getDefaultMLSearchAnalysisMode() : searchParameters.getMlAnalaysisMode();
            List<Locale> locales = searchParameters.getLocales();
            List<Locale> expandedLocales = new ArrayList<Locale>();
            for (Locale locale : (((locales == null) || (locales.size() == 0)) ? Collections.singletonList(I18NUtil.getLocale()) : locales))
            {
                expandedLocales.addAll(MLAnalysisMode.getLocales(mlAnalysisMode, locale, false));
            }
            for (Locale locale : (((expandedLocales == null) || (expandedLocales.size() == 0)) ? Collections.singletonList(I18NUtil.getLocale()) : expandedLocales))
            {
                String mlFieldName = expandedFieldName;

                if (tokenisationMode == IndexTokenisationMode.BOTH)
                {
                    mlFieldName = mlFieldName + "." + locale + ".sort";
                }

                addLocaleSpecificUntokenisedMLOrTextFunction(expandedFieldName, queryText, luceneFunction, booleanQuery, mlAnalysisMode, locale, mlFieldName);

            }
            return booleanQuery;
        }
        // Content
        else if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
        {
            throw new UnsupportedOperationException("Lucene functions not supported for content");
        }
        else if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.TEXT)))
        {
            if (propertyQName.equals(ContentModel.PROP_USER_USERNAME) || propertyQName.equals(ContentModel.PROP_USERNAME) || propertyQName.equals(ContentModel.PROP_AUTHORITY_NAME))
            {
                throw new UnsupportedOperationException("Functions are not supported agaisnt special text fields");
            }

            BooleanQuery booleanQuery = new BooleanQuery();
            MLAnalysisMode mlAnalysisMode = searchParameters.getMlAnalaysisMode() == null ? config.getDefaultMLSearchAnalysisMode() : searchParameters.getMlAnalaysisMode();
            List<Locale> locales = searchParameters.getLocales();
            List<Locale> expandedLocales = new ArrayList<Locale>();
            for (Locale locale : (((locales == null) || (locales.size() == 0)) ? Collections.singletonList(I18NUtil.getLocale()) : locales))
            {
                expandedLocales.addAll(MLAnalysisMode.getLocales(mlAnalysisMode, locale, false));
            }
            for (Locale locale : (((expandedLocales == null) || (expandedLocales.size() == 0)) ? Collections.singletonList(I18NUtil.getLocale()) : expandedLocales))
            {
                String textFieldName = expandedFieldName;

                if (tokenisationMode == IndexTokenisationMode.BOTH)
                {
                    textFieldName = textFieldName + "." + locale + ".sort";
                }

                addLocaleSpecificUntokenisedMLOrTextFunction(expandedFieldName, queryText, luceneFunction, booleanQuery, mlAnalysisMode, locale, textFieldName);

            }
            return booleanQuery;
        }
        else
        {
            throw new UnsupportedOperationException("Lucene Function");
        }
    }

    private void addLocaleSpecificUntokenisedMLOrTextFunction(String expandedFieldName, String queryText, LuceneFunction luceneFunction, BooleanQuery booleanQuery,
            MLAnalysisMode mlAnalysisMode, Locale locale, String textFieldName)
    {
        String termText = queryText;
        if (locale.toString().length() > 0)
        {
            termText = "{" + locale + "}" + queryText;
        }
        Query subQuery = buildFunctionQuery(textFieldName, termText, luceneFunction);
        booleanQuery.add(subQuery, Occur.SHOULD);

        if (booleanQuery.getClauses().length == 0)
        {
            booleanQuery.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
        }
    }

    private Query buildFunctionQuery(String expandedFieldName, String termText, LuceneFunction luceneFunction)
    {
        String testText = termText;
        if (termText.startsWith("{"))
        {
            int index = termText.indexOf("}");
            testText = termText.substring(index + 1);
        }
        switch (luceneFunction)
        {
        case LOWER:
            if (testText.equals(testText.toLowerCase()))
            {
                return new CaseInsensitiveFieldQuery(new Term(expandedFieldName, termText));
            }
            else
            {
                // No match
                return new TermQuery(new Term("NO_TOKENS", "__"));
            }
        case UPPER:
            if (testText.equals(testText.toUpperCase()))
            {
                return new CaseInsensitiveFieldQuery(new Term(expandedFieldName, termText));
            }
            else
            {
                // No match
                return new TermQuery(new Term("NO_TOKENS", "__"));
            }
        default:
            throw new UnsupportedOperationException("Unsupported Lucene Function " + luceneFunction);

        }
    }

    public static void main(String[] args) throws ParseException, java.text.ParseException
    {
        Query query;

        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        SimpleDateFormat df = CachingDateFormat.getDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", false);

        Date date = df.parse("2007-11-30T22:58:58.998");
        System.out.println(date);
        start.setTime(date);
        System.out.println(start);

        date = df.parse("2008-01-01T03:00:01.002");
        System.out.println(date);
        end.setTime(date);
        System.out.println(end);

        // start.set(Calendar.YEAR, start.getMinimum(Calendar.YEAR));
        // start.set(Calendar.DAY_OF_YEAR, start.getMinimum(Calendar.DAY_OF_YEAR));
        // start.set(Calendar.HOUR_OF_DAY, start.getMinimum(Calendar.HOUR_OF_DAY));
        // start.set(Calendar.MINUTE, start.getMinimum(Calendar.MINUTE));
        // start.set(Calendar.SECOND, start.getMinimum(Calendar.SECOND));
        // start.set(Calendar.MILLISECOND, start.getMinimum(Calendar.MILLISECOND));
        LuceneQueryParser lqp = new LuceneQueryParser(null, null);
        query = lqp.buildDateTimeRange("TEST", start, end, false, false);
        System.out.println("Query is " + query);
    }

    @Override
    public LuceneAnalyser getAnalyzer()
    {
        return luceneAnalyser;
    }

}
