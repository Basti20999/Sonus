package dev.minceraft.sonus.service.commands.builtin;

import dev.minceraft.sonus.common.adapter.SonusAdapter;
import dev.minceraft.sonus.service.SonusService;
import dev.minceraft.sonus.service.commands.Command;
import dev.minceraft.sonus.service.commands.LiteralCommandNode;
import dev.minceraft.sonus.service.player.SonusPlayer;
import dev.minceraft.sonus.web.adapter.WebAdapter;
import dev.minceraft.sonus.web.adapter.config.WebConfig;
import org.jspecify.annotations.NullMarked;

import static dev.minceraft.sonus.service.commands.CommandNode.literal;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.event.ClickEvent.openUrl;

@NullMarked
public class SonusCommand extends Command {

    public SonusCommand() {
        super("sonus", "audio", "voice", "voicechat");
    }

    @Override
    public LiteralCommandNode construct() {
        return literal(this.getLabel())
                .requires(ctx -> ctx.sender().hasPermission("sonus.command", true)
                        && ctx.sender() instanceof SonusPlayer)
                .executes(ctx -> this.execute(ctx.service(), (SonusPlayer) ctx.sender()))
                .with(new GroupCommand().construct());
    }

    private boolean execute(SonusService service, SonusPlayer player) {
        SonusAdapter adapter = player.getAdapter();
        if (adapter != null && player.isConnected()) {
            player.sendMessage(translatable("sonus.command.sonus.connected-info")
                    .arguments(translatable("sonus.adapter." + adapter.getAdapterInfo().id() + ".name")));
            return true; // already connecting/connected
        }
        WebAdapter webAdapter = service.getAdapters().getAdapter(WebAdapter.class);
        if (webAdapter == null || !webAdapter.getAdapterInfo().enabled()) {
            return false;
        }

        String linkPattern = service.getConfig().getSubConfig(WebConfig.class).linkPattern;
        String token = webAdapter.getSessions().generateToken(player);
        String url = String.format(linkPattern, token);

        player.sendMessage(translatable("sonus.command.sonus.web.token")
                .clickEvent(openUrl(url))
                .arguments(text(url)));
        return true;
    }
}
