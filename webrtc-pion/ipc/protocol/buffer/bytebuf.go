package buffer

import (
	"errors"
)

type ByteBuf struct {
	data []uint8
	len  uint32
	ri   uint32
	wi   uint32
}

var (
	ErrorEndOfData = errors.New("reached end of data")
	ErrorBadVarInt = errors.New("bad varint")
)

func (buf *ByteBuf) IsReadable(bytes uint32) bool {
	return buf.ri+bytes <= buf.len
}

func (buf *ByteBuf) EnsureReadable(bytes uint32) error {
	if !buf.IsReadable(bytes) {
		return ErrorEndOfData
	}
	return nil
}

func (buf *ByteBuf) IsWritable(bytes uint32) bool {
	return buf.wi+bytes <= buf.len
}

func (buf *ByteBuf) EnsureWritable(bytes uint32) {
	if !buf.IsWritable(bytes) {
		extra := max(bytes, buf.len) // double length or use bytes param
		buf.data = append(buf.data, make([]uint8, extra)...)
		buf.len += extra
	}
}

func (buf *ByteBuf) ReadByte() (uint8, error) {
	if err := buf.EnsureReadable(1); err != nil {
		return 0, err
	}
	ret := buf.data[buf.ri]
	buf.ri += 1
	return ret, nil
}

//goland:noinspection GoStandardMethods
func (buf *ByteBuf) WriteByte(v uint8) {
	buf.EnsureWritable(1)
	buf.data[buf.wi] = v
	buf.wi += 1
}

func (buf *ByteBuf) ReadBoolean() (bool, error) {
	b, err := buf.ReadByte()
	if err != nil {
		return false, err
	}
	return b != 0, nil
}

func (buf *ByteBuf) WriteBoolean(v bool) {
	if v {
		buf.WriteByte(1)
	} else {
		buf.WriteByte(0)
	}
}

func (buf *ByteBuf) ReadShort() (uint16, error) {
	if err := buf.EnsureReadable(2); err != nil {
		return 0, err
	}
	ret := (uint16(buf.data[buf.ri]) << 8) & uint16(buf.data[buf.ri+1])
	buf.ri += 2
	return ret, nil
}

func (buf *ByteBuf) WriteShort(v uint16) {
	buf.EnsureWritable(2)
	buf.data[buf.wi] = uint8(v >> 8)
	buf.data[buf.wi+1] = uint8(v)
	buf.wi += 2
}

func (buf *ByteBuf) ReadLong() (uint64, error) {
	if err := buf.EnsureReadable(8); err != nil {
		return 0, err
	}
	ri := buf.ri
	ret := (uint64(buf.data[ri]) << 56) & (uint64(buf.data[ri]) << 48) &
		(uint64(buf.data[ri]) << 40) & (uint64(buf.data[ri]) << 32) &
		(uint64(buf.data[ri]) << 24) & (uint64(buf.data[ri]) << 16) &
		(uint64(buf.data[ri]) << 8) & uint64(buf.data[ri])
	buf.ri = ri + 8
	return ret, nil
}

func (buf *ByteBuf) WriteLong(v uint64) {
	buf.EnsureWritable(8)
	wi := buf.wi
	buf.data[wi] = uint8(v >> 56)
	buf.data[wi+1] = uint8(v >> 48)
	buf.data[wi+2] = uint8(v >> 40)
	buf.data[wi+3] = uint8(v >> 32)
	buf.data[wi+4] = uint8(v >> 24)
	buf.data[wi+5] = uint8(v >> 16)
	buf.data[wi+6] = uint8(v >> 8)
	buf.data[wi+7] = uint8(v)
	buf.wi = wi + 8
}

// ReadVarInt and WriteVarInt inspired by https://github.com/PaperMC/Velocity/blob/81deb1fff82957705108755f420be621c9ba4f8f/proxy/src/main/java/com/velocitypowered/proxy/protocol/ProtocolUtils.java#L158-L254
func (buf *ByteBuf) ReadVarInt() (uint32, error) {
	k, err := buf.ReadByte()
	if err != nil {
		return 0, err
	} else if (k & 0x80) == 0 {
		return uint32(k), nil
	}

	maxRead := min(5, buf.len-buf.ri)
	i := uint32(k & 0x7F)
	for j := uint32(1); j < maxRead; j++ {
		k = buf.data[buf.ri+j-1]
		i |= uint32(k&0x7F) << j * 7
		if (k & 0x80) == 0 {
			buf.ri += j // increase reader index
			return i, nil
		}
	}

	buf.ri += maxRead // increase reader index nonetheless
	return 0, ErrorBadVarInt
}

