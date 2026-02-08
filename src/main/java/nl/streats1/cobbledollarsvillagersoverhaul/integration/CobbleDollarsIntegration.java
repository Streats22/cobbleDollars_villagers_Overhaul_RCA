package nl.streats1.cobbledollarsvillagersoverhaul.integration;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import nl.streats1.cobbledollarsvillagersoverhaul.CobbleDollarsVillagersOverhaulRca;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.math.BigInteger;

/**
 * Optional integration with CobbleDollars mod.
 * Uses reflection so CobbleDollars is not required at compile time.
 */
public final class CobbleDollarsIntegration {

    private static final String COBBLEDOLLARS_MOD_ID = "cobbledollars";

    private static Boolean modLoaded = null;
    @Nullable
    private static MethodHandle getBalanceHandle = null;
    @Nullable
    private static MethodHandle setBalanceHandle = null;
    private static boolean reflectionResolved = false;

    private CobbleDollarsIntegration() {}

    public static boolean isModLoaded() {
        if (modLoaded == null) {
            try {
                modLoaded = net.neoforged.fml.ModList.get().isLoaded(COBBLEDOLLARS_MOD_ID);
            } catch (Throwable t) {
                modLoaded = false;
            }
        }
        return modLoaded;
    }

    private static void resolveReflection() {
        if (reflectionResolved) return;
        reflectionResolved = true;
        if (!isModLoaded()) return;

        String[] possibleClassNames = {
                "fr.harmex.cobbledollars.common.utils.extensions.PlayerExtensionKt",
                "fr.harmex.cobbledollars.api.CobbleDollarsAPI",
                "fr.harmex.cobbledollars.common.CobbleDollars",
                "fr.harmex.cobbledollars.common.utils.MoneyUtil",
                "com.harmex.cobbledollars.CobbleDollars",
                "com.harmex.cobbledollars.api.CobbleDollarsAPI"
        };

        for (String className : possibleClassNames) {
            try {
                Class<?> apiClass = Class.forName(className);
                Class<?> bigIntegerClass = BigInteger.class;
                for (Method m : apiClass.getMethods()) {
                    if (java.lang.reflect.Modifier.isStatic(m.getModifiers()) && m.getParameterCount() >= 1) {
                        String name = m.getName();
                        Class<?>[] params = m.getParameterTypes();
                        if ((name.equals("getCobbleDollars") || name.equals("getBalance") || name.equals("get")) && params.length == 1) {
                            if (Player.class.isAssignableFrom(params[0]) && (m.getReturnType() == long.class || m.getReturnType() == int.class || m.getReturnType() == bigIntegerClass)) {
                                getBalanceHandle = MethodHandles.publicLookup().unreflect(m);
                            }
                        }
                        if ((name.equals("setCobbleDollars") || name.equals("setBalance") || name.equals("set")) && params.length == 2) {
                            if (Player.class.isAssignableFrom(params[0]) && (params[1] == long.class || params[1] == int.class || params[1] == bigIntegerClass)) {
                                setBalanceHandle = MethodHandles.publicLookup().unreflect(m);
                            }
                        }
                    }
                }
                if (getBalanceHandle != null && setBalanceHandle != null) break;
            } catch (Throwable ignored) {
            }
        }

        if (getBalanceHandle == null || setBalanceHandle == null) {
            CobbleDollarsVillagersOverhaulRca.LOGGER.debug("CobbleDollars mod is loaded but balance API could not be resolved. Villager CobbleDollars payment will be disabled.");
        }
    }

    public static long getBalance(Player player) {
        if (!isModLoaded() || !(player instanceof ServerPlayer)) return -1;
        resolveReflection();
        if (getBalanceHandle == null) return -1;
        try {
            Object result = getBalanceHandle.invoke(player);
            if (result instanceof BigInteger bi) {
                if (bi.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) return Long.MAX_VALUE;
                return bi.longValue();
            }
            if (result instanceof Number n) return n.longValue();
            return -1;
        } catch (Throwable t) {
            CobbleDollarsVillagersOverhaulRca.LOGGER.debug("CobbleDollars getBalance failed", t);
            return -1;
        }
    }

    public static boolean addBalance(Player player, long amount) {
        if (!isModLoaded() || !(player instanceof ServerPlayer)) return false;
        resolveReflection();
        if (getBalanceHandle == null || setBalanceHandle == null) return false;
        try {
            Object current = getBalanceHandle.invoke(player);
            BigInteger currentBi = current instanceof BigInteger ? (BigInteger) current : BigInteger.valueOf(((Number) current).longValue());
            BigInteger newBalance = currentBi.add(BigInteger.valueOf(amount));
            if (newBalance.signum() < 0) return false;
            Class<?> secondParam = setBalanceHandle.type().parameterType(1);
            if (secondParam == BigInteger.class) {
                setBalanceHandle.invoke(player, newBalance);
            } else {
                long clamped = newBalance.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0 ? Long.MAX_VALUE : newBalance.longValue();
                setBalanceHandle.invoke(player, secondParam == long.class ? clamped : (int) clamped);
            }
            return true;
        } catch (Throwable t) {
            CobbleDollarsVillagersOverhaulRca.LOGGER.debug("CobbleDollars addBalance failed", t);
            return false;
        }
    }

    public static boolean isAvailable() {
        if (!isModLoaded()) return false;
        resolveReflection();
        return getBalanceHandle != null && setBalanceHandle != null;
    }
}
