package org.alfresco.repo.security.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class AuthenticationUtilTest
{
    @Test
    public void testMaskUsernameNullInput()
    {
        // given
        String userName = null;

        // when
        String maskedUsername = AuthenticationUtil.maskUsername(userName);

        // then
        assertNull(maskedUsername);
    }

    @Test
    public void testMaskUsernameEmptyString()
    {
        // given
        String userName = "";

        // when
        String maskedUsername = AuthenticationUtil.maskUsername(userName);

        // then
        assertEquals(userName, maskedUsername);
    }

    @Test
    public void testMaskUsernameSingleCharacter()
    {
        // given
        String userName = "a";

        // when
        String maskedUsername = AuthenticationUtil.maskUsername(userName);

        // then
        assertEquals(userName, maskedUsername);
    }

    @Test
    public void testMaskUsernameTwoCharacters()
    {
        // given
        String userName = "ab";

        // when
        String maskedUsername = AuthenticationUtil.maskUsername(userName);

        // then
        assertEquals(userName, maskedUsername);
    }

    @Test
    public void testMaskUsernameThreeCharacters()
    {
        // given
        String userName = "abc";

        // when
        String maskedUsername = AuthenticationUtil.maskUsername(userName);

        // then
        String expected = "ab*";
        assertEquals(expected, maskedUsername);
    }

    @Test
    public void testMaskUsernameLongerString()
    {
        // given
        String userName = "administrator";

        // when
        String maskedUsername = AuthenticationUtil.maskUsername(userName);

        // then
        String expected = "ad***********";
        assertEquals(expected, maskedUsername);
    }

    @Test
    public void testMaskUsernameWhitespaceFirstTwoCharacters()
    {
        // given
        String userName = "  bob";

        // when
        String maskedUsername = AuthenticationUtil.maskUsername(userName);

        // then
        String expected = "  ***";
        assertEquals(expected, maskedUsername);
    }

    @Test
    public void testMaskUsernameUnicodeCharacters()
    {
        // given
        String userName = "żółw"; // 4 chars

        // when
        String maskedUsername = AuthenticationUtil.maskUsername(userName);

        // then
        String expected = "żó**";
        assertEquals(expected, maskedUsername);
    }
}
