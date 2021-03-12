package danix25.skywars.utility;
import org.bukkit.ChatColor;

public class ChatUtil {

    public static String format(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

}
