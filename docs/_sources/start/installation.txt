.. _start.installation:

Installation
============

This section describes the steps needed to install GeoGit.

Binaries
--------

Pre-built binaries are available for GeoGit.

#. If not already on your system, install a `Java JDK <http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html>`_. GeoGit requires Java 7 or higher to run. Make sure the `java` executable is accessible (i.e. check your PATH environment variable) and the JAVA_HOME environment variable points to the JDK or JRE installation directory.

#. After the JDK is installed, navigate to http://geogit.org and click :guilabel:`Download`.

#. Extract this archive to your preferred program directory. (For example, :file:`C:\\Program Files\\GeoGit` or :file:`/opt/geogit`.) 

   .. note:: The same packages can be used on Windows, OS X, and Linux.

#. Add the bin directory to your ``PATH`` environment variable.

When finished, you should be able to run the ``geogit --help`` and see the command usage.

Building from source code
-------------------------

To build GeoGit and have an executable binary that you can run:

#. Clone the GeoGit source code repository. To do so, create a new folder where you want the GeoGit source code to be kept, open a terminal and move to that folder. Now type the following::

	   git clone http://github.com/boundlessgeo/GeoGit.git

#. If not already on your system, install a `Java JDK <http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html>`_. A Java JRE is not sufficient to build GeoGit.

#. If not already on your system, install `Maven <http://maven.apache.org/download.cgi>`_.

#. Move to the ``src/parent`` folder under the folder where you have cloned the GeoGit source code, and type the following::

	   mvn clean install

   .. note:: To speed up the build process, you can skip tests:

             ::

               mvn clean install -DskipTests

#. GeoGit will now build. Scripts ready to be run should be available in the :file:`src/cli-app/target/geogit/bin` directory. Add that directory to your ``PATH`` environment variable.

When finished, you should be able to run the ``geogit --help`` from a terminal and see the command usage.
