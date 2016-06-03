package org.alfresco.web.bean;

import java.io.Serializable;

import org.alfresco.service.namespace.QName;
import org.alfresco.web.bean.repository.Node;

/**
 * @author Kevin Roast
 */
public interface NodeEventListener extends Serializable
{
   /**
    * Callback executed when a Node wrapped object is created. This is generally used
    * to add additional property resolvers to the Node for a specific model type.
    * 
    * @param node    The Node wrapper that has been created
    * @param type    Type of the Node that has been created
    */
   public void created(Node node, QName type);
}
