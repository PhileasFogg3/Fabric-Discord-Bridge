package org.phileasfogg3.fabricdiscordbridge.utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.minecraft.server.network.ServerPlayerEntity;

public final class LuckPermsUtil {

    private static LuckPerms luckPerms;

    private LuckPermsUtil() {}

    public static boolean hasPermission(ServerPlayerEntity player, String permission) {
        if (permission == null || permission.isBlank()) return false;

        try {
            if (luckPerms == null) {
                luckPerms = LuckPermsProvider.get();
            }

            User user = luckPerms
                    .getUserManager()
                    .getUser(player.getUuid());

            if (user == null) return false;

            return user.getCachedData()
                    .getPermissionData()
                    .checkPermission(permission)
                    .asBoolean();

        } catch (IllegalStateException e) {
            // LuckPerms not loaded yet
            return false;
        }
    }
}
