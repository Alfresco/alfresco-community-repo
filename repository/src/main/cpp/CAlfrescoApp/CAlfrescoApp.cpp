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

#include "stdafx.h"
#include "CAlfrescoApp.h"
#include "CAlfrescoAppDlg.h"

#include <stdlib.h>

#include "util\String.h"
#include "util\DataBuffer.h"
#include "util\FileName.h"
#include "util\Integer.h"
#include "util\Debug.h"

#include <shellapi.h>

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

using namespace std;
using namespace Alfresco;

// CCAlfrescoAppApp

BEGIN_MESSAGE_MAP(CAlfrescoApp, CWinApp)
	ON_COMMAND(ID_HELP, CWinApp::OnHelp)
END_MESSAGE_MAP()

// CCAlfrescoApp construction

CAlfrescoApp::CAlfrescoApp()
{
	// TODO: add construction code here,
	// Place all significant initialization in InitInstance
}


// The one and only CCAlfrescoAppApp object

CAlfrescoApp theApp;


// CCAlfrescoAppApp initialization

BOOL CAlfrescoApp::InitInstance()
{
	// InitCommonControls() is required on Windows XP if an application
	// manifest specifies use of ComCtl32.dll version 6 or later to enable
	// visual styles.  Otherwise, any window creation will fail.

	InitCommonControls();
	CWinApp::InitInstance();
	AfxEnableControlContainer();

	// Check if debug logging is enabled

	char dbgLogName[MAX_PATH];
	size_t dbgLogSize;

	if ( getenv_s( &dbgLogSize, dbgLogName, sizeof( dbgLogName), "ALFDEBUG") == 0) {

		// Enable debug output

		Debug::openLog( dbgLogName);

		// Log the application startup

		DBGOUT_TS << "---------- Desktop client app started ----------" << endl; 
	}

	// Get the application path

	String appPath = __wargv[0];

	int pos = appPath.lastIndexOf(PathSeperator);

	if ( pos < 0) {
		AfxMessageBox( L"Invalid application path", MB_OK | MB_ICONSTOP);
		DBGOUT_TS << "Error, bad application path, " << appPath << endl;
		return 1;
	}

	// Get the path to the folder containing the application

	String folderPath = appPath.substring(0, pos);
	String exeName    = appPath.substring(pos + 1);

	// Create a list of the command line arguments

	StringList argList;
	bool argSetWorkDir = false;

	for ( int i = 1; i < __argc; i++) {

		// Check if the argument is a path or switch

		String arg = __wargv[i];

		if ( arg.startsWith( "/")) {

			// Check for the set working directory switch

			if ( arg.equalsIgnoreCase( "/D")) {
				argSetWorkDir = true;

				// DEBUG

				DBGOUT_TS << "/D switch specified" << endl;
			}
			else {
				String msg = L"Invalid command line switch - ";
				msg.append( arg);
				AfxMessageBox( msg.data(), MB_OK | MB_ICONSTOP);
				DBGOUT_TS << "Error, " << msg << endl;
				return 2;
			}
		}
		else {

			// Add the path to the argument list

			argList.addString( arg);
		}
	}

	// Check if the working directory should be set to the path of the first document

	if ( argSetWorkDir == true) {

		// Check if there are any document paths

		if ( argList.numberOfStrings() == 0) {
			AfxMessageBox( L"Cannot set working directory, no document paths", MB_OK | MB_ICONSTOP);
			DBGOUT_TS << "Error, cannot set working directory, no document paths" << endl;
			return 3;
		}

		// Get the first document path and remove the file name

		String docPath = argList[0];
		pos = docPath.lastIndexOf( PathSeperator);

		if ( pos < 0) {
			AfxMessageBox( L"Invalid document path", MB_OK | MB_ICONSTOP);
			DBGOUT_TS << "Error, invalid document path, " << docPath << endl;
			return 4;
		}

		// Set the document path as the working directory folder

		folderPath = docPath.substring(0, pos);

		// DEBUG

		DBGOUT_TS << "Using document path as working directory, " << folderPath << endl;
	}

	// DEBUG

	if ( HAS_DEBUG)
		DBGOUT_TS << "Using folder path " << folderPath << " for Alfresco base dir" << endl;

	// Create the Alfresco interface

	AlfrescoInterface alfresco(folderPath);

	if ( alfresco.isAlfrescoFolder()) {

		try {

			// DEBUG

			DBGOUT_TS << "Using folder " << folderPath << endl;

			// Get the action information

			AlfrescoActionInfo actionInfo = alfresco.getActionInformation(exeName);

			// DEBUG

			if ( HAS_DEBUG) {
				DBGOUT_TS << "Action " << actionInfo.getName() << endl;
				DBGOUT_TS << "  PreProcess: ";

				if ( actionInfo.hasPreProcessAction( PreConfirmAction))
					DBGOUT << "Confirm ";
				if ( actionInfo.hasPreProcessAction( PreCopyToTarget))
					DBGOUT << "CopyToTarget ";
				if ( actionInfo.hasPreProcessAction( PreLocalToWorkingCopy))
					DBGOUT << "LocalToWorkingCopy";
				DBGOUT << endl;
			}

			// Check if the action should be confirmed

			if ( actionInfo.hasPreProcessAction(PreConfirmAction)) {

				// Get the confirmation message

				String confirmMsg = actionInfo.getConfirmationMessage();
				if ( confirmMsg.length() == 0)
					confirmMsg = L"Run action ?";

				// DEBUG

				DBGOUT_TS << "Confirm action, message = " << confirmMsg << endl;

				// Display a confirmation dialog

				if ( AfxMessageBox( confirmMsg, MB_OKCANCEL | MB_ICONQUESTION) == IDCANCEL) {
					DBGOUT_TS << "User cancelled action" << endl;
					return FALSE;
				}
			}

			// Check if the action supports multiple paths, if not then call the action once for each supplied path

			if ( actionInfo.hasAttribute(AttrMultiplePaths)) {

				// Run the action

				runAction( alfresco, argList, actionInfo);
			}

			// Check if the action supports file/folder targets

			else if ( actionInfo.hasAttribute( AttrAnyFilesFolders) == true) {

				// Pass one path at a time to the action

				for ( size_t i = 0; i < argList.numberOfStrings(); i++) {

					// Create a path list with a single path

					StringList pathList;
					pathList.addString( argList[i]);

					// Run the action

					runAction( alfresco, pathList, actionInfo);
				}
			}

			// Action does not use targets, just run the action

			else if ( actionInfo.allowsNoParameters()) {

				// Run the action

				StringList emptyList;
				runAction( alfresco, emptyList, actionInfo);
			}
		}
		catch (Exception ex) {
			CString msg;
			msg.FormatMessage( L"Exception occurred\n\n%1", ex.getMessage().data());
			AfxMessageBox( msg, MB_OK | MB_ICONSTOP);
		}
	}
	else {
		AfxMessageBox( L"Not a valid Alfresco CIFS folder", MB_OK | MB_ICONSTOP);
		DBGOUT_TS << "Error, not a valid Alfresco CIFS folder, " << folderPath << endl;
		return 1;
	}

	//	Exit the application

	return FALSE;
}

