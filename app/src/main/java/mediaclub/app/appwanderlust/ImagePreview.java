package mediaclub.app.appwanderlust;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import mediaclub.app.appwanderlust.Controller.AppController;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ImagePreview extends AppCompatActivity {

    String imageUrl;
    ImageView image;
    PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        image = (ImageView) findViewById(R.id.image);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            imageUrl = extras.getString("URL");
        } else {
            //Log.v("LoginActivity", savedInstanceState.toString());
            //Toast.makeText(ChatActivity.this, savedInstanceState.toString(), Toast.LENGTH_SHORT).show();
            imageUrl = savedInstanceState.getString("URL");
            //Toast.makeText(ChatActivity.this, otherId + " " + otherNickname, Toast.LENGTH_SHORT).show();
        }

        ImageLoader imageLoader = AppController.getInstance().getImageLoader();

// If you are using normal ImageView
        imageLoader.get("http://appwanderlust.com/appapi/" + imageUrl, new ImageLoader.ImageListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e(TAG, "Image Load Error: " + error.getMessage());
                //Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                if (response.getBitmap() != null) {
                    // load image into imageview
                    image.setImageBitmap(response.getBitmap());
                }
            }
        }, 2000, 1200);

        mAttacher = new PhotoViewAttacher(image);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Toast.makeText(ChatActivity.this, "save state", Toast.LENGTH_SHORT).show();

        outState.putString("URL", imageUrl);
    }
}
