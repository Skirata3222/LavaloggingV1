package net.skirata3222.lavalogging.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.skirata3222.lavalogging.util.Lavaloggable;

@Mixin(HorizontalConnectingBlock.class)
public abstract class HorizontalConnectingBlockMixin implements Lavaloggable{

	@Shadow protected VoxelShape[] boundingShapes;

	@Invoker("getShapeIndex")
	protected abstract int callGetShapeIndex(BlockState state);

	private VoxelShape shapeFor(BlockState s){
		return boundingShapes[callGetShapeIndex(s)];
	}

	@Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
	private void handleOutline(BlockState state, BlockView world, BlockPos pos,
								  ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
		if (state.contains(LAVALOGGED) && state.get(LAVALOGGED)) {
			VoxelShape shape = shapeFor(state.with(LAVALOGGED, false));
			cir.setReturnValue(shape);
		}
	}

	@Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
	private void handleCollision(BlockState state, BlockView world, BlockPos pos,
								  ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
		if (state.contains(LAVALOGGED) && state.get(LAVALOGGED)) {
			VoxelShape shape = shapeFor(state.with(LAVALOGGED, false));
			cir.setReturnValue(shape);
		}
	}

	@Inject(method = "getFluidState", at = @At("RETURN"), cancellable = true)
	private void fixFluidGetting(BlockState state, CallbackInfoReturnable<FluidState> cir) {
		if (state.contains(LAVALOGGED) && state.get(LAVALOGGED)) {
			cir.setReturnValue(Fluids.LAVA.getStill(false));
		}
		if (state.contains(Properties.WATERLOGGED) && state.get(Properties.WATERLOGGED)) {
			cir.setReturnValue(Fluids.WATER.getStill(false));
		}
	}


}
