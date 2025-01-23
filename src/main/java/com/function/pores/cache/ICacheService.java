package com.function.pores.cache;

import com.function.execution.component.util.Utilities;

public interface ICacheService extends Utilities {

    public void putCache(String key, Object object);

    public String getCache(String key);

    public void deleteCache(String key);
}
