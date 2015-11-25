/*
 */
package theftdetection2;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;
import org.opencv.videoio.Videoio;

/**
 * The controller associated with the only view of our application. The
 * application logic is implemented here. It handles the button for
 * starting/stopping the camera, the acquired video stream, the relative
 * controls and the image segmentation process.
 * 
 * @author <a href="mailto:shofwan.x7@gmail.com">M. Shofwan Amrullah</a>
 * @since 2015-09-26
 * 
 */

public class FXMLController{
    static Mat imag = null;
    static Mat diffFrame = null;
    BackgroundSubtractorMOG2 BS = Video.createBackgroundSubtractorMOG2();
    Vector<Rect> vectorOfRect = new Vector<Rect>();
    // FXML camera button
    @FXML
    private Button cameraButton;
    // the FXML area for showing the current frame
    @FXML
    private ImageView originalFrame;
    // the FXML area for showing the mask
    @FXML
    private ImageView backgroundSubtractionImage;
    // the FXML area for showing the output of the morphological operations
    @FXML
    private Slider minBlob;
    @FXML
    private Slider maxBlob;
    @FXML
    private Slider learningRate;
    @FXML
    private Slider saturationStart;
    @FXML
    private Slider saturationStop;
    @FXML
    private Slider valueStart;
    @FXML
    private Slider valueStop;
    @FXML
    private Slider threshold;
    // FXML label to show the current values set with the sliders
    @FXML
    private Label CurrentValues;
    @FXML
    private Slider distTolerance;
    @FXML
    private Slider areaTolerance;
    @FXML
    private Slider time;
    @FXML
    private TextField fileName;
    
    //DELETE
    public static int FRAME_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width / 2;
    public static int FRAME_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height / 2;

    public static double MIN_BLOB_AREA = 250;
    public static double MAX_BLOB_AREA = 3000;

    public static Scalar Colors[] = {
        new Scalar(255, 0, 0),      //0. Blue
        new Scalar(0, 255, 0),      //1. Green
        new Scalar(0, 0, 255),      //2. Red
        new Scalar(255, 255, 0),    //3. Cyan
        new Scalar(0, 255, 255),    //4. Yellow
        new Scalar(255, 0, 255),    //5. magenta
        new Scalar(255, 127, 255),  //6. light purple
        new Scalar(127, 0, 255),    //7. purple/pink
        new Scalar(127, 0, 127),    //8. dark purple
        new Scalar(255, 255, 255),  //9. White
        new Scalar(0, 0, 0)}; //Black
    
    static ObjekList allObjek = new ObjekLinkedList();
    static Objek detectedObjek;
    public static double FPS;
    public static double width;
    public static double height;
    public static double videoSize;
    // a timer for acquiring the video stream
    private Timer timer;
    // the OpenCV object that performs the video capture
    private VideoCapture capture = new VideoCapture();
    // a flag to change the button behavior
    private boolean cameraActive;

    // property for object binding
    private ObjectProperty<Image> backgroundSubtractionProp;
    private ObjectProperty<String> ValuesProp;

