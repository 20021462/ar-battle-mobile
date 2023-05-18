package com.example.myapplication;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CustomArFragment extends ArFragment {
    List<Bitmap> images = new ArrayList<>();
    List<String> names = new ArrayList<>();
    AugmentedImageDatabase aid;
    @Override
    protected Config getSessionConfiguration(Session session) {

        Config config = new Config(session);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);

        aid = new AugmentedImageDatabase(session);

        AsyncGettingBitmapFromUrl asyncGettingBitmapFromUrl = new AsyncGettingBitmapFromUrl();
        asyncGettingBitmapFromUrl.execute();

//        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.image);
//        Bitmap image2 = BitmapFactory.decodeResource(getResources(), R.drawable.image2);
//        aid.addImage("image", image);
//        aid.addImage("image2", image2);
        for (int i = 0; i < images.size(); i++) {
            aid.addImage(names.get(i), images.get(i));
        }

        config.setAugmentedImageDatabase(aid);

        this.getArSceneView().setupSession(session);
        return config;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        FrameLayout frameLayout = (FrameLayout) super.onCreateView(inflater, container, savedInstanceState);

        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);

        return frameLayout;
    }

    @Nullable
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncGettingBitmapFromUrl extends AsyncTask<String, Void, Bitmap> {
        FirebaseFirestore db;
        static final String TAG = "Read Data Activity";

        @Override
        protected Bitmap doInBackground(String... params) {
            db = FirebaseFirestore.getInstance();
            db.collection("map")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    String imageUrl = document.getData().get("image").toString();
                                    String name = document.getData().get("name").toString();
                                    images.add(getBitmapFromURL(imageUrl));
                                    names.add(name);
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            System.out.println("bitmap" + bitmap);

        }
    }
}

