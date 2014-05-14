package com.weimed.app.widgets;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.weimed.app.newsblaze.ApplicationClass;
import com.weimed.app.newsblaze.R;

/**
 * Created by Richard Lee on 5/5/14.
 */
public class TextViewWithImages extends TextView {

    public TextViewWithImages(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public TextViewWithImages(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TextViewWithImages(Context context) {
        super(context);
    }
    @Override
    public void setText(CharSequence text, BufferType type) {
        Spannable s = getTextWithImages(getContext(), text);
        super.setText(s, BufferType.SPANNABLE);
    }

    private static final Spannable.Factory spannableFactory = Spannable.Factory.getInstance();

    private static boolean addImages(Context context, Spannable spannable) {
        //final String IMAGE_PATTERN = "([^\\w]+\\.(?i)(bmp|jpg|gif|png)$)";
        //final String IMAGE_PATTERN = "(.+?)";
        final String IMAGE_PATTERN = "(http.*\\.(?i)(jpg|jpeg|png|gif|bmp)?)";
        //Pattern refImg = Pattern.compile("\\Q[img src=\\E" + IMAGE_PATTERN + "\\Q/]\\E");
        Pattern refImg = Pattern.compile("\\Q![](\\E" + IMAGE_PATTERN + "\\Q)\\E");
        boolean hasChanges = false;

        Matcher matcher = refImg.matcher(spannable);
        while (matcher.find()) {
            boolean set = true;
            for (ImageSpan span : spannable.getSpans(matcher.start(), matcher.end(), ImageSpan.class)) {
                if (spannable.getSpanStart(span) >= matcher.start()
                        && spannable.getSpanEnd(span) <= matcher.end()
                        ) {
                    spannable.removeSpan(span);
                } else {
                    set = false;
                    break;
                }
            }
            String resname = spannable.subSequence(matcher.start(1), matcher.end(1)).toString().trim();
            Log.i("RESNAME", resname);
            //int id = context.getResources().getIdentifier(resname, "raw", context.getPackageName());
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
//            try {
//                bitmap = BitmapFactory.decodeStream(context.getResources().getAssets().open(resname));
//            } catch (IOException e) {}
            //bitmap = getBitmapFromURL(resname);
            Uri imageUri = Uri.parse(resname);
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(ApplicationClass.getAppContext().getContentResolver(), imageUri);
//            } catch (FileNotFoundException e) {
//                Log.i("RESNAME", e.getMessage());
//            } catch (IOException e) {
//                Log.i("RESNAME", e.getMessage());
//            }
            //Drawable d = LoadImageFromWebOperations(resname);
            //bitmap = drawableToBitmap(LoadImageFromWebOperations(resname));
//            bitmap = ImageLoader.getInstance().loadImageSync(resname);
//            try {
//                URL url = new URL(resname);
//                bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//            } catch (MalformedURLException e) {}
//            catch (IOException e) {}
            // Load image, decode it to Bitmap and return Bitmap to callback
//            ImageLoader.getInstance().loadImage(resname, new SimpleImageLoadingListener() {
//                @Override
//                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                    // Do whatever you want with Bitmap
//                    hasChanges = true;
//                    spannable.setSpan(  new ImageSpan(context, bitmap),
//                            matcher.start(),
//                            matcher.end(),
//                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                    );
//                }
//            });
            if (set) {
                hasChanges = true;
                spannable.setSpan(  new ImageSpan(context, bitmap),
                        matcher.start(),
                        matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
        }

        return hasChanges;
    }
    private static Spannable getTextWithImages(Context context, CharSequence text) {
        Spannable spannable = spannableFactory.newSpannable(text);
        addImages(context, spannable);
        return spannable;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }

    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public ImageDownloader(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap mIcon = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                mIcon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
            return mIcon;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
    //new ImageDownloader(icon).execute(contactArray.get(position).getImageURL());
}
