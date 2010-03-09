This simple example connects to the Alfresco CMIS Server and displays the names
of the folders within the Alfresco root folder called "Company Home".

The example utilizes the Alfresco CMIS Web Services interfaces.

The example is stand-alone and includes all of its dependencies.

Steps to execute the example...

Pre-requisites:
a) The Alfresco Server must be running for the test client to succeed.
b) ant is required to build the test client

1) Unpack JavaCmisTest.zip to <destdir>
2) Run <destdir>/ant
3) Run <destdir>cmis-test.bat http://<alfresco_host>:<alfresco_port> <username> <password>

Note: A cmis-test.sh also exists.

Upon successful completion, a list of folder names is presented.

The source code for the example is also included.