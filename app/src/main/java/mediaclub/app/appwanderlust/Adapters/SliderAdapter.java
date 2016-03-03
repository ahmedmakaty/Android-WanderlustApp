package mediaclub.app.appwanderlust.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mediaclub.app.appwanderlust.Controller.AppController;
import mediaclub.app.appwanderlust.SliderImage;

/**
 * Created by Bloom on 9/2/2016.
 */
public class SliderAdapter extends FragmentStatePagerAdapter {

    List<String> images = new ArrayList<String>();

    public SliderAdapter(FragmentManager fm, List<String> images) {
        super(fm);
        this.images = images;

        Toast.makeText(AppController.getInstance().getApplicationContext(), "adapter " + images.toString(), Toast.LENGTH_SHORT).show();
    }
    @Override
    public Fragment getItem(int position) {
        SliderImage slide = new SliderImage(images.get(position));
        return slide;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public int getItemPosition(Object object) {
        SliderImage slide = (SliderImage) object;
        String url = slide.getUrl();
        int position = images.indexOf(url);

        if(position >= 0){
            return position;
        }else{
            return POSITION_NONE;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object obj = super.instantiateItem(container, position);
        return obj;
    }
}
