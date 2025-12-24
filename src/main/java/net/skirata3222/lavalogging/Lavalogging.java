package net.skirata3222.lavalogging;

import net.fabricmc.api.ModInitializer;
import net.minecraft.world.level.block.Block;
import net.skirata3222.lavalogging.util.LavalogConfigLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Lavalogging implements ModInitializer {

	public static final String MOD_ID = "lavalogging";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		System.out.println("[Lavaloggable] Loading mod now.");
		
		LavalogConfigLoader.load();
		for (Block block : LavalogConfigLoader.BLOCKLIST) {
			System.out.println(block);
		}
	}
	

}