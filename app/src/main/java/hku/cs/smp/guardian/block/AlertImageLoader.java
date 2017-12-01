package hku.cs.smp.guardian.block;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class AlertImageLoader extends ImageLoader {
    private static AlertImageLoader instance;
    private RequestQueue requestQueue;

    /**
     * Constructs a new ImageLoader.
     *
     * @param queue      The RequestQueue to use for making image requests.
     * @param imageCache The cache to use as an L1 cache.
     */
    public AlertImageLoader(RequestQueue queue, ImageCache imageCache) {
        super(queue, imageCache);
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new AlertImageLoader(Volley.newRequestQueue(context), new ImageCache() {
                private final LruCache<String, Bitmap> cache = new LruCache<>(10);

                @Override
                public Bitmap getBitmap(String url) {
                    return cache.get(url);
                }

                @Override
                public void putBitmap(String url, Bitmap bitmap) {
                    cache.put(url, bitmap);
                }
            });
        }
    }

    public static AlertImageLoader getInstance() {
        return instance;
    }
}
