package sonusbound

import (
	"github.com/pion/webrtc/v4"
	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
)

type IpcPeerOnConnectionStateChange struct {
	HandlerId uint32
	State     webrtc.PeerConnectionState
}

func (msg *IpcPeerOnConnectionStateChange) Decode(*buffer.ByteBuf) (err error) {
	panic("unsupported")
}

func (msg *IpcPeerOnConnectionStateChange) Encode(buf *buffer.ByteBuf) error {
	buf.WriteVarInt(msg.HandlerId)
	buf.WriteVarInt(uint32(msg.State))
	return nil
}

func (msg *IpcPeerOnConnectionStateChange) Handle(*ipc.Handler) error {
	panic("unsupported")
}

func (msg *IpcPeerOnConnectionStateChange) GetHandlerId() uint32 {
	return msg.HandlerId
}
