package org.alfresco.repo.security.encryption;

import java.io.UnsupportedEncodingException;


public interface EncryptionEngine
{
    public byte[] encrypt(byte[] input);
    public byte[] decrypt(byte[] input);
    public byte[] encryptString(String input) throws UnsupportedEncodingException;
    public String decryptAsString(byte[] input) throws UnsupportedEncodingException;
}
