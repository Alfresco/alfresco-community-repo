package org.alfresco.util;

import junit.framework.TestCase;

/**
 * Test File Name validation.
 * 
 * @author Derek Hulley
 */
public class FileNameValidatorTest extends TestCase
{
    public void testValidator()
    {
        String [] badNames = { "\"", "\\", "/", "<", ">", "?", "*",
                               ":", "|" };
        for (String name : badNames)
        {
            assertFalse(FileNameValidator.isValid(name));
        }
    }
    
    public void testGetValidFileName()
    {
        // " * \ > < ? / : |
        assertEquals("ABCDEFG.txt", FileNameValidator.getValidFileName("ABCDEFG.txt"));
        assertEquals("A_B_C_D_E_F_G_H_I_J.txt", FileNameValidator.getValidFileName("A\"B*C\\D>E<F?G/H:I|J.txt"));
    }
}
