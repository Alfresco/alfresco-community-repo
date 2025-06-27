package org.alfresco;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import org.alfresco.repo.web.scripts.solr.NodeContentGetTest;

@Suite.SuiteClasses({
        // Add your test classes here
        NodeContentGetTest.class
})
@RunWith(Suite.class)
public class NodeContentTestSuite
{}
