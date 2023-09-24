package com.railwayteam.railways.forge.mixin;

import com.railwayteam.railways.content.fuel.tank.FuelTankBlockEntity;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Contraption.class, remap = false)
public abstract class ContraptionMixin {
    @Shadow protected abstract BlockPos toLocalPos(BlockPos globalPos);

    @Inject(method = "getBlockEntityNBT", at = @At("TAIL"))
    private void getBlockEntityNBT(Level world, BlockPos pos, CallbackInfoReturnable<CompoundTag> cir) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null)
            return;
        CompoundTag nbt = blockEntity.saveWithFullMetadata();

        if (blockEntity instanceof FuelTankBlockEntity && nbt.contains("Controller"))
            nbt.put("Controller", NbtUtils.writeBlockPos(toLocalPos(NbtUtils.readBlockPos(nbt.getCompound("Controller")))));
    }
}
