package buffer

import "sync"

const DefaultInitialCapacity = 256

var bufferPool = sync.Pool{
	New: func() any {
		return AllocateUnpooled(DefaultInitialCapacity)
	},
}

func AllocatePooled(initialCapacity uint32) *ByteBuf {
	buf := bufferPool.Get().(*ByteBuf)
	buf.EnsureWritable(initialCapacity)
	return buf
}

func AllocateUnpooled(initialCapacity uint32) *ByteBuf {
	return &ByteBuf{
		data: make([]uint8, initialCapacity),
		len:  initialCapacity,
	}
}
