This project is a Java application that demonstrates the usage of Interactive Brokers' API.

This documentation outlines the steps to get the development environment set up on Windows 7.

## Set up Java
1. Download and install [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

	**Note**: The JDK version has to be 8, instead of the latest version 9, due to a [bug](https://github.com/spring-projects/spring-boot/issues/11042). See also [here](https://jira.spring.io/browse/SPR-15859).

1. Create an environment variable ``JAVA_HOME`` and set it to the location where **JDK** (not JRE) is installed, e.g., ``C:\Program Files\Java\jdk1.8.0_161``
1. Edit the ``PATH`` environment variable to **prepend** the following: 
``%JAVA_HOME%\bin;``

	**Note**: This has to be added to the very beginning of the ``PATH`` environment in order to override the existing path that was created by the JDK installer. That existing path does not contain the Java Compiler which we will need in subsequent steps.

## Set up Maven
1. Download from [here](http://mirror.intergrid.com.au/apache/maven/maven-3/3.5.3/binaries/apache-maven-3.5.3-bin.zip).
1. Unzip the downloaded file to a convenient location, such as ``C:\Maven``
1. Add the following two environment variables:

 ```
 M2_HOME=C:\Maven\apache-maven-3.5.3
 MAVEN_HOME=C:\Maven\apache-maven-3.5.3
 ```

1. Update the ``PATH`` envieronment variable and append ``%M2_HOME%\bin`` to it.

### Verify
Open a command prompt window and execute ``mvn -v``. Expect to see something like below:

```
C:\Users\test>mvn -v
Apache Maven 3.5.3 (3383c37e1f9e9b3bc3df5050c29c8aff9f295297; 2018-02-25T06:49:05+11:00)
Maven home: C:\Maven\apache-maven-3.5.3\bin\..
Java version: 9.0.4, vendor: Oracle Corporation
Java home: C:\Program Files\Java\jre-9.0.4
Default locale: en_US, platform encoding: Cp1252
OS name: "windows 7", version: "6.1", arch: "amd64", family: "windows"
```
## Set up Git
Follow [these steps](https://www.atlassian.com/git/tutorials/install-git#windows).

## Compile IB Java API
**Note**: The following steps are meant to be conducted in _**Git Bash**_ which has been installed as part of the Git set-up process you have just gone through in earlier steps. Open Git Bash from the Start menu under the Git section.

1. Clone the IB API source repository.
1. Go to ``interactive-brokers-api/9.73.06/IBJts/source/JavaClient/`` and execute ``mvn install``.

For your reference, below are the steps on my computer. You will watch out for something like "BUILD SUCCESS". When you see that, everything has gone well.

```
test@test-PC MINGW64 ~
$ cd Code/

test@test-PC MINGW64 ~/Code
$ git clone https://verbarmont@bitbucket.org/waratah/interactive-brokers-api.git
Cloning into 'interactive-brokers-api'...
remote: Counting objects: 547, done.
remote: Compressing objects: 100% (464/464), done.
remote: Total 547 (delta 68), reused 547 (delta 68)
Receiving objects: 100% (547/547), 1.50 MiB | 248.00 KiB/s, done.
Resolving deltas: 100% (68/68), done.

test@test-PC MINGW64 ~/Code
$ cd interactive-brokers-api/9.73.06/IBJts/source/JavaClient/

test@test-PC MINGW64 ~/Code/interactive-brokers-api/9.73.06/IBJts/source/JavaClient (master)
$ mvn install

[INFO] Scanning for projects...
[INFO]
[INFO] -------------------< com.interactivebrokers:tws-api >-------------------
[INFO] Building tws-api 9.73.06
... ...
... ...
... ...
[INFO] --- maven-jar-plugin:2.4:jar (default-jar) @ tws-api ---
[INFO] Building jar: C:\Users\test\Code\interactive-brokers-api\9.73.06\IBJts\source\JavaClient\build\tws-api-9.73.06.jar
[INFO]
[INFO] --- maven-install-plugin:2.4:install (default-install) @ tws-api ---
[INFO] Installing C:\Users\test\Code\interactive-brokers-api\9.73.06\IBJts\source\JavaClient\build\tws-api-9.73.06.jar to C:\Users\test\.m2\repository\com\interactivebrokers\tws-api\9.73.06\tws-api-9.73.06.jar
[INFO] Installing C:\Users\test\Code\interactive-brokers-api\9.73.06\IBJts\source\JavaClient\pom.xml to C:\Users\test\.m2\repository\com\interactivebrokers\tws-api\9.73.06\tws-api-9.73.06.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 30.179 s
[INFO] Finished at: 2018-03-13T17:36:19+11:00
[INFO] ------------------------------------------------------------------------
```

## What's next?
Up until now, if you have successfully compiled the IB Java API, then congratulations - you have set up your Java build environment correctly.

The next step is to fetch the code for the **IB Client Tool** and do some real development work. Of course, you may also want to install a proper IDE, such as IntelliJ IDEA, to make life easier. 

## Compile the IB Client Tool
1. Clone the source respository for the IB Client Tool.
1. Go to the ``interactive-brokers-client`` directory.
1. ``mvn package``

Again, if you see "BUILD SUCCESS", then you are good.

## Give it a spin
1. Open a command prompt window (not Git Bash).
1. Go to the source code directory and then ``target``, execute:

 ```
 java -classpath .\ib-client-1.0-SNAPSHOT-jar-with-dependencies.jar;..\lib\swt_win.jar org.zhuang.trading.IBClientMain
 ```

If you see the window, then everything is working fine.
	
## Set up an IDE
Download and install [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/download-thanks.html?platform=windows&code=IIC).