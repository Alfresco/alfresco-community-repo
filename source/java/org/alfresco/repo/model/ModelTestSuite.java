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
package org.alfresco.repo.model;

import org.alfresco.repo.model.filefolder.FileFolderDuplicateChildTest;
import org.alfresco.repo.model.filefolder.FileFolderServiceImplTest;
import org.alfresco.repo.model.ml.tools.ContentFilterLanguagesMapTest;
import org.alfresco.repo.model.ml.tools.EditionServiceImplTest;
import org.alfresco.repo.model.ml.tools.EmptyTranslationAspectTest;
import org.alfresco.repo.model.ml.tools.MLContainerTypeTest;
import org.alfresco.repo.model.ml.tools.MultilingualContentServiceImplTest;
import org.alfresco.repo.model.ml.tools.MultilingualDocumentAspectTest;
import org.alfresco.util.ApplicationContextHelper;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Model test suite
 */
public class ModelTestSuite extends TestSuite
{
    /**
     * Creates the test suite
     *
     * @return  the test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite();

        // Ensure that the default context is available
        ApplicationContextHelper.getApplicationContext();

        // Add the multilingual tests
        suite.addTestSuite( ContentFilterLanguagesMapTest.class );
        suite.addTestSuite( EmptyTranslationAspectTest.class );
        suite.addTestSuite( MLContainerTypeTest.class );
        suite.addTestSuite( MultilingualContentServiceImplTest.class );
        suite.addTestSuite( MultilingualDocumentAspectTest.class );
        suite.addTestSuite( EditionServiceImplTest.class );
        
        // Add the file folder tests
        // These need to come afterwards, as they insert extra
        //  interceptors which would otherwise confuse things
        suite.addTestSuite( FileFolderServiceImplTest.class );
        suite.addTestSuite( FileFolderDuplicateChildTest.class );

        return suite;
    }
}
