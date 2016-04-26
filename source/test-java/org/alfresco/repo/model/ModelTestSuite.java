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
