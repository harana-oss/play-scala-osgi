# Play-Scala-OSGI

### Getting Started

Add this library to both your server and plugins:

```
com.harana %% play-scala-osgi % 1.0
```

#### Server

Make sure you have bundles and plugins directories created.

And that within bundles you have the following files:
```
org.apache.felix.bundlerepository-2.0.10.jar
org.apache.felix.configadmin-1.8.16.jar
org.apache.felix.fileinstall-3.6.4.jar
```
Which you can download from [here](https://felix.apache.org/downloads.cgi).

#### Plugin
    
To create a bundle:

```
sbt osgiBundle
```

Configure as per instructions on:

```
https://github.com/doolse/sbt-osgi-felix
```

