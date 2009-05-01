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
        this.queue.addFirst(createElement(null, headAssociation));
    }

    protected DescendantsQueueManager()
    {
    }

    public boolean isEmpty()
    {
        return this.queue.isEmpty();
    }

    /**
     * This method gets and immediately removes next element from the queue
     * 
     * @return next <b>DescendantElement</b> (in this case - first element) in the queue if queue still contain any element or <b>null</b> if queue is empty
     */
    public DescendantElement getNextElement()
    {
        DescendantElement result = queue.isEmpty() ? null : queue.getFirst();
        queue.remove(result);
        return result;
    }

    public void addFirst(DescendantElement parent, List<ChildAssociationRef> children)
    {
        for (ChildAssociationRef child : children)
        {
            queue.addFirst(createElement(parent, child));
        }
    }

    private DescendantElement createElement(DescendantElement parent, ChildAssociationRef child)
    {
        return new DescendantElement(parent, child);
    }

    public void addLast(DescendantElement element)
    {
        queue.addLast(element);
    }

    public void removeParents(DescendantElement element, List<String> undeletedNodes)
    {

        while (element.getParent() != null)
        {
            element = element.getParent();
            String child = element.getChildAssoc().getChildRef().toString();
            if (!undeletedNodes.contains(child))
            {
                undeletedNodes.add(child);
            }

            queue.remove(element);
        }
    }

    public class DescendantElement
    {
        private DescendantElement parent;
        private ChildAssociationRef childAssoc;

        public DescendantElement(DescendantElement parent, ChildAssociationRef childAssoc)
        {
            this.parent = parent;
            this.childAssoc = childAssoc;
        }

        protected DescendantElement()
        {
        }

        public DescendantElement getParent()
        {
            return parent;
        }

        public ChildAssociationRef getChildAssoc()
        {
            return childAssoc;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof DescendantElement))
            {
                return false;
            }

            DescendantElement currentElement = (DescendantElement) obj;
            return (childAssoc != null) ? (childAssoc.equals(currentElement.getChildAssoc())) : (currentElement.getChildAssoc() == null);
        }
    }

}
