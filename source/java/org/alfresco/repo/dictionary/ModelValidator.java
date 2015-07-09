package org.alfresco.repo.dictionary;

import org.alfresco.service.namespace.QName;

/**
 * Validates model changes and deletes against the repository.
 * 
 * @author sglover
 *
 */
public interface ModelValidator
{
    /**
     * validate against dictionary
     * 
     * if new model 
     * then nothing to validate
     * 
     * else if an existing model 
     * then could be updated (or unchanged) so validate to currently only allow incremental updates
     *   - addition of new types, aspects (except default aspects), properties, associations
     *   - no deletion of types, aspects or properties or associations
     *   - no addition, update or deletion of default/mandatory aspects
     * 
     * @throws ModelInUseException if the model is being used by nodes or properties
     */
    void validateModel(CompiledModel compiledModel);

	/**
     * Can the model be deleted (validate against repository contents / workflows)?
     * 
     * @return true only if the model is not being used or if the model does not
     * exist
     */
	boolean canDeleteModel(QName modelName);
}
