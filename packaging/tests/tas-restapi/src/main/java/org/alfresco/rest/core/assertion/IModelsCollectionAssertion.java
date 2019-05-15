package org.alfresco.rest.core.assertion;

@SuppressWarnings("rawtypes")
public interface IModelsCollectionAssertion<ModelCollection> {
  public ModelsCollectionAssertion and();

  public ModelsCollectionAssertion assertThat();

  public ModelCollection when();
}
