package org.alfresco.web.bean.repository;

import java.io.Serializable;

/**
 * Simple interface used to implement small classes capable of calculating dynamic property values
 * for Nodes at runtime. This allows bean responsible for building large lists of Nodes to
 * encapsulate the code needed to retrieve non-standard Node properties. The values are then
 * calculated on demand by the property resolver.
 * 
 * When a node is reset() the standard and other props are cleared. If property resolvers are used
 * then the non-standard props will be restored automatically as well. 
 * 
 * @author Kevin Roast
 */
public interface NodePropertyResolver extends Serializable
{
   /**
    * Get the property value for this resolver
    * 
    * @param node       Node this property is for
    * 
    * @return property value
    */
   public Object get(Node node);
}
