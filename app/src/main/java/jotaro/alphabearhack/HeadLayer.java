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

import java.io.File;

;

/**
 * Creates the head layer view which is displayed directly on window manager.
 * It means that this view is above every application's view on your phone -
 * until another application does the same.
 */
public class HeadLayer extends View {

    private Context context;
    private FrameLayout frameLayout;
    private WindowManager windowManager;


    public HeadLayer(Context context) {
        super(context);

        this.context = context;

        createHeadView();
    }

    /**
     * Creates head view and adds it to the window manager.
     */
    private void createHeadView() {
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
        Button b = new Button(context);
        windowManager.addView(b, params);
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
                            System.out.println(myBitmap.getPixel(0,0));
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

}
