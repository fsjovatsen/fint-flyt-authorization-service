package no.fintlabs.flyt.azure;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AzureUserCacheRepository {
    private final Map<String, AzureUserCache> azureUserCacheMap = new ConcurrentHashMap<>();

    public void save(AzureUserCache azureUserCache) {
        azureUserCacheMap.put(azureUserCache.getObjectIdentifier(), azureUserCache);
    }

    public void saveAll(List<AzureUserCache> azureUserCaches) {
        azureUserCaches.forEach(this::save);
    }

    public AzureUserCache findByObjectIdentifier(String objectIdentifier) {
        return azureUserCacheMap.get(objectIdentifier);
    }

    public Map<String, AzureUserCache> findAll() {
        return azureUserCacheMap;
    }

    public void deleteByObjectIdentifier(String objectIdentifier) {
        azureUserCacheMap.remove(objectIdentifier);
    }

}
