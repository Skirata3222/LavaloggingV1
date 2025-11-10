package net.skirata3222.lavalogging.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.AbstractBlock.AbstractBlockState;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;

import net.minecraft.world.tick.ScheduledTickView;
import net.skirata3222.lavalogging.util.Lavaloggable;

@Mixin(AbstractBlockState.class)
public abstract class AbstractBlockStateMixin {
	
	@Inject(method = "getLuminance", at = @At("HEAD"), cancellable = true)
	private void lavaloggedLuminance(CallbackInfoReturnable<Integer> cir) {
		BlockState self = (BlockState)(Object)this;
		if (self.contains(Lavaloggable.LAVALOGGED) && self.get(Lavaloggable.LAVALOGGED)) {
			cir.setReturnValue(15);
		}
	}

	@Inject(method = "getStateForNeighborUpdate",
			at = @At("HEAD"))
	private void scheduleFluidTicks(WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random, CallbackInfoReturnable<BlockState> cir) {
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof AnvilBlock) {
			if (state.contains(Lavaloggable.LAVALOGGED) && state.get(Lavaloggable.LAVALOGGED)) {
				tickView.scheduleFluidTick(pos, Fluids.LAVA, Fluids.LAVA.getTickRate(world));
			}
			if (state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED)) {
				tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
			}
		}
	}

}
