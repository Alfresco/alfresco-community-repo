package org.alfresco.util.schemacomp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for tests in the schemacomp package.
 * 
 * @author Matt Ward
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
{
            DbObjectXMLTransformerTest.class,
            DbPropertyTest.class,
            DbToXMLTest.class,
            DefaultComparisonUtilsTest.class,
            DifferenceTest.class,
            ExportDbTest.class,
            MultiFileDumperTest.class,
            RedundantDbObjectTest.class,
            SchemaComparatorTest.class,
            SchemaToXMLTest.class,
            ValidationResultTest.class,
            ValidatingVisitorTest.class,
            XMLToSchemaTest.class
})
public class SchemaCompTestSuite
{
}
