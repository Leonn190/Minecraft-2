import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;

/** Checa os intervalos declarados usando o comparador Maven usado pelo ecossistema FML. */
class CheckModVersions {
    static void flatten(JsonArray entries, List<JsonObject> out) {
        for (var value : entries) {var entry=value.getAsJsonObject();out.add(entry);flatten(entry.getAsJsonArray("children"),out);}
    }
    public static void main(String[] args) throws Exception {
        var path=Path.of(args[0]);var report=JsonParser.parseString(Files.readString(path)).getAsJsonObject();
        List<JsonObject> entries=new ArrayList<>();flatten(report.getAsJsonArray("entries"),entries);
        Map<String,DefaultArtifactVersion> versions=new TreeMap<>();
        for (var entry:entries) for (var m:entry.getAsJsonArray("mods")) {
            var mod=m.getAsJsonObject();var id=mod.get("modId").getAsString();var v=new DefaultArtifactVersion(mod.get("version").getAsString());
            versions.merge(id,v,(a,b)->a.compareTo(b)>=0?a:b);
        }
        versions.put("minecraft",new DefaultArtifactVersion("1.21.1"));versions.put("neoforge",new DefaultArtifactVersion("21.1.263"));
        JsonArray conflicts=new JsonArray();
        for (var entry:entries) {
            if (entry.get("loader").isJsonNull() || entry.get("loader").getAsString().equals("fabric")) continue;
            for (var deps:entry.getAsJsonObject("dependencies").entrySet()) {
                if (!deps.getValue().isJsonArray()) continue;
                for (var v:deps.getValue().getAsJsonArray()) {
                    var dep=v.getAsJsonObject();String id=dep.get("modId").getAsString();
                    if (!versions.containsKey(id)||!dep.has("versionRange")) continue;
                    String spec=dep.get("versionRange").getAsString();
                    if (!(spec.startsWith("[")||spec.startsWith("("))) continue; // versão simples é recomendação Maven
                    try {
                        boolean contains=VersionRange.createFromVersionSpec(spec).containsVersion(versions.get(id));
                        boolean incompatible=dep.has("type")&&dep.get("type").getAsString().equals("incompatible");
                        if (incompatible ? contains : !contains) {
                            JsonObject c=new JsonObject();c.addProperty("file",entry.get("file").getAsString());c.addProperty("dependency",id);c.addProperty("declaredRange",spec);c.addProperty("observedVersion",versions.get(id).toString());conflicts.add(c);
                        }
                    } catch (Exception e) {throw new IllegalArgumentException(entry.get("file")+": "+spec,e);}
                }
            }
        }
        report.add("declaredRangeConflicts",conflicts);
        Files.writeString(path,new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(report)+"\n");
        System.out.println("Intervalos declarados divergentes: "+conflicts.size()+" (não substitui resultado do loader).");
    }
}
