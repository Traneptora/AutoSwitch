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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import thebombzen.mods.autoswitch.configuration.Configuration;
import thebombzen.mods.thebombzenapi.ThebombzenAPI;
import cpw.mods.fml.common.Loader;
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
			mc.thePlayer.getAttributeMap().applyAttributeModifiers(itemstack.getAttributeModifiers());
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
	
	public static int getFullTinkersConstructItemStackDamage(ItemStack stack,
			EntityLivingBase entity) throws ClassNotFoundException {
		NBTTagCompound tags = stack.getTagCompound();
		NBTTagCompound toolTags = stack.getTagCompound().getCompoundTag(
				"InfiTool");
		int damage = toolTags.getInteger("Attack");
		boolean broken = toolTags.getBoolean("Broken");
		int durability = tags.getCompoundTag("InfiTool").getInteger("Damage");
		float stonebound = tags.getCompoundTag("InfiTool").getFloat("Shoddy");
		float stoneboundDamage = (float) Math.log(durability / 72f + 1) * -2
				* stonebound;
		int earlyModDamage = 0;
		Iterable<?> activeModifiers = ThebombzenAPI.getPrivateField(null,
				Class.forName("tconstruct.library.TConstructRegistry"),
				"activeModifiers");
		Class<?> activeToolModClass = Class
				.forName("tconstruct.library.ActiveToolMod");
		Class<?> toolCoreClass = Class
				.forName("tconstruct.library.tools.ToolCore");
		Class<?>[] attackModClasses = new Class<?>[] { int.class, int.class,
				toolCoreClass, NBTTagCompound.class, NBTTagCompound.class,
				ItemStack.class, EntityLivingBase.class, Entity.class };
		Item tool = stack.getItem();
		for (Object activeToolMod : activeModifiers) {
			earlyModDamage = ThebombzenAPI.invokePrivateMethod(activeToolMod,
					activeToolModClass, "baseAttackDamage", attackModClasses,
					earlyModDamage, damage, tool, tags, toolTags, stack,
					mc.thePlayer, entity);
		}
		damage += earlyModDamage;
		if (mc.thePlayer.isPotionActive(Potion.damageBoost)) {
			damage += 3 << mc.thePlayer.getActivePotionEffect(
					Potion.damageBoost).getAmplifier();
		}
		if (mc.thePlayer.isPotionActive(Potion.weakness)) {
			damage -= 2 << mc.thePlayer.getActivePotionEffect(Potion.weakness)
					.getAmplifier();
		}
		float enchantDamage = 0;
		if (entity instanceof EntityLivingBase) {
			enchantDamage = EnchantmentHelper.getEnchantmentModifierLiving(
					mc.thePlayer, entity);
		}
		damage += stoneboundDamage;
		if (damage < 1) {
			damage = 1;
		}
		if (mc.thePlayer.isSprinting()) {
			float lunge = ThebombzenAPI.invokePrivateMethod(tool,
					toolCoreClass, "chargeAttack", new Class<?>[] {});
			if (lunge > 1f) {
				damage *= lunge;
			}
		}
		int modDamage = 0;
		for (Object activeToolMod : activeModifiers) {
			modDamage = ThebombzenAPI.invokePrivateMethod(activeToolMod,
					activeToolModClass, "attackDamage", attackModClasses,
					modDamage, damage, tool, tags, toolTags, stack,
					mc.thePlayer, entity);
		}
		damage += modDamage;
		if (damage > 0 || enchantDamage > 0) {
			boolean criticalHit = mc.thePlayer.fallDistance > 0.0F
					&& !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder()
					&& !mc.thePlayer.isInWater()
					&& !mc.thePlayer.isPotionActive(Potion.blindness)
					&& mc.thePlayer.ridingEntity == null;
			for (Object activeToolMod : activeModifiers) {
				if (ThebombzenAPI.invokePrivateMethod(activeToolMod,
						activeToolModClass, "doesCriticalHit", new Class<?>[] {
								toolCoreClass, NBTTagCompound.class,
								NBTTagCompound.class, ItemStack.class,
								EntityLivingBase.class, Entity.class }, tool,
						tags, toolTags, stack, mc.thePlayer, entity))
					;
				criticalHit = true;
			}
			if (criticalHit) {
				damage *= 1.5F; // This is not actually accurate. It just makes
								// it fully objective. Also, this is how it is
								// in vanilla now.
			}
			damage += enchantDamage;
			float damageModifier = ThebombzenAPI.invokePrivateMethod(tool,
					toolCoreClass, "getDamageModifier", new Class<?>[] {});
			if (damageModifier != 1f) {
				damage *= damageModifier;
			}
			if (broken) {
				damage = 1;
			}
			return damage;
		} else {
			return 0;
		}
	}
	
	public static double getFullRegularItemStackDamage(ItemStack itemStack,
			EntityLivingBase entity) {
		fakeItemForPlayer(itemStack);
		double damage = AutoSwitch.instance.getConfiguration()
				.getCustomWeaponDamage(itemStack);
		if (damage < 0) {
			damage = mc.thePlayer.getEntityAttribute(
					SharedMonsterAttributes.attackDamage).getAttributeValue();
		}
		double enchDamage = EnchantmentHelper.getEnchantmentModifierLiving(
				mc.thePlayer, entity);

		if (damage > 0.0D || enchDamage > 0.0D) {
			boolean critical = mc.thePlayer.fallDistance > 0.0F
					&& !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder()
					&& !mc.thePlayer.isInWater()
					&& !mc.thePlayer.isPotionActive(Potion.blindness)
					&& mc.thePlayer.ridingEntity == null;

			if (critical && damage > 0) {

				damage *= 1.5D;
			}
			damage += enchDamage;
		}
		unFakeItemForPlayer();
		return damage;
	}
	
	public static double getFullItemStackDamage(ItemStack itemStack, EntityLivingBase entity){
		try {
			if (itemStack != null && itemStack.getTagCompound() != null && Loader.isModLoaded("TConstruct")){
				Class<?> clazz = Class.forName("tconstruct.library.tools.ToolCore");
				if (clazz.isAssignableFrom(itemStack.getItem().getClass())){
					return Tests.getFullTinkersConstructItemStackDamage(itemStack, entity);
				}
			}
		} catch (ClassNotFoundException e){
			AutoSwitch.instance.throwException("Error in Tinkers Construct Compatability", e, false);
		}
		return Tests.getFullRegularItemStackDamage(itemStack, entity);
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
		String name = GameRegistry.findUniqueIdentifierFor(itemstack.getItem()).name.toLowerCase();
		if (name.endsWith("sword")){
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
