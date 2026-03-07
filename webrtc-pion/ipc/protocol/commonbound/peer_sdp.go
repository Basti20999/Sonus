package commonbound

import (
	"minceraft.dev/sonus/webrtc-pion/ipc"
	"minceraft.dev/sonus/webrtc-pion/ipc/protocol/buffer"
)

type IpcPeerSdp struct {
	HandlerId uint32
	Sdp       string
}

func (msg *IpcPeerSdp) GetSonusboundId() uint8 {
	return 0x01
}

func (msg *IpcPeerSdp) GetPionboundId() uint8 {
	return 0x01
}

func (msg *IpcPeerSdp) Decode(buf *buffer.ByteBuf) (err error) {
	if msg.HandlerId, err = buf.ReadVarInt(); err != nil {
		return
	}
	msg.Sdp, err = buf.ReadUtf8()
	return
}

func (msg *IpcPeerSdp) Encode(buf *buffer.ByteBuf) error {
	buf.WriteVarInt(msg.HandlerId)
	buf.WriteUtf8(msg.Sdp)
	return nil
}

func (msg *IpcPeerSdp) Handle(handler *ipc.Handler) error {
	answerSdp, err := handler.Peer.HandleOffer(msg.Sdp)
	if err != nil {
		return err
	}
	return handler.Socket.Send(&IpcPeerSdp{
		HandlerId: msg.HandlerId,
		Sdp:       answerSdp,
	})
}

func (msg *IpcPeerSdp) GetHandlerId() uint32 {
	return msg.HandlerId
}
