package thebombzen.mods.autoswitch;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import thebombzen.mods.autoswitch.configuration.Configuration;
import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class Tests {
	
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	private static ItemStack prevHeldItem = null;
	private static Random prevRandom = null;
	
	public static ItemStack createStackedBlock(Block block, int metadata) {
		return ThebombzenAPI.invokePrivateMethod(block, Block.class,
				new String[] { "createStackedBlock", "func_149644_j", "j" },
				new Class<?>[] { int.class }, metadata);
	}
	

	public static boolean doesFortuneWorkOnBlock(World world, int x, int y, int z) {

		Block block = world.getBlock(x, y, z);
		int metadata = world.getBlockMetadata(x, y, z);

		if (block == null) {
			return false;
		}

		int state = AutoSwitch.instance.getConfiguration().getFortuneOverrideState(block, metadata);
		if (state == Configuration.OVERRIDDEN_NO){
			return false;
		} else if (state == Configuration.OVERRIDDEN_YES){
			return true;
		}

		Random maxRandom = new NotSoRandom(false);
		Random zeroRandom = new NotSoRandom(true);

		List<ItemStack> defaultMaxRandom;
		List<ItemStack> defaultZeroRandom;
		List<ItemStack> fortuneMaxRandom;
		List<ItemStack> fortuneZeroRandom;

		fakeRandomForWorld(world, maxRandom);
		defaultMaxRandom = block.getDrops(world, x, y, z, metadata, 0);
		fortuneMaxRandom = block.getDrops(world, x, y, z, metadata, 3);
		unFakeRandomForWorld(world);

		fakeRandomForWorld(world, zeroRandom);
		defaultZeroRandom = block.getDrops(world, x, y, z, metadata, 0);
		fortuneZeroRandom = block.getDrops(world, x, y, z, metadata, 3);
		unFakeRandomForWorld(world);

		if (!ThebombzenAPI.areItemStackCollectionsEqual(defaultMaxRandom,
				fortuneMaxRandom)
				|| !ThebombzenAPI.areItemStackCollectionsEqual(
						defaultZeroRandom, fortuneZeroRandom)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean doesSilkTouchWorkOnBlock(World world, int x, int y, int z) {

		Block block = world.getBlock(x, y, z);
		int metadata = world.getBlockMetadata(x, y, z);

		if (block == null) {
			return false;
		}

		int state = AutoSwitch.instance.getConfiguration().getSilkTouchOverrideState(block, metadata);
		if (state == Configuration.OVERRIDDEN_NO){
			return false;
		} else if (state == Configuration.OVERRIDDEN_YES){
			return true;
		}

		boolean silkHarvest = block.canSilkHarvest(world, mc.thePlayer, x, y, z, metadata);
		if (!silkHarvest){
			return false;
		}
		
		Random zeroRandom = new NotSoRandom(true);
		Random maxRandom = new NotSoRandom(false);
		
		ItemStack stackedBlock = createStackedBlock(block, metadata);
		List<ItemStack> stackedBlockList = Collections.singletonList(stackedBlock);
		
		List<ItemStack> defaultMaxRandom;
		List<ItemStack> defaultZeroRandom;
		
		fakeRandomForWorld(world, maxRandom);
		defaultMaxRandom = block.getDrops(world, x, y, z, metadata, 0);
		unFakeRandomForWorld(world);

		fakeRandomForWorld(world, zeroRandom);
		defaultZeroRandom = block.getDrops(world, x, y, z, metadata, 0);
		unFakeRandomForWorld(world);
		
		if (!ThebombzenAPI.areItemStackCollectionsEqual(stackedBlockList, defaultMaxRandom) || !ThebombzenAPI.areItemStackCollectionsEqual(stackedBlockList, defaultZeroRandom)){
			return true;
		} else {
			return false;
		}
	}

	private static void fakeItemForPlayer(ItemStack itemstack) {
		prevHeldItem = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem];
		mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = itemstack;
		if (prevHeldItem != null) {
			mc.thePlayer.getAttributeMap().removeAttributeModifiers(prevHeldItem.getAttributeModifiers());
		}
		if (itemstack != null) {
			mc.thePlayer.getAttributeMap().applyAttributeModifiers(
					itemstack.getAttributeModifiers());
		}
	}

	private static void fakeRandomForWorld(World world, Random random) {
		prevRandom = world.rand;
		world.rand = random;
	}

	public static int getAdjustedBlockStr(double blockStr){
		if (blockStr <= 0){
			return Integer.MIN_VALUE;
		} else {
			return -MathHelper.ceiling_double_int(1D / blockStr);
		}
	}
	
	public static float getBlockHardness(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		if (block == null) {
			return 0;
		} else {
			// func_149712_f == getBlockHardness
			return block.getBlockHardness(world, x, y, z);
		}
	}

	public static float getBlockStrength(ItemStack itemstack, World world, int x,
			int y, int z) {
		Block block = world.getBlock(x, y, z);
		fakeItemForPlayer(itemstack);
		float str = block.getPlayerRelativeBlockHardness(mc.thePlayer, world, x, y, z);
		unFakeItemForPlayer();
		return str;
	}
	
	public static float getDigSpeed(ItemStack itemstack, Block block, int metadata) {
		return itemstack == null ? 1.0F : itemstack.getItem().getDigSpeed(
				itemstack, block, metadata);
	}
	
	public static float getEff(float str, ItemStack itemstack) {
		if (str <= 1.5F) {
			return str;
		}
		fakeItemForPlayer(itemstack);
		float effLevel = EnchantmentHelper.getEfficiencyModifier(mc.thePlayer);
		unFakeItemForPlayer();
		if (effLevel == 0) {
			return str;
		}
		return str + effLevel * effLevel + 1;
	}
	

	public static float getEnchantmentModifierLiving(ItemStack itemstack,
			EntityLivingBase entityover) {
		fakeItemForPlayer(itemstack);
		float modifier = EnchantmentHelper.getEnchantmentModifierLiving(
				mc.thePlayer, entityover);
		unFakeItemForPlayer();
		return modifier;
	}
	
	public static int getHarvestLevel(ItemStack itemstack, Block block, int metadata) {
		int state = AutoSwitch.instance.getConfiguration().getHarvestOverrideState(itemstack, block, metadata);
		if (state == Configuration.OVERRIDDEN_NO){
			return -2;
		} else if (state == Configuration.OVERRIDDEN_YES){
			return 2;
		}
		fakeItemForPlayer(null);
		boolean noTool = mc.thePlayer.canHarvestBlock(block);
		unFakeItemForPlayer();
		if (noTool){
			return 0;
		}
		fakeItemForPlayer(itemstack);
		boolean can = block.canHarvestBlock(mc.thePlayer, metadata);
		unFakeItemForPlayer();
		return can ? 1 : -1;
	}
	
	public static double getItemStackDamage(ItemStack itemStack){
		double damage = AutoSwitch.instance.getConfiguration().getCustomWeaponDamage(itemStack);
		if (damage >= 0){
			return damage;
		}
		fakeItemForPlayer(itemStack);
		damage = mc.thePlayer.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
		unFakeItemForPlayer();
		return damage;
	}

	@SuppressWarnings("unchecked")
	public static Set<Enchantment> getNonstandardNondamageEnchantmentsOnBothStacks(
			ItemStack stack1, ItemStack stack2) {
		Set<Integer> bothItemsEnchantments = new HashSet<Integer>();
		Set<Enchantment> ret = new HashSet<Enchantment>();

		if (stack1 != null) {
			bothItemsEnchantments.addAll(EnchantmentHelper.getEnchantments(
					stack1).keySet());
		}
		if (stack2 != null) {
			bothItemsEnchantments.addAll(EnchantmentHelper.getEnchantments(
					stack2).keySet());
		}

		Iterator<Integer> bothItemsEnchantmentsIterator = bothItemsEnchantments
				.iterator();
		while (bothItemsEnchantmentsIterator.hasNext()) {
			Integer effectId = bothItemsEnchantmentsIterator.next();

			if (effectId == Enchantment.efficiency.effectId
					|| effectId == Enchantment.silkTouch.effectId
					|| effectId == Enchantment.fortune.effectId
					|| effectId == Enchantment.unbreaking.effectId
					|| effectId == Enchantment.looting.effectId
					|| effectId == Enchantment.knockback.effectId
					|| effectId == Enchantment.fireAspect.effectId
					|| effectId == Enchantment.field_151369_A.effectId
					|| effectId == Enchantment.field_151370_z.effectId) {
				continue;
			}

			if (Enchantment.enchantmentsList[effectId].getName().startsWith(
					"enchantment.damage.")) {
				continue;
			}

			ret.add(Enchantment.enchantmentsList[effectId]);

		}

		return ret;
	}
	
	public static int getStrongToolStandardness(ItemStack itemstack, World world, int x,
			int y, int z) {
		Block block = world.getBlock(x, y, z);
		int metadata = world.getBlockMetadata(x, y, z);
		int state = AutoSwitch.instance.getConfiguration().getStandardToolOverrideState(itemstack, block, metadata);
		if (state == Configuration.OVERRIDDEN_NO){
			return -3;
		} else if (state == Configuration.OVERRIDDEN_YES){
			return 3;
		}
		return Tests.getHarvestLevel(itemstack, block, metadata);
	}
	
	public static int getToolStandardness(ItemStack itemstack, World world, int x, int y, int z){
		return Tests.getStrongToolStandardness(itemstack, world, x, y, z) * 3 + Tests.getWeakToolStandardness(itemstack, world, x, y, z);
	}

	public static int getWeakToolStandardness(ItemStack itemstack, World world, int x, int y, int z){
		Block block = world.getBlock(x, y, z);
		int metadata = world.getBlockMetadata(x, y, z);
		float hardness = Tests.getBlockHardness(world, x, y, z);
		if (hardness <= 0F){
			return 0;
		}
		
		float blockStrForNull = Tests.getBlockStrength(null, world, x, y, z);
		fakeItemForPlayer(null);
		boolean harvestable = mc.thePlayer.canHarvestBlock(block);
		unFakeItemForPlayer();
		
		float blockStr = Tests.getBlockStrength(itemstack, world, x, y, z);
		fakeItemForPlayer(itemstack);
		boolean harvest = block.canHarvestBlock(mc.thePlayer, metadata);
		unFakeItemForPlayer();
		
		if (harvest && !harvestable){
			blockStr *= 0.3F;
		}
		
		if (blockStr > blockStrForNull * 1.5F){
			return 1;
		} else if (Tests.isItemStackDamageableOnBlock(itemstack, world, x, y, z)) {
			return -1;
		} else {
			return 0;
		}
	}

	public static boolean isItemStackDamageable(ItemStack itemstack) {
		return itemstack != null && itemstack.getItem().isDamageable();
	}

	public static boolean isItemStackDamageableOnBlock(ItemStack itemstack,
			World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		int metadata = world.getBlockMetadata(x, y, z);
		int state = AutoSwitch.instance.getConfiguration().getDamageableOverrideState(itemstack, block, metadata);
		if (state == Configuration.OVERRIDDEN_NO){
			return false;
		} else if (state == Configuration.OVERRIDDEN_YES){
			return true;
		}
		if (!isItemStackDamageable(itemstack)) {
			return false;
		}
		return getBlockHardness(world, x, y, z) != 0.0F;
	}

	public static boolean isSword(ItemStack itemstack) {
		if (itemstack == null){
			return false;
		}
		if (itemstack.getItem() instanceof ItemSword){
			return true;
		}
		if (GameRegistry.findUniqueIdentifierFor(itemstack.getItem()).name.toLowerCase().contains("_sword")){
			return true;
		}
		return false;
	}

	private static void unFakeItemForPlayer() {
		ItemStack fakedStack = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem];
		mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = prevHeldItem;
		if (fakedStack != null) {
			mc.thePlayer.getAttributeMap().removeAttributeModifiers(
					fakedStack.getAttributeModifiers());
		}
		if (prevHeldItem != null) {
			mc.thePlayer.getAttributeMap().applyAttributeModifiers(
					prevHeldItem.getAttributeModifiers());
		}
	}
	
	private static void unFakeRandomForWorld(World world) {
		world.rand = prevRandom;
		prevRandom = null;
	}
	
	private Tests() {
		
	}
}
