package net.windyweather.panimagetest;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED;
import static javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER;

public class PanImageTestController {
    public ScrollPane spScrollPane;
    public ImageView imgImageView;
    public Label lblStatus;
    public Button btnHello;
    public Button btnOpenImage;
    public AnchorPane apAnchorPane;

    public Image anImage;

    static double dScaleFactor;

    static {
        dScaleFactor = 1.0;
    }

    public ScrollBar sbSlideToZoom;
    public Spinner<Double> spinScrollPaneAdjust;

    /*
        Center a child in the parent, if the parent contains the child
     */
    private void centerNode(Node parentNode, Node childNode ) {
        Bounds parentBounds = parentNode.getBoundsInLocal();
        Bounds childBounds = childNode.getLayoutBounds();
        if ( parentBounds.contains( childBounds) ) {
            printSysOut( String.format("centerNode - node centered [%.0f, %.0f]",
                    childNode.getTranslateX(), childNode.getTranslateY() ) );
            childNode.setTranslateX( (parentBounds.getWidth() / 2.0) - (childBounds.getWidth() / 2.0 ) );
            childNode.setTranslateY( (parentBounds.getHeight() / 2.0) - (childBounds.getHeight() / 2.0 ) );
        }
    }

    /*
     On zoom, adjust scroll bounds to center image if small
     or adjust bounds to allow edges if large
     Here is where we apply those insane values from the ScrollPanelAdjustmentSpinner
  */
    private void AdjustScrollPane() {
        var x = spScrollPane.getHvalue();
        var y = spScrollPane.getVvalue();

        /*
            Very confusing. Horizontal vs Width  Vertical vs Height
         */
        double imgH = imgImageView.getFitWidth()*imgImageView.getScaleX(); // horizontal
        double imgV = imgImageView.getFitHeight()*imgImageView.getScaleY(); // vertical
        double spH = spScrollPane.getViewportBounds().getWidth();
        double spV = spScrollPane.getViewportBounds().getHeight();

        double dHfraction = (spScrollPane.getHvalue() - spScrollPane.getHmin() ) / (spScrollPane.getHmax() - spScrollPane.getHmin());
        double dVfraction = (spScrollPane.getVvalue() - spScrollPane.getVmin() ) / (spScrollPane.getVmax() - spScrollPane.getVmin());

        double dSpinAdjust = spinScrollPaneAdjust.getValue();
        printSysOut(String.format("AdjustScrollPane - adjust value %.0f", dSpinAdjust));
        printSysOut(String.format("AdjustScrollPane - ImageView width %.0f ScrollPane width %.0f",
                imgH, spH) );
        /*
            Horizontal stuff
         */
        if ( imgH < spH ) {
            spScrollPane.setHmax( 1.0);
            spScrollPane.setHvalue(0.5);
            spScrollPane.setHmin( 0.0);

            printSysOut("AdjustScrollPane - center image horizontally");
        } else {
            /*
                Put the Hvalue back at same fraction of new range
             */
            double dHhigh = imgH * dSpinAdjust;
            double dHlow = -imgH;
            double dHpos = (dHhigh - dHlow) * dHfraction;
            spScrollPane.setHmax( dHhigh );
            spScrollPane.setHmin( dHlow );
            spScrollPane.setHvalue( dHpos );
            printSysOut(String.format("AdjustScrollPane - ImageView width %.0f ScrollPane Hrange %.0f Hpos %.0f",
                    imgH, dHhigh-dHlow, dHpos ));
        }
        /*
            Vertical Stuff
         */
        if ( imgV < spV ) {
            spScrollPane.setVmax( 1.0);
            spScrollPane.setVvalue(0.5);
            spScrollPane.setVmin( 0.0);

            printSysOut("AdjustScrollPane - center image vertically");
        } else {

            /*
                Put the Vvalue back at same fraction of new range
             */
            double dVhigh = imgV * dSpinAdjust;
            double dVlow = -imgV;
            double dVpos = (dVhigh - dVlow) * dVfraction;
            spScrollPane.setVmax( dVhigh );
            spScrollPane.setVmin( dVlow );
            spScrollPane.setVvalue( dVpos );
            printSysOut(String.format("AdjustScrollPane - ImageView height %.0f ScrollPane Vrange %.0f VPos %.0f",
                    imgV, dVhigh-dVlow, dVpos ));
        }

    }

    @FXML

