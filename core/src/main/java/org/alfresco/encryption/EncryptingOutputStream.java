/*
 * Copyright 2005-2010 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.encryption;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * An output stream that encrypts data to another output stream. A lightweight yet secure hybrid encryption scheme is
 * used. A random symmetric key is generated and encrypted using the receiver's public key. The supplied data is then
 * encrypted using the symmetric key and sent to the underlying stream on a streaming basis. An HMAC checksum is also
 * computed on an ongoing basis and appended to the output when the stream is closed. This class can be used in
 * conjunction with {@link DecryptingInputStream} to transport data securely.
 */
public class EncryptingOutputStream extends OutputStream
{
    /** The wrapped stream. */
    private final OutputStream wrapped;

    /** The output cipher. */
    private final Cipher outputCipher;

    /** The MAC generator. */
    private final Mac mac;

    /** Internal buffer for MAC computation. */
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);

    /** A DataOutputStream on top of our interal buffer. */
    private final DataOutputStream dataStr = new DataOutputStream(this.buffer);

    /**
     * Constructs an EncryptingOutputStream using default symmetric encryption parameters.
     * 
     * @param wrapped
     *            outputstream to store the encrypted data
     * @param receiverKey
     *            the receiver's public key for encrypting the symmetric key
     * @param rand
     *            a secure source of randomness
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     * @throws NoSuchPaddingException
     *             the no such padding exception
     * @throws InvalidKeyException
     *             the invalid key exception
     * @throws BadPaddingException
     *             the bad padding exception
     * @throws IllegalBlockSizeException
     *             the illegal block size exception
     */
    public EncryptingOutputStream(final OutputStream wrapped, final PublicKey receiverKey, final SecureRandom rand)
            throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException
    {
        this(wrapped, receiverKey, "AES", rand, 128, "CBC", "PKCS5PADDING");
    }

    /**
     * Constructs an EncryptingOutputStream.
     * 
     * @param wrapped
     *            outputstream to store the encrypted data
     * @param receiverKey
     *            the receiver's public key for encrypting the symmetric key
     * @param algorithm
     *            symmetric encryption algorithm (e.g. "AES")
     * @param rand
     *            a secure source of randomness
     * @param strength
     *            the key size in bits (e.g. 128)
     * @param mode
     *            encryption mode (e.g. "CBC")
     * @param padding
     *            padding scheme (e.g. "PKCS5PADDING")
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     * @throws NoSuchPaddingException
     *             the no such padding exception
     * @throws InvalidKeyException
     *             the invalid key exception
     * @throws BadPaddingException
     *             the bad padding exception
     * @throws IllegalBlockSizeException
     *             the illegal block size exception
     */
    public EncryptingOutputStream(final OutputStream wrapped, final PublicKey receiverKey, final String algorithm,
            final SecureRandom rand, final int strength, final String mode, final String padding) throws IOException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException
    {
        // Initialise
        this.wrapped = wrapped;

        // Generate a random symmetric key
        final KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
        keyGen.init(strength, rand);
        final Key symKey = keyGen.generateKey();

        // Instantiate Symmetric cipher for encryption.
        this.outputCipher = Cipher.getInstance(algorithm + "/" + mode + "/" + padding);
        this.outputCipher.init(Cipher.ENCRYPT_MODE, symKey, rand);

        // Set up HMAC
        this.mac = Mac.getInstance("HMACSHA1");
        final byte[] macKeyBytes = new byte[20];
        rand.nextBytes(macKeyBytes);
        final Key macKey = new SecretKeySpec(macKeyBytes, "HMACSHA1");
        this.mac.init(macKey);

        // Set up RSA to encrypt symmetric key
        final Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING");
        rsa.init(Cipher.ENCRYPT_MODE, receiverKey, rand);

        // Write the header

        // Write out an RSA-encrypted block for the key of the cipher.
        writeBlock(rsa.doFinal(symKey.getEncoded()));

        // Write out RSA-encrypted Initialisation Vector block
        writeBlock(rsa.doFinal(this.outputCipher.getIV()));

        // Write out key for HMAC.
        writeBlock(this.outputCipher.doFinal(macKey.getEncoded()));
    }

    /*
     * (non-Javadoc)
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(final int b) throws IOException
    {
        write(new byte[]
        {
            (byte) b
        }, 0, 1);
    }

    /*
     * (non-Javadoc)
     * @see java.io.OutputStream#write(byte[])
     */
    @Override
    public void write(final byte b[]) throws IOException
    {
        write(b, 0, b.length);
    }

    /*
     * (non-Javadoc)
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public void write(final byte b[], final int off, final int len) throws IOException
    {
        if (b == null)
        {
            throw new NullPointerException();
        }
        else if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0)
        {
            throw new IndexOutOfBoundsException();
        }
        else if (len == 0)
        {
            return;
        }
        final byte[] out = this.outputCipher.update(b, off, len); // Encrypt data.
        if (out != null && out.length > 0)
        {
            writeBlock(out);
        }
    }

    /**
     * Writes a block of data, preceded by its length, and adds it to the HMAC checksum.
     * 
     * @param out
     *            the data to be written.
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void writeBlock(final byte[] out) throws IOException
    {
        this.dataStr.writeInt(out.length); // Write length.
        this.dataStr.write(out); // Write encrypted data.
        this.dataStr.flush();
        final byte[] block = this.buffer.toByteArray();
        this.buffer.reset();
        this.mac.update(block);
        this.wrapped.write(block);
    }

    /*
     * (non-Javadoc)
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush() throws IOException
    {
        this.wrapped.flush();
    }

    /*
     * (non-Javadoc)
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close() throws IOException
    {
        try
        {
            // Write the last block
            writeBlock(this.outputCipher.doFinal());
        }
        catch (final GeneralSecurityException e)
        {
            throw new RuntimeException(e);
        }
        // Write the MAC code
        writeBlock(this.mac.doFinal());
        this.wrapped.close();
        this.dataStr.close();
    }

}
