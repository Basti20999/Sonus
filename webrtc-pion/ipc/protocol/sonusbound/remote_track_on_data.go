package sonusbound

import (
	"time"

	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
)

type IpcRemoteTrackOnData struct {
	HandlerId uint32
	TrackId   uint32
	Data      []byte
	Duration  time.Duration
}

func (msg *IpcRemoteTrackOnData) Decode(*buffer.ByteBuf) (err error) {
	panic("unsupported")
}

func (msg *IpcRemoteTrackOnData) Encode(buf *buffer.ByteBuf) error {
	buf.WriteVarInt(msg.HandlerId)
	buf.WriteVarInt(msg.TrackId)
	buf.WriteByteArray(msg.Data)
	buf.WriteLong(uint64(msg.Duration))
	return nil
}

func (msg *IpcRemoteTrackOnData) Handle(*ipc.Handler) error {
	panic("unsupported")
}

func (msg *IpcRemoteTrackOnData) GetHandlerId() uint32 {
	return msg.HandlerId
}
