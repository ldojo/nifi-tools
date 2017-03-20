# nifi-tools
NiFi tools contains custom nifi processors, scripts and other.

[Apache NiFi](https://github.com/apache/nifi) is an easy to use, powerful, and reliable system to process and distribute data.

## Requirements
* JDK 1.8 or newer
* Apache Maven 3.1.0 or newer
* Git Client (used during build process by 'bower' plugin)

## Build
Execute `mvn clean install`

## Deploy 
Get `.nar` file in /target directory.
>        laptop:nifi myuser$ cd nifi-unix-nar
>        laptop:nifi-unix-nar myuser$ ls -l target/
>        drwxr-xr-x 1 myuser mygroup       0 Mar 20 20:13 classes/
>        drwxr-xr-x 1 myuser mygroup       0 Mar 20 20:13 maven-archiver/
>        drwxr-xr-x 1 myuser mygroup       0 Mar 20 20:13 maven-shared-archive-resources/
>        drwxr-xr-x 1 myuser mygroup       0 Mar 20 20:13 maven-status/
>        -rw-r--r-- 1 myuser mygroup 5579215 Mar 20 20:13 nifi-unix-nar-0.0.1-SNAPSHOT.nar
>        drwxr-xr-x 1 myuser mygroup       0 Mar 20 20:13 test-classes/
Move `.nar` file `$NIFI_HOME/lib/*`

Apache License 2.0

## Contact 
Melvin Mendoza - mcamendoza1@gmail.com


