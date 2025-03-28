module net.windyweather.panimagetest {
    requires javafx.controls;
    requires javafx.fxml;


    opens net.windyweather.panimagetest to javafx.fxml;
    exports net.windyweather.panimagetest;
}