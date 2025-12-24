package net.skirata3222.lavalogging.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(Item.class)
public interface ItemRaycastInvoker {
	@Invoker("getPlayerPOVHitResult")
	static BlockHitResult invokeRaycast(Level level, Player player, ClipContext.Fluid fluidMode) {
		throw new AssertionError();
	}
}
