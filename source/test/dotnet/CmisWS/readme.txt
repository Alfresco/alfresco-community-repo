COMPILATION

    To compile "WcfCmisWSTests" project you need:
        1) install .NET SDK v3.5 to your system;
        2) copy full "WcfCmisWSTests" project to some directory (e.g.: "C:\WcfCmisWSTests");
        3) check if the .NET SDK location and "SDK_LOCATION" variable value in the "build.bat" file are the same
           and introduce neccessary corrections;
        4) run "build.bat" file.
    After compilation finished "build" folder should be created in the project directory. This folder will
    contain all neccessary for tests execution libraries and configuration files.
    "WcfCmisWSTests.dll" library is the library that design for execution in the NUnit.

    NOTE: this tests were developed under NUnit 2.4.5 version.

TESTS RUNNING

    To run tests you may use one of the next two methods:
        I.  1) launch "NUnit.exe" executable;
            2) choose "File" > "Open project..." menu and in file dialog select "WcfCmisWSTests.dll" file and
               click "Open" button;
            3) in the left side of the main "NUnit" window select neccessary test tree node.
               NOTE: select most top node to select all tests; you can make right click on that tree and click on
               "Show CheckBoxes" menu item to select not grouped tests;
            4) After "Run" button clicking "NUnit" will start test execution. "Run" button became disable and
               "Stop" button - enabled. During tests execution "NUnit" will mark tests nodes in the tree with
               icons those will be conform to passing status (see "NUnit" documentation for more details). After
               "Run" button became enabled again you can see for errors and/or warnings reports and some other log
               information if any clicking on the tabs placed in the below of main "NUnit" window.
        II. 1) launch system command line console ("Start" > "Run...", type "cmd" in the appeared window and click
               "Ok");
            2) if system environment not configured with "NUnit" home path navigate with "cd" system command to
               "NUnit" home folder;
            3) type "nunit-console.exe", break and full path to "WcfCmisWSTests.dll" file (e.g.: nunit-console.exe
               C:\WcfCmisWSTests\build\WcfCmisWSTests.dll);
            4)* type break. Now you can introduce some options for "NUnit" system and report generation. For
                example, to store testing results to the xml you can add "xml=C:\test-report.xml". For more details
                about "NUnit" conlose options see "NUnit" documentation or execute "nunit-console.exe" without any
                parameters;
            5) press "Enter" key. After "NUnit" finish tests execution you can see generated report.

            * This step is optional and may be skipped.

            NOTE: by default "NUnit" save testing results in the "TestResult.xml" or "<SelectedClassName>.xml"
            file in the "NUnit" home directory. Also some information about tests execution will be displayed in
            the console window.
