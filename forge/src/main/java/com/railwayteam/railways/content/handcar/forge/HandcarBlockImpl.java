package com.railwayteam.railways.content.handcar.forge;

import com.railwayteam.railways.content.handcar.HandcarBlock;

public class HandcarBlockImpl extends HandcarBlock {
    protected HandcarBlockImpl(Properties pProperties) {
        super(pProperties);
    }

    public static HandcarBlock create(Properties properties) {
        return new HandcarBlockImpl(properties);
    }
}