/**
 * Process the command line arguments and build the parameter list for the desktop action
 *
 * @param alfresco AlfrescoInterface&
 * @param paths StringList&
 * @param actionInfo AlfrescoActionInfo&
 * @param params DesktopParams&
 * @return bool
 */
bool CAlfrescoApp::buildDesktopParameters( AlfrescoInterface& alfresco, StringList& paths, AlfrescoActionInfo& actionInfo,
										    DesktopParams& params) {

	// If there are no paths then just return a success

    if ( paths.numberOfStrings() == 0)
	    return true;

	// Process the list of files and either check in the file if it is a working copy or check out
	// the file

	for ( unsigned int i = 0; i < paths.numberOfStrings(); i++) {

		// Get the current file name

		String curFile = paths.getStringAt( i);

		// DEBUG

		DBGOUT_TS << "Parameter: " << curFile << endl;

		// Check if the path is on an Alfresco mapped drive

		if ( alfresco.isMappedDrive() && curFile.startsWithIgnoreCase( alfresco.getDrivePath())) {

			// Convert the path to a UNC path

			String uncPath = alfresco.getRootPath();
			uncPath.append( curFile.substring(3));

			curFile = uncPath;
		}

		// Check if the path is to a file/folder, and whether it is a local path

		bool copyFile = false;
		DWORD attr = GetFileAttributes( curFile);

		if ( attr != INVALID_FILE_ATTRIBUTES) {
			
			// Check if the action supports the file/folder type

			bool isDir = (attr & FILE_ATTRIBUTE_DIRECTORY) != 0 ? true : false;

			if ( isDir && actionInfo.supportsFolders() == false) {
				AfxMessageBox(L"Action does not support folders", MB_OK | MB_ICONSTOP);
				DBGOUT_TS << "Error, action does not support folders" << endl;
				return false;
			}
			else if ( actionInfo.supportsFiles() == false) {
				AfxMessageBox(L"Action does not support files", MB_OK | MB_ICONSTOP);
				DBGOUT_TS << "Error, action does not support files" << endl;
				return false;
			}

			// Get the file name from the path

			StringList nameParts = FileName::splitPath( curFile);
			String curName = nameParts.getStringAt( 1);

			// If the path is to a file that is not on the Alfresco share the file will need to be copied,
			// after checking the status of a matching file in the Alfresco folder

			if ( curFile.length() >= 3 && curFile.substring(1,3).equals( L":\\") &&
				(alfresco.isMappedDrive() == false || curFile.startsWithIgnoreCase( alfresco.getDrivePath()) == false)) {

				// Check if the action supports local files

				if ( isDir == false && actionInfo.hasAttribute(AttrClientFiles) == false) {
					AfxMessageBox(L"Action does not support local files", MB_OK | MB_ICONSTOP);
					DBGOUT_TS << "Error, action does not support local files" << endl;
					return false;
				}
				else if ( isDir == true && actionInfo.hasAttribute(AttrClientFolders) == false) {
					AfxMessageBox(L"Action does not support local folders", MB_OK | MB_ICONSTOP);
					DBGOUT_TS << "Error, action does not support local folders" << endl;
					return false;
				}

				// Check if there is an existing file in the Alfresco with the same name, check if the file is locked
				
				PTR_AlfrescoFileInfo fInfo = alfresco.getFileInformation( curName);
				if ( fInfo.get() != NULL) {

					// There is an existing file in the Alfresco folder with the same name, check if it is locked

					if ( fInfo->getLockType() != LockNone) {
						AfxMessageBox( L"Cannot copy file to Alfresco folder, destination file is locked", MB_OK | MB_ICONEXCLAMATION);
						DBGOUT_TS << "Error, cannot copy to Alfresco folder, destination file is locked" << endl;
						return false;
					}
					else if ( actionInfo.hasPreProcessAction(PreLocalToWorkingCopy) == true && fInfo->isWorkingCopy() == false) {
						AfxMessageBox( L"Cannot copy to Alfresco folder, destination must overwrite a working copy", MB_OK | MB_ICONEXCLAMATION);
						DBGOUT_TS << "Error, cannot copy to Alfresco folder, destination must overwrite a working copy" << endl;
						return false;
					}
				}
				else if ( actionInfo.hasPreProcessAction(PreLocalToWorkingCopy) == true) {

					// Target folder does not contain a matching working copy of the local file

					CString msg;
					msg.FormatMessage( L"No matching working copy for %1", curName.data());
					AfxMessageBox( msg, MB_OK | MB_ICONEXCLAMATION);
					DBGOUT_TS << "Error, no matching working copy for " << curName << endl;
					return false;
				}

				// Copy the files/folders using the Windows shell

				bool copyAborted = false;

				if ( copyFilesUsingShell( curFile, alfresco.getUNCPath(), copyAborted) == false) {

					// Check if the copy failed or the user aborted the copy

					if ( copyAborted == false) {

						// File copy failed

						CString msg;
						msg.FormatMessage( isDir ? L"Failed to copy folder %1" : L"Failed to copy file %1", curFile.data());

						AfxMessageBox( msg, MB_OK | MB_ICONSTOP);
						DBGOUT_TS << "Error, copy failed for " << curName << endl;
						return false;
					}
					else {

						// User aborted the file copy

						CString msg;
						msg.FormatMessage( L"Copy aborted for %1", curFile.data());
						AfxMessageBox( msg, MB_OK | MB_ICONSTOP);
						DBGOUT_TS << "Error, copy aborted by user, " << curName << endl;
						return false;
					}
				}

				// DEBUG

				DBGOUT_TS << "Added target " << curName << endl;

				// Add a desktop target for the copied file

				params.addTarget( new DesktopTarget(isDir ? TargetCopiedFolder : TargetCopiedFile, curName));
			}
			else {

				//	Path is a UNC path, check if the file/folder is in the same folder as the action

				DesktopTarget* pTarget = NULL;

				if ( curFile.startsWith( alfresco.getUNCPath())) {

					// Path is in the same folder as the application, or in a sub-folder

					String relPath = curFile.substring( alfresco.getUNCPath().length() + 1);

					if ( relPath.indexOf( L"\\") == -1) {

						// Create a target using the file name only

						pTarget = new DesktopTarget( isDir ? TargetFolder : TargetFile, relPath);
					}
				}

				// If the target is not valid the file/folder is not in the same folder as the client-side application,
				// copy the files/folders to the target folder or use the root relative path to the file/folder

				if ( pTarget == NULL) {

					// Check if Alfresco files/folders should be copied to the target folder

					if ( actionInfo.hasPreProcessAction(PreCopyToTarget)) {

						// Copy the files/folders using the Windows shell

						bool copyAborted = false;

						if ( copyFilesUsingShell( curFile, alfresco.getUNCPath(), copyAborted) == false) {

							// Check if the copy failed or the user aborted the copy

							if ( copyAborted == false) {

								// File copy failed

								CString msg;
								msg.FormatMessage( isDir ? L"Failed to copy folder %1" : L"Failed to copy file %1", curFile.data());

								AfxMessageBox( msg, MB_OK | MB_ICONSTOP);
								DBGOUT_TS << "Error, copy failed for " << curName << endl;
								return false;
							}
							else {

								// User aborted the file copy

								CString msg;
								msg.FormatMessage( L"Copy aborted for %1", curFile.data());
								AfxMessageBox( msg, MB_OK | MB_ICONSTOP);
								DBGOUT_TS << "Error, copy aborted for " << curName << endl;
								return false;
							}
						}

						// Add a desktop target for the copied file

						pTarget= new DesktopTarget(isDir ? TargetCopiedFolder : TargetCopiedFile, curName);
					}
					else {

						// Get the root relative path to the file/folder

						String rootRelPath = curFile.substring(alfresco.getRootPath().length());
						pTarget = new DesktopTarget( isDir ? TargetFolder : TargetFile, rootRelPath);
					}
				}

				// DEBUG

				DBGOUT_TS << "Added target " << pTarget->getTarget() << endl;

				// Add the desktop target

				params.addTarget( pTarget);
			}
		}
	}

	// Return status

	return true;
}

