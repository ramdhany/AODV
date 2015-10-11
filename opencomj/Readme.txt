Install
-------

OpenCOMJ v1.3.2 requires ANT version 1.6 and greater. This can be obtained from
http://ant.apache.org/

Yout the environment variable OpenCOM. OpenCOM should point
to the root location of OpenCOMJ (e.g.set OpenCOM= C:\OpenCOMJ).

To use OpenCOM with class files only - execute "ant compile" in the root
directory. However, these are not true 3rd party deployable components.
To use OpenCOM after this, simply set the classpath to env{OpenCOM}\classes e.g. C:\OpenCOMJ\classes

There are 5 demo programs to run:
ant demo
ant visualdemo
ant frameworkdemo
ant visualframeworkdemo
ant contractdemo
ant sequentialreceptacledemo
ant parallelreceptacledemo
ant contextreceptacledemo

To create JAR versions of components; execute ant jar

To dynamically register the components, without hardcoding the classpath with the
new jars. First set the JRE environment to the java runtime environment. 
(e.g. set JRE=c:\jre1.5.0_04;) Then execute "ant register" in the OpenCOM root. N.b. The
ant commands to execute demos will not work, so use the java commands e.g.
java CalculatorTest.TestProgram.



