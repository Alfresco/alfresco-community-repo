/*-
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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

package org.alfresco.repo.copy;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Unit tests for {@link CopyServiceImpl} class.
 *
 * @author Sara Aspery
 */
public class CopyServiceImplUnitTest
{
    private static final String FILE_NAME = "Test File";
    private static final String FILE_EXTENSION = ".txt";

    /* I18N labels used by the tests */
    private static final String COPY_OF_LABEL = "copy_service.copy_of_label";

    private CopyServiceImpl copyServiceImpl;

    private Locale preservedLocale;
    private String copyOfLabelTranslated;

    @Before
    public void setup()
    {
        I18NUtil.registerResourceBundle("alfresco/messages/copy-service");
        this.preservedLocale = I18NUtil.getLocale();

        this.copyServiceImpl = new CopyServiceImpl();
    }

    @Test
    public void testBuildNewName_FileWithExtension()
    {
        switchLocale(Locale.ENGLISH);
        assertEquals(copyOfLabelTranslated + FILE_NAME + FILE_EXTENSION, copyServiceImpl.buildNewName(FILE_NAME + FILE_EXTENSION));
        restoreLocale();
    }

    @Test
    public void testBuildNewName_FileWithExtensionAndCopyOf()
    {
        switchLocale(Locale.ENGLISH);
        String fileNameOfCopy = copyOfLabelTranslated + FILE_NAME + FILE_EXTENSION;
        assertEquals(copyOfLabelTranslated + fileNameOfCopy, copyServiceImpl.buildNewName(fileNameOfCopy));
        restoreLocale();
    }

    @Test
    public void testBuildNewName_FileWithoutExtension()
    {
        switchLocale(Locale.ENGLISH);
        assertEquals(copyOfLabelTranslated + FILE_NAME, copyServiceImpl.buildNewName(FILE_NAME));
        restoreLocale();
    }

    @Test
    public void testBuildNewName_FileWithExtension_JapaneseLocale()
    {
        switchLocale(Locale.JAPANESE);
        assertEquals(FILE_NAME + copyOfLabelTranslated + FILE_EXTENSION, copyServiceImpl.buildNewName(FILE_NAME + FILE_EXTENSION));
        restoreLocale();
    }

    @Test
    public void testBuildNewName_FileWithExtensionAndCopyOf_JapaneseLocale()
    {
        switchLocale(Locale.JAPANESE);
        String fileNameOfCopy = FILE_NAME + copyOfLabelTranslated;
        assertEquals(fileNameOfCopy + copyOfLabelTranslated + FILE_EXTENSION, copyServiceImpl.buildNewName(fileNameOfCopy + FILE_EXTENSION));
        restoreLocale();
    }

    @Test
    public void testBuildNewName_FileWithoutExtension_JapaneseLocale()
    {
        switchLocale(Locale.JAPANESE);
        assertEquals(FILE_NAME + copyOfLabelTranslated , copyServiceImpl.buildNewName(FILE_NAME));
        restoreLocale();
    }

    /*
     * Helper method to switch Locale
     */
    private void switchLocale(Locale newLocale)
    {
        I18NUtil.setLocale(newLocale);
        copyOfLabelTranslated = I18NUtil.getMessage(COPY_OF_LABEL, "");
    }

    /*
     * Helper method to restore Locale
     */
    private void restoreLocale()
    {
        I18NUtil.setLocale(preservedLocale);
        copyOfLabelTranslated = I18NUtil.getMessage(COPY_OF_LABEL, "");
    }
}
