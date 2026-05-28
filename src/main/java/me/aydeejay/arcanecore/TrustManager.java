package me.aydeejay.arcanecore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class TrustManager {

    private final ArcaneCore plugin;

    private final Map<UUID, Set<UUID>> trustedPlayers = new HashMap<>();
    private final Map<UUID, UUID> pendingRequests = new HashMap<>();

    public TrustManager(ArcaneCore plugin) {
        this.plugin = plugin;
    }

    public void sendRequest(Player requester, Player target) {
        pendingRequests.put(target.getUniqueId(), requester.getUniqueId());
    }

    public boolean hasRequest(Player target, Player requester) {
        return pendingRequests.containsKey(target.getUniqueId())
                && pendingRequests.get(target.getUniqueId()).equals(requester.getUniqueId());
    }

    public void removeRequest(Player target) {
        pendingRequests.remove(target.getUniqueId());
    }

    public void trust(Player a, Player b) {

        trustedPlayers
                .computeIfAbsent(a.getUniqueId(), uuid -> new HashSet<>())
                .add(b.getUniqueId());

        trustedPlayers
                .computeIfAbsent(b.getUniqueId(), uuid -> new HashSet<>())
                .add(a.getUniqueId());

        saveTrusts();
    }

    public void untrust(Player a, Player b) {

        if (trustedPlayers.containsKey(a.getUniqueId())) {
            trustedPlayers.get(a.getUniqueId()).remove(b.getUniqueId());
        }

        if (trustedPlayers.containsKey(b.getUniqueId())) {
            trustedPlayers.get(b.getUniqueId()).remove(a.getUniqueId());
        }

        saveTrusts();
    }

    public boolean trusts(Player a, Player b) {

        return trustedPlayers
                .getOrDefault(a.getUniqueId(), new HashSet<>())
                .contains(b.getUniqueId())

                &&

                trustedPlayers
                        .getOrDefault(b.getUniqueId(), new HashSet<>())
                        .contains(a.getUniqueId());
    }

    public boolean shouldBlockDamage(Player a, Player b) {
        return trusts(a, b);
    }

    public Set<UUID> getTrusted(Player player) {
        return trustedPlayers.getOrDefault(player.getUniqueId(), new HashSet<>());
    }

    public boolean isTrustedByUUID(UUID a, UUID b) {

        return trustedPlayers
                .getOrDefault(a, new HashSet<>())
                .contains(b)

                &&

                trustedPlayers
                        .getOrDefault(b, new HashSet<>())
                        .contains(a);
    }

    public void loadTrusts() {

        FileConfiguration config = plugin.getConfig();

        if (!config.contains("trusts")) {
            return;
        }

        for (String key : config.getConfigurationSection("trusts").getKeys(false)) {

            UUID playerId = UUID.fromString(key);

            List<String> list = config.getStringList("trusts." + key);

            Set<UUID> trusted = new HashSet<>();

            for (String id : list) {
                trusted.add(UUID.fromString(id));
            }

            trustedPlayers.put(playerId, trusted);
        }
    }

    public void saveTrusts() {

        FileConfiguration config = plugin.getConfig();

        config.set("trusts", null);

        for (UUID playerId : trustedPlayers.keySet()) {

            List<String> list = new ArrayList<>();

            for (UUID trustedId : trustedPlayers.get(playerId)) {
                list.add(trustedId.toString());
            }

            config.set("trusts." + playerId, list);
        }

        plugin.saveConfig();
    }
}