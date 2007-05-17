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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.service.cmr.attributes;

import java.util.List;

import org.alfresco.repo.attributes.Attribute;
import org.alfresco.util.Pair;

/**
 * This provides services for reading, writing, and querying global attributes.
 * <p>
 * Attributes are organized hierarchically.
 * Each segment within the hierarchy is referred to as a "key".  
 * Keys are indexed so that they may be queried efficiently.  
 * Because databases may impose length restrictions 
 * on index of primary keys, you are strongly advised to keep 
 * "large" strings in <em>values</em>, not <em>keys</em>.  
 * For example, 
 * http://dev.mysql.com/tech-resources/crash-me.php reports
 * that the index length limit of MySQL-4.1.1pre InnoDB is 1024,
 * bytes.  Assuming keys are stored in UTF8, this means keys 
 * should be no longer 170 chars (170*6 + 1 = 1020 < 1024).
 * <p>
 *
 * When an attribute within the hierarchy is represented as a "path", 
 * the set of keys used to reach it is concatenated using the '/' character.
 * Thus, "a/b/c" refers to attribute "c" within "b" within "a".
 * This "path" notation is merely a convenience; if you prefer,
 * lower-level functions can also be used that allow you to 
 * supply the list of keys directly.   If you need to create a "path"
 * that includes a key with an embedded '/' character, you must 
 * escape it with '\' (e.g.:  "silly\/example").   No such restriction
 * applies when you use an API that accepts a list of keys directly.
 * <p>
 * Lookups for attributes never attempt to search any other
 * leaf key (final path segment) than the one specified
 * function call.  Thus, if you have an attribute named
 * "egg", but no attribute named "hen/egg", a lookup for
 * "egg" will suceed, but a lookup for "hen/egg" will fail 
 * (i.e.: it will return null).  
 *
 * @author britt
 */
public interface AttributeService 
{
    /**
     * Get an Attribute using a path.
     *
     * @param path The path of the Attribute
     * @return The value of the attribute or null.
     */
    public Attribute getAttribute(String path);
    
    /**
     * Get an attribute using a list of keys.
     *
     * @param keys List of attribute path keys (path components).
     * @return The value of the attribute or null.
     */
    public Attribute getAttribute(List<String> keys);
    
    /**
     * Set an attribute, overwriting its prior value if it already existed.
     *
     * @param name The name of the Attribute.
     * @param value The value to set.
     */
    public void setAttribute(String path, String name, Attribute value);
    
    /**
     * Set an attribute, overwriting its prior value if it already existed.
     *
     * @param keys List of attribute path keys (path components).
     * @param name The name of the attribute to set.
     * @param value The Attribute to set.
     */
    public void setAttribute(List<String> keys, String name, Attribute value);
    
    /**
     * Set an attribute in a list.
     *
     * @param path The path to the {@link org.alfresco.repo.attributes.ListAttribute ListAttribute}.
     * @param index The list index.
     * @param value The Attribute to set.
     */
    public void setAttribute(String path, int index, Attribute value);
    
    /**
     * Set an attribute in a list.
     * @param keys List of attribute path keys (path components).
     * @param index The list index.
     * @param value The Attribute to set within the {@link org.alfresco.repo.attributes.ListAttribute ListAttribute}
     */
    public void setAttribute(List<String> keys, int index, Attribute value);
    
    /**
     * Add an attribute to a list.
     *
     * @param path The path to the list.
     * @param value The Attribute to add to the {@link org.alfresco.repo.attributes.ListAttribute ListAttribute}
     */
    public void addAttribute(String path, Attribute value);
    
    /**
     * Add an attribute to a list.
     *
     * @param keys List of attribute path keys (path components).
     * @param value The Attribute to add to the {@link org.alfresco.repo.attributes.ListAttribute ListAttribute}
     */
    public void addAttribute(List<String> keys, Attribute value);
    
    /**
     * Remove an Attribute.
     * @param name The name of the Attribute.
     */
    public void removeAttribute(String path, String name);
    
    /**
     * Remove an Attribute.
     * @param keys List of attribute path keys (path components).
     * @param name The name of the attribute to remove.
     */
    public void removeAttribute(List<String> keys, String name);
    
