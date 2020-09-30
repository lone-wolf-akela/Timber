package timber.event;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LogBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import timber.config.Config;

import java.util.ArrayList;

public class Timber {
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        World world = event.getPlayer().getEntityWorld();
        if (world.isRemote) {
            return;
        }
        if (!Config.CLIENT.activateTimberMod.get()) {
            return;
        }
        if (!timber.Main.isEnabled)
        {
            return;
        }
        if (Config.CLIENT.reverseControl.get() == !event.getPlayer().isCrouching()) {
            return;
        }
        if (event.getPlayer().getHeldItem(event.getPlayer().getActiveHand()).getItem() instanceof AxeItem
                && world.getBlockState(event.getPos()).getBlock() instanceof LogBlock) {
            boolean isCreative = event.getPlayer().isCreative();
            if(!isCreative)
            {
                event.getPlayer().addExhaustion(0.025F);
            }
            boolean drop = !isCreative || Config.SERVER.dropsInCreativeMode.get();
            chopLogs(world, event.getPos(), world.getBlockState(event.getPos()).getBlock(), drop, event.getPlayer());
        }
    }

    private static void chopLogs(World world, BlockPos pos, Block block, boolean drop, PlayerEntity player) {
        ArrayList<BlockPos> list = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x != 0 || y != 0 || z != 0) {
                        BlockPos target = pos.add(x, y, z);
                        if (world.getBlockState(target).getBlock() == block) {
                            list.add(target);
                        }
                    }
                }
            }
        }
        if (list.size() <= 0 || list == null) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            BlockPos target = list.get(i);
            destroyBlock(world, target, pos, drop);
            if (Config.SERVER.damageAxe.get() && !player.isCreative()) {
                player.getHeldItemMainhand().attemptDamageItem(1, player.getRNG(), null);
            }
            chopLogs(world, target, block, drop, player);
        }
    }

    public static boolean destroyBlock(World world, BlockPos pos, BlockPos posToDropItems, boolean drop) {
        BlockState blockstate = world.getBlockState(pos);
        if (blockstate.isAir(world, pos)) {
            return false;
        } else {
            TileEntity tileentity = blockstate.hasTileEntity() ? world.getTileEntity(pos) : null;
            if (drop) {
                Block.spawnDrops(blockstate, world, posToDropItems, tileentity);
            }

            return world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }
    }

}
