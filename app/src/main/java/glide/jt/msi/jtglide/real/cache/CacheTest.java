package glide.jt.msi.jtglide.real.cache;

import glide.jt.msi.jtglide.real.cache.recycle.BitmapPool;
import glide.jt.msi.jtglide.real.cache.recycle.LruBitmapPool;
import glide.jt.msi.jtglide.real.cache.recycle.Resource;

/**
 * 50:31
 */
public class CacheTest implements Resource.ResourceListener, MemoryCache.ResourceRemoveListener {

    private ActivityResource activityResource;
    private LruMemoryCache lruMemoryCache;

    BitmapPool bitmapPool;

    public Resource test(Key key) {
        bitmapPool=new LruBitmapPool(10);
        //内存缓存
        lruMemoryCache = new LruMemoryCache(10);
        //活动资源缓存
        activityResource = new ActivityResource(this);

        //从活动资源中查找是否有正在使用的图片
        Resource resource = activityResource.get(key);
        if (null != resource) {
            resource.acquire();
            return resource;
        }
        /**
         * 从内存缓存中查找
         */
        resource = lruMemoryCache.get(key);
        if (null != resource) {
            lruMemoryCache.remove(key);
            resource.acquire();
            activityResource.activate(key, resource);
            return resource;
        }
        return null;
    }


    /**
     * 这个资源没有正在使用
     *将其从活动资源移除，加回内存
     * @param resource
     * @param key
     */
    @Override
    public void onResourceReleased(Resource resource, Key key) {
        activityResource.deActivate(key);
        lruMemoryCache.put(key, resource);
    }

    /**
     * 从内存缓存被动移除回调
     * 放入复用池
     *
     * @param resource
     */
    @Override
    public void onResourceRemoved(Resource resource) {
        bitmapPool.put(resource.getBitmap());
    }
}
