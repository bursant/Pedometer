Pedometer
=========

Android Pedometer Pedorepo.

IDE Configuration
=================

Known problems
-------------

 * Maven does not select `./pedometer-droid/src/` as source catalog (-fixed-)
 * Maven does not set properly *Language level*. Check if set is `6.0`. (-fixed-).

Howto
-----

 * Grab this project using `git clone`
 * Go into the `./Pedometer/pedometer-common` catalog and run `mvn install`.
 * Open or import project `./Pedometer/` using `./Pedometer/pom.xml` as project file.