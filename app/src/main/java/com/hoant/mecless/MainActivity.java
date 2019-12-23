package com.hoant.mecless;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "";
    private EditText txtUrl;
    private GridView gvGallery;
    private GalleryAdapter galleryAdapter;
    private TextView tvNumImages;
    static final int PICK_IMAGE_MULTIPLE = 1;
    private ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
    private List<String> imagesEncodedList = new ArrayList<String>();
    private String BASE_URL = "http://10.0.2.2:8080/function/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtUrl = findViewById(R.id.txtUrl);
        gvGallery = findViewById(R.id.gvGallery);
        tvNumImages = findViewById(R.id.tvNumImages);
        txtUrl.setText("http://10.0.2.2:8080/function/",TextView.BufferType.EDITABLE);
    }

    // Connect to server
    public void connectServer(View view) {
        // Create Retrofit instance
        BASE_URL = txtUrl.getText().toString();
        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
        okHttpClient.addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public okhttp3.Response intercept(@NotNull Chain chain) throws IOException {
                Request original = chain.request();

                Request request = original.newBuilder()
                        .header("Content-Type", "multipart/form-data; charset=utf-8; boundary=klab")
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            }
        });
        OkHttpClient client = okHttpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Get client and call object
        UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);

        // Generate file
        File file = new File(imagesEncodedList.get(0));

        RequestBody fileReqBody  = RequestBody.create(MediaType.parse("image/jpeg"), file);
        //RequestBody fileReqBody  = RequestBody.create(MediaType.parse(getContentResolver().getType(uri)), file);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);

        Call<PigoResponse> call = uploadAPIs.uploadImage(filePart);
        call.enqueue(new Callback<PigoResponse>() {
            @Override
            public void onResponse(@NonNull Call<PigoResponse> call, @NonNull Response<PigoResponse> response) {
                Toast.makeText(getApplicationContext(),response.body().getStatus(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<PigoResponse> call, @NonNull Throwable t) {
                Toast.makeText(getApplicationContext(),t.toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Select Images
    public void selectImages(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, PICK_IMAGE_MULTIPLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == Activity.RESULT_OK
                    && null != data) {
                // Get the Image from data
                if(data.getData()!=null){
                    // 1 Image
                    Uri mImageUri=data.getData();
                    String imagePath = getPath(getApplicationContext(), mImageUri);
                    mArrayUri.clear();
                    imagesEncodedList.clear();
                    mArrayUri.add(mImageUri);
                    tvNumImages.setText(mArrayUri.size() + " image selected");
                    imagesEncodedList.add(imagePath);
                    Toast.makeText(this, "1 image selected", Toast.LENGTH_SHORT).show();
                } else {
                    //Multiple
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        mArrayUri.clear();
                        imagesEncodedList.clear();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            imagesEncodedList.add(getPath(getApplicationContext(), uri));
                        }
                        tvNumImages.setText(mArrayUri.size() + " images selected");
                        //Log.v("LOG_TAG", "Selected Images: " + imagesEncodedList);
                        Toast.makeText(this, mArrayUri.size() + " images selected", Toast.LENGTH_SHORT).show();
                    }
                }
                // Display images in GridView
                galleryAdapter = new GalleryAdapter(this, mArrayUri);
                gvGallery.setAdapter(galleryAdapter);
                gvGallery.setVerticalSpacing(gvGallery.getHorizontalSpacing());
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) gvGallery
                        .getLayoutParams();
                mlp.setMargins(0, gvGallery.getHorizontalSpacing(), 0, 0);
                //Toast.makeText(this, imagesEncodedList.get(0), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
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
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
