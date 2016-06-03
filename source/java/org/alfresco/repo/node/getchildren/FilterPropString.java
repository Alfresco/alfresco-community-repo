package org.alfresco.repo.node.getchildren;

import org.alfresco.service.namespace.QName;

/**
 * GetChildren - for string property filtering
 *
 * @author janv
 * @since 4.0
 */
public class FilterPropString implements FilterProp
{
    public static enum FilterTypeString implements FilterType
    {
        STARTSWITH_IGNORECASE,
        STARTSWITH,
        EQUALS_IGNORECASE,
        EQUALS,
        ENDSWITH_IGNORECASE,
        ENDSWITH,
        MATCHES_IGNORECASE,
        MATCHES        
    }
    
    private QName propName;
    private String propVal;
    private FilterTypeString filterType;
    
    public FilterPropString(QName propName, String propVal, FilterTypeString filterType)
    {
        this.propName = propName;
        this.propVal = propVal;
        this.filterType = filterType;
        
    }
    
    public QName getPropName()
    {
        return propName;
    }
    
    public String getPropVal()
    {
        return propVal;
    }
    
    public FilterType getFilterType()
    {
        return filterType;
    }
}
