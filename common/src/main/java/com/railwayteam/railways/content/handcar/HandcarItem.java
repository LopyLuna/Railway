package com.railwayteam.railways.content.handcar;

import com.railwayteam.railways.registry.CRBlocks;
import com.railwayteam.railways.registry.CREdgePointTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.content.trains.track.TrackTargetingBlockItem;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
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
import org.apache.commons.lang3.mutable.MutableObject;

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
            if (level.isClientSide)
                return InteractionResult.SUCCESS;

            Vec3 lookAngle = player.getLookAngle();
            boolean front = track.getNearestTrackAxis(level, pos, state, lookAngle)
                    .getSecond() == Direction.AxisDirection.POSITIVE;
            EdgePointType<?> type = getType(stack);

            MutableObject<OverlapResult> result = new MutableObject<>(null);
            withGraphLocation(level, pos, front, null, type, (overlap, location) -> result.setValue(overlap));

            if (result.getValue().feedback != null) {
                player.displayClientMessage(Lang.translateDirect(result.getValue().feedback)
                        .withStyle(ChatFormatting.RED), true);
                AllSoundEvents.DENY.play(level, null, pos, .5f, 1);
                return InteractionResult.FAIL;
            }

            Direction[] directions = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP};
            Direction successDirection = null;
            for (Direction direction : directions) {
                BlockPos placePos = pos.relative(direction);
                Vec3 hitPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                        .add(direction.getStepX() * 0.5, direction.getStepY() * 0.5, direction.getStepZ() * 0.5);
                BlockPlaceContext ctx = new BlockPlaceContext(
                        player, pContext.getHand(), stack, new BlockHitResult(hitPos, direction.getOpposite(), placePos, false)
                );
                if (level.getBlockState(placePos).canBeReplaced(ctx)) {
                    successDirection = direction;
                    break;
                }
            }

            if (successDirection == null) {
                return InteractionResult.FAIL;
            }

            BlockPos placePos = pos.relative(successDirection);
            BlockState placeState = CRBlocks.HANDCAR.getDefaultState();
            level.setBlock(placePos, placeState, 0);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }
}
//    @Override
//    public InteractionResult useOn(UseOnContext pContext) {
//        ItemStack stack = pContext.getItemInHand();
//        BlockPos pos = pContext.getClickedPos();
//        Level level = pContext.getLevel();
//        BlockState state = level.getBlockState(pos);
//        Player player = pContext.getPlayer();
//
//        if (player == null)
//            return InteractionResult.FAIL;
//
//        if (state.getBlock() instanceof ITrackBlock track) {
//            Direction direction = Direction.UP;
//            BlockPos placePos = pos.relative(direction);
//            Vec3 hitPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
//                    .add(0, direction.getStepY() * 0.5, 0);
//            BlockPlaceContext ctx = new BlockPlaceContext(
//                    player, pContext.getHand(), stack, new BlockHitResult(hitPos, direction.getOpposite(), placePos, false)
//            );
//            BlockState placeState = CRBlocks.HANDCAR.getDefaultState();
//            level.setBlock(placePos, placeState, 0);
//            return InteractionResult.SUCCESS;
//        }
//        return InteractionResult.PASS;
//    }

