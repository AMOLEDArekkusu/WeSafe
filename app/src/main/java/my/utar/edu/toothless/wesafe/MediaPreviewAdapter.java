package my.utar.edu.toothless.wesafe;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Adapter for displaying media previews in a horizontal RecyclerView
 */
public class MediaPreviewAdapter extends RecyclerView.Adapter<MediaPreviewAdapter.ViewHolder> {

    private List<Uri> mediaUris;
    private Context context;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public MediaPreviewAdapter(List<Uri> mediaUris, Context context) {
        this.executorService = Executors.newFixedThreadPool(2);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.mediaUris = mediaUris;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_media_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position < 0 || position >= mediaUris.size()) {
            return;
        }

        Uri mediaUri = mediaUris.get(position);
        if (mediaUri == null) {
            return;
        }

        // Clear any existing image and set a placeholder
        holder.ivPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        
        // Load the image using our loadImage method
        loadImage(mediaUri, holder.ivPreview);

        // Set up click listener for the preview image
        holder.ivPreview.setOnClickListener(v -> {
            // Open full screen view
            Intent intent = new Intent(context, MediaViewActivity.class);
            intent.setData(mediaUri);
            context.startActivity(intent);
        });

        // Set up click listener for the delete button
        holder.btnDelete.setOnClickListener(v -> {
            // It's important to use holder.getAdapterPosition() instead of the `position`
            // parameter when an item is removed, as positions can change.
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                mediaUris.remove(currentPosition);
                notifyItemRemoved(currentPosition);
                // If you want to update the range for subsequent items
                notifyItemRangeChanged(currentPosition, mediaUris.size());


                // Notify the parent activity about the change
                if (context instanceof IncidentReportActivity) {
                    ((IncidentReportActivity) context).updateMediaPreviewVisibility();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaUris.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPreview;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivPreview = itemView.findViewById(R.id.iv_preview);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    private void loadImage(Uri uri, ImageView imageView) {
        if (uri == null || imageView == null) {
            return;
        }

        // Keep a weak reference to prevent memory leaks
        final WeakReference<ImageView> imageViewRef = new WeakReference<>(imageView);

        executorService.execute(() -> {
            Bitmap bitmap = null;
            InputStream inputStream = null;

            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;

                // First decode with inJustDecodeBounds=true to check dimensions
                if ("content".equals(uri.getScheme())) {
                    inputStream = context.getContentResolver().openInputStream(uri);
                    if (inputStream != null) {
                        BitmapFactory.decodeStream(inputStream, null, options);
                        inputStream.close();
                    }
                } else if ("file".equals(uri.getScheme())) {
                    BitmapFactory.decodeFile(uri.getPath(), options);
                }

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, 1024, 1024);
                options.inJustDecodeBounds = false;

                // Decode bitmap with inSampleSize set
                if ("content".equals(uri.getScheme())) {
                    inputStream = context.getContentResolver().openInputStream(uri);
                    if (inputStream != null) {
                        bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                    }
                } else if ("file".equals(uri.getScheme())) {
                    bitmap = BitmapFactory.decodeFile(uri.getPath(), options);
                }

            } catch (SecurityException e) {
                // Handle permission denied
                e.printStackTrace();
            } catch (IOException e) {
                // Handle I/O errors
                e.printStackTrace();
            } catch (OutOfMemoryError e) {
                // Handle out of memory
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            final Bitmap finalBitmap = bitmap;
            mainHandler.post(() -> {
                ImageView iv = imageViewRef.get();
                if (iv != null) {
                    if (finalBitmap != null) {
                        iv.setImageBitmap(finalBitmap);
                    } else {
                        // Set a placeholder for failed loads
                        iv.setImageResource(android.R.drawable.ic_dialog_alert);
                    }
                }
            });
        });
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}