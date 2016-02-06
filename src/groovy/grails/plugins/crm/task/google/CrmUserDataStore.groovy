package grails.plugins.crm.task.google

import com.google.api.client.util.store.DataStore
import com.google.api.client.util.store.DataStoreFactory

/**
 * Created by goran on 2016-02-03.
 */
class CrmUserDataStore implements DataStore<String> {

    private long userId

    CrmDataStoreFactoryService dataStoreFactory

    public CrmUserDataStore(long userId, CrmDataStoreFactoryService factory) {
        this.userId = userId
        this.dataStoreFactory = factory
    }

    @Override
    DataStoreFactory getDataStoreFactory() {
        dataStoreFactory
    }

    @Override
    String getId() {
        String.valueOf(userId)
    }

    @Override
    int size() throws IOException {
        dataStoreFactory.size(userId)
    }

    @Override
    boolean isEmpty() throws IOException {
        size() == 0
    }

    @Override
    boolean containsKey(String key) throws IOException {
        dataStoreFactory.containsKey(userId, key)
    }

    @Override
    boolean containsValue(String key) throws IOException {
        dataStoreFactory.containsValue(userId, key)
    }

    @Override
    Set<String> keySet() throws IOException {
        dataStoreFactory.keySet(userId)
    }

    @Override
    Collection<String> values() throws IOException {
        dataStoreFactory.values(userId)
    }

    @Override
    String get(String key) throws IOException {
        dataStoreFactory.get(userId, key)
    }

    @Override
    DataStore<String> set(String key, String value) throws IOException {
        dataStoreFactory.set(userId, key, value)
        println "DataStore[$userId] $key=$value"
        this
    }

    @Override
    DataStore<String> clear() throws IOException {
        this
    }

    @Override
    DataStore<String> delete(String key) throws IOException {
        dataStoreFactory.delete(userId, key)
        this
    }
}
