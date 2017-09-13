package com.asha.md360player4android;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDHitEvent;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.google.android.apps.muzei.render.GLTextureView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Uri[] sMockData;

    private VRLibManager manager;

    public static void start(Context context) {
        Intent i = new Intent(context, RecyclerViewActivity.class);
        context.startActivity(i);
    }

    public RecyclerViewActivity() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sMockData = new Uri[] {
                getDrawableUri(R.drawable.bitmap360)
                ,getDrawableUri(R.drawable.texture)
        };

        setContentView(R.layout.activity_main);
        manager = new VRLibManager(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        final FeedAdapter adapter = new FeedAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(null);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.fireResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.firePaused();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.fireDestroy();
    }

    private static class FeedModel {

        private final Uri uri;
        private final int type;

        public FeedModel(int type, Uri uri) {
            this.type = type;
            this.uri = uri;
        }
    }

    private abstract class FeedVH extends RecyclerView.ViewHolder {

        public FeedVH(ViewGroup vp, int layoutId) {
            super(create(vp, layoutId));
        }

        public abstract void bind(FeedModel feedModel);
    }

    private class FeedTextVH extends FeedVH {

        public FeedTextVH(ViewGroup vp) {
            super(vp, R.layout.feed_text_layout);
        }

        @Override
        public void bind(FeedModel feedModel) {
        }
    }

    private class FeedVRVH extends FeedVH implements MDVRLibrary.IBitmapProvider {

        private TextView text;

        private GLTextureView glTextureView;

        private ViewGroup parent;

        private MDVRLibrary vrlib;

        private FeedModel model;

        private long ts;

        public FeedVRVH(ViewGroup vp) {
            super(vp, R.layout.feed_panorama_layout);
            text = (TextView) itemView.findViewById(R.id.feed_text);
            glTextureView = (GLTextureView) itemView.findViewById(R.id.feed_texture_view);
            parent = (ViewGroup) glTextureView.getParent();
        }

        @Override
        public void bind(FeedModel model) {
            this.model = model;
            ensureVRLib();
            vrlib.notifyPlayerChanged();
        }

        private void ensureVRLib() {
            if (vrlib == null) {
                vrlib = manager.create(this, glTextureView);
                vrlib.setEyePickChangedListener(new MDVRLibrary.IEyePickListener2() {
                    @Override
                    public void onHotspotHit(MDHitEvent hitEvent) {
                        long delta = System.currentTimeMillis() - ts;
                        if (delta < 500) {
                            return;
                        }

                        String brief = vrlib.getDirectorBrief().toString();
                        text.setText(brief);
                        ts = System.currentTimeMillis();
                    }
                });
            }
        }

        @Override
        public void onProvideBitmap(final MD360BitmapTexture.Callback callback) {
            if (model == null) {
                return;
            }

            Picasso.with(itemView.getContext()).load(model.uri).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    vrlib.onTextureResize(bitmap.getWidth(), bitmap.getHeight());
                    callback.texture(bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }
    }

    private Uri getDrawableUri(@DrawableRes int resId){
        Resources resources = getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId) + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId) );
    }

    private class FeedAdapter extends RecyclerView.Adapter<FeedVH> {

        private List<FeedModel> feeds = new ArrayList<>();

        public FeedAdapter() {
            int i = 0;
            while (i++ < 50) {
                Uri uri = sMockData[(int) (Math.random() * sMockData.length)];
                feeds.add(new FeedModel(Math.random() > 0.3 ? 0 : 1, uri));
            }
        }

        @Override
        public FeedVH onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0) {
                return new FeedVRVH(parent);
            } else {
                return new FeedTextVH(parent);
            }
        }

        @Override
        public void onBindViewHolder(FeedVH holder, int position) {
            holder.bind(feeds.get(position));
        }

        @Override
        public void onViewRecycled(FeedVH holder) {
            super.onViewRecycled(holder);
        }

        @Override
        public int getItemViewType(int position) {
            return feeds.get(position).type;
        }

        @Override
        public int getItemCount() {
            return feeds.size();
        }
    }

    private static View create(ViewGroup vp, int layout) {
        return LayoutInflater.from(vp.getContext()).inflate(layout, vp, false);
    }
}
