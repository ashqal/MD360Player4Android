package com.asha.md360player4android;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.asha.vrlib.MDVRLibrary;
import com.asha.vrlib.model.MDPinchConfig;
import com.asha.vrlib.texture.MD360BitmapTexture;
import com.google.android.apps.muzei.render.GLTextureView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.asha.vrlib.MDVRLibrary.INTERACTIVE_MODE_MOTION_WITH_TOUCH;

public class RecyclerViewActivity extends AppCompatActivity {

    private Uri[] sMockData;

    private static final String TAG = "MainActivity";

    private VRLibManager manager;

    public RecyclerViewActivity() {
    }

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
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        final FeedAdapter adapter = new FeedAdapter();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(null);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState != RecyclerView.SCROLL_STATE_DRAGGING) {
                    int pos = layoutManager.findFirstCompletelyVisibleItemPosition();
                    adapter.onPosition(pos);
                }
            }
        });
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
        private final int index;
        private int pos;

        public FeedModel(int index, Uri uri) {
            this.index = index;
            this.uri = uri;
        }

        public void setPos(int pos) {
            this.pos = pos;
        }
    }

    private abstract class FeedVH extends RecyclerView.ViewHolder {

        public FeedVH(ViewGroup vp, int layoutId) {
            super(create(vp, layoutId));
        }

        public abstract void bind(FeedModel feedModel);
    }

    private class FeedSimpleVH extends FeedVH {

        public FeedSimpleVH(ViewGroup vp) {
            super(vp, R.layout.feed_simple_layout);
        }

        @Override
        public void bind(FeedModel feedModel) {
        }
    }

    private class FeedVRVH extends FeedVH implements MDVRLibrary.IBitmapProvider {

        private ImageView cover;

        private TextView text;

        private GLTextureView glTextureView;

        private ViewGroup parent;

        private MDVRLibrary vrlib;

        private FeedModel model;

        public FeedVRVH(ViewGroup vp) {
            super(vp, R.layout.feed_panorama_layout);
            cover = (ImageView) itemView.findViewById(R.id.feed_img_cover);
            text = (TextView) itemView.findViewById(R.id.feed_text);
            glTextureView = (GLTextureView) itemView.findViewById(R.id.feed_texture_view);
            parent = (ViewGroup) glTextureView.getParent();
        }

        @Override
        public void bind(FeedModel model) {
            Log.d(TAG, "FeedVRVH bind.");
            this.model = model;
            // Picasso.with(itemView.getContext()).load(model.url).into(cover);
            ensureVRLib();
            vrlib.notifyPlayerChanged();
            /*
            if (model.pos == model.index) {
                text.setText("on");
                if (glTextureView.getParent() == null) {
                    // parent.addView(glTextureView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                }

            } else {
                text.setText("off");
                // parent.removeView(glTextureView);
            }
            */
        }

        private void ensureVRLib() {
            if (vrlib == null) {
                vrlib = manager.create(this, glTextureView);
            }
        }

        @Override
        public void onProvideBitmap(final MD360BitmapTexture.Callback callback) {
            if (model == null) {
                return;
            }
            Log.d(TAG, "onProvideBitmap");
            Picasso.with(itemView.getContext()).load(model.uri).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Log.d(TAG, "onProvideBitmap onBitmapLoaded");

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
            int i = -1;
            while (i++ < 50) {
                Uri url = sMockData[(int) (Math.random() * sMockData.length)];
                feeds.add(new FeedModel(i, url));
            }
        }

        @Override
        public FeedVH onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0) {
                return new FeedVRVH(parent);
            } else {
                return new FeedSimpleVH(parent);
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
            return position == 0 || position == 5 ? 0 : 1;
        }

        @Override
        public int getItemCount() {
            return feeds.size();
        }

        private int prevPos = -1;
        public void onPosition(int pos) {
            if (prevPos == pos) {
                return;
            }

            for (FeedModel feedModel : feeds) {
                feedModel.setPos(pos);
            }

            notifyItemChanged(prevPos);
            notifyItemChanged(pos);
            prevPos = pos;
        }
    }

    private static View create(ViewGroup vp, int layout) {
        return LayoutInflater.from(vp.getContext()).inflate(layout, vp, false);
    }

    private static class VRLibManager {
        private Activity activity;

        private boolean isResumed;

        private List<MDVRLibrary> libs = new LinkedList<>();

        public VRLibManager(Activity activity) {
            this.activity = activity;
        }

        public MDVRLibrary create(MDVRLibrary.IBitmapProvider provider, GLTextureView textureView) {
            MDVRLibrary lib =  MDVRLibrary.with(activity)
                    .asBitmap(provider)
                    .pinchConfig(new MDPinchConfig().setMin(0.8f).setSensitivity(8).setDefaultValue(0.8f))
                    .interactiveMode(INTERACTIVE_MODE_MOTION_WITH_TOUCH)
                    .build(textureView);
            add(lib);
            return lib;
        }

        private void add(MDVRLibrary lib) {
            if (isResumed) {
                lib.onResume(activity);
            }

            libs.add(lib);
        }

        public void fireResumed() {
            isResumed = true;
            for (MDVRLibrary library : libs) {
                library.onResume(activity);
            }
        }

        public void firePaused() {
            isResumed = false;
            for (MDVRLibrary library : libs) {
                library.onPause(activity);
            }
        }

        public void fireDestroy() {
            for (MDVRLibrary library : libs) {
                library.onDestroy();
            }
        }
    }
}
