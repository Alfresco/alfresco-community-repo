package org.alfresco.module.org_alfresco_module_rm.identifier;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Basic identifier generator implementation.
 *
 * @author Roy Wetherall
 */
public class BasicIdentifierGenerator extends IdentifierGeneratorBase
{
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.identifier.IdentifierGenerator#generateId(java.util.Map)
     */
    @Override
    public String generateId(Map<String, Serializable> context)
    {
        NodeRef nodeRef = (NodeRef)context.get(IdentifierService.CONTEXT_NODEREF);
        Long dbId = 0l;
        if (nodeRef != null)
        {
            dbId = (Long)nodeService.getProperty(nodeRef, ContentModel.PROP_NODE_DBID);
        }
        else
        {
            dbId = System.currentTimeMillis();
        }

        Calendar fileCalendar = Calendar.getInstance();
        String year = Integer.toString(fileCalendar.get(Calendar.YEAR));
        return year + "-" + padString(dbId.toString(), 10);
    }
}
