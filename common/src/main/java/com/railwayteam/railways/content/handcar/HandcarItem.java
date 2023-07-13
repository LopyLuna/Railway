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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class HandcarItem extends TrackTargetingBlockItem {
    public HandcarItem(Block block, Item.Properties properties) {
        super(block, properties, CREdgePointTypes.COUPLER);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        ItemStack stack = pContext.getItemInHand();
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        BlockState state = level.getBlockState(pos);
        Player player = pContext.getPlayer();

        if (player == null)
            return InteractionResult.FAIL;

        if (state.getBlock() instanceof ITrackBlock track) {
            Direction direction = Direction.UP;
            BlockPos placePos = pos.relative(direction);
            Vec3 hitPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                    .add(0, direction.getStepY() * 0.5, 0);
            BlockPlaceContext ctx = new BlockPlaceContext(
                    player, pContext.getHand(), stack, new BlockHitResult(hitPos, direction.getOpposite(), placePos, false)
            );
            if (level.getBlockState(placePos).canBeReplaced(ctx)) {
                BlockState placeState = CRBlocks.HANDCAR.getDefaultState();
                level.setBlock(placePos, placeState, 0);
            }
        }
        return InteractionResult.PASS;
    }
}
