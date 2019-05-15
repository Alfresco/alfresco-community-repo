package org.alfresco.rest.core;

import java.util.List;

import org.alfresco.rest.exception.EmptyRestModelCollectionException;
import org.alfresco.rest.model.RestPaginationModel;

public interface IRestModelsCollection<Model> {

  public List<Model> getEntries();

  public Model getOneRandomEntry() throws EmptyRestModelCollectionException;

  /**
   * @return boolean value if entry is empty
   */
  public boolean isEmpty();

  public RestPaginationModel getPagination();

}