    /**
     * The action triggered by pushing the "start camera" button on the GUI
     */
@FXML
private void startCamera(){
    //System.out.println("masuk");
    // bind an image property with the original frame container
    final ObjectProperty<Image> imageProp = new SimpleObjectProperty<>();
    this.originalFrame.imageProperty().bind(imageProp);

    // bind an image property with the background Subtraction container
    backgroundSubtractionProp = new SimpleObjectProperty<>();
    this.backgroundSubtractionImage.imageProperty().bind(backgroundSubtractionProp);

    // bind a text property with the string containing the current range of
    // variable's values
    ValuesProp = new SimpleObjectProperty<>();
    this.CurrentValues.textProperty().bind(ValuesProp);

    // set a fixed width for all the image to show and preserve image ratio
    this.imageViewProperties(this.originalFrame, 400);
    this.imageViewProperties(this.backgroundSubtractionImage, 400);

    if (!this.cameraActive){
        // start the video capture
        //this.capture.open(Integer.parseInt(fileName.getText()));
        this.capture.open(fileName.getText());

        //this.capture.open(0);
        //is the video stream available?
        if (this.capture.isOpened()){
            this.cameraActive = true;

            FXMLController.FPS = this.capture.get(Videoio.CAP_PROP_FPS);
            System.out.println("FPS = "+FPS);
            FXMLController.height = this.capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            FXMLController.width  = this.capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
            FXMLController.videoSize = width * height;
            double a = this.capture.get(Videoio.CAP_PROP_FRAME_HEIGHT),
                    b = this.capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
            double c= a*b;

            System.out.println(FXMLController.height
                               +" x "+FXMLController.width
                               +" = "+FXMLController.videoSize);
            System.out.println(a+" x "+b+" = "+c);

            //clear the previous object
            allObjek.clear();
            //System.out.println("jumlah object terdeteksi DIAWAL: "+allObjek.size());

            TimerTask frameGrabber = new TimerTask() {
                @Override
                public void run(){
                    Image frame = grabFrame();
                    onFXThread(imageProp, frame);
                }
            };
            this.timer = new Timer();
            //grab each frame --> every (1000/FPS) ms
            this.timer.schedule(frameGrabber, 0, (long) (1000/FPS));
            //this.timer.schedule(frameGrabber, 0, (long) 30);
            ObjekLinkedList.numberFrames = 1;

            // update the button content
            this.cameraButton.setText("Stop Camera");
        }
        else{
            // log the error
            System.err.println("Failed to open the camera connection...");
        }
    }
    else{
        // the camera is not active at this point
        this.cameraActive = false;
        // update again the button content
        this.cameraButton.setText("Start Camera");
        // stop the timer
        if (this.timer != null){
            this.timer.cancel();
            this.timer = null;
        }
        // release the camera
        this.capture.release();
    }
}

@FXML
private void startSupervising(){
    allObjek.startSupervised();
    System.out.println("TRACKING STARTED");
}
/**
 * Get a frame from the opened video stream (if any)
 * 
 * @return the {@link Image} to show
 */
private Image grabFrame(){
    Image imageToShow = null;
    Mat frame = new Mat();

    // check if the capture is open
    if (this.capture.isOpened()){
        try{
            // read the current frame
            this.capture.read(frame);
            imag = frame.clone();
            // if the frame is not empty, process it
            if (!frame.empty()){
                //**set area and distance tolerance based on user desire
                ObjekLinkedList.toleranceCenter = (int)(this.distTolerance.getValue()
                                                  *(Math.sqrt(height * height + width * width))
                                                  /100);
                MIN_BLOB_AREA = (this.minBlob.getValue()*FXMLController.videoSize/100);
                MAX_BLOB_AREA = (this.maxBlob.getValue()*FXMLController.videoSize/100);
                diffFrame = new Mat(frame.size(), CvType.CV_8UC1);
                //**Background subtraction
                processFrame(capture, frame, diffFrame, BS);
                //BS.getBackgroundImage(frame);
                frame = diffFrame.clone();

                //**Blob detection / connected component labeling
                vectorOfRect = detectionContours(diffFrame);
                //if(vectorOfRect.isEmpty()) System.out.println("vectorOfRect is empty");

                //**this is for storing the Objek that meet User's requirement
                storeObjek();

                System.out.println("jumlah object dideteksi saat ini: "+allObjek.size());
                if((ObjekLinkedList.objekDiawasi != null)&&(!ObjekLinkedList.objekDiawasi.isEmpty())){
                    //**comparing objects detected from current frame with observed objects 
                    allObjek.supervisingObjek();
                    //**detecting if any object that being observed are missing/not
                    detectPencurian();  
                }

                // show the current range
                String valuesToPrint = "Blob area: " + ((double)Math.round((this.minBlob.getValue()*100))/100) 
                                + "%-"+ ((double)Math.round((this.maxBlob.getValue()*100))/100)
                                + "%\tLearning Rate: " + ((double)Math.round((this.learningRate.getValue()*10000))/10000)  
                                + "\tThreshold: " + Math.round(this.threshold.getValue())
                                +"\tDistance Tolerance: "+((double)Math.round((this.distTolerance.getValue()*100))/100)
                                +"%\tTime: "+((double)Math.round((this.time.getValue()*100))/100)+" sec";
                this.onFXThread(this.ValuesProp, valuesToPrint);
                // show the BS output
                this.onFXThread(backgroundSubtractionProp, this.mat2Image(frame));

                // convert the Mat object (OpenCV) to Image (JavaFX)
                imageToShow = mat2Image(imag);
            }
            else{
                this.capture.release();
            }
        }
        catch (Exception e){
            // log the (full) error
            System.err.print("ERROR");
            e.printStackTrace();
        }
    }
    return imageToShow;
}

/**
 * background subtraction goes here
 * Image morphology goes here
 */
protected void processFrame(VideoCapture capture, Mat frameNow,
                Mat result, BackgroundSubtractorMOG2 BS) {
    //Background subtraction
    BS.apply(frameNow, result, this.learningRate.getValue());

    //Image morphology
    Mat erode = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, 
                                                new Size(10, 10));
    Mat dilate = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                                                new Size(10, 10));
    Mat openElem = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                    new Size(3, 3), new Point(1, 1));
    Mat closeElem = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                    new Size(10, 10), new Point(3, 3));
    Imgproc.threshold(result, result, threshold.getValue(), 255, Imgproc.THRESH_BINARY);
    Imgproc.morphologyEx(result, result, Imgproc.MORPH_OPEN, erode);
    Imgproc.morphologyEx(result, result, Imgproc.MORPH_OPEN, dilate);
    Imgproc.morphologyEx(result, result, Imgproc.MORPH_OPEN, openElem);
    Imgproc.morphologyEx(result, result, Imgproc.MORPH_CLOSE, closeElem);
}

/**
 * this is Blob detection / connected component labeling
 * @param outmat
 * @return 
 */
