# orange-maven-plugin
This is a Maven plugin facilitating development and deployment.
This Maven plugin will help you upload the successfully installed jar directly to the specified remote server directory through SFTP, and delete old jars in the remote directory

**You need to publish the current project to the repository through Maven, and then follow the steps below to use the plugin**
## Add the following configuration to your pom.xml file
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.orange</groupId>
            <artifactId>orange-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <goals>
                        <goal>sftp-push</goal>
                    </goals>
                    <configuration>
                        <pushEnabled>true</pushEnabled>
                        <configFilePath>/Users/orange/work/code/sft/210/server/qnfc.properties</configFilePath>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
configFilePath is the sftp configuration file path, eg:
```properties
sftp.host=xxx.xxx.xxx.xxx
sftp.username=xxx
sftp.password=xxxx
sftp.remotePath=/home/app/lib
```