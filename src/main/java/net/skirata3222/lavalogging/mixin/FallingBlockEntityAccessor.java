package net.skirata3222.lavalogging.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityAccessor {
	@Invoker("<init>")
	public static FallingBlockEntity invokeNew(Level level, double x, double y, double z, BlockState state) {
		throw new AssertionError();
	}
}
