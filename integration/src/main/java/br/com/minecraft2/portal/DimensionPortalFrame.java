package br.com.minecraft2.portal;

import com.benbenlaw.portals.portal.frame.PortalFrameTester;
import com.benbenlaw.portals.portal.frame.VanillaPortalAreaHelper;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Valida posições e cantos; o BBL continua responsável por transporte e links. */
public final class DimensionPortalFrame extends VanillaPortalAreaHelper {
    private final boolean pokemon;

    public DimensionPortalFrame(boolean pokemon) { this.pokemon = pokemon; }

    private Block material(int row, int interiorHeight) {
        // Altura externa ímpar: a linha central pertence à metade superior.
        return pokemon ? (row + 1 < (interiorHeight + 2) / 2 ? Blocks.IRON_BLOCK : Blocks.REDSTONE_BLOCK)
            : Blocks.CRYING_OBSIDIAN;
    }

    @Override
    public PortalFrameTester init(LevelAccessor level, BlockPos pos, Direction.Axis axis, Block... ignored) {
        super.init(level, pos, axis, pokemon ? new Block[]{Blocks.IRON_BLOCK, Blocks.REDSTONE_BLOCK}
            : new Block[]{Blocks.CRYING_OBSIDIAN});
        return this;
    }

    @Override
    public boolean isValidFrame() {
        if (!super.isValidFrame()) return false;
        for (int row = -1; row <= height; row++) {
            for (int col = -1; col <= width; col++) {
                if (row != -1 && row != height && col != -1 && col != width) continue;
                if (!world.getBlockState(lowerCorner.relative(axis, col).above(row)).is(material(row, height))) return false;
            }
        }
        return true;
    }

    @Override
    public Optional<PortalFrameTester> getOrEmpty(LevelAccessor level, BlockPos pos,
            Predicate<PortalFrameTester> predicate, Direction.Axis axis, Block... blocks) {
        // O helper vanilla do BBL cria VanillaPortalAreaHelper aqui, perdendo subclasses.
        for (Direction.Axis a : new Direction.Axis[]{axis, axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X}) {
            PortalFrameTester tester = new DimensionPortalFrame(pokemon).withPortalLink(portalLink).init(level, pos, a, blocks);
            if (predicate.test(tester)) return Optional.of(tester);
        }
        return Optional.empty();
    }

    @Override
    public void lightPortal(Block ignored) {
        super.lightPortal(pokemon ? Blocks.IRON_BLOCK : Blocks.CRYING_OBSIDIAN);
    }

    @Override
    public void createPortal(Level level, BlockPos requested, BlockState ignored, Direction.Axis axis) {
        BlockPos pos = requested.atY(Math.max(level.getMinBuildHeight() + 2,
            Math.min(level.getMaxBuildHeight() - 5, requested.getY())));
        Direction.Axis normal = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        // Moldura externa padrão 4x5, com plataforma dos dois lados e espaço para sair.
        for (int col = -1; col <= 2; col++) for (int row = -1; row <= 3; row++) {
            BlockPos p = pos.relative(axis, col).above(row);
            Block block = row == -1 || row == 3 || col == -1 || col == 2 ? material(row, 3) : Blocks.AIR;
            level.setBlock(p, block.defaultBlockState(), 18);
        }
        for (int col = -1; col <= 2; col++) for (int side : new int[]{-1, 1}) {
            BlockPos base = pos.relative(axis, col).relative(normal, side);
            for (int row = 0; row <= 3; row++) level.setBlock(base.above(row), Blocks.AIR.defaultBlockState(), 18);
            level.setBlock(base.below(), (pokemon ? Blocks.IRON_BLOCK : Blocks.CRYING_OBSIDIAN).defaultBlockState(), 18);
        }
        init(level, pos, axis);
        lightPortal(ignored.getBlock());
    }
}
