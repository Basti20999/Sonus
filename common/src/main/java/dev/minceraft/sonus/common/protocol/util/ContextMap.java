package dev.minceraft.sonus.common.protocol.util;

import io.netty.util.Recycler;

import java.util.HashMap;
import java.util.Map;

public class ContextMap {

    private static final Recycler<ContextMap> CONTEXT_RECYCLER = new Recycler<>() {
        @Override
        protected ContextMap newObject(Handle<ContextMap> handle) {
            return new ContextMap(handle);
        }
    };
    private final Recycler.Handle<ContextMap> handle;
    private final Map<String, Object> delegate = new HashMap<>();

    private ContextMap(Recycler.Handle<ContextMap> handle) {
        this.handle = handle;
    }

    public static ContextMap newInstance() {
        return CONTEXT_RECYCLER.get();
    }

    public void recycle() {
        this.handle.recycle(this);
    }

    @SuppressWarnings("unchecked")
    public <T> T getUnchecked(String key) {
        return (T) this.delegate.get(key);
    }

    public Object get(String key) {
        return this.delegate.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T putUnchecked(String key, T value) {
        return (T) delegate.put(key, value);
    }

    public Object put(String key, Object value) {
        return this.delegate.put(key, value);
    }

    public boolean containsKey(String key) {
        return this.delegate.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.delegate.containsValue(value);
    }

    public Object remove(String key) {
        return this.delegate.remove(key);
    }

    public void clear() {
        this.delegate.clear();
    }

    public int size() {
        return this.delegate.size();
    }

    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }

    public Map<String, Object> getDelegate() {
        return this.delegate;
    }
}
