/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.util;

import junit.framework.TestCase;

public class ISO9075Test extends TestCase
{

    public ISO9075Test()
    {
        super();
    }

    public ISO9075Test(String arg0)
    {
        super(arg0);
    }

    public void testEncoding()
    {
        assertEquals("My2Documents", ISO9075.encode("My2Documents"));
        assertEquals("My_x002f_Documents", ISO9075.encode("My/Documents"));
        assertEquals("My_Documents", ISO9075.encode("My_Documents"));
        assertEquals("My_x0020_Documents", ISO9075.encode("My Documents"));
        assertEquals("My_x0020Documents", ISO9075.encode("My_x0020Documents"));
        assertEquals("My_x005f_x0020_Documents", ISO9075.encode("My_x0020_Documents"));
        assertEquals("_x005f_x0020_Documents", ISO9075.encode("_x0020_Documents"));
        assertEquals("_x0040__x005f_x0020_Documents", ISO9075.encode("@_x0020_Documents"));
        assertEquals("Andy_x0027_s_x0020_Bits_x0020__x0026__x0020_Bobs_x0020__xabcd__x005c_", ISO9075
                .encode("Andy's Bits & Bobs \uabcd\\"));
        assertEquals(
                "_x0020__x0060__x00ac__x00a6__x0021__x0022__x00a3__x0024__x0025__x005e__x0026__x002a__x0028__x0029_-__x003d__x002b__x0009__x000a__x005c__x0000__x005b__x005d__x007b__x007d__x003b__x0027__x0023__x003a__x0040__x007e__x002c_._x002f__x003c__x003e__x003f__x005c__x007c_",
                ISO9075.encode(" `\u00ac\u00a6!\"\u00a3$%^&*()-_=+\t\n\\\u0000[]{};'#:@~,./<>?\\|"));
        assertEquals("\u0123_x4567_\u8900_xabcd__xefff__xT65A_", ISO9075
                .encode("\u0123\u4567\u8900\uabcd\uefff_xT65A_"));
        assertEquals("_x003a_", ISO9075.encode(":"));
    }

    public void testDeEncoding()
    {
        assertEquals("MyDocuments", ISO9075.decode("MyDocuments"));
        assertEquals("My_Documents", ISO9075.decode("My_Documents"));
        assertEquals("My Documents", ISO9075.decode("My_x0020_Documents"));
        assertEquals("My_x0020Documents", ISO9075.decode("My_x0020Documents"));
        assertEquals("My_x0020_Documents", ISO9075.decode("My_x005f_x0020_Documents"));
        assertEquals("_x0020_Documents", ISO9075.decode("_x005f_x0020_Documents"));
        assertEquals("@_x0020_Documents", ISO9075.decode("_x0040__x005f_x0020_Documents"));
        assertEquals("Andy's Bits & Bobs \uabcd", ISO9075
                .decode("Andy_x0027_s_x0020_Bits_x0020__x0026__x0020_Bobs_x0020__xabcd_"));
        assertEquals("Andy's Bits & Bobs \uabcd\\", ISO9075
                .decode("Andy_x0027_s_x0020_Bits_x0020__x0026__x0020_Bobs_x0020__xabcd__x005c_"));
        assertEquals(
                " `\u00ac\u00a6!\"\u00a3$%^&*()-_=+\t\n\\\u0000[]{};'#:@~,./<>?\\|",
                ISO9075
                        .decode("_x0020__x0060__x00ac__x00a6__x0021__x0022__x00a3__x0024__x0025__x005e__x0026__x002a__x0028__x0029_-__x003d__x002b__x0009__x000a__x005c__x0000__x005b__x005d__x007b__x007d__x003b__x0027__x0023__x003a__x0040__x007e__x002c_._x002f__x003c__x003e__x003f__x005c__x007c_"));
        assertEquals("\u0123\u4567\u8900\uabcd\uefff_xT65A_", ISO9075
                .decode("\u0123_x4567_\u8900_xabcd__xefff__xT65A_"));
    }
    