private Vector<Rect> detectionContours(Mat outmat) {
    Mat temp = new Mat();
    Mat binaryImage = outmat.clone();
    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    Imgproc.findContours(binaryImage, contours, temp, Imgproc.RETR_LIST,
                    Imgproc.CHAIN_APPROX_SIMPLE);

    int maxAreaIdx = -1;
    Rect r = null;
    Vector<Rect> rect_array = new Vector<Rect>();

    for (int idx = 0; idx < contours.size(); idx++) {
        Mat contour = contours.get(idx);
        double contourarea = Imgproc.contourArea(contour);
        if (contourarea > MIN_BLOB_AREA && contourarea < MAX_BLOB_AREA) {
            maxAreaIdx = idx;
            r = Imgproc.boundingRect(contours.get(maxAreaIdx));
            rect_array.add(r);
            //Imgproc.drawContours(imag, contours, maxAreaIdx, Colors[0]);
        }
    }
    temp.release();
    return rect_array;
}

/**
 * Set typical {@link ImageView} properties: a fixed width and the
 * information to preserve the original image ration
 * 
 * @param image
 *            the {@link ImageView} to use
 * @param dimension
 *            the width of the image to set
 */
private void imageViewProperties(ImageView image, int dimension){
    // set a fixed width for the given ImageView
    image.setFitWidth(dimension);
    // preserve the image ratio
    image.setPreserveRatio(true);
}

/**
 * Convert a {@link Mat} object (OpenCV) in the corresponding {@link Image}
 * for JavaFX
 * 
 * @param frame
 *            the {@link Mat} representing the current frame
 * @return the {@link Image} to show
 */
private Image mat2Image(Mat frame){
    // create a temporary buffer
    MatOfByte buffer = new MatOfByte();
    // encode the frame in the buffer, according to the PNG format
    Imgcodecs.imencode(".png", frame, buffer);
    // build and return an Image created from the image encoded in the
    // buffer
    return new Image(new ByteArrayInputStream(buffer.toArray()));
}

/**
 * Storing the objek that has been detected
 */
private void storeObjek() {
    Iterator<Rect> it = vectorOfRect.iterator();
    allObjek.clear();
    while (it.hasNext()) {
        Rect obj = it.next();
        int ObjekCenterX = (int) ((obj.tl().x + obj.br().x) / 2);
        int ObjekCenterY = (int) ((obj.tl().y + obj.br().y) / 2);

        Point pt = new Point(ObjekCenterX, ObjekCenterY);
        detectedObjek = new Objek(pt, obj);
        allObjek.add(detectedObjek);

        //make the rectangle
        Imgproc.rectangle(imag, obj.br(), obj.tl(), Colors[3], 2);
    }
}

/**
 * counting the time of the object missing.
 * And assuming that theft have been detected if object has been missing for more than "detik" seconds  
 * @param detik the threshold time
 */
private void detectPencurian() {
    Point a = new Point(0,(int)FXMLController.height/2);
    for (Objek obj : allObjek.getList()) {
        Imgproc.putText(imag, ".", obj.point, 3, 0.5, Colors[4]);
        //System.out.println("object "+i+" occurred in: "+String.valueOf(obj.occurCount+" frames"));
    }
    for (Objek obj : ObjekLinkedList.objekDiawasi) {
        //putting the center point
        Imgproc.putText(imag, ".", obj.point, 3, 0.5, Colors[4]);
        if(obj.haveOccured == true){
            Imgproc.rectangle(imag, obj.rectangle.br(), obj.rectangle.tl(), Colors[4], 2);
        }
        else{
            //**tambahkan pada object tsb sudah berapa frame dia hilang
            obj.missingCount++;
            //**jika sudah lebih dari n detik, maka dinyatakan ada pencurian
            if(obj.missingCount > (int) (this.time.getValue())*FPS){
                Imgproc.rectangle(imag, obj.rectangle.br(), obj.rectangle.tl(), Colors[5], 2);
                Imgproc.putText(imag, "PENCURIAN", a, 3, 1.75, Colors[2],3);
            }
        }
        obj.haveOccured = false;
    }
}
    
    /**
     * printing Mat (for debug purpose)
     * @param print the Mat that want to be printed
     */
    private void printMat(Mat print) {
        double[] aa = new double[5];
        System.out.println("");
        System.out.println("");
        for(int i = 0; i<print.height(); i++){
            for(int j = 0; j<print.width(); j++){
                
                aa = print.get(i, j);
                for(int k = 0; k<aa.length; k++){
                     System.out.print(aa[k]+" ||");
                }
            }
            System.out.println("");
        }
        System.out.println("");
        System.out.println("");
        System.out.println("");
    }
    
    /**
     * Generic method for putting element running on a non-JavaFX thread on the
     * JavaFX thread, to properly update the UI
     * 
     * @param property
     *            a {@link ObjectProperty}
     * @param value
     *            the value to set for the given {@link ObjectProperty}
     */
    private <T> void onFXThread(final ObjectProperty<T> property, final T value){
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                property.set(value);
            }
        });
    }

}
