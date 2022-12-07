/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.search;

import org.alfresco.repo.search.impl.parsers.CMISTest;
import org.alfresco.repo.search.impl.parsers.CMIS_FTSTest;
import org.alfresco.repo.search.impl.parsers.FTSTest;
import org.alfresco.repo.search.impl.solr.SolrCategoryServiceImplTest;
import org.alfresco.repo.search.impl.solr.SolrChildApplicationContextFactoryTest;
import org.alfresco.repo.search.impl.solr.SolrSubsystemTest;
import org.alfresco.util.NumericEncodingTest;
import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Suite.SuiteClasses({
    MLAnaysisModeExpansionTest.class, 
    QueryRegisterComponentTest.class,
    SearcherComponentTest.class,
    DocumentNavigatorTest.class,
    NumericEncodingTest.class,
    CMIS_FTSTest.class,
    CMISTest.class,
    FTSTest.class,
    SolrChildApplicationContextFactoryTest.class,
    SolrSubsystemTest.class,
    SolrCategoryServiceImplTest.class
})

public class SearchTestSuite
{
}
