package hohserg.elegant.networking.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.EncoderException;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public interface ISerializer<Packet> {
    void serialize(Packet value, ByteBuf acc);

    Packet unserialize(ByteBuf buf);

    int packetId();

    default void serialize_Boolean_Generic(boolean value, ByteBuf acc) {
        acc.writeBoolean(value);
    }

    default void serialize_Byte_Generic(byte value, ByteBuf acc) {
        acc.writeByte(value);
    }

    default void serialize_Short_Generic(short value, ByteBuf acc) {
        acc.writeShort(value);
    }

    default void serialize_Int_Generic(int value, ByteBuf acc) {
        acc.writeInt(value);
    }

    default void serialize_Long_Generic(long value, ByteBuf acc) {
        acc.writeLong(value);
    }

    default void serialize_Char_Generic(char value, ByteBuf acc) {
        acc.writeChar(value);
    }

    default void serialize_Float_Generic(float value, ByteBuf acc) {
        acc.writeFloat(value);
    }

    default void serialize_Double_Generic(double value, ByteBuf acc) {
        acc.writeDouble(value);
    }

    default void serialize_String_Generic(String value, ByteBuf acc) {
        byte[] utf8Bytes = value.getBytes(StandardCharsets.UTF_8);
        acc.writeInt(utf8Bytes.length);
        acc.writeBytes(utf8Bytes);
    }

    default void serialize_CompoundNBT_Generic(CompoundNBT value, ByteBuf acc) {
        if (value != null)
            try {
                acc.writeBoolean(true);
                CompressedStreamTools.write(value, new ByteBufOutputStream(acc));
            } catch (IOException e) {
                throw new EncoderException("Failed to write CompoundNBT to packet.", e);
            }
        else
            acc.writeBoolean(false);
    }

    default void serialize_ItemStack_Generic(ItemStack value, ByteBuf acc) {
        new PacketBuffer(acc).writeItemStack(value);
    }

    default void serialize_FluidStack_Generic(FluidStack value, ByteBuf acc) {
        new PacketBuffer(acc).writeFluidStack(value);
    }

    default void serialize_Item_Generic(Item value, ByteBuf acc) {
        acc.writeInt(((ForgeRegistry<Item>) ForgeRegistries.ITEMS).getID(value));
    }

    default void serialize_Block_Generic(Block value, ByteBuf acc) {
        acc.writeInt(((ForgeRegistry<Block>) ForgeRegistries.BLOCKS).getID(value));
    }

    default void serialize_Fluid_Generic(Fluid value, ByteBuf acc) {
        acc.writeInt(((ForgeRegistry<Fluid>) ForgeRegistries.FLUIDS).getID(value));
    }

    default void serialize_ResourceLocation_Generic(ResourceLocation value, ByteBuf acc) {
        serialize_String_Generic(value.toString(), acc);
    }

    default void serialize_UUID_Generic(UUID value, ByteBuf acc) {
        serialize_String_Generic(value.toString(), acc);
    }


    default boolean unserialize_Boolean_Generic(ByteBuf buf) {
        return buf.readBoolean();
    }

    default byte unserialize_Byte_Generic(ByteBuf buf) {
        return buf.readByte();
    }

    default short unserialize_Short_Generic(ByteBuf buf) {
        return buf.readShort();
    }

    default int unserialize_Int_Generic(ByteBuf buf) {
        return buf.readInt();
    }

    default long unserialize_Long_Generic(ByteBuf buf) {
        return buf.readLong();
    }

    default char unserialize_Char_Generic(ByteBuf buf) {
        return buf.readChar();
    }

    default float unserialize_Float_Generic(ByteBuf buf) {
        return buf.readFloat();
    }

    default double unserialize_Double_Generic(ByteBuf buf) {
        return buf.readDouble();
    }

    default String unserialize_String_Generic(ByteBuf buf) {
        int len = buf.readInt();
        String str = buf.toString(buf.readerIndex(), len, StandardCharsets.UTF_8);
        buf.readerIndex(buf.readerIndex() + len);
        return str;
    }

    default CompoundNBT unserialize_NBTTagCompound_Generic(ByteBuf buf) {
        if (buf.readBoolean())
            try {
                return CompressedStreamTools.read(new ByteBufInputStream(buf), new NBTSizeTracker(2097152L));
            } catch (IOException e) {
                throw new EncoderException("Failed to read CompoundNBT from packet.", e);
            }
        else
            return null;
    }

    default ItemStack unserialize_ItemStack_Generic(ByteBuf buf) {
        return new PacketBuffer(buf).readItemStack();
    }

    default FluidStack unserialize_FluidStack_Generic(ByteBuf buf) {
        return new PacketBuffer(buf).readFluidStack();
    }

    default Item unserialize_Item_Generic(ByteBuf buf) {
        return ((ForgeRegistry<Item>) ForgeRegistries.ITEMS).getValue(buf.readInt());
    }

    default Block unserialize_Block_Generic(ByteBuf buf) {
        return ((ForgeRegistry<Block>) ForgeRegistries.BLOCKS).getValue(buf.readInt());
    }

    default Fluid unserialize_Fluid_Generic(ByteBuf buf) {
        return ((ForgeRegistry<Fluid>) ForgeRegistries.FLUIDS).getValue(buf.readInt());
    }

    default ResourceLocation unserialize_ResourceLocation_Generic(ByteBuf buf) {
        return new ResourceLocation(unserialize_String_Generic(buf));
    }

    default UUID unserialize_UUID_Generic(ByteBuf buf) {
        return UUID.fromString(unserialize_String_Generic(buf));
    }
}