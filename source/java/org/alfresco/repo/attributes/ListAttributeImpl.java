/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.attributes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.service.cmr.avm.AVMBadArgumentException;

/**
 * Persistent implementation of a list attribute.
 * @author britt
 */
public class ListAttributeImpl extends AttributeImpl implements ListAttribute
{
    private static final long serialVersionUID = -394553378173857035L;

    public ListAttributeImpl()
    {
    }
    
    public ListAttributeImpl(ListAttribute other)
    {
        super(other.getAcl());
        int index = 0;
        AVMDAOs.Instance().fAttributeDAO.save(this);
        for (Attribute entry : other)
        {
            Attribute newEntry = null;
            switch (entry.getType())
            {
                case BOOLEAN :
                {
                    newEntry = new BooleanAttributeImpl((BooleanAttribute)entry);
                    break;
                }
                case BYTE :
                {
                    newEntry = new ByteAttributeImpl((ByteAttribute)entry);
                    break;
                }
                case SHORT :
                {
                    newEntry = new ShortAttributeImpl((ShortAttribute)entry);
                    break;
                }
                case INT :
                {
                    newEntry = new IntAttributeImpl((IntAttribute)entry);
                    break;
                }
                case LONG :
                {
                    newEntry = new LongAttributeImpl((LongAttribute)entry);
                    break;
                }
                case FLOAT :
                {
                    newEntry = new FloatAttributeImpl((FloatAttribute)entry);
                    break;
                }
                case DOUBLE :
                {
                    newEntry = new DoubleAttributeImpl((DoubleAttribute)entry);
                    break;
                }
                case STRING :
                {
                    newEntry = new StringAttributeImpl((StringAttribute)entry);
                    break;
                }
                case SERIALIZABLE :
                {
                    newEntry = new SerializableAttributeImpl((SerializableAttribute)entry);
                    break;
                }
                case MAP :
                {
                    newEntry = new MapAttributeImpl((MapAttribute)entry);
                    break;
                }
                case LIST :
                {
                    newEntry = new ListAttributeImpl((ListAttribute)entry);
                    break;
                }
                default :
                {
                    throw new AlfrescoRuntimeException("Unknown Attribute Type: " + entry.getType());
                }
            }
            ListEntryKey key = new ListEntryKey(this, index++);
            ListEntry listEntry = new ListEntryImpl(key, newEntry);
            AVMDAOs.Instance().fListEntryDAO.save(listEntry);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.Attribute#getType()
     */
    public Type getType()
    {
        return Type.LIST;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#add(org.alfresco.repo.attributes.Attribute)
     */
    @Override
    public void add(Attribute attr)
    {
        int size = AVMDAOs.Instance().fListEntryDAO.size(this);
        ListEntryKey key = new ListEntryKey(this, size);
        ListEntry entry = new ListEntryImpl(key, attr);
        AVMDAOs.Instance().fListEntryDAO.save(entry);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#add(int, org.alfresco.repo.attributes.Attribute)
     */
    @Override
    public void add(int index, Attribute attr)
    {
        ListEntryDAO dao = AVMDAOs.Instance().fListEntryDAO;
        int size = dao.size(this);
        if (index > size || index < 0)
        {
            throw new AVMBadArgumentException("Index out of bounds: " + index);
        }
        for (int i = size; i > index; i--)
        {
            ListEntryKey key = new ListEntryKey(this, i - 1);
            ListEntry entry = dao.get(key);
            key = new ListEntryKey(this, i);
            ListEntry newEntry = new ListEntryImpl(key, entry.getAttribute());
            dao.delete(entry);
            dao.save(newEntry);
        }
        ListEntryKey key = new ListEntryKey(this, index);
        ListEntry newEntry = new ListEntryImpl(key, attr);
        dao.save(newEntry);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#clear()
     */
    @Override
    public void clear()
    {
        AVMDAOs.Instance().fListEntryDAO.delete(this);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#get(int)
     */
    @Override
    public Attribute get(int index)
    {
        ListEntryKey key = new ListEntryKey(this, index);
        ListEntry entry = AVMDAOs.Instance().fListEntryDAO.get(key);
        if (entry == null)
        {
            return null;
        }
        return entry.getAttribute();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#iterator()
     */
    @Override
    public Iterator<Attribute> iterator()
    {
        List<ListEntry> entries = AVMDAOs.Instance().fListEntryDAO.get(this);
        List<Attribute> attrList = new ArrayList<Attribute>();
        for (ListEntry entry : entries)
        {
            attrList.add(entry.getAttribute());
        }
        return attrList.iterator();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#size()
     */
    @Override
    public int size()
    {
        return AVMDAOs.Instance().fListEntryDAO.size(this);
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#remove(java.lang.String)
     */
    @Override
    public void remove(int index)
    {
        ListEntryDAO dao = AVMDAOs.Instance().fListEntryDAO;
        ListEntryKey key = new ListEntryKey(this, index);
        ListEntry entry = dao.get(key);
        if (entry == null)
        {
            throw new AVMBadArgumentException("Index out of bounds: " + index);
        }
        int size = dao.size(this);
        dao.delete(entry);
        AVMDAOs.Instance().fAttributeDAO.delete(entry.getAttribute());
        for (int i = index; i < size - 1; i++)
        {
            key = new ListEntryKey(this, i + 1);
            entry = dao.get(key);
            key = new ListEntryKey(this, i);
            ListEntry newEntry = new ListEntryImpl(key, entry.getAttribute());
            dao.delete(entry);
            dao.save(newEntry);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (Attribute child : this)
        {
            builder.append(child.toString());
            builder.append(' ');
        }
        builder.append(']');
        return builder.toString();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.attributes.AttributeImpl#set(int, org.alfresco.repo.attributes.Attribute)
     */
    @Override
    public void set(int index, Attribute value)
    {
        ListEntryKey key = new ListEntryKey(this, index);
        ListEntry entry = AVMDAOs.Instance().fListEntryDAO.get(key);
        if (entry == null)
        {
            throw new AVMBadArgumentException("Index out of bounds: " + index);
        }
        Attribute oldAttr = entry.getAttribute();
        entry.setAttribute(value);
        AVMDAOs.Instance().fAttributeDAO.delete(oldAttr);
    }
}
