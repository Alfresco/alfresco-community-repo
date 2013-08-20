/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;

import org.alfresco.util.LogAdapterTest;
import org.alfresco.util.LogTeeTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author adavis
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
    TransformerConfigStatisticsTest.class,
    TransformerConfigLimitsTest.class,
    TransformerConfigSupportedTest.class,
    TransformerConfigPropertyTest.class,
    TransformerPropertyNameExtractorTest.class,
    TransformerPropertyGetterTest.class,
    TransformerPropertySetterTest.class,
    TransformerConfigDynamicTransformersTest.class,

    LogAdapterTest.class,
    LogTeeTest.class,
    
    TransformerLoggerTest.class,
    TransformerLogTest.class,
    TransformerDebugLogTest.class,
    TransformerDebugTest.class,
    
    TransformerConfigImplTest.class,
    TransformerConfigMBeanImplTest.class,

    TransformerSelectorImplTest.class})

/**
 * Test classes in the Transformers subsystem
 * 
 * @author Alan Davis
 */
public class TransformerConfigTestSuite
{
}