    public void testRoundTrip1()
    {
        assertEquals("MyDocuments", ISO9075.decode(ISO9075.encode("MyDocuments")));
        assertEquals("My_Documents", ISO9075.decode(ISO9075.encode("My_Documents")));
        assertEquals("My Documents", ISO9075.decode(ISO9075.encode("My Documents")));
        assertEquals("My_x0020Documents", ISO9075.decode(ISO9075.encode("My_x0020Documents")));
        assertEquals("My_x0020_Documents", ISO9075.decode(ISO9075.encode("My_x0020_Documents")));
        assertEquals("_x0020_Documents", ISO9075.decode(ISO9075.encode("_x0020_Documents")));
        assertEquals("@_x0020_Documents", ISO9075.decode(ISO9075.encode("@_x0020_Documents")));
        assertEquals("Andy's Bits & Bobs \uabcd", ISO9075.decode(ISO9075.encode("Andy's Bits & Bobs \uabcd")));
        assertEquals("Andy's Bits & Bobs \uabcd\\", ISO9075.decode(ISO9075.encode("Andy's Bits & Bobs \uabcd\\")));
        assertEquals(
                " `\u00ac\u00a6!\"\u00a3$%^&*()-_=+\t\n\\\u0000[]{};'#:@~,./<>?\\|",
                ISO9075.decode(ISO9075.encode(" `\u00ac\u00a6!\"\u00a3$%^&*()-_=+\t\n\\\u0000[]{};'#:@~,./<>?\\|")));
        assertEquals("\u0123\u4567\u8900\uabcd\uefff_xT65A_", ISO9075.decode(ISO9075.encode("\u0123\u4567\u8900\uabcd\uefff_xT65A_")));
    }
    
    public void testRoundTrip2()
    {
        assertEquals("MyDocuments", ISO9075.encode(ISO9075.decode("MyDocuments")));
        assertEquals("My_Documents", ISO9075.encode(ISO9075.decode("My_Documents")));
        assertEquals("My_x0020_Documents", ISO9075.encode(ISO9075.decode("My_x0020_Documents")));
        assertEquals("My_x0020Documents", ISO9075.encode(ISO9075.decode("My_x0020Documents")));
        assertEquals("My_x005f_x0020_Documents", ISO9075.encode(ISO9075.decode("My_x005f_x0020_Documents")));
        assertEquals("_x005f_x0020_Documents", ISO9075.encode(ISO9075.decode("_x005f_x0020_Documents")));
        assertEquals("_x0040__x005f_x0020_Documents", ISO9075.encode(ISO9075.decode("_x0040__x005f_x0020_Documents")));
        assertEquals("Andy_x0027_s_x0020_Bits_x0020__x0026__x0020_Bobs_x0020__xabcd_", ISO9075.encode(ISO9075
                .decode("Andy_x0027_s_x0020_Bits_x0020__x0026__x0020_Bobs_x0020__xabcd_")));
        assertEquals("Andy_x0027_s_x0020_Bits_x0020__x0026__x0020_Bobs_x0020__xabcd__x005c_", ISO9075.encode(ISO9075
                .decode("Andy_x0027_s_x0020_Bits_x0020__x0026__x0020_Bobs_x0020__xabcd__x005c_")));
        assertEquals(
                "_x0020__x0060__x00ac__x00a6__x0021__x0022__x00a3__x0024__x0025__x005e__x0026__x002a__x0028__x0029_-__x003d__x002b__x0009__x000a__x005c__x0000__x005b__x005d__x007b__x007d__x003b__x0027__x0023__x003a__x0040__x007e__x002c_._x002f__x003c__x003e__x003f__x005c__x007c_",
                ISO9075.encode(ISO9075
                        .decode("_x0020__x0060__x00ac__x00a6__x0021__x0022__x00a3__x0024__x0025__x005e__x0026__x002a__x0028__x0029_-__x003d__x002b__x0009__x000a__x005c__x0000__x005b__x005d__x007b__x007d__x003b__x0027__x0023__x003a__x0040__x007e__x002c_._x002f__x003c__x003e__x003f__x005c__x007c_")));
        assertEquals("\u0123_x4567_\u8900_xabcd__xefff__xT65A_", ISO9075.encode(ISO9075
                .decode("\u0123_x4567_\u8900_xabcd__xefff__xT65A_")));
    }

}
