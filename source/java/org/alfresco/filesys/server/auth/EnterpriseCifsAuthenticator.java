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
package org.alfresco.filesys.server.auth;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.sasl.RealmCallback;

import org.alfresco.config.ConfigElement;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.filesys.server.auth.AuthenticatorException;
import org.alfresco.filesys.server.auth.CifsAuthenticator;
import org.alfresco.filesys.server.auth.ClientInfo;
import org.alfresco.filesys.server.auth.NTLanManAuthContext;
import org.alfresco.filesys.server.auth.kerberos.KerberosDetails;
import org.alfresco.filesys.server.auth.kerberos.SessionSetupPrivilegedAction;
import org.alfresco.filesys.server.auth.ntlm.NTLM;
import org.alfresco.filesys.server.auth.ntlm.NTLMMessage;
import org.alfresco.filesys.server.auth.ntlm.NTLMv2Blob;
import org.alfresco.filesys.server.auth.ntlm.TargetInfo;
import org.alfresco.filesys.server.auth.ntlm.Type1NTLMMessage;
import org.alfresco.filesys.server.auth.ntlm.Type2NTLMMessage;
import org.alfresco.filesys.server.auth.ntlm.Type3NTLMMessage;
import org.alfresco.filesys.server.auth.spnego.NegTokenInit;
import org.alfresco.filesys.server.auth.spnego.NegTokenTarg;
import org.alfresco.filesys.server.auth.spnego.OID;
import org.alfresco.filesys.server.auth.spnego.SPNEGO;
import org.alfresco.filesys.server.config.InvalidConfigurationException;
import org.alfresco.filesys.server.config.ServerConfiguration;
import org.alfresco.filesys.smb.Capability;
import org.alfresco.filesys.smb.SMBStatus;
import org.alfresco.filesys.smb.server.SMBSrvException;
import org.alfresco.filesys.smb.server.SMBSrvPacket;
import org.alfresco.filesys.smb.server.SMBSrvSession;
import org.alfresco.filesys.smb.server.VirtualCircuit;
import org.alfresco.filesys.util.DataPacker;
import org.alfresco.filesys.util.HexDump;
import org.alfresco.repo.security.authentication.NTLMMode;
import org.ietf.jgss.Oid;

/**
 * Enterprise CIFS Authenticator Class
 * 
 * <p>CIFS authenticator that supports NTLMSSP and Kerberos logins.
 * 
 * @author gkspencer
 */
public class EnterpriseCifsAuthenticator extends CifsAuthenticator implements CallbackHandler
{
    // Constants
    //
    // Default login configuration entry name
    
    private static final String LoginConfigEntry    = "AlfrescoCIFS";
    
    // NTLM flags mask, used to mask out features that are not supported
    
    private static final int NTLM_FLAGS = NTLM.Flag56Bit +
                                          NTLM.Flag128Bit +
                                          NTLM.FlagLanManKey +
                                          NTLM.FlagNegotiateNTLM +
                                          NTLM.FlagNTLM2Key +
                                          NTLM.FlagNegotiateUnicode;
    
    // Use NTLMSSP or SPNEGO
    
    private boolean m_useRawNTLMSSP;
    
    // Flag to control whether NTLMv1 is accepted
    
    private boolean m_acceptNTLMv1;
    
    // Kerberos settings
    //
    // Account name and password for server ticket
    //
    // The account name must be built from the CIFS server name, in the format :-
    //
    //      cifs/<server_name>@<realm>
    
    private String m_accountName;
    private String m_password;
    
    // Kerberos realm and KDC address
    
    private String m_krbRealm;
    private String m_krbKDC;
    
    // Login configuration entry name
    
    private String m_loginEntryName = LoginConfigEntry; 

    // Server login context
    
    private LoginContext m_loginContext;
    
    // SPNEGO NegTokenInit blob, sent to the client in the SMB negotiate response
    
    private byte[] m_negTokenInit;
    
    /**
     * Class constructor
     */
    public EnterpriseCifsAuthenticator()
    {
    }
    
    /**
     * Initialize the authenticator
     * 
     * @param config ServerConfiguration
     * @param params ConfigElement
     * @exception InvalidConfigurationException
     */
    public void initialize(ServerConfiguration config, ConfigElement params) throws InvalidConfigurationException
    {
        super.initialize(config, params);
        
        // Check if Kerberos is enabled, get the Kerberos KDC address

        ConfigElement kdcAddress = params.getChild("KDC");
        
        if (kdcAddress != null && kdcAddress.getValue() != null && kdcAddress.getValue().length() > 0)
        {
            // Set the Kerberos KDC address
            
            m_krbKDC = kdcAddress.getValue();
        
            // Get the Kerberos realm
            
            ConfigElement krbRealm = params.getChild("Realm");
            if ( krbRealm != null && krbRealm.getValue() != null && krbRealm.getValue().length() > 0)
            {
                // Set the Kerberos realm
                
                m_krbRealm = krbRealm.getValue();
            }
            else
                throw new InvalidConfigurationException("Kerberos realm not specified");
            
            // Get the CIFS service account password
            
            ConfigElement srvPassword = params.getChild("Password");
            if ( srvPassword != null && srvPassword.getValue() != null && srvPassword.getValue().length() > 0)
            {
                // Set the CIFS service account password
                
                m_password = srvPassword.getValue();
            }
            else
                throw new InvalidConfigurationException("CIFS service account password not specified");
            
            // Get the login configuration entry name
            
            ConfigElement loginEntry = params.getChild("LoginEntry");
            
            if ( loginEntry != null)
            {
                if ( loginEntry.getValue() != null && loginEntry.getValue().length() > 0)
                {
                    // Set the login configuration entry name to use
                    
                    m_loginEntryName = loginEntry.getValue();
                }
                else
                    throw new InvalidConfigurationException("Invalid login entry specified");
            }
            
            // Build the CIFS service account name
            
            StringBuilder cifsAccount = new StringBuilder();
            
            cifsAccount.append("cifs/");
            cifsAccount.append( config.getServerName().toLowerCase());
            cifsAccount.append("@");
            cifsAccount.append(m_krbRealm);
            
            m_accountName = cifsAccount.toString();
            
            // Create a login context for the CIFS server service
            
            try
            {
                // Login the CIFS server service
                
                m_loginContext = new LoginContext( m_loginEntryName, this);
                m_loginContext.login();
            }
            catch ( LoginException ex)
            {
                // Debug
                
                if ( logger.isErrorEnabled())
                    logger.error("CIFS Kerberos authenticator error", ex);
                
                throw new InvalidConfigurationException("Failed to login CIFS server service");
            }
            
            // Create the Oid list for the SPNEGO NegTokenInit, include NTLMSSP for fallback
            
            Vector<Oid> mechTypes = new Vector<Oid>();

            mechTypes.add(OID.NTLMSSP);
            mechTypes.add(OID.KERBEROS5);
            mechTypes.add(OID.MSKERBEROS5);
        
            // Build the SPNEGO NegTokenInit blob
    
            try
            {
                // Build the mechListMIC principle
                //
                // Note: This field is not as specified
                
                String mecListMIC = null;
                
                StringBuilder mic = new StringBuilder();
                mic.append( config.getServerName().toLowerCase());
                mic.append("$@");
                mic.append( m_krbRealm);
                
                mecListMIC = mic.toString();
                
                // Build the SPNEGO NegTokenInit that contains the authentication types that the CIFS server accepts
                
                NegTokenInit negTokenInit = new NegTokenInit(mechTypes, mecListMIC);
                
                // Encode the NegTokenInit blob
                
                m_negTokenInit = negTokenInit.encode();
            }
            catch (IOException ex)
            {
                // Debug
                
                if ( logger.isErrorEnabled())
                    logger.error("Error creating SPNEGO NegTokenInit blob", ex);
                
                throw new InvalidConfigurationException("Failed to create SPNEGO NegTokenInit blob");
            }
            
            // Indicate that SPNEGO security blobs are being used
            
            m_useRawNTLMSSP = false;
        }
        else
        {
            // Check if raw NTLMSSP or SPNEGO/NTLMSSP should be used
            
            ConfigElement useSpnego = params.getChild("useSPNEGO");
            
            if ( useSpnego != null)
            {
                // Create the Oid list for the SPNEGO NegTokenInit
                
                Vector<Oid> mechTypes = new Vector<Oid>();

                mechTypes.add( OID.NTLMSSP);
                
                // Build the SPNEGO NegTokenInit blob
                
                try
                {
                    // Build the SPNEGO NegTokenInit that contains the authentication types that the CIFS server accepts
                    
                    NegTokenInit negTokenInit = new NegTokenInit(mechTypes, null);
                    
                    // Encode the NegTokenInit blob
                    
                    m_negTokenInit = negTokenInit.encode();
                }
                catch (IOException ex)
                {
                    // Debug
                    
                    if ( logger.isErrorEnabled())
                        logger.error("Error creating SPNEGO NegTokenInit blob", ex);
                    
                    throw new InvalidConfigurationException("Failed to create SPNEGO NegTokenInit blob");
                }
                
                // Indicate that SPNEGO security blobs are being used
                
                m_useRawNTLMSSP = false;
            }
            else
            {
                // Use raw NTLMSSP security blobs
                
                m_useRawNTLMSSP = true;
            }
        }
        
        // Check if NTLMv1 logons are accepted
        
        ConfigElement disallowNTLMv1 = params.getChild("disallowNTLMv1");
        
        m_acceptNTLMv1 = disallowNTLMv1 != null ? false : true;
        
        //  Make sure that either Kerberos support is enabled and/or the authentication component
        //  supports MD4 hashed passwords
        
        if ( isKerberosEnabled() == false && m_authComponent.getNTLMMode() != NTLMMode.MD4_PROVIDER)
        {
            //  Log an error
            
            logger.error("No valid CIFS authentication combination available");
            logger.error("Either enable Kerberos support or use an authentication component that supports MD4 hashed passwords");
            
            //  Throw an exception to stop the CIFS server startup
            
            throw new AlfrescoRuntimeException("Invalid CIFS authenticator configuration");
        }
    }

