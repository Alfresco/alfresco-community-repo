package org.alfresco;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import org.alfresco.repo.web.scripts.solr.NodeContentGetTest;
import org.alfresco.util.testing.category.DBTests;
import org.alfresco.util.testing.category.NonBuildTests;

@RunWith(Categories.class)
@Categories.ExcludeCategory({DBTests.class, NonBuildTests.class})
@Suite.SuiteClasses({
        // Add your test classes here
        NodeContentGetTest.class
})
public class NodeContentTestSuite
{}
