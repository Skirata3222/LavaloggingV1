package net.skirata3222.lavalogging.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.skirata3222.lavalogging.util.BlockListRegistry;
import net.skirata3222.lavalogging.util.Lavaloggable;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockMixin {

	@ModifyArgs(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"
		)
	)
	private void lavalogOnLanding(Args args) {
		BlockPos pos = (BlockPos) args.get(0);
		BlockState state = (BlockState) args.get(1);
		World world = ((FallingBlockEntity)(Object)this).getWorld();

		if (state.contains(Lavaloggable.LAVALOGGED)
			&& BlockListRegistry.isAllowed(state.getBlock())
			&& world.getFluidState(pos).getFluid() == Fluids.LAVA) {
			args.set(1, state.with(Lavaloggable.LAVALOGGED, true));
		}
	}

}
