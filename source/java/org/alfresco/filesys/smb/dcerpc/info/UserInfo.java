/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.filesys.smb.dcerpc.info;

import java.util.BitSet;

import org.alfresco.filesys.smb.dcerpc.DCEBuffer;
import org.alfresco.filesys.smb.dcerpc.DCEBufferException;
import org.alfresco.filesys.smb.dcerpc.DCEReadable;

/**
 * User Information Class
 * <p>
 * Contains the details of a user account on a remote server.
 */
public class UserInfo implements DCEReadable
{

    // Information levels supported

    public static final int InfoLevel1 = 1;
    public static final int InfoLevel3 = 3;
    public static final int InfoLevel21 = 21;

    // public static final int InfoLevel2 = 2;
    // public static final int InfoLevel4 = 4;
    // public static final int InfoLevel5 = 5;
    // public static final int InfoLevel6 = 6;
    // public static final int InfoLevel7 = 7;
    // public static final int InfoLevel8 = 8;
    // public static final int InfoLevel9 = 9;
    // public static final int InfoLevel10 = 10;
    // public static final int InfoLevel11 = 11;
    // public static final int InfoLevel12 = 12;
    // public static final int InfoLevel13 = 13;
    // public static final int InfoLevel14 = 14;
    // public static final int InfoLevel16 = 16;
    // public static final int InfoLevel17 = 17;
    // public static final int InfoLevel20 = 20;

    // Account privilege levels

    public static final int PrivGuest = 0;
    public static final int PrivUser = 1;
    public static final int PrivAdmin = 2;

    // Account operator privileges

    public static final int OperPrint = 0;
    public static final int OperComm = 1;
    public static final int OperServer = 2;
    public static final int OperAccounts = 3;

    // Account flags

    private static final int AccountDisabled = 0x0001;
    private static final int AccountHomeDirRequired = 0x0002;
    private static final int AccountPasswordNotRequired = 0x0004;
    private static final int AccountTemporaryDuplicate = 0x0008;
    private static final int AccountNormal = 0x0010;
    private static final int AccountMNSUser = 0x0020;
    private static final int AccountDomainTrust = 0x0040;
    private static final int AccountWorkstationTrust = 0x0080;
    private static final int AccountServerTrust = 0x0100;
    private static final int AccountPasswordNotExpire = 0x0200;
    private static final int AccountAutoLocked = 0x0400;

    // Information level

    private int m_infoLevel;

    // User information

    private String m_userName;

    private int m_pwdAge;
    private int m_priv;

    private String m_homeDir;
    private String m_comment;
    private String m_description;
    private String m_accComment;

    private int m_flags;

    private String m_scriptPath;
    // private int m_authFlags;

    private String m_fullName;
    private String m_appParam;
    private String m_workStations;

    private long m_lastLogon;
    private long m_lastLogoff;
    private long m_acctExpires;
    private long m_lastPwdChange;
    private long m_pwdCanChange;
    private long m_pwdMustchange;

    // private int m_maxStorage;
    private int m_unitsPerWeek;
    private byte[] m_logonHoursRaw;
    private BitSet m_logonHours;

    private int m_badPwdCount;
    private int m_numLogons;
    private String logonSrv;

    private int m_countryCode;
    private int m_codePage;

    private int m_userRID;
    private int m_groupRID;
    // private SID m_userSID;

    private String m_profile;
    private String m_homeDirDrive;

    private int m_pwdExpired;

    private String m_callBack;
    private String m_unknown1;
    private String m_unknown2;
    private String m_unknown3;

    /**
     * Default constructor
     */
    public UserInfo()
    {
    }

    /**
     * Class constructor
     * 
     * @param lev int
     */
    public UserInfo(int lev)
    {
        m_infoLevel = lev;
    }

    /**
     * Get the information level
     * 
     * @return int
     */
    public final int getInformationLevel()
    {
        return m_infoLevel;
    }

    /**
     * Return the logon server name
     * 
     * @return String
     */
    public final String getLogonServer()
    {
        return logonSrv;
    }

    /**
     * Return the date/time the account expires, or NTTime.Infinity if it does not expire
     * 
     * @return long
     */
    public final long getAccountExpires()
    {
        return m_acctExpires;
    }

    /**
     * Return the application parameter string
     * 
     * @return String
     */
    public final String getApplicationParameter()
    {
        return m_appParam;
    }

