package org.alfresco.util.schemacomp.model;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for all the model tests.
 * 
 * @author Matt Ward
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
{
    AbstractDbObjectTest.class,
    ColumnTest.class,
    ForeignKeyTest.class,
    IndexTest.class,
    PrimaryKeyTest.class,
    SchemaTest.class,
    SequenceTest.class,
    TableTest.class
})
public class ModelTestSuite
{
    // Suite defined by annotation above.
}