    /**
     * Determine if Kerberos support is enabled
     * 
     * @return boolean
     */
    private final boolean isKerberosEnabled()
    {
        return m_krbKDC != null && m_loginContext != null;
    }

    /**
     * Determine if raw NTLMSSP or SPNEGO security blobs are being used
     * 
     * @return boolean
     */
    private final boolean useRawNTLMSSP()
    {
        return m_useRawNTLMSSP;
    }
    
    /**
     * Determine if NTLMv1 logons are accepted
     * 
     * @return boolean
     */
    private final boolean acceptNTLMv1Logon()
    {
        return m_acceptNTLMv1;
    }
    
    /**
     * JAAS callback handler
     * 
     * @param callbacks Callback[]
     * @exception IOException
     * @exception UnsupportedCallbackException
     */
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
    {
        // Process the callback list
        
        for (int i = 0; i < callbacks.length; i++)
        {
            // Request for user name
            
            if (callbacks[i] instanceof NameCallback)
            {
                NameCallback cb = (NameCallback) callbacks[i];
                cb.setName(m_accountName);
            }
            
            // Request for password
            else if (callbacks[i] instanceof PasswordCallback)
            {
                PasswordCallback cb = (PasswordCallback) callbacks[i];
                cb.setPassword(m_password.toCharArray());
            }
            
            // Request for realm
            
            else if (callbacks[i] instanceof RealmCallback)
            {
                RealmCallback cb = (RealmCallback) callbacks[i];
                cb.setText(m_krbRealm);
            }
            else
            {
                throw new UnsupportedCallbackException(callbacks[i]);
            }
        }
    }

    /**
     * Return the encryption key/challenge length
     * 
     * @return int
     */
    public int getEncryptionKeyLength()
    {
        return 8;
    }
    
    /**
     * Return the server capability flags
     * 
     * @return int
     */
    public int getServerCapabilities()
    {
        return Capability.Unicode + Capability.RemoteAPIs + Capability.NTSMBs + Capability.NTFind +
               Capability.NTStatus + Capability.LargeFiles + Capability.LargeRead + Capability.LargeWrite +
               Capability.ExtendedSecurity;
    }
    
    /**
     * Generate the CIFS negotiate response packet, the authenticator should add authentication specific fields
     * to the response.
     * 
     * @param sess SMBSrvSession
     * @param respPkt SMBSrvPacket
     * @param extendedSecurity boolean
     * @exception AuthenticatorException
     */
    public void generateNegotiateResponse(SMBSrvSession sess, SMBSrvPacket respPkt, boolean extendedSecurity)
        throws AuthenticatorException
    {
        // If the client does not support extended security then return a standard negotiate response
        // with an 8 byte challenge
        
        if ( extendedSecurity == false)
        {
            super.generateNegotiateResponse( sess, respPkt, extendedSecurity);
            return;
        }
        
        // Make sure the extended security negotiation flag is set
        
        if (( respPkt.getFlags2() & SMBSrvPacket.FLG2_EXTENDEDSECURITY) == 0)
            respPkt.setFlags2( respPkt.getFlags2() + SMBSrvPacket.FLG2_EXTENDEDSECURITY);
        
        // Get the negotiate response byte area position
        
        int pos = respPkt.getByteOffset();
        byte[] buf = respPkt.getBuffer();
        
        // Pack the CIFS server GUID into the negotiate response

        UUID serverGUID = sess.getSMBServer().getServerGUID();
        
        DataPacker.putIntelLong( serverGUID.getLeastSignificantBits(), buf, pos);
        DataPacker.putIntelLong( serverGUID.getMostSignificantBits(), buf, pos + 8);
        
        pos += 16;
        
        // If SPNEGO is enabled then pack the NegTokenInit blob

        if ( useRawNTLMSSP() == false)
        {
            System.arraycopy( m_negTokenInit, 0, buf, pos, m_negTokenInit.length);
            pos += m_negTokenInit.length;
        }

        // Set the negotiate response length
        
        respPkt.setByteCount(pos - respPkt.getByteOffset());
    }

