package me.aydeejay.arcanecore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TrustManager {

    private final ArcaneCore plugin;
    private final File file;

    private final Map<UUID, Set<UUID>> trustedPlayers = new HashMap<>();
    private final Map<UUID, UUID> pendingRequests = new HashMap<>();

    public TrustManager(ArcaneCore plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "trusts.yml");
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
        trustedPlayers.computeIfAbsent(a.getUniqueId(), uuid -> new HashSet<>()).add(b.getUniqueId());
        trustedPlayers.computeIfAbsent(b.getUniqueId(), uuid -> new HashSet<>()).add(a.getUniqueId());
        save();
    }

    public void untrust(Player a, Player b) {
        Set<UUID> aSet = trustedPlayers.get(a.getUniqueId());
        if (aSet != null) {
            aSet.remove(b.getUniqueId());
        }
        Set<UUID> bSet = trustedPlayers.get(b.getUniqueId());
        if (bSet != null) {
            bSet.remove(a.getUniqueId());
        }
        save();
    }

    public boolean trusts(Player a, Player b) {
        return isTrustedByUUID(a.getUniqueId(), b.getUniqueId());
    }

    public boolean shouldBlockDamage(Player a, Player b) {
        return trusts(a, b);
    }

    public Set<UUID> getTrusted(Player player) {
        return trustedPlayers.getOrDefault(player.getUniqueId(), new HashSet<>());
    }

    public boolean isTrustedByUUID(UUID a, UUID b) {
        return trustedPlayers.getOrDefault(a, new HashSet<>()).contains(b)
                && trustedPlayers.getOrDefault(b, new HashSet<>()).contains(a);
    }

    public void load() {
        trustedPlayers.clear();

        if (file.exists()) {
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            loadTrusts(data, "");
        }

        migrateLegacyTrusts();
    }

    private void migrateLegacyTrusts() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("trusts");
        if (section == null) {
            return;
        }

        loadTrusts(section, "");
        if (!saveToFile()) {
            return;
        }

        plugin.getConfig().set("trusts", null);
        plugin.saveConfig();
    }

    private void loadTrusts(ConfigurationSection section, String pathPrefix) {
        for (String key : section.getKeys(false)) {
            UUID playerId;
            try {
                playerId = UUID.fromString(key);
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            Set<UUID> trusted = new HashSet<>();
            for (String id : section.getStringList(pathPrefix + key)) {
                try {
                    trusted.add(UUID.fromString(id));
                } catch (IllegalArgumentException ignored) {
                    // Skip malformed entry.
                }
            }
            trustedPlayers.put(playerId, trusted);
        }
    }

    public void save() {
        saveToFile();
    }

    private boolean saveToFile() {
        FileConfiguration data = new YamlConfiguration();

        for (Map.Entry<UUID, Set<UUID>> entry : trustedPlayers.entrySet()) {
            List<String> list = new ArrayList<>();
            for (UUID trustedId : entry.getValue()) {
                list.add(trustedId.toString());
            }
            data.set(entry.getKey().toString(), list);
        }

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            data.save(file);
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save trusts.yml: " + e.getMessage());
            return false;
        }
    }
}
