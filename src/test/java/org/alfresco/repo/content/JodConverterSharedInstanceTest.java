/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.repo.content;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.File;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests methods added to JodConverterSharedInstance that try to use the replaced oooDirect settings.
 * @author Alan Davis
 */
public class JodConverterSharedInstanceTest
{
    public static final String LIBREOFFICE = "libreoffice";
    public static final String     PROGRAM = "program";
    public static final String   ELSEWHERE = "elsewhere";
    public static final String SOFFICE_BIN = "soffice.bin";
    public static final String SOFFICE_EXE = "soffice.exe";

    private static final File OFFICE_HOME_DIR = new File(LIBREOFFICE);
    private static final File     PROGRAM_DIR = new File(OFFICE_HOME_DIR, PROGRAM);
    private static final File   ELSEWHERE_DIR = new File(OFFICE_HOME_DIR, ELSEWHERE);

    private static final String        OFFICE_HOME =                          OFFICE_HOME_DIR.getPath();
    private static final String        PROGRAM_BIN = new File(    PROGRAM_DIR,   SOFFICE_BIN).getPath();
    private static final String        PROGRAM_EXE = new File(    PROGRAM_DIR,   SOFFICE_EXE).getPath();
    private static final String      ELSEWHERE_BIN = new File(  ELSEWHERE_DIR,   SOFFICE_BIN).getPath();
    private static final String NO_OFFICE_HOME_BIN = new File(new File(PROGRAM), SOFFICE_BIN).getPath();
    private static final String   JUST_SOFFICE_BIN = new File(                   SOFFICE_BIN).getPath();


