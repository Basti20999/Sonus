package sonusbound

import (
	"github.com/pion/webrtc/v4"
	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
)

type IpcPeerOnIceConnectionStateChange struct {
	HandlerId uint32
	State     webrtc.ICEConnectionState
}

func (msg *IpcPeerOnIceConnectionStateChange) Decode(*buffer.ByteBuf) (err error) {
	panic("unsupported")
}

func (msg *IpcPeerOnIceConnectionStateChange) Encode(buf *buffer.ByteBuf) error {
	buf.WriteVarInt(msg.HandlerId)
	buf.WriteVarInt(uint32(msg.State))
	return nil
}

func (msg *IpcPeerOnIceConnectionStateChange) Handle(*ipc.Handler) error {
	panic("unsupported")
}

func (msg *IpcPeerOnIceConnectionStateChange) GetHandlerId() uint32 {
	return msg.HandlerId
}
