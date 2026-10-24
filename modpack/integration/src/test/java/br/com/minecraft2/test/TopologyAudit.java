package br.com.minecraft2.test;

import br.com.minecraft2.WorldShapeDiagnostics;
import com.exoticworlds.api.v1.ExoticWorldsApi;
import com.exoticworlds.core.ShapedChunkGenerator;
import com.exoticworlds.engine.noise.GenerationTransformerContext;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;

/** Destrutivo somente no mundo descartável criado pelo runner. Duas inicializações. */
final class TopologyAudit {
    private static void check(JsonArray errors, boolean ok, String message) {
        if (!ok) errors.add(message);
    }

    static void run(MinecraftServer server) {
        boolean reopen = Boolean.getBoolean("m2.audit.reopen");
        JsonObject report = new JsonObject();
        JsonArray errors = new JsonArray();
        JsonArray dimensions = new JsonArray();
        report.addProperty("startedUtc",Instant.now().toString());
        report.addProperty("phase",reopen ? "reopen" : "write");
        report.addProperty("seed",server.overworld().getSeed());
        report.addProperty("sampling","climate source in generator context; fixed Y=64, step=64; not surface-height or client exploration");
        report.add("errors",errors); report.add("dimensions",dimensions);
        try {
            for (var entry : new TreeMap<>(WorldShapeDiagnostics.WIDTHS).entrySet()) {
                String id = entry.getKey();
                var level = server.getLevel(net.minecraft.resources.ResourceKey.create(Registries.DIMENSION,
                    net.minecraft.resources.ResourceLocation.parse(id)));
                check(errors,level != null,"Dimensão ausente: "+id);
                if (level == null) continue;
                JsonObject d = new JsonObject(); dimensions.add(d); d.addProperty("id",id);
                var gen = level.getChunkSource().getGenerator();
                d.addProperty("generatorClass",gen.getClass().getName());
                d.addProperty("biomeSourceClass",gen.getBiomeSource().getClass().getName());
                d.addProperty("coordinateScale",level.dimensionType().coordinateScale());
                d.add("generator",ChunkGenerator.CODEC.encodeStart(level.registryAccess().createSerializationContext(JsonOps.INSTANCE),gen).getOrThrow());
                var optional = ExoticWorldsApi.shapeOf(level);
                check(errors,optional.isPresent(),"Sem wrapping: "+id);
                if (optional.isEmpty()) continue;
                var shape = optional.get(); int width = entry.getValue();
                d.addProperty("widthX",shape.widthBlocks(Direction.Axis.X));
                d.addProperty("widthZ",shape.widthBlocks(Direction.Axis.Z));
                check(errors,shape.widthBlocks(Direction.Axis.X)==width && shape.widthBlocks(Direction.Axis.Z)==width,"Larguras: "+id);
                JsonArray probes = new JsonArray(); d.add("blockProbes",probes);
                // Centro, lados e quatro cantos; coordenadas dos aliases nunca são pré-dobradas.
                int min=-width/2, max=width/2-1;
                int[][] points={{0,0},{min,17},{max,17},{17,min},{17,max},{min,min},{min,max},{max,min},{max,max}};
                int index=0;
                for (int[] point : points) {
                    BlockPos a=new BlockPos(point[0],level.getMaxBuildHeight()-8,point[1]);
                    var marker=Blocks.GOLD_BLOCK.defaultBlockState();
                    JsonObject probe=new JsonObject();probes.add(probe);
                    probe.addProperty("x",a.getX());probe.addProperty("y",a.getY());probe.addProperty("z",a.getZ());
                    boolean persisted=level.getBlockState(a).equals(marker);
                    if (reopen) check(errors,persisted,id+" marcador não persistiu "+a);
                    probe.addProperty("persistedMarker",reopen && persisted);
                    var original=level.getBlockState(a);
                    level.setBlock(a,marker,3);
                    boolean same=true, reads=true, removal=true;
                    for (int dx : new int[]{-width,0,width}) for (int dz : new int[]{-width,0,width}) {
                        BlockPos alias=a.offset(dx,0,dz);
                        same &= level.getChunkAt(a)==level.getChunkAt(alias);
                        reads &= level.getBlockState(alias).equals(marker);
                        level.setBlock(alias,Blocks.AIR.defaultBlockState(),3);
                        removal &= level.getBlockState(a).isAir();
                        level.setBlock(alias,marker,3);
                        reads &= level.getBlockState(a).equals(marker);
                    }
                    check(errors,same && reads && removal,id+" identidade/escrita/remoção "+a);
                    probe.addProperty("sameChunkNineAliases",same);
                    probe.addProperty("readWriteNineAliases",reads);
                    probe.addProperty("removeNineAliases",removal);
                    // Marcadores ficam até o segundo boot; não usados no perfil do jogador.
                    if (reopen) level.setBlock(a,Blocks.AIR.defaultBlockState(),3);
                    index++;
                }
                var namespaces=new TreeMap<String,Integer>();
                gen.getBiomeSource().possibleBiomes().forEach(b -> namespaces.merge(b.unwrapKey().orElseThrow().location().getNamespace(),1,Integer::sum));
                d.add("paletteNamespaces",new GsonBuilder().create().toJsonTree(namespaces));
                if (id.equals("minecraft:overworld")) {
                    check(errors,namespaces.getOrDefault("biomeswevegone",0)>0,"BYG ausente");
                    check(errors,!namespaces.containsKey("ars_nouveau") && !namespaces.containsKey("ars_elemental"),"Ars na paleta Overworld");
                }
                if (id.equals("minecraft:the_nether")) check(errors,namespaces.containsKey("betternether"),"BetterNether ausente");
                if (id.equals("minecraft:the_end")) check(errors,namespaces.containsKey("betterend"),"BetterEnd ausente");
                if (id.equals("m2arcana:world")) {
                    Map<String,Integer> counts=new TreeMap<>(); JsonArray samples=new JsonArray();
                    Map<String,Integer> surfaceCounts=new TreeMap<>();
                    var sampler=level.getChunkSource().randomState().sampler();
                    GenerationTransformerContext.runWithTransformer(ShapedChunkGenerator.transformerOf(gen), () -> {
                        for (int x=min; x<=max; x+=64) for (int z=min; z<=max; z+=64) {
                            String biome=gen.getBiomeSource().getNoiseBiome(x>>2,16,z>>2,sampler).unwrapKey().orElseThrow().location().toString();
                            counts.merge(biome,1,Integer::sum);
                            JsonArray sample=new JsonArray();sample.add(x);sample.add(z);sample.add(biome);samples.add(sample);
                            int height=gen.getBaseHeight(x,z,net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG,
                                level,level.getChunkSource().randomState());
                            String surfaceBiome=gen.getBiomeSource().getNoiseBiome(x>>2,(height-1)>>2,z>>2,sampler).unwrapKey().orElseThrow().location().toString();
                            sample.add(height);sample.add(surfaceBiome);
                            surfaceCounts.merge(surfaceBiome,1,Integer::sum);
                        }
                    });
                    d.add("spatialBiomeCountsY64",new GsonBuilder().create().toJsonTree(counts));
                    d.add("spatialSamplesY64",samples);
                    d.add("spatialBiomeCountsBaseSurface",new GsonBuilder().create().toJsonTree(surfaceCounts));
                    d.addProperty("surfaceSampling","generator base height, before features; no generated-chunk or client assertion");
                    d.addProperty("observedBiomesY64",counts.size());
                    JsonArray absent=new JsonArray();
                    gen.getBiomeSource().possibleBiomes().forEach(b -> {String key=b.unwrapKey().orElseThrow().location().toString();if(!counts.containsKey(key)) absent.add(key);});
                    d.add("notObservedY64",absent);
                }
                System.out.println("[M2 TOPOLOGY] "+id+" concluído; erros="+errors.size());
            }
            // Verifica injeções efetivas, depois da aplicação dos biome modifiers.
            JsonObject injections=new JsonObject();report.add("arsFeaturesByBiomeNamespace",injections);
            var features=server.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
            Map<String,Integer> counts=new TreeMap<>();
            for(var entry:server.registryAccess().registryOrThrow(Registries.BIOME).entrySet()) {
                String ns=entry.getKey().location().getNamespace();
                for(var step:entry.getValue().getGenerationSettings().features()) for(var feature:step) {
                    var key=features.getKey(feature.value());
                    if(key!=null && (key.getNamespace().equals("ars_nouveau") || key.getNamespace().equals("ars_elemental"))) counts.merge(ns,1,Integer::sum);
                }
            }
            counts.forEach(injections::addProperty);
            // Biomas originais registrados Ars podem conter suas próprias features sem serem gerados no Overworld.
            check(errors,counts.getOrDefault("m2arcana",0)>0,"Ars ausente dos clones Arcanus");
            check(errors,counts.getOrDefault("minecraft",0)==0 && counts.getOrDefault("biomeswevegone",0)==0,"Features Ars injetadas em biomas vanilla/BYG");
            server.saveEverything(true,true,true);
        } catch(Throwable t) {
            errors.add(t.toString());t.printStackTrace();
        } finally {
            report.addProperty("passed",errors.isEmpty());
            try {Files.writeString(Path.of("m2-topology-"+(reopen?"reopen":"write")+".json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}
            catch(Exception e) {throw new RuntimeException(e);}
            System.out.println("[M2 TOPOLOGY] Resultado: "+(errors.isEmpty()?"PASS":"FAIL")+" "+errors);
        }
    }
}
