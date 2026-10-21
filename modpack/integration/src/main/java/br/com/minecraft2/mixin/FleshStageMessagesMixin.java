package br.com.minecraft2.mixin;
import br.com.minecraft2.gameplay.GameplayRules;
import java.util.List;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Pseudo
@Mixin(targets="net.mcreator.thefleshthathates.FEvents.FleshWorld", remap=false)
public abstract class FleshStageMessagesMixin {
    @Redirect(method={"playSoundForStageChange", "sendServerMessage"}, at=@At(value="INVOKE", target="Lnet/minecraft/server/players/PlayerList;getPlayers()Ljava/util/List;"))
    private static List<ServerPlayer> m2$audience(PlayerList players) {
        return players.getPlayers().stream().filter(p -> GameplayRules.fleshAllowed(p.level())).toList();
    }
}
