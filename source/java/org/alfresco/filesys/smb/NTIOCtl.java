/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.filesys.smb;

/**
 * NT IO Control Codes Class
 */
public class NTIOCtl
{

    //	Device type codes
  
    public static final int DeviceBeep                  = 0x0001;
    public static final int DeviceCDRom                 = 0x0002;
    public static final int DeviceCDRomFileSystem       = 0x0003;
    public static final int DeviceController            = 0x0004;
    public static final int DeviceDataLink              = 0x0005;
    public static final int DeviceDFS                   = 0x0006;
    public static final int DeviceDisk                  = 0x0007;
    public static final int DeviceDiskFileSystem        = 0x0008;
    public static final int DeviceFileSystem            = 0x0009;
    public static final int DeviceInportPort            = 0x000A;
    public static final int DeviceKeyboard              = 0x000B;
    public static final int DeviceMailSlot              = 0x000C;
    public static final int DeviceMidiIn                = 0x000D;
    public static final int DeviceMidiOut               = 0x000E;
    public static final int DeviceMouse                 = 0x000F;
    public static final int DeviceMultiUNCProvider      = 0x0010;
    public static final int DeviceNamedPipe             = 0x0011;
    public static final int DeviceNetwork               = 0x0012;
    public static final int DeviceNetworkBrowser        = 0x0013;
    public static final int DeviceNetworkFileSystem     = 0x0014;
    public static final int DeviceNull                  = 0x0015;
    public static final int DeviceParallelPort          = 0x0016;
    public static final int DevicePhysicalNetCard       = 0x0017;
    public static final int DevicePrinter               = 0x0018;
    public static final int DeviceScanner               = 0x0019;
    public static final int DeviceSerialMousePort       = 0x001A;
    public static final int DeviceSerialPort            = 0x001B;
    public static final int DeviceScreen                = 0x001C;
    public static final int DeviceSound                 = 0x001D;
    public static final int DeviceStreams               = 0x001E;
    public static final int DeviceTape                  = 0x001F;
    public static final int DeviceTapeFileSystem        = 0x0020;
    public static final int DeviceTransport             = 0x0021;
    public static final int DeviceUnknown               = 0x0022;
    public static final int DeviceVideo                 = 0x0023;
    public static final int DeviceVirtualDisk           = 0x0024;
    public static final int DeviceWaveIn                = 0x0025;
    public static final int DeviceWaveOut               = 0x0026;
    public static final int Device8042Port              = 0x0027;
    public static final int DeviceNetworkRedirector     = 0x0028;
    public static final int DeviceBattery               = 0x0029;
    public static final int DeviceBusExtender           = 0x002A;
    public static final int DeviceModem                 = 0x002B;
    public static final int DeviceVDM                   = 0x002C;
    public static final int DeviceMassStorage           = 0x002D;
    public static final int DeviceSMB                   = 0x002E;
    public static final int DeviceKS                    = 0x002F;
    public static final int DeviceChanger               = 0x0030;
    public static final int DeviceSmartCard             = 0x0031;
    public static final int DeviceACPI                  = 0x0032;
    public static final int DeviceDVD                   = 0x0033;
    public static final int DeviceFullScreenVideo       = 0x0034;
    public static final int DeviceDFSFileSystem         = 0x0035;
    public static final int DeviceDFSVolume             = 0x0036;
  
    //	Method types for I/O and filesystem controls
  
    public static final int MethodBuffered					= 0;
    public static final int MethodInDirect					= 1;
    public static final int MethodOutDirect					= 2;
    public static final int MethodNeither						= 3;
  
    //	Access check types
  
    public static final int AccessAny								= 0;
    public static final int AccessRead							= 0x0001;
    public static final int AccessWrite							= 0x0002;
  
    //	Filesystem function codes
  
