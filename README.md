# PanImageTest
This is a JavaFx test program to explore displaying images with Pan and Zoom in
a ScrollPanel. 

It doesn't work as yet. The panning behaviour does not allow panning
to the edges / corners of the image when the image is zoomed more than 1x1. 

Zooming can be accomplished by either using the mouse wheel or by using a 
slider on the interface.

When the image is smaller than the viewport in either direction the
image is centered viewport and panning is not disabled.

An adjustment value spinner is provided to set an adjustment value for the
Hmin, Hmax and Vmin, Vmax values of the scrollpane in an effort to allow
panning to the edges of the images, but this does not work.

Test images are provided which easily show the panning / zooming progress and
highlight the edges of the image.

A work in progress.