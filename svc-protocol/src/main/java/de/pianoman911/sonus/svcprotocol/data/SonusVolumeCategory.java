package de.pianoman911.sonus.svcprotocol.data;

import com.google.gson.JsonObject;
import dev.minceraft.sonus.common.protocol.util.DataTypeUtil;
import dev.minceraft.sonus.common.protocol.util.Utf8String;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.jspecify.annotations.Nullable;

public class SonusVolumeCategory {

    private final String id;
    private final String name;
    private final @Nullable String description;
    private final int @Nullable [][] icon;

    public SonusVolumeCategory(
            String id, String name,
            @Nullable String description,
            int @Nullable [][] icon
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
    }

    public SonusVolumeCategory(ByteBuf buf) {
        this.id = Utf8String.read(buf, 16);
        this.name = Utf8String.read(buf, 16);
        this.description = DataTypeUtil.readIf(buf, Utf8String::read);
        this.icon = DataTypeUtil.readIf(buf, SonusVolumeCategory::readIconBytes);
    }

    public SonusVolumeCategory(JsonObject json) {
        this.id = json.get("id").getAsString();
        this.name = json.get("name").getAsString();
        this.description = json.has("description") ?
                json.get("description").getAsString() : null;
        if (json.has("icon")) {
            byte[] iconBytes = ByteBufUtil.decodeHexDump(json.get("icon").getAsString());
            this.icon = readIconBytes(Unpooled.wrappedBuffer(iconBytes));
        } else {
            this.icon = null;
        }
    }

    private static int[][] readIconBytes(ByteBuf buf) {
        int[][] icon = new int[16][16];
        for (int x = 0; x < icon.length; x++) {
            for (int y = 0; y < icon.length; y++) {
                icon[x][y] = buf.readInt();
            }
        }
        return icon;
    }

    private static void writeIconBytes(ByteBuf buf, int[][] icon) {
        if (icon.length != 16) {
            throw new IllegalStateException("Icon is not 16x16");
        }
        for (int[] ints : icon) {
            for (int y = 0; y < icon.length; y++) {
                buf.writeInt(ints[y]);
            }
        }
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public @Nullable String getDescription() {
        return this.description;
    }

    public int[] @Nullable [] getIcon() {
        return this.icon;
    }

    public void encode(ByteBuf buf) {
        Utf8String.write(buf, this.id);
        Utf8String.write(buf, this.name);
        DataTypeUtil.writeNullable(buf, this.description, Utf8String::write);
        DataTypeUtil.writeNullableIf(buf, this.icon, i -> i[0] != null, SonusVolumeCategory::writeIconBytes);
    }

    public void encode(JsonObject json) {
        json.addProperty("id", this.id);
        json.addProperty("name", this.name);
        if (this.description != null) {
            json.addProperty("description", this.description);
        }
        if (this.icon != null && this.icon[0] != null) {
            ByteBuf buf = Unpooled.buffer(16 * 16 * Integer.BYTES);
            try {
                writeIconBytes(buf, this.icon);
                json.addProperty("icon", ByteBufUtil.hexDump(buf));
            } finally {
                buf.release();
            }
        }
    }
}
