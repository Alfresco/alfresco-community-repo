package org.alfresco.module.org_alfresco_module_rm.search;

import org.alfresco.service.namespace.QName;

/*package*/ class SortItem
{
    public QName property = null;
    public boolean assc = true;
    public SortItem(QName property, boolean assc)
    {
        this.property = property;
        this.assc = assc;
    }

}