    /**
     * Return the bad password count
     * 
     * @return int
     */
    public final int getBadPasswordCount()
    {
        return m_badPwdCount;
    }

    /**
     * Return the code page
     * 
     * @return int
     */
    public final int getCodePage()
    {
        return m_codePage;
    }

    /**
     * Return the account comment
     * 
     * @return String
     */
    public final String getComment()
    {
        return m_comment;
    }

    /**
     * Return the account description
     * 
     * @return String
     */
    public final String getDescription()
    {
        return m_description;
    }

    /**
     * Return the country code
     * 
     * @return int
     */
    public final int getCountryCode()
    {
        return m_countryCode;
    }

    /**
     * Return the account flags
     * 
     * @return int
     */
    public final int getFlags()
    {
        return m_flags;
    }

    /**
     * Check if the account is disabled
     * 
     * @return boolean
     */
    public final boolean isDisabled()
    {
        return (m_flags & AccountDisabled) != 0 ? true : false;
    }

    /**
     * Check if the account does not require a home directory
     * 
     * @return boolean
     */
    public final boolean requiresHomeDirectory()
    {
        return (m_flags & AccountHomeDirRequired) != 0 ? true : false;
    }

    /**
     * Check if the account does not require a password
     * 
     * @return boolean
     */
    public final boolean requiresPassword()
    {
        return (m_flags & AccountPasswordNotRequired) != 0 ? false : true;
    }

    /**
     * Check if the account is a normal user account
     * 
     * @return boolean
     */
    public final boolean isNormalUser()
    {
        return (m_flags & AccountNormal) != 0 ? true : false;
    }

    /**
     * Check if the account is a domain trust account
     * 
     * @return boolean
     */
    public final boolean isDomainTrust()
    {
        return (m_flags & AccountDomainTrust) != 0 ? true : false;
    }

    /**
     * Check if the account is a workstation trust account
     * 
     * @return boolean
     */
    public final boolean isWorkstationTrust()
    {
        return (m_flags & AccountWorkstationTrust) != 0 ? true : false;
    }

    /**
     * Check if the account is a server trust account
     * 
     * @return boolean
     */
    public final boolean isServerTrust()
    {
        return (m_flags & AccountServerTrust) != 0 ? true : false;
    }

    /**
     * Check if the account password expires
     * 
     * @return boolean
     */
    public final boolean passwordExpires()
    {
        return (m_flags & AccountPasswordNotExpire) != 0 ? false : true;
    }

    /**
     * Check if the account is auto locked
     * 
     * @return boolean
     */
    public final boolean isAutoLocked()
    {
        return (m_flags & AccountAutoLocked) != 0 ? true : false;
    }

    /**
     * Return the full account name
     * 
     * @return String
     */
    public final String getFullName()
    {
        return m_fullName;
    }

    /**
     * Return the group resource id
     * 
     * @return int
     */
    public final int getGroupRID()
    {
        return m_groupRID;
    }

    /**
     * Return the home directory path
     * 
     * @return String
     */
    public final String getHomeDirectory()
    {
        return m_homeDir;
    }

    /**
     * Return the home drive
     * 
     * @return String
     */
    public final String getHomeDirectoryDrive()
    {
        return m_homeDirDrive;
    }

    /**
     * Return the date/time of last logoff
     * 
     * @return long
     */
    public final long getLastLogoff()
    {
        return m_lastLogoff;
    }

    /**
     * Return the date/time of last logon, to this server
     * 
     * @return long
     */
    public final long getLastLogon()
    {
        return m_lastLogon;
    }

    /**
     * Return the allowed logon hours bit set
     * 
     * @return BitSet
     */
    public final BitSet getLogonHours()
    {
        return m_logonHours;
    }

    /**
     * Return the number of logons for the account, to this server
     * 
     * @return int
     */
    public final int numberOfLogons()
    {
        return m_numLogons;
    }

    /**
     * Return the account provileges
     * 
     * @return int
     */
    public final int getPrivileges()
    {
        return m_priv;
    }

    /**
     * Return the profile path
     * 
     * @return String
     */
    public final String getProfile()
    {
        return m_profile;
    }

    /**
     * Return the password expired flag
     * 
     * @return int
     */
    public final int getPasswordExpired()
    {
        return m_pwdExpired;
    }

    /**
     * Return the logon script path
     * 
     * @return String
     */
    public final String getLogonScriptPath()
    {
        return m_scriptPath;
    }

