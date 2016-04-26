package org.alfresco.repo.node.getchildren;

import java.io.Serializable;

import org.alfresco.service.namespace.QName;

/**
 * GetChildren - for property filtering
 *
 * @author janv
 * @since 4.0
 */
public interface FilterProp
{
    public QName getPropName();
    public Serializable getPropVal();
    public FilterType getFilterType();
}
