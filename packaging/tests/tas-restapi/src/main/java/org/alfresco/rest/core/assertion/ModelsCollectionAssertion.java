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

import static org.alfresco.utility.report.log.Step.STEP;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.alfresco.rest.core.IRestModelsCollection;
import org.alfresco.utility.exception.TestConfigurationException;
import org.alfresco.utility.model.Model;
import org.apache.commons.beanutils.BeanUtils;
import org.testng.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Ordering;
import io.restassured.path.json.JsonPath;

/**
 * Assertion on Rest Model Collection
 * Just pass your rest model collection as constructor
 * 
 * @author Paul Brodner
 */
public class ModelsCollectionAssertion<C> 
{
  @SuppressWarnings("rawtypes")
  private IRestModelsCollection modelCollection;

  @SuppressWarnings("rawtypes")
  public ModelsCollectionAssertion(IRestModelsCollection modelCollection) 
  {
    this.modelCollection = modelCollection;
  }

  /**
   * check if "entries" list from JSON is not empty
   */
  @SuppressWarnings("unchecked")
  public C entriesListIsNotEmpty() 
  {
    STEP("REST API: Assert that entries list from response is not empty");
    Assert.assertFalse(modelCollection.isEmpty(), "Entries list from response is empty.Check the logs for more details!");
    return (C) modelCollection;
  }

  /**
   * check if "entries" list from JSON is empty
   */
  @SuppressWarnings("unchecked")
  public C entriesListIsEmpty() 
  {
    STEP("REST API: Assert that entries list from response is empty");
    Assert.assertTrue(modelCollection.isEmpty(), "Entries list from response is not empty.Check the logs for more details!");
    return (C) modelCollection;
  }

  @SuppressWarnings("unchecked")
  public C entriesListCountIs(int expectedCount)
  {
      STEP(String.format("REST API: Assert that entries list count is %d", expectedCount));
      int actualSize = modelCollection.getEntries().size();
      Assert.assertEquals(actualSize, expectedCount);
      return (C) modelCollection;
  }
  
  @SuppressWarnings("unchecked")
  public C entriesListContains(String key, String value)
  {    
    List<Model> modelEntries = modelCollection.getEntries();
    String fieldValue = "";
    for (Model m : modelEntries) {
        Object model = loadModel(m);
        try {
          ObjectMapper mapper = new ObjectMapper();
          String jsonInString = mapper.writeValueAsString(model);
          fieldValue = JsonPath.with(jsonInString).get(key);
         if (fieldValue != null && fieldValue.equals(value)) {
                 break;
         }
      } catch (Exception e) {
        throw new TestConfigurationException(String.format(
            "You try to assert field [%s] that doesn't exist in class: [%s]. Exception: %s, Please check your code!",
            key, getClass().getCanonicalName(), e.getMessage()));
      }
    }
    Assert.assertEquals(fieldValue, value, String.format("Entry with key: [%s] with value [%s] not found in list", key, value));
  

    return (C) modelCollection;
  }

  @SuppressWarnings("unchecked")
  public C entriesListDoesNotContain(String key, String value)
  {
    boolean exist = false;
    List<Model> modelEntries = modelCollection.getEntries();
    for (Model m : modelEntries) {
        Object model = loadModel(m);
        String fieldValue = "";
      try {
          ObjectMapper mapper = new ObjectMapper();
          String jsonInString = mapper.writeValueAsString(model);
          fieldValue = JsonPath.with(jsonInString).get(key);
        if (fieldValue != null && fieldValue.equals(value)) {
          exist = true;
          break;
        }
      } catch (Exception e) {
        // nothing to do
      }
    }
    Assert.assertFalse(exist,
        String.format("Entry with key: %s and value %s was found in list", key, value));

    return (C) modelCollection;
  }

  public C entrySetContains(String key, String... expectedValues)
  {
    return entrySetContains(key, Arrays.stream(expectedValues).collect(Collectors.toSet()));
  }

  @SuppressWarnings("unchecked")
  public C entrySetContains(String key, Collection<String> expectedValues)
  {
    Collection<String> actualValues = ((List<Model>) modelCollection.getEntries()).stream()
        .map(model -> extractValueAsString(model, key))
        .collect(Collectors.toSet());

    Assert.assertTrue(actualValues.containsAll(expectedValues), String.format("Entry with key: \"%s\" is expected to contain values: %s, but actual values are: %s",
        key, expectedValues, actualValues));

    return (C) modelCollection;
  }

