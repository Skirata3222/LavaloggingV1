package net.skirata3222.lavalogging.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

@Mixin(Item.class)
public interface ItemInvoker {
	@Invoker("raycast")
	static BlockHitResult callRaycast(World world, PlayerEntity user, RaycastContext.FluidHandling fluidHandling) {
		throw new AssertionError();
	}
}