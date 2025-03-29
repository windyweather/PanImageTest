package net.windyweather.panimagetest;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
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
        spScrollPane.setPannable( true );
        spScrollPane.setHbarPolicy( AS_NEEDED );
        spScrollPane.setVbarPolicy( AS_NEEDED );

        /*
            The setFit makes no difference either way
         */
        /*
        spScrollPane.setFitToHeight( true);
        spScrollPane.setFitToWidth(true);
        */

        spScrollPane.setFitToHeight( false);
        spScrollPane.setFitToWidth(false);

        /*
            Now make image zoomable with a SetOnScroll event handler
            Major big deal!! This routine and the handler make ZOOM of
            an image work just great. I added the feature to stop zooming
            at 200% and 10%. when the getscale returns either of those,
            just consume the event.
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
                            event.consume();
                            return;
                        } else if (deltaY < 0.0 && dScale < 0.20) {
                            event.consume();
                            return;
                        }

                        if (deltaY < 0) {
                            zoomFactor = 0.95;
                        }
                        imgImageView.setScaleX(imgImageView.getScaleX() * zoomFactor);
                        imgImageView.setScaleY(imgImageView.getScaleY() * zoomFactor);

                        setStatus(String.format("ImageView scale factors [%.3f, %.3f]", imgImageView.getScaleX(), imgImageView.getScaleY()));

                    /*
                        see if we can set the scroll page to fit the new size of the image
                     */
                        double dScaleX = imgImageView.getScaleX();
                        double dScaleY = imgImageView.getScaleY();
                        double dWidth = imgImageView.getFitWidth() * dScaleX;
                        double dHeight = imgImageView.getFitHeight() * dScaleY;


                        //spScrollPane.setMaxWidth ( dWidth );
                        //spScrollPane.setMaxHeight( dHeight );
                        spScrollPane.setHmax(dWidth);
                        spScrollPane.setHmin(0.0);
                        spScrollPane.setVmax(dHeight);
                        spScrollPane.setVmin(0.0);
                        printSysOut(String.format("ScrollEvent - [%.0f, %.0f]", dWidth, dHeight));
                        event.consume();
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
        try {
            selectedImageFile = fileChooser.showOpenDialog(stage);
        }
        catch (Exception e)
        {
            /*
             We can ignore the exception and just continue because
             we initialized selectedImageFile with null
             */
            setStatus("Bogus starting file path for fileChooser");
            return;
        }
        if (selectedImageFile == null )
        {
            setStatus("No Image Chosen");
            return;
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

        Image anImage = new Image( imageAsStream );
        double dWidth = anImage.getWidth();
        double dHeight = anImage.getHeight();

        imgImageView.setFitWidth( dWidth );
        imgImageView.setFitHeight( dHeight );
        imgImageView.setImage( anImage );
        setStatus(String.format("image displayed [%.0f, %.0f]", dWidth, dHeight));
    }
}