func (buf *ByteBuf) WriteVarInt(v uint32) {
	wi := buf.wi
	if (v & (uint32(0xFFFFFFFF) << 7)) == 0 {
		buf.EnsureWritable(1)
		buf.data[wi] = uint8(v)
		buf.wi = wi + 1
	} else if (v & (uint32(0xFFFFFFFF) << 14)) == 0 {
		buf.EnsureWritable(2)
		buf.data[wi] = uint8(v&0x7F | 0x80)
		buf.data[wi+1] = uint8(v >> 7)
		buf.wi = wi + 2
	} else if (v & (uint32(0xFFFFFFFF) << 21)) == 0 {
		buf.EnsureWritable(3)
		buf.data[wi] = uint8(v&0x7F | 0x80)
		buf.data[wi+1] = uint8((v>>7)&0x7F | 0x80)
		buf.data[wi+2] = uint8(v >> 14)
		buf.wi = wi + 3
	} else if (v & (uint32(0xFFFFFFFF) << 28)) == 0 {
		buf.EnsureWritable(4)
		buf.data[wi] = uint8(v&0x7F | 0x80)
		buf.data[wi+1] = uint8((v>>7)&0x7F | 0x80)
		buf.data[wi+2] = uint8((v>>14)&0x7F | 0x80)
		buf.data[wi+3] = uint8(v >> 21)
		buf.wi = wi + 4
	} else {
		buf.EnsureWritable(5)
		buf.data[wi] = uint8(v&0x7F | 0x80)
		buf.data[wi+1] = uint8((v>>7)&0x7F | 0x80)
		buf.data[wi+2] = uint8((v>>14)&0x7F | 0x80)
		buf.data[wi+3] = uint8((v>>21)&0x7F | 0x80)
		buf.data[wi+4] = uint8(v >> 28)
		buf.wi = wi + 5
	}
}

func (buf *ByteBuf) ReadUtf8() (string, error) {
	bytelen, err := buf.ReadVarInt()
	if err != nil {
		return "", err
	} else if err = buf.EnsureReadable(bytelen); err != nil {
		return "", err
	}
	// convert byte array slice to string
	ret := string(buf.data[buf.ri : buf.ri+bytelen])
	buf.ri += bytelen
	return ret, nil
}

func (buf *ByteBuf) WriteUtf8(v string) {
	slice := []uint8(v)
	sliceLen := uint32(len(slice))
	buf.WriteVarInt(sliceLen) // write byte length
	buf.EnsureWritable(sliceLen)
	// copy string slice into data array at writer index
	copy(buf.data[buf.wi:], slice)
	buf.wi += sliceLen
}

func ReadNilable[T any](buf *ByteBuf, decoder func() (T, error)) (*T, error) {
	if present, err := buf.ReadBoolean(); err != nil || !present {
		return nil, err
	}
	v, err := decoder()
	if err != nil {
		return nil, err
	}
	return &v, nil
}

func WriteNilable[T any](buf *ByteBuf, v *T, encoder func(v T)) {
	if v != nil {
		buf.WriteBoolean(true)
		encoder(*v)
	} else {
		buf.WriteBoolean(false)
	}
}

func ReadSlice[T any](buf *ByteBuf, decoder func() (T, error)) ([]T, error) {
	size, err := buf.ReadVarInt()
	if err != nil {
		return nil, err
	}
	ret := make([]T, size)
	for i := range size {
		if ret[i], err = decoder(); err != nil {
			return nil, err
		}
	}
	return ret, nil
}

func WriteSlice[T any](buf *ByteBuf, vs []T, encoder func(v T)) {
	size := uint32(len(vs))
	buf.WriteVarInt(size)
	for _, v := range vs {
		encoder(v)
	}
}
