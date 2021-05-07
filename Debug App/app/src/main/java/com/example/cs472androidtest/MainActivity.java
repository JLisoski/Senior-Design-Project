package com.example.cs472androidtest;



import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import android.accessibilityservice.AccessibilityService;
import android.graphics.Region;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Magnifier;
import android.widget.Magnifier.Builder;
import android.widget.TextView;
import android.accessibilityservice.AccessibilityService.MagnificationController;
import android.accessibilityservice.AccessibilityService.MagnificationController.OnMagnificationChangedListener;
import android.view.accessibility.AccessibilityManager;
import android.content.Context;
import java.lang.reflect.Method;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button button;
    Button button2;
    TextView textView;
    View rootView;
    ImageView image;
    AccessibilityManager am;
    Builder builder;
    Magnifier magnifier;
    Vibrator vibrator;
    Context context;
    float number = 2.0f;

    //THis function is to check if Accessibility Services is enabled on an Android device.
    //Will change variable names to something less ambiguous later.
    public void accessibilityServiceChecker() {
        if (am == null)
            return;

        Class clazz = am.getClass();
        Method m = null;
        try {
            m = clazz.getMethod("isHighTextContrastEnabled", null);
        } catch (NoSuchMethodException e) {
            Log.w("FAIL", "isHighTextContrastEnabled not found in AccessibilityManager");
        }

        Object result = null;
        try {
            result = m.invoke(am, null);
            if (result != null && result instanceof Boolean) {
                Boolean b = (Boolean) result;
                Log.d("result", "b =" + b);
            }
        } catch (Exception e) {
            android.util.Log.d("fail", "isHighTextContrastEnabled invoked with an exception" + e.getMessage());
            //return;
        }
    }
    //Creates the views and getter for the buttons and text boxes.
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rootView = findViewById(android.R.id.content).getRootView();
        context = getApplicationContext();
        am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        image = findViewById(R.id.imageView);
        image.setImageResource(R.drawable.ic_launcher_background);
        vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);


        textView.setContentDescription("Text box");
        button.setContentDescription("Vibration");
        button2.setContentDescription("Magnifier");
        image.setContentDescription("green box");

        //learn how to use these properly
//        textView.setScreenReaderFocusable(true);
//        textView.setFocusableInTouchMode(false);
//        textView.setNextFocusUpId(R.id.textView);
//        textView.setAccessibilityTraversalBefore(R.id.button);
//        textView.setAccessibilityTraversalAfter(R.id.button);
//        textView.isImportantForAccessibility();

        if (am.isEnabled()) {
            AccessibilityEvent event = AccessibilityEvent.obtain();
            event.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
            event.setClassName(getClass().getName());
            event.setPackageName(context.getPackageName());
            event.getText().add("Welcome to our application.");
            am.sendAccessibilityEvent(event);
        }

        class magThread extends Thread {
            public void run() {
                //setting the OnClick listener
                button.setOnClickListener(new View.OnClickListener() {
                    boolean button_on = false;

                    //Version Code is for different levels of Android API
                    //N = API level 24
                    @RequiresApi(api = Build.VERSION_CODES.Q)
                    @Override
                    public void onClick(View v) {
                        if (button_on == false) {
                            System.out.println("yes");
                            textView.setText("Magnification Clicked!");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                magnification(textView);
                            }
                            button_on = true;
                        }
                        else if (button_on == true) {
                            textView.setText("Magnification Stopped!");
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                magnification(textView);
                            }
                            button_on = false;
                        }
                    }
                });
            }
        }

        /* not sure if this works, I don't have a real Android device to test on. It should allow the user
          to dynamically magnify based on where they are holding their finger down on the screen
          Taken from Android here: https://developer.android.com/guide/topics/text/magnifier#java*/
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (magnifier == null){
                    return false;
                }

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        // Fall through.
                    case MotionEvent.ACTION_MOVE: {
                        final int[] viewPosition = new int[2];
                        v.getLocationOnScreen(viewPosition);
                        magnifier.show(event.getRawX(),
                                event.getRawY(), event.getRawX(), event.getRawY());
                        break;
                    }

                    case MotionEvent.ACTION_CANCEL:
                        // Fall through.
                    case MotionEvent.ACTION_UP: {
//                        magnifier.dismiss();
                    }
                }
                return true;
            }
        });

        //Button2 click
        class vibThread extends Thread {
            public void run() {
                button2.setOnClickListener(new View.OnClickListener() {
                    boolean button_on = false;

                    @Override
                    public void onClick(View v) {
                        if (button_on == false) {
                            textView.setText("Vibration Clicked!");
                            vibration(image, button_on);
                            accessibilityServiceChecker();
                            button_on = true;
                            return;
                        }
                        if (button_on == true) {
                            textView.setText("Vibration Cancelled!");
                            vibration(image, button_on);
                            accessibilityServiceChecker();
                            button_on = false;
                            return;
                        }
                    }
                });
            }
        }

        magThread magnificationThread = new magThread();
        vibThread vibrationThread = new vibThread();
        magnificationThread.start();
        vibrationThread.start();
    }

    //Function that executes when you click magnify
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void magnification(View view) {
        if(magnifier != null){
            magnifier.dismiss();
            magnifier = null;
            button.setText("Magnification");
            return;
        }

        view.setAccessibilityDelegate(new View.AccessibilityDelegate());
        builder = new Builder(rootView);
        builder.setInitialZoom(number);
        builder.setClippingEnabled(true);
        builder.setCornerRadius(100.0f);
        builder.setDefaultSourceToMagnifierOffset(view.getWidth()/2,view.getHeight()/2);

        builder.setElevation(100.0f);
        builder.setSize(500,600);
        magnifier = builder.build();
        magnifier.show(view.getWidth()/2, view.getHeight()/2,
                view.getWidth()/2, view.getHeight()/2);

        button.setText("Dismiss Magnification");

//        if (am.isEnabled()) {
//            AccessibilityEvent event = AccessibilityEvent.obtain();
//            event.setEventType(AccessibilityEvent.TYPE_ANNOUNCEMENT);
//            event.setClassName(getClass().getName());
//            event.setPackageName(context.getPackageName());
//            event.getText().add(textView.getText());
//            am.sendAccessibilityEvent(event);
//        }

    }

    //Function that executes when you click vibrate
    public void vibration(View view, boolean button_on) {
        view.setAccessibilityDelegate(new View.AccessibilityDelegate());
        if (button_on == false)
            vibrator.vibrate(5000);

        //Cancels vibration when the button is clicked again.
        if (button_on == true) {
                vibrator.cancel();
        }
        //This is selfvoicing - not really Talkback
        //Need to explore this more to get full Talkback working!
//        textView.announceForAccessibility(textView.getText());

        //Big downsides when using selfvoicing.

    }

}

