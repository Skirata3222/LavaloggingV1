package net.skirata3222.lavalogging.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

@Mixin(Block.class)
public interface BlockAccessor {
	@Invoker("setDefaultState")
	void invokeSetDefaultState(BlockState state);

}