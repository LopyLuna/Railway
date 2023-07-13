package com.railwayteam.railways.content.handcart;

import com.railwayteam.railways.content.custom_bogeys.CRBogeyBlock;
import com.railwayteam.railways.registry.CRBogeyStyles;
import com.simibubi.create.content.trains.bogey.BogeySizes;
import com.simibubi.create.content.trains.bogey.BogeySizes.BogeySize;
import com.simibubi.create.content.trains.bogey.BogeyStyle;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;

public class HandcartBlock extends CRBogeyBlock {
    public HandcartBlock(BlockBehaviour.Properties props) {
        this(props, CRBogeyStyles.HANDCART, BogeySizes.SMALL);
    }

    protected HandcartBlock(BlockBehaviour.Properties props, BogeyStyle style, BogeySize size) {
        super(props, style, size);
    }

    @Override
    public Vec3 getConnectorAnchorOffset() {
        return new Vec3(0, 7 / 32f, 8 / 32f);
    }
}
