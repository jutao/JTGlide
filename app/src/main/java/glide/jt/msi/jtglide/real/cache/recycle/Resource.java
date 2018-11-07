package glide.jt.msi.jtglide.real.cache.recycle;

import android.graphics.Bitmap;

import glide.jt.msi.jtglide.real.cache.Key;

public class Resource {
    private Bitmap bitmap;

    /**
     * 引用计数
     */
    private int acquired;

    public ResourceListener resourceListener;

    private Key key;

    /**
     * 当 acquired 为 0 的时候回调 onResourceReleased
     */
    public interface ResourceListener{
        /**
         * 资源释放
         */
        void onResourceReleased(Resource resource, Key key);
    }

    public Resource(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void recycle(){
        if(acquired>0){
            return;
        }
        if(!bitmap.isRecycled()){
            bitmap.recycle();
        }
    }

    public void setResourceListener(Key key,ResourceListener resourceListener) {
        this.key=key;
        this.resourceListener = resourceListener;
    }

    /**
     * 释放
     */
    public void release(){
        if(--acquired==0){
            resourceListener.onResourceReleased(this,key);
        }
    }

    public void acquire(){
        if(bitmap.isRecycled()){
            throw new IllegalArgumentException("Acquire a recycled resource");
        }
        ++acquired;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
