package br.com.minecraft2.test;

import com.exoticworlds.core.ShapedChunkGenerator;
import com.exoticworlds.engine.noise.GenerationTransformerContext;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

/** Atlas denso de todos os 752x752 pontos de bioma na horizontal, Y=64. */
final class ArcanusAtlas {
    static void run(MinecraftServer server) {
        JsonObject report=new JsonObject();
        try {
            var level=server.getLevel(ResourceKey.create(Registries.DIMENSION,ResourceLocation.parse("m2arcana:world")));
            var gen=level.getChunkSource().getGenerator();
            var sampler=level.getChunkSource().randomState().sampler();
            Map<String,Integer> counts=new TreeMap<>();
            Map<String,int[]> first=new TreeMap<>();
            GenerationTransformerContext.runWithTransformer(ShapedChunkGenerator.transformerOf(gen),()-> {
                for(int x=-1504;x<1504;x+=4) for(int z=-1504;z<1504;z+=4) {
                    String biome=gen.getBiomeSource().getNoiseBiome(x>>2,16,z>>2,sampler).unwrapKey().orElseThrow().location().toString();
                    counts.merge(biome,1,Integer::sum);
                    first.putIfAbsent(biome,new int[]{x,64,z});
                }
            });
            report.addProperty("seed",level.getSeed());
            report.addProperty("sampleCount",counts.values().stream().mapToInt(Integer::intValue).sum());
            report.addProperty("stepBlocks",4);report.addProperty("y",64);
            report.addProperty("observedBiomes",counts.size());
            report.addProperty("sampling","all quart X/Z positions in the 3008-block world at fixed Y=64; climate source, not client or vegetation");
            report.add("counts",new GsonBuilder().create().toJsonTree(counts));
            report.add("firstPositionByBiome",new GsonBuilder().create().toJsonTree(first));
            report.addProperty("passed",counts.values().stream().mapToInt(Integer::intValue).sum()==565504);
        } catch(Throwable t) {
            report.addProperty("passed",false);report.addProperty("error",t.toString());t.printStackTrace();
        } finally {
            try {Files.writeString(Path.of("m2-arcanus-atlas.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}
            catch(Exception e) {throw new RuntimeException(e);}
            System.out.println("[M2 ATLAS] Resultado: "+report);
        }
    }
}
