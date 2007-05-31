/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.search.impl.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.lucene.analysis.MLTokenDuplicator;
import org.alfresco.repo.search.impl.lucene.analysis.VerbatimAnalyser;
import org.alfresco.repo.search.impl.lucene.query.PathQuery;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.saxpath.SAXPathException;

import com.werken.saxpath.XPathReader;

public class LuceneQueryParser extends QueryParser
{
    private static Logger s_logger = Logger.getLogger(LuceneQueryParser.class);

    private NamespacePrefixResolver namespacePrefixResolver;

    private DictionaryService dictionaryService;

    private SearchParameters searchParameters;

    private LuceneConfig config;

    /**
     * Parses a query string, returning a {@link org.apache.lucene.search.Query}.
     * 
     * @param query
     *            the query string to be parsed.
     * @param field
     *            the default field for query terms.
     * @param analyzer
     *            used to find terms in the query text.
     * @param config
     * @throws ParseException
     *             if the parsing fails
     */
    static public Query parse(String query, String field, Analyzer analyzer,
            NamespacePrefixResolver namespacePrefixResolver, DictionaryService dictionaryService,
            Operator defaultOperator, SearchParameters searchParameters, LuceneConfig config) throws ParseException
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Using Alfresco Lucene Query Parser for query: " + query);
        }
        LuceneQueryParser parser = new LuceneQueryParser(field, analyzer);
        parser.setDefaultOperator(defaultOperator);
        parser.setNamespacePrefixResolver(namespacePrefixResolver);
        parser.setDictionaryService(dictionaryService);
        parser.setSearchParameters(searchParameters);
        parser.setLuceneConfig(config);
        // TODO: Apply locale contstraints at the top level if required for the non ML doc types.
        Query result = parser.parse(query);
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Query " + query + "                             is\n\t" + result.toString());
        }
        return result;
    }

    private void setLuceneConfig(LuceneConfig config)
    {
        this.config = config;
    }

    private void setSearchParameters(SearchParameters searchParameters)
    {
        this.searchParameters = searchParameters;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver namespacePrefixResolver)
    {
        this.namespacePrefixResolver = namespacePrefixResolver;
    }

    public LuceneQueryParser(String arg0, Analyzer arg1)
    {
        super(arg0, arg1);
    }

    public LuceneQueryParser(CharStream arg0)
    {
        super(arg0);
    }

    public LuceneQueryParser(QueryParserTokenManager arg0)
    {
        super(arg0);
    }

    protected Query getFieldQuery(String field, String queryText) throws ParseException
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
                    Collection<QName> contentAttributes = dictionaryService
                            .getAllProperties(DataTypeDefinition.CONTENT);
                    BooleanQuery query = new BooleanQuery();
                    for (QName qname : contentAttributes)
                    {
                        // The super implementation will create phrase queries etc if required
                        Query part = getFieldQuery("@" + qname.toString(), queryText);
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
                        Query part = getFieldQuery(fieldName, queryText);
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
                TermQuery termQuery = new TermQuery(new Term(field, queryText));
                return termQuery;
            }
            else if (field.equals("PRIMARYPARENT"))
            {
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
            else if (field.equals("TYPE"))
            {
                TypeDefinition target;
                if (queryText.startsWith("{"))
                {
                    target = dictionaryService.getType(QName.createQName(queryText));
                }
                else
                {
                    int colonPosition = queryText.indexOf(':');
                    if (colonPosition == -1)
                    {
                        // use the default namespace
                        target = dictionaryService.getType(QName.createQName(namespacePrefixResolver
                                .getNamespaceURI(""), queryText));
                    }
                    else
                    {
                        // find the prefix
                        target = dictionaryService.getType(QName.createQName(namespacePrefixResolver
                                .getNamespaceURI(queryText.substring(0, colonPosition)), queryText
                                .substring(colonPosition + 1)));
                    }
                }
                if (target == null)
                {
                    throw new SearcherException("Invalid type: " + queryText);
                }
                QName targetQName = target.getName();
                HashSet<QName> subclasses = new HashSet<QName>();
                for (QName classRef : dictionaryService.getAllTypes())
                {
                    TypeDefinition current = dictionaryService.getType(classRef);
                    while ((current != null) && !current.getName().equals(targetQName))
                    {
                        current = (current.getParentName() == null) ? null : dictionaryService.getType(current
                                .getParentName());
                    }
                    if (current != null)
                    {
                        subclasses.add(classRef);
                    }
                }
                BooleanQuery booleanQuery = new BooleanQuery();
                for (QName qname : subclasses)
                {
                    TermQuery termQuery = new TermQuery(new Term(field, qname.toString()));
                    if (termQuery != null)
                    {
                        booleanQuery.add(termQuery, Occur.SHOULD);
                    }
                }
                return booleanQuery;
            }
            else if (field.equals("EXACTTYPE"))
            {
                TypeDefinition target;
                if (queryText.startsWith("{"))
                {
                    target = dictionaryService.getType(QName.createQName(queryText));
                }
                else
                {
                    int colonPosition = queryText.indexOf(':');
                    if (colonPosition == -1)
                    {
                        // use the default namespace
                        target = dictionaryService.getType(QName.createQName(namespacePrefixResolver
                                .getNamespaceURI(""), queryText));
                    }
                    else
                    {
                        // find the prefix
                        target = dictionaryService.getType(QName.createQName(namespacePrefixResolver
                                .getNamespaceURI(queryText.substring(0, colonPosition)), queryText
                                .substring(colonPosition + 1)));
                    }
                }
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
                AspectDefinition target;
                if (queryText.startsWith("{"))
                {
                    target = dictionaryService.getAspect(QName.createQName(queryText));
                }
                else
                {
                    int colonPosition = queryText.indexOf(':');
                    if (colonPosition == -1)
                    {
                        // use the default namespace
                        target = dictionaryService.getAspect(QName.createQName(namespacePrefixResolver
                                .getNamespaceURI(""), queryText));
                    }
                    else
                    {
                        // find the prefix
                        target = dictionaryService.getAspect(QName.createQName(namespacePrefixResolver
                                .getNamespaceURI(queryText.substring(0, colonPosition)), queryText
                                .substring(colonPosition + 1)));
                    }
                }

                QName targetQName = target.getName();
                HashSet<QName> subclasses = new HashSet<QName>();
                for (QName classRef : dictionaryService.getAllAspects())
                {
                    AspectDefinition current = dictionaryService.getAspect(classRef);
                    while ((current != null) && !current.getName().equals(targetQName))
                    {
                        current = (current.getParentName() == null) ? null : dictionaryService.getAspect(current
                                .getParentName());
                    }
                    if (current != null)
                    {
                        subclasses.add(classRef);
                    }
                }

                BooleanQuery booleanQuery = new BooleanQuery();
                for (QName qname : subclasses)
                {
                    TermQuery termQuery = new TermQuery(new Term(field, qname.toString()));
                    if (termQuery != null)
                    {
                        booleanQuery.add(termQuery, Occur.SHOULD);
                    }
                }
                return booleanQuery;
            }
            else if (field.equals("EXACTASPECT"))
            {
                AspectDefinition target;
                if (queryText.startsWith("{"))
                {
                    target = dictionaryService.getAspect(QName.createQName(queryText));
                }
                else
                {
                    int colonPosition = queryText.indexOf(':');
                    if (colonPosition == -1)
                    {
                        // use the default namespace
                        target = dictionaryService.getAspect(QName.createQName(namespacePrefixResolver
                                .getNamespaceURI(""), queryText));
                    }
                    else
                    {
                        // find the prefix
                        target = dictionaryService.getAspect(QName.createQName(namespacePrefixResolver
                                .getNamespaceURI(queryText.substring(0, colonPosition)), queryText
                                .substring(colonPosition + 1)));
                    }
                }

                QName targetQName = target.getName();
                TermQuery termQuery = new TermQuery(new Term("ASPECT", targetQName.toString()));

                return termQuery;
            }
            else if (field.startsWith("@"))
            {
                Query query = attributeQueryBuilder(field, queryText, new FieldQuery(), true);
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
                        Query part = getFieldQuery("@" + qname.toString(), queryText);
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
                        Query part = getFieldQuery(fieldName, queryText);
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
            else if (field.equals("ISNULL"))
            {
                String qnameString = expandFieldName(queryText);
                QName qname = QName.createQName(qnameString);
                PropertyDefinition pd = dictionaryService.getProperty(qname);
                if (pd != null)
                {
                    QName container = pd.getContainerClass().getName();
                    BooleanQuery query = new BooleanQuery();
                    Query typeQuery = getFieldQuery("TYPE", container.toString());
                    Query presenceQuery = getWildcardQuery("@" + qname.toString(), "*");
                    if ((typeQuery != null) && (presenceQuery != null))
                    {
                        query.add(typeQuery, Occur.MUST);
                        query.add(presenceQuery, Occur.MUST_NOT);
                    }
                    return query;
                }
                else
                {
                    return super.getFieldQuery(field, queryText);
                }

            }
            else if (field.equals("ISNOTNULL"))
            {
                String qnameString = expandFieldName(queryText);
                QName qname = QName.createQName(qnameString);
                PropertyDefinition pd = dictionaryService.getProperty(qname);
                if (pd != null)
                {
                    QName container = pd.getContainerClass().getName();
                    BooleanQuery query = new BooleanQuery();
                    Query typeQuery = getFieldQuery("TYPE", container.toString());
                    Query presenceQuery = getWildcardQuery("@" + qname.toString(), "*");
                    if ((typeQuery != null) && (presenceQuery != null))
                    {
                        query.add(typeQuery, Occur.MUST);
                        query.add(presenceQuery, Occur.MUST);
                    }
                    return query;
                }
                else
                {
                    return super.getFieldQuery(field, queryText);
                }

            }
            else if (dictionaryService.getDataType(QName.createQName(expandFieldName(field))) != null)
            {
                Collection<QName> contentAttributes = dictionaryService.getAllProperties(dictionaryService.getDataType(
                        QName.createQName(expandFieldName(field))).getName());
                BooleanQuery query = new BooleanQuery();
                for (QName qname : contentAttributes)
                {
                    // The super implementation will create phrase queries etc if required
                    Query part = getFieldQuery("@" + qname.toString(), queryText);
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
                return super.getFieldQuery(field, queryText);
            }

        }
        catch (SAXPathException e)
        {
            throw new ParseException("Failed to parse XPath...\n" + e.getMessage());
        }

    }

    /**
     * @exception ParseException
     *                throw in overridden method to disallow
     */
    protected Query getRangeQuery(String field, String part1, String part2, boolean inclusive) throws ParseException
    {
        if (field.startsWith("@"))
        {
            String fieldName = expandAttributeFieldName(field);
            return new RangeQuery(new Term(fieldName, getToken(fieldName, part1)), new Term(fieldName, getToken(
                    fieldName, part2)), inclusive);

        }
        else
        {
            return super.getRangeQuery(field, part1, part2, inclusive);
        }

    }

    private String expandAttributeFieldName(String field)
    {
        String fieldName = field;
        // Check for any prefixes and expand to the full uri
        if (field.charAt(1) != '{')
        {
            int colonPosition = field.indexOf(':');
            if (colonPosition == -1)
            {
                // use the default namespace
                fieldName = "@{" + namespacePrefixResolver.getNamespaceURI("") + "}" + field.substring(1);
            }
            else
            {
                // find the prefix
                fieldName = "@{"
                        + namespacePrefixResolver.getNamespaceURI(field.substring(1, colonPosition)) + "}"
                        + field.substring(colonPosition + 1);
            }
        }
        return fieldName;
    }

    private String expandFieldName(String field)
    {
        String fieldName = field;
        // Check for any prefixes and expand to the full uri
        if (field.charAt(0) != '{')
        {
            int colonPosition = field.indexOf(':');
            if (colonPosition == -1)
            {
                // use the default namespace
                fieldName = "{" + namespacePrefixResolver.getNamespaceURI("") + "}" + field;
            }
            else
            {
                // find the prefix
                fieldName = "{"
                        + namespacePrefixResolver.getNamespaceURI(field.substring(0, colonPosition)) + "}"
                        + field.substring(colonPosition + 1);
            }
        }
        return fieldName;
    }

    private String getToken(String field, String value)
    {
        TokenStream source = analyzer.tokenStream(field, new StringReader(value));
        org.apache.lucene.analysis.Token t;
        String tokenised = null;

        while (true)
        {
            try
            {
                t = source.next();
            }
            catch (IOException e)
            {
                t = null;
            }
            if (t == null)
                break;
            tokenised = t.termText();
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
    protected Query getPrefixQuery(String field, String termStr) throws ParseException
    {
        if (field.startsWith("@"))
        {
            return attributeQueryBuilder(field, termStr, new PrefixQuery(), false);
        }
        else if (field.equals("TEXT"))
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
            return super.getPrefixQuery(field, termStr);
        }
    }

    @Override
    protected Query getWildcardQuery(String field, String termStr) throws ParseException
    {
        if (field.startsWith("@"))
        {
            return attributeQueryBuilder(field, termStr, new WildcardQuery(), false);
        }

        else if (field.equals("TEXT"))
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
            return super.getWildcardQuery(field, termStr);
        }
    }

    @Override
    protected Query getFuzzyQuery(String field, String termStr, float minSimilarity) throws ParseException
    {
        if (field.startsWith("@"))
        {
            return attributeQueryBuilder(field, termStr, new FuzzyQuery(minSimilarity), false);
        }

        else if (field.equals("TEXT"))
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
            return super.getFuzzyQuery(field, termStr, minSimilarity);
        }
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public Query getSuperFieldQuery(String field, String queryText) throws ParseException
    {
        return super.getFieldQuery(field, queryText);
    }

    public Query getSuperFuzzyQuery(String field, String termStr, float minSimilarity) throws ParseException
    {
        return super.getFuzzyQuery(field, termStr, minSimilarity);
    }

    public Query getSuperPrefixQuery(String field, String termStr) throws ParseException
    {
        return super.getPrefixQuery(field, termStr);
    }

    public Query getSuperWildcardQuery(String field, String termStr) throws ParseException
    {
        return super.getWildcardQuery(field, termStr);
    }

    interface SubQuery
    {
        Query getQuery(String field, String queryText) throws ParseException;
    }

    class FieldQuery implements SubQuery
    {
        public Query getQuery(String field, String queryText) throws ParseException
        {
            return getSuperFieldQuery(field, queryText);
        }
    }

    class FuzzyQuery implements SubQuery
    {
        float minSimilarity;

        FuzzyQuery(float minSimilarity)
        {
            this.minSimilarity = minSimilarity;
        }

        public Query getQuery(String field, String termStr) throws ParseException
        {
            return getSuperFuzzyQuery(field, termStr, minSimilarity);
        }
    }

    class PrefixQuery implements SubQuery
    {
        public Query getQuery(String field, String termStr) throws ParseException
        {
            return getSuperPrefixQuery(field, termStr);
        }
    }

    class WildcardQuery implements SubQuery
    {
        public Query getQuery(String field, String termStr) throws ParseException
        {
            return getSuperWildcardQuery(field, termStr);
        }
    }

    private Query attributeQueryBuilder(String field, String queryText, SubQuery subQueryBuilder, boolean isAnalysed) throws ParseException
    {
        // Expand prefixes

        String expandedFieldName = expandAttributeFieldName(field);

        // Mime type
        if (expandedFieldName.endsWith(".mimetype"))
        {
            QName propertyQName = QName.createQName(expandedFieldName.substring(1, expandedFieldName.length() - 9));
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
            if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
            {
                return subQueryBuilder.getQuery(expandedFieldName, queryText);
            }

        }
        else if (expandedFieldName.endsWith(".size"))
        {
            QName propertyQName = QName.createQName(expandedFieldName.substring(1, expandedFieldName.length() - 5));
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
            if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
            {
                return subQueryBuilder.getQuery(expandedFieldName, queryText);
            }

        }
        else if (expandedFieldName.endsWith(".locale"))
        {
            QName propertyQName = QName.createQName(expandedFieldName.substring(1, expandedFieldName.length() - 7));
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
            if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
            {
                return subQueryBuilder.getQuery(expandedFieldName, queryText);
            }

        }

        // Already in expanded form

        // ML

        QName propertyQName = QName.createQName(expandedFieldName.substring(1));
        PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
        if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT)))
        {
            // Build a sub query for each locale and or the results together - the analysis will take care of
            // cross language matching for each entry
            BooleanQuery booleanQuery = new BooleanQuery();
            List<Locale> locales = searchParameters.getLocales();
            for (Locale locale : (((locales == null) || (locales.size() == 0)) ? Collections.singletonList(I18NUtil
                    .getLocale()) : locales))
            {
                
                if(isAnalysed)
                {
                    StringBuilder builder = new StringBuilder(queryText.length() + 10);
                    builder.append("\u0000").append(locale.toString()).append("\u0000").append(queryText);
                    Query subQuery = subQueryBuilder.getQuery(expandedFieldName, builder.toString());
                    if (subQuery != null)
                    {
                        booleanQuery.add(subQuery, Occur.SHOULD);
                    }
                    else
                    {
                        booleanQuery.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
                    }
                }
                else
                {
                    // analyse ml text
                    MLAnalysisMode analysisMode = searchParameters.getMlAnalaysisMode() == null ? config
                            .getDefaultMLSearchAnalysisMode() : searchParameters.getMlAnalaysisMode();
                    // Do the analysis here
                    VerbatimAnalyser vba = new VerbatimAnalyser(false);        
                    MLTokenDuplicator duplicator = new MLTokenDuplicator(vba.tokenStream(field, new StringReader(queryText)), locale, null, analysisMode);
                    Token t;
                    try
                    {
                        while( (t = duplicator.next()) != null)
                        {
                            Query subQuery = subQueryBuilder.getQuery(expandedFieldName, t.termText());
                            booleanQuery.add(subQuery, Occur.SHOULD);
                        }
                    }
                    catch (IOException e)
                    {
                       
                    }
                    if(booleanQuery.getClauses().length == 0)
                    {
                        booleanQuery.add(new TermQuery(new Term("NO_TOKENS", "__")), Occur.SHOULD);
                    }
                    
                }
                
            }
            return booleanQuery;
        }
        // Content
        else if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
        {
            // Build a sub query for each locale and or the results together -
            // - add an explicit condition for the locale

            MLAnalysisMode analysisMode = searchParameters.getMlAnalaysisMode() == null ? config
                    .getDefaultMLSearchAnalysisMode() : searchParameters.getMlAnalaysisMode();

            if (analysisMode.includesAll())
            {
                return subQueryBuilder.getQuery(expandedFieldName, queryText);
            }

            List<Locale> locales = searchParameters.getLocales();
            List<Locale> expandedLocales = new ArrayList<Locale>();
            for (Locale locale : (((locales == null) || (locales.size() == 0)) ? Collections.singletonList(I18NUtil
                    .getLocale()) : locales))
            {
                expandedLocales.addAll(MLAnalysisMode.getLocales(analysisMode, locale, true));
            }

            if (expandedLocales.size() > 0)
            {
                BooleanQuery booleanQuery = new BooleanQuery();
                Query contentQuery = subQueryBuilder.getQuery(expandedFieldName, queryText);
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
                Query query = subQueryBuilder.getQuery(expandedFieldName, queryText);
                if(query != null)
                {
                    return query;
                }
                else
                {
                    return new TermQuery(new Term("NO_TOKENS", "__"));
                }
            }

        }
        else
        {
            Query query = subQueryBuilder.getQuery(expandedFieldName, queryText);
            if(query != null)
            {
                return query;
            }
            else
            {
                return new TermQuery(new Term("NO_TOKENS", "__"));
            }
        }
    }

}
