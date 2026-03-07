package commonbound

import (
	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
)

type IpcPeerAddIceCandidate struct {
	HandlerId     uint32
	Candidate     string
	SdpMid        *string
	SdpMLineIndex *uint16
}

func (msg *IpcPeerAddIceCandidate) GetSonusboundId() uint8 {
	return 0x00
}

func (msg *IpcPeerAddIceCandidate) GetPionboundId() uint8 {
	return 0x00
}

func (msg *IpcPeerAddIceCandidate) Decode(buf *buffer.ByteBuf) (err error) {
	if msg.HandlerId, err = buf.ReadVarInt(); err != nil {
		return
	} else if msg.Candidate, err = buf.ReadUtf8(); err != nil {
		return
	} else if msg.SdpMid, err = buffer.ReadNilable(buf, buf.ReadUtf8); err != nil {
		return
	}
	msg.SdpMLineIndex, err = buffer.ReadNilable(buf, buf.ReadShort)
	return
}

func (msg *IpcPeerAddIceCandidate) Encode(buf *buffer.ByteBuf) error {
	buf.WriteVarInt(msg.HandlerId)
	buf.WriteUtf8(msg.Candidate)
	buffer.WriteNilable(buf, msg.SdpMid, buf.WriteUtf8)
	buffer.WriteNilable(buf, msg.SdpMLineIndex, buf.WriteShort)
	return nil
}

func (msg *IpcPeerAddIceCandidate) Handle(handler *ipc.Handler) error {
	return handler.Peer.AddIceCandidate(msg.Candidate, msg.SdpMid, msg.SdpMLineIndex)
}

func (msg *IpcPeerAddIceCandidate) GetHandlerId() uint32 {
	return msg.HandlerId
}
