package org.alfresco.repo.node.getchildren;

import org.alfresco.service.namespace.QName;

/**
 * GetChildren - for boolean property filtering
 *
 * @author Nick Burch
 * @since Odin
 */
public class FilterPropBoolean implements FilterProp
{
    private QName propName;
    private Boolean propVal;
    
    public FilterPropBoolean(QName propName, Boolean propVal)
    {
        this.propName = propName;
        this.propVal = propVal;
        
    }
    
    public QName getPropName()
    {
        return propName;
    }
    
    public Boolean getPropVal()
    {
        return propVal;
    }

    public FilterType getFilterType()
    {
        // There is only the one type
        return null;
    }
}