    protected void setStatus( String sts ) {
        lblStatus.setText(sts);
    }

    public static void printSysOut( String str ) {
        System.out.println(str);
    }
    /*
        Set up stuff we can't do until the window is up
        Called from the app class when the stage is set.
     */
    public void setUpStuff() {

        /*
            Make things pannable with the mouse
         */
        spScrollPane.setPannable(true);
        spScrollPane.setHbarPolicy(AS_NEEDED);
        spScrollPane.setVbarPolicy(AS_NEEDED);

        /*
            Now set up the spinner for our ScrollPaneAdjustment value
            If these numbers look insane, just watch the program run. LOL
         */

        SpinnerValueFactory<Double> spinFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                1000.0, 1000000000000000000.0, 1000.0, 100000.0 );

        spinScrollPaneAdjust.setValueFactory( spinFactory );
        spinScrollPaneAdjust.editorProperty().get().setAlignment(Pos.CENTER_RIGHT);

        /*
            The setFit makes no difference either way
         */
        if (true) {
            spScrollPane.setFitToHeight( true);
            spScrollPane.setFitToWidth(true);
        }

        /*
            Personally I hate these embedded listener methods
            They make things really hard to read and with IntelliJ inserting
            stuff for you frequently as you edit, sometimes you can get lost
            and have to mess around endlessly to get it put back together. Sigh.
         */
        sbSlideToZoom.valueProperty().addListener((o, oldV, newV) -> {
            var x = spScrollPane.getHvalue();
            var y = spScrollPane.getVvalue();
            double dScale = newV.doubleValue();
            double newW = anImage.getWidth() * dScale;

            imgImageView.setScaleX( dScale );
            imgImageView.setScaleY( dScale );
            spScrollPane.setHvalue(x);
            spScrollPane.setVvalue(y);
            /*
                Makes no different whether this is called or not
                Panning still does not work
             */
            //imgImageView.setFitWidth( newW );

            /*
                Adjust the scroll pane scroll bounds based on image size
             */
            AdjustScrollPane();


            setStatus( String.format("Zoom Scale %.4f", dScale));
            printSysOut(String.format("sbSlideToZoom value %.3f  x,y [%.0f,%.0f] zoom %.4f", newV.doubleValue(), x, y, dScale ));
        });

        /*
            *************** Restore the Scroll Handler which does a zoom,        **************
            *************** Removing this and using Slider Did not fix problem   **************
         */

       /*
            Now make image zoomable with a SetOnScroll event handler
            Major big deal!! This routine and the handler make ZOOM of
            an image work just great. I added the feature to stop zooming
            at 200% and 10%. when the getscale returns either of those,
            just consume the event.
            *** Let's try to zoom on the scroll pane rather than the imageview ***
            *** Well that doesn't work. It scrolls rather than zooming ***
         */