  @SuppressWarnings("unchecked")
  public C entrySetMatches(String key, Collection<String> expectedValues)
  {
    Collection<String> actualValues = ((List<Model>) modelCollection.getEntries()).stream()
        .map(model -> extractValueAsString(model, key))
        .collect(Collectors.toSet());

    Assert.assertEqualsNoOrder(actualValues, expectedValues, String.format("Entry with key: \"%s\" is expected to match values: %s, but actual values are: %s",
        key, expectedValues, actualValues));

    return (C) modelCollection;
  }

  private String extractValueAsString(Model model, String key)
  {
    String fieldValue;
    Object modelObject = loadModel(model);
    try {
      ObjectMapper mapper = new ObjectMapper();
      String jsonInString = mapper.writeValueAsString(modelObject);
      fieldValue = JsonPath.with(jsonInString).get(key);
    } catch (Exception e) {
      throw new TestConfigurationException(String.format(
          "You try to assert field [%s] that doesn't exist in class: [%s]. Exception: %s, Please check your code!",
          key, getClass().getCanonicalName(), e.getMessage()));
    }
    return fieldValue;
  }

  @SuppressWarnings("unchecked")
  public C entriesListDoesNotContain(String key)
  {
    boolean exist = modelInList(key);
    Assert.assertFalse(exist,
        String.format("Entry list contains key: %s", key));

    return (C) modelCollection;
  }

  @SuppressWarnings("unchecked")
  public C entriesListContains(String key)
  {
    boolean exist = modelInList(key);
    Assert.assertTrue(exist,
    String.format("Entry list doesn't contain key: %s", key));

    return (C) modelCollection;
  }

  private boolean modelInList(String key)
  {
      List<Model> modelEntries = modelCollection.getEntries();
      for (Model m : modelEntries)
      {
          Object model = loadModel(m);
          ObjectMapper mapper = new ObjectMapper();
          String jsonInString;
          try
          {
              jsonInString = mapper.writeValueAsString(model);
          }
          catch (JsonProcessingException e)
          {
              throw new IllegalStateException("Failed to convert model to string.", e);
          }
          Object fieldValue = JsonPath.with(jsonInString).get(key);
          if (fieldValue != null)
          {
              return true;
          }
      }
      return false;
  }

  @SuppressWarnings("unchecked")
  public C paginationExist()
  {
    STEP("REST API: Assert that response has pagination");
    Assert.assertNotNull(modelCollection.getPagination(), "Pagination is was not found in the response");
    return (C) modelCollection;
  }

  /**
   * Check one field from pagination json body
   *
   * @param field
   * @return
   */
  @SuppressWarnings("rawtypes")
  public PaginationAssertionVerbs paginationField(String field)
  {
    return new PaginationAssertionVerbs<C>(modelCollection, field, modelCollection.getPagination());
  }

  /**
   * check is the entries are ordered ASC by a specific field
   *
   * @param field from json response
   * @return
   */
  @SuppressWarnings("unchecked")
  public C entriesListIsSortedAscBy(String field)
  {
      List<Model> modelEntries = modelCollection.getEntries();
      List<String> fieldValues = new ArrayList<String>();
      for(Model m: modelEntries)
      {
          Object model = loadModel(m);
          String fieldValue = "";
          try {
              fieldValue = BeanUtils.getProperty(model, field);
              fieldValues.add(fieldValue);
          }
          catch (Exception e)
          {
              // nothing to do
          }
      }
      Assert.assertTrue(Ordering.natural().isOrdered(fieldValues), String.format("Entries are not ordered ASC by %s", field));
      return (C) modelCollection;
  }

  /**
   * check is the entries are ordered DESC by a specific field
   *
   * @param field from json response
   * @return
   */
  @SuppressWarnings("unchecked")
  public C entriesListIsSortedDescBy(String field)
  {
      List<Model> modelEntries = modelCollection.getEntries();
      List<String> fieldValues = new ArrayList<String>();
      for(Model m: modelEntries)
      {
          Object model = loadModel(m);
          String fieldValue = "";
          try {
              fieldValue = BeanUtils.getProperty(model, field);
              fieldValues.add(fieldValue);
          }
          catch (Exception e)
          {
              // nothing to do
          }
      }
      Assert.assertTrue(Ordering.natural().reverse().isOrdered(fieldValues), String.format("Entries are not ordered DESC by %s", field));
      return (C) modelCollection;
  }

    private Object loadModel(Model m)
    {
        try
        {
            Method method = m.getClass().getMethod("onModel", new Class[] {});
            return method.invoke(m, new Object[] {});
        }
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
        {
            throw new IllegalStateException("Failed to load model using reflection.", e);
        }
    }
}
