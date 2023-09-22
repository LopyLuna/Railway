package com.railwayteam.railways.registry;

import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Pair;
import io.github.fabricators_of_create.porting_lib.extensions.ITeleporter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class CRPortalTracks {
    public static void register() {
        AllPortalTracks.registerIntegration(new ResourceLocation("betterend", "betterend_portal"), CRPortalTracks::betterend);
    }

    private static Pair<ServerLevel, BlockFace> betterend(Pair<ServerLevel, BlockFace> inbound) {
        ResourceKey<Level> betterEndLevelKey =
                ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("betterend", "the_end"));
        return AllPortalTracks.standardPortalProvider(inbound, Level.OVERWORLD, betterEndLevelKey, level -> {
            try {
                return (ITeleporter) Class.forName("org.betterx.betterend.portal.PortalBuilder")
                        .getDeclaredConstructor(ServerLevel.class, boolean.class)
                        .newInstance(level, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return level.getPortalForcer();
        });
    }
}