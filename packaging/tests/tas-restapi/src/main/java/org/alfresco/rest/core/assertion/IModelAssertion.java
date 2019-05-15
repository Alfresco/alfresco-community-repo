package org.alfresco.rest.core.assertion;

public interface IModelAssertion<Model> 
{
  public ModelAssertion<Model> and();
  
  public ModelAssertion<Model> assertThat();
}
