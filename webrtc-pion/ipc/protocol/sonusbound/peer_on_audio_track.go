package sonusbound

import (
	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
)

type AudioTrackType uint8

const (
	AudioTrackTypeLocal  = 0
	AudioTrackTypeRemote = 1
)

type IpcPeerOnAudioTrack struct {
	HandlerId  uint32
	Type       AudioTrackType
	TrackId    uint32
	SampleRate uint32
	Channels   uint16
}

func (msg *IpcPeerOnAudioTrack) GetSonusboundId() uint8 {
	return 0x03
}

func (msg *IpcPeerOnAudioTrack) GetPionboundId() uint8 {
	return 0xFF
}

func (msg *IpcPeerOnAudioTrack) Decode(*buffer.ByteBuf) (err error) {
	panic("unsupported")
}

func (msg *IpcPeerOnAudioTrack) Encode(buf *buffer.ByteBuf) error {
	buf.WriteVarInt(msg.HandlerId)
	buf.WriteVarInt(uint32(msg.Type))
	buf.WriteVarInt(msg.TrackId)
	buf.WriteVarInt(msg.SampleRate)
	buf.WriteShort(msg.Channels)
	return nil
}

func (msg *IpcPeerOnAudioTrack) Handle(*ipc.Handler) error {
	panic("unsupported")
}

func (msg *IpcPeerOnAudioTrack) GetHandlerId() uint32 {
	return msg.HandlerId
}
