package com.railwayteam.railways.content.handcar;

import com.railwayteam.railways.content.conductor.vent.VentBlock;
import com.railwayteam.railways.content.custom_bogeys.CRBogeyBlock;
import com.railwayteam.railways.registry.CRBogeyStyles;
import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.content.trains.bogey.BogeySizes.BogeySize;
import com.simibubi.create.content.trains.bogey.BogeyStyle;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class HandcarBlock extends CRBogeyBlock {
    public HandcarBlock(BlockBehaviour.Properties props) {
        this(props, CRBogeyStyles.HANDCAR, BogeySizes.SMALL);
    }

    @ExpectPlatform
    public static HandcarBlock create(Properties properties) {
        throw new AssertionError();
    }

    protected HandcarBlock(BlockBehaviour.Properties props, BogeyStyle style, BogeySize size) {
        super(props, style, size);
    }

    @Override
    public Vec3 getConnectorAnchorOffset() {
        return new Vec3(0, 7 / 32f, 8 / 32f);
    }

    @Override
    public RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

}