/**
 * Copy a file/folder using the Windows shell
 *
 * @param fromFileFolder const String&
 * @param toFolder const String&
 * @param aborted bool&
 * @return bool
 */
bool CAlfrescoApp::copyFilesUsingShell(const String& fromFileFolder, const String& toFolder, bool& aborted) {

	// DEBUG

	DBGOUT_TS << "Copy from " << fromFileFolder << " to " << toFolder << endl;

	// Build the from/to paths, must be double null terminated

	wchar_t fromPath[MAX_PATH + 1];
	wchar_t toPath[MAX_PATH + 1];

	memset( fromPath, 0, sizeof( fromPath));
	memset( toPath, 0, sizeof( toPath));

	wcscpy( fromPath, fromFileFolder.data());
	wcscpy( toPath, toFolder.data());

	// Copy the local file to the Alfresco folder

	SHFILEOPSTRUCT fileOpStruct;
	memset( &fileOpStruct, 0, sizeof(SHFILEOPSTRUCT));

	fileOpStruct.hwnd  = HWND_DESKTOP;
	fileOpStruct.wFunc = FO_COPY;
	fileOpStruct.pFrom = fromPath;
	fileOpStruct.pTo   = toPath;
	fileOpStruct.fFlags= 0;
	fileOpStruct.fAnyOperationsAborted =false;

	// Copy the file to the Alfresco folder

	bool sts = false;

	if ( SHFileOperation( &fileOpStruct) == 0) {

		// File copy successful

		sts = true;
	}
	else if ( fileOpStruct.fAnyOperationsAborted) {

		// User aborted the file copy

		aborted = true;
	}

	// Return the copy status

	return sts;
}

