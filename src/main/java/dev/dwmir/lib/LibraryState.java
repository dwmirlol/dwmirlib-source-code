package dev.dwmir.lib;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.loader.api.FabricLoader;
public final class LibraryState {
 public enum Theme { ONYX,INFERNO,SAKURA,FROST,QUARTZ }
 public static Theme theme=Theme.ONYX;
 public static boolean tooltips=true;
 public static void toggleTooltips(){tooltips=!tooltips;save();}
 private static final Set<String> accepted=new HashSet<>();
 private static boolean loaded;
 private static Path file(){return FabricLoader.getInstance().getConfigDir().resolve("dwmirlib.json");}
 public static void load(){if(loaded)return;loaded=true;try{if(Files.exists(file())){var d=JsonParser.parseString(Files.readString(file())).getAsJsonObject();theme=Theme.valueOf(d.get("theme").getAsString());if(d.has("tooltips"))tooltips=d.get("tooltips").getAsBoolean();if(d.has("acknowledgedV1"))for(var a:d.getAsJsonArray("acknowledgedV1"))accepted.add(a.getAsString());}}catch(Exception e){org.slf4j.LoggerFactory.getLogger("dwmirlib").warn("Could not read library preferences",e);}}
 public static boolean accepted(String id){load();return accepted.contains(id);}
 public static void accept(String id){accepted.add(id);save();}
 public static void select(Theme t){theme=t;save();}
 private static void save(){try{var d=new JsonObject();d.addProperty("theme",theme.name());d.addProperty("tooltips",tooltips);var a=new JsonArray();accepted.stream().sorted().forEach(a::add);d.add("acknowledgedV1",a);Files.createDirectories(file().getParent());Path temp=file().resolveSibling("dwmirlib.json.tmp");Files.writeString(temp,new GsonBuilder().setPrettyPrinting().create().toJson(d));try{Files.move(temp,file(),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(temp,file(),StandardCopyOption.REPLACE_EXISTING);}}catch(Exception e){org.slf4j.LoggerFactory.getLogger("dwmirlib").error("Could not save library preferences",e);}}
}
