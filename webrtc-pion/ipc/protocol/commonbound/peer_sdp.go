package commonbound

import (
	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
)

type IcpPeerSdp struct {
	HandlerId uint32
	Sdp       string
}

func (msg *IcpPeerSdp) Decode(buf *buffer.ByteBuf) (err error) {
	if msg.HandlerId, err = buf.ReadVarInt(); err != nil {
		return
	}
	msg.Sdp, err = buf.ReadUtf8()
	return
}

func (msg *IcpPeerSdp) Encode(buf *buffer.ByteBuf) error {
	buf.WriteVarInt(msg.HandlerId)
	buf.WriteUtf8(msg.Sdp)
	return nil
}

func (msg *IcpPeerSdp) Handle(handler *ipc.Handler) error {
	answerSdp, err := handler.Peer.HandleOffer(msg.Sdp)
	if err != nil {
		return err
	}
	return handler.Socket.Send(&IcpPeerSdp{
		HandlerId: msg.HandlerId,
		Sdp:       answerSdp,
	})
}

func (msg *IcpPeerSdp) GetHandlerId() uint32 {
	return msg.HandlerId
}
