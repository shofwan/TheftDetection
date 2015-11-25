/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package theftdetection2;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.opencv.core.Core;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

public class TheftDetection extends Application{
    /**
     * The main class for a JavaFX application. It creates and handles the main
     * window with its resources (style, graphics, etc.).
     * 
     * 
     */
    @Override
    public void start(Stage primaryStage){
        try{
            // load the FXML resource
            BorderPane root = (BorderPane) FXMLLoader.load(getClass().getResource("FXML.fxml"));
            // set a whitesmoke background
            root.setStyle("-fx-background-color: whitesmoke;");
            // create and style a scene
            Scene scene = new Scene(root, 1200, 650);
            // create the stage with the given title and the previously created scene
            primaryStage.setTitle("Theft Detection");
            primaryStage.setScene(scene);
            // show the GUI
            primaryStage.show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}
