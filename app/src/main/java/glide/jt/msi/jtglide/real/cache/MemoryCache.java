package glide.jt.msi.jtglide.real.cache;

import glide.jt.msi.jtglide.real.cache.recycle.Resource;

/**
 * 内存缓存接口
 */
public interface MemoryCache {

    interface ResourceRemoveListener{
        void onResourceRemoved(Resource resource);
    }

    void setResourceRemoveListener(ResourceRemoveListener listener);

    Resource put(Key key,Resource resource);

    Resource lruRemove(Key key);
}
