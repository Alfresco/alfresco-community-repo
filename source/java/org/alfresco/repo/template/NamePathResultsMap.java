package org.alfresco.repo.template;

import java.util.List;
import java.util.StringTokenizer;

import org.alfresco.repo.search.QueryParameterDefImpl;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.search.QueryParameterDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * A special Map that executes an XPath against the parent Node as part of the get()
 * Map interface implementation.
 * 
 * @author Kevin Roast
 */
public class NamePathResultsMap extends BasePathResultsMap
{
    /**
     * Constructor
     * 
     * @param parent         The parent TemplateNode to execute searches from 
     * @param services       The ServiceRegistry to use
     */
    public NamePathResultsMap(TemplateNode parent, ServiceRegistry services)
    {
        super(parent, services);
    }
    
    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public Object get(Object key)
    {
        String path = key.toString();
        StringBuilder xpath = new StringBuilder(path.length() << 1);
        StringTokenizer t = new StringTokenizer(path, "/");
        int count = 0;
        QueryParameterDefinition[] params = new QueryParameterDefinition[t.countTokens()];
        DataTypeDefinition ddText =
            this.services.getDictionaryService().getDataType(DataTypeDefinition.TEXT);
        NamespaceService ns = this.services.getNamespaceService();
        while (t.hasMoreTokens())
        {
            if (xpath.length() != 0)
            {
                xpath.append('/');
            }
            String strCount = Integer.toString(count);
            xpath.append("*[@cm:name=$cm:name")
                 .append(strCount)
                 .append(']');
            params[count++] = new QueryParameterDefImpl(
                    QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, "name" + strCount, ns),
                    ddText,
                    true,
                    t.nextToken());
        }
        
        List<TemplateNode> nodes = getChildrenByXPath(xpath.toString(), params, true);
        
        return (nodes.size() != 0) ? nodes.get(0) : null;
    }
}
