/*
 * Copyright 2005-2010 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.encryption;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * An input stream that encrypts data produced by a {@link EncryptingOutputStream}. A lightweight yet secure hybrid
 * encryption scheme is used. A random symmetric key is decrypted using the receiver's private key. The supplied data is
 * then decrypted using the symmetric key and read on a streaming basis. When the end of the stream is reached or the
 * stream is closed, a HMAC checksum of the entire stream contents is validated.
 */
public class DecryptingInputStream extends InputStream
{

    /** The wrapped stream. */
    private final DataInputStream wrapped;

    /** The input cipher. */
    private final Cipher inputCipher;

    /** The MAC generator. */
    private final Mac mac;

    /** Internal buffer for MAC computation. */
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream(1024);

    /** A DataOutputStream on top of our interal buffer. */
    private final DataOutputStream dataStr = new DataOutputStream(this.buffer);

    /** The current unencrypted data block. */
    private byte[] currentDataBlock;

    /** The next encrypted data block. (could be the HMAC checksum) */
    private byte[] nextDataBlock;

    /** Have we read to the end of the underlying stream?. */
    private boolean isAtEnd;

    /** Our current position within currentDataBlock. */
    private int currentDataPos;

    /**
     * Constructs a DecryptingInputStream using default symmetric encryption parameters.
     * 
     * @param wrapped
     *            the input stream to decrypt
     * @param privKey
     *            the receiver's private key for decrypting the symmetric key
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws NoSuchAlgorithmException
     *             the no such algorithm exception
     * @throws NoSuchPaddingException
     *             the no such padding exception
     * @throws InvalidKeyException
     *             the invalid key exception
     * @throws IllegalBlockSizeException
     *             the illegal block size exception
     * @throws BadPaddingException
     *             the bad padding exception
     * @throws InvalidAlgorithmParameterException
     *             the invalid algorithm parameter exception
     * @throws NoSuchProviderException
     *             the no such provider exception
     */
    public DecryptingInputStream(final InputStream wrapped, final PrivateKey privKey) throws IOException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException, NoSuchProviderException
    {
        this(wrapped, privKey, "AES", "CBC", "PKCS5PADDING");
    }

    /**
     * Constructs a DecryptingInputStream.
     * 
     * @param wrapped
     *            the input stream to decrypt
     * @param privKey
     *            the receiver's private key for decrypting the symmetric key
     * @param algorithm
     *            encryption algorithm (e.g. "AES")
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
     * @throws IllegalBlockSizeException
     *             the illegal block size exception
     * @throws BadPaddingException
     *             the bad padding exception
     * @throws InvalidAlgorithmParameterException
     *             the invalid algorithm parameter exception
     * @throws NoSuchProviderException
     *             the no such provider exception
     */
    public DecryptingInputStream(final InputStream wrapped, final PrivateKey privKey, final String algorithm,
            final String mode, final String padding) throws IOException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, NoSuchProviderException
    {
        // Initialise a secure source of randomness
        this.wrapped = new DataInputStream(wrapped);
        final SecureRandom secRand = SecureRandom.getInstance("SHA1PRNG");

        // Set up RSA
        final Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING");
        rsa.init(Cipher.DECRYPT_MODE, privKey, secRand);

        // Read and decrypt the symmetric key
        final SecretKey symKey = new SecretKeySpec(rsa.doFinal(readBlock()), algorithm);

        // Read and decrypt initialisation vector
        final byte[] keyIV = rsa.doFinal(readBlock());

        // Set up cipher for decryption
        this.inputCipher = Cipher.getInstance(algorithm + "/" + mode + "/" + padding);
        this.inputCipher.init(Cipher.DECRYPT_MODE, symKey, new IvParameterSpec(keyIV));

        // Read and decrypt the MAC key
        final SecretKey macKey = new SecretKeySpec(this.inputCipher.doFinal(readBlock()), "HMACSHA1");

        // Set up HMAC
        this.mac = Mac.getInstance("HMACSHA1");
        this.mac.init(macKey);

        // Always read a block ahead so we can intercept the HMAC block
        this.nextDataBlock = readBlock(false);
    }

