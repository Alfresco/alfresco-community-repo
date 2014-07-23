package org.alfresco.repo.dictionary;

import java.util.List;

import org.alfresco.service.namespace.QName;

/**
 * 
 * @author sglover
 *
 */
public interface ModelValidator
{
	void setStoreUrls(List<String> storeUrls);
	void validateModel(CompiledModel compiledModel);
	void validateModelDelete(final QName modelName);
}