/**
 * Run an action
 *
 * @param alfresco AlfrescoInterface&
 * @param pathList StringList&
 * @param actionInfo AlfrescoActionInfo&
 * @return bool
 */
bool CAlfrescoApp::runAction( AlfrescoInterface& alfresco, StringList& pathList, AlfrescoActionInfo& actionInfo) {

	// Build the desktop action parameter list, perform any file copying of local files

	bool sts = false;
	DesktopParams desktopParams;
	
	if ( buildDesktopParameters( alfresco, pathList, actionInfo, desktopParams)) {

		// Check if the action requires parameters

		if ( actionInfo.allowsNoParameters() == false && desktopParams.numberOfTargets() == 0) {
			AfxMessageBox( L"No parameters for action", MB_OK | MB_ICONEXCLAMATION);
			DBGOUT_TS << "Error, no parameters for action" << endl;
			return false;
		}

		// Run the desktop action

		DesktopResponse response = alfresco.runAction( actionInfo, desktopParams);

		// Check the response status

		if ( response.getStatus() != StsSuccess) {

			// Check if the status indicates a command line should be launched

			if ( response.getStatus() == StsCommandLine) {

				// DEBUG

				DBGOUT_TS << "Action returned command line, " << response.getStatusMessage() << endl;

				// Launch a process using the command line

				sts = doCommandLine( alfresco, response.getStatusMessage());
			}

			// Check if a web browser should be launched with a URL

			else if ( response.getStatus() == StsLaunchURL) {

				// DEBUG

				DBGOUT_TS << "Action returned URL, " << response.getStatusMessage() << endl;

				// Use the Windows shell to open the URL

				sts = doURL( alfresco, response.getStatusMessage());
			}

			// Error status

			else {

				//	Get the error message

				String errMsg;

				switch ( response.getStatus()) {
					case StsFileNotFound:
						errMsg = L"File not found";
						break;
					case StsAccessDenied:
						errMsg = L"Access denied";
						break;
					case StsBadParameter:
						errMsg = L"Bad parameter in request";
						break;
					case StsNoSuchAction:
						errMsg = L"No such action";
						break;
					default:
						errMsg = L"Error running action";
						break;
				}

				// Display an error dialog

				CString msg;

				if ( response.hasStatusMessage())
					msg.FormatMessage( L"%1\n\n%2", errMsg.data(), response.getStatusMessage().data());
				else
					msg = errMsg.data();

				AfxMessageBox( msg, MB_OK | MB_ICONERROR);

				DBGOUT_TS << "Action returned error status, " << msg << endl;
			}
		}
		else if ( response.hasStatusMessage()) {

			// Display the message returned by the action

			CString msg;
			msg.FormatMessage( L"Action returned message\n\n%1", response.getStatusMessage().data());
			AfxMessageBox( msg, MB_OK | MB_ICONINFORMATION);

			DBGOUT_TS << "Action returned error message, " << msg << endl;
		}
	}

	// Return the action status

	return sts;
}

