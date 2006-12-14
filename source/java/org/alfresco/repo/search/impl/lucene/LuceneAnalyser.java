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

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.search.MLAnalysisMode;
import org.alfresco.repo.search.impl.lucene.analysis.AlfrescoStandardAnalyser;
import org.alfresco.repo.search.impl.lucene.analysis.LongAnalyser;
import org.alfresco.repo.search.impl.lucene.analysis.MLAnalayser;
import org.alfresco.repo.search.impl.lucene.analysis.PathAnalyser;
import org.alfresco.repo.search.impl.lucene.analysis.VerbatimAnalyser;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;

/**
 * Analyse properties according to the property definition. The default is to use the standard tokeniser. The tokeniser
 * should not have been called when indexing properties that require no tokenisation. (tokenise should be set to false
 * when adding the field to the document)
 * 
 * @author andyh
 */

public class LuceneAnalyser extends Analyzer
{
    // Dictinary service to look up analyser classes by data type and locale.
    private DictionaryService dictionaryService;

    // If all else fails a fall back analyser
    private Analyzer defaultAnalyser;

    // Cached analysers for non ML data types.
    private Map<String, Analyzer> analysers = new HashMap<String, Analyzer>();

    private MLAnalysisMode mlAlaysisMode;

    /**
     * Constructs with a default standard analyser
     * 
     * @param defaultAnalyzer
     *            Any fields not specifically defined to use a different analyzer will use the one provided here.
     */
    public LuceneAnalyser(DictionaryService dictionaryService, MLAnalysisMode mlAlaysisMode)
    {
        this(new AlfrescoStandardAnalyser());
        this.dictionaryService = dictionaryService;
        this.mlAlaysisMode = mlAlaysisMode;
    }

    /**
     * Constructs with default analyzer.
     * 
     * @param defaultAnalyzer
     *            Any fields not specifically defined to use a different analyzer will use the one provided here.
     */
    public LuceneAnalyser(Analyzer defaultAnalyser)
    {
        this.defaultAnalyser = defaultAnalyser;
    }

    public TokenStream tokenStream(String fieldName, Reader reader)
    {
        // Treat multilingual as a special case.
        // If multilingual then we need to find the correct tokeniser.
        // This is done dynamically by reading a language code at the start of the reader.
        if (fieldName.startsWith("@"))
        {
            QName propertyQName = QName.createQName(fieldName.substring(1));
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
            if (propertyDef != null)
            {
                if (propertyDef.getDataType().getName().equals(DataTypeDefinition.MLTEXT))
                {
                    MLAnalayser analyser = new MLAnalayser(dictionaryService, mlAlaysisMode);
                    return analyser.tokenStream(fieldName, reader);
                }
            }
        }

        Analyzer analyser = (Analyzer) analysers.get(fieldName);
        if (analyser == null)
        {
            analyser = findAnalyser(fieldName);
        }
        return analyser.tokenStream(fieldName, reader);
    }

    /**
     * Pick the analyser from the field name
     * 
     * @param fieldName
     * @return
     */
    private Analyzer findAnalyser(String fieldName)
    {
        Analyzer analyser;
        if (fieldName.equals("PATH"))
        {
            analyser = new PathAnalyser();
        }
        else if (fieldName.equals("QNAME"))
        {
            analyser = new PathAnalyser();
        }
        else if (fieldName.equals("TYPE"))
        {
            throw new UnsupportedOperationException("TYPE must not be tokenised");
        }
        else if (fieldName.equals("ASPECT"))
        {
            throw new UnsupportedOperationException("ASPECT must not be tokenised");
        }
        else if (fieldName.equals("ANCESTOR"))
        {
            analyser = new WhitespaceAnalyzer();
        }
        else if (fieldName.startsWith("@"))
        {
            if (fieldName.endsWith(".mimetype"))
            {
                analyser = new VerbatimAnalyser();
            }
            else if (fieldName.endsWith(".size"))
            {
                analyser = new LongAnalyser();
            }
            else if (fieldName.endsWith(".locale"))
            {
                analyser = new VerbatimAnalyser(true);
            }
            else
            {
                QName propertyQName = QName.createQName(fieldName.substring(1));
                PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
                if (propertyDef != null)
                {
                    if (propertyDef.isTokenisedInIndex())
                    {
                        DataTypeDefinition dataType = propertyDef.getDataType();
                        if (dataType.getName().equals(DataTypeDefinition.CONTENT))
                        {
                            analyser = new MLAnalayser(dictionaryService, MLAnalysisMode.ALL_ONLY);
                        }
                        else if (dataType.getName().equals(DataTypeDefinition.TEXT))
                        {
                            analyser = new MLAnalayser(dictionaryService, MLAnalysisMode.ALL_ONLY);
                        }
                        else
                        {
                            analyser = loadAnalyzer(dataType);
                        }
                    }
                    else
                    {
                        analyser = new VerbatimAnalyser();
                    }
                }
                else
                {
                    DataTypeDefinition dataType = dictionaryService.getDataType(DataTypeDefinition.TEXT);
                    analyser = loadAnalyzer(dataType);
                }
            }
        }
        else
        {
            analyser = defaultAnalyser;
        }
        analysers.put(fieldName, analyser);
        return analyser;
    }

    /**
     * Find an instantiate an analyser. The shuld all be thread sade as Analyser.tokenStream should be re-entrant.
     * 
     * @param dataType
     * @return
     */
    private Analyzer loadAnalyzer(DataTypeDefinition dataType)
    {
        String analyserClassName = dataType.getAnalyserClassName();
        try
        {
            Class<?> clazz = Class.forName(analyserClassName);
            Analyzer analyser = (Analyzer) clazz.newInstance();
            return analyser;
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Unable to load analyser for property of type "
                    + dataType.getName() + " using " + analyserClassName);
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException("Unable to load analyser for property of type "
                    + dataType.getName() + " using " + analyserClassName);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("Unable to load analyser for property of type "
                    + dataType.getName() + " using " + analyserClassName);
        }
    }

    /**
     * For multilingual fields we separate the tokens for each instance to break phrase queries spanning different
     * languages etc.
     */
    @Override
    public int getPositionIncrementGap(String fieldName)
    {
        if (fieldName.startsWith("@") && !fieldName.endsWith(".mimetype"))
        {
            QName propertyQName = QName.createQName(fieldName.substring(1));
            PropertyDefinition propertyDef = dictionaryService.getProperty(propertyQName);
            if (propertyDef != null)
            {
                if (propertyDef.getDataType().equals(DataTypeDefinition.MLTEXT))
                {
                    return 1000;
                }
            }
        }
        return super.getPositionIncrementGap(fieldName);
    }

}
