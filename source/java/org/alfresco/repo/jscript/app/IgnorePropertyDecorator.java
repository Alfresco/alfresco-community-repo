package org.alfresco.repo.jscript.app;

import java.io.Serializable;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONAware;

/**
 * Ignores a given property and doesn't output anything in the decoration.  This means the property will not appear in the
 * resulting JSON.
 *
 * @author Roy Wetherall
 */
public class IgnorePropertyDecorator extends BasePropertyDecorator
{
    /**
     * @see org.alfresco.repo.jscript.app.PropertyDecorator#decorate(QName, org.alfresco.service.cmr.repository.NodeRef, java.io.Serializable)
     */
    public JSONAware decorate(QName propertyName, NodeRef nodeRef, Serializable value)
    {
        return null;
    }
}
