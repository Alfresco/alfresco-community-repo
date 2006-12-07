/*
 * Copyright (C) 2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.service.cmr.repository;

import java.util.Locale;

import junit.framework.TestCase;

/**
 * @see org.alfresco.service.cmr.repository.MLText
 * 
 * @author Derek Hulley
 */
public class MLTextTest extends TestCase
{
    MLText mlText;
    
    @Override
    protected void setUp()
    {
        mlText = new MLText(Locale.CANADA_FRENCH, Locale.CANADA_FRENCH.toString());
        mlText.addValue(Locale.US, Locale.US.toString());
        mlText.addValue(Locale.UK, Locale.UK.toString());
        mlText.addValue(Locale.FRENCH, Locale.FRENCH.toString());
        mlText.addValue(Locale.CHINESE, Locale.CHINESE.toString());
    }

    public void testGetByLocale()
    {
        // check each value
        assertNull("Expected nothing for German", mlText.getValue(Locale.GERMAN));
        assertEquals(Locale.US.toString(), mlText.get(Locale.US));
        assertEquals(Locale.UK.toString(), mlText.get(Locale.UK));
        assertNull("Expected no value for Japanese", mlText.getValue(Locale.JAPANESE));
        assertNotNull("Expected an arbirary value for Japanese", mlText.getClosestValue(Locale.JAPANESE));
    }
}
