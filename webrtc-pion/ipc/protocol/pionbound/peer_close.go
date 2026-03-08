package pionbound

import (
	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
)

type IpcPeerClose struct {
	HandlerId uint32
}

func (msg *IpcPeerClose) GetSonusboundId() uint8 {
	return 0xFF
}

func (msg *IpcPeerClose) GetPionboundId() uint8 {
	return 0x04
}

func (msg *IpcPeerClose) Decode(buf *buffer.ByteBuf) (err error) {
	msg.HandlerId, err = buf.ReadVarInt()
	return
}

func (msg *IpcPeerClose) Encode(*buffer.ByteBuf) error {
	panic("unsupported")
}

func (msg *IpcPeerClose) Handle(handler *ipc.Handler) error {
	return handler.Close()
}

func (msg *IpcPeerClose) GetHandlerId() uint32 {
	return msg.HandlerId
}
