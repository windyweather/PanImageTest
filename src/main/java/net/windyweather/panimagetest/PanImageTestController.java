package net.windyweather.panimagetest;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
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
  */
    private void AdjustScrollPane() {
        var x = spScrollPane.getHvalue();
        var y = spScrollPane.getVvalue();


        double imgW = imgImageView.getFitWidth();
        double imgH = imgImageView.getFitHeight();
        double spVW = spScrollPane.getViewportBounds().getWidth();
        double spVH = spScrollPane.getViewportBounds().getHeight();

        if (imgW < spVW) {
            spScrollPane.setHmax( 1.0);
            spScrollPane.setHvalue(0.0);
            spScrollPane.setHmin( 0.0);
            x = 0.0;
            y = 0.0;
            spScrollPane.setHvalue(x);
            spScrollPane.setVvalue(y);
            printSysOut("AdjustScrollPane - center image");
        } else {
            spScrollPane.setHmax( imgW * 20.0);
            // spScrollPane.setHvalue(1.0);
            spScrollPane.setHmin( - imgW * 10.0);
            printSysOut(String.format("AdjustScrollPane - imgW %.0f spVW %.0f", imgW, spVW) );
        }
        /*
        spScrollPane.setVvalue( );
        spScrollPane.setHvalue( )
        */

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
            *************** Remove the Scroll Handler which was doing a zoom, **************
            *************** in case it messes with the zoom via slider        **************
         */
        /*
            Let's try a scroll event on the page now and see what we find.
            Let's see if this is called when we pan.
            ****** THIS IS NEVER CALLED **** no clue why
         */

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
    }


    @FXML
    protected void onHelloButtonClick() {

        setStatus("I said do not click that!");


    }

    public void onOpenImageClick(ActionEvent actionEvent) throws FileNotFoundException {
        FileChooser fileChooser = new FileChooser();
        /*
            get stage to use as parent for dialog
            and set a default path for the file chooser
         */
        File defFile = new File("D:\\MMO_Pictures\\AlienBlackout\\2025_03");
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

        setStatus(String.format("image displayed [%.0f, %.0f]", dWidth, dHeight));
    }

    /*
        Let's hook some events in the Scene Builder and see if they are called on Pan
        ** THESE ARE NEVER CALLED **
     */
    public void SPOnMouseDragged(MouseEvent mouseEvent) {



        printSysOut(String.format(" SpOnMouseDragged spScrollPane H V Values [%.2f, %.2f]",
                spScrollPane.getHvalue(), spScrollPane.getVvalue()));
        printSysOut(String.format("SpOnMouseDragged ScrollEvent imgImageView X,Y Values [%.2f, %.2f]",
                imgImageView.getX(), imgImageView.getY()));

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
        //imgImageView.setFitWidth( anImage.getWidth() * dScaleFactor );
        printSysOut(String.format("ImgOnMouseClicked imgImageView X,Y Values [%.2f, %.2f] zoom %.4f",
                imgImageView.getX(), imgImageView.getY(), dScaleFactor));
        Bounds bnds = imgImageView.getBoundsInLocal();
        printSysOut(String.format("ImgOnMouseClicked ImageView bounds [%.0f, %.0f]",
                bnds.getWidth(), bnds.getHeight() ));
    }

    /*
        This is called on dragging image
     */
    public void ImgOnMouseDragged(MouseEvent mouseEvent) {

        /*
            Do we need this here?
            Nope. This screws up panning
         */
            //imgImageView.setFitWidth( anImage.getWidth() );

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
            printSysOut(String.format("ImgOnMouseDragged ImageView view XY [%.0f, %.0f] fitWH [%.0f, %.0f]",
                    imgImageView.getX(), imgImageView.getY(), imgImageView.getFitWidth(), imgImageView.getFitHeight()));
            printSysOut(String.format("ImgOnMouseDragged spScrollPane H V [%.2f, %.2f] X,Y [%.2f, %.2f]",
                    spScrollPane.getHvalue(), spScrollPane.getVvalue(),imgImageView.getX(), imgImageView.getY() ));

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
