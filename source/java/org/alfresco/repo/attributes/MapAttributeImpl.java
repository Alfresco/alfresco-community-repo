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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.alfresco.repo.avm.AVMDAOs;
import org.alfresco.service.cmr.avm.AVMNotFoundException;

/**
 * Persistent map attribute implementation.
 * @author britt
 */
public class MapAttributeImpl extends AttributeImpl implements MapAttribute
{
    private static final long serialVersionUID = -2627849542488029248L;

    public MapAttributeImpl()
    {
    }

    public MapAttributeImpl(MapAttribute attr)
    {
        super(attr.getAcl());
        AVMDAOs.Instance().fAttributeDAO.save(this);
        for (Map.Entry<String, Attribute> entry : attr.entrySet())
        {
            String key = entry.getKey();
            Attribute value = entry.getValue();
            // Use the object's factory for AttributeValue
            Attribute newAttr = value.getAttributeImpl();
            // Persist it
            MapEntryKey keyEntity = new MapEntryKey(this, key);
            MapEntry mapEntry = new MapEntryImpl(keyEntity, newAttr);
            AVMDAOs.Instance().fMapEntryDAO.save(mapEntry);
        }
    }

    public Type getType()
    {
        return Type.MAP;
    }

    public Serializable getRawValue()
    {
        List<MapEntry> entries = AVMDAOs.Instance().fMapEntryDAO.get(this);
        HashMap<String, Serializable> ret = new HashMap<String, Serializable>(entries.size() * 2);
        for (MapEntry entry : entries)
        {
            ret.put(entry.getKey().getKey(), entry.getAttribute().getSerializableValue());
        }
        return ret;
    }

    @Override
    public void clear()
    {
        AVMDAOs.Instance().fMapEntryDAO.delete(this);
    }

    @Override
    public Set<Entry<String, Attribute>> entrySet()
    {
        List<MapEntry> entries = AVMDAOs.Instance().fMapEntryDAO.get(this);
        Map<String, Attribute> map = new HashMap<String, Attribute>();
        for (MapEntry entry : entries)
        {
            map.put(entry.getKey().getKey(), entry.getAttribute());
        }
        return map.entrySet();
    }

    @Override
    public Attribute get(String key)
    {
        MapEntryKey entryKey = new MapEntryKey(this, key);
        MapEntry entry = AVMDAOs.Instance().fMapEntryDAO.get(entryKey);
        if (entry == null)
        {
            return null;
        }
        Attribute attr = entry.getAttribute();
        return attr;
    }

    @Override
    public Set<String> keySet()
    {
        List<MapEntry> entries = AVMDAOs.Instance().fMapEntryDAO.get(this);
        Set<String> keys = new HashSet<String>();
        for (MapEntry entry : entries)
        {
            keys.add(entry.getKey().getKey());
        }
        return keys;
    }

    @Override
    public void put(String key, Attribute value)
    {
        MapEntryKey entryKey = new MapEntryKey(this, key);
        MapEntry entry = AVMDAOs.Instance().fMapEntryDAO.get(entryKey);
        if (entry != null)
        {
            Attribute oldAttr = entry.getAttribute();
            entry.setAttribute(value);
            AVMDAOs.Instance().fAttributeDAO.delete(oldAttr);
            return;
        }
        entry = new MapEntryImpl(entryKey, value);
        AVMDAOs.Instance().fMapEntryDAO.save(entry);
    }

    @Override
    public void remove(String key)
    {
        MapEntryKey entryKey = new MapEntryKey(this, key);
        MapEntry entry = AVMDAOs.Instance().fMapEntryDAO.get(entryKey);
        if (entry == null)
        {
            throw new AVMNotFoundException("Attribute Not Found: " + key);
        }
        Attribute attr = entry.getAttribute();
        AVMDAOs.Instance().fMapEntryDAO.delete(entry);
        AVMDAOs.Instance().fAttributeDAO.delete(attr);
    }

    @Override
    public Collection<Attribute> values()
    {
        List<MapEntry> entries = AVMDAOs.Instance().fMapEntryDAO.get(this);
        List<Attribute> attrs = new ArrayList<Attribute>();
        for (MapEntry entry : entries)
        {
            attrs.add(entry.getAttribute());
        }
        return attrs;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        for (Map.Entry<String, Attribute> entry : entrySet())
        {
            builder.append(entry.getKey());
            builder.append('=');
            builder.append(entry.getValue().toString());
            builder.append(' ');
        }
        builder.append('}');
        return builder.toString();
    }

    @Override
    public int size()
    {
        return AVMDAOs.Instance().fMapEntryDAO.size(this);
    }
}