    public static final int FsCtlRequestOplockLevel1	= 0;
    public static final int FsCtlRequestOplockLevel2	= 1;
    public static final int FsCtlRequestBatchOplock 	= 2;
    public static final int FsCtlOplockBreakAck     	= 3;
    public static final int FsCtlOpBatchAckClosePend	= 4;
    public static final int FsCtlOplockBreakNotify  	= 5;
    public static final int FsCtlLockVolume         	= 6;
    public static final int FsCtlUnlockVolume       	= 7;
    public static final int FsCtlDismountVolume     	= 8;
    public static final int FsCtlIsVolumeMounted    	= 10;
    public static final int FsCtlIsPathnameValid    	= 11;
    public static final int FsCtlMarkVolumeDirty    	= 12;
    public static final int FsCtlQueryRetrievalPtrs 	= 14;
    public static final int FsCtlGetCompression     	= 15;
    public static final int FsCtlSetCompression     	= 16;
    public static final int FsCtlMarkAsSystemHive   	= 19;
    public static final int FsCtlOplockBreakAck2    	= 20;
    public static final int FsCtlInvalidateVolumes  	= 21;
    public static final int FsCtlQueryFatBPB        	= 22;
    public static final int FsCtlRequestFilterOplock	= 23;
    public static final int FsCtlFileSysGetStats    	= 24;
    public static final int FsCtlGetNTFSVolumeData  	= 25;
    public static final int FsCtlGetNTFSFileRecord  	= 26;
    public static final int FsCtlGetVolumeBitmap    	= 27;
    public static final int FsCtlGetRetrievalPtrs   	= 28;
    public static final int FsCtlMoveFile           	= 29;
    public static final int FsCtlIsVolumeDirty      	= 30;
    public static final int FsCtlGetHFSInfo         	= 31;
    public static final int FsCtlAllowExtenDasdIO   	= 32;
    public static final int FsCtlReadPropertyData   	= 33;
    public static final int FsCtlWritePropertyData  	= 34;
    public static final int FsCtlFindFilesBySID     	= 35;
    public static final int FsCtlDumpPropertyData   	= 37;
    public static final int FsCtlSetObjectId        	= 38;
    public static final int FsCtlGetObjectId        	= 39;
    public static final int FsCtlDeleteObjectId     	= 40;
    public static final int FsCtlSetReparsePoint    	= 41;
    public static final int FsCtlGetReparsePoint    	= 42;
    public static final int FsCtlDeleteReparsePoint 	= 43;
    public static final int FsCtlEnumUsnData        	= 44;
    public static final int FsCtlSecurityIdCheck    	= 45;
    public static final int FsCtlReadUsnJournal     	= 46;
    public static final int FsCtlSetObjectIdExtended	= 47;
    public static final int FsCtlCreateOrGetObjectId	= 48;
    public static final int FsCtlSetSparse          	= 49;
    public static final int FsCtlSetZeroData        	= 50;
    public static final int FsCtlQueryAllocRanges   	= 51;
    public static final int FsCtlEnableUpgrade      	= 52;
    public static final int FsCtlSetEncryption      	= 53;
    public static final int FsCtlEncryptionFsCtlIO  	= 54;
    public static final int FsCtlWriteRawEncrypted  	= 55;
    public static final int FsCtlReadRawEncrypted   	= 56;
    public static final int FsCtlCreateUsnJournal   	= 57;
    public static final int FsCtlReadFileUsnData    	= 58;
    public static final int FsCtlWriteUsnCloseRecord	= 59;
    public static final int FsCtlExtendVolume       	= 60;

    // Base value for custom control codes
    
    public static final int FsCtlCustom                 = 0x800;
    
    /**
     * Extract the device type from an I/O control code
     * 
     * @param ioctl int
     * @return int
     */
    public final static int getDeviceType(int ioctl)
    {
        return (ioctl >> 16) & 0x0000FFFF;
    }

    /**
     * Extract the access type from an I/O control code
     * 
     * @param ioctl int
     * @return int
     */
    public final static int getAccessType(int ioctl)
    {
        return (ioctl >> 14) & 0x00000003;
    }

    /**
     * Extract the function code from the I/O control code
     * 
     * @param ioctl int
     * @return int
     */
    public final static int getFunctionCode(int ioctl)
    {
        return (ioctl >> 2) & 0x00000FFF;
    }

    /**
     * Extract the method code from the I/O control code
     * 
     * @param ioctl int
     * @return int
     */
    public final static int getMethod(int ioctl)
    {
        return ioctl & 0x00000003;
    }

    /**
     * Make a control code
     * 
     * @param devType int
     * @param func int
     * @param method int
     * @param access int
     * @return int
     */
    public final static int makeControlCode(int devType, int func, int method, int access)
    {
        return (devType << 16) + (access << 14) + (func << 2) + (method);
    }

    /**
     * Return an I/O control code as a string
     * 
     * @param ioctl int
     * @return String
     */
    public final static String asString(int ioctl)
    {
        StringBuffer str = new StringBuffer();

        str.append("[Func:");
        str.append(getFunctionCode(ioctl));

        str.append(",DevType:");
        str.append(getDeviceType(ioctl));

        str.append(",Access:");
        str.append(getAccessType(ioctl));

        str.append(",Method:");
        str.append(getMethod(ioctl));

        str.append("]");

        return str.toString();
    }
}
