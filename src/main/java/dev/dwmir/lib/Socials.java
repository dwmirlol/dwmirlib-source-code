package dev.dwmir.lib;
public final class Socials {
 public static String url(String key){return switch(key){case "modrinth"->"https://modrinth.com/user/dwmirlol";case "github"->"https://github.com/dwmirlol";case "youtube"->"https://www.youtube.com/@dwmirlol";case "tiktok"->"https://www.tiktok.com/@dwmirlol";case "discord"->"https://discord.gg/VtVagpUtSf";default->throw new IllegalArgumentException(key);};}
 public static void open(String key){net.minecraft.util.Util.getOperatingSystem().open(java.net.URI.create(url(key)));}
}
