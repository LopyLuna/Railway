package com.railwayteam.railways.mixin;

import com.railwayteam.railways.Config;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.switches.TrackSwitch;
import com.railwayteam.railways.content.switches.TrackSwitchBlock.SwitchState;
import com.railwayteam.railways.mixin_interfaces.IGenerallySearchableNavigation;
import com.railwayteam.railways.registry.CRPackets;
import com.railwayteam.railways.util.packet.SwitchDataUpdatePacket;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Navigation;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.Optional;

@Mixin(value = CarriageContraptionEntity.class, remap = false)
public abstract class MixinCarriageContraptionEntity extends OrientedContraptionEntity {
    @Shadow private Carriage carriage;

    private MixinCarriageContraptionEntity(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(method = "control", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/trains/entity/Train;maxSpeed()F"))
    private void showSwitchOverlay(BlockPos controlsLocalPos, Collection<Integer> heldControls, Player player,
                                   CallbackInfoReturnable<Boolean> cir) {
        Navigation nav = carriage.train.navigation;

        StructureTemplate.StructureBlockInfo info = contraption.getBlocks()
                .get(controlsLocalPos);
        Direction initialOrientation = getInitialOrientation().getCounterClockWise();
        boolean inverted = false;
        if (info != null && info.state.hasProperty(ControlsBlock.FACING))
            inverted = !info.state.getValue(ControlsBlock.FACING)
                    .equals(initialOrientation);

        int targetSpeed = 0;
        if (heldControls.contains(0))
            targetSpeed++;
        if (heldControls.contains(1))
            targetSpeed--;

        if (inverted) {
            targetSpeed *= -1;
        }

        boolean spaceDown = heldControls.contains(4);

        double directedSpeed = targetSpeed != 0 ? targetSpeed : carriage.train.speed;
        Railways.temporarilySkipSwitches = true;
        boolean forward = !carriage.train.doubleEnded || (directedSpeed != 0 ? directedSpeed > 0 : !inverted);
        Pair<TrackSwitch, Pair<Boolean, Optional<SwitchState>>> lookAheadData = ((IGenerallySearchableNavigation) nav).findNearestApproachableSwitch(forward);
        Railways.temporarilySkipSwitches = false;
        TrackSwitch lookAhead = lookAheadData.getFirst();
        boolean headOn = lookAheadData.getSecond().getFirst();
        Optional<SwitchState> targetState = lookAheadData.getSecond().getSecond();

        if (lookAhead != null) {
            // try to reserve switch
            if (Config.FLIP_DISTANT_SWITCHES.get() && spaceDown && lookAhead.isAutomatic()
                    && !lookAhead.isLocked() && !carriage.train.navigation.isActive()) {
                if (headOn) {
                    lookAhead.trySetSwitchState(SwitchState.fromSteerDirection(carriage.train.manualSteer, forward));
                } else targetState.ifPresent(lookAhead::trySetSwitchState);
            }
            boolean wrong = headOn ?
                    SwitchState.fromSteerDirection(carriage.train.manualSteer, forward) != lookAhead.getSwitchState() :
                    targetState.isPresent() && lookAhead.getSwitchState() != targetState.get();
            displayApproachSwitchMessage(player, lookAhead, wrong);
        } else
            cleanUpApproachSwitchMessage(player);
    }

    boolean switchMessage = false;

    private void displayApproachSwitchMessage(Player player, TrackSwitch sw, boolean isWrong) {
        sendSwitchInfo(player, sw.getSwitchState(), sw.isAutomatic(), isWrong);
        switchMessage = true;
    }

    private void cleanUpApproachSwitchMessage(Player player) {
        if (!switchMessage)
            return;
        if (player instanceof ServerPlayer sp)
            CRPackets.PACKETS.sendTo(sp, SwitchDataUpdatePacket.clear());
        switchMessage = false;
    }

    private void sendSwitchInfo(Player player, SwitchState state, boolean automatic, boolean isWrong) {
        if (player instanceof ServerPlayer sp)
            CRPackets.PACKETS.sendTo(sp, new SwitchDataUpdatePacket(state, automatic, isWrong));
    }
}