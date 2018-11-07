package glide.jt.msi.jtglide.real.cache;

import java.security.MessageDigest;

public interface Key {
    void updateDiskCacheKey(MessageDigest md);
}
