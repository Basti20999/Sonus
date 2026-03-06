package pionbound

import (
	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
)

type IpcPeerClose struct {
	HandlerId uint32
}

func (msg *IpcPeerClose) Decode(buf *buffer.ByteBuf) (err error) {
	msg.HandlerId, err = buf.ReadVarInt()
	return
}

func (msg *IpcPeerClose) Encode(*buffer.ByteBuf) error {
	panic("unsupported")
}

func (msg *IpcPeerClose) Handle(handler *ipc.Handler) error {
	err := handler.Peer.Close()
	delete(handler.Socket.Handlers, msg.HandlerId) // remove nonetheless
	return err
}

func (msg *IpcPeerClose) GetHandlerId() uint32 {
	return msg.HandlerId
}
