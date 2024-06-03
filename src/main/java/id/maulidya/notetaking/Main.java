package id.maulidya.notetaking;

import id.maulidya.notetaking.Firebase.Firebase;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

    public class Main extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            Firebase.FirebaseUtil.initializeFirebase();
            Parent root = FXMLLoader.load(getClass().getResource("/id.maulidya.notetaking/register.fxml"));
            primaryStage.setTitle("Aplikasi Note Taking");
            primaryStage.setScene(new Scene(root, 800, 600));
            primaryStage.show();
        }

        public static void main(String[] args) {
            launch(args);
        }
    }
