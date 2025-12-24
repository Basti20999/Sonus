package dev.minceraft.sonus.service.velocity;

import net.kyori.adventure.util.TriState;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class LuckPermsProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger("Sonus");

    private final LuckPermsAdapter adapter;

    public LuckPermsProvider() {
        LuckPermsAdapter adapter;
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            adapter = new LuckPermsAdapter();
            LOGGER.info("LuckPerms found - enabling permission integration.");
        } catch (ClassNotFoundException ignored) {
            adapter = null; // LuckPerms not found
        }
        this.adapter = adapter;
    }

    public boolean isEnabled() {
        return this.adapter != null;
    }

    public void setPermission(UUID playerId, String permission, TriState value) {
        if (this.adapter != null) {
            this.adapter.setPermission(playerId, permission, value);
        }
    }

    public static class LuckPermsAdapter {

        private final LuckPerms luckPerms = net.luckperms.api.LuckPermsProvider.get();

        public void setPermission(UUID playerId, String permission, TriState value) {
            this.luckPerms.getUserManager().loadUser(playerId).thenAccept(user -> {
                Node node = Node.builder(permission).value(value.toBooleanOrElse(false)).build();
                if (value == TriState.NOT_SET) {
                    user.data().remove(node);
                } else {
                    user.data().add(node);
                }
                this.luckPerms.getUserManager().saveUser(user);
            });
        }
    }
}
