package br.com.minecraft2.test;

import com.exoticworlds.core.WorldLoopAttachments;
import com.exoticworlds.engine.net.PacketTranslator;
import com.exoticworlds.engine.net.ListenerCopies;
import com.exoticworlds.engine.seam.ClientPosition;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Regressão com classes reais transformadas, fora do JAR de produção.
 * Altera câmera por reflexão somente dentro do comando, restaurando-a em finally.
 * Não envia as câmeras artificiais ao cliente nem toca em mundos pessoais.
 */
@Mod("m2_camera_tests")
public final class CameraAudit {
    public CameraAudit(IEventBus bus) { NeoForge.EVENT_BUS.addListener(CameraAudit::commands); }

    private static void commands(RegisterCommandsEvent event) {
        if (!Files.isRegularFile(Path.of("m2-disposable-client.json"))) return;
        event.getDispatcher().register(Commands.literal("m2cameraaudit").requires(s -> s.hasPermission(2))
                .executes(c -> run(c.getSource().getPlayerOrException())));
    }

    private static void check(JsonArray checks, String label, boolean passed) {
        if (!passed) throw new AssertionError(label);
        checks.add(label);
    }

    private static int run(ServerPlayer player) {
        JsonObject report = new JsonObject(); JsonArray checks = new JsonArray();
        report.add("checks", checks);
        report.addProperty("dimension", player.level().dimension().location().toString());
        var position = ClientPosition.of(player);
        var original = player.getCamera();
        try {
            var camera = ServerPlayer.class.getDeclaredField("camera"); camera.setAccessible(true);
            var fold = WorldLoopAttachments.foldOf(player.level());
            var seated = fold.fold(player.position());
            position.rebase(seated.x, seated.z, player.level().dimension(), player.level(), fold);
            camera.set(player, null);
            position.followCamera(player);
            check(checks, "null usa ancora do jogador", position.cameraChunk() == null && position.anchor(0).x == seated.x && position.anchor(0).z == seated.z);
            var packet = PacketTranslator.toClient(new ClientboundSetDefaultSpawnPositionPacket(BlockPos.containing(player.position()), 0), player);
            check(checks, "pacote traduzido com camera null continua", packet instanceof ClientboundSetDefaultSpawnPositionPacket);
            var source = seated.add(48, 0, 48);
            check(checks, "som com camera null usa jogador", ListenerCopies.nearestTo(fold, java.util.List.of(player), p -> true, source).equals(java.util.List.of(fold.nearestCopy(player.position(), source))));
            check(checks, "filtro de ouvintes preservado", ListenerCopies.nearestTo(fold, java.util.List.of(player), p -> false, source).equals(java.util.List.of(source)));
            camera.set(player, player); position.followCamera(player);
            check(checks, "camera padrao preservada", position.cameraChunk() == null);
            var other = new ArmorStand(player.serverLevel(), seated.x + 32, player.getY(), seated.z + 32);
            camera.set(player, other); position.followCamera(player);
            check(checks, "outra entidade acompanha copia toroidal", position.cameraChunk() != null && position.anchor(0).x == seated.x + 32 && position.anchor(0).z == seated.z + 32);
            check(checks, "som acompanha outra entidade", ListenerCopies.nearestTo(fold, java.util.List.of(player), p -> true, source).equals(java.util.List.of(fold.nearestCopy(other.position(), source))));
            camera.set(player, null); position.followCamera(player);
            check(checks, "sair da camera retorna ancora do jogador", position.cameraChunk() == null && position.anchor(0).x == seated.x);
            position.dropLeftCamera();
            check(checks, "limpeza da camera encerrada", position.seatChunk().equals(position.chunk()));
            var foreignLevel = player.server.getLevel(player.level().dimension().equals(Level.NETHER) ? Level.OVERWORLD : Level.NETHER);
            camera.set(player, new ArmorStand(foreignLevel, 32, 80, 32)); position.followCamera(player);
            check(checks, "camera em outra dimensao ignorada", position.cameraChunk() == null && position.anchor(0).x == seated.x);
            position.rebase(seated.x, seated.z, player.level().dimension(), player.level(), fold);
            camera.set(player, null); position.followCamera(player);
            check(checks, "rebase e camera null preservam dimensao", position.describes(player.level().dimension()) && position.cameraChunk() == null);
            report.addProperty("passed", true);
        } catch (Throwable error) {
            report.addProperty("passed", false); report.addProperty("error", error.toString()); error.printStackTrace();
        } finally {
            try { var field = ServerPlayer.class.getDeclaredField("camera"); field.setAccessible(true); field.set(player, original); ClientPosition.rebase(player); }
            catch (Exception error) { report.addProperty("restoreError", error.toString()); report.addProperty("passed", false); }
        }
        try {
            Files.writeString(Path.of("m2-camera-audit-" + System.currentTimeMillis() + ".json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch (Exception error) { throw new RuntimeException(error); }
        System.out.println("[M2 CAMERA] " + report);
        player.sendSystemMessage(Component.literal("[M2 CAMERA] " + report));
        return report.get("passed").getAsBoolean() ? 1 : 0;
    }
}
