/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.action;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * A dummy test class to get the dev build &amp; auto-build working with JUnit 4.
 * 
 * @author Neil McErlean
 */
public class SampleJ4Test
{
    @Test
    public void thisTestPasses() {
        // Intentionally empty
    }

    @Test
    public void thisTestHasAnError() {
        throw new UnsupportedOperationException("JUnit 4 tc intentionally failed.");
    }

    @Test
    public void thisTestHasAFailure() {
        Assert.fail("JUnit 4 tc intentionally failed.");
    }

    @Ignore("Intentionally ignoring this test case.")
    @Test
    public void thisTestHasIsIgnored() {
        // Intentionally empty
    }
}




