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
import java.util.List;
import java.util.Map;

import org.alfresco.repo.attributes.Attribute.Type;
import org.alfresco.service.cmr.attributes.AttrQuery;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.avm.AVMWrongTypeException;
import org.alfresco.util.Pair;

/**
 * Implementation of the AttributeService interface.
 * @author britt
 */
public class AttributeServiceImpl implements AttributeService
{
    private GlobalAttributeEntryDAO fGlobalAttributeEntryDAO;
    
    private AttributeDAO fAttributeDAO;
    
    private AttributeConverter fAttributeConverter;
    
    public AttributeServiceImpl()
    {
    }
    
    public void setGlobalAttributeEntryDao(GlobalAttributeEntryDAO dao)
    {
        fGlobalAttributeEntryDAO = dao;
    }
    
    public void setAttributeDao(AttributeDAO dao)
    {
        fAttributeDAO = dao;
    }
    
    public void setAttributeConverter(AttributeConverter converter)
    {
        fAttributeConverter = converter;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#getAttribute(java.lang.String)
     */
    public Attribute getAttribute(String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        List<String> keys = parsePath(path);
        return getAttribute(keys);
    }

    /**
     * Utility to parse paths.  Paths are of the form '/name/name'. '\' can
     * be used to escape '/'s.
     * @param path The path to parse.
     * @return The components of the path.
     */
    private List<String> parsePath(String path)
    {
        List<String> components = new ArrayList<String>();
        int off = 0;
        while (off < path.length())
        {
            while (off < path.length() && path.charAt(off) == '/') 
            {
                off++;
            }
            StringBuilder builder = new StringBuilder();
            while (off < path.length())
            {
                char c = path.charAt(off);
                if (c == '/')
                {
                    break;
                }
                if (c == '\\')
                {
                    off++;
                    if (off >= path.length())
                    {
                        break;
                    }
                    c = path.charAt(off);
                }
                builder.append(c);
                off++;
            }
            components.add(builder.toString());
        }
        return components;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#query(java.lang.String, org.alfresco.service.cmr.attributes.AttrQuery)
     */
    public List<Pair<String, Attribute>> query(String path, AttrQuery query)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null Attribute Path.");
        }
        List<String> keys = parsePath(path);
        return query(keys, query);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String path, String name)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null Attribute Path.");
        }
        List<String> keys = parsePath(path);
        removeAttribute(keys, name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#setAttribute(java.lang.String, org.alfresco.repo.attributes.Attribute)
     */
    public void setAttribute(String path, String name, Attribute value)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null path.");
        }
        List<String> keys = parsePath(path);
        setAttribute(keys, name, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#getKeys(java.lang.String)
     */
    public List<String> getKeys(String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null Attribute Path.");
        }
        List<String> keys = parsePath(path);
        return getKeys(keys);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#getAttribute(java.util.List)
     */
    public Attribute getAttribute(List<String> keys)
    {
        if (keys == null)
        {
            throw new AVMBadArgumentException("Null Attribute Path List.");
        }
        if (keys.size() < 1)
        {
            throw new AVMBadArgumentException("Bad Attribute Path List.");
        }
        Attribute found = getAttributeFromPath(keys);
        if (found == null)
        {
            return null;
        }
        return fAttributeConverter.toValue(found);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#getKeys(java.util.List)
     */
    public List<String> getKeys(List<String> keys)
    {
        if (keys == null)
        {
            throw new AVMBadArgumentException("Null Keys List.");
        }
        if (keys.size() == 0)
        {
            return fGlobalAttributeEntryDAO.getKeys();
        }
        Attribute found = getAttributeFromPath(keys);
        if (found == null)
        {
            throw new AVMNotFoundException("Attribute Not Found: " + keys);
        }
        if (found.getType() != Type.MAP)
        {
            throw new AVMWrongTypeException("Not a Map: " + keys.get(keys.size() - 1));
        }
        return new ArrayList<String>(found.keySet());
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#setAttribute(java.util.List, java.lang.String, org.alfresco.repo.attributes.Attribute)
     */
    public void setAttribute(List<String> keys, String name, Attribute value)
    {
        if (keys == null || name == null || value == null)
        {
            throw new AVMBadArgumentException("Null argument.");
        }
        if (keys.size() == 0)
        {
            Attribute toSave = fAttributeConverter.toPersistent(value);
            GlobalAttributeEntry found = fGlobalAttributeEntryDAO.get(name);
            if (found == null)
            {
                found = new GlobalAttributeEntryImpl(name, toSave);
                fGlobalAttributeEntryDAO.save(found);
                return;
            }
            found.setAttribute(toSave);
            return;
        }
        Attribute found = getAttributeFromPath(keys);
        if (found == null)
        {
            throw new AVMNotFoundException("Attribute Not Found: " + keys);
        }
        if (found.getType() != Type.MAP)
        {
            throw new AVMWrongTypeException("Not a Map: " + keys);
        }
        found.put(name, fAttributeConverter.toPersistent(value));   
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#query(java.util.List, org.alfresco.service.cmr.attributes.AttrQuery)
     */
    public List<Pair<String, Attribute>> query(List<String> keys, AttrQuery query)
    {
        if (keys == null || query == null)
        {
            throw new AVMBadArgumentException("Null argument.");
        }
        if (keys.size() == 0)
        {
            throw new AVMBadArgumentException("Cannot query top level Attributes.");
        }
        Attribute found = getAttributeFromPath(keys);
        if (found == null)
        {
            throw new AVMNotFoundException("Attribute Not Found: " + keys);
        }
        if (found.getType() != Type.MAP)
        {
            throw new AVMWrongTypeException("Not a Map: " + keys);
        }
        List<Pair<String, Attribute>> rawResult =
            fAttributeDAO.find((MapAttribute)found, query);
        List<Pair<String, Attribute>> result = 
            new ArrayList<Pair<String, Attribute>>();
        for (Pair<String, Attribute> raw : rawResult)
        {
            result.add(new Pair<String, Attribute>(raw.getFirst(), 
                                                   fAttributeConverter.toValue(raw.getSecond())));
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#removeAttribute(java.util.List, java.lang.String)
     */
    public void removeAttribute(List<String> keys, String name)
    {
        if (keys == null || name == null)
        {
            throw new AVMBadArgumentException("Null argument.");
        }
        if (keys.size() == 0)
        {
            fGlobalAttributeEntryDAO.delete(name);
            return;
        }
        Attribute found = getAttributeFromPath(keys);
        if (found == null)
        {
            throw new AVMNotFoundException("Attribute Not Found: " + keys);
        }
        if (found.getType() != Type.MAP)
        {
            throw new AVMWrongTypeException("Attribute Not Map: " + keys);
        }
        found.remove(name);
    }
    
    private Attribute getAttributeFromPath(List<String> keys)
    {
        GlobalAttributeEntry entry = fGlobalAttributeEntryDAO.get(keys.get(0));
        if (entry == null)
        {
            return null;
        }
        Attribute current = entry.getAttribute();
        for (int i = 1; i < keys.size(); i++)
        {
            if (current.getType() == Type.MAP)
            {
                current = current.get(keys.get(i));
            }
            else if (current.getType() == Type.LIST)
            {
                current = current.get(Integer.parseInt(keys.get(i)));
            }
            else
            {
                throw new AVMWrongTypeException("Not a Map or List: " + keys.get(i - 1));
            }
            if (current == null)
            {
                return null;
            }
        }
        return current;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#addAttribute(java.util.List, org.alfresco.repo.attributes.Attribute)
     */
    public void addAttribute(List<String> keys, Attribute value)
    {
        if (keys == null || value == null)
        {
            throw new AVMBadArgumentException("Illegal Null Argument.");
        }
        if (keys.size() < 1)
        {
            throw new AVMBadArgumentException("Path too short: " + keys);
        }
        Attribute found = getAttributeFromPath(keys);
        if (found == null)
        {
            throw new AVMNotFoundException("Attribute Not Found: " + keys);
        }
        if (found.getType() != Type.LIST)
        {
            throw new AVMWrongTypeException("Attribute Not List: " + keys);
        }
        found.add(fAttributeConverter.toPersistent(value));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#addAttribute(java.lang.String, org.alfresco.repo.attributes.Attribute)
     */
    public void addAttribute(String path, Attribute value)
    {
        if (path == null || value == null)
        {
            throw new AVMBadArgumentException("Illegal null arguments.");
        }
        List<String> keys = parsePath(path);
        addAttribute(keys, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#removeAttribute(java.util.List, int)
     */
    public void removeAttribute(List<String> keys, int index)
    {
        if (keys == null)
        {
            throw new AVMBadArgumentException("Illegal Null Keys.");
        }
        if (keys.size() < 1)
        {
            throw new AVMBadArgumentException("Keys too short: " + keys);
        }
        Attribute found = getAttributeFromPath(keys);
        if (found == null)
        {
            throw new AVMNotFoundException("Attribute Not Found: " + keys);
        }
        if (found.getType() != Type.LIST)
        {
            throw new AVMWrongTypeException("Attribute Not List: " + keys);
        }
        found.remove(index);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#removeAttribute(java.lang.String, int)
     */
    public void removeAttribute(String path, int index)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Illegal null path.");
        }
        List<String> keys = parsePath(path);
        removeAttribute(keys, index);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#removeEntries(java.util.List, org.alfresco.service.cmr.attributes.AttrQuery)
     */
    public void removeEntries(List<String> keys, AttrQuery query)
    {
        if (keys == null || query == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        if (keys.size() == 0)
        {
            throw new AVMBadArgumentException("Illegal zero length key path.");
        }
        Attribute map = getAttributeFromPath(keys);
        if (map == null)
        {
            throw new AVMNotFoundException("Could not find attribute: " + keys);
        }
        if (map.getType() != Attribute.Type.MAP)
        {
            throw new AVMWrongTypeException("Not a map: " + keys);
        }
        fAttributeDAO.delete((MapAttribute)map, query);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#removeEntries(java.lang.String, org.alfresco.service.cmr.attributes.AttrQuery)
     */
    public void removeEntries(String path, AttrQuery query)
    {
        if (path == null || query == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        List<String> keys = parsePath(path);
        removeEntries(keys, query);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#setAttribute(java.util.List, int, org.alfresco.repo.attributes.Attribute)
     */
    public void setAttribute(List<String> keys, int index, Attribute value)
    {
        if (keys == null || value == null)
        {
            throw new AVMBadArgumentException("Illegal Null Argument.");
        }
        if (keys.size() < 1)
        {
            throw new AVMBadArgumentException("Keys too short.");
        }
        Attribute found = getAttributeFromPath(keys);
        if (found == null)
        {
            throw new AVMNotFoundException("Attribute Not Found: " + keys);
        }
        if (found.getType() != Type.LIST)
        {
            throw new AVMWrongTypeException("Attribute Not List: " + keys);
        }
        found.set(index, fAttributeConverter.toPersistent(value));
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#setAttribute(java.lang.String, int, org.alfresco.repo.attributes.Attribute)
     */
    public void setAttribute(String path, int index, Attribute value)
    {
        if (path == null || value == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        List<String> keys = parsePath(path);
        setAttribute(keys, index, value);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#exists(java.util.List)
     */
    public boolean exists(List<String> keys)
    {
        if (keys == null)
        {
            throw new AVMBadArgumentException("Null keys list.");
        }
        if (keys.size() == 0)
        {
            throw new AVMBadArgumentException("Illegal zero length keys list.");
        }
        return getAttributeFromPath(keys) != null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#exists(java.lang.String)
     */
    public boolean exists(String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null attribute path.");
        }
        List<String> keys = parsePath(path);
        return exists(keys);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#getCount(java.util.List)
     */
    public int getCount(List<String> keys)
    {
        if (keys == null)
        {
            throw new AVMBadArgumentException("Null keys list.");
        }
        if (keys.size() == 0)
        {
            throw new AVMBadArgumentException("Illegal empty keys list.");
        }
        Attribute found = getAttributeFromPath(keys);
        if (found == null)
        {
            throw new AVMNotFoundException("Attribute not found: " + keys);
        }
        if (found.getType() != Attribute.Type.LIST &&
            found.getType() != Attribute.Type.MAP)
        {
            throw new AVMWrongTypeException("Not a map or list: " + keys);
        }
        return found.size();
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#getCount(java.lang.String)
     */
    public int getCount(String path)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null attribute path.");
        }
        List<String> keys = parsePath(path);
        return getCount(keys);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#addAttributes(java.util.List, java.util.List)
     */
    public void addAttributes(List<String> keys, List<Attribute> values)
    {
        if (keys == null || values == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        if (keys.size() == 0)
        {
            throw new AVMBadArgumentException("Zero length keys.");
        }
        Attribute list = getAttributeFromPath(keys);
        if (list.getType() != Attribute.Type.LIST)
        {
            throw new AVMWrongTypeException("Attribute not list: " + list.getType());
        }
        for (Attribute value : values)
        {
            Attribute persistent = fAttributeConverter.toPersistent(value);
            list.add(persistent);
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#addAttributes(java.lang.String, java.util.List)
     */
    public void addAttributes(String path, List<Attribute> values)
    {
        if (path == null || values == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        List<String> keys = parsePath(path);
        addAttributes(keys, values);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#setAttributes(java.util.List, java.util.Map)
     */
    public void setAttributes(List<String> keys, Map<String, Attribute> entries)
    {
        if (keys == null || entries == null)
        {
            throw new AVMBadArgumentException("Null argument.");
        }
        if (keys.size() == 0)
        {
            for (Map.Entry<String, Attribute> entry : entries.entrySet())
            {
                String name = entry.getKey();
                Attribute value = entry.getValue();
                Attribute toSave = fAttributeConverter.toPersistent(value);
                GlobalAttributeEntry found = fGlobalAttributeEntryDAO.get(name);
                if (found == null)
                {
                    found = new GlobalAttributeEntryImpl(name, toSave);
                    fGlobalAttributeEntryDAO.save(found);
                    return;
                }
                found.setAttribute(toSave);
            }
            return;
        }
        Attribute found = getAttributeFromPath(keys);
        if (found == null)
        {
            throw new AVMNotFoundException("Attribute Not Found: " + keys);
        }
        if (found.getType() != Type.MAP)
        {
            throw new AVMWrongTypeException("Not a Map: " + keys);
        }
        for (Map.Entry<String, Attribute> entry : entries.entrySet())
        {
            String name = entry.getKey();
            Attribute value = entry.getValue();
            found.put(name, fAttributeConverter.toPersistent(value));
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#setAttributes(java.lang.String, java.util.Map)
     */
    public void setAttributes(String path, Map<String, Attribute> entries)
    {
        if (path == null || entries == null)
        {
            throw new AVMBadArgumentException("Illegal null argument.");
        }
        List<String> keys = parsePath(path);
        setAttributes(keys, entries);
    }
}

