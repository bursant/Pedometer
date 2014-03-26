Pedometer
=========

Android Pedometer Pedorepo.

IDE Configuration
=================

 * Grab this project using `git clone`
 * Go into the `./Pedometer/` catalog
 * Run commands: `mvn install` and `mvn idea:idea` (or `mvn eclipse:eclipse`)
 * Open IntelliJ IDEA and select `./Pedometer/pedometer-main.ipr`
 * Wait until project is open.
 * If you see notification about Android framework detect, press *Configure* and *OK*.
 * Select *Pedometer [pedometer-main]* root tree catalog in *Project* view (`[alt]+[1]`) and press `[F4]`
 * In *Project Settings → Project* select project level language to `6.0`
 * In *Project Settings → Modules* for module `pedometer-droid` check if `src/` is select as source catalog. If not set it as source catalog. Check if module has Android facet added. If not, add it.