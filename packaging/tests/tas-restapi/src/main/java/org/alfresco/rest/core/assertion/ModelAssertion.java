/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.core.assertion;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import io.restassured.path.json.JsonPath;
import org.alfresco.utility.exception.TestConfigurationException;
import org.alfresco.utility.model.TestModel;
import org.testng.Assert;

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
    private final Object model;

    public ModelAssertion(Object model)
    {
        this.model = model;
    }

    /**
     * Use this DSL for asserting particular fields of your model if your model
     * is like this (basic POJO)
     * public class Person extends ModelAssertion<Person>
     *     { private String id = "1234"; }
     * you can use assert the id of this person as:
     * Person p = new Person(); p.assertThat().field("id").is("1234")
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
     * Use this method for asserting whole model with different model object. Method allows to ignore particular fields during the comparison.
     *
     * WARNING: For proper work model should implement {@code toString()} and {@code equals()} methods.
     *
     * @param expected - expected model.
     * @param ignoreFields - fields which should be ignored during assertion.
     * @return model.
     */
    @SuppressWarnings("unchecked")
    public T isEqualTo(T expected, String... ignoreFields)
    {
        T modelCopy = createCopyIgnoringFields((T) model, ignoreFields);
        T expectedCopy = createCopyIgnoringFields(expected, ignoreFields);
        Assert.assertEquals(modelCopy, expectedCopy, String.format("Compared objects of type: %s are not equal!", model.getClass()));
        return (T) model;
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

    @SuppressWarnings("unchecked")
    private T createCopyIgnoringFields(T model, String... ignoreFields)
    {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(new SerializableTypeAdapterFactory());
        Gson gson = gsonBuilder.create();
        JsonObject jsonObject = gson.fromJson(gson.toJson(model), JsonObject.class);
        for (String ignoreField : ignoreFields)
        {
            jsonObject.remove(ignoreField);
        }
        return gson.fromJson(gson.toJson(jsonObject), (Class<? extends T>) model.getClass());
    }

    /** Workaround from https://github.com/google/gson/issues/544 */
    private class SerializableTypeAdapterFactory implements TypeAdapterFactory
    {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type)
        {
            if (Serializable.class.equals(type.getRawType()))
            {
                return (TypeAdapter<T>) gson.getAdapter(Object.class);
            }
            return null;
        }
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
            {
                Assert.fail(errorMessage("does NOT contain expected value: " + value + ", Current Value: " + fieldValue.toString()));
            }

            return (T) model;
        }

        public T containsOnce(String value)
        {
            final String fieldContent = fieldValue.toString();
            final int i = fieldContent.indexOf(value);
            if (i == -1)
            {
                Assert.fail(errorMessage("does NOT contain at all the expected value: " + value + ", Current Value: " + fieldValue.toString()));
            }
            if (i != fieldContent.lastIndexOf(value))
            {
                Assert.fail(errorMessage("contains more than one expected value: " + value + ", Current Value: " + fieldValue.toString()));
            }

            return (T) model;
        }

        public T notContains(String value)
        {
            if (fieldValue.toString().contains(value))
            {
                Assert.fail(errorMessage("does contain unexpected value: " + value + ", Current Value: " + fieldValue.toString()));
            }

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
