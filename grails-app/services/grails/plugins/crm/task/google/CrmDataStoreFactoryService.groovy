package grails.plugins.crm.task.google

import com.google.api.client.util.store.DataStore
import com.google.api.client.util.store.DataStoreFactory
import grails.plugins.crm.security.CrmUser
import grails.plugins.crm.security.CrmUserOption
import grails.transaction.Transactional

/**
 * Created by goran on 2016-02-03.
 */
@Transactional
class CrmDataStoreFactoryService implements DataStoreFactory {

    @Override
    def <V extends Serializable> DataStore<V> getDataStore(String s) throws IOException {
        new CrmUserDataStore(Long.valueOf(s), this)
    }

    int size(long userId) throws IOException {
        CrmUserOption.createCriteria().count() {
            eq('userId', userId)
        }
    }

    boolean containsKey(long userId, String key) throws IOException {
        CrmUserOption.createCriteria().count() {
            eq('userId', userId)
            eq('key', key)
        } > 0
    }

    boolean containsValue(long userId, String key) throws IOException {
        CrmUserOption.createCriteria().count() {
            eq('userId', userId)
            eq('v', groovy.json.JsonOutput.toJson([v: key]))
        } > 0
    }

    Set<String> keySet(long userId) throws IOException {
        CrmUserOption.createCriteria().list() {
            projections {
                property('key')
            }
            eq('userId', userId)
            order 'key', 'asc'
        } as Set
    }

    Collection<String> values(long userId) throws IOException {
        CrmUserOption.createCriteria().list() {
            eq('userId', userId)
            order 'key', 'asc'
        }.collect { it.value }
    }

    String get(long userId, String key) throws IOException {
        CrmUserOption.createCriteria().get() {
            eq('userId', userId)
            eq('key', key)
        }?.value
    }

    void set(long userId, String key, String value) throws IOException {
        CrmUserOption opt = CrmUserOption.createCriteria().get() {
            eq('userId', userId)
            eq('key', key)
        }
        if (opt) {
            opt.setValue(value)
            opt.save()
        } else {
            CrmUser user = CrmUser.get(userId)
            if (user) {
                user.setOption(key, value)
                user.save()
            }
        }
    }

    void clear(long userId) throws IOException {
    }

    void delete(long userId, String s) throws IOException {
        CrmUserOption opt = CrmUserOption.createCriteria().get() {
            eq('userId', userId)
            eq('key', s)
        }
        if (opt) {
            opt.delete()
        }
    }
}
