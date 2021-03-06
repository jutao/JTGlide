package glide.jt.msi.jtglide.real.cache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import glide.jt.msi.jtglide.real.cache.Key;
import glide.jt.msi.jtglide.real.cache.recycle.Resource;

/**
 * 正在使用的图片资源
 */
public class ActivityResource {

    private ReferenceQueue<Resource> queue;
    private final Resource.ResourceListener resourceListener;
    private Thread cleanReferenceQueueThread;

    private boolean isShutDown;

    public ActivityResource(Resource.ResourceListener listener) {
        this.resourceListener = listener;
    }

    /**
     * 加入活动缓存
     *
     * @param key
     * @param resource
     */
    public void activate(Key key, Resource resource) {
        resource.setResourceListener(key, resourceListener);
        activityResource.put(key, new ResourceWeakReference(key, resource, queue));

    }

    /**
     * 移除活动缓存
     */
    public Resource deActivate(Key key) {
        ResourceWeakReference reference = activityResource.remove(key);
        if (reference != null) {
            return reference.get();
        }
        return null;
    }

    /**
     * 引用队列，通知我们弱引用被回收了
     * 让我们得到通知
     *
     * @return
     */
    public ReferenceQueue<Resource> getRefrenceQueue() {
        if (null == queue) {
            queue = new ReferenceQueue<>();
            cleanReferenceQueueThread = new Thread() {
                @Override
                public void run() {
                    while (!isShutDown) {
                        try {
                            //被回收掉的引用
                            ResourceWeakReference reference = (ResourceWeakReference) queue.remove();
                            activityResource.remove(reference.key);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            cleanReferenceQueueThread.start();
        }
        return queue;
    }

    void shutDown() {
        isShutDown = true;
        if (cleanReferenceQueueThread != null) {
            cleanReferenceQueueThread.interrupt();
            try {
                //必须在5秒内结束线程
                cleanReferenceQueueThread.join(TimeUnit.SECONDS.toMillis(5));
                if (cleanReferenceQueueThread.isAlive()) {
                    throw new RuntimeException("Failed to join in time");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param key
     * @return
     */
    public Resource get(Key key){
        ResourceWeakReference reference = activityResource.get(key);
        if (reference != null) {
            return reference.get();
        }
        return null;
    }

    private Map<Key, ResourceWeakReference> activityResource = new HashMap<>();

    static final class ResourceWeakReference extends WeakReference<Resource> {

        final Key key;

        public ResourceWeakReference(Key key, Resource referent, ReferenceQueue<? super Resource> queue) {
            super(referent, queue);
            this.key = key;
        }

    }
}
