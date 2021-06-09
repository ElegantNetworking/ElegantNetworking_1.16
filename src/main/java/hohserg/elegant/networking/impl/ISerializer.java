package hohserg.elegant.networking.impl;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.EncoderException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

import java.io.IOException;

public interface ISerializer<Packet> extends ISerializerBase<Packet>, RegistrableSingletonSerializer {

    default void serialize_BlockPos_Generic(BlockPos value, ByteBuf acc) {
        acc.writeInt(value.getX());
        acc.writeInt(value.getY());
        acc.writeInt(value.getZ());
    }

    default BlockPos unserialize_BlockPos_Generic(ByteBuf buf) {
        return new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    }

    default void serialize_CompoundNBT_Generic(CompoundNBT value, ByteBuf acc) {
        Preconditions.checkNotNull(value);
        try {
            CompressedStreamTools.write(value, new ByteBufOutputStream(acc));
        } catch (IOException e) {
            throw new EncoderException("Failed to write CompoundNBT to packet.", e);
        }
    }


    default CompoundNBT unserialize_CompoundNBT_Generic(ByteBuf buf) {
        try {
            return CompressedStreamTools.read(new ByteBufInputStream(buf), new NBTSizeTracker(2097152L));
        } catch (IOException e) {
            throw new EncoderException("Failed to read CompoundNBT from packet.", e);
        }
    }

    default void serialize_ItemStack_Generic(ItemStack value, ByteBuf acc) {
        new PacketBuffer(acc).writeItemStack(value);
    }

    default ItemStack unserialize_ItemStack_Generic(ByteBuf buf) {
        return new PacketBuffer(buf).readItemStack();
    }

    default void serialize_FluidStack_Generic(FluidStack value, ByteBuf acc) {
        new PacketBuffer(acc).writeFluidStack(value);
    }

    default FluidStack unserialize_FluidStack_Generic(ByteBuf buf) {
        return new PacketBuffer(buf).readFluidStack();
    }

    default void serialize_ResourceLocation_Generic(ResourceLocation value, ByteBuf acc) {
        serialize_String_Generic(value.toString(), acc);
    }

    default ResourceLocation unserialize_ResourceLocation_Generic(ByteBuf buf) {
        return new ResourceLocation(unserialize_String_Generic(buf));
    }
}