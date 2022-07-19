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