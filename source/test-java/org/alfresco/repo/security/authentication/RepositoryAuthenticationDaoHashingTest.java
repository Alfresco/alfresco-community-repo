package org.alfresco.repo.security.authentication;

import static org.junit.Assert.*;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gethin on 01/10/15.
 */
public class RepositoryAuthenticationDaoHashingTest
{
    RepositoryAuthenticationDao authenticationDao;
    CompositePasswordEncoder cpe;

    @Before
    public void setUp() throws Exception
    {
        authenticationDao = new RepositoryAuthenticationDao();
        cpe = new CompositePasswordEncoder();
        cpe.setEncoders(CompositePasswordEncoderTest.encodersConfig);
        authenticationDao.setCompositePasswordEncoder(cpe);
    }

    @Test
    public void testRehashedPassword() throws Exception
    {
        //Use md4
        cpe.setPreferredEncoding("md4");
        String salt = GUID.generate();
        String md4Hashed = cpe.encode("md4","HASHED_MY_PASSWORD", null);
        String sha256Hashed = cpe.encode("sha256","HASHED_MY_PASSWORD", salt);

        Map<QName, Serializable> properties = new HashMap<>();

        properties.put(ContentModel.PROP_PASSWORD, "nonsense");
        assertFalse("Should be empty", properties.containsKey(ContentModel.PROP_PASSWORD_HASH));
        //No hashing to do but we need to update the Indicator
        assertTrue(authenticationDao.rehashedPassword(properties));
        assertEquals(CompositePasswordEncoder.MD4, properties.get(ContentModel.PROP_HASH_INDICATOR));
        assertTrue("Should now contain the password", properties.containsKey(ContentModel.PROP_PASSWORD_HASH));
        assertFalse("Should remove the property", properties.containsKey(ContentModel.PROP_PASSWORD));
        assertFalse("Should remove the property", properties.containsKey(ContentModel.PROP_PASSWORD_SHA256));
        assertEquals("nonsense", properties.get(ContentModel.PROP_PASSWORD_HASH));
        //We copied the plain text (above) but it won't work (see next)

        properties.clear();
        properties.put(ContentModel.PROP_PASSWORD, "PLAIN TEXT PASSWORD");
        //We don't support plain text.
        assertTrue(authenticationDao.rehashedPassword(properties));
        assertEquals(CompositePasswordEncoder.MD4, properties.get(ContentModel.PROP_HASH_INDICATOR));
        assertTrue("Should now contain the password", properties.containsKey(ContentModel.PROP_PASSWORD_HASH));
        assertFalse("Should remove the property", properties.containsKey(ContentModel.PROP_PASSWORD));
        assertFalse("Should remove the property", properties.containsKey(ContentModel.PROP_PASSWORD_SHA256));
        assertEquals("PLAIN TEXT PASSWORD", properties.get(ContentModel.PROP_PASSWORD_HASH));
        assertFalse("We copied a plain text password to the new property but"
                +" the legacy encoding is set to MD4 so the password would NEVER match.",
                matches("PLAIN TEXT PASSWORD", properties, cpe));

        properties.clear();
        properties.put(ContentModel.PROP_PASSWORD, md4Hashed);
        cpe.setPreferredEncoding("bcrypt10");

        assertTrue("We have the property", properties.containsKey(ContentModel.PROP_PASSWORD));
        assertFalse("Should be empty", properties.containsKey(ContentModel.PROP_PASSWORD_HASH));
        //We rehashed this password by taking the md4 and hashing it by bcrypt
        assertTrue(authenticationDao.rehashedPassword(properties));
        assertEquals(Arrays.asList("md4","bcrypt10"), properties.get(ContentModel.PROP_HASH_INDICATOR));
        assertTrue("Should now contain the password", properties.containsKey(ContentModel.PROP_PASSWORD_HASH));
        assertTrue(matches("HASHED_MY_PASSWORD", properties, cpe));
        assertFalse("Should remove the property", properties.containsKey(ContentModel.PROP_PASSWORD));
        assertFalse("Should remove the property", properties.containsKey(ContentModel.PROP_PASSWORD_SHA256));

        properties.clear();
        properties.put(ContentModel.PROP_PASSWORD, "This should be ignored");
        properties.put(ContentModel.PROP_PASSWORD_SHA256, sha256Hashed);
        properties.put(ContentModel.PROP_SALT, salt);

        assertTrue("We have the property", properties.containsKey(ContentModel.PROP_PASSWORD));
        assertTrue("We have the property", properties.containsKey(ContentModel.PROP_PASSWORD_SHA256));
        assertFalse("Should be empty", properties.containsKey(ContentModel.PROP_PASSWORD_HASH));
        //We rehashed this password by taking the sha256 and hashing it by bcrypt
        assertTrue(authenticationDao.rehashedPassword(properties));
        assertEquals(Arrays.asList("sha256","bcrypt10"), properties.get(ContentModel.PROP_HASH_INDICATOR));
        assertTrue("Should now contain the password", properties.containsKey(ContentModel.PROP_PASSWORD_HASH));
        assertTrue(matches("HASHED_MY_PASSWORD", properties, cpe));
        assertFalse("Should remove the property", properties.containsKey(ContentModel.PROP_PASSWORD));
        assertFalse("Should remove the property", properties.containsKey(ContentModel.PROP_PASSWORD_SHA256));
    }
    @Test
    public void testRehashedPasswordBcrypt() throws Exception
    {
        cpe.setPreferredEncoding("bcrypt10");
        Map<QName, Serializable> properties = new HashMap<>();
        properties.put(ContentModel.PROP_HASH_INDICATOR, (Serializable) Arrays.asList("bcrypt10"));
        properties.put(ContentModel.PROP_PASSWORD_HASH,  "long hash");
        //Nothing to do.
        assertFalse(authenticationDao.rehashedPassword(properties));

        cpe.setPreferredEncoding("bcrypt11");
        assertTrue(authenticationDao.rehashedPassword(properties));
        assertEquals(Arrays.asList("bcrypt10","bcrypt11"),authenticationDao.determinePasswordHash(properties).getFirst());

        cpe.setPreferredEncoding("bcrypt12");
        assertTrue(authenticationDao.rehashedPassword(properties));
        //Triple hashing!
        assertEquals(Arrays.asList("bcrypt10","bcrypt11","bcrypt12"),authenticationDao.determinePasswordHash(properties).getFirst());
    }