        imgImageView.setOnScroll(
                new EventHandler<ScrollEvent>() {
                    @Override
                    public void handle(ScrollEvent event) {
                        double zoomFactor = 1.05;
                        double deltaY = event.getDeltaY();
                    /*
                        Don't zoom forever. Just ignore it after
                        a while.
                     */
                        double dScale = imgImageView.getScaleX();
                        if (deltaY > 0.0 && dScale > 10.0) {
                            setStatus("Don't scale too big");
                            event.consume();
                            return;
                        } else if (deltaY < 0.0 && dScale < 0.20) {
                            setStatus("Don't scale too small");
                            event.consume();
                            return;
                        }

                        if (deltaY < 0) {
                            zoomFactor = 0.95;
                        }
                        /*
                            Save the scale factor for later to set the fitwidth
                         */
                        dScaleFactor = imgImageView.getScaleX() * zoomFactor;
                        double dOriginalWidth = imgImageView.getFitWidth();
                        /*
                            Save these values to put back the postion after the zoom
                         */
                        double dSpVvalue = spScrollPane.getVvalue();
                        double dSpHValue = spScrollPane.getHvalue();

                        imgImageView.setScaleX( dScaleFactor);
                        imgImageView.setScaleY( dScaleFactor);
                        String scaleReport = String.format("SetOnScroll - ImageView scale factors [%.3f, %.3f]", imgImageView.getScaleX(), imgImageView.getScaleY());

                        setStatus(scaleReport);
                        printSysOut(scaleReport);

                        /*
                            see if we can set the scroll page to fit the new size of the image
                         */
                        double dScaleX = imgImageView.getScaleX();
                        double dScaleY = imgImageView.getScaleY();
                        double dWidth = imgImageView.getFitWidth();
                        double dHeight = imgImageView.getFitHeight();
                        /*
                            Center the image in the ScrollPane if the image is
                            smaller than the ScrollPane
                         */
                        centerNode( spScrollPane, imgImageView );

                        spScrollPane.setHmax(dWidth * 5.0);
                        spScrollPane.setHmin(0.0);
                        spScrollPane.setVmax(dHeight * 5.0);
                        spScrollPane.setVmin(0.0);

                        /*
                            The below has absolutely no effect on scrolling behaviour and
                            the spScrollPane H and V values stay at 0.5
                         */
                        if (false) {
                        /*
                            What are teh scroll pane Hvalue and Vvalue anyway?
                            Let's set them and see if it partially works
                            ScrollBar range is always from 0.0 -> 1.0? Who Knew?
                         */
                            spScrollPane.setHvalue(0.5);
                            spScrollPane.setVvalue(0.5);
                        }
                        /*
                            The magic that fixes the pan problem?
                         */
                        double dScaledWidth = anImage.getWidth() * dScaleFactor;
                        printSysOut(String.format("IMG ScrollEvent imgSetFitWidth width %.0f scale factor %.4f newWidth %.4f",
                                anImage.getWidth(), dScaleFactor, dScaledWidth) );
                        /*
                            ***************** this is apparently what made ImagePanZoom test work  ****************
                            * ********** but it's not working here *************
                         */
                        imgImageView.setFitWidth( anImage.getWidth() ); // we already did this above* dScaleFactor );
                        imgImageView.setFitHeight( anImage.getHeight() ); // we already did this above * dScaleFactor );
                        spScrollPane.setFitToHeight( true );
                        spScrollPane.setFitToWidth( true );


                        printSysOut(String.format("IMG ScrollEvent imgImageView Scaled w,h [%.0f, %.0f]", dWidth, dHeight));
                        printSysOut(String.format("IMG ScrollEvent spScrollPane H V Values [%.2f, %.2f]",
                                spScrollPane.getHvalue(), spScrollPane.getVvalue()));
                        printSysOut(String.format("IMG ScrollEvent imgImageView X,Y Values [%.2f, %.2f]",
                                imgImageView.getX(), imgImageView.getY()));
                        Bounds bnds = imgImageView.getBoundsInLocal();
                        printSysOut(String.format("IMG ScrollEvent ImageView bounds [%.0f, %.0f]",
                                bnds.getWidth(), bnds.getHeight() ));

                        /*
                            Let's change scrollpane Hmin/max and Vmin/max
                            Whateven are they supposed to be. What's the model?
                            if image inside of scrollpane is 0-n, then to scroll to see the whole
                            image, the range needs to be larger than 0-n or exactly 0-n
                         */
                        double dImgViewWidth = imgImageView.getFitWidth() * 10.0;
                        double dImgViewHeight = imgImageView.getFitHeight() * 10.0;
                        spScrollPane.setHmin( 0.0 ); //- dImgViewWidth);
                        spScrollPane.setHmax( dImgViewWidth );
                        spScrollPane.setVmin( 0.0 ); //-dImgViewHeight);
                        spScrollPane.setVmax( dImgViewHeight );

                        printSysOut(String.format("IMG ScrollEvent ImageView w,h [%.0f, %.0f]", dImgViewWidth, dImgViewHeight));


                        event.consume();
                    }
                }
        );

