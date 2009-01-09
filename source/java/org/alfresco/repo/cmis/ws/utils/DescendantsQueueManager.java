/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.ws.utils;

import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.repository.ChildAssociationRef;

/**
 * @author Dmitry Velichkevich
 */
public class DescendantsQueueManager
{
    private LinkedList<DescendantElement> queue;

    public DescendantsQueueManager(ChildAssociationRef headAssociation)
    {

        this.queue = new LinkedList<DescendantElement>();
        this.queue.addFirst(createElement(headAssociation, null));
    }

    public DescendantElement createElement(ChildAssociationRef data, DescendantElement parent)
    {

        return new DescendantElement(parent, data);
    }

    public void removeParents(DescendantElement source, List<String> undeletedNodes)
    {

        while (source.getParentElement() != null)
        {
            source = source.getParentElement();

            determineUndeletedObjectToPut(source.getNodesAssociation().getChildRef().toString(), undeletedNodes);

            this.queue.remove(source);
        }
    }

    public void addChildren(List<ChildAssociationRef> children, DescendantElement parent)
    {

        for (ChildAssociationRef child : children)
        {
            this.queue.addFirst(createElement(child, parent));
        }
    }

    /**
     * This method receives and immediately removes next element from the queue
     * 
     * @return next <b>DescendantElement</b> (in this case - first element) in the queue if queue still contain any element or <b>null</b> if queue is empty
     */
    public DescendantElement receiveNextElement()
    {

        DescendantElement result = (this.queue.isEmpty()) ? (null) : (this.queue.getFirst());

        this.queue.remove(result);

        return result;
    }

    public void addElementToQueueEnd(DescendantElement element)
    {

        this.queue.addLast(element);
    }

    public boolean isDepleted()
    {

        return this.queue.isEmpty();
    }

    protected DescendantsQueueManager()
    {
    }

    private void determineUndeletedObjectToPut(String undeletedObjectIdentifier, List<String> undeletedNodes)
    {

        if (!undeletedNodes.contains(undeletedObjectIdentifier))
        {
            undeletedNodes.add(undeletedObjectIdentifier);
        }
    }

    public class DescendantElement
    {
        private DescendantElement parentElement;
        private ChildAssociationRef nodesAssociation;

        public DescendantElement(DescendantElement parentElement, ChildAssociationRef nodesAssociation)
        {

            this.parentElement = parentElement;
            this.nodesAssociation = nodesAssociation;
        }

        public DescendantElement getParentElement()
        {

            return parentElement;
        }

        public ChildAssociationRef getNodesAssociation()
        {

            return nodesAssociation;
        }

        @Override
        public boolean equals(Object obj)
        {

            if (!(obj instanceof DescendantElement))
            {
                return false;
            }

            DescendantElement currentElement = (DescendantElement) obj;

            return (this.nodesAssociation != null) ? (this.nodesAssociation.equals(currentElement.getNodesAssociation())) : (currentElement.getNodesAssociation() == null);
        }

        protected DescendantElement()
        {
        }
    }
}
