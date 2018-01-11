# Installation
Do the following BEFORE starting IntelliJ
1. Install JDK 9.0.1+ from the Oracle website
1. `brew install gradle`
1. `brew install --with-apr --with-ssl --with-compression --devel asaph/tomcat/tomcat`

Now, start IntelliJ
 
1. Open the root level of this as a project.
1. You may be asked to configure gradle. Set Gradle Home to
`/usr/local/Cellar/gradle/4.4.1/libexec`
1. Once the project is loaded, open the gradle panel and hit the refresh icon.
1. If it errors, you may need to click the settings button and select
"use project jdk" for "Gradle JDK"

You'll know the refresh works if `build.gradle` is fully hilighted. 

Further, there doesn't seem to be a way to "share" Application Server settings
using intellij, so go to `Preferences > Build, Execution, Deployment >
Application Servers` then add a Tomcat server (not Tomcat EE). Use 
`/usr/local/Cellar/tomcat/VERSION/libexec` as the path.


# Deploy script
1. You need to have the root key in a known location
2. `pip3 install requests[socks]` 