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


# Development
### Postgres
If `ENVIRONMENT` is set to `local`, we look for the
`hostname.psql.properties` property file for db connection info. If you
want to use RDS, forward a port using an SSH tunnel like this:

```ssh -i ~/.ssh/monday-root.pem -L 9999:mp72vx2mjszadf.c4erhqsd8cq3.us-east-2.rds.amazonaws.com:5432 ec2-user@bastion.monday.health -N```

where `mp72vx2mjszadf.c4erhqsd8cq3.us-east-2.rds.amazonaws.com` is the
internal address of the db server you're trying to connect to.


# Deploy script
1. You need to have the root key in a known location
2. `pip3 install requests[socks]` 