package com.railwayteam.railways.content.handcar;

import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.CREdgePointTypes;
import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.content.trains.track.TrackTargetingBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class HandcarItem extends TrackTargetingBlockItem {
    public HandcarItem(Block block, Item.Properties properties) {
        super(block, properties, CREdgePointTypes.HANDCAR);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        ItemStack stack = pContext.getItemInHand();
        BlockPos pos = pContext.getClickedPos().above();
        Level level = pContext.getLevel();
        BlockState state = level.getBlockState(pos);
        Player player = pContext.getPlayer();

        if (player == null)
            return InteractionResult.FAIL;

        if (state.getBlock() instanceof ITrackBlock) {
            //goal is to make handcart align with track, not sure how tho
            //ITrackBlock track = (ITrackBlock) level.getBlockState(pos).getBlock();
            //BlockState placeState = track.getBogeyAnchor(level, pos, state);
            BlockState placeState = CRBlocks.HANDCAR.getDefaultState();
            level.setBlock(pos, placeState, 0);
//            level.setBlock(pos.relative(Direction.NORTH), placeState, 0);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
