package dev.minceraft.sonus.service.commands.builtin;

import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.commands.Command;
import dev.minceraft.sonus.service.commands.LiteralCommandNode;
import dev.minceraft.sonus.service.player.SonusPlayer;
import dev.minceraft.sonus.web.adapter.WebAdapter;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

import static dev.minceraft.sonus.service.commands.CommandNode.literal;

@NullMarked
public class SonusCommand extends Command {

    public SonusCommand() {
        super("sonus");
    }

    @Override
    public LiteralCommandNode construct() {
        return literal(this.getLabel())
                .requires(ctx ->
                        ctx.sender().hasPermission("") &&
                                ctx.sender() instanceof SonusPlayer player &&
                                player.getAdapter() == null)
                .executes(ctx -> this.execute(ctx.service(), (SonusPlayer) ctx.sender()));
    }

    private boolean execute(SonusService service, SonusPlayer player) {
        WebAdapter adapter = service.getAdapters().getAdapter(WebAdapter.class);
        if (adapter == null) {
            return false;
        }
        String token = adapter.getSessions().generateToken(player);

        player.sendMessage(Component.translatable("sonus.command.sonus.web.token").arguments(
                Component.text(token)
        ));

        return true;
    }
}
