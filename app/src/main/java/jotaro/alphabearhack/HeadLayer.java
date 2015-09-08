package jotaro.alphabearhack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

;

/**
 * Creates the head layer view which is displayed directly on window manager.
 * It means that this view is above every application's view on your phone -
 * until another application does the same.
 */
@SuppressWarnings("ResourceType")
public class HeadLayer extends View {

    private Context context;
    private FrameLayout frameLayout;
    private WindowManager windowManager;
    private int dataColor = -2295585;
    private int whiteColor = -1;
    private int blackColor = -16777216;
    private int tileSize = 0;
    public Button b;
    private final Scanner dictionary = new Scanner(new File("/sdcard/AlphaBearHack/words.txt"));
    private TessBaseAPI baseApi = new TessBaseAPI();

    public HeadLayer(Context context) throws FileNotFoundException {
        super(context);

        this.context = context;

        createHeadView();
    }

    /**
     * Creates head view and adds it to the window manager.
     */
    private void createHeadView() throws FileNotFoundException {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;

        frameLayout = new FrameLayout(context);

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //windowManager.addView(frameLayout, params);
        b = new Button(context);
        b.setText("Click to get Answer");
        windowManager.addView(b, params);
        String DATA_PATH = "/sdcard/AlphaBearHack";
        baseApi.init(DATA_PATH, "eng");

        b.setOnClickListener(
            new OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("Clicked");
                    File folder = new File("/sdcard/Pictures/Screenshots");
                    System.out.println(folder.isDirectory());
                    File image = folder.listFiles()[folder.listFiles().length - 1];
                    if (image.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
                        System.out.println(image.getName());
                        int width = myBitmap.getWidth();
                        int height = myBitmap.getHeight();
                        Bitmap cropped = Bitmap.createBitmap(myBitmap, 0, (height - width) / 2, width, width);
                        tileSize = getTileSize(cropped, width);
                        System.out.println("tileSize: " + tileSize);
                        binarize(cropped, dataColor);
                        System.out.println("Done binarization");
                        ImagePiece[][] grid = divideImage(cropped, width, baseApi);
                        AlphaBearSolver abs = new AlphaBearSolver(dictionary);
                        ArrayList<String> results = abs.getWords(printLetters(grid));
                        displayFirstFive(results, view);
                    }
                }
            }
        );

        frameLayout.setOnTouchListener(
                new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent me) {
                        System.out.println("Clicked");
                        return true;
                    }
                }
        );

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Here is the place where you can inject whatever layout you want.
        layoutInflater.inflate(R.layout.head, frameLayout);
    }

    /**
     * Removes the view from window manager.
     */
    public void destroy() {
        windowManager.removeView(frameLayout);
    }

    //Takes a BufferedImage and the title of the output file name, and write PNG image to that file
    public void outputImage(Bitmap croped, String title) throws IOException{
        FileOutputStream out = null;
        try {
            out = new FileOutputStream("/sdcard/AlphaBearHack/" + title);
            croped.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getTileSize(Bitmap img, int width){
        int size = 0;
        int currentColor = img.getPixel(0, 0);
        int counter = 0;
        int sumTime = 0;
        int sum = 0;
        for(int y=0; y<img.getWidth(); y++) {
            for(int x=0; x<img.getHeight(); x++) {
                if(img.getPixel(x, y) == currentColor){
                    counter++;
                } else {
                    if(counter > 80 && counter < 130){
                        double ratio = counter/(width*1.0);
                        //System.out.println("ratio is :" + ratio + " counter is:" + counter +  " " + x + " " + y);
                        if (ratio < 0.089814815) {
                            sum += 9;
                        } else {
                            sum += 7;
                        }
                        sumTime++;
                    }
                    counter=0;
                    currentColor = img.getPixel(x, y);
                }
            }
        }
        sum/=sumTime;
        if(sum >= 8){
            return 9;
        }
        return 7;
    }

    // Perform binarization on the input BufferedImage based on the dataColor
    public void binarize(Bitmap img, int dataColor){
        int whiteColor = -1;
        int blackColor = -16777216;
        for(int i=0; i<img.getWidth(); i++) {
            for(int j=0; j<img.getHeight(); j++) {
                if(img.getPixel(i, j) != dataColor){
                    img.setPixel(i, j, whiteColor);
                }else {
                    img.setPixel(i, j, blackColor);
                }
            }
        }
    }

    public ImagePiece[][] divideImage(Bitmap img, int imageSize, TessBaseAPI scanner){
        ImagePiece[][] grid = new ImagePiece[tileSize][tileSize];
        for(int y=0; y<tileSize; y++) {
            for(int x=0; x<tileSize; x++) {
                try {
                    grid[x][y] = new ImagePiece(img, x, y, (double)imageSize/tileSize, scanner);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Alternating tile size");
                    tileSize = tileSize == 7 ? 9:7;
                    return divideImage(img, imageSize, scanner);
                }
            }
        }
        return grid;
    }

    public String printLetters(ImagePiece[][] grid) {
        String result = "";
        for(int y=0; y<tileSize; y++) {
            for(int x=0; x<tileSize; x++) {
                if(grid[x][y].isHasInfo()){
                    result += grid[x][y].getLetter();
                }
            }
        }
        System.out.println(result);
        return result;
    }

    // Takes a list of String, and print the first five elements in there
    public void displayFirstFive(ArrayList<String> results, View view){
        String answer = "";
        int size = 0;
        if(results.size() > 5){
            size = 5;
        } else {
            size = results.size();
        }
        for(int i = 0; i < size; i++){
            answer += results.get(i) + ", ";
        }
        b.setText(answer += results.get(size-1));
    }

    public void printGrid(ImagePiece[][] grid) throws IOException{
        for(int y=0; y<tileSize; y++) {
            for(int x=0; x<tileSize; x++) {
                if(grid[x][y].isHasInfo()){
                    outputImage(grid[x][y].getImage(), "/gridPiece/"+y+x+grid[x][y].getLetter() + ".png");
                }
                if(grid[x][y].isHasInfo()){
                    System.out.println("printGrid: " + grid[x][y].isHasInfo() + grid[x][y].getLetter());
                }
            }
        }
    }
}
