/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.template;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.dtm.ref.DTMNodeProxy;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

/**
 * @author Brian
 * 
 */
public class XSLTProcessorMethodInvoker
{
    private final static Log log = LogFactory.getLog(XSLTProcessorMethodInvoker.class);
    
    private final static Map<String, TemplateProcessorMethod> PROCESSOR_METHODS = new TreeMap<String, TemplateProcessorMethod>();

    public XSLTProcessorMethodInvoker()
    {
    }

    public static void addMethod(String name, TemplateProcessorMethod method)
    {
        PROCESSOR_METHODS.put(name, method);
    }
    
    public static void removeMethods(Collection<String> methodNames)
    {
        for (String methodName : methodNames)
        {
            PROCESSOR_METHODS.remove(methodName);
        }
    }
    
    private Object[] convertArguments(final Object[] arguments)
    {
        final List<Object> result = new LinkedList<Object>();
        for (int i = 0; i < arguments.length; i++)
        {
            log.debug("args[" + i + "] = " + arguments[i] + "("
                    + (arguments[i] != null ? arguments[i].getClass().getName() : "null") + ")");
            if (arguments[i] == null || arguments[i] instanceof String || arguments[i] instanceof Number)
            {
                result.add(arguments[i]);
            }
            else if (arguments[i] instanceof DTMNodeProxy)
            {
                result.add(((DTMNodeProxy) arguments[i]).getStringValue());
            }
            else if (arguments[i] instanceof Node)
            {
                log.debug("node type is " + ((Node) arguments[i]).getNodeType() + " content "
                        + ((Node) arguments[i]).getTextContent());
                result.add(((Node) arguments[i]).getNodeValue());
            }
            else if (arguments[i] instanceof NodeIterator)
            {
                Node n = ((NodeIterator) arguments[i]).nextNode();
                while (n != null)
                {
                    log.debug("iterated to node " + n + " type " + n.getNodeType() + " value " + n.getNodeValue()
                            + " tc " + n.getTextContent() + " nn " + n.getNodeName() + " sv "
                            + ((org.apache.xml.dtm.ref.DTMNodeProxy) n).getStringValue());
                    if (n instanceof DTMNodeProxy)
                    {
                        result.add(((DTMNodeProxy) n).getStringValue());
                    }
                    else
                    {
                        result.add(n);
                    }
                    n = ((NodeIterator) arguments[i]).nextNode();
                }
            }
            else
            {
                throw new IllegalArgumentException("unable to convert argument " + arguments[i]);
            }
        }

        return result.toArray(new Object[result.size()]);
    }

    public Object invokeMethod(final String id, Object[] arguments) throws Exception
    {
        if (!PROCESSOR_METHODS.containsKey(id))
        {
            throw new NullPointerException("unable to find method " + id);
        }

        final TemplateProcessorMethod method = PROCESSOR_METHODS.get(id);
        arguments = this.convertArguments(arguments);
        log.debug("invoking " + id + " with " + arguments.length);

        Object result = method.exec(arguments);
        log.debug(id + " returned a " + result);
        if (result == null)
        {
            return null;
        }
        else if (result.getClass().isArray() && Node.class.isAssignableFrom(result.getClass().getComponentType()))
        {
            log.debug("converting " + result + " to a node iterator");
            final Node[] array = (Node[]) result;
            return new NodeIterator()
            {
                private int index = 0;
                private boolean detached = false;

                public void detach()
                {
                    if (log.isDebugEnabled())
                        log.debug("detaching NodeIterator");
                    this.detached = true;
                }

                public boolean getExpandEntityReferences()
                {
                    return true;
                }

                public int getWhatToShow()
                {
                    return NodeFilter.SHOW_ALL;
                }

                public Node getRoot()
                {
                    return (array.length == 0 ? null : array[0].getOwnerDocument().getDocumentElement());
                }

                public NodeFilter getFilter()
                {
                    return new NodeFilter()
                    {
                        public short acceptNode(final Node n)
                        {
                            return NodeFilter.FILTER_ACCEPT;
                        }
                    };
                }

                public Node nextNode() throws DOMException
                {
                    if (log.isDebugEnabled())
                        log.debug("NodeIterator.nextNode(" + index + ")");
                    if (this.detached)
                        throw new DOMException(DOMException.INVALID_STATE_ERR, null);
                    return index == array.length ? null : array[index++];
                }

                public Node previousNode() throws DOMException
                {
                    if (log.isDebugEnabled())
                        log.debug("NodeIterator.previousNode(" + index + ")");
                    if (this.detached)
                        throw new DOMException(DOMException.INVALID_STATE_ERR, null);
                    return index == -1 ? null : array[index--];
                }
            };
        }
        else if (result instanceof String || result instanceof Number || result instanceof Node)
        {
            log.debug("returning " + result + " as is");
            return result;
        }
        else
        {
            throw new IllegalArgumentException("unable to convert " + result.getClass().getName());
        }
    }
}
