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
package org.alfresco.repo.model;

import org.alfresco.repo.model.filefolder.FileFolderDuplicateChildTest;
import org.alfresco.repo.model.filefolder.FileFolderLoaderTest;
import org.alfresco.repo.model.filefolder.FileFolderPerformanceTester;
import org.alfresco.repo.model.filefolder.FileFolderServiceImplTest;
import org.alfresco.repo.model.filefolder.FileFolderServicePropagationTest;
import org.alfresco.repo.model.filefolder.HiddenAspectCmisConfigTest;
import org.alfresco.repo.model.filefolder.HiddenAspectTest;
import org.alfresco.repo.model.ml.tools.ContentFilterLanguagesMapTest;
import org.alfresco.repo.model.ml.tools.EditionServiceImplTest;
import org.alfresco.repo.model.ml.tools.EmptyTranslationAspectTest;
import org.alfresco.repo.model.ml.tools.LanguagesTest;
import org.alfresco.repo.model.ml.tools.MLContainerTypeTest;
import org.alfresco.repo.model.ml.tools.MultilingualContentServiceImplTest;
import org.alfresco.repo.model.ml.tools.MultilingualDocumentAspectTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Model test suite
 */
@RunWith(Suite.class)
@SuiteClasses({
    ContentFilterLanguagesMapTest.class,
    EmptyTranslationAspectTest.class,
    MLContainerTypeTest.class,
    MultilingualContentServiceImplTest.class,
    MultilingualDocumentAspectTest.class,
    EditionServiceImplTest.class,
    LanguagesTest.class,
    HiddenAspectTest.class,
    HiddenAspectCmisConfigTest.class,
    FileFolderLoaderTest.class,
    FileFolderPerformanceTester.class,
    
    // Add the file folder tests
    // These need to come afterwards, as they insert extra
    //  interceptors which would otherwise confuse things
    FileFolderServiceImplTest.class,
    FileFolderDuplicateChildTest.class,
    FileFolderServicePropagationTest.class
})
public class ModelTestSuite
{
}