    /**
     * Process the CIFS session setup request packet and build the session setup response
     * 
     * @param sess SMBSrvSession
     * @param reqPkt SMBSrvPacket
     * @param respPkt SMBSrvPacket
     * @exception SMBSrvException
     */
    public void processSessionSetup(SMBSrvSession sess, SMBSrvPacket reqPkt, SMBSrvPacket respPkt)
        throws SMBSrvException
    {
        //  Check that the received packet looks like a valid NT session setup andX request

        if (reqPkt.checkPacketIsValid(12, 0) == false)
            throw new SMBSrvException(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);

        //  Check if the request is using security blobs or the older hashed password format
        
        if ( reqPkt.getParameterCount() == 13)
        {
            //  Process the hashed password session setup
                
            doHashedPasswordLogon( sess, reqPkt, respPkt);
            return;
        }
        
        //  Extract the session details

        int maxBufSize = reqPkt.getParameter(2);
        int maxMpx     = reqPkt.getParameter(3);
        int vcNum      = reqPkt.getParameter(4);
        int secBlobLen = reqPkt.getParameter(7);
        int capabs     = reqPkt.getParameterLong(10);

        //  Extract the client details from the session setup request

        int dataPos = reqPkt.getByteOffset();
        byte[] buf = reqPkt.getBuffer();

        //  Determine if ASCII or unicode strings are being used
            
        boolean isUni = reqPkt.isUnicode();

        //  Make a note of the security blob position
        
        int secBlobPos = dataPos;
        
        //  Extract the clients primary domain name string

        dataPos += secBlobLen;
        reqPkt.setPosition( dataPos);
        
        String domain = "";

        if (reqPkt.hasMoreData()) {

            //    Extract the callers domain name

            domain = reqPkt.unpackString(isUni);
            
            if (domain == null)
                throw new SMBSrvException(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
        }

        //  Extract the clients native operating system

        String clientOS = "";

        if (reqPkt.hasMoreData()) {

          //    Extract the callers operating system name

            clientOS = reqPkt.unpackString(isUni);
            
          if (clientOS == null)
              throw new SMBSrvException( SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
        }

        //  DEBUG

        if (logger.isDebugEnabled())
          logger.debug("NT Session setup " + (useRawNTLMSSP() ? "NTLMSSP" : "SPNEGO") + ", MID=" + reqPkt.getMultiplexId() + ", UID=" + reqPkt.getUserId() + ", PID=" + reqPkt.getProcessId());

        //  Store the client maximum buffer size, maximum multiplexed requests count and client capability flags
            
        sess.setClientMaximumBufferSize(maxBufSize != 0 ? maxBufSize : SMBSrvSession.DefaultBufferSize);
        sess.setClientMaximumMultiplex(maxMpx);
        sess.setClientCapabilities(capabs);

        //  Create the client information and store in the session

        ClientInfo client = new ClientInfo();
        client.setDomain(domain);
        client.setOperatingSystem(clientOS);
        
        client.setLogonType( ClientInfo.LogonNormal);

        // Set the remote address, if available
        
        if ( sess.hasRemoteAddress())
          client.setClientAddress(sess.getRemoteAddress().getHostAddress());

        //  Set the process id for this client, for multi-stage logons
        
        client.setProcessId( reqPkt.getProcessId());
        
        // Get the current sesion setup object, or null
        
        Object setupObj = sess.getSetupObject( client.getProcessId());
        
        //  Process the security blob
        
        byte[] respBlob = null;
        boolean isNTLMSSP = false;
        
        try
        {
        	
            // Check if the blob has the NTLMSSP signature
            
            if ( secBlobLen >= NTLM.Signature.length) {
              
              // Check for the NTLMSSP signature
              
              int idx = 0;
              while ( idx < NTLM.Signature.length && buf[secBlobPos + idx] == NTLM.Signature[ idx])
                idx++;
              
              if ( idx == NTLM.Signature.length)
                isNTLMSSP = true;
            }
            
        	// Process the security blob
            
            if ( isNTLMSSP == true)
            {
                //  Process an NTLMSSP security blob
    
                respBlob = doNtlmsspSessionSetup( sess, client, buf, secBlobPos, secBlobLen, isUni);
            }
            else
            {
                //  Process an SPNEGO security blob
                
                respBlob = doSpnegoSessionSetup( sess, client, buf, secBlobPos, secBlobLen, isUni);
            }
        }
        catch (SMBSrvException ex)
        {
            //  Cleanup any stored context
            
            sess.removeSetupObject( client.getProcessId());
            
            //  Rethrow the exception
            
            throw ex;
        }

        // Debug
        
        if ( logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_NEGOTIATE))
            logger.debug("User " + client.getUserName() + " logged on " + (client != null ? " (type " + client.getLogonTypeString() + ")" : ""));

        //  Update the client information if not already set
            
        if ( sess.getClientInformation() == null ||
             sess.getClientInformation().getUserName().length() == 0) {
                      
            //  Set the client details for the session
                    
            sess.setClientInformation(client);
        }

        //  Get the response blob length, it can be null
        
        int respLen = respBlob != null ? respBlob.length : 0;
        
        //  Check if there is/was a session setup object stored in the session, this indicates a multi-stage session
        //  setup so set the status code accordingly
        
        boolean loggedOn = false;
        
        if ( useRawNTLMSSP() || isNTLMSSP == true || sess.hasSetupObject( client.getProcessId()) || setupObj != null)
        {
            //  NTLMSSP has two stages, if there is a stored setup object then indicate more processing
            //  required
            
            if ( sess.hasSetupObject( client.getProcessId()))
                respPkt.setLongErrorCode( SMBStatus.NTMoreProcessingRequired);
            else
            {
                respPkt.setLongErrorCode( SMBStatus.NTSuccess);
                
                // Indicate that the user is logged on
                
                loggedOn = true;
            }

            respPkt.setParameterCount(4);
            respPkt.setParameter(0, 0xFF);      //  No chained response
            respPkt.setParameter(1, 0);         //  Offset to chained response
            
            respPkt.setParameter(2, 0);         //  Action
            respPkt.setParameter(3, respLen);
        }
        else
        {
            //  Build a completed session setup response
            
            respPkt.setLongErrorCode( SMBStatus.NTSuccess);
            
            //  Build the session setup response SMB
    
            respPkt.setParameterCount(12);
            respPkt.setParameter(0, 0xFF);      //  No chained response
            respPkt.setParameter(1, 0);         //  Offset to chained response
    
            respPkt.setParameter(2, SMBSrvSession.DefaultBufferSize);
            respPkt.setParameter(3, SMBSrvSession.NTMaxMultiplexed);
            respPkt.setParameter(4, 0);         //  virtual circuit number
            respPkt.setParameterLong(5, 0);     //  session key
            respPkt.setParameter(7, respLen);
                                                //  security blob length
            respPkt.setParameterLong(8, 0);     //  reserved
            respPkt.setParameterLong(10, getServerCapabilities());
            
            // Indicate that the user is logged on
            
            loggedOn = true;
        }
        
        // If the user is logged on then allocate a virtual circuit

        int uid = 0;
        
        if ( loggedOn == true) {

          // Clear any stored session setup object for the logon
          
          sess.removeSetupObject( client.getProcessId());
          
          // Create a virtual circuit for the new logon
          
          VirtualCircuit vc = new VirtualCircuit( vcNum, client);
          uid = sess.addVirtualCircuit( vc);
          
          if ( uid == VirtualCircuit.InvalidUID)
          {
        	  // DEBUG
            
        	  if ( logger.isDebugEnabled() && sess.hasDebug( SMBSrvSession.DBG_NEGOTIATE))
        		  logger.debug("Failed to allocate UID for virtual circuit, " + vc);
            
        	  // Failed to allocate a UID
            
        	  throw new SMBSrvException(SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
          }
          else if ( logger.isDebugEnabled() && sess.hasDebug( SMBSrvSession.DBG_NEGOTIATE)) {
            
        	  // DEBUG
            
        	  logger.debug("Allocated UID=" + uid + " for VC=" + vc);
          }
        }
        
        // Common session setup response
        
        respPkt.setCommand( reqPkt.getCommand());
        respPkt.setByteCount(0);

        respPkt.setTreeId( 0);
        respPkt.setUserId( uid);

        //  Set the various flags

        int flags = respPkt.getFlags();
        flags &= ~SMBSrvPacket.FLG_CASELESS;
        respPkt.setFlags(flags);
        
        int flags2 = SMBSrvPacket.FLG2_LONGFILENAMES + SMBSrvPacket.FLG2_EXTENDEDSECURITY + SMBSrvPacket.FLG2_LONGERRORCODE;
        if ( isUni)
          flags2 += SMBSrvPacket.FLG2_UNICODE;
        respPkt.setFlags2( flags2);
        
        //  Pack the security blob

        int pos = respPkt.getByteOffset();
        buf = respPkt.getBuffer();

        if ( respBlob != null)
        {
            System.arraycopy( respBlob, 0, buf, pos, respBlob.length);
            pos += respBlob.length;
        }
        
        // Pack the OS, dialect and domain name strings
        
        if ( isUni)
            pos = DataPacker.wordAlign(pos);

        pos = DataPacker.putString("Java", buf, pos, true, isUni);
        pos = DataPacker.putString("Alfresco CIFS Server " + sess.getServer().isVersion(), buf, pos, true, isUni);
        pos = DataPacker.putString(sess.getServer().getConfiguration().getDomainName(), buf, pos, true, isUni);
        
        respPkt.setByteCount(pos - respPkt.getByteOffset());
    }
    
    /**
     * Process an NTLMSSP security blob
     * 
     * @param sess SMBSrvSession
     * @param client ClientInfo
     * @param secbuf byte[]
     * @param secpos int
     * @param seclen int
     * @param unicode boolean
     * @exception SMBSrvException
     */
    private final byte[] doNtlmsspSessionSetup( SMBSrvSession sess, ClientInfo client,
            byte[] secbuf, int secpos, int seclen, boolean unicode) throws SMBSrvException
    {
        // Determine the NTLmSSP message type
        
        int msgType = NTLMMessage.isNTLMType( secbuf, secpos);
        byte[] respBlob = null;
        
        if ( msgType == -1)
        {
            // DEBUG
            
            if ( logger.isDebugEnabled())
            {
                logger.debug("Invalid NTLMSSP token received");
                logger.debug("  Token=" + HexDump.hexString( secbuf, secpos, seclen, " "));
            }

            // Return a logon failure status
            
            throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
        }
        
        // Check for a type 1 NTLMSSP message
        
        else if ( msgType ==  NTLM.Type1)
        {
            // Create the type 1 NTLM message from the token
            
            Type1NTLMMessage type1Msg = new Type1NTLMMessage( secbuf, secpos, seclen);
            
            //  Build the type 2 NTLM response message
            //
            //  Get the flags from the client request and mask out unsupported features
            
            int ntlmFlags = type1Msg.getFlags() & NTLM_FLAGS;
            
            //  Generate a challenge for the response
            
            NTLanManAuthContext ntlmCtx = new NTLanManAuthContext();
            
            //  Build a type2 message to send back to the client, containing the challenge

            String domain = sess.getSMBServer().getServerName();
            
            List<TargetInfo> tList = new ArrayList<TargetInfo>();
            
            tList.add(new TargetInfo(NTLM.TargetDomain, domain));
            tList.add(new TargetInfo(NTLM.TargetServer, sess.getServerName()));
            tList.add(new TargetInfo(NTLM.TargetDNSDomain, domain));
            tList.add(new TargetInfo(NTLM.TargetFullDNS, domain));
            
            ntlmFlags = NTLM.FlagChallengeAccept + NTLM.FlagRequestTarget +
                        NTLM.Flag128Bit + NTLM.FlagNegotiateNTLM + NTLM.FlagNegotiateUnicode +
                        NTLM.FlagNTLM2Key + NTLM.FlagKeyExchange + NTLM.FlagTargetInfo;
            
            if ( acceptNTLMv1Logon())
                ntlmFlags += NTLM.Flag56Bit;
            
            // NTLM.FlagAlwaysSign + NTLM.FlagNegotiateSign +
            
            Type2NTLMMessage type2Msg = new Type2NTLMMessage();
            
            type2Msg.buildType2(ntlmFlags, domain, ntlmCtx.getChallenge(), null, tList);

            //  Store the type 2 message in the session until the session setup is complete
            
            sess.setSetupObject( client.getProcessId(), type2Msg);
            
            // Set the response blob using the type 2 message
            
            respBlob = type2Msg.getBytes();
        }
        else if ( msgType == NTLM.Type3)
        {
            //  Create the type 3 NTLM message from the token
            
            Type3NTLMMessage type3Msg = new Type3NTLMMessage( secbuf, secpos, seclen, unicode);
            
            //  Make sure a type 2 message was stored in the first stage of the session setup
            
            if ( sess.hasSetupObject( client.getProcessId()) == false || sess.getSetupObject( client.getProcessId()) instanceof Type2NTLMMessage == false)
            {
                //  Clear the setup object
                
                sess.removeSetupObject( client.getProcessId());
                
                //  Return a logon failure

                throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            }

            //  Determine if the client sent us NTLMv1 or NTLMv2
            
            if ( type3Msg.hasFlag( NTLM.Flag128Bit) && type3Msg.hasFlag( NTLM.FlagNTLM2Key))
            {
                //  Determine if the client sent us an NTLMv2 blob or an NTLMv2 session key
                
                if ( type3Msg.getNTLMHashLength() > 24)
                {
                    //  Looks like an NTLMv2 blob
                    
                    doNTLMv2Logon( sess, client, type3Msg);

                    //  Debug
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("Logged on using NTLMSSP/NTLMv2");
                }
                else
                {
                    //  Looks like an NTLMv2 session key
                    
                    doNTLMv2SessionKeyLogon( sess, client, type3Msg);

                    //  Debug
                    
                    if ( logger.isDebugEnabled())
                        logger.debug("Logged on using NTLMSSP/NTLMv2SessKey");
                }
            }
            else
            {
                //  Looks like an NTLMv1 blob
                
                doNTLMv1Logon( sess, client, type3Msg);

                //  Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("Logged on using NTLMSSP/NTLMv1");
            }
        }
        
        // Return the response blob
        
        return respBlob;
    }

    /**
     * Process an SPNEGO security blob
     * 
     * @param sess SMBSrvSession
     * @param client ClientInfo
     * @param secbuf byte[]
     * @param secpos int
     * @param seclen int
     * @param unicode boolean
     * @exception SMBSrvException
     */
    private final byte[] doSpnegoSessionSetup( SMBSrvSession sess, ClientInfo client,
            byte[] secbuf, int secpos, int seclen, boolean unicode) throws SMBSrvException
    {
        //  Check the received token type, if it is a target token and there is a stored session setup object, this is the second
        //  stage of an NTLMSSP session setup that is wrapped with SPNEGO

        int tokType = -1;
        
        try
        {
            tokType = SPNEGO.checkTokenType( secbuf, secpos, seclen);
        }
        catch ( IOException ex)
        {
        }

        //  Check for the second stage of an NTLMSSP logon
        
        NegTokenTarg negTarg = null;
        
        if ( tokType == SPNEGO.NegTokenTarg && sess.hasSetupObject( client.getProcessId()) && sess.getSetupObject( client.getProcessId()) instanceof Type2NTLMMessage)
        {
            //  Get the NTLMSSP blob from the NegTokenTarg blob
            
            NegTokenTarg negToken = new NegTokenTarg();
            
            try
            {
                // Decode the security blob
                
                negToken.decode( secbuf, secpos, seclen);
            }
            catch ( IOException ex)
            {
                // Log the error
                
                logger.error(ex);
                
                // Return a logon failure status
                
                throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            }

            //  Get the second stage NTLMSSP blob
            
            byte[] ntlmsspBlob = negToken.getResponseToken();

            //  Perform an NTLMSSP session setup
            
            byte[] ntlmsspRespBlob = doNtlmsspSessionSetup( sess, client, ntlmsspBlob, 0, ntlmsspBlob.length, unicode);
            
            //  NTLMSSP is a two stage process, set the SPNEGO status
            
            int spnegoSts = SPNEGO.AcceptCompleted;
            
            if ( sess.hasSetupObject( client.getProcessId()))
                spnegoSts = SPNEGO.AcceptIncomplete;
            
            //  Package the NTLMSSP response in an SPNEGO response

            negTarg = new NegTokenTarg( spnegoSts, null, ntlmsspRespBlob);
        }
        else if ( tokType == SPNEGO.NegTokenInit)
        {
            //  Parse the SPNEGO security blob to get the Kerberos ticket
            
            NegTokenInit negToken = new NegTokenInit();
            
            try
            {
                // Decode the security blob
                
                negToken.decode( secbuf, secpos, seclen);
            }
            catch ( IOException ex)
            {
                // Log the error
                
                logger.error(ex);
                
                // Return a logon failure status
                
                throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            }
    
            //  Determine the authentication mechanism the client is using and logon
            
            String oidStr = null;
            if ( negToken.numberOfOids() > 0)
                oidStr = negToken.getOidAt( 0).toString();
            
            if ( oidStr != null && oidStr.equals( OID.ID_NTLMSSP))
            {
                //  NTLMSSP logon, get the NTLMSSP security blob that is inside the SPNEGO blob
                
                byte[] ntlmsspBlob = negToken.getMechtoken();
    
                //  Perform an NTLMSSP session setup
                
                byte[] ntlmsspRespBlob = doNtlmsspSessionSetup( sess, client, ntlmsspBlob, 0, ntlmsspBlob.length, unicode);
                
                //  NTLMSSP is a two stage process, set the SPNEGO status
                
                int spnegoSts = SPNEGO.AcceptCompleted;
                
                if ( sess.hasSetupObject( client.getProcessId()))
                    spnegoSts = SPNEGO.AcceptIncomplete;
                
                //  Package the NTLMSSP response in an SPNEGO response
    
                negTarg = new NegTokenTarg( spnegoSts, OID.NTLMSSP, ntlmsspRespBlob);
            }
            else if (  oidStr != null && (oidStr.equals( OID.ID_MSKERBEROS5) || oidStr.equals(OID.ID_KERBEROS5)))
            {
                //  Kerberos logon
                
                negTarg = doKerberosLogon( sess, negToken, client);
            }
            else
            {
                //  Debug
                
                if ( logger.isDebugEnabled())
                {
                    logger.debug("No matching authentication OID found");
                    logger.debug("  " + negToken.toString());
                }
                    
                //  No valid authentication mechanism
                
                throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            }
        }
        else
        {
            //  Unknown SPNEGO token type
            
            logger.error( "Unknown SPNEGO token type");
            
            // Return a logon failure status
            
            throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
        }
        
        // Generate the NegTokenTarg blob

        byte[] respBlob = null;
        
        try
        {
            // Generate the response blob
            
           respBlob = negTarg.encode();
        }
        catch ( IOException ex)
        {
            //  Debug
            
            if ( logger.isDebugEnabled())
                logger.debug("Failed to encode NegTokenTarg", ex);

            //  Failed to build response blob
            
            throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
        }
        
        //  Return the SPNEGO response blob
        
        return respBlob;
    }
    
    /**
     * Perform a Kerberos login and return an SPNEGO response
     * 
     * @param sess SMBSrvSession
     * @param negToken NegTokenInit
     * @param client ClientInfo
     * @return NegTokenTarg
     * @exception SMBSrvException
     */
    private final NegTokenTarg doKerberosLogon( SMBSrvSession sess, NegTokenInit negToken, ClientInfo client)
        throws SMBSrvException
    {
        //  Authenticate the user
        
        KerberosDetails krbDetails = null;
        NegTokenTarg negTokenTarg = null;
        
        try
        {
            //  Run the session setup as a privileged action
            
            SessionSetupPrivilegedAction sessSetupAction = new SessionSetupPrivilegedAction( m_accountName, negToken.getMechtoken());
            Object result = Subject.doAs( m_loginContext.getSubject(), sessSetupAction);
    
            if ( result != null)
            {
                // Access the Kerberos response
                
                krbDetails = (KerberosDetails) result;
                
                // Create the NegTokenTarg response blob
                
                negTokenTarg = new NegTokenTarg( SPNEGO.AcceptCompleted, OID.KERBEROS5, krbDetails.getResponseToken());
                
            	// Start a transaction
            	
            	sess.beginReadTransaction( m_transactionService);
            	
                // Setup the Acegi authenticated user
                
                // Set the current user to be authenticated, save the authentication token
                
                client.setAuthenticationToken( m_authComponent.setCurrentUser( mapUserNameToPerson(krbDetails.getUserName())));
                
                // Store the full user name in the client information, indicate that this is not a guest logon
                
                client.setUserName( krbDetails.getSourceName());
                client.setGuest( false);
                
                // Indicate that the session is logged on
                
                sess.setLoggedOn(true);

                //  Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("Logged on using Kerberos");
            }
        }
        catch (Exception ex)
        {
            // Log the error
            
            logger.error(ex);
    
            // Return a logon failure status
            
            throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
        }
    
        // Return the response SPNEGO blob
        
        return negTokenTarg;
    }
    
    /**
     * Perform an NTLMv1 logon using the NTLMSSP type3 message
     * 
     * @param sess SMBSrvSession
     * @param client ClientInfo
     * @param type3Msg Type3NTLMMessage
     * @exception SMBSrvException 
     */
    private final void doNTLMv1Logon(SMBSrvSession sess, ClientInfo client, Type3NTLMMessage type3Msg)
        throws SMBSrvException
    {
        // Check if NTLMv1 logons are allowed
        
        if ( acceptNTLMv1Logon() == false)
        {
            //  NTLMv1 password hashes not accepted
            
            logger.warn("NTLMv1 not accepted, client " + sess.getRemoteName());
            
            //  Return a logon failure

            throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
        }
        
        //  Get the type 2 message that contains the challenge sent to the client
        
        Type2NTLMMessage type2Msg = (Type2NTLMMessage) sess.getSetupObject( client.getProcessId());
        sess.removeSetupObject( client.getProcessId());
        
        // Check if we are using local MD4 password hashes or passthru authentication
        
        if ( m_authComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
        {
            // Get the NTLM logon details
            
            String userName = type3Msg.getUserName();
            
            //  Check for a null logon
            
            if ( userName.length() == 0)
            {
                //  DEBUG
                
                if ( logger.isDebugEnabled())
                    logger.debug("Null logon");
                
                //  Indicate a null logon in the client information
                
                client.setLogonType( ClientInfo.LogonNull);
                return;
            }
            
            //  Get the stored MD4 hashed password for the user, or null if the user does not exist
            
            String md4hash = m_authComponent.getMD4HashedPassword(userName);
            
            if ( md4hash != null)
            {
                // Generate the local encrypted password using the challenge that was sent to the client
                
                byte[] p21 = new byte[21];
                byte[] md4byts = m_md4Encoder.decodeHash(md4hash);
                System.arraycopy(md4byts, 0, p21, 0, 16);
                
                // Generate the local hash of the password using the same challenge
                
                byte[] localHash = null;
                
                try
                {
                    localHash = getEncryptor().doNTLM1Encryption(p21, type2Msg.getChallenge());
                }
                catch (NoSuchAlgorithmException ex)
                {
                }
                
                // Validate the password
                
                byte[] clientHash = type3Msg.getNTLMHash();

                if ( clientHash != null && localHash != null && clientHash.length == localHash.length)
                {
                    int i = 0;

                    while ( i < clientHash.length && clientHash[i] == localHash[i])
                        i++;
                    
                    if ( i != clientHash.length)
                    {
                        //  Return a logon failure

                        throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
                    }
                }

                // Setup the Acegi authenticated user
                
                client.setAuthenticationToken( m_authComponent.setCurrentUser( mapUserNameToPerson(userName)));
                
                // Store the full user name in the client information, indicate that this is not a guest logon
                
                client.setUserName( userName.toLowerCase());
                client.setGuest( false);
                
                // Indicate that the session is logged on
                
                sess.setLoggedOn(true);
            }
            else
            {
                //  Log a warning, user does not exist
                
                logger.warn("User does not exist, " + userName);
                
                //  Return a logon failure

                throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            }
        }
        else
        {
            //  Log a warning, authentication component does not support MD4 hashed passwords
            
            logger.warn("Authentication component does not support MD4 password hashes");
            
            //  Return a logon failure

            throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
        }
    }

    /**
     * Perform an NTLMv1 logon using the NTLMSSP type3 message
     * 
     * @param sess SMBSrvSession
     * @param client ClientInfo
     * @exception SMBSrvException 
     */
    private final void doNTLMv1Logon(SMBSrvSession sess, ClientInfo client)
        throws SMBSrvException
    {
        // Check if NTLMv1 logons are allowed
        
        if ( acceptNTLMv1Logon() == false)
        {
            //  NTLMv1 password hashes not accepted
            
            logger.warn("NTLMv1 not accepted, client " + sess.getRemoteName());
            
            //  Return a logon failure

            throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
        }
        
    	// Start a transaction
    	
    	sess.beginReadTransaction( m_transactionService);
    	
        // Check if we are using local MD4 password hashes or passthru authentication
        
        if ( m_authComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
        {
            //  Check for a null logon
            
            if ( client.getUserName().length() == 0)
            {
                //  DEBUG
                
                if ( logger.isDebugEnabled())
                    logger.debug("Null logon");
                
                //  Indicate a null logon in the client information
                
                client.setLogonType( ClientInfo.LogonNull);
                return;
            }
            
            //  Get the stored MD4 hashed password for the user, or null if the user does not exist
            
            String md4hash = m_authComponent.getMD4HashedPassword(client.getUserName());
            
            if ( md4hash != null)
            {
                // Generate the local encrypted password using the challenge that was sent to the client
                
                byte[] p21 = new byte[21];
                byte[] md4byts = m_md4Encoder.decodeHash(md4hash);
                System.arraycopy(md4byts, 0, p21, 0, 16);

                // Get the challenge that was sent to the client during negotiation
                
                byte[] challenge = null;
                if ( sess.hasAuthenticationContext())
                {
                    // Get the challenge from the authentication context
                    
                    NTLanManAuthContext ntlmCtx = (NTLanManAuthContext) sess.getAuthenticationContext();
                    challenge = ntlmCtx.getChallenge();
                }
                
                // Generate the local hash of the password using the same challenge
                
                byte[] localHash = null;
                
                try
                {
                    localHash = getEncryptor().doNTLM1Encryption(p21, challenge);
                }
                catch (NoSuchAlgorithmException ex)
                {
                }
                
                // Validate the password
                
                byte[] clientHash = client.getPassword();
                
                if ( clientHash != null && localHash != null && clientHash.length == localHash.length)
                {
                    int i = 0;

                    while ( i < clientHash.length && clientHash[i] == localHash[i])
                        i++;
                    
                    if ( i != clientHash.length)
                    {
                        //  Return a logon failure

                        throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
                    }
                }

                // Setup the Acegi authenticated user
                
                client.setAuthenticationToken( m_authComponent.setCurrentUser( mapUserNameToPerson( client.getUserName())));
                
                // Store the full user name in the client information, indicate that this is not a guest logon
                
                client.setGuest( false);
                
                // Indicate that the session is logged on
                
                sess.setLoggedOn(true);
            }
            else
            {
                //  Log a warning, user does not exist
                
                logger.warn("User does not exist, " + client.getUserName());
                
                //  Return a logon failure

                throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            }
        }
        else
        {
            //  Log a warning, authentication component does not support MD4 hashed passwords
            
            logger.warn("Authentication component does not support MD4 password hashes");
            
            //  Return a logon failure

            throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
        }
    }

    /**
     * Perform an NTLMv2 logon using the NTLMSSP type3 message
     * 
     * @param sess SMBSrvSession
     * @param client ClientInfo
     * @param type3Msg Type3NTLMMessage
     * @exception SMBSrvException 
     */
    private final void doNTLMv2Logon(SMBSrvSession sess, ClientInfo client, Type3NTLMMessage type3Msg)
        throws SMBSrvException
    {
        //  Get the type 2 message that contains the challenge sent to the client
        
        Type2NTLMMessage type2Msg = (Type2NTLMMessage) sess.getSetupObject( client.getProcessId());
        sess.removeSetupObject( client.getProcessId());
        
    	// Start a transaction
    	
    	sess.beginReadTransaction( m_transactionService);

    	// Check if we are using local MD4 password hashes or passthru authentication
        
        if ( m_authComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
        {
            // Get the NTLM logon details
            
            String userName = type3Msg.getUserName();
            
            //  Check for a null logon
            
            if ( userName.length() == 0)
            {
                //  DEBUG
                
                if ( logger.isDebugEnabled())
                    logger.debug("Null logon");
                
                //  Indicate a null logon in the client information
                
                client.setLogonType( ClientInfo.LogonNull);
                return;
            }
            
            //  Get the stored MD4 hashed password for the user, or null if the user does not exist
            
            String md4hash = m_authComponent.getMD4HashedPassword(userName);
            
            if ( md4hash != null)
            {
                try
                {
                    // Generate the v2 hash using the challenge that was sent to the client
    
                    byte[] v2hash = getEncryptor().doNTLM2Encryption( m_md4Encoder.decodeHash(md4hash), type3Msg.getUserName(), type3Msg.getDomain());
                    
                    // Get the NTLMv2 blob sent by the client and the challenge that was sent by the server
                    
                    NTLMv2Blob v2blob = new NTLMv2Blob(type3Msg.getNTLMHash());
                    byte[] srvChallenge = type2Msg.getChallenge();
                    
                    // Calculate the HMAC of the received blob and compare
                    
                    byte[] srvHmac = v2blob.calculateHMAC( srvChallenge, v2hash);
                    byte[] clientHmac = v2blob.getHMAC();
    
                    if ( clientHmac != null && srvHmac != null && clientHmac.length == srvHmac.length)
                    {
                        int i = 0;
    
                        while ( i < clientHmac.length && clientHmac[i] == srvHmac[i])
                            i++;
                        
                        if ( i != clientHmac.length)
                        {
                            //  Return a logon failure
    
                            throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
                        }
                    }
    
                    // Setup the Acegi authenticated user
                    
                    client.setAuthenticationToken( m_authComponent.setCurrentUser( mapUserNameToPerson( userName)));
                    
                    // Store the full user name in the client information, indicate that this is not a guest logon
                    
                    client.setUserName( userName.toLowerCase());
                    client.setGuest( false);
                    
                    // Indicate that the session is logged on
                    
                    sess.setLoggedOn(true);
                }
                catch ( Exception ex)
                {
                    // Log the error
                    
                    logger.error(ex);
                    
                    //  Return a logon failure

                    throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
                }
            }
            else
            {
                //  Log a warning, user does not exist
                
                logger.warn("User does not exist, " + userName);
                
                //  Return a logon failure

                throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            }
        }
        else
        {
            //  Log a warning, authentication component does not support MD4 hashed passwords
            
            logger.warn("Authentication component does not support MD4 password hashes");
            
            //  Return a logon failure

            throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
        }
    }
    
    /**
     * Perform an NTLMv2 logon using the NTLMSSP type3 message
     * 
     * @param sess SMBSrvSession
     * @param client ClientInfo
     * @exception SMBSrvException 
     */
    private final void doNTLMv2Logon(SMBSrvSession sess, ClientInfo client)
        throws SMBSrvException
    {
    	// Start a transaction
    	
    	sess.beginReadTransaction( m_transactionService);
    	
        // Check if we are using local MD4 password hashes or passthru authentication
        
        if ( m_authComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
        {
            //  Check for a null logon
            
            if ( client.getUserName().length() == 0)
            {
                //  DEBUG
                
                if ( logger.isDebugEnabled())
                    logger.debug("Null logon");
                
                //  Indicate a null logon in the client information
                
                client.setLogonType( ClientInfo.LogonNull);
                return;
            }
            
            //  Get the stored MD4 hashed password for the user, or null if the user does not exist
            
            String md4hash = m_authComponent.getMD4HashedPassword(client.getUserName());
            
            if ( md4hash != null)
            {
                try
                {
                    // Create the NTLMv2 blob from the received hashed password bytes
                    
                    NTLMv2Blob v2blob = new NTLMv2Blob(client.getPassword());
                    
                    // Generate the v2 hash using the challenge that was sent to the client
    
                    byte[] v2hash = getEncryptor().doNTLM2Encryption( m_md4Encoder.decodeHash(md4hash), client.getUserName(), client.getDomain());

                    // Get the challenge that was sent to the client during negotiation
                    
                    byte[] srvChallenge = null;
                    if ( sess.hasAuthenticationContext())
                    {
                        // Get the challenge from the authentication context
                        
                        NTLanManAuthContext ntlmCtx = (NTLanManAuthContext) sess.getAuthenticationContext();
                        srvChallenge = ntlmCtx.getChallenge();
                    }
                    
                    // Calculate the HMAC of the received blob and compare
                    
                    byte[] srvHmac = v2blob.calculateHMAC( srvChallenge, v2hash);
                    byte[] clientHmac = v2blob.getHMAC();
    
                    if ( clientHmac != null && srvHmac != null && clientHmac.length == srvHmac.length)
                    {
                        int i = 0;
    
                        while ( i < clientHmac.length && clientHmac[i] == srvHmac[i])
                            i++;
                        
                        if ( i != clientHmac.length)
                        {
                            //  Return a logon failure
    
                            throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
                        }
                    }
    
                    // Setup the Acegi authenticated user
                    
                    client.setAuthenticationToken( m_authComponent.setCurrentUser( mapUserNameToPerson( client.getUserName())));
                    
                    // Store the full user name in the client information, indicate that this is not a guest logon
                    
                    client.setGuest( false);
                    
                    // Indicate that the session is logged on
                    
                    sess.setLoggedOn(true);
                }
                catch ( Exception ex)
                {
                    // Log the error
                    
                    logger.error(ex);
                    
                    //  Return a logon failure

                    throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
                }
            }
            else
            {
                //  Log a warning, user does not exist
                
                logger.warn("User does not exist, " + client.getUserName());
                
                //  Return a logon failure

                throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            }
        }
        else
        {
            //  Log a warning, authentication component does not support MD4 hashed passwords
            
            logger.warn("Authentication component does not support MD4 password hashes");
            
            //  Return a logon failure

            throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
        }
    }
    
    /**
     * Perform an NTLMv2 session key logon
     * 
     * @param sess SMBSrvSession
     * @param client ClientInfo
     * @param type3Msg Type3NTLMMessage
     * @exception SMBSrvException 
     */
    private final void doNTLMv2SessionKeyLogon(SMBSrvSession sess, ClientInfo client, Type3NTLMMessage type3Msg)
       throws SMBSrvException
    {
        //  Get the type 2 message that contains the challenge sent to the client
       
        Type2NTLMMessage type2Msg = (Type2NTLMMessage) sess.getSetupObject( client.getProcessId());
        sess.removeSetupObject( client.getProcessId());

        // Start a transaction
    	
    	sess.beginReadTransaction( m_transactionService);
       
        // Check if we are using local MD4 password hashes or passthru authentication
       
        if ( m_authComponent.getNTLMMode() == NTLMMode.MD4_PROVIDER)
        {
            // Get the NTLM logon details
           
            String userName = type3Msg.getUserName();
           
            //  Check for a null logon
           
            if ( userName.length() == 0)
            {
                //  DEBUG
               
                if ( logger.isDebugEnabled())
                    logger.debug("Null logon");
               
                //  Indicate a null logon in the client information
               
                client.setLogonType( ClientInfo.LogonNull);
                return;
            }
           
            //  Get the stored MD4 hashed password for the user, or null if the user does not exist
           
            String md4hash = m_authComponent.getMD4HashedPassword(userName);
           
            if ( md4hash != null)
            {
                // Create the value to be encrypted by appending the server challenge and client challenge
                // and applying an MD5 digest
                
                byte[] nonce = new byte[16];
                System.arraycopy( type2Msg.getChallenge(), 0, nonce, 0, 8);
                System.arraycopy( type3Msg.getLMHash(), 0, nonce, 8, 8);
                
                MessageDigest md5 = null;
                byte[] v2challenge = new byte[8];
                
                try
                {
                    //  Create the MD5 digest
                    
                    md5 = MessageDigest.getInstance( "MD5");
                    
                    //  Apply the MD5 digest to the nonce
                    
                    md5.update( nonce);
                    byte[] md5nonce = md5.digest();
                    
                    //  We only want the first 8 bytes
                    
                    System.arraycopy( md5nonce, 0, v2challenge, 0, 8);
                }
                catch ( NoSuchAlgorithmException ex)
                {
                    // Log the error
                    
                    logger.error( ex);
                }
                
                // Generate the local encrypted password using the MD5 generated challenge
               
                byte[] p21 = new byte[21];
                byte[] md4byts = m_md4Encoder.decodeHash(md4hash);
                System.arraycopy(md4byts, 0, p21, 0, 16);
               
                // Generate the local hash of the password
               
                byte[] localHash = null;
               
                try
                {
                    localHash = getEncryptor().doNTLM1Encryption(p21, v2challenge);
                }
                catch (NoSuchAlgorithmException ex)
                {
                    // Log the error
                    
                    logger.error( ex);
                }
               
                // Validate the password
               
                byte[] clientHash = type3Msg.getNTLMHash();

                if ( clientHash != null && localHash != null && clientHash.length == localHash.length)
                {
                    int i = 0;

                    while ( i < clientHash.length && clientHash[i] == localHash[i])
                        i++;
                   
                    if ( i != clientHash.length)
                    {
                        //  Return a logon failure

                        throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
                    }
                }

                // Setup the Acegi authenticated user
               
                client.setAuthenticationToken( m_authComponent.setCurrentUser( mapUserNameToPerson( userName)));
               
                // Store the full user name in the client information, indicate that this is not a guest logon
               
                client.setUserName( userName.toLowerCase());
                client.setGuest( false);
               
                // Indicate that the session is logged on
               
                sess.setLoggedOn(true);
            }
            else
            {
                //  Log a warning, user does not exist
               
                logger.warn("User does not exist, " + userName);
               
                //  Return a logon failure

                throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
            }
        }
        else
        {
            //  Log a warning, authentication component does not support MD4 hashed passwords
           
            logger.warn("Authentication component does not support MD4 password hashes");
           
            //  Return a logon failure

            throw new SMBSrvException( SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
        }
    }
    
    /**
     * Perform a hashed password logon using either NTLMv1 or NTLMv2
     * 
     * @param sess SMBSrvSession
     * @param reqPkt SMBSrvPacket
     * @param respPkt SMBSrvPacket
     * @exception SMBSrvException
     */
    private final void doHashedPasswordLogon( SMBSrvSession sess, SMBSrvPacket reqPkt, SMBSrvPacket respPkt)
        throws SMBSrvException
    {
        // Check that the received packet looks like a valid NT session setup andX request

        if (reqPkt.checkPacketIsValid(13, 0) == false)
        {
            throw new SMBSrvException(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
        }

        // Extract the session details

        int maxBufSize = reqPkt.getParameter(2);
        int maxMpx = reqPkt.getParameter(3);
        int vcNum = reqPkt.getParameter(4);
        int ascPwdLen = reqPkt.getParameter(7);
        int uniPwdLen = reqPkt.getParameter(8);
        int capabs = reqPkt.getParameterLong(11);

        // Extract the client details from the session setup request

        byte[] buf = reqPkt.getBuffer();

        // Determine if ASCII or unicode strings are being used

        boolean isUni = reqPkt.isUnicode();

        // Extract the password strings

        byte[] ascPwd = reqPkt.unpackBytes(ascPwdLen);
        byte[] uniPwd = reqPkt.unpackBytes(uniPwdLen);

        // Extract the user name string

        String user = reqPkt.unpackString(isUni);

        if (user == null)
        {
            throw new SMBSrvException(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
        }

        // Extract the clients primary domain name string

        String domain = "";

        if (reqPkt.hasMoreData())
        {

            // Extract the callers domain name

            domain = reqPkt.unpackString(isUni);

            if (domain == null)
            {
                throw new SMBSrvException(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            }
        }

        // Extract the clients native operating system

        String clientOS = "";

        if (reqPkt.hasMoreData())
        {

            // Extract the callers operating system name

            clientOS = reqPkt.unpackString(isUni);

            if (clientOS == null)
            {
                throw new SMBSrvException(SMBStatus.NTInvalidParameter, SMBStatus.SRVNonSpecificError, SMBStatus.ErrSrv);
            }
        }

        // DEBUG

        if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_NEGOTIATE))
        {
            logger.debug("NT Session setup from user=" + user + ", password="
                    + (uniPwd != null ? HexDump.hexString(uniPwd) : "none") + ", ANSIpwd="
                    + (ascPwd != null ? HexDump.hexString(ascPwd) : "none") + ", domain=" + domain + ", os=" + clientOS
                    + ", VC=" + vcNum + ", maxBuf=" + maxBufSize + ", maxMpx=" + maxMpx
                    + ", authCtx=" + sess.getAuthenticationContext());
            logger.debug("  MID=" + reqPkt.getMultiplexId() + ", UID=" + reqPkt.getUserId() + ", PID="
                    + reqPkt.getProcessId());
        }

        // Store the client maximum buffer size, maximum multiplexed requests count and client
        // capability flags

        sess.setClientMaximumBufferSize(maxBufSize);
        sess.setClientMaximumMultiplex(maxMpx);
        sess.setClientCapabilities(capabs);

        // Create the client information and store in the session

        ClientInfo client = new ClientInfo(user, uniPwd);
        client.setANSIPassword(ascPwd);
        client.setDomain(domain);
        client.setOperatingSystem(clientOS);

        if (sess.hasRemoteAddress())
            client.setClientAddress(sess.getRemoteAddress().getHostAddress());

        // Check if this is a null session logon

        if (user.length() == 0 && domain.length() == 0 && uniPwdLen == 0)
            client.setLogonType(ClientInfo.LogonNull);

        // Authenticate the user using the Unicode password hash, this is either NTLMv1 or NTLMv2 encoded

        boolean isGuest = false;

        if ( uniPwd != null)
        {
            if ( uniPwd.length == 24)
            {
                // NTLMv1 hashed password
                
                doNTLMv1Logon(sess, client);

                //  Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("Logged on using Hashed/NTLMv1");
            }
            else if ( uniPwd.length > 0)
            {
                // NTLMv2 blob
                
                doNTLMv2Logon( sess, client);

                //  Debug
                
                if ( logger.isDebugEnabled())
                    logger.debug("Logged on using Hashed/NTLMv2");
            }
        }
        
        // Check if the user was logged on as guest
        
        if ( client.isGuest())
        {

            // Guest logon

            isGuest = true;

            // DEBUG

            if (logger.isDebugEnabled() && sess.hasDebug(SMBSrvSession.DBG_NEGOTIATE))
                logger.debug("User " + user + ", logged on as guest");
        }

        // Create a virtual circuit and allocate a UID to the new circuit

        VirtualCircuit vc = new VirtualCircuit( vcNum, client);
        int uid = sess.addVirtualCircuit( vc);
        
        if ( uid == VirtualCircuit.InvalidUID)
        {
        
        	// DEBUG
          
        	if ( logger.isDebugEnabled() && sess.hasDebug( SMBSrvSession.DBG_NEGOTIATE))
        		logger.debug("Failed to allocate UID for virtual circuit, " + vc);
          
        	// Failed to allocate a UID
          
        	throw new SMBSrvException(SMBStatus.NTLogonFailure, SMBStatus.DOSAccessDenied, SMBStatus.ErrDos);
        }
        else if ( logger.isDebugEnabled() && sess.hasDebug( SMBSrvSession.DBG_NEGOTIATE))
        {
        	// DEBUG
          
        	logger.debug("Allocated UID=" + uid + " for VC=" + vc);
        }
        
        // Set the guest flag for the client, indicate that the session is logged on

        client.setGuest(isGuest);
        sess.setLoggedOn(true);

        // Build the session setup response SMB

        respPkt.setParameterCount(3);
        respPkt.setParameter(0, 0); // No chained response
        respPkt.setParameter(1, 0); // Offset to chained response
        respPkt.setParameter(2, isGuest ? 1 : 0);
        respPkt.setByteCount(0);

        respPkt.setTreeId(0);
        respPkt.setUserId(uid);

        // Set the various flags

        int flags = respPkt.getFlags();
        flags &= ~SMBSrvPacket.FLG_CASELESS;
        respPkt.setFlags(flags);

        int flags2 = SMBSrvPacket.FLG2_LONGFILENAMES;
        if (isUni)
            flags2 += SMBSrvPacket.FLG2_UNICODE;
        respPkt.setFlags2(flags2);

        // Pack the OS, dialect and domain name strings.

        int pos = respPkt.getByteOffset();
        buf = respPkt.getBuffer();

        if (isUni)
            pos = DataPacker.wordAlign(pos);

        pos = DataPacker.putString("Java", buf, pos, true, isUni);
        pos = DataPacker.putString("Alfresco CIFS Server " + sess.getServer().isVersion(), buf, pos, true, isUni);
        pos = DataPacker.putString(sess.getServer().getConfiguration().getDomainName(), buf, pos, true, isUni);

        respPkt.setByteCount(pos - respPkt.getByteOffset());
    }
}
