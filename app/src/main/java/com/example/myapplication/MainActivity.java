package com.example.myapplication;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView txtTime;
    private View controlsLayout;
    private ImageButton btnExitFS;
    private Button btnSetTimer, btnStartPause, btnReset, btnFullScreen, btnColor, btnDarkMode;
    
    private float textSize = 40f;
    private int selectedColor = Color.WHITE;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ScaleGestureDetector scaleGestureDetector;

    // Timer state
    private boolean isTimerMode = false;
    private boolean isTimerRunning = false;
    private long timeLeftInMillis = 0;
    private boolean isFullScreen = false;
    private boolean isDark = false;

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isTimerMode) {
                if (isTimerRunning && timeLeftInMillis > 0) {
                    timeLeftInMillis -= 1000;
                }
                updateTimerDisplay();
                if (timeLeftInMillis <= 0 && isTimerRunning) {
                    isTimerRunning = false;
                    btnStartPause.setText("Start");
                    Toast.makeText(MainActivity.this, "Time's up!", Toast.LENGTH_SHORT).show();
                }
            } else {
                String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                txtTime.setText(currentTime);
            }
            handler.postDelayed(this, 1000);
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        txtTime = findViewById(R.id.txtTime);
        controlsLayout = findViewById(R.id.controls);
        btnExitFS = findViewById(R.id.btnExitFS);
        
        btnSetTimer = findViewById(R.id.btnSetTimer);
        btnStartPause = findViewById(R.id.btnStartPause);
        btnReset = findViewById(R.id.btnReset);
        btnFullScreen = findViewById(R.id.btnFullScreen);
        btnColor = findViewById(R.id.btnColor);
        btnDarkMode = findViewById(R.id.btnDarkMode);

        // Restore state if available
        if (savedInstanceState != null) {
            textSize = savedInstanceState.getFloat("textSize", 40f);
            selectedColor = savedInstanceState.getInt("selectedColor", Color.WHITE);
            isTimerMode = savedInstanceState.getBoolean("isTimerMode", false);
            isTimerRunning = savedInstanceState.getBoolean("isTimerRunning", false);
            timeLeftInMillis = savedInstanceState.getLong("timeLeftInMillis", 0);
            isFullScreen = savedInstanceState.getBoolean("isFullScreen", false);
            isDark = savedInstanceState.getBoolean("isDark", false);

            txtTime.setTextSize(textSize);
            txtTime.setTextColor(selectedColor);
            btnDarkMode.setText(isDark ? "Light Mode" : "Dark Mode");
            
            if (isTimerMode) {
                btnStartPause.setVisibility(View.VISIBLE);
                btnReset.setVisibility(View.VISIBLE);
                btnSetTimer.setVisibility(View.GONE);
                btnStartPause.setText(isTimerRunning ? "Pause" : "Start");
                updateTimerDisplay();
            }
            
            if (isFullScreen) {
                // Apply full screen state after layout is ready
                txtTime.post(() -> toggleFullScreen(true));
            }
        }

        // Apply Window Insets to handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnSetTimer.setOnClickListener(v -> showSetTimerDialog());
        
        btnStartPause.setOnClickListener(v -> {
            isTimerRunning = !isTimerRunning;
            btnStartPause.setText(isTimerRunning ? "Pause" : "Start");
        });

        btnReset.setOnClickListener(v -> {
            isTimerMode = false;
            isTimerRunning = false;
            btnStartPause.setVisibility(View.GONE);
            btnReset.setVisibility(View.GONE);
            btnSetTimer.setVisibility(View.VISIBLE);
            btnStartPause.setText("Start");
        });

        btnColor.setOnClickListener(v -> showColorPickerDialog());
        btnFullScreen.setOnClickListener(v -> toggleFullScreen(true));
        btnExitFS.setOnClickListener(v -> toggleFullScreen(false));

        btnDarkMode.setOnClickListener(v -> {
            isDark = !isDark;
            if (isDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                btnDarkMode.setText("Light Mode");
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                btnDarkMode.setText("Dark Mode");
            }
        });

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                textSize *= detector.getScaleFactor();
                textSize = Math.max(10.0f, Math.min(textSize, 300.0f));
                txtTime.setTextSize(textSize);
                return true;
            }
        });

        findViewById(R.id.main).setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });

        handler.post(runnable);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat("textSize", textSize);
        outState.putInt("selectedColor", selectedColor);
        outState.putBoolean("isTimerMode", isTimerMode);
        outState.putBoolean("isTimerRunning", isTimerRunning);
        outState.putLong("timeLeftInMillis", timeLeftInMillis);
        outState.putBoolean("isFullScreen", isFullScreen);
        outState.putBoolean("isDark", isDark);
    }

    private void showSetTimerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Timer");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(50, 40, 50, 10);
        layout.setGravity(Gravity.CENTER);

        final EditText inputHours = new EditText(this);
        inputHours.setHint("HH");
        inputHours.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputHours.setGravity(Gravity.CENTER);
        inputHours.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        final TextView colon = new TextView(this);
        colon.setText(":");
        colon.setTextSize(20);

        final EditText inputMinutes = new EditText(this);
        inputMinutes.setHint("MM");
        inputMinutes.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputMinutes.setGravity(Gravity.CENTER);
        inputMinutes.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        layout.addView(inputHours);
        layout.addView(colon);
        layout.addView(inputMinutes);
        builder.setView(layout);

        builder.setPositiveButton("Set", (dialog, which) -> {
            String hText = inputHours.getText().toString();
            String mText = inputMinutes.getText().toString();
            
            long hours = hText.isEmpty() ? 0 : Integer.parseInt(hText);
            long minutes = mText.isEmpty() ? 0 : Integer.parseInt(mText);
            
            if (hours > 0 || minutes > 0) {
                timeLeftInMillis = (hours * 3600000L) + (minutes * 60000L);
                isTimerMode = true;
                isTimerRunning = false;
                btnStartPause.setVisibility(View.VISIBLE);
                btnReset.setVisibility(View.VISIBLE);
                btnSetTimer.setVisibility(View.GONE);
                updateTimerDisplay();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateTimerDisplay() {
        int hours = (int) (timeLeftInMillis / 1000) / 3600;
        int minutes = (int) ((timeLeftInMillis / 1000) % 3600) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        txtTime.setText(timeLeftFormatted);
    }

    private void toggleFullScreen(boolean goFull) {
        this.isFullScreen = goFull;
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        if (isFullScreen) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            controlsLayout.setVisibility(View.GONE);
            btnExitFS.setVisibility(View.VISIBLE);
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars());
            controlsLayout.setVisibility(View.VISIBLE);
            btnExitFS.setVisibility(View.GONE);
        }
    }

    private void showColorPickerDialog() {
        final String[] colors = {"White", "Red", "Green", "Blue", "Yellow", "Cyan", "Magenta"};
        final int[] colorValues = {Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Timer Color");
        builder.setItems(colors, (dialog, which) -> {
            selectedColor = colorValues[which];
            txtTime.setTextColor(selectedColor);
        });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}