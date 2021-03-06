package io.github.elytra.davincisvessels.common.handler;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import io.github.elytra.davincisvessels.common.entity.EntityParachute;

public class CommonPlayerTicker {
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase == TickEvent.Phase.END && e.player.getRidingEntity() instanceof EntityParachute && e.player.getRidingEntity().ticksExisted < 40) {
            if (e.player.isSneaking()) {
                e.player.setSneaking(false);
            }
        }
    }
}
