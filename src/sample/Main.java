package sample;

import org.opencv.core.Core;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class Main extends Application
{
    @Override
    public void start(Stage primaryStage) throws IOException {
        // Получим fxml сцену
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DetectingFace.fxml"));
        // Сохраним fxml сцену, чтобы ее можно было использовать
        BorderPane rootElement = (BorderPane) loader.load();
        // Создаем сцену
        Scene scene = new Scene(rootElement, 700, 550);
        //Создаем сцену
        primaryStage.setScene(scene);
        //Задаем назвние
        primaryStage.setTitle("lab3");
        // Показываем GUI
        primaryStage.show();

        // Устанавливаем правильное поведение при закрытии приложения
        Controller controller = loader.getController();
        primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we)
            {
                try {
                    controller.setClosed();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    public static void main(String[] args)
    {
        // загружаем нативную библиотеку OpenCV
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        Application.launch(args);
    }
}