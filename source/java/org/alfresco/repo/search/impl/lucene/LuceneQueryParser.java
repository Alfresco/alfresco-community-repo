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
import java.util.HashSet;

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
import org.saxpath.SAXPathException;

import com.werken.saxpath.XPathReader;

public class LuceneQueryParser extends QueryParser
{
    private static Logger s_logger = Logger.getLogger(LuceneQueryParser.class);

    private NamespacePrefixResolver namespacePrefixResolver;

    private DictionaryService dictionaryService;

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
            NamespacePrefixResolver namespacePrefixResolver, DictionaryService dictionaryService, int defaultOperator)
            throws ParseException
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Using Alfresco Lucene Query Parser for query: " + query);
        }
        LuceneQueryParser parser = new LuceneQueryParser(field, analyzer);
        parser.setOperator(defaultOperator);
        parser.setNamespacePrefixResolver(namespacePrefixResolver);
        parser.setDictionaryService(dictionaryService);
        return parser.parse(query);
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
                TypeDefinition target = dictionaryService.getType(QName.createQName(queryText));
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
                    booleanQuery.add(termQuery, false, false);
                }
                return booleanQuery;
            }
            else if (field.equals("ASPECT"))
            {
                AspectDefinition target = dictionaryService.getAspect(QName.createQName(queryText));
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
                    booleanQuery.add(termQuery, false, false);
                }
                return booleanQuery;
            }
            else if (field.startsWith("@"))
            {

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
                
                if(expandedFieldName.endsWith(".mimetype"))
                {
                    QName propertyQName = QName.createQName(expandedFieldName.substring(1, expandedFieldName.length()-9));
                    PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
                    if((propertyDef != null) && (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT)))
                    {
                        TermQuery termQuery = new TermQuery(new Term(expandedFieldName, queryText));
                        return termQuery;
                    }
                            
                }
                
                // Already in expanded form
                return super.getFieldQuery(expandedFieldName, queryText);
               

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

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

}
