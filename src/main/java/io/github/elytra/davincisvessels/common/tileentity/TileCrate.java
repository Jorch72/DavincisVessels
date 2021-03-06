package io.github.elytra.davincisvessels.common.tileentity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import io.github.elytra.davincisvessels.common.entity.EntityShip;
import io.github.elytra.movingworld.api.IMovingTile;
import io.github.elytra.movingworld.common.chunk.mobilechunk.MobileChunk;
import io.github.elytra.movingworld.common.entity.EntityMovingWorld;

public class TileCrate extends TileEntity implements IMovingTile, ITickable {
    private EntityShip parentShip;
    private int containedEntityId;
    private Entity containedEntity;
    private int refreshTime;
    private BlockPos chunkPos;

    public TileCrate() {
        parentShip = null;
        containedEntityId = 0;
        containedEntity = null;
        refreshTime = 0;
    }

    @Override
    public void setParentMovingWorld(EntityMovingWorld movingWorld, BlockPos pos) {
        chunkPos = pos;
        parentShip = (EntityShip) movingWorld;
    }

    @Override
    public EntityShip getParentMovingWorld() {
        return parentShip;
    }

    @Override
    public void setParentMovingWorld(EntityMovingWorld entityMovingWorld) {
        setParentMovingWorld(entityMovingWorld, new BlockPos(BlockPos.ORIGIN));
    }

    @Override
    public BlockPos getChunkPos() {
        return chunkPos;
    }

    @Override
    public void setChunkPos(BlockPos chunkPos) {
        this.chunkPos = chunkPos;
    }

    @Override
    public void tick(MobileChunk mobileChunk) {
        // We'll try using this experimental tick function (sort of experimental) to keep the entity on the ship...?
        if (containedEntity == null) {
            if (refreshTime > 0) {
                refreshTime--;
            }
        } else if (containedEntity.isDead) {
            setContainedEntity(null);
        } else {
            containedEntity.motionX = containedEntity.motionY = containedEntity.motionZ = 0d;
            if (parentShip == null) {
                containedEntity.setPosition(pos.getX() + 0.5d, pos.getY() + 0.15f + containedEntity.getYOffset(), pos.getZ() + 0.5d);
            } else {
                parentShip.updatePassengerPosition(containedEntity, pos, 2);
            }

            if (containedEntity.hurtResistantTime > 0 || containedEntity.isSneaking()) {
                containedEntity.posY += 1d;
                releaseEntity();
            }
        }
    }

    public boolean canCatchEntity() {
        return refreshTime == 0;
    }

    public void releaseEntity() {
        setContainedEntity(null);
        refreshTime = 60;
    }

    public Entity getContainedEntity() {
        return containedEntity;
    }

    public void setContainedEntity(Entity entity) {
        containedEntity = entity;
        containedEntityId = containedEntity == null ? 0 : containedEntity.getEntityId();
        refreshTime = 0;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(compound);
        return new SPacketUpdateTileEntity(pos, 0, compound);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        readFromNBT(packet.getNbtCompound());
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        if (tag.hasKey("contained")) {
            if (worldObj == null) {
                containedEntityId = tag.getInteger("contained");
            } else {
                setContainedEntity(worldObj.getEntityByID(tag.getInteger("contained")));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        if (containedEntity != null) {
            tag.setInteger("contained", containedEntity.getEntityId());
        }
        return tag;
    }

    @Override
    public void update() {
        if (worldObj.isRemote) {
            if (parentShip != null && parentShip.isDead) {
                parentShip = null;
            }
            if (containedEntity == null) {
                if (containedEntityId != 0) {
                    setContainedEntity(worldObj.getEntityByID(containedEntityId));
                }
            }
        }

        if (containedEntity == null) {
            if (refreshTime > 0) {
                refreshTime--;
            }
        } else if (containedEntity.isDead) {
            setContainedEntity(null);
        } else {
            containedEntity.motionX = containedEntity.motionY = containedEntity.motionZ = 0d;
            if (parentShip == null) {
                containedEntity.setPosition(pos.getX() + 0.5d, pos.getY() + 0.15f + containedEntity.getYOffset(), pos.getZ() + 0.5d);
            } else {
                parentShip.updatePassengerPosition(containedEntity, pos, 2);
            }

            if (containedEntity.hurtResistantTime > 0 || containedEntity.isSneaking()) {
                containedEntity.posY += 1d;
                releaseEntity();
            }
        }
    }
}
