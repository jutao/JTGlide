package glide.jt.msi.jtglide.real.cache.recycle;

import android.graphics.Bitmap;

public interface BitmapPool {

    void put(Bitmap bitmap);

    /**
     * 获得一个可复用的 Bitmap
     * 通过这三个参数可以计算出内存大小
     * @param width
     * @param height
     * @param config
     * @return
     */
    Bitmap get(int width,int height,Bitmap.Config config);

}
