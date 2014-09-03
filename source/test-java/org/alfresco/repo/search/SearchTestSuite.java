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
package org.alfresco.repo.search;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.alfresco.repo.search.impl.lucene.ADMLuceneCategoryTest;
import org.alfresco.repo.search.impl.lucene.ADMLuceneTest;
import org.alfresco.repo.search.impl.lucene.ALF947Test;
import org.alfresco.repo.search.impl.lucene.LuceneIndexBackupComponentTest;
import org.alfresco.repo.search.impl.lucene.MultiReaderTest;
import org.alfresco.repo.search.impl.lucene.index.IndexInfoTest;
import org.alfresco.repo.search.impl.parsers.CMISTest;
import org.alfresco.repo.search.impl.parsers.CMIS_FTSTest;
import org.alfresco.repo.search.impl.parsers.FTSTest;
import org.alfresco.util.NumericEncodingTest;

/**
 * @author Andy Hind
 *
 */
public class SearchTestSuite extends TestSuite
{

    /**
     * Creates the test suite
     * 
     * @return  the test suite
     */
    public static Test suite() 
    {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(MLAnaysisModeExpansionTest.class);
        suite.addTestSuite(QueryRegisterComponentTest.class);
        suite.addTestSuite(SearcherComponentTest.class);
        suite.addTestSuite(SearchServiceTest.class);
        suite.addTestSuite(ADMLuceneCategoryTest.class);
        suite.addTestSuite(ADMLuceneTest.class);
        suite.addTestSuite(ALF947Test.class);
        suite.addTestSuite(LuceneIndexBackupComponentTest.class);
        suite.addTestSuite(MultiReaderTest.class);
        suite.addTestSuite(NumericEncodingTest.class);
        suite.addTestSuite(IndexInfoTest.class);
        suite.addTestSuite(CMIS_FTSTest.class);
        suite.addTestSuite(CMISTest.class);
        suite.addTestSuite(FTSTest.class);

        return suite;
    }
}
