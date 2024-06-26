package llc.redstone.hysentials.command;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class VisitPlayerCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "visitplayer";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/visitplayer <player>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1) return;
        String player = args[0];
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/visit " + player);
    }
}
