package net.skirata3222.lavalogging.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import net.skirata3222.lavalogging.util.LavalogPropUtil;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockBehaviourMixin {

	@Shadow
	private int lightEmission;

	@Inject(method = "getLightEmission", at = @At("HEAD"), cancellable = true)
	private void fixLightLevel(CallbackInfoReturnable<Integer> cir) {
		BlockState state = ((BlockState)(Object)this);

		if (state.hasProperty(LavalogPropUtil.LAVALOGGED) && state.getValue(LavalogPropUtil.LAVALOGGED)) {
			cir.setReturnValue(15);
		}
	}


}
