/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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

package org.alfresco.repo.dictionary;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import org.alfresco.repo.dictionary.ValueDataTypeValidator;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for {@link ValueDataTypeValidatorImpl}
 *
 * @author Jamal Kaabi-Mofrad
 */
public class ValueDataTypeValidatorImplTest
{
    public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();

    private static ValueDataTypeValidator validator;

    @BeforeClass
    public static void initStaticData() throws Exception
    {
        validator = APP_CONTEXT_INIT.getApplicationContext().getBean("valueDataTypeValidator", ValueDataTypeValidator.class);
    }

    @Test
    public void testValueNumericDataType() throws Exception
    {
        // d:int tests
        {
            final String intDataType = "d:int";

            validate_invalid(intDataType, " ");// space
            validate_invalid(intDataType,"abc"); // text
            validate_invalid(intDataType, "1.0"); // double
            validate_invalid(intDataType, "1,2,3"); // text
            validate_invalid("int", "1"); // invalid data type

            validate_valid(intDataType, null); // no validation
            validate_valid(intDataType, ""); // no validation
            validate_valid(intDataType, "12"); // valid value
        }

        // d:long tests
        {
            final String longDataType = "d:long";

            validate_invalid(longDataType, " ");// space
            validate_invalid(longDataType,"abc"); // text
            validate_invalid(longDataType, "1.0"); // double
            validate_invalid(longDataType, "1,2,3"); //  text
            validate_invalid("long", "10"); // invalid data type

            validate_valid(longDataType, null); // no validation
            validate_valid(longDataType, ""); // no validation
            validate_valid(longDataType, "20"); // valid value
        }
        
        // d:float tests
        {
            final String floatDataType = "d:float";

            validate_invalid(floatDataType, " ");// space
            validate_invalid(floatDataType,"abc"); // text
            validate_invalid(floatDataType, "1.0,2.0,3.0"); // text
            validate_invalid("float", "10.0"); // invalid data type

            validate_valid(floatDataType, null); // no validation
            validate_valid(floatDataType, ""); // no validation
            validate_valid(floatDataType, "1.0"); // valid value
            validate_valid(floatDataType, "1.0f"); // valid value
            validate_valid(floatDataType, "1.0d"); // valid value
            validate_valid(floatDataType, "1"); // valid value
        }

        // d:double tests
        {
            final String doubleDataType = "d:double";

            validate_invalid(doubleDataType, " ");// space
            validate_invalid(doubleDataType,"abc"); // text
            validate_invalid(doubleDataType, "1.0,2.0,3.0"); // text
            validate_invalid("double", "10.0"); // invalid data type

            validate_valid(doubleDataType, null); // no validation
            validate_valid(doubleDataType, ""); // no validation
            validate_valid(doubleDataType, "1.0"); // valid value
            validate_valid(doubleDataType, "1.0f"); // valid value
            validate_valid(doubleDataType, "1.0d"); // valid value
            validate_valid(doubleDataType, "1"); // valid value
        }
    }

    @Test
    public void testValueBooleanDataType() throws Exception
    {
        final String booleanDataType = "d:boolean";

        validate_invalid(booleanDataType, " ");// space
        validate_invalid(booleanDataType,"abc"); // text
        validate_invalid(booleanDataType, "1"); // number
        validate_invalid("boolean", "true"); // invalid data type

        validate_valid(booleanDataType, null); // no validation
        validate_valid(booleanDataType, ""); // no validation
        validate_valid(booleanDataType, "true"); // valid value
        validate_valid(booleanDataType, "false"); // valid value
    }

    @Test
    public void testValueDateDataType() throws Exception
    {
        final String dateDataType = "d:date";

        validate_invalid(dateDataType, " ");// space
        validate_invalid(dateDataType,"abcd"); // text
        validate_invalid(dateDataType, "20/05/15"); // non-ISO8601 date
        validate_invalid(dateDataType, "20-05-2015"); // non-ISO8601 date
        validate_invalid("date", "2015-05-20"); // invalid data type

        validate_valid(dateDataType, null); // no validation
        validate_valid(dateDataType, ""); // no validation
        validate_valid(dateDataType, "2015-05-20"); // valid value
        validate_valid(dateDataType, "20150520"); // valid value
        validate_valid(dateDataType, "2015-05-01T12:00:00+01:00"); // valid value
    }

    @Test
    public void testValueDateTimeDataType() throws Exception
    {
        final String datetimeDataType = "d:datetime";

        validate_invalid(datetimeDataType, " ");// space
        validate_invalid(datetimeDataType,"abcd"); // text
        validate_invalid(datetimeDataType, "20/05/15T12:00:00+01:00"); // non-ISO8601 date
        validate_invalid(datetimeDataType, "20-05-2015T12:00:00+01:00"); // non-ISO8601 date
        validate_invalid("datetime", "2015-05-20T12:00:00+01:00"); // invalid data type

        validate_valid(datetimeDataType, null); // no validation
        validate_valid(datetimeDataType, ""); // no validation
        validate_valid(datetimeDataType, "2015-05-20"); // valid value
        validate_valid(datetimeDataType, "20150520T14:30Z"); // valid value
        validate_valid(datetimeDataType, "2015-05-01T12:00:00+01:00"); // valid value
    }

    @Test
    public void testValueTextDataType() throws Exception
    {
        // d:text tests
        {
            final String textDataType = "d:text";
            validate_invalid("somePrefix:", "some text"); // invalid QName prefixed data type
            validate_invalid("unknownPrefix" + System.currentTimeMillis() + ":text", "some text"); // prefix is not mapped to a namespace URI
            validate_invalid(null, "some text"); // invalid data type
            validate_invalid("", "some text");// invalid data type

            validate_valid(textDataType, null); // no validation
            validate_valid(textDataType, ""); // no validation
            validate_valid(textDataType, "some text"); // valid value
            validate_valid(textDataType, "1,2,3"); // valid value
            validate_valid(textDataType, "2"); // valid value
            validate_valid(textDataType, " "); // valid value
        }

        // d:mltext tests
        {
            final String mltextDataType = "d:mltext";
            validate_invalid("somePrefix:", "Très bon!"); // invalid QName prefixed data type
            validate_invalid("unknownPrefix" + System.currentTimeMillis() + ":mltext", "some mltext"); // prefix is not mapped to a namespace URI
            validate_invalid(null, "some mltext"); // invalid data type
            validate_invalid("", "some mltext");// invalid data type

            validate_valid(mltextDataType, "Très bon!"); // valid value
        }

    }

    @Test
    public void testValueContentDataType() throws Exception
    {
        // d:content tests
        {
            final String contentDataType = "d:content";
            validate_invalid("somePrefix:", "some very long text"); // invalid QName prefixed data type
            validate_invalid("unknownPrefix" + System.currentTimeMillis() + ":content", "some text"); // prefix is not mapped to a namespace URI

            validate_valid(contentDataType, null); // no validation
            validate_valid(contentDataType, ""); // no validation
            ByteArrayInputStream is = new ByteArrayInputStream("very long text".getBytes("UTF-8"));
            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer, "UTF-8");
            validate_valid(contentDataType,  writer.toString()); // valid value
        }
    }

    private void validate_invalid(String dataType, String value)
    {
        try
        {
            validator.validateValue(dataType, value);
            fail("Validation should have failed.");
            
        }
        catch (Exception ex)
        {
            // expected
        }
    }

    private void validate_valid(String dataType, String value)
    {
        try
        {
            validator.validateValue(dataType, value);
        }
        catch (Exception ex)
        {
            fail("Validation should have succeeded.");
        }
    }
}
