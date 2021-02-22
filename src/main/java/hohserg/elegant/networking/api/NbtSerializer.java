package hohserg.elegant.networking.api;

import hohserg.elegant.networking.impl.ISerializerBase;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.ByteArrayNBT;
import net.minecraft.nbt.CompoundNBT;

public class NbtSerializer<A extends IByteBufSerializable> {
    private final ISerializerBase<A> serializer;

    public NbtSerializer(ISerializerBase<A> serializer) {
        this.serializer = serializer;
    }

    public CompoundNBT serialize(A value) {
        CompoundNBT r = new CompoundNBT();
        r.put("content", serializeToByteArray(value));
        return r;
    }

    public A unserialize(CompoundNBT nbt) {
        if (nbt.contains("content", 7))
            return unserializeFromByteArray((ByteArrayNBT) nbt.get("content"));
        else
            throw new IllegalArgumentException("invalid nbt data " + nbt);
    }

    public ByteArrayNBT serializeToByteArray(A value) {
        ByteBuf buffer = Unpooled.buffer();
        serializer.serialize(value, buffer);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return new ByteArrayNBT(bytes);
    }

    public A unserializeFromByteArray(ByteArrayNBT nbt) {
        ByteBuf buffer = Unpooled.buffer(nbt.getByteArray().length);
        buffer.writeBytes(nbt.getByteArray());
        return serializer.unserialize(buffer);
    }
}
