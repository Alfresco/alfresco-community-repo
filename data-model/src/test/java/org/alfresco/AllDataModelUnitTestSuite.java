/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Suite.SuiteClasses({
        org.alfresco.opencmis.dictionary.CMISAbstractDictionaryServiceTest.class,
        org.alfresco.repo.content.MimetypeMapTest.class,
        org.alfresco.repo.content.encoding.CharsetFinderTest.class,
        org.alfresco.repo.dictionary.AbstractModelTest.class,
        org.alfresco.repo.dictionary.DictionaryComponentTest.class,
        org.alfresco.repo.dictionary.DictionaryDAOTest.class,
        org.alfresco.repo.dictionary.DiffModelTest.class,
        org.alfresco.repo.dictionary.constraint.ConstraintsTest.class,
        org.alfresco.repo.index.ShardMethodEnumTest.class,
        org.alfresco.repo.search.impl.parsers.CMISTest.class,
        org.alfresco.repo.search.impl.parsers.CMIS_FTSTest.class,
        org.alfresco.repo.search.impl.parsers.FTSTest.class,
        org.alfresco.repo.security.authentication.InMemoryTicketComponentTest.class,
        org.alfresco.service.cmr.repository.MLTextTest.class,
        org.alfresco.service.cmr.repository.NodeRefTest.class,
        org.alfresco.service.cmr.repository.PathTest.class,
        org.alfresco.service.cmr.repository.PeriodTest.class,
        org.alfresco.service.cmr.repository.datatype.DefaultTypeConverterTest.class,
        org.alfresco.service.cmr.search.StatsProcessorTest.class,
        org.alfresco.service.namespace.DynamicNameSpaceResolverTest.class,
        org.alfresco.service.namespace.QNamePatternTest.class,
        org.alfresco.service.namespace.QNameTest.class,
        org.alfresco.util.ConfigFileFinderTest.class,
        org.alfresco.util.ConfigSchedulerTest.class,
        org.alfresco.util.ISO9075Test.class,
        org.alfresco.util.NumericEncodingTest.class,
        org.alfresco.util.SearchDateConversionTest.class,
        org.alfresco.util.SearchLanguageConversionTest.class
})
public class AllDataModelUnitTestSuite {
}
