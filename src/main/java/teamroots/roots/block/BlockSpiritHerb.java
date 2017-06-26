package teamroots.roots.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.fml.common.registry.GameRegistry;
import teamroots.roots.RegistryManager;
import teamroots.roots.Roots;

public class BlockSpiritHerb extends BlockCrops implements IModeledBlock {
	public Item itemBlock;
	public AxisAlignedBB bounds = new AxisAlignedBB(0,0,0,1,1,1);
	public BlockSpiritHerb(String name, boolean addToTab) {
		super();
		setUnlocalizedName(name);
		setRegistryName(Roots.MODID+":"+name);
		
		GameRegistry.register(this);
        GameRegistry.register(itemBlock = (new ItemBlock(this).setRegistryName(this.getRegistryName())));
    }
	
	@Override
    protected boolean canSustainBush(IBlockState state)
    {
        return state.getBlock() == Blocks.SOUL_SAND;
    }
	
	@Override
    public net.minecraftforge.common.EnumPlantType getPlantType(net.minecraft.world.IBlockAccess world, BlockPos pos)
    {
        return EnumPlantType.Nether;
    }
	
	@Override
	public Item getSeed(){
		return RegistryManager.spirit_herb_item;
	}
	
	@Override
	public Item getCrop(){
		return RegistryManager.spirit_herb_item;
	}
	
	public BlockSpiritHerb setBoundingBox(double x1, double y1, double z1, double x2, double y2, double z2){
		this.bounds = new AxisAlignedBB(x1,y1,z1,x2,y2,z2);
		return this;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos){
		return new AxisAlignedBB(0,0,0,1,0.125f*(state.getValue(this.AGE)+1),1);
	}
	
	@Override
	public void initModel(){
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName().toString(),"inventory"));
	}
}
