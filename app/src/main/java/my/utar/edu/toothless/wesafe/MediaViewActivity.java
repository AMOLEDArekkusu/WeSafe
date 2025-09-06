package my.utar.edu.toothless.wesafe;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Activity for viewing media (images) in full screen
 */
public class MediaViewActivity extends AppCompatActivity {

    private static final String TAG = "MediaViewActivity";
    private ImageView imageView;

    // Executor service to run image loading on a background thread
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    // Handler to post results back to the main thread
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_view);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        initializeViews();
        loadImage();
    }

    private void initializeViews() {
        imageView = findViewById(R.id.image_view);
    }

    private void loadImage() {
        // Get image URI from intent
        Uri imageUri = getIntent().getData();
        if (imageUri != null) {
            // Load image on a background thread
            executorService.execute(() -> {
                Bitmap bitmap = loadBitmapFromUri(imageUri);
                mainThreadHandler.post(() -> {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        // Optionally, set a placeholder or error image
                        // imageView.setImageResource(R.drawable.ic_error_placeholder);
                        Log.e(TAG, "Failed to load image from URI: " + imageUri);
                    }
                });
            });
        } else {
            Log.e(TAG, "Image URI is null.");
            // Optionally, set a placeholder or error image
            // imageView.setImageResource(R.drawable.ic_no_image_placeholder);
        }
    }

    private Bitmap loadBitmapFromUri(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(imageUri);
            if (inputStream != null) {
                // Basic decoding, consider BitmapFactory.Options for larger images
                // to avoid OutOfMemoryError (e.g., inSampleSize)
                return BitmapFactory.decodeStream(inputStream);
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found for URI: " + imageUri, e);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "OutOfMemoryError while loading image: " + imageUri, e);
            // Handle OOM, perhaps by trying to load a smaller sample
        } catch (Exception e) {
            Log.e(TAG, "Error loading image from URI: " + imageUri, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (java.io.IOException e) {
                    Log.e(TAG, "Error closing input stream", e);
                }
            }
        }
        return null;
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown the executor service when the activity is destroyed
        // to prevent potential leaks and stop background tasks.
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}