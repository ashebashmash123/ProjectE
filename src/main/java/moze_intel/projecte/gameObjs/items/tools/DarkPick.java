package moze_intel.projecte.gameObjs.items.tools;

import com.google.common.collect.Multimap;
import moze_intel.projecte.gameObjs.EnumMatterType;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.utils.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class DarkPick extends PEToolBase
{
	public DarkPick(Builder builder)
	{
		super(builder, (byte)2, new String[] {
				"pe.darkpick.mode1", "pe.darkpick.mode2",
				"pe.darkpick.mode3", "pe.darkpick.mode4"});
		this.peToolMaterial = EnumMatterType.DARK_MATTER;
		this.harvestMaterials.add(Material.IRON);
		this.harvestMaterials.add(Material.ANVIL);
		this.harvestMaterials.add(Material.ROCK);
	}

	// Only for RedPick
	protected DarkPick(Builder builder, byte numCharges, String[] modeDesc)
	{
		super(builder, numCharges, modeDesc);
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if (world.isRemote)
		{
			return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
		}

		if (ProjectEConfig.items.pickaxeAoeVeinMining)
		{
			mineOreVeinsInAOE(stack, player, hand);
		}
		else
		{
			RayTraceResult mop = this.rayTrace(world, player, false);
			if (mop != null && mop.type == RayTraceResult.Type.BLOCK)
			{
				if (ItemHelper.isOre(world.getBlockState(mop.getBlockPos()).getBlock()))
				{
					tryVeinMine(stack, player, mop);
				}
			}
		}

		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public boolean onBlockDestroyed(ItemStack stack, World world, IBlockState state, BlockPos pos, EntityLivingBase eLiving)
	{
		digBasedOnMode(stack, world, state.getBlock(), pos, eLiving);
		return true;
	}

	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state)
	{
		Block block = state.getBlock();
		if (block == ObjHandler.dmBlock || block == ObjHandler.dmFurnaceOff)
		{
			return 1200000.0F;
		}
		
		return super.getDestroySpeed(stack, state);
	}
	
	@Nonnull
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EntityEquipmentSlot slot, ItemStack stack)
	{
		if (slot != EntityEquipmentSlot.MAINHAND) return super.getAttributeModifiers(slot, stack);
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
		multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", this instanceof RedPick ? 8 : 7, 0));
		multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", -2.8, 0));
		return multimap;
	}
}
