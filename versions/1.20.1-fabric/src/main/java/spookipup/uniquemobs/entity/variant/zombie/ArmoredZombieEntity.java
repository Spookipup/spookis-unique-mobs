package spookipup.uniquemobs.entity.variant.zombie;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ArmoredZombieEntity extends Zombie {

	public ArmoredZombieEntity(EntityType<? extends Zombie> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Zombie.createAttributes()
			.add(Attributes.MAX_HEALTH, 26.0)
			.add(Attributes.MOVEMENT_SPEED, 0.2)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
			.add(Attributes.ARMOR, 8.0);
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
		this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
		this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
		this.setDropChance(EquipmentSlot.HEAD, 0.02F);
		this.setDropChance(EquipmentSlot.CHEST, 0.02F);
	}

	@Override
	protected void populateDefaultEquipmentEnchantments(RandomSource random, DifficultyInstance difficulty) {
		// no enchantments
	}
}
