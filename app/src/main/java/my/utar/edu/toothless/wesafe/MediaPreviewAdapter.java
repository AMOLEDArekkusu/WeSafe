package my.utar.edu.toothless.wesafe;

import android.content.Context;
import android.content.Intent; // Added Intent import
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore; // Added MediaStore import
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Removed: import com.bumptech.glide.Glide; // No longer using Glide

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Adapter for displaying media previews in a horizontal RecyclerView
 */
public class MediaPreviewAdapter extends RecyclerView.Adapter<MediaPreviewAdapter.ViewHolder> {

    private List<Uri> mediaUris;
    private Context context;

    public MediaPreviewAdapter(List<Uri> mediaUris, Context context) {
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
        Uri mediaUri = mediaUris.get(position);

        // Load image directly without Glide
        // This is a simplified example. For production apps, consider:
        // - Offloading image loading to a background thread (e.g., using AsyncTask or Kotlin Coroutines)
        // - Implementing image caching
        // - Handling different image types and potential errors more robustly
        // - Resizing images to fit the ImageView to save memory

        // Example using AsyncTask to load image in the background
        new LoadImageTask(holder.ivPreview, context).execute(mediaUri);


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

    // AsyncTask to load images in the background
    private static class LoadImageTask extends AsyncTask<Uri, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<Context> contextReference;

        public LoadImageTask(ImageView imageView, Context context) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<>(imageView);
            contextReference = new WeakReference<>(context);
        }

        @Override
        protected Bitmap doInBackground(Uri... params) {
            Uri uri = params[0];
            Context context = contextReference.get();
            if (context == null || uri == null) {
                return null;
            }

            try {
                // For content URIs (e.g., from gallery or file picker)
                if ("content".equals(uri.getScheme())) {
                    InputStream inputStream = context.getContentResolver().openInputStream(uri);
                    if (inputStream != null) {
                        // Decode the bitmap, you might want to add options for sampling
                        // to reduce memory usage for large images.
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                        return bitmap;
                    }
                } else if ("file".equals(uri.getScheme())) { // For file URIs
                    // Decode the bitmap directly from the file path.
                    // Be cautious with direct file paths, ensure you have permissions.
                    Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
                    return bitmap;
                }
                // Add handling for other URI schemes if necessary (e.g., http, https)
            } catch (FileNotFoundException e) {
                e.printStackTrace(); // Log error
            } catch (IOException e) {
                e.printStackTrace(); // Log error
            } catch (SecurityException e) {
                e.printStackTrace(); // Log error, e.g., if you don't have permission to read the URI
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null; // Ensure bitmap is null if task is cancelled
            }

            ImageView imageView = imageViewReference.get();
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else if (imageView != null) {
                // Optionally set a placeholder if the bitmap is null (e.g., loading failed)
                // imageView.setImageResource(R.drawable.placeholder_image);
            }
        }
    }
}