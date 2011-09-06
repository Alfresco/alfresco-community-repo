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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.repo.dictionary.IndexTokenisationMode;
import org.alfresco.repo.search.EmptyResultSet;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.search.impl.lucene.analysis.DateTimeAnalyser;
import org.alfresco.repo.search.impl.parsers.AlfrescoFunctionEvaluationContext;
import org.alfresco.repo.search.impl.parsers.FTSParser;
import org.alfresco.repo.search.impl.parsers.FTSQueryParser;
import org.alfresco.repo.search.impl.querymodel.Argument;
import org.alfresco.repo.search.impl.querymodel.Column;
import org.alfresco.repo.search.impl.querymodel.Constraint;
import org.alfresco.repo.search.impl.querymodel.Function;
import org.alfresco.repo.search.impl.querymodel.Order;
import org.alfresco.repo.search.impl.querymodel.Ordering;
import org.alfresco.repo.search.impl.querymodel.QueryEngine;
import org.alfresco.repo.search.impl.querymodel.QueryEngineResults;
import org.alfresco.repo.search.impl.querymodel.QueryModelFactory;
import org.alfresco.repo.search.impl.querymodel.QueryOptions;
import org.alfresco.repo.search.impl.querymodel.QueryOptions.Connective;
import org.alfresco.repo.search.impl.querymodel.impl.functions.PropertyAccessor;
import org.alfresco.repo.search.impl.querymodel.impl.functions.Score;
import org.alfresco.repo.search.impl.querymodel.impl.lucene.LuceneOrdering;
import org.alfresco.repo.search.results.SortedResultSet;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition;
import org.alfresco.service.cmr.search.SearchParameters.SortDefinition.SortType;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser.Operator;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

/**
 * Alfresco FTS Query language support
 * 
 * @author andyh
 */
public class LuceneAlfrescoLuceneQueryLanguage extends AbstractLuceneQueryLanguage
{
    static Log s_logger = LogFactory.getLog(LuceneAlfrescoLuceneQueryLanguage.class);

    public LuceneAlfrescoLuceneQueryLanguage()
    {
        this.setName(SearchService.LANGUAGE_LUCENE);
    }
    
