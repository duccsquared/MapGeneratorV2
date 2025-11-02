package com;



import com.controller.MainController;
// import com.data.SphericalVoronoi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main3D extends Application {


    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/view/main.fxml"));
        // scene
        Scene scene = new Scene(loader.load(), 600, 400);
        MainController controller = loader.getController();
        controller.init(scene);
        stage.setScene(scene);
        stage.setTitle("JavaFX App");
        stage.show();
    }


}
