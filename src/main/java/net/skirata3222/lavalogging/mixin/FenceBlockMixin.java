package net.skirata3222.lavalogging.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import net.skirata3222.lavalogging.util.LavalogConfigLoader;
import net.skirata3222.lavalogging.util.LavalogPropUtil;

@Mixin(FenceBlock.class)
public abstract class FenceBlockMixin {
	
	@Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
	private void addLavaloggedProperty(StateDefinition.Builder<Block,BlockState> builder, CallbackInfo ci) {
		builder.add(LavalogPropUtil.LAVALOGGED);
	}

	@Inject(method = "<init>", 
		at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/world/level/block/FenceBlock;registerDefaultState(Lnet/minecraft/world/level/block/state/BlockState;)V",
		opcode = Opcodes.INVOKEVIRTUAL,
		shift = At.Shift.AFTER
		)
	)
	private void injectDefaultState(BlockBehaviour.Properties properties, CallbackInfo ci) {
		Block self = (Block)(Object)this;
		((BlockAccessor)self).invokeRegisterDefaultState(
			self.defaultBlockState().setValue(LavalogPropUtil.LAVALOGGED, false)
		);
	}

	@Inject(method = "getStateForPlacement", at = @At("TAIL"), cancellable = true)
	private void injectLavaPlacement(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir) {
		BlockState state = cir.getReturnValue();
		FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
		if (fluidState.getType() == Fluids.LAVA && state.hasProperty(LavalogPropUtil.LAVALOGGED) && LavalogConfigLoader.BLOCKLIST.contains(state.getBlock())) {
			cir.setReturnValue(state.trySetValue(LavalogPropUtil.LAVALOGGED, true));
			return;
		} else {
			cir.setReturnValue(state.trySetValue(LavalogPropUtil.LAVALOGGED,false));
			return;
		}
	}

	@Inject(method = "updateShape", at = @At("RETURN"))
	private void lavalogNeighbor(BlockState state, LevelReader reader, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random, CallbackInfoReturnable<BlockState> cir) {
		if (state.hasProperty(LavalogPropUtil.LAVALOGGED) && (Boolean)state.getValue(LavalogPropUtil.LAVALOGGED)) {
			tickAccess.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(reader));
		}
	}

}
