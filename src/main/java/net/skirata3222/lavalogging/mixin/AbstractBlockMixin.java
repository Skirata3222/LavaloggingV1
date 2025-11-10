package net.skirata3222.lavalogging.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.Properties;
import net.skirata3222.lavalogging.util.Lavaloggable;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {
	
	@Inject(method = "getFluidState", at = @At("HEAD"), cancellable = true)
	private void fixFluidState(BlockState state, CallbackInfoReturnable<FluidState> cir) {
		if (state.contains(Lavaloggable.LAVALOGGED) && state.get(Lavaloggable.LAVALOGGED)) {
			cir.setReturnValue(Fluids.LAVA.getStill(false));
			return;
		}
		if (state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED)) {
			cir.setReturnValue(Fluids.WATER.getStill(false));
			return;
		}
	}

}
