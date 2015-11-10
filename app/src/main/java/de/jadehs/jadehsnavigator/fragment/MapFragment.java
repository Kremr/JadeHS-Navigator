/**
 * This file is part of JadeHS-Navigator.
 *
 * JadeHS-Navigator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * JadeHS-Navigator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JadeHS-Navigator.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.jadehs.jadehsnavigator.fragment;

import android.app.Fragment;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.util.Preferences;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

/**
 * Created by re1015 on 18.08.2015.
 */
public class MapFragment extends Fragment {
    final String TAG = "MapFragment";

    private Preferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            final ImageViewTouch mapImage = (ImageViewTouch) getActivity().findViewById(R.id.map);

            Matrix matrix = new Matrix();
            // Set start zoom state
            matrix.postScale((float)2,(float)2);

            this.preferences = new Preferences(getActivity());
            Bitmap bitmap;
            if (this.preferences.getLocation().equals(getActivity().getString(R.string.bez_OLB))){
                bitmap = getBitmapFromAsset("images/plan_oldb.png");
            }else if(this.preferences.getLocation().equals(getActivity().getString(R.string.bez_WHV))){
                bitmap = getBitmapFromAsset("images/plan_whv.png");
            }else{
                bitmap = getBitmapFromAsset("images/plan_els.png");
            }

            // show image
            mapImage.setImageBitmap(bitmap, matrix, 1, 3);

            if(this.preferences.getLocation().equals(getActivity().getString(R.string.bez_ELS))) {

                getActivity().findViewById(R.id.btncontrol).setVisibility(View.VISIBLE);
                Button btnOverview = (Button) getActivity().findViewById(R.id.btn_overview);
                Button btn1 = (Button) getActivity().findViewById(R.id.btn_1);
                Button btn2 = (Button) getActivity().findViewById(R.id.btn_2);
                Button btn3 = (Button) getActivity().findViewById(R.id.btn_3);

                btnOverview.setOnClickListener(new ButtonOnClickListener("images/plan_els.png", matrix));
                btn1.setOnClickListener(new ButtonOnClickListener("images/plan_els_oben.png", matrix));
                btn2.setOnClickListener(new ButtonOnClickListener("images/plan_els_mitte.png", matrix));
                btn3.setOnClickListener(new ButtonOnClickListener("images/plan_els_unten.png", matrix));
            }

        }catch (Exception ex){
            Log.wtf(TAG, "FAILED TO LOAD IMAGE", ex);
        }
    }

    public Bitmap getBitmapFromAsset(String filePath){
        AssetManager assetManager = getActivity().getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try{
            istr = assetManager.open(filePath);
            // Converts source file into bitmap file
            bitmap = BitmapFactory.decodeStream(istr);
        }catch (IOException ex){
            Log.wtf(TAG, "FAILED TO CREATE BITMAP", ex);
        }
        return bitmap;
    }

    private class ButtonOnClickListener implements View.OnClickListener {
        String src;
        Matrix matrix;

        public ButtonOnClickListener(String src, Matrix matrix) {
            this.src = src;
            this.matrix = matrix;
        }

        @Override
        public void onClick(View v) {
            Log.wtf(TAG, "Button has been clicked!");
            final ImageViewTouch mapImage = (ImageViewTouch) getActivity().findViewById(R.id.map);
            Bitmap bitmap = getBitmapFromAsset(this.src);
            mapImage.setImageBitmap(bitmap, matrix, 1, 3);
        }
    }
}
