package br.com.minecraft2.test;

import br.com.minecraft2.portal.DimensionPortalFrame;
import com.benbenlaw.portals.Portals;
import com.benbenlaw.portals.block.CustomPortalBlock;
import com.benbenlaw.portals.portal.PortalPlacer;
import com.benbenlaw.portals.util.CustomPortalApiRegistry;
import com.benbenlaw.portals.util.PortalIgnitionSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Interage com blocos e API reais em mundo descartável; não simula um cliente conectado. */
public final class PortalSmokeTest {
    private static void check(boolean ok, String message, JsonArray errors) { if (!ok) errors.add(message); }

    private static Block material(boolean pokemon, int row, int height) {
        return pokemon ? (row + 1 < (height + 2) / 2 ? Blocks.IRON_BLOCK : Blocks.REDSTONE_BLOCK) : Blocks.CRYING_OBSIDIAN;
    }

    private static void frame(ServerLevel level, BlockPos pos, Direction.Axis axis, int width, int height, boolean pokemon) {
        for (int x=-1; x<=width; x++) for (int y=-1; y<=height; y++) {
            Block block = x==-1 || x==width || y==-1 || y==height ? material(pokemon,y,height) : Blocks.AIR;
            level.setBlock(pos.relative(axis,x).above(y),block.defaultBlockState(),18);
        }
    }

