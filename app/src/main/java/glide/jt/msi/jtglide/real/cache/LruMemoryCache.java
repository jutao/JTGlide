package glide.jt.msi.jtglide.real.cache;


import android.os.Build;
import android.support.v4.util.LruCache;

import glide.jt.msi.jtglide.real.cache.recycle.Resource;

public class LruMemoryCache extends LruCache<Key, Resource> implements MemoryCache {


    private ResourceRemoveListener listener;

    private boolean isRemoved=true;

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public LruMemoryCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(Key key, Resource value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //Android 4.4及以上只需要被复用的Bitmap的内存必须大于等于需要新获得Bitmap的内存，则允许复用此Bitmap
            return value.getBitmap().getAllocationByteCount();
        }
        return value.getBitmap().getByteCount();
    }

    @Override
    protected void entryRemoved(boolean evicted, Key key, Resource oldValue, Resource newValue) {
        //给复用池使用
        if(null!=listener && null!=oldValue&&!isRemoved){
            listener.onResourceRemoved(oldValue);
        }

    }

    /**
     * 当资源从内存缓存移除的时候的监听
     * @param listener
     */
    @Override
    public void setResourceRemoveListener(ResourceRemoveListener listener) {
        this.listener=listener;
    }

    @Override
    public Resource lruRemove(Key key) {
        //如果是主动移除不回调 ResourceRemoveListener.onResourceRemoved
        isRemoved=true;
        Resource removed=remove(key);
        isRemoved=false;
        return removed;
    }
}
