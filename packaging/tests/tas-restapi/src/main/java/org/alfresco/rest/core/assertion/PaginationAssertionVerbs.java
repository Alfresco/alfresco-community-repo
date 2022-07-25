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

import org.alfresco.rest.core.IRestModelsCollection;
import org.alfresco.rest.model.RestPaginationModel;
import org.alfresco.utility.exception.TestConfigurationException;
import org.apache.commons.beanutils.BeanUtils;
import org.testng.Assert;

/**
 * Pagination related assertions
 * 
 * @author Paul Brodner
 */
public class PaginationAssertionVerbs<C> {
  private RestPaginationModel pagination;
  private C modelCollection;
  private String fieldName;

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public PaginationAssertionVerbs(IRestModelsCollection modelCollection, String fieldName, RestPaginationModel pagination) {
    this.modelCollection = (C)modelCollection;
    this.fieldName = fieldName;
    this.pagination = pagination;
  }

  /**
   * @return the value of the field
   */
  private String getFieldValue() {
    String value = "";
    try {
      value = BeanUtils.getProperty(pagination, fieldName);

    } catch (Exception e) {
      throw new TestConfigurationException(String.format(
          "You try to assert field [%s] that doesn't exist in class: [%s]. Exception: %s, Please check your code!",
          fieldName, modelCollection.getClass().getCanonicalName(), e.getMessage()));
    }

    return value;
  }

  private String errorMessage(String info) {
    return String.format("The value of field [%s -> from %s] %s", fieldName,
        modelCollection.getClass().getCanonicalName(), info);
  }

  public C is(String expected) {

    Assert.assertEquals(getFieldValue(), expected, errorMessage("is NOT correct,"));
    return modelCollection;
  }

  public C isNot(Object expected) {

    Assert.assertNotEquals(getFieldValue(), expected, errorMessage("is correct,"));
    return modelCollection;
  }

  public C isNotEmpty() {
    Assert.assertNotEquals(getFieldValue(), "", errorMessage("is empty,"));
    return modelCollection;
  }

  public C isNotNull() {
    Assert.assertNotNull(getFieldValue(), errorMessage("is null,"));
    return modelCollection;
  }
  
  public C isNotPresent() {
      Assert.assertNull(getFieldValue(), errorMessage("is present,"));
      return modelCollection;
    }

  public C isEmpty() {
    Assert.assertEquals(getFieldValue(), "", errorMessage("is NOT empty,"));
    return modelCollection;
  }
}
