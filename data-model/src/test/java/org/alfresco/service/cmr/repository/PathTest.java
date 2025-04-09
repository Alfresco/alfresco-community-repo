/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.service.cmr.repository;

import junit.framework.TestCase;

import org.alfresco.service.namespace.QName;

/**
 * @see org.alfresco.service.cmr.repository.Path
 * 
 * @author Derek Hulley
 */
public class PathTest extends TestCase
{
    private Path absolutePath;
    private Path relativePath;
    private QName typeQName;
    private QName qname;
    private StoreRef storeRef;
    private NodeRef parentRef;
    private NodeRef childRef;

    public PathTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        absolutePath = new Path();
        relativePath = new Path();
        typeQName = QName.createQName("http://www.alfresco.org/PathTest/1.0", "testType");
        qname = QName.createQName("http://www.google.com", "documentx");
        storeRef = new StoreRef("x", "y");
        parentRef = new NodeRef(storeRef, "P");
        childRef = new NodeRef(storeRef, "C");
    }

    public void testQNameElement() throws Exception
    {
        // plain
        Path.Element element = new Path.ChildAssocElement(new ChildAssociationRef(typeQName, parentRef, qname, childRef));
        assertEquals("Element string incorrect",
                qname.toString(),
                element.getElementString());
        // sibling
        element = new Path.ChildAssocElement(new ChildAssociationRef(typeQName, parentRef, qname, childRef, true, 5));
        assertEquals("Element string incorrect", "{http://www.google.com}documentx[5]", element.getElementString());
    }

    public void testElementTypes() throws Exception
    {
        Path.Element element = new Path.DescendentOrSelfElement();
        assertEquals("DescendentOrSelf element incorrect",
                "descendant-or-self::node()",
                element.getElementString());

        element = new Path.ParentElement();
        assertEquals("Parent element incorrect", "..", element.getElementString());

        element = new Path.SelfElement();
        assertEquals("Self element incorrect", ".", element.getElementString());
    }

    public void testAppendingAndPrepending() throws Exception
    {
        Path.Element element0 = new Path.ChildAssocElement(new ChildAssociationRef(null, null, null, parentRef));
        Path.Element element1 = new Path.ChildAssocElement(new ChildAssociationRef(typeQName, parentRef, qname, childRef, true, 4));
        Path.Element element2 = new Path.DescendentOrSelfElement();
        Path.Element element3 = new Path.ParentElement();
        Path.Element element4 = new Path.SelfElement();
        // append them all to the path
        absolutePath.append(element0).append(element1).append(element2).append(element3).append(element4);
        relativePath.append(element1).append(element2).append(element3).append(element4);
        // check
        assertEquals("Path appending didn't work",
                "/{http://www.google.com}documentx[4]/descendant-or-self::node()/../.",
                absolutePath.toString());

        // copy the path
        Path copy = new Path();
        copy.append(relativePath).append(relativePath);
        // check
        assertEquals("Path appending didn't work",
                relativePath.toString() + "/" + relativePath.toString(),
                copy.toString());

        // prepend
        relativePath.prepend(element2);
        // check
        assertEquals("Prepending didn't work",
                "descendant-or-self::node()/{http://www.google.com}documentx[4]/descendant-or-self::node()/../.",
                relativePath.toString());
    }
}
