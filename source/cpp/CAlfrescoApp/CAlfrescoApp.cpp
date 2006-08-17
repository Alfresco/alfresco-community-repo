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

#include <shellapi.h>

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

using namespace std;
using namespace Alfresco;

// CCAlfrescoAppApp

BEGIN_MESSAGE_MAP(CCAlfrescoAppApp, CWinApp)
	ON_COMMAND(ID_HELP, CWinApp::OnHelp)
END_MESSAGE_MAP()


// CCAlfrescoAppApp construction

CCAlfrescoAppApp::CCAlfrescoAppApp()
{
	// TODO: add construction code here,
	// Place all significant initialization in InitInstance
}


// The one and only CCAlfrescoAppApp object

CCAlfrescoAppApp theApp;


// CCAlfrescoAppApp initialization

BOOL CCAlfrescoAppApp::InitInstance()
{
	// InitCommonControls() is required on Windows XP if an application
	// manifest specifies use of ComCtl32.dll version 6 or later to enable
	// visual styles.  Otherwise, any window creation will fail.

	InitCommonControls();
	CWinApp::InitInstance();
	AfxEnableControlContainer();

	// Get the application path

	String appPath = __wargv[0];

	int pos = appPath.lastIndexOf(PathSeperator);

	if ( pos < 0) {
		AfxMessageBox( L"Invalid application path", MB_OK | MB_ICONSTOP);
		return 1;
	}

	// Get the path to the folder containing the application

	String folderPath = appPath.substring(0, pos);
	String exeName    = appPath.substring(pos + 1);

	// Create the Alfresco interface

	AlfrescoInterface alfresco(folderPath);
	if ( alfresco.isAlfrescoFolder()) {

		try {

			// Get the action information

			AlfrescoActionInfo actionInfo = alfresco.getActionInformation(exeName);

			// Check if the action should be confirmed

			if ( actionInfo.hasPreProcessAction(PreConfirmAction)) {

				// Get the confirmation message

				String confirmMsg = actionInfo.getConfirmationMessage();
				if ( confirmMsg.length() == 0)
					confirmMsg = L"Run action ?";

				// Display a confirmation dialog

				if ( AfxMessageBox( confirmMsg, MB_OKCANCEL | MB_ICONQUESTION) == IDCANCEL)
					return FALSE;
			}

			// Check if the action supports multiple paths, if not then call the action once for each supplied path

			if ( actionInfo.hasAttribute(AttrMultiplePaths)) {

				// Build a list of paths from the command line arguments

				StringList pathList;

				for ( int i = 1; i < __argc; i++)
					pathList.addString( String(__wargv[i]));

				// Run the action

				runAction( alfresco, pathList, actionInfo);
			}

			// Check if the action supports file/folder targets

			else if ( actionInfo.hasAttribute( AttrAnyFilesFolders) == true) {

				// Pass one path at a time to the action

				for ( int i = 1; i < __argc; i++) {

					// Create a path list with a single path

					StringList pathList;
					pathList.addString( String(__wargv[i]));

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
bool CCAlfrescoAppApp::buildDesktopParameters( AlfrescoInterface& alfresco, StringList& paths, AlfrescoActionInfo& actionInfo,
											   DesktopParams& params) {

	// If there are no paths then just return a success

    if ( paths.numberOfStrings() == 0)
	    return true;

	// Process the list of files and either check in the file if it is a working copy or check out
	// the file

	for ( unsigned int i = 0; i < paths.numberOfStrings(); i++) {

		// Get the current file name

		String curFile = paths.getStringAt( i);

		// Check if the path is on an Alfresco mapped drive

		if ( alfresco.isMappedDrive() && curFile.startsWithIgnoreCase( alfresco.getDrivePath())) {

			// Convert the path to a UNC path

			String uncPath = alfresco.getRootPath();
			uncPath.append( curFile.substring(2));

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
				return false;
			}
			else if ( actionInfo.supportsFiles() == false) {
				AfxMessageBox(L"Action does not support files", MB_OK | MB_ICONSTOP);
				return false;
			}

			// Get the file name from the path

			StringList nameParts = FileName::splitPath( curFile);
			String curName = nameParts.getStringAt( 1);

			// If the path is to a file that is not on the Alfresco share the file will need to be copied,
			// after checking the status of a matching file in the Alfresco folder

			if ( curFile.length() >= 3 && curFile.substring(1,3).equals( L":\\")) {

				// Check if the action supports local files

				if ( isDir == false && actionInfo.hasAttribute(AttrClientFiles) == false) {
					AfxMessageBox(L"Action does not support local files", MB_OK | MB_ICONSTOP);
					return false;
				}
				else if ( isDir == true && actionInfo.hasAttribute(AttrClientFolders) == false) {
					AfxMessageBox(L"Action does not support local folders", MB_OK | MB_ICONSTOP);
					return false;
				}

				// Check if there is an existing file in the Alfresco with the same name, check if the file is locked
				
				PTR_AlfrescoFileInfo fInfo = alfresco.getFileInformation( curName);
				if ( fInfo.get() != NULL) {

					// There is an existing file in the Alfresco folder with the same name, check if it is locked

					if ( fInfo->getLockType() != LockNone) {
						AfxMessageBox( L"Cannot copy file to Alfresco folder, destination file is locked", MB_OK | MB_ICONEXCLAMATION);
						return false;
					}
					else if ( actionInfo.hasPreProcessAction(PreLocalToWorkingCopy) == true && fInfo->isWorkingCopy() == false) {
						AfxMessageBox( L"Cannot copy to Alfresco folder, destination must overwrite a working copy", MB_OK | MB_ICONEXCLAMATION);
						return false;
					}
				}
				else if ( actionInfo.hasPreProcessAction(PreLocalToWorkingCopy) == true) {

					// Target folder does not contain a matching working copy of the local file

					CString msg;
					msg.FormatMessage( L"No matching working copy for %1", curName.data());
					AfxMessageBox( msg, MB_OK | MB_ICONEXCLAMATION);
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
						return false;
					}
					else {

						// User aborted the file copy

						CString msg;
						msg.FormatMessage( L"Copy aborted for %1", curFile.data());
						AfxMessageBox( msg, MB_OK | MB_ICONSTOP);
						return false;
					}
				}

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
								return false;
							}
							else {

								// User aborted the file copy

								CString msg;
								msg.FormatMessage( L"Copy aborted for %1", curFile.data());
								AfxMessageBox( msg, MB_OK | MB_ICONSTOP);
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
bool CCAlfrescoAppApp::copyFilesUsingShell(const String& fromFileFolder, const String& toFolder, bool& aborted) {

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
bool CCAlfrescoAppApp::runAction( AlfrescoInterface& alfresco, StringList& pathList, AlfrescoActionInfo& actionInfo) {

	// Build the desktop action parameter list, perform any file copying of local files

	bool sts = false;
	DesktopParams desktopParams;
	
	if ( buildDesktopParameters( alfresco, pathList, actionInfo, desktopParams)) {

		// Check if the action requires parameters

		if ( actionInfo.allowsNoParameters() == false && desktopParams.numberOfTargets() == 0) {
			AfxMessageBox( L"No parameters for action", MB_OK | MB_ICONEXCLAMATION);
			return false;
		}

		// Run the desktop action

		DesktopResponse response = alfresco.runAction( actionInfo, desktopParams);

		// Check the response status

		if ( response.getStatus() != StsSuccess) {

			// Check if the status indicates a command line should be launched

			if ( response.getStatus() == StsCommandLine) {

				// Initialize the startup information

				STARTUPINFO startupInfo;
				memset(&startupInfo, 0, sizeof(STARTUPINFO));

				// Launch a process using the command line

				PROCESS_INFORMATION processInfo;
				memset(&processInfo, 0, sizeof(PROCESS_INFORMATION));

				if ( CreateProcess( response.getStatusMessage().data(), NULL, NULL, NULL, true, 0, NULL, NULL,
						&startupInfo, &processInfo) == false) {
					CString msg;
					msg.FormatMessage( L"Failed to launch command line\n\n%1\n\nError %2!d!", response.getStatusMessage().data(), GetLastError());
					AfxMessageBox( msg, MB_OK | MB_ICONERROR);
				}
				else
					sts = true;
			}

			// Check if a web browser should be launched with a URL

			else if ( response.getStatus() == StsLaunchURL) {

				// Use the Windows shell to open the URL

				HINSTANCE shellSts = ShellExecute( NULL, NULL, response.getStatusMessage().data(), NULL, NULL, SW_SHOWNORMAL);
				if (( int) shellSts < 32) {
					CString msg;
					msg.FormatMessage( L"Failed to launch URL\n\n%1", response.getStatusMessage().data());
					AfxMessageBox( msg, MB_OK | MB_ICONERROR);
				}
				else
					sts = true;
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
			}
		}
		else if ( response.hasStatusMessage()) {

			// Display the message returned by the action

			CString msg;
			msg.FormatMessage( L"Action returned message\n\n%1", response.getStatusMessage().data());
			AfxMessageBox( msg, MB_OK | MB_ICONINFORMATION);
		}
	}

	// Return the action status

	return sts;
}
