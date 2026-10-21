package br.com.minecraft2.test;

import com.exoticworlds.api.v1.ExoticWorldsApi;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import io.github.worldgenrouter.WorldgenRouterRuntime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Mod de teste separado; nunca instalar no perfil do jogador. */
@Mod("m2_worldgen_tests")
public final class WorldgenSmokeTest {
    public WorldgenSmokeTest(IEventBus bus) {
        NeoForge.EVENT_BUS.addListener(WorldgenSmokeTest::started);
    }

    private static void started(ServerStartedEvent event) {
        event.getServer().execute(() -> audit(event.getServer()));
    }

    private static void audit(MinecraftServer server) {
        JsonObject report = new JsonObject();
        JsonArray errors = new JsonArray();
        JsonArray dimensions = new JsonArray();
        report.add("errors",errors); report.add("dimensions",dimensions);
        Map<String,Integer> widths=Map.of("minecraft:overworld",9984,"minecraft:the_nether",4992,"minecraft:the_end",6016,"m2pokemon:world",4992,"m2arcana:world",3008,"undergarden:undergarden",4992,"macabre:the_pit",2016);
        try {
            report.add("portals",PortalSmokeTest.audit(server,errors));
            for (var level : server.getAllLevels()) {
                String id=level.dimension().location().toString();
                if (!widths.containsKey(id)) continue;
                System.out.println("[M2 AUDIT] Inspecionando "+id);
                JsonObject d=new JsonObject(); dimensions.add(d); d.addProperty("id",id);
                var gen=level.getChunkSource().getGenerator();
                d.addProperty("generatorClass",gen.getClass().getName());
                d.addProperty("biomeSourceClass",gen.getBiomeSource().getClass().getName());
                d.addProperty("coordinateScale",level.dimensionType().coordinateScale());
                var encoded=ChunkGenerator.CODEC.encodeStart(level.registryAccess().createSerializationContext(JsonOps.INSTANCE),gen).getOrThrow();
                Files.writeString(Path.of("m2-audit-generator-"+id.replace(':','-')+".json"),encoded.toString());
                Map<String,Integer> biomes=new TreeMap<>();
                gen.getBiomeSource().possibleBiomes().forEach(b -> b.unwrapKey().ifPresent(k -> biomes.merge(k.location().getNamespace(),1,Integer::sum)));
                d.add("biomeNamespaces",new GsonBuilder().create().toJsonTree(biomes));
                if (id.equals("minecraft:overworld") && biomes.getOrDefault("biomeswevegone",0)==0) errors.add("BYG ausente da paleta Overworld");
                if (id.equals("minecraft:the_end") && biomes.getOrDefault("betterend",0)==0) errors.add("BetterEnd ausente da paleta End");
                if (id.equals("minecraft:the_nether") && biomes.getOrDefault("betternether",0)==0) errors.add("BetterNether ausente da paleta Nether");
                if (id.equals("m2pokemon:world") && !biomes.keySet().equals(java.util.Set.of("m2pokemon"))) errors.add("Biomas externos no Pokémon");
                if (id.equals("m2arcana:world") && !biomes.keySet().equals(java.util.Set.of("m2arcana"))) errors.add("Biomas externos no Arcanus");
                if (id.equals("minecraft:the_nether") && level.dimensionType().coordinateScale()!=2) errors.add("Escala Nether incorreta");
                var shape=ExoticWorldsApi.shapeOf(level).orElseThrow();
                for (var axis : new Direction.Axis[]{Direction.Axis.X,Direction.Axis.Z}) {
                    int width=shape.widthBlocks(axis); d.addProperty("width"+axis,width);
                    if (width!=widths.get(id)) errors.add("Largura incorreta: "+id+" "+axis);
                    if (shape.foldBlock(axis,shape.maxBlock(axis))!=shape.minBlock(axis)) errors.add("Falha fold positivo: "+id);
                    if (shape.foldBlock(axis,shape.minBlock(axis)-1)!=shape.maxBlock(axis)-1) errors.add("Falha fold negativo: "+id);
                }
                d.addProperty("foldBothAxes",true);
                var runtime=WorldgenRouterRuntime.activeOrNull();
                boolean alex=runtime.spawnFilter().allow(level.dimension(),ResourceLocation.parse("alexsmobs:grizzly_bear"));
                boolean fauna=runtime.spawnFilter().allow(level.dimension(),ResourceLocation.parse("ars_fauna:chefbuncle"));
                d.addProperty("alexNaturalAllowed",alex);d.addProperty("arsFaunaNaturalAllowed",fauna);
                if (alex==(id.equals("m2pokemon:world")||id.equals("m2arcana:world"))) errors.add("Regra Alex incorreta: "+id);
                if (fauna!=id.equals("m2arcana:world")) errors.add("Regra Ars Fauna incorreta: "+id);
                // API real de spawn com motivo COMMAND: a integração não pode impedir.
                var entity=BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse("alexsmobs:grizzly_bear")).spawn(level,new BlockPos(0,150,0),MobSpawnType.COMMAND);
                d.addProperty("manualAlexSpawn",entity!=null);
                if (entity==null) errors.add("Invocação manual bloqueada: "+id);else entity.discard();
                for (String entityId : new String[]{"alexsmobs:grizzly_bear","ars_fauna:chefbuncle"}) {
                    boolean expected=entityId.startsWith("alexsmobs")?alex:fauna;
                    var type=BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entityId));
                    for (var reason : new MobSpawnType[]{MobSpawnType.NATURAL,MobSpawnType.CHUNK_GENERATION,MobSpawnType.COMMAND}) {
                        var spawned=type.spawn(level,new BlockPos(0,150,0),reason);
                        // Sem jogadores, a seção pode estar oculta no lookup de UUID,
                        // embora o addNewEntity tenha sido aceito pelo nível.
                        boolean inserted=spawned!=null&&spawned.isAddedToLevel();
                        d.addProperty(entityId+"/"+reason,inserted);
                        if (inserted!=(reason==MobSpawnType.COMMAND||expected)) errors.add("Spawn efetivo incorreto: "+id+" "+entityId+" "+reason);
                        if (spawned!=null) spawned.discard();
                    }
                }
                if (Boolean.getBoolean("m2.audit.chunks")) {
                    int edge=shape.maxChunk(Direction.Axis.X), opposite=shape.minChunk(Direction.Axis.X);
                    var a=level.getChunk(edge,0);var b=level.getChunk(opposite,0);
                    d.addProperty("sameChunkAcrossX",a==b);
                    if (a!=b) errors.add("Chunk não dobrado X: "+id);
                    var z=level.getChunk(0,shape.maxChunk(Direction.Axis.Z));
                    d.addProperty("sameChunkAcrossZ",z==level.getChunk(0,shape.minChunk(Direction.Axis.Z)));
                    if (z!=level.getChunk(0,shape.minChunk(Direction.Axis.Z))) errors.add("Chunk não dobrado Z: "+id);
                }
                Files.writeString(Path.of("m2-worldgen-audit.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));
            }
            Map<String,Integer> cobbleFeatures=new TreeMap<>();
            var features=server.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
            for (var entry : server.registryAccess().registryOrThrow(Registries.BIOME).entrySet()) {
                String namespace=entry.getKey().location().getNamespace();
                for (var step : entry.getValue().getGenerationSettings().features()) for (var feature : step) {
                    var key=features.getKey(feature.value());
                    if (key!=null && key.getNamespace().equals("cobblemon")) cobbleFeatures.merge(namespace,1,Integer::sum);
                }
            }
            report.add("cobblemonFeatureInjectionByBiomeNamespace",new GsonBuilder().create().toJsonTree(cobbleFeatures));
            if (cobbleFeatures.getOrDefault("m2pokemon",0)==0) errors.add("Cobblemon sem features no Pokémon");
            if (cobbleFeatures.keySet().stream().anyMatch(ns -> !ns.equals("m2pokemon"))) errors.add("Cobblemon injetado fora dos clones Pokémon");
        } catch (Throwable problem) {
            errors.add(problem.toString());problem.printStackTrace();
        } finally {
            report.addProperty("passed",errors.isEmpty());
            try {Files.writeString(Path.of("m2-worldgen-audit.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));} catch (Exception e) {e.printStackTrace();}
            System.out.println("[M2 AUDIT] Resultado: "+report);
            server.halt(false);
        }
    }
}
