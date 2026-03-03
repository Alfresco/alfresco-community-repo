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
        String masked = AuthenticationUtil.maskUsername(userName);

        // then
        assertNull(masked);
    }

    @Test
    public void testMaskUsernameEmptyString()
    {
        // given
        String userName = "";

        // when
        String masked = AuthenticationUtil.maskUsername(userName);

        // then
        assertEquals("", masked);
    }

    @Test
    public void testMaskUsernameSingleCharacter()
    {
        // given
        String userName = "a";

        // when
        String masked = AuthenticationUtil.maskUsername(userName);

        // then
        assertEquals("a", masked);
    }

    @Test
    public void testMaskUsernameTwoCharacters()
    {
        // given
        String userName = "ab";

        // when
        String masked = AuthenticationUtil.maskUsername(userName);

        // then
        assertEquals("ab", masked);
    }

    @Test
    public void testMaskUsernameThreeCharacters()
    {
        // given
        String userName = "abc";

        // when
        String masked = AuthenticationUtil.maskUsername(userName);

        // then
        assertEquals("ab*", masked);
    }

    @Test
    public void testMaskUsernameLongerString()
    {
        // given
        String userName = "administrator";

        // when
        String masked = AuthenticationUtil.maskUsername(userName);

        // then
        assertEquals("ad***********", masked);
    }

    @Test
    public void testMaskUsernameWhitespaceFirstTwoCharacters()
    {
        // given
        String userName = "  bob";

        // when
        String masked = AuthenticationUtil.maskUsername(userName);

        // then
        assertEquals("  ***", masked);
    }

    @Test
    public void testMaskUsernameUnicodeCharacters()
    {
        // given
        String userName = "żółw"; // 4 chars

        // when
        String masked = AuthenticationUtil.maskUsername(userName);

        // then
        assertEquals("żó**", masked);
    }
}
