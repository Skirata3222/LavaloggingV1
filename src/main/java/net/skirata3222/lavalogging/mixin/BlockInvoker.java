package net.skirata3222.lavalogging.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Block.class)
public interface BlockInvoker {
    
    @Invoker("registerDefaultState")
    void invokeRegisterDefaultState(BlockState state);

}
