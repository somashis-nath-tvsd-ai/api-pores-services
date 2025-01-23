package com.igot.cb.pores.cache;

import com.igot.cb.execution.component.Utilities;

public interface ICacheService extends Utilities {

    public void putCache(String key, Object object);

    public String getCache(String key);

    public void deleteCache(String key);
}