    @Test
    public void testGetPasswordHash() throws Exception
    {

        Map<QName, Serializable> properties = new HashMap<>();
        cpe.setPreferredEncoding("bcrypt10");

        try
        {
            authenticationDao.determinePasswordHash(properties);
            fail("Should throw exception");
        }
        catch (AlfrescoRuntimeException are)
        {
            assertTrue(are.getMessage().contains("Unable to find a user password"));
        }

        //if the PROP_PASSWORD field is the only one availble then we are using MD4
        properties.put(ContentModel.PROP_PASSWORD, "mypassword");
        Pair<List<String>, String> passwordHashed = authenticationDao.determinePasswordHash(properties);
        assertEquals(CompositePasswordEncoder.MD4, passwordHashed.getFirst());
        assertEquals("mypassword", passwordHashed.getSecond());

        //if the PROP_PASSWORD_SHA256 field is used then we are using SHA256
        properties.put(ContentModel.PROP_PASSWORD_SHA256, "sha_password");
        passwordHashed = authenticationDao.determinePasswordHash(properties);
        assertEquals(CompositePasswordEncoder.SHA256, passwordHashed.getFirst());
        assertEquals("sha_password", passwordHashed.getSecond());

        properties.put(ContentModel.PROP_HASH_INDICATOR, null);
        //If the indicator is NULL then it still uses the old password field
        passwordHashed = authenticationDao.determinePasswordHash(properties);
        assertEquals(CompositePasswordEncoder.SHA256, passwordHashed.getFirst());
        assertEquals("sha_password", passwordHashed.getSecond());

        properties.put(ContentModel.PROP_HASH_INDICATOR, new ArrayList<String>(0));
        //If the indicator doesn't have a value
        passwordHashed = authenticationDao.determinePasswordHash(properties);
        assertEquals(CompositePasswordEncoder.SHA256, passwordHashed.getFirst());
        assertEquals("sha_password", passwordHashed.getSecond());

        //Now it uses the correct property
        properties.put(ContentModel.PROP_HASH_INDICATOR, (Serializable) Arrays.asList("myencoding"));
        properties.put(ContentModel.PROP_PASSWORD_HASH, "hashed this time");
        passwordHashed = authenticationDao.determinePasswordHash(properties);
        assertEquals(Arrays.asList("myencoding"), passwordHashed.getFirst());
        assertEquals("hashed this time",passwordHashed.getSecond());

    }

    private static boolean matches(String password, Map<QName, Serializable> properties, CompositePasswordEncoder cpe)
    {
        return cpe.matchesPassword(password, (String) properties.get(ContentModel.PROP_PASSWORD_HASH), (String) properties.get(ContentModel.PROP_SALT), (List<String>) properties.get(ContentModel.PROP_HASH_INDICATOR) );
    }

}