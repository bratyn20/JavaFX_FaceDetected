package sample;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import sample.utils.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Controller
{
    @FXML
    private Button button;
    @FXML
    private ImageView currentFrame;

    // таймер для получения видеопотока
    private ScheduledExecutorService timer;
    // объект OpenCV, который реализует захват видео
    private VideoCapture capture = new VideoCapture();
    // флаг для изменения поведения кнопки
    private boolean cameraActive = false;
    // идентификатор камеры, которая будет использоваться
    private static int cameraId = 0;


    @FXML
    protected void startCamera(ActionEvent event) throws InterruptedException {
        if (!this.cameraActive)
        {
            // начать захват видео
            this.capture.open(cameraId);

            // видео поток доступен?
            if (this.capture.isOpened())
            {
                this.cameraActive = true;
                // захватывать кадр каждые 33 мс (30 кадров / с)
                Runnable frameGrabber = new Runnable() {

                    @Override
                    public void run()
                    {
                        // захватывать и обрабатывать один кадр
                        Mat frame = grabFrame();
                        //обнаружить и захватить лица
                        Mat frameDetectionFace = detectionFace(frame);
                        // конвертировать и показать кадр
                        Image imageToShow = Utils.mat2Image(frameDetectionFace);
                        updateImageView(currentFrame, imageToShow);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // обновить содержимое кнопки
                this.button.setText("Stop Camera");
            }
        }

        else
        {
            // камера не активна в этот момент
            this.cameraActive = false;
            // обновить содержимое кнопки
            this.button.setText("Start Camera");

            // остановить таймер
            this.stopAcquisition();
        }
    }

    private Mat detectionFace(Mat frame)
    {
        // Используем CascadeClassifier
        String xmlFile = "C:/opencv/sources/data/lbpcascades/lbpcascade_frontalface.xml";
        String xmlFile2 = "C:/opencv/sources/data/lbpcascades/haarcascade_eye_tree_eyeglasses.xml";
        CascadeClassifier classifier = new CascadeClassifier(xmlFile);
        CascadeClassifier eyesCascade = new CascadeClassifier(xmlFile2);


        // Обнаружение лица
        MatOfRect faceDetections = new MatOfRect();
        classifier.detectMultiScale(frame, faceDetections);

        // Обведение в квадрат
        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(
                    frame,                                               // где нарисовать рамку
                    new Point(rect.x, rect.y),                            // нижняя левая
                    new Point(rect.x + rect.width, rect.y + rect.height), // верхняя правая
                    new Scalar(0, 0, 255),
                    3                                                    //цвет рамки
            );
        }

        List<Rect> listOfFaces = faceDetections.toList();
        for (Rect face : listOfFaces) {
           /* Point center = new Point(face.x + face.width / 2, face.y + face.height / 2);
            Imgproc.ellipse(frame, center, new Size(face.width / 2, face.height / 2), 0, 0, 360,
                    new Scalar(255, 0, 255));*/
            Mat faceROI = frame.submat(face);
            // -- In each face, detect eyes
            MatOfRect eyes = new MatOfRect();
            eyesCascade.detectMultiScale(faceROI, eyes);
            List<Rect> listOfEyes = eyes.toList();
            for (Rect eye : listOfEyes) {
                Point eyeCenter = new Point(face.x + eye.x + eye.width / 2, face.y + eye.y + eye.height / 2);
                int radius = (int) Math.round((eye.width + eye.height) * 0.2);
                Imgproc.circle(frame, eyeCenter, radius, new Scalar(255, 0, 0), 4);
            }
        }



        return frame;
    }

    private Mat grabFrame(){
        Mat frame = new Mat();
        // проверяем, открыт ли захват
        if (this.capture.isOpened())
        {
            // читать текущий кадр
            this.capture.read(frame);
        }
        return frame;
    }


    private void stopAcquisition() throws InterruptedException {
        if (this.timer!=null && !this.timer.isShutdown())
        {
            // остановить таймер
            this.timer.shutdown();
            this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
        }

        if (this.capture.isOpened())
        {
            // отключить камеру
            this.capture.release();
        }
    }

    private void updateImageView(ImageView view, Image image)
    {
        Utils.onFXThread(view.imageProperty(), image);
    }

    /**
     * При закрытии приложения прекращаем получение с камеры
     */
    protected void setClosed() throws InterruptedException {
        this.stopAcquisition();
    }
}