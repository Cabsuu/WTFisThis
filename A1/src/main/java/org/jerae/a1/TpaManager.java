package org.jerae.a1;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TpaManager {

    private final A1 plugin;

    public enum RequestType {
        TPA,
        TPAHERE
    }

    public static class TpaRequest {
        private final UUID requester;
        private final RequestType type;

        public TpaRequest(UUID requester, RequestType type) {
            this.requester = requester;
            this.type = type;
        }

        public UUID getRequester() {
            return requester;
        }

        public RequestType getType() {
            return type;
        }
    }

    private final Map<UUID, TpaRequest> requests = new HashMap<>();

    public TpaManager(A1 plugin) {
        this.plugin = plugin;
    }

    public void addRequest(UUID target, UUID requester, RequestType type) {
        requests.put(target, new TpaRequest(requester, type));
    }

    public TpaRequest getRequest(UUID target) {
        return requests.get(target);
    }

    public void removeRequest(UUID target) {
        requests.remove(target);
    }
}
