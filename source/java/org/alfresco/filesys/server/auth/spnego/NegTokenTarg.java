/*
 * Copyright (C) 2005-2006 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.filesys.server.auth.spnego;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DEREnumerated;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

/**
 * NegTokenTarg Class
 * 
 * <p>Contains the details of an SPNEGO NegTokenTarg blob for use with CIFS.
 * 
 * @author gkspencer
 */
public class NegTokenTarg
{
    // Result code
    
    private int m_result;
    
    // Supported mechanism
    
    private Oid m_supportedMech;
    
    //  Response token
    
    private byte[] m_responseToken;
    
    /**
     * Class constructor for decoding
     */
    public NegTokenTarg()
    {
    }
    
    /**
     * Class constructor
     * 
     * @param result int
     * @param mech Oid
     * @param response byte[]
     */
    public NegTokenTarg(int result, Oid mech, byte[] response)
    {
        m_result = result;
        m_supportedMech = mech;
        m_responseToken = response;
    }
    
    /**
     * Return the result
     * 
     * @return int
     */
    public final int getResult()
    {
        return m_result;
    }
    
    /**
     * Return the supported mech type Oid
     * 
     * @return Oid
     */
    public final Oid getSupportedMech()
    {
        return m_supportedMech;
    }
    
    /**
     * Determine if there is a valid response token
     * 
     * @return boolean
     */
    public final boolean hasResponseToken()
    {
        return m_responseToken != null ? true : false;
    }
    
    /**
     * Return the response token
     * 
     * @return byte[]
     */
    public final byte[] getResponseToken()
    {
        return m_responseToken;
    }
    
    /**
     * Decode an SPNEGO NegTokenTarg blob
     * 
     * @param buf byte[]
     * @param off int
     * @param len int
     * @exception IOException
     */
    public void decode(byte[] buf, int off, int len) throws IOException
    {
        // Create a stream around the security blob
        
        ByteArrayInputStream bytStream = new ByteArrayInputStream( buf, off, len);
        ASN1InputStream asnStream = new ASN1InputStream( bytStream);
        
        // Read the top level object from the security blob
        
        DERObject derObj = asnStream.readObject();
        
        if ( derObj instanceof DERTaggedObject == false)
            throw new IOException("Bad blob format (Tagged)");

        // Access the sequence

        DERTaggedObject derTag = (DERTaggedObject) derObj;
        if ( derTag.getObject() instanceof DERSequence == false)
            throw new IOException("Bad blob format (Seq)");
        
        DERSequence derSeq = (DERSequence) derTag.getObject();
        Enumeration seqEnum = derSeq.getObjects();
        
        while ( seqEnum.hasMoreElements())
        {
            // Read an object from the sequence
            
            derObj = (DERObject) seqEnum.nextElement();
            if ( derObj instanceof DERTaggedObject)
            {
                // Tag 0 should be a status
                
                derTag = (DERTaggedObject) derObj;
                if ( derTag.getTagNo() == 0 && derTag.getObject() instanceof DEREnumerated)
                {
                    //  Result code
                    
                    DEREnumerated derEnum = (DEREnumerated) derTag.getObject();
                    m_result = derEnum.getValue().intValue();
                }
                else if ( derTag.getTagNo() == 1 && derTag.getObject() instanceof DERObjectIdentifier)
                {
                    // Mech type
                    
                    DERObjectIdentifier derOid = (DERObjectIdentifier) derTag.getObject();
                    try
                    {
                        m_supportedMech = new Oid(derOid.getId());
                    }
                    catch (GSSException ex)
                    {
                    }
                }
                else if ( derTag.getTagNo() == 2 && derTag.getObject() instanceof DEROctetString)
                {
                    // Unpack the response token
                    
                    DEROctetString derStr = (DEROctetString) derTag.getObject();
                    m_responseToken = derStr.getOctets();
                }
                else if ( derTag.getTagNo() == 3 &&derTag.getObject() instanceof DEROctetString)
                {
                    // mechListMIC
                    
                }
                else
                    throw new IOException("Bad format, unexpected type");
            }
            else
                throw new IOException("Bad format, untagged type");
        }
    }
    
    /**
     * Encode an SPNEGO NegTokenTarg blob
     * 
     * @return byte[]
     * @exception IOException
     */
    public byte[] encode() throws IOException
    {
        ByteArrayOutputStream tokStream = new ByteArrayOutputStream();
        
        // Create an SPNEGO NegTokenTarg token
        
        DEROutputStream derOut = new DEROutputStream( tokStream);

        ASN1EncodableVector asnList = new ASN1EncodableVector();

        // Pack the result code
        
        asnList.add( new DERTaggedObject( true, 0, new DEREnumerated(m_result)));
        
        // Pack the supportedMech field
        
        if ( m_supportedMech != null)
            asnList.add( new DERTaggedObject( true, 1, new DERObjectIdentifier( m_supportedMech.toString())));
        
        // Pack the response token
        
        if ( m_responseToken != null)
            asnList.add( new DERTaggedObject( true, 2, new DEROctetString(m_responseToken)));

        // Generate the SPNEGO NegTokenTarg blob
        
        derOut.writeObject( new DERTaggedObject( true, SPNEGO.NegTokenTarg, new DERSequence( asnList)));
        return tokStream.toByteArray();
    }
    
    /**
     * Return the NegtokenTarg object as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("[NegtokenTarg result=");
        str.append( SPNEGO.asResultString( getResult()));
        
        str.append(" oid=");
        str.append( getSupportedMech());
        
        str.append(" response=");
        if ( hasResponseToken())
        {
            str.append(getResponseToken().length);
            str.append(" bytes");
        }
        else
            str.append("null");
        str.append("]");
        
        return str.toString();
    }
}
