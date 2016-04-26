package org.alfresco.repo.jscript.app;

import java.io.Serializable;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONAware;

/**
 * Interface for property decorators used by ApplicationScriptUtils.toJSON()
 *
 * @author Mike Hatfield
 */
public interface PropertyDecorator
{
    Set<QName> getPropertyNames();
    
    JSONAware decorate(QName propertyName, NodeRef nodeRef, Serializable value);
}
