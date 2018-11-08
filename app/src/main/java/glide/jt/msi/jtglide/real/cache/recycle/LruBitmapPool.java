package glide.jt.msi.jtglide.real.cache.recycle;

import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;

import java.util.NavigableMap;
import java.util.TreeMap;

public class LruBitmapPool extends LruCache<Integer, Bitmap> implements BitmapPool {

    private boolean isRemoved = true;

    /**
     * 有序,负责筛选
     */
    NavigableMap<Integer, Integer> map = new TreeMap<>();

    /**
     * 溢出大小倍数
     */
    private final static int MAX_OVER_SIZE_MULTPLE = 2;

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public LruBitmapPool(int maxSize) {
        super(maxSize);
    }

    /**
     * 将 bitmap 放入复用池
     *
     * @param bitmap
     */
    @Override
    public void put(Bitmap bitmap) {
        //isMutable 必须是 true 才能被复用
        if (!bitmap.isMutable()) {
            bitmap.recycle();
            return;
        }
        int size = getSize(bitmap);
        //如果一张图片的大小大于复用池的总大小
        if (size >= maxSize()) {
            bitmap.recycle();
            return;
        }
        put(size, bitmap);
        map.put(size, 0);
    }

    protected int getSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //Android 4.4及以上只需要被复用的Bitmap的内存必须大于等于需要新获得Bitmap的内存，则允许复用此Bitmap
            return bitmap.getAllocationByteCount();
        }
        return bitmap.getByteCount();
    }

    @Override
    protected int sizeOf(Integer key, Bitmap value) {
        return getSize(value);
    }

    @Override
    protected void entryRemoved(boolean evicted, Integer key, Bitmap oldValue, Bitmap newValue) {
        map.remove(key);
        if(!isRemoved){
            oldValue.recycle();
        }
    }

    /**
     * 获得一个可复用的Bitmap
     *
     * @param width
     * @param height
     * @param config
     * @return
     */
    @Override
    public Bitmap get(int width, int height, Bitmap.Config config) {
        //ARGB_8888 占4位 ;RGB_565 2位 暂时只关心这两种
        //新 Bitmap 需要的内存大小
        int size = width * height * (config == Bitmap.Config.ARGB_8888 ? 4 : 2);
        //获得一个大于等于这个 Size 的 key
        Integer key = map.ceilingKey(size);
        //从 key 集合中找一个大于等于size 并且小于等于 size*MAX_OVER_SIZE_MULTPLE
        if (null != key && key <= size * MAX_OVER_SIZE_MULTPLE) {
            isRemoved = true;
            Bitmap remove = remove(key);
            isRemoved = false;
            return remove;
        }
        return null;
    }


}
