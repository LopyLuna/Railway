package com.railwayteam.railways.content.handcar;

import com.simibubi.create.content.trains.signal.TrackEdgePoint;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;

public class Handcar extends TrackEdgePoint {
    @Override
    public boolean canMerge() {
        return false;
    }

    @Override
    public void invalidate(LevelAccessor level) {

    }

    @Override
    public void blockEntityAdded(BlockEntity blockEntity, boolean front) {

    }

    @Override
    public void blockEntityRemoved(BlockPos blockEntityPos, boolean front) {

    }
}
