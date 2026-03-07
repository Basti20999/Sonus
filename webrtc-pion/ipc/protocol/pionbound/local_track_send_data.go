package pionbound

import (
	"time"

	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
)

type IpcLocalTrackSendData struct {
	HandlerId uint32
	TrackId   uint32
	Data      []byte
	Duration  time.Duration
}

func (msg *IpcLocalTrackSendData) GetSonusboundId() uint8 {
	return 0xFF
}

func (msg *IpcLocalTrackSendData) GetPionboundId() uint8 {
	return 0x03
}

func (msg *IpcLocalTrackSendData) Decode(buf *buffer.ByteBuf) (err error) {
	var durationNanos uint64
	if msg.HandlerId, err = buf.ReadVarInt(); err != nil {
		return
	} else if msg.TrackId, err = buf.ReadVarInt(); err != nil {
		return
	} else if msg.Data, err = buf.ReadByteArray(); err != nil {
		return
	} else if durationNanos, err = buf.ReadLong(); err != nil {
		return
	}
	msg.Duration = time.Duration(durationNanos)
	return
}

func (msg *IpcLocalTrackSendData) Encode(*buffer.ByteBuf) error {
	panic("unsupported")
}

func (msg *IpcLocalTrackSendData) Handle(handler *ipc.Handler) error {
	return handler.SendLocalAudio(msg.TrackId, msg.Data, msg.Duration)
}

func (msg *IpcLocalTrackSendData) GetHandlerId() uint32 {
	return msg.HandlerId
}
