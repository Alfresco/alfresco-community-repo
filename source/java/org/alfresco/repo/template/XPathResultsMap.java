package org.alfresco.repo.template;

import org.alfresco.service.ServiceRegistry;

/**
 * A special Map that executes an XPath against the parent Node as part of the get()
 * Map interface implementation.
 * 
 * @author Kevin Roast
 */
public class XPathResultsMap extends BasePathResultsMap
{
   /**
    * Constructor
    * 
     * @param parent         The parent TemplateNode to execute searches from 
     * @param services       The ServiceRegistry to use
    */
   public XPathResultsMap(TemplateNode parent, ServiceRegistry services)
   {
      super(parent, services);
   }
   
   /**
    * @see java.util.Map#get(java.lang.Object)
    */
   public Object get(Object key)
   {
      return getChildrenByXPath(key.toString(), null, false);
   }
}
