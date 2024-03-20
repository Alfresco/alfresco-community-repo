package org.alfresco;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Suite.SuiteClasses({
        org.alfresco.opencmis.dictionary.CMISAbstractDictionaryServiceTest.class
})
public class AllDataModelTestSuite {
}