    /**
     * Return the allowed units per week
     * 
     * @return int
     */
    public final int getUnitsPerWeek()
    {
        return m_unitsPerWeek;
    }

    /**
     * Return the account name
     * 
     * @return String
     */
    public final String getUserName()
    {
        return m_userName;
    }

    /**
     * Return the user resource id
     * 
     * @return int
     */
    public final int getUserRID()
    {
        return m_userRID;
    }

    /**
     * Return the workstations that the account is allowed to logon from
     * 
     * @return String
     */
    public final String getWorkStations()
    {
        return m_workStations;
    }

    /**
     * Return the date/time of the last password change
     * 
     * @return long
     */
    public final long getLastPasswordChange()
    {
        return m_lastPwdChange;
    }

    /**
     * Return the date/time that the password must be changed by
     * 
     * @return long
     */
    public final long getPasswordMustChangeBy()
    {
        return m_pwdMustchange;
    }

    /**
     * Clear all string values
     */
    private final void clearStrings()
    {

        // Clear the string values

        m_appParam = null;
        m_comment = null;
        m_fullName = null;
        m_homeDir = null;
        m_homeDirDrive = null;
        m_profile = null;
        m_scriptPath = null;
        m_userName = null;
        m_workStations = null;
        m_description = null;
        m_accComment = null;
    }

    /**
     * Read the user information from the DCE buffer
     * 
     * @param buf DCEBuffer
     * @throws DCEBufferException
     */
    public void readObject(DCEBuffer buf) throws DCEBufferException
    {

        // clear all existing string values

        clearStrings();

        // Unpack the user information

        int ival = 0;
        int pval = 0;

        switch (getInformationLevel())
        {

        // Information level 1

        case InfoLevel1:
            m_userName = buf.getCharArrayPointer();
            m_fullName = buf.getCharArrayPointer();
            m_groupRID = buf.getInt();
            m_description = buf.getCharArrayPointer();
            m_comment = buf.getCharArrayPointer();
            break;

        // Information level 3

        case InfoLevel3:
            m_userName = buf.getCharArrayPointer();
            m_fullName = buf.getCharArrayPointer();

            m_userRID = buf.getInt();
            m_groupRID = buf.getInt();

            m_homeDir = buf.getCharArrayPointer();
            m_homeDirDrive = buf.getCharArrayPointer();
            m_scriptPath = buf.getCharArrayPointer();
            m_profile = buf.getCharArrayPointer();
            m_workStations = buf.getCharArrayPointer();

            m_lastLogon = buf.getNTTime();
            m_lastLogoff = buf.getNTTime();
            m_lastPwdChange = buf.getNTTime();
            buf.skipBytes(8); // allow password change NT time
            buf.skipBytes(8); // force password change NT time

            ival = buf.getShort(DCEBuffer.ALIGN_INT);
            pval = buf.getPointer();

            if (ival != 0 && pval != 0)
                m_logonHoursRaw = new byte[ival / 8];

            m_badPwdCount = buf.getShort();
            m_numLogons = buf.getShort();

            m_flags = buf.getInt();
            break;

        // Information level 21

        case InfoLevel21:
            m_lastLogon = buf.getNTTime();
            m_lastLogoff = buf.getNTTime();
            m_lastPwdChange = buf.getNTTime();
            m_acctExpires = buf.getNTTime();
            m_pwdCanChange = buf.getNTTime();
            m_pwdMustchange = buf.getNTTime();

            m_userName = buf.getCharArrayPointer();
            m_fullName = buf.getCharArrayPointer();

            m_homeDir = buf.getCharArrayPointer();
            m_homeDirDrive = buf.getCharArrayPointer();
            m_scriptPath = buf.getCharArrayPointer();
            m_profile = buf.getCharArrayPointer();
            m_description = buf.getCharArrayPointer();
            m_workStations = buf.getCharArrayPointer();
            m_accComment = buf.getCharArrayPointer();

            m_callBack = buf.getCharArrayPointer();
            m_unknown1 = buf.getCharArrayPointer();
            m_unknown2 = buf.getCharArrayPointer();
            m_unknown3 = buf.getCharArrayPointer();

            buf.skipBytes(8); // buffer length and pointer

            m_userRID = buf.getInt();
            m_groupRID = buf.getInt();

            m_flags = buf.getInt();

            buf.getInt(); // fields present flags

            ival = buf.getShort(DCEBuffer.ALIGN_INT);
            pval = buf.getPointer();

            if (ival != 0 && pval != 0)
                m_logonHoursRaw = new byte[ival / 8];

            m_badPwdCount = buf.getShort();
            m_numLogons = buf.getShort();

            m_countryCode = buf.getShort();
            m_codePage = buf.getShort();

            buf.skipBytes(2); // NT and LM pwd set flags

            m_pwdExpired = buf.getByte(DCEBuffer.ALIGN_INT);
            break;
        }
    }

