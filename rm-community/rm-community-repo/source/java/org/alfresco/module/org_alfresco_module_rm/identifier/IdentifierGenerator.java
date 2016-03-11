package org.alfresco.module.org_alfresco_module_rm.identifier;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 * Generates an identifier for a content type from a given context.
 * 
 * @author Roy Wetherall
 */
public interface IdentifierGenerator
{
    /**
     * The content type this generator is applicible to.
     * @return  QName   the type
     */
    QName getType();
    
    /**
     * Generates the next id based on the provided context.
     * @param context   map of context values
     * @return String   the next id
     */
    String generateId(Map<String, Serializable> context);
}
