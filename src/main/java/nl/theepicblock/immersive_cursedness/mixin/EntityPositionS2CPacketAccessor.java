package nl.theepicblock.immersive_cursedness.mixin;

import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityPositionS2CPacket.class)
public interface EntityPositionS2CPacketAccessor {
    @Accessor("change")
    void ic$setChange(PlayerPosition v);
}
