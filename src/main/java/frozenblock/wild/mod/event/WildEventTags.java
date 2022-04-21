package frozenblock.wild.mod.event;

import frozenblock.wild.mod.WildMod;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.event.GameEvent;

public class WildEventTags {
    public static final TagKey<GameEvent> WARDEN_CAN_LISTEN = of("warden_can_listen");
    public static final TagKey<GameEvent> SHRIEKER_CAN_LISTEN = of("shrieker_can_listen");
    public static final TagKey<GameEvent> DAMPENABLE_VIBRATIONS = of("dampenable_vibrations");

    private WildEventTags() {
    }

    private static TagKey<net.minecraft.world.event.GameEvent> of(String id) {
        return TagKey.of(Registry.GAME_EVENT_KEY, new Identifier(WildMod.MOD_ID, id));
    }
}