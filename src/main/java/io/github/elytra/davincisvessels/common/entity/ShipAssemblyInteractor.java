package io.github.elytra.davincisvessels.common.entity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import io.github.elytra.davincisvessels.DavincisVesselsMod;
import io.github.elytra.davincisvessels.common.api.block.IBlockBalloon;
import io.github.elytra.davincisvessels.common.handler.ConnectionHandler;
import io.github.elytra.davincisvessels.common.object.DavincisVesselsObjects;
import io.github.elytra.davincisvessels.common.object.block.BlockHelm;
import io.github.elytra.davincisvessels.common.tileentity.AnchorInstance;
import io.github.elytra.davincisvessels.common.tileentity.TileAnchorPoint;
import io.github.elytra.davincisvessels.common.tileentity.TileEntitySecuredBed;
import io.github.elytra.davincisvessels.common.tileentity.TileHelm;
import io.github.elytra.movingworld.MovingWorldMod;
import io.github.elytra.movingworld.common.chunk.LocatedBlock;
import io.github.elytra.movingworld.common.chunk.MovingWorldAssemblyInteractor;
import io.github.elytra.movingworld.common.chunk.assembly.CanAssemble;
import io.netty.buffer.ByteBuf;

import static io.github.elytra.movingworld.common.chunk.assembly.AssembleResult.ResultType.RESULT_NONE;

public class ShipAssemblyInteractor extends MovingWorldAssemblyInteractor {

    private int balloonCount;

    public ShipAssemblyInteractor() {
    }

    @Override
    public void toByteBuf(ByteBuf byteBuf) {
        byteBuf.writeInt(getBalloonCount());
    }

    @Override
    public MovingWorldAssemblyInteractor fromByteBuf(byte resultCode, ByteBuf buf) {
        if (resultCode == RESULT_NONE.toByte()) {
            return new ShipAssemblyInteractor();
        }
        int balloons = buf.readInt();

        ShipAssemblyInteractor assemblyInteractor = new ShipAssemblyInteractor();
        assemblyInteractor.setBalloonCount(balloons);

        return assemblyInteractor;
    }

    @Override
    public MovingWorldAssemblyInteractor fromNBT(NBTTagCompound tag, World world) {
        ShipAssemblyInteractor mov = new ShipAssemblyInteractor();
        mov.setBalloonCount(tag.getInteger("balloonCount"));
        return mov;
    }

    @Override
    public void blockAssembled(LocatedBlock locatedBlock) {
        Block block = locatedBlock.blockState.getBlock();
        if (block instanceof IBlockBalloon) {
            try {
                balloonCount += ((IBlockBalloon) block).getBalloonWorth(locatedBlock.tileEntity);
            } catch (NullPointerException e) {
                MovingWorldMod.LOG.error("IBlockBalloon didn't check if something was null or not, report to mod author of the following block, " + block.toString());
            }
        } else if (block == DavincisVesselsObjects.blockBalloon) {
            balloonCount++;
        } else if (DavincisVesselsMod.INSTANCE.getNetworkConfig().isBalloon(block)) {
            balloonCount++;
        }
    }

    @Override
    public void blockDisassembled(LocatedBlock locatedBlock) {
        super.blockDisassembled(locatedBlock); // Currently unimplemented but leaving there just in case.

        if (locatedBlock.blockState.getBlock() == DavincisVesselsObjects.blockSecuredBed) {
            if (locatedBlock.tileEntity != null && locatedBlock.tileEntity instanceof TileEntitySecuredBed) {
                TileEntitySecuredBed securedBed = (TileEntitySecuredBed) locatedBlock.tileEntity;

                securedBed.doMove = true;
                ConnectionHandler.playerBedMap.remove(securedBed.getPlayerID());
                securedBed.addToConnectionMap(securedBed.getPlayerID());
                securedBed.moveBed(locatedBlock.blockPos);
            }
        }
    }

    @Override
    public boolean isBlockMovingWorldMarker(Block block) {
        if (block != null)
            return block.getUnlocalizedName() == DavincisVesselsObjects.blockMarkShip.getUnlocalizedName();
        else
            return false;
    }

    @Override
    public boolean isTileMovingWorldMarker(TileEntity tile) {
        if (tile != null)
            return tile instanceof TileHelm;
        else
            return false;
    }

    @Override
    public CanAssemble isBlockAllowed(World world, LocatedBlock lb) {
        IBlockState state = lb.blockState;
        CanAssemble canAssemble = super.isBlockAllowed(world, lb);

        if (state.getBlock() == DavincisVesselsObjects.blockStickyBuffer || DavincisVesselsMod.INSTANCE.getNetworkConfig().isSticky(state.getBlock()))
            canAssemble.assembleThenCancel = true;

        if (lb.tileEntity != null && lb.tileEntity instanceof TileAnchorPoint
                && ((TileAnchorPoint) lb.tileEntity).getInstance().getType() == AnchorInstance.InstanceType.FORLAND)
            canAssemble.justCancel = true;

        return canAssemble;
    }

    @Override
    public EnumFacing getFrontDirection(LocatedBlock marker) {
        return marker.blockState.getValue(BlockHelm.FACING).getOpposite();
    }

    public int getBalloonCount() {
        return balloonCount;
    }

    public void setBalloonCount(int balloonCount) {
        this.balloonCount = balloonCount;
    }

    @Override
    public void writeNBTFully(NBTTagCompound tag) {
        writeNBTMetadata(tag);
    }

    @Override
    public void writeNBTMetadata(NBTTagCompound tag) {
        tag.setInteger("balloonCount", getBalloonCount());
    }
}