    public ResultSet executeQuery(SearchParameters searchParameters, ADMLuceneSearcherImpl admLuceneSearcher)
    {
        try
        {

            Operator defaultOperator;
            if (searchParameters.getDefaultOperator() == SearchParameters.AND)
            {
                defaultOperator = LuceneQueryParser.AND_OPERATOR;
            }
            else
            {
                defaultOperator = LuceneQueryParser.OR_OPERATOR;
            }

            ClosingIndexSearcher searcher = admLuceneSearcher.getClosingIndexSearcher();
            Query query = LuceneQueryParser.parse(searchParameters.getQuery(), searchParameters.getDefaultFieldName(), new LuceneAnalyser(admLuceneSearcher.getDictionaryService(),
                    searchParameters.getMlAnalaysisMode() == null ? admLuceneSearcher.getLuceneConfig().getDefaultMLSearchAnalysisMode() : searchParameters.getMlAnalaysisMode()),
                    admLuceneSearcher.getNamespacePrefixResolver(), admLuceneSearcher.getDictionaryService(), admLuceneSearcher.getTenantService(), defaultOperator, searchParameters, admLuceneSearcher.getLuceneConfig().getDefaultMLSearchAnalysisMode(), searcher.getIndexReader());
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Query is " + query.toString());
            }
            if (searcher == null)
            {
                // no index return an empty result set
                return new EmptyResultSet();
            }

            Hits hits;

            boolean requiresPostSort = false;
            if (searchParameters.getSortDefinitions().size() > 0)
            {
                int index = 0;
                SortField[] fields = new SortField[searchParameters.getSortDefinitions().size()];
                for (SearchParameters.SortDefinition sd : searchParameters.getSortDefinitions())
                {
                    switch (sd.getSortType())
                    {
                    case FIELD:
                        Locale sortLocale = admLuceneSearcher.getLocale(searchParameters);
                        String field = sd.getField();
                        if (field.startsWith("@"))
                        {
                            field = admLuceneSearcher.expandAttributeFieldName(field);
                            PropertyDefinition propertyDef = admLuceneSearcher.getDictionaryService().getProperty(QName.createQName(field.substring(1)));

                            if(propertyDef == null)
                            {   
                                if(field.endsWith(".size"))
                                {
                                    propertyDef = admLuceneSearcher.getDictionaryService().getProperty(QName.createQName(field.substring(1, field.length()-5)));
                                    if (!propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
                                    {
                                        throw new SearcherException("Order for .size only supported on content properties");
                                    }
                                }
                                else if (field.endsWith(".mimetype"))
                                {
                                    propertyDef = admLuceneSearcher.getDictionaryService().getProperty(QName.createQName(field.substring(1, field.length()-9)));
                                    if (!propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
                                    {
                                        throw new SearcherException("Order for .mimetype only supported on content properties");
                                    }
                                }
                                else
                                {
                                    // nothing
                                }
                            }
                            else
                            {
                                if (propertyDef.getDataType().getName().equals(DataTypeDefinition.CONTENT))
                                {
                                    throw new SearcherException("Order on content properties is not curently supported");
                                }
                                
                                else if (propertyDef.getDataType().getName().equals(DataTypeDefinition.TEXT))
                                {
                                    if(propertyDef.getIndexTokenisationMode() == IndexTokenisationMode.FALSE)
                                    {
                                        // use field as is
                                    }
                                    else
                                    {

                                        String noLocalField = field+".no_locale";
                                        for (Object current : searcher.getIndexReader().getFieldNames(FieldOption.INDEXED))
                                        {
                                            String currentString = (String) current;
                                            if (currentString.equals(noLocalField))
                                            {
                                                field = noLocalField;
                                            }
                                        }
                                        
                                        if(!field.endsWith(".no_locale"))
                                        {
                                            field = admLuceneSearcher.findSortField(searchParameters, searcher, field, sortLocale);
                                        }
                                    }
                                }
                                
                                else if (propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT))
                                {
                                  
                                    field = admLuceneSearcher.findSortField(searchParameters, searcher, field, sortLocale);

                                }
                                else if (propertyDef.getDataType().getName().equals(DataTypeDefinition.DATETIME))
                                {
                                    DataTypeDefinition dataType = propertyDef.getDataType();
                                    String analyserClassName = propertyDef.resolveAnalyserClassName();
                                    if (analyserClassName.equals(DateTimeAnalyser.class.getCanonicalName()))
                                    {
                                        switch (propertyDef.getIndexTokenisationMode())
                                        {
                                        case TRUE:
                                            requiresPostSort = true;
                                            break;
                                        case BOTH:
                                            field = field + ".sort";
                                            break;
                                        case FALSE:
                                            // Should be able to sort on actual field OK
                                            break;
                                        }
                                    }
                                    else
                                    {
                                        requiresPostSort = true;
                                    }
                                }
                            }
                        }
                        
                        if (LuceneUtils.fieldHasTerm(searcher.getReader(), field))
                        {
                            fields[index++] = new SortField(field, sortLocale, !sd.isAscending());
                        }
                        else
                        {
                            fields[index++] = new SortField(null, SortField.DOC, !sd.isAscending());
                        }
                        break;
                    case DOCUMENT:
                        fields[index++] = new SortField(null, SortField.DOC, !sd.isAscending());
                        break;
                    case SCORE:
                        fields[index++] = new SortField(null, SortField.SCORE, !sd.isAscending());
                        break;
                    }

                }
                hits = searcher.search(query, new Sort(fields));

            }
            else
            {
                hits = searcher.search(query);
            }

            ResultSet rs = new LuceneResultSet(hits, searcher, admLuceneSearcher.getNodeService(), admLuceneSearcher.getTenantService(), searchParameters, admLuceneSearcher.getLuceneConfig());
            rs = new PagingLuceneResultSet(rs, searchParameters, admLuceneSearcher.getNodeService());
            if (admLuceneSearcher.getLuceneConfig().getPostSortDateTime() && requiresPostSort)
            {
                ResultSet sorted = new SortedResultSet(rs, admLuceneSearcher.getNodeService(), searchParameters, admLuceneSearcher.getNamespacePrefixResolver());
                return sorted;
            }
            else
            {
                return rs;
            }
        }
        catch (ParseException e)
        {
            throw new SearcherException("Failed to parse query: " + searchParameters.getQuery(), e);
        }
        catch (IOException e)
        {
            throw new SearcherException("IO exception during search", e);
        }
    }

}
