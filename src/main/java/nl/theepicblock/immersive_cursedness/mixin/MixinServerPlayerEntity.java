package nl.theepicblock.immersive_cursedness.mixin;

import com.mojang.authlib.GameProfile;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView; // <-- Import this
import net.minecraft.storage.WriteView; // <-- Import this
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nl.theepicblock.immersive_cursedness.IC_Config;
import nl.theepicblock.immersive_cursedness.PlayerInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("PointlessBooleanExpression")
@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends MixinPlayerEntity implements PlayerInterface {
	public MixinServerPlayerEntity(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Unique
	private volatile boolean isCloseToPortal;
	@Unique
	private World unFakedWorld;
	@Unique
	private boolean enabled = true;

	@Override
	public void immersivecursedness$setCloseToPortal(boolean v) {
		isCloseToPortal = v;
	}

	@Override
	public boolean immersivecursedness$getCloseToPortal() {
		return isCloseToPortal;
	}

	@Override
	public void immersivecursedness$fakeWorld(World world) {
		unFakedWorld = this.getWorld();
		this.setWorld(world);
	}

	@Override
	public void immersivecursedness$deFakeWorld() {
		if (unFakedWorld != null) {
			setWorld(unFakedWorld);
			unFakedWorld = null;
		}
	}

	@Override
	public ServerWorld immersivecursedness$getUnfakedWorld() {
		if (unFakedWorld != null) return (ServerWorld) unFakedWorld;
		return (ServerWorld) getWorld();
	}

	@Inject(method = "writeCustomData(Lnet/minecraft/storage/WriteView;)V", at = @At("HEAD"))
	public void writeInject(WriteView view, CallbackInfo ci) {
		if (enabled != AutoConfig.getConfigHolder(IC_Config.class).getConfig().defaultEnabled) {
			view.putBoolean("immersivecursednessenabled", enabled);
		}
	}

	@Inject(method = "readCustomData(Lnet/minecraft/storage/ReadView;)V", at = @At("HEAD"))
	public void readInject(ReadView view, CallbackInfo ci) {
		enabled = view.getBoolean("immersivecursednessenabled", AutoConfig.getConfigHolder(IC_Config.class).getConfig().defaultEnabled);
	}

	@Override
	public void immersivecursedness$setEnabled(boolean v) {
		enabled = v;
	}

	@Override
	public boolean immersivecursedness$getEnabled() {
		return enabled;
	}
}