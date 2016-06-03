/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertTrue;

/**
 * Representation of a document node - initially for client tests for Nodes (aka File Folder) API
 *
 * @author janv
 *
 */
public class Document extends Node
{
    public Document() {
        super();
    }

    @Override
    public void expected(Object o)
    {
        super.expected(o);

        assertTrue(o instanceof Document);
    }
}
