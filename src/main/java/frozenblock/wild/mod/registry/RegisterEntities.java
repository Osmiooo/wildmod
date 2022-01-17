package frozenblock.wild.mod.registry;

import frozenblock.wild.mod.WildMod;
import frozenblock.wild.mod.entity.*;
import frozenblock.wild.mod.entity.chestboat.*;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

public class RegisterEntities {

    public static EntityType<MangroveBoatEntity> MANGROVE_BOAT;
    public static EntityType<ChestBoatEntity> CHEST_BOAT;
    public static final EntityType<WardenEntity> WARDEN = Registry.register(Registry.ENTITY_TYPE, new Identifier(WildMod.MOD_ID, "warden"), FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, WardenEntity::new).dimensions(EntityDimensions.fixed(1.5f, 2.95f)).build());
    public static final EntityType<FrogEntity> FROG = Registry.register(Registry.ENTITY_TYPE, new Identifier(WildMod.MOD_ID, "frog"), FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, FrogEntity::new).dimensions(EntityDimensions.fixed(0.75f, 0.75f)).build());
    public static final EntityType<TadpoleEntity> TADPOLE = Registry.register(Registry.ENTITY_TYPE, new Identifier(WildMod.MOD_ID, "tadpole"), FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, TadpoleEntity::new).dimensions(EntityDimensions.fixed(0.5F, 0.3F)).build());
    public static final EntityType<FireflyEntity> FIREFLY = Registry.register(Registry.ENTITY_TYPE, new Identifier(WildMod.MOD_ID, "firefly"), FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, FireflyEntity::new).dimensions(EntityDimensions.fixed(0.5F, 0.3F)).build());


    public static void RegisterEntities() {
        MANGROVE_BOAT = Registry.register(Registry.ENTITY_TYPE, new Identifier(WildMod.MOD_ID, "mangrove_boat"), FabricEntityTypeBuilder.<MangroveBoatEntity>create(SpawnGroup.CREATURE, MangroveBoatEntity::new).dimensions(EntityDimensions.fixed(1.375F, 0.5625F)).build());
        CHEST_BOAT = Registry.register(Registry.ENTITY_TYPE, new Identifier(WildMod.MOD_ID, "chest_boat"), FabricEntityTypeBuilder.<ChestBoatEntity>create(SpawnGroup.CREATURE, ChestBoatEntity::new).dimensions(EntityDimensions.fixed(1.375F, 0.5625F)).build());
        FabricDefaultAttributeRegistry.register(WARDEN, WardenEntity.createWardenAttributes());
        FabricDefaultAttributeRegistry.register(FROG, FrogEntity.createFrogAttributes());
        FabricDefaultAttributeRegistry.register(TADPOLE, TadpoleEntity.createTadpoleAttributes());
        FabricDefaultAttributeRegistry.register(FIREFLY, FireflyEntity.createFireflyAttributes());


        BiomeModifications.addSpawn(BiomeSelectors.categories(Biome.Category.SWAMP), FROG.getSpawnGroup(), RegisterEntities.FROG, 200, 3, 6);
    }
}
