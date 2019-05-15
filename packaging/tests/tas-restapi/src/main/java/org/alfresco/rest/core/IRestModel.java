package org.alfresco.rest.core;

import org.alfresco.rest.core.assertion.IModelAssertion;

public interface IRestModel<Model> extends IModelAssertion<Model> {

  public Model onModel();
 
}
