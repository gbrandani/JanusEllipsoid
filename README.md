JanusEllipsoid
==========

## Description

A Monte Carlo simulation and visualization tool for studying the adsorption of a Janus ellipsoidal colloid at a fluid interface.

In the visualization, the polar hydrophilic side of the colloid is in red and the apolar hyrophobic side is in yellow. The water is in blue and and oil in white.
The optimal colloid orientation and position relative to the interface tend to
maximize the area of water/oil interface covered, while localizing the polar side in water and the apolar side in oil.
An orientational phase transition is observed as a function of the colloid shape and surface tension parameters.

## Build and run

```sh
$ cd src
$ javac *.java
$ java InteractiveJanusEllipsoid
```

## Parameters

alpha = the angle (in degrees) defining the size of the yellow hydrophobic patch relative to the red hydrophilic region (0 fully hydrophilic, 180 fully hydrophobic, 90 symmetric Janus ellipsoid)  
Lz = the length (nm) of the colloid along the axis of polarity  
Lx=Ly = the lengths (nm) perpendicular to the axis of polarity  
dz = the typical displacement (nm) step along the z direction during Monte Carlo dynamics  
dphi = the typical angular step (degrees) during Monte Carlo dynamics  
gamma = the water/oil surface tension in kBT/nm^2  
cos(thetaY/R) = cosine of the contact angles of the yellow and red sides of the colloid (-1 very hydrophobic, 0 neutral, 1 very hydrophilic)  


## Other notes

for more on this topic, see:
Brandani, G. B., Schor, M., Morris, R., Stanley-Wall, N., MacPhee, C. E., Marenduzzo, D., & Zachariae, U. (2015). The bacterial hydrophobin BslA is a switchable ellipsoidal janus nanocolloid. Langmuir, 31(42), 11558-11563

