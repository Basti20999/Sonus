package buffer

import (
	"errors"
	"fmt"
	"io"
)

type ByteBuf struct {
	data     []uint8
	len      uint32
	ri       uint32
	wi       uint32
	refCount uint16
}

var (
	ErrorEndOfData = errors.New("reached end of data")
	ErrorBadVarInt = errors.New("bad varint")
)

func (buf *ByteBuf) GetReaderIndex() uint32 {
	return buf.ri
}

func (buf *ByteBuf) SetReaderIndex(ri uint32) error {
	if ri > buf.wi {
		return fmt.Errorf("can't move ri after wi: %d > %d (limit %d)", ri, buf.wi, buf.len)
	}
	buf.ri = ri
	return nil
}

func (buf *ByteBuf) ReadableBytes() uint32 {
	return buf.wi - buf.ri
}

func (buf *ByteBuf) Array() []uint8 {
	return append([]uint8{}, buf.UnsafeArraySlice()...)
}

func (buf *ByteBuf) UnsafeArraySlice() []uint8 {
	return buf.data[buf.ri:buf.wi]
}

func (buf *ByteBuf) ReadFrom(reader io.Reader) error {
	n, err := reader.Read(buf.data[buf.wi:buf.len])
	buf.wi += uint32(n)
	return err
}

func (buf *ByteBuf) Clear() {
	buf.ri = 0
	buf.wi = 0
	// don't need to clear data, can't be read anyway
}

func (buf *ByteBuf) Retain() *ByteBuf {
	if buf.refCount >= 1 {
		buf.refCount++
	}
	return buf
}

func (buf *ByteBuf) Release() *ByteBuf {
	if buf.refCount == 1 {
		buf.refCount = 0
		buf.Clear()
		bufferPool.Put(buf)
	} else if buf.refCount > 1 {
		buf.refCount--
	}
	return buf
}

func (buf *ByteBuf) IsReadable(bytes uint32) bool {
	return buf.ri+bytes <= buf.wi
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

	maxRead := min(5, buf.wi-buf.ri)
	i := uint32(k & 0x7F)
	for j := uint32(1); j < maxRead; j++ {
		k = buf.data[buf.ri]
		buf.ri++
		i |= uint32(k&0x7F) << (j * 7)
		if (k & 0x80) == 0 {
			return i, nil
		}
	}

	return 0, ErrorBadVarInt
}

func (buf *ByteBuf) WriteVarInt(v uint32) {
	wi := buf.wi
	if (v & 0xFFFFFF80) == 0 {
		buf.EnsureWritable(1)
		buf.data[wi] = uint8(v)
		buf.wi = wi + 1
	} else if (v & 0xFFFFC000) == 0 {
		buf.EnsureWritable(2)
		buf.data[wi] = uint8(v&0x7F | 0x80)
		buf.data[wi+1] = uint8(v >> 7)
		buf.wi = wi + 2
	} else if (v & 0xFFE00000) == 0 {
		buf.EnsureWritable(3)
		buf.data[wi] = uint8(v&0x7F | 0x80)
		buf.data[wi+1] = uint8((v>>7)&0x7F | 0x80)
		buf.data[wi+2] = uint8(v >> 14)
		buf.wi = wi + 3
	} else if (v & 0xF0000000) == 0 {
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
	// doesn't matter that we don't copy the memory, the reference
	// gets lost when converting to string
	arr, err := buf.ReadUnsafeByteArray()
	if err != nil {
		return "", err
	}
	return string(arr), nil
}

func (buf *ByteBuf) WriteUtf8(v string) {
	buf.WriteByteArray([]uint8(v))
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
	buf.WriteVarInt(uint32(len(vs)))
	for _, v := range vs {
		encoder(v)
	}
}

// ReadByteArray copies the return value to prevent errors caused by memory being re-used
func (buf *ByteBuf) ReadByteArray() ([]uint8, error) {
	arr, err := buf.ReadUnsafeByteArray()
	if err != nil {
		return nil, err
	}
	return append([]uint8{}, arr...), nil
}

func (buf *ByteBuf) ReadUnsafeByteArray() ([]uint8, error) {
	size, err := buf.ReadVarInt()
	if err != nil {
		return nil, err
	}
	return buf.ReadUnsafeByteArrayWithLength(size)
}

func (buf *ByteBuf) ReadUnsafeByteArrayWithLength(size uint32) ([]uint8, error) {
	if err := buf.EnsureReadable(size); err != nil {
		return nil, err
	}
	ret := buf.data[buf.ri : buf.ri+size]
	buf.ri += size
	return ret, nil
}

// ReadUnsafeSlice returns a slice of this buffer, with separate ri + wi; note
// that this still respects the refcount of the "parent" and is not safe against modifications
// of the "parent" buffer
func (buf *ByteBuf) ReadUnsafeSlice(size uint32) (*ByteBuf, error) {
	data, err := buf.ReadUnsafeByteArrayWithLength(size)
	if err != nil {
		return nil, err
	}
	return &ByteBuf{
		data: data,
		len:  size,
	}, nil
}

func (buf *ByteBuf) WriteByteArray(vs []uint8) {
	arrLen := uint32(len(vs))
	buf.WriteVarInt(arrLen) // write byte length
	buf.EnsureWritable(arrLen)
	// copy given array into data array at writer index
	copy(buf.data[buf.wi:], vs)
	buf.wi += arrLen
}

func (buf *ByteBuf) WriteBytes(vs *ByteBuf) {
	wlen := vs.wi - vs.ri
	buf.EnsureWritable(wlen)
	copy(buf.data[buf.wi:], vs.data[buf.ri:buf.wi])
	buf.wi += wlen
}

func (buf *ByteBuf) DiscardSomeReadBytes(threshold uint32) {
	ri := buf.ri
	if ri < threshold {
		return // don't discard, not enough read
	}
	// adjust indices
	buf.len -= ri
	buf.wi -= ri
	buf.ri = 0
	// resize data array to start at reader index without copying
	buf.data = buf.data[ri:]
}
