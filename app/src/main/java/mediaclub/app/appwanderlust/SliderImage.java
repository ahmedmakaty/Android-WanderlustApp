package mediaclub.app.appwanderlust;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import mediaclub.app.appwanderlust.Controller.AppController;


/**
 * A simple {@link Fragment} subclass.
 */
public class SliderImage extends Fragment {

    ImageView image;
    View view;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    String url;

    public SliderImage() {
        // Required empty public constructor
    }

    public SliderImage(String url) {
        // Required empty public constructor
        this.url = url;
        Toast.makeText(AppController.getInstance().getApplicationContext(), url, Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_slider_image, container, false);

        image = (ImageView) view.findViewById(R.id.image);

        ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        //Toast.makeText(AppController.getInstance().getApplicationContext(), url, Toast.LENGTH_SHORT).show();
// If you are using normal ImageView
        imageLoader.get("http://appwanderlust.com/appapi/" + url, new ImageLoader.ImageListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //Log.e(TAG, "Image Load Error: " + error.getMessage());
                //Toast.makeText(getContext(), error.getMessage() + "error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                if (response.getBitmap() != null) {
                    // load image into imageview
                    image.setImageBitmap(response.getBitmap());
                }
            }
        }, 2500, 2500);

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            // Fetch data or something...
        }
    }

}
