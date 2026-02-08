package nl.streats1.cobbledollarsvillagersoverhaul;

import com.mojang.logging.LogUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import nl.streats1.cobbledollarsvillagersoverhaul.integration.CobbleDollarsIntegration;
import nl.streats1.cobbledollarsvillagersoverhaul.integration.VillagerCobbleDollarsHandler;
import nl.streats1.cobbledollarsvillagersoverhaul.network.CobbleDollarsShopPayloadHandlers;
import nl.streats1.cobbledollarsvillagersoverhaul.network.CobbleDollarsShopPayloads;
import org.slf4j.Logger;

@Mod(CobbleDollarsVillagersOverhaulRca.MOD_ID)
public class CobbleDollarsVillagersOverhaulRca {
    public static final String MOD_ID = "cobbledollars_villagers_overhaul_rca";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CobbleDollarsVillagersOverhaulRca(IEventBus modEventBus, ModContainer modContainer) {
        CobbleDollarsShopPayloadHandlers.registerPayloads(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(VillagerCobbleDollarsHandler.class);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    /** When CobbleDollars shop UI is enabled, right-clicking a villager opens our shop screen instead of vanilla trading. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        handleVillagerShopInteract(event.getTarget(), event.getLevel().isClientSide(), () -> {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }, () -> event.getTarget().getId());
    }

    /** Fires before EntityInteract; needed so we cancel before vanilla opens the merchant GUI. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        handleVillagerShopInteract(event.getTarget(), event.getLevel().isClientSide(), () -> {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }, () -> event.getTarget().getId());
    }

    private void handleVillagerShopInteract(net.minecraft.world.entity.Entity target, boolean isClientSide,
            Runnable cancelAction, java.util.function.IntSupplier getId) {
        if (!Config.USE_COBBLEDOLLARS_SHOP_UI.get() || !CobbleDollarsIntegration.isModLoaded()) return;
        if (target instanceof Villager villager) {
            VillagerProfession prof = villager.getVillagerData().getProfession();
            if (prof == VillagerProfession.NONE || prof == VillagerProfession.NITWIT) return;
        } else if (!(target instanceof WanderingTrader)) {
            return;
        }

        cancelAction.run();
        if (isClientSide) {
            PacketDistributor.sendToServer(new CobbleDollarsShopPayloads.RequestShopData(getId.getAsInt()));
        }
    }
}