    /**
     * Reads the next block of data, adding it to the HMAC checksum. Strips the header recording the number of bytes in
     * the block.
     * 
     * @return the data block, or <code>null</code> if the end of the stream has been reached
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private byte[] readBlock() throws IOException
    {
        return readBlock(true);
    }

    /**
     * Reads the next block of data, optionally adding it to the HMAC checksum. Strips the header recording the number
     * of bytes in the block.
     * 
     * @param updateMac
     *            should the block be added to the HMAC checksum?
     * @return the data block, or <code>null</code> if the end of the stream has been reached
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private byte[] readBlock(final boolean updateMac) throws IOException
    {
        int len;
        try
        {
            len = this.wrapped.readInt();
        }
        catch (final EOFException e)
        {
            return null;
        }
        final byte[] in = new byte[len];
        this.wrapped.readFully(in);
        if (updateMac)
        {
            macBlock(in);
        }
        return in;
    }

    /**
     * Updates the HMAC checksum with the given data block.
     * 
     * @param block
     *            the block
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void macBlock(final byte[] block) throws IOException
    {
        this.dataStr.writeInt(block.length);
        this.dataStr.write(block);
        // If we don't have the MAC key yet, buffer up until we do
        if (this.mac != null)
        {
            this.dataStr.flush();
            final byte[] bytes = this.buffer.toByteArray();
            this.buffer.reset();
            this.mac.update(bytes);
        }
    }

    /*
     * (non-Javadoc)
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException
    {
        final byte[] buf = new byte[1];
        int bytesRead;
        while ((bytesRead = read(buf)) == 0)
        {
            ;
        }
        return bytesRead == -1 ? -1 : buf[0] & 0xFF;
    }

    /*
     * (non-Javadoc)
     * @see java.io.InputStream#read(byte[])
     */
    @Override
    public int read(final byte b[]) throws IOException
    {
        return read(b, 0, b.length);
    }

    /*
     * (non-Javadoc)
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read(final byte b[], int off, final int len) throws IOException
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
            return 0;
        }

        int bytesToRead = len;
        OUTER: while (bytesToRead > 0)
        {
            // Fetch another block if necessary
            while (this.currentDataBlock == null || this.currentDataPos >= this.currentDataBlock.length)
            {
                byte[] newDataBlock;
                // We're right at the end of the last block so finish
                if (this.isAtEnd)
                {
                    this.currentDataBlock = this.nextDataBlock = null;
                    break OUTER;
                }
                // We've already read the last block so validate the MAC code
                else if ((newDataBlock = readBlock(false)) == null)
                {
                    if (!MessageDigest.isEqual(this.mac.doFinal(), this.nextDataBlock))
                    {
                        throw new IOException("Invalid HMAC");
                    }
                    // We still have what's left in the cipher to read
                    try
                    {
                        this.currentDataBlock = this.inputCipher.doFinal();
                    }
                    catch (final GeneralSecurityException e)
                    {
                        throw new RuntimeException(e);
                    }
                    this.isAtEnd = true;
                }
                // We have an ordinary data block to MAC and decrypt
                else
                {
                    macBlock(this.nextDataBlock);
                    this.currentDataBlock = this.inputCipher.update(this.nextDataBlock);
                    this.nextDataBlock = newDataBlock;
                }
                this.currentDataPos = 0;
            }
            final int bytesRead = Math.min(bytesToRead, this.currentDataBlock.length - this.currentDataPos);
            System.arraycopy(this.currentDataBlock, this.currentDataPos, b, off, bytesRead);
            bytesToRead -= bytesRead;
            off += bytesRead;
            this.currentDataPos += bytesRead;
        }
        return bytesToRead == len ? -1 : len - bytesToRead;
    }

    /*
     * (non-Javadoc)
     * @see java.io.InputStream#available()
     */
    @Override
    public int available() throws IOException
    {
        return this.currentDataBlock == null ? 0 : this.currentDataBlock.length - this.currentDataPos;
    }

    /*
     * (non-Javadoc)
     * @see java.io.InputStream#close()
     */
    @Override
    public void close() throws IOException
    {
        // Read right to the end, just to ensure the MAC code is valid!
        if (this.nextDataBlock != null)
        {
            final byte[] skipBuff = new byte[1024];
            while (read(skipBuff) != -1)
            {
                ;
            }
        }
        this.wrapped.close();
        this.dataStr.close();
    }

}
