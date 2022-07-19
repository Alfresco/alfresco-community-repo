package org.alfresco.rest.core.assertion;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.utility.exception.TestConfigurationException;
import org.alfresco.utility.model.TestModel;
import org.testng.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.path.json.JsonPath;

/**
 * Assertion on Rest Model
 * Just pass your rest model as constructor
 *
 * @author Paul Brodner
 */

public class ModelAssertion<T>
{
    protected static void checkFieldIsPresent(Object fieldNameToBeRetuned, Object fieldValueToBeRetuned)
    {
        if (fieldValueToBeRetuned == null)
        {
            Assert.fail(String.format("Field {%s} was not found in returned response.",fieldNameToBeRetuned));
        }
    }
    private Object model;

    public ModelAssertion(Object model)
    {
        this.model = model;
    }

    /**
     * Use this DSL for asserting particular fields of your model if your model
     * is like this (basic POJO) public class Person extends
     * ModelAssertion<Person> { private String id = "1234"; you can use assert
     * the id of this person as:
     * Person p = new Person(); p.assertField("id").is("1234")
     *
     * @param fieldName
     * @return
     * @throws IllegalStateException If the field cannot be converted to JSON.
     */
    public AssertionVerbs field(String fieldName)
    {

        ObjectMapper mapper = new ObjectMapper();

        String jsonInString = null;
        try
        {
            jsonInString = mapper.writeValueAsString(model);
        }
        catch (JsonProcessingException e)
        {
            throw new IllegalStateException(e);
        }

        Object fieldValue = JsonPath.with(jsonInString).get(fieldName);

        return new AssertionVerbs(model, fieldValue, fieldName);
    }

    public AssertionItemVerbs fieldsCount()
    {

        int actualSize = 0;
        List<Field> allFields = getAllDeclaredFields(new LinkedList<Field>(), model.getClass());

        for (Field field : allFields)
        {

            field.setAccessible(true);
            Object fieldValue = null;
            try
            {
                fieldValue = field.get(model);
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalStateException("Unable to load model using reflection.", e);
            }
            if (fieldValue != null)
                actualSize++;
        }
        return new AssertionItemVerbs(model, actualSize);
    }

    /**
     * Get all fields declared from all classes hierarchy
     *
     * @param fields
     * @param classz
     * @return
     */
    private List<Field> getAllDeclaredFields(List<Field> fields, Class<?> classz)
    {
        if (classz.isAssignableFrom(TestModel.class))
        {
            return fields;
        }

        fields.addAll(Arrays.asList(classz.getDeclaredFields()));

        if (classz.getSuperclass() != null)
        {
            fields = getAllDeclaredFields(fields, classz.getSuperclass());
        }

        return fields;
    }

    /**
     * DSL assertion on Rest Model fields
     *
     * @author Paul Brodner
     */
    @SuppressWarnings("unchecked")
    public class AssertionVerbs
    {
        private String fieldName;
        private Object model;
        private Object fieldValue;

        public AssertionVerbs(Object model, Object fieldValue, String fieldName)
        {
            this.model = model;
            this.fieldValue = fieldValue;
            this.fieldName = fieldName;
        }

        private String errorMessage(String info)
        {
            return String.format("The value of field [%s -> from %s] %s", fieldName, model.getClass().getCanonicalName(), info);
        }

        public T isNot(Object expected)
        {
            checkFieldIsPresent(fieldName, fieldValue);
            Assert.assertNotEquals(fieldValue, expected, errorMessage("is correct,"));
            return (T) model;
        }

        @Override
        public boolean equals(Object o)
        {
            throw new UnsupportedOperationException("You probably want to use is() rather than equals()");
        }

        public T is(Object expected)
        {
            checkFieldIsPresent(fieldName, fieldValue);
            Assert.assertEquals(fieldValue.toString(), expected.toString(), errorMessage("is NOT correct,"));
            return (T) model;
        }