    private JodConverterSharedInstance instance;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        instance = new JodConverterSharedInstance();
    }

    @Test
    public void setupTest()
    {
        String SLASH = File.separator;
        assertEquals(LIBREOFFICE,                                           OFFICE_HOME);
        assertEquals(LIBREOFFICE + SLASH + PROGRAM   + SLASH + SOFFICE_EXE, PROGRAM_EXE);
        assertEquals(LIBREOFFICE + SLASH + PROGRAM   + SLASH + SOFFICE_BIN, PROGRAM_BIN);
        assertEquals(LIBREOFFICE + SLASH + ELSEWHERE + SLASH + SOFFICE_BIN, ELSEWHERE_BIN);
        assertEquals(                      PROGRAM   + SLASH + SOFFICE_BIN, NO_OFFICE_HOME_BIN);
        assertEquals(                                          SOFFICE_BIN, JUST_SOFFICE_BIN);
        assertNotEquals(PROGRAM_BIN, PROGRAM_EXE);
    }

    @Test
    public void officeHomeTest()
    {
        // Only jodconverter.officehome
        instance.setOfficeHome(OFFICE_HOME);
        instance.setDeprecatedOooExe(null);
        assertEquals(OFFICE_HOME, instance.getOfficeHome());

        // Use ooo.exe
        instance.setOfficeHome(null);
        instance.setDeprecatedOooExe(PROGRAM_BIN);
        assertEquals(OFFICE_HOME, instance.getOfficeHome());

        // jodconverter.officehome wins
        instance.setOfficeHome(OFFICE_HOME);
        instance.setDeprecatedOooExe(PROGRAM_EXE);
        assertEquals(OFFICE_HOME, instance.getOfficeHome());

        // ooo.exe has no parent
        instance.setOfficeHome(null);
        instance.setDeprecatedOooExe(JUST_SOFFICE_BIN);
        assertEquals("", instance.getOfficeHome());

        // ooo.exe parent is not "program"
        instance.setOfficeHome(null);
        instance.setDeprecatedOooExe(ELSEWHERE_BIN);
        assertEquals("", instance.getOfficeHome());

        // ooo.exe has a parent "program" directory but no grandparent
        instance.setOfficeHome(null);
        instance.setDeprecatedOooExe(NO_OFFICE_HOME_BIN);
        assertEquals("", instance.getOfficeHome());
    }

    @Test
    public void enabledTest()
    {
        // If ooo.enabled is true the JodConverter will be enabled, otherwise the jodconverter.enabled value is used.
        // Community set properties via alfresco-global.properties.
        // Enterprise may do the same but may also reset jodconverter.enabled them via the Admin console.
        // In the case of Enterprise it is very unlikely that ooo.enabled will be set to true.

        // Only jodconverter.enabled
        instance = new JodConverterSharedInstance();
        instance.setEnabled("true");
        instance.setDeprecatedOooEnabled(null);
        assertTrue(instance.isEnabled());

        instance = new JodConverterSharedInstance();
        instance.setEnabled("true");
        assertTrue(instance.isEnabled());

        instance = new JodConverterSharedInstance();
        instance.setEnabled("false");
        instance.setDeprecatedOooEnabled(null);
        assertFalse(instance.isEnabled());

        instance = new JodConverterSharedInstance();
        instance.setEnabled("false");
        assertFalse(instance.isEnabled());

        instance = new JodConverterSharedInstance();
        instance.setEnabled("any value other than true");
        instance.setDeprecatedOooEnabled(null);
        assertFalse(instance.isEnabled());

        instance = new JodConverterSharedInstance();
        instance.setEnabled("");
        instance.setDeprecatedOooEnabled(null);
        assertFalse(instance.isEnabled());

        instance = new JodConverterSharedInstance();
        instance.setEnabled(null);
        instance.setDeprecatedOooEnabled(null);
        assertFalse(instance.isEnabled());

        // Use ooo.enabled
        instance = new JodConverterSharedInstance();
        instance.setEnabled(null);
        instance.setDeprecatedOooEnabled("true");
        assertTrue(instance.isEnabled());

        instance = new JodConverterSharedInstance();
        instance.setEnabled(null);
        instance.setDeprecatedOooEnabled("false");
        assertFalse(instance.isEnabled());

        instance = new JodConverterSharedInstance();
        instance.setDeprecatedOooEnabled("true");
        assertTrue(instance.isEnabled());

        instance = new JodConverterSharedInstance();
        instance.setDeprecatedOooEnabled("false");
        assertFalse(instance.isEnabled());

        // Check jodconverter.enabled is used if ooo.enabled is false - Original Enterprise setup
        instance = new JodConverterSharedInstance();
        instance.setEnabled("true");
        instance.setDeprecatedOooEnabled("false");
        assertTrue(instance.isEnabled());

        instance = new JodConverterSharedInstance();
        instance.setEnabled("false");
        instance.setDeprecatedOooEnabled("false");
        assertFalse(instance.isEnabled());

        // Check jodconverter.enabled is ignored if ooo.enabled is true - Original Community setup
        instance = new JodConverterSharedInstance();
        instance.setEnabled("true");
        instance.setDeprecatedOooEnabled("true");
        assertTrue(instance.isEnabled());

        instance = new JodConverterSharedInstance();
        instance.setEnabled("false");
        instance.setDeprecatedOooEnabled("true");
        assertTrue(instance.isEnabled());

        // Check reset of jodconverter.enabled turns off isAvailable
        instance = new JodConverterSharedInstance();
        instance.setEnabled("true");
        instance.isAvailable = true; // Normally set to true after running afterPropertiesSet()
        instance.setEnabled("true");
        assertTrue(instance.isAvailable);
        instance.setEnabled("false");
        assertFalse(instance.isAvailable);

        instance = new JodConverterSharedInstance();
        instance.setEnabled("true");
        instance.setDeprecatedOooEnabled("false"); // Extra line compare with previous
        instance.isAvailable = true;
        instance.setEnabled("true");
        assertTrue(instance.isAvailable);
        instance.setEnabled("false");
        assertFalse(instance.isAvailable);
    }

    @Test
    public void portNumbersTest()
    {
        // ooo.port or jodconverter.portNumber is used depending on the setting of enabled properties.
        // If jodconverter.enabled is true jodconverter.portNumber is used.
        // If jodconverter.enabled is false and ooo.enabled is true ooo.port is used.
        // If jodconverter.enabled is false and ooo.enabled is true ooo.port is used.

        // jodconverter.enabled=true use jodconverter.portNumber
        instance.setEnabled("true");
        instance.setPortNumbers("8001,8002,8003");
        instance.setDeprecatedOooPort("8001");
        assertArrayEquals(new int[] {8001, 8002, 8003}, instance.getPortNumbers());

        // jodconverter.enabled=true and ooo.enabled=true use jodconverter.portNumber
        instance.setDeprecatedOooEnabled("true");
        assertArrayEquals(new int[] {8001, 8002, 8003}, instance.getPortNumbers());

        // jodconverter.enabled=false and ooo.enabled=true use ooo.port
        instance.setEnabled("false");
        assertArrayEquals(new int[] {8001}, instance.getPortNumbers());
    }
}
