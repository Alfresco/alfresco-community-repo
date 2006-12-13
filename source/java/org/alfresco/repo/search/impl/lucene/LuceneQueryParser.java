/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.search.impl.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.lucene.query.PathQuery;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
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

    private List<Locale> locales;

    /**
     * Parses a query string, returning a {@link org.apache.lucene.search.Query}.
     * 
     * @param query
     *            the query string to be parsed.
     * @param field
     *            the default field for query terms.
     * @param analyzer
     *            used to find terms in the query text.
     * @throws ParseException
     *             if the parsing fails
     */
    static public Query parse(String query, String field, Analyzer analyzer,
            NamespacePrefixResolver namespacePrefixResolver, DictionaryService dictionaryService,
            Operator defaultOperator, List<Locale> locales) throws ParseException
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Using Alfresco Lucene Query Parser for query: " + query);
        }
        LuceneQueryParser parser = new LuceneQueryParser(field, analyzer);
        parser.setDefaultOperator(defaultOperator);
        parser.setNamespacePrefixResolver(namespacePrefixResolver);
        parser.setDictionaryService(dictionaryService);
        parser.setLocales(locales);
        // TODO: Apply locale contstraints at the top level if required for the non ML doc types.
        return parser.parse(query);
    }

    private void setLocales(List<Locale> locales)
    {
        this.locales = locales;
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
                Collection<QName> contentAttributes = dictionaryService.getAllProperties(DataTypeDefinition.CONTENT);
                BooleanQuery query = new BooleanQuery();
                for (QName qname : contentAttributes)
                {
                    // The super implementation will create phrase queries etc if required
                    Query part = super.getFieldQuery("@" + qname.toString(), queryText);
                    query.add(part, Occur.SHOULD);
                }
                return query;
            }
            else if (field.equals("ID"))
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
                    booleanQuery.add(termQuery, Occur.SHOULD);
                }
                return booleanQuery;
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
                    booleanQuery.add(termQuery, Occur.SHOULD);
                }
                return booleanQuery;
            }
            else if (field.startsWith("@"))
            {
                // Expand prefixes

                String expandedFieldName = field;
                // Check for any prefixes and expand to the full uri
                if (field.charAt(1) != '{')
                {
                    int colonPosition = field.indexOf(':');
                    if (colonPosition == -1)
                    {
                        // use the default namespace
                        expandedFieldName = "@{"
                                + namespacePrefixResolver.getNamespaceURI("") + "}" + field.substring(1);
                    }
                    else
                    {
                        // find the prefix
                        expandedFieldName = "@{"
                                + namespacePrefixResolver.getNamespaceURI(field.substring(1, colonPosition)) + "}"
                                + field.substring(colonPosition + 1);
                    }
                }

                // Mime type
                if (expandedFieldName.endsWith(".mimetype"))
                {
                    QName propertyQName = QName.createQName(expandedFieldName.substring(1,
                            expandedFieldName.length() - 9));
                    PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
                    if ((propertyDef != null)
                            && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
                    {
                        return super.getFieldQuery(expandedFieldName, queryText);
                    }

                }
                else if (expandedFieldName.endsWith(".size"))
                {
                    QName propertyQName = QName.createQName(expandedFieldName.substring(1,
                            expandedFieldName.length() - 5));
                    PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
                    if ((propertyDef != null)
                            && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
                    {
                        return super.getFieldQuery(expandedFieldName, queryText);
                    }

                }
                else if (expandedFieldName.endsWith(".locale"))
                {
                    QName propertyQName = QName.createQName(expandedFieldName.substring(1,
                            expandedFieldName.length() - 7));
                    PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
                    if ((propertyDef != null)
                            && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
                    {
                        return super.getFieldQuery(expandedFieldName, queryText);
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
                    for (Locale locale : (((locales == null) || (locales.size() == 0)) ? Collections
                            .singletonList(I18NUtil.getLocale()) : locales))
                    {
                        StringBuilder builder = new StringBuilder(queryText.length() + 10);
                        builder.append("\u0000").append(locale.toString()).append("\u0000").append(queryText);
                        Query subQuery = super.getFieldQuery(expandedFieldName, builder.toString());
                        booleanQuery.add(subQuery, Occur.SHOULD);
                    }
                    return booleanQuery;
                }
                else
                {
                    return super.getFieldQuery(expandedFieldName, queryText);
                }

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
            return new RangeQuery(new Term(fieldName, getToken(fieldName, part1)), new Term(fieldName, getToken(
                    fieldName, part2)), inclusive);

        }
        else
        {
            return super.getRangeQuery(field, part1, part2, inclusive);
        }

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
            // Expand prefixes

            String expandedFieldName = field;
            // Check for any prefixes and expand to the full uri
            if (field.charAt(1) != '{')
            {
                int colonPosition = field.indexOf(':');
                if (colonPosition == -1)
                {
                    // use the default namespace
                    expandedFieldName = "@{" + namespacePrefixResolver.getNamespaceURI("") + "}" + field.substring(1);
                }
                else
                {
                    // find the prefix
                    expandedFieldName = "@{"
                            + namespacePrefixResolver.getNamespaceURI(field.substring(1, colonPosition)) + "}"
                            + field.substring(colonPosition + 1);
                }
            }

            // Mime type
            if (expandedFieldName.endsWith(".mimetype"))
            {
                QName propertyQName = QName.createQName(expandedFieldName.substring(1, expandedFieldName.length() - 9));
                PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
                if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
                {
                    return super.getPrefixQuery(expandedFieldName, termStr);
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
                for (Locale locale : (((locales == null) || (locales.size() == 0)) ? Collections.singletonList(I18NUtil
                        .getLocale()) : locales))
                {
                    StringBuilder builder = new StringBuilder(termStr.length() + 10);
                    builder.append("\u0000").append(locale.toString()).append("\u0000").append(termStr);
                    Query subQuery = super.getPrefixQuery(expandedFieldName, builder.toString());
                    booleanQuery.add(subQuery, Occur.SHOULD);
                }
                return booleanQuery;
            }
            else
            {
                return super.getPrefixQuery(expandedFieldName, termStr);
            }

        }

        else if (field.equals("TEXT"))
        {
            Collection<QName> contentAttributes = dictionaryService.getAllProperties(DataTypeDefinition.CONTENT);
            BooleanQuery query = new BooleanQuery();
            for (QName qname : contentAttributes)
            {
                // The super implementation will create phrase queries etc if required
                Query part = super.getPrefixQuery("@" + qname.toString(), termStr);
                query.add(part, Occur.SHOULD);
            }
            return query;

        }
        else
        {
            return super.getFieldQuery(field, termStr);
        }
    }

    @Override
    protected Query getWildcardQuery(String field, String termStr) throws ParseException
    {
        if (field.startsWith("@"))
        {
            // Expand prefixes

            String expandedFieldName = field;
            // Check for any prefixes and expand to the full uri
            if (field.charAt(1) != '{')
            {
                int colonPosition = field.indexOf(':');
                if (colonPosition == -1)
                {
                    // use the default namespace
                    expandedFieldName = "@{" + namespacePrefixResolver.getNamespaceURI("") + "}" + field.substring(1);
                }
                else
                {
                    // find the prefix
                    expandedFieldName = "@{"
                            + namespacePrefixResolver.getNamespaceURI(field.substring(1, colonPosition)) + "}"
                            + field.substring(colonPosition + 1);
                }
            }

            // Mime type
            if (expandedFieldName.endsWith(".mimetype"))
            {
                QName propertyQName = QName.createQName(expandedFieldName.substring(1, expandedFieldName.length() - 9));
                PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
                if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
                {
                    return super.getWildcardQuery(expandedFieldName, termStr);
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
                for (Locale locale : (((locales == null) || (locales.size() == 0)) ? Collections.singletonList(I18NUtil
                        .getLocale()) : locales))
                {
                    StringBuilder builder = new StringBuilder(termStr.length() + 10);
                    builder.append("\u0000").append(locale.toString()).append("\u0000").append(termStr);
                    Query subQuery = super.getWildcardQuery(expandedFieldName, builder.toString());
                    booleanQuery.add(subQuery, Occur.SHOULD);
                }
                return booleanQuery;
            }
            else
            {
                return super.getWildcardQuery(expandedFieldName, termStr);
            }

        }

        else if (field.equals("TEXT"))
        {
            Collection<QName> contentAttributes = dictionaryService.getAllProperties(DataTypeDefinition.CONTENT);
            BooleanQuery query = new BooleanQuery();
            for (QName qname : contentAttributes)
            {
                // The super implementation will create phrase queries etc if required
                Query part = super.getWildcardQuery("@" + qname.toString(), termStr);
                query.add(part, Occur.SHOULD);
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
            // Expand prefixes

            String expandedFieldName = field;
            // Check for any prefixes and expand to the full uri
            if (field.charAt(1) != '{')
            {
                int colonPosition = field.indexOf(':');
                if (colonPosition == -1)
                {
                    // use the default namespace
                    expandedFieldName = "@{" + namespacePrefixResolver.getNamespaceURI("") + "}" + field.substring(1);
                }
                else
                {
                    // find the prefix
                    expandedFieldName = "@{"
                            + namespacePrefixResolver.getNamespaceURI(field.substring(1, colonPosition)) + "}"
                            + field.substring(colonPosition + 1);
                }
            }

            // Mime type
            if (expandedFieldName.endsWith(".mimetype"))
            {
                QName propertyQName = QName.createQName(expandedFieldName.substring(1, expandedFieldName.length() - 9));
                PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
                if ((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
                {
                    return super.getFuzzyQuery(expandedFieldName, termStr, minSimilarity);
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
                for (Locale locale : (((locales == null) || (locales.size() == 0)) ? Collections.singletonList(I18NUtil
                        .getLocale()) : locales))
                {
                    StringBuilder builder = new StringBuilder(termStr.length() + 10);
                    builder.append("\u0000").append(locale.toString()).append("\u0000").append(termStr);
                    Query subQuery = super.getFuzzyQuery(expandedFieldName, builder.toString(), minSimilarity);
                    booleanQuery.add(subQuery, Occur.SHOULD);
                }
                return booleanQuery;
            }
            else
            {
                return super.getFuzzyQuery(expandedFieldName, termStr, minSimilarity);
            }

        }

        else if (field.equals("TEXT"))
        {
            Collection<QName> contentAttributes = dictionaryService.getAllProperties(DataTypeDefinition.CONTENT);
            BooleanQuery query = new BooleanQuery();
            for (QName qname : contentAttributes)
            {
                // The super implementation will create phrase queries etc if required
                Query part = super.getFuzzyQuery("@" + qname.toString(), termStr, minSimilarity);
                query.add(part, Occur.SHOULD);
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

}