        /**
         * Check if the supplied field is a non-empty String, Collection or Map.
         *
         * @throws AssertionError if the field is empty.
         * @throws UnsupportedOperationException if the field cannot be checked for emptiness.
         */
        public T isNotEmpty()
        {
            checkFieldIsPresent(fieldName, fieldValue);
            if (fieldValue instanceof Collection)
            {
                Assert.assertNotEquals(fieldValue, Collections.emptyList(), errorMessage("is empty,"));
            }
            else if (fieldValue instanceof String)
            {
                Assert.assertNotEquals(fieldValue, "", errorMessage("is empty,"));
            }
            else if (fieldValue instanceof Map)
            {
                Assert.assertNotEquals(fieldValue, Collections.emptyMap(), errorMessage("is empty,"));
            }
            else if (fieldValue instanceof Integer)
            {
                Assert.assertNotEquals(fieldValue.toString(), "", errorMessage("is empty,"));
            }
            else if (fieldValue instanceof Long)
            {
                Assert.assertNotEquals(fieldValue.toString(), "", errorMessage("is empty,"));
            }
            else if (fieldValue instanceof Boolean)
            {
                Assert.assertNotEquals(String.valueOf(fieldValue), "", errorMessage("is empty,"));
            }
            else
            {
                throw new UnsupportedOperationException("Cannot check for emptiness of " + fieldValue.getClass());
            }
            return (T) model;
        }

        public T isNotNull()
        {
            checkFieldIsPresent(fieldName, fieldValue);
            Assert.assertNotNull(fieldValue, errorMessage("is null,"));
            return (T) model;
        }

        public T isNull()
        {
            Assert.assertNull(fieldValue, errorMessage("is not null,"));
            return (T) model;
        }

        /**
         * Check if the supplied field is an empty String, Collection or Map.
         *
         * @throws AssertionError if the field is not empty.
         * @throws UnsupportedOperationException if the field cannot be checked for emptiness.
         */
        public T isEmpty()
        {
            checkFieldIsPresent(fieldName, fieldValue);
            if (fieldValue instanceof Collection)
            {
                Assert.assertEquals((Collection<?>) fieldValue, Collections.emptyList(), errorMessage("is NOT empty,"));
            }
            else if (fieldValue instanceof String)
            {
                Assert.assertEquals(fieldValue, "", errorMessage("is NOT empty,"));
            }
            else if (fieldValue instanceof Map)
            {
                Assert.assertEquals(fieldValue, Collections.emptyMap(), errorMessage("is NOT empty,"));
            }
            else
            {
                throw new UnsupportedOperationException("Cannot check for emptiness of " + fieldValue.getClass());
            }
            return (T) model;
        }

        public T contains(String value)
        {
            if (!fieldValue.toString().contains(value))
                Assert.fail(errorMessage("does NOT contain expected value: " + value + ", Current Value: " + fieldValue.toString()));

            return (T) model;
        }
        /**
         * Assert if predicate value is greater than the field value
         * @author Michael Suzuki
         * @param value the predicate
         * @return
         * @throws TestConfigurationException
         *
         */
        public T isGreaterThan(Integer value) throws TestConfigurationException
        {
            return validateSize(value, Operation.Greater);
        }

        private T validateSize(Integer value, Operation operation) throws TestConfigurationException
        {
            try
            {
                if(value == null)
                {
                    throw new TestConfigurationException("Input must be valid");
                }
                Integer b = Integer.valueOf(fieldValue.toString());
                switch (operation)
                {
                case Greater:
                    if(value > b)
                    {
                        Assert.fail(errorMessage(String.format("The expected value %s is not greater than the actual value %s ",
                                value, fieldValue.toString())));
                    }
                    break;
                case Less:
                    if(value < b)
                    {
                        Assert.fail(errorMessage(String.format("The expected value %s is not less than the actual value %s ",
                                value, fieldValue.toString())));
                    }
                    break;

                default:
                    Assert.fail(errorMessage("No operation type provided"));
                    break;
                }
            }
            catch(NumberFormatException e)
            {
                Assert.fail(errorMessage("The field is not numeric " + fieldValue.toString()));
            }
            catch (NullPointerException ne)
            {
                Assert.fail(errorMessage("The input value must be numeric " + value));
            }
            return (T) model;
        }
        /**
         * Assert if predicate value is less than the field value
         * @author Michael Suzuki
         * @param value the predicate
         * @return
         * @throws TestConfigurationException
         *
         */
        public T isLessThan(Integer value) throws TestConfigurationException
        {
            return validateSize(value, Operation.Less);
        }
    }

    @SuppressWarnings("unchecked")
    public class AssertionItemVerbs
    {
        private Object model;
        private Object actual;

        public AssertionItemVerbs(Object model, Object actual)
        {
            this.model = model;
            this.actual = actual;
        }

        public T is(Object expected) {
            Assert.assertEquals(actual, expected, String.format("For model [%s], the expected value is not correct ",
                    model.getClass().getSimpleName(), expected.toString(), actual.toString()));
            return (T) model;
        }
    }
    public static enum Operation
    {
        Less,Greater
    }
}
