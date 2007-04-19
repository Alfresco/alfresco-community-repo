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
        if (keys.size() < 1)
        {
            throw new AVMBadArgumentException("Bad Attribute Path: " + path);
        }
        GlobalAttributeEntry entry = fGlobalAttributeEntryDAO.get(keys.get(0));
        if (entry == null)
        {
            return null;
        }
        Attribute current = entry.getAttribute();
        for (int i = 1; i < keys.size(); i++)
        {
            if (current.getType() != Type.MAP)
            {
                return null;
            }
            current = current.get(keys.get(i));
            if (current == null)
            {
                return null;
            }
        }
        return fAttributeConverter.toValue(current);
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
        if (keys.size() == 0)
        {
            throw new AVMBadArgumentException("Cannot query top level Attributes.");
        }
        GlobalAttributeEntry entry = fGlobalAttributeEntryDAO.get(keys.get(0));
        if (entry == null)
        {
            throw new AVMNotFoundException("Attribute Not Found: " + keys.get(0));
        }
        Attribute current = entry.getAttribute();
        if (current.getType() != Type.MAP)
        {
            throw new AVMWrongTypeException("Attribute Not Map: " + keys.get(0));
        }
        for (int i = 1; i < keys.size(); i++)
        {
            current = current.get(keys.get(i));
            if (current == null)
            {
                throw new AVMNotFoundException("Attribute Not Found: " + keys.get(i));
            }
            if (current.getType() != Type.MAP)
            {
                throw new AVMWrongTypeException("Attribute Not Map: " + keys.get(i));
            }
        }
        List<Pair<String, Attribute>> rawResult =
            fAttributeDAO.find((MapAttribute)current, query);
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
     * @see org.alfresco.service.cmr.attributes.AttributeService#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String path, String name)
    {
        if (path == null)
        {
            throw new AVMBadArgumentException("Null Attribute Path.");
        }
        List<String> keys = parsePath(path);
        if (keys.size() == 0)
        {
            fGlobalAttributeEntryDAO.delete(name);
            return;
        }
        GlobalAttributeEntry entry = fGlobalAttributeEntryDAO.get(keys.get(0));
        if (entry == null)
        {
            throw new AVMNotFoundException("Attribute Not Found: " + keys.get(0));
        }
        Attribute current = entry.getAttribute();
        if (current.getType() != Type.MAP)
        {
            throw new AVMWrongTypeException("Attribute Not Map: " + keys.get(0));
        }
        for (int i = 1; i < keys.size(); i++)
        {
            current = current.get(keys.get(i));
            if (current == null)
            {
                throw new AVMNotFoundException("Attribute Not Found: " + keys.get(i));
            }
            if (current.getType() != Type.MAP)
            {
                throw new AVMWrongTypeException("Attribute Not Map: " + keys.get(i));
            }
        }
        current.remove(name);
    }

    /* (non-Javadoc)
     * @see org.alfresco.service.cmr.attributes.AttributeService#setAttribute(java.lang.String, org.alfresco.repo.attributes.Attribute)
     */
    public void setAttribute(String path, String name, Attribute value)
    {
        List<String> keys = parsePath(path);
        Attribute toSave = fAttributeConverter.toPersistent(value);
        if (keys.size() == 0)
        {
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
        GlobalAttributeEntry gEntry = fGlobalAttributeEntryDAO.get(keys.get(0));
        if (gEntry == null)
        {
            throw new AVMNotFoundException("Global Attribute Not Found: " + keys.get(0));
        }
        Attribute current = gEntry.getAttribute();
        if (current.getType() != Type.MAP)
        {
            throw new AVMWrongTypeException("Global Attribute Not Map: " + keys.get(0)); 
        }
        for (int i = 1; i < keys.size(); i++)
        {
            Attribute child = current.get(keys.get(i));
            if (child == null)
            {
                throw new AVMNotFoundException("Attribute Not Found: " + keys.get(i));
            }
            if (child.getType() != Type.MAP)
            {
                throw new AVMWrongTypeException("Attribute Not Map: " + keys.get(i));
            }
            current = child;
        }
        current.put(name, toSave);   
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
        if (keys.size() == 0)
        {
            return fGlobalAttributeEntryDAO.getKeys();
        }
        GlobalAttributeEntry entry = fGlobalAttributeEntryDAO.get(keys.get(0));
        if (entry == null)
        {
            throw new AVMNotFoundException("Attribute Not Found: " + keys.get(0));
        }
        Attribute current = entry.getAttribute();
        if (current.getType() != Type.MAP)
        {
            throw new AVMWrongTypeException("Attribute Not Map: " + keys.get(0));
        }
        for (int i = 1; i < keys.size(); i++)
        {
            current = current.get(keys.get(i));
            if (current == null)
            {
                throw new AVMNotFoundException("Attribute Not Found: " + keys.get(i));
            }
            if (current.getType() != Type.MAP)
            {
                throw new AVMWrongTypeException("Attribute Not Map: " + keys.get(i));
            }
        }
        return new ArrayList<String>(current.keySet());
    }
}

