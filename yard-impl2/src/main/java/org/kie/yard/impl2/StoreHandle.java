package org.kie.yard.impl2;

import org.drools.ruleunits.api.DataHandle;
import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.SingletonStore;

public class StoreHandle<T> {
    private SingletonStore<T> wrapped;
    private DataHandle datahandle;

    private StoreHandle(T value) {
        wrapped = DataSource.createSingleton();
        datahandle = wrapped.set(value);
    }

    private StoreHandle() {
        wrapped = DataSource.createSingleton();
        datahandle = null;
    }

    public static <T> StoreHandle<T> of(T value) {
        return new StoreHandle<>(value);
    }

    public static <T> StoreHandle<T> empty(Class<T> type) {
        return new StoreHandle<>();
    }

    public DataHandle set(T value) {
        datahandle = wrapped.set(value);
        return datahandle;
    }

    public void clear() {
        datahandle = null;
        wrapped.clear();
    }

    public boolean isValuePresent() {
        return !(datahandle==null);
    }

    public T get() {
        if (datahandle == null) {
            throw new IllegalStateException("was never set");
        }
        @SuppressWarnings("unchecked")
        T result = (T) datahandle.getObject();
        return result;
    }
}
