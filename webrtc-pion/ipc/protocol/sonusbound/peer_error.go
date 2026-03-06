package sonusbound

import (
	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
)

type IpcPeerError struct {
	HandlerId uint32
	Error     string
}

func (msg *IpcPeerError) Decode(*buffer.ByteBuf) (err error) {
	panic("unsupported")
}

func (msg *IpcPeerError) Encode(buf *buffer.ByteBuf) error {
	buf.WriteVarInt(msg.HandlerId)
	buf.WriteUtf8(msg.Error)
	return nil
}

func (msg *IpcPeerError) Handle(*ipc.Handler) error {
	panic("unsupported")
}

func (msg *IpcPeerError) GetHandlerId() uint32 {
	return msg.HandlerId
}
