package de.impelon.disenchanter.blocks;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import de.impelon.disenchanter.DisenchanterMain;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraft.util.IIcon;

public class BlockDisenchantmentTable extends BlockContainer {
	
    @SideOnly(Side.CLIENT)
    private IIcon[] top;
    @SideOnly(Side.CLIENT)
    private IIcon[] bottom;
    @SideOnly(Side.CLIENT)
    private IIcon[] side;

	public BlockDisenchantmentTable() {
		super(Material.rock);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.75F, 1.0F);
		this.setLightOpacity(0);
		this.setCreativeTab(CreativeTabs.tabDecorations);
		this.setBlockName("blockDisenchantmentTable");
		this.setHardness(5.0F);
		this.setResistance(2000.0F);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World w, int x, int y, int z, Random random) {
		super.randomDisplayTick(w, x, y, z, random);

		for (int blockX = x - 2; blockX <= x + 2; ++blockX)
			for (int blockZ = z - 2; blockZ <= z + 2; ++blockZ) {
				if (blockX > x - 2 && blockX < x + 2 && blockZ == z - 1) 
					blockZ = z + 2;

				if (random.nextInt(16) == 0)
					for (int blockY = y; blockY <= y + 1; ++blockY)
						if (ForgeHooks.getEnchantPower(w, blockX, blockY, blockZ) > 0) {
							if (!w.isAirBlock((blockX - x) / 2 + x, blockY, (blockZ - z) / 2 + z))
								break;
							
							w.spawnParticle("enchantmenttable",
									(double) blockX + 0.25D,
									(double) blockY + 0.55D,
									(double) blockZ + 0.25D,
									(double) (x - blockX) + 0.5D,
									(double) (y - blockY) + (random.nextFloat() / 2) + 0.15D,
									(double) (z - blockZ) + 0.5D);
						}
			}
	}
	
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int unknown, CreativeTabs tab, List subItems) {
		for (byte n = 0; n < 8; n++)
			subItems.add(new ItemStack(this, 1, n));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		switch (side) {
		case 0:
			return this.bottom[(meta / 4) % 2];
		case 1:
			return this.top[(meta / 2) % 2];
		}
		return this.side[meta % 2];
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister i) {
		this.top = new IIcon[2];
		this.side = new IIcon[2];
		this.bottom = new IIcon[2];
		
		this.side[0] 	= i.registerIcon("Disenchanter:disenchantmenttable_side");
		this.side[1] 	= i.registerIcon("Disenchanter:disenchantmenttable_side_automatic");
		this.top[0] 	= i.registerIcon("Disenchanter:disenchantmenttable_top");
		this.top[1] 	= i.registerIcon("Disenchanter:disenchantmenttable_top_bulk");
		this.bottom[0] 	= i.registerIcon("Disenchanter:disenchantmenttable_bottom");
		this.bottom[1] 	= i.registerIcon("Disenchanter:disenchantmenttable_bottom_voiding");
		
		this.blockIcon 	= this.side[0];
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	public boolean isAutomatic(int meta) {
		return meta % 2 == 1;
	}
	
	public boolean isBulkDisenchanting(int meta) {
		return (meta / 2) % 2 == 1;
	}
	
	public boolean isVoiding(int meta) {
		return (meta / 4) % 2 == 1;
	}
	
	@Override
	public int damageDropped (int metadata) {
		return metadata;
	}
	
	@Override
	public boolean hasComparatorInputOverride() {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(World w, int x, int y, int z, int side) {
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileEntityDisenchantmentTableAutomatic) 
			return Container.calcRedstoneFromInventory((IInventory)te);
		return 0;
	}

	@Override
	public boolean onBlockActivated(World w, int x, int y, int z, EntityPlayer p,
			int metadata, float sideX, float sideY, float sideZ) {
		if (!w.isRemote)
			p.openGui(DisenchanterMain.instance, 0, w, x, y, z);
		return true;
	}

	@Override
	public void onBlockPlacedBy(World w, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		super.onBlockPlacedBy(w, x, y, z, entity, stack);

		if (stack.hasDisplayName())
			((TileEntityDisenchantmentTable) w.getTileEntity(x, y, z)).setCustomName(stack.getDisplayName());
	}
		
	@Override
	public void breakBlock(World w, int x, int y, int z, Block b, int meta) {
		TileEntity te = w.getTileEntity(x, y, z);
		if (te instanceof TileEntityDisenchantmentTableAutomatic) {
			TileEntityDisenchantmentTableAutomatic ta = (TileEntityDisenchantmentTableAutomatic) te;
			
			for (int i = 0; i < ta.getSizeInventory(); ++i) {
                ItemStack itemstack = ta.getStackInSlot(i);

                if (itemstack != null) {
                    float offsetX = (float) (Math.random() * 0.8F + 0.1F);
                    float offsetY = (float) (Math.random() * 0.8F + 0.1F);
                    float offsetZ = (float) (Math.random() * 0.8F + 0.1F);
                    EntityItem entityitem;

                    while (itemstack.stackSize > 0) {
                        int size = (int) (Math.random() * 21 + 10);

                        if (size > itemstack.stackSize)
                            size = itemstack.stackSize;

                        itemstack.stackSize -= size;
                        entityitem = new EntityItem(w, x + offsetX, y + offsetY, z + offsetZ, new ItemStack(itemstack.getItem(), size, itemstack.getItemDamage()));
                        entityitem.motionX = Math.random() * 0.05F;
                        entityitem.motionY = Math.random() * 0.05F + 0.2F;
                        entityitem.motionZ = Math.random() * 0.05F;

                        if (itemstack.hasTagCompound())
                            entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
                        w.spawnEntityInWorld(entityitem);
                    }
                }
            }
		}
		super.breakBlock(w, x, y, z, b, meta);
	}
	
	@Override
	public TileEntity createNewTileEntity(World w, int metadata) {
		if (isAutomatic(metadata))
			return new TileEntityDisenchantmentTableAutomatic();
		return new TileEntityDisenchantmentTable();
	}
	
	public float getEnchantingPower(World w, int x, int y, int z) {
		float power = 1;
		for (int blockZ = -1; blockZ <= 1; ++blockZ) {
			for (int blockX = -1; blockX <= 1; ++blockX) {
				if ((blockZ != 0 || blockX != 0) && w.isAirBlock(x + blockX, y, z + blockZ)
						&& w.isAirBlock(x + blockX, y + 1, z + blockZ)) {
					power += ForgeHooks.getEnchantPower(w, x + blockX * 2, y, z + blockZ * 2);
					power += ForgeHooks.getEnchantPower(w, x + blockX * 2, y + 1, z + blockZ * 2);

					if (blockX != 0 && blockZ != 0) {
						power += ForgeHooks.getEnchantPower(w, x + blockX * 2, y, z + blockZ);
						power += ForgeHooks.getEnchantPower(w, x + blockX * 2, y + 1, z + blockZ);
						power += ForgeHooks.getEnchantPower(w, x + blockX, y, z + blockZ * 2);
						power += ForgeHooks.getEnchantPower(w, x + blockX, y + 1, z + blockZ * 2);
					}
				}
			}
		}
		
		if (power > 15)
			power = 15;
		return power;
	}
	
	protected void disenchant(IInventory inventory, boolean isAutomatic, World world, int posX, int posY, int posZ, Random random) {
		if (inventory.getSizeInventory() < 3 || (isAutomatic && inventory.getStackInSlot(2) != null))
			return;

		ItemStack itemstack = inventory.getStackInSlot(0);
		ItemStack bookstack = inventory.getStackInSlot(1);
		ItemStack outputBookstack = new ItemStack(Items.enchanted_book);

		if (itemstack != null && bookstack != null && this.getEnchantmentList(itemstack) != null) {
			if (bookstack.stackSize > 1)
				bookstack.stackSize--;
			else
				bookstack = (ItemStack) null;
			inventory.setInventorySlotContents(1, bookstack);
				
			this.disenchant(itemstack, outputBookstack, isAutomatic, world, posX, posY, posZ, random);
			
			if (itemstack.getItemDamage() > itemstack.getMaxDamage())
				itemstack = null;
			
			if (itemstack != null && this.getEnchantmentList(itemstack) == null) {
				if (itemstack.getItem() == Items.enchanted_book)
					itemstack = new ItemStack(Items.book);
				if (this.isVoiding(world.getBlockMetadata(posX, posY, posZ)))
					itemstack = null;
			}
			inventory.setInventorySlotContents(0, itemstack);
			
			if (isAutomatic && outputBookstack.getTagCompound() != null && outputBookstack.getTagCompound().getTag("StoredEnchantments") != null)
				inventory.setInventorySlotContents(2, outputBookstack);

			world.playSoundEffect(posX, posY, posZ, "Disenchanter:block.disenchantment_table.use", isAutomatic ? 0.5F : 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
		}
	}
	
	protected void disenchant(ItemStack itemstack, ItemStack outputBookstack, boolean isAutomatic, World world, int posX, int posY, int posZ, Random random) {
		float power = this.getEnchantingPower(world, posX, posY, posZ);
		int flatDmg = DisenchanterMain.config.get("disenchanting", "FlatDamage", 10).getInt();
		double durabiltyDmg = DisenchanterMain.config.get("disenchanting", "MaxDurabilityDamage", 0.025).getDouble();
		double reduceableDmg = DisenchanterMain.config.get("disenchanting", "MaxDurabilityDamageReduceable", 0.2).getDouble();
		double machineDmgMultiplier = isAutomatic ? DisenchanterMain.config.get("disenchanting", "MachineDamageMultiplier", 2.5).getDouble() : 1.0;

		while (this.getEnchantmentList(itemstack) != null) {
			this.transferEnchantment(itemstack, outputBookstack, 0, random);
			
			itemstack.attemptDamageItem((int) (machineDmgMultiplier * (flatDmg + itemstack.getMaxDamage() * durabiltyDmg + 
					itemstack.getMaxDamage() * (reduceableDmg / power))), random);
			
			if (itemstack.getItemDamage() > itemstack.getMaxDamage() || 
					!(this.isBulkDisenchanting(world.getBlockMetadata(posX, posY, posZ))))
				break;
		}
	}
	
	public void transferEnchantment(ItemStack input, ItemStack output, int index, Random random) {
		if (input != null && output != null && input.getTagCompound() != null) {
			double enchantmentLoss = DisenchanterMain.config.get("disenchanting", "EnchantmentLossChance", 0.0).getDouble();
			
			NBTTagList enchants = this.getEnchantmentList(input);
			if (enchants == null)
				return;
			
			if (enchants.tagCount() > 0) {
				index = Math.min(Math.abs(index), enchants.tagCount() - 1);
				
				NBTTagCompound enchant = enchants.getCompoundTagAt(index);
				int id = enchant.getInteger("id");
				int lvl = enchant.getInteger("lvl");
				
				if (random.nextFloat() > enchantmentLoss)
					Items.enchanted_book.addEnchantment(output, new EnchantmentData(id, lvl));
				
				enchants.removeTag(index);
				input.setRepairCost(input.getRepairCost() / 2);
			}
			if (enchants.tagCount() <= 0)
				if (this.isEnchantmentStorage(input))
					input.getTagCompound().removeTag("StoredEnchantments");
				else
					input.getTagCompound().removeTag("ench");
		}
	}
	
	public NBTTagList getEnchantmentList(ItemStack itemstack) {
		if (itemstack == null || itemstack.getTagCompound() == null)
			return null;
		
		if (itemstack.getTagCompound().getTag("InfiTool") != null)
			if (DisenchanterMain.config.get("disenchanting", "EnableTCBehaviour", true).getBoolean())
				return null;
		if (itemstack.getTagCompound().getTag("TinkerData") != null)
			if (DisenchanterMain.config.get("disenchanting", "EnableTCBehaviour", true).getBoolean())
				return null;
		
		if (itemstack.getTagCompound().getTag("ench") != null)
			return (NBTTagList) itemstack.getTagCompound().getTag("ench");
		if (itemstack.getTagCompound().getTag("StoredEnchantments") != null)
			return (NBTTagList) itemstack.getTagCompound().getTag("StoredEnchantments");
		return null;
	}
	
	public boolean isEnchantmentStorage(ItemStack itemstack) {
		return itemstack.getTagCompound().getTag("StoredEnchantments") != null;
	}

}