    /**
     * Read the strings for this user information from the DCE buffer
     * 
     * @param buf DCEBuffer
     * @throws DCEBufferException
     */
    public void readStrings(DCEBuffer buf) throws DCEBufferException
    {

        // Read the strings/structures for this user information

        switch (getInformationLevel())
        {

        // Information level 1

        case InfoLevel1:
            m_userName = buf.getCharArrayNotNull(m_userName, DCEBuffer.ALIGN_INT);
            m_fullName = buf.getCharArrayNotNull(m_fullName, DCEBuffer.ALIGN_INT);

            m_description = buf.getCharArrayNotNull(m_description, DCEBuffer.ALIGN_INT);
            m_comment = buf.getCharArrayNotNull(m_comment, DCEBuffer.ALIGN_INT);
            break;

        // Information level 3

        case InfoLevel3:
            m_userName = buf.getCharArrayNotNull(m_userName, DCEBuffer.ALIGN_INT);
            m_fullName = buf.getCharArrayNotNull(m_fullName, DCEBuffer.ALIGN_INT);

            m_homeDir = buf.getCharArrayNotNull(m_homeDir, DCEBuffer.ALIGN_INT);
            m_homeDirDrive = buf.getCharArrayNotNull(m_homeDirDrive, DCEBuffer.ALIGN_INT);

            m_scriptPath = buf.getCharArrayNotNull(m_scriptPath, DCEBuffer.ALIGN_INT);
            m_profile = buf.getCharArrayNotNull(m_profile, DCEBuffer.ALIGN_INT);
            m_workStations = buf.getCharArrayNotNull(m_workStations, DCEBuffer.ALIGN_INT);

            m_logonHoursRaw = buf.getByteStructure(m_logonHoursRaw);
            break;

        // Information level 21

        case InfoLevel21:
            m_userName = buf.getCharArrayNotNull(m_userName, DCEBuffer.ALIGN_INT);
            m_fullName = buf.getCharArrayNotNull(m_fullName, DCEBuffer.ALIGN_INT);

            m_homeDir = buf.getCharArrayNotNull(m_homeDir, DCEBuffer.ALIGN_INT);
            m_homeDirDrive = buf.getCharArrayNotNull(m_homeDirDrive, DCEBuffer.ALIGN_INT);

            m_scriptPath = buf.getCharArrayNotNull(m_scriptPath, DCEBuffer.ALIGN_INT);
            m_profile = buf.getCharArrayNotNull(m_profile, DCEBuffer.ALIGN_INT);
            m_description = buf.getCharArrayNotNull(m_description, DCEBuffer.ALIGN_INT);
            m_workStations = buf.getCharArrayNotNull(m_workStations, DCEBuffer.ALIGN_INT);
            m_accComment = buf.getCharArrayNotNull(m_profile, DCEBuffer.ALIGN_INT);

            m_callBack = buf.getCharArrayNotNull(m_callBack, DCEBuffer.ALIGN_INT);
            m_unknown1 = buf.getCharArrayNotNull(m_unknown1, DCEBuffer.ALIGN_INT);
            m_unknown2 = buf.getCharArrayNotNull(m_unknown2, DCEBuffer.ALIGN_INT);
            m_unknown3 = buf.getCharArrayNotNull(m_unknown3, DCEBuffer.ALIGN_INT);

            m_logonHoursRaw = buf.getByteStructure(m_logonHoursRaw);
            break;
        }
    }

    /**
     * Return an account type as a string
     * 
     * @param typ int
     * @return String
     */
    public final static String getAccountTypeAsString(int typ)
    {
        String ret = "";
        switch (typ)
        {
        case PrivGuest:
            ret = "Guest";
            break;
        case PrivUser:
            ret = "User";
            break;
        case PrivAdmin:
            ret = "Administrator";
            break;
        }
        return ret;
    }

    /**
     * Return the user information as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();

        str.append("[");
        str.append(getUserName());
        str.append(":");
        str.append(getInformationLevel());
        str.append(":");

        str.append("]");
        return str.toString();
    }
}