        /*
            Let's try a scroll event on the page now and see what we find.
            Let's see if this is called when we pan.
            ****** THIS IS NEVER CALLED **** no clue why
         */
/*
        spScrollPane.setOnScroll(
                new EventHandler<ScrollEvent>() {
                    @Override
                    public void handle(ScrollEvent scrollEvent) {
                        //printSysOut(String.format("IMG ScrollEvent imgImageView size- [%.0f, %.0f]", dWidth, dHeight));
                        printSysOut(String.format("SP ScrollEvent spScrollPane H V Values [%.2f, %.2f]",
                                spScrollPane.getHvalue(), spScrollPane.getVvalue()));
                        printSysOut(String.format("SP ScrollEvent imgImageView X,Y Values [%.2f, %.2f]",
                                imgImageView.getX(), imgImageView.getY()));
                    }
                }
        );
        */
    }


    @FXML
    protected void onHelloButtonClick() {

        setStatus("I said do not click that!");
        /*
            Inspect GUI parts to report their sizes
         */
        printSysOut(String.format("OnHello Image wh [%.0f,%.0f] Imageview wh [%.0f,%.0f] ScrollPane wh [%.0f,%.0f]",
                anImage.getWidth(), anImage.getHeight(),
                imgImageView.getViewport().getWidth(), imgImageView.getViewport().getHeight(),
                spScrollPane.getWidth(), spScrollPane.getHeight()
        ));
    }

    public void onOpenImageClick(ActionEvent actionEvent) throws FileNotFoundException {
        FileChooser fileChooser = new FileChooser();
        /*
            get stage to use as parent for dialog
            and set a default path for the file chooser
         */
        //File defFile = new File("D:\\MMO_Pictures\\AlienBlackout\\2025_03");
        String sDirExamples = System.getProperty("user.dir") + "\\progress";

        File defFile = new File(sDirExamples);
        fileChooser.setInitialDirectory(defFile);
        Stage stage = (Stage) btnOpenImage.getScene().getWindow();
        File selectedImageFile = null;
        /*
            Keep trying until cancel or we get a path
         */
        while (true) {
            try {
                selectedImageFile = fileChooser.showOpenDialog(stage);
            } catch (Exception e) {
                /*
                 We can ignore the exception and just continue because
                 we initialized selectedImageFile with null
                 */
                setStatus("Bogus starting file path for fileChooser");
                /*
                    Go back around with a default path and let the
                    user find an image.
                 */
                File aFile = new File("C:\\");
                fileChooser.setInitialDirectory(aFile);

                continue;
            }
            if (selectedImageFile == null) {
                setStatus("No Image Chosen");
                return;
            } else {
                break;
            }
        }
        /*
            reset scale factors in case we were messing with them
         */
        imgImageView.setScaleX( 1.0 );
        imgImageView.setScaleY( 1.0 );
        /*
            we appear to have a file so we will try to load the image with it.
            Later we need a try catch around this stuff since some of it can toss
            exceptions.
         */
        InputStream imageAsStream = new FileInputStream(selectedImageFile);

        anImage  = new Image( imageAsStream );
        double dWidth = anImage.getWidth();
        double dHeight = anImage.getHeight();

        imgImageView.setFitWidth( dWidth );
        imgImageView.setFitHeight( dHeight );
        imgImageView.setImage( anImage );

        /*
            Did we forget to do this?
            NO! IT MAKES NO F****** DIFFERENCE
         */
        Rectangle2D viewportRect = new Rectangle2D( 0, 0, dWidth, dHeight);
        imgImageView.setViewport(viewportRect);

        printSysOut(String.format("image displayed [%.0f, %.0f]", dWidth, dHeight));


        AdjustScrollPane();

        setStatus(String.format("image displayed [%.0f, %.0f]", dWidth, dHeight));
    }

    /*
        Let's hook some events in the Scene Builder and see if they are called on Pan
        ** THESE ARE NEVER CALLED **
     */
    public void SPOnMouseDragged(MouseEvent mouseEvent) {

        if ( false ) {

            printSysOut(String.format(" SpOnMouseDragged spScrollPane H V Values [%.2f, %.2f]",
                    spScrollPane.getHvalue(), spScrollPane.getVvalue()));
            printSysOut(String.format("SpOnMouseDragged ScrollEvent imgImageView X,Y Values [%.2f, %.2f]",
                    imgImageView.getX(), imgImageView.getY()));
        }

    }
    /*
        Hurray. The following two are called on a drag.
        Clicked before and after the drag.
        And drag over and over during the drag.
     */

    public void ImgOnMouseClicked(MouseEvent mouseEvent) {
        /*
            Let's try something. Do the setFitWidth here.
            Drags start with a click so we make sure the FitWidth is set
            before we start dragging and use the last Scalefactor that
            we saw on the last zoom
         */
        if (false) {
            //imgImageView.setFitWidth( anImage.getWidth() * dScaleFactor );
            printSysOut(String.format("ImgOnMouseClicked imgImageView X,Y Values [%.2f, %.2f] zoom %.4f",
                    imgImageView.getX(), imgImageView.getY(), dScaleFactor));
            Bounds bnds = imgImageView.getBoundsInLocal();
            printSysOut(String.format("ImgOnMouseClicked ImageView bounds [%.0f, %.0f]",
                    bnds.getWidth(), bnds.getHeight()));
        }
    }

    /*
        This is called on dragging image
     */
    public void ImgOnMouseDragged(MouseEvent mouseEvent) {

        /*
            Do we need this here?
            Nope. makes no difference
         */
        imgImageView.setFitWidth( anImage.getWidth() );

        /*
            Adjust the scroll pane stuff with insane values
            NOT needed
         */
        //AdjustScrollPane();

        /*
            Dump stuff out to watch it
         */
        double dSpH = spScrollPane.getHeight();
        double dSpW = spScrollPane.getWidth();
        double dSpHvalue = spScrollPane.getHvalue();
        double dSpVvalue = spScrollPane.getVvalue();
        double dSpHmin = spScrollPane.getHmin();
        double dSpHmax = spScrollPane.getHmax();

        printSysOut(String.format("ImgOnMouseDragged ScrollPane w,h [%.0f, %.0f] hmin,max [%.0f, %.0f] h,v val [%.0f, %.0f]",
                dSpH, dSpW, dSpHmin, dSpHmax, dSpHvalue, dSpVvalue
        ));
        /*
            Can we brute force the fix?
         */
        if ( dSpHvalue == dSpHmax ) {
            spScrollPane.setHmax( dSpHmax + 1000.0);
            printSysOut(String.format("ImgOnMouseDragged Hmax adjusted to %.0f", dSpHmax+1000) );
        }
        if ( dSpHvalue == dSpHmin ) {
            spScrollPane.setHmin( dSpHmin - 1000.0 );
            printSysOut(String.format("ImgOnMouseDragged Hmin adjusted to %.0f", dSpHmin-1000) );
        }

        if ( false ) {
            printSysOut(String.format("ImgOnMouseDragged ImageView view XY [%.0f, %.0f] fitWH [%.0f, %.0f]",
                    imgImageView.getX(), imgImageView.getY(), imgImageView.getFitWidth(), imgImageView.getFitHeight()));
            printSysOut(String.format("ImgOnMouseDragged spScrollPane H V [%.2f, %.2f] imgView X,Y [%.2f, %.2f]",
                    spScrollPane.getHvalue(), spScrollPane.getVvalue(), imgImageView.getX(), imgImageView.getY()));

        /*
            Move Hmin,Hmax to avoid bumping image
         */
            printSysOut(String.format("ImgOnMouseDragged spScrollPane Hmin Hmax [%.2f, %.2f] X,Y [%.2f, %.2f]",
                    spScrollPane.getHmin(), spScrollPane.getHmax(), imgImageView.getX(), imgImageView.getY()));
        }
        // don't do this, it breaks it
        //mouseEvent.consume();
        }

    public void SPOnScroll(ScrollEvent scrollEvent) {
        printSysOut(String.format("SPOnScroll spScrollPane H V Values [%.2f, %.2f]",
                spScrollPane.getHvalue(), spScrollPane.getVvalue()));
        printSysOut(String.format("SPOnScroll imgImageView X,Y Values [%.2f, %.2f]",
                imgImageView.getX(), imgImageView.getY()));

        printSysOut(String.format("ImgOnMouseDragged imgImageView X,Y Values [%.2f, %.2f]",
            imgImageView.getX(), imgImageView.getY()));
    }

    public void SpOnZoom(ZoomEvent zoomEvent) {
        printSysOut("SpOnZoom");
    }
    public void SpOnZoomStarted(ZoomEvent zoomEvent) {
        printSysOut("SpOnZoomStarted");
    }
    public void SpOnZoomFinished(ZoomEvent zoomEvent) {
        printSysOut("SpOnZoomFinished");
    }


    public void SbOnScrollStarted(ScrollEvent scrollEvent) {
        printSysOut("SbOnScrollStarted");
    }

    public void SbOnScrollFinished(ScrollEvent scrollEvent) {
        printSysOut("SbOnScrollFinished");
    }

    public void SbOnScroll(ScrollEvent scrollEvent) {
        printSysOut("SbOnScroll");
    }

    public void SbOnMouseDragged(MouseEvent mouseEvent) {
        printSysOut(String.format("SbOnMouseDragged value %.4f", sbSlideToZoom.getValue()));
    }

    public void SbMouseClicked(MouseEvent mouseEvent) {
        printSysOut(String.format("SbMouseClicked value %.4f", sbSlideToZoom.getValue()));
    }


}
