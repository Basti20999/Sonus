package commonbound

import (
	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
)

type IcpPeerAddIceCandidate struct {
	HandlerId     uint32
	Candidate     string
	SdpMid        *string
	SdpMLineIndex *uint16
}

func (msg *IcpPeerAddIceCandidate) GetSonusboundId() uint8 {
	return 0x00
}

func (msg *IcpPeerAddIceCandidate) GetPionboundId() uint8 {
	return 0x00
}

func (msg *IcpPeerAddIceCandidate) Decode(buf *buffer.ByteBuf) (err error) {
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

func (msg *IcpPeerAddIceCandidate) Encode(buf *buffer.ByteBuf) error {
	buf.WriteVarInt(msg.HandlerId)
	buf.WriteUtf8(msg.Candidate)
	buffer.WriteNilable(buf, msg.SdpMid, buf.WriteUtf8)
	buffer.WriteNilable(buf, msg.SdpMLineIndex, buf.WriteShort)
	return nil
}

func (msg *IcpPeerAddIceCandidate) Handle(handler *ipc.Handler) error {
	return handler.Peer.AddIceCandidate(msg.Candidate, msg.SdpMid, msg.SdpMLineIndex)
}

func (msg *IcpPeerAddIceCandidate) GetHandlerId() uint32 {
	return msg.HandlerId
}
