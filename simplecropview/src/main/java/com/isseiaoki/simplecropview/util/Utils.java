package com.isseiaoki.simplecropview.util;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.GLES10;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("unused")
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();
    private static final int SIZE_DEFAULT = 2048;
    private static final int SIZE_LIMIT = 4096;
    public static int sInputImageWidth = 0;
    public static int sInputImageHeight = 0;

    public static int getExifRotation(File file) {
        if (file == null) return 0;
        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            return getRotateDegreeFromOrientation(
                    exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED
                    )
            );
        } catch (IOException e) {
            Logger.e("An error occurred while getting the exif data: " + e.getMessage(), e);
        }
        return 0;
    }

    public static int getExifRotation(Context context, Uri uri){
        Cursor cursor = null;
        String[] projection = {MediaStore.Images.ImageColumns.ORIENTATION};
        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                return 0;
            }
            return cursor.getInt(0);
        } catch (RuntimeException ignored) {
            return 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int getExifOrientation(Context context, Uri uri) {
        String authority = uri.getAuthority().toLowerCase();
        int orientation;
        if (authority.endsWith("media")) {
            orientation = getExifRotation(context, uri);
        } else {
            orientation = getExifRotation(getFileFromUri(context, uri));
        }
        return orientation;
    }

    public static int getRotateDegreeFromOrientation(int orientation) {
        int degree = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
            default:
                break;
        }
        return degree;
    }

    public static Matrix getMatrixFromExifOrientation(int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_UNDEFINED:
                break;
            case ExifInterface.ORIENTATION_NORMAL:
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1.0f, 1.0f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180.0f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1.0f, -1.0f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90.0f);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postRotate(-90.0f);
                matrix.postScale(1.0f, -1.0f);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postRotate(90.0f);
                matrix.postScale(1.0f, -1.0f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(-90.0f);
                break;
        }
        return matrix;
    }

    public static int getExifOrientationFromAngle(int angle) {
        int normalizedAngle = angle % 360;
        switch (normalizedAngle) {
            case 0:
                return ExifInterface.ORIENTATION_NORMAL;
            case 90:
                return ExifInterface.ORIENTATION_ROTATE_90;
            case 180:
                return ExifInterface.ORIENTATION_ROTATE_180;
            case 270:
                return ExifInterface.ORIENTATION_ROTATE_270;
            default:
                return ExifInterface.ORIENTATION_NORMAL;
        }
    }

    @SuppressWarnings("ResourceType")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static Uri ensureUriPermission(Context context, Intent intent) {
        Uri uri = intent.getData();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final int takeFlags = intent.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            context.getContentResolver().takePersistableUriPermission(uri, takeFlags);
        }
        return uri;
    }

    /**
     * Get image file from uri
     *
     * @param context The context
     * @param uri     The Uri of the image
     * @return Image file
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static File getFileFromUri(final Context context, final Uri uri) {
        String filePath = null;
        final boolean isKitkat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitkat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    filePath = Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                filePath = getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };
                filePath = getDataColumn(context, contentUri, selection, selectionArgs);
            } else if (isGoogleDriveDocument(uri)) {
                return getGoogleDriveFile(context, uri);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri)) {
                filePath = uri.getLastPathSegment();
            } else {
                filePath = getDataColumn(context, uri, null, null);
            }
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        if (filePath != null) {
            return new File(filePath);
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String[] projection = {
                MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnIndex =
                        (uri.toString().startsWith("content://com.google.android.gallery3d")) ?
                                cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME) :
                                cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                if (columnIndex != -1) {
                    return cursor.getString(columnIndex);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check
     * @return Whether the Uri authority is Google Drive.
     */
    public static boolean isGoogleDriveDocument(Uri uri) {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority());
    }

    /**
     * @param context The context
     * @param uri     The Uri of Google Drive file
     * @return Google Drive file
     */
    private static File getGoogleDriveFile(Context context, Uri uri) {
        if (uri == null) return null;
        FileInputStream input = null;
        FileOutputStream output = null;
        String filePath = new File(context.getCacheDir(), "tmp").getAbsolutePath();
        try {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd == null) return null;
            FileDescriptor fd = pfd.getFileDescriptor();
            input = new FileInputStream(fd);
            output = new FileOutputStream(filePath);
            int read;
            byte[] bytes = new byte[4096];
            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            return new File(filePath);
        } catch (IOException ignored) {
        } finally {
            closeQuietly(input);
            closeQuietly(output);
        }
        return null;
    }

    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri sourceUri, int requestSize) {
        InputStream is = null;
        try {
            is = context.getContentResolver().openInputStream(sourceUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = Utils.calculateInSampleSize(context, sourceUri, requestSize);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(is, null, options);
    }

    public static int calculateInSampleSize(Context context, Uri sourceUri, int requestSize) {
        InputStream is = null;
        // check image size
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            is = context.getContentResolver().openInputStream(sourceUri);
            BitmapFactory.decodeStream(is, null, options);
        } catch (FileNotFoundException ignored) {
        } finally {
            closeQuietly(is);
        }
        int inSampleSize = 1;
        sInputImageWidth = options.outWidth;
        sInputImageHeight = options.outHeight;
        while (options.outWidth / inSampleSize > requestSize
                || options.outHeight / inSampleSize > requestSize) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    public static Bitmap getScaledBitmapForHeight(Bitmap bitmap, int outHeight) {
        float currentWidth = bitmap.getWidth();
        float currentHeight = bitmap.getHeight();
        float ratio = currentWidth / currentHeight;
        int outWidth = Math.round(outHeight * ratio);
        return getScaledBitmap(bitmap, outWidth, outHeight);
    }

    public static Bitmap getScaledBitmapForWidth(Bitmap bitmap, int outWidth) {
        float currentWidth = bitmap.getWidth();
        float currentHeight = bitmap.getHeight();
        float ratio = currentWidth / currentHeight;
        int outHeight = Math.round(outWidth / ratio);
        return getScaledBitmap(bitmap, outWidth, outHeight);
    }

    public static Bitmap getScaledBitmap(Bitmap bitmap, int outWidth, int outHeight) {
        int currentWidth = bitmap.getWidth();
        int currentHeight = bitmap.getHeight();
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.postScale(
                (float) outWidth / (float) currentWidth,
                (float) outHeight / (float) currentHeight
        );
        return Bitmap.createBitmap(bitmap, 0, 0, currentWidth, currentHeight, scaleMatrix, true);
    }

    public static int getMaxSize() {
        int maxSize = SIZE_DEFAULT;
        int[] arr = new int[1];
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, arr, 0);
        if (arr[0] > 0) {
            maxSize = Math.min(arr[0], SIZE_LIMIT);
        }
        return maxSize;
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Throwable ignored) {
        }
    }
}
