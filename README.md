# Installation
1. Install JDK 9.0.1+ from the Oracle website
1. `brew install gradle`
1. `brew install --with-apr --with-ssl --with-compression --devel asaph/tomcat/tomcat`
1. Configure intellij with jdk9 you installed
1. In settings > application servers, add your local tomcat 9 install
1. Open this project

# Deploy script
1. You need to have the root key in a known location
2. `pip3 install requests[socks]` 