    /**
     * Remove an attribute from a list.
     * @param path The path to the list.
     * @param index The index to remove from the  
     *              {@link org.alfresco.repo.attributes.ListAttribute ListAttribute}
     */
    public void removeAttribute(String path, int index);

    /**
     * Remove an attribute from a list.
     * @param keys List of attribute path keys (path components).
     * @param index The index to remove from the  
     *              {@link org.alfresco.repo.attributes.ListAttribute ListAttribute}
     */
    public void removeAttribute(List<String> keys, int index);
    
    /**
     * Query for the list of attributes that is contained in the map
     * defined by the given path and meet the query criteria.
     *
     * <p>
     * <b>Example 1:</b><br>
     * Find all attributes within the nested namespace "a/b" 
     * that are lexically greater than or equal to the string "v":
     * <pre>
     *          query("a/b", new AttrQueryGTE("v"))
     * </pre>
     * <p>
     * <b>Example 2:</b><br>
     * Find all attributes within the namespace "xyz" that are 
     * either lexically less than the string "d" or greater than
     * the string "w":
     * <pre>
     *           query("xyz", new AttrOrQuery(new AttrQueryLT("d"),
     *                                        new AttrQueryGT("w")))
     * </pre>
     *
     * @param path
     * @param query
     * @return A List of matching attributes.
     */
    public List<Pair<String, Attribute>> query(String path, AttrQuery query);
    
    /**
     * Query for a list of attributes which are contained in a map defined by the
     * given path and meet the query criteria.
     * @param keys List of attribute path keys (path components).
     * @param query
     * @return A list of matching attributes.
     */
    public List<Pair<String, Attribute>> query(List<String> keys, AttrQuery query);
    
    /**
     * Get all the keys at a given attribute path.
     * When prior call to 
     * {@link #setAttribute setAttribute}
     * has associated a path with a
     * {@link org.alfresco.repo.attributes.Attribute.Type#MAP MAP}, you can fetch the
     * keys for that map via this function.
     * <p>
     * <b>Example:</b><br>
     * Suppose <code>AttribSvc</code> is an attribute service object:<pre>
     *
     *   MapAttribute x = new MapAttributeValue();
     *   x.put("cow",  new StringAttributeValue("moo");
     *   x.put("bird", new StringAttributeValue("tweet");
     *  
     *   MapAttribute y = new MapAttributeValue();
     *   y.put("pekingese",    new StringAttributeValue("yip-yip-yip");
     *   y.put("blood hound",  new StringAttributeValue("Aroooooooooooo");
     *   y.put("labrador",     new StringAttributeValue("Hello, kind stranger!");
     *
     *   AttribSvc.setAttribute("",  "x", x);
     *   AttribSvc.setAttribute("x", "y", y);
     *
     *   List&lt;String&gt; x_keys  = AttribSvc.getKeys("x");    // cow, bird
     *   List&lt;String&gt; y_keys  = AttribSvc.getKeys("x/y");  // pekingese, blood hound, labrador
     * </pre>
     * 
     * @param path The attribute path.
     * @return A list of all keys.
     */
    public List<String> getKeys(String path);
    
    /**
     * Get all the keys at a given attribute path as specified by a list of path components.
     * @param keys List of attribute path keys (path components).
     * @return A list of all keys at the specified Attribute location
     */
    public List<String> getKeys(List<String> keys);

    /**
     * Get the size of a map or list.
     * @param keys List of attribute path keys.
     * @return The size of of the list or map.
     */
    public int getCount(List<String> keys);
    
    /**
     * Get the size of a map or list.
     * @param path The path to the map or list.
     * @return The size of the list or map.
     */
    public int getCount(String path);
    
    /**
     * Does an attribute exist.
     * @param keys List of attribute path keys.
     * @return Whether the attribute exists.
     */
    public boolean exists(List<String> keys);
 
    /**
     * Does an attribute exist.
     * @param path The path to the attribute.
     * @return Whether the attribute exists.
     */
    public boolean exists(String path);
}