/**
 * Launch a command line
 *
 * @param alfresco AlfrescoInterface&
 * @param cmdStr const String&
 * @return bool
 */
bool CAlfrescoApp::doCommandLine( AlfrescoInterface& alfresco, const String& cmdStr) {

	// Check if the command line contains any environment variables/tokens

	String cmdLine = cmdStr;
	int pos = cmdLine.indexOf( L'%');

	if ( pos != -1) {

		// Command line contains environment variables or other tokens that must be replaced

		String newCmdLine = L"";
		if (pos > 0)
			newCmdLine = cmdLine.substring( 0, pos);

		wchar_t envBuf[256];
		size_t envLen;

		while ( pos != -1) {

			// Find the end of the current token

			int endPos = cmdLine.indexOf ( L'%', pos + 1);

			if ( endPos == -1) {
				CString msg;
				msg.FormatMessage( L"Bad token in command line\n\n%1", cmdLine.data());
				AfxMessageBox( msg, MB_OK | MB_ICONERROR);

				return false;
			}

			// Extract the token

			String token = cmdLine.substring( pos + 1, endPos);

			// Replace the token with an environment variable value or other values

			if ( token.equals( L"AlfrescoDir")) {

				// Use the local path to the Alfresco folder that the application is running from

				newCmdLine.append( alfresco.getUNCPath());
			}
			else {

				// Find the environment variable value

				envLen = sizeof( envBuf)/sizeof(wchar_t);
				const wchar_t* pEnvName = token.data();

				if ( _wgetenv_s( &envLen, envBuf, envLen, pEnvName) == 0) {

					// Append the environment variable value

					newCmdLine.append( envBuf);
				}
				else {

					// Error converting the environment variable

					CString msg;
					msg.FormatMessage( L"Failed to convert environment variable\n\n%1\n\n%2", token.data(), cmdLine.data());
					AfxMessageBox( msg, MB_OK | MB_ICONERROR);

					return false;
				}
			}

			// Update the token search position

			pos = endPos + 1;

			if (( unsigned int) pos < cmdStr.length()) {

				// Search for the next token

				pos = cmdLine.indexOf( L'%', pos);
			}
			else {

				// End of string, finish the token search

				pos = -1;
			}

			// Append the normal string between tokens

			if ( pos > (endPos + 1)) {

				// Get the between token sting

				String filler = cmdLine.substring( endPos + 1, pos);
				newCmdLine.append( filler);
			}
			else if ( pos == -1) {

				// Append the remaining string

				String filler = cmdLine.substring( endPos + 1);
				newCmdLine.append( filler);
			}
		}

		// Update the command line

		cmdLine = newCmdLine;
	}

	// Initialize the startup information

	STARTUPINFO startupInfo;
	memset(&startupInfo, 0, sizeof(STARTUPINFO));

	// Launch a process using the command line

	PROCESS_INFORMATION processInfo;
	memset(&processInfo, 0, sizeof(PROCESS_INFORMATION));

	bool sts = false;

	if ( CreateProcess( NULL, (LPWSTR) cmdLine.data(), NULL, NULL, true, 0, NULL, NULL,
			&startupInfo, &processInfo) == false) {
		CString msg;
		msg.FormatMessage( L"Failed to launch command line\n\n%1\n\nError %2!d!", cmdLine.data(), GetLastError());
		AfxMessageBox( msg, MB_OK | MB_ICONERROR);

		// DEBUG

		DBGOUT_TS << "Error, failed to launch command line, status " << GetLastError() << endl;
	}
	else
		sts = true;

	return sts;
}

/**
 * Browse to a URL
 *
 * @param alfresco AlfrescoInterface&
 * @param url const String&
 * @return bool
 */
bool CAlfrescoApp::doURL( AlfrescoInterface& alfresco, const String& url) {

	// Use the Windows shell to open the URL

	bool sts = false;

	HINSTANCE shellSts = ShellExecute( NULL, NULL, url.data(), NULL, NULL, SW_SHOWNORMAL);
	if (( int) shellSts < 32) {
		CString msg;
		msg.FormatMessage( L"Failed to launch URL\n\n%1", url.data());
		AfxMessageBox( msg, MB_OK | MB_ICONERROR);
	}
	else
		sts = true;

	return sts;
}