    private static boolean click(ServerLevel level, BlockPos frame, Direction face, net.minecraft.world.item.Item item, JsonArray errors) {
        var player=FakePlayerFactory.getMinecraft(level);
        player.setPos(frame.getX()+0.5,frame.getY()+1,frame.getZ()+0.5);
        var stack=new ItemStack(item);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var hit=new BlockHitResult(Vec3.atCenterOf(frame),face,frame,false);
        var event=new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,frame,hit);
        NeoForge.EVENT_BUS.post(event);
        boolean success=event.isCanceled();
        if (!success) success=stack.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,hit)).consumesAction();
        if (BuiltInRegistries.ITEM.getKey(item).toString().equals("undergarden:catalyst"))
            check(stack.getCount()==1 && stack.getDamageValue()==0,"Catalyst consumido/danificado",errors);
        return success;
    }

    public static JsonObject audit(MinecraftServer server, JsonArray errors) {
        JsonObject report=new JsonObject();
        ServerLevel level=server.overworld();
        var catalyst=BuiltInRegistries.ITEM.get(ResourceLocation.parse("undergarden:catalyst"));
        int cases=0;
        for (boolean pokemon : new boolean[]{true,false}) {
            String name=pokemon?"pokemon":"arcanus";
            var link=CustomPortalApiRegistry.getPortalLinkFromBase(pokemon?Blocks.IRON_BLOCK:Blocks.CRYING_OBSIDIAN);
            check(link!=null,"Portal KubeJS ausente: "+name,errors);
            if (link==null) continue;
            check(link.doesIgnitionMatch(PortalIgnitionSource.ItemUseSource(catalyst)),"Ativador incorreto: "+name,errors);
            check(!link.doesIgnitionMatch(PortalIgnitionSource.FIRE),"Fogo permitido: "+name,errors);
            check(!link.canLightInDim(ResourceLocation.parse("undergarden:undergarden")),"Portal habilitado em dimensão externa",errors);
            for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X,Direction.Axis.Z}) {
                BlockPos pos=new BlockPos(pokemon?96:128,200,96);
                for (int[] size : new int[][]{{2,3},{3,4},{21,21}}) {
                    frame(level,pos,axis,size[0],size[1],pokemon);
                    check(new DimensionPortalFrame(pokemon).init(level,pos,axis).isValidFrame(),"Moldura válida rejeitada "+name+axis+size[0]+"x"+size[1],errors);
                    cases++;
                }
                for (int[] size : new int[][]{{1,3},{2,2},{22,3},{2,22}}) {
                    frame(level,pos,axis,size[0],size[1],pokemon);
                    check(!new DimensionPortalFrame(pokemon).init(level,pos,axis).isValidFrame(),"Tamanho inválido aceito "+name+axis,errors);
                    cases++;
                }
                frame(level,pos,axis,2,3,pokemon);
                level.setBlock(pos.relative(axis,-1).below(),Blocks.AIR.defaultBlockState(),18);
                check(!PortalPlacer.attemptPortalLight(level,pos,PortalIgnitionSource.ItemUseSource(catalyst)),"Canto ausente aceito "+name+axis,errors);cases++;
                frame(level,pos,axis,2,3,pokemon);
                level.setBlock(pos.relative(axis,-1).above(),Blocks.AIR.defaultBlockState(),18);
                check(!PortalPlacer.attemptPortalLight(level,pos,PortalIgnitionSource.ItemUseSource(catalyst)),"Lateral incompleta aceita "+name+axis,errors);cases++;
                if (pokemon) {
                    frame(level,pos,axis,2,3,true);
                    for (int x=-1;x<=2;x++) for (int y=-1;y<=3;y++) if(x==-1||x==2||y==-1||y==3)
                        level.setBlock(pos.relative(axis,x).above(y),(material(true,y,3)==Blocks.IRON_BLOCK?Blocks.REDSTONE_BLOCK:Blocks.IRON_BLOCK).defaultBlockState(),18);
                    check(!PortalPlacer.attemptPortalLight(level,pos,PortalIgnitionSource.ItemUseSource(catalyst)),"Metades invertidas aceitas",errors);cases++;
                    frame(level,pos,axis,2,3,true);
                    level.setBlock(pos.relative(axis,-1).above(2),Blocks.IRON_BLOCK.defaultBlockState(),18);
                    check(!PortalPlacer.attemptPortalLight(level,pos,PortalIgnitionSource.ItemUseSource(catalyst)),"Mistura aleatória aceita",errors);cases++;
                }
                frame(level,pos,axis,2,3,pokemon);
                click(level,pos.below(),Direction.UP,Items.FLINT_AND_STEEL,errors);
                check(!(level.getBlockState(pos).getBlock() instanceof CustomPortalBlock),"Flint and Steel ativou "+name,errors);cases++;
                frame(level,pos,axis,2,3,pokemon);
                check(click(level,pos.below(),Direction.UP,catalyst,errors),"Clique Catalyst falhou "+name+axis,errors);
                check(level.getBlockState(pos).getBlock() instanceof CustomPortalBlock,"Portal físico ausente "+name+axis,errors);cases++;
                // Interação na parte superior também precisa reconhecer o redstone.
                frame(level,pos,axis,2,3,pokemon);
                check(click(level,pos.above(3),Direction.DOWN,catalyst,errors),"Clique superior falhou "+name+axis,errors);cases++;
                check(new DimensionPortalFrame(pokemon).init(level,pos,axis).isAlreadyLitPortalFrame(),"Portal instável "+name+axis,errors);
            }
            // Uma viagem real de entidade, geração do destino e retorno via link persistente BBL.
            BlockPos start=new BlockPos(pokemon?96:128,200,96);
            frame(level,start,Direction.Axis.X,2,3,pokemon);
            click(level,start.below(),Direction.UP,catalyst,errors);
            var pig=EntityType.PIG.create(level);
            pig.moveTo(start.getX()+0.5,start.getY(),start.getZ()+0.5,0,0);
            level.addFreshEntity(pig);
            var portal=(CustomPortalBlock)level.getBlockState(start).getBlock();
            var outbound=portal.getPortalDestination(level,pig,start);
            check(outbound!=null && outbound.newLevel().dimension().location().equals(link.dimID),"Destino incorreto "+name,errors);
            if (outbound!=null) {
                var arrived=pig.changeDimension(outbound);
                check(arrived!=null,"Entidade não atravessou "+name,errors);
                if (arrived!=null) {
                    check(arrived.isOnPortalCooldown(),"Sem cooldown "+name,errors);
                    var previousProcess=arrived.portalProcess;
                    arrived.setAsInsidePortal(portal,arrived.blockPosition());
                    check(arrived.portalProcess==previousProcess,"Cooldown não impediu reentrada "+name,errors);
                    var stored=Portals.PORTAL_LINKING_STORAGE.getDestination(start,level.dimension());
                    check(stored!=null,"Link BBL ausente "+name,errors);
                    if (stored!=null) {
                        var snapshot=Portals.PORTAL_LINKING_STORAGE.save(new net.minecraft.nbt.CompoundTag(),server.registryAccess());
                        var restored=com.benbenlaw.portals.portal.linking.PortalLinkingStorage.read(snapshot,server.registryAccess());
                        check(stored.equals(restored.getDestination(start,level.dimension())),"Serialização do vínculo falhou "+name,errors);
                        ServerLevel destination=outbound.newLevel();
                        check(new DimensionPortalFrame(pokemon).init(destination,stored.pos(),Direction.Axis.X).isAlreadyLitPortalFrame(),"Moldura de destino incorreta "+name,errors);
                        check(destination.getBlockState(stored.pos().below().north()).isSolid(),"Sem plataforma de saída "+name,errors);
                        arrived.setPortalCooldown(0);
                        var back=((CustomPortalBlock)destination.getBlockState(stored.pos()).getBlock()).getPortalDestination(destination,arrived,stored.pos());
                        check(back!=null && back.newLevel()==level,"Retorno incorreto "+name,errors);
                        if(back!=null) {
                            check(back.pos().distanceTo(Vec3.atBottomCenterOf(start))<3,"Retorno não usa o portal original "+name,errors);
                            var returned=arrived.changeDimension(back);
                            check(returned!=null && returned.level()==level,"Travessia de retorno falhou "+name,errors);
                            if(returned!=null)returned.discard();
                        } else arrived.discard();
                        var repeat=Portals.PORTAL_LINKING_STORAGE.getDestination(start,level.dimension());
                        check(stored.equals(repeat),"Link alterado ao retornar "+name,errors);
                    } else arrived.discard();
                }
            } else pig.discard();
            report.addProperty(name+"Destination",link.dimID.toString());
            report.addProperty(name+"Tint",link.colorID);
        }
        BlockPos ug=new BlockPos(160,200,96);
        for(int x=-1;x<=2;x++)for(int y=-1;y<=3;y++)level.setBlock(ug.east(x).above(y),
            (x==-1||x==2||y==-1||y==3?Blocks.STONE_BRICKS:Blocks.AIR).defaultBlockState(),18);
        check(click(level,ug.below(),Direction.UP,catalyst,errors),"Catalyst original Undergarden falhou",errors);
        check(BuiltInRegistries.BLOCK.getKey(level.getBlockState(ug).getBlock()).toString().equals("undergarden:undergarden_portal"),"Portal original Undergarden não gerado",errors);
        check(CustomPortalApiRegistry.getPortalLinkFromBase(Blocks.STONE_BRICKS)==null,"Integração tomou moldura Undergarden",errors);
        check(CustomPortalApiRegistry.getPortalLinkFromBase(Blocks.OBSIDIAN)==null,"Integração tomou moldura Nether",errors);
        BlockPos nether=new BlockPos(176,200,96);
        for(int x=-1;x<=2;x++)for(int y=-1;y<=3;y++)level.setBlock(nether.east(x).above(y),
            (x==-1||x==2||y==-1||y==3?Blocks.OBSIDIAN:Blocks.AIR).defaultBlockState(),18);
        click(level,nether.below(),Direction.UP,Items.FLINT_AND_STEEL,errors);
        check(level.getBlockState(nether).is(Blocks.NETHER_PORTAL),"Portal vanilla Nether deixou de acender",errors);
        // Conversão real pelo NetherPortalBlock vanilla com o tipo de dimensão 1:2.
        BlockPos scaledPortal=new BlockPos(4000,120,0);
        for(int x=-1;x<=2;x++)for(int y=-1;y<=3;y++)level.setBlock(scaledPortal.east(x).above(y),
            (x==-1||x==2||y==-1||y==3?Blocks.OBSIDIAN:Blocks.AIR).defaultBlockState(),18);
        click(level,scaledPortal.below(),Direction.UP,Items.FLINT_AND_STEEL,errors);
        var traveler=EntityType.PIG.create(level);
        traveler.moveTo(4000.5,120,0.5,0,0);level.addFreshEntity(traveler);
        var netherBlock=(net.minecraft.world.level.block.NetherPortalBlock)level.getBlockState(scaledPortal).getBlock();
        var transfer=netherBlock.getPortalDestination(level,traveler,scaledPortal);
        check(transfer!=null && transfer.newLevel().dimension()==net.minecraft.world.level.Level.NETHER,"Destino Nether incorreto",errors);
        if(transfer!=null) {
            report.addProperty("netherSourceX",traveler.getX());
            report.addProperty("netherDestinationX",transfer.pos().x);
            // A busca vanilla pode colocar a moldura até 16 blocos do alvo escalado.
            check(Math.abs(transfer.pos().x-traveler.getX()/2)<32,"Conversão Nether não corresponde a 1:2",errors);
            var transferred=traveler.changeDimension(transfer);
            check(transferred!=null && transferred.level().dimension()==net.minecraft.world.level.Level.NETHER,"Viagem Nether falhou",errors);
            if(transferred!=null)transferred.discard();
        } else traveler.discard();
        report.addProperty("frameAndIgnitionCases",cases);
        report.addProperty("originalUndergardenCatalyst",true);
        report.addProperty("scope","Servidor real; FakePlayer para cliques, porco para viagens. Sem cliente ou dois jogadores conectados.");
        return report;
    }